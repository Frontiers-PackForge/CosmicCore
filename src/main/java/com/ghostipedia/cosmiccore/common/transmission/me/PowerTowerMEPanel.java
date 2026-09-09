package com.ghostipedia.cosmiccore.common.transmission.me;

import com.ghostipedia.cosmiccore.client.transmission.PowerTowerMELocator;
import com.ghostipedia.cosmiccore.common.machine.transmission.PowerTowerMEHatch;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;
import com.gregtechceu.gtceu.common.mui.GTMuiWidgets;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.drawable.GuiTextures;
import brachy.modularui.drawable.UITexture;
import brachy.modularui.factory.PosGuiData;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.utils.serialization.network.ByteBufAdapters;
import brachy.modularui.value.sync.GenericSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.StringSyncValue;
import brachy.modularui.value.sync.SyncHandler;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.textfield.TextFieldWidget;
import io.netty.buffer.ByteBuf;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

public final class PowerTowerMEPanel {

    private static final int ROWS = 6;
    private static final UITexture LOCATE_ICON = UITexture.builder()
            .location(ResourceLocation.fromNamespaceAndPath("extendedae", "textures/guis/nicons.png"))
            .imageSize(64, 64).subAreaXYWH(16, 0, 16, 16).build();

    private PowerTowerMEPanel() {}

    public static ModularPanel build(PowerTowerMEHatch hatch, PosGuiData data, PanelSyncManager sync) {
        var panel = ModularPanel.defaultPanel("power_tower_me", 340, hatch.isInput() ? 286 : 170);
        panel.child(GTMuiWidgets.createTitleBar(hatch.getDefinition(), 340));
        var view = new View(hatch);
        var control = new Control(hatch, view);
        sync.syncValue("actions", control.allowC2S());
        panel.child(new ButtonWidget<>().pos(314, 4).size(16).overlay(GTGuiTextures.INFO)
                .tooltipBuilder(tooltip -> {
                    for (int i = 0; i < 7; i++) tooltip.addLine(Text.lang("cosmiccore.tower_me.help." + i));
                }));

        var name = new StringSyncValue(() -> {
            var source = hatch.isInput() ? hatch :
                    hatch.registry() == null ? null : hatch.registry().source(hatch.binding());
            return source == null ? "" : source.circuitName();
        }, value -> {
            if (hatch.canConfigure(data.getPlayer())) hatch.rename(value);
        }).allowC2S(hatch.isInput());
        sync.syncValue("circuit_name", name);
        if (hatch.isInput()) {
            panel.child(Text.lang("cosmiccore.tower_me.name").asWidget().pos(8, 29).size(72, 10));
            panel.child(new TextFieldWidget().pos(82, 25).size(250, 18).setMaxLength(48)
                    .value(name).autoUpdateOnChange(true));
        } else {
            text(panel, 27, () -> tr("source", name.getStringValue().isBlank() ? tr("unnamed") :
                    Component.literal(name.getStringValue())));
        }
        var status = string(sync, "status", () -> hatch.registry() == null ? "connecting" :
                hatch.registry().status(hatch));
        text(panel, 49, () -> tr(status.getStringValue()).withStyle(statusColor(status.getStringValue())));
        var identity = string(sync, "identity", () -> {
            var id = hatch.isInput() ? hatch.identity() : hatch.binding();
            return id == null ? "" : id.circuit().toString();
        });
        text(panel, 65, () -> tr("identity", identity.getStringValue().isBlank() ? "-" : identity.getStringValue()));
        var position = position(sync, "position", () -> {
            var binding = hatch.isInput() ? hatch.identity() : hatch.binding();
            return binding == null ? null : binding.input();
        });
        text(panel, 79, () -> tr("input_position", position.getValue() == null ? "-" :
                position.getValue().toShortString()));
        if (!hatch.isInput()) panel.child(locateButton(316, 76, hatch, position::getValue, name, true));
        var used = integer(sync, "used", () -> hatch.registry() == null ? -1 : hatch.registry().usedChannels(hatch));
        var capacity = integer(sync, "capacity",
                () -> hatch.registry() == null ? -1 : hatch.registry().capacity(hatch));
        text(panel, 96, () -> tr(hatch.isInput() ? "channels" : "branch_channels", amount(used.getIntValue()),
                amount(capacity.getIntValue())));

        if (!hatch.isInput()) {
            text(panel, 115, () -> tr("bind_hint"));
            panel.child(button(8, 143, 130, tr("clear"), () -> control.request(0, 0)));
            return panel;
        }

        var count = integer(sync, "count", () -> view.positions().size());
        text(panel, 115, () -> tr("destinations", count.getIntValue()));
        for (int index = 0; index < ROWS; index++) {
            final int row = index;
            var pos = position(sync, "row_pos_" + row, () -> view.position(row));
            var state = string(sync, "row_state_" + row, () -> {
                var output = view.output(row);
                return output == null ? "unloaded" : hatch.registry().status(output);
            });
            var channels = integer(sync, "row_channels_" + row, () -> {
                var output = view.output(row);
                return output == null ? -1 : hatch.registry().usedChannels(output);
            });
            panel.child(new ParentWidget<>().pos(8, 131 + row * 20).size(324, 19)
                    .background(GTGuiTextures.BACKGROUND_INVERSE)
                    .child(new TextWidget<>(Text.dynamic(() -> pos.getValue() == null ? Component.empty() :
                            tr("destination", pos.getValue().toShortString(), amount(channels.getIntValue()))))
                            .pos(4, 3).size(180, 12).scale(0.8f))
                    .child(new TextWidget<>(Text.dynamic(() -> pos.getValue() == null ? Component.empty() :
                            tr(state.getStringValue()).withStyle(statusColor(state.getStringValue()))))
                            .pos(188, 3).size(112, 12).scale(0.75f))
                    .child(locateButton(305, 2, hatch, pos::getValue, name, false)));
        }
        var page = integer(sync, "page", () -> view.page);
        panel.child(button(8, 260, 30, Component.literal("<"), () -> control.request(1, -1)));
        panel.child(button(302, 260, 30, Component.literal(">"), () -> control.request(1, 1)));
        panel.child(Text.dynamic(() -> tr("page", page.getIntValue() + 1,
                Math.max(1, (count.getIntValue() + ROWS - 1) / ROWS))).asWidget().pos(45, 264).size(250, 10));
        return panel;
    }

    private static void text(ModularPanel panel, int y, Supplier<Component> value) {
        panel.child(new TextWidget<>(Text.dynamic(value)).pos(8, y).size(324, 12).scale(0.85f));
    }

    private static ButtonWidget<?> button(int x, int y, int width, Component label, Runnable action) {
        return new ButtonWidget<>().pos(x, y).size(width, 18).overlay(Text.of(label))
                .onMousePressed((context, button) -> {
                    if (button != 0) return false;
                    action.run();
                    return true;
                });
    }

    private static ButtonWidget<?> locateButton(int x, int y, PowerTowerMEHatch hatch, Supplier<BlockPos> position,
                                                StringSyncValue name, boolean input) {
        return new ButtonWidget<>().pos(x, y).size(16)
                .overlay((GTCEu.isModLoaded("extendedae") ? LOCATE_ICON : GuiTextures.SEARCH).asIcon().size(14))
                .setEnabledIf(widget -> position.get() != null)
                .tooltipBuilder(tooltip -> {
                    tooltip.addLine(Text.lang("cosmiccore.tower_me.locate"));
                    tooltip.addLine(Text.lang("cosmiccore.tower_me.locate_hint"));
                })
                .onMousePressed((context, button) -> {
                    if (button != 0 || position.get() == null) return false;
                    if (PowerTowerMELocator.locate(position.get(), hatch.getLevel().dimension(),
                            name.getStringValue(), input)) {
                        ((ModularGuiContext) context).getScreen().getMainPanel().closeIfOpen();
                    }
                    return true;
                });
    }

    private static GenericSyncValue<ByteBuf, BlockPos> position(PanelSyncManager sync, String id,
                                                                Supplier<BlockPos> supplier) {
        var value = GenericSyncValue.<ByteBuf, BlockPos>builder(BlockPos.class).getter(supplier)
                .adapter(ByteBufAdapters.BLOCKPOS).copyImmutable().nullable().build();
        sync.syncValue(id, value);
        return value;
    }

    private static StringSyncValue string(PanelSyncManager sync, String id, Supplier<String> supplier) {
        var value = new StringSyncValue(supplier, ignored -> {});
        sync.syncValue(id, value);
        return value;
    }

    private static IntSyncValue integer(PanelSyncManager sync, String id, IntSupplier supplier) {
        var value = new IntSyncValue(supplier, ignored -> {});
        sync.syncValue(id, value);
        return value;
    }

    private static Component amount(int value) {
        return value < 0 ? tr("unavailable") : value == Integer.MAX_VALUE ? tr("unlimited") :
                Component.literal(Integer.toString(value));
    }

    private static net.minecraft.network.chat.MutableComponent tr(String key, Object... args) {
        return Component.translatable("cosmiccore.tower_me." + key, args);
    }

    private static ChatFormatting statusColor(String status) {
        return switch (status) {
            case "linked" -> ChatFormatting.DARK_GREEN;
            case "booting", "connecting", "overloaded", "unloaded" -> ChatFormatting.GOLD;
            default -> ChatFormatting.DARK_RED;
        };
    }

    private static final class View {

        private final PowerTowerMEHatch hatch;
        private long tick = Long.MIN_VALUE;
        private List<BlockPos> positions = List.of();
        private int page;

        View(PowerTowerMEHatch hatch) {
            this.hatch = hatch;
        }

        List<BlockPos> positions() {
            if (hatch.registry() == null || hatch.getLevel() == null) return List.of();
            long now = hatch.getLevel().getGameTime();
            if (tick != now) {
                tick = now;
                positions = hatch.registry().destinations(hatch.circuitId());
                page = Math.min(page, Math.max(0, (positions.size() - 1) / ROWS));
            }
            return positions;
        }

        BlockPos position(int row) {
            var positions = positions();
            int index = page * ROWS + row;
            return index >= positions.size() ? null : positions.get(index);
        }

        PowerTowerMEHatch output(int row) {
            var pos = position(row);
            return pos == null ? null : hatch.registry().loadedAt(pos);
        }
    }

    private static final class Control extends SyncHandler<Control> {

        private final PowerTowerMEHatch hatch;
        private final View view;

        Control(PowerTowerMEHatch hatch, View view) {
            this.hatch = hatch;
            this.view = view;
        }

        void request(int id, int direction) {
            syncToServer(id, buffer -> buffer.writeVarInt(direction));
        }

        @Override
        public void readOnServer(int id, RegistryFriendlyByteBuf buffer) {
            if (!hatch.canConfigure(getSyncManager().getPlayer())) return;
            int direction = buffer.readVarInt();
            if (id == 0) hatch.clearBinding();
            if (id == 1) view.page = Math.clamp(view.page + Integer.signum(direction), 0,
                    Math.max(0, (view.positions().size() - 1) / ROWS));
        }

        @Override
        public void readOnClient(int id, RegistryFriendlyByteBuf buffer) {}
    }
}
