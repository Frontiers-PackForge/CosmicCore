package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;
import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.data.GTMaterials;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class CosmicCoilBlock {
    public enum CoilType implements StringRepresentable, ICoilType {
        PRISMATIC_TUNGSTENSTEEL("prismatic_tungstensteel", 4500, 3, 4, 2, () -> CosmicMaterials.PrismaticTungstensteel, CosmicCore.id("block/casings/coils/prismatic_tungstensteel")),
        SHIMMERING_NEUTRONIUM("shimmering_neutronium", 19840, 8, 20, 10, () -> null, CosmicCore.id("block/casings/coils/shimmering_neutronium"));

        @NotNull
        @Getter
        private final String name;
        //electric blast furnace properties
        @Getter
        private final int coilTemperature;
        @Getter
        private final int tier;
        //multi smelter properties
        @Getter
        private final int level;
        @Getter
        private final int energyDiscount;
        @NotNull
        private final Supplier<Material> material;
        @NotNull @Getter
        private final ResourceLocation texture;

        CoilType(String name, int coilTemperature, int tier, int level, int energyDiscount, Supplier<Material> material, ResourceLocation texture) {
            this.name = name;
            this.coilTemperature = coilTemperature;
            this.tier = tier;
            this.level = level;
            this.energyDiscount = energyDiscount;
            this.material = material;
            this.texture = texture;
        }

        public Material getMaterial() {
            return material.get();
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
