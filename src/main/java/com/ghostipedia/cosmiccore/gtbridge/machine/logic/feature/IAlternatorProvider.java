package com.ghostipedia.cosmiccore.gtbridge.machine.logic.feature;

import com.gregtechceu.gtceu.api.machine.feature.IMachineFeature;
import com.ghostipedia.cosmiccore.gtbridge.machine.kinetic.Alternator;

import java.util.Set;


public interface IAlternatorProvider extends IMachineFeature {

    Set<Alternator> getTypes();
}
