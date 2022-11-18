package fi.dy.masa.malilib.network;

import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface IPluginChannelHandler
{
    List<ResourceLocation> getChannels();

    void onPacketReceived(FriendlyByteBuf buf);
}
