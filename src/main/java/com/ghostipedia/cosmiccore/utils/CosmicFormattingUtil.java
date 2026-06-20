package com.ghostipedia.cosmiccore.utils;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.math.BigInteger;
import java.text.DecimalFormat;

public class CosmicFormattingUtil {

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
