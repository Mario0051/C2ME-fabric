package com.ishland.c2me.mixin.fixes.worldgen.threading;

import com.ishland.c2me.common.fixes.worldgen.threading.ThreadLocalChunkRandom;
import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Mixin(StructureStart.class)
public class MixinStructureStart<C extends FeatureConfig> {

    @Mutable
    @Shadow
    @Final
    protected ChunkRandom random;

    @Mutable
    @Shadow @Final protected List<StructurePiece> children;
    private final AtomicInteger referencesAtomic = new AtomicInteger();

    @Inject(method = "<init>", at = @At("RETURN"))
    private void postInit(StructureFeature<C> feature, int chunkX, int chunkZ, BlockBox box, int references, long seed, CallbackInfo ci) {
        this.random = new ThreadLocalChunkRandom(seed,
                chunkRandom -> chunkRandom.setCarverSeed(seed, chunkX, chunkZ) // TODO [VanillaCopy]
        );
        this.children = Collections.synchronizedList(children);
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/StructureStart;references:I", opcode = Opcodes.GETFIELD))
    private int redirectGetReferences(StructureStart<?> structureStart) {
        return referencesAtomic.get();
    }

    /**
     * @author ishland
     * @reason atomic operation
     */
    @Overwrite
    public void incrementReferences() {
        this.referencesAtomic.incrementAndGet();
    }

}
