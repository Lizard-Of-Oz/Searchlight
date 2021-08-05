package me.lizardofoz.searchlight;

import com.google.common.collect.ImmutableMap;
import me.lizardofoz.searchlight.block.*;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.DyeColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;

@Mod("searchlight")
public final class SearchlightModForge extends SearchlightMod
{
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "searchlight");
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "searchlight");
    private static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITIES, "searchlight");

    public SearchlightModForge()
    {
        PacketStuff.initialize();
        BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        TILE_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());

        creativeItemGroup = new ItemGroup("searchlight") {
            @Override
            public ItemStack createIcon() {
                return new ItemStack(searchlightBlock);
            }
        };
        blockEntitySynchronizer = PacketStuff::sendUpdateRequestToClient;
        blockEntityConstructor = SearchlightBlockEntity::new;

        registerSearchlightBlock();
        registerSearchlightLightSourceBlock();
        registerWallLightBlocks();
    }

    private void registerSearchlightBlock()
    {
        searchlightBlock = new SearchlightBlock(
                AbstractBlock.Settings.of(Material.METAL)
                        .sounds(BlockSoundGroup.METAL)
                        .requiresTool()
                        .strength(5)
                        .nonOpaque());
        searchlightItem = new BlockItem(searchlightBlock, new Item.Settings().group(creativeItemGroup));
        searchlightBlockEntityType = BlockEntityType.Builder
                .create((blockPos, blockState) -> blockEntityConstructor.apply(blockPos, blockState), searchlightBlock)
                .build(null);

        BLOCKS.register("searchlight", () -> searchlightBlock);
        ITEMS.register("searchlight", () -> searchlightItem);
        TILE_ENTITIES.register("searchlight_entity", () -> searchlightBlockEntityType);
        if (FMLEnvironment.dist == Dist.CLIENT)
            BlockEntityRendererFactories.register(searchlightBlockEntityType, SearchlightBlockRenderer::new);
    }

    private void registerSearchlightLightSourceBlock()
    {
        lightSourceBlock = new SearchlightLightSourceBlock(
                AbstractBlock.Settings.of(
                        new Material.Builder(MapColor.CLEAR)
                                .replaceable()
                                .notSolid()
                                .build())
                        .sounds(BlockSoundGroup.WOOD)
                        .strength(3600000.8F)
                        .dropsNothing()
                        .nonOpaque()
                        .luminance((state) -> 15));
        lightSourceBlockEntityType = BlockEntityType.Builder.create(SearchlightLightSourceBlockEntity::new, lightSourceBlock).build(null);

        BLOCKS.register("searchlight_lightsource", () -> lightSourceBlock);
        TILE_ENTITIES.register("searchlight_lightsource_entity", () -> lightSourceBlockEntityType);
    }

    private void registerWallLightBlocks()
    {
        Map<Block, Item> wallLights = new HashMap<>();
        registerWallLight("iron", wallLights);
        registerWallLight("copper", wallLights);
        registerWallLight("prismarine", wallLights);
        for (DyeColor color : DyeColor.values())
            registerWallLight(color.getName(), wallLights);
        wallLightBlocks = ImmutableMap.copyOf(wallLights);
    }

    private void registerWallLight(String postfix, Map<Block,Item> wallLightMap)
    {
        Block block = new WallLightBlock(
                AbstractBlock.Settings.of(Material.DECORATION)
                        .strength(0.5F)
                        .luminance((state) -> 14)
                        .sounds(BlockSoundGroup.STONE)
                        .nonOpaque()
                        .noCollision());
        Item item = new BlockItem(block, new Item.Settings().group(creativeItemGroup));

        BLOCKS.register("wall_light_" + postfix, () -> block);
        ITEMS.register("wall_light_" + postfix, () -> item);
        wallLightMap.put(block, item);
    }
}