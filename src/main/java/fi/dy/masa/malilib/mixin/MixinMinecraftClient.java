package fi.dy.masa.malilib.mixin;

import javax.annotation.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import fi.dy.masa.malilib.event.WorldLoadHandler;

@Mixin(Minecraft.class)
public abstract class MixinMinecraftClient
{
    @Shadow public ClientWorld level;

    private ClientWorld worldBefore;

    @Inject(method = "setLevel(Lnet/minecraft/client/world/ClientWorld;)V", at = @At("HEAD"))
    private void onLoadWorldPre(@Nullable ClientWorld worldClientIn, CallbackInfo ci)
    {
        // Only handle dimension changes/respawns here.
        // The initial join is handled in MixinClientPlayNetworkHandler onGameJoin
        if (this.level != null)
        {
            this.worldBefore = this.level;
            ((WorldLoadHandler) WorldLoadHandler.getInstance()).onWorldLoadPre(this.level, worldClientIn, (Minecraft)(Object) this);
        }
    }

    @Inject(method = "setLevel(Lnet/minecraft/client/world/ClientWorld;)V", at = @At("RETURN"))
    private void onLoadWorldPost(@Nullable ClientWorld worldClientIn, CallbackInfo ci)
    {
        if (this.worldBefore != null)
        {
            ((WorldLoadHandler) WorldLoadHandler.getInstance()).onWorldLoadPost(this.worldBefore, worldClientIn, (Minecraft)(Object) this);
            this.worldBefore = null;
        }
    }

    @Inject(method = "clearLevel(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("HEAD"))
    private void onDisconnectPre(Screen screen, CallbackInfo ci)
    {
        this.worldBefore = this.level;
        ((WorldLoadHandler) WorldLoadHandler.getInstance()).onWorldLoadPre(this.worldBefore, null, (Minecraft)(Object) this);
    }

    @Inject(method = "clearLevel(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("RETURN"))
    private void onDisconnectPost(Screen screen, CallbackInfo ci)
    {
        ((WorldLoadHandler) WorldLoadHandler.getInstance()).onWorldLoadPost(this.worldBefore, null, (Minecraft)(Object) this);
        this.worldBefore = null;
    }
}
