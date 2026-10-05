package com.ghostipedia.cosmiccore.common.compat.gtceu;

import com.gregtechceu.gtceu.api.machine.MachineDefinition;
import com.gregtechceu.gtceu.api.machine.MultiblockMachineDefinition;
import com.gregtechceu.gtceu.api.multiblock.pattern.IBlockPattern;
import com.gregtechceu.gtceu.api.registry.registrate.entry.MachineEntry;

import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class MachineDefinitionOverrides {

    private static final Map<ResourceLocation, List<Consumer<MachineDefinition.Properties>>> OVERRIDES = new HashMap<>();

    private MachineDefinitionOverrides() {}

    public static void edit(MachineEntry<?> entry, Consumer<MachineDefinition.Properties> edit) {
        OVERRIDES.computeIfAbsent(entry.getId(), ignored -> new ArrayList<>()).add(edit);
    }

    public static void editMultiblock(MachineEntry<MultiblockMachineDefinition> entry,
                                      Consumer<MultiblockMachineDefinition.Properties> edit) {
        edit(entry, properties -> edit.accept((MultiblockMachineDefinition.Properties) properties));
    }

    public static void setPattern(MachineEntry<MultiblockMachineDefinition> entry, String name,
                                  Supplier<IBlockPattern> pattern) {
        editMultiblock(entry, properties -> properties.patterns().put(name, ignored -> pattern.get()));
    }

    public static void apply(ResourceLocation id, MachineDefinition.Properties properties) {
        for (var edit : OVERRIDES.getOrDefault(id, List.of())) {
            edit.accept(properties);
        }
    }
}
