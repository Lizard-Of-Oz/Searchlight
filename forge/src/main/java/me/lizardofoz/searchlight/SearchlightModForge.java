package me.lizardofoz.searchlight;

import me.lizardofoz.searchlight.block.*;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import java.util.ArrayList;
import java.util.HashMap;

@Mod("searchlight")
public final class SearchlightModForge extends SearchlightMod
{
    private final HashMap<String, Object> wallLightTypes = new HashMap<>();

    public SearchlightModForge()
    {
        FMLJavaModLoadingContext.get().getModEventBus().register(this);

        wallLightTypes.put("iron", new Object());
        wallLightTypes.put("copper", new Object());
        wallLightTypes.put("prismarine", new Object());
        for (DyeColor color : DyeColor.values())
            wallLightTypes.put(color.getName(), new Object());
    }

    @SubscribeEvent
    public void forgePleaseStopChangingYourAPI(RegisterEvent event)
    {
        Registry.register(Registries.ITEM_GROUP, creativeItemGroup,
                ItemGroup.builder()
                        .icon(() -> new ItemStack(searchlightBlock))
                        .displayName(Text.translatable("itemGroup.searchlight"))
                        .build());

        event.register(ForgeRegistries.Keys.BLOCKS, helper -> {
            searchlightBlock = new SearchlightBlock(
                    AbstractBlock.Settings.create()
                            .mapColor(MapColor.CLEAR)
                            .pistonBehavior(PistonBehavior.DESTROY)
                            .sounds(BlockSoundGroup.METAL)
                            .requiresTool()
                            .strength(4)
                            .nonOpaque());

            lightSourceBlock = new SearchlightLightSourceBlock(
                    AbstractBlock.Settings.create()
                            .mapColor(MapColor.CLEAR)
                            .replaceable()
                            .notSolid()
                            .sounds(BlockSoundGroup.WOOD)
                            .strength(3600000.8F)
                            .dropsNothing()
                            .nonOpaque()
                            .pistonBehavior(PistonBehavior.DESTROY)
                            .luminance((state) -> 15));

            helper.register(new Identifier("searchlight", "searchlight"), searchlightBlock);
            helper.register(new Identifier("searchlight", "searchlight_lightsource"), lightSourceBlock);

            for (String postfix : new ArrayList<>(wallLightTypes.keySet()))
            {
                Block block = new WallLightBlock(
                        AbstractBlock.Settings.create()
                                .notSolid()
                                .pistonBehavior(PistonBehavior.DESTROY)
                                .strength(0.5F)
                                .luminance((state) -> 14)
                                .sounds(BlockSoundGroup.STONE)
                                .nonOpaque()
                                .noCollision());
                wallLightTypes.put(postfix, block);
                helper.register(new Identifier("searchlight", "wall_light_" + postfix), block);
            }
        });

        event.register(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, helper -> {
            searchlightBlockEntityType = BlockEntityType.Builder
                    .create(SearchlightBlockEntity::new, searchlightBlock)
                    .build(null);

            lightSourceBlockEntityType = BlockEntityType.Builder
                    .create(SearchlightLightSourceBlockEntity::new, lightSourceBlock)
                    .build(null);

            helper.register(new Identifier("searchlight", "searchlight_entity"), searchlightBlockEntityType);
            helper.register(new Identifier("searchlight", "searchlight_lightsource_entity"), lightSourceBlockEntityType);
        });

        event.register(ForgeRegistries.Keys.ITEMS, helper -> {
            searchlightItem = new BlockItem(searchlightBlock, new Item.Settings());
            helper.register(new Identifier("searchlight", "searchlight"), searchlightItem);

            wallLightTypes.forEach((postfix, block) -> {
                Item item = new BlockItem((Block) block, new Item.Settings());
                helper.register(new Identifier("searchlight", "wall_light_" + postfix), item);
            });
        });
    }

    @SubscribeEvent
    public void registerCreativeTabItems(BuildCreativeModeTabContentsEvent event)
    {
        if (creativeItemGroup.equals(event.getTabKey()))
        {
            event.add(searchlightItem);
            wallLightTypes.forEach((postfix, block) -> event.add((Block)block));
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void registerEntityRenderer(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerBlockEntityRenderer(searchlightBlockEntityType, SearchlightBlockRenderer::new);
    }
}