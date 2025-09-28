package com.ghostipedia.cosmiccore.common.data;

import forestry.api.apiculture.IBeeHousing;
import forestry.api.apiculture.IBeeModifier;
import forestry.api.apiculture.genetics.IBee;
import forestry.api.apiculture.genetics.IBeeSpecies;
import forestry.api.apiculture.hives.IHiveFrame;
import forestry.api.genetics.IGenome;
import forestry.api.genetics.IMutation;
import forestry.core.items.ItemForestry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.List;

public class CosmicBeesItemHiveFraming extends ItemForestry implements IHiveFrame {

    private final Modifier beeMod;

    public CosmicBeesItemHiveFraming(CosmicBeesItemHiveFrameBuilder builder) {
        super((new Item.Properties()).durability(builder.maxDmg));
        this.beeMod = new Modifier(builder.ageMult,
                builder.speedMult,
                builder.pollinationMult,
                builder.decayMult,
                builder.mutationMult,
                builder.isRainproof,
                builder.isAlwaysSunny,
                builder.isHellish);
    }

    @Override
    public @NotNull ItemStack frameUsed(IBeeHousing iBeeHousing, ItemStack itemStack, IBee iBee, int i) {
        return itemStack.hurt(i, iBeeHousing.getWorldObj().getRandom(), null) ? ItemStack.EMPTY : itemStack;
    }

    @Override
    public IBeeModifier getBeeModifier(ItemStack itemStack) {
        return this.beeMod;
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level world, List<Component> tooltip, TooltipFlag advanced) {
        super.appendHoverText(stack, world, tooltip, advanced);
        DecimalFormat FORMAT = new DecimalFormat("#.##");
        if (beeMod.ageMult != 1) {
            tooltip.add(Component.translatable("item.cosmicbees.bee.modifier.aging_multiplier").withStyle(ChatFormatting.GRAY)
                    .append(": ")
                    .append(Component.literal(FORMAT.format(beeMod.ageMult) + "x")
                            .withStyle(beeMod.ageMult > 1 ? ChatFormatting.RED : ChatFormatting.GREEN)));
        }

        if (!stack.isDamaged()) {
            tooltip.add(Component.translatable("item.forestry.durability", new Object[]{stack.getMaxDamage()}));
        }
    }

    private class Modifier implements IBeeModifier {

        private final float ageMult;
        private final float speedMult;
        private final float pollinationMult;
        private final float decayMult;
        private final float mutationMult;
        private final boolean isRainproof;
        private final boolean isAlwaysSunny;
        private final boolean isHellish;
        public Modifier(float ageMult, float speedMult, float pollinationMult, float decayMult, float mutationMult, boolean isRainproof, boolean isAlwaysSunny, boolean isHellish) {
            this.ageMult = ageMult;
            this.speedMult = speedMult;
            this.pollinationMult = pollinationMult;
            this.decayMult = decayMult;
            this.mutationMult = mutationMult;
            this.isRainproof = isRainproof;
            this.isAlwaysSunny = isAlwaysSunny;
            this.isHellish = isHellish;
        }

        @Override
        public float modifyMutationChance(IGenome genome, IGenome mate, IMutation<IBeeSpecies> mutation, float currentChance) {
            // mult cap is the base mutation chance to the power of 3. ie. 0.06 -> 0.09 -> 0.135 -> 0.203 -> 0.304, capped at 0.5
            float multCap = Math.min((float)(mutation.getChance() * (Math.pow(1.5, 4))), 0.5f);
            return Math.min(currentChance * mutationMult, multCap);
        }

        @Override
        public float modifyAging(IGenome genome, @Nullable IGenome mate, float currentAging) {
            return currentAging * ageMult;
        }

        @Override
        public float modifyProductionSpeed(IGenome genome, float currentSpeed) {
            return currentSpeed * speedMult;
        }

        @Override
        public float modifyPollination(IGenome genome, float currentPollination) {
            return currentPollination * pollinationMult;
        }

        @Override
        public float modifyGeneticDecay(IGenome genome, float currentDecay) {
            return currentDecay * decayMult;
        }

        @Override
        public boolean isSealed() {
            return isRainproof;
        }

        @Override
        public boolean isSunlightSimulated() {
            return isAlwaysSunny;
        }

        @Override
        public boolean isHellish() {
            return isHellish;
        }
    }

    public static class CosmicBeesItemHiveFrameBuilder {

        // required params
        private int maxDmg = 64;

        // optional params
        private float ageMult = 1;
        private float speedMult = 1;
        private float pollinationMult = 1;
        private float decayMult = 1;
        private float mutationMult = 1;
        private boolean isRainproof = false;
        private boolean isAlwaysSunny = false;
        private boolean isHellish = false;

        public CosmicBeesItemHiveFrameBuilder(int maxDmg) {
            this.maxDmg = maxDmg;
        }

        public CosmicBeesItemHiveFrameBuilder setAgeMult(float ageMult) {
            this.ageMult = ageMult;
            return this;
        }

        public CosmicBeesItemHiveFrameBuilder setSpeedMult(float speedMult) {
            this.speedMult = speedMult;
            return this;
        }

        public CosmicBeesItemHiveFrameBuilder setPollinationMult(float pollinationMult) {
            this.pollinationMult = pollinationMult;
            return this;
        }

        public CosmicBeesItemHiveFrameBuilder setDecayMult(float decayMult) {
            this.decayMult = decayMult;
            return this;
        }

        public CosmicBeesItemHiveFrameBuilder setMutationMult(float mutationMult) {
            this.mutationMult = mutationMult;
            return this;
        }

        public CosmicBeesItemHiveFrameBuilder setIsRainproof(boolean isRainproof) {
            this.isRainproof = isRainproof;
            return this;
        }

        public CosmicBeesItemHiveFrameBuilder setIsAlwaysSunny(boolean isAlwaysSunny) {
            this.isAlwaysSunny = isAlwaysSunny;
            return this;
        }

        public CosmicBeesItemHiveFrameBuilder setIsHellish(boolean isHellish) {
            this.isHellish = isHellish;
            return this;
        }

        public CosmicBeesItemHiveFraming build() {
            return new CosmicBeesItemHiveFraming(this);
        }
    }

}
