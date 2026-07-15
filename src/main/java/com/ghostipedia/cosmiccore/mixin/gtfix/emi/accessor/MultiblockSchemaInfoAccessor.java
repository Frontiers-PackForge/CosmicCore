package com.ghostipedia.cosmiccore.mixin.gtfix.emi.accessor;

import com.gregtechceu.gtceu.api.mui.MultiblockSchemaInfo;
import com.gregtechceu.gtceu.api.multiblock.util.AbstractStructureHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = MultiblockSchemaInfo.class, remap = false)
public interface MultiblockSchemaInfoAccessor {

    @Accessor("structureHelper")
    void cosmiccore$setStructureHelper(AbstractStructureHelper structureHelper);
}
