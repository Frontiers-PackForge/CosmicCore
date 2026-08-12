package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.api.registries.CosmicRegistration;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.common.item.behavior.CoverPlaceBehavior;
import com.gregtechceu.gtceu.common.item.behavior.TooltipBehavior;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import com.tterrag.registrate.util.entry.ItemEntry;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.common.data.GTItems.attach;

public final class CosmicCoverItems {

    static {
        CosmicRegistration.REGISTRATE.creativeModeTab(() -> CosmicCreativeModeTabs.COSMIC_CORE);
    }

    public static final ItemEntry<Item> STEAM_MOTOR = REGISTRATE.item("steam_motor", Item::new)
            .lang("Steam Motor")
            .tag(CustomTags.ELECTRIC_MOTORS)
            .defaultModel()
            .register();

    public static final ItemEntry<Item> STEAM_PISTON = REGISTRATE.item("steam_piston", Item::new)
            .lang("Steam Piston")
            .tag(CustomTags.ELECTRIC_PISTONS)
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> STEAM_CONVEYOR = REGISTRATE
            .item("steam_conveyor", ComponentItem::new)
            .lang("Steam Conveyor")
            .onRegister(attach(new CoverPlaceBehavior(CosmicCovers.STEAM_CONVEYOR)))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.cosmiccore.steam_conveyor.tooltip"));
                lines.add(Component.translatable("cosmiccore.universal.tooltip.item_transfer_rate",
                        CosmicCovers.STEAM_ITEM_TRANSFER_RATE));
            })))
            .tag(CustomTags.CONVEYOR_MODULES)
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> STEAM_PUMP = REGISTRATE
            .item("steam_pump", ComponentItem::new)
            .lang("Steam Pump")
            .onRegister(attach(new CoverPlaceBehavior(CosmicCovers.STEAM_PUMP)))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.cosmiccore.steam_pump.tooltip"));
                lines.add(Component.translatable("cosmiccore.universal.tooltip.fluid_transfer_rate",
                        CosmicCovers.STEAM_FLUID_TRANSFER_RATE));
            })))
            .tag(CustomTags.ELECTRIC_PUMPS)
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> STEAM_ROBOT_ARM = REGISTRATE
            .item("steam_robot_arm", ComponentItem::new)
            .lang("Steam Robot Arm")
            .onRegister(attach(new CoverPlaceBehavior(CosmicCovers.STEAM_ROBOT_ARM)))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.cosmiccore.steam_robot_arm.tooltip"));
                lines.add(Component.translatable("cosmiccore.universal.tooltip.item_transfer_rate",
                        CosmicCovers.STEAM_ITEM_TRANSFER_RATE));
            })))
            .tag(CustomTags.ROBOT_ARMS)
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> STEAM_FLUID_REGULATOR = REGISTRATE
            .item("steam_fluid_regulator", ComponentItem::new)
            .lang("Steam Fluid Regulator")
            .onRegister(attach(new CoverPlaceBehavior(CosmicCovers.STEAM_FLUID_REGULATOR)))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.translatable("item.cosmiccore.steam_fluid_regulator.tooltip"));
                lines.add(Component.translatable("cosmiccore.universal.tooltip.fluid_transfer_rate",
                        CosmicCovers.STEAM_FLUID_TRANSFER_RATE));
            })))
            .tag(CustomTags.FLUID_REGULATORS)
            .defaultModel()
            .register();

    private CosmicCoverItems() {}

    public static void init() {}
}
