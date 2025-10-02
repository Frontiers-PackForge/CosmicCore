package com.ghostipedia.cosmiccore.ember.blockentity;

import com.ghostipedia.cosmiccore.common.data.CosmicBlocks;
import com.ghostipedia.cosmiccore.ember.ICosmicEmberStats;
import com.rekindled.embers.RegistryManager;
import com.rekindled.embers.api.capabilities.EmbersCapabilities;
import com.rekindled.embers.api.power.IEmberCapability;
import com.rekindled.embers.api.power.IEmberPacketProducer;
import com.rekindled.embers.api.power.IEmberPacketReceiver;
import com.rekindled.embers.api.power.ITargetable;
import com.rekindled.embers.blockentity.EmberEmitterBlockEntity;
import com.rekindled.embers.datagen.EmbersSounds;
import com.rekindled.embers.entity.EmberPacketEntity;
import com.rekindled.embers.power.DefaultEmberCapability;
import com.rekindled.embers.util.Misc;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import java.util.HashSet;
import java.util.Random;

import static com.rekindled.embers.blockentity.EmberEmitterBlockEntity.getBurstVelocity;

public class CosmicEmberEmitter extends BlockEntity implements IEmberPacketProducer, ITargetable, ICosmicEmberStats {

    private double transferRate = 100;
    private double pullRate = 20;

    public final IEmberCapability capability = new DefaultEmberCapability() {
        @Override
        public void onContentsChanged() {
            CosmicEmberEmitter.this.setChanged();
        }
        @Override
        public boolean acceptsVolatile() {
            return false;
        }
    };

    public BlockPos target = null;
    public long ticksExisted = 0;
    public final Random random = new Random();
    public final int offset = random.nextInt(40);
    public HashSet<ChunkPos> trajectoryChunks = null;

    public CosmicEmberEmitter(BlockPos pos, BlockState state){
        super(CosmicBlocks.CRYOGENIC_CASING.get(),pos,state);
    }

}
