package com.ghostipedia.cosmiccore.utils;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.time.Duration;

public class CosmicFormattingUtil {

    public static MutableComponent formatEnergyStorageDuration(BigInteger seconds) {
        Duration duration = Duration.ofSeconds(seconds.max(BigInteger.ZERO)
                .min(BigInteger.valueOf(Long.MAX_VALUE)).longValue());
        String unit;
        long value;
        if (duration.getSeconds() <= 180) {
            unit = "seconds";
            value = duration.getSeconds();
        } else if (duration.toMinutes() <= 180) {
            unit = "minutes";
            value = duration.toMinutes();
        } else if (duration.toHours() <= 72) {
            unit = "hours";
            value = duration.toHours();
        } else if (duration.toDays() <= 730) {
            unit = "days";
            value = duration.toDays();
        } else if (duration.toDays() / 365 < 1_000_000) {
            unit = "years";
            value = duration.toDays() / 365;
        } else {
            return Component.translatable("gtceu.multiblock.power_substation.time_forever");
        }
        return Component.translatable("gtceu.multiblock.power_substation.time_" + unit,
                FormattingUtil.formatNumbers(value));
    }

    public static String formatNumberWithCharacterLimit(BigInteger number, int characterLimit) {
        String formattedNumber = FormattingUtil.formatNumbers(number);
        if (formattedNumber.length() <= characterLimit) return formattedNumber;
        else return (new DecimalFormat("0.000E0")).format(number);
    }

    public static MutableComponent formatWithConstantWidth(String labelKey, Component body, int width) {
        var tmp = Component.translatable(labelKey, body);
        var baseLength = getComponentLength(tmp);
        var spaceLength = width - baseLength;
        if (spaceLength <= 0) return Component.literal("Err: Too long");
        var spacerCount = (spaceLength / 2) - 4;
        var spacer = spacerCount > 0 ? (".".repeat((spaceLength / 2) - 4) + " ") : "";
        var spacerComponent = Component.literal(spacer).withStyle(ChatFormatting.DARK_GRAY);
        return Component.translatable(labelKey, spacerComponent.append(body));
    }

    public static MutableComponent combineWithConstantWidth(Component comp1, Component comp2, int width) {
        var tmp = Component.empty().append(comp1).append(comp2);
        var baseLength = getComponentLength(tmp);
        var spaceLength = width - baseLength;
        if (spaceLength <= 0) return Component.literal("Err: Too long");
        var spacerCount = (spaceLength / 2) - 4;
        var spacer = spacerCount > 0 ? (".".repeat((spaceLength / 2) - 4) + " ") : "";
        var spacerComponent = Component.literal(spacer).withStyle(ChatFormatting.DARK_GRAY);
        return Component.empty().append(comp1).append(spacerComponent).append(comp2);
    }

    private static int getComponentLength(Component component) {
        var util = new StupidFontUtils();
        return util.getStringWidth(component.getString());
    }
}
