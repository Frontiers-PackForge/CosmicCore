package com.ghostipedia.cosmiccore.api.machine.multiblock;

import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.MultithreadedMachine;
import com.ghostipedia.cosmiccore.common.machine.multiblock.multi.logic.MultithreadedRecipeLogic;

import com.gregtechceu.gtceu.api.GTValues;
import com.gregtechceu.gtceu.api.capability.recipe.FluidRecipeCapability;
import com.gregtechceu.gtceu.api.capability.recipe.ItemRecipeCapability;
import com.gregtechceu.gtceu.api.gui.GuiTextures;
import com.gregtechceu.gtceu.api.blockentity.BlockEntityCreationInfo;
import com.gregtechceu.gtceu.api.machine.feature.multiblock.IDisplayUIMachine;
import com.gregtechceu.gtceu.api.machine.multiblock.MultiblockDisplayText;
import com.gregtechceu.gtceu.api.recipe.GTRecipe;
import com.gregtechceu.gtceu.api.recipe.content.Content;
import com.gregtechceu.gtceu.utils.FormattingUtil;
import com.gregtechceu.gtceu.utils.GTUtil;

import com.lowdragmc.lowdraglib.gui.widget.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The Dreamer's Basin Machine - A multithreaded processing machine with custom UI.
 * <p>
 * This machine extends MultithreadedMachine and provides a rich UI that displays:
 * - Thread status with color-coded indicators
 * - Per-thread recipe progress bars
 * - Current recipe information for each thread
 * - Energy consumption breakdown
 * - Overclock levels per thread
 */
public class DreamersBasinMachine extends MultithreadedMachine implements IDisplayUIMachine {


    public DreamersBasinMachine(BlockEntityCreationInfo holder) {
        super(holder);
    }


    // ===== Custom UI Implementation =====

    @Override
    public Widget createUIWidget() {
        var group = new WidgetGroup(0, 0, 256, 200);

        // Main scrollable area
        var scrollable = new DraggableScrollableWidgetGroup(4, 4, 248, 192)
                .setBackground(getScreenTexture());

        // Title
        scrollable.addWidget(new LabelWidget(4, 5, self().getBlockState().getBlock().getDescriptionId()));

        // Component panel for dynamic text (status, energy, etc.)
        scrollable.addWidget(new ComponentPanelWidget(4, 17, this::addDisplayText)
                .textSupplier(this.getLevel().isClientSide ? null : this::addDisplayText)
                .setMaxWidthLimit(240)
                .clickHandler(this::handleDisplayClick));

        group.addWidget(scrollable);
        group.setBackground(GuiTextures.BACKGROUND_INVERSE);

        return group;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        // Use MultiblockDisplayText builder for consistent formatting
        var builder = MultiblockDisplayText.builder(textList, isFormed())
                .setWorkingStatus(isWorkingEnabled(), getRunningThreadCount() > 0);

        if (isFormed()) {
            // Energy info first
            builder.addEnergyUsageLine(energyContainer);
            builder.addEnergyTierLine(tier);

            // Separator
            builder.addCustom(tl -> tl.add(Component.empty()));

            // Thread Status Header
            builder.addCustom(tl -> {
                tl.add(Component.translatable("cosmiccore.machine.dreamers_basin.thread_header")
                        .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

                // Summary line
                int running = getRunningThreadCount();
                int total = getThreadLogics().size();
                int max = getMaxThreads();

                MutableComponent summary = Component.literal("  ")
                        .append(Component.translatable("cosmiccore.machine.dreamers_basin.threads_summary",
                                running, total, max));

                if (running == total && total > 0) {
                    summary = summary.withStyle(ChatFormatting.GREEN);
                } else if (running > 0) {
                    summary = summary.withStyle(ChatFormatting.YELLOW);
                } else {
                    summary = summary.withStyle(ChatFormatting.GRAY);
                }
                tl.add(summary);
            });

            // Per-thread detailed status
            builder.addCustom(tl -> {
                tl.add(Component.empty());

                for (MultithreadedRecipeLogic logic : getThreadLogics().values()) {
                    addThreadStatusLine(tl, logic);
                }
            });

            // EU Budget info
            builder.addCustom(tl -> {
                tl.add(Component.empty());
                tl.add(Component.translatable("cosmiccore.machine.dreamers_basin.eu_budget_header")
                        .withStyle(ChatFormatting.GOLD));

                if (!getThreadLogics().isEmpty()) {
                    MultithreadedRecipeLogic firstThread = getThreadLogics().values().iterator().next();
                    long euPerThread = firstThread.getMaxEUtPerThread();
                    int voltageTier = GTUtil.getFloorTierByVoltage(euPerThread);
                    String tierName = GTValues.VNF[voltageTier];

                    tl.add(Component.literal("  ")
                            .append(Component.translatable("cosmiccore.machine.dreamers_basin.eu_per_thread",
                                    FormattingUtil.formatNumbers(euPerThread), tierName))
                            .withStyle(ChatFormatting.GRAY));
                }
            });
        }

        // Additional display from definition
        getDefinition().getAdditionalDisplay().accept(this, textList);
    }

    /**
     * Add a detailed status line for a single thread.
     */
    private void addThreadStatusLine(List<Component> textList, MultithreadedRecipeLogic logic) {
        int color = logic.getThreadColor();
        String colorName = getColorDisplayName(color);
        ChatFormatting colorFormat = getColorChatFormatting(color);

        // Build the thread status line
        MutableComponent line = Component.literal("  ");

        // Color indicator [COLOR]
        line.append(Component.literal("[" + colorName + "] ").withStyle(colorFormat));

        if (logic.isWorking()) {
            // Thread is actively processing
            GTRecipe recipe = logic.getCurrentRecipe();
            int progress = logic.getProgress();
            int duration = logic.getDuration();
            int percent = duration > 0 ? (progress * 100 / duration) : 0;

            // Progress bar visualization
            String progressBar = createProgressBar(percent);

            // Build hover tooltip with recipe details
            Component hoverTooltip = buildRecipeTooltip(recipe, duration);

            // Create the progress portion with hover event
            MutableComponent progressComponent = Component.literal(progressBar + " ")
                    .withStyle(Style.EMPTY
                            .withColor(ChatFormatting.GREEN)
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverTooltip)));

            line.append(progressComponent);
            line.append(Component.literal(percent + "%").withStyle(ChatFormatting.WHITE));

            // Recipe EU/t info
            if (recipe != null) {
                long recipeEUt = recipe.getInputEUt().getTotalEU();
                if (recipeEUt > 0) {
                    line.append(Component.literal(" (")
                            .append(Component.literal(FormattingUtil.formatNumbers(recipeEUt) + " EU/t")
                                    .withStyle(ChatFormatting.YELLOW))
                            .append(Component.literal(")")));
                }
            }

            textList.add(line);

            // Add time remaining on next line (also with hover)
            if (duration > 0) {
                int ticksRemaining = duration - progress;
                float secondsRemaining = ticksRemaining / 20.0f;
                MutableComponent timeLine = Component.literal("    ")
                        .append(Component.translatable("cosmiccore.machine.dreamers_basin.time_remaining",
                                String.format("%.1fs", secondsRemaining))
                                .withStyle(Style.EMPTY
                                        .withColor(ChatFormatting.DARK_GRAY)
                                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverTooltip))));
                textList.add(timeLine);
            }

        } else if (logic.isIdle()) {
            line.append(Component.translatable("cosmiccore.machine.dreamers_basin.status_idle")
                    .withStyle(ChatFormatting.GRAY));
            textList.add(line);

        } else if (logic.isWaiting()) {
            line.append(Component.translatable("cosmiccore.machine.dreamers_basin.status_waiting")
                    .withStyle(ChatFormatting.YELLOW));
            textList.add(line);

        } else if (logic.isSuspend()) {
            line.append(Component.translatable("cosmiccore.machine.dreamers_basin.status_suspended")
                    .withStyle(ChatFormatting.RED));
            textList.add(line);

        } else {
            line.append(Component.translatable("cosmiccore.machine.dreamers_basin.status_unknown")
                    .withStyle(ChatFormatting.DARK_GRAY));
            textList.add(line);
        }
    }

    /**
     * Build a hover tooltip showing the first recipe output and production rate.
     * Due to Minecraft hover event limitations, this is kept to a single line.
     */
    private Component buildRecipeTooltip(GTRecipe recipe, int duration) {
        if (recipe == null) {
            return Component.translatable("cosmiccore.machine.dreamers_basin.tooltip.no_recipe");
        }

        // Calculate rate (items per second)
        float recipesPerSecond = duration > 0 ? 20.0f / duration : 0;

        MutableComponent tooltip = Component.translatable("cosmiccore.machine.dreamers_basin.tooltip.crafting")
                .withStyle(ChatFormatting.GOLD)
                .append(Component.literal(" ").withStyle(ChatFormatting.RESET));

        // Try to find first item output
        // Note: Content stores SizedIngredient, not raw ItemStack
        List<Content> itemOutputs = recipe.getOutputContents(ItemRecipeCapability.CAP);
        if (itemOutputs != null && !itemOutputs.isEmpty()) {
            for (Content content : itemOutputs) {
                Object contentObj = content.getContent();
                if (contentObj instanceof SizedIngredient ingredient) {
                    ItemStack[] items = ingredient.getItems();
                    if (items.length > 0 && !items[0].isEmpty()) {
                        ItemStack stack = items[0];
                        int count = stack.getCount();
                        float perSecond = count * recipesPerSecond;

                        tooltip.append(stack.getHoverName().copy().withStyle(ChatFormatting.WHITE))
                                .append(Component.literal(" x" + count).withStyle(ChatFormatting.GRAY));

                        if (perSecond >= 0.1f) {
                            tooltip.append(Component.literal(String.format(" (%.1f/s)", perSecond))
                                    .withStyle(ChatFormatting.AQUA));
                        }
                        return tooltip;
                    }
                }
            }
        }

        // Try fluid outputs if no items
        // Note: Content stores SizedFluidIngredient, not raw FluidStack
        List<Content> fluidOutputs = recipe.getOutputContents(FluidRecipeCapability.CAP);
        if (fluidOutputs != null && !fluidOutputs.isEmpty()) {
            for (Content content : fluidOutputs) {
                Object contentObj = content.getContent();
                if (contentObj instanceof SizedFluidIngredient ingredient) {
                    FluidStack[] fluids = ingredient.getFluids();
                    if (fluids.length > 0 && !fluids[0].isEmpty()) {
                        int amount = ingredient.amount();
                        float perSecond = amount * recipesPerSecond;

                        tooltip.append(fluids[0].getHoverName().copy().withStyle(ChatFormatting.BLUE))
                                .append(Component.literal(" " + FormattingUtil.formatNumbers(amount) + "mB")
                                        .withStyle(ChatFormatting.GRAY));

                        if (perSecond >= 1f) {
                            tooltip.append(Component.literal(String.format(" (%.0f mB/s)", perSecond))
                                    .withStyle(ChatFormatting.AQUA));
                        }
                        return tooltip;
                    }
                }
            }
        }

        // Generic fallback
        return tooltip.append(Component.translatable("cosmiccore.machine.dreamers_basin.tooltip.processing")
                .withStyle(ChatFormatting.GRAY));
    }

    /**
     * Create a simple text-based progress bar.
     */
    private String createProgressBar(int percent) {
        int filled = percent / 10;
        int empty = 10 - filled;
        return "[" + "=".repeat(filled) + "-".repeat(empty) + "]";
    }

    /**
     * Get a display-friendly color name.
     * GTCEu stores painting color as dye.getMapColor().col
     */
    private String getColorDisplayName(int color) {
        if (color == -1) return "Default";

        for (DyeColor dye : DyeColor.values()) {
            // GTCEu uses getMapColor().col for painted colors
            if (dye.getMapColor().col == color) {
                // Capitalize first letter of each word
                String name = dye.getName().replace("_", " ");
                StringBuilder result = new StringBuilder();
                for (String word : name.split(" ")) {
                    if (!result.isEmpty()) result.append(" ");
                    result.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
                }
                return result.toString();
            }
        }
        return "Custom";
    }

    /**
     * Get the ChatFormatting color that best matches the thread color.
     * GTCEu stores painting color as dye.getMapColor().col
     */
    private ChatFormatting getColorChatFormatting(int color) {
        if (color == -1) return ChatFormatting.WHITE;

        for (DyeColor dye : DyeColor.values()) {
            // GTCEu uses getMapColor().col for painted colors
            if (dye.getMapColor().col == color) {
                return switch (dye) {
                    case WHITE -> ChatFormatting.WHITE;
                    case ORANGE -> ChatFormatting.GOLD;
                    case MAGENTA -> ChatFormatting.LIGHT_PURPLE;
                    case LIGHT_BLUE -> ChatFormatting.AQUA;
                    case YELLOW -> ChatFormatting.YELLOW;
                    case LIME -> ChatFormatting.GREEN;
                    case PINK -> ChatFormatting.LIGHT_PURPLE;
                    case GRAY -> ChatFormatting.DARK_GRAY;
                    case LIGHT_GRAY -> ChatFormatting.GRAY;
                    case CYAN -> ChatFormatting.DARK_AQUA;
                    case PURPLE -> ChatFormatting.DARK_PURPLE;
                    case BLUE -> ChatFormatting.BLUE;
                    case BROWN -> ChatFormatting.GOLD;
                    case GREEN -> ChatFormatting.DARK_GREEN;
                    case RED -> ChatFormatting.RED;
                    case BLACK -> ChatFormatting.DARK_GRAY;
                };
            }
        }
        return ChatFormatting.WHITE;
    }

    /**
     * Get all thread logics for iteration.
     */
    public Iterable<MultithreadedRecipeLogic> getThreadLogicsIterable() {
        return getThreadLogics().values();
    }
}
