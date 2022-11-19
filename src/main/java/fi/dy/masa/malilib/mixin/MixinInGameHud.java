package fi.dy.masa.malilib.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import fi.dy.masa.malilib.event.RenderEventHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

@Mixin(Gui.class)
public abstract class MixinInGameHud
{
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "render", at = @At("RETURN"))
    private void onGameOverlayPost(PoseStack matrixStack, float partialTicks, CallbackInfo ci)
    {
        ((RenderEventHandler) RenderEventHandler.getInstance()).onRenderGameOverlayPost(matrixStack, this.minecraft, partialTicks);
    }
}
