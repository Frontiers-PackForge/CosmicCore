package com.ghostipedia.cosmiccore.gtbridge.recipemaker;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.item.component.IItemUIFactory;
import com.gregtechceu.gtceu.api.machine.multiblock.CleanroomType;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;

import com.lowdragmc.lowdraglib.gui.factory.HeldItemUIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib.gui.widget.ButtonWidget;
import com.lowdragmc.lowdraglib.gui.widget.DraggableScrollableWidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.LabelWidget;
import com.lowdragmc.lowdraglib.gui.widget.SelectorWidget;
import com.lowdragmc.lowdraglib.gui.widget.TextFieldWidget;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.gui.widget.custom.PlayerInventoryWidget;
import com.lowdragmc.lowdraglib.utils.Position;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RecipeMakerBehavior implements IItemUIFactory {

    private static final String CRAFTING = "minecraft:crafting";
    private static final int WIDTH = 288;
    private static final int HEIGHT = 312;
    private static final int POOL_ITEM = 64;
    private static final int POOL_FLUID = 32;
    private static final int TANK_CAPACITY = 64_000;
    private static final int COLS = 3;

    @Override
    public ModularUI createUI(HeldItemUIFactory.HeldItemHolder holder, Player player) {
        ModularUI ui = new ModularUI(WIDTH, HEIGHT, holder, player);
        ui.widget(new LabelWidget(8, 6, "Recipe Forge"));

        WidgetGroup editorArea = new WidgetGroup(148, 20, WIDTH - 156, HEIGHT - 102);

        Consumer<String> open = entryId -> {
            editorArea.clearAllWidgets();
            if (CRAFTING.equals(entryId)) {
                CraftingRecipeEditor.build(editorArea);
                return;
            }
            ResourceLocation rl = ResourceLocation.tryParse(entryId);
            if (rl != null && BuiltInRegistries.RECIPE_TYPE.get(rl) instanceof GTRecipeType type) {
                buildGtEditor(editorArea, type);
            } else {
                CodecRecipeEditor.build(editorArea, player, entryId);
            }
        };

        DraggableScrollableWidgetGroup list = new DraggableScrollableWidgetGroup(8, 32, 132, HEIGHT - 114);
        list.setBackground(new ColorRectTexture(0x60000000));

        String[] search = { "" };
        Consumer<String> populate = filter -> {
            list.clearAllWidgets();
            String needle = filter == null ? "" : filter.toLowerCase(Locale.ROOT).trim();
            int[] rowY = { 1 };
            addEntry(list, rowY, "crafting (3x3)", CRAFTING, needle, open);
            for (ResourceLocation id : allRecipeTypeIds()) {
                if (id.toString().equals(CRAFTING)) continue;
                addEntry(list, rowY, id.getPath(), id.toString(), needle, open);
            }
        };
        populate.accept("");

        ui.widget(new SearchFieldWidget(8, 18, 132, 12, () -> search[0],
                s -> {
                    search[0] = s;
                    populate.accept(s);
                },
                text -> {
                    search[0] = text;
                    populate.accept(text);
                }));
        ui.widget(list);
        ui.widget(editorArea);

        PlayerInventoryWidget inventory = new PlayerInventoryWidget();
        inventory.setPlayer(player);
        inventory.setSelfPosition(new Position(63, HEIGHT - 85));
        ui.widget(inventory);

        open.accept(CRAFTING);
        ui.mainGroup.setBackground(GuiTextures.BACKGROUND);
        return ui;
    }

    private static void addEntry(DraggableScrollableWidgetGroup list, int[] rowY, String label, String id,
                                 String needle, Consumer<String> open) {
        if (!needle.isEmpty() && !id.toLowerCase(Locale.ROOT).contains(needle)) return;
        int y = rowY[0];
        list.addWidget(new ButtonWidget(1, y, 128, 14, GuiTextures.VANILLA_BUTTON, data -> open.accept(id)));
        list.addWidget(new LabelWidget(4, y + 3, label));
        rowY[0] += 15;
    }

    private static void buildGtEditor(WidgetGroup editor, GTRecipeType type) {
        CustomItemStackHandler itemIn = new CustomItemStackHandler(POOL_ITEM);
        CustomItemStackHandler itemOut = new CustomItemStackHandler(POOL_ITEM);
        FluidTank[] fluidIn = tanks();
        FluidTank[] fluidOut = tanks();
        int[] tier = { GTValues.LV };
        long[] amperage = { 1 };
        int[] duration = { 100 };
        int[] blastTemp = { 0 };
        int[] cwu = { 0 };
        String[] cleanroom = { "none" };
        String[] dimension = { "" };
        boolean[] capOpen = { false };
        boolean[] condOpen = { false };
        int[] outChance = new int[POOL_ITEM];
        int[] outBoost = new int[POOL_ITEM];
        int[] inChance = new int[POOL_ITEM];
        int[] inBoost = new int[POOL_ITEM];
        boolean[] inNoConsume = new boolean[POOL_ITEM];
        int[] fOutChance = new int[POOL_FLUID];
        int[] fOutBoost = new int[POOL_FLUID];
        int[] fInChance = new int[POOL_FLUID];
        int[] fInBoost = new int[POOL_FLUID];
        int[] selSlot = { 0 };
        boolean[] selOut = { false };
        boolean[] selFluid = { false };
        Arrays.fill(outChance, RecipeDraft.GUARANTEED);
        Arrays.fill(inChance, RecipeDraft.GUARANTEED);
        Arrays.fill(fOutChance, RecipeDraft.GUARANTEED);
        Arrays.fill(fInChance, RecipeDraft.GUARANTEED);
        Supplier<int[]> chanceArr = () -> selFluid[0] ? (selOut[0] ? fOutChance : fInChance) :
                (selOut[0] ? outChance : inChance);
        Supplier<int[]> boostArr = () -> selFluid[0] ? (selOut[0] ? fOutBoost : fInBoost) :
                (selOut[0] ? outBoost : inBoost);
        String[] recipeId = { "" };

        editor.addWidget(new LabelWidget(0, 0, type.registryName.getPath()));
        editor.addWidget(new LabelWidget(0, 11, "id"));
        editor.addWidget(new TextFieldWidget(16, 10, 106, 12, () -> recipeId[0], s -> recipeId[0] = s));
        editor.addWidget(new LabelWidget(0, 26, "Inputs"));
        editor.addWidget(new LabelWidget(66, 26, "Outputs"));

        DraggableScrollableWidgetGroup inputGroup = new DraggableScrollableWidgetGroup(0, 35, 62, 83);
        DraggableScrollableWidgetGroup outputGroup = new DraggableScrollableWidgetGroup(66, 35, 62, 83);
        inputGroup.setBackground(new ColorRectTexture(0x40000000));
        outputGroup.setBackground(new ColorRectTexture(0x40000000));
        editor.addWidget(inputGroup);
        editor.addWidget(outputGroup);

        editor.addWidget(new LabelWidget(0, 126, "Amp"));
        editor.addWidget(new TextFieldWidget(26, 124, 30, 12,
                () -> String.valueOf(amperage[0]), s -> amperage[0] = parseLong(s, 1))
                .setNumbersOnly(1L, Long.MAX_VALUE));
        editor.addWidget(new LabelWidget(64, 126, "Tick"));
        editor.addWidget(new TextFieldWidget(90, 124, 30, 12,
                () -> String.valueOf(duration[0]), s -> duration[0] = (int) parseLong(s, 100))
                .setNumbersOnly(1L, Integer.MAX_VALUE));

        editor.addWidget(new LabelWidget(0, 142, "Tier"));
        editor.addWidget(new SelectorWidget(26, 140, 90, 16, tierNames(), tier[0])
                .setIsUp(true)
                .setOnChanged(name -> tier[0] = tierIndex(name))
                .setSupplier(() -> GTValues.VN[tier[0]]));

        WidgetGroup capabilities = new WidgetGroup(0, 0, 128, 138);
        capabilities.setBackground(GuiTextures.BACKGROUND);
        capabilities.setVisible(false);
        capabilities.setActive(false);
        capabilities.addWidget(new LabelWidget(4, 3, "Capabilities"));
        capabilities.addWidget(new LabelWidget(4, 20, "CWU/t"));
        capabilities.addWidget(new TextFieldWidget(52, 18, 70, 12,
                () -> String.valueOf(cwu[0]), s -> cwu[0] = (int) parseLong(s, 0))
                .setNumbersOnly(0L, Integer.MAX_VALUE));

        WidgetGroup conditions = new WidgetGroup(0, 0, 128, 138);
        conditions.setBackground(GuiTextures.BACKGROUND);
        conditions.setVisible(false);
        conditions.setActive(false);
        conditions.addWidget(new LabelWidget(4, 3, "Conditions"));

        WidgetGroup heatRow = new WidgetGroup(0, 18, 128, 14);
        heatRow.addWidget(new LabelWidget(4, 3, "Heat K"));
        heatRow.addWidget(new TextFieldWidget(52, 1, 70, 12,
                () -> String.valueOf(blastTemp[0]), s -> blastTemp[0] = (int) parseLong(s, 0))
                .setNumbersOnly(0L, Integer.MAX_VALUE));
        conditions.addWidget(heatRow);

        conditions.addWidget(new LabelWidget(4, 38, "Clean"));
        conditions.addWidget(new SelectorWidget(40, 35, 84, 14, cleanroomNames(), cleanroomIndex(cleanroom[0]))
                .setOnChanged(name -> cleanroom[0] = name)
                .setSupplier(() -> cleanroom[0]));

        conditions.addWidget(new LabelWidget(4, 56, "Dim"));
        conditions.addWidget(new TextFieldWidget(40, 54, 84, 12,
                () -> dimension[0], s -> dimension[0] = s.trim()));

        WidgetGroup slotConfig = new WidgetGroup(0, 0, 128, 138);
        slotConfig.setBackground(GuiTextures.BACKGROUND);
        slotConfig.setVisible(false);
        slotConfig.setActive(false);
        slotConfig.addWidget(new LabelWidget(4, 3, "Slot options"));
        slotConfig.addWidget(new LabelWidget(4, 22, "Chance"));
        slotConfig.addWidget(new TextFieldWidget(52, 20, 70, 12,
                () -> String.valueOf(chanceArr.get()[selSlot[0]]),
                s -> chanceArr.get()[selSlot[0]] = (int) parseLong(s, RecipeDraft.GUARANTEED))
                .setNumbersOnly(1L, RecipeDraft.GUARANTEED));
        slotConfig.addWidget(new LabelWidget(4, 36, "/ 10000"));
        slotConfig.addWidget(new LabelWidget(4, 52, "Boost"));
        slotConfig.addWidget(new TextFieldWidget(52, 50, 70, 12,
                () -> String.valueOf(boostArr.get()[selSlot[0]]),
                s -> boostArr.get()[selSlot[0]] = (int) parseLong(s, 0)).setNumbersOnly(0L, Integer.MAX_VALUE));

        WidgetGroup consumeRow = new WidgetGroup(0, 68, 128, 16);
        consumeRow.addWidget(new LabelWidget(4, 2, "Consume"));
        consumeRow.addWidget(new SelectorWidget(52, 0, 70, 14, List.of("consumed", "not consumed"), 0)
                .setOnChanged(name -> inNoConsume[selSlot[0]] = name.equals("not consumed"))
                .setSupplier(() -> inNoConsume[selSlot[0]] ? "not consumed" : "consumed"));
        consumeRow.setVisible(false);
        consumeRow.setActive(false);
        slotConfig.addWidget(consumeRow);

        Runnable closeAll = () -> {
            capOpen[0] = false;
            condOpen[0] = false;
            capabilities.setVisible(false);
            capabilities.setActive(false);
            conditions.setVisible(false);
            conditions.setActive(false);
            slotConfig.setVisible(false);
            slotConfig.setActive(false);
        };

        SlotConfig onConfigure = (slot, isOutput, isFluid) -> {
            closeAll.run();
            selSlot[0] = slot;
            selOut[0] = isOutput;
            selFluid[0] = isFluid;
            boolean itemInput = !isOutput && !isFluid;
            consumeRow.setVisible(itemInput);
            consumeRow.setActive(itemInput);
            slotConfig.setVisible(true);
            slotConfig.setActive(true);
        };

        slotConfig.addWidget(new ButtonWidget(4, 118, 120, 14, GuiTextures.VANILLA_BUTTON, data -> closeAll.run()));
        slotConfig.addWidget(new LabelWidget(54, 121, "Close"));
        capabilities.addWidget(new ButtonWidget(4, 118, 120, 14, GuiTextures.VANILLA_BUTTON, data -> closeAll.run()));
        capabilities.addWidget(new LabelWidget(54, 121, "Close"));
        conditions.addWidget(new ButtonWidget(4, 118, 120, 14, GuiTextures.VANILLA_BUTTON, data -> closeAll.run()));
        conditions.addWidget(new LabelWidget(54, 121, "Close"));

        editor.addWidget(new ButtonWidget(0, 158, 120, 14, GuiTextures.VANILLA_BUTTON, data -> {
            boolean open = !capOpen[0];
            closeAll.run();
            capOpen[0] = open;
            capabilities.setVisible(open);
            capabilities.setActive(open);
        }));
        editor.addWidget(new LabelWidget(34, 161, "Capabilities"));

        editor.addWidget(new ButtonWidget(0, 176, 120, 14, GuiTextures.VANILLA_BUTTON, data -> {
            boolean open = !condOpen[0];
            closeAll.run();
            condOpen[0] = open;
            conditions.setVisible(open);
            conditions.setActive(open);
        }));
        editor.addWidget(new LabelWidget(38, 179, "Conditions"));

        editor.addWidget(new ExportButtonWidget(0, 192, 120, 16, GuiTextures.VANILLA_BUTTON,
                () -> buildScript(type, recipeId, itemIn, itemOut, fluidIn, fluidOut, tier, amperage, duration,
                        blastTemp, cwu, cleanroom, dimension, outChance, outBoost, inChance, inBoost, inNoConsume,
                        fOutChance, fOutBoost, fInChance, fInBoost)));
        editor.addWidget(new LabelWidget(34, 196, "Copy KubeJS"));
        editor.addWidget(capabilities);
        editor.addWidget(conditions);
        editor.addWidget(slotConfig);
        rebuild(type, inputGroup, outputGroup, itemIn, itemOut, fluidIn, fluidOut, heatRow, onConfigure);
    }

    private static void rebuild(GTRecipeType type, DraggableScrollableWidgetGroup inputGroup,
                                DraggableScrollableWidgetGroup outputGroup, CustomItemStackHandler itemIn,
                                CustomItemStackHandler itemOut, FluidTank[] fluidIn, FluidTank[] fluidOut,
                                WidgetGroup heatRow, SlotConfig onConfigure) {
        inputGroup.clearAllWidgets();
        outputGroup.clearAllWidgets();
        boolean blast = type != null && type.registryName.getPath().contains("blast");
        heatRow.setVisible(blast);
        heatRow.setActive(blast);
        if (type == null) return;
        fillSide(inputGroup, itemIn, type.getMaxInputs(ItemRecipeCapability.CAP),
                fluidIn, type.getMaxInputs(FluidRecipeCapability.CAP), false, onConfigure);
        fillSide(outputGroup, itemOut, type.getMaxOutputs(ItemRecipeCapability.CAP),
                fluidOut, type.getMaxOutputs(FluidRecipeCapability.CAP), true, onConfigure);
    }

    private static void fillSide(DraggableScrollableWidgetGroup group, CustomItemStackHandler items, int itemCount,
                                 FluidTank[] tanks, int fluidCount, boolean isOutput, SlotConfig onConfigure) {
        for (int i = 0; i < itemCount && i < items.getSlots(); i++) {
            int slot = i;
            group.addWidget(new ConfigurableItemSlot(items, i, (i % COLS) * 18, (i / COLS) * 18)
                    .setOnConfigure(() -> onConfigure.open(slot, isOutput, false)));
        }
        int fluidY = (itemCount == 0 ? 0 : (itemCount + COLS - 1) / COLS * 18 + 4);
        for (int i = 0; i < fluidCount && i < tanks.length; i++) {
            int slot = i;
            group.addWidget(new ConfigurableFluidSlot(tanks[i], (i % COLS) * 18, fluidY + (i / COLS) * 18)
                    .setOnConfigure(() -> onConfigure.open(slot, isOutput, true)));
        }
    }

    @FunctionalInterface
    private interface SlotConfig {

        void open(int slot, boolean isOutput, boolean isFluid);
    }

    private static String buildScript(GTRecipeType type, String[] recipeId, CustomItemStackHandler itemIn,
                                      CustomItemStackHandler itemOut, FluidTank[] fluidIn, FluidTank[] fluidOut,
                                      int[] tier, long[] amperage, int[] duration, int[] blastTemp, int[] cwu,
                                      String[] cleanroom, String[] dimension, int[] outChance, int[] outBoost,
                                      int[] inChance, int[] inBoost, boolean[] inNoConsume, int[] fOutChance,
                                      int[] fOutBoost, int[] fInChance, int[] fInBoost) {
        if (type == null) return "// select a recipe type first";
        RecipeDraft draft = new RecipeDraft();
        draft.recipeType = type;
        for (int i = 0; i < type.getMaxInputs(ItemRecipeCapability.CAP); i++) {
            ItemStack stack = itemIn.getStackInSlot(i);
            if (!stack.isEmpty()) {
                draft.itemInputs.add(stack.copy());
                draft.itemInputNotConsumed.add(inNoConsume[i]);
                draft.itemInputChances.add(inChance[i]);
                draft.itemInputBoosts.add(inBoost[i]);
            }
        }
        for (int i = 0; i < type.getMaxOutputs(ItemRecipeCapability.CAP); i++) {
            ItemStack stack = itemOut.getStackInSlot(i);
            if (!stack.isEmpty()) {
                draft.itemOutputs.add(stack.copy());
                draft.itemOutputChances.add(outChance[i]);
                draft.itemOutputBoosts.add(outBoost[i]);
            }
        }
        for (int i = 0; i < type.getMaxInputs(FluidRecipeCapability.CAP); i++) {
            FluidStack fluid = fluidIn[i].getFluid();
            if (!fluid.isEmpty()) {
                draft.fluidInputs.add(fluid.copy());
                draft.fluidInputChances.add(fInChance[i]);
                draft.fluidInputBoosts.add(fInBoost[i]);
            }
        }
        for (int i = 0; i < type.getMaxOutputs(FluidRecipeCapability.CAP); i++) {
            FluidStack fluid = fluidOut[i].getFluid();
            if (!fluid.isEmpty()) {
                draft.fluidOutputs.add(fluid.copy());
                draft.fluidOutputChances.add(fOutChance[i]);
                draft.fluidOutputBoosts.add(fOutBoost[i]);
            }
        }
        draft.voltageTier = tier[0];
        draft.amperage = amperage[0];
        draft.duration = duration[0];
        draft.blastTemp = blastTemp[0];
        draft.cwu = cwu[0];
        draft.cleanroom = cleanroom[0];
        draft.dimension = dimension[0];
        return KubeJsRecipeExporter.export(draft, recipeId[0]);
    }

    private static FluidTank[] tanks() {
        FluidTank[] result = new FluidTank[POOL_FLUID];
        for (int i = 0; i < POOL_FLUID; i++) result[i] = new FluidTank(TANK_CAPACITY);
        return result;
    }

    private static long parseLong(String text, long fallback) {
        try {
            return Long.parseLong(text.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static List<String> tierNames() {
        List<String> names = new ArrayList<>();
        for (int i = 0; i <= GTValues.MAX; i++) names.add(GTValues.VN[i]);
        return names;
    }

    private static int tierIndex(String name) {
        for (int i = 0; i <= GTValues.MAX; i++) {
            if (GTValues.VN[i].equals(name)) return i;
        }
        return GTValues.LV;
    }

    private static List<String> cleanroomNames() {
        List<String> names = new ArrayList<>();
        names.add("none");
        CleanroomType.getAllTypes().stream().map(CleanroomType::name).sorted().forEach(names::add);
        return names;
    }

    private static int cleanroomIndex(String name) {
        int index = cleanroomNames().indexOf(name);
        return index < 0 ? 0 : index;
    }

    private static List<ResourceLocation> allRecipeTypeIds() {
        List<ResourceLocation> ids = new ArrayList<>(BuiltInRegistries.RECIPE_TYPE.keySet());
        ids.sort(Comparator.comparing(ResourceLocation::getPath).thenComparing(ResourceLocation::getNamespace));
        return ids;
    }
}
