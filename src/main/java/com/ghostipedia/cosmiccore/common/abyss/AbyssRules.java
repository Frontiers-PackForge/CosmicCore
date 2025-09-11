package com.ghostipedia.cosmiccore.common.abyss;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;


public final class AbyssRules {

    private AbyssRules() {}

    //Dimension to Target.
    public static final ResourceKey<Level> DIM = ResourceKey.create(Registries.DIMENSION, new ResourceLocation("minecraft", "the_nether"));

    public static final long MAX_TICKS = 20L * 3600; //Max 1HR
    public static final long FIRST_ENTRY_TICKS = 20L * 600; // 10M
    public static final double REGEN_PER_SECOND = 0.25; // By Default Regen 1s of time per 4s without external intervention.
    public static final AbyssAction TIMEOUT = AbyssAction.DEATH;
    public static final int[] WARNINGS = {600,300,60,30,10,5};

    //Enum if I want to add extra behaviors *or* change it.
    public enum AbyssAction {EJECT, DEATH}

}
