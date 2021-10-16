package com.ishland.c2me.common.fixes.worldgen.threading;

import net.minecraft.world.gen.ChunkRandom;

import java.util.function.Consumer;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class ThreadLocalChunkRandom extends ChunkRandom {

    private final ThreadLocal<ChunkRandom> chunkRandomThreadLocal;

    @SuppressWarnings("unused")
    public ThreadLocalChunkRandom() { // called by asm generated code
        this(System.nanoTime());
    }

    public ThreadLocalChunkRandom(long seed) {
        this(seed, chunkRandom -> {});
    }

    public ThreadLocalChunkRandom(long seed, Consumer<ChunkRandom> preHook) {
        this.chunkRandomThreadLocal = ThreadLocal.withInitial(() -> {
            final ChunkRandom chunkRandom = new ChunkRandom(seed);
            preHook.accept(chunkRandom);
            return chunkRandom;
        });
    }

    @Override
    public long setTerrainSeed(int chunkX, int chunkZ) {
        return chunkRandomThreadLocal.get().setTerrainSeed(chunkX, chunkZ);
    }

    @Override
    public long setPopulationSeed(long worldSeed, int blockX, int blockZ) {
        return chunkRandomThreadLocal.get().setPopulationSeed(worldSeed, blockX, blockZ);
    }

    @Override
    public long setDecoratorSeed(long populationSeed, int index, int step) {
        return chunkRandomThreadLocal.get().setDecoratorSeed(populationSeed, index, step);
    }

    @Override
    public long setCarverSeed(long worldSeed, int chunkX, int chunkZ) {
        return chunkRandomThreadLocal.get().setCarverSeed(worldSeed, chunkX, chunkZ);
    }

    @Override
    public long setRegionSeed(long worldSeed, int regionX, int regionZ, int salt) {
        return chunkRandomThreadLocal.get().setRegionSeed(worldSeed, regionX, regionZ, salt);
    }

    @Override
    public void setSeed(long seed) {
        if (chunkRandomThreadLocal == null) return; // Special case when doing <init>
        chunkRandomThreadLocal.get().setSeed(seed);
    }

    @Override
    public void nextBytes(byte[] bytes) {
        chunkRandomThreadLocal.get().nextBytes(bytes);
    }

    @Override
    public int nextInt() {
        return chunkRandomThreadLocal.get().nextInt();
    }

    @Override
    public int nextInt(int bound) {
        return chunkRandomThreadLocal.get().nextInt(bound);
    }

    @Override
    public long nextLong() {
        return chunkRandomThreadLocal.get().nextLong();
    }

    @Override
    public boolean nextBoolean() {
        return chunkRandomThreadLocal.get().nextBoolean();
    }

    @Override
    public float nextFloat() {
        return chunkRandomThreadLocal.get().nextFloat();
    }

    @Override
    public double nextDouble() {
        return chunkRandomThreadLocal.get().nextDouble();
    }

    @Override
    public double nextGaussian() {
        return chunkRandomThreadLocal.get().nextGaussian();
    }

    @Override
    public IntStream ints(long streamSize) {
        return chunkRandomThreadLocal.get().ints(streamSize);
    }

    @Override
    public IntStream ints() {
        return chunkRandomThreadLocal.get().ints();
    }

    @Override
    public IntStream ints(long streamSize, int randomNumberOrigin, int randomNumberBound) {
        return chunkRandomThreadLocal.get().ints(streamSize, randomNumberOrigin, randomNumberBound);
    }

    @Override
    public IntStream ints(int randomNumberOrigin, int randomNumberBound) {
        return chunkRandomThreadLocal.get().ints(randomNumberOrigin, randomNumberBound);
    }

    @Override
    public LongStream longs(long streamSize) {
        return chunkRandomThreadLocal.get().longs(streamSize);
    }

    @Override
    public LongStream longs() {
        return chunkRandomThreadLocal.get().longs();
    }

    @Override
    public LongStream longs(long streamSize, long randomNumberOrigin, long randomNumberBound) {
        return chunkRandomThreadLocal.get().longs(streamSize, randomNumberOrigin, randomNumberBound);
    }

    @Override
    public LongStream longs(long randomNumberOrigin, long randomNumberBound) {
        return chunkRandomThreadLocal.get().longs(randomNumberOrigin, randomNumberBound);
    }

    @Override
    public DoubleStream doubles(long streamSize) {
        return chunkRandomThreadLocal.get().doubles(streamSize);
    }

    @Override
    public DoubleStream doubles() {
        return chunkRandomThreadLocal.get().doubles();
    }

    @Override
    public DoubleStream doubles(long streamSize, double randomNumberOrigin, double randomNumberBound) {
        return chunkRandomThreadLocal.get().doubles(streamSize, randomNumberOrigin, randomNumberBound);
    }

    @Override
    public DoubleStream doubles(double randomNumberOrigin, double randomNumberBound) {
        return chunkRandomThreadLocal.get().doubles(randomNumberOrigin, randomNumberBound);
    }

    @Override
    public void consume(int count) {
        chunkRandomThreadLocal.get().consume(count);
    }

    @Override
    public boolean equals(Object obj) {
        return chunkRandomThreadLocal.get().equals(obj);
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    @Override
    public String toString() {
        return chunkRandomThreadLocal.get().toString();
    }
}
