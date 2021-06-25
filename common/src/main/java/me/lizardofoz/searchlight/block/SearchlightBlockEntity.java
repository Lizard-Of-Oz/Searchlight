package me.lizardofoz.searchlight.block;

import lombok.Getter;
import me.lizardofoz.searchlight.SearchlightMod;
import me.lizardofoz.searchlight.util.MutableVector3d;
import me.lizardofoz.searchlight.util.SearchlightUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.light.ChunkLightProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SearchlightBlockEntity extends BlockEntity
{
    @Getter
    private @Nullable BlockPos lightSourcePos;

    //==============================
    //Block Entity Info and Overrides
    //==============================
    public SearchlightBlockEntity()
    {
        super(SearchlightMod.getSearchlightBlockEntityType());
    }

    @Override
    public CompoundTag toTag(CompoundTag tag)
    {
        super.toTag(tag);
        if (lightSourcePos != null)
        {
            tag.putInt("light_source_x", lightSourcePos.getX());
            tag.putInt("light_source_y", lightSourcePos.getY());
            tag.putInt("light_source_z", lightSourcePos.getZ());
        }
        return tag;
    }

    public CompoundTag toClientTag(@NotNull CompoundTag tag)
    {
        return toTag(tag);
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag)
    {
        super.fromTag(state, tag);
        fromClientTag(tag);
    }

    public void fromClientTag(@NotNull CompoundTag tag)
    {
        if (tag.contains("light_source_x") && tag.contains("light_source_y") && tag.contains("light_source_z"))
            lightSourcePos = new BlockPos(tag.getInt("light_source_x"), tag.getInt("light_source_y"), tag.getInt("light_source_z"));
        else
            lightSourcePos = null;
    }

    @Override
    public CompoundTag toInitialChunkDataTag()
    {
        return this.toTag(super.toInitialChunkDataTag());
    }

    @Environment(EnvType.CLIENT)
    public double getRenderDistance()
    {
        return SearchlightUtil.displayBeams() ? 256.0D : super.getRenderDistance();
    }

    //==============================
    //Block functionality
    //==============================
    public @NotNull Vec3d getBeamDirection()
    {
        if (lightSourcePos == null)
            return SearchlightUtil.directionToBeamVector(SearchlightUtil.getDirection(getCachedState()));
        BlockPos delta = lightSourcePos.subtract(getPos());
        return new Vec3d(delta.getX(), delta.getY(), delta.getZ()).normalize();
    }

    /**
     * Returns FALSE is the {@link SearchlightBlockEntity#lightSourcePos} happened to be NOT a Light {@link SearchlightLightSourceBlock}
     * Returns TRUE if everything was normal and the Light Source was deleted
     */
    public boolean deleteLightSource()
    {
        BlockPos oldLightSourcePos = lightSourcePos;
        setLightSourcePos(null);
        if (oldLightSourcePos != null && SearchlightUtil.getBlockStateForceLoad(world, oldLightSourcePos).getBlock() instanceof SearchlightLightSourceBlock)
            return SearchlightUtil.setBlockStateForceLoad(world, oldLightSourcePos, Blocks.AIR.getDefaultState());
        return false;
    }

    /**
     * Returns true if the new light source has been placed
     */
    public boolean raycastAndPlaceLightSource(@NotNull Vec3d beamDirection)
    {
        beamDirection = beamDirection.normalize();
        BlockPos newLightPos = calculateLightSourcePosition(beamDirection);
        return newLightPos != null && placeLightSource(newLightPos);
    }

    /**
     * Deletes an old light source and tries to place a new light source block.<br>
     * Failing to do so or having input being null will result in removing the light source and returning FALSE
     *
     * @return TRUE if successfully placed a new light source.
     */
    public boolean placeLightSource(@Nullable BlockPos newLightPos)
    {
        deleteLightSource();
        if (newLightPos == null)
        {
            setLightSourcePos(null);
            return false;
        }
        BlockState oldBlockState = SearchlightUtil.getBlockStateForceLoad(world, newLightPos);
        if (!SearchlightUtil.setBlockStateForceLoad(world, newLightPos, SearchlightMod.getLightSourceBlock().getDefaultState()))
            return false;
        if (!SearchlightUtil.castBlockEntity(world.getBlockEntity(newLightPos), newLightPos, (SearchlightLightSourceBlockEntity lightBlockEntity) -> {
            lightBlockEntity.searchlightBlockPos = getPos();
            setLightSourcePos(newLightPos);
        }))
        {
            SearchlightUtil.setBlockStateForceLoad(world, newLightPos, oldBlockState);
            setLightSourcePos(null);
            return false;
        }
        return true;
    }

    /**
     * Iterates over blocks starting from a Searchlight and finds the furthest {@link BlockState#isAir()} block away from [this] Searchlight.
     * The raycast can go through stairs, leaves, and other blocks which let the light through according to vanilla rules
     * (e.g. stairs may or may not let the light through depending on its rotation)
     *
     * Returns null if the raycast went outside of the build limit or into the unloaded chunks.
     */
    public @Nullable BlockPos calculateLightSourcePosition(@NotNull Vec3d beamDirection)
    {
        beamDirection = beamDirection.normalize();
        ChunkManager chunkManager = world.getChunkManager();

        MutableVector3d currentBlockPosD = new MutableVector3d(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5);
        BlockPos.Mutable currentBlockPos = new BlockPos.Mutable(currentBlockPosD.x, currentBlockPosD.y, currentBlockPosD.z);
        BlockPos.Mutable prevBlockPos = new BlockPos.Mutable(0, 0, 0);

        BlockPos.Mutable currentChunkPos = new BlockPos.Mutable(0, 0, 0);
        BlockPos.Mutable prevChunkPos = new BlockPos.Mutable(0, 0, 0);
        BlockPos lastValidBlockPos = null;

        while (true)
        {
            prevBlockPos.set(currentBlockPos);
            currentBlockPosD.add(beamDirection);
            currentBlockPos.set(currentBlockPosD.x, currentBlockPosD.y, currentBlockPosD.z);

            if (prevBlockPos.equals(currentBlockPos))
                continue;

            if (!World.isInBuildLimit(currentBlockPos))
                return null;

            prevChunkPos.set(prevBlockPos.getX() >> 4, 0, prevBlockPos.getZ() >> 4);
            currentChunkPos.set(currentBlockPos.getX() >> 4, 0, currentBlockPos.getZ() >> 4);

            if (!prevChunkPos.equals(currentChunkPos) && !chunkManager.isChunkLoaded(currentChunkPos.getX(), currentChunkPos.getZ()))
                return null;

            //Better off having to load an unloaded chunk, than peeking into an unloaded chunk and receiving AIR
            BlockState currentBlockState = SearchlightUtil.getBlockStateForceLoad(world, currentBlockPos);
            BlockState prevBlockState = SearchlightUtil.getBlockStateForceLoad(world, prevBlockPos);

            if (ChunkLightProvider.getRealisticOpacity(
                    world,
                    prevBlockState, prevBlockPos,
                    currentBlockState, currentBlockPos,
                    Direction.getFacing(beamDirection.x, beamDirection.y, beamDirection.z),
                    currentBlockState.getOpacity(world, currentBlockPos)) >= world.getMaxLightLevel()
                    || !world.getFluidState(currentBlockPos).isEmpty())
                return SearchlightUtil.moveAwayFromSurfaces(world, lastValidBlockPos);

            if (currentBlockState.isAir() || currentBlockPos.equals(lightSourcePos))
                lastValidBlockPos = currentBlockPos.toImmutable();
        }
    }

    /**
     * Doesn't move the actual Light Source, just updates
     * the {@link SearchlightBlockEntity#lightSourcePos} value and syncs it on the client
     */
    protected void setLightSourcePos(@Nullable BlockPos lightSourcePos)
    {
        this.lightSourcePos = lightSourcePos;
        if (world != null && !world.isClient)
            SearchlightMod.getBlockEntitySynchronizer().accept(this);
    }
}