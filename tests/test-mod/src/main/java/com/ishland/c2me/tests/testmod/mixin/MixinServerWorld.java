package com.ishland.c2me.tests.testmod.mixin;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.ScheduledTick;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld extends World implements ServerWorldAccess {

    protected MixinServerWorld(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryKey<DimensionType> registryRef2, DimensionType dimensionType, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed) {
        super(properties, registryRef, registryRef2, dimensionType, profiler, isClient, debugWorld, seed);
    }

    /**
     * @author ishland
     * @reason no ticking
     */
    @Overwrite
    private void tickFluid(ScheduledTick<Fluid> tick) {
        // nope
    }

    /**
     * @author ishland
     * @reason no ticking
     */
    @Overwrite
    private void tickBlock(ScheduledTick<Block> tick) {
        // nope
    }

}
