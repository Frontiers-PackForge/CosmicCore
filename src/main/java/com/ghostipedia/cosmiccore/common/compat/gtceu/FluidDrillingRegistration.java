package com.ghostipedia.cosmiccore.common.compat.gtceu;

import com.gregtechceu.gtceu.api.machine.multiblock.PartAbility;
import com.gregtechceu.gtceu.api.multiblock.pattern.MultiblockPatternBuilder;
import com.gregtechceu.gtceu.common.data.machines.GTMultiMachines;
import com.gregtechceu.gtceu.common.machine.multiblock.electric.FluidDrillMachine;
import com.gregtechceu.gtceu.utils.memoization.GTMemoizer;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import static com.gregtechceu.gtceu.api.multiblock.Predicates.*;
import static com.gregtechceu.gtceu.api.multiblock.util.RelativeDirection.*;

public final class FluidDrillingRegistration {

    private FluidDrillingRegistration() {}

    public static void init() {
        for (var definition : GTMultiMachines.FLUID_DRILLING_RIG) {
            if (definition == null) continue;
            int tier = definition.getTier();
            var previous = definition.getTooltipBuilder();
            definition.setTooltipBuilder((stack, lines) -> {
                if (previous != null) previous.accept(stack, lines);
                lines.removeIf(line -> line.getContents() instanceof TranslatableContents text &&
                        text.getKey().equals("gtceu.machine.fluid_drilling_rig.depletion"));
                lines.add(Component.translatable("cosmiccore.fluid_drill.area.tooltip", 2 * (tier - 1) + 1));
                lines.add(Component.translatable("cosmiccore.fluid_drill.hatches"));
            });
            definition.setPattern("main", GTMemoizer.memoize(() -> MultiblockPatternBuilder.start(FRONT, UP, RIGHT)
                    .slice("XXX", "#F#", "#F#", "#F#", "###", "###", "###")
                    .slice("XXX", "FCF", "FCF", "FCF", "#F#", "#F#", "#F#")
                    .slice("XSX", "#F#", "#F#", "#F#", "###", "###", "###")
                    .where('S', controller(blocks(definition.get())))
                    .where('X', blocks(FluidDrillMachine.getCasingState(tier)).setMinGlobalLimited(3)
                            .and(abilities(PartAbility.INPUT_ENERGY).setMinGlobalLimited(1).setMaxGlobalLimited(2))
                            .and(abilities(PartAbility.EXPORT_FLUIDS, PartAbility.EXPORT_FLUIDS_4X)
                                    .setMinGlobalLimited(1)))
                    .where('C', blocks(FluidDrillMachine.getCasingState(tier)))
                    .where('F', frames(FluidDrillMachine.getFrameMaterial(tier)))
                    .where('#', any()).build()));
        }
    }
}
