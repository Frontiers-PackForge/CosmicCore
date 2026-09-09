package com.ghostipedia.cosmiccore.common.compat.gtceu;

import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockControllerMachine;
import com.gregtechceu.gtceu.common.machine.trait.EnvironmentalExplosionTrait;

import java.util.ArrayList;
import java.util.List;

public final class RainSealant {

    private RainSealant() {}

    public static boolean vulnerable(MetaMachine machine) {
        return machine.getAllTraits().stream().anyMatch(trait -> trait instanceof EnvironmentalExplosionTrait);
    }

    public static boolean sealed(MetaMachine machine) {
        return machine instanceof RainSealable sealable && sealable.cosmiccore$isRainSealed();
    }

    public static List<MetaMachine> targets(MetaMachine machine) {
        var result = new ArrayList<MetaMachine>();
        if (machine instanceof MultiblockControllerMachine controller && controller.isFormed()) {
            result.add(controller);
            for (var part : controller.getParts()) {
                if (vulnerable(part)) result.add(part);
            }
        } else if (vulnerable(machine)) {
            result.add(machine);
        }
        return result;
    }
}
