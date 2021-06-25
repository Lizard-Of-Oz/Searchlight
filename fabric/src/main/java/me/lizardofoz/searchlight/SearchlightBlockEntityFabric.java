package me.lizardofoz.searchlight;

import me.lizardofoz.searchlight.block.SearchlightBlockEntity;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;

public class SearchlightBlockEntityFabric extends SearchlightBlockEntity implements BlockEntityClientSerializable
{
    public SearchlightBlockEntityFabric(BlockPos blockPos, BlockState blockState)
    {
        super(SearchlightMod.getSearchlightBlockEntityType(), blockPos, blockState);
    }

    public static SearchlightBlockEntity create(BlockPos blockPos, BlockState blockState)
    {
        return new SearchlightBlockEntityFabric(blockPos, blockState);
    }
}
