package com.ghostipedia.cosmiccore.client.aeroponics;

import com.ghostipedia.cosmiccore.client.compat.modularui.SchemaRenderState;
import com.ghostipedia.cosmiccore.common.compat.qualityfood.QualityFoodCompat;

import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

import brachy.modularui.api.drawable.IDrawable;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.GuiDraw;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetTheme;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.widget.Widget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public final class AeroponicsCupGrid extends Widget<AeroponicsCupGrid> {

    private static final int WIDTH = 18;
    private static final int HEIGHT = 16;
    private static final int INSET = 2;
    private static final int ICON_SIZE = HEIGHT - 2 * INSET;
    private static final int LABEL_COUNT = 102;
    private static final int COLUMNS = 16;
    private static final CompoundTag EMPTY = new CompoundTag();
    private final List<Cup> cups = new ArrayList<>(160);
    private final Map<Appearance, Tile> tiles = new LinkedHashMap<>();
    private final ToIntFunction<CompoundTag> progress;
    private Cup hovered;
    private TextureTarget atlas;
    private BakedModel missingModel;
    private WidgetTheme tileTheme;
    private int scale;
    private int rows;
    private boolean animated;
    private long lastTick;

    public AeroponicsCupGrid(List<Supplier<CompoundTag>> layers, ToIntFunction<CompoundTag> progress,
                             Function<CompoundTag, Component> details) {
        this.progress = progress;
        size(302, 168).background(IDrawable.EMPTY);
        tooltipBuilder(tip -> {
            var cup = hovered;
            if (cup != null) tip.addLine(Text.dynamic(() -> details.apply(cup.row)));
        });
        for (int level = 0; level < layers.size(); level++) {
            var layer = layers.get(level);
            for (int i = 0; i < 32; i++) {
                String key = Integer.toString(i);
                int x = i / 4 * (2 * WIDTH + 2) + i % 2 * WIDTH;
                int y = (4 - level) * (2 * HEIGHT + 2) + (i % 4 < 2 ? HEIGHT : 0);
                cups.add(new Cup(x, y, () -> (CompoundTag) layer.get().get(key)));
            }
        }
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        var mc = context.getMC();
        var model = mc.getModelManager().getMissingModel();
        var theme = widgetTheme.getTheme(false);
        int guiScale = (int) mc.getWindow().getGuiScale();
        boolean rebuild = atlas == null || model != missingModel || theme != tileTheme || scale != guiScale;
        Cup underMouse = null;
        int mouseX = context.getMouseX(), mouseY = context.getMouseY();
        for (var cup : cups) {
            rebuild |= cup.readPlanting();
            if (mouseX >= cup.x && mouseX < cup.x + WIDTH && mouseY >= cup.y && mouseY < cup.y + HEIGHT)
                underMouse = cup;
        }
        if (hovered != underMouse) {
            hovered = underMouse;
            markTooltipDirty();
        }
        if (rebuild) {
            tiles.clear();
            animated = false;
            for (var cup : cups) {
                cup.tile = tiles.computeIfAbsent(cup.appearance, key -> new Tile(key, LABEL_COUNT + tiles.size(), mc));
                animated |= cup.tile.animated;
            }
            rows = Math.ceilDiv(LABEL_COUNT + tiles.size(), COLUMNS);
            scale = guiScale;
            missingModel = model;
            tileTheme = theme;
        }
        if (rebuild || (animated && lastTick != context.getTick())) {
            refreshAtlas(context, rebuild);
            lastTick = context.getTick();
        }
        drawTiles(context);
    }

    private void refreshAtlas(ModularGuiContext context, boolean all) {
        var graphics = context.getGraphics();
        graphics.flush();
        var saved = new SchemaRenderState();
        int readFbo = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int drawFbo = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        boolean hadScissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
        int[] scissor = new int[4];
        if (hadScissor) GL11.glGetIntegerv(GL11.GL_SCISSOR_BOX, scissor);
        boolean hadStencil = GL11.glIsEnabled(GL11.GL_STENCIL_TEST);
        float[] clearColor = new float[4];
        GL11.glGetFloatv(GL11.GL_COLOR_CLEAR_VALUE, clearColor);
        var pose = graphics.pose();
        pose.pushPose();
        try {
            RenderSystem.disableScissor();
            GL11.glDisable(GL11.GL_STENCIL_TEST);
            int w = COLUMNS * WIDTH, h = rows * HEIGHT;
            if (atlas == null) atlas = new TextureTarget(w * scale, h * scale, true, Minecraft.ON_OSX);
            else if (atlas.width != w * scale || atlas.height != h * scale)
                atlas.resize(w * scale, h * scale, Minecraft.ON_OSX);
            atlas.bindWrite(true);
            RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0, w, h, 0, 1000, 21000),
                    VertexSorting.ORTHOGRAPHIC_Z);
            RenderSystem.getModelViewStack().identity().translate(0, 0, -11000);
            RenderSystem.applyModelViewMatrix();
            pose.setIdentity();
            RenderSystem.clearColor(0, 0, 0, 0);
            RenderSystem.depthMask(true);
            if (all) {
                RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
                graphics.setColor(1, 1, 1, 1);
                var font = context.getFont();
                for (int i = 0; i < LABEL_COUNT; i++) {
                    int x = i % COLUMNS * WIDTH;
                    int y = i / COLUMNS * HEIGHT;
                    String label = switch (i) {
                        case 100 -> "!!!";
                        case 101 -> "?";
                        default -> i + "%";
                    };
                    RenderSystem.enableScissor((x + INSET) * scale, (h - y - HEIGHT + INSET) * scale,
                            (WIDTH - 2 * INSET) * scale, ICON_SIZE * scale);
                    pose.pushPose();
                    pose.translate(x + WIDTH - INSET, y + HEIGHT - INSET, 0);
                    pose.scale(0.5f, 0.5f, 1);
                    graphics.drawString(font, label, -font.width(label) - 1, -font.lineHeight - 1, 0xFFFFFFFF, true);
                    graphics.flush();
                    pose.popPose();
                }
            }
            for (var tile : tiles.values()) {
                if (!all && !tile.animated) continue;
                int x = tile.index % COLUMNS * WIDTH;
                int y = tile.index / COLUMNS * HEIGHT;
                int glY = (h - y - HEIGHT) * scale;
                RenderSystem.enableScissor(x * scale, glY, WIDTH * scale, HEIGHT * scale);
                if (!all) RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
                GTGuiTextures.SLOT.draw(context, x, y, WIDTH, HEIGHT, tileTheme);
                if (tile.stack.isEmpty()) continue;
                RenderSystem.enableScissor((x + INSET) * scale, glY + INSET * scale,
                        (WIDTH - 2 * INSET) * scale, ICON_SIZE * scale);
                graphics.setColor(1, 1, 1, 1);
                GuiDraw.drawItem(graphics, tile.stack, x + INSET, y + INSET, ICON_SIZE, ICON_SIZE, 0);
                if (tile.star != null) graphics.blitSprite(tile.star, x + INSET, y + INSET, 300, ICON_SIZE, ICON_SIZE);
                graphics.flush();
            }
        } finally {
            pose.popPose();
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFbo);
            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFbo);
            saved.restore();
            RenderSystem.clearColor(clearColor[0], clearColor[1], clearColor[2], clearColor[3]);
            if (hadStencil) GL11.glEnable(GL11.GL_STENCIL_TEST);
            if (hadScissor) RenderSystem.enableScissor(scissor[0], scissor[1], scissor[2], scissor[3]);
            else RenderSystem.disableScissor();
        }
    }

    private void drawTiles(ModularGuiContext context) {
        var matrix = context.graphicsPose().last().pose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, atlas.getColorTextureId());
        var quads = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        for (var cup : cups) {
            appendTile(quads, matrix, cup, cup.tile.index, 0);
            if (cup.row != EMPTY) {
                int label = cup.row.contains("duration") ? progress.applyAsInt(cup.row) : 101;
                appendTile(quads, matrix, cup, label, 1);
            }
        }
        BufferUploader.drawWithShader(quads.buildOrThrow());
    }

    private void appendTile(VertexConsumer quads, Matrix4f matrix, Cup cup, int index, int z) {
        int col = index % COLUMNS, row = index / COLUMNS;
        float u0 = (float) col / COLUMNS, u1 = (float) (col + 1) / COLUMNS;
        float v0 = 1f - (float) row / rows, v1 = 1f - (float) (row + 1) / rows;
        quads.addVertex(matrix, cup.x, cup.y + HEIGHT, z).setUv(u0, v1);
        quads.addVertex(matrix, cup.x + WIDTH, cup.y + HEIGHT, z).setUv(u1, v1);
        quads.addVertex(matrix, cup.x + WIDTH, cup.y, z).setUv(u1, v0);
        quads.addVertex(matrix, cup.x, cup.y, z).setUv(u0, v0);
    }

    @Override
    public void dispose() {
        if (atlas != null) {
            int readFbo = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
            int drawFbo = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
            atlas.destroyBuffers();
            GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFbo);
            GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFbo);
            atlas = null;
        }
        super.dispose();
    }

    private record Appearance(String seed, int quality) {}

    private static final class Cup {

        private final int x, y;
        private final Supplier<CompoundTag> source;
        private CompoundTag row = EMPTY;
        private Appearance appearance = new Appearance("", 0);
        private Tile tile;

        private Cup(int x, int y, Supplier<CompoundTag> source) {
            this.x = x;
            this.y = y;
            this.source = source;
        }

        private boolean readPlanting() {
            var next = source.get();
            if (next == null) next = EMPTY;
            if (next == row) return false;
            row = next;
            String id = row.getString("seed");
            int tier = row.getByte("quality");
            if (tier == appearance.quality && id.equals(appearance.seed)) return false;
            appearance = new Appearance(id, tier);
            return true;
        }
    }

    private static final class Tile {

        private final int index;
        private final ItemStack stack;
        private final ResourceLocation star;
        private final boolean animated;

        private Tile(Appearance key, int index, Minecraft mc) {
            this.index = index;
            this.stack = key.seed.isEmpty() ? ItemStack.EMPTY :
                    BuiltInRegistries.ITEM.get(ResourceLocation.parse(key.seed)).getDefaultInstance();
            this.star = QualityFoodCompat.star(key.quality);
            this.animated = !stack.isEmpty() && needsRefresh(mc, stack);
        }

        private static boolean needsRefresh(Minecraft mc, ItemStack stack) {
            var model = mc.getItemRenderer().getModel(stack, mc.level, mc.player, 0);
            if (stack.hasFoil() || model.isCustomRenderer() || !model.getOverrides().getOverrides().isEmpty())
                return true;
            var random = RandomSource.create();
            var sides = Direction.values();
            for (var pass : model.getRenderPasses(stack, true)) {
                for (int i = 0; i <= sides.length; i++) {
                    var face = i == sides.length ? null : sides[i];
                    random.setSeed(42L);
                    for (var quad : pass.getQuads(null, face, random)) {
                        if (quad.getSprite().contents().getUniqueFrames().limit(2).count() > 1) return true;
                    }
                }
            }
            return false;
        }
    }
}
