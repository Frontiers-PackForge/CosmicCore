package com.ghostipedia.cosmiccore.integration.jade.provider;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.feature.IStellarIrisProvider;
import com.ghostipedia.cosmiccore.api.machine.multiblock.StellarBaseModule;

import com.gregtechceu.gtceu.api.blockentity.MetaMachineBlockEntity;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.integration.jade.GTElementHelper;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.util.FluidTextHelper;

import java.awt.Color;

public class StellarModuleProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {

    private static final ResourceLocation UID = CosmicCore.id("stellar_module");
    private static final String DATA_KEY = UID.toString();

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof MetaMachineBlockEntity be)) return;
        if (!(be.getMetaMachine() instanceof StellarBaseModule module)) return;

        CompoundTag tag = new CompoundTag();
        writeModuleData(tag, module);
        data.put(DATA_KEY, tag);
    }

    private void writeModuleData(CompoundTag tag, StellarBaseModule module) {
        IStellarIrisProvider iris = module.getStellarIris();
        boolean connected = iris != null && iris.isFormed();
        boolean canProcess = connected && iris.canProcess();

        tag.putBoolean("connected", connected);
        tag.putBoolean("canProcess", canProcess);
        tag.putBoolean("wirelessAvailable", module.isWirelessEnergyAvailable());
        tag.putLong("energyPerTick", module.getEnergyConsumedPerTick());

        if (iris != null) {
            tag.putString("stage", iris.getStage().toString());
            if (canProcess) tag.putDouble("speedBonus", iris.getSpeedBonus());
        }

        RecipeLogic logic = module.getRecipeLogic();
        tag.putBoolean("active", logic.isActive());
        tag.putInt("progress", logic.getProgress());
        tag.putInt("maxProgress", logic.getMaxProgress());

        GTRecipe recipe = logic.getLastRecipe();
        if (logic.isWorking() && recipe != null) {
            tag.putBoolean("working", true);
            if (recipe.parallels > 1) tag.putInt("recipeParallels", recipe.parallels);
            writeRecipeOutputs(tag, recipe);
        }
    }

    private void writeRecipeOutputs(CompoundTag tag, GTRecipe recipe) {
        int recipeTier = RecipeHelper.getPreOCRecipeEuTier(recipe);
        int chanceTier = recipeTier + recipe.ocLevel;
        var chanceFunc = recipe.getType().getChanceFunction();

        ListTag itemTags = new ListTag();
        for (var item : recipe.getOutputContents(ItemRecipeCapability.CAP)) {
            var stacks = ItemRecipeCapability.CAP.of(item.content).getItems();
            if (stacks.length == 0 || stacks[0].isEmpty()) continue;

            var itemTag = new CompoundTag();
            GTUtil.saveItemStack(stacks[0], itemTag);
            if (item.chance < item.maxChance) {
                double adjusted = (double) stacks[0].getCount() * recipe.parallels *
                        chanceFunc.getBoostedChance(item, recipeTier, chanceTier) / item.maxChance;
                itemTag.putInt("Count", Math.max(1, (int) Math.round(adjusted)));
            }
            itemTags.add(itemTag);
        }
        if (!itemTags.isEmpty()) tag.put("outputItems", itemTags);

        ListTag fluidTags = new ListTag();
        for (var fluid : recipe.getOutputContents(FluidRecipeCapability.CAP)) {
            FluidStack[] stacks = FluidRecipeCapability.CAP.of(fluid.content).getStacks();
            if (stacks.length == 0 || stacks[0].isEmpty()) continue;

            var fluidTag = new CompoundTag();
            stacks[0].writeToNBT(fluidTag);
            if (fluid.chance < fluid.maxChance) {
                double adjusted = (double) stacks[0].getAmount() * recipe.parallels *
                        chanceFunc.getBoostedChance(fluid, recipeTier, chanceTier) / fluid.maxChance;
                fluidTag.putInt("Amount", Math.max(1, (int) Math.round(adjusted)));
            }
            fluidTags.add(fluidTag);
        }
        if (!fluidTags.isEmpty()) tag.put("outputFluids", fluidTags);
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag tag = accessor.getServerData().getCompound(DATA_KEY);
        if (tag.isEmpty() || !tag.contains("connected")) return;

        boolean connected = tag.getBoolean("connected");
        boolean canProcess = tag.getBoolean("canProcess");

        if (!connected) {
            tooltip.add(Component.translatable("cosmiccore.jade.stellar_module.not_connected")
                    .withStyle(ChatFormatting.RED));
        } else if (!canProcess) {
            tooltip.add(Component.translatable("cosmiccore.jade.stellar_module.iris_not_ready")
                    .withStyle(ChatFormatting.YELLOW));
            addStageInfo(tooltip, tag, ChatFormatting.GRAY);
        } else {
            tooltip.add(Component.translatable("cosmiccore.jade.stellar_module.connected")
                    .withStyle(ChatFormatting.GREEN));
            addStageInfo(tooltip, tag, ChatFormatting.AQUA);
            if (tag.contains("speedBonus")) {
                tooltip.add(Component.translatable("cosmiccore.jade.stellar_module.speed_bonus",
                        String.format("%.1fx", tag.getDouble("speedBonus"))).withStyle(ChatFormatting.GREEN));
            }
        }

        if (!tag.getBoolean("wirelessAvailable")) {
            tooltip.add(Component.translatable("cosmiccore.jade.stellar_module.no_wireless")
                    .withStyle(ChatFormatting.RED));
        } else {
            long eu = tag.getLong("energyPerTick");
            if (eu > 0) {
                tooltip.add(Component.translatable("cosmiccore.jade.stellar_module.energy_usage",
                        formatEnergy(eu)).withStyle(ChatFormatting.YELLOW));
            }
        }

        addProgressBar(tooltip, tag);
        addParallelInfo(tooltip, tag);
        addOutputs(tooltip, tag);
    }

    private void addStageInfo(ITooltip tooltip, CompoundTag tag, ChatFormatting color) {
        if (tag.contains("stage")) {
            tooltip.add(Component.translatable("cosmiccore.jade.stellar_module.stage",
                    tag.getString("stage")).withStyle(color));
        }
    }

    private void addProgressBar(ITooltip tooltip, CompoundTag tag) {
        if (!tag.getBoolean("active")) return;

        int progress = tag.getInt("progress");
        int max = tag.getInt("maxProgress");
        if (max <= 0) return;

        Component text = max < 20 ? Component.translatable("gtceu.jade.progress_tick", progress, max) :
                Component.translatable("gtceu.jade.progress_sec", Math.round(progress / 20f), Math.round(max / 20f));

        IElementHelper helper = tooltip.getElementHelper();
        float hue = (System.currentTimeMillis() % 3000) / 3000f;
        int rainbow = Color.HSBtoRGB(hue, 0.8f, 1f) | 0xFF000000;

        tooltip.add(helper.progress(
                max == 0 ? 0 : (float) progress / max,
                text,
                helper.progressStyle().color(rainbow).textColor(-1),
                Util.make(BoxStyle.DEFAULT, s -> s.borderColor = 0xFF555555),
                true));
    }

    private void addParallelInfo(ITooltip tooltip, CompoundTag tag) {
        if (!tag.contains("recipeParallels")) return;
        tooltip.add(Component.translatable("gtceu.multiblock.parallel.exact",
                Component.literal(FormattingUtil.formatNumbers(tag.getInt("recipeParallels")))
                        .withStyle(ChatFormatting.DARK_PURPLE)));
    }

    private void addOutputs(ITooltip tooltip, CompoundTag tag) {
        if (!tag.getBoolean("working")) return;

        boolean hasHeader = false;
        if (tag.contains("outputItems", Tag.TAG_LIST)) {
            ListTag items = tag.getList("outputItems", Tag.TAG_COMPOUND);
            if (!items.isEmpty()) {
                tooltip.add(Component.translatable("gtceu.top.recipe_output"));
                hasHeader = true;
                addItemOutputs(tooltip, items);
            }
        }

        if (tag.contains("outputFluids", Tag.TAG_LIST)) {
            ListTag fluids = tag.getList("outputFluids", Tag.TAG_COMPOUND);
            if (!fluids.isEmpty()) {
                if (!hasHeader) tooltip.add(Component.translatable("gtceu.top.recipe_output"));
                addFluidOutputs(tooltip, fluids);
            }
        }
    }

    private void addItemOutputs(ITooltip tooltip, ListTag items) {
        IElementHelper helper = tooltip.getElementHelper();
        for (Tag t : items) {
            if (!(t instanceof CompoundTag itemTag)) continue;
            ItemStack stack = GTUtil.loadItemStack(itemTag);
            if (stack.isEmpty()) continue;

            int count = stack.getCount();
            stack.setCount(1);
            tooltip.add(helper.smallItem(stack));
            tooltip.append(CommonComponents.space()
                    .append(String.valueOf(count))
                    .append(Component.translatable("gtceu.gui.content.times_item",
                            ComponentUtils.wrapInSquareBrackets(stack.getItem().getDescription())
                                    .withStyle(ChatFormatting.WHITE))));
        }
    }

    private void addFluidOutputs(ITooltip tooltip, ListTag fluids) {
        for (Tag t : fluids) {
            if (!(t instanceof CompoundTag fluidTag)) continue;
            FluidStack stack = FluidStack.loadFluidStackFromNBT(fluidTag);
            if (stack.isEmpty()) continue;

            tooltip.add(GTElementHelper.smallFluid(JadeFluidObject.of(stack.getFluid(), stack.getAmount())));
            tooltip.append(CommonComponents.space()
                    .append(FluidTextHelper.getUnicodeMillibuckets(stack.getAmount(), true))
                    .append(CommonComponents.space())
                    .append(ComponentUtils.wrapInSquareBrackets(stack.getDisplayName())
                            .withStyle(ChatFormatting.WHITE)));
        }
    }

    private String formatEnergy(long eu) {
        if (eu >= 1_000_000_000) return String.format("%.1fG EU/t", eu / 1e9);
        if (eu >= 1_000_000) return String.format("%.1fM EU/t", eu / 1e6);
        if (eu >= 1000) return String.format("%.1fk EU/t", eu / 1e3);
        return eu + " EU/t";
    }
}
