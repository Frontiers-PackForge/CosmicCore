package com.ghostipedia.cosmiccore.client.gui.widget.stellar;

import com.ghostipedia.cosmiccore.api.machine.feature.IStellarModuleReceiver;
import com.ghostipedia.cosmiccore.api.machine.multiblock.IrisMultiblockMachine;

import com.lowdragmc.lowdraglib.gui.widget.Widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

public class ModuleSelectorWidget extends Widget {

    public static final int MAX_MODULES = 8;
    public static final int INNER_RING_COUNT = 4;
    public static final int OUTER_RING_COUNT = 4;

    private final Supplier<IrisMultiblockMachine> machineSupplier;
    private final Consumer<Integer> onModuleSelected;

    private final List<ModuleSlotData> moduleSlots = new ArrayList<>();
    private float animPhase = 0f;
    private int hoveredSlot = -1;
    private int selectedSlot = -1;
    private float pulsePhase = 0f;

    public ModuleSelectorWidget(int x, int y, int size, Supplier<IrisMultiblockMachine> machineSupplier,
                                Consumer<Integer> onModuleSelected) {
        super(x, y, size, size);
        this.machineSupplier = machineSupplier;
        this.onModuleSelected = onModuleSelected;

        for (int i = 0; i < MAX_MODULES; i++) {
            moduleSlots.add(new ModuleSlotData());
        }
    }

    @Override
    public void detectAndSendChanges() {
        // Always call super and sync data even when not visible
        // so that data is ready when we become visible
        super.detectAndSendChanges();

        IrisMultiblockMachine machine = machineSupplier.get();
        if (machine == null) return;

        List<IStellarModuleReceiver> modules = new ArrayList<>(machine.getConnectedModules());

        // Check for changes and sync
        boolean changed = false;
        for (int i = 0; i < MAX_MODULES; i++) {
            ModuleSlotData slot = moduleSlots.get(i);
            boolean hasModule = i < modules.size();
            String newName = hasModule ? getModuleName(modules.get(i)) : "";
            boolean newWorking = hasModule && isModuleWorking(modules.get(i));

            if (slot.populated != hasModule || !slot.moduleName.equals(newName) || slot.working != newWorking) {
                slot.populated = hasModule;
                slot.moduleName = newName;
                slot.working = newWorking;
                changed = true;
            }
        }

        if (changed) {
            writeUpdateInfo(200, buf -> {
                for (ModuleSlotData slot : moduleSlots) {
                    buf.writeBoolean(slot.populated);
                    buf.writeUtf(slot.moduleName);
                    buf.writeBoolean(slot.working);
                }
            });
        }
    }

    public void forceSync() {
        detectAndSendChanges();
    }

    private String getModuleName(IStellarModuleReceiver module) {
        if (module instanceof com.gregtechceu.gtceu.api.machine.MetaMachine metaMachine) {
            return metaMachine.getBlockState().getBlock().getDescriptionId();
        }
        return "Unknown Module";
    }

    private boolean isModuleWorking(IStellarModuleReceiver module) {
        if (module instanceof com.gregtechceu.gtceu.api.machine.multiblock.WorkableMultiblockMachine workable) {
            return workable.getRecipeLogic().isWorking();
        }
        return false;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readUpdateInfo(int id, RegistryFriendlyByteBuf buffer) {
        if (id == 200) {
            for (ModuleSlotData slot : moduleSlots) {
                slot.populated = buffer.readBoolean();
                slot.moduleName = buffer.readUtf();
                slot.working = buffer.readBoolean();
            }
        } else {
            super.readUpdateInfo(id, buffer);
        }
    }

    @Override
    public void writeInitialData(RegistryFriendlyByteBuf buffer) {
        super.writeInitialData(buffer);

        // Populate slot data before writing
        IrisMultiblockMachine machine = machineSupplier.get();
        if (machine != null) {
            List<IStellarModuleReceiver> modules = new ArrayList<>(machine.getConnectedModules());
            for (int i = 0; i < MAX_MODULES; i++) {
                ModuleSlotData slot = moduleSlots.get(i);
                boolean hasModule = i < modules.size();
                slot.populated = hasModule;
                slot.moduleName = hasModule ? getModuleName(modules.get(i)) : "";
                slot.working = hasModule && isModuleWorking(modules.get(i));
            }
        }

        for (ModuleSlotData slot : moduleSlots) {
            buffer.writeBoolean(slot.populated);
            buffer.writeUtf(slot.moduleName);
            buffer.writeBoolean(slot.working);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void readInitialData(RegistryFriendlyByteBuf buffer) {
        super.readInitialData(buffer);
        for (ModuleSlotData slot : moduleSlots) {
            slot.populated = buffer.readBoolean();
            slot.moduleName = buffer.readUtf();
            slot.working = buffer.readBoolean();
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void updateScreen() {
        super.updateScreen();
        animPhase += 0.02f;
        pulsePhase += 0.08f;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInBackground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInBackground(graphics, mouseX, mouseY, partialTicks);

        int cx = getPosition().x + getSize().width / 2;
        int cy = getPosition().y + getSize().height / 2;
        int maxRadius = getSize().width / 2 - 5;

        // Draw the two rings
        drawRings(graphics, cx, cy, maxRadius);

        // Draw central hub
        drawCentralHub(graphics, cx, cy);

        // Draw module slots on both rings
        drawModuleSlots(graphics, cx, cy, maxRadius, mouseX, mouseY);

        // Draw connecting lines from hub to slots
        drawConnectingLines(graphics, cx, cy, maxRadius);
    }

    private int getRingRadius(int ringIndex, int maxRadius) {
        // Inner ring at 40% of max, outer ring at 75% of max
        // Leaves room for future rings
        return switch (ringIndex) {
            case 0 -> (int) (maxRadius * 0.40); // Inner ring
            case 1 -> (int) (maxRadius * 0.75); // Outer ring
            default -> (int) (maxRadius * (0.40 + ringIndex * 0.35));
        };
    }

    private float getCardinalAngle(int slotInRing) {
        // Start from top (North = -PI/2), go clockwise
        return switch (slotInRing) {
            case 0 -> -Mth.HALF_PI;        // North (top)
            case 1 -> 0f;                   // East (right)
            case 2 -> Mth.HALF_PI;          // South (bottom)
            case 3 -> Mth.PI;               // West (left)
            default -> 0f;
        };
    }

    private int[] getSlotPosition(int moduleIndex) {
        int ring = moduleIndex / INNER_RING_COUNT;
        int slotInRing = moduleIndex % INNER_RING_COUNT;
        return new int[] { ring, slotInRing };
    }

    private void drawRings(GuiGraphics graphics, int cx, int cy, int maxRadius) {
        // Draw both ring circles
        int ringColor = 0x60606080;
        int ringColorBright = 0x908080A0;

        for (int ringIndex = 0; ringIndex < 2; ringIndex++) {
            int ringRadius = getRingRadius(ringIndex, maxRadius);

            // Draw ring circle
            for (int angle = 0; angle < 360; angle += 3) {
                float rad = angle * Mth.DEG_TO_RAD;
                int px = cx + (int) (Mth.cos(rad) * ringRadius);
                int py = cy + (int) (Mth.sin(rad) * ringRadius);

                // Brighter at cardinal points
                boolean isCardinal = (angle % 90) < 10 || (angle % 90) > 80;
                int color = isCardinal ? ringColorBright : ringColor;
                graphics.fill(px, py, px + 1, py + 1, color);
            }
        }

        // Inner glow
        int hubRadius = 15;
        for (int r = hubRadius + 10; r > hubRadius; r -= 2) {
            float progress = (float) (r - hubRadius) / 10f;
            int alpha = (int) (20 * (1f - progress));
            int color = (alpha << 24) | 0x101020;
            drawCircle(graphics, cx, cy, r, color);
        }
    }

    private void drawCentralHub(GuiGraphics graphics, int cx, int cy) {
        int hubRadius = 15;

        // Hub glow
        for (int r = hubRadius + 5; r > hubRadius; r--) {
            int alpha = (int) (60 * (1f - (float) (r - hubRadius) / 5f));
            int color = (alpha << 24) | 0x4080AA;
            drawCircle(graphics, cx, cy, r, color);
        }

        // Hub body
        drawCircle(graphics, cx, cy, hubRadius, 0xE0101820);

        // Hub border
        int pulseAlpha = (int) (100 + 50 * Mth.sin(pulsePhase));
        int borderColor = (pulseAlpha << 24) | 0x4080AA;
        for (int angle = 0; angle < 360; angle += 5) {
            float rad = angle * Mth.DEG_TO_RAD;
            int px = cx + (int) (Mth.cos(rad) * hubRadius);
            int py = cy + (int) (Mth.sin(rad) * hubRadius);
            graphics.fill(px, py, px + 1, py + 1, borderColor);
        }

        // Hub icon (8 dots for modules)
        var font = Minecraft.getInstance().font;
        String icon = "\u2699"; // Gear icon
        int textWidth = font.width(icon);
        graphics.drawString(font, icon, cx - textWidth / 2, cy - font.lineHeight / 2, 0xFF4080AA, false);
    }

    private void drawModuleSlots(GuiGraphics graphics, int cx, int cy, int maxRadius, int mouseX, int mouseY) {
        int dotSize = 12;

        hoveredSlot = -1;

        for (int i = 0; i < MAX_MODULES; i++) {
            // Get ring and position within ring
            int[] pos = getSlotPosition(i);
            int ringIndex = pos[0];
            int slotInRing = pos[1];

            // Calculate position
            int ringRadius = getRingRadius(ringIndex, maxRadius);
            float angle = getCardinalAngle(slotInRing);

            int slotX = cx + (int) (Mth.cos(angle) * ringRadius);
            int slotY = cy + (int) (Mth.sin(angle) * ringRadius);

            ModuleSlotData slot = moduleSlots.get(i);

            // Check hover
            int dx = mouseX - slotX;
            int dy = mouseY - slotY;
            boolean hovered = dx * dx + dy * dy <= (dotSize + 2) * (dotSize + 2);
            if (hovered) {
                hoveredSlot = i;
            }

            // Draw slot
            drawModuleSlot(graphics, slotX, slotY, dotSize, slot, hovered, i == selectedSlot, ringIndex);
        }
    }

    private void drawModuleSlot(GuiGraphics graphics, int x, int y, int size, ModuleSlotData slot,
                                boolean hovered, boolean selected, int ringIndex) {
        int halfSize = size / 2;

        // Determine colors - inner ring slightly different tint
        int bgColor;
        int borderColor;
        int glowColor;

        if (slot.populated) {
            if (slot.working) {
                // Working - green pulse
                int pulse = (int) (200 + 55 * Mth.sin(pulsePhase * 2));
                bgColor = 0xE0102010;
                borderColor = (pulse << 24) | 0x44FF44;
                glowColor = 0x4044FF44;
            } else {
                // Connected but idle - cyan (inner ring slightly more blue)
                bgColor = ringIndex == 0 ? 0xE0101825 : 0xE0101820;
                borderColor = ringIndex == 0 ? 0xFF5090BB : 0xFF4080AA;
                glowColor = ringIndex == 0 ? 0x505090BB : 0x504080AA;
            }
        } else {
            // Empty slot - more visible
            bgColor = 0x90181820;
            borderColor = 0x80606070;
            glowColor = 0x00000000;
        }

        // Hover/selected effects
        if (selected) {
            borderColor = 0xFFFFFFFF;
            glowColor = 0x60FFFFFF;
        } else if (hovered) {
            borderColor = slot.populated ? 0xFFFFCC44 : 0x80AAAAAA;
            glowColor = slot.populated ? 0x40FFCC44 : 0x20AAAAAA;
        }

        // Draw glow
        if (glowColor != 0) {
            for (int r = halfSize + 5; r > halfSize; r--) {
                int alpha = (glowColor >> 24) & 0xFF;
                alpha = alpha * (halfSize + 5 - r) / 5;
                int color = (alpha << 24) | (glowColor & 0x00FFFFFF);
                drawCircle(graphics, x, y, r, color);
            }
        }

        // Draw slot background
        drawCircle(graphics, x, y, halfSize, bgColor);

        // Draw border - solid circle
        for (int angle = 0; angle < 360; angle += 10) {
            float rad = angle * Mth.DEG_TO_RAD;
            int px = x + (int) (Mth.cos(rad) * halfSize);
            int py = y + (int) (Mth.sin(rad) * halfSize);
            graphics.fill(px, py, px + 1, py + 1, borderColor);
        }

        // Draw center indicator
        if (!slot.populated) {
            // Empty slot - small diamond shape
            int dimColor = 0x60FFFFFF;
            graphics.fill(x, y - 2, x + 1, y + 3, dimColor);
            graphics.fill(x - 2, y, x + 3, y + 1, dimColor);
        } else {
            // Draw working indicator
            if (slot.working) {
                int indicatorPulse = (int) (255 * (0.5f + 0.5f * Mth.sin(pulsePhase * 3)));
                graphics.fill(x - 2, y - 2, x + 3, y + 3, (indicatorPulse << 24) | 0x44FF44);
            } else {
                graphics.fill(x - 2, y - 2, x + 3, y + 3, 0xC04080AA);
            }
        }
    }

    private void drawConnectingLines(GuiGraphics graphics, int cx, int cy, int maxRadius) {
        int hubRadius = 18;

        for (int i = 0; i < MAX_MODULES; i++) {
            ModuleSlotData slot = moduleSlots.get(i);
            if (!slot.populated) continue;

            // Get ring and position
            int[] pos = getSlotPosition(i);
            int ringIndex = pos[0];
            int slotInRing = pos[1];

            int ringRadius = getRingRadius(ringIndex, maxRadius);
            float angle = getCardinalAngle(slotInRing);

            // Line from hub edge to slot
            int startX = cx + (int) (Mth.cos(angle) * hubRadius);
            int startY = cy + (int) (Mth.sin(angle) * hubRadius);
            int endX = cx + (int) (Mth.cos(angle) * (ringRadius - 8));
            int endY = cy + (int) (Mth.sin(angle) * (ringRadius - 8));

            int lineColor = slot.working ? 0xA044FF44 : 0x704080AA;

            // Draw dotted line
            int segments = ringIndex == 0 ? 4 : 8;
            for (int s = 0; s < segments; s += 2) {
                float t1 = (float) s / segments;
                float t2 = (float) (s + 1) / segments;
                int x1 = (int) (startX + (endX - startX) * t1);
                int y1 = (int) (startY + (endY - startY) * t1);
                int x2 = (int) (startX + (endX - startX) * t2);
                int y2 = (int) (startY + (endY - startY) * t2);
                graphics.fill(Math.min(x1, x2), Math.min(y1, y2),
                        Math.max(x1, x2) + 1, Math.max(y1, y2) + 1, lineColor);
            }
        }
    }

    private void drawCircle(GuiGraphics graphics, int cx, int cy, int radius, int color) {
        if (radius <= 0) return;
        for (int y = -radius; y <= radius; y++) {
            int halfWidth = (int) Math.sqrt(radius * radius - y * y);
            graphics.fill(cx - halfWidth, cy + y, cx + halfWidth + 1, cy + y + 1, color);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && hoveredSlot >= 0) {
            ModuleSlotData slot = moduleSlots.get(hoveredSlot);
            if (slot.populated) {
                selectedSlot = hoveredSlot;
                if (onModuleSelected != null) {
                    onModuleSelected.accept(hoveredSlot);
                }
                playButtonClickSound();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void drawInForeground(@Nonnull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.drawInForeground(graphics, mouseX, mouseY, partialTicks);

        // Draw tooltip for hovered slot
        if (hoveredSlot >= 0) {
            ModuleSlotData slot = moduleSlots.get(hoveredSlot);
            List<Component> tooltip = new ArrayList<>();

            if (slot.populated) {
                tooltip.add(Component.translatable(slot.moduleName));
                if (slot.working) {
                    tooltip.add(Component.literal("\u00A7aWorking"));
                } else {
                    tooltip.add(Component.literal("\u00A77Idle"));
                }
                tooltip.add(Component.literal("\u00A78Click to configure"));
            } else {
                tooltip.add(Component.literal("\u00A78Empty Slot " + (hoveredSlot + 1)));
            }

            graphics.renderTooltip(Minecraft.getInstance().font, tooltip, java.util.Optional.empty(), mouseX, mouseY);
        }
    }

    public void clearSelection() {
        selectedSlot = -1;
    }

    public int getSelectedSlot() {
        return selectedSlot;
    }

    private static class ModuleSlotData {

        boolean populated = false;
        String moduleName = "";
        boolean working = false;
    }
}
