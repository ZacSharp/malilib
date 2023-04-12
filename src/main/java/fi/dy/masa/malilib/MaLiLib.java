package fi.dy.masa.malilib;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmlclient.ConfigGuiHandler.ConfigGuiFactory;
import net.minecraftforge.fmllegacy.network.FMLNetworkConstants;
import fi.dy.masa.malilib.event.InitializationHandler;

@Mod(MaLiLibReference.MOD_ID)
public class MaLiLib
{
    public static final Logger logger = LogManager.getLogger(MaLiLibReference.MOD_ID);

    public MaLiLib()
    {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onInterModProcess);
    }

    private void onClientSetup(final FMLClientSetupEvent event)
    {
        // Make sure the mod being absent on the other network side does not cause
        // the client to display the server as incompatible
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> FMLNetworkConstants.IGNORESERVERONLY, (incoming, isNetwork) -> true));

        // Make the "Config" button in the mod list open the config gui
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiFactory.class, () -> new ConfigGuiFactory((mc, parent) -> {
                MaLiLibConfigGui gui = new MaLiLibConfigGui() {
                        @Override
                        public void render(com.mojang.blaze3d.vertex.PoseStack matrixStack, int mouseX, int mouseY, float partialTicks)
                        {
                            matrixStack.translate(0, 0, -1);
                            parent.render(matrixStack, 0, 0, partialTicks);
                            matrixStack.translate(0, 0, 1);
                            super.render(matrixStack, mouseX, mouseY, partialTicks);
                        }
                };
                gui.setParentGui(parent);
                return gui;
        }));

        MinecraftForge.EVENT_BUS.register(new ForgeInputEventHandler());
        MinecraftForge.EVENT_BUS.register(new ForgeRenderEventHandler());
        MinecraftForge.EVENT_BUS.register(new ForgeTickEventHandler());

        InitializationHandler.getInstance().registerInitializationHandler(new MaLiLibInitHandler());
    }

    private void onInterModProcess(final InterModProcessEvent event)
    {
        ((InitializationHandler) InitializationHandler.getInstance()).onGameInitDone();
    }
}