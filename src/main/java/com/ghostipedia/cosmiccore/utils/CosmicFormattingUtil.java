package com.ghostipedia.cosmiccore.utils;

import com.gregtechceu.gtceu.utils.FormattingUtil;

import java.math.BigInteger;
import java.text.DecimalFormat;

public class CosmicFormattingUtil {

    public static String formatNumberWithCharacterLimit(BigInteger number, int characterLimit) {
        String formattedNumber = FormattingUtil.formatNumbers(number);
        if (formattedNumber.length() <= characterLimit) return formattedNumber;
        else return (new DecimalFormat("0.000E0")).format(number);
    }

}