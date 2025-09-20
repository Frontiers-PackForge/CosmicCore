package com.ghostipedia.cosmiccore.common.item.tcon;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.CosmicItems;
import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;
import slimeknights.tconstruct.library.tools.item.IModifiable;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;

import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;

public class CosmicCreativeTinkersTab {




    public static void addCreativeTabItems(CreativeModeTab.ItemDisplayParameters itemDisplayParameters,
                                           CreativeModeTab.Output tab) {
        Consumer<ItemStack> output = tab::accept;
        acceptTool(output, CosmicTinkerTools.wireCutter);
        acceptTool(output, CosmicTinkerTools.wrench);

        acceptPart(output, CosmicTinkerToolPart.wrenchHead);


    }

    private static void acceptTool(Consumer<ItemStack> output, Supplier<? extends IModifiable> tool) {
        ToolBuildHandler.addVariants(output, tool.get(), "");
    }

    public static void acceptPart(Consumer<ItemStack> output, Supplier<? extends IMaterialItem> item){
        item.get().addVariants(output, "");
    }



    private static void acceptTools(Consumer<ItemStack> output, EnumObject<?, ? extends IModifiable> tools) {
        tools.forEach(tool -> ToolBuildHandler.addVariants(output, tool, ""));
    }
}
