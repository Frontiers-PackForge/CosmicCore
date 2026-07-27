package com.ghostipedia.cosmiccore.common.compat.ftbquests;

import com.ghostipedia.cosmiccore.CosmicCore;
import com.ghostipedia.cosmiccore.common.mirror.deed.Deed;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedRegistry;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedTeams;
import com.ghostipedia.cosmiccore.common.mirror.deed.DeedsAPI;
import com.ghostipedia.cosmiccore.common.network.CCoreNetwork;
import com.ghostipedia.cosmiccore.common.network.packet.DeedQuestRequestPacket;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import dev.ftb.mods.ftblibrary.config.ConfigGroup;
import dev.ftb.mods.ftblibrary.config.NameMap;
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.TeamData;
import dev.ftb.mods.ftbquests.quest.task.AbstractBooleanTask;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;

import java.util.List;

public final class DeedTask extends AbstractBooleanTask {

    private static final ResourceLocation INVALID_DEED = CosmicCore.id("invalid");

    private ResourceLocation deedId = INVALID_DEED;
    private Presentation presentation = Presentation.STANDARD;
    private Disclosure disclosure = Disclosure.SEALED;

    public DeedTask(long id, Quest quest) {
        super(id, quest);
    }

    public ResourceLocation deedId() {
        return deedId;
    }

    public Presentation presentation() {
        return presentation;
    }

    public Disclosure disclosure() {
        return disclosure;
    }

    public boolean requirementsComplete(TeamData teamData) {
        if (teamData == null || !teamData.canStartTasks(getQuest())) return false;
        for (Task task : getQuest().getTasks()) {
            if (task == this || task instanceof DeedTask || task.isOptionalForProgression(teamData)) continue;
            if (!teamData.isCompleted(task)) return false;
        }
        return true;
    }

    public void requestPresentation() {
        CCoreNetwork.sendToServer(new DeedQuestRequestPacket(id));
    }

    @Override
    public TaskType getType() {
        return DeedQuestCompat.taskType();
    }

    @Override
    public Component getAltTitle() {
        Deed deed = DeedRegistry.get(deedId);
        if (disclosure == Disclosure.VISIBLE && deed != null) return Component.translatable(deed.nameKey());
        return Component.translatable("cosmiccore.ftbquests.deed.task");
    }

    @Override
    public boolean canSubmit(TeamData teamData, ServerPlayer player) {
        MinecraftServer server = player.getServer();
        return server != null && DeedRegistry.get(deedId) != null &&
                DeedsAPI.isWoven(server, DeedTeams.teamKey(player), deedId);
    }

    @Override
    public boolean checkOnLogin() {
        return true;
    }

    @Override
    public void onButtonClicked(Button button, boolean canClick) {
        if (!canClick) return;
        button.playClickSound();
        requestPresentation();
    }

    @Override
    public void writeData(CompoundTag tag, HolderLookup.Provider provider) {
        super.writeData(tag, provider);
        tag.putString("deed", deedId.toString());
        tag.putString("presentation", presentation.serializedName());
        tag.putString("disclosure", disclosure.serializedName());
    }

    @Override
    public void readData(CompoundTag tag, HolderLookup.Provider provider) {
        super.readData(tag, provider);
        ResourceLocation parsed = ResourceLocation.tryParse(tag.getString("deed"));
        deedId = parsed == null ? INVALID_DEED : parsed;
        presentation = Presentation.fromName(tag.getString("presentation"));
        disclosure = tag.contains("disclosure") ? Disclosure.fromName(tag.getString("disclosure")) :
                presentation == Presentation.ASCENSION ? Disclosure.ASCENSION : Disclosure.SEALED;
    }

    @Override
    public void writeNetData(RegistryFriendlyByteBuf buffer) {
        super.writeNetData(buffer);
        buffer.writeResourceLocation(deedId);
        buffer.writeEnum(presentation);
        buffer.writeEnum(disclosure);
    }

    @Override
    public void readNetData(RegistryFriendlyByteBuf buffer) {
        super.readNetData(buffer);
        deedId = buffer.readResourceLocation();
        presentation = buffer.readEnum(Presentation.class);
        disclosure = buffer.readEnum(Disclosure.class);
    }

    @Override
    public void fillConfigGroup(ConfigGroup config) {
        super.fillConfigGroup(config);
        List<ResourceLocation> deedIds = DeedRegistry.all().stream().map(Deed::id).toList();
        if (deedIds.isEmpty()) deedIds = List.of(INVALID_DEED);
        ResourceLocation defaultDeed = deedIds.getFirst();
        ResourceLocation selectedDeed = deedIds.contains(deedId) ? deedId : defaultDeed;
        NameMap<ResourceLocation> deeds = NameMap.of(defaultDeed, deedIds)
                .id(ResourceLocation::toString)
                .name(id -> {
                    Deed deed = DeedRegistry.get(id);
                    if (deed == null) return Component.literal(id.toString());
                    return Component.translatable(deed.nameKey())
                            .append(Component.literal(" (" + id + ")").withStyle(ChatFormatting.DARK_GRAY));
                })
                .create();
        config.add("deed", new DeedSelectorConfig(deeds), selectedDeed, value -> deedId = value, defaultDeed);
        config.addEnum("presentation", presentation, value -> presentation = value,
                NameMap.of(Presentation.STANDARD, Presentation.values()).create());
        config.addEnum("disclosure", disclosure, value -> disclosure = value,
                NameMap.of(Disclosure.SEALED, Disclosure.values()).create());
    }

    public enum Presentation {

        STANDARD,
        QUEST,
        ASCENSION;

        public boolean grantsWhenReady() {
            return this == STANDARD;
        }

        public String serializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }

        public static Presentation fromName(String name) {
            for (Presentation presentation : values()) {
                if (presentation.serializedName().equalsIgnoreCase(name)) return presentation;
            }
            return STANDARD;
        }
    }

    public enum Disclosure {

        ASCENSION,
        SEALED,
        VISIBLE;

        public String serializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }

        public static Disclosure fromName(String name) {
            for (Disclosure disclosure : values()) {
                if (disclosure.serializedName().equalsIgnoreCase(name)) return disclosure;
            }
            return SEALED;
        }
    }
}
