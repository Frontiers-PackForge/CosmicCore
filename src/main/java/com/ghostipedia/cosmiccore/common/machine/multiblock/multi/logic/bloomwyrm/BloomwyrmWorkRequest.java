package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm;

import com.gregtechceu.gtceu.api.recipe.GTRecipe;

public record BloomwyrmWorkRequest(
                                   GTRecipe recipe,
                                   int requestedParallel,
                                   int baseParallel,
                                   int eligibleParallel,
                                   long eutPerParallel,
                                   int biopowerInputPerParallel,
                                   int biopowerOutputPerParallel,
                                   long chargeInputPerParallel,
                                   long chargeOutputPerParallel,
                                   long seasonalChargeInputPerParallel,
                                   long seasonalChargeOutputPerParallel,
                                   BloomwyrmSeason favoredSeason) {}
