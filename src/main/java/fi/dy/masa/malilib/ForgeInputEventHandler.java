package fi.dy.masa.malilib;

import net.minecraft.client.Minecraft;
import fi.dy.masa.malilib.event.InputEventHandler;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

class ForgeInputEventHandler
{
    @SubscribeEvent
    public void onKeyboardInput(InputEvent.KeyInputEvent event)
    {
        if (Minecraft.getInstance().currentScreen == null)
        {
            // This event isn't cancellable, and is fired after vanilla key handling >_>
            // So this one needs to be handled with a Mixin
            boolean allowRepeat = MaLiLibConfigs.Generic.KEY_EVENT_ALLOW_REPEAT.getBooleanValue();
            int action = event.getAction();

            if (allowRepeat == false && action == 2)
            {
                return;
            }

            boolean press = action == 1 || (allowRepeat && action == 2);

            ((InputEventHandler) InputEventHandler.getInputManager()).onKeyInput(event.getKey(), event.getScanCode(), event.getModifiers(), press);
        }
    }

    @SubscribeEvent
    public void onMouseInputEvent(InputEvent.RawMouseEvent event)
    {
        int mouseX = 0;
        int mouseY = 0;

        if (Minecraft.getInstance().currentScreen == null &&
            ((InputEventHandler) InputEventHandler.getInputManager()).onMouseClick(mouseX, mouseY, event.getButton(), event.getAction() != 0))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onMouseScrollEvent(InputEvent.MouseScrollEvent event)
    {
        if (((InputEventHandler) InputEventHandler.getInputManager()).onMouseScroll((int) event.getMouseX(), (int) event.getMouseY(), event.getScrollDelta()))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGuiKeyboardKeyPressPre(GuiScreenEvent.KeyboardKeyPressedEvent.Pre event)
    {
        if (((InputEventHandler) InputEventHandler.getInputManager()).onKeyInput(event.getKeyCode(), event.getScanCode(), event.getModifiers(), true))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGuiKeyboardKeyReleasePre(GuiScreenEvent.KeyboardKeyReleasedEvent.Pre event)
    {
        if (((InputEventHandler) InputEventHandler.getInputManager()).onKeyInput(event.getKeyCode(), event.getScanCode(), event.getModifiers(), false))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGuiMouseClickPre(GuiScreenEvent.MouseClickedEvent.Pre event)
    {
        if (((InputEventHandler) InputEventHandler.getInputManager()).onMouseClick((int) event.getMouseX(), (int) event.getMouseY(), event.getButton(), true))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGuiMouseReleasePre(GuiScreenEvent.MouseReleasedEvent.Pre event)
    {
        if (((InputEventHandler) InputEventHandler.getInputManager()).onMouseClick((int) event.getMouseX(), (int) event.getMouseY(), event.getButton(), false))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGuiMouseScrolledPre(GuiScreenEvent.MouseScrollEvent.Pre event)
    {
        if (((InputEventHandler) InputEventHandler.getInputManager()).onMouseScroll((int) event.getMouseX(), (int) event.getMouseY(), event.getScrollDelta()))
        {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onGuiMouseMovedPre(GuiScreenEvent.MouseDragEvent.Pre event)
    {
        ((InputEventHandler) InputEventHandler.getInputManager()).onMouseMove((int) event.getMouseX(), (int) event.getMouseY());
    }
}