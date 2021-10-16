package com.ishland.c2me;

import com.ibm.asyncutil.util.Combinators;
import com.ishland.c2me.common.config.C2MEConfig;
import net.fabricmc.api.ModInitializer;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.storage.ChunkStreamVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class C2MEMod implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("C2ME");

    @Override
    public void onInitialize() {
        if (Boolean.getBoolean("com.ishland.c2me.mixin.doAudit")) {
            MixinEnvironment.getCurrentEnvironment().audit();
        }
        if (Boolean.getBoolean("com.ishland.c2me.runCompressionBenchmark")) {
            LOGGER.info("Benchmarking chunk stream speed");
            LOGGER.info("Warming up");
            for (int i = 0; i < 3; i++) {
                runBenchmark("GZIP", ChunkStreamVersion.GZIP, true);
                runBenchmark("DEFLATE", ChunkStreamVersion.DEFLATE, true);
                runBenchmark("UNCOMPRESSED", ChunkStreamVersion.UNCOMPRESSED, true);
            }
            runBenchmark("GZIP", ChunkStreamVersion.GZIP, false);
            runBenchmark("DEFLATE", ChunkStreamVersion.DEFLATE, false);
            runBenchmark("UNCOMPRESSED", ChunkStreamVersion.UNCOMPRESSED, false);
        }
        if (C2MEConfig.generalOptimizationsConfig.chunkStreamVersion != -1 && !ChunkStreamVersion.exists(C2MEConfig.generalOptimizationsConfig.chunkStreamVersion)) {
            LOGGER.error("Unknown chunk stream version in config: {}", C2MEConfig.generalOptimizationsConfig.chunkStreamVersion);
            throw new IllegalArgumentException(String.format("Unknown chunk stream version in config: %s", C2MEConfig.generalOptimizationsConfig.chunkStreamVersion));
        }
        consistencyTest();
    }

    private void runBenchmark(String name, ChunkStreamVersion version, boolean suppressLog) {
        try {
            final DecimalFormat decimalFormat = new DecimalFormat("0.###");
            if (!suppressLog) LOGGER.info("Generating 128MB random data");
            final byte[] bytes = new byte[128 * 1024 * 1024];
            new Random().nextBytes(bytes);
            if (!suppressLog) LOGGER.info("Starting benchmark for {}", name);
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            {
                final OutputStream wrappedOutputStream = version.wrap(outputStream);
                long startTime = System.nanoTime();
                wrappedOutputStream.write(bytes);
                wrappedOutputStream.close();
                long endTime = System.nanoTime();
                if (!suppressLog) LOGGER.info("{} write speed: {} MB/s ({} MB/s compressed)", name, decimalFormat.format((bytes.length / 1024.0 / 1024.0) / ((endTime - startTime) / 1_000_000_000.0)), decimalFormat.format((outputStream.size() / 1024.0 / 1024.0) / ((endTime - startTime) / 1_000_000_000.0)));
                if (!suppressLog) LOGGER.info("{} compression ratio: {} %", name, decimalFormat.format(outputStream.size() / (double) bytes.length * 100.0));
            }
            {
                final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                final InputStream wrappedInputStream = version.wrap(inputStream);
                long startTime = System.nanoTime();
                final byte[] readAllBytes = wrappedInputStream.readAllBytes();
                wrappedInputStream.close();
                long endTime = System.nanoTime();
                if (!suppressLog) LOGGER.info("{} read speed: {} MB/s ({} MB/s compressed)", name, decimalFormat.format((readAllBytes.length / 1024.0 / 1024.0) / ((endTime - startTime) / 1_000_000_000.0)), decimalFormat.format((outputStream.size() / 1024.0 / 1024.0) / ((endTime - startTime) / 1_000_000_000.0)));
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void consistencyTest() {
        int taskSize = 512;
        AtomicIntegerArray array = new AtomicIntegerArray(taskSize);
        final List<CompletableFuture<Integer>> futures = IntStream.range(0, taskSize)
                .mapToObj(value -> CompletableFuture.supplyAsync(() -> {
                    final ChunkRandom chunkRandom = new ChunkRandom();
                    final int i = chunkRandom.nextInt();
                    array.set(value, i);
                    return i;
                }))
                .toList();
        final List<Integer> join = Combinators.collect(futures, Collectors.toList()).toCompletableFuture().join();
        for (int i = 0; i < taskSize; i++) {
            if (array.get(i) != join.get(i))
                throw new IllegalArgumentException("Mismatch at index " + i);
        }
    }
}
