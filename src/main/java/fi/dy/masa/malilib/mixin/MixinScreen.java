package fi.dy.masa.malilib.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.vertex.PoseStack;
import fi.dy.masa.malilib.event.RenderEventHandler;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;

@Mixin(Screen.class)
public abstract class MixinScreen extends AbstractContainerEventHandler
{
    @Inject(method = "renderTooltip(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/item/ItemStack;II)V", at = @At("RETURN"))
    private void onRenderTooltip(PoseStack matrixStack, ItemStack stack, int x, int y, CallbackInfo ci)
    {
        ((RenderEventHandler) RenderEventHandler.getInstance()).onRenderTooltipLast(matrixStack, stack, x, y);
    }
}
