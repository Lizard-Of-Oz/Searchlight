package me.lizardofoz.searchlight;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import me.lizardofoz.searchlight.block.SearchlightBlockEntity;
import me.lizardofoz.searchlight.block.SearchlightLightSourceBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class SearchlightMod
{
    public static final Logger LOGGER = LogManager.getLogger("Searchlight");

    @Getter protected final RegistryKey<ItemGroup> creativeItemGroup = RegistryKey.of(RegistryKeys.ITEM_GROUP, new Identifier("searchlight", "searchlight"));

    @Getter protected static Item searchlightItem;
    @Getter protected static Block searchlightBlock;
    @Getter protected static BlockEntityType<SearchlightBlockEntity> searchlightBlockEntityType;

    @Getter protected static Block lightSourceBlock;
    @Getter protected static BlockEntityType<SearchlightLightSourceBlockEntity> lightSourceBlockEntityType;

    @Getter protected static ImmutableMap<Block, Item> wallLightBlocks;

}
