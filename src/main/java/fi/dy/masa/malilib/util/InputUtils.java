package fi.dy.masa.malilib.util;

import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;

public class InputUtils
{
    public static int getMouseX()
    {
        Minecraft mc = Minecraft.getInstance();
        MainWindow window = mc.getWindow();
        return (int) (mc.mouseHandler.xpos() * (double) window.getGuiScaledWidth() / (double) window.getScreenWidth());
    }

    public static int getMouseY()
    {
        Minecraft mc = Minecraft.getInstance();
        MainWindow window = mc.getWindow();
        return (int) (mc.mouseHandler.ypos() * (double) window.getGuiScaledHeight() / (double) window.getScreenHeight());
    }
}
