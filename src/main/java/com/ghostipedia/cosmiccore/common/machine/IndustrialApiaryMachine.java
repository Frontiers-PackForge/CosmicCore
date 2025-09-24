package com.ghostipedia.cosmiccore.common.machine;

import com.gregtechceu.gtceu.api.capability.IControllable;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.fancy.ConfiguratorPanel;
import com.gregtechceu.gtceu.api.gui.fancy.FancyMachineUIWidget;
import com.gregtechceu.gtceu.api.gui.fancy.IFancyConfiguratorButton;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.TieredEnergyMachine;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CombinedDirectionalFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputItem;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.feature.IUIMachine;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import com.lowdragmc.lowdraglib.utils.BlockInfo;
import com.lowdragmc.lowdraglib.utils.TrackedDummyWorld;
import lombok.Setter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import lombok.Getter;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class IndustrialApiaryMachine extends TieredEnergyMachine implements IAutoOutputItem, IFancyUIMachine, IMachineLife, IWorkable {

    private int tier;

    @Getter
    @Persisted
    private final NotifiableItemStackHandler inputInventory;

    @Getter
    @Persisted
    private final NotifiableItemStackHandler outputInventory;

    // TODO; Might need more vars for the math behind the logic, but these i put in for the TL keys

    @Getter
    private int machineTier;

    @Getter
    private int duration;

    @Getter
    int productionAmplifier;
    //Yoinked from Fisher
    @Getter
    @Persisted
    @DescSynced
    @RequireRerender
    protected Direction outputFacingItems;
    @Getter
    @Persisted
    @DescSynced
    @RequireRerender
    protected boolean autoOutputItems;
    @Getter
    @Setter
    @Persisted
    protected boolean allowInputFromOutputSideItems;

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            IndustrialApiaryMachine.class, TieredEnergyMachine.MANAGED_FIELD_HOLDER);

    public IndustrialApiaryMachine(IMachineBlockEntity holder, int tier, Object... args) {
        super(holder, tier, args);
        this.tier = tier;
        this.inputInventory = new NotifiableItemStackHandler(this, 1, IO.NONE, IO.BOTH);
        this.outputInventory = new NotifiableItemStackHandler(this, 9, IO.NONE, IO.BOTH);
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    // Vomitting over what this is. lord help me.
    // I HATE MAGIC NUMBERS WOOOO I LOVE GUI XY I LOVE IT SO MUCH WOOO YEAHHHH
    @Override
    public ModularUI createUI(Player entityPlayer) {
        // spotless:off
        var text = new WidgetGroup(0, 0, 176, 164);
        text.addWidget(new LabelWidget(9, 5, "gui.cosmiccore.iapiary")); //Note: canTakeItems would probably be what we want to lock? idk can we do that dynamically???
        text.addWidget(new LabelWidget(9, 50, Component.translatable("gui.cosmiccore.iapiary.yield"))); //Note: canTakeItems would probably be what we want to lock? idk can we do that dynamically???
        text.addWidget(new LabelWidget(9, 59, Component.translatable("gui.cosmiccore.iapiary.duration"))); //Note: canTakeItems would probably be what we want to lock? idk can we do that dynamically???
        text.addWidget(new LabelWidget(9, 68, Component.translatable("gui.cosmiccore.iapiary.production_amp"))); //Note: canTakeItems would probably be what we want to lock? idk can we do that dynamically???
        //For Moving all Output Slots by the same amount, references, the top left slot
        int groupOutX = 113;
        int groupOutY = 25;
        var group = new WidgetGroup(0, 0, 176, 164);
        //TODO: canTakeItems would probably be what we want to lock when running? idk can we do that dynamically???
        group.addWidget(new SlotWidget(this.inputInventory,0,8,groupOutY,true,true).setBackground(GuiTextures.SLOT));
        //TODO : PROGRESS WIDGET, I'm assuming we'll have a way to track progress in recipeLogic and then make the bar show// between the input slot and the outputs group.addWidget(new ProgressWidget());
        group.addWidget(new SlotWidget(this.outputInventory,0, groupOutX,groupOutY).setBackground(GuiTextures.SLOT));
        group.addWidget(new SlotWidget(this.outputInventory,1,groupOutX + 18,groupOutY).setBackground(GuiTextures.SLOT));
        group.addWidget(new SlotWidget(this.outputInventory,2,groupOutX + 36,groupOutY).setBackground(GuiTextures.SLOT));
        group.addWidget(new SlotWidget(this.outputInventory,3, groupOutX,groupOutY + 18).setBackground(GuiTextures.SLOT));
        group.addWidget(new SlotWidget(this.outputInventory,4,groupOutX + 18,groupOutY + 18).setBackground(GuiTextures.SLOT));
        group.addWidget(new SlotWidget(this.outputInventory,5,groupOutX + 36,groupOutY + 18).setBackground(GuiTextures.SLOT));
        group.addWidget(new SlotWidget(this.outputInventory,6,groupOutX,groupOutY + 36).setBackground(GuiTextures.SLOT));
        group.addWidget(new SlotWidget(this.outputInventory,7,groupOutX + 18,groupOutY + 36).setBackground(GuiTextures.SLOT));
        group.addWidget(new SlotWidget(this.outputInventory,8,groupOutX + 36,groupOutY + 36).setBackground(GuiTextures.SLOT));
        group.addWidget(new DraggableScrollableWidgetGroup(6,46,104,34).setBackground(GuiTextures.BACKGROUND_INVERSE));

        return new ModularUI(176, 164, this, entityPlayer)
                .background(GuiTextures.BACKGROUND)
                .widget(group)
                .widget(text)
                .widget(UITemplate.bindPlayerInventory(entityPlayer.getInventory(), GuiTextures.SLOT, 7, 84, true));
        // spotless:on
    }

    @Override
    public void attachSideTabs(TabsWidget sideTabs) {
        var directionalConfigurator = CombinedDirectionalFancyConfigurator.of(self(), self());
        if (directionalConfigurator != null)
            sideTabs.attachSubTab(directionalConfigurator);
    }

    //TODO: HELP IM SCARED
    @Override
    public boolean shouldWeatherOrTerrainExplosion() {
        return false;
    }

    @Override
    public int getProgress() {
        return 0;
    }

    @Override
    public int getMaxProgress() {
        return 0;
    }

    @Override
    public boolean isActive() {
        return false;
    }

    @Override
    public boolean isWorkingEnabled() {
        return false;
    }

    @Override
    public void setWorkingEnabled(boolean isWorkingAllowed) {

    }

    @Override
    public void setAutoOutputItems(boolean allow) {

    }

    @Override
    public void setOutputFacingItems(@Nullable Direction outputFacing) {

    }
// TODO:
    // Grab Species, Lifespan, Production Speed, Flower(?)

    // By default all I-Apiary runs are 60 seconds (Regardless of tier, tier will be used elsewhere)

    // Lifespan modifies this duration
    /*
     * Longest - 3x
     * Longer - 2.5
     * Long - 1.5
     * Normal - 1
     * Short - 0.75
     * Shorter - 0.5
     * Shortest - 0.25
     */

    // Production Speed Modifies the total Comb output (more on that below)
    /*
     * Fastest - 2x
     * Faster - 1.5x
     * Fast - 1.25x
     * Normal - 1
     * Slow - 0.75
     * Slower - 0.5
     * Slowest - 0.25
     */

    // To Determine how many combs are rewarded

    // Base Speed (60 seconds) always yields 20 combs
    // Lifespan adds a linear multi, 3x Duration = 6x Yield (20 -> 120 combs)
    // Production Speed adds a multiplier to the base value (20 * 2.5)
    // Machine Tier adds a Flat Bonus to the default ( Extra +10 per tier )

    // This is a math fiasco Idk how to solve so leaving my best examples on how the logic should work

    // The End result is basically, you stick a queen in an input slot, the queen gets locked to that slot, and the
    // machine runs, ideally i don't want to have to make the player 'cycle' the queen once it's in because that just
    // feels obnoxious!

    // TODO: Current Issues
    // No configurable output for the output
    // The UI doesn't have that top bar piece of the EIO selector thingy and idk where it is :cri:
}
