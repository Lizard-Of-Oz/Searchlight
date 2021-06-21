package me.lizardofoz.searchlight.block;

import me.lizardofoz.searchlight.SearchlightMod;
import me.lizardofoz.searchlight.util.SearchlightUtil;
import me.lizardofoz.searchlight.util.MutableVector3d;
import me.lizardofoz.searchlight.util.MutableVector3i;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SearchlightLightSourceBlockEntity extends BlockEntity
{
    public @Nullable BlockPos searchlightBlockPos;

    //==============================
    //Block Entity Info and Overrides
    //==============================
    public SearchlightLightSourceBlockEntity()
    {
        super(SearchlightMod.getLightSourceBlockEntityType());
    }

    @Override
    public CompoundTag toTag(CompoundTag tag)
    {
        super.toTag(tag);
        if (searchlightBlockPos != null)
        {
            tag.putInt("searchlight_x", searchlightBlockPos.getX());
            tag.putInt("searchlight_y", searchlightBlockPos.getY());
            tag.putInt("searchlight_z", searchlightBlockPos.getZ());
        }
        return tag;
    }

    @Override
    public void fromTag(BlockState state, CompoundTag tag)
    {
        super.fromTag(state, tag);
        if (tag.contains("searchlight_x") && tag.contains("searchlight_y") && tag.contains("searchlight_z"))
            searchlightBlockPos = new BlockPos(tag.getInt("searchlight_x"), tag.getInt("searchlight_y"), tag.getInt("searchlight_z"));
        else
            searchlightBlockPos = null;
    }

    @Override
    public CompoundTag toInitialChunkDataTag()
    {
        return this.toTag(super.toInitialChunkDataTag());
    }

    //==============================
    //Block functionality
    //==============================

    /**
     * This method is called when the block is getting replaced and we need to find a new position for the lightsource
     */
    public void moveLightSource()
    {
        if (world == null || world.isClient || searchlightBlockPos == null)
            return;
        SearchlightUtil.castBlockEntity(world.getBlockEntity(searchlightBlockPos), searchlightBlockPos, (SearchlightBlockEntity searchlightBlockEntity) -> {
            if (getPos().equals(searchlightBlockEntity.getLightSourcePos())) //Here we have sure [this] Light Source is connected to the Searchlight in question
                searchlightBlockEntity.placeLightSource(calculateLightSourcePosition(searchlightBlockEntity.getBeamDirection().multiply(-1)));
        });
    }

    /**
     * Unlike a similar method in {@link SearchlightBlockEntity}, this method iterates from the Light Source's position,
     * while that method iterates from the position of a Searchlight
     *
     * Returns null if the raycast went outside of the build limit or into the unloaded chunks.
     */
    public @Nullable BlockPos calculateLightSourcePosition(@NotNull Vec3d direction)
    {
        direction = direction.normalize();
        ChunkManager chunkManager = world.getChunkManager();
        MutableVector3d currentPosVecD = new MutableVector3d(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5);
        MutableVector3i currentPosVecI = new MutableVector3i(currentPosVecD.x, currentPosVecD.y, currentPosVecD.z);
        MutableVector3i prevPosVecI = new MutableVector3i(0, 0, 0);

        MutableVector3i currentChunkVec = new MutableVector3i(0, 0, 0);
        MutableVector3i prevChunkVec = new MutableVector3i(0, 0, 0);

        while (true)
        {
            prevPosVecI.set(currentPosVecI);
            currentPosVecD.add(direction);
            currentPosVecI.set(currentPosVecD);

            if (prevPosVecI.equals(currentPosVecI))
                continue;
            if (World.isOutOfBuildLimitVertically(currentPosVecI.y))
                return null;

            prevChunkVec.set(prevPosVecI.x >> 4, 0, prevPosVecI.z >> 4);
            currentChunkVec.set(currentPosVecI.x >> 4, 0, currentPosVecI.z >> 4);

            if (!prevChunkVec.areSame(currentChunkVec) && !chunkManager.isChunkLoaded(currentPosVecI.x >> 4, currentPosVecI.z >> 4))
                return null;

            BlockPos currentBlockPos = new BlockPos(currentPosVecI.x, currentPosVecI.y, currentPosVecI.z);
            if (currentBlockPos.equals(searchlightBlockPos))
                return null;
            if (SearchlightUtil.getBlockStateIfLoaded(world, currentBlockPos).isAir())
                return SearchlightUtil.moveAwayFromSurfaces(world, currentBlockPos);
        }
    }
}