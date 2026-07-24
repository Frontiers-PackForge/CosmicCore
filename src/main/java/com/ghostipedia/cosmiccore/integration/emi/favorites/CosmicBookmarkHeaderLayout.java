package com.ghostipedia.cosmiccore.integration.emi.favorites;

import net.minecraft.util.Mth;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.registry.EmiExclusionAreas;
import dev.emi.emi.screen.EmiScreenBase;
import dev.emi.emi.screen.EmiScreenManager.SidebarPanel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record CosmicBookmarkHeaderLayout(
                                         Bounds header,
                                         Bounds cycle,
                                         Bounds groupPrevious,
                                         Bounds groupLabel,
                                         Bounds groupNext,
                                         Bounds pagePrevious,
                                         Bounds pageLabel,
                                         Bounds pageNext,
                                         Bounds groupAction) {

    private static final int HEADER_HEIGHT = 18;
    private static final int CYCLE_SIZE = 16;
    private static final int MIN_ARROW_SIZE = 6;
    private static final int MAX_ARROW_SIZE = 16;

    public static @Nullable CosmicBookmarkHeaderLayout create(SidebarPanel panel) {
        if (!panel.header || panel.space == null) return null;
        Bounds header = new Bounds(panel.space.tx, panel.space.ty - HEADER_HEIGHT, panel.space.tw * 18, HEADER_HEIGHT);
        Bounds usable = widestGap(header, EmiExclusionAreas.getExclusion(EmiScreenBase.getCurrent()));
        if (usable.width() < CYCLE_SIZE * 2 + MIN_ARROW_SIZE * 4) return null;

        Bounds cycle = new Bounds(usable.x(), usable.y() + 1, CYCLE_SIZE, CYCLE_SIZE);
        Bounds groupAction = new Bounds(usable.right() - CYCLE_SIZE, usable.y() + 1, CYCLE_SIZE, CYCLE_SIZE);
        int navX = cycle.right() + 2;
        int navWidth = groupAction.left() - 2 - navX;
        if (navWidth < MIN_ARROW_SIZE * 4) {
            navX = cycle.right();
            navWidth = groupAction.left() - navX;
        }
        int groupWidth = navWidth / 2;
        int pageWidth = navWidth - groupWidth;
        int arrowSize = Mth.clamp(Math.min(groupWidth, pageWidth) / 3, MIN_ARROW_SIZE, MAX_ARROW_SIZE);
        arrowSize = Math.min(arrowSize, Math.min(groupWidth, pageWidth) / 2);
        int arrowY = usable.y() + (HEADER_HEIGHT - arrowSize) / 2;

        Bounds groupPrevious = new Bounds(navX, arrowY, arrowSize, arrowSize);
        Bounds groupNext = new Bounds(navX + groupWidth - arrowSize, arrowY, arrowSize, arrowSize);
        Bounds groupLabel = labelBounds(navX, usable.y(), groupWidth, arrowSize);

        int pageX = navX + groupWidth;
        Bounds pagePrevious = new Bounds(pageX, arrowY, arrowSize, arrowSize);
        Bounds pageNext = new Bounds(pageX + pageWidth - arrowSize, arrowY, arrowSize, arrowSize);
        Bounds pageLabel = labelBounds(pageX, usable.y(), pageWidth, arrowSize);
        return new CosmicBookmarkHeaderLayout(
                usable,
                cycle,
                groupPrevious,
                groupLabel,
                groupNext,
                pagePrevious,
                pageLabel,
                pageNext,
                groupAction);
    }

    private static Bounds labelBounds(int x, int y, int width, int arrowSize) {
        int labelX = x + arrowSize + 1;
        return new Bounds(labelX, y, Math.max(0, width - arrowSize * 2 - 2), HEADER_HEIGHT);
    }

    private static Bounds widestGap(Bounds header, List<Bounds> exclusions) {
        List<Bounds> overlaps = new ArrayList<>();
        for (Bounds exclusion : exclusions) {
            if (exclusion.bottom() <= header.top() || exclusion.top() >= header.bottom()) continue;
            int left = Math.max(header.left(), exclusion.left() - 1);
            int right = Math.min(header.right(), exclusion.right() + 1);
            if (right > left) overlaps.add(new Bounds(left, header.y(), right - left, header.height()));
        }
        overlaps.sort(Comparator.comparingInt(Bounds::left));

        List<Bounds> gaps = new ArrayList<>();
        int cursor = header.left();
        for (Bounds overlap : overlaps) {
            if (overlap.left() > cursor) {
                gaps.add(new Bounds(cursor, header.y(), overlap.left() - cursor, header.height()));
            }
            cursor = Math.max(cursor, overlap.right());
        }
        if (cursor < header.right()) {
            gaps.add(new Bounds(cursor, header.y(), header.right() - cursor, header.height()));
        }
        if (gaps.isEmpty()) return Bounds.EMPTY;

        int center = header.left() + header.width() / 2;
        return gaps.stream()
                .max(Comparator.comparingInt(Bounds::width)
                        .thenComparingInt(gap -> gap.contains(center, gap.y()) ? 1 : 0)
                        .thenComparingInt(gap -> -Math.abs((gap.left() + gap.right()) / 2 - center)))
                .orElse(Bounds.EMPTY);
    }
}
