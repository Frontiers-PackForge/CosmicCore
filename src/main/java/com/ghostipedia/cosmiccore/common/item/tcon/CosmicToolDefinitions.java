package com.ghostipedia.cosmiccore.common.item.tcon;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CosmicToolDefinitions {

    public static final ToolDefinition WIRE_CUTTERS = ToolDefinition.create(CosmicTinkerTools.wireCutter);
    public static final ToolDefinition WRENCHES = ToolDefinition.create(CosmicTinkerTools.wrench);
}
