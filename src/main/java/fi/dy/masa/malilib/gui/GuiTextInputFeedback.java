package fi.dy.masa.malilib.gui;

import javax.annotation.Nullable;
import fi.dy.masa.malilib.interfaces.IStringConsumerFeedback;
import net.minecraft.client.gui.screens.Screen;

public class GuiTextInputFeedback extends GuiTextInputBase
{
    protected final IStringConsumerFeedback consumer;

    public GuiTextInputFeedback(int maxTextLength, String titleKey, String defaultText, @Nullable Screen parent, IStringConsumerFeedback consumer)
    {
        super(maxTextLength, titleKey, defaultText, parent);

        this.consumer = consumer;
    }

    @Override
    protected boolean applyValue(String string)
    {
        return this.consumer.setString(this.textField.getValue());
    }
}
