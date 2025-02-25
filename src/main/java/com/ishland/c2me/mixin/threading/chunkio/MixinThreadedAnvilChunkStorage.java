package com.ishland.c2me.mixin.threading.chunkio;

import com.ibm.asyncutil.locks.AsyncNamedLock;
import com.ishland.c2me.common.GlobalExecutors;
import com.ishland.c2me.common.threading.chunkio.AsyncSerializationManager;
import com.ishland.c2me.common.threading.chunkio.ChunkIoMainThreadTaskUtils;
import com.ishland.c2me.common.threading.chunkio.ISerializingRegionBasedStorage;
import com.ishland.c2me.common.util.SneakyThrow;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Either;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.structure.StructureManager;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class MixinThreadedAnvilChunkStorage extends VersionedChunkStorage implements ChunkHolder.PlayersWatchingChunkProvider {

    public MixinThreadedAnvilChunkStorage(File file, DataFixer dataFixer, boolean bl) {
        super(file, dataFixer, bl);
    }

    @Shadow
    @Final
    private ServerWorld world;

    @Shadow
    @Final
    private StructureManager structureManager;

    @Shadow
    @Final
    private PointOfInterestStorage pointOfInterestStorage;

    @Shadow
    protected abstract byte method_27053(ChunkPos chunkPos, ChunkStatus.ChunkType chunkType);

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    protected abstract void method_27054(ChunkPos chunkPos);

    @Shadow
    @Final
    private Supplier<PersistentStateManager> persistentStateManagerFactory;

    @Shadow
    @Final
    private ThreadExecutor<Runnable> mainThreadExecutor;

    @Shadow
    protected abstract boolean method_27055(ChunkPos chunkPos);

    private AsyncNamedLock<ChunkPos> chunkLock = AsyncNamedLock.createFair();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        chunkLock = AsyncNamedLock.createFair();
    }

    private Set<ChunkPos> scheduledChunks = new HashSet<>();

    private ConcurrentLinkedQueue<CompletableFuture<Void>> saveFutures = new ConcurrentLinkedQueue<>();

    @Dynamic
    @Redirect(method = "method_18843", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;save(Lnet/minecraft/world/chunk/Chunk;)Z"))
    // method: consumer in tryUnloadChunk
    private boolean asyncSave(ThreadedAnvilChunkStorage tacs, Chunk chunk) {
        // TODO [VanillaCopy] - check when updating minecraft version
        this.pointOfInterestStorage.method_20436(chunk.getPos());
        if (!chunk.needsSaving()) {
            return false;
        } else {
            chunk.setShouldSave(false);
            ChunkPos chunkPos = chunk.getPos();

            try {
                ChunkStatus chunkStatus = chunk.getStatus();
                if (chunkStatus.getChunkType() != ChunkStatus.ChunkType.field_12807) {
                    if (this.method_27055(chunkPos)) {
                        return false;
                    }

                    if (chunkStatus == ChunkStatus.EMPTY && chunk.getStructureStarts().values().stream().noneMatch(StructureStart::hasChildren)) {
                        return false;
                    }
                }

                this.world.getProfiler().visit("chunkSave");
                // C2ME start - async serialization
                if (saveFutures == null) saveFutures = new ConcurrentLinkedQueue<>();
                AsyncSerializationManager.Scope scope = new AsyncSerializationManager.Scope(chunk, world);

                saveFutures.add(chunkLock.acquireLock(chunk.getPos()).toCompletableFuture().thenCompose(lockToken ->
                        CompletableFuture.supplyAsync(() -> {
                                    scope.open();
                                    AsyncSerializationManager.push(scope);
                                    try {
                                        return ChunkSerializer.serialize(this.world, chunk);
                                    } finally {
                                        AsyncSerializationManager.pop(scope);
                                    }
                                }, GlobalExecutors.executor)
                                .thenAccept(compoundTag -> this.setTagAt(chunkPos, compoundTag))
                                .handle((unused, throwable) -> {
                                    lockToken.releaseLock();
                                    if (throwable != null)
                                        LOGGER.error("Failed to save chunk {},{}", chunkPos.x, chunkPos.z, throwable);
                                    return unused;
                                })));
                this.method_27053(chunkPos, chunkStatus.getChunkType());
                // C2ME end
                return true;
            } catch (Exception var5) {
                LOGGER.error("Failed to save chunk {},{}", chunkPos.x, chunkPos.z, var5);
                return false;
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo info) {
        GlobalExecutors.executor.execute(() -> saveFutures.removeIf(CompletableFuture::isDone));
    }

    @Override
    public void completeAll() {
        final CompletableFuture<Void> future = CompletableFuture.allOf(saveFutures.toArray(new CompletableFuture[0]));
        this.mainThreadExecutor.runTasks(future::isDone); // wait for serialization to complete
        super.completeAll();
    }
}
