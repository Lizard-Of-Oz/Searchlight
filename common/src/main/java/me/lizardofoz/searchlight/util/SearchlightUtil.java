package me.lizardofoz.searchlight.util;

import me.lizardofoz.searchlight.SearchlightMod;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.function.Consumer;

public final class SearchlightUtil
{
    private SearchlightUtil()
    {
    }

    public static <@Nullable T extends BlockEntity> boolean castBlockEntity(@Nullable BlockEntity blockEntity, @NotNull BlockPos blockPos, @NotNull Consumer<T> result)
    {
        if (blockEntity == null)
        {
            try
            {
                throw new IllegalStateException();
            }
            catch (Exception e)
            {
                SearchlightMod.LOGGER.error("Attempted to cast a null blockEntity at " + blockPos, e);
            }
            return false;
        }
        if (!blockEntity.hasWorld())
        {
            try
            {
                throw new IllegalStateException();
            }
            catch (Exception e)
            {
                SearchlightMod.LOGGER.error(
                        String.format("Attempted to use a blockEntity '%s' (%s) at %s with world==null.",
                                blockEntity.writeNbt(new NbtCompound()),
                                blockEntity.getClass(),
                                blockPos), e);
            }
            return false;
        }
        try
        {
            //noinspection unchecked
            result.accept((T) blockEntity);
            return true;
        }
        catch (ClassCastException ex)
        {
            SearchlightMod.LOGGER.error(
                    String.format("Attempted to cast '%s' (%s) at %s but failed",
                            blockEntity.writeNbt(new NbtCompound()),
                            blockEntity.getClass(),
                            blockPos),
                    ex);
            return false;
        }
    }

    public static @NotNull BlockState getBlockStateForceLoad(@NotNull World world, @NotNull BlockPos blockPos)
    {
        return world.getBlockState(blockPos);
    }

    public static @NotNull BlockState getBlockStateIfLoaded(World world, BlockPos blockPos)
    {
        if (world.isInBuildLimit(blockPos))
            return Blocks.VOID_AIR.getDefaultState();
        BlockView chunk = world.getChunkAsView(blockPos.getX() >> 4, blockPos.getZ() >> 4);
        if (chunk == null)
            return Blocks.VOID_AIR.getDefaultState();
        return chunk.getBlockState(blockPos);
    }

    public static boolean setBlockStateForceLoad(World world, BlockPos blockPos, BlockState blockState)
    {
        return world.setBlockState(blockPos, blockState);
    }

    public static Direction getDirection(BlockState state)
    {
        WallMountLocation face = state.get(WallMountedBlock.FACE);
        if (face == WallMountLocation.CEILING)
            return Direction.DOWN;
        else if (face == WallMountLocation.FLOOR)
            return Direction.UP;
        return state.get(WallMountedBlock.FACING);
    }

    public static @NotNull Vec3d directionToBeamVector(@NotNull Direction direction)
    {
        Vec3i vector = direction.getVector();
        return new Vec3d(vector.getX(), vector.getY(), vector.getZ()).normalize();
    }

    /**
     * This method moves the light source 1 block away from walls, floor and ceiling
     */
    public static BlockPos moveAwayFromSurfaces(World world, BlockPos blockPos)
    {
        if (blockPos == null)
            return null;
        BlockPos resultPos = blockPos.toImmutable();

        if (!world.getBlockState(resultPos.add(-1, 0, 0)).isAir() && world.getBlockState(resultPos.add(1, 0, 0)).isAir())
            resultPos = resultPos.add(1, 0, 0);
        else if (!world.getBlockState(resultPos.add(1, 0, 0)).isAir() && world.getBlockState(resultPos.add(-1, 0, 0)).isAir())
            resultPos = resultPos.add(-1, 0, 0);

        if (!world.getBlockState(resultPos.add(0, -1, 0)).isAir() && world.getBlockState(resultPos.add(0, 1, 0)).isAir())
            resultPos = resultPos.add(0, 1, 0);
        else if (!world.getBlockState(resultPos.add(0, 1, 0)).isAir() && world.getBlockState(resultPos.add(0, -1, 0)).isAir())
            resultPos = resultPos.add(0, -1, 0);

        if (!world.getBlockState(resultPos.add(0, 0, -1)).isAir() && world.getBlockState(resultPos.add(0, 0, 1)).isAir())
            resultPos = resultPos.add(0, 0, 1);
        else if (!world.getBlockState(resultPos.add(0, 0, 1)).isAir() && world.getBlockState(resultPos.add(0, 0, -1)).isAir())
            resultPos = resultPos.add(0, 0, -1);

        return resultPos;
    }

    @Environment(EnvType.CLIENT)
    public static boolean displayBeams()
    {
        PlayerEntity player = MinecraftClient.getInstance().player;
        return player != null && player.isHolding(SearchlightMod.getSearchlightItem());
    }
}
