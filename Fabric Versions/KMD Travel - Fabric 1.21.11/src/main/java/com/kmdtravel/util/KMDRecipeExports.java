package com.kmdtravel.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

public final class KMDRecipeExports {
    private static final List<String> RECIPES = List.of(
            "acacia_fast_travel_post.json",
            "birch_fast_travel_post.json",
            "cherry_fast_travel_post.json",
            "dark_oak_fast_travel_post.json",
            "fast_travel_post.json",
            "jungle_fast_travel_post.json",
            "mangrove_fast_travel_post.json",
            "shared_fast_travel_post.json",
            "shared_fast_travel_post_diamond_base.json",
            "spruce_fast_travel_post.json");

    private KMDRecipeExports() {
    }

    public static void exportDefaults() {
        Path recipes = KMDPaths.recipes();
        KMDPaths.create(recipes);
        ClassLoader loader = KMDRecipeExports.class.getClassLoader();
        for (String recipe : RECIPES) {
            Path target = recipes.resolve(recipe);
            if (Files.exists(target)) {
                continue;
            }
            try (InputStream input = loader.getResourceAsStream("data/kmdtravel/recipe/" + recipe)) {
                if (input != null) {
                    Files.copy(input, target, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (IOException ignored) {
            }
        }
        writeReadme(recipes);
    }

    private static void writeReadme(Path recipes) {
        Path readme = recipes.resolve("README.txt");
        if (Files.exists(readme)) {
            return;
        }
        try {
            Files.writeString(readme, """
                    KMD Travel recipe JSON exports.

                    These files are generated as editable references for modpack/server owners.
                    Minecraft recipes are loaded through datapacks, so to make an edited recipe active,
                    copy the edited JSON into a datapack under:

                    data/kmdtravel/recipe/<recipe_name>.json

                    KMD will not overwrite files in this folder after they are created.
                    """);
        } catch (IOException ignored) {
        }
    }
}
