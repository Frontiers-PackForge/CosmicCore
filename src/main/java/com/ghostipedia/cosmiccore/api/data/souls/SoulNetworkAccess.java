package com.ghostipedia.cosmiccore.api.data.souls;

import com.ghostipedia.cosmiccore.api.capability.souls.SoulType;
import com.ghostipedia.cosmiccore.api.recipe.ingredient.SoulStack;

import net.minecraft.server.level.ServerLevel;

import com.breakinblocks.neovitae.api.soul.AnimaTicket;
import com.breakinblocks.neovitae.api.soul.IAnima;
import com.breakinblocks.neovitae.util.helper.AnimaHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.ToIntFunction;

public record SoulNetworkAccess(SoulNetwork souls, IAnima anima) {

    public static SoulNetworkAccess get(ServerLevel level, UUID soulOwner, UUID animaOwner) {
        return new SoulNetworkAccess(SoulNetworkSavedData.getSoulNetwork(level, soulOwner),
                AnimaHelper.getAnima(animaOwner));
    }

    public int getAmount(SoulType type) {
        return type == SoulType.Anima ? anima.getCurrentEV() : souls.getAmount(type);
    }

    public List<SoulStack> getContents() {
        List<SoulStack> contents = new ArrayList<>();
        int amount = anima.getCurrentEV();
        if (amount > 0) contents.add(new SoulStack(SoulType.Anima, amount));
        souls.getContents().stream().filter(stack -> stack.type() != SoulType.Anima).forEach(contents::add);
        return contents;
    }

    public SoulStack add(SoulStack stack, int throughput, int capacity, boolean simulate) {
        if (stack.type() != SoulType.Anima) return souls.add(stack, throughput, capacity, simulate);
        int accepted = Math.max(0, Math.min(Math.min(stack.amount(), throughput), capacity - anima.getCurrentEV()));
        if (!simulate && accepted > 0) accepted = anima.add(AnimaTicket.create(accepted), capacity);
        return stack.withAmount(accepted);
    }

    public boolean extractAll(Collection<SoulStack> stacks, ToIntFunction<SoulType> throughput, boolean simulate) {
        long requested = requestedAnima(stacks);
        List<SoulStack> other = otherSouls(stacks);
        synchronized (souls) {
            if (requested > Math.max(0, throughput.applyAsInt(SoulType.Anima)) || requested > anima.getCurrentEV())
                return false;
            if (!souls.extractAll(other, throughput, true)) return false;
            if (simulate) return true;
            if (requested > 0) anima.syphon(AnimaTicket.create((int) requested));
            return souls.extractAll(other, throughput, false);
        }
    }

    public boolean insertAll(Collection<SoulStack> stacks, ToIntFunction<SoulType> throughput,
                             ToIntFunction<SoulType> capacity, boolean simulate) {
        long requested = requestedAnima(stacks);
        List<SoulStack> other = otherSouls(stacks);
        synchronized (souls) {
            if (requested > Math.max(0, throughput.applyAsInt(SoulType.Anima))) return false;
            if (requested > 0 && (long) anima.getCurrentEV() + requested > capacity.applyAsInt(SoulType.Anima))
                return false;
            if (!souls.insertAll(other, throughput, capacity, true)) return false;
            if (simulate) return true;
            if (requested > 0) anima.add(AnimaTicket.create((int) requested), capacity.applyAsInt(SoulType.Anima));
            return souls.insertAll(other, throughput, capacity, false);
        }
    }

    private static long requestedAnima(Collection<SoulStack> stacks) {
        return stacks.stream().filter(stack -> stack != null && !stack.isEmpty() && stack.type() == SoulType.Anima)
                .mapToLong(SoulStack::amount).sum();
    }

    private static List<SoulStack> otherSouls(Collection<SoulStack> stacks) {
        return stacks.stream().filter(stack -> stack != null && stack.type() != SoulType.Anima).toList();
    }
}
