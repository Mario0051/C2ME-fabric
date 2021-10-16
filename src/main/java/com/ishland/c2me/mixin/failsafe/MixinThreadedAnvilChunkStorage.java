package com.ishland.c2me.mixin.failsafe;

import com.mojang.datafixers.util.Either;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.world.chunk.Chunk;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(ThreadedAnvilChunkStorage.class)
public class MixinThreadedAnvilChunkStorage {

    @Shadow
    @Final
    private LongSet loadedChunks;

    @Shadow @Final private static Logger LOGGER;

    @Dynamic
    @Inject(method = "method_17227", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/WorldChunk;loadToWorld()V"), cancellable = false)
    // lambda expression in convertToFullChunk
    private void afterLoadToWorld(ChunkHolder chunkHolder, Chunk protoChunk, CallbackInfoReturnable<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> cir) {
        if (this.loadedChunks.contains(chunkHolder.getPos().toLong()))
            LOGGER.error("Double scheduling chunk loading detected on chunk {}", chunkHolder.getPos());
    }

}
