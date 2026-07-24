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
import dev.ftb.mods.ftblibrary.ui.Button;
import dev.ftb.mods.ftblibrary.ui.ContextMenu;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestScreen;
import dev.ftb.mods.ftbquests.client.gui.quests.TooltipContextMenuItem;
import dev.ftb.mods.ftbquests.net.EditObjectMessage;
import dev.ftb.mods.ftbquests.quest.Quest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;

public final class DependencyLineMenus {

    private DependencyLineMenus() {}

    public static List<ContextMenuItem> append(List<ContextMenuItem> original, QuestScreen screen,
                                               Quest dependent) {
        List<ContextMenuItem> menu = new ArrayList<>(original);
        menu.add(ContextMenuItem.SEPARATOR);
        menu.add(buildRoot(screen, dependent));
        return menu;
    }

    public static List<ContextMenuItem> appendSelected(List<ContextMenuItem> original, QuestScreen screen,
                                                       Quest dependent) {
        Collection<Quest> selected = screen.getSelectedQuests();
        if (selected.size() < 2) return append(original, screen, dependent);

        List<ContextMenuItem> menu = new ArrayList<>(original);
        menu.add(ContextMenuItem.SEPARATOR);
        menu.add(buildSelectionRoot(screen, selected));
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
        return boundedSubMenu(
                Component.translatable("cosmiccore.ftbquests.dependency_lines"),
                Icons.ART,
                entries);
    }

    private static ContextMenuItem buildSelectionRoot(QuestScreen screen, Collection<Quest> selected) {
        List<SelectedDependencyEdge> edges = collectSelectedEdges(selected);
        List<ContextMenuItem> entries = new ArrayList<>();
        if (edges.isEmpty()) {
            entries.add(new TooltipContextMenuItem(
                    Component.translatable("cosmiccore.ftbquests.dependency_lines.paint.none")
                            .withStyle(ChatFormatting.GRAY),
                    Icons.INFO_GRAY,
                    null,
                    Component.translatable("cosmiccore.ftbquests.dependency_lines.paint.scope")
                            .withStyle(ChatFormatting.DARK_GRAY)));
        } else {
            long styleCount = edges.stream()
                    .map(SelectedDependencyEdge::settings)
                    .map(DependencyLineSettings::asset)
                    .distinct()
                    .count();
            entries.add(new TooltipContextMenuItem(
                    Component.translatable(
                            "cosmiccore.ftbquests.dependency_lines.paint.summary",
                            edges.size(),
                            selected.size(),
                            styleCount)
                            .withStyle(ChatFormatting.GRAY),
                    Icons.INFO_GRAY,
                    null,
                    Component.translatable("cosmiccore.ftbquests.dependency_lines.paint.scope")
                            .withStyle(ChatFormatting.DARK_GRAY)));
            entries.add(ContextMenuItem.SEPARATOR);
            entries.add(buildSelectionPreset(
                    screen,
                    edges,
                    "cosmiccore.ftbquests.dependency_lines.asset.main_questline",
                    DependencyLineSettings.MAIN_QUESTLINE_ASSET));
            entries.add(buildSelectionPreset(
                    screen,
                    edges,
                    "cosmiccore.ftbquests.dependency_lines.asset.offroad",
                    DependencyLineSettings.OFFROAD_ASSET));
            entries.add(new ContextMenuItem(
                    Component.translatable("cosmiccore.ftbquests.dependency_lines.asset.choose"),
                    Icons.ART,
                    button -> openSelectionAssetPicker(screen, edges)));
            entries.add(new ContextMenuItem(
                    Component.translatable("cosmiccore.ftbquests.dependency_lines.asset.default"),
                    edges.stream().allMatch(edge -> edge.settings().asset().isEmpty()) ?
                            Icons.ACCEPT : Icons.ART,
                    button -> updateSelectionAsset(screen, edges, "")));
            entries.add(ContextMenuItem.SEPARATOR);
            entries.add(new ContextMenuItem(
                    Component.translatable("cosmiccore.ftbquests.dependency_lines.paint.show"),
                    Icons.VISIBILITY_SHOW,
                    button -> updateSelection(
                            screen,
                            edges,
                            settings -> new DependencyLineSettings(true, settings.asset()))));
            entries.add(new ContextMenuItem(
                    Component.translatable("cosmiccore.ftbquests.dependency_lines.paint.hide"),
                    Icons.VISIBILITY_HIDE,
                    button -> updateSelection(
                            screen,
                            edges,
                            settings -> new DependencyLineSettings(false, settings.asset()))));
        }
        return boundedSubMenu(
                Component.translatable("cosmiccore.ftbquests.dependency_lines.paint"),
                Icons.ART,
                entries);
    }

    private static List<SelectedDependencyEdge> collectSelectedEdges(Collection<Quest> selected) {
        Set<Long> selectedIds = selected.stream().map(quest -> quest.id).collect(java.util.stream.Collectors.toSet());
        return selected.stream()
                .sorted(Comparator.comparingLong(quest -> quest.id))
                .flatMap(dependent -> dependent.streamDependencies()
                        .filter(Quest.class::isInstance)
                        .map(Quest.class::cast)
                        .filter(dependency -> selectedIds.contains(dependency.id))
                        .map(dependency -> new SelectedDependencyEdge(
                                dependent,
                                dependency.id,
                                extension(dependent).cosmiccore$getDependencyLineSettings(dependency.id))))
                .distinct()
                .toList();
    }

    private static ContextMenuItem buildSelectionPreset(QuestScreen screen, List<SelectedDependencyEdge> edges,
                                                        String name, String asset) {
        return new ContextMenuItem(
                Component.translatable(name),
                edges.stream().allMatch(edge -> edge.settings().asset().equals(asset)) ?
                        Icons.ACCEPT : Icon.getIcon(asset),
                button -> updateSelectionAsset(screen, edges, asset));
    }

    private static void openSelectionAssetPicker(QuestScreen screen, List<SelectedDependencyEdge> edges) {
        ImageResourceConfig config = new ImageResourceConfig();
        List<String> assets = edges.stream()
                .map(SelectedDependencyEdge::settings)
                .map(DependencyLineSettings::asset)
                .distinct()
                .toList();
        ResourceLocation current = assets.size() == 1 && !assets.getFirst().isEmpty() ?
                ResourceLocation.tryParse(assets.getFirst()) : ImageResourceConfig.NONE;
        config.setValue(current == null ? ImageResourceConfig.NONE : current);
        new SelectImageResourceScreen(config, accepted -> {
            if (accepted) {
                ResourceLocation selected = config.getValue();
                String asset = ImageResourceConfig.NONE.equals(selected) ? "" : selected.toString();
                updateSelectionAsset(screen, edges, asset);
            }
            screen.openGui();
        }).openGui();
    }

    private static void updateSelectionAsset(QuestScreen screen, List<SelectedDependencyEdge> edges, String asset) {
        updateSelection(
                screen,
                edges,
                settings -> new DependencyLineSettings(settings.visible(), asset));
    }

    private static void updateSelection(QuestScreen screen, List<SelectedDependencyEdge> edges,
                                        UnaryOperator<DependencyLineSettings> operation) {
        Set<Quest> changed = new LinkedHashSet<>();
        for (SelectedDependencyEdge edge : edges) {
            DependencyLineSettings current = extension(edge.dependent())
                    .cosmiccore$getDependencyLineSettings(edge.dependencyId());
            DependencyLineSettings updated = operation.apply(current);
            if (!current.equals(updated)) {
                extension(edge.dependent()).cosmiccore$setDependencyLineSettings(edge.dependencyId(), updated);
                changed.add(edge.dependent());
            }
        }
        changed.forEach(EditObjectMessage::sendToServer);
        if (!changed.isEmpty()) screen.questPanel.refreshWidgets();
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
        return boundedSubMenu(title, destination.getIcon(), entries);
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
        return boundedSubMenu(
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

    private static ContextMenuItem boundedSubMenu(Component title, Icon icon, List<ContextMenuItem> entries) {
        return new ContextMenuItem(title, icon, button -> openBoundedSubMenu(button, entries)).setCloseMenu(false);
    }

    private static void openBoundedSubMenu(Button button, List<ContextMenuItem> entries) {
        ContextMenu menu = new ContextMenu(button.getParent(), entries);
        button.getGui().openContextMenu(menu);

        int margin = 2;
        int screenWidth = button.getWindow().getGuiScaledWidth();
        int screenHeight = button.getWindow().getGuiScaledHeight();
        int right = button.getX() + button.width;
        int left = button.getX() - menu.width;
        int absoluteX = right + menu.width <= screenWidth - margin ? right : left;
        int absoluteY = button.getY();
        absoluteX = Math.max(margin, Math.min(absoluteX, Math.max(margin, screenWidth - menu.width - margin)));
        absoluteY = Math.max(margin, Math.min(absoluteY, Math.max(margin, screenHeight - menu.height - margin)));
        menu.setPos(absoluteX - button.getParent().getX(), absoluteY - button.getParent().getY());
    }

    private static QuestDependencyLineExtension extension(Quest quest) {
        return (QuestDependencyLineExtension) (Object) quest;
    }

    private record SelectedDependencyEdge(Quest dependent, long dependencyId, DependencyLineSettings settings) {}
}
