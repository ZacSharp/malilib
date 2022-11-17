package fi.dy.masa.malilib.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.text.StringTextComponent;

public class GuiTextFieldGeneric extends TextFieldWidget
{
    protected int x;
    protected int y;
    protected int width;
    protected int height;

    public GuiTextFieldGeneric(int x, int y, int width, int height, FontRenderer textRenderer)
    {
        super(textRenderer, x, y, width, height, new StringTextComponent(""));

        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        this.setMaxLength(256);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton)
    {
        boolean ret = super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseButton == 1 && this.isMouseOver((int) mouseX, (int) mouseY))
        {
            this.setValue("");
            this.setFocused(true);
            return true;
        }

        return ret;
    }

    public int getX()
    {
        return this.x;
    }

    public int getY()
    {
        return this.y;
    }

    public void setX(int x)
    {
        this.x = x;
    }

    public void setY(int y)
    {
        this.y = y;
    }

    public boolean isMouseOver(int mouseX, int mouseY)
    {
        return mouseX >= this.x && mouseX < this.x + this.width &&
               mouseY >= this.y && mouseY < this.y + this.height;
    }

    @Override
    public void setFocused(boolean isFocusedIn)
    {
        boolean wasFocused = this.isFocused();
        super.setFocused(isFocusedIn);

        if (this.isFocused() != wasFocused)
        {
            Minecraft.getInstance().keyboardHandler.setSendRepeatsToGui(this.isFocused());
        }
    }

    public GuiTextFieldGeneric setZLevel(int zLevel)
    {
        this.setBlitOffset(zLevel);
        return this;
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        if (this.getBlitOffset() != 0)
        {
            RenderSystem.pushMatrix();
            RenderSystem.translatef(0, 0, this.getBlitOffset());

            super.render(matrixStack, mouseX, mouseY, partialTicks);

            RenderSystem.popMatrix();
        }
        else
        {
            super.render(matrixStack, mouseX, mouseY, partialTicks);
        }
    }
}
