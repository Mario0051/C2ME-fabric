package com.ishland.c2me.common.optimization.worldgen.global_biome_cache;

import net.minecraft.util.registry.Registry;
import com.mojang.serialization.Codec;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;

import java.util.List;

public class UncachedBiomeSource extends BiomeSource {
    private final BiomeCache.BiomeProvider sampler;

    public UncachedBiomeSource(List<Biome> biomes, BiomeCache.BiomeProvider sampler) {
        super(biomes);
        this.sampler = sampler;
    }

    @Override
    protected Codec<? extends BiomeSource> method_28442() {
        return VanillaLayeredBiomeSource.CODEC;
    }

    @Override
    public BiomeSource withSeed(long seed) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
        return sampler.sample(biomeX, biomeY, biomeZ);
    }
}
