package com.ghostipedia.cosmiccore.common.item.tcon;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicCreativeModeTabs;

import com.ghostipedia.cosmiccore.common.data.CosmicCreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.tconstruct.common.registration.ItemDeferredRegisterExtension;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.library.tools.part.ToolPartItem;
import slimeknights.tconstruct.tools.stats.HeadMaterialStats;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;

public class CosmicTinkerToolPart {

    static {
        REGISTRATE.creativeModeTab(() -> CosmicCreativeModeTabs.COSMIC_CORE_TINKERS_TOOLS);
    }
    // Tinkers uses Protected in their Registry class to seal it, we should do the same to avoid conflicts!!
    protected static final ItemDeferredRegisterExtension COSMIC_TINKER_PARTS = new ItemDeferredRegisterExtension(
            CosmicCore.MOD_ID);
    protected static final Item.Properties ITEM_PROPS = new Item.Properties();

    public static final ItemObject<ToolPartItem> wrenchHead = COSMIC_TINKER_PARTS.register("wrench_head",
            () -> new ToolPartItem(ITEM_PROPS, HeadMaterialStats.ID));

    public static final ItemObject<ToolPartItem> screwdriverHead = COSMIC_TINKER_PARTS.register("screwdriver_head",
            () -> new ToolPartItem(ITEM_PROPS, HeadMaterialStats.ID));

    public static void init() {
        COSMIC_TINKER_PARTS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private static void accept(Consumer<ItemStack> output, Supplier<? extends IMaterialItem> item) {
        item.get().addVariants(output, "");
    }
}
