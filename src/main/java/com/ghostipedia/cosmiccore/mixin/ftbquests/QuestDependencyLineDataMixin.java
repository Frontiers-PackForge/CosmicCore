package com.ghostipedia.cosmiccore.mixin.ftbquests;

import com.ghostipedia.cosmiccore.common.compat.ftbquests.DependencyLineSettings;
import com.ghostipedia.cosmiccore.common.compat.ftbquests.DependencyLineStyle;
import com.ghostipedia.cosmiccore.common.compat.ftbquests.QuestDependencyLineExtension;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.RegistryFriendlyByteBuf;

import dev.ftb.mods.ftbquests.quest.Quest;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.TreeMap;

@Mixin(value = Quest.class, remap = false)
public class QuestDependencyLineDataMixin implements QuestDependencyLineExtension {

    @Unique
    private final Map<Long, DependencyLineSettings> cosmiccore$dependencyLineSettings = new TreeMap<>();

    @Override
    public DependencyLineSettings cosmiccore$getDependencyLineSettings(long dependencyId) {
        return cosmiccore$dependencyLineSettings.getOrDefault(dependencyId, DependencyLineSettings.DEFAULT);
    }

    @Override
    public void cosmiccore$setDependencyLineSettings(long dependencyId, DependencyLineSettings settings) {
        if (settings.isDefault()) {
            cosmiccore$dependencyLineSettings.remove(dependencyId);
        } else {
            cosmiccore$dependencyLineSettings.put(dependencyId, settings);
        }
    }

    @Inject(method = "writeData", at = @At("TAIL"))
    private void cosmiccore$writeDependencyLineSettings(CompoundTag tag, HolderLookup.Provider provider,
                                                        CallbackInfo ci) {
        cosmiccore$pruneDependencyLineSettings();
        if (cosmiccore$dependencyLineSettings.isEmpty()) return;
        ListTag list = new ListTag();
        cosmiccore$dependencyLineSettings.forEach((dependencyId, settings) -> {
            CompoundTag entry = new CompoundTag();
            entry.putLong("dependency", dependencyId);
            entry.putBoolean("visible", settings.visible());
            entry.putString("style", settings.style().name().toLowerCase());
            list.add(entry);
        });
        tag.put("cosmiccore_dependency_lines", list);
    }

    @Inject(method = "readData", at = @At("TAIL"))
    private void cosmiccore$readDependencyLineSettings(CompoundTag tag, HolderLookup.Provider provider,
                                                       CallbackInfo ci) {
        cosmiccore$dependencyLineSettings.clear();
        ListTag list = tag.getList("cosmiccore_dependency_lines", CompoundTag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            long dependencyId = entry.getLong("dependency");
            DependencyLineSettings settings = new DependencyLineSettings(
                    !entry.contains("visible") || entry.getBoolean("visible"),
                    DependencyLineStyle.byName(entry.getString("style")));
            cosmiccore$setDependencyLineSettings(dependencyId, settings);
        }
    }

    @Inject(method = "writeNetData", at = @At("TAIL"))
    private void cosmiccore$writeDependencyLineSettingsNetwork(RegistryFriendlyByteBuf buffer, CallbackInfo ci) {
        cosmiccore$pruneDependencyLineSettings();
        buffer.writeVarInt(cosmiccore$dependencyLineSettings.size());
        cosmiccore$dependencyLineSettings.forEach((dependencyId, settings) -> {
            buffer.writeLong(dependencyId);
            buffer.writeBoolean(settings.visible());
            buffer.writeEnum(settings.style());
        });
    }

    @Inject(method = "readNetData", at = @At("TAIL"))
    private void cosmiccore$readDependencyLineSettingsNetwork(RegistryFriendlyByteBuf buffer, CallbackInfo ci) {
        cosmiccore$dependencyLineSettings.clear();
        int count = buffer.readVarInt();
        for (int i = 0; i < count; i++) {
            cosmiccore$setDependencyLineSettings(
                    buffer.readLong(),
                    new DependencyLineSettings(buffer.readBoolean(), buffer.readEnum(DependencyLineStyle.class)));
        }
    }

    @Unique
    private void cosmiccore$pruneDependencyLineSettings() {
        Quest quest = (Quest) (Object) this;
        cosmiccore$dependencyLineSettings.keySet().removeIf(dependencyId -> quest.streamDependencies()
                .noneMatch(dependency -> dependency.id == dependencyId));
    }
}
