package fi.dy.masa.malilib.render;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.Vec3;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import fi.dy.masa.malilib.config.HudAlignment;
import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.util.Color4f;
import fi.dy.masa.malilib.util.GuiUtils;
import fi.dy.masa.malilib.util.IntBoundingBox;
import fi.dy.masa.malilib.util.InventoryUtils;
import fi.dy.masa.malilib.util.PositionUtils;
import fi.dy.masa.malilib.util.PositionUtils.HitPart;

public class RenderUtils
{
    public static final ResourceLocation TEXTURE_MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");
    public static final ResourceLocation TEXTURE_MAP_BACKGROUND_CHECKERBOARD = new ResourceLocation("textures/map/map_background_checkerboard.png");

    private static final Random RAND = new Random();
    //private static final Vec3d LIGHT0_POS = (new Vec3d( 0.2D, 1.0D, -0.7D)).normalize();
    //private static final Vec3d LIGHT1_POS = (new Vec3d(-0.2D, 1.0D,  0.7D)).normalize();

    public static void setupBlend()
    {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    }

    public static void setupBlendSimple()
    {
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
    }

    public static void bindTexture(ResourceLocation texture)
    {
        RenderSystem.setShaderTexture(0, texture);
    }

    public static void color(float r, float g, float b, float a)
    {
        RenderSystem.setShaderColor(r, g, b, a);
    }

    public static void disableDiffuseLighting()
    {
        // FIXME 1.15-pre4+
        Lighting.setupForFlatItems();
    }

    public static void enableDiffuseLightingForLevel(PoseStack matrixStack)
    {
        Lighting.setupNetherLevel(matrixStack.last().pose());
    }

    public static void enableDiffuseLightingGui3D()
    {
        // FIXME 1.15-pre4+
        Lighting.setupFor3DItems();
    }

    public static void drawOutlinedBox(int x, int y, int width, int height, int colorBg, int colorBorder)
    {
        drawOutlinedBox(x, y, width, height, colorBg, colorBorder, 0f);
    }

    public static void drawOutlinedBox(int x, int y, int width, int height, int colorBg, int colorBorder, float zLevel)
    {
        // Draw the background
        drawRect(x, y, width, height, colorBg, zLevel);

        // Draw the border
        drawOutline(x - 1, y - 1, width + 2, height + 2, colorBorder, zLevel);
    }

    public static void drawOutline(int x, int y, int width, int height, int colorBorder)
    {
        drawOutline(x, y, width, height, colorBorder, 0f);
    }

    public static void drawOutline(int x, int y, int width, int height, int colorBorder, float zLevel)
    {
        drawRect(x                    , y,      1, height, colorBorder, zLevel); // left edge
        drawRect(x + width - 1        , y,      1, height, colorBorder, zLevel); // right edge
        drawRect(x + 1,              y, width - 2,      1, colorBorder, zLevel); // top edge
        drawRect(x + 1, y + height - 1, width - 2,      1, colorBorder, zLevel); // bottom edge
    }

    public static void drawOutline(int x, int y, int width, int height, int borderWidth, int colorBorder)
    {
        drawOutline(x, y, width, height, borderWidth, colorBorder, 0f);
    }

    public static void drawOutline(int x, int y, int width, int height, int borderWidth, int colorBorder, float zLevel)
    {
        drawRect(x                      ,                        y, borderWidth            , height     , colorBorder, zLevel); // left edge
        drawRect(x + width - borderWidth,                        y, borderWidth            , height     , colorBorder, zLevel); // right edge
        drawRect(x + borderWidth        ,                        y, width - 2 * borderWidth, borderWidth, colorBorder, zLevel); // top edge
        drawRect(x + borderWidth        , y + height - borderWidth, width - 2 * borderWidth, borderWidth, colorBorder, zLevel); // bottom edge
    }

    public static void drawTexturedRect(int x, int y, int u, int v, int width, int height)
    {
        drawTexturedRect(x, y, u, v, width, height, 0);
    }

    public static void drawRect(int x, int y, int width, int height, int color)
    {
        drawRect(x, y, width, height, color, 0f);
    }

    public static void drawRect(int x, int y, int width, int height, int color, float zLevel)
    {
        float a = (float) (color >> 24 & 255) / 255.0F;
        float r = (float) (color >> 16 & 255) / 255.0F;
        float g = (float) (color >>  8 & 255) / 255.0F;
        float b = (float) (color & 255) / 255.0F;

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.applyModelViewMatrix();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        setupBlend();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        buffer.vertex(x        , y         , zLevel).color(r, g, b, a).endVertex();
        buffer.vertex(x        , y + height, zLevel).color(r, g, b, a).endVertex();
        buffer.vertex(x + width, y + height, zLevel).color(r, g, b, a).endVertex();
        buffer.vertex(x + width, y         , zLevel).color(r, g, b, a).endVertex();

        tessellator.end();

        RenderSystem.disableBlend();
    }

    public static void drawTexturedRect(int x, int y, int u, int v, int width, int height, float zLevel)
    {
        float pixelWidth = 0.00390625F;
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.applyModelViewMatrix();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        setupBlend();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        buffer.vertex(x        , y + height, zLevel).uv( u          * pixelWidth, (v + height) * pixelWidth).endVertex();
        buffer.vertex(x + width, y + height, zLevel).uv((u + width) * pixelWidth, (v + height) * pixelWidth).endVertex();
        buffer.vertex(x + width, y         , zLevel).uv((u + width) * pixelWidth,  v           * pixelWidth).endVertex();
        buffer.vertex(x        , y         , zLevel).uv( u          * pixelWidth,  v           * pixelWidth).endVertex();

        tessellator.end();
    }

    public static void drawTexturedRectBatched(int x, int y, int u, int v, int width, int height, BufferBuilder buffer)
    {
        drawTexturedRectBatched(x, y, u, v, width, height, 0, buffer);
    }

    public static void drawTexturedRectBatched(int x, int y, int u, int v, int width, int height, float zLevel, BufferBuilder buffer)
    {
        float pixelWidth = 0.00390625F;

        buffer.vertex(x        , y + height, zLevel).uv( u          * pixelWidth, (v + height) * pixelWidth).endVertex();
        buffer.vertex(x + width, y + height, zLevel).uv((u + width) * pixelWidth, (v + height) * pixelWidth).endVertex();
        buffer.vertex(x + width, y         , zLevel).uv((u + width) * pixelWidth,  v           * pixelWidth).endVertex();
        buffer.vertex(x        , y         , zLevel).uv( u          * pixelWidth,  v           * pixelWidth).endVertex();
    }

    public static void drawHoverText(int x, int y, List<String> textLines, PoseStack matrixStack)
    {
        Minecraft mc = mc();

        if (textLines.isEmpty() == false && GuiUtils.getCurrentScreen() != null)
        {
            Font font = mc.font;
            disableDiffuseLighting();
            RenderSystem.disableDepthTest();
            int maxLineLength = 0;
            int maxWidth = GuiUtils.getCurrentScreen().width;
            List<String> linesNew = new ArrayList<>();

            for (String lineOrig : textLines)
            {
                String[] lines = lineOrig.split("\\n");

                for (String line : lines)
                {
                    int length = font.width(line);

                    if (length > maxLineLength)
                    {
                        maxLineLength = length;
                    }

                    linesNew.add(line);
                }
            }

            textLines = linesNew;

            final int lineHeight = font.lineHeight + 1;
            int textHeight = textLines.size() * lineHeight - 2;
            int textStartX = x + 4;
            int textStartY = Math.max(8, y - textHeight - 6);

            if (textStartX + maxLineLength + 6 > maxWidth)
            {
                textStartX = Math.max(2, maxWidth - maxLineLength - 8);
            }

            double zLevel = 300;
            int borderColor = 0xF0100010;
            drawGradientRect(textStartX - 3, textStartY - 4, textStartX + maxLineLength + 3, textStartY - 3, zLevel, borderColor, borderColor);
            drawGradientRect(textStartX - 3, textStartY + textHeight + 3, textStartX + maxLineLength + 3, textStartY + textHeight + 4, zLevel, borderColor, borderColor);
            drawGradientRect(textStartX - 3, textStartY - 3, textStartX + maxLineLength + 3, textStartY + textHeight + 3, zLevel, borderColor, borderColor);
            drawGradientRect(textStartX - 4, textStartY - 3, textStartX - 3, textStartY + textHeight + 3, zLevel, borderColor, borderColor);
            drawGradientRect(textStartX + maxLineLength + 3, textStartY - 3, textStartX + maxLineLength + 4, textStartY + textHeight + 3, zLevel, borderColor, borderColor);

            int fillColor1 = 0x505000FF;
            int fillColor2 = 0x5028007F;
            drawGradientRect(textStartX - 3, textStartY - 3 + 1, textStartX - 3 + 1, textStartY + textHeight + 3 - 1, zLevel, fillColor1, fillColor2);
            drawGradientRect(textStartX + maxLineLength + 2, textStartY - 3 + 1, textStartX + maxLineLength + 3, textStartY + textHeight + 3 - 1, zLevel, fillColor1, fillColor2);
            drawGradientRect(textStartX - 3, textStartY - 3, textStartX + maxLineLength + 3, textStartY - 3 + 1, zLevel, fillColor1, fillColor1);
            drawGradientRect(textStartX - 3, textStartY + textHeight + 2, textStartX + maxLineLength + 3, textStartY + textHeight + 3, zLevel, fillColor2, fillColor2);

            for (int i = 0; i < textLines.size(); ++i)
            {
                String str = textLines.get(i);
                font.drawShadow(matrixStack, str, textStartX, textStartY, 0xFFFFFFFF);
                textStartY += lineHeight;
            }

            RenderSystem.enableDepthTest();
            enableDiffuseLightingGui3D();
        }
    }

    public static void drawGradientRect(int left, int top, int right, int bottom, double zLevel, int startColor, int endColor)
    {
        int sa = (startColor >> 24 & 0xFF);
        int sr = (startColor >> 16 & 0xFF);
        int sg = (startColor >>  8 & 0xFF);
        int sb = (startColor & 0xFF);

        int ea = (endColor >> 24 & 0xFF);
        int er = (endColor >> 16 & 0xFF);
        int eg = (endColor >>  8 & 0xFF);
        int eb = (endColor & 0xFF);

        RenderSystem.disableTexture();
        setupBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.applyModelViewMatrix();

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        buffer.vertex(right, top,    zLevel).color(sr, sg, sb, sa).endVertex();
        buffer.vertex(left,  top,    zLevel).color(sr, sg, sb, sa).endVertex();
        buffer.vertex(left,  bottom, zLevel).color(er, eg, eb, ea).endVertex();
        buffer.vertex(right, bottom, zLevel).color(er, eg, eb, ea).endVertex();

        tessellator.end();

        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
    }

    public static void drawCenteredString(int x, int y, int color, String text, PoseStack matrixStack)
    {
        Font textRenderer = mc().font;
        textRenderer.drawShadow(matrixStack, text, x - textRenderer.width(text) / 2, y, color);
    }

    public static void drawHorizontalLine(int x, int y, int width, int color)
    {
        drawRect(x, y, width, 1, color);
    }

    public static void drawVerticalLine(int x, int y, int height, int color)
    {
        drawRect(x, y, 1, height, color);
    }

    public static void renderSprite(int x, int y, int width, int height, ResourceLocation atlas, ResourceLocation texture, PoseStack matrixStack)
    {
        if (texture != null)
        {
            TextureAtlasSprite sprite = mc().getTextureAtlas(atlas).apply(texture);
            GuiComponent.blit(matrixStack, x, y, 0, width, height, sprite);//.drawTexturedRect(x, y, sprite, width, height);
        }
    }

    public static void renderText(int x, int y, int color, String text, PoseStack matrixStack)
    {
        String[] parts = text.split("\\\\n");
        Font textRenderer = mc().font;

        for (String line : parts)
        {
            textRenderer.drawShadow(matrixStack, line, x, y, color);
            y += textRenderer.lineHeight + 1;
        }
    }

    public static void renderText(int x, int y, int color, List<String> lines, PoseStack matrixStack)
    {
        if (lines.isEmpty() == false)
        {
            Font textRenderer = mc().font;

            for (String line : lines)
            {
                textRenderer.draw(matrixStack, line, x, y, color);
                y += textRenderer.lineHeight + 2;
            }
        }
    }

    public static int renderText(int xOff, int yOff, double scale, int textColor, int bgColor,
            HudAlignment alignment, boolean useBackground, boolean useShadow, List<String> lines,
            PoseStack matrixStack)
    {
        Font fontRenderer = mc().font;
        final int scaledWidth = GuiUtils.getScaledWindowWidth();
        final int lineHeight = fontRenderer.lineHeight + 2;
        final int contentHeight = lines.size() * lineHeight - 2;
        final int bgMargin = 2;

        // Only Chuck Norris can divide by zero
        if (scale < 0.0125)
        {
            return 0;
        }

        PoseStack globalStack = RenderSystem.getModelViewStack();
        boolean scaled = scale != 1.0;

        if (scaled)
        {
            if (scale != 0)
            {
                xOff = (int) (xOff * scale);
                yOff = (int) (yOff * scale);
            }

            globalStack.pushPose();
            globalStack.scale((float) scale, (float) scale, 1.0f);
            RenderSystem.applyModelViewMatrix();
        }

        double posX = xOff + bgMargin;
        double posY = yOff + bgMargin;

        posY = getHudPosY((int) posY, yOff, contentHeight, scale, alignment);
        posY += getHudOffsetForPotions(alignment, scale, mc().player);

        for (String line : lines)
        {
            final int width = fontRenderer.width(line);

            switch (alignment)
            {
                case TOP_RIGHT:
                case BOTTOM_RIGHT:
                    posX = (scaledWidth / scale) - width - xOff - bgMargin;
                    break;
                case CENTER:
                    posX = (scaledWidth / scale / 2) - (width / 2) - xOff;
                    break;
                default:
            }

            final int x = (int) posX;
            final int y = (int) posY;
            posY += lineHeight;

            if (useBackground)
            {
                drawRect(x - bgMargin, y - bgMargin, width + bgMargin, bgMargin + fontRenderer.lineHeight, bgColor);
            }

            if (useShadow)
            {
                fontRenderer.drawShadow(matrixStack, line, x, y, textColor);
            }
            else
            {
                fontRenderer.draw(matrixStack, line, x, y, textColor);
            }
        }

        if (scaled)
        {
            globalStack.popPose();
            RenderSystem.applyModelViewMatrix();
        }

        return contentHeight + bgMargin * 2;
    }

    public static int getHudOffsetForPotions(HudAlignment alignment, double scale, Player player)
    {
        if (alignment == HudAlignment.TOP_RIGHT)
        {
            // Only Chuck Norris can divide by zero
            if (scale == 0d)
            {
                return 0;
            }

            Collection<MobEffectInstance> effects = player.getActiveEffects();

            if (effects.isEmpty() == false)
            {
                int y1 = 0;
                int y2 = 0;

                for (MobEffectInstance effectInstance : effects)
                {
                    MobEffect effect = effectInstance.getEffect();

                    if (effectInstance.isVisible() && effectInstance.showIcon())
                    {
                        if (effect.isBeneficial())
                        {
                            y1 = 26;
                        }
                        else
                        {
                            y2 = 52;
                            break;
                        }
                    }
                }

                return (int) (Math.max(y1, y2) / scale);
            }
        }

        return 0;
    }

    public static int getHudPosY(int yOrig, int yOffset, int contentHeight, double scale, HudAlignment alignment)
    {
        int scaledHeight = GuiUtils.getScaledWindowHeight();
        int posY = yOrig;

        switch (alignment)
        {
            case BOTTOM_LEFT:
            case BOTTOM_RIGHT:
                posY = (int) ((scaledHeight / scale) - contentHeight - yOffset);
                break;
            case CENTER:
                posY = (int) ((scaledHeight / scale / 2.0d) - (contentHeight / 2.0d) + yOffset);
                break;
            default:
        }

        return posY;
    }

    /**
     * Assumes a BufferBuilder in GL_QUADS mode has been initialized
     */
    public static void drawBlockBoundingBoxSidesBatchedQuads(BlockPos pos, Color4f color, double expand, BufferBuilder buffer)
    {
        double minX = pos.getX() - expand;
        double minY = pos.getY() - expand;
        double minZ = pos.getZ() - expand;
        double maxX = pos.getX() + expand + 1;
        double maxY = pos.getY() + expand + 1;
        double maxZ = pos.getZ() + expand + 1;

        drawBoxAllSidesBatchedQuads(minX, minY, minZ, maxX, maxY, maxZ, color, buffer);
    }

    /**
     * Assumes a BufferBuilder in GL_LINES mode has been initialized
     */
    public static void drawBlockBoundingBoxOutlinesBatchedLines(BlockPos pos, Color4f color, double expand, BufferBuilder buffer)
    {
        drawBlockBoundingBoxOutlinesBatchedLines(pos, Vec3.ZERO, color, expand, buffer);
    }

    /**
     * Assumes a BufferBuilder in GL_LINES mode has been initialized.
     * The cameraPos value will be subtracted from the absolute coordinate values of the passed in BlockPos.
     * @param pos
     * @param cameraPos
     * @param color
     * @param expand
     * @param buffer
     */
    public static void drawBlockBoundingBoxOutlinesBatchedLines(BlockPos pos, Vec3 cameraPos, Color4f color, double expand, BufferBuilder buffer)
    {
        double minX = pos.getX() - expand - cameraPos.x;
        double minY = pos.getY() - expand - cameraPos.y;
        double minZ = pos.getZ() - expand - cameraPos.z;
        double maxX = pos.getX() + expand - cameraPos.x + 1;
        double maxY = pos.getY() + expand - cameraPos.y + 1;
        double maxZ = pos.getZ() + expand - cameraPos.z + 1;

        drawBoxAllEdgesBatchedLines(minX, minY, minZ, maxX, maxY, maxZ, color, buffer);
    }

    /**
     * Assumes a BufferBuilder in GL_QUADS mode has been initialized
     */
    public static void drawBoxAllSidesBatchedQuads(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
            Color4f color, BufferBuilder buffer)
    {
        drawBoxHorizontalSidesBatchedQuads(minX, minY, minZ, maxX, maxY, maxZ, color, buffer);
        drawBoxTopBatchedQuads(minX, minZ, maxX, maxY, maxZ, color, buffer);
        drawBoxBottomBatchedQuads(minX, minY, minZ, maxX, maxZ, color, buffer);
    }

    /**
     * Draws a box with outlines around the given corner positions.
     * Takes in buffers initialized for GL_QUADS and GL_LINES modes.
     * @param posMin
     * @param posMax
     * @param colorLines
     * @param colorSides
     * @param bufferQuads
     * @param bufferLines
     */
    public static void drawBoxWithEdgesBatched(BlockPos posMin, BlockPos posMax, Color4f colorLines, Color4f colorSides, BufferBuilder bufferQuads, BufferBuilder bufferLines)
    {
        drawBoxWithEdgesBatched(posMin, posMax, Vec3.ZERO, colorLines, colorSides, bufferQuads, bufferLines);
    }

    /**
     * Draws a box with outlines around the given corner positions.
     * Takes in buffers initialized for GL_QUADS and GL_LINES modes.
     * The cameraPos value will be subtracted from the absolute coordinate values of the passed in block positions.
     * @param posMin
     * @param posMax
     * @param cameraPos
     * @param colorLines
     * @param colorSides
     * @param bufferQuads
     * @param bufferLines
     */
    public static void drawBoxWithEdgesBatched(BlockPos posMin, BlockPos posMax, Vec3 cameraPos, Color4f colorLines, Color4f colorSides, BufferBuilder bufferQuads, BufferBuilder bufferLines)
    {
        final double x1 = posMin.getX() - cameraPos.x;
        final double y1 = posMin.getY() - cameraPos.y;
        final double z1 = posMin.getZ() - cameraPos.z;
        final double x2 = posMax.getX() + 1 - cameraPos.x;
        final double y2 = posMax.getY() + 1 - cameraPos.y;
        final double z2 = posMax.getZ() + 1 - cameraPos.z;

        fi.dy.masa.malilib.render.RenderUtils.drawBoxAllSidesBatchedQuads(x1, y1, z1, x2, y2, z2, colorSides, bufferQuads);
        fi.dy.masa.malilib.render.RenderUtils.drawBoxAllEdgesBatchedLines(x1, y1, z1, x2, y2, z2, colorLines, bufferLines);
    }

    /**
     * Assumes a BufferBuilder in GL_QUADS mode has been initialized
     */
    public static void drawBoxHorizontalSidesBatchedQuads(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
            Color4f color, BufferBuilder buffer)
    {
        // West side
        buffer.vertex(minX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();

        // East side
        buffer.vertex(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();

        // North side
        buffer.vertex(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(minX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();

        // South side
        buffer.vertex(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
    }

    /**
     * Assumes a BufferBuilder in GL_QUADS mode has been initialized
     */
    public static void drawBoxTopBatchedQuads(double minX, double minZ, double maxX, double maxY, double maxZ, Color4f color, BufferBuilder buffer)
    {
        // Top side
        buffer.vertex(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
    }

    /**
     * Assumes a BufferBuilder in GL_QUADS mode has been initialized
     */
    public static void drawBoxBottomBatchedQuads(double minX, double minY, double minZ, double maxX, double maxZ, Color4f color, BufferBuilder buffer)
    {
        // Bottom side
        buffer.vertex(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(minX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
    }

    /**
     * Assumes a BufferBuilder in GL_LINES mode has been initialized
     */
    public static void drawBoxAllEdgesBatchedLines(double minX, double minY, double minZ, double maxX, double maxY, double maxZ,
            Color4f color, BufferBuilder buffer)
    {
        // West side
        buffer.vertex(minX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();

        buffer.vertex(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();

        buffer.vertex(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();

        buffer.vertex(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(minX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();

        // East side
        buffer.vertex(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();

        buffer.vertex(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();

        buffer.vertex(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();

        buffer.vertex(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();

        // North side (don't repeat the vertical lines that are done by the east/west sides)
        buffer.vertex(maxX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(minX, minY, minZ).color(color.r, color.g, color.b, color.a).endVertex();

        buffer.vertex(minX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(maxX, maxY, minZ).color(color.r, color.g, color.b, color.a).endVertex();

        // South side (don't repeat the vertical lines that are done by the east/west sides)
        buffer.vertex(minX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(maxX, minY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();

        buffer.vertex(maxX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
        buffer.vertex(minX, maxY, maxZ).color(color.r, color.g, color.b, color.a).endVertex();
    }

    public static void drawBox(IntBoundingBox bb, Vec3 cameraPos, Color4f color, BufferBuilder bufferQuads, BufferBuilder bufferLines)
    {
        double minX = bb.minX - cameraPos.x;
        double minY = bb.minY - cameraPos.y;
        double minZ = bb.minZ - cameraPos.z;
        double maxX = bb.maxX + 1 - cameraPos.x;
        double maxY = bb.maxY + 1 - cameraPos.y;
        double maxZ = bb.maxZ + 1 - cameraPos.z;

        drawBoxAllSidesBatchedQuads(minX, minY, minZ, maxX, maxY, maxZ, color, bufferQuads);
        drawBoxAllEdgesBatchedLines(minX, minY, minZ, maxX, maxY, maxZ, color, bufferLines);
    }

    /**
     * Renders a text plate/billboard, similar to the player name plate.<br>
     * The plate will always face towards the viewer.
     * @param text
     * @param x
     * @param y
     * @param z
     * @param scale
     */
    public static void drawTextPlate(List<String> text, double x, double y, double z, float scale)
    {
        Entity entity = mc().getCameraEntity();

        if (entity != null)
        {
            drawTextPlate(text, x, y, z, entity.getYRot(), entity.getXRot(), scale, 0xFFFFFFFF, 0x40000000, true);
        }
    }

    public static void drawTextPlate(List<String> text, double x, double y, double z, float yaw, float pitch,
                                     float scale, int textColor, int bgColor, boolean disableDepth)
    {
        Vec3 cameraPos = mc().gameRenderer.getMainCamera().getPosition();
        double cx = cameraPos.x;
        double cy = cameraPos.y;
        double cz = cameraPos.z;
        Font textRenderer = mc().font;

        PoseStack globalStack = RenderSystem.getModelViewStack();
        globalStack.pushPose();
        globalStack.translate(x - cx, y - cy, z - cz);

        Quaternion rot = Vector3f.YP.rotationDegrees(-yaw);
        rot.mul(Vector3f.XP.rotationDegrees(pitch));
        globalStack.mulPose(rot);

        globalStack.scale(-scale, -scale, scale);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.disableCull();

        setupBlend();
        RenderSystem.disableTexture();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        int maxLineLen = 0;

        for (String line : text)
        {
            maxLineLen = Math.max(maxLineLen, textRenderer.width(line));
        }

        int strLenHalf = maxLineLen / 2;
        int textHeight = textRenderer.lineHeight * text.size() - 1;
        int bga = ((bgColor >>> 24) & 0xFF);
        int bgr = ((bgColor >>> 16) & 0xFF);
        int bgg = ((bgColor >>>  8) & 0xFF);
        int bgb = (bgColor          & 0xFF);

        if (disableDepth)
        {
            RenderSystem.depthMask(false);
            RenderSystem.disableDepthTest();
        }

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        buffer.vertex(-strLenHalf - 1,          -1, 0.0D).color(bgr, bgg, bgb, bga).endVertex();
        buffer.vertex(-strLenHalf - 1,  textHeight, 0.0D).color(bgr, bgg, bgb, bga).endVertex();
        buffer.vertex( strLenHalf    ,  textHeight, 0.0D).color(bgr, bgg, bgb, bga).endVertex();
        buffer.vertex( strLenHalf    ,          -1, 0.0D).color(bgr, bgg, bgb, bga).endVertex();
        tessellator.end();

        RenderSystem.enableTexture();
        int textY = 0;

        // translate the text a bit infront of the background
        if (disableDepth == false)
        {
            RenderSystem.enablePolygonOffset();
            RenderSystem.polygonOffset(-0.6f, -1.2f);
        }

        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix.setIdentity();

        for (String line : text)
        {
            if (disableDepth)
            {
                RenderSystem.depthMask(false);
                RenderSystem.disableDepthTest();
                MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate(buffer);
                textRenderer.drawInBatch(line, -strLenHalf, textY, 0x20000000 | (textColor & 0xFFFFFF), false, modelMatrix, immediate, true, 0, 15728880);
                immediate.endBatch();
                RenderSystem.enableDepthTest();
                RenderSystem.depthMask(true);
            }

            MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate(buffer);
            textRenderer.drawInBatch(line, -strLenHalf, textY, textColor, false, modelMatrix, immediate, true, 0, 15728880);
            immediate.endBatch();
            textY += textRenderer.lineHeight;
        }

        if (disableDepth == false)
        {
            RenderSystem.polygonOffset(0f, 0f);
            RenderSystem.disablePolygonOffset();
        }

        color(1f, 1f, 1f, 1f);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        globalStack.popPose();
    }

    public static void renderBlockTargetingOverlay(Entity entity, BlockPos pos, Direction side, Vec3 hitVec,
            Color4f color, PoseStack matrixStack, Minecraft mc)
    {
        Direction playerFacing = entity.getDirection();
        HitPart part = PositionUtils.getHitPart(side, playerFacing, pos, hitVec);
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

        double x = pos.getX() + 0.5d - cameraPos.x;
        double y = pos.getY() + 0.5d - cameraPos.y;
        double z = pos.getZ() + 0.5d - cameraPos.z;

        PoseStack globalStack = RenderSystem.getModelViewStack();
        globalStack.pushPose();
        blockTargetingOverlayTranslations(x, y, z, side, playerFacing, globalStack);
        RenderSystem.applyModelViewMatrix();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableTexture();

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        int quadAlpha = (int) (0.18f * 255f);
        int hr = (int) (color.r * 255f);
        int hg = (int) (color.g * 255f);
        int hb = (int) (color.b * 255f);
        int ha = (int) (color.a * 255f);
        int c = 255;

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // White full block background
        buffer.vertex(x - 0.5, y - 0.5, z).color(c, c, c, quadAlpha).endVertex();
        buffer.vertex(x + 0.5, y - 0.5, z).color(c, c, c, quadAlpha).endVertex();
        buffer.vertex(x + 0.5, y + 0.5, z).color(c, c, c, quadAlpha).endVertex();
        buffer.vertex(x - 0.5, y + 0.5, z).color(c, c, c, quadAlpha).endVertex();

        switch (part)
        {
            case CENTER:
                buffer.vertex(x - 0.25, y - 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.vertex(x + 0.25, y - 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.vertex(x + 0.25, y + 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.vertex(x - 0.25, y + 0.25, z).color(hr, hg, hb, ha).endVertex();
                break;
            case LEFT:
                buffer.vertex(x - 0.50, y - 0.50, z).color(hr, hg, hb, ha).endVertex();
                buffer.vertex(x - 0.25, y - 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.vertex(x - 0.25, y + 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.vertex(x - 0.50, y + 0.50, z).color(hr, hg, hb, ha).endVertex();
                break;
            case RIGHT:
                buffer.vertex(x + 0.50, y - 0.50, z).color(hr, hg, hb, ha).endVertex();
                buffer.vertex(x + 0.25, y - 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.vertex(x + 0.25, y + 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.vertex(x + 0.50, y + 0.50, z).color(hr, hg, hb, ha).endVertex();
                break;
            case TOP:
                buffer.vertex(x - 0.50, y + 0.50, z).color(hr, hg, hb, ha).endVertex();
                buffer.vertex(x - 0.25, y + 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.vertex(x + 0.25, y + 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.vertex(x + 0.50, y + 0.50, z).color(hr, hg, hb, ha).endVertex();
                break;
            case BOTTOM:
                buffer.vertex(x - 0.50, y - 0.50, z).color(hr, hg, hb, ha).endVertex();
                buffer.vertex(x - 0.25, y - 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.vertex(x + 0.25, y - 0.25, z).color(hr, hg, hb, ha).endVertex();
                buffer.vertex(x + 0.50, y - 0.50, z).color(hr, hg, hb, ha).endVertex();
                break;
            default:
        }

        tessellator.end();

        // FIXME: line width doesn't work currently
        RenderSystem.lineWidth(1.6f);

        buffer.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        // Middle small rectangle
        buffer.vertex(x - 0.25, y - 0.25, z).color(c, c, c, c).endVertex();
        buffer.vertex(x + 0.25, y - 0.25, z).color(c, c, c, c).endVertex();
        buffer.vertex(x + 0.25, y + 0.25, z).color(c, c, c, c).endVertex();
        buffer.vertex(x - 0.25, y + 0.25, z).color(c, c, c, c).endVertex();
        buffer.vertex(x - 0.25, y - 0.25, z).color(c, c, c, c).endVertex();
        tessellator.end();

        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        // Bottom left
        buffer.vertex(x - 0.50, y - 0.50, z).color(c, c, c, c).endVertex();
        buffer.vertex(x - 0.25, y - 0.25, z).color(c, c, c, c).endVertex();

        // Top left
        buffer.vertex(x - 0.50, y + 0.50, z).color(c, c, c, c).endVertex();
        buffer.vertex(x - 0.25, y + 0.25, z).color(c, c, c, c).endVertex();

        // Bottom right
        buffer.vertex(x + 0.50, y - 0.50, z).color(c, c, c, c).endVertex();
        buffer.vertex(x + 0.25, y - 0.25, z).color(c, c, c, c).endVertex();

        // Top right
        buffer.vertex(x + 0.50, y + 0.50, z).color(c, c, c, c).endVertex();
        buffer.vertex(x + 0.25, y + 0.25, z).color(c, c, c, c).endVertex();
        tessellator.end();

        globalStack.popPose();
    }

    public static void renderBlockTargetingOverlaySimple(Entity entity, BlockPos pos, Direction side,
            Color4f color, PoseStack matrixStack, Minecraft mc)
    {
        Direction playerFacing = entity.getDirection();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

        double x = pos.getX() + 0.5d - cameraPos.x;
        double y = pos.getY() + 0.5d - cameraPos.y;
        double z = pos.getZ() + 0.5d - cameraPos.z;

        PoseStack globalStack = RenderSystem.getModelViewStack();
        globalStack.pushPose();

        blockTargetingOverlayTranslations(x, y, z, side, playerFacing, globalStack);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();

        int a = (int) (color.a * 255f);
        int r = (int) (color.r * 255f);
        int g = (int) (color.g * 255f);
        int b = (int) (color.b * 255f);
        int c = 255;

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        // Simple colored quad
        buffer.vertex(x - 0.5, y - 0.5, z).color(r, g, b, a).endVertex();
        buffer.vertex(x + 0.5, y - 0.5, z).color(r, g, b, a).endVertex();
        buffer.vertex(x + 0.5, y + 0.5, z).color(r, g, b, a).endVertex();
        buffer.vertex(x - 0.5, y + 0.5, z).color(r, g, b, a).endVertex();

        tessellator.end();

        // FIXME: line width doesn't work currently
        RenderSystem.lineWidth(1.6f);

        buffer.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        // Middle rectangle
        buffer.vertex(x - 0.375, y - 0.375, z).color(c, c, c, c).endVertex();
        buffer.vertex(x + 0.375, y - 0.375, z).color(c, c, c, c).endVertex();
        buffer.vertex(x + 0.375, y + 0.375, z).color(c, c, c, c).endVertex();
        buffer.vertex(x - 0.375, y + 0.375, z).color(c, c, c, c).endVertex();

        tessellator.end();

        globalStack.popPose();
    }

    private static void blockTargetingOverlayTranslations(double x, double y, double z,
            Direction side, Direction playerFacing, PoseStack matrixStack)
    {
        matrixStack.translate(x, y, z);

        switch (side)
        {
            case DOWN:
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(180f - playerFacing.toYRot()));
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(90f));
                break;
            case UP:
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(180f - playerFacing.toYRot()));
                matrixStack.mulPose(Vector3f.XP.rotationDegrees(-90f));
                break;
            case NORTH:
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(180f));
                break;
            case SOUTH:
                break;
            case WEST:
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(-90f));
                break;
            case EAST:
                matrixStack.mulPose(Vector3f.YP.rotationDegrees(90f));
                break;
        }

        matrixStack.translate(-x, -y, -z + 0.510);
    }

    public static void renderMapPreview(ItemStack stack, int x, int y, int dimensions)
    {
        if (stack.getItem() instanceof MapItem && GuiBase.isShiftDown())
        {
            color(1f, 1f, 1f, 1f);

            int y1 = y - dimensions - 20;
            int y2 = y1 + dimensions;
            int x1 = x + 8;
            int x2 = x1 + dimensions;
            int z = 300;

            Integer mapId = MapItem.getMapId(stack);
            MapItemSavedData mapState = MapItem.getSavedData(mapId, mc().level);

            ResourceLocation bgTexture = mapState == null ? TEXTURE_MAP_BACKGROUND : TEXTURE_MAP_BACKGROUND_CHECKERBOARD;
            bindTexture(bgTexture);
            setupBlend();

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.applyModelViewMatrix();
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder buffer = tessellator.getBuilder();
            buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
            buffer.vertex(x1, y2, z).uv(0.0f, 1.0f).endVertex();
            buffer.vertex(x2, y2, z).uv(1.0f, 1.0f).endVertex();
            buffer.vertex(x2, y1, z).uv(1.0f, 0.0f).endVertex();
            buffer.vertex(x1, y1, z).uv(0.0f, 0.0f).endVertex();
            tessellator.end();
            RenderSystem.disableBlend();

            if (mapState != null)
            {
                x1 += 8;
                y1 += 8;
                z = 310;
                MultiBufferSource.BufferSource consumer = MultiBufferSource.immediate(buffer);
                double scale = (double) (dimensions - 16) / 128.0D;
                PoseStack matrixStack = new PoseStack();
                matrixStack.pushPose();
                matrixStack.translate(x1, y1, z);
                matrixStack.scale((float) scale, (float) scale, 0);
                mc().gameRenderer.getMapRenderer().render(matrixStack, consumer, mapId, mapState, false, 0xF000F0);
                consumer.endBatch();
                matrixStack.popPose();
            }
        }
    }

    public static void renderShulkerBoxPreview(ItemStack stack, int baseX, int baseY, boolean useBgColors)
    {
        if (stack.hasTag())
        {
            NonNullList<ItemStack> items = InventoryUtils.getStoredItems(stack, -1);

            if (items.size() == 0)
            {
                return;
            }

            InventoryOverlay.InventoryRenderType type = InventoryOverlay.getInventoryType(stack);
            InventoryOverlay.InventoryProperties props = InventoryOverlay.getInventoryPropsTemp(type, items.size());

            int screenWidth = GuiUtils.getScaledWindowWidth();
            int screenHeight = GuiUtils.getScaledWindowHeight();
            int height = props.height + 18;
            int x = Mth.clamp(baseX + 8     , 0, screenWidth - props.width);
            int y = Mth.clamp(baseY - height, 0, screenHeight - height);

            if (stack.getItem() instanceof BlockItem && ((BlockItem) stack.getItem()).getBlock() instanceof ShulkerBoxBlock)
            {
                setShulkerboxBackgroundTintColor((ShulkerBoxBlock) ((BlockItem) stack.getItem()).getBlock(), useBgColors);
            }
            else
            {
                color(1f, 1f, 1f, 1f);
            }

            disableDiffuseLighting();
            PoseStack matrixStack = RenderSystem.getModelViewStack();
            matrixStack.pushPose();
            matrixStack.translate(0, 0, 400);

            InventoryOverlay.renderInventoryBackground(type, x, y, props.slotsPerRow, items.size(), mc());

            enableDiffuseLightingGui3D();

            Container inv = fi.dy.masa.malilib.util.InventoryUtils.getAsInventory(items);
            InventoryOverlay.renderInventoryStacks(type, inv, x + props.slotOffsetX, y + props.slotOffsetY, props.slotsPerRow, 0, -1, mc());

            matrixStack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

    /**
     * Calls RenderUtils.color() with the dye color of the provided shulker box block's color
     * @param block
     * @param useBgColors
     */
    public static void setShulkerboxBackgroundTintColor(@Nullable ShulkerBoxBlock block, boolean useBgColors)
    {
        if (block != null && useBgColors)
        {
            // In 1.13+ there is the uncolored Shulker Box variant, which returns null from getColor()
            final DyeColor dye = block.getColor() != null ? block.getColor() : DyeColor.PURPLE;
            final float[] colors = dye.getTextureDiffuseColors();
            color(colors[0], colors[1], colors[2], 1f);
        }
        else
        {
            color(1f, 1f, 1f, 1f);
        }
    }

    public static void renderModelInGui(int x, int y, BakedModel model, BlockState state, float zLevel)
    {
        if (state.getBlock() == Blocks.AIR)
        {
            return;
        }

        PoseStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.pushPose();
        bindTexture(InventoryMenu.BLOCK_ATLAS);
        mc().getTextureManager().getTexture(InventoryMenu.BLOCK_ATLAS).setFilter(false, false);

        RenderSystem.enableBlend();
        setupBlendSimple();
        color(1f, 1f, 1f, 1f);

        setupGuiTransform(x, y, model.isGui3d(), zLevel);
        //model.getItemCameraTransforms().applyTransform(ItemCameraTransforms.TransformType.GUI);
        matrixStack.mulPose(new Quaternion(Vector3f.XP, 30, true));
        matrixStack.mulPose(new Quaternion(Vector3f.YP, 225, true));
        matrixStack.scale(0.625f, 0.625f, 0.625f);

        renderModel(model, state);

        matrixStack.popPose();
    }

    public static void setupGuiTransform(int xPosition, int yPosition, boolean isGui3d, float zLevel)
    {
        setupGuiTransform(RenderSystem.getModelViewStack(), xPosition, yPosition, zLevel);
    }

    public static void setupGuiTransform(PoseStack matrixStack, int xPosition, int yPosition, float zLevel)
    {
        matrixStack.translate(xPosition + 8.0, yPosition + 8.0, zLevel + 100.0);
        matrixStack.scale(16, -16, 16);
    }

    private static void renderModel(BakedModel model, BlockState state)
    {
        PoseStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.pushPose();
        matrixStack.translate(-0.5, -0.5, -0.5);
        int color = 0xFFFFFFFF;

        if (model.isCustomRenderer() == false)
        {
            RenderSystem.setShader(GameRenderer::getRendertypeSolidShader);
            RenderSystem.applyModelViewMatrix();
            Tesselator tessellator = Tesselator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuilder();
            bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);

            for (Direction face : Direction.values())
            {
                RAND.setSeed(0);
                renderQuads(bufferbuilder, model.getQuads(state, face, RAND), state, color);
            }

            RAND.setSeed(0);
            renderQuads(bufferbuilder, model.getQuads(state, null, RAND), state, color);
            tessellator.end();
        }

        matrixStack.popPose();
    }

    private static void renderQuads(BufferBuilder renderer, List<BakedQuad> quads, BlockState state, int color)
    {
        final int quadCount = quads.size();

        for (int i = 0; i < quadCount; ++i)
        {
            BakedQuad quad = quads.get(i);
            renderQuad(renderer, quad, state, 0xFFFFFFFF);
        }
    }

    private static void renderQuad(BufferBuilder buffer, BakedQuad quad, BlockState state, int color)
    {
        /*
        buffer.putVertexData(quad.getVertexData());
        buffer.setQuadColor(color);

        if (quad.hasColor())
        {
            BlockColors blockColors = mc().getBlockColorMap();
            int m = blockColors.getColorMultiplier(state, null, null, quad.getColorIndex());

            float r = (float) (m >>> 16 & 0xFF) / 255F;
            float g = (float) (m >>>  8 & 0xFF) / 255F;
            float b = (float) (m        & 0xFF) / 255F;
            buffer.multiplyColor(r, g, b, 4);
            buffer.multiplyColor(r, g, b, 3);
            buffer.multiplyColor(r, g, b, 2);
            buffer.multiplyColor(r, g, b, 1);
        }

        putQuadNormal(buffer, quad);
    }

    private static void putQuadNormal(BufferBuilder renderer, BakedQuad quad)
    {
        Vec3i direction = quad.getFace().getVector();
        renderer.normal(direction.getX(), direction.getY(), direction.getZ());
        */
    }

    private static Minecraft mc()
    {
        return Minecraft.getInstance();
    }

    /*
    public static void enableGUIStandardItemLighting(float scale)
    {
        RenderSystem.pushMatrix();
        RenderSystem.rotate(-30.0F, 0.0F, 1.0F, 0.0F);
        RenderSystem.rotate(165.0F, 1.0F, 0.0F, 0.0F);

        enableStandardItemLighting(scale);

        RenderSystem.popMatrix();
    }

    public static void enableStandardItemLighting(float scale)
    {
        RenderSystem.enableLighting();
        RenderSystem.enableLight(0);
        RenderSystem.enableLight(1);
        RenderSystem.enableColorMaterial();
        RenderUtils.colorMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT_AND_DIFFUSE);
        RenderSystem.glLight(GL11.GL_LIGHT0, GL11.GL_POSITION, RenderHelper.setColorBuffer((float) LIGHT0_POS.x, (float) LIGHT0_POS.y, (float) LIGHT0_POS.z, 0.0f));

        float lightStrength = 0.3F * scale;
        RenderSystem.glLight(GL11.GL_LIGHT0, GL11.GL_DIFFUSE, RenderHelper.setColorBuffer(lightStrength, lightStrength, lightStrength, 1.0F));
        RenderSystem.glLight(GL11.GL_LIGHT0, GL11.GL_AMBIENT, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        RenderSystem.glLight(GL11.GL_LIGHT0, GL11.GL_SPECULAR, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        RenderSystem.glLight(GL11.GL_LIGHT1, GL11.GL_POSITION, RenderHelper.setColorBuffer((float) LIGHT1_POS.x, (float) LIGHT1_POS.y, (float) LIGHT1_POS.z, 0.0f));
        RenderSystem.glLight(GL11.GL_LIGHT1, GL11.GL_DIFFUSE, RenderHelper.setColorBuffer(lightStrength, lightStrength, lightStrength, 1.0F));
        RenderSystem.glLight(GL11.GL_LIGHT1, GL11.GL_AMBIENT, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
        RenderSystem.glLight(GL11.GL_LIGHT1, GL11.GL_SPECULAR, RenderHelper.setColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));

        RenderSystem.shadeModel(GL11.GL_FLAT);

        float ambientLightStrength = 0.4F;
        RenderSystem.glLightModel(GL11.GL_LIGHT_MODEL_AMBIENT, RenderHelper.setColorBuffer(ambientLightStrength, ambientLightStrength, ambientLightStrength, 1.0F));
    }
    */
}
