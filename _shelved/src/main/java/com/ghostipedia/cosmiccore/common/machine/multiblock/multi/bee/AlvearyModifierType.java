package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.bee;

import lombok.Getter;

@Getter
public enum AlvearyModifierType {

    // Climate
    HEATER("heater", "Heater"),
    COOLER("cooler", "Cooler"),
    HUMIDIFIER("humidifier", "Humidifier"),
    DRYER("dryer", "Dryer"),
    // Production
    PRODUCTIVITY("productivity", "Productivity"),
    SIEVE("sieve", "Sieve"),
    WEATHERPROOF("weatherproof", "Weatherproof"),
    LIGHTING("lighting", "Lighting"),
    // Breeding
    MUTAGENIC("mutagenic", "Mutagenic"),
    ACCELERANT("accelerant", "Accelerant"),
    LONGEVITY("longevity", "Longevity"),
    STABILISER("stabiliser", "Stabiliser"),
    // Utility
    TERRITORY("territory", "Territory"),
    SEALING("sealing", "Sealing");

    private final String id;
    private final String displayName;

    AlvearyModifierType(String id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }
}
