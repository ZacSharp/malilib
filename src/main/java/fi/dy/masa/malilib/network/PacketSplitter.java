package fi.dy.masa.malilib.network;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import org.apache.commons.lang3.tuple.Pair;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.PacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import io.netty.buffer.Unpooled;

/**
 * Network packet splitter code from QuickCarpet by skyrising
 * @author skyrising
 *
 */
public class PacketSplitter
{
    public static final int MAX_TOTAL_PER_PACKET_S2C = 1048576;
    public static final int MAX_PAYLOAD_PER_PACKET_S2C = MAX_TOTAL_PER_PACKET_S2C - 5;
    public static final int MAX_TOTAL_PER_PACKET_C2S = 32767;
    public static final int MAX_PAYLOAD_PER_PACKET_C2S = MAX_TOTAL_PER_PACKET_C2S - 5;
    public static final int DEFAULT_MAX_RECEIVE_SIZE_C2S = 1048576;
    public static final int DEFAULT_MAX_RECEIVE_SIZE_S2C = 67108864;

    private static final Map<Pair<PacketListener, ResourceLocation>, ReadingSession> READING_SESSIONS = new HashMap<>();

    public static void send(ServerGamePacketListenerImpl networkHandler, ResourceLocation channel, FriendlyByteBuf packet)
    {
        send(packet, MAX_PAYLOAD_PER_PACKET_S2C, buf -> networkHandler.send(new ClientboundCustomPayloadPacket(channel, buf)));
    }

    public static void send(ClientPacketListener networkHandler, ResourceLocation channel, FriendlyByteBuf packet)
    {
        send(packet, MAX_PAYLOAD_PER_PACKET_C2S, buf -> networkHandler.send(new ServerboundCustomPayloadPacket(channel, buf)));
    }

    private static void send(FriendlyByteBuf packet, int payloadLimit, Consumer<FriendlyByteBuf> sender)
    {
        int len = packet.writerIndex();

        packet.resetReaderIndex();

        for (int offset = 0; offset < len; offset += payloadLimit)
        {
            int thisLen = Math.min(len - offset, payloadLimit);
            FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer(thisLen));

            buf.resetWriterIndex();

            if (offset == 0)
            {
                buf.writeVarInt(len);
            }

            buf.writeBytes(packet, thisLen);

            sender.accept(buf);
        }

        packet.release();
    }

    /*
    @Nullable
    public static PacketBuffer receive(ServerPlayNetHandler networkHandler, CCustomPayloadPacket message)
    {
        return receive(networkHandler, message, DEFAULT_MAX_RECEIVE_SIZE_C2S);
    }

    @Nullable
    private static PacketBuffer receive(ServerPlayNetHandler networkHandler, CCustomPayloadPacket message, int maxLength)
    {
        CustomPayloadC2SPacketAccessor messageAccessor = (CustomPayloadC2SPacketAccessor) message;
        Pair<INetHandler, ResourceLocation> key = Pair.of(networkHandler, messageAccessor.getChannel());

        return READING_SESSIONS.computeIfAbsent(key, ReadingSession::new).receive(messageAccessor.getData(), maxLength);
    }
    */

    @Nullable
    public static FriendlyByteBuf receive(ClientPacketListener networkHandler, ClientboundCustomPayloadPacket message)
    {
        return receive(networkHandler, message, DEFAULT_MAX_RECEIVE_SIZE_S2C);
    }

    @Nullable
    private static FriendlyByteBuf receive(ClientPacketListener networkHandler, ClientboundCustomPayloadPacket message, int maxLength)
    {
        Pair<PacketListener, ResourceLocation> key = Pair.of(networkHandler, message.getIdentifier());

        return READING_SESSIONS.computeIfAbsent(key, ReadingSession::new).receive(message.getData(), maxLength);
    }

    private static class ReadingSession
    {
        private final Pair<PacketListener, ResourceLocation> key;
        private int expectedSize = -1;
        private FriendlyByteBuf received;

        private ReadingSession(Pair<PacketListener, ResourceLocation> key)
        {
            this.key = key;
        }

        @Nullable
        private FriendlyByteBuf receive(FriendlyByteBuf data, int maxLength)
        {
            if (this.expectedSize < 0)
            {
                this.expectedSize = data.readVarInt();

                if (this.expectedSize > maxLength)
                {
                    throw new IllegalArgumentException("Payload too large");
                }

                this.received = new FriendlyByteBuf(Unpooled.buffer(this.expectedSize));
            }

            this.received.writeBytes(data.readBytes(data.readableBytes()));

            if (this.received.writerIndex() >= this.expectedSize)
            {
                READING_SESSIONS.remove(this.key);
                return this.received;
            }

            return null;
        }
    }
}
