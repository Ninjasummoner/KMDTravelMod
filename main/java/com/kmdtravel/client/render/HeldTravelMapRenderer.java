package com.kmdtravel.client.render;

import com.kmdtravel.KMDTravel;
import com.kmdtravel.client.ClientMapCache;
import com.kmdtravel.network.HeldMapDataPacket;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import org.joml.Matrix4f;

import java.util.List;

public final class HeldTravelMapRenderer {
    private static final int TEXTURE_WIDTH = 192;
    private static final int TEXTURE_HEIGHT = 128;
    private static final int BLOCKS_PER_PIXEL = 2;
    private static final int BORDER = 3;
    private static final int UNKNOWN_COLOR = 0xFF9C9C9C;

    private static NativeImage image;
    private static DynamicTexture texture;
    private static Identifier textureLocation;
    private static int lastPlayerX = Integer.MIN_VALUE;
    private static int lastPlayerZ = Integer.MIN_VALUE;
    private static Identifier lastDimension;
    private static long lastRevision = Long.MIN_VALUE;
    private static int lastUpdateTick = Integer.MIN_VALUE;
    private static Identifier markerDimension;
    private static List<HeldMapDataPacket.Marker> markers = List.of();
    private static long markerRevision;
    private static long lastMarkerRevision = Long.MIN_VALUE;

    private HeldTravelMapRenderer() {
    }

    public static void setMarkers(Identifier dimension, List<HeldMapDataPacket.Marker> updatedMarkers) {
        markerDimension = dimension;
        markers = List.copyOf(updatedMarkers);
        markerRevision++;
    }

    public static void render(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight,
            AbstractClientPlayer player,
            float reveal
    ) {
        if (reveal <= 0.025F || !ClientMapCache.hasActiveMap()) {
            return;
        }

        updateTexture(player);
        if (textureLocation == null) {
            return;
        }

        float visible = Math.max(0.015F, reveal);
        float centerX = 8.0F / 16.0F;
        float halfWidth = (5.58F / 16.0F) * visible;
        float left = centerX - halfWidth;
        float right = centerX + halfWidth;
        float top = -12.15F / 16.0F;
        float bottom = -3.85F / 16.0F;
        float z = 6.78F / 16.0F;
        float u0 = 0.5F - visible * 0.5F;
        float u1 = 0.5F + visible * 0.5F;

        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(textureLocation),
                (pose, consumer) -> drawQuad(poseStack, consumer, packedLight, left, top, right, bottom, z, u0, 0.0F, u1, 1.0F));

        if (reveal > 0.82F) {
            renderPlayerHead(poseStack, collector, packedLight, player, z - 0.003F);
        }
    }

    private static void updateTexture(AbstractClientPlayer player) {
        Identifier dimension = player.level().dimension().identifier();
        int playerX = player.blockPosition().getX();
        int playerZ = player.blockPosition().getZ();
        long revision = ClientMapCache.revision();
        boolean samePosition = playerX == lastPlayerX && playerZ == lastPlayerZ && dimension.equals(lastDimension);
        if (texture != null && samePosition && revision == lastRevision && markerRevision == lastMarkerRevision) {
            return;
        }
        if (texture != null && samePosition && revision == lastRevision && player.tickCount - lastUpdateTick < 4) {
            return;
        }

        ensureTexture();
        lastPlayerX = playerX;
        lastPlayerZ = playerZ;
        lastDimension = dimension;
        lastRevision = revision;
        lastMarkerRevision = markerRevision;
        lastUpdateTick = player.tickCount;

        int halfWidth = TEXTURE_WIDTH / 2;
        int halfHeight = TEXTURE_HEIGHT / 2;
        for (int pixelY = 0; pixelY < TEXTURE_HEIGHT; pixelY++) {
            for (int pixelX = 0; pixelX < TEXTURE_WIDTH; pixelX++) {
                int color;
                if (pixelX < BORDER || pixelY < BORDER
                        || pixelX >= TEXTURE_WIDTH - BORDER || pixelY >= TEXTURE_HEIGHT - BORDER) {
                    color = borderColor(pixelX, pixelY);
                } else {
                    int worldX = playerX + (pixelX - halfWidth) * BLOCKS_PER_PIXEL;
                    int worldZ = playerZ + (pixelY - halfHeight) * BLOCKS_PER_PIXEL;
                    color = ClientMapCache.sampledColor(worldX, worldZ);
                    if (color == UNKNOWN_COLOR) {
                        color = 0xFF8F8F8F;
                    }
                }
                image.setPixel(pixelX, pixelY, color);
            }
        }
        if (dimension.equals(markerDimension)) {
            for (HeldMapDataPacket.Marker marker : markers) {
                int pixelX = halfWidth + Math.floorDiv(marker.x() - playerX, BLOCKS_PER_PIXEL);
                int pixelY = halfHeight + Math.floorDiv(marker.z() - playerZ, BLOCKS_PER_PIXEL);
                drawShieldMarker(pixelX, pixelY, marker.color(), marker.pattern(), marker.shared());
            }
        }
        texture.upload();
    }

    private static void drawShieldMarker(int centerX, int centerY, int color, int pattern, boolean shared) {
        int border = shared ? 0xFFFFD36A : 0xFF1F160D;
        int fill = 0xFF000000 | (color & 0xFFFFFF);
        int accent = readableAccent(fill);
        setMapPixel(centerX - 2, centerY - 3, border);
        setMapPixel(centerX - 1, centerY - 3, border);
        setMapPixel(centerX, centerY - 3, border);
        setMapPixel(centerX + 1, centerY - 3, border);
        setMapPixel(centerX + 2, centerY - 3, border);
        for (int y = -2; y <= 1; y++) {
            setMapPixel(centerX - 2, centerY + y, border);
            setMapPixel(centerX + 2, centerY + y, border);
            for (int x = -1; x <= 1; x++) {
                int pixelColor = fill;
                if ((pattern & 1) != 0 && x == 0) {
                    pixelColor = accent;
                }
                if ((pattern & 2) != 0 && y == -1) {
                    pixelColor = accent;
                }
                if ((pattern & 4) != 0 && x < 0) {
                    pixelColor = accent;
                }
                if ((pattern & 8) != 0 && x > 0) {
                    pixelColor = accent;
                }
                setMapPixel(centerX + x, centerY + y, pixelColor);
            }
        }
        setMapPixel(centerX - 1, centerY + 2, border);
        setMapPixel(centerX, centerY + 2, fill);
        setMapPixel(centerX + 1, centerY + 2, border);
        setMapPixel(centerX, centerY + 3, border);
    }

    private static int readableAccent(int color) {
        int red = (color >> 16) & 0xFF;
        int green = (color >> 8) & 0xFF;
        int blue = color & 0xFF;
        return red + green + blue > 420 ? 0xFF2D2012 : 0xFFFFE8A8;
    }

    private static void setMapPixel(int x, int y, int color) {
        if (x < BORDER || y < BORDER || x >= TEXTURE_WIDTH - BORDER || y >= TEXTURE_HEIGHT - BORDER) {
            return;
        }
        image.setPixel(x, y, color);
    }

    private static void ensureTexture() {
        if (texture != null) {
            return;
        }
        image = new NativeImage(TEXTURE_WIDTH, TEXTURE_HEIGHT, false);
        texture = new DynamicTexture(() -> "KMD held travel map", image);
        textureLocation = Identifier.fromNamespaceAndPath(KMDTravel.MOD_ID, "held_travel_map");
        Minecraft.getInstance().getTextureManager().register(textureLocation, texture);
    }

    private static int borderColor(int x, int y) {
        if (x == 0 || y == 0 || x == TEXTURE_WIDTH - 1 || y == TEXTURE_HEIGHT - 1) {
            return 0xFF3F2814;
        }
        if (x == 1 || y == 1 || x == TEXTURE_WIDTH - 2 || y == TEXTURE_HEIGHT - 2) {
            return 0xFFD3A94F;
        }
        return 0xFF6E451E;
    }

    private static int toNativeRgba(int argb) {
        int alpha = (argb >>> 24) & 0xFF;
        int red = (argb >>> 16) & 0xFF;
        int green = (argb >>> 8) & 0xFF;
        int blue = argb & 0xFF;
        return alpha << 24 | blue << 16 | green << 8 | red;
    }

    private static void renderPlayerHead(
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight,
            AbstractClientPlayer player,
            float z
    ) {
        Identifier skin = player.getSkin().body().texturePath();
        float size = 0.21F / 16.0F;
        float centerX = 8.0F / 16.0F;
        float centerY = -8.0F / 16.0F;
        collector.submitCustomGeometry(poseStack, RenderTypes.entityCutout(skin), (pose, skinConsumer) -> {
        drawQuad(poseStack, skinConsumer, packedLight,
                centerX - size, centerY - size, centerX + size, centerY + size, z,
                8.0F / 64.0F, 8.0F / 64.0F, 16.0F / 64.0F, 16.0F / 64.0F);
        drawQuad(poseStack, skinConsumer, packedLight,
                centerX - size, centerY - size, centerX + size, centerY + size, z - 0.001F,
                40.0F / 64.0F, 8.0F / 64.0F, 48.0F / 64.0F, 16.0F / 64.0F);
        });
    }

    private static void drawQuad(
            PoseStack poseStack,
            VertexConsumer consumer,
            int packedLight,
            float left,
            float top,
            float right,
            float bottom,
            float z,
            float u0,
            float v0,
            float u1,
            float v1
    ) {
        Matrix4f matrix = poseStack.last().pose();
        consumer.addVertex(matrix, left, bottom, z).setColor(255, 255, 255, 255).setUv(u0, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 0.0F, -1.0F);
        consumer.addVertex(matrix, right, bottom, z).setColor(255, 255, 255, 255).setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 0.0F, -1.0F);
        consumer.addVertex(matrix, right, top, z).setColor(255, 255, 255, 255).setUv(u1, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 0.0F, -1.0F);
        consumer.addVertex(matrix, left, top, z).setColor(255, 255, 255, 255).setUv(u0, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(0.0F, 0.0F, -1.0F);
    }
}
