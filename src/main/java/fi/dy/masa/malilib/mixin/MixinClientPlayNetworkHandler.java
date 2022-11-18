package fi.dy.masa.malilib.mixin;

import javax.annotation.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import fi.dy.masa.malilib.event.WorldLoadHandler;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPlayNetworkHandler
{
    @Shadow private Minecraft minecraft;
    @Shadow private ClientLevel level;

    @Nullable private ClientLevel worldBefore;

    @Inject(method = "handleLogin", at = @At("HEAD"))
    private void onPreJoinGameHead(ClientboundLoginPacket packet, CallbackInfo ci)
    {
        // Need to grab the old world reference at the start of the method,
        // because the next injection point is right after the world has been assigned,
        // since we need the new world reference for the callback.
        this.worldBefore = this.level;
    }

    @Inject(method = "handleLogin", at = @At(value = "INVOKE",
                target = "Lnet/minecraft/client/Minecraft;setLevel(" +
                         "Lnet/minecraft/client/multiplayer/ClientLevel;)V"))
    private void onPreGameJoin(ClientboundLoginPacket packet, CallbackInfo ci)
    {
        ((WorldLoadHandler) WorldLoadHandler.getInstance()).onWorldLoadPre(this.worldBefore, this.level, this.minecraft);
    }

    @Inject(method = "handleLogin", at = @At("RETURN"))
    private void onPostGameJoin(ClientboundLoginPacket packet, CallbackInfo ci)
    {
        ((WorldLoadHandler) WorldLoadHandler.getInstance()).onWorldLoadPost(this.worldBefore, this.level, this.minecraft);
        this.worldBefore = null;
    }
}
