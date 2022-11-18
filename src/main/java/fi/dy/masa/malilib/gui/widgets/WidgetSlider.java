package fi.dy.masa.malilib.gui.widgets;

import com.mojang.blaze3d.vertex.PoseStack;
import fi.dy.masa.malilib.gui.interfaces.ISliderCallback;
import fi.dy.masa.malilib.render.RenderUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class WidgetSlider extends WidgetBase
{
    public static final ResourceLocation VANILLA_WIDGETS = new ResourceLocation("textures/gui/widgets.png");

    protected final ISliderCallback callback;
    protected int sliderWidth;
    protected int lastMouseX;
    protected boolean dragging;

    public WidgetSlider(int x, int y, int width, int height, ISliderCallback callback)
    {
        super(x, y, width, height);

        this.callback = callback;
        int usableWidth = this.width - 4;
        this.sliderWidth = Mth.clamp(usableWidth / callback.getMaxSteps(), 8, usableWidth / 2);
    }

    @Override
    protected boolean onMouseClickedImpl(int mouseX, int mouseY, int mouseButton)
    {
        this.callback.setValueRelative(this.getRelativePosition(mouseX));
        this.lastMouseX = mouseX;
        this.dragging = true;

        return true;
    }

    @Override
    public void onMouseReleasedImpl(int mouseX, int mouseY, int mouseButton)
    {
        this.dragging = false;
    }

    @Override
    public void render(int mouseX, int mouseY, boolean selected, PoseStack matrixStack)
    {
        if (this.dragging && mouseX != this.lastMouseX)
        {
            this.callback.setValueRelative(this.getRelativePosition(mouseX));
            this.lastMouseX = mouseX;
        }

        this.bindTexture(VANILLA_WIDGETS);
        RenderUtils.color(1f, 1f, 1f, 1f);

        RenderUtils.drawTexturedRect(this.x + 1             , this.y,   0, 46, this.width - 6, 20);
        RenderUtils.drawTexturedRect(this.x + this.width - 5, this.y, 196, 46,              4, 20);

        double relPos = this.callback.getValueRelative();
        int sw = this.sliderWidth;
        int usableWidth = this.width - 4 - sw;
        int s = sw / 2;

        RenderUtils.drawTexturedRect(this.x + 2 + (int) (relPos * usableWidth)    , this.y,       0, 66, s, 20);
        RenderUtils.drawTexturedRect(this.x + 2 + (int) (relPos * usableWidth) + s, this.y, 200 - s, 66, s, 20);

        String str = this.callback.getFormattedDisplayValue();
        int w = this.getStringWidth(str);
        this.drawString(this.x + (this.width / 2) - w / 2, this.y + 6, 0xFFFFFFA0, str, matrixStack);
    }

    protected double getRelativePosition(int mouseX)
    {
        int relPos = mouseX - this.x - this.sliderWidth / 2;
        return Mth.clamp((double) relPos / (double) (this.width - this.sliderWidth - 4), 0, 1);
    }
}
