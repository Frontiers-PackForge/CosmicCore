package com.ghostipedia.cosmiccore.gtbridge.recipemaker;

import com.ghostipedia.cosmiccore.api.capability.recipe.CosmicRecipeCapabilities;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.RecipeCapability;
import com.gregtechceu.gtceu.api.mui.IItemUIHolder;
import com.gregtechceu.gtceu.api.recipe.GTRecipeType;
import com.gregtechceu.gtceu.api.registry.GTRegistries;
import com.gregtechceu.gtceu.api.transfer.item.CustomItemStackHandler;
import com.gregtechceu.gtceu.common.mui.GTGuiTextures;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import brachy.modularui.api.IPanelHandler;
import brachy.modularui.api.drawable.Text;
import brachy.modularui.factory.PlayerInventoryGuiData;
import brachy.modularui.factory.inventory.InventoryTypes;
import brachy.modularui.screen.ModularPanel;
import brachy.modularui.screen.UISettings;
import brachy.modularui.utils.Alignment;
import brachy.modularui.value.StringValue;
import brachy.modularui.value.sync.DynamicSyncHandler;
import brachy.modularui.value.sync.FluidSlotSyncHandler;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.value.sync.PhantomItemSlotSyncHandler;
import brachy.modularui.value.sync.StringSyncValue;
import brachy.modularui.widget.Widget;
import brachy.modularui.widgets.ButtonWidget;
import brachy.modularui.widgets.CycleButtonWidget;
import brachy.modularui.widgets.Dialog;
import brachy.modularui.widgets.ListWidget;
import brachy.modularui.widgets.SlotGroupWidget;
import brachy.modularui.widgets.TextWidget;
import brachy.modularui.widgets.dynamic.DynamicWidget;
import brachy.modularui.widgets.layout.Flow;
import brachy.modularui.widgets.slot.ModularSlot;
import brachy.modularui.widgets.textfield.TextFieldWidget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

/**
 * The Recipe Forge (DONK) UI as a GregTech item component. A searchable type list on the left swaps the editor on the
 * right IN PLACE (a DynamicSyncHandler rebuilds just that subtree, the panel never reopens). Every slot and field is a
 * native synced widget pulled from a shared handler pool via getOrCreateSyncHandler, so phantom item/fluid contents and
 * numeric fields sync both ways; the Copy button builds the KubeJS line server-side and drops it on the clipboard. The
 * crafting and generic-codec layouts live in their own classes; the GregTech layout is here.
 */
public class RecipeMakerBehavior implements IItemUIHolder {

    static final String CRAFTING = "minecraft:crafting";
    static final String FOOD = "cosmiccore:food";
    private static final int WIDTH = 380;
    private static final int HEIGHT = 300;
    static final int COLS = 3;
    static final int POOL_ITEM = 25;
    static final int POOL_FLUID = 12;
    static final int POOL_CODEC_VAL = 16;
    static final int FOOD_ROWS = 4;
    private static final int TAG_SLOTS = 16;
    private static final int TANK_CAPACITY = 64_000;

    private record ScalarCap(RecipeCapability<?> cap, String label) {}

    private static final List<ScalarCap> SCALAR_CAPS = List.of(
            new ScalarCap(CosmicRecipeCapabilities.EMBER, "Ember"));

    @Override
    public InteractionResultHolder<ItemStack> use(ItemStack item, Level level, Player player,
                                                  InteractionHand usedHand) {
        if (!player.hasPermissions(4)) {
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.translatable("cosmiccore.recipe_maker.access_denied")
                        .withStyle(ChatFormatting.RED));
            }
            return InteractionResultHolder.fail(item);
        }
        return IItemUIHolder.super.use(item, level, player, usedHand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.hasPermissions(4)) {
            if (player != null && !context.getLevel().isClientSide) {
                player.sendSystemMessage(Component.translatable("cosmiccore.recipe_maker.access_denied")
                        .withStyle(ChatFormatting.RED));
            }
            return InteractionResult.FAIL;
        }
        return IItemUIHolder.super.useOn(context);
    }

    /** All backing stores for one open session; shared across the editor families, created once per open. */
    public static final class State {

        final CustomItemStackHandler itemIn = new CustomItemStackHandler(POOL_ITEM);
        final CustomItemStackHandler itemOut = new CustomItemStackHandler(POOL_ITEM);
        final FluidTank[] fluidIn = tanks();
        final FluidTank[] fluidOut = tanks();
        final String[] recipeId = { "" };
        final int[] tier = { GTValues.LV };
        final long[] amperage = { 1 };
        final int[] duration = { 100 };
        final int[] blastTemp = { 0 };
        final int[] cwu = { 0 };
        final String[] cleanroom = { "none" };
        final String[] dimension = { "" };
        final int[] outChance = new int[POOL_ITEM];
        final int[] inChance = new int[POOL_ITEM];
        final String[] inTag = new String[POOL_ITEM];
        final int[] outBoost = new int[POOL_ITEM];
        final int[] inConsume = new int[POOL_ITEM];
        final int[] fluidInChance = new int[POOL_FLUID];
        final int[] fluidOutChance = new int[POOL_FLUID];
        final int[] selSlot = { 0 };
        final int[] selSide = { 0 };
        final int[] selType = { 0 };
        final int[] craftMode = { 0 };
        final int[] euMode = { 0 };
        final long[] rawVoltage = { 32 };
        final int[] voltArray = { 0 };
        final String[] codecVals = new String[POOL_CODEC_VAL];
        final String[] scalarIn = new String[SCALAR_CAPS.size()];
        final String[] scalarOut = new String[SCALAR_CAPS.size()];
        final String[] bloomwyrmBiopowerInput = { "" };
        final String[] bloomwyrmBiopowerOutput = { "" };
        final String[] bloomwyrmChargeInput = { "" };
        final String[] bloomwyrmChargeOutput = { "" };
        final String[] bloomwyrmMaxParallel = { "" };
        final FoodState food = new FoodState();

        State() {
            Arrays.fill(outChance, RecipeDraft.GUARANTEED);
            Arrays.fill(inChance, RecipeDraft.GUARANTEED);
            Arrays.fill(fluidInChance, RecipeDraft.GUARANTEED);
            Arrays.fill(fluidOutChance, RecipeDraft.GUARANTEED);
            Arrays.fill(inTag, "");
            Arrays.fill(codecVals, "");
            Arrays.fill(scalarIn, "");
            Arrays.fill(scalarOut, "");
        }
    }

    public static final class FoodState {

        final int[] category = { 0 };
        final int[] pickRow = { 0 };
        final String[] health = { "0" };
        final String[] regen = { "0" };
        final String[] duration = { "20m" };
        final String[] effectId = new String[FOOD_ROWS];
        final String[] effectAmp = new String[FOOD_ROWS];
        final String[] attrId = new String[FOOD_ROWS];
        final String[] attrAmount = new String[FOOD_ROWS];
        final int[] attrOp = new int[FOOD_ROWS];
        final String[] behGlyph = new String[FOOD_ROWS];
        final String[] behColor = new String[FOOD_ROWS];
        final String[] behLabel = new String[FOOD_ROWS];
        final String[] behValue = new String[FOOD_ROWS];

        FoodState() {
            Arrays.fill(effectId, "");
            Arrays.fill(effectAmp, "0");
            Arrays.fill(attrId, "");
            Arrays.fill(attrAmount, "0");
            Arrays.fill(behGlyph, "");
            Arrays.fill(behColor, "#ffffff");
            Arrays.fill(behLabel, "");
            Arrays.fill(behValue, "");
        }
    }

    @Override
    public ModularPanel<?> buildUI(PlayerInventoryGuiData<?> data, PanelSyncManager syncManager, UISettings settings) {
        Player player = data.getPlayer();
        State state = new State();

        RecipeMakerControl control = new RecipeMakerControl();
        syncManager.syncValue("control", control.allowC2S());

        if (data.getInventoryType() == InventoryTypes.PLAYER) {
            syncManager.bindPlayerInventory(player, (inv, index) -> index == data.getSlotIndex() ?
                    ModularSlot.playerSlot(inv, index, player).accessibility(false, false) :
                    ModularSlot.playerSlot(inv, index, player));
        }

        ModularPanel<?> panel = ModularPanel.defaultPanel("recipe_forge", WIDTH, HEIGHT)
                .background(GTGuiTextures.BACKGROUND);

        IntSyncValue selSlot = new IntSyncValue(() -> state.selSlot[0], v -> state.selSlot[0] = v);
        selSlot.allowC2S();
        syncManager.syncValue("sslot", selSlot);
        IntSyncValue selSide = new IntSyncValue(() -> state.selSide[0], v -> state.selSide[0] = v);
        selSide.allowC2S();
        syncManager.syncValue("sside", selSide);
        IntSyncValue selType = new IntSyncValue(() -> state.selType[0], v -> state.selType[0] = v);
        selType.allowC2S();
        syncManager.syncValue("stype", selType);

        IPanelHandler capPanel = syncManager.syncedPanel("rm_cap", true, (sm, sh) -> buildCapPanel(sm, state, panel));
        IPanelHandler condPanel = syncManager.syncedPanel("rm_cond", true,
                (sm, sh) -> buildCondPanel(sm, state, panel));
        IPanelHandler tagPanel = syncManager.syncedPanel("rm_tag", true,
                (sm, sh) -> buildTagPanel(sm, state, panel, sh));
        IPanelHandler slotPanel = syncManager.syncedPanel("rm_slot", true,
                (sm, sh) -> buildSlotPanel(sm, state, panel, tagPanel));
        IPanelHandler foodEffectPanel = syncManager.syncedPanel("rm_feff", true,
                (sm, sh) -> FoodEditor.buildEffectPicker(sm, state, panel, sh));
        IPanelHandler foodAttrPanel = syncManager.syncedPanel("rm_fattr", true,
                (sm, sh) -> FoodEditor.buildAttrPicker(sm, state, panel, sh));

        DynamicSyncHandler editorSync = new DynamicSyncHandler().widgetProvider((sm, buf) -> {
            String typeId = buf.readUtf();
            return buildEditor(sm, typeId, state, control, player, capPanel, condPanel, slotPanel, foodEffectPanel,
                    foodAttrPanel, selSlot, selSide, selType);
        });
        syncManager.syncValue("editor", editorSync);
        control.setEditorSelector(typeId -> {
            if (isValidTypeId(typeId)) {
                editorSync.notifyUpdate(buf -> buf.writeUtf(typeId));
            }
        });

        boolean[] inited = { false };
        syncManager.onServerTick(() -> {
            if (!inited[0]) {
                inited[0] = true;
                editorSync.notifyUpdate(buf -> buf.writeUtf(CRAFTING));
            }
        });

        StringValue search = new StringValue("");
        ListWidget list = new ListWidget<>();
        list.collapseDisabledChildren().expanded().widthRel(1f);
        addRow(list, "crafting (3x3)", CRAFTING, search, control);
        addRow(list, "food (cosmic)", FOOD, search, control);
        for (ResourceLocation id : allRecipeTypeIds()) {
            if (id.toString().equals(CRAFTING)) continue;
            addRow(list, id.getPath(), id.toString(), search, control);
        }

        DynamicWidget editorDyn = new DynamicWidget<>();
        editorDyn.syncHandler(editorSync);
        editorDyn.expanded().heightRel(1f);

        panel.child(new TextWidget<>(Text.str("Recipe Forge")).pos(8, 4));
        panel.child(Flow.row().pos(6, 16).size(WIDTH - 12, 196).childPadding(4)
                .child(Flow.column().width(150).heightRel(1f).childPadding(2)
                        .child(new TextFieldWidget().value(search).widthRel(1f).height(12).autoUpdateOnChange(true))
                        .child(list))
                .child(editorDyn));
        panel.child(SlotGroupWidget.playerInventory(7, true, (index, slot) -> slot.background(GTGuiTextures.SLOT)));
        return panel;
    }

    private static void addRow(ListWidget list, String label, String id, StringValue search,
                               RecipeMakerControl control) {
        list.child(new ButtonWidget<>()
                .background(GTGuiTextures.BUTTON)
                .size(144, 14)
                .onMousePressed((context, button) -> {
                    control.requestEditor(id);
                    return true;
                })
                .setEnabledIf(w -> matches(id, search.getStringValue()))
                .child(new TextWidget<>(Text.str(label)).textAlign(Alignment.CenterLeft).sizeRel(1f)
                        .padding(3, 0, 0, 0)));
    }

    private static boolean isValidTypeId(String typeId) {
        if (CRAFTING.equals(typeId) || FOOD.equals(typeId)) return true;
        ResourceLocation id = ResourceLocation.tryParse(typeId);
        return id != null && BuiltInRegistries.RECIPE_TYPE.containsKey(id);
    }

    static boolean matches(String id, String needle) {
        if (needle == null || needle.isEmpty()) return true;
        return id.toLowerCase(Locale.ROOT).contains(needle.toLowerCase(Locale.ROOT));
    }

    private static Widget<?> buildEditor(PanelSyncManager sm, String typeId, State state, RecipeMakerControl control,
                                         Player player, IPanelHandler capPanel, IPanelHandler condPanel,
                                         IPanelHandler slotPanel, IPanelHandler foodEffectPanel,
                                         IPanelHandler foodAttrPanel, IntSyncValue selSlot, IntSyncValue selSide,
                                         IntSyncValue selType) {
        ListWidget content = new ListWidget<>();
        content.sizeRel(1f);
        content.child(fieldRow("id", new TextFieldWidget()
                .value(strSync(sm, "rid", () -> state.recipeId[0], v -> state.recipeId[0] = v)).expanded().height(12)));

        if (CRAFTING.equals(typeId)) {
            CraftingRecipeEditor.build(sm, content, state, control, selSlot, selSide, selType, slotPanel);
        } else if (FOOD.equals(typeId)) {
            FoodEditor.build(sm, content, state, control, foodEffectPanel, foodAttrPanel);
        } else {
            ResourceLocation rl = ResourceLocation.tryParse(typeId);
            if (rl != null && BuiltInRegistries.RECIPE_TYPE.get(rl) instanceof GTRecipeType type) {
                buildGtEditor(sm, content, type, state, control, capPanel, condPanel, slotPanel, selSlot, selSide,
                        selType, player);
            } else {
                CodecRecipeEditor.build(sm, content, player, typeId, state, control, selSlot, selSide, selType,
                        slotPanel);
            }
        }
        return content;
    }

    private static void buildGtEditor(PanelSyncManager sm, ListWidget content, GTRecipeType type, State state,
                                      RecipeMakerControl control, IPanelHandler capPanel, IPanelHandler condPanel,
                                      IPanelHandler slotPanel, IntSyncValue selSlot, IntSyncValue selSide,
                                      IntSyncValue selType, Player player) {
        int maxItemIn = Math.min(type.getMaxInputs(ItemRecipeCapability.CAP), POOL_ITEM);
        int maxItemOut = Math.min(type.getMaxOutputs(ItemRecipeCapability.CAP), POOL_ITEM);
        int maxFluidIn = Math.min(type.getMaxInputs(FluidRecipeCapability.CAP), POOL_FLUID);
        int maxFluidOut = Math.min(type.getMaxOutputs(FluidRecipeCapability.CAP), POOL_FLUID);

        Flow inSide = Flow.column().coverChildren().child(new TextWidget<>(Text.str("In")).height(9));
        if (maxItemIn > 0) {
            inSide.child(itemGrid(sm, "ii", state.itemIn, maxItemIn, selSlot, selSide, selType, 0, slotPanel));
        }
        if (maxFluidIn > 0) {
            inSide.child(fluidGrid(sm, "fi", state.fluidIn, maxFluidIn, selSlot, selSide, selType, 0, slotPanel));
        }
        Flow outSide = Flow.column().coverChildren().child(new TextWidget<>(Text.str("Out")).height(9));
        if (maxItemOut > 0) {
            outSide.child(itemGrid(sm, "io", state.itemOut, maxItemOut, selSlot, selSide, selType, 1, slotPanel));
        }
        if (maxFluidOut > 0) {
            outSide.child(fluidGrid(sm, "fo", state.fluidOut, maxFluidOut, selSlot, selSide, selType, 1, slotPanel));
        }
        content.child(Flow.row().coverChildrenHeight().widthRel(1f).childPadding(6).child(inSide).child(outSide));

        content.child(fieldRow("Tick", new TextFieldWidget().setNumbers(1, Integer.MAX_VALUE)
                .value(intSync(sm, "dur", () -> state.duration[0], v -> state.duration[0] = v)).expanded().height(12)));
        content.child(fieldRow("EU", new CycleButtonWidget().background(GTGuiTextures.BUTTON).stateCount(2)
                .stateOverlay(0, Text.str("tier").alignment(Alignment.TopLeft).asTextIcon())
                .stateOverlay(1, Text.str("raw").alignment(Alignment.TopLeft).asTextIcon())
                .value(intSync(sm, "eumode", () -> state.euMode[0], v -> state.euMode[0] = v))
                .expanded().height(14)));
        Flow tierRow = Flow.row().coverChildrenHeight().widthRel(1f).childPadding(2)
                .child(new TextWidget<>(Text.str("Tier")).width(34))
                .child(new TextFieldWidget().setNumbers(0, GTValues.MAX)
                        .value(intSync(sm, "tier", () -> state.tier[0], v -> state.tier[0] = v)).width(26).height(12))
                .child(new TextWidget<>(Text.dynamic(() -> Component.literal(GTValues.VN[state.tier[0]]))).expanded());
        tierRow.setEnabledIf(w -> state.euMode[0] == 0);
        content.child(tierRow);
        Flow voltRow = fieldRow("Volt", new CycleButtonWidget().background(GTGuiTextures.BUTTON).stateCount(4)
                .stateOverlay(0, Text.str("VA").alignment(Alignment.TopLeft).asTextIcon())
                .stateOverlay(1, Text.str("V").alignment(Alignment.TopLeft).asTextIcon())
                .stateOverlay(2, Text.str("VH").alignment(Alignment.TopLeft).asTextIcon())
                .stateOverlay(3, Text.str("VHA").alignment(Alignment.TopLeft).asTextIcon())
                .value(intSync(sm, "varr", () -> state.voltArray[0], v -> state.voltArray[0] = v))
                .expanded().height(14));
        voltRow.setEnabledIf(w -> state.euMode[0] == 0);
        content.child(voltRow);
        Flow rawRow = fieldRow("EU/t", new TextFieldWidget().setNumbersLong(() -> 1L, () -> Long.MAX_VALUE)
                .value(longSync(sm, "raweu", () -> state.rawVoltage[0], v -> state.rawVoltage[0] = v)).expanded()
                .height(12));
        rawRow.setEnabledIf(w -> state.euMode[0] == 1);
        content.child(rawRow);
        content.child(fieldRow("Amp", new TextFieldWidget().setNumbersLong(() -> 1L, () -> Long.MAX_VALUE)
                .value(longSync(sm, "amp", () -> state.amperage[0], v -> state.amperage[0] = v)).expanded()
                .height(12)));

        content.child(labelButton("Capabilities", capPanel::openPanel));
        content.child(labelButton("Conditions", condPanel::openPanel));
        if (isBloomwyrmType(type)) {
            content.child(new TextWidget<>(Text.str("Bloomwyrm")).height(9));
            content.child(fieldRow("Biopower use", 86,
                    strField(sm, "wyrm_bio_in", () -> state.bloomwyrmBiopowerInput[0],
                            v -> state.bloomwyrmBiopowerInput[0] = v)));
            if (isBloomwyrmProducer(type)) {
                content.child(fieldRow("Biopower yield", 86,
                        strField(sm, "wyrm_bio_out", () -> state.bloomwyrmBiopowerOutput[0],
                                v -> state.bloomwyrmBiopowerOutput[0] = v)));
            }
            content.child(fieldRow("Bloomwyrm in", 86,
                    strField(sm, "wyrm_in", () -> state.bloomwyrmChargeInput[0],
                            v -> state.bloomwyrmChargeInput[0] = v)));
            if (isBloomwyrmProducer(type)) {
                content.child(fieldRow("Bloomwyrm out", 86,
                        strField(sm, "wyrm_out", () -> state.bloomwyrmChargeOutput[0],
                                v -> state.bloomwyrmChargeOutput[0] = v)));
            }
            if (supportsBloomwyrmParallel(type)) {
                content.child(fieldRow("Max parallel", 86,
                        strField(sm, "wyrm_parallel", () -> state.bloomwyrmMaxParallel[0],
                                v -> state.bloomwyrmMaxParallel[0] = v)));
            }
        }

        control.setExporter(() -> buildGtScript(type, state, player));
        content.child(copyButton(control));
    }

    private static ModularPanel<?> buildCapPanel(PanelSyncManager sm, State state, ModularPanel<?> parent) {
        Flow body = popoutBody("Capabilities")
                .child(fieldRow("CWU", intField(sm, "cwu", () -> state.cwu[0], v -> state.cwu[0] = v)));
        for (int k = 0; k < SCALAR_CAPS.size(); k++) {
            int ki = k;
            ScalarCap sc = SCALAR_CAPS.get(k);
            body.child(fieldRow(sc.label() + " in", new TextFieldWidget()
                    .value(strSync(sm, "scin" + ki, () -> state.scalarIn[ki], v -> state.scalarIn[ki] = v))
                    .expanded().height(12)));
            body.child(fieldRow(sc.label() + " out", new TextFieldWidget()
                    .value(strSync(sm, "scout" + ki, () -> state.scalarOut[ki], v -> state.scalarOut[ki] = v))
                    .expanded().height(12)));
        }
        return popout("rm_cap_dialog", parent, 160, 52 + SCALAR_CAPS.size() * 32, body);
    }

    private static ModularPanel<?> buildCondPanel(PanelSyncManager sm, State state, ModularPanel<?> parent) {
        Flow body = popoutBody("Conditions")
                .child(fieldRow("Heat", intField(sm, "heat", () -> state.blastTemp[0], v -> state.blastTemp[0] = v)))
                .child(fieldRow("Clean", strField(sm, "clean", () -> state.cleanroom[0], v -> state.cleanroom[0] = v)))
                .child(fieldRow("Dim",
                        strField(sm, "dim", () -> state.dimension[0], v -> state.dimension[0] = v.trim())));
        return popout("rm_cond_dialog", parent, 172, 86, body);
    }

    private static ModularPanel<?> buildSlotPanel(PanelSyncManager sm, State state, ModularPanel<?> parent,
                                                  IPanelHandler tagPanel) {
        Flow sizeRow = Flow.row().coverChildrenHeight().widthRel(1f).childPadding(2)
                .child(new TextWidget<>(
                        Text.dynamic(() -> Component.literal(state.selType[0] == 1 ? "Amount" : "Count"))).width(40))
                .child(new TextFieldWidget().setNumbers(1, Integer.MAX_VALUE)
                        .value(intSync(sm, "psize", () -> slotSize(state), v -> setSlotSize(state, v)))
                        .expanded().height(12));
        Flow useRow = fieldRow("Use", new CycleButtonWidget().background(GTGuiTextures.BUTTON).stateCount(2)
                .stateOverlay(0, Text.str("consumed").alignment(Alignment.TopLeft).asTextIcon())
                .stateOverlay(1, Text.str("not consumed").alignment(Alignment.TopLeft).asTextIcon())
                .value(intSync(sm, "pcon",
                        () -> state.selType[0] == 0 && state.selSide[0] == 0 &&
                                state.inConsume[idx(state.selSlot[0], POOL_ITEM)] == 1 ? 1 : 0,
                        v -> {
                            if (state.selType[0] == 0 && state.selSide[0] == 0) {
                                state.inConsume[idx(state.selSlot[0], POOL_ITEM)] = v;
                            }
                        }))
                .expanded().height(14));
        useRow.setEnabledIf(w -> state.selType[0] == 0 && state.selSide[0] == 0);
        Flow tagRow = fieldRow("Tag", new ButtonWidget<>().expanded().height(14)
                .onMousePressed((context, button) -> {
                    tagPanel.openPanel();
                    return true;
                })
                .child(new TextWidget<>(Text.dynamic(() -> {
                    String tag = state.inTag[idx(state.selSlot[0], POOL_ITEM)];
                    return Component.literal(tag == null || tag.isEmpty() ? "convert..." : "#" + tag);
                })).textAlign(Alignment.Center).sizeRel(1f)));
        tagRow.setEnabledIf(w -> state.selType[0] == 0 && state.selSide[0] == 0);
        Flow body = popoutBody("Slot options")
                .child(new TextWidget<>(Text.dynamic(() -> Component.literal(slotLabel(state)))).height(9))
                .child(sizeRow)
                .child(fieldRow("Chance", new TextFieldWidget().setNumbers(1, RecipeDraft.GUARANTEED)
                        .value(intSync(sm, "pch", () -> slotChance(state), v -> setSlotChance(state, v)))
                        .expanded().height(12)))
                .child(useRow)
                .child(tagRow);
        return popout("rm_slot_dialog", parent, 172, 116, body);
    }

    private static ModularPanel<?> buildTagPanel(PanelSyncManager sm, State state, ModularPanel<?> parent,
                                                 IPanelHandler self) {
        StringSyncValue tagValue = strSync(sm, "itag", () -> state.inTag[idx(state.selSlot[0], POOL_ITEM)],
                v -> state.inTag[idx(state.selSlot[0], POOL_ITEM)] = v);
        ListWidget body = new ListWidget<>();
        body.collapseDisabledChildren().sizeRel(1f);
        body.child(new TextWidget<>(Text.str("Convert to tag")).height(10));
        body.child(labelButton("use item", () -> {
            tagValue.setStringValue("", true, true);
            self.closePanel();
        }));
        for (int k = 0; k < TAG_SLOTS; k++) {
            int slot = k;
            ButtonWidget<?> tagButton = new ButtonWidget<>().widthRel(1f).height(14)
                    .onMousePressed((context, button) -> {
                        List<String> tags = currentItemTags(state);
                        if (slot < tags.size()) {
                            tagValue.setStringValue(tags.get(slot), true, true);
                            self.closePanel();
                        }
                        return true;
                    })
                    .child(new TextWidget<>(Text.dynamic(() -> {
                        List<String> tags = currentItemTags(state);
                        return Component.literal(slot < tags.size() ? "#" + tags.get(slot) : "");
                    })).textAlign(Alignment.Center).sizeRel(1f));
            tagButton.setEnabledIf(w -> slot < currentItemTags(state).size());
            body.child(tagButton);
        }
        return popout("rm_tag_dialog", parent, 210, 150, body);
    }

    private static List<String> currentItemTags(State state) {
        if (state.selType[0] != 0) return List.of();
        CustomItemStackHandler handler = state.selSide[0] == 1 ? state.itemOut : state.itemIn;
        ItemStack stack = handler.getStackInSlot(idx(state.selSlot[0], POOL_ITEM));
        if (stack.isEmpty()) return List.of();
        return stack.getTags().map(tag -> tag.location().toString()).sorted().toList();
    }

    private static String slotLabel(State state) {
        return (state.selSide[0] == 1 ? "out " : "in ") + (state.selType[0] == 1 ? "fluid " : "item ") +
                state.selSlot[0];
    }

    private static int slotSize(State state) {
        if (state.selType[0] == 1) {
            FluidTank tank = (state.selSide[0] == 1 ? state.fluidOut : state.fluidIn)[idx(state.selSlot[0],
                    POOL_FLUID)];
            FluidStack fluid = tank.getFluid();
            return fluid.isEmpty() ? 1 : fluid.getAmount();
        }
        CustomItemStackHandler handler = state.selSide[0] == 1 ? state.itemOut : state.itemIn;
        ItemStack stack = handler.getStackInSlot(idx(state.selSlot[0], POOL_ITEM));
        return stack.isEmpty() ? 1 : stack.getCount();
    }

    private static void setSlotSize(State state, int size) {
        int amount = Math.max(1, size);
        if (state.selType[0] == 1) {
            FluidTank tank = (state.selSide[0] == 1 ? state.fluidOut : state.fluidIn)[idx(state.selSlot[0],
                    POOL_FLUID)];
            FluidStack fluid = tank.getFluid();
            if (!fluid.isEmpty()) {
                FluidStack copy = fluid.copy();
                copy.setAmount(amount);
                tank.setFluid(copy);
            }
            return;
        }
        CustomItemStackHandler handler = state.selSide[0] == 1 ? state.itemOut : state.itemIn;
        int i = idx(state.selSlot[0], POOL_ITEM);
        ItemStack stack = handler.getStackInSlot(i);
        if (!stack.isEmpty()) {
            ItemStack copy = stack.copy();
            copy.setCount(amount);
            handler.setStackInSlot(i, copy);
        }
    }

    private static int slotChance(State state) {
        if (state.selType[0] == 1) {
            int i = idx(state.selSlot[0], POOL_FLUID);
            return state.selSide[0] == 1 ? state.fluidOutChance[i] : state.fluidInChance[i];
        }
        int i = idx(state.selSlot[0], POOL_ITEM);
        return state.selSide[0] == 1 ? state.outChance[i] : state.inChance[i];
    }

    private static void setSlotChance(State state, int chance) {
        if (state.selType[0] == 1) {
            int i = idx(state.selSlot[0], POOL_FLUID);
            if (state.selSide[0] == 1) {
                state.fluidOutChance[i] = chance;
            } else {
                state.fluidInChance[i] = chance;
            }
            return;
        }
        int i = idx(state.selSlot[0], POOL_ITEM);
        if (state.selSide[0] == 1) {
            state.outChance[i] = chance;
        } else {
            state.inChance[i] = chance;
        }
    }

    private static Flow popoutBody(String title) {
        return Flow.column().sizeRel(1f).padding(4).childPadding(2)
                .child(new TextWidget<>(Text.str(title)).height(10));
    }

    static ModularPanel<?> popout(String name, ModularPanel<?> parent, int width, int height, Widget<?> body) {
        return new Dialog<>(name)
                .disablePanelsBelow(false).draggable(true).closeOnOutOfBoundsClick(true)
                .size(width, height).relative(parent).left(158).top(22)
                .background(GTGuiTextures.BACKGROUND)
                .child(body);
    }

    private static TextFieldWidget intField(PanelSyncManager sm, String key, IntSupplier getter, IntConsumer setter) {
        return new TextFieldWidget().setNumbers(0, Integer.MAX_VALUE).value(intSync(sm, key, getter, setter))
                .expanded().height(12);
    }

    static TextFieldWidget strField(PanelSyncManager sm, String key, Supplier<String> getter,
                                    Consumer<String> setter) {
        return new TextFieldWidget().value(strSync(sm, key, getter, setter)).expanded().height(12);
    }

    private static RecipeExportResult buildGtScript(GTRecipeType type, State state, Player player) {
        int maxItemIn = Math.min(type.getMaxInputs(ItemRecipeCapability.CAP), POOL_ITEM);
        int maxItemOut = Math.min(type.getMaxOutputs(ItemRecipeCapability.CAP), POOL_ITEM);
        int maxFluidIn = Math.min(type.getMaxInputs(FluidRecipeCapability.CAP), POOL_FLUID);
        int maxFluidOut = Math.min(type.getMaxOutputs(FluidRecipeCapability.CAP), POOL_FLUID);
        RecipeDraft draft = new RecipeDraft();
        draft.recipeType = type;
        for (int i = 0; i < maxItemIn; i++) {
            ItemStack stack = state.itemIn.getStackInSlot(i);
            if (!stack.isEmpty()) {
                draft.itemInputs.add(stack.copy());
                draft.itemInputTags.add(state.inTag[i]);
                draft.itemInputNotConsumed.add(state.inConsume[i] == 1);
                draft.itemInputChances.add(state.inChance[i]);
            }
        }
        for (int i = 0; i < maxItemOut; i++) {
            ItemStack stack = state.itemOut.getStackInSlot(i);
            if (!stack.isEmpty()) {
                draft.itemOutputs.add(stack.copy());
                draft.itemOutputChances.add(state.outChance[i]);
                draft.itemOutputBoosts.add(state.outBoost[i]);
            }
        }
        for (int i = 0; i < maxFluidIn; i++) {
            FluidStack fluid = state.fluidIn[i].getFluid();
            if (!fluid.isEmpty()) {
                draft.fluidInputs.add(fluid.copy());
                draft.fluidInputChances.add(state.fluidInChance[i]);
            }
        }
        for (int i = 0; i < maxFluidOut; i++) {
            FluidStack fluid = state.fluidOut[i].getFluid();
            if (!fluid.isEmpty()) {
                draft.fluidOutputs.add(fluid.copy());
                draft.fluidOutputChances.add(state.fluidOutChance[i]);
            }
        }
        draft.voltageTier = state.tier[0];
        draft.amperage = state.amperage[0];
        draft.rawEU = state.euMode[0] == 1;
        draft.rawVoltage = state.rawVoltage[0];
        draft.voltageArray = voltArrayName(state.voltArray[0]);
        draft.duration = state.duration[0];
        draft.blastTemp = state.blastTemp[0];
        draft.cwu = state.cwu[0];
        draft.cleanroom = state.cleanroom[0];
        draft.dimension = state.dimension[0];
        for (int k = 0; k < SCALAR_CAPS.size(); k++) {
            ScalarCap sc = SCALAR_CAPS.get(k);
            String id = GTRegistries.RECIPE_CAPABILITIES.getKey(sc.cap()).toString();
            if (!state.scalarIn[k].isBlank()) {
                draft.extraLines.add(".input('" + id + "', " + state.scalarIn[k].trim() + ")");
            }
            if (!state.scalarOut[k].isBlank()) {
                draft.extraLines.add(".output('" + id + "', " + state.scalarOut[k].trim() + ")");
            }
        }
        if (isBloomwyrmType(type)) {
            addBloomwyrmData(draft, "biopowerInput", state.bloomwyrmBiopowerInput[0]);
            if (isBloomwyrmProducer(type)) {
                addBloomwyrmData(draft, "biopowerOutput", state.bloomwyrmBiopowerOutput[0]);
            }
            addBloomwyrmData(draft, "bloomwyrmChargeInput", state.bloomwyrmChargeInput[0]);
            if (isBloomwyrmProducer(type)) {
                addBloomwyrmData(draft, "bloomwyrmChargeOutput", state.bloomwyrmChargeOutput[0]);
            }
            if (supportsBloomwyrmParallel(type)) {
                addBloomwyrmData(draft, "maxCampusParallel", state.bloomwyrmMaxParallel[0]);
            }
        }
        return KubeJsRecipeExporter.export(player, draft, state.recipeId[0]);
    }

    private static boolean isBloomwyrmType(GTRecipeType type) {
        return type == CosmicRecipeTypes.ABYSSAL_CULTURE_VAT ||
                type == CosmicRecipeTypes.SCULK_BIOCHAMBER ||
                type == CosmicRecipeTypes.BIOMANA_DIGESTOR ||
                type == CosmicRecipeTypes.MANAWOMB_LEECHING_POND;
    }

    private static boolean isBloomwyrmProducer(GTRecipeType type) {
        return type == CosmicRecipeTypes.ABYSSAL_CULTURE_VAT;
    }

    private static boolean supportsBloomwyrmParallel(GTRecipeType type) {
        return type == CosmicRecipeTypes.SCULK_BIOCHAMBER || type == CosmicRecipeTypes.BIOMANA_DIGESTOR;
    }

    private static void addBloomwyrmData(RecipeDraft draft, String method, String value) {
        if (value != null && !value.isBlank()) {
            draft.extraLines.add("." + method + "(" + value.trim() + ")");
        }
    }

    private static Flow itemGrid(PanelSyncManager sm, String key, CustomItemStackHandler handler, int count,
                                 IntSyncValue selSlot, IntSyncValue selSide, IntSyncValue selType, int side,
                                 IPanelHandler slotPanel) {
        Flow column = Flow.column().coverChildren();
        Flow row = null;
        for (int i = 0; i < count; i++) {
            if (i % COLS == 0) {
                row = Flow.row().coverChildren();
                column.child(row);
            }
            int slot = i;
            row.child(new ConfigurableItemSlot(itemSync(sm, key, i, handler), () -> {
                selSlot.setIntValue(slot, true, true);
                selSide.setIntValue(side, true, true);
                selType.setIntValue(0, true, true);
                slotPanel.openPanel();
            }).size(18));
        }
        return column;
    }

    private static Flow fluidGrid(PanelSyncManager sm, String key, FluidTank[] tanks, int count,
                                  IntSyncValue selSlot, IntSyncValue selSide, IntSyncValue selType, int side,
                                  IPanelHandler slotPanel) {
        Flow column = Flow.column().coverChildren();
        Flow row = null;
        for (int i = 0; i < count; i++) {
            if (i % COLS == 0) {
                row = Flow.row().coverChildren();
                column.child(row);
            }
            int slot = i;
            row.child(new ConfigurableFluidSlot(fluidSync(sm, key, i, tanks[i]), () -> {
                selSlot.setIntValue(slot, true, true);
                selSide.setIntValue(side, true, true);
                selType.setIntValue(1, true, true);
                slotPanel.openPanel();
            }).size(18));
        }
        return column;
    }

    // shared widget + sync helpers, used by all editor families

    static PhantomItemSlotSyncHandler itemSync(PanelSyncManager sm, String key, int i, CustomItemStackHandler handler) {
        return sm.getOrCreateSyncHandler(key, i, PhantomItemSlotSyncHandler.class, () -> {
            RecipeSlotSyncHandler sh = new RecipeSlotSyncHandler(new ModularSlot(handler, i));
            sh.allowC2S();
            return sh;
        });
    }

    static FluidSlotSyncHandler fluidSync(PanelSyncManager sm, String key, int i, FluidTank tank) {
        return sm.getOrCreateSyncHandler(key, i, FluidSlotSyncHandler.class, () -> {
            FluidSlotSyncHandler sh = new FluidSlotSyncHandler(tank);
            sh.phantom(true);
            sh.controlsAmount(true);
            sh.allowC2S();
            return sh;
        });
    }

    static IntSyncValue intSync(PanelSyncManager sm, String key, IntSupplier getter, IntConsumer setter) {
        return sm.getOrCreateSyncHandler(key, IntSyncValue.class, () -> {
            IntSyncValue value = new IntSyncValue(getter, setter);
            value.allowC2S();
            return value;
        });
    }

    static LongSyncValue longSync(PanelSyncManager sm, String key, LongSupplier getter, LongConsumer setter) {
        return sm.getOrCreateSyncHandler(key, LongSyncValue.class, () -> {
            LongSyncValue value = new LongSyncValue(getter, setter);
            value.allowC2S();
            return value;
        });
    }

    static StringSyncValue strSync(PanelSyncManager sm, String key, Supplier<String> getter, Consumer<String> setter) {
        return sm.getOrCreateSyncHandler(key, StringSyncValue.class, () -> {
            StringSyncValue value = new StringSyncValue(getter, setter);
            value.allowC2S();
            return value;
        });
    }

    static Flow fieldRow(String label, Widget<?> widget) {
        return fieldRow(label, 40, widget);
    }

    private static Flow fieldRow(String label, int labelWidth, Widget<?> widget) {
        return Flow.row().coverChildrenHeight().widthRel(1f).childPadding(2)
                .child(new TextWidget<>(Text.str(label)).width(labelWidth))
                .child(widget);
    }

    static ButtonWidget<?> copyButton(RecipeMakerControl control) {
        return labelButton("Copy KubeJS", () -> control.requestExport());
    }

    static ButtonWidget<?> labelButton(String text, Runnable action) {
        return new ButtonWidget<>()
                .background(GTGuiTextures.BUTTON)
                .widthRel(1f).height(16)
                .onMousePressed((context, button) -> {
                    action.run();
                    return true;
                })
                .child(new TextWidget<>(Text.str(text)).textAlign(Alignment.Center).sizeRel(1f));
    }

    static int idx(int sel, int max) {
        return max <= 0 ? 0 : Math.min(Math.max(sel, 0), max - 1);
    }

    private static String voltArrayName(int index) {
        return switch (index) {
            case 1 -> "V";
            case 2 -> "VH";
            case 3 -> "VHA";
            default -> "VA";
        };
    }

    private static FluidTank[] tanks() {
        FluidTank[] result = new FluidTank[POOL_FLUID];
        for (int i = 0; i < POOL_FLUID; i++) result[i] = new FluidTank(TANK_CAPACITY);
        return result;
    }

    private static List<ResourceLocation> allRecipeTypeIds() {
        List<ResourceLocation> ids = new ArrayList<>(BuiltInRegistries.RECIPE_TYPE.keySet());
        ids.sort(Comparator.comparing(ResourceLocation::getPath).thenComparing(ResourceLocation::getNamespace));
        return ids;
    }
}
