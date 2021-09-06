package org.yatopiamc.c2me.mixin.optimization.worldgen.global_biome_cache;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.biome.source.BiomeLayerSampler;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.VanillaLayeredBiomeSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yatopiamc.c2me.common.optimization.worldgen.global_biome_cache.BiomeCache;
import org.yatopiamc.c2me.common.optimization.worldgen.global_biome_cache.IVanillaLayeredBiomeSource;

import java.util.List;

@Mixin(VanillaLayeredBiomeSource.class)
public abstract class MixinVanillaLayeredBiomeSource extends BiomeSource implements IVanillaLayeredBiomeSource {

    @Shadow @Final private BiomeLayerSampler biomeSampler;

    protected MixinVanillaLayeredBiomeSource(List<Biome> biomes) {
        super(biomes);
    }

    private BiomeCache cacheImpl = null;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.cacheImpl = new BiomeCache(biomeSampler, biomes);
    }

    /**
     * @author ishland
     * @reason re-implement caching
     */
    @Overwrite
    public Biome getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
        return this.cacheImpl.getBiomeForNoiseGen(biomeX, biomeY, biomeZ);
    }

    @Override
    public BiomeArray preloadBiomes(ChunkPos pos, BiomeArray def) {
        return cacheImpl.preloadBiomes(pos, def);
    }

    @Override
    public BiomeArray getBiomes(ChunkPos pos) {
        return cacheImpl.preloadBiomes(pos, null);
    }
}
