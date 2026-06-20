package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.data.materials.CosmicMaterials;

import com.gregtechceu.gtceu.api.block.ICoilType;
import com.gregtechceu.gtceu.api.data.chemical.material.Material;
import com.gregtechceu.gtceu.common.block.CoilBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class CosmicCoilBlock extends CoilBlock implements EntityBlock {

    public CosmicCoilBlock(Properties properties, ICoilType coilType) {
        super(properties, coilType);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return CosmicBlocks.COIL_CAUSAL_FABRIC.getSibling(Registries.BLOCK_ENTITY_TYPE)
                .get().create(pos, state);
    }

    public enum CoilType implements StringRepresentable, ICoilType {

        PRISMATIC_TUNGSTENSTEEL("prismatic_tungstensteel", 4500, 3, 4, 2, () -> CosmicMaterials.PrismaticTungstensteel,
                CosmicCore.id("block/casings/coils/prismatic_tungstensteel")),
        RESONANT_VIRTUE_MELD("resonant_virtue_meld", 5400, 4, 5, 3, () -> CosmicMaterials.ResonantVirtueMeld,
                CosmicCore.id("block/casings/coils/resonant_virtue_meld")),
        NAQUADIC_SUPERALLOY("naquadric_superalloy", 7200, 5, 6, 4, () -> CosmicMaterials.NaquadicSuperalloy,
                CosmicCore.id("block/casings/coils/naquadric_superalloy")),
        TRINAVINE("reinforced_trinavine", 9500, 3, 7, 4, () -> CosmicMaterials.Trinavine,
                CosmicCore.id("block/casings/coils/trinavine")),
        LIVING_IGNICLAD("living_igniclad", 10800, 7, 8, 5, () -> CosmicMaterials.LivingIgniclad,
                CosmicCore.id("block/casings/coils/living_igniclad")),
        PSIONIC_GALVORN("psionic_galvorn", 12800, 8, 9, 6, () -> CosmicMaterials.PsionicGalvorn,
                CosmicCore.id("block/casings/coils/psionic_galvorn")),
        PROGRAMMABLE_MATTER("programable_matter", 15800, 9, 10, 7, () -> CosmicMaterials.ProgrammableMatter,
                CosmicCore.id("block/casings/coils/programable_matter")),
        SHIMMERING_NEUTRONIUM("shimmering_neutronium", 19840, 10, 11, 8, () -> CosmicMaterials.ShimmeringNeutronium,
                CosmicCore.id("block/casings/coils/shimmering_neutronium")),
        CAUSAL_FABRIC("causal_fabric", 36000, 11, 15, 9, () -> CosmicMaterials.CausalFabric,
                CosmicCore.id("block/casings/coils/causal_fabric"));

        @NotNull
        private final String name;
        // electric blast furnace properties
        private final int coilTemperature;
        private final int tier;
        // multi smelter properties
        private final int level;
        private final int energyDiscount;
        @NotNull
        private final Supplier<Material> material;
        @NotNull
        private final ResourceLocation texture;

        CoilType(String name, int coilTemperature, int tier, int level, int energyDiscount, Supplier<Material> material,
                 ResourceLocation texture) {
            this.name = name;
            this.coilTemperature = coilTemperature;
            this.tier = tier;
            this.level = level;
            this.energyDiscount = energyDiscount;
            this.material = material;
            this.texture = texture;
        }

        @NotNull
        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getCoilTemperature() {
            return coilTemperature;
        }

        @Override
        public int getTier() {
            return tier;
        }

        @Override
        public int getLevel() {
            return level;
        }

        @Override
        public int getEnergyDiscount() {
            return energyDiscount;
        }

        @NotNull
        @Override
        public ResourceLocation getTexture() {
            return texture;
        }

        @Override
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
