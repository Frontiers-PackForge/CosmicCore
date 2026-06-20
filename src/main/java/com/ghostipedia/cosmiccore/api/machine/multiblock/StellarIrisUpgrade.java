package com.ghostipedia.cosmiccore.api.machine.multiblock;

import java.util.EnumSet;
import java.util.Set;
import java.util.function.Supplier;

public enum StellarIrisUpgrade {

    // IGNITION - Star Lifecycle
    THERMAL_STABILIZER(Branch.IGNITION, 1, 15, 1, () -> Set.of()),
    PLASMA_CONDUITS(Branch.IGNITION, 1, 15, 1, () -> Set.of()),
    FUSION_CATALYST(Branch.IGNITION, 2, 25, 1, () -> Set.of(THERMAL_STABILIZER, PLASMA_CONDUITS)),
    MAGNETIC_CONFINEMENT(Branch.IGNITION, 2, 25, 1, () -> Set.of(THERMAL_STABILIZER, PLASMA_CONDUITS)),
    CORE_HARMONICS(Branch.IGNITION, 3, 40, 1, () -> Set.of(MAGNETIC_CONFINEMENT)),
    PROTON_RECYCLER(Branch.IGNITION, 3, 40, 1, () -> Set.of(FUSION_CATALYST)),
    STELLAR_REGENERATION(Branch.IGNITION, 4, 60, 1, () -> Set.of(CORE_HARMONICS, PROTON_RECYCLER)),
    ETERNAL_EMBER(Branch.IGNITION, 4, 60, 1, () -> Set.of(CORE_HARMONICS, PROTON_RECYCLER)),
    PHOENIX_PROTOCOL(Branch.IGNITION, 5, 80, 1, () -> Set.of(STELLAR_REGENERATION)),
    SOLAR_DOMINION(Branch.IGNITION, 5, 80, 1, () -> Set.of(ETERNAL_EMBER)),
    PERPETUAL_IGNITION(Branch.IGNITION, 6, 100, 1, () -> Set.of(PHOENIX_PROTOCOL, SOLAR_DOMINION)),
    SUPERNOVA_CORE(Branch.IGNITION, 7, 130, 1, () -> Set.of(PERPETUAL_IGNITION)),
    PLASMA_HURRICANE(Branch.IGNITION, 7, 130, 1, () -> Set.of(PERPETUAL_IGNITION)),
    STELLAR_NURSERY(Branch.IGNITION, 8, 170, 1, () -> Set.of(SUPERNOVA_CORE)),
    CORONA_EXPANSION(Branch.IGNITION, 8, 170, 1, () -> Set.of(PLASMA_HURRICANE)),
    HELIOS_FORGE(Branch.IGNITION, 9, 220, 1, () -> Set.of(STELLAR_NURSERY, CORONA_EXPANSION)),
    FUSION_OVERDRIVE(Branch.IGNITION, 9, 220, 1, () -> Set.of(STELLAR_NURSERY, CORONA_EXPANSION)),
    DYSON_LATTICE(Branch.IGNITION, 10, 280, 1, () -> Set.of(HELIOS_FORGE)),
    SOLAR_GENESIS(Branch.IGNITION, 10, 280, 1, () -> Set.of(FUSION_OVERDRIVE)),
    PRIMORDIAL_FLAME(Branch.IGNITION, 11, 350, 1, () -> Set.of(DYSON_LATTICE, SOLAR_GENESIS)),

    // FUSION - Processing Power
    GRAVITON_LENS(Branch.FUSION, 1, 15, 1, () -> Set.of()),
    SUPERCONDUCTING_GRID(Branch.FUSION, 1, 15, 1, () -> Set.of()),
    TEMPORAL_ACCELERATION(Branch.FUSION, 2, 25, 1, () -> Set.of(GRAVITON_LENS, SUPERCONDUCTING_GRID)),
    PARALLEL_MANIFOLD(Branch.FUSION, 2, 25, 1, () -> Set.of(GRAVITON_LENS, SUPERCONDUCTING_GRID)),
    STELLAR_COMPRESSION(Branch.FUSION, 3, 40, 1, () -> Set.of(TEMPORAL_ACCELERATION)),
    MASS_EFFICIENCY(Branch.FUSION, 3, 40, 1, () -> Set.of(SUPERCONDUCTING_GRID, PARALLEL_MANIFOLD)),
    RELATIVISTIC_PROCESSING(Branch.FUSION, 4, 60, 1, () -> Set.of(STELLAR_COMPRESSION)),
    QUANTUM_TUNNELING(Branch.FUSION, 4, 60, 1, () -> Set.of(STELLAR_COMPRESSION, MASS_EFFICIENCY)),
    HYPERDENSE_CORE(Branch.FUSION, 5, 80, 1, () -> Set.of(RELATIVISTIC_PROCESSING)),
    TACHYON_WEAVE(Branch.FUSION, 5, 80, 1, () -> Set.of(QUANTUM_TUNNELING)),
    SINGULARITY_ENGINE(Branch.FUSION, 6, 100, 1, () -> Set.of(HYPERDENSE_CORE, TACHYON_WEAVE)),
    NEUTRON_CASCADE(Branch.FUSION, 7, 130, 1, () -> Set.of(SINGULARITY_ENGINE)),
    WARP_FIELD_MATRIX(Branch.FUSION, 7, 130, 1, () -> Set.of(SINGULARITY_ENGINE)),
    PARTICLE_STORM(Branch.FUSION, 8, 170, 1, () -> Set.of(NEUTRON_CASCADE)),
    SUBSPACE_HARMONICS(Branch.FUSION, 8, 170, 1, () -> Set.of(WARP_FIELD_MATRIX)),
    ANTIMATTER_INJECTION(Branch.FUSION, 9, 220, 1, () -> Set.of(PARTICLE_STORM, SUBSPACE_HARMONICS)),
    ZERO_POINT_TAP(Branch.FUSION, 9, 220, 1, () -> Set.of(PARTICLE_STORM, SUBSPACE_HARMONICS)),
    QUARK_GLUON_PLASMA(Branch.FUSION, 10, 280, 1, () -> Set.of(ANTIMATTER_INJECTION)),
    PLANCK_RESONANCE(Branch.FUSION, 10, 280, 1, () -> Set.of(ZERO_POINT_TAP)),
    OMEGA_COMPRESSION(Branch.FUSION, 11, 350, 1, () -> Set.of(QUARK_GLUON_PLASMA, PLANCK_RESONANCE)),

    // COLLAPSE - Prestige
    SHARD_COLLECTOR(Branch.COLLAPSE, 1, 15, 1, () -> Set.of()),
    RESONANT_SACRIFICE(Branch.COLLAPSE, 1, 15, 1, () -> Set.of()),
    EARLY_HARVEST(Branch.COLLAPSE, 2, 25, 1, () -> Set.of(SHARD_COLLECTOR, RESONANT_SACRIFICE)),
    EFFICIENT_CONSUMPTION(Branch.COLLAPSE, 2, 25, 1, () -> Set.of(SHARD_COLLECTOR, RESONANT_SACRIFICE)),
    POINT_AMPLIFIER(Branch.COLLAPSE, 3, 40, 1, () -> Set.of(SHARD_COLLECTOR, EARLY_HARVEST)),
    DUAL_SACRIFICE(Branch.COLLAPSE, 3, 40, 1, () -> Set.of(RESONANT_SACRIFICE, EFFICIENT_CONSUMPTION)),
    PRESTIGE_MOMENTUM(Branch.COLLAPSE, 4, 60, 1, () -> Set.of(POINT_AMPLIFIER)),
    ECHO_OF_COLLAPSE(Branch.COLLAPSE, 5, 80, 1, () -> Set.of(PRESTIGE_MOMENTUM)),
    ENTROPY_HARVEST(Branch.COLLAPSE, 5, 80, 1, () -> Set.of(DUAL_SACRIFICE)),
    INFINITE_RECURSION(Branch.COLLAPSE, 6, 100, 1, () -> Set.of(ECHO_OF_COLLAPSE, ENTROPY_HARVEST)),
    CASCADING_COLLAPSE(Branch.COLLAPSE, 7, 130, 1, () -> Set.of(INFINITE_RECURSION)),
    TEMPORAL_ECHO(Branch.COLLAPSE, 7, 130, 1, () -> Set.of(INFINITE_RECURSION)),
    MASS_CONVERSION(Branch.COLLAPSE, 8, 170, 1, () -> Set.of(CASCADING_COLLAPSE)),
    STELLAR_DEBT(Branch.COLLAPSE, 8, 170, 1, () -> Set.of(TEMPORAL_ECHO)),
    ENTROPY_ENGINE(Branch.COLLAPSE, 9, 220, 1, () -> Set.of(MASS_CONVERSION, STELLAR_DEBT)),
    SACRIFICE_AMPLIFIER(Branch.COLLAPSE, 9, 220, 1, () -> Set.of(MASS_CONVERSION, STELLAR_DEBT)),
    COSMIC_TITHE(Branch.COLLAPSE, 10, 280, 1, () -> Set.of(ENTROPY_ENGINE)),
    ANNIHILATION_YIELD(Branch.COLLAPSE, 10, 280, 1, () -> Set.of(SACRIFICE_AMPLIFIER)),
    HEAT_DEATH(Branch.COLLAPSE, 11, 350, 1, () -> Set.of(COSMIC_TITHE, ANNIHILATION_YIELD)),

    // VOID - Black Hole
    HAWKING_RADIATOR(Branch.VOID, 1, 15, 1, () -> Set.of()),
    CHROMATIC_TUNING(Branch.VOID, 1, 15, 1, () -> Set.of()),
    EXOTIC_MATTER_TAP(Branch.VOID, 2, 25, 1, () -> Set.of(HAWKING_RADIATOR, CHROMATIC_TUNING)),
    VOID_WHISPERS(Branch.VOID, 2, 25, 1, () -> Set.of(HAWKING_RADIATOR)),
    EVENT_HORIZON_LOCK(Branch.VOID, 3, 40, 1, () -> Set.of(VOID_WHISPERS)),
    SINGULARITY_SIPHON(Branch.VOID, 3, 40, 1, () -> Set.of(EXOTIC_MATTER_TAP)),
    GRAVITATIONAL_MASTERY(Branch.VOID, 4, 60, 1, () -> Set.of(SINGULARITY_SIPHON)),
    VOID_HARVESTER(Branch.VOID, 4, 60, 1, () -> Set.of(EVENT_HORIZON_LOCK)),
    ELDRITCH_INSIGHT(Branch.VOID, 5, 80, 1, () -> Set.of(GRAVITATIONAL_MASTERY)),
    ABYSS_WALKER(Branch.VOID, 5, 80, 1, () -> Set.of(VOID_HARVESTER)),
    ETERNAL_VOID(Branch.VOID, 6, 100, 1, () -> Set.of(ELDRITCH_INSIGHT, ABYSS_WALKER)),
    DARK_MATTER_LENS(Branch.VOID, 7, 130, 1, () -> Set.of(ETERNAL_VOID)),
    NEGATIVE_MASS(Branch.VOID, 7, 130, 1, () -> Set.of(ETERNAL_VOID)),
    VACUUM_DECAY(Branch.VOID, 8, 170, 1, () -> Set.of(DARK_MATTER_LENS)),
    PHOTON_SPHERE(Branch.VOID, 8, 170, 1, () -> Set.of(NEGATIVE_MASS)),
    SCHWARZSCHILD_RADIUS(Branch.VOID, 9, 220, 1, () -> Set.of(VACUUM_DECAY, PHOTON_SPHERE)),
    ERGOSPHERE_TAP(Branch.VOID, 9, 220, 1, () -> Set.of(VACUUM_DECAY, PHOTON_SPHERE)),
    PENROSE_PROCESS(Branch.VOID, 10, 280, 1, () -> Set.of(SCHWARZSCHILD_RADIUS)),
    KERR_EXTRACTION(Branch.VOID, 10, 280, 1, () -> Set.of(ERGOSPHERE_TAP)),
    FALSE_VACUUM(Branch.VOID, 11, 350, 1, () -> Set.of(PENROSE_PROCESS, KERR_EXTRACTION)),

    // REPEATABLE
    STELLAR_EFFICIENCY(Branch.REPEATABLE, 0, 10, 10, () -> Set.of()),
    PARALLEL_THREADING(Branch.REPEATABLE, 0, 15, 8, () -> Set.of()),
    ENERGY_OPTIMIZATION(Branch.REPEATABLE, 0, 12, 10, () -> Set.of()),
    FUEL_EFFICIENCY(Branch.REPEATABLE, 0, 10, 10, () -> Set.of()),
    PRESTIGE_AMPLIFIER(Branch.REPEATABLE, 0, 8, 10, () -> Set.of()),
    DECAY_RESISTANCE(Branch.REPEATABLE, 0, 10, 10, () -> Set.of()),
    GROWTH_CATALYST(Branch.REPEATABLE, 0, 10, 10, () -> Set.of()),
    VOID_ATTUNEMENT(Branch.REPEATABLE, 0, 12, 10, () -> Set.of());

    public enum Branch {
        IGNITION,
        FUSION,
        COLLAPSE,
        VOID,
        REPEATABLE
    }

    private final Branch branch;
    private final int row;
    private final int baseCost;
    private final int maxLevel;
    private final Supplier<Set<StellarIrisUpgrade>> prerequisitesSupplier;
    private Set<StellarIrisUpgrade> prerequisites;

    StellarIrisUpgrade(Branch branch, int row, int cost, int maxLevel, Supplier<Set<StellarIrisUpgrade>> prereqs) {
        this.branch = branch;
        this.row = row;
        this.baseCost = cost;
        this.maxLevel = maxLevel;
        this.prerequisitesSupplier = prereqs;
    }

    public Branch getBranch() {
        return branch;
    }

    public int getRow() {
        return row;
    }

    public int getCost() {
        return baseCost;
    }

    public int getCostForLevel(int level) {
        if (!isRepeatable() || level <= 1) return baseCost;
        return (int) Math.ceil(baseCost * Math.pow(1.5, level - 1));
    }

    public int getTotalCostForLevel(int targetLevel) {
        int total = 0;
        for (int i = 1; i <= targetLevel; i++) {
            total += getCostForLevel(i);
        }
        return total;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public boolean isRepeatable() {
        return branch == Branch.REPEATABLE;
    }

    public Set<StellarIrisUpgrade> getPrerequisites() {
        if (prerequisites == null) {
            prerequisites = prerequisitesSupplier.get();
        }
        return prerequisites;
    }

    public int getRequiredTier() {
        return isRepeatable() ? 0 : Math.max(0, row - 1);
    }

    public boolean isCapstone() {
        return row == 11 && !isRepeatable();
    }

    public boolean canUnlock(Set<StellarIrisUpgrade> owned, int currentTier) {
        if (isRepeatable()) return true;
        if (currentTier < getRequiredTier()) return false;
        if (owned.contains(this)) return false;

        Set<StellarIrisUpgrade> prereqs = getPrerequisites();
        if (prereqs.isEmpty()) return true;

        if (row <= 2) {
            for (StellarIrisUpgrade prereq : prereqs) {
                if (owned.contains(prereq)) return true;
            }
            return false;
        }
        return owned.containsAll(prereqs);
    }

    public String getTranslationKey() {
        return "cosmiccore.stellar.upgrade." + name().toLowerCase();
    }

    public String getDescriptionKey() {
        return getTranslationKey() + ".desc";
    }

    public static Set<StellarIrisUpgrade> getByBranch(Branch branch) {
        EnumSet<StellarIrisUpgrade> result = EnumSet.noneOf(StellarIrisUpgrade.class);
        for (StellarIrisUpgrade upgrade : values()) {
            if (upgrade.branch == branch) result.add(upgrade);
        }
        return result;
    }

    public static Set<StellarIrisUpgrade> getByRow(int row) {
        EnumSet<StellarIrisUpgrade> result = EnumSet.noneOf(StellarIrisUpgrade.class);
        for (StellarIrisUpgrade upgrade : values()) {
            if (upgrade.row == row && !upgrade.isRepeatable()) result.add(upgrade);
        }
        return result;
    }

    public static StellarIrisUpgrade getCapstone(Branch branch) {
        for (StellarIrisUpgrade upgrade : values()) {
            if (upgrade.branch == branch && upgrade.isCapstone()) return upgrade;
        }
        return null;
    }

    public static Set<StellarIrisUpgrade> getRepeatables() {
        return getByBranch(Branch.REPEATABLE);
    }

    public static int getTotalCost() {
        int total = 0;
        for (StellarIrisUpgrade upgrade : values()) {
            if (!upgrade.isRepeatable()) total += upgrade.baseCost;
        }
        return total;
    }
}
