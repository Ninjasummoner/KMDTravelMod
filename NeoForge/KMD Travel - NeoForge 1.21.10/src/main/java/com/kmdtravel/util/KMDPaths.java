package com.kmdtravel.util;

import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class KMDPaths {
    private KMDPaths() {
    }

    public static Path root() {
        return FMLPaths.GAMEDIR.get().resolve("kmdtravel");
    }

    public static Path configRoot() {
        return FMLPaths.CONFIGDIR.get().resolve("kmdtravel");
    }

    public static Path mapCache() {
        return root().resolve("map-cache");
    }

    public static Path events() {
        return root().resolve("events");
    }

    public static Path recipes() {
        return root().resolve("recipes");
    }

    public static void ensureRoot() {
        create(root());
        create(mapCache());
        create(events());
        create(recipes());
    }

    public static void create(Path path) {
        try {
            Files.createDirectories(path);
        } catch (IOException ignored) {
        }
    }
}
