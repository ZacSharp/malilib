package fi.dy.masa.malilib.interfaces;

import java.util.function.Supplier;

public interface IRenderer
{
    /**
     * Called after the vanilla overlays have been rendered
     */
    default void onRenderGameOverlayPost(com.mojang.blaze3d.vertex.PoseStack matrixStack) {}

    /**
     * Called after vanilla world rendering
     */
    default void onRenderWorldLast(com.mojang.blaze3d.vertex.PoseStack matrixStack, com.mojang.math.Matrix4f projMatrix) {}

    /**
     * Called after the tooltip text of an item has been rendered
     */
    default void onRenderTooltipLast(net.minecraft.world.item.ItemStack stack, int x, int y) {}

    /**
     * Returns a supplier for the profiler section name that should be used for this renderer
     */
    default Supplier<String> getProfilerSectionSupplier()
    {
        return () -> this.getClass().getName();
    }
}
