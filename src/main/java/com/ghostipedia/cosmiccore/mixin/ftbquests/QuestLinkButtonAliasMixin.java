package com.ghostipedia.cosmiccore.mixin.ftbquests;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.ftb.mods.ftbquests.client.gui.quests.QuestLinkButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = QuestLinkButton.class, remap = false)
public class QuestLinkButtonAliasMixin {

    @ModifyExpressionValue(
                           method = "draw",
                           at = @At(
                                    value = "INVOKE",
                                    target = "Ldev/ftb/mods/ftbquests/client/ClientQuestFile;canEdit()Z"))
    private boolean cosmiccore$showAliasBadge(boolean canEdit) {
        return true;
    }
}
