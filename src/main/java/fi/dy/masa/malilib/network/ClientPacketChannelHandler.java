package fi.dy.masa.malilib.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.resources.ResourceLocation;
import com.google.common.base.Charsets;
import fi.dy.masa.malilib.MaLiLib;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class ClientPacketChannelHandler implements IClientPacketChannelHandler
{
    public static final ResourceLocation REGISTER = new ResourceLocation("minecraft:register");
    public static final ResourceLocation UNREGISTER = new ResourceLocation("minecraft:unregister");

    private static final ClientPacketChannelHandler INSTANCE = new ClientPacketChannelHandler();

    private final HashMap<ResourceLocation, IPluginChannelHandler> handlers;

    public static IClientPacketChannelHandler getInstance()
    {
        return INSTANCE;
    }

    private ClientPacketChannelHandler()
    {
        this.handlers = new HashMap<>();
    }

    @Override
    public void registerClientChannelHandler(IPluginChannelHandler handler)
    {
        List<ResourceLocation> toRegister = new ArrayList<>();

        for (ResourceLocation channel : handler.getChannels())
        {
            if (this.handlers.containsKey(channel) == false)
            {
                this.handlers.put(channel, handler);
                toRegister.add(channel);
            }
        }

        if (toRegister.isEmpty() == false)
        {
            this.sendRegisterPacket(REGISTER, toRegister);
        }
    }

    @Override
    public void unregisterClientChannelHandler(IPluginChannelHandler handler)
    {
        List<ResourceLocation> toUnRegister = new ArrayList<>();

        for (ResourceLocation channel : handler.getChannels())
        {
            if (this.handlers.remove(channel, handler))
            {
                toUnRegister.add(channel);
            }
        }

        if (toUnRegister.isEmpty() == false)
        {
            this.sendRegisterPacket(UNREGISTER, toUnRegister);
        }
    }

    /**
     * NOT PUBLIC API - DO NOT CALL
     */
    public boolean processPacketFromServer(ClientboundCustomPayloadPacket packet, ClientPacketListener netHandler)
    {
        ResourceLocation channel = packet.getIdentifier();
        IPluginChannelHandler handler = this.handlers.get(channel);

        if (handler != null)
        {
            FriendlyByteBuf buf = PacketSplitter.receive(netHandler, packet);

            // Finished the complete packet
            if (buf != null)
            {
                handler.onPacketReceived(buf);
            }

            return true;
        }

        return false;
    }

    private void sendRegisterPacket(ResourceLocation type, List<ResourceLocation> channels)
    {
        String joinedChannels = channels.stream().map(ResourceLocation::toString).collect(Collectors.joining("\0"));
        ByteBuf payload = Unpooled.wrappedBuffer(joinedChannels.getBytes(Charsets.UTF_8));
        ServerboundCustomPayloadPacket packet = new ServerboundCustomPayloadPacket(type, new FriendlyByteBuf(payload));

        ClientPacketListener handler = Minecraft.getInstance().getConnection();

        if (handler != null)
        {
            handler.send(packet);
        }
        else
        {
            MaLiLib.logger.warn("Failed to send register channel packet - network handler was null");
        }
    }
}
