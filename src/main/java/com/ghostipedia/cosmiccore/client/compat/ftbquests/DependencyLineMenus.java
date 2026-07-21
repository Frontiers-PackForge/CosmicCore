package com.ghostipedia.cosmiccore.client.compat.ftbquests;

import com.ghostipedia.cosmiccore.common.compat.ftbquests.DependencyLineSettings;
import com.ghostipedia.cosmiccore.common.compat.ftbquests.DependencyLineStyle;
import com.ghostipedia.cosmiccore.common.compat.ftbquests.QuestDependencyLineExtension;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

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
                        new DependencyLineSettings(!settings.visible(), settings.style()))));
        entries.add(buildStyleMenu(screen, dependent, dependency.id, settings));
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

    private static ContextMenuItem buildStyleMenu(QuestScreen screen, Quest dependent, long dependencyId,
                                                  DependencyLineSettings settings) {
        List<ContextMenuItem> styles = new ArrayList<>();
        for (DependencyLineStyle style : DependencyLineStyle.values()) {
            styles.add(new ContextMenuItem(
                    Component.translatable("cosmiccore.ftbquests.dependency_lines.style." + style.name().toLowerCase()),
                    settings.style() == style ? Icons.ACCEPT : Icons.ACCEPT_GRAY,
                    button -> update(screen, dependent, dependencyId,
                            new DependencyLineSettings(settings.visible(), style))));
        }
        return ContextMenuItem.subMenu(
                Component.translatable("cosmiccore.ftbquests.dependency_lines.style"),
                Icons.ART,
                styles);
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
