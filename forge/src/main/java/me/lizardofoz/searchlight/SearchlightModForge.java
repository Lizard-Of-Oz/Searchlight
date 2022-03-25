package me.lizardofoz.searchlight;

import me.lizardofoz.searchlight.block.*;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.*;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.DyeColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Mod("searchlight")
public final class SearchlightModForge extends SearchlightMod
{
    private HashMap<String, Object> wallLightTypes = new HashMap<>();

    public SearchlightModForge()
    {
        FMLJavaModLoadingContext.get().getModEventBus().register(this);

        creativeItemGroup = new ItemGroup("searchlight")
        {
            @Override
            public ItemStack createIcon()
            {
                return new ItemStack(searchlightBlock);
            }
        };

        wallLightTypes.put("iron", new Object());
        wallLightTypes.put("copper", new Object());
        wallLightTypes.put("prismarine", new Object());
        for (DyeColor color : DyeColor.values())
            wallLightTypes.put(color.getName(), new Object());
    }

    @SubscribeEvent
    public void registerSearchlightBlock(RegistryEvent.Register<Block> event)
    {
        searchlightBlock = new SearchlightBlock(
                AbstractBlock.Settings.of(Material.METAL, MapColor.CLEAR)
                        .sounds(BlockSoundGroup.METAL)
                        .strength(4)
                        .requiresTool()
                        .nonOpaque())
                .setRegistryName("searchlight");
        event.getRegistry().register(searchlightBlock);

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
                        .luminance((state) -> 15))
                .setRegistryName("searchlight_lightsource");
        event.getRegistry().register(lightSourceBlock);
    }

    @SubscribeEvent
    public void registerSearchlightBlockItem(RegistryEvent.Register<Item> event)
    {
        searchlightItem = new BlockItem(searchlightBlock, new Item.Settings().group(creativeItemGroup));
        searchlightItem.setRegistryName("searchlight");
        event.getRegistry().register(searchlightItem);
    }

    @SubscribeEvent
    public void registerLightBlockEntity(RegistryEvent.Register<BlockEntityType<?>> event)
    {
        searchlightBlockEntityType = BlockEntityType.Builder
                .create(SearchlightBlockEntity::new, searchlightBlock)
                .build(null);
        searchlightBlockEntityType.setRegistryName("searchlight_entity");
        event.getRegistry().register(searchlightBlockEntityType);

        lightSourceBlockEntityType = BlockEntityType.Builder
                .create(SearchlightLightSourceBlockEntity::new, lightSourceBlock)
                .build(null);
        lightSourceBlockEntityType.setRegistryName("searchlight_lightsource_entity");
        event.getRegistry().register(lightSourceBlockEntityType);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void registerEntityRenderer(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerBlockEntityRenderer(searchlightBlockEntityType, SearchlightBlockRenderer::new);
    }

    @SubscribeEvent
    public void registerWallLightBlocks(RegistryEvent.Register<Block> event)
    {
        for (String postfix : new ArrayList<>(wallLightTypes.keySet()))
        {
            Block block = new WallLightBlock(
                    AbstractBlock.Settings.of(Material.DECORATION)
                            .strength(0.5F)
                            .luminance((state) -> 14)
                            .sounds(BlockSoundGroup.STONE)
                            .nonOpaque()
                            .noCollision());
            block.setRegistryName("wall_light_" + postfix);
            event.getRegistry().register(block);
            wallLightTypes.put(postfix, block);
        }
    }

    @SubscribeEvent
    public void registerWallLightBlockItems(RegistryEvent.Register<Item> event)
    {
        for (Map.Entry<String, Object> entry : wallLightTypes.entrySet())
        {
            Block block = (Block) entry.getValue();
            Item item = new BlockItem(block, new Item.Settings().group(creativeItemGroup)).setRegistryName("wall_light_" + entry.getKey());
            event.getRegistry().register(item);
        }
    }
}