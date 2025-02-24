package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.api.item.armor.*;
import com.ghostipedia.cosmiccore.api.registries.CosmicRegistration;
import com.ghostipedia.cosmiccore.common.data.tag.item.CosmicItemTags;
import com.ghostipedia.cosmiccore.common.item.behavior.EffectApplicationBehavior;
import com.ghostipedia.cosmiccore.common.item.behavior.StructureWriteBehavior;

import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.common.item.TooltipBehavior;
import com.gregtechceu.gtceu.common.item.armor.GTArmorMaterials;
import com.gregtechceu.gtceu.common.item.armor.QuarkTechSuite;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;

import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import earth.terrarium.adastra.common.tags.ModItemTags;
import wayoftime.bloodmagic.common.item.BloodOrb;
import wayoftime.bloodmagic.common.item.ItemBloodOrb;
import wayoftime.bloodmagic.common.registration.impl.BloodOrbRegistryObject;

import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static wayoftime.bloodmagic.common.item.BloodMagicItems.BLOOD_ORBS;

@SuppressWarnings({ "unused" })
public class CosmicItems {

    public static final BloodOrbRegistryObject<BloodOrb> ORB_ASCENDANT;
    public static final BloodOrbRegistryObject<BloodOrb> ORB_VOIDSENT;
    public static final BloodOrbRegistryObject<BloodOrb> ORB_SOVEREIGN;

    static {
        CosmicRegistration.REGISTRATE.creativeModeTab(() -> CosmicCreativeModeTabs.COSMIC_CORE);
        ORB_ASCENDANT = BLOOD_ORBS.register("ascendantbloodorb", () -> {
            return new BloodOrb(new ResourceLocation("bloodmagic", "ascendantbloodorb"), 5, 25000000, 1000);
        });
        ORB_VOIDSENT = BLOOD_ORBS.register("voidsentbloodorb", () -> {
            return new BloodOrb(new ResourceLocation("bloodmagic", "voidsentbloodorb"), 5, 50000000, 1000);
        });
        ORB_SOVEREIGN = BLOOD_ORBS.register("sovereignbloodorb", () -> {
            return new BloodOrb(new ResourceLocation("bloodmagic", "sovereignbloodorb"), 5, 100000000, 10000);
        });
    }

    public static final CosmicBloodOrbDeferredRegister COSMIC_BLOOD_ORBS = new CosmicBloodOrbDeferredRegister(
            "cosmiccore");

    // Literally Random shit
    public static final ItemEntry<ComponentItem> DONK = REGISTRATE.item("donk", ComponentItem::create)
            .lang("Donk")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> DILUMIXAL_NAQ_DOPED_BOULE = REGISTRATE
            .item("dilumixal_naquadah_doped_silicon_boule", ComponentItem::create)
            .lang("DiLumixal Naquadah Doped Silicon Boule")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> DILUMIXAL_NAQ_DOPED_WAFER = REGISTRATE
            .item("dilumixal_naquadah_doped_silicon_wafer", ComponentItem::create)
            .lang("DiLumixal Naquadah Doped Silicon Wafer")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CRYSTAL_CHIPLET_MASK = REGISTRATE
            .item("crystal_chiplet_mask", ComponentItem::create)
            .lang("Crystal Chiplet Mask")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> MASKED_CRYSTAL_CHIPLET_PACKAGE = REGISTRATE
            .item("masked_crystal_chiplet_package", ComponentItem::create)
            .lang("Masked Crystal Chiplet Package")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CRYSTAL_CHIPLET_BASE = REGISTRATE
            .item("crystal_chiplet_base", ComponentItem::create)
            .lang("Crystal Chiplet Base")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ENGRAVED_CRYSTAL_CHIPLET = REGISTRATE
            .item("engraved_crystal_chiplet", ComponentItem::create)
            .lang("Engraved Crystal Chiplet")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> UNSEALED_CRYSTAL_CPU = REGISTRATE
            .item("unsealed_crystal_cpu", ComponentItem::create)
            .lang("Unsealed Crystal CPU")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CRYSTALLINE_TRANSISTOR = REGISTRATE
            .item("crystalline_transistor", ComponentItem::create)
            .lang("Crystalline Transistor")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CRYSTALLINE_RESISTOR = REGISTRATE
            .item("crystalline_resistor", ComponentItem::create)
            .lang("Crystalline Resistor")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CRYSTALLINE_CAPACITOR = REGISTRATE
            .item("crystalline_capacitor", ComponentItem::create)
            .lang("Crystalline Capacitor")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CRYSTALLINE_DIODE = REGISTRATE
            .item("crystalline_diode", ComponentItem::create)
            .lang("Crystalline Diode")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CRYSTALLINE_INDUCTOR = REGISTRATE
            .item("crystalline_inductor", ComponentItem::create)
            .lang("Crystalline Inductor")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ItemBloodOrb> ITEM_ORB_ASCENDANT = REGISTRATE
            .item("asc_blood_orb", (p) -> new ItemBloodOrb(ORB_ASCENDANT))
            .lang("Ascendant Blood Orb")
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .register();
    public static final ItemEntry<ItemBloodOrb> ITEM_ORB_VOIDSENT = REGISTRATE
            .item("void_blood_orb", (p) -> new ItemBloodOrb(ORB_VOIDSENT))
            .lang("Ascendant Blood Orb")
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .register();
    public static final ItemEntry<ItemBloodOrb> ITEM_ORB_SOVEREIGNT = REGISTRATE
            .item("sov_blood_orb", (p) -> new ItemBloodOrb(ORB_SOVEREIGN))
            .lang("Ascendant Blood Orb")
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> THERMAL_CHAIN_AGENT = REGISTRATE
            .item("thermal_chain_agent", ComponentItem::create)
            .lang("Thermal Chain Agent")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    // public static final ItemEntry<ComponentItem> VOMAPLAST = REGISTRATE.item("vomaplast", ComponentItem::create)
    // .lang("Vomaplast")
    // .properties(p -> p.stacksTo(64))
    // .defaultModel()
    // .register();
    public static final ItemEntry<ComponentItem> PERPETUITY_SHARD = REGISTRATE
            .item("shard_of_perpetuity", ComponentItem::create)
            .lang("Shard of Perpetuity")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.lore.shard_small.0"));
                tooltips.add(Component.translatable("cosmiccore.lore.shard_small.1"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PERPETUITY_SHARD_LARGE = REGISTRATE
            .item("large_shard_of_perpetuity", ComponentItem::create)
            .lang("Large Shard of Perpetuity")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.lore.shard_large.0"));
                tooltips.add(Component.translatable("cosmiccore.lore.shard_large.1"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PERPETUITY_SHARD_MASSIVE = REGISTRATE
            .item("cluster_of_perpetuity", ComponentItem::create)
            .lang("Cluster of Perpetuity")
            .properties(p -> p.stacksTo(60))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.lore.shard_huge.0"));
                tooltips.add(Component.translatable("cosmiccore.lore.shard_huge.1"));
                tooltips.add(Component.translatable("cosmiccore.lore.shard_huge.2"));
            })))
            .defaultModel()
            .register();

    public static ItemEntry<ComponentItem> THE_ONE_RING = REGISTRATE
            .item("the_one_ring", p -> (ComponentItem) new ComponentItem(p) {

                @Override
                public boolean canBeHurtBy(DamageSource damageSource) {
                    return damageSource.is(DamageTypes.LAVA);
                }

                @Override
                public int getEntityLifespan(ItemStack itemStack, Level level) {
                    return Short.MIN_VALUE;
                }

                @Override
                public boolean onDroppedByPlayer(ItemStack item, Player player) {
                    return false;
                }

                @Override
                public boolean isFoil(ItemStack stack) {
                    return true;
                }
            })
            .lang("The One Ring")
            .properties(p -> p.stacksTo(1).fireResistant())
            .onRegister(attach(new EffectApplicationBehavior()
                    .addEffect(() -> new MobEffectInstance(MobEffects.INVISIBILITY, 10), 1.0F)
                    .addEffect(() -> new MobEffectInstance(MobEffects.UNLUCK, 10, 5), 1.0F)
                    .addEffect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10, 1), 1.0F),
                    new TooltipBehavior(list -> {
                        list.add(Component.translatable("item.cosmiccore.the_one_ring.tooltip.0"));
                        list.add(Component.translatable("item.cosmiccore.the_one_ring.tooltip.1"));
                    })))
            .register();
    // public static final ItemEntry<ComponentItem> PARADOX_ECHOS = REGISTRATE.item("paradox_echos",
    // ComponentItem::create)
    // .lang("Paradox Echos")
    // .properties(p -> p.stacksTo(64))
    // .defaultModel()
    // .register();
    // public static final ItemEntry<ComponentItem> ECTOPHASM = REGISTRATE.item("ectophasm", ComponentItem::create)
    // .lang("Ectophasm")
    // .properties(p -> p.stacksTo(64))
    // .defaultModel()
    // .register();
    // public static final ItemEntry<ComponentItem> DEMONIC_DESIRE = REGISTRATE.item("demonic_desire",
    // ComponentItem::create)
    // .lang("Deomic Desire")
    // .properties(p -> p.stacksTo(64))
    // .defaultModel()
    // .register();
    // public static final ItemEntry<ComponentItem> WEAKENED_SOUL = REGISTRATE.item("weakened_soul",
    // ComponentItem::create)
    // .lang("Weakened Soul")
    // .properties(p -> p.stacksTo(64))
    // .defaultModel()
    // .register();
    //

    public static ItemEntry<ComponentItem> SPACE_RADIO = REGISTRATE
            .item("space_radio", ComponentItem::create)
            .lang("Space Radio")
            .properties(p -> p.stacksTo(1).fireResistant())
            .onRegister(attach(new TooltipBehavior(list -> {
                list.add(Component.translatable("item.cosmiccore.space_radio.tooltip"));
            })))
            .register();

    public static final ItemEntry<ComponentItem> WAXED_LEATHER = REGISTRATE.item("waxed_leather", ComponentItem::create)
            .lang("Waxed Leather")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OVERLOADED_PEARLS = REGISTRATE
            .item("overloaded_pearls", ComponentItem::create)
            .lang("Overloaded Pearls")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ADVANCED_RAM_WAFER = REGISTRATE
            .item("aram_wafer", ComponentItem::create)
            .lang("ARAM Wafer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ADVANCED_RAM_CHIP = REGISTRATE.item("aram_chip", ComponentItem::create)
            .lang("ARAM Chip")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> BLACKSTONE_PUSTULE = REGISTRATE
            .item("blackstone_pustule", ComponentItem::create)
            .lang("Blackstone Pustule")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> WRAPPED_S = REGISTRATE
            .item("blackstone_pustule", ComponentItem::create)
            .lang("Blackstone Pustule")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    // New Circuits
    // Echo (ZPM-UEV)
    public static final ItemEntry<ComponentItem> SONAR_PROCESSOR = REGISTRATE
            .item("echo_processor", ComponentItem::create)
            .lang("Echo Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SONAR_PROCESSOR_ASSEMBLY = REGISTRATE
            .item("echo_processor_assembly", ComponentItem::create)
            .lang("Echo Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SONAR_PROCESSOR_SUPERCOMPUTER = REGISTRATE
            .item("echo_processor_supercomputer", ComponentItem::create)
            .lang("Echo Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SONAR_PROCESSOR_MAINFRAME = REGISTRATE
            .item("echo_processor_mainframe", ComponentItem::create)
            .lang("Echo Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    // Optical (UV-UIV)
    public static final ItemEntry<ComponentItem> OPTICAL_PROCESSOR = REGISTRATE
            .item("optical_processor", ComponentItem::create)
            .lang("Optical Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OPTICAL_PROCESSOR_ASSEMBLY = REGISTRATE
            .item("optical_processor_assembly", ComponentItem::create)
            .lang("Optical Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OPTICAL_PROCESSOR_SUPERCOMPUTER = REGISTRATE
            .item("optical_processor_supercomputer", ComponentItem::create)
            .lang("Optical Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OPTICAL_PROCESSOR_MAINFRAME = REGISTRATE
            .item("optical_processor_mainframe", ComponentItem::create)
            .lang("Optical Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    // Cosmic (UHV-UXV)
    public static final ItemEntry<ComponentItem> COSMIC_PROCESSOR = REGISTRATE
            .item("cosmic_processor", ComponentItem::create)
            .lang("Cosmic Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> COSMIC_PROCESSOR_ASSEMBLY = REGISTRATE
            .item("cosmic_processor_assembly", ComponentItem::create)
            .lang("Cosmic Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> COSMIC_PROCESSOR_SUPERCOMPUTER = REGISTRATE
            .item("cosmic_processor_supercomputer", ComponentItem::create)
            .lang("Cosmic Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> COSMIC_PROCESSOR_MAINFRAME = REGISTRATE
            .item("cosmic_processor_mainframe", ComponentItem::create)
            .lang("Cosmic Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    // Psionic Circuit (UEV-OPV)
    public static final ItemEntry<ComponentItem> PSIONIC_PROCESSOR = REGISTRATE
            .item("psionic_processor", ComponentItem::create)
            .lang("Psionic Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PSIONIC_PROCESSOR_ASSEMBLY = REGISTRATE
            .item("psionic_processor_assembly", ComponentItem::create)
            .lang("Psionic Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PSIONIC_PROCESSOR_SUPERCOMPUTER = REGISTRATE
            .item("psionic_processor_supercomputer", ComponentItem::create)
            .lang("Psionic Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PSIONIC_PROCESSOR_MAINFRAME = REGISTRATE
            .item("psionic_processor_mainframe", ComponentItem::create)
            .lang("Psionic Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    // Macroverse (UIV-MAX)
    public static final ItemEntry<ComponentItem> MACROVERSE_PROCESSOR = REGISTRATE
            .item("macroverse_processor", ComponentItem::create)
            .lang("Macroverse Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> MACROVERSE_PROCESSOR_ASSEMBLY = REGISTRATE
            .item("macroverse_processor_assembly", ComponentItem::create)
            .lang("Macroverse Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> MACROVERSE_PROCESSOR_SUPERCOMPUTER = REGISTRATE
            .item("macroverse_processor_supercomputer", ComponentItem::create)
            .lang("Macroverse Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> MACROVERSE_PROCESSOR_MAINFRAME = REGISTRATE
            .item("macroverse_processor_mainframe", ComponentItem::create)
            .lang("Macroverse Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> FIRECLAY_BALL = REGISTRATE.item("fireclay_ball", ComponentItem::create)
            .lang("Fireclay ball")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> HARDENED_RESIN = REGISTRATE
            .item("hardened_resin", ComponentItem::create)
            .lang("Hardened Resin")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static ItemEntry<ComponentItem> DEBUG_STRUCTURE_WRITER = REGISTRATE
            .item("debug_structure_writer", ComponentItem::create)
            .lang("Debug Structure Writer")
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(StructureWriteBehavior.INSTANCE))
            .register();

    // Space Suite
    public static ItemEntry<SpaceArmorComponentItem> SPACE_NANO_CHESTPLATE = REGISTRATE
            .item("space_nanomuscle_chestplate",
                    (p) -> new SpaceArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.CHESTPLATE, 5000, p)
                            .setArmorLogic(new NanoMuscleSpaceSuite(ArmorItem.Type.CHESTPLATE, 512,
                                    6_400_000L * (long) Math.max(1,
                                            Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierNanoSuit - 3)),
                                    ConfigHolder.INSTANCE.tools.voltageTierNanoSuit)))
            .tag(CosmicItemTags.NANOMUSCLE_SPACE_SUITE, ModItemTags.SPACE_SUITS, ModItemTags.FREEZE_RESISTANT_ARMOR,
                    ModItemTags.HEAT_RESISTANT_ARMOR)
            .lang("NanoMuscle™ Space Suite Chestplate")
            .properties(p -> p.rarity(Rarity.RARE))
            .register();
    public static ItemEntry<SpaceArmorComponentItem> ADVANCED_SPACE_NANO_CHESTPLATE = REGISTRATE
            .item("space_advanced_nanomuscle_chestplate",
                    (p) -> new SpaceArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.CHESTPLATE, 10000, p)
                            .setArmorLogic(new AdvancedNanoMuscleSpaceSuite(512,
                                    12_800_000L * (long) Math.max(1,
                                            Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierAdvNanoSuit - 3)),
                                    ConfigHolder.INSTANCE.tools.voltageTierAdvNanoSuit)))
            .tag(CosmicItemTags.NANOMUSCLE_SPACE_SUITE, ModItemTags.SPACE_SUITS, ModItemTags.FREEZE_RESISTANT_ARMOR,
                    ModItemTags.HEAT_RESISTANT_ARMOR)
            .lang("Advanced NanoMuscle™ Space Suite Chestplate")
            .properties(p -> p.rarity(Rarity.EPIC))
            .register();
    public static ItemEntry<SpaceArmorComponentItem> SPACE_QUARK_CHESTPLATE = REGISTRATE
            .item("space_quarktech_chestplate",
                    (p) -> new SpaceArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.CHESTPLATE, 20000, p)
                            .setArmorLogic(new QuarkTechSpaceSuite(ArmorItem.Type.CHESTPLATE, 8192,
                                    100_000_000L * (long) Math.max(1,
                                            Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierQuarkTech - 5)),
                                    ConfigHolder.INSTANCE.tools.voltageTierQuarkTech)))
            .tag(CosmicItemTags.QUARKTECH_SPACE_SUITE, ModItemTags.SPACE_SUITS, ModItemTags.FREEZE_RESISTANT_ARMOR,
                    ModItemTags.HEAT_RESISTANT_ARMOR)
            .lang("QuarkTech™ Space Suite Chestplate")
            .properties(p -> p.rarity(Rarity.RARE))
            .register();
    public static ItemEntry<SpaceArmorComponentItem> ADVANCED_SPACE_QUARK_CHESTPLATE = REGISTRATE
            .item("space_advanced_quarktech_chestplate",
                    (p) -> new SpaceArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.CHESTPLATE, 50000, p)
                            .setArmorLogic(new AdvancedQuarkTechSpaceSuite(8192,
                                    1_000_000_000L * (long) Math.max(1,
                                            Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierAdvQuarkTech - 6)),
                                    ConfigHolder.INSTANCE.tools.voltageTierAdvQuarkTech)))
            .tag(CosmicItemTags.QUARKTECH_SPACE_SUITE, ModItemTags.SPACE_SUITS, ModItemTags.FREEZE_RESISTANT_ARMOR,
                    ModItemTags.HEAT_RESISTANT_ARMOR)
            .lang("Advanced QuarkTech™ Space Suite Chestplate")
            .properties(p -> p.rarity(Rarity.EPIC))
            .register();
    // Oiled up white girl trying to understand what the FUCK an armor tag is, i'm doing to fucking shove a whole
    // pineapple up the ass of whatever mojang employee thought these were **OKAY TO CODE**
    public static ItemEntry<SpaceArmorComponentItem> VOMHINEE_WARPTECH_CHESTPLATE = REGISTRATE
            .item("vomahine_warptech_chestplate",
                    (p) -> new SpaceArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.CHESTPLATE, 100000, p)
                            .setArmorLogic(new QuarkTechSpaceSuite(ArmorItem.Type.CHESTPLATE, 8192,
                                    100_000_000L * (long) Math.max(1,
                                            Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierQuarkTech - 5)),
                                    ConfigHolder.INSTANCE.tools.voltageTierAdvQuarkTech)))
            .tag(CosmicItemTags.QUARKTECH_SPACE_SUITE, ModItemTags.SPACE_SUITS, ModItemTags.FREEZE_RESISTANT_ARMOR,
                    ModItemTags.HEAT_RESISTANT_ARMOR)
            .lang("Vomahine™ WarpTech Chestplate")
            .properties(p -> p.rarity(Rarity.EPIC))
            .register();
    public static ItemEntry<SpaceArmorComponentItem> ADVANCED_VOMHINEE_WARPTECH_CHESTPLATE = REGISTRATE
            .item("vomahine_warptech_gravplate",
                    (p) -> new SpaceArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.CHESTPLATE, 100000, p)
                            .setArmorLogic(new AdvancedQuarkTechSpaceSuite(8192,
                                    10_000_000_000L * (long) Math.max(1,
                                            Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierAdvQuarkTech - 6)),
                                    ConfigHolder.INSTANCE.tools.voltageTierAdvQuarkTech)))
            .tag(CosmicItemTags.QUARKTECH_SPACE_SUITE, ModItemTags.SPACE_SUITS, ModItemTags.FREEZE_RESISTANT_ARMOR,
                    ModItemTags.HEAT_RESISTANT_ARMOR)
            .lang("Vomahine™ WarpTech Gravplate")
            .properties(p -> p.rarity(Rarity.EPIC))
            .register();
    public static ItemEntry<ArmorComponentItem> VOMHINEE_WARPTECH_LEGGINGS = REGISTRATE
            .item("vomahine_warptech_leggings",
                    (p) -> new ArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.LEGGINGS, p)
                            .setArmorLogic(new QuarkTechSuite(ArmorItem.Type.LEGGINGS,
                                    8192,
                                    100_000_000L * (long) Math.max(1,
                                            Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierQuarkTech - 5)),
                                    ConfigHolder.INSTANCE.tools.voltageTierQuarkTech)))
            .lang("Vomahine™ WarpTech Leggings")
            .properties(p -> p.rarity(Rarity.EPIC))
            .tag(CustomTags.PPE_ARMOR)
            .register();
    public static ItemEntry<ArmorComponentItem> VOMHINEE_WARPTECH_HELMET = REGISTRATE.item("vomahine_warptech_helmet",
            (p) -> new ArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.HELMET, p)
                    .setArmorLogic(new QuarkTechSuite(ArmorItem.Type.HELMET,
                            8192,
                            100_000_000L * (long) Math.max(1,
                                    Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierQuarkTech - 5)),
                            ConfigHolder.INSTANCE.tools.voltageTierQuarkTech)))
            .lang("Vomahine™ WarpTech Leggings")
            .properties(p -> p.rarity(Rarity.EPIC))
            .tag(CustomTags.PPE_ARMOR)
            .register();
    public static ItemEntry<ArmorComponentItem> VOMHINEE_WARPTECH_BOOTS = REGISTRATE.item("vomahine_warptech_boots",
            (p) -> new ArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.BOOTS, p)
                    .setArmorLogic(new QuarkTechSuite(ArmorItem.Type.BOOTS,
                            8192,
                            100_000_000L * (long) Math.max(1,
                                    Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierQuarkTech - 5)),
                            ConfigHolder.INSTANCE.tools.voltageTierQuarkTech)))
            .lang("Vomahine™ WarpTech Leggings")
            .properties(p -> p.rarity(Rarity.EPIC))
            .tag(CustomTags.PPE_ARMOR)
            .register();

    // OMNIA CIRCUITS

    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_LV = REGISTRATE
            .item("omnia_circuit_lv", ComponentItem::create)
            .lang("LV Omnia Circuit")
            .properties(p -> p.stacksTo(64))
            .tag(CustomTags.LV_CIRCUITS)
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.lv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_MV = REGISTRATE
            .item("omnia_circuit_mv", ComponentItem::create)
            .lang("MV Omnia Circuit")
            .properties(p -> p.stacksTo(64))
            .tag(CustomTags.MV_CIRCUITS)
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.mv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_HV = REGISTRATE
            .item("omnia_circuit_hv", ComponentItem::create)
            .lang("HV Omnia Circuit")
            .properties(p -> p.stacksTo(64))
            .tag(CustomTags.HV_CIRCUITS)
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.hv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_EV = REGISTRATE
            .item("omnia_circuit_ev", ComponentItem::create)
            .lang("EV Omnia Circuit")
            .properties(p -> p.stacksTo(64))
            .tag(CustomTags.EV_CIRCUITS)
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.ev"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_IV = REGISTRATE
            .item("omnia_circuit_iv", ComponentItem::create)
            .lang("IV Omnia Circuit")
            .properties(p -> p.stacksTo(64))
            .tag(CustomTags.IV_CIRCUITS)
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.iv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_LUV = REGISTRATE
            .item("omnia_circuit_luv", ComponentItem::create)
            .lang("LuV Omnia Circuit")
            .properties(p -> p.stacksTo(64))
            .tag(CustomTags.LuV_CIRCUITS)
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.luv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_ZPM = REGISTRATE
            .item("omnia_circuit_zpm", ComponentItem::create)
            .lang("ZPM Omnia Circuit")
            .properties(p -> p.stacksTo(64))
            .tag(CustomTags.ZPM_CIRCUITS)
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.zpm"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_UV = REGISTRATE
            .item("omnia_circuit_uv", ComponentItem::create)
            .lang("UV Omnia Circuit")
            .properties(p -> p.stacksTo(64))
            .tag(CustomTags.UV_CIRCUITS)
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.uv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_UHV = REGISTRATE
            .item("omnia_circuit_uhv", ComponentItem::create)
            .lang("UHV Omnia Circuit")
            .tag(CustomTags.UHV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.uhv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_UEV = REGISTRATE
            .item("omnia_circuit_uev", ComponentItem::create)
            .lang("UEV Omnia Circuit")
            .tag(CustomTags.UEV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.uev"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_UIV = REGISTRATE
            .item("omnia_circuit_uiv", ComponentItem::create)
            .lang("UIV Omnia Circuit")
            .tag(CustomTags.UIV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.uiv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_UXV = REGISTRATE
            .item("omnia_circuit_uxv", ComponentItem::create)
            .lang("UXV Omnia Circuit")
            .tag(CustomTags.UXV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.uxv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_OPV = REGISTRATE
            .item("omnia_circuit_opv", ComponentItem::create)
            .lang("OPV Omnia Circuit")
            .tag(CustomTags.OpV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.opv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RUNE_SLATE_ARKLYS = REGISTRATE
            .item("rune_slate_arklys", ComponentItem::create)
            .lang("Rune Slate [Arklys]")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.arklys.1"));
                tooltips.add(Component.translatable("cosmiccore.arklys.2"));
                tooltips.add(Component.translatable("cosmiccore.rune_vague"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RUNE_SLATE_TYLOMIR = REGISTRATE
            .item("rune_slate_tylomir", ComponentItem::create)
            .lang("Rune Slate [Tylomir]")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.tylomir.1"));
                tooltips.add(Component.translatable("cosmiccore.tylomir.2"));
                tooltips.add(Component.translatable("cosmiccore.rune_vague"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RUNE_SLATE_KHORUTH = REGISTRATE
            .item("rune_slate_khoruth", ComponentItem::create)
            .lang("Rune Slate [Khoruth]")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.khoruth.1"));
                tooltips.add(Component.translatable("cosmiccore.khoruth.2"));
                tooltips.add(Component.translatable("cosmiccore.rune_vague"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RUNE_SLATE_ZELOTHAR = REGISTRATE
            .item("rune_slate_zelothar", ComponentItem::create)
            .lang("Rune Slate [Zelothar]")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.zelothar.1"));
                tooltips.add(Component.translatable("cosmiccore.zelothar.2"));
                tooltips.add(Component.translatable("cosmiccore.rune_vague"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RUNE_SLATE_TENURA = REGISTRATE
            .item("rune_slate_tenura", ComponentItem::create)
            .lang("Rune Slate [Tenura]")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.tenura.1"));
                tooltips.add(Component.translatable("cosmiccore.tenura.2"));
                tooltips.add(Component.translatable("cosmiccore.rune_vague"));
            })))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> RUNE_SLATE_VALDRIS = REGISTRATE
            .item("rune_slate_valdris", ComponentItem::create)
            .lang("Rune Slate [Valdris]")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.valdris.1"));
                tooltips.add(Component.translatable("cosmiccore.valdris.2"));
                tooltips.add(Component.translatable("cosmiccore.rune_vague"));
            })))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> RUNE_CONJUNCTION_VALKRUTH = REGISTRATE
            .item("rune_conjunction_valkruth", ComponentItem::create)
            .lang("Rune Conjunction [Valkruth]")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.conjuct_valkruth.1"));
                tooltips.add(Component.translatable("cosmiccore.conjuct_valkruth.2"));
                tooltips.add(Component.translatable("cosmiccore.conjuct_valkruth_emotion.1"));
                tooltips.add(Component.translatable("cosmiccore.rune_emotion_weak.1"));
                tooltips.add(Component.translatable("cosmiccore.rune_emotion_weak.2"));
            })))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> RUNE_CONJUNCTION_KHOLYS = REGISTRATE
            .item("rune_conjunction_kholys", ComponentItem::create)
            .lang("Rune Conjunction [Kholys]")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.conjuct_kholys.1"));
                tooltips.add(Component.translatable("cosmiccore.conjuct_kholys.2"));
                tooltips.add(Component.translatable("cosmiccore.conjuct_kholys_emotion.1"));
                tooltips.add(Component.translatable("cosmiccore.rune_emotion_weak.1"));
                tooltips.add(Component.translatable("cosmiccore.rune_emotion_weak.2"));
            })))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> RUNE_CONJUNCTION_ARKLYTHAR = REGISTRATE
            .item("rune_conjunction_arklythar", ComponentItem::create)
            .lang("Rune Conjunction [Arklythar]")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.conjuct_arklythar.1"));
                tooltips.add(Component.translatable("cosmiccore.conjuct_arklythar.2"));
                tooltips.add(Component.translatable("cosmiccore.conjuct_arklythar_emotion.1"));
                tooltips.add(Component.translatable("cosmiccore.rune_emotion_weak.1"));
                tooltips.add(Component.translatable("cosmiccore.rune_emotion_weak.2"));
            })))
            .defaultModel()
            .register();
    // Gravity Normalizer Item Variation
    public static final ItemEntry<ComponentItem> PORTABLE_GRAVITY_CORE = REGISTRATE
            .item("portable_gravity_core", ComponentItem::create)
            .lang("§6Portable Gravity Core")
            .tag()
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.gravpack.1"));
            })))
            .defaultModel()
            .register();

    public static <T extends ComponentItem> NonNullConsumer<T> attach(IItemComponent... components) {
        return item -> item.attachComponents(components);
    }

    public static void init() {}
}
