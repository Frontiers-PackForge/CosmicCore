package com.ghostipedia.cosmiccore.common.item.tcon;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.item.tcon.base.ChargableModifiableItem;
import com.ghostipedia.cosmiccore.common.item.tcon.modifiers.CosmicCoreModifiers;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.tconstruct.common.registration.ItemDeferredRegisterExtension;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;

public class CosmicTinkerTools {

    protected static final ItemDeferredRegisterExtension COSMIC_TINKER_ITEM = new ItemDeferredRegisterExtension(
            CosmicCore.MOD_ID);

    private static final Item.Properties UNSTACKABLE_PROPS = new Item.Properties().stacksTo(1);

    public static final ItemObject<ModifiableItem> file = COSMIC_TINKER_ITEM.register("file",
            () -> new ModifiableItem(UNSTACKABLE_PROPS, CosmicToolDefinitions.FILES));
    public static final ItemObject<ModifiableItem> saw = COSMIC_TINKER_ITEM.register("saw",
            () -> new ModifiableItem(UNSTACKABLE_PROPS, CosmicToolDefinitions.SAWS));
    public static final ItemObject<ModifiableItem> drill = COSMIC_TINKER_ITEM.register("drill",
            () -> new ModifiableItem(UNSTACKABLE_PROPS, CosmicToolDefinitions.DRILLS));
    public static final ItemObject<ModifiableItem> screwdriver = COSMIC_TINKER_ITEM.register("screwdriver",
            () -> new ModifiableItem(UNSTACKABLE_PROPS, CosmicToolDefinitions.SCREWDRIVERS));
    public static final ItemObject<ModifiableItem> softMallet = COSMIC_TINKER_ITEM.register("soft_mallet",
            () -> new ModifiableItem(UNSTACKABLE_PROPS, CosmicToolDefinitions.SOFT_MALLETS));
    public static final ItemObject<ModifiableItem> plunger = COSMIC_TINKER_ITEM.register("plunger",
            () -> new ModifiableItem(UNSTACKABLE_PROPS, CosmicToolDefinitions.PLUNGERS));
    public static final ItemObject<ModifiableItem> crowbar = COSMIC_TINKER_ITEM.register("crowbar",
            () -> new ModifiableItem(UNSTACKABLE_PROPS, CosmicToolDefinitions.CROWBARS));

    public static final ItemObject<ModifiableItem> wireCutter = COSMIC_TINKER_ITEM.register("wire_cutter",
            () -> new ModifiableItem(UNSTACKABLE_PROPS, CosmicToolDefinitions.WIRE_CUTTERS));

    public static final ItemObject<ModifiableItem> wrench = COSMIC_TINKER_ITEM.register("wrench",
            () -> new ChargableModifiableItem(UNSTACKABLE_PROPS, CosmicToolDefinitions.WRENCHES) {

                @Override
                public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
                    return true;
                }
            });

    public static void init() {
        COSMIC_TINKER_ITEM.register(FMLJavaModLoadingContext.get().getModEventBus());
        CosmicCoreModifiers.init();
    }
}
