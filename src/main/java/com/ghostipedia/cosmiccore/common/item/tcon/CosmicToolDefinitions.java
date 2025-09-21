package com.ghostipedia.cosmiccore.common.item.tcon;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import slimeknights.tconstruct.library.tools.definition.ToolDefinition;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CosmicToolDefinitions {

    public static final ToolDefinition WIRE_CUTTERS = ToolDefinition.create(CosmicTinkerTools.wireCutter);
    public static final ToolDefinition WRENCHES = ToolDefinition.create(CosmicTinkerTools.wrench);
    public static final ToolDefinition FILES = ToolDefinition.create(CosmicTinkerTools.file);
    public static final ToolDefinition SAWS = ToolDefinition.create(CosmicTinkerTools.saw);
    public static final ToolDefinition DRILLS = ToolDefinition.create(CosmicTinkerTools.drill);
    public static final ToolDefinition SCREWDRIVERS = ToolDefinition.create(CosmicTinkerTools.screwdriver);
    public static final ToolDefinition SOFT_MALLETS = ToolDefinition.create(CosmicTinkerTools.softMallet);
    public static final ToolDefinition PLUNGERS = ToolDefinition.create(CosmicTinkerTools.plunger);
    public static final ToolDefinition CROWBARS = ToolDefinition.create(CosmicTinkerTools.crowbar);
}
