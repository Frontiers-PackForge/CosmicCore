package com.ghostipedia.cosmiccore.common.data;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.item.LinkedTerminalBehavior;
import com.ghostipedia.cosmiccore.api.registries.CosmicRegistration;
import com.ghostipedia.cosmiccore.client.renderer.item.HaloItemRenderer;
import com.ghostipedia.cosmiccore.client.renderer.item.RadianceItemRenderer;
import com.ghostipedia.cosmiccore.common.airControl.OxygenConfig;
import com.ghostipedia.cosmiccore.common.data.tag.item.CosmicItemTags;
import com.ghostipedia.cosmiccore.common.item.AbyssalSonarItem;
import com.ghostipedia.cosmiccore.common.item.AirBladderItem;
import com.ghostipedia.cosmiccore.common.item.AsteroidItem;
import com.ghostipedia.cosmiccore.common.item.AsteroidTargetingChipItem;
import com.ghostipedia.cosmiccore.common.item.OxygenTankItem;
import com.ghostipedia.cosmiccore.common.item.SoulNetworkReaderItem;
import com.ghostipedia.cosmiccore.common.item.StealthCoatingItem;
import com.ghostipedia.cosmiccore.common.item.armor.ChestSanguineWarptechSuite;
import com.ghostipedia.cosmiccore.common.item.armor.HelmetSanguineWarptechSuite;
import com.ghostipedia.cosmiccore.common.item.armor.SanguineWarptechSuite;
import com.ghostipedia.cosmiccore.common.item.armor.boots.TravelerBootsItem;
import com.ghostipedia.cosmiccore.common.item.behavior.DowsingRodBehavior;
import com.ghostipedia.cosmiccore.common.item.behavior.EffectApplicationBehavior;
import com.ghostipedia.cosmiccore.common.item.behavior.InfiniteSprayCanBehavior;
import com.ghostipedia.cosmiccore.common.item.behavior.OxygenSupplyTankBehavior;
import com.ghostipedia.cosmiccore.common.item.behavior.StructureWriteBehavior;
import com.ghostipedia.cosmiccore.common.item.behavior.VeinSurveyBehavior;
import com.ghostipedia.cosmiccore.common.item.behavior.WirelessPDABehavior;
import com.ghostipedia.cosmiccore.common.reflection.item.MirrorItem;
import com.ghostipedia.cosmiccore.common.reflection.item.SoulMutilatorItem;
import com.ghostipedia.cosmiccore.gtbridge.recipemaker.RecipeMakerBehavior;
import com.ghostipedia.cosmiccore.utils.ItemData;
import com.ghostipedia.cosmiccore.utils.StringUtil;

import com.gregtechceu.gtceu.GTCEu;
import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.armor.ArmorComponentItem;
import com.gregtechceu.gtceu.api.item.component.ElectricStats;
import com.gregtechceu.gtceu.api.item.component.ICustomDescriptionId;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.item.component.ThermalFluidStats;
import com.gregtechceu.gtceu.common.data.GTItems;
import com.gregtechceu.gtceu.common.item.armor.GTArmorMaterials;
import com.gregtechceu.gtceu.common.item.behavior.ItemFluidContainer;
import com.gregtechceu.gtceu.common.item.behavior.TooltipBehavior;
import com.gregtechceu.gtceu.common.registry.GTRegistration;
import com.gregtechceu.gtceu.config.ConfigHolder;
import com.gregtechceu.gtceu.data.recipe.CustomTags;

import com.lowdragmc.lowdraglib.utils.LocalizationUtils;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import com.simibubi.create.content.equipment.armor.DivingBootsItem;
import com.simibubi.create.content.equipment.armor.DivingHelmetItem;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import java.awt.*;
import java.util.function.Function;

import static com.ghostipedia.cosmiccore.CosmicUtils.attachRenderer;
import static com.ghostipedia.cosmiccore.api.registries.CosmicRegistration.REGISTRATE;
import static com.gregtechceu.gtceu.common.data.GTItems.attach;

public class CosmicItems {

    // TODO(stellaris): SUN_GLOBE item used Ad Astra GLOBES registry + RenderedBlockItem â€” dropped with Ad Astra (bead
    // cosmiccore-42.13)
    static {
        CosmicRegistration.REGISTRATE.creativeModeTab(() -> CosmicCreativeModeTabs.COSMIC_CORE);
    }
    // Modules

    public static final ItemEntry<SoulNetworkReaderItem> SOUL_READER = REGISTRATE
            .item("soul_reader", SoulNetworkReaderItem::new)
            .lang("Soul Network Reader")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<AbyssalSonarItem> ABYSSAL_SONAR = REGISTRATE
            .item("abyssal_sonar", AbyssalSonarItem::new)
            .lang("Abyssal Sonar")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<Item> NETHER_PERMIT = REGISTRATE
            .item("nether_permit", Item::new)
            .lang("Nether Permit")
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .register();

    public static final ItemEntry<Item> FIRMAMENT_PERMIT = REGISTRATE
            .item("firmament_permit", Item::new)
            .lang("Firmament Permit")
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .register();

    public static final ItemEntry<DivingHelmetItem> SHADEBLOOM_DIVING_HELMET = REGISTRATE
            .item("shadebloom_diving_helmet",
                    p -> new DivingHelmetItem(CosmicArmorMaterials.SHADEBLOOM,
                            p.durability(ArmorItem.Type.HELMET.getDurability(37)),
                            CosmicCore.id("shadebloom")))
            .lang("Shadebloom Diving Helmet")
            .model(NonNullBiConsumer.noop())
            .register();

    public static final ItemEntry<ArmorItem> SHADEBLOOM_CHESTPLATE = REGISTRATE
            .item("shadebloom_chestplate",
                    p -> new ArmorItem(CosmicArmorMaterials.SHADEBLOOM, ArmorItem.Type.CHESTPLATE,
                            p.durability(ArmorItem.Type.CHESTPLATE.getDurability(37))))
            .lang("Shadebloom Chestplate")
            .model(NonNullBiConsumer.noop())
            .register();

    public static final ItemEntry<ArmorItem> SHADEBLOOM_LEGGINGS = REGISTRATE
            .item("shadebloom_leggings",
                    p -> new ArmorItem(CosmicArmorMaterials.SHADEBLOOM, ArmorItem.Type.LEGGINGS,
                            p.durability(ArmorItem.Type.LEGGINGS.getDurability(37))))
            .lang("Shadebloom Leggings")
            .model(NonNullBiConsumer.noop())
            .register();

    public static final ItemEntry<TravelerBootsItem> SHADEBLOOM_BOOTS = REGISTRATE
            .item("shadebloom_travelers_boots",
                    p -> new TravelerBootsItem(CosmicArmorMaterials.SHADEBLOOM,
                            p.durability(ArmorItem.Type.BOOTS.getDurability(45)).fireResistant()))
            .lang("Shadebloom Traveler's Boots")
            .tag(CustomTags.PPE_ARMOR)
            .register();

    public static final ItemEntry<DivingBootsItem> SHADEBLOOM_DIVING_BOOTS = REGISTRATE
            .item("shadebloom_diving_boots",
                    p -> new DivingBootsItem(CosmicArmorMaterials.SHADEBLOOM,
                            p.durability(ArmorItem.Type.BOOTS.getDurability(37)),
                            CosmicCore.id("shadebloom")))
            .lang("Shadebloom Diving Boots")
            .model(NonNullBiConsumer.noop())
            .register();

    public static final ItemEntry<Item> ABYSS_BERRY = REGISTRATE
            .item("abyss_berry", Item::new)
            .lang("Abyss Berry")
            .properties(p -> p.food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.3f).build()))
            .model(NonNullBiConsumer.noop())
            .register();

    public static final ItemEntry<Item> BLOOMSCRAP = REGISTRATE
            .item("bloomscrap", Item::new)
            .lang("Bloomscrap")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    public static final ItemEntry<StealthCoatingItem> STEALTH_COATING_1 = REGISTRATE
            .item("stealth_coating_1", p -> new StealthCoatingItem(p.stacksTo(16), 1))
            .lang("Stealth Coating I")
            .model(NonNullBiConsumer.noop())
            .register();

    public static final ItemEntry<StealthCoatingItem> STEALTH_COATING_2 = REGISTRATE
            .item("stealth_coating_2", p -> new StealthCoatingItem(p.stacksTo(16), 2))
            .lang("Stealth Coating II")
            .model(NonNullBiConsumer.noop())
            .register();

    public static final ItemEntry<StealthCoatingItem> STEALTH_COATING_3 = REGISTRATE
            .item("stealth_coating_3", p -> new StealthCoatingItem(p.stacksTo(16), 3))
            .lang("Stealth Coating III")
            .model(NonNullBiConsumer.noop())
            .register();

    /*
     * SHELVED (cosmiccore-42.14): Malum 1.8.2 reworked the spirit-type system
     * (MalumSpiritType.create / SpiritVisualMotif / SpiritTypeRegistry.register removed,
     * now DeferredSpiritTypes + SpiritColorProperties). Re-add the 4 cosmic spirit types
     * and their SpiritShardItems once the Malum spirit API is ported.
     * public static final ItemEntry<SpiritShardItem> ETHERIC_SPIRIT_ITEM = REGISTRATE
     * .item("etheric_spirit", (properties -> new SpiritShardItem(properties, CosmicItems.ETHERIC_SPIRIT)))
     * .lang("Etheric Spirit")
     * .properties(p -> p.stacksTo(64))
     * .tag()
     * .defaultModel()
     * .register();
     * 
     * public static MalumSpiritType ETHERIC_SPIRIT = SpiritTypeRegistry.register(MalumSpiritType.create("etheric",
     * new SpiritVisualMotif(new Color(120, 75, 255), new Color(55, 55, 55), 0.9f, Easing.BOUNCE_IN_OUT),
     * ETHERIC_SPIRIT_ITEM)
     * .setItemColor(SpiritVisualMotif::getPrimaryColor)
     * .build());
     * 
     * public static final ItemEntry<SpiritShardItem> WRATHFUL_SPIRIT_ITEM = REGISTRATE
     * .item("wrathful_spirit", (properties -> new SpiritShardItem(properties, CosmicItems.WRATHFUL_SPIRIT)))
     * .lang("Wrathful Spirit")
     * .properties(p -> p.stacksTo(64))
     * .tag()
     * .defaultModel()
     * .register();
     * 
     * public static MalumSpiritType WRATHFUL_SPIRIT = SpiritTypeRegistry.register(MalumSpiritType.create("wrathful",
     * new SpiritVisualMotif(2, new Color(120, 200, 80), new Color(200, 55, 0), 0.9f, Easing.SINE_IN_OUT),
     * WRATHFUL_SPIRIT_ITEM)
     * .setItemColor(SpiritVisualMotif::getPrimaryColor)
     * .build());
     * 
     * public static final ItemEntry<SpiritShardItem> PRIDEFUL_SPIRIT_ITEM = REGISTRATE
     * .item("prideful_spirit", (properties -> new SpiritShardItem(properties, CosmicItems.PRIDEFUL_SPIRIT)))
     * .lang("Prideful Spirit")
     * .properties(p -> p.stacksTo(64))
     * .tag()
     * .defaultModel()
     * .register();
     * 
     * public static MalumSpiritType PRIDEFUL_SPIRIT = SpiritTypeRegistry.register(MalumSpiritType.create("prideful",
     * new SpiritVisualMotif(4, new Color(120, 0, 100), new Color(200, 55, 0), 0.9f, Easing.SINE_IN_OUT),
     * PRIDEFUL_SPIRIT_ITEM)
     * .setItemColor(SpiritVisualMotif::getPrimaryColor)
     * .build());
     * 
     * public static final ItemEntry<SpiritShardItem> MALICE_SPIRIT_ITEM = REGISTRATE
     * .item("malice_spirit", (properties -> new SpiritShardItem(properties, CosmicItems.MALICE_SPIRIT)))
     * .lang("Malice Spirit")
     * .properties(p -> p.stacksTo(64))
     * .tag()
     * .defaultModel()
     * .register();
     * 
     * public static MalumSpiritType MALICE_SPIRIT = SpiritTypeRegistry.register(MalumSpiritType.create("malice",
     * new SpiritVisualMotif(4, new Color(210, 210, 210), new Color(200, 55, 0), 0.9f, Easing.SINE_IN_OUT),
     * MALICE_SPIRIT_ITEM)
     * .setItemColor(SpiritVisualMotif::getPrimaryColor)
     * .build());
     */
    public static final ItemEntry<ComponentItem> PROD_MOD_1 = REGISTRATE.item("prod_mod_1", ComponentItem::new)
            .lang("Productivity Module Mk.1")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PROD_MOD_2 = REGISTRATE.item("prod_mod_2", ComponentItem::new)
            .lang("Productivity Module Mk.2")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PROD_MOD_3 = REGISTRATE.item("prod_mod_3", ComponentItem::new)
            .lang("Productivity Module Mk.3")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PROD_MOD_4 = REGISTRATE.item("prod_mod_4", ComponentItem::new)
            .lang("Productivity Module Mk.4")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> PARA_MOD_1 = REGISTRATE.item("para_mod_1", ComponentItem::new)
            .lang("Parallelization Module Mk.1")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PARA_MOD_2 = REGISTRATE.item("para_mod_2", ComponentItem::new)
            .lang("Parallelization Module Mk.2")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PARA_MOD_3 = REGISTRATE.item("para_mod_3", ComponentItem::new)
            .lang("Parallelization Module Mk.3")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PARA_MOD_4 = REGISTRATE.item("para_mod_4", ComponentItem::new)
            .lang("Parallelization Module Mk.4")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> RESONANT_MODULE = REGISTRATE
            .item("resonant_mod", ComponentItem::new)
            .lang("Resonant Module")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> PROTOCYTE_MOD = REGISTRATE.item("protocyte_mod", ComponentItem::new)
            .lang("Protocyte Module")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> FUSION_MODULE_MK1 = REGISTRATE
            .item("resonant_mod", ComponentItem::new)
            .lang("Fusion Module Mk.1")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> PALE_SAW = REGISTRATE
            .item("pale_saw", ComponentItem::new)
            .lang("Pale Saw")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PALE_SCRAP = REGISTRATE
            .item("pale_scrap", ComponentItem::new)
            .lang("Pale Scrap")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ABRASIVE_ROSIN_MILLSTONES = REGISTRATE
            .item("abrasive_rosin_millstones", ComponentItem::new)
            .lang("Abrasive Rosin Millstones")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> BITUMEN_WAX = REGISTRATE
            .item("bitumen_wax", ComponentItem::new)
            .lang("Bitumen Wax")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ENERGIZED_SILK = REGISTRATE
            .item("energized_silk", ComponentItem::new)
            .lang("Energized Silk")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> HARMONICALLY_TUNED_CIRCUIT_BOARD = REGISTRATE
            .item("harmonically_tuned_circuit_board",
                    ComponentItem::new)
            .lang("Harmonically Tuned Circuit Board")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> HARMONICALLY_TUNED_PRINTED_CIRCUIT_BOARD = REGISTRATE
            .item("harmonically_tuned_printed_circuit_board",
                    ComponentItem::new)
            .lang("Harmonically Tuned Printed Circuit Board")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> OPTICALLY_REFINED_CIRCUIT_BOARD = REGISTRATE
            .item("optically_refined_circuit_board",
                    ComponentItem::new)
            .lang("Optically Refined Circuit Board")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OPTICALLY_REFINED_PRINTED_CIRCUIT_BOARD = REGISTRATE
            .item("optically_refined_printed_circuit_board",
                    ComponentItem::new)
            .lang("Optically Refined Printed Circuit Board")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> PERSONA_CORE_ASSISTED_CIRCUIT_BOARD = REGISTRATE
            .item("persona_core_assisted_circuit_board",
                    ComponentItem::new)
            .lang("Persona Core Assisted Circuit Board")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PERSONA_CORE_ASSISTED_PRINTED_CIRCUIT_BOARD = REGISTRATE
            .item("persona_core_assisted_printed_circuit_board",
                    ComponentItem::new)
            .lang("Persona Core Assisted Printed Circuit Board")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> RECORD_KEPT_CIRCUIT_BOARD = REGISTRATE
            .item("record_kept_circuit_board",
                    ComponentItem::new)
            .lang("Record Kept Circuit Board")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RECORD_KEPT_PRINTED_CIRCUIT_BOARD = REGISTRATE
            .item("record_kept_printed_circuit_board",
                    ComponentItem::new)
            .lang("Record Kept Printed Circuit Board")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    // Drone Frames
    public static final ItemEntry<ComponentItem> DRONE_FRAME_1 = REGISTRATE.item("drone_frame_1", ComponentItem::new)
            .lang("Drone Frame Mk.1")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> DRONE_FRAME_2 = REGISTRATE.item("drone_frame_2", ComponentItem::new)
            .lang("Drone Frame Mk.2")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> DRONE_FRAME_3 = REGISTRATE.item("drone_frame_3", ComponentItem::new)
            .lang("Drone Frame Mk.3")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> DRONE_FRAME_4 = REGISTRATE.item("drone_frame_4", ComponentItem::new)
            .lang("Drone Frame Mk.4")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> DRONE_FRAME_5 = REGISTRATE.item("drone_frame_5", ComponentItem::new)
            .lang("Drone Frame Mk.5")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();

    // Harmonic Chip Stuff
    public static final ItemEntry<ComponentItem> FLAWED_RESONANT_WAFER = REGISTRATE
            .item("flawed_resonant_wafer", ComponentItem::new)
            .lang("Flawed Harmonic Wafer")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> REFINED_RESONANT_WAFER = REGISTRATE
            .item("refined_resonant_wafer", ComponentItem::new)
            .lang("Refined Harmonic Wafer")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> WAFER_PRAGMISO = REGISTRATE
            .item("wafer_pragmiso", ComponentItem::new)
            .lang("Pragmiso Wafer [Physics]")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> WAFER_ALCHEMICA = REGISTRATE
            .item("alchemia_wafer", ComponentItem::new)
            .lang("Alchemica Wafer [Chemistry]")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> WAFER_THAUMICA = REGISTRATE
            .item("thaumica_wafer", ComponentItem::new)
            .lang("Thaumica Wafer [Arcana]")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> WAFER_ETERNA = REGISTRATE.item("eterna_wafer", ComponentItem::new)
            .lang("Eterna Wafer [Aionology]")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> WAFER_LOGOS = REGISTRATE
            .item("fused_wafer_of_logos", ComponentItem::new)
            .lang("Fused Harmonic Wafer of Logos")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> WAFER_ESOTERIC = REGISTRATE
            .item("fused_wafer_of_esoterica", ComponentItem::new)
            .lang("Fused Harmonic Wafer of Esoterica")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> HARMONIC_OSCILLATING_CHIP = REGISTRATE
            .item("harmonic_chiplet_oscillating", ComponentItem::new)
            .lang("Harmonic Central Processing Unit")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> NULL_WAFER_HARMONIC = REGISTRATE
            .item("null_refined_resonant_wafer", ComponentItem::new)
            .lang("Nullified Harmonic Wafer")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> NULLIFIED_HARMONICS_WAFER = REGISTRATE
            .item("nullified_harmonics_wafer", ComponentItem::new)
            .lang("Nullified Harmonic Wafer")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> REFINED_HARMONICS_WAFER = REGISTRATE
            .item("refined_harmonics_wafer", ComponentItem::new)
            .lang("Refined Harmonic Wafer")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    // Tesserae
    public static final ItemEntry<ComponentItem> TESSARON = REGISTRATE.item("tessaron", ComponentItem::new)
            .lang("Vexil - [Tessaron]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ESSON = REGISTRATE.item("esson", ComponentItem::new)
            .lang("Luminon - [Esson]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> VEXIUN = REGISTRATE.item("vexiun", ComponentItem::new)
            .lang("Vexil - [Vexiun]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PHANTNON = REGISTRATE.item("phantnon", ComponentItem::new)
            .lang("Luminon - [Phantnon]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> AMBRION = REGISTRATE.item("ambrion", ComponentItem::new)
            .lang("Vexil - [Ambrion]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SPECTIL = REGISTRATE.item("spectil", ComponentItem::new)
            .lang("Luminon - [Spectil]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> ETHERA = REGISTRATE.item("ethera", ComponentItem::new)
            .lang("Vexil - [Ethera]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> NYXON = REGISTRATE.item("nyxon", ComponentItem::new)
            .lang("Luminon - [Nyxon]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> PYRITH = REGISTRATE.item("pyrith", ComponentItem::new)
            .lang("Vexil - [Pyrith]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SERAPHON = REGISTRATE.item("seraphon", ComponentItem::new)
            .lang("Luminon - [Seraphon]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> TENAEBRUM = REGISTRATE.item("tenaebrum", ComponentItem::new)
            .lang("Vexil - [Tenaebrum]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> DYNAMIA = REGISTRATE.item("dynamia", ComponentItem::new)
            .lang("Luminon - [Dynamia]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> CRYSTALA = REGISTRATE.item("crystala", ComponentItem::new)
            .lang("Vexil - [Crystala]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> MYSTRIX = REGISTRATE.item("mystrix", ComponentItem::new)
            .lang("Luminon - [Mystrix]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> CHRONIA = REGISTRATE.item("chronia", ComponentItem::new)
            .lang("Vexil - [Chronia]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ECHON = REGISTRATE.item("echon", ComponentItem::new)
            .lang("Luminon - [Echon]")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();

    // Literally Random shit
    public static final ItemEntry<ComponentItem> DONK = REGISTRATE.item("donk", ComponentItem::new)
            .lang("Donk")
            .properties(p -> p.stacksTo(16))
            .onRegister(attach(new RecipeMakerBehavior()))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> DILUMIXAL_NAQ_DOPED_BOULE = REGISTRATE
            .item("dilumixal_naquadah_doped_silicon_boule", ComponentItem::new)
            .lang("DiLumixal Naquadah-doped Silicon Boule")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> DILUMIXAL_NAQ_DOPED_WAFER = REGISTRATE
            .item("dilumixal_naquadah_doped_silicon_wafer", ComponentItem::new)
            .lang("DiLumixal Naquadah-doped Silicon Wafer")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CRYSTAL_CHIPLET_MASK = REGISTRATE
            .item("crystal_chiplet_mask", ComponentItem::new)
            .lang("Crystal Chiplet Mask")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> MASKED_CRYSTAL_CHIPLET_PACKAGE = REGISTRATE
            .item("masked_crystal_chiplet_package", ComponentItem::new)
            .lang("Masked Crystal Chiplet Package")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CRYSTAL_CHIPLET_BASE = REGISTRATE
            .item("crystal_chiplet_base", ComponentItem::new)
            .lang("Crystal Chiplet Base")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ENGRAVED_CRYSTAL_CHIPLET = REGISTRATE
            .item("engraved_crystal_chiplet", ComponentItem::new)
            .lang("Engraved Crystal Chiplet")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> UNSEALED_CRYSTAL_CPU = REGISTRATE
            .item("unsealed_crystal_cpu", ComponentItem::new)
            .lang("Unsealed Crystal CPU")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CRYSTALLINE_TRANSISTOR = REGISTRATE
            .item("crystalline_transistor", ComponentItem::new)
            .lang("Crystalline Transistor")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CRYSTALLINE_RESISTOR = REGISTRATE
            .item("crystalline_resistor", ComponentItem::new)
            .lang("Crystalline Resistor")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CRYSTALLINE_CAPACITOR = REGISTRATE
            .item("crystalline_capacitor", ComponentItem::new)
            .lang("Crystalline Capacitor")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CRYSTALLINE_DIODE = REGISTRATE
            .item("crystalline_diode", ComponentItem::new)
            .lang("Crystalline Diode")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CRYSTALLINE_INDUCTOR = REGISTRATE
            .item("crystalline_inductor", ComponentItem::new)
            .lang("Crystalline Inductor")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    // Lucid Circuit Components
    public static final ItemEntry<ComponentItem> TEMPORAL_STABLE_THUNDERING_WAFER = REGISTRATE
            .item("temporal_stable_thundering_wafer", ComponentItem::new)
            .lang("Temporal Stable Thundering Wafer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> LUCIDITY_CPU_MASK = REGISTRATE
            .item("lucidity_cpu_mask", ComponentItem::new)
            .lang("Lucidity CPU Mask")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PREPARED_LUCID_WAFER = REGISTRATE
            .item("prepared_lucid_wafer", ComponentItem::new)
            .lang("Prepared Lucid Wafer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> LUCID_CPU_WAFER = REGISTRATE
            .item("lucid_cpu_wafer", ComponentItem::new)
            .lang("Lucid CPU Wafer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SOUL_CUT_LUCID_CPU_CHIP = REGISTRATE
            .item("soul_cut_lucid_cpu_chip", ComponentItem::new)
            .lang("Soul Cut Lucid CPU Chip")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> TEMPORAL_REINFORCED_LUCID_CIRCUIT_BOARD = REGISTRATE
            .item("temporal_reinforced_lucid_circuit_board", ComponentItem::new)
            .lang("Temporal Reinforced Lucid Circuit Board")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> TEMPORAL_REINFORCED_LUCID_PRINTED_CIRCUIT_BOARD = REGISTRATE
            .item("temporal_reinforced_lucid_printed_circuit_board", ComponentItem::new)
            .lang("Temporal Reinforced Lucid Printed Circuit Board")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> FIBER_MESH_INGOT_FRAME = REGISTRATE
            .item("fiber_mesh_ingot_frame", ComponentItem::new)
            .lang("Fiber Mesh Ingot Frame")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> THERMAL_CHAIN_AGENT = REGISTRATE
            .item("thermal_chain_agent", ComponentItem::new)
            .lang("Thermal Chain Agent")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RADIO_MODULE_HV = REGISTRATE
            .item("hv_radio_module", ComponentItem::new)
            .lang("HV Radio Module")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RADIO_MODULE_EV = REGISTRATE
            .item("ev_radio_module", ComponentItem::new)
            .lang("EV Radio Module")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RADIO_MODULE_IV = REGISTRATE
            .item("iv_radio_module", ComponentItem::new)
            .lang("IV Radio Module")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RADIO_MODULE_LUV = REGISTRATE
            .item("luv_radio_module", ComponentItem::new)
            .lang("LuV Radio Module")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RADIO_MODULE_ZPM = REGISTRATE
            .item("zpm_radio_module", ComponentItem::new)
            .lang("ZPM Radio Module")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RADIO_MODULE_UV = REGISTRATE
            .item("uv_radio_module", ComponentItem::new)
            .lang("UV Radio Module")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RADIO_MODULE_UHV = REGISTRATE
            .item("uhv_radio_module", ComponentItem::new)
            .lang("UHV Radio Module")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RADIO_MODULE_UEV = REGISTRATE
            .item("uev_radio_module", ComponentItem::new)
            .lang("UEV Radio Module")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RADIO_MODULE_UIV = REGISTRATE
            .item("uiv_radio_module", ComponentItem::new)
            .lang("UIV Radio Module")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RADIO_MODULE_UXV = REGISTRATE
            .item("uxv_radio_module", ComponentItem::new)
            .lang("UXV Radio Module")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RADIO_MODULE_OPV = REGISTRATE
            .item("opv_radio_module", ComponentItem::new)
            .lang("OPV Radio Module")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    // The Fuckin Spinny Boi
    public static final ItemEntry<ComponentItem> GYROSCOPE_UV = REGISTRATE
            .item("uv_gyroscope", ComponentItem::new)
            .lang("UV Gyroscope")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> GYROSCOPE_UHV = REGISTRATE
            .item("uhv_gyroscope", ComponentItem::new)
            .lang("UHV Gyroscope")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> GYROSCOPE_UEV = REGISTRATE
            .item("uev_gyroscope", ComponentItem::new)
            .lang("UEV Gyroscope")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> GYROSCOPE_UIV = REGISTRATE
            .item("uiv_gyroscope", ComponentItem::new)
            .lang("UIV Gyroscope")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> GYROSCOPE_UXV = REGISTRATE
            .item("uxv_gyroscope", ComponentItem::new)
            .lang("UXV Gyroscope")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> GYROSCOPE_OPV = REGISTRATE
            .item("opv_gyroscope", ComponentItem::new)
            .lang("OPv Gyroscope")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> GELATIN_SCAFFOLD = REGISTRATE
            .item("gelatin_scaffold", ComponentItem::new)
            .lang("Gelatin Scaffold")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> BIFIDOBACTERIUM_BREVE_CULTURE = REGISTRATE
            .item("bifidobacterium_breve_culture", ComponentItem::new)
            .lang("Bifidobacterium Breve Culture")
            .properties(p -> p.stacksTo(4))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> BIFIDOBACTERIUM_BREVE = REGISTRATE
            .item("bifidobacterium_breve", ComponentItem::new)
            .lang("Bifidobacterium Breve")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    // Strep
    public static final ItemEntry<ComponentItem> STREPTOCOCCUS_PYOGENES_CULTURE = REGISTRATE
            .item("streptococcus_pyogenes_culture", ComponentItem::new)
            .lang("Streptococcus Pyogenes Culture")
            .properties(p -> p.stacksTo(4))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> STREPTOCOCCUS_PYOGENES = REGISTRATE
            .item("streptococcus_pyogenes", ComponentItem::new)
            .lang("Streptococcus Pyogenes")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    // E COLI
    public static final ItemEntry<ComponentItem> ESCHERICHIA_COLI_CULTURE = REGISTRATE
            .item("escherichia_coli_culture", ComponentItem::new)
            .lang("Escherichia Coli Culture")
            .properties(p -> p.stacksTo(4))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ESCHERICHIA_COLI = REGISTRATE
            .item("escherichia_coli", ComponentItem::new)
            .lang("Escherichia Coli")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> BLUE_PROTO_ALGAE = REGISTRATE
            .item("blue_proto_algae", ComponentItem::new)
            .lang("Blue Proto-Algae")
            .properties(p -> p.stacksTo(4))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> GREEN_PROTO_ALGAE = REGISTRATE
            .item("green_proto_algae", ComponentItem::new)
            .lang("Green Proto-Algae")
            .properties(p -> p.stacksTo(4))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RED_PROTO_ALGAE = REGISTRATE
            .item("red_proto_algae", ComponentItem::new)
            .lang("Red Proto-Algae")
            .properties(p -> p.stacksTo(4))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> GREEN_ALGAE = REGISTRATE
            .item("green_algae", ComponentItem::new)
            .lang("Green Algae")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RED_ALGAE = REGISTRATE
            .item("red_algae", ComponentItem::new)
            .lang("Red Algae")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> BLUE_ALGAE = REGISTRATE
            .item("blue_algae", ComponentItem::new)
            .lang("Blue Algae")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> BLOOMWYRM_ALGAE = REGISTRATE
            .item("bloomwyrm_algae", ComponentItem::new)
            .lang("Bloomwyrm Algae")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> CONTAMINATED_PETRI_DISH = REGISTRATE
            .item("contaminated_petri_dish", ComponentItem::new)
            .lang("Contaminated Petri Dish")
            .properties(p -> p.stacksTo(8))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PREPARED_PETRI_DISH = REGISTRATE
            .item("prepared_petri_dish", ComponentItem::new)
            .lang("Prepared Petri Dish")
            .properties(p -> p.stacksTo(8))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ULTRASONIC_HOMOGENIZER = REGISTRATE
            .item("ultrasonic_homogenizer", ComponentItem::new)
            .lang("Ultrasonic Homogenizer")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> COMPUTATION_SUPPORT_UNIT = REGISTRATE
            .item("computation_support_unit", ComponentItem::new)
            .lang("Computation Support Unit")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> WIRED_PETRI_DISH = REGISTRATE
            .item("wired_petri_dish", ComponentItem::new)
            .lang("Wired Petri Dish")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> SCULK_FIBROBLAST = REGISTRATE
            .item("sculk_fibroblast", ComponentItem::new)
            .lang("Sculk Fibroblast")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SCULK_MYOFIBROBLAST = REGISTRATE
            .item("sculk_myofibroblast", ComponentItem::new)
            .lang("Sculk Myofibroblast")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    // UNSURE IF THESE WILL BE USED
    public static final ItemEntry<ComponentItem> RESPIRATORY_SCULK_HEMOCYTOBLAST = REGISTRATE
            .item("resipiratory_sculk_hemocytoblast", ComponentItem::new)
            .lang("Respiratory Sculk Hemocytoblast")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SATURATED_SCULK_HEMOCYTOBLAST = REGISTRATE
            .item("saturated_sculk_hemocytoblast", ComponentItem::new)
            .lang("Saturated Sculk Hemocytoblast")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> HEME_RING = REGISTRATE
            .item("heme_ring", ComponentItem::new)
            .lang("Heme Ring")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    // Thrusters (Space Industry stuff)

    public static final ItemEntry<ComponentItem> THRUSTER_UV = REGISTRATE
            .item("uv_thruster", ComponentItem::new)
            .lang(" Thruster")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> THRUSTER_UHV = REGISTRATE
            .item("uhv_thruster", ComponentItem::new)
            .lang("UHV Thruster")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> THRUSTER_UEV = REGISTRATE
            .item("uev_thruster", ComponentItem::new)
            .lang("UEV Thruster")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> THRUSTER_UIV = REGISTRATE
            .item("uiv_thruster", ComponentItem::new)
            .lang("UIV Thruster")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> THRUSTER_UXV = REGISTRATE
            .item("uxv_thruster", ComponentItem::new)
            .lang("UXV Thruster")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> THRUSTER_OPV = REGISTRATE
            .item("opv_thruster", ComponentItem::new)
            .lang("OPv Thruster")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    // Power Cells
    public static final ItemEntry<ComponentItem> POWER_CELL_UV = REGISTRATE
            .item("uv_powercell", ComponentItem::new)
            .lang("UV Power Cell")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> POWER_CELL_UHV = REGISTRATE
            .item("uhv_powercell", ComponentItem::new)
            .lang("UHV Power Cell")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> POWER_CELL_UEV = REGISTRATE
            .item("uev_powercell", ComponentItem::new)
            .lang("UEV Power Cell")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> POWER_CELL_UIV = REGISTRATE
            .item("uiv_powercell", ComponentItem::new)
            .lang("UIV Power Cell")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> POWER_CELL_UXV = REGISTRATE
            .item("uxv_powercell", ComponentItem::new)
            .lang("UXV Power Cell")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> POWER_CELL_OPV = REGISTRATE
            .item("opv_powercell", ComponentItem::new)
            .lang("OPv Power Cell")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> FERMIUM_RAD_CHARGES = REGISTRATE
            .item("fermium_rad_charges", ComponentItem::new)
            .lang("Fermium Radiation Charge")
            .properties(p -> p.stacksTo(8))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> NEURO_PROCESSING_ASSEMBLY = REGISTRATE
            .item("neuro_processing_assembly", ComponentItem::new)
            .lang("Neuroprocessing Assembly Board")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SOMATIC_PROCESSING_ASSEMBLY = REGISTRATE
            .item("somatic_processing_assembly", ComponentItem::new)
            .lang("Somatoprocessing Assembly Board")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OPTIC_PROCESSING_ASSEMBLY = REGISTRATE
            .item("optical_processing_assembly", ComponentItem::new)
            .lang("Optical Processor Assembly")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SELF_AWARE_PROCESSING_ASSEMBLY = REGISTRATE
            .item("self_aware_processing_assembly", ComponentItem::new)
            .lang("Self Aware Processor Assembly")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RECORD_KEEPING_PROCESSOR_ASSEMBLY = REGISTRATE
            .item("record_keeping_processor_assembly", ComponentItem::new)
            .lang("Record Keeping Processor Assembly")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PROGRAMMABLE_MOTE = REGISTRATE
            .item("programmable_mote", ComponentItem::new)
            .lang("Â§5Programmable Mote")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PERPETUITY_SHARD = REGISTRATE
            .item("shard_of_perpetuity", ComponentItem::new)
            .lang("Shard of Perpetuity")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(
                    new TooltipBehavior(tooltips -> {
                        tooltips.add(Component.translatable("cosmiccore.lore.shard_small.0"));
                        tooltips.add(Component.translatable("cosmiccore.lore.shard_small.1"));
                    }),
                    new com.ghostipedia.cosmiccore.common.reflection.item.ShardConsumeBehavior(1)))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PERPETUITY_SHARD_LARGE = REGISTRATE
            .item("large_shard_of_perpetuity", ComponentItem::new)
            .lang("Large Shard of Perpetuity")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(
                    new TooltipBehavior(tooltips -> {
                        tooltips.add(Component.translatable("cosmiccore.lore.shard_large.0"));
                        tooltips.add(Component.translatable("cosmiccore.lore.shard_large.1"));
                    }),
                    new com.ghostipedia.cosmiccore.common.reflection.item.CapacityShardBehavior(10)))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PERPETUITY_SHARD_MASSIVE = REGISTRATE
            .item("cluster_of_perpetuity", ComponentItem::new)
            .lang("Cluster of Perpetuity")
            .properties(p -> p.stacksTo(16))
            .onRegister(attach(
                    new TooltipBehavior(tooltips -> {
                        tooltips.add(Component.translatable("cosmiccore.lore.shard_huge.0"));
                        tooltips.add(Component.translatable("cosmiccore.lore.shard_huge.1"));
                        tooltips.add(Component.translatable("cosmiccore.lore.shard_huge.2"));
                    }),
                    new com.ghostipedia.cosmiccore.common.reflection.item.ScarRemovalBehavior()))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> WIRELESS_PDA = REGISTRATE
            .item("wireless_pda", ComponentItem::new)
            .lang("Wireless Data PDA")
            .properties(p -> p.stacksTo(1))
            .tag()
            .onRegister(attach(new WirelessPDABehavior()))
            .defaultModel()
            .register();

    /*
     * SHELVED (cosmiccore-42.14): CosmicScytheItem needs the Malum 1.8.2 scythe rework
     * (enchantment-based scythe system removed), NeoForge item-capability rework, and
     * GTCEu electric-item port. Re-add the three scythes once CosmicScytheItem is ported.
     * public static final ItemEntry<CosmicScytheItem> NANO_SCYTHE = REGISTRATE
     * .item("nano_scythe",
     * props -> new CosmicScytheItem(
     * SOUL_STAINED_STEEL,
     * 10.5f, 0.5f, props))
     * .properties(p -> p.stacksTo(1))
     * .lang("Nano Scythe")
     * .defaultModel()
     * .register();
     * 
     * public static final ItemEntry<CosmicScytheItem> QUANTUM_SCYTHE = REGISTRATE
     * .item("quantum_scythe",
     * props -> new CosmicScytheItem(
     * SOUL_STAINED_STEEL,
     * 25.5f, 0.5f, props))
     * .properties(p -> p.stacksTo(1))
     * .lang("Quark Scythe")
     * .defaultModel()
     * .register();
     * 
     * public static final ItemEntry<CosmicScytheItem> SANGUINE_SCYTHE = REGISTRATE
     * .item("sanguine_scythe",
     * props -> new CosmicScytheItem(
     * SOUL_STAINED_STEEL,
     * 100f, 0.5f, props))
     * .properties(p -> p.stacksTo(1))
     * .lang("Sanguine Scythe")
     * .defaultModel()
     * .register();
     */

    public static ItemEntry<ComponentItem> THE_ONE_RING = REGISTRATE
            .item("the_one_ring", p -> (ComponentItem) new ComponentItem(p) {

                @Override
                public boolean canBeHurtBy(ItemStack stack, DamageSource damageSource) {
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
    // public static final ItemEntry<ComponentItem> PARADOX_ECHOS = REGISTRATE.item("paradox_harmonics",
    // ComponentItem::new)
    // .lang("Paradox Harmonics")
    // .properties(p -> p.stacksTo(64))
    // .defaultModel()
    // .register();
    // public static final ItemEntry<ComponentItem> ECTOPHASM = REGISTRATE.item("ectophasm", ComponentItem::new)
    // .lang("Ectophasm")
    // .properties(p -> p.stacksTo(64))
    // .defaultModel()
    // .register();
    // public static final ItemEntry<ComponentItem> DEMONIC_DESIRE = REGISTRATE.item("demonic_desire",
    // ComponentItem::new)
    // .lang("Deomic Desire")
    // .properties(p -> p.stacksTo(64))
    // .defaultModel()
    // .register();
    // public static final ItemEntry<ComponentItem> WEAKENED_SOUL = REGISTRATE.item("weakened_soul",
    // ComponentItem::new)
    // .lang("Weakened Soul")
    // .properties(p -> p.stacksTo(64))
    // .defaultModel()
    // .register();
    //

    public static ItemEntry<ComponentItem> SPACE_RADIO = REGISTRATE
            .item("space_radio", ComponentItem::new)
            .lang("Space Radio")
            .properties(p -> p.stacksTo(1).fireResistant())
            .onRegister(attach(new TooltipBehavior(list -> {
                list.add(Component.translatable("item.cosmiccore.space_radio.tooltip"));
            })))
            .register();

    public static ItemEntry<ComponentItem> SIMPLE_REBREATHER = REGISTRATE
            .item("simple_rebreather", ComponentItem::new)
            .lang("Simple Rebreather")
            .properties(p -> p.stacksTo(1).fireResistant())
            .onRegister(attach(new TooltipBehavior(list -> {
                list.add(Component.translatable("item.cosmiccore.simple_rebreather.tooltip"));
            })))
            .register();

    public static ItemEntry<ComponentItem> PRESSURIZED_REBREATHER = REGISTRATE
            .item("pressurized_rebreather", ComponentItem::new)
            .lang("Pressurized Rebreather")
            .properties(p -> p.stacksTo(1).fireResistant())
            .onRegister(attach(new TooltipBehavior(list -> {
                list.add(Component.translatable("item.cosmiccore.simple_rebreather.tooltip"));
                list.add(Component.translatable("item.cosmiccore.pressurized_rebreather.tooltip"));
            })))
            .register();

    public static ItemEntry<ComponentItem> PALMS_OF_THE_GLOBESTRIDER = REGISTRATE
            .item("palms_of_the_globestrider", ComponentItem::new)
            .lang("Palms of the Globestrider")
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(new TooltipBehavior(list -> {
                list.add(Component.translatable("item.cosmiccore.palms_of_the_globestrider.tooltip"));
            })))
            .register();

    public static final ItemEntry<ComponentItem> WAXED_LEATHER = REGISTRATE.item("waxed_leather", ComponentItem::new)
            .lang("Waxed Leather")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OVERLOADED_PEARLS = REGISTRATE
            .item("overloaded_pearls", ComponentItem::new)
            .lang("Overloaded Pearls")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ADVANCED_RAM_WAFER = REGISTRATE
            .item("aram_wafer", ComponentItem::new)
            .lang("ARAM Wafer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ADVANCED_RAM_CHIP = REGISTRATE.item("aram_chip", ComponentItem::new)
            .lang("ARAM Chip")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> RUNEWOVEN_PCB = REGISTRATE
            .item("runewoven_plastic_circuit_board", ComponentItem::new)
            .lang("Runewoven Plastic Circuit Board")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> MANA_PCB = REGISTRATE
            .item("plastic_circuit_board", ComponentItem::new)
            .lang("Mana-doped Plastic Circuit Board")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ENTHELIC_BOARD = REGISTRATE
            .item("multilayered_enthel_circuit_board", ComponentItem::new)
            .lang("Multilayered Enthel Circuit Board")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ENTHELIC_PCB = REGISTRATE
            .item("spirit_engraved_enthel_circuit_board", ComponentItem::new)
            .lang("Spirit Engraved Enthel Circuit Board")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> ENTHEL_CPU = REGISTRATE
            .item("spirit_runed_enthel_cpu", ComponentItem::new)
            .lang("Spirit Runed Enthel CPU")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ENTHEL_CPU_WAFER = REGISTRATE
            .item("spirit_runed_enthel_cpu_wafer", ComponentItem::new)
            .lang("Spirit Runed Enthel CPU Wafer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> RUNIC_HEX_CPU = REGISTRATE.item("runic_hex_cpu", ComponentItem::new)
            .lang("Hex Etched CPU Chip")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RUNIC_HEX_CPU_WAFER = REGISTRATE
            .item("runic_hex_cpu_wafer", ComponentItem::new)
            .lang("Hex Etched CPU Wafer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> BLACKSTONE_PUSTULE = REGISTRATE
            .item("blackstone_pustule", ComponentItem::new)
            .lang("Blackstone Pustule")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    // public static final ItemEntry<ComponentItem> WRAPPED_S = REGISTRATE
    // .item("blackstone_pustule", ComponentItem::new)
    // .lang("Blackstone Pustule")
    // .properties(p -> p.stacksTo(64))
    // .defaultModel()
    // .register();

    // New Circuits

    // Hex circuits
    public static final ItemEntry<ComponentItem> HEX_PROCESSOR = REGISTRATE
            .item("hex_processor", ComponentItem::new)
            .lang("Hex Processor")
            .tag(CustomTags.MV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> HEX_PROCESSOR_ASSEMBLY = REGISTRATE
            .item("hex_processor_assembly", ComponentItem::new)
            .lang("Hex Processor Assembly")
            .tag(CustomTags.HV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> HEX_PROCESSOR_SUPERCOMPUTER = REGISTRATE
            .item("hex_processor_supercomputer", ComponentItem::new)
            .lang("Hex Processor Supercomputer")
            .tag(CustomTags.EV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> HEX_PROCESSOR_MAINFRAME = REGISTRATE
            .item("hex_processor_mainframe", ComponentItem::new)
            .lang("Hex Processor Mainframe")
            .tag(CustomTags.IV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    // Enthelic
    public static final ItemEntry<ComponentItem> ENTHELIC_PROCESSOR = REGISTRATE
            .item("enthelic_processor", ComponentItem::new)
            .lang("Enthelic Processor")
            .tag(CustomTags.HV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ENTHELIC_PROCESSOR_ASSEMBLY = REGISTRATE
            .item("enthelic_processor_assembly", ComponentItem::new)
            .lang("Enthelic Processor Assembly")
            .tag(CustomTags.EV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ENTHELIC_PROCESSOR_SUPERCOMPUTER = REGISTRATE
            .item("enthelic_processor_supercomputer", ComponentItem::new)
            .lang("Enthelic Processor Supercomputer")
            .tag(CustomTags.IV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ENTHELIC_PROCESSOR_MAINFRAME = REGISTRATE
            .item("enthelic_processor_mainframe", ComponentItem::new)
            .lang("Enthelic Processor Mainframe")
            .tag(CustomTags.LuV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    // Lucidic
    public static final ItemEntry<ComponentItem> LUCIDIC_PROCESSOR = REGISTRATE
            .item("lucidic_processor", ComponentItem::new)
            .lang("Lucidic Processor")
            .tag(CustomTags.EV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> LUCIDIC_PROCESSOR_ASSEMBLY = REGISTRATE
            .item("lucidic_processor_assembly", ComponentItem::new)
            .lang("Lucidic Processor Assembly")
            .tag(CustomTags.IV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> LUCIDIC_PROCESSOR_SUPERCOMPUTER = REGISTRATE
            .item("lucidic_processor_supercomputer", ComponentItem::new)
            .lang("Lucidic Processor Supercomputer")
            .tag(CustomTags.LuV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> LUCIDIC_PROCESSOR_MAINFRAME = REGISTRATE
            .item("lucidic_processor_mainframe", ComponentItem::new)
            .lang("Lucidic Processor Mainframe")
            .tag(CustomTags.ZPM_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    // Harmonic (ZPM-UEV)
    public static final ItemEntry<ComponentItem> SONAR_PROCESSOR = REGISTRATE
            .item("harmonic_processor", ComponentItem::new)
            .lang("Harmonic Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SONAR_PROCESSOR_ASSEMBLY = REGISTRATE
            .item("harmonic_processor_assembly", ComponentItem::new)
            .lang("Harmonic Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SONAR_PROCESSOR_SUPERCOMPUTER = REGISTRATE
            .item("harmonic_processor_supercomputer", ComponentItem::new)
            .lang("Harmonic Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SONAR_PROCESSOR_MAINFRAME = REGISTRATE
            .item("harmonic_processor_mainframe", ComponentItem::new)
            .lang("Harmonic Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    // Optical (UV-UIV)
    public static final ItemEntry<ComponentItem> OPTICAL_PROCESSOR = REGISTRATE
            .item("optical_processor", ComponentItem::new)
            .lang("Optical Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OPTICAL_PROCESSOR_ASSEMBLY = REGISTRATE
            .item("optical_processor_assembly", ComponentItem::new)
            .lang("Optical Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OPTICAL_PROCESSOR_SUPERCOMPUTER = REGISTRATE
            .item("optical_processor_supercomputer", ComponentItem::new)
            .lang("Optical Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OPTICAL_PROCESSOR_MAINFRAME = REGISTRATE
            .item("optical_processor_mainframe", ComponentItem::new)
            .lang("Optical Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    // Suelescent (UHV-UXV)
    public static final ItemEntry<ComponentItem> COSMIC_PROCESSOR = REGISTRATE
            .item("suelescent_processor", ComponentItem::new)
            .lang("Suelescent Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> COSMIC_PROCESSOR_ASSEMBLY = REGISTRATE
            .item("suelescent_processor_assembly", ComponentItem::new)
            .lang("Suelescent Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> COSMIC_PROCESSOR_SUPERCOMPUTER = REGISTRATE
            .item("suelescent_processor_supercomputer", ComponentItem::new)
            .lang("Suelescent Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> COSMIC_PROCESSOR_MAINFRAME = REGISTRATE
            .item("suelescent_processor_mainframe", ComponentItem::new)
            .lang("Suelescent Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    // Akashic Circuit (UEV-OPV)
    public static final ItemEntry<ComponentItem> PSIONIC_PROCESSOR = REGISTRATE
            .item("akashic_processor", ComponentItem::new)
            .lang("Akashic Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PSIONIC_PROCESSOR_ASSEMBLY = REGISTRATE
            .item("akashic_processor_assembly", ComponentItem::new)
            .lang("Akashic Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PSIONIC_PROCESSOR_SUPERCOMPUTER = REGISTRATE
            .item("akashic_processor_supercomputer", ComponentItem::new)
            .lang("Akashic Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PSIONIC_PROCESSOR_MAINFRAME = REGISTRATE
            .item("akashic_processor_mainframe", ComponentItem::new)
            .lang("Akashic Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    // Eschaton (UIV-MAX)
    public static final ItemEntry<ComponentItem> ESCHATON_PROCESSOR = REGISTRATE
            .item("eschaton_processor", ComponentItem::new)
            .lang("Eschaton Processor")
            .properties(p -> p.stacksTo(64))
            .onRegister(attachRenderer(() -> HaloItemRenderer.create(6, 0xFFFFFFFF,
                    ResourceLocation.fromNamespaceAndPath(CosmicCore.MOD_ID, "block/iris/rnd/tentacle_halo"), true,
                    false)))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ESCHATON_PROCESSOR_ASSEMBLY = REGISTRATE
            .item("eschaton_processor_assembly", ComponentItem::new)
            .lang("Eschaton Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .onRegister(attachRenderer(() -> HaloItemRenderer.create(6, 0xFFFFFFFF,
                    ResourceLocation.fromNamespaceAndPath(CosmicCore.MOD_ID, "block/iris/rnd/tentacle_halo"), true,
                    false)))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ESCHATON_PROCESSOR_SUPERCOMPUTER = REGISTRATE
            .item("eschaton_processor_supercomputer", ComponentItem::new)
            .lang("Eschaton Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .onRegister(attachRenderer(() -> HaloItemRenderer.create(6, 0xFFFFFFFF,
                    ResourceLocation.fromNamespaceAndPath(CosmicCore.MOD_ID, "block/iris/rnd/tentacle_halo"), true,
                    false)))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ESCHATON_PROCESSOR_MAINFRAME = REGISTRATE
            .item("eschaton_processor_mainframe", ComponentItem::new)
            .lang("Eschaton Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .onRegister(attachRenderer(() -> HaloItemRenderer.create(6, 0xFFFFFFFF,
                    ResourceLocation.fromNamespaceAndPath(CosmicCore.MOD_ID, "block/iris/rnd/tentacle_halo"), true,
                    false)))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.literal(StringUtil
                        .rainbowDancing(LocalizationUtils.format("cosmiccore.circuit.lore.tier.max.0"))));
                lines.add(Component.translatable("cosmiccore.circuit.lore.tier.max.1"));
                lines.add(Component.translatable("cosmiccore.circuit.lore.tier.max.2"));
                lines.add(Component.translatable("cosmiccore.circuit.lore.tier.max.3"));

            })))
            .defaultModel()
            .register();

    // Demon/Soul Related Items

    public static final ItemEntry<ComponentItem> WICKED_ESSENCE = REGISTRATE
            .item("wicked_essence", ComponentItem::new)
            .lang("Wicked Essence")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.literal(StringUtil
                        .goldFlicker(LocalizationUtils.format("cosmiccore.lore.broken_virtue.0"))));
            })))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> ABERRANT_ESSENCE = REGISTRATE
            .item("aberrant_essence", ComponentItem::new)
            .lang("Â§6Aberrant Essence")
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(lines -> {
                lines.add(Component.literal(StringUtil
                        .midnightOscillation(LocalizationUtils.format("cosmiccore.lore.broken_virtue.1"))));
            })))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> FIRECLAY_BALL = REGISTRATE.item("fireclay_ball", ComponentItem::new)
            .lang("Fireclay Ball")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> HARDENED_RESIN = REGISTRATE
            .item("hardened_resin", ComponentItem::new)
            .lang("Hardened Resin")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static ItemEntry<ComponentItem> DEBUG_STRUCTURE_WRITER = REGISTRATE
            .item("debug_structure_writer", ComponentItem::new)
            .lang("Debug Structure Writer")
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(StructureWriteBehavior.INSTANCE))
            .register();

    // Space Suite â€” TODO(stellaris): AA/Botarium oxygen space-suit chestplates shelved (see _shelved/api/item/armor,
    // bead cosmiccore-42.13)
    /*
     * SHELVED (AA/Botarium space suits â€” armor logic moved to _shelved):
     * public static ItemEntry<SpaceArmorComponentItem> SPACE_NANO_CHESTPLATE = REGISTRATE
     * .item("space_nanomuscle_chestplate",
     * (p) -> new SpaceArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.CHESTPLATE, 5000, p)
     * .setArmorLogic(new NanoMuscleSpaceSuite(ArmorItem.Type.CHESTPLATE, 512,
     * 6_400_000L * (long) Math.max(1,
     * Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierNanoSuit - 3)),
     * ConfigHolder.INSTANCE.tools.voltageTierNanoSuit)))
     * .tag(CosmicItemTags.NANOMUSCLE_SPACE_SUITE, ModItemTags.SPACE_SUITS, ModItemTags.FREEZE_RESISTANT_ARMOR,
     * ModItemTags.HEAT_RESISTANT_ARMOR)
     * .lang("NanoMuscleâ„¢ Space Suite Chestplate")
     * .properties(p -> p.rarity(Rarity.RARE))
     * .register();
     * public static ItemEntry<SpaceArmorComponentItem> ADVANCED_SPACE_NANO_CHESTPLATE = REGISTRATE
     * .item("space_advanced_nanomuscle_chestplate",
     * (p) -> new SpaceArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.CHESTPLATE, 10000, p)
     * .setArmorLogic(new AdvancedNanoMuscleSpaceSuite(512,
     * 12_800_000L * (long) Math.max(1,
     * Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierAdvNanoSuit - 3)),
     * ConfigHolder.INSTANCE.tools.voltageTierAdvNanoSuit)))
     * .tag(CosmicItemTags.NANOMUSCLE_SPACE_SUITE, ModItemTags.SPACE_SUITS, ModItemTags.FREEZE_RESISTANT_ARMOR,
     * ModItemTags.HEAT_RESISTANT_ARMOR)
     * .lang("Advanced NanoMuscleâ„¢ Space Suite Chestplate")
     * .properties(p -> p.rarity(Rarity.EPIC))
     * .register();
     * public static ItemEntry<SpaceArmorComponentItem> SPACE_QUARK_CHESTPLATE = REGISTRATE
     * .item("space_quarktech_chestplate",
     * (p) -> new SpaceArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.CHESTPLATE, 20000, p)
     * .setArmorLogic(new QuarkTechSpaceSuite(ArmorItem.Type.CHESTPLATE, 8192,
     * 100_000_000L * (long) Math.max(1,
     * Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierQuarkTech - 5)),
     * ConfigHolder.INSTANCE.tools.voltageTierQuarkTech)))
     * .tag(CosmicItemTags.QUARKTECH_SPACE_SUITE, ModItemTags.SPACE_SUITS, ModItemTags.FREEZE_RESISTANT_ARMOR,
     * ModItemTags.HEAT_RESISTANT_ARMOR)
     * .lang("QuarkTechâ„¢ Space Suite Chestplate")
     * .properties(p -> p.rarity(Rarity.RARE))
     * .register();
     * public static ItemEntry<SpaceArmorComponentItem> ADVANCED_SPACE_QUARK_CHESTPLATE = REGISTRATE
     * .item("space_advanced_quarktech_chestplate",
     * (p) -> new SpaceArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.CHESTPLATE, 50000, p)
     * .setArmorLogic(new AdvancedQuarkTechSpaceSuite(8192,
     * 1_000_000_000L * (long) Math.max(1,
     * Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierAdvQuarkTech - 6)),
     * ConfigHolder.INSTANCE.tools.voltageTierAdvQuarkTech)))
     * .tag(CosmicItemTags.QUARKTECH_SPACE_SUITE, ModItemTags.SPACE_SUITS, ModItemTags.FREEZE_RESISTANT_ARMOR,
     * ModItemTags.HEAT_RESISTANT_ARMOR)
     * .lang("Advanced QuarkTechâ„¢ Space Suite Chestplate")
     * .properties(p -> p.rarity(Rarity.EPIC))
     * .register();
     */
    // Oiled up white girl trying to understand what the FUCK an armor tag is, i'm doing to fucking shove a whole
    // pineapple up the ass of whatever mojang employee thought these were **OKAY TO CODE**

    public static ItemEntry<ArmorComponentItem> SANGUINE_WARPTECH_HELMET = REGISTRATE.item("sanguine_warptech_helmet",
            (p) -> new ArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.HELMET, p)
                    .setArmorLogic(new HelmetSanguineWarptechSuite(ArmorItem.Type.HELMET,
                            8192,
                            100_000_000L * (long) Math.max(1,
                                    Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierQuarkTech - 5)),
                            ConfigHolder.INSTANCE.tools.voltageTierQuarkTech)))
            .lang("Sanguine WarpTech Helmet")
            .properties(p -> p.rarity(Rarity.EPIC))
            .tag(CustomTags.PPE_ARMOR)
            .register();

    public static ItemEntry<ArmorComponentItem> SANGUINE_WARPTECH_CHESTPLATE = REGISTRATE
            .item("sanguine_warptech_chestplate",
                    (p) -> new ArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.CHESTPLATE, p)
                            .setArmorLogic(new ChestSanguineWarptechSuite(8192,
                                    10_000_000_000L * (long) Math.max(1,
                                            Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierAdvQuarkTech - 6)),
                                    ConfigHolder.INSTANCE.tools.voltageTierAdvQuarkTech)))
            // TODO(stellaris): AA ModItemTags (SPACE_SUITS/FREEZE/HEAT) dropped with Ad Astra; oxygen-tank sub-feature
            // removed (now plain armor)
            .tag(CosmicItemTags.QUARKTECH_SPACE_SUITE)
            .lang("Sanguine WarpTech Gravplate")
            .properties(p -> p.rarity(Rarity.EPIC))
            .register();
    public static ItemEntry<ArmorComponentItem> SANGUINE_WARPTECH_LEGGINGS = REGISTRATE
            .item("sanguine_warptech_leggings",
                    (p) -> new ArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.LEGGINGS, p)
                            .setArmorLogic(new SanguineWarptechSuite(ArmorItem.Type.LEGGINGS,
                                    8192,
                                    100_000_000L * (long) Math.max(1,
                                            Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierQuarkTech - 5)),
                                    ConfigHolder.INSTANCE.tools.voltageTierQuarkTech)))
            .lang("Sanguine WarpTech Leggings")
            .properties(p -> p.rarity(Rarity.EPIC))
            .tag(CustomTags.PPE_ARMOR)
            .register();

    public static ItemEntry<ArmorComponentItem> SANGUINE_WARPTECH_BOOTS = REGISTRATE.item("sanguine_warptech_boots",
            (p) -> new ArmorComponentItem(GTArmorMaterials.ARMOR, ArmorItem.Type.BOOTS, p)
                    .setArmorLogic(new SanguineWarptechSuite(ArmorItem.Type.BOOTS,
                            8192,
                            100_000_000L * (long) Math.max(1,
                                    Math.pow(4, ConfigHolder.INSTANCE.tools.voltageTierQuarkTech - 5)),
                            ConfigHolder.INSTANCE.tools.voltageTierQuarkTech)))
            .lang("Sanguine WarpTech Boots")
            .properties(p -> p.rarity(Rarity.EPIC))
            .tag(CustomTags.PPE_ARMOR)
            .register();

    // OMNIA CIRCUITS

    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_LV = REGISTRATE
            .item("omnia_circuit_lv", ComponentItem::new)
            .lang("LV Omnia Circuit")
            .properties(p -> p.stacksTo(64))
            .tag(CustomTags.LV_CIRCUITS)
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.lv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_MV = REGISTRATE
            .item("omnia_circuit_mv", ComponentItem::new)
            .lang("MV Omnia Circuit")
            .properties(p -> p.stacksTo(64))
            .tag(CustomTags.MV_CIRCUITS)
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.mv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_HV = REGISTRATE
            .item("omnia_circuit_hv", ComponentItem::new)
            .lang("HV Omnia Circuit")
            .properties(p -> p.stacksTo(64))
            .tag(CustomTags.HV_CIRCUITS)
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.hv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_EV = REGISTRATE
            .item("omnia_circuit_ev", ComponentItem::new)
            .lang("EV Omnia Circuit")
            .properties(p -> p.stacksTo(64))
            .tag(CustomTags.EV_CIRCUITS)
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.ev"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_IV = REGISTRATE
            .item("omnia_circuit_iv", ComponentItem::new)
            .lang("IV Omnia Circuit")
            .properties(p -> p.stacksTo(64))
            .tag(CustomTags.IV_CIRCUITS)
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.iv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_LUV = REGISTRATE
            .item("omnia_circuit_luv", ComponentItem::new)
            .lang("LuV Omnia Circuit")
            .properties(p -> p.stacksTo(64))
            .tag(CustomTags.LuV_CIRCUITS)
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.luv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_ZPM = REGISTRATE
            .item("omnia_circuit_zpm", ComponentItem::new)
            .lang("ZPM Omnia Circuit")
            .properties(p -> p.stacksTo(64))
            .tag(CustomTags.ZPM_CIRCUITS)
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.zpm"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_UV = REGISTRATE
            .item("omnia_circuit_uv", ComponentItem::new)
            .lang("UV Omnia Circuit")
            .properties(p -> p.stacksTo(64))
            .tag(CustomTags.UV_CIRCUITS)
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.uv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_UHV = REGISTRATE
            .item("omnia_circuit_uhv", ComponentItem::new)
            .lang("UHV Omnia Circuit")
            .tag(CustomTags.UHV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.uhv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_UEV = REGISTRATE
            .item("omnia_circuit_uev", ComponentItem::new)
            .lang("UEV Omnia Circuit")
            .tag(CustomTags.UEV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.uev"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_UIV = REGISTRATE
            .item("omnia_circuit_uiv", ComponentItem::new)
            .lang("UIV Omnia Circuit")
            .tag(CustomTags.UIV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.uiv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_UXV = REGISTRATE
            .item("omnia_circuit_uxv", ComponentItem::new)
            .lang("UXV Omnia Circuit")
            .tag(CustomTags.UXV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.uxv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OMNIA_CIRCUIT_OPV = REGISTRATE
            .item("omnia_circuit_opv", ComponentItem::new)
            .lang("OPV Omnia Circuit")
            .tag(CustomTags.OpV_CIRCUITS)
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("cosmiccore.omnia_circuit.opv"));
            })))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RUNE_SLATE_ARKLYS = REGISTRATE
            .item("rune_slate_arklys", ComponentItem::new)
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
            .item("rune_slate_tylomir", ComponentItem::new)
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
            .item("rune_slate_khoruth", ComponentItem::new)
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
            .item("rune_slate_zelothar", ComponentItem::new)
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
            .item("rune_slate_tenura", ComponentItem::new)
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
            .item("rune_slate_valdris", ComponentItem::new)
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
            .item("rune_conjunction_valkruth", ComponentItem::new)
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
            .item("rune_conjunction_kholys", ComponentItem::new)
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
            .item("rune_conjunction_arklythar", ComponentItem::new)
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
            .item("portable_gravity_core", ComponentItem::new)
            .lang("Â§6Portable Gravity Core")
            .tag()
            .properties(p -> p.stacksTo(64))
            .onRegister(attach(new TooltipBehavior(tooltips -> {
                tooltips.add(Component.translatable("item.cosmiccore.portable_gravity_core.tooltip"));
            })))
            .defaultModel()
            .register();
    // infinite spraycan
    public static final ItemEntry<ComponentItem> INFINITE_SPRAY_CAN = REGISTRATE
            .item("infinite_spray_can", ComponentItem::new)
            .lang("Â§5 Infinite_spray_can")
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(new InfiniteSprayCanBehavior(1)))
            .onRegister(modelPredicate(CosmicCore.id("color"),
                    (itemStack) -> (float) ItemData.readTag(itemStack).getInt(InfiniteSprayCanBehavior.ColorTag)))
            .register();

    public static ItemEntry<ComponentItem> NEUTRONITE_FLUID_CELL = GTRegistration.REGISTRATE
            .item("indestructible_fluid_cell", ComponentItem::new)
            .lang("Indestructible %s Fluid Cell")
            .setData(ProviderType.ITEM_MODEL, NonNullBiConsumer.noop())
            .color(() -> GTItems::cellColor)
            .onRegister(attach(
                    ThermalFluidStats.create(1024000, 1000000, true, true, true, true, true),
                    new ItemFluidContainer(), cellName()))
            .register();
    // Drones
    public static final ItemEntry<ComponentItem> RUSTY_DRONE = REGISTRATE
            .item("rusty_drone", ComponentItem::new)
            .lang("Rusty Drone")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ROBUST_DRONE = REGISTRATE
            .item("robust_drone", ComponentItem::new)
            .lang("Robust Drone")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> INDUSTRIAL_DRONE = REGISTRATE
            .item("industrial_drone", ComponentItem::new)
            .lang("Industrial Drone")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SANGUINE_DRONE = REGISTRATE
            .item("sanguine_drone", ComponentItem::new)
            .lang("Sanguine Drone")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PLASMATIC_DRONE = REGISTRATE
            .item("plasmatic_drone", ComponentItem::new)
            .lang("plasmatic_drone")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    // WildFire Cores
    public static final ItemEntry<ComponentItem> LV_WILDFIRE_CORE = REGISTRATE
            .item("lv_wildfire_core", ComponentItem::new)
            .lang("LV Wildfire Core")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> MV_WILDFIRE_CORE = REGISTRATE
            .item("mv_wildfire_core", ComponentItem::new)
            .lang("MV Wildfire Core")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> HV_WILDFIRE_CORE = REGISTRATE
            .item("hv_wildfire_core", ComponentItem::new)
            .lang("HV Wildfire Core")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> EV_WILDFIRE_CORE = REGISTRATE
            .item("ev_wildfire_core", ComponentItem::new)
            .lang("EV Wildfire Core")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> IV_WILDFIRE_CORE = REGISTRATE
            .item("iv_wildfire_core", ComponentItem::new)
            .lang("IV Wildfire Core")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> LUV_WILDFIRE_CORE = REGISTRATE
            .item("luv_wildfire_core", ComponentItem::new)
            .lang("LuV Wildfire Core")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ZPM_WILDFIRE_CORE = REGISTRATE
            .item("zpm_wildfire_core", ComponentItem::new)
            .lang("ZPM Wildfire Core")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> BASIC_GENE_KIT = REGISTRATE
            .item("basic_gene_kit", ComponentItem::new)
            .lang("Basic Gene Kit")
            .properties(p -> p.stacksTo(16))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> INTERMEDIATE_GENE_KIT = REGISTRATE
            .item("intermediate_gene_kit", ComponentItem::new)
            .lang("Intermediate Gene Kit")
            .properties(p -> p.stacksTo(16))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> ADVANCED_GENE_KIT = REGISTRATE
            .item("advanced_gene_kit", ComponentItem::new)
            .lang("Advanced Gene Kit")
            .properties(p -> p.stacksTo(16))
            .defaultModel()
            .register();

    // MANA WAFERS AND CHIPS
    public static final ItemEntry<ComponentItem> LATENT_CAPACITY_WAFER = REGISTRATE
            .item("latent_capacity_wafer", ComponentItem::new)
            .lang("Latent Capacity Wafer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> LATENT_EFFICACY_WAFER = REGISTRATE
            .item("latent_efficacy_wafer", ComponentItem::new)
            .lang("Latent Efficacy Wafer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> LATENT_POTENCY_WAFER = REGISTRATE
            .item("latent_potency_wafer", ComponentItem::new)
            .lang("Latent Potency Wafer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> LATENT_VERBOSITY_WAFER = REGISTRATE
            .item("latent_verbosity_wafer", ComponentItem::new)
            .lang("Latent Verbosity Wafer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    // BOULE AND WAFER
    public static final ItemEntry<ComponentItem> LIVINGROCK_ALUMINATE_BOULE = REGISTRATE
            .item("livingrock_aluminate_boule", ComponentItem::new)
            .lang("Livingrock Aluminate Boule")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> LIVINGROCK_ALUMINATE_WAFER = REGISTRATE
            .item("livirock_aluminite_wafer", ComponentItem::new)
            .lang("Livingrock Aluminate Wafer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    // CHIPS
    public static final ItemEntry<ComponentItem> CAPACITY_CHIP = REGISTRATE
            .item("capacity_chip", ComponentItem::new)
            .lang("Capacity Chip")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> EFFICACY_CHIP = REGISTRATE
            .item("efficacy_chip", ComponentItem::new)
            .lang("Efficacy Chip")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> POTENCY_CHIP = REGISTRATE
            .item("potency_chip", ComponentItem::new)
            .lang("Potency Chip")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> VERBOSITY_CHIP = REGISTRATE
            .item("verbosity_chip", ComponentItem::new)
            .lang("Verbosity Chip")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    // Project Star Eater

    public static final ItemEntry<ComponentItem> HAULER_PROBE_GRADE_1 = REGISTRATE
            .item("freight_beetle_grade_1", ComponentItem::new)
            .lang("Freight Beetle Mk.1")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> ARMORED_HAULER_PROBE_GRADE_1 = REGISTRATE
            .item("armored_freight_beetle_grade_1", ComponentItem::new)
            .lang("Armored Freight Beetle Mk.1")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> CRUSHER_WASPS_GRADE_1 = REGISTRATE
            .item("crusher_wasps_grade_1", ComponentItem::new)
            .lang("Crusher Wasps Mk.1")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> RAZOR_HORNET_GRADE_1 = REGISTRATE
            .item("razor_hornet_grade_1", ComponentItem::new)
            .lang("Razor Hornet Mk.1")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> PULVERIZING_BEETLE_GRADE_1 = REGISTRATE
            .item("pulverizing_beetle_grade_1", ComponentItem::new)
            .lang("Pulverizing Beetle Mk.1")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> CRUCIBLE_MANTIS_GRADE_1 = REGISTRATE
            .item("crucible_mantis_grade_1", ComponentItem::new)
            .lang("Crucible Mantis Mk.1")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> HAULER_PROBE_GRADE_2 = REGISTRATE
            .item("freight_beetle_grade_2", ComponentItem::new)
            .lang("Freight Beetle Mk.2")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> ARMORED_HAULER_PROBE_GRADE_2 = REGISTRATE
            .item("armored_freight_beetle_grade_2", ComponentItem::new)
            .lang("Armored Freight Beetle Mk.2")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> CRUSHER_WASPS_GRADE_2 = REGISTRATE
            .item("crusher_wasps_grade_2", ComponentItem::new)
            .lang("Crusher Wasps Mk.2")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> RAZOR_HORNET_GRADE_2 = REGISTRATE
            .item("razor_hornet_grade_2", ComponentItem::new)
            .lang("Razor Hornet Mk.2")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> PULVERIZING_BEETLE_GRADE_2 = REGISTRATE
            .item("pulverizing_beetle_grade_2", ComponentItem::new)
            .lang("Pulverizing Beetle Mk.2")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> CRUCIBLE_MANTIS_GRADE_2 = REGISTRATE
            .item("razor_hornet_grade_2", ComponentItem::new)
            .lang("Crucible Mantis Mk.2")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();
    // GRADE 3

    public static final ItemEntry<ComponentItem> HAULER_PROBE_GRADE_3 = REGISTRATE
            .item("freight_beetle_grade_3", ComponentItem::new)
            .lang("Freight Beetle Mk.3")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> ARMORED_HAULER_PROBE_GRADE_3 = REGISTRATE
            .item("armored_freight_beetle_grade_3", ComponentItem::new)
            .lang("Armored Freight Beetle Mk.3")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> CRUSHER_WASPS_GRADE_3 = REGISTRATE
            .item("crusher_wasps_grade_3", ComponentItem::new)
            .lang("Crusher Wasps Mk.3")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> RAZOR_HORNET_GRADE_3 = REGISTRATE
            .item("razor_hornet_grade_3", ComponentItem::new)
            .lang("Razor Hornet Mk.3")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> PULVERIZING_BEETLE_GRADE_3 = REGISTRATE
            .item("pulverizing_beetle_grade_3", ComponentItem::new)
            .lang("Pulverizing Beetle Mk.3")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> CRUCIBLE_MANTIS_GRADE_3 = REGISTRATE
            .item("razor_hornet_grade_3", ComponentItem::new)
            .lang("Crucible Mantis Mk.3")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    // GRADE 4

    public static final ItemEntry<ComponentItem> HAULER_PROBE_GRADE_4 = REGISTRATE
            .item("freight_beetle_grade_4", ComponentItem::new)
            .lang("Freight Beetle Mk.4")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> ARMORED_HAULER_PROBE_GRADE_4 = REGISTRATE
            .item("armored_freight_beetle_grade_4", ComponentItem::new)
            .lang("Armored Freight Beetle Mk.4")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> CRUSHER_WASPS_GRADE_4 = REGISTRATE
            .item("crusher_wasps_grade_4", ComponentItem::new)
            .lang("Crusher Wasps Mk.4")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> RAZOR_HORNET_GRADE_4 = REGISTRATE
            .item("razor_hornet_grade_4", ComponentItem::new)
            .lang("Razor Hornet Mk.4")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> PULVERIZING_BEETLE_GRADE_4 = REGISTRATE
            .item("pulverizing_beetle_grade_4", ComponentItem::new)
            .lang("Pulverizing Beetle Mk.4")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> CRUCIBLE_MANTIS_GRADE_4 = REGISTRATE
            .item("razor_hornet_grade_4", ComponentItem::new)
            .lang("Crucible Mantis Mk.4")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> HAULER_PROBE_GRADE_5 = REGISTRATE
            .item("freight_beetle_grade_5", ComponentItem::new)
            .lang("Freight Beetle Mk.5")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> ARMORED_HAULER_PROBE_GRADE_5 = REGISTRATE
            .item("armored_freight_beetle_grade_5", ComponentItem::new)
            .lang("Armored Freight Beetle Mk.5")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> CRUSHER_WASPS_GRADE_5 = REGISTRATE
            .item("crusher_wasps_grade_5", ComponentItem::new)
            .lang("Crusher Wasps Mk.5")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> RAZOR_HORNET_GRADE_5 = REGISTRATE
            .item("razor_hornet_grade_5", ComponentItem::new)
            .lang("Razor Hornet Mk.5")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> PULVERIZING_BEETLE_GRADE_5 = REGISTRATE
            .item("pulverizing_beetle_grade_5", ComponentItem::new)
            .lang("Pulverizing Beetle Mk.5")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> CRUCIBLE_MANTIS_GRADE_5 = REGISTRATE
            .item("razor_hornet_grade_5", ComponentItem::new)
            .lang("Crucible Mantis Mk.5")
            .properties(p -> p.stacksTo(64).durability(1024))
            .tag()
            .defaultModel()
            .register();

    public static ItemEntry<ComponentItem> LINKED_TERMINAL = REGISTRATE
            .item("linked_terminal", ComponentItem::new)
            .lang("Linked Terminal")
            .model((ctx, prov) -> prov.generated(
                    ctx::getEntry,
                    prov.modLoc("item/terminal/linked_terminal"),
                    prov.modLoc("item/terminal/terminal_overlay")))
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(new LinkedTerminalBehavior()))
            .register();

    public static final ItemEntry<AsteroidItem> CARBON_ASTEROID = REGISTRATE
            .item("carbon_asteroid_base", AsteroidItem::new)
            .lang("Carbonic Asteroid")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .onRegister(attachRenderer(() -> RadianceItemRenderer.INSTANCE))
            .register();
    public static final ItemEntry<AsteroidItem> FERRIC_ASTEROID = REGISTRATE
            .item("ferric_asteroid", AsteroidItem::new)
            .lang("Ferric Asteroid")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .onRegister(attachRenderer(() -> RadianceItemRenderer.INSTANCE))
            .register();

    public static final ItemEntry<AsteroidItem> RARE_METAL_ASTEROID = REGISTRATE
            .item("rare_metals_asteroid", AsteroidItem::new)
            .lang("Exotic Metals Asteroid")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .onRegister(attachRenderer(() -> RadianceItemRenderer.INSTANCE))
            .register();

    public static final ItemEntry<AsteroidItem> AURIC_ASTEROID = REGISTRATE
            .item("auric_asteroid", AsteroidItem::new)
            .lang("Auric Asteroid")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .onRegister(attachRenderer(() -> RadianceItemRenderer.INSTANCE))
            .register();

    public static final ItemEntry<AsteroidItem> BRIMSTONE_ASTEROID = REGISTRATE
            .item("brimstone_asteroid", AsteroidItem::new)
            .lang("Brimstone Asteroid")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .onRegister(attachRenderer(() -> RadianceItemRenderer.INSTANCE))
            .register();

    public static final ItemEntry<AsteroidItem> LITH_ASTEROID = REGISTRATE
            .item("lith_asteroid", AsteroidItem::new)
            .lang("Lith Asteroid")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .onRegister(attachRenderer(() -> RadianceItemRenderer.INSTANCE))
            .register();

    public static final ItemEntry<AsteroidItem> MAFIC_ASTEROID = REGISTRATE
            .item("mafic_asteroid", AsteroidItem::new)
            .lang("Mafic Asteroid")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .onRegister(attachRenderer(() -> RadianceItemRenderer.INSTANCE))
            .register();

    public static final ItemEntry<AsteroidItem> MOSSY_ASTEROID = REGISTRATE
            .item("mossy_asteroid", AsteroidItem::new)
            .lang("Mossy Asteroid")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .onRegister(attachRenderer(() -> RadianceItemRenderer.INSTANCE))
            .register();

    public static final ItemEntry<AsteroidItem> OCCULT_ASTEROID = REGISTRATE
            .item("occult_asteroid", AsteroidItem::new)
            .lang("Occult Asteroid")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .onRegister(attachRenderer(() -> RadianceItemRenderer.INSTANCE))
            .register();

    public static final ItemEntry<AsteroidItem> OXIDE_ASTEROID = REGISTRATE
            .item("oxide_asteroid", AsteroidItem::new)
            .lang("Oxide Asteroid")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .onRegister(attachRenderer(() -> RadianceItemRenderer.INSTANCE))
            .register();

    public static final ItemEntry<AsteroidItem> SANGUINE_ASTEROID = REGISTRATE
            .item("sanguine_asteroid", AsteroidItem::new)
            .lang("Sanguine Asteroid")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .onRegister(attachRenderer(() -> RadianceItemRenderer.INSTANCE))
            .register();

    public static final ItemEntry<AsteroidItem> WASTELAND_ASTEROID = REGISTRATE
            .item("wasteland_asteroid", AsteroidItem::new)
            .lang("Wasteland Asteroid")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .onRegister(attachRenderer(() -> RadianceItemRenderer.INSTANCE))
            .register();

    public static final ItemEntry<ComponentItem> TUNGSTENSTEEL_NANOLATTICE_SPOOL = REGISTRATE
            .item("tungstensteel_nanolattice_spool", ComponentItem::new)
            .lang("Tungstensteel Nanolattice Spool")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> TRINAVINE_NANOLATTICE_SPOOL = REGISTRATE
            .item("trinavine_nanolattice_spool", ComponentItem::new)
            .lang("Trinavine Nanolattice Spool")
            .properties(p -> p.stacksTo(16))
            .tag()
            .defaultModel()
            .register();
    // What we'd write our NBT ON and Read in LARVA
    public static final ItemEntry<AsteroidTargetingChipItem> TARGETING_CHIP = REGISTRATE
            .item("asteroid_targeting_chip", props -> new AsteroidTargetingChipItem(props.stacksTo(1)))
            .lang("Asteroid Targeting Chip")
            .properties(p -> p.stacksTo(1))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> FLESH_PACKED_PLUTONIUM_FUEL = REGISTRATE
            .item("flesh_packed_plutonium_fuel", ComponentItem::new)
            .lang("Flesh Packed Plutonium Fuel")
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> FLESH_PACKED_URANIUM_FUEL = REGISTRATE
            .item("flesh_packed_uranium_fuel", ComponentItem::new)
            .lang("Flesh Packed Uranium Fuel")
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> FLESH_PACKED_NEPTUNIUM_FUEL = REGISTRATE
            .item("flesh_packed_neptunium_fuel", ComponentItem::new)
            .lang("Flesh Packed Neptunium Fuel")
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> SPENT_FLESH_PACKED_PLUTONIUM_FUEL = REGISTRATE
            .item("spent_flesh_packed_plutonium_fuel", ComponentItem::new)
            .lang("Spent Flesh Packed Plutonium Fuel")
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> SPENT_FLESH_PACKED_URANIUM_FUEL = REGISTRATE
            .item("spent_flesh_packed_uranium_fuel", ComponentItem::new)
            .lang("Spent Flesh Packed Uranium Fuel")
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> SPENT_FLESH_PACKED_NEPTUNIUM_FUEL = REGISTRATE
            .item("spent_flesh_packed_neptunium_fuel", ComponentItem::new)
            .lang("Spent Flesh Packed Neptunium Fuel")
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> SUPERHEATED_FUEL_ROD = REGISTRATE
            .item("superheated_fuel_rod", ComponentItem::new)
            .lang("Superheated Fuel Rod")
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> EMPTY_FUEL_ROD = REGISTRATE
            .item("empty_fuel_rod", ComponentItem::new)
            .lang("Empty Fuel Rod")
            .properties(p -> p.stacksTo(16))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> FLESH_WASTE_URANIUM = REGISTRATE
            .item("fleshy_uranium_waste", ComponentItem::new)
            .lang("Bio-Metallic Fleshy Uranium Waste")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> FLESH_WASTE_PLUTONIUM = REGISTRATE
            .item("fleshy_plutonium_waste", ComponentItem::new)
            .lang("Bio-Metallic Fleshy Plutonium Waste")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> FLESH_WASTE_NEPTUNIUM = REGISTRATE
            .item("fleshy_neptunium_waste", ComponentItem::new)
            .lang("Bio-Metallic Fleshy Neptunium Waste")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    // -------------------------------------------------------------------------
    // Air Bladder - pre-LV portable air
    // -------------------------------------------------------------------------

    public static final ItemEntry<AirBladderItem> AIR_BLADDER = REGISTRATE
            .item("air_bladder", AirBladderItem::new)
            .lang("Air Bladder")
            .properties(p -> p.stacksTo(1))
            .model((ctx, prov) -> {})
            .onRegister(modelPredicate(
                    ResourceLocation.fromNamespaceAndPath(CosmicCore.MOD_ID, "charges"),
                    stack -> (float) AirBladderItem.getCharges(stack) / OxygenConfig.AIR_BLADDER_MAX_CHARGES))
            .register();

    // -------------------------------------------------------------------------
    // Oxygen Supply Tanks
    // -------------------------------------------------------------------------

    public static final ItemEntry<OxygenTankItem> OXYGEN_SUPPLY_TANK_BRONZE = REGISTRATE
            .item("bronze_supply_tank", OxygenTankItem::new)
            .lang("Bronze Supply Tank")
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(new OxygenSupplyTankBehavior(1000, 10)))
            .defaultModel()
            .register();

    public static final ItemEntry<OxygenTankItem> OXYGEN_SUPPLY_TANK_STEEL = REGISTRATE
            .item("steel_supply_tank", OxygenTankItem::new)
            .lang("Steel Supply Tank")
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(new OxygenSupplyTankBehavior(2500, 15)))
            .defaultModel()
            .register();

    public static ICustomDescriptionId cellName() {
        return new ICustomDescriptionId() {

            @Override
            public Component getItemName(ItemStack stack) {
                Component prefix = FluidUtil.getFluidContained(stack).map(FluidStack::getDisplayName)
                        .orElse(Component.translatable("gtceu.fluid.empty"));
                return Component.translatable(stack.getDescriptionId(), prefix);
            }
        };
    }

    public static <T extends ComponentItem> NonNullConsumer<T> attach(IItemComponent... components) {
        return item -> item.attachComponents(components);
    }

    public static ItemEntry<TravelerBootsItem> STEEL_TRAVELERS_BOOTS = REGISTRATE
            .item("steel_travelers_boots",
                    p -> new TravelerBootsItem(CosmicArmorMaterials.STEEL_TRAVELER,
                            p.durability(ArmorItem.Type.BOOTS.getDurability(33))))
            .lang("Steel Traveler's Boots")
            .properties(p -> p.rarity(Rarity.UNCOMMON))
            .tag(CustomTags.PPE_ARMOR)
            .register();

    public static ItemEntry<TravelerBootsItem> NETHERITE_TRAVELERS_BOOTS = REGISTRATE
            .item("netherite_travelers_boots",
                    p -> new TravelerBootsItem(CosmicArmorMaterials.NETHERITE_TRAVELER,
                            p.durability(ArmorItem.Type.BOOTS.getDurability(37)).fireResistant()))
            .lang("Netherite Traveler's Boots")
            .properties(p -> p.rarity(Rarity.RARE))
            .tag(CustomTags.PPE_ARMOR)
            .register();

    public static ItemEntry<TravelerBootsItem> NANO_BOOTS = REGISTRATE
            .item("nano_travelers_boots",
                    p -> new TravelerBootsItem(CosmicArmorMaterials.NANO_TRAVELER,
                            p.durability(ArmorItem.Type.BOOTS.getDurability(55)).fireResistant()))
            .lang("Nano Traveler's Boots")
            .properties(p -> p.rarity(Rarity.EPIC))
            .tag(CustomTags.PPE_ARMOR)
            .register();

    public static ItemEntry<TravelerBootsItem> QUARK_BOOTS = REGISTRATE
            .item("quark_travelers_boots",
                    p -> new TravelerBootsItem(CosmicArmorMaterials.QUARK_TRAVELER,
                            p.durability(ArmorItem.Type.BOOTS.getDurability(75)).fireResistant()))
            .lang("Quark Traveler's Boots")
            .properties(p -> p.rarity(Rarity.EPIC))
            .tag(CustomTags.PPE_ARMOR)
            .register();

    // -------------------------------------------------------------------------
    // Reflection System
    // -------------------------------------------------------------------------

    public static final ItemEntry<MirrorItem> REFLECTION_MIRROR = REGISTRATE
            .item("reflection_mirror", MirrorItem::new)
            .lang("Mirror of Erosion")
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .register();

    public static final ItemEntry<SoulMutilatorItem> SOUL_MUTILATOR = REGISTRATE
            .item("soul_mutilator", SoulMutilatorItem::new)
            .lang("Soul Mutilator")
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .register();

    // -------------------------------------------------------------------------
    // Vein Survey Scanner
    // -------------------------------------------------------------------------

    // LV tier - shows vein type + distance only
    public static final ItemEntry<ComponentItem> VEIN_SURVEY_SCANNER_LV = REGISTRATE
            .item("vein_survey_scanner_lv", ComponentItem::new)
            .lang("Vein Survey Scanner (LV)")
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(
                    ElectricStats.createElectricItem(100_000L, GTValues.LV),
                    new VeinSurveyBehavior(2000, GTValues.V[GTValues.LV] * 4, VeinSurveyBehavior.DetailLevel.BASIC)))
            .defaultModel()
            .register();

    // MV tier - also shows compass direction
    public static final ItemEntry<ComponentItem> VEIN_SURVEY_SCANNER_MV = REGISTRATE
            .item("vein_survey_scanner_mv", ComponentItem::new)
            .lang("Advanced Vein Survey Scanner (MV)")
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(
                    ElectricStats.createElectricItem(400_000L, GTValues.MV),
                    new VeinSurveyBehavior(2000, GTValues.V[GTValues.MV] * 4, VeinSurveyBehavior.DetailLevel.COMPASS)))
            .defaultModel()
            .register();

    // HV tier - shows exact coordinates
    public static final ItemEntry<ComponentItem> VEIN_SURVEY_SCANNER_HV = REGISTRATE
            .item("vein_survey_scanner_hv", ComponentItem::new)
            .lang("Precision Vein Survey Scanner (HV)")
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(
                    ElectricStats.createElectricItem(1_600_000L, GTValues.HV),
                    new VeinSurveyBehavior(2000, GTValues.V[GTValues.HV] * 4, VeinSurveyBehavior.DetailLevel.PRECISE)))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> DOWSING_ROD = REGISTRATE
            .item("dowsing_rod", ComponentItem::new)
            .lang("Dowsing Rod")
            .properties(p -> p.stacksTo(1))
            .onRegister(attach(new DowsingRodBehavior(500, 100)))
            .defaultModel()
            .register();

    // Oneiric Signets â€” non-consumed gate items (Blood Orb replacement)
    public static final ItemEntry<ComponentItem> ONEIRIC_SIGNET_T1 = REGISTRATE
            .item("oneiric_signet_t1", ComponentItem::new)
            .lang("Oneiric Signet Mk.I")
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ONEIRIC_SIGNET_T2 = REGISTRATE
            .item("oneiric_signet_t2", ComponentItem::new)
            .lang("Oneiric Signet Mk.II")
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ONEIRIC_SIGNET_T3 = REGISTRATE
            .item("oneiric_signet_t3", ComponentItem::new)
            .lang("Oneiric Signet Mk.III")
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ONEIRIC_SIGNET_T4 = REGISTRATE
            .item("oneiric_signet_t4", ComponentItem::new)
            .lang("Oneiric Signet Mk.IV")
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ONEIRIC_SIGNET_T5 = REGISTRATE
            .item("oneiric_signet_t5", ComponentItem::new)
            .lang("Oneiric Signet Mk.V")
            .properties(p -> p.stacksTo(1))
            .defaultModel()
            .register();

    // Soul Motes â€” tiered crafting ingredients (Slate replacement)
    public static final ItemEntry<ComponentItem> FAINT_MOTE = REGISTRATE
            .item("faint_mote", ComponentItem::new)
            .lang("Faint Mote")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> DIM_MOTE = REGISTRATE
            .item("dim_mote", ComponentItem::new)
            .lang("Dim Mote")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PALE_MOTE = REGISTRATE
            .item("pale_mote", ComponentItem::new)
            .lang("Pale Mote")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CLEAR_MOTE = REGISTRATE
            .item("clear_mote", ComponentItem::new)
            .lang("Clear Mote")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> BRIGHT_MOTE = REGISTRATE
            .item("bright_mote", ComponentItem::new)
            .lang("Bright Mote")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RADIANT_MOTE = REGISTRATE
            .item("radiant_mote", ComponentItem::new)
            .lang("Radiant Mote")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> VIVID_MOTE = REGISTRATE
            .item("vivid_mote", ComponentItem::new)
            .lang("Vivid Mote")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> BLAZING_MOTE = REGISTRATE
            .item("blazing_mote", ComponentItem::new)
            .lang("Blazing Mote")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> INCANDESCENT_MOTE = REGISTRATE
            .item("incandescent_mote", ComponentItem::new)
            .lang("Incandescent Mote")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> TRANSCENDENT_MOTE = REGISTRATE
            .item("transcendent_mote", ComponentItem::new)
            .lang("Transcendent Mote")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    // Wrapped Soul Motes â€” intermediate crafting step between mote tiers
    public static final ItemEntry<ComponentItem> WRAPPED_FAINT_MOTE = REGISTRATE
            .item("wrapped_faint_mote", ComponentItem::new)
            .lang("Wrapped Faint Mote")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> WRAPPED_DIM_MOTE = REGISTRATE
            .item("wrapped_dim_mote", ComponentItem::new)
            .lang("Wrapped Dim Mote")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> WRAPPED_PALE_MOTE = REGISTRATE
            .item("wrapped_pale_mote", ComponentItem::new)
            .lang("Wrapped Pale Mote")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> WRAPPED_CLEAR_MOTE = REGISTRATE
            .item("wrapped_clear_mote", ComponentItem::new)
            .lang("Wrapped Clear Mote")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> WRAPPED_BRIGHT_MOTE = REGISTRATE
            .item("wrapped_bright_mote", ComponentItem::new)
            .lang("Wrapped Bright Mote")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> WRAPPED_RADIANT_MOTE = REGISTRATE
            .item("wrapped_radiant_mote", ComponentItem::new)
            .lang("Wrapped Radiant Mote")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> WRAPPED_VIVID_MOTE = REGISTRATE
            .item("wrapped_vivid_mote", ComponentItem::new)
            .lang("Wrapped Vivid Mote")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> WRAPPED_BLAZING_MOTE = REGISTRATE
            .item("wrapped_blazing_mote", ComponentItem::new)
            .lang("Wrapped Blazing Mote")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> WRAPPED_INCANDESCENT_MOTE = REGISTRATE
            .item("wrapped_incandescent_mote", ComponentItem::new)
            .lang("Wrapped Incandescent Mote")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> WRAPPED_TRANSCENDENT_MOTE = REGISTRATE
            .item("wrapped_transcendent_mote", ComponentItem::new)
            .lang("Wrapped Transcendent Mote")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    // Soul Reagents â€” BM reagent replacements
    public static final ItemEntry<ComponentItem> SOMNOLENT_DEW = REGISTRATE
            .item("somnolent_dew", ComponentItem::new)
            .lang("Somnolent Dew")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PYRETIC_ICHOR = REGISTRATE
            .item("pyretic_ichor", ComponentItem::new)
            .lang("Pyretic Ichor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> ETHER_WISP = REGISTRATE
            .item("ether_wisp", ComponentItem::new)
            .lang("Ether Wisp")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> VERDANT_ANIMA = REGISTRATE
            .item("verdant_anima", ComponentItem::new)
            .lang("Verdant Anima")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> TETHERING_RESIN = REGISTRATE
            .item("tethering_resin", ComponentItem::new)
            .lang("Tethering Resin")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> STASIS_BRINE = REGISTRATE
            .item("stasis_brine", ComponentItem::new)
            .lang("Stasis Brine")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    // Tau items â€” BM tau replacements
    public static final ItemEntry<ComponentItem> TAU_OIL = REGISTRATE
            .item("tau_oil", ComponentItem::new)
            .lang("Tau Oil")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> WEAK_BINDING_PUTTY = REGISTRATE
            .item("weak_binding_putty", ComponentItem::new)
            .lang("Weak Binding Putty")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> COMPACTIFIED_ROD = REGISTRATE
            .item("compactified_rod", ComponentItem::new)
            .lang("Compactified Rod")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> HEAVY_BINDINGS = REGISTRATE
            .item("heavy_bindings", ComponentItem::new)
            .lang("Heavy Bindings")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> STURDY_PLATES = REGISTRATE
            .item("sturdy_plates", ComponentItem::new)
            .lang("Sturdy Plates")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> COGWORK_UNITS = REGISTRATE
            .item("cogwork_units", ComponentItem::new)
            .lang("Cogwork Units")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> VULCANIZED_BINDING_COMPOUND = REGISTRATE
            .item("vulcanized_binding_compound", ComponentItem::new)
            .lang("Vulcanized Binding Compound")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RIVETED_STRAPS = REGISTRATE
            .item("riveted_straps", ComponentItem::new)
            .lang("Riveted Straps")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CREOSOTE_GREASE = REGISTRATE
            .item("creosote_grease", ComponentItem::new)
            .lang("Creosote Grease")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> LV_COGWORK_MAGICAPACITOR = REGISTRATE
            .item("lv_cogwork_magicapacitor", ComponentItem::new)
            .lang("LV Cogwork Magicapacitor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> LV_MODULAR_FRAMEWORKS = REGISTRATE
            .item("lv_modular_frameworks", ComponentItem::new)
            .lang("LV Modular Frameworks")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> LV_POWER_INTERFACE = REGISTRATE
            .item("lv_power_interface", ComponentItem::new)
            .lang("LV Power Interface")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> LV_CLADDING = REGISTRATE
            .item("lv_cladding", ComponentItem::new)
            .lang("LV Cladding")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> MV_COGWORK_MAGICAPACITOR = REGISTRATE
            .item("mv_cogwork_magicapacitor", ComponentItem::new)
            .lang("MV Cogwork Magicapacitor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> MV_MODULAR_FRAMEWORKS = REGISTRATE
            .item("mv_modular_frameworks", ComponentItem::new)
            .lang("MV Modular Frameworks")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> MV_CLADDING = REGISTRATE
            .item("mv_cladding", ComponentItem::new)
            .lang("MV Cladding")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> HV_COGWORK_MAGICAPACITOR = REGISTRATE
            .item("hv_cogwork_magicapacitor", ComponentItem::new)
            .lang("HV Cogwork Magicapacitor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> HV_MODULAR_FRAMEWORKS = REGISTRATE
            .item("hv_modular_frameworks", ComponentItem::new)
            .lang("HV Modular Frameworks")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> HV_CLADDING = REGISTRATE
            .item("hv_cladding", ComponentItem::new)
            .lang("HV Cladding")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    public static final ItemEntry<ComponentItem> SHREDDED_MAGEBLOOM = REGISTRATE
            .item("shredded_magebloom", ComponentItem::new)
            .lang("Shredded Magebloom")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> WASHED_MAGEBLOOM = REGISTRATE
            .item("washed_magebloom", ComponentItem::new)
            .lang("Washed Magebloom")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> EMBRACED_LUMINITE_INGOT = REGISTRATE
            .item("embraced_luminite_ingot", ComponentItem::new)
            .lang("Embraced Luminite Ingot")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> NEUTRONIUM_LATTICE = REGISTRATE
            .item("neutronium_lattice", ComponentItem::new)
            .lang("Neutronium Lattice")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> HEAVY_NEUTRON_FILTER = REGISTRATE
            .item("heavy_neutron_filter", ComponentItem::new)
            .lang("Heavy Neutron Filter")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> NEUTRONIUM_SEEDLING = REGISTRATE
            .item("neutronium_seedling", ComponentItem::new)
            .lang("Neutronium Seedling")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> TERRAWEAVE_CLOTH = REGISTRATE
            .item("terraweave_cloth", ComponentItem::new)
            .lang("Terraweave Cloth")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CARBON_ETHERSTEEL_LATTICE = REGISTRATE
            .item("carbon_ethersteel_lattice", ComponentItem::new)
            .lang("Carbon Fiber Ethersteel Lattice")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> RESPLENDENT_SYLVAN_NANOLATTICE = REGISTRATE
            .item("resplendent_sylvan_nanolattice", ComponentItem::new)
            .lang("Resplendent Sylvan Nanolattice")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SPOOLED_TERRAWEAVE = REGISTRATE
            .item("spooled_terraweave", ComponentItem::new)
            .lang("Spooled Terraweave")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> TARANIUM_RESONANTION_CHARGE = REGISTRATE
            .item("taranium_resonantion_charge", ComponentItem::new)
            .lang("Taranium Resonantion Charge")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> NAQUADRIA_RESONANTION_CHARGE = REGISTRATE
            .item("naquadria_resonantion_charge", ComponentItem::new)
            .lang("Naquadria Resonantion Charge")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CHROMATICALLY_DOPED_NETHER_STAR_BOULE = REGISTRATE
            .item("chromatically_doped_nether_star_boule", ComponentItem::new)
            .lang("Chromatically Doped Nether star Boule")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SEAL_SHARDS = REGISTRATE
            .item("seal_shards", ComponentItem::new)
            .lang("Shards of the Underworld")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PRISMATIC_LENS = REGISTRATE
            .item("prismatic_lens", ComponentItem::new)
            .lang("Prismatic Lens")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> UNDERGARDEN_DIM = REGISTRATE
            .item("undergarden_dim", ComponentItem::new)
            .lang("Undergarden")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> CINDER_FUEL_PELLETS = REGISTRATE
            .item("cinder_fuel_pellets", ComponentItem::new)
            .lang("Cinder Fuel Ember Pellets")
            .properties(p -> p.stacksTo(64))
            .tag()
            .defaultModel()
            .register();

    public static <T extends Item> NonNullConsumer<T> modelPredicate(ResourceLocation predicate,
                                                                     Function<ItemStack, Float> property) {
        return item -> {
            if (GTCEu.isClientSide()) {
                ItemProperties.register(item, predicate, (itemStack, c, l, i) -> property.apply(itemStack));
            }
        };
    }

    public static void init() {}
}
