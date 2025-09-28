package com.ghostipedia.cosmiccore.common.data.materials.tinkers;

import net.minecraft.world.item.Tier;

import lombok.Getter;
import net.minecraft.world.item.crafting.Ingredient;
import slimeknights.tconstruct.library.client.data.spritetransformer.IColorMapping;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.stats.IMaterialStats;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.modifiers.util.LazyModifier;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.tools.stats.*;

import java.util.*;
import java.util.function.Supplier;

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
    private Set<StatlessMaterialStats> statlessMaterialStats;
    private Map<MaterialStatsId, List<Supplier<ModifierEntry>>> statSpecificTraits = new HashMap<>();
    private List<IMaterialStats> stats;
    private int sortOrder;
    private boolean craftable;
    private int tier;
    private IColorMapping colorMapping;
    private int color;
    private  List<String> fallbacks;
    private Ingredient ingredient;
    private int value;
    private int needed;

    public MaterialId getMaterialLocation() {
        return new MaterialId("cosmiccore", this.name);
    }

    private TinkersMaterial(Builder builder) {
        this.name = builder.name;
        this.materialValue = builder.materialValue;
        this.headMaterialStats = builder.headMaterialStats;
        this.handleMaterialStats = builder.handleMaterialStats;
        this.gripMaterialStats = builder.gripMaterialStats;
        this.statlessMaterialStats = builder.statlessMaterialStats;
        this.sortOrder = builder.sortOrder;
        this.craftable = builder.craftable;
        this.tier = builder.tier;
        this.stats = builder.stats;
        this.defaultTraits = builder.defaultTraits;
        this.colorMapping = builder.colorMapping;
        this.traits = new HashMap<>();
        this.color = builder.color;
        this.fallbacks = new ArrayList<>(builder.fallbacks);
        this.ingredient = builder.ingredient;
        this.value = builder.value;
        this.needed = builder.needed;
    }

    public List<IMaterialStats> getStats() {
        return this.stats;
    }

    public static final class Builder {

        private IColorMapping colorMapping;
        private boolean craftable = false;
        private int tier = 0;
        private String name;
        private Set<Modifier> modifiers = new HashSet<>();
        private int materialValue = 1;
        private int sortOrder = 0;
        private final List<IMaterialStats> stats = new ArrayList<>();
        private HeadMaterialStats headMaterialStats;
        private HandleMaterialStats handleMaterialStats;
        private GripMaterialStats gripMaterialStats;
        private final Set<StatlessMaterialStats> statlessMaterialStats = new HashSet<>();
        private SkullStats skullStats;
        private ToolStats toolStats;
        private final Set<LazyModifier> defaultTraits = new HashSet<>();
        private final Map<MaterialStatsId, List<Supplier<ModifierEntry>>> statSpecificTraits = new HashMap<>();
        private int color;
        private final List<String> fallbacks = new ArrayList<>();
        private Ingredient ingredient;
        private int value;
        private int needed;


        public Builder(String name) {
            this.name = name;
        }

        public Builder Ingredient(Ingredient ingredient, int value, int needed) {
            this.ingredient = ingredient;
            this.value = value;
            this.needed = needed;
            return this;
        }

        public Builder tier(int tier) {
            this.tier = tier;
            return this;
        }

        public Builder color (int color){
            this.color = color;
            return this;
        }


        public Builder craftable(boolean craftable) {
            this.craftable = craftable;
            return this;
        }

        public Builder modifier(Modifier modifier) {
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
            this.stats.add(this.headMaterialStats);
            return this;
        }

        public Builder handleMaterialStats(float durability, float attackDamage, float attackSpeed, float miningSpeed) {
            this.handleMaterialStats = new HandleMaterialStats(durability, attackDamage, attackSpeed, miningSpeed);
            this.stats.add(this.handleMaterialStats);
            return this;
        }

        public Builder gripMaterialStats(float durability, float attackSpeed, float meleeAttack) {
            this.gripMaterialStats = new GripMaterialStats(durability, attackSpeed, meleeAttack);
            this.stats.add(this.gripMaterialStats);
            return this;
        }

        public Builder addStatlessType(StatlessMaterialStats type) {
            this.statlessMaterialStats.add(type);
            this.stats.add(type);
            return this;
        }

        public Builder toolStats(ToolStats toolStats) {
            this.toolStats = toolStats;
            return this;
        }

        public Builder defaultTrait(ModifierId modifier) {
            this.defaultTraits.add(new LazyModifier(modifier));
            return this;
        }

        public Builder defaultTrait(Modifier modifier) {
            this.defaultTraits.add(new LazyModifier(modifier.getId()));
            return this;
        }

        public Builder trait(Modifier modifier, int level, MaterialStatsId statsId) {
            List<Supplier<ModifierEntry>> traits = this.statSpecificTraits.computeIfAbsent(statsId, k -> new ArrayList<>());
            traits.add(() -> new ModifierEntry(modifier, level));
            return this;
        }
        public Builder trait(Supplier<ModifierEntry> modifier, MaterialStatsId statsId) {
            List<Supplier<ModifierEntry>> traits = this.statSpecificTraits.computeIfAbsent(statsId, k -> new ArrayList<>());
            traits.add(modifier);
            return this;
        }

        public Builder colorMapping(IColorMapping colorMapping) {
            this.colorMapping = colorMapping;
            return this;
        }

        public Builder fallback(String fallback) {
            this.fallbacks.add(fallback);
            return this;
        }

        public Builder fallbacks(Collection<String> fallbacks) {
            this.fallbacks.addAll(fallbacks);
            return this;
        }

        public TinkersMaterial build() {
            TinkersMaterial material = new TinkersMaterial(this);
            TinkersMaterial.MATERIALS.add(material);
            return material;
        }
    }
}
