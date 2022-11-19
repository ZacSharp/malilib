package fi.dy.masa.malilib.mixin;

import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fi.dy.masa.malilib.config.ConfigManager;
import fi.dy.masa.malilib.event.InitializationHandler;
import fi.dy.masa.malilib.event.TickHandler;
import fi.dy.masa.malilib.event.WorldLoadHandler;
import fi.dy.masa.malilib.hotkeys.KeybindMulti;

@Mixin(Minecraft.class)
public abstract class MixinMinecraftClient
{
    @Shadow
    public ClientLevel level;

    private ClientLevel worldBefore;

    @Inject(method = "<init>(Lnet/minecraft/client/main/GameConfig;)V", at = @At("RETURN"))
    private void onInitComplete(GameConfig args, CallbackInfo ci)
    {
        // Register all mod handlers
        ((InitializationHandler) InitializationHandler.getInstance()).onGameInitDone();
    }

    @Inject(method = "stop()V", at = @At("RETURN"))
    private void onStop(CallbackInfo ci)
    {
        ((ConfigManager) ConfigManager.getInstance()).saveAllConfigs();
    }

    @Inject(method = "tick()V", at = @At("RETURN"))
    private void onPostKeyboardInput(CallbackInfo ci)
    {
        KeybindMulti.reCheckPressedKeys();
        TickHandler.getInstance().onClientTick((Minecraft)(Object) this);
    }

    @Inject(method = "setLevel(Lnet/minecraft/client/multiplayer/ClientLevel;)V", at = @At("HEAD"))
    private void onLoadWorldPre(@Nullable ClientLevel worldClientIn, CallbackInfo ci)
    {
        // Only handle dimension changes/respawns here.
        // The initial join is handled in MixinClientPlayNetworkHandler onGameJoin 
        if (this.level != null)
        {
            this.worldBefore = this.level;
            ((WorldLoadHandler) WorldLoadHandler.getInstance()).onWorldLoadPre(this.level, worldClientIn, (Minecraft)(Object) this);
        }
    }

    @Inject(method = "setLevel(Lnet/minecraft/client/multiplayer/ClientLevel;)V", at = @At("RETURN"))
    private void onLoadWorldPost(@Nullable ClientLevel worldClientIn, CallbackInfo ci)
    {
        if (this.worldBefore != null)
        {
            ((WorldLoadHandler) WorldLoadHandler.getInstance()).onWorldLoadPost(this.worldBefore, worldClientIn, (Minecraft)(Object) this);
            this.worldBefore = null;
        }
    }

    @Inject(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"))
    private void onDisconnectPre(Screen screen, CallbackInfo ci)
    {
        this.worldBefore = this.level;
        ((WorldLoadHandler) WorldLoadHandler.getInstance()).onWorldLoadPre(this.worldBefore, null, (Minecraft)(Object) this);
    }

    @Inject(method = "clearLevel(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("RETURN"))
    private void onDisconnectPost(Screen screen, CallbackInfo ci)
    {
        ((WorldLoadHandler) WorldLoadHandler.getInstance()).onWorldLoadPost(this.worldBefore, null, (Minecraft)(Object) this);
        this.worldBefore = null;
    }
}
