package com.ghostipedia.cosmiccore.mixin;


import com.ghostipedia.cosmiccore.common.data.CosmicGendustryUpgradeType;
import forestry.core.inventory.IInventoryAdapter;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import thedarkcolour.gendustry.item.EliteGendustryUpgradeType;
import thedarkcolour.gendustry.item.GendustryUpgradeItem;
import thedarkcolour.gendustry.item.GendustryUpgradeType;
import thedarkcolour.gendustry.item.IGendustryUpgradeType;


@Mixin(targets = "thedarkcolour.gendustry.blockentity.IndustrialApiaryBeeModifier", remap = false)
class IndustrialApiaryBeeModifierMixin {


    @Shadow float territory;
    @Shadow float mutation;
    @Shadow float lifespan;
    @Shadow float productivity;
    @Shadow float pollination;
    @Shadow int throttle;
    @Shadow int fertility;
    @Shadow int temperature;
    @Shadow int humidity;
    @Shadow boolean automated;
    @Shadow boolean stabilized;
    @Shadow boolean weatherproof;
    @Shadow boolean lighting;
    @Shadow boolean sky;
    @Shadow boolean nether;
    @Shadow boolean scrubber;
    @Shadow boolean sieve;

    @Shadow
    private void reset() {
        this.territory = 1f;
        this.mutation = 1f;
        this.lifespan = 1f;
        this.productivity = 1f;
        this.pollination = 1f;
        this.throttle = 0;
        this.fertility = 0;
        this.temperature = 0;
        this.humidity = 0;
        this.automated = false;
        this.stabilized = false;
        this.weatherproof = false;
        this.lighting = false;
        this.sky = false;
        this.nether = false;
        this.scrubber = false;
        this.sieve = false;
    }

    /**
     * @author - Ghostipedia
     * @reason - I need More Upgrades and this is hardcoded and socalizing with people scares me when i need things.
     */
    @Overwrite
    public int recalculate(IInventoryAdapter inventory) {
        this.reset();
        int energyCost = 0;

        for(int i = 0; i < 4; ++i) {
            ItemStack stack = inventory.getItem(2 + i);
            Item item = stack.getItem();
            int count = stack.getCount();
            if (item instanceof GendustryUpgradeItem upgrade) {
                IGendustryUpgradeType upgradeType = upgrade.getType();
                energyCost += upgradeType.energyCost() * count;
                if (upgradeType instanceof GendustryUpgradeType) {
                    GendustryUpgradeType type = (GendustryUpgradeType)upgradeType;
                    switch (type) {
                        case AUTOMATION:
                            this.automated = true;
                            break;
                        case HEATER:
                            this.temperature += count;
                            break;
                        case COOLER:
                            this.temperature -= count;
                            break;
                        case HUMIDIFIER:
                            this.humidity += count;
                            break;
                        case DRYER:
                            this.humidity -= count;
                            break;
                        case POLLINATION:
                            this.pollination += 0.25F * (float)count;
                            break;
                        case SCRUBBER:
                            this.scrubber = true;
                            break;
                        case NETHER:
                            this.nether = true;
                            break;
                        case LIFESPAN:
                            this.lifespan += 2.0F * (float)count;
                            break;
                        case LIGHTING:
                            this.lighting = true;
                            break;
                        case PRODUCTIVITY:
                            this.productivity += 0.25F * (float)count;
                            break;
                        case WEATHERPROOF:
                            this.weatherproof = true;
                            break;
                        case SIEVE:
                            this.sieve = true;
                            break;
                        case SKY:
                            this.sky = true;
                            break;
                        case STABILIZER:
                            this.stabilized = true;
                            break;
                        case TERRITORY:
                            this.territory += 0.25F * (float)count;
                    }
                } else if (upgradeType instanceof EliteGendustryUpgradeType) {
                    EliteGendustryUpgradeType type = (EliteGendustryUpgradeType)upgradeType;
                    switch (type) {
                        case MUTATION:
                            this.mutation += 0.25F;
                            break;
                        case ACTIVITY_SIMULATOR:
                            this.lighting = true;
                            this.sky = true;
                            this.weatherproof = true;
                            break;
                        case PRODUCTIVITY:
                            this.productivity += 0.25F * (float)count;
                            this.throttle += 15 * count;
                            break;
                        case TERRITORY:
                            this.territory += 0.25F * (float)count;
                            break;
                        case YOUTH:
                            this.mutation -= 0.2F * (float)count;
                            break;
                        case FERTILITY:
                            this.fertility += count;
                    }
                } else if (upgradeType instanceof CosmicGendustryUpgradeType){
                    CosmicGendustryUpgradeType type = (CosmicGendustryUpgradeType)upgradeType;
                    switch (type) {
                        case WAILING:
                            this.mutation = 1000F;
                            break;
                        case DECAYING:
                            this.lifespan = 1000f;
                            this.throttle = 10000;
                    }
                }
            }
        }

        return energyCost;
    }
}
