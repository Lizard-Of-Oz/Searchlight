package me.lizardofoz.searchlight.block;

import me.lizardofoz.searchlight.SearchlightMod;
import me.lizardofoz.searchlight.util.MutableVector3d;
import me.lizardofoz.searchlight.util.SearchlightUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.ChunkManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SearchlightLightSourceBlockEntity extends BlockEntity
{
    public @Nullable BlockPos searchlightBlockPos;

    //==============================
    //Block Entity Info and Overrides
    //==============================
    public SearchlightLightSourceBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        super(SearchlightMod.getLightSourceBlockEntityType(), blockPos, blockState);
    }

    @Override
    public void writeNbt(NbtCompound tag)
    {
        super.writeNbt(tag);
        if (searchlightBlockPos != null)
        {
            tag.putInt("searchlight_x", searchlightBlockPos.getX());
            tag.putInt("searchlight_y", searchlightBlockPos.getY());
            tag.putInt("searchlight_z", searchlightBlockPos.getZ());
        }
    }

    @Override
    public void readNbt(NbtCompound tag)
    {
        super.readNbt(tag);
        if (tag.contains("searchlight_x") && tag.contains("searchlight_y") && tag.contains("searchlight_z"))
            searchlightBlockPos = new BlockPos(tag.getInt("searchlight_x"), tag.getInt("searchlight_y"), tag.getInt("searchlight_z"));
        else
            searchlightBlockPos = null;
    }

    @Override
    public NbtCompound toInitialChunkDataNbt()
    {
        NbtCompound tag = super.toInitialChunkDataNbt();
        this.writeNbt(tag);
        return tag;
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
        MutableVector3d currentBlockPosD = new MutableVector3d(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5);
        BlockPos.Mutable currentBlockPos = new BlockPos.Mutable(currentBlockPosD.x, currentBlockPosD.y, currentBlockPosD.z);
        BlockPos.Mutable prevBlockPos = new BlockPos.Mutable(0, 0, 0);

        BlockPos.Mutable currentChunkPos = new BlockPos.Mutable(0, 0, 0);
        BlockPos.Mutable prevChunkPos = new BlockPos.Mutable(0, 0, 0);

        while (true)
        {
            prevBlockPos.set(currentBlockPos);
            currentBlockPosD.add(direction);
            currentBlockPos.set(currentBlockPosD.x, currentBlockPosD.y, currentBlockPosD.z);

            if (prevBlockPos.equals(currentBlockPos))
                continue;

            if (!world.isInBuildLimit(currentBlockPos))
                return null;

            prevChunkPos.set(prevBlockPos.getX() >> 4, 0, prevBlockPos.getZ() >> 4);
            currentChunkPos.set(currentBlockPos.getX() >> 4, 0, currentBlockPos.getZ() >> 4);

            if (!prevChunkPos.equals(currentChunkPos) && !chunkManager.isChunkLoaded(currentChunkPos.getX(), currentChunkPos.getZ()))
                return null;

            if (currentBlockPos.equals(searchlightBlockPos))
                return null;

            if (SearchlightUtil.getBlockStateIfLoaded(world, currentBlockPos).isAir())
                return SearchlightUtil.moveAwayFromSurfaces(world, currentBlockPos);
        }
    }
}