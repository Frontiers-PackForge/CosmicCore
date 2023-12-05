package com.ghostipedia.CosmicCore.common.data;

import com.ghostipedia.CosmicCore.CosmicCore;
import com.gregtechceu.gtceu.api.item.ComponentItem;
import com.gregtechceu.gtceu.api.item.component.IItemComponent;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import static com.ghostipedia.CosmicCore.api.registries.CosmicRegistries.REGISTRATE;


@SuppressWarnings("Convert2MethodRef")
public class CosmicItems  {
    static {
        GTRegistries.REGISTRATE.creativeModeTab(() -> CosmicCreativeModeTabs.COSMIC_CORE);
    }
    // region chips
    public static final ItemEntry<ComponentItem> DONK = REGISTRATE.item("donk", ComponentItem::create)
            .lang("Donk")
            .properties(p -> p.stacksTo(16))
            .defaultModel()
            .register();

 //New Circuits
 //Sonic (ZPM-UEV)
    public static final ItemEntry<ComponentItem> SONAR_PROCESSOR = REGISTRATE.item("sonic_processor", ComponentItem::create)
            .lang("Sonic Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SONAR_PROCESSOR_ASSEMBLY = REGISTRATE.item("sonic_processor_assembly", ComponentItem::create)
            .lang("Sonic Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SONAR_PROCESSOR_SUPERCOMPUTER = REGISTRATE.item("sonic_processor_supercomputer", ComponentItem::create)
            .lang("Sonic Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> SONAR_PROCESSOR_MAINFRAME = REGISTRATE.item("sonic_processor_mainframe", ComponentItem::create)
            .lang("Sonic Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
//Optical (UV-UIV)
    public static final ItemEntry<ComponentItem> OPTICAL_PROCESSOR = REGISTRATE.item("optical_processor", ComponentItem::create)
            .lang("Optical Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OPTICAL_PROCESSOR_ASSEMBLY = REGISTRATE.item("optical_processor_assembly", ComponentItem::create)
            .lang("Optical Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OPTICAL_PROCESSOR_SUPERCOMPUTER = REGISTRATE.item("optical_processor_supercomputer", ComponentItem::create)
            .lang("Optical Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> OPTICAL_PROCESSOR_MAINFRAME = REGISTRATE.item("optical_processor_mainframe", ComponentItem::create)
            .lang("Optical Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
//Cosmic (UHV-UXV)
    public static final ItemEntry<ComponentItem> COSMIC_PROCESSOR = REGISTRATE.item("cosmic_processor", ComponentItem::create)
            .lang("Cosmic Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> COSMIC_PROCESSOR_ASSEMBLY = REGISTRATE.item("cosmic_processor_assembly", ComponentItem::create)
            .lang("Cosmic Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> COSMIC_PROCESSOR_SUPERCOMPUTER = REGISTRATE.item("cosmic_processor_supercomputer", ComponentItem::create)
            .lang("Cosmic Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> COSMIC_PROCESSOR_MAINFRAME = REGISTRATE.item("cosmic_processor_mainframe", ComponentItem::create)
            .lang("Cosmic Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
//Psionic Circuit (UEV-OPV)
    public static final ItemEntry<ComponentItem> PSIONIC_PROCESSOR = REGISTRATE.item("psionic_processor", ComponentItem::create)
            .lang("Psionic Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PSIONIC_PROCESSOR_ASSEMBLY = REGISTRATE.item("psionic_processor_assembly", ComponentItem::create)
            .lang("Psionic Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PSIONIC_PROCESSOR_SUPERCOMPUTER = REGISTRATE.item("psionic_processor_supercomputer", ComponentItem::create)
            .lang("Psionic Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> PSIONIC_PROCESSOR_MAINFRAME = REGISTRATE.item("psionic_processor_mainframe", ComponentItem::create)
            .lang("Psionic Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    //Macroverse (UIV-MAX)
    public static final ItemEntry<ComponentItem> MACROVERSE_PROCESSOR = REGISTRATE.item("macroverse_processor", ComponentItem::create)
            .lang("Macroverse Processor")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> MACROVERSE_PROCESSOR_ASSEMBLY = REGISTRATE.item("macroverse_processor_assembly", ComponentItem::create)
            .lang("Macroverse Processor Assembly")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> MACROVERSE_PROCESSOR_SUPERCOMPUTER = REGISTRATE.item("macroverse_processor_supercomputer", ComponentItem::create)
            .lang("Macroverse Processor Supercomputer")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();
    public static final ItemEntry<ComponentItem> MACROVERSE_PROCESSOR_MAINFRAME = REGISTRATE.item("macroverse_processor_mainframe", ComponentItem::create)
            .lang("Macroverse Processor Mainframe")
            .properties(p -> p.stacksTo(64))
            .defaultModel()
            .register();

    public static <T extends ComponentItem> NonNullConsumer<T> attach(IItemComponent... components) {
        return item -> item.attachComponents(components);
    }

    public static void init() {

    }
}