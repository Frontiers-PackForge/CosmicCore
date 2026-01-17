package com.ghostipedia.cosmiccore.integration.emi;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.runtime.EmiFavorite;

public class CosmicFavorite extends EmiFavorite {

    private long amount;

    public CosmicFavorite(EmiIngredient stack, long amount) {
        super(stack, null);
        this.amount = Math.max(1, amount);
    }

    public void adjustAmount(long delta) {
        this.amount = Math.max(1, amount + delta);
    }

    @Override
    public long getAmount() {
        return amount;
    }
}
