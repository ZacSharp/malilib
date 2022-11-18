package fi.dy.masa.malilib.event;

import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.pipeline.RenderTarget;
import fi.dy.masa.malilib.interfaces.IRenderDispatcher;
import fi.dy.masa.malilib.interfaces.IRenderer;
import fi.dy.masa.malilib.util.InfoUtils;

public class RenderEventHandler implements IRenderDispatcher
{
    private static final RenderEventHandler INSTANCE = new RenderEventHandler();

    private final List<IRenderer> overlayRenderers = new ArrayList<>();
    private final List<IRenderer> tooltipLastRenderers = new ArrayList<>();
    private final List<IRenderer> worldLastRenderers = new ArrayList<>();

    public static IRenderDispatcher getInstance()
    {
        return INSTANCE;
    }

    @Override
    public void registerGameOverlayRenderer(IRenderer renderer)
    {
        if (this.overlayRenderers.contains(renderer) == false)
        {
            this.overlayRenderers.add(renderer);
        }
    }

    @Override
    public void registerTooltipLastRenderer(IRenderer renderer)
    {
        if (this.tooltipLastRenderers.contains(renderer) == false)
        {
            this.tooltipLastRenderers.add(renderer);
        }
    }

    @Override
    public void registerWorldLastRenderer(IRenderer renderer)
    {
        if (this.worldLastRenderers.contains(renderer) == false)
        {
            this.worldLastRenderers.add(renderer);
        }
    }

    /**
     * NOT PUBLIC API - DO NOT CALL
     */
    public void onRenderGameOverlayPost(net.minecraft.client.Minecraft mc, float partialTicks, PoseStack matrixStack)
    {
        mc.getProfiler().push("malilib_rendergameoverlaypost");

        if (this.overlayRenderers.isEmpty() == false)
        {
            for (IRenderer renderer : this.overlayRenderers)
            {
                mc.getProfiler().push(renderer.getProfilerSectionSupplier());
                renderer.onRenderGameOverlayPost(partialTicks, matrixStack);
                mc.getProfiler().pop();
            }
        }

        mc.getProfiler().push("malilib_ingamemessages");
        InfoUtils.renderInGameMessages(matrixStack);
        mc.getProfiler().pop();

        mc.getProfiler().pop();
    }

    /**
     * NOT PUBLIC API - DO NOT CALL
     */
    public void onRenderTooltipLast(com.mojang.blaze3d.vertex.PoseStack matrixStack, net.minecraft.world.item.ItemStack stack, int x, int y)
    {
        if (this.tooltipLastRenderers.isEmpty() == false)
        {
            for (IRenderer renderer : this.tooltipLastRenderers)
            {
                renderer.onRenderTooltipLast(stack, x, y);
            }
        }
    }

    /**
     * NOT PUBLIC API - DO NOT CALL
     */
    public void onRenderWorldLast(com.mojang.blaze3d.vertex.PoseStack matrixStack, net.minecraft.client.Minecraft mc, float partialTicks)
    {
        if (this.worldLastRenderers.isEmpty() == false)
        {
            mc.getProfiler().popPush("malilib_renderworldlast");

            RenderTarget fb = Minecraft.useShaderTransparency() ? mc.levelRenderer.getTranslucentTarget() : null;

            if (fb != null)
            {
                fb.bindWrite(false);
            }

            for (IRenderer renderer : this.worldLastRenderers)
            {
                mc.getProfiler().push(renderer.getProfilerSectionSupplier());
                renderer.onRenderWorldLast(partialTicks, matrixStack);
                mc.getProfiler().pop();
            }

            if (fb != null)
            {
                mc.getMainRenderTarget().bindWrite(false);
            }
        }
    }
}
