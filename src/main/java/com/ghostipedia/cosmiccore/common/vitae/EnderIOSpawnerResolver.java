package com.ghostipedia.cosmiccore.common.vitae;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class EnderIOSpawnerResolver {

    private static final ResourceLocation POWERED_SPAWNER = ResourceLocation.fromNamespaceAndPath(
            "enderio", "powered_spawner");
    private static final ResourceLocation BROKEN_SPAWNER = ResourceLocation.fromNamespaceAndPath(
            "enderio", "broken_spawner");
    private static final ResourceLocation SOUL_VIAL = ResourceLocation.fromNamespaceAndPath("enderio", "soul_vial");
    private static final ResourceLocation SOUL_COMPONENT = ResourceLocation.fromNamespaceAndPath("enderio", "soul");
    private static final String SOUL_CLASS = "com.enderio.enderio.api.soul.Soul";
    private static final ConcurrentHashMap<Class<?>, Optional<Method>> ENTITY_ID_METHODS = new ConcurrentHashMap<>();

    private EnderIOSpawnerResolver() {}

    public static boolean isAttunedPoweredSpawner(ItemStack stack) {
        return resolve(stack).isPresent();
    }

    public static Optional<ResourceLocation> resolveSpawnerSoul(ItemStack stack) {
        return isSpawnerItem(stack) ? resolveSoul(stack) : Optional.empty();
    }

    public static Optional<ResourceLocation> resolveSoulVial(ItemStack stack) {
        if (stack.isEmpty() || !SOUL_VIAL.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
            return Optional.empty();
        }
        return resolveSoul(stack);
    }

    public static ItemStack createAttunedPoweredSpawner(ResourceLocation entity) {
        return createAttunedStack(POWERED_SPAWNER, entity);
    }

    public static ItemStack createAttunedBrokenSpawner(ResourceLocation entity) {
        return createAttunedStack(BROKEN_SPAWNER, entity);
    }

    public static ItemStack createFilledSoulVial(ResourceLocation entity) {
        return createAttunedStack(SOUL_VIAL, entity);
    }

    private static ItemStack createAttunedStack(ResourceLocation itemId, ResourceLocation entity) {
        Item item = BuiltInRegistries.ITEM.get(itemId);
        ItemStack stack = item.getDefaultInstance();
        DataComponentType<?> componentType = BuiltInRegistries.DATA_COMPONENT_TYPE.get(SOUL_COMPONENT);
        if (componentType == null) return stack;
        createSoul(entity).ifPresent(soul -> setComponent(stack, componentType, soul));
        return stack;
    }

    private static boolean isSpawnerItem(ItemStack stack) {
        if (stack.isEmpty()) return false;
        ResourceLocation item = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return POWERED_SPAWNER.equals(item) || BROKEN_SPAWNER.equals(item);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Optional<ResourceLocation> resolve(ItemStack stack) {
        if (stack.isEmpty() || !POWERED_SPAWNER.equals(BuiltInRegistries.ITEM.getKey(stack.getItem()))) {
            return Optional.empty();
        }
        return resolveSoul(stack);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Optional<ResourceLocation> resolveSoul(ItemStack stack) {
        DataComponentType componentType = BuiltInRegistries.DATA_COMPONENT_TYPE.get(SOUL_COMPONENT);
        if (componentType == null) return Optional.empty();
        Object soul = stack.get(componentType);
        if (soul == null) return Optional.empty();
        Optional<Method> method = ENTITY_ID_METHODS.computeIfAbsent(soul.getClass(), type -> {
            try {
                return Optional.of(type.getMethod("entityTypeId"));
            } catch (NoSuchMethodException ignored) {
                return Optional.empty();
            }
        });
        if (method.isEmpty()) return Optional.empty();
        try {
            Object id = method.get().invoke(soul);
            return id instanceof ResourceLocation resourceLocation ? Optional.of(resourceLocation) : Optional.empty();
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            return Optional.empty();
        }
    }

    private static Optional<Object> createSoul(ResourceLocation entity) {
        try {
            Class<?> soulClass = Class.forName(SOUL_CLASS);
            return Optional.ofNullable(soulClass.getMethod("of", ResourceLocation.class).invoke(null, entity));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |
                 InvocationTargetException ignored) {
            return Optional.empty();
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static void setComponent(ItemStack stack, DataComponentType componentType, Object value) {
        stack.set(componentType, value);
    }
}
