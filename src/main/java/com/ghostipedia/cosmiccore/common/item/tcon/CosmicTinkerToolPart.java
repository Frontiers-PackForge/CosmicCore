package com.ghostipedia.cosmiccore.common.item.tcon;

import com.ghostipedia.cosmiccore.CosmicCore;
import net.minecraft.world.item.Item;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.registration.ItemDeferredRegisterExtension;
import slimeknights.tconstruct.library.tools.part.ToolPartItem;
import slimeknights.tconstruct.tools.stats.HeadMaterialStats;

public class CosmicTinkerToolPart {

    //Tinkers uses Protected in their Registry class to seal it, we should do the same to avoid conflicts!!
    protected static final ItemDeferredRegisterExtension COSMIC_TINKER_PARTS = new ItemDeferredRegisterExtension(CosmicCore.MOD_ID);
    protected static final Item.Properties ITEM_PROPS = new Item.Properties();

    public static final ItemObject<ToolPartItem> wrenchHead = COSMIC_TINKER_PARTS.register("wrench_head", () -> new ToolPartItem(ITEM_PROPS, HeadMaterialStats.ID));

    public static void init() {
        COSMIC_TINKER_PARTS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

}
