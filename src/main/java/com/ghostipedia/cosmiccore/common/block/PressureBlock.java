package com.ghostipedia.cosmiccore.common.block;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.block.IPressureType;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.ActiveBlock;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.client.renderer.block.TextureOverrideRenderer;
import com.gregtechceu.gtceu.utils.GTUtil;
import com.lowdragmc.lowdraglib.Platform;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Map;

@ParametersAreNonnullByDefault
public class PressureBlock extends ActiveBlock {

    public IPressureType pressureBlock;

    public PressureBlock(Properties properties, IPressureType pressureType) {
        super(properties, Platform.isClient() ? new TextureOverrideRenderer(new ResourceLocation("block/cube_all"),
                        Map.of("all", pressureType.getTexture())) : null,
                Platform.isClient() ? new TextureOverrideRenderer(GTCEu.id("block/cube_2_layer_all"),
                        Map.of("bot_all", pressureType.getTexture(),
                                "top_all", new ResourceLocation(pressureType.getTexture() + "_bloom"))) : null);
        this.pressureBlock = pressureType;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (GTUtil.isShiftDown()) {
            tooltip.add(Component.translatable("cosmiccore.wire_coil.magnet_stats"));
            tooltip.add(Component.translatable("cosmiccore.wire_coil.magnet_capacity", pressureBlock.getPressureVesselCapacity()));
            tooltip.add(Component.translatable("cosmiccore.wire_coil.magnet_regen", pressureBlock.getVarietyMultiplier()));

        } else {
            tooltip.add(Component.translatable("block.gtceu.wire_coil.tooltip_extended_info"));
        }
    }

    @Getter
    public enum PressureType implements StringRepresentable, IPressureType {
        YGGDRASIL_REALITY_COMPRESSION_VESSEL("yggdrasil_reality_compression", 2500000, 512, CosmicMaterials.PsionicGalvorn, CosmicCore.id("block/casings/pressure/yggdrasil_reality_compression")),
        PLANET_GRADE_VESSEL("planet_grade_pressure", 2500000, 512, CosmicMaterials.PsionicGalvorn, CosmicCore.id("block/casings/pressure/vessel_planet_grade"));

        @NotNull
        @Getter
        private final String name;
        @Getter
        private final int pressureVesselCapacity;
        @Getter
        private final int varietyMultiplier;
        @Getter
        private final Material material;
        @NotNull
        @Getter
        private final ResourceLocation texture;

        PressureType(String name, int pressureVesselCapacity, int varietyMultiplier, Material material, ResourceLocation texture) {
            this.name = name;
            this.pressureVesselCapacity = pressureVesselCapacity;
            this.varietyMultiplier = varietyMultiplier;
            this.material = material;
            this.texture = texture;
        }

        @NotNull
        @Override
        public String toString() {
            return getName();
        }

        @Override
        @NotNull
        public String getSerializedName() {
            return name;
        }
    }


}

