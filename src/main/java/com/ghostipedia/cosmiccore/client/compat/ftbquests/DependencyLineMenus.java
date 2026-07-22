package com.ghostipedia.cosmiccore.client.compat.ftbquests;

import com.ghostipedia.cosmiccore.common.compat.ftbquests.DependencyLineSettings;
import com.ghostipedia.cosmiccore.common.compat.ftbquests.QuestDependencyLineExtension;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import dev.ftb.mods.ftblibrary.config.ImageResourceConfig;
import dev.ftb.mods.ftblibrary.config.ui.resource.SelectImageResourceScreen;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.icon.Icons;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.client.gui.quests.TooltipContextMenuItem;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.quest.Quest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class DependencyLineMenus {

    private DependencyLineMenus() {}

    public static List<ContextMenuItem> append(List<ContextMenuItem> original, QuestScreen screen,
                                               Quest dependent) {
        List<ContextMenuItem> menu = new ArrayList<>(original);
        menu.add(ContextMenuItem.SEPARATOR);
        menu.add(buildRoot(screen, dependent));
        return menu;
    }

    public static List<ContextMenuItem> appendTop(Iterable<ContextMenuItem> original, QuestScreen screen,
                                                  Quest dependent) {
        List<ContextMenuItem> menu = new ArrayList<>();
        original.forEach(menu::add);
        menu.add(buildRoot(screen, dependent));
        return menu;
    }

    private static ContextMenuItem buildRoot(QuestScreen screen, Quest dependent) {
        List<Quest> dependencies = dependent.streamDependencies()
                .filter(Quest.class::isInstance)
                .map(Quest.class::cast)
                .sorted(Comparator.comparing(quest -> quest.getTitle().getString()))
                .toList();
        List<Quest> dependants = dependent.getDependants().stream()
                .filter(Quest.class::isInstance)
                .map(Quest.class::cast)
                .sorted(Comparator.comparing(quest -> quest.getTitle().getString()))
                .toList();
        List<ContextMenuItem> entries = new ArrayList<>();
        if (dependencies.isEmpty() && dependants.isEmpty()) {
            entries.add(new ContextMenuItem(
                    Component.translatable("cosmiccore.ftbquests.dependency_lines.none")
                            .withStyle(ChatFormatting.GRAY),
                    Icons.INFO_GRAY,
                    null));
        } else {
            if (!dependencies.isEmpty()) {
                entries.add(ContextMenuItem.title(
                        Component.translatable("cosmiccore.ftbquests.dependency_lines.dependencies")
                                .withStyle(ChatFormatting.GRAY)));
                dependencies.forEach(dependency -> entries.add(
                        buildDependency(screen, dependent, dependency, dependency)));
            }
            if (!dependants.isEmpty()) {
                if (!entries.isEmpty()) entries.add(ContextMenuItem.SEPARATOR);
                entries.add(ContextMenuItem.title(
                        Component.translatable("cosmiccore.ftbquests.dependency_lines.dependants")
                                .withStyle(ChatFormatting.GRAY)));
                dependants.forEach(dependant -> entries.add(
                        buildDependency(screen, dependant, dependent, dependant)));
            }
        }
        long taskDependencyCount = dependent.streamDependencies()
                .filter(dependency -> !(dependency instanceof Quest))
                .count();
        if (taskDependencyCount > 0) {
            entries.add(ContextMenuItem.SEPARATOR);
            entries.add(new TooltipContextMenuItem(
                    Component.translatable("cosmiccore.ftbquests.dependency_lines.task_count", taskDependencyCount)
                            .withStyle(ChatFormatting.DARK_GRAY),
                    Icons.INFO_GRAY,
                    null,
                    Component.translatable("cosmiccore.ftbquests.dependency_lines.task_hint")
                            .withStyle(ChatFormatting.GRAY)));
        }
        return ContextMenuItem.subMenu(
                Component.translatable("cosmiccore.ftbquests.dependency_lines"),
                Icons.ART,
                entries);
    }

    private static ContextMenuItem buildDependency(QuestScreen screen, Quest dependent, Quest dependency,
                                                   Quest destination) {
        DependencyLineSettings settings = extension(dependent)
                .cosmiccore$getDependencyLineSettings(dependency.id);
        List<ContextMenuItem> entries = new ArrayList<>();
        entries.add(new ContextMenuItem(
                Component.translatable(settings.visible() ? "cosmiccore.ftbquests.dependency_lines.hide" :
                        "cosmiccore.ftbquests.dependency_lines.show"),
                settings.visible() ? Icons.VISIBILITY_HIDE : Icons.VISIBILITY_SHOW,
                button -> update(screen, dependent, dependency.id,
                        new DependencyLineSettings(!settings.visible(), settings.asset()))));
        entries.add(buildAssetMenu(screen, dependent, dependency.id, settings));
        entries.add(ContextMenuItem.SEPARATOR);
        entries.add(new ContextMenuItem(
                Component.translatable("cosmiccore.ftbquests.dependency_lines.jump"),
                Icons.MAP,
                button -> {
                    screen.scrollTo(destination);
                    screen.viewQuest(destination);
                }));

        Component title = Component.translatable(
                "cosmiccore.ftbquests.dependency_lines.entry",
                destination.getQuestChapter().getTitle(),
                destination.getTitle());
        return ContextMenuItem.subMenu(title, destination.getIcon(), entries);
    }

    private static ContextMenuItem buildAssetMenu(QuestScreen screen, Quest dependent, long dependencyId,
                                                  DependencyLineSettings settings) {
        List<ContextMenuItem> assets = new ArrayList<>();
        assets.add(new ContextMenuItem(
                Component.translatable("cosmiccore.ftbquests.dependency_lines.asset.default"),
                settings.asset().isEmpty() ? Icons.ACCEPT : Icons.ACCEPT_GRAY,
                button -> update(screen, dependent, dependencyId,
                        new DependencyLineSettings(settings.visible(), ""))));
        assets.add(buildPreset(
                screen,
                dependent,
                dependencyId,
                settings,
                "cosmiccore.ftbquests.dependency_lines.asset.main_questline",
                DependencyLineSettings.MAIN_QUESTLINE_ASSET));
        assets.add(buildPreset(
                screen,
                dependent,
                dependencyId,
                settings,
                "cosmiccore.ftbquests.dependency_lines.asset.offroad",
                DependencyLineSettings.OFFROAD_ASSET));
        assets.add(ContextMenuItem.SEPARATOR);
        Component current = settings.asset().isEmpty() ?
                Component.translatable("cosmiccore.ftbquests.dependency_lines.asset.default") :
                Component.literal(settings.asset());
        assets.add(new TooltipContextMenuItem(
                Component.translatable("cosmiccore.ftbquests.dependency_lines.asset.choose"),
                settings.assetLocation().<Icon>map(Icon::getIcon).orElse(Icons.ART),
                button -> openAssetPicker(screen, dependent, dependencyId, settings),
                Component.translatable("cosmiccore.ftbquests.dependency_lines.asset.current", current)
                        .withStyle(ChatFormatting.GRAY),
                Component.translatable("cosmiccore.ftbquests.dependency_lines.asset.hint")
                        .withStyle(ChatFormatting.DARK_GRAY)));
        return ContextMenuItem.subMenu(
                Component.translatable("cosmiccore.ftbquests.dependency_lines.asset"),
                settings.assetLocation().<Icon>map(Icon::getIcon).orElse(Icons.ART),
                assets);
    }

    private static ContextMenuItem buildPreset(QuestScreen screen, Quest dependent, long dependencyId,
                                               DependencyLineSettings settings, String name, String asset) {
        return new ContextMenuItem(
                Component.translatable(name),
                settings.asset().equals(asset) ? Icons.ACCEPT : Icon.getIcon(asset),
                button -> update(screen, dependent, dependencyId,
                        new DependencyLineSettings(settings.visible(), asset)));
    }

    private static void openAssetPicker(QuestScreen screen, Quest dependent, long dependencyId,
                                        DependencyLineSettings settings) {
        ImageResourceConfig config = new ImageResourceConfig();
        config.setValue(settings.assetLocation().orElse(ImageResourceConfig.NONE));
        new SelectImageResourceScreen(config, accepted -> {
            if (accepted) {
                ResourceLocation selected = config.getValue();
                String asset = ImageResourceConfig.NONE.equals(selected) ? "" : selected.toString();
                update(screen, dependent, dependencyId, new DependencyLineSettings(settings.visible(), asset));
            }
            screen.openGui();
        }).openGui();
    }

    private static void update(QuestScreen screen, Quest dependent, long dependencyId,
                               DependencyLineSettings settings) {
        extension(dependent).cosmiccore$setDependencyLineSettings(dependencyId, settings);
        EditObjectMessage.sendToServer(dependent);
        screen.questPanel.refreshWidgets();
    }

    private static QuestDependencyLineExtension extension(Quest quest) {
        return (QuestDependencyLineExtension) (Object) quest;
    }
}
