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
}
