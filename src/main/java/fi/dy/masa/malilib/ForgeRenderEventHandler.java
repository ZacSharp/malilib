package fi.dy.masa.malilib;

import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import fi.dy.masa.malilib.event.RenderEventHandler;

class ForgeRenderEventHandler
{
    @SubscribeEvent
    public void onRenderGameOverlayPost(RenderGameOverlayEvent.Post event)
    {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL)
        {
            ((RenderEventHandler) RenderEventHandler.getInstance()).onRenderGameOverlayPost(event.getMatrixStack(), net.minecraft.client.Minecraft.getInstance(), event.getPartialTicks());
        }
    }

    @SubscribeEvent
    public void onRenderTooltipPost(RenderTooltipEvent.Pre event)
    {
        ((RenderEventHandler) RenderEventHandler.getInstance()).onRenderTooltipLast(event.getMatrixStack(), event.getStack(), event.getX(), event.getY());
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event)
    {
        net.minecraft.client.Camera cam = net.minecraft.client.Minecraft.getInstance().gameRenderer.getMainCamera();
        float yaw = cam.getYRot();
        float pitch = cam.getXRot();
        event.getMatrixStack().pushPose();
//        event.getMatrixStack().glRotatef(pitch, 1, 0, 0);
//        event.getMatrixStack().glRotatef(yaw+180, 0, 1, 0);
        ((RenderEventHandler) RenderEventHandler.getInstance()).onRenderWorldLast(event.getMatrixStack(), event.getProjectionMatrix(), net.minecraft.client.Minecraft.getInstance());
        event.getMatrixStack().popPose();
    }
}
