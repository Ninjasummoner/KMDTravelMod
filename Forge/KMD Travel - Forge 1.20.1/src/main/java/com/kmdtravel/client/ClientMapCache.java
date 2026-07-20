package com.kmdtravel.client;

import com.kmdtravel.util.KMDPaths;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public final class ClientMapCache {
    private static final int MAGIC = 0x4B4D4453;
    private static final int PLACEHOLDER_COLOR = 0xFF9C9C9C;
    private static final int CHUNK_FILL_RADIUS = 4;
    private static final int CHUNKS_PER_TICK = 8;
    private static final Map<Long, Integer> TILES = new LinkedHashMap<>();
    private static final Queue<Long> CHUNK_FILL_QUEUE = new ArrayDeque<>();
    private static final Set<Long> QUEUED_CHUNKS = new HashSet<>();
    private static Path cacheDir;
    private static long activeSeed = Long.MIN_VALUE;
    private static ResourceLocation activeBaseDimension;
    private static ResourceLocation activeDimension;
    private static int tickCounter;
    private static int lastChunkX = Integer.MIN_VALUE;
    private static int lastChunkZ = Integer.MIN_VALUE;
    private static boolean dirty;
    private static long revision;

    private ClientMapCache() {
    }

    public static void init() {
        Minecraft minecraft = Minecraft.getInstance();
        cacheDir = KMDPaths.mapCache();
        try {
            Files.createDirectories(cacheDir);
            Path oldDir = minecraft.gameDirectory.toPath().resolve("kmdtravel-map-cache");
            if (Files.isDirectory(oldDir)) {
                try (var files = Files.list(oldDir)) {
                    files.filter(Files::isRegularFile).forEach(path -> {
                        try {
                            Files.move(path, cacheDir.resolve(path.getFileName()), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException ignored) {
                        }
                    });
                }
            }
        } catch (IOException ignored) {
        }
    }

    public static void useMap(long seed, ResourceLocation dimension) {
        ResourceLocation mappedDimension = mapDimension(dimension);
        if (activeSeed == seed && dimension.equals(activeBaseDimension) && mappedDimension.equals(activeDimension)) {
            return;
        }
        save();
        activeSeed = seed;
        activeBaseDimension = dimension;
        activeDimension = mappedDimension;
        TILES.clear();
        revision++;
        ParchmentMap.clearTerrainCache();
        CHUNK_FILL_QUEUE.clear();
        QUEUED_CHUNKS.clear();
        lastChunkX = Integer.MIN_VALUE;
        lastChunkZ = Integer.MIN_VALUE;
        load();
        for (Map.Entry<Long, Integer> entry : TILES.entrySet()) {
            int tileX = (int) (entry.getKey() >> 32);
            int tileZ = (int) (long) entry.getKey();
            ParchmentMap.rememberSample(activeSeed, activeDimension, tileX * ParchmentMap.tileSize(), tileZ * ParchmentMap.tileSize(), entry.getValue());
        }
    }

    public static ResourceLocation mapDimension(ResourceLocation dimension) {
        return dimension;
    }

    public static boolean hasActiveMap() {
        return activeSeed != Long.MIN_VALUE && activeDimension != null;
    }

    public static boolean hasSampleAt(int worldX, int worldZ) {
        if (!hasActiveMap()) {
            return false;
        }
        int tileX = Math.floorDiv(worldX, ParchmentMap.tileSize());
        int tileZ = Math.floorDiv(worldZ, ParchmentMap.tileSize());
        return TILES.containsKey(ParchmentMap.tileKey(tileX, tileZ));
    }

    public static boolean matchesDimension(ResourceLocation dimension) {
        return activeBaseDimension != null && activeBaseDimension.equals(dimension);
    }

    public static int sampledColor(int worldX, int worldZ) {
        if (!hasActiveMap()) return PLACEHOLDER_COLOR;
        int tileX = Math.floorDiv(worldX, ParchmentMap.tileSize());
        int tileZ = Math.floorDiv(worldZ, ParchmentMap.tileSize());
        return TILES.getOrDefault(ParchmentMap.tileKey(tileX, tileZ), PLACEHOLDER_COLOR);
    }

    public static long revision() {
        return revision;
    }

    public static void tick() {
        if (activeSeed == Long.MIN_VALUE) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) {
            save();
            return;
        }

        ResourceLocation currentBaseDimension = minecraft.level.dimension().location();
        if (activeBaseDimension == null || !activeBaseDimension.equals(currentBaseDimension)) {
            useMap(activeSeed, currentBaseDimension);
            return;
        }

        ResourceLocation mappedDimension = mapDimension(activeBaseDimension == null ? activeDimension : activeBaseDimension);
        if (activeDimension != null && !activeDimension.equals(mappedDimension)) {
            useMap(activeSeed, activeBaseDimension);
            return;
        }

        tickCounter++;
        BlockPos playerPos = minecraft.player.blockPosition();
        int playerChunkX = Math.floorDiv(playerPos.getX(), 16);
        int playerChunkZ = Math.floorDiv(playerPos.getZ(), 16);
        if (playerChunkX != lastChunkX || playerChunkZ != lastChunkZ || tickCounter % 20 == 0) {
            enqueueChunkArea(playerChunkX, playerChunkZ);
            lastChunkX = playerChunkX;
            lastChunkZ = playerChunkZ;
        }
        sampleChunk(minecraft, playerChunkX, playerChunkZ);
        processChunkFillQueue(minecraft);

        if (dirty && (CHUNK_FILL_QUEUE.isEmpty() || tickCounter % 40 == 0)) {
            save();
        }
    }

    private static void enqueueChunkArea(int centerChunkX, int centerChunkZ) {
        enqueueChunk(centerChunkX, centerChunkZ);
        for (int dz = -CHUNK_FILL_RADIUS; dz <= CHUNK_FILL_RADIUS; dz++) {
            for (int dx = -CHUNK_FILL_RADIUS; dx <= CHUNK_FILL_RADIUS; dx++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                enqueueChunk(centerChunkX + dx, centerChunkZ + dz);
            }
        }
    }

    private static void enqueueChunk(int chunkX, int chunkZ) {
        long key = (((long) chunkX) << 32) ^ (chunkZ & 0xFFFFFFFFL);
        if (QUEUED_CHUNKS.add(key)) {
            CHUNK_FILL_QUEUE.add(key);
        }
    }

    private static void processChunkFillQueue(Minecraft minecraft) {
        for (int i = 0; i < CHUNKS_PER_TICK && !CHUNK_FILL_QUEUE.isEmpty(); i++) {
            long key = CHUNK_FILL_QUEUE.poll();
            QUEUED_CHUNKS.remove(key);
            int chunkX = (int) (key >> 32);
            int chunkZ = (int) key;
            sampleChunk(minecraft, chunkX, chunkZ);
        }
    }

    private static void sampleChunk(Minecraft minecraft, int chunkX, int chunkZ) {
        int centerX = chunkX * 16 + 8;
        int centerZ = chunkZ * 16 + 8;
        if (!WorldMapSampler.hasLoadedColumn(minecraft, centerX, centerZ)) {
            return;
        }
        int startTileX = Math.floorDiv(chunkX * 16, ParchmentMap.tileSize());
        int startTileZ = Math.floorDiv(chunkZ * 16, ParchmentMap.tileSize());
        int tilesPerChunk = Math.max(1, 16 / ParchmentMap.tileSize());
        Map<Long, Integer> sampled = new LinkedHashMap<>();
        for (int dz = 0; dz < tilesPerChunk; dz++) {
            for (int dx = 0; dx < tilesPerChunk; dx++) {
                int tileX = startTileX + dx;
                int tileZ = startTileZ + dz;
                Integer color = sampleTileColor(minecraft, tileX, tileZ);
                if (color == null) {
                    return;
                }
                sampled.put(ParchmentMap.tileKey(tileX, tileZ), color);
            }
        }
        for (Map.Entry<Long, Integer> entry : sampled.entrySet()) {
            Integer previous = TILES.put(entry.getKey(), entry.getValue());
            if (previous == null || !previous.equals(entry.getValue())) {
                dirty = true;
                revision++;
            }
            int tileX = (int) (entry.getKey() >> 32);
            int tileZ = (int) (long) entry.getKey();
            ParchmentMap.rememberSample(activeSeed, activeDimension, tileX * ParchmentMap.tileSize(), tileZ * ParchmentMap.tileSize(), entry.getValue());
        }
    }

    private static Integer sampleTileColor(Minecraft minecraft, int tileX, int tileZ) {
        int worldX = tileX * ParchmentMap.tileSize() + ParchmentMap.tileSize() / 2;
        int worldZ = tileZ * ParchmentMap.tileSize() + ParchmentMap.tileSize() / 2;
        if (!WorldMapSampler.hasLoadedColumn(minecraft, worldX, worldZ)) {
            return null;
        }
        int color = WorldMapSampler.sample(minecraft, activeSeed, worldX, worldZ);
        if (isPlaceholderColor(color)) {
            return null;
        }
        return color;
    }

    private static void load() {
        Path file = cacheFile();
        if (file == null || !Files.isRegularFile(file)) {
            return;
        }
        try (DataInputStream input = new DataInputStream(new BufferedInputStream(Files.newInputStream(file)))) {
            if (input.readInt() != MAGIC) {
                ParchmentMap.clearTerrainCache();
                return;
            }
            Map<Long, Integer> loaded = new LinkedHashMap<>();
            int count = input.readInt();
            for (int i = 0; i < count; i++) {
                long key = input.readLong();
                int color = input.readInt();
                if (!isPlaceholderColor(color)) {
                    loaded.put(key, color);
                }
            }
            TILES.putAll(loaded);
        } catch (IOException ignored) {
        }
    }

    private static void save() {
        if (!dirty || activeSeed == Long.MIN_VALUE || activeDimension == null) {
            return;
        }
        Path file = cacheFile();
        if (file == null) {
            return;
        }
        try {
            Files.createDirectories(file.getParent());
            try (DataOutputStream output = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(file)))) {
                output.writeInt(MAGIC);
                output.writeInt((int) TILES.entrySet().stream()
                        .filter(entry -> !isPlaceholderColor(entry.getValue()))
                        .count());
                for (Map.Entry<Long, Integer> entry : TILES.entrySet()) {
                    if (isPlaceholderColor(entry.getValue())) {
                        continue;
                    }
                    output.writeLong(entry.getKey());
                    output.writeInt(entry.getValue());
                }
            }
            dirty = false;
        } catch (IOException ignored) {
        }
    }

    private static Path cacheFile() {
        if (cacheDir == null) {
            init();
        }
        if (cacheDir == null || activeDimension == null) {
            return null;
        }
        String dimension = activeDimension.getNamespace() + "_" + activeDimension.getPath().replace('/', '_');
        return cacheDir.resolve(Long.toUnsignedString(activeSeed) + "_" + dimension + ".bin");
    }

    private static boolean isPlaceholderColor(int color) {
        return color == PLACEHOLDER_COLOR;
    }

}
