package com.ghostipedia.cosmiccore.common.block;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.block.IMagnetType;
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
public class MagnetBlock extends ActiveBlock {
    public IMagnetType magnetBlock;
    public MagnetBlock(Properties properties, IMagnetType magnetType) {
        super(properties, Platform.isClient() ? new TextureOverrideRenderer(new ResourceLocation("block/cube_all"),
                        Map.of("all", magnetType.getTexture())) : null,
                Platform.isClient() ? new TextureOverrideRenderer(GTCEu.id("block/cube_2_layer_all"),
                        Map.of("bot_all", magnetType.getTexture(),
                                "top_all", new ResourceLocation(magnetType.getTexture() + "_bloom"))) : null);
        this.magnetBlock = magnetType;
    }
    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        if (GTUtil.isShiftDown()) {
            tooltip.add(Component.translatable("cosmiccore.wire_coil.magnet_stats"));
            tooltip.add(Component.translatable("cosmiccore.wire_coil.magnet_capacity", magnetBlock.getMagnetFieldCapacity()));
            tooltip.add(Component.translatable("cosmiccore.wire_coil.magnet_regen", magnetBlock.getMagnetRegenRate()));
            tooltip.add(Component.translatable("cosmiccore.wire_coil.eu_multiplier",  magnetBlock.energyMultiplier()));

        } else {
            tooltip.add(Component.translatable("block.gtceu.wire_coil.tooltip_extended_info"));
        }
    }
    public enum MagnetType implements StringRepresentable, IMagnetType{
        HIGH_POWERED("high_powered",1500,100,3, CosmicMaterials.LivingIgniclad, CosmicCore.id("block/casings/solid/alternator_flux_coiling_copper")),
        FUSION_GRADE("fusion_grade",10000,500,3, CosmicMaterials.LivingIgniclad, CosmicCore.id("block/casings/solid/alternator_flux_coiling_copper"));

        @NotNull @Getter
        private final String name;
        @Getter
        private final int magnetStrength;
        @Getter
        private final int regenRate;
        @Getter
        private final int energyMultiplier;
        @Getter
        private final Material material;
        @NotNull @Getter
        private final ResourceLocation texture;
        MagnetType(String name, int magnetStrength, int regenRate, int energyMultiplier, Material material, ResourceLocation texture) {
            this.name = name;
            this.magnetStrength = magnetStrength;
            this.regenRate = regenRate;
            this.energyMultiplier = energyMultiplier;
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


        @Override
        public int getMagnetFieldCapacity() {
            return getMagnetStrength();
        }

        @Override
        public int getMagnetRegenRate() {
            return getRegenRate();
        }

        @Override
        public int energyMultiplier() {
            return getEnergyMultiplier();
        }

    }


}
