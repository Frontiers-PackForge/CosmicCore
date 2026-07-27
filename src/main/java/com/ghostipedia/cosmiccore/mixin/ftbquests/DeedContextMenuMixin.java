package com.ghostipedia.cosmiccore.mixin.ftbquests;

import com.ghostipedia.cosmiccore.common.compat.ftbquests.DeedTask;
import com.ghostipedia.cosmiccore.common.config.CosmicCoreConfig;

import net.minecraft.network.chat.Component;

import dev.ftb.mods.ftblibrary.ui.BaseScreen;
import dev.ftb.mods.ftblibrary.ui.ContextMenuItem;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.gui.ContextMenuBuilder;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.theme.property.ThemeProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = ContextMenuBuilder.class, remap = false)
public abstract class DeedContextMenuMixin {

    @Shadow
    @Final
    private QuestObjectBase object;

    @Inject(method = "build", at = @At("RETURN"), cancellable = true)
    private void cosmiccore$restoreDeedTaskDeletion(BaseScreen screen,
                                                    CallbackInfoReturnable<List<ContextMenuItem>> cir) {
        if (!(object instanceof DeedTask) || !CosmicCoreConfig.devVisor()) return;
        Component deleteTitle = Component.translatable("selectServer.delete");
        List<ContextMenuItem> items = cir.getReturnValue();
        for (ContextMenuItem item : items) {
            if (item.getTitle().getString().equals(deleteTitle.getString())) return;
        }
        ContextMenuItem delete = new ContextMenuItem(deleteTitle, ThemeProperties.DELETE_ICON.get(),
                button -> ClientQuestFile.INSTANCE.deleteObject(object.id));
        delete.setYesNoText(Component.translatable("delete_item", object.getTitle()));
        ArrayList<ContextMenuItem> restored = new ArrayList<>(items);
        restored.add(Math.min(2, restored.size()), delete);
        cir.setReturnValue(List.copyOf(restored));
    }
}
