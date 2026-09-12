package com.ghostipedia.cosmiccore.api.machine.activity;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.fluids.FluidStack;

public final class ActivityResource {

    private final String kind;
    private final String id;
    private final ItemStack item;
    private final FluidStack fluid;

    private ActivityResource(String kind, String id, ItemStack item, FluidStack fluid) {
        this.kind = kind;
        this.id = id;
        this.item = item;
        this.fluid = fluid;
    }

    public static ActivityResource item(ItemStack stack) {
        return new ActivityResource("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(),
                stack.copyWithCount(1), FluidStack.EMPTY);
    }

    public static ActivityResource fluid(FluidStack stack) {
        return new ActivityResource("fluid", BuiltInRegistries.FLUID.getKey(stack.getFluid()).toString(),
                ItemStack.EMPTY, stack.copyWithAmount(1));
    }

    public static ActivityResource value(String kind, String id) {
        return new ActivityResource(kind, id, null, null);
    }

    public String kind() {
        return kind;
    }

    public String id() {
        return id;
    }

    public boolean matches(ItemStack stack) {
        return kind.equals("item") && ItemStack.isSameItemSameComponents(item, stack);
    }

    public boolean matches(FluidStack stack) {
        return kind.equals("fluid") && FluidStack.isSameFluidSameComponents(fluid, stack);
    }

    public boolean matches(String kind, String id) {
        return this.kind.equals(kind) && this.id.equals(id);
    }

    public CompoundTag toTag(HolderLookup.Provider lookup) {
        CompoundTag tag = new CompoundTag();
        tag.putString("kind", kind);
        tag.putString("id", id);
        if (item != null && !item.isEmpty()) tag.put("icon", item.save(lookup));
        else if (fluid != null && !fluid.isEmpty()) tag.put("icon", fluid.saveOptional(lookup));
        else tag.put("icon", new CompoundTag());
        return tag;
    }

    public static ActivityResource fromTag(CompoundTag tag, HolderLookup.Provider lookup) {
        String kind = tag.getString("kind");
        return switch (kind) {
            case "item" -> {
                ItemStack stack = ItemStack.parseOptional(lookup, tag.getCompound("icon"));
                yield stack.isEmpty() ? null : item(stack);
            }
            case "fluid" -> {
                FluidStack stack = FluidStack.parseOptional(lookup, tag.getCompound("icon"));
                yield stack.isEmpty() ? null : fluid(stack);
            }
            default -> tag.getString("id").length() > 256 || kind.length() > 64 ? null :
                    value(kind, tag.getString("id"));
        };
    }
}
