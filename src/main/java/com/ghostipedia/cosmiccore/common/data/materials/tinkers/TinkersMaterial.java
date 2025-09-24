package com.ghostipedia.cosmiccore.common.data.materials.tinkers;

import net.minecraft.world.item.Tier;

import lombok.Getter;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.util.LazyModifier;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.tools.stats.*;

import java.util.*;

@Getter
public class TinkersMaterial {

    public static final Set<TinkersMaterial> MATERIALS = new HashSet<>();
    private String name;
    private Set<LazyModifier> defaultTraits = new HashSet<>();
    private Map<MaterialStatsId, Set<ModifierEntry>> traits = new HashMap<>();
    private int materialValue;
    private HeadMaterialStats headMaterialStats;
    private HandleMaterialStats handleMaterialStats;
    private GripMaterialStats gripMaterialStats;
    private Set<StatlessMaterialStats> statlessMaterialStats = new HashSet<>();
    private int sortOrder;
    private boolean craftable;
    private int tier;

    public MaterialId getMaterialLocation() {
        return new MaterialId("cosmiccore", this.name);
    }

    private TinkersMaterial(Builder builder) {
        this.name = builder.name;
        this.materialValue = builder.materialValue;
        this.headMaterialStats = builder.headMaterialStats;
    }

    public static final class Builder {
        private  boolean craftable = false;
        private  int tier = 0;
        private String name;
        private Set<LazyModifier> modifiers = new HashSet<>();
        private int materialValue = 1;
        private int sortOrder = 0;
        private HeadMaterialStats headMaterialStats;
        private HandleMaterialStats handleMaterialStats;
        private GripMaterialStats gripMaterialStats;
        private Set<StatlessMaterialStats> statlessMaterialStats = new HashSet<>();
        private SkullStats skullStats;
        private ToolStats toolStats;
        private Set<LazyModifier> defaultTraits;
        private Map<MaterialStatsId, List<ModifierEntry>> statSpecificTraits = new HashMap<>();

        public Builder(String name) {
            this.name = name;
        }

        public Builder tier(int tier) {
            this.tier = tier;
            return this;
        }

        public Builder craftable(boolean craftable) {
            this.craftable = craftable;
            return this;
        }

        public Builder modifier(LazyModifier modifier) {
            modifiers.add(modifier);
            return this;
        }

        public Builder materialValue(int materialValue) {
            this.materialValue = materialValue;
            return this;
        }

        public Builder sortOrder(int sortOrder) {
            this.sortOrder = sortOrder;
            return this;
        }

        public Builder headMaterialStats(int durability, float miningSpeed, Tier miningLevel, float attack) {
            this.headMaterialStats = new HeadMaterialStats(durability, miningSpeed, miningLevel, attack);
            return this;
        }

        public Builder handleMaterialStats(float durability, float attackDamage, float attackSpeed, float miningSpeed) {
            this.handleMaterialStats = new HandleMaterialStats(durability, attackDamage, attackSpeed, miningSpeed);
            return this;
        }

        public Builder gripMaterialStats(float durability, float attackSpeed, float meleeAttack) {
            this.gripMaterialStats = new GripMaterialStats(durability, attackSpeed, meleeAttack);
            return this;
        }

        public Builder addStatlessType(StatlessMaterialStats type) {
            this.statlessMaterialStats.add(type);
            return this;
        }

        public Builder toolStats(ToolStats toolStats) {
            this.toolStats = toolStats;
            return this;
        }

        /**
         * Adds a simple, level 1 trait. Corresponds to addDefaultTraits().
         *
         * @param modifier The modifier to add as a default trait.
         * @return The builder instance.
         */
        public Builder defaultTrait(LazyModifier modifier) {
            this.defaultTraits.add(modifier);
            return this;
        }

        /**
         * Adds a trait with a specific level. Corresponds to addTraits().
         *
         * @param modifier The modifier to add as a leveled trait.
         * @param level    The level of the modifier.
         * @return The builder instance.
         */
        public Builder trait(LazyModifier modifier, int level, MaterialStatsId statsId) {
            List<ModifierEntry> traits = this.statSpecificTraits.computeIfAbsent(statsId, k -> new ArrayList<>());
            traits.add(new ModifierEntry(modifier.get(), level));
            return this;
        }

        public TinkersMaterial build() {
            TinkersMaterial material = new TinkersMaterial(this);
            TinkersMaterial.MATERIALS.add(material);
            return material;
        }
    }
}
