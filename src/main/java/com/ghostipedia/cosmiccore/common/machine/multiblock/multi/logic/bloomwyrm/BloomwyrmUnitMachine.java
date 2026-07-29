package com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.bloomwyrm;

import com.ghostipedia.cosmiccore.api.capability.ILinkedMultiblock;
import com.ghostipedia.cosmiccore.api.machine.multiblock.LinkedWorkableElectricMultiblockMachine;

import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.capability.recipe.EURecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.ContentModifier;
import com.gregtechceu.gtceu.api.recipe.modifier.ModifierFunction;
import com.gregtechceu.gtceu.api.sync_system.annotations.SaveField;
import com.gregtechceu.gtceu.common.mui.GTMultiblockTextUtil;
import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import brachy.modularui.api.drawable.Text;
import brachy.modularui.api.widget.IWidget;
import brachy.modularui.value.sync.BooleanSyncValue;
import brachy.modularui.value.sync.IntSyncValue;
import brachy.modularui.value.sync.LongSyncValue;
import brachy.modularui.value.sync.PanelSyncManager;
import brachy.modularui.widget.Widget;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class BloomwyrmUnitMachine extends LinkedWorkableElectricMultiblockMachine {

    public static final int MAX_DESIRED_PARALLEL = 16;

    @SaveField
    private int desiredParallel = 1;
    @SaveField
    private int eligibleParallel;
    private int requestedParallel;
    @SaveField
    private int offeredParallel;
    @SaveField
    private int allocatedParallel;
    @SaveField
    private long allocatedEUt;
    @SaveField
    private int allocatedBiopower;
    @SaveField
    private int allocatedBiopowerOutput;
    @SaveField
    private long allocatedChargeInput;
    @SaveField
    private long allocatedChargeOutput;
    @SaveField
    private long deliveredChargeOutput;
    @SaveField
    private String plannedRecipeId = "";
    @SaveField
    private int allocationConstraint = BloomwyrmAllocationConstraint.NONE.ordinal();

    protected BloomwyrmUnitMachine(BlockEntityCreationInfo info) {
        super(info, new BloomwyrmRecipeLogic());
    }

    @NotNull
    @Override
    public BloomwyrmRecipeLogic getRecipeLogic() {
        return (BloomwyrmRecipeLogic) super.getRecipeLogic();
    }

    @Override
    public LinkRole getLinkRole() {
        return LinkRole.REMOTE;
    }

    @Override
    public int getMaxPartners() {
        return 1;
    }

    @Override
    public boolean canLinkTo(GlobalPos partner, ILinkedMultiblock partnerMachine) {
        GlobalPos own = getGlobalPos();
        return own != null &&
                partnerMachine instanceof BloomwyrmHeartMachine &&
                own.dimension().equals(partner.dimension()) &&
                own.pos().distSqr(partner.pos()) <= BloomwyrmHeartMachine.MAX_LINK_DISTANCE_SQUARED;
    }

    public int getDesiredParallel() {
        return supportsParallelControl() ? desiredParallel : 1;
    }

    public void setDesiredParallel(int parallel) {
        if (!supportsParallelControl()) return;
        desiredParallel = Mth.clamp(parallel, 1, MAX_DESIRED_PARALLEL);
    }

    public int getEligibleParallel() {
        return eligibleParallel;
    }

    public int getRequestedParallel() {
        return requestedParallel;
    }

    public int getOfferedParallel() {
        return offeredParallel;
    }

    public boolean supportsParallelControl() {
        return true;
    }

    public int getAllocatedParallel() {
        return allocatedParallel;
    }

    public long getAllocatedEUt() {
        return allocatedEUt;
    }

    public int getAllocatedBiopower() {
        return allocatedBiopower;
    }

    public int getAllocatedBiopowerOutput() {
        return allocatedBiopowerOutput;
    }

    public long getAllocatedChargeInput() {
        return allocatedChargeInput;
    }

    public long getAllocatedChargeOutput() {
        return Math.max(0, allocatedChargeOutput - deliveredChargeOutput);
    }

    public BloomwyrmAllocationConstraint getAllocationConstraint() {
        return BloomwyrmAllocationConstraint.values()[Math.floorMod(
                allocationConstraint,
                BloomwyrmAllocationConstraint.values().length)];
    }

    public boolean hasAllocation() {
        return allocatedParallel > 0;
    }

    public boolean isAvailableForAllocation() {
        return isFormed() && isWorkingEnabled() && !hasAllocation() && !getRecipeLogic().isActive();
    }

    public void recordParallelEligibility(int parallel) {
        eligibleParallel = Math.max(0, parallel);
        if (eligibleParallel == 0) {
            offeredParallel = 0;
            requestedParallel = 0;
        }
    }

    public void recordParallelRequest(int parallel) {
        requestedParallel = Math.max(0, parallel);
    }

    public void recordHeartOffer(int parallel, BloomwyrmAllocationConstraint constraint) {
        offeredParallel = Math.max(0, parallel);
        allocationConstraint = constraint.ordinal();
    }

    public BloomwyrmWorkRequest createWorkRequest(
                                                  GTRecipe recipe,
                                                  int requestedParallel,
                                                  int eligibleParallel) {
        long eut = Math.max(0L, recipe.getInputEUt().getTotalEU());
        int biopowerInput = Math.max(0, recipe.data.getInt(BloomwyrmRecipeKeys.BIOPOWER_INPUT));
        int biopowerOutput = Math.max(0, recipe.data.getInt(BloomwyrmRecipeKeys.BIOPOWER_OUTPUT));
        long chargeInput = Math.max(0L, recipe.data.getLong(BloomwyrmRecipeKeys.CHARGE_INPUT));
        long chargeOutput = Math.max(0L, recipe.data.getLong(BloomwyrmRecipeKeys.CHARGE_OUTPUT));
        return new BloomwyrmWorkRequest(
                recipe,
                requestedParallel,
                eligibleParallel,
                eut,
                biopowerInput,
                biopowerOutput,
                chargeInput,
                chargeOutput);
    }

    public boolean beginAllocation(BloomwyrmWorkRequest request, int parallel) {
        if (parallel <= 0 || !isAvailableForAllocation()) {
            return false;
        }
        allocatedParallel = parallel;
        allocatedEUt = saturatingMultiply(request.eutPerParallel(), parallel);
        allocatedBiopower = saturatingMultiplyInt(request.biopowerInputPerParallel(), parallel);
        allocatedBiopowerOutput = saturatingMultiplyInt(request.biopowerOutputPerParallel(), parallel);
        allocatedChargeInput = saturatingMultiply(request.chargeInputPerParallel(), parallel);
        allocatedChargeOutput = saturatingMultiply(request.chargeOutputPerParallel(), parallel);
        deliveredChargeOutput = 0;
        plannedRecipeId = request.recipe().id.toString();
        allocationConstraint = BloomwyrmAllocationConstraint.NONE.ordinal();
        getRecipeLogic().markLastRecipeDirty();
        if (getRecipeLogic().startPlannedRecipe(request.recipe())) {
            return true;
        }
        clearAllocation();
        allocationConstraint = BloomwyrmAllocationConstraint.LOCAL_IO.ordinal();
        return false;
    }

    public void denyAllocation(BloomwyrmAllocationConstraint constraint) {
        allocationConstraint = constraint.ordinal();
    }

    public long completeAllocation() {
        long producedCharge = getAllocatedChargeOutput();
        clearAllocation();
        return producedCharge;
    }

    public void deliverChargeForProgress(int progress, int duration) {
        if (duration <= 0 || allocatedChargeOutput <= 0) return;
        int boundedProgress = Math.max(0, Math.min(progress, duration));
        long quotient = allocatedChargeOutput / duration;
        long remainder = allocatedChargeOutput % duration;
        long target = quotient * boundedProgress + remainder * boundedProgress / duration;
        long pending = Math.max(0, target - deliveredChargeOutput);
        BloomwyrmHeartMachine heart = getHeart();
        if (heart != null && pending > 0) {
            deliveredChargeOutput = saturatingAdd(
                    deliveredChargeOutput,
                    heart.acceptCharge(pending));
        }
    }

    public void deliverCharge(long producedCharge) {
        BloomwyrmHeartMachine heart = getHeart();
        if (heart != null && producedCharge > 0) {
            heart.acceptCharge(producedCharge);
        }
    }

    public boolean hasCampusPowerThisTick() {
        BloomwyrmHeartMachine heart = getHeart();
        return heart != null && heart.ensureCampusPowerForCurrentTick();
    }

    public BloomwyrmHeartMachine getHeart() {
        for (GlobalPos partner : getLinkedPartners()) {
            ILinkedMultiblock linked = getPartnerMachine(partner);
            if (linked instanceof BloomwyrmHeartMachine heart && canLinkTo(partner, heart)) {
                return heart;
            }
        }
        return null;
    }

    public static @NotNull ModifierFunction recipeModifier(@NotNull MetaMachine machine, @NotNull GTRecipe recipe) {
        if (!(machine instanceof BloomwyrmUnitMachine unit) ||
                unit.allocatedParallel <= 0 ||
                recipe.id == null ||
                !recipe.id.toString().equals(unit.plannedRecipeId)) {
            return ModifierFunction.NULL;
        }
        return original -> {
            GTRecipe modified = original.copy(ContentModifier.multiplier(unit.allocatedParallel), false);
            modified.tickInputs.remove(EURecipeCapability.CAP);
            modified.parallels = original.parallels * unit.allocatedParallel;
            return modified;
        };
    }

    @Override
    public long getDisplayRecipeVoltage() {
        return allocatedEUt;
    }

    @Override
    public List<IWidget> getWidgetsForDisplay(PanelSyncManager syncManager) {
        List<IWidget> widgets = new ArrayList<>();
        BooleanSyncValue linked = new BooleanSyncValue(() -> getHeart() != null);
        IntSyncValue desired = new IntSyncValue(this::getDesiredParallel);
        IntSyncValue requested = new IntSyncValue(this::getRequestedParallel);
        IntSyncValue eligible = new IntSyncValue(this::getEligibleParallel);
        IntSyncValue offered = new IntSyncValue(this::getOfferedParallel);
        IntSyncValue allocated = new IntSyncValue(this::getAllocatedParallel);
        LongSyncValue eut = new LongSyncValue(this::getAllocatedEUt);
        IntSyncValue biopower = new IntSyncValue(this::getAllocatedBiopower);
        IntSyncValue biopowerOutput = new IntSyncValue(this::getAllocatedBiopowerOutput);
        LongSyncValue charge = new LongSyncValue(this::getAllocatedChargeInput);
        IntSyncValue constraint = new IntSyncValue(() -> getAllocationConstraint().ordinal());
        syncManager.syncValue("bloomwyrm_unit_linked", linked);
        syncManager.syncValue("bloomwyrm_unit_desired", desired);
        syncManager.syncValue("bloomwyrm_unit_requested", requested);
        syncManager.syncValue("bloomwyrm_unit_eligible", eligible);
        syncManager.syncValue("bloomwyrm_unit_offered", offered);
        syncManager.syncValue("bloomwyrm_unit_allocated", allocated);
        syncManager.syncValue("bloomwyrm_unit_eut", eut);
        syncManager.syncValue("bloomwyrm_unit_biopower", biopower);
        syncManager.syncValue("bloomwyrm_unit_biopower_output", biopowerOutput);
        syncManager.syncValue("bloomwyrm_unit_charge", charge);
        syncManager.syncValue("bloomwyrm_unit_constraint", constraint);
        widgets.add(GTMultiblockTextUtil.addUnformedWarning(this, syncManager));
        widgets.add(Text.dynamic(() -> Component.translatable(
                linked.getBoolValue() ?
                        "cosmiccore.bloomwyrm.unit.linked" :
                        "cosmiccore.bloomwyrm.unit.unlinked")
                .withStyle(linked.getBoolValue() ? ChatFormatting.GREEN : ChatFormatting.RED)).asWidget());
        if (supportsParallelControl()) {
            widgets.add(Text.dynamic(() -> Component.translatable(
                    "cosmiccore.bloomwyrm.unit.parallel_requested",
                    coloredValue(desired.getIntValue(), ChatFormatting.AQUA),
                    coloredValue(requested.getIntValue(), ChatFormatting.GREEN))
                    .withStyle(ChatFormatting.WHITE)).asWidget());
            widgets.add(Text.dynamic(() -> Component.translatable(
                    "cosmiccore.bloomwyrm.unit.parallel_limits",
                    coloredValue(eligible.getIntValue(), ChatFormatting.WHITE),
                    coloredValue(offered.getIntValue(), ChatFormatting.LIGHT_PURPLE),
                    coloredValue(allocated.getIntValue(), ChatFormatting.GREEN))
                    .withStyle(ChatFormatting.WHITE)).asWidget());
        }
        widgets.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.bloomwyrm.unit.allocation",
                coloredValue(FormattingUtil.formatNumbers(eut.getLongValue()), ChatFormatting.YELLOW),
                coloredValue(FormattingUtil.formatNumbers(charge.getLongValue()), ChatFormatting.AQUA))
                .withStyle(ChatFormatting.WHITE)).asWidget());
        widgets.add(Text.dynamic(() -> Component.translatable(
                "cosmiccore.bloomwyrm.unit.biopower",
                coloredValue(FormattingUtil.formatNumbers(biopower.getIntValue()), ChatFormatting.LIGHT_PURPLE),
                coloredValue(FormattingUtil.formatNumbers(biopowerOutput.getIntValue()), ChatFormatting.GREEN))
                .withStyle(ChatFormatting.WHITE))
                .asWidget());
        widgets.add(Text.dynamic(() -> Component.translatable(
                BloomwyrmAllocationConstraint.values()[Math.floorMod(
                        constraint.getIntValue(),
                        BloomwyrmAllocationConstraint.values().length)].translationKey())
                .withStyle(constraint.getIntValue() == BloomwyrmAllocationConstraint.NONE.ordinal() ?
                        ChatFormatting.GREEN : ChatFormatting.GOLD))
                .asWidget());
        widgets.add(GTMultiblockTextUtil.addProgressLine(this, syncManager));
        widgets.add(GTMultiblockTextUtil.addWorkingStatusLine(this, syncManager));
        return widgets;
    }

    @Override
    public Widget<?> getMainTextPanel(PanelSyncManager syncManager) {
        return BloomwyrmDisplayUI.create(this, syncManager);
    }

    @Override
    public void invalidateStructure(String name) {
        boolean interruptedAllocation = hasAllocation();
        super.invalidateStructure(name);
        if (interruptedAllocation) {
            clearAllocation();
            offeredParallel = 0;
            allocationConstraint = BloomwyrmAllocationConstraint.STRUCTURE.ordinal();
        }
    }

    private static Component coloredValue(Object value, ChatFormatting color) {
        return Component.literal(String.valueOf(value)).withStyle(color);
    }

    private void clearAllocation() {
        boolean completedAllocation = hasAllocation();
        allocatedParallel = 0;
        allocatedEUt = 0;
        allocatedBiopower = 0;
        allocatedBiopowerOutput = 0;
        allocatedChargeInput = 0;
        allocatedChargeOutput = 0;
        deliveredChargeOutput = 0;
        plannedRecipeId = "";
        BloomwyrmHeartMachine heart = getHeart();
        if (completedAllocation && heart != null) {
            heart.markAllocationComplete(getBlockPos());
        }
    }

    private static long saturatingMultiply(long value, long multiplier) {
        if (value <= 0 || multiplier <= 0) return 0;
        if (value > Long.MAX_VALUE / multiplier) return Long.MAX_VALUE;
        return value * multiplier;
    }

    private static int saturatingMultiplyInt(int value, int multiplier) {
        if (value <= 0 || multiplier <= 0) return 0;
        if (value > Integer.MAX_VALUE / multiplier) return Integer.MAX_VALUE;
        return value * multiplier;
    }

    private static long saturatingAdd(long first, long second) {
        if (second > 0 && first > Long.MAX_VALUE - second) return Long.MAX_VALUE;
        return first + second;
    }
}
