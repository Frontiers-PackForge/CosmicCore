package com.ghostipedia.cosmiccore.integration.jade.provider;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.api.machine.feature.IStellarIrisProvider;
import com.ghostipedia.cosmiccore.api.machine.multiblock.StellarBaseModule;

import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.machine.MetaMachine;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IMultiController;
import com.gregtechceu.gtceu.api.machine.trait.RecipeLogic;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.RecipeHelper;
import com.gregtechceu.gtceu.integration.jade.GTElementHelper;
import com.gregtechceu.gtceu.integration.jade.provider.CapabilityBlockProvider;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.ui.BoxStyle;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.util.FluidTextHelper;

public class StellarModuleProvider extends CapabilityBlockProvider<StellarBaseModule> {

    public StellarModuleProvider() {
        super(CosmicCore.id("stellar_module"));
    }

    @Nullable
    @Override
    protected StellarBaseModule getCapability(Level level, BlockPos blockPos, @Nullable Direction direction) {
        if (MetaMachine.getMachine(level, blockPos) instanceof IMultiController controller) {
            if (controller instanceof StellarBaseModule module) {
                return module;
            }
        }
        return null;
    }

    @Override
    protected void write(CompoundTag tag, StellarBaseModule module) {
        IStellarIrisProvider iris = module.getStellarIris();
        boolean connected = iris != null && iris.isFormed();
        boolean canProcess = connected && iris.canProcess();

        tag.putBoolean("connected", connected);
        tag.putBoolean("canProcess", canProcess);
        tag.putBoolean("wirelessAvailable", module.isWirelessEnergyAvailable());
        tag.putLong("energyPerTick", module.getEnergyConsumedPerTick());

        if (iris != null) {
            tag.putString("stage", iris.getStage().toString());
            if (canProcess) {
                tag.putDouble("speedBonus", iris.getSpeedBonus());
            }
        }

        RecipeLogic recipeLogic = module.getRecipeLogic();
        tag.putBoolean("active", recipeLogic.isActive());
        tag.putBoolean("workingEnabled", recipeLogic.isWorkingEnabled());
        tag.putInt("progress", recipeLogic.getProgress());
        tag.putInt("maxProgress", recipeLogic.getMaxProgress());

        GTRecipe recipe = recipeLogic.getLastRecipe();
        if (recipeLogic.isWorking() && recipe != null) {
            tag.putBoolean("working", true);
            if (recipe.parallels > 1) {
                tag.putInt("recipeParallels", recipe.parallels);
            }
            writeRecipeOutputs(tag, recipe);
        }
    }

    private void writeRecipeOutputs(CompoundTag tag, GTRecipe recipe) {
        int recipeTier = RecipeHelper.getPreOCRecipeEuTier(recipe);
        int chanceTier = recipeTier + recipe.ocLevel;
        var function = recipe.getType().getChanceFunction();

        // Item outputs
        var itemContents = recipe.getOutputContents(ItemRecipeCapability.CAP);
        ListTag itemTags = new ListTag();
        for (var item : itemContents) {
            var stacks = ItemRecipeCapability.CAP.of(item.content).getItems();
            if (stacks.length == 0 || stacks[0].isEmpty()) continue;
            var stack = stacks[0];
            var itemTag = new CompoundTag();
            GTUtil.saveItemStack(stack, itemTag);
            if (item.chance < item.maxChance) {
                int count = stack.getCount();
                double countD = (double) count * recipe.parallels *
                        function.getBoostedChance(item, recipeTier, chanceTier) / item.maxChance;
                count = countD < 1 ? 1 : (int) Math.round(countD);
                itemTag.putInt("Count", count);
            }
            itemTags.add(itemTag);
        }
        if (!itemTags.isEmpty()) {
            tag.put("outputItems", itemTags);
        }

        // Fluid outputs
        var fluidContents = recipe.getOutputContents(FluidRecipeCapability.CAP);
        ListTag fluidTags = new ListTag();
        for (var fluid : fluidContents) {
            FluidStack[] stacks = FluidRecipeCapability.CAP.of(fluid.content).getStacks();
            if (stacks.length == 0 || stacks[0].isEmpty()) continue;
            var stack = stacks[0];
            var fluidTag = new CompoundTag();
            stack.writeToNBT(fluidTag);
            if (fluid.chance < fluid.maxChance) {
                int amount = stacks[0].getAmount();
                double amountD = (double) amount * recipe.parallels *
                        function.getBoostedChance(fluid, recipeTier, chanceTier) / fluid.maxChance;
                amount = amountD < 1 ? 1 : (int) Math.round(amountD);
                fluidTag.putInt("Amount", amount);
            }
            fluidTags.add(fluidTag);
        }
        if (!fluidTags.isEmpty()) {
            tag.put("outputFluids", fluidTags);
        }
    }

    @Override
    protected void addTooltip(CompoundTag tag, ITooltip tooltip, Player player, BlockAccessor accessor,
                              BlockEntity blockEntity, IPluginConfig config) {
        if (!tag.contains("connected")) {
            return;
        }

        boolean connected = tag.getBoolean("connected");
        boolean canProcess = tag.getBoolean("canProcess");
        boolean wirelessAvailable = tag.getBoolean("wirelessAvailable");

        if (!connected) {
            tooltip.add(Component.translatable("cosmiccore.jade.stellar_module.not_connected")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        } else if (!canProcess) {
            tooltip.add(Component.translatable("cosmiccore.jade.stellar_module.iris_not_ready")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)));
            if (tag.contains("stage")) {
                tooltip.add(Component.translatable("cosmiccore.jade.stellar_module.stage",
                        tag.getString("stage"))
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
            }
        } else {
            tooltip.add(Component.translatable("cosmiccore.jade.stellar_module.connected")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
            if (tag.contains("stage")) {
                tooltip.add(Component.translatable("cosmiccore.jade.stellar_module.stage",
                        tag.getString("stage"))
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)));
            }
            if (tag.contains("speedBonus")) {
                tooltip.add(Component.translatable("cosmiccore.jade.stellar_module.speed_bonus",
                        String.format("%.1fx", tag.getDouble("speedBonus")))
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));
            }
        }

        if (!wirelessAvailable) {
            tooltip.add(Component.translatable("cosmiccore.jade.stellar_module.no_wireless")
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        } else {
            long euPerTick = tag.getLong("energyPerTick");
            if (euPerTick > 0) {
                tooltip.add(Component.translatable("cosmiccore.jade.stellar_module.energy_usage",
                        formatEnergy(euPerTick))
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW)));
            }
        }

        if (tag.getBoolean("active")) {
            int progress = tag.getInt("progress");
            int maxProgress = tag.getInt("maxProgress");

            if (maxProgress > 0) {
                Component text;
                if (maxProgress < 20) {
                    text = Component.translatable("gtceu.jade.progress_tick", progress, maxProgress);
                } else {
                    text = Component.translatable("gtceu.jade.progress_sec",
                            Math.round(progress / 20.0F), Math.round(maxProgress / 20.0F));
                }

                int rainbowColor = getRainbowColor();
                IElementHelper helper = tooltip.getElementHelper();
                tooltip.add(helper.progress(
                        getProgress(progress, maxProgress),
                        text,
                        helper.progressStyle().color(rainbowColor).textColor(-1),
                        Util.make(BoxStyle.DEFAULT, style -> style.borderColor = 0xFF555555),
                        true));
            }
        }

        if (tag.contains("recipeParallels")) {
            int parallels = tag.getInt("recipeParallels");
            Component parallelsText = Component.literal(FormattingUtil.formatNumbers(parallels))
                    .withStyle(ChatFormatting.DARK_PURPLE);
            tooltip.add(Component.translatable("gtceu.multiblock.parallel.exact", parallelsText));
        }

        if (tag.getBoolean("working")) {
            boolean hasOutputs = false;

            if (tag.contains("outputItems", Tag.TAG_LIST)) {
                ListTag itemTags = tag.getList("outputItems", Tag.TAG_COMPOUND);
                if (!itemTags.isEmpty()) {
                    if (!hasOutputs) {
                        tooltip.add(Component.translatable("gtceu.top.recipe_output"));
                        hasOutputs = true;
                    }
                    addItemOutputs(tooltip, itemTags);
                }
            }

            if (tag.contains("outputFluids", Tag.TAG_LIST)) {
                ListTag fluidTags = tag.getList("outputFluids", Tag.TAG_COMPOUND);
                if (!fluidTags.isEmpty()) {
                    if (!hasOutputs) {
                        tooltip.add(Component.translatable("gtceu.top.recipe_output"));
                    }
                    addFluidOutputs(tooltip, fluidTags);
                }
            }
        }
    }

    private void addItemOutputs(ITooltip tooltip, ListTag itemTags) {
        IElementHelper helper = tooltip.getElementHelper();
        for (Tag tag : itemTags) {
            if (tag instanceof CompoundTag itemTag) {
                ItemStack stack = GTUtil.loadItemStack(itemTag);
                if (!stack.isEmpty()) {
                    MutableComponent text = CommonComponents.space();
                    text.append(String.valueOf(stack.getCount()));
                    stack.setCount(1);
                    text.append(Component.translatable("gtceu.gui.content.times_item",
                            getItemName(stack)).withStyle(ChatFormatting.WHITE));
                    tooltip.add(helper.smallItem(stack));
                    tooltip.append(text);
                }
            }
        }
    }

    private void addFluidOutputs(ITooltip tooltip, ListTag fluidTags) {
        for (Tag tag : fluidTags) {
            if (tag instanceof CompoundTag fluidTag) {
                FluidStack stack = FluidStack.loadFluidStackFromNBT(fluidTag);
                if (!stack.isEmpty()) {
                    MutableComponent text = CommonComponents.space();
                    text.append(FluidTextHelper.getUnicodeMillibuckets(stack.getAmount(), true));
                    text.append(CommonComponents.space())
                            .append(getFluidName(stack))
                            .withStyle(ChatFormatting.WHITE);
                    tooltip.add(GTElementHelper.smallFluid(JadeFluidObject.of(stack.getFluid(), stack.getAmount())));
                    tooltip.append(text);
                }
            }
        }
    }

    private Component getItemName(ItemStack stack) {
        return ComponentUtils.wrapInSquareBrackets(stack.getItem().getDescription()).withStyle(ChatFormatting.WHITE);
    }

    private Component getFluidName(FluidStack stack) {
        return ComponentUtils.wrapInSquareBrackets(stack.getDisplayName()).withStyle(ChatFormatting.WHITE);
    }

    private String formatEnergy(long eu) {
        if (eu >= 1_000_000_000) return String.format("%.1fG EU/t", eu / 1_000_000_000.0);
        if (eu >= 1_000_000) return String.format("%.1fM EU/t", eu / 1_000_000.0);
        if (eu >= 1000) return String.format("%.1fk EU/t", eu / 1000.0);
        return String.format("%d EU/t", eu);
    }

    private int getRainbowColor() {
        float hue = (System.currentTimeMillis() % 3000) / 3000.0f;
        return java.awt.Color.HSBtoRGB(hue, 0.8f, 1.0f) | 0xFF000000;
    }
}
