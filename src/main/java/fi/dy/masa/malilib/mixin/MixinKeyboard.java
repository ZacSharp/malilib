package fi.dy.masa.malilib.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import fi.dy.masa.malilib.event.InputEventHandler;
import fi.dy.masa.malilib.util.IF3KeyStateSetter;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;

@Mixin(KeyboardHandler.class)
public abstract class MixinKeyboard implements IF3KeyStateSetter
{
    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private boolean handledDebugKey;

    @Override
    public void setF3KeyState(boolean value)
    {
        this.handledDebugKey = value;
    }

    @Inject(method = "keyPress", cancellable = true,
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/KeyboardHandler;debugCrashKeyTime:J", ordinal = 0))
    private void onKeyboardInput(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci)
    {
        if (((InputEventHandler) InputEventHandler.getInputManager()).onKeyInput(key, scanCode, modifiers, action != 0))
        {
            ci.cancel();
        }
    }
}
