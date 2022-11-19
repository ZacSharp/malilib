package fi.dy.masa.malilib.mixin;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.platform.Window;
import fi.dy.masa.malilib.event.InputEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;

@Mixin(MouseHandler.class)
public abstract class MixinMouse
{
    @Shadow @Final private Minecraft minecraft;
    @Shadow private double accumulatedScroll;

    @Inject(method = "onMove",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/MouseHandler;ignoreFirstMove:Z", ordinal = 0))
    private void hookOnMouseMove(long handle, double xpos, double ypos, CallbackInfo ci)
    {
        Window window = this.minecraft.getWindow();
        int mouseX = (int) (((MouseHandler) (Object) this).xpos() * (double) window.getGuiScaledWidth() / (double) window.getScreenWidth());
        int mouseY = (int) (((MouseHandler) (Object) this).ypos() * (double) window.getGuiScaledHeight() / (double) window.getScreenHeight());

        ((InputEventHandler) InputEventHandler.getInputManager()).onMouseMove(mouseX, mouseY);
    }

    @Inject(method = "onScroll", cancellable = true,
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;screen:Lnet/minecraft/client/gui/screens/Screen;", ordinal = 0))
    private void hookOnMouseScroll(long handle, double xOffset, double yOffset, CallbackInfo ci)
    {
        Window window = this.minecraft.getWindow();
        int mouseX = (int) (((MouseHandler) (Object) this).xpos() * (double) window.getGuiScaledWidth() / (double) window.getScreenWidth());
        int mouseY = (int) (((MouseHandler) (Object) this).ypos() * (double) window.getGuiScaledHeight() / (double) window.getScreenHeight());

        if (((InputEventHandler) InputEventHandler.getInputManager()).onMouseScroll(mouseX, mouseY, xOffset, yOffset))
        {
            this.accumulatedScroll = 0.0;
            ci.cancel();
        }
    }

    @Inject(method = "onPress", cancellable = true,
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;ON_OSX:Z", ordinal = 0))
    private void hookOnMouseClick(long handle, final int button, final int action, int mods, CallbackInfo ci)
    {
        Window window = this.minecraft.getWindow();
        int mouseX = (int) (((MouseHandler) (Object) this).xpos() * (double) window.getGuiScaledWidth() / (double) window.getScreenWidth());
        int mouseY = (int) (((MouseHandler) (Object) this).ypos() * (double) window.getGuiScaledHeight() / (double) window.getScreenHeight());
        final boolean keyState = action == GLFW.GLFW_PRESS;

        if (((InputEventHandler) InputEventHandler.getInputManager()).onMouseClick(mouseX, mouseY, button, keyState))
        {
            ci.cancel();
        }
    }
}
