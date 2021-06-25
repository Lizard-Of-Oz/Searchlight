package me.lizardofoz.searchlight.block;

import me.lizardofoz.searchlight.SearchlightMod;
import me.lizardofoz.searchlight.util.SearchlightUtil;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

@SuppressWarnings("deprecation")
public class SearchlightLightSourceBlock extends Block implements BlockEntityProvider
{
    public SearchlightLightSourceBlock(Settings settings)
    {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        return new SearchlightLightSourceBlockEntity(blockPos, blockState);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context)
    {
        return context.isHolding(SearchlightMod.getSearchlightItem()) ? VoxelShapes.fullCube() : VoxelShapes.empty();
    }

    @Override
    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos)
    {
        return true;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state)
    {
        return BlockRenderType.INVISIBLE;
    }

    @Override
    public float getAmbientOcclusionLightLevel(BlockState state, BlockView world, BlockPos pos)
    {
        return 1;
    }

    @Override
    public PistonBehavior getPistonBehavior(BlockState state) {
        return PistonBehavior.DESTROY;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved)
    {
        SearchlightUtil.castBlockEntity(world.getBlockEntity(pos), pos, SearchlightLightSourceBlockEntity::moveLightSource);
        super.onStateReplaced(state, world, pos, newState, moved);
    }
}
