package me.lizardofoz.searchlight;

import me.lizardofoz.searchlight.block.SearchlightBlockEntity;
import me.lizardofoz.searchlight.util.SearchlightUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.network.NetworkRegistry;
import net.minecraftforge.fmllegacy.network.PacketDistributor;
import net.minecraftforge.fmllegacy.network.simple.SimpleChannel;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

final class PacketStuff
{
    private static final String PROTOCOL_VERSION = "1.1";

    private static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new Identifier("searchlight", "packets"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private PacketStuff() { }

    public static void initialize()
    {
        INSTANCE.registerMessage(0, UpdateSearchlightS2CPacket.class,
                UpdateSearchlightS2CPacket::write,
                UpdateSearchlightS2CPacket::new,
                UpdateSearchlightS2CPacket::consume);
    }

    public static void sendUpdateRequestToClient(SearchlightBlockEntity blockEntity)
    {
        INSTANCE.send(PacketDistributor.TRACKING_CHUNK.with(() -> (WorldChunk) blockEntity.getWorld().getChunk(blockEntity.getPos())),
                new UpdateSearchlightS2CPacket(blockEntity));
    }

    private static class UpdateSearchlightS2CPacket
    {
        public final NbtCompound compoundTag;

        //Sender's constructor
        public UpdateSearchlightS2CPacket(@NotNull SearchlightBlockEntity blockEntity)
        {
            this.compoundTag = blockEntity.toClientTag(new NbtCompound());
        }

        //Receiver's constructor
        public UpdateSearchlightS2CPacket(PacketByteBuf buf)
        {
            compoundTag = buf.readNbt();
        }

        //Sender's writer
        public void write(PacketByteBuf buf)
        {
            buf.writeNbt(compoundTag);
        }

        //Receiver's consumer
        public void consume(Supplier<NetworkEvent.Context> supplier)
        {
            supplier.get().enqueueWork(this::syncBlock);
            supplier.get().setPacketHandled(true);
        }

        /**
         * For some reason, having the body of this method within [consume] method leads
         *   to the dedicated server trying to load Client-side classes.
         * By having this method separately, we avoid that
         */
        @OnlyIn(Dist.CLIENT)
        private void syncBlock()
        {
            World world = MinecraftClient.getInstance().world;
            if (world == null)
                return;
            BlockPos blockPos = new BlockPos(compoundTag.getInt("x"), compoundTag.getInt("y"), compoundTag.getInt("z"));
            SearchlightUtil.castBlockEntity(world.getBlockEntity(blockPos), blockPos,
                    (SearchlightBlockEntity blockEntity) -> blockEntity.fromClientTag(compoundTag));
        }
    }
}