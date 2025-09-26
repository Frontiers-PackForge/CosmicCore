package com.ghostipedia.cosmiccore.common.machine;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.gtbridge.CosmicRecipeTypes;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.IWorkable;
import com.gregtechceu.gtceu.api.capability.recipe.IO;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.gui.UITemplate;
import com.gregtechceu.gtceu.api.gui.fancy.TabsWidget;
import com.gregtechceu.gtceu.api.gui.widget.SlotWidget;
import com.gregtechceu.gtceu.api.gui.widget.TankWidget;
import com.gregtechceu.gtceu.api.machine.IMachineBlockEntity;
import com.gregtechceu.gtceu.api.machine.WorkableTieredMachine;
import com.gregtechceu.gtceu.api.machine.fancyconfigurator.CombinedDirectionalFancyConfigurator;
import com.gregtechceu.gtceu.api.machine.feature.IAutoOutputItem;
import com.gregtechceu.gtceu.api.machine.feature.IFancyUIMachine;
import com.gregtechceu.gtceu.api.machine.feature.IMachineLife;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableFluidTank;
import com.gregtechceu.gtceu.api.machine.trait.NotifiableItemStackHandler;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.chance.logic.ChanceLogic;
import com.gregtechceu.gtceu.data.recipe.builder.GTRecipeBuilder;

import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import com.lowdragmc.lowdraglib.gui.texture.ProgressTexture;
import com.lowdragmc.lowdraglib.gui.widget.*;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import forestry.api.ForestryCapabilities;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.genetics.alleles.BeeChromosomes;
import forestry.api.genetics.alleles.ForestryAlleles;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

// TOOD: IAutoOutputItem
// Just wanna make it work first ngl
public class IndustrialApiaryMachine extends WorkableTieredMachine implements IFancyUIMachine, IMachineLife, IWorkable {

    @Getter
    private int beeTier;

    @Getter
    @Setter
    protected int beeProduct;

    // TODO; Might need more vars for the math behind the logic, but these i put in for the TL keys

    @Getter
    private int machineTier;

    @Getter
    private int duration;

    @Persisted
    @Getter
    protected final NotifiableFluidTank tank;

    @Persisted
    @Getter
    protected final NotifiableItemStackHandler itemCacheIn;

    @Getter
    int productionAmplifier;
    protected boolean allowInputFromOutputSideItems;

    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(
            IndustrialApiaryMachine.class, WorkableTieredMachine.MANAGED_FIELD_HOLDER);

    public IndustrialApiaryMachine(IMachineBlockEntity holder, int beeTier) {
        super(holder, beeTier, (ignored) -> 0);
        this.beeTier = beeTier;
        this.tank = createTank();
        this.itemCacheIn = createStorageCache();
    }

    @Override
    public @NotNull ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    protected NotifiableFluidTank createTank(Object... args) {
        return new NotifiableFluidTank(this, 1, 16000, IO.BOTH);
    }
    //For Input i tems
    protected NotifiableItemStackHandler createStorageCache(Object... args) {
        return new NotifiableItemStackHandler(this, 1, IO.IN, IO.IN);
    }

    //Attempting to set the non-override from 9 to 12 (with override)
    @Override
    protected @NotNull NotifiableItemStackHandler createExportItemHandler(Object... args) {
        return new NotifiableItemStackHandler(this, 12, IO.OUT, IO.OUT);
    }

    @Override
    public ModularUI createUI(Player entityPlayer) {
        // spotless:off

        var text = new WidgetGroup(0, 0, 176, 164);
        text.addWidget(new LabelWidget(9, 5, "gui.cosmiccore.iapiary")); //Note: canTakeItems would probably be what we want to lock? idk can we do that dynamically???
        int groupOutX = 113;
        int groupOutY = 7;
        var group = new WidgetGroup(0, 0, 176, 164);
        //TODO: canTakeItems would probably be what we want to lock when running? idk can we do that dynamically??? We want to lock the queen to this Ind.Apiary to avoid people cycling them across several manually or otherwise!
        group.addWidget(new SlotWidget(getItemCacheIn().storage, 0, 8, groupOutY+15, true, true).setBackground(GuiTextures.SLOT));
        //TODO : PROGRESS WIDGET, I'm assuming we'll have a way to track progress in recipeLogic and then make the bar show// between the input slot and the outputs group.addWidget(new ProgressWidget());
        group.addWidget(new SlotWidget(this.exportItems, 0, groupOutX, groupOutY, true, false).setBackground(GuiTextures.SLOT));
        group.addWidget(new SlotWidget(this.exportItems, 1, groupOutX + 18, groupOutY, true, false).setBackground(GuiTextures.SLOT));
        group.addWidget(new SlotWidget(this.exportItems, 2, groupOutX + 36, groupOutY, true, false).setBackground(GuiTextures.SLOT));
        group.addWidget(new SlotWidget(this.exportItems, 3, groupOutX, groupOutY + 18, true, false).setBackground(GuiTextures.SLOT));
        group.addWidget(new SlotWidget(this.exportItems, 4, groupOutX + 18, groupOutY + 18, true, false).setBackground(GuiTextures.SLOT));
        group.addWidget(new SlotWidget(this.exportItems, 5, groupOutX + 36, groupOutY + 18, true, false).setBackground(GuiTextures.SLOT));
        group.addWidget(new SlotWidget(this.exportItems, 6, groupOutX, groupOutY + 36, true, false).setBackground(GuiTextures.SLOT));
        group.addWidget(new SlotWidget(this.exportItems, 7, groupOutX + 18, groupOutY + 36, true, false).setBackground(GuiTextures.SLOT));
        group.addWidget(new SlotWidget(this.exportItems, 8, groupOutX + 36, groupOutY + 36, true, false).setBackground(GuiTextures.SLOT));
        group.addWidget(new SlotWidget(this.exportItems,9,groupOutX,groupOutY + 54,true,false).setBackground(GuiTextures.SLOT));
        group.addWidget(new SlotWidget(this.exportItems,10,groupOutX + 18,groupOutY + 54,true,false).setBackground(GuiTextures.SLOT));
        group.addWidget(new SlotWidget(this.exportItems,11,groupOutX + 36,groupOutY + 54,true,false).setBackground(GuiTextures.SLOT));
        group.addWidget(new DraggableScrollableWidgetGroup(6, 46, 104, 34).setBackground(GuiTextures.BACKGROUND_INVERSE));


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

    // TODO: HELP IM SCARED
    @Override
    protected @NotNull RecipeLogic createRecipeLogic(Object @NotNull ... args) {
        return new IndustrialApiaryMachine.BeeRecipeLogic(this);
    }

    @Override
    public boolean shouldWeatherOrTerrainExplosion() {
        return false;
    }

    public static class BeeRecipeLogic extends RecipeLogic {

        int beeTier;
        public int beeProduct;

        public BeeRecipeLogic(IndustrialApiaryMachine machine) {
            super(machine);
            this.beeTier = machine.getTier();
        }

        // Just doing production Mult now
        private final List<Float> productionMultipliers = List.of(0.25f, 0.5f, 0.75f, 1f, 1.25f, 2.5f, 4f);

        @Override
        public @NotNull Iterator<GTRecipe> searchRecipe() {
            var itemHandlers = machine.getCapabilitiesFlat(IO.IN, ItemRecipeCapability.CAP);
            for (var handler : itemHandlers) {
                if (!(handler instanceof NotifiableItemStackHandler itemHandler)) continue;
                for (var content : itemHandler.getContents()) {
                    if (!(content instanceof ItemStack stack)) continue;

                    // Check if it's a Forestry Handler Item
                    var optionalCap = stack.getCapability(ForestryCapabilities.INDIVIDUAL_HANDLER_ITEM,
                            (Direction) null);
                    if (!optionalCap.isPresent()) continue;
                    var cap = optionalCap.resolve().get();

                    // Check if it's a bee
                    var individual = cap.getIndividual();
                    if (!(individual instanceof IBee bee)) continue;
                    var genome = bee.getGenome();

                    // Generate recipe from bee
                    // TODO: Implement the rest of the logic
                    // TODO: Do we also want to do stuff with secondary species? reject if not equal? output a mix of
                    // both?
                    IBeeSpecies primary = genome.getActiveValue(BeeChromosomes.SPECIES);
                    IBeeSpecies secondary = genome.getInactiveValue(BeeChromosomes.SPECIES);

                    // Forestry Genome Values:

                    var lifespan = genome.getActiveAllele(BeeChromosomes.LIFESPAN);
                    // List.of(LIFESPAN_SHORTEST, LIFESPAN_SHORTER, LIFESPAN_SHORT, LIFESPAN_SHORTENED, LIFESPAN_NORMAL,
                    // LIFESPAN_ELONGATED, LIFESPAN_LONG, LIFESPAN_LONGER, LIFESPAN_LONGEST, LIFESPAN_IMMORTAL);

                    var production = genome.getActiveAllele(BeeChromosomes.SPEED);
                    // List.of(SPEED_SLOWEST, SPEED_SLOWER, SPEED_SLOW, SPEED_NORMAL, SPEED_FAST, SPEED_FASTER,
                    // SPEED_FASTEST);
                    var productionIndex = ForestryAlleles.DEFAULT_SPEEDS.indexOf(production);
                    var productionMultiplier = productionMultipliers.get(productionIndex);

                    // For other forestry genome values, see ForestryAlleles.java

                    // Define the builder, add the outputs dynamically
                    var builder = GTRecipeBuilder
                            .of(CosmicCore.id("bee_recipe_"), CosmicRecipeTypes.BEES)
                            .EUt(GTValues.VA[GTValues.LV])
                            .duration((int) (20 * 480));

                    for (var product : primary.getProducts()) {
                        builder.chancedOutput(
                                new ItemStack(
                                        product.item(),
                                        (int) (16 + (productionMultiplier * 8) + (beeTier * 16))),
                                (int) (product.chance() * ChanceLogic.getMaxChancedValue()),
                                0);
                    }
                    return Collections.singleton(builder.buildRawRecipe()).iterator();
                }
            }
            return Collections.emptyIterator();
        }
    }

    // TODO:
    // Grab Species, Lifespan, Production Speed, Flower(?)

    // By default, all I-Apiary runs are 60 seconds (Regardless of tier, tier will be used elsewhere)

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
    // Fix UI text to not crash UI loading - can't reproduce!
    // Tweak values (see above todo) - values tweaked
    // Decide about second species - Don't do anything with them
    // Re-implement IAutoOutputItem - will need help
    // No configurable output for the output
    // The UI doesn't have that top bar piece of the EIO selector thingy and idk where it is :cri:
}
