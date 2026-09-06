package com.ghostipedia.cosmiccore.client.orrery;

import com.ghostipedia.cosmiccore.client.gui.MajorInfoPanelRenderer;
import com.ghostipedia.cosmiccore.common.orrery.OrreryState;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;

public final class OrreryRadialScreen extends Screen {

    private int bar;
    private int hovered = -1;
    private boolean dismissed;
    private java.util.UUID tool;
    private long hoveredAt;

    public OrreryRadialScreen() {
        super(OrreryClient.text("title"));
        if (OrreryClient.state() != null) {
            bar = OrreryClient.state().active;
            tool = OrreryClient.toolId();
        }
        OrreryClient.sound(OrreryClient.Feedback.OPEN);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        if (OrreryClient.heldHand() == null) {
            onClose();
            return;
        }
        var state = OrreryClient.state();
        if (state != null) {
            if (tool == null) {
                tool = OrreryClient.toolId();
                bar = state.active;
            } else if (!tool.equals(state.id)) {
                onClose();
                return;
            }
        }
        if (!OrreryClient.keyDown()) {
            hovered = hoverAt(minecraft.mouseHandler.xpos() * width / minecraft.getWindow().getScreenWidth(),
                    minecraft.mouseHandler.ypos() * height / minecraft.getWindow().getScreenHeight());
            if (state != null && hovered >= 0 && state.filter(bar, hovered) != null) {
                OrreryClient.select(bar, hovered);
                OrreryClient.sound(OrreryClient.Feedback.SELECT);
                dismissed = true;
            }
            onClose();
        }
    }

    @Override
    public void onClose() {
        if (!dismissed) OrreryClient.sound(OrreryClient.Feedback.DISMISS);
        dismissed = true;
        super.onClose();
    }

    @Override
    public boolean mouseScrolled(double x, double y, double horizontal, double vertical) {
        if (!OrreryClient.keyDown() || vertical == 0) return true;
        bar = Math.floorMod(bar + (vertical > 0 ? -1 : 1), OrreryState.LOADOUTS);
        hovered = -1;
        OrreryClient.sound(OrreryClient.Feedback.BAR);
        return true;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        var state = OrreryClient.state();
        int slot = hoverAt(x, y);
        if (button == 2 && state != null && slot >= 0 && state.filter(bar, slot) != null) {
            var design = state.filter(bar, slot);
            dismissed = true;
            onClose();
            OrreryClient.requestCraft(design, new OrreryConfigScreen.View(bar, 0, 0, false, ""));
        }
        return true;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        var state = OrreryClient.state();
        if (state == null) return;
        g.fill(0, 0, width, height, 0x60101922);
        float scale = Math.min(1, Math.min(width / 360f, height / 360f));
        float cx = width / 2f, cy = height / 2f;
        int next = hoverAt(mouseX, mouseY);
        if (next != hovered) {
            hovered = next;
            hoveredAt = net.minecraft.Util.getMillis();
            if (hovered >= 0) OrreryClient.sound(OrreryClient.Feedback.WEDGE);
        }
        g.pose().pushPose();
        g.pose().translate(cx, cy, 0);
        g.pose().scale(scale, scale, 1);
        for (int slot = 0; slot < 8; slot++) {
            boolean hover = slot == hovered;
            var id = state.filter(bar, slot);
            var design = OrreryClient.design(id);
            boolean stocked = OrreryClient.live() && design != null && design.count() > 0;
            arc(g, slot, 64, 161, hover ? 0xD08A8A8A : 0xB83B3B3B);
            arc(g, slot, 158, 161, hover ? 0xFFE6E6E6 : 0xA06B6B6B);
            if (bar == state.active && slot == state.selected(bar) && id != null)
                arc(g, slot, 64, 67, 0xFFDB9A61);
            double angle = slot * Math.PI / 4 - Math.PI / 2;
            int x = (int) (Math.cos(angle) * 113), y = (int) (Math.sin(angle) * 113);
            if (design != null) {
                g.renderItem(design.iconStack(), x - 8, y - 18);
                if (!stocked) g.fill(x - 8, y - 18, x + 8, y - 2, 0x7718242D);
                centred(g, ellipsis(design.name(), 86), x, y + 2, stocked ? 0xFFE9EEEE : 0xFF9AA9AF);
                centred(g, OrreryClient.count(design), x, y + 14, stocked ? 0xFF99D9D7 : 0xFFE5B782);
            } else centred(g, id == null ? "·" : "?", x, y - 4, 0xFF71848D);
        }
        MajorInfoPanelRenderer.draw(g, -68, -44, 136, 100);
        centred(g, ellipsis(state.label(bar).getString(), 117), 0, -30, 0xFFEBC08B);
        centred(g, (bar + 1) + " / " + OrreryState.LOADOUTS, 0, -17, 0xFF93B9C8);
        var design = hovered < 0 ? null : OrreryClient.design(state.filter(bar, hovered));
        if (design != null) {
            int overflow = font.width(design.name()) - 116;
            if (overflow <= 0) centred(g, design.name(), 0, 5, 0xFFE9EEEE);
            else {
                double distance = Math.max(0, net.minecraft.Util.getMillis() - hoveredAt - 800) * 0.035;
                double cycle = distance % (overflow * 2 + 56);
                int offset = (int) Math.clamp(cycle < overflow + 28 ? cycle : overflow * 2 + 28 - cycle, 0, overflow);
                g.enableScissor(-58, 3, 58, 16);
                g.drawString(font, design.name(), -58 - offset, 5, 0xFFE9EEEE);
                g.disableScissor();
            }
            float textScale = 0.7f;
            var lines = font.split(design.machineName(), (int) (116 / textScale));
            if (lines.size() * font.lineHeight * textScale > 26) {
                textScale = 0.55f;
                lines = font.split(design.machineName(), (int) (116 / textScale));
            }
            g.pose().pushPose();
            try {
                g.pose().translate(0, 21, 0);
                g.pose().scale(textScale, textScale, 1);
                for (int line = 0; line < lines.size(); line++) {
                    var text = lines.get(line);
                    g.drawString(font, text, -font.width(text) / 2, line * font.lineHeight, 0xFF91A3AD);
                }
            } finally {
                g.pose().popPose();
            }
        } else {
            centred(g, ellipsis(OrreryClient.text("radial_hint").getString(), 118), 0, 4, 0xFFA6BCC4);
            centred(g, ellipsis(OrreryClient.status().getString(), 118), 0, 18, 0xFF91A3AD);
        }
        g.pose().popPose();
    }

    private int hoverAt(double mouseX, double mouseY) {
        float scale = Math.min(1, Math.min(width / 360f, height / 360f));
        double dx = (mouseX - width / 2f) / scale, dy = (mouseY - height / 2f) / scale;
        double radius = Math.sqrt(dx * dx + dy * dy);
        return radius >= 65 && radius <= 161 ?
                Math.floorMod((int) Math.floor((Math.atan2(dy, dx) + Math.PI / 2 + Math.PI / 8) / (Math.PI / 4)), 8) :
                -1;
    }

    private void centred(GuiGraphics g, String text, int x, int y, int color) {
        g.drawString(font, text, x - font.width(text) / 2, y, color);
    }

    private String ellipsis(String text, int width) {
        return font.width(text) <= width ? text : font.plainSubstrByWidth(text, width - font.width("…")) + "…";
    }

    private static void arc(GuiGraphics g, int slot, float inner, float outer, int color) {
        var vertices = g.bufferSource().getBuffer(RenderType.gui());
        var pose = g.pose().last().pose();
        double start = slot * Math.PI / 4 - Math.PI / 2 - Math.PI / 8 + 0.018;
        double span = Math.PI / 4 - 0.036;
        for (int step = 0; step < 12; step++) {
            double a = start + span * step / 12, b = start + span * (step + 1) / 12;
            vertices.addVertex(pose, (float) Math.cos(a) * inner, (float) Math.sin(a) * inner, 0).setColor(color);
            vertices.addVertex(pose, (float) Math.cos(b) * inner, (float) Math.sin(b) * inner, 0).setColor(color);
            vertices.addVertex(pose, (float) Math.cos(b) * outer, (float) Math.sin(b) * outer, 0).setColor(color);
            vertices.addVertex(pose, (float) Math.cos(a) * outer, (float) Math.sin(a) * outer, 0).setColor(color);
        }
        g.flush();
    }
}
