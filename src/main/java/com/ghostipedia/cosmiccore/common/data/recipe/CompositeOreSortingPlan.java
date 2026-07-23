package com.ghostipedia.cosmiccore.common.data.recipe;

import com.gregtechceu.gtceu.api.data.tag.TagPrefix;

import java.util.List;

import static com.ghostipedia.cosmiccore.api.data.CosmicTagPrefix.atomicallyPurifiedOreChunk;
import static com.ghostipedia.cosmiccore.api.data.CosmicTagPrefix.crystallizedOreChunk;
import static com.ghostipedia.cosmiccore.api.data.CosmicTagPrefix.flocculatedOre;
import static com.ghostipedia.cosmiccore.api.data.CosmicTagPrefix.powderizedOre;
import static com.gregtechceu.gtceu.api.data.tag.TagPrefix.crushedPurified;

public final class CompositeOreSortingPlan {

    public static final int SORT_EUT = 2;
    public static final int SORT_TIME_PER_TYPE = 300;
    public static final int SORTER_IO_CAP = 6;

    private static final List<SortStage> STAGES = List.of(
            new SortStage(crushedPurified, 2, 1, "sort_purified_"),
            new SortStage(powderizedOre, 3, 1, "sort_powder_"),
            new SortStage(flocculatedOre, 4, 2, "sort_flocculated_"),
            new SortStage(crystallizedOreChunk, 5, 2, "sort_crystallized_"),
            new SortStage(atomicallyPurifiedOreChunk, SORTER_IO_CAP, 3, "sort_atom_purified_"));

    private CompositeOreSortingPlan() {}

    public static List<SortStage> stages() {
        return STAGES;
    }

    public static int amountFor(int index) {
        return switch (index) {
            case 0 -> 4;
            case 1 -> 2;
            default -> 1;
        };
    }

    public record SortStage(TagPrefix inputForm, int typeCount, int yieldMultiplier, String recipeNamePrefix) {

        public int firstOutputIndex() {
            int stageIndex = STAGES.indexOf(this);
            return stageIndex <= 0 ? 0 : STAGES.get(stageIndex - 1).typeCount();
        }

        public int outputAmount(int index) {
            return amountFor(index) * yieldMultiplier;
        }
    }
}
