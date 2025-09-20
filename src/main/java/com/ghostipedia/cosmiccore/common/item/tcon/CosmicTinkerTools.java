package com.ghostipedia.cosmiccore.common.item.tcon;

import com.ghostipedia.cosmiccore.CosmicCore;

import net.minecraft.world.item.Item;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.tconstruct.common.registration.ItemDeferredRegisterExtension;
import slimeknights.tconstruct.library.tools.item.ModifiableItem;

public class CosmicTinkerTools {

    protected static final ItemDeferredRegisterExtension COSMIC_TINKER_ITEM = new ItemDeferredRegisterExtension(
            CosmicCore.MOD_ID);

    private static final Item.Properties UNSTACKABLE_PROPS = new Item.Properties().stacksTo(1);

    public static final ItemObject<ModifiableItem> wireCutter = COSMIC_TINKER_ITEM.register("wire_cutter",
            () -> new ModifiableItem(UNSTACKABLE_PROPS, CosmicToolDefinitions.WIRE_CUTTERS));
    public static final ItemObject<ModifiableItem> wrench = COSMIC_TINKER_ITEM.register("wrench",
            () -> new ModifiableItem(UNSTACKABLE_PROPS, CosmicToolDefinitions.WRENCHES));

    public static void init() {
        COSMIC_TINKER_ITEM.register(FMLJavaModLoadingContext.get().getModEventBus());
    }
}
