package fi.dy.masa.malilib.render;

import java.util.ArrayList;
import java.util.List;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrewingStandBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import fi.dy.masa.malilib.gui.GuiBase;

public class InventoryOverlay
{
    public static final ResourceLocation TEXTURE_BREWING_STAND    = new ResourceLocation("textures/gui/container/brewing_stand.png");
    public static final ResourceLocation TEXTURE_DISPENSER        = new ResourceLocation("textures/gui/container/dispenser.png");
    public static final ResourceLocation TEXTURE_DOUBLE_CHEST     = new ResourceLocation("textures/gui/container/generic_54.png");
    public static final ResourceLocation TEXTURE_FURNACE          = new ResourceLocation("textures/gui/container/furnace.png");
    public static final ResourceLocation TEXTURE_HOPPER           = new ResourceLocation("textures/gui/container/hopper.png");
    public static final ResourceLocation TEXTURE_PLAYER_INV       = new ResourceLocation("textures/gui/container/hopper.png");
    public static final ResourceLocation TEXTURE_SINGLE_CHEST     = new ResourceLocation("textures/gui/container/shulker_box.png");

    private static final EquipmentSlot[] VALID_EQUIPMENT_SLOTS = new EquipmentSlot[] { EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET };
    public static final InventoryProperties INV_PROPS_TEMP = new InventoryProperties();

    private static final ResourceLocation[] EMPTY_SLOT_TEXTURES = new ResourceLocation[] {
            new ResourceLocation("item/empty_armor_slot_boots"),
            new ResourceLocation("item/empty_armor_slot_leggings"),
            new ResourceLocation("item/empty_armor_slot_chestplate"),
            new ResourceLocation("item/empty_armor_slot_helmet") };

    public static void renderInventoryBackground(InventoryRenderType type, int x, int y, int slotsPerRow, int totalSlots, Minecraft mc)
    {
        RenderUtils.setupBlend();
        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.applyModelViewMatrix();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        if (type == InventoryRenderType.FURNACE)
        {
            RenderUtils.bindTexture(TEXTURE_FURNACE);
            RenderUtils.drawTexturedRectBatched(x     , y     ,   0,   0,   4,  64, buffer); // left (top)
            RenderUtils.drawTexturedRectBatched(x +  4, y     ,  84,   0,  92,   4, buffer); // top (right)
            RenderUtils.drawTexturedRectBatched(x     , y + 64,   0, 162,  92,   4, buffer); // bottom (left)
            RenderUtils.drawTexturedRectBatched(x + 92, y +  4, 172, 102,   4,  64, buffer); // right (bottom)
            RenderUtils.drawTexturedRectBatched(x +  4, y +  4,  52,  13,  88,  60, buffer); // middle
        }
        else if (type == InventoryRenderType.BREWING_STAND)
        {
            RenderUtils.bindTexture(TEXTURE_BREWING_STAND);
            RenderUtils.drawTexturedRectBatched(x      , y     ,   0,   0,   4,  68, buffer); // left (top)
            RenderUtils.drawTexturedRectBatched(x +   4, y     ,  63,   0, 113,   4, buffer); // top (right)
            RenderUtils.drawTexturedRectBatched(x      , y + 68,   0, 162, 113,   4, buffer); // bottom (left)
            RenderUtils.drawTexturedRectBatched(x + 113, y +  4, 172,  98,   4,  68, buffer); // right (bottom)
            RenderUtils.drawTexturedRectBatched(x +   4, y +  4,  13,  13, 109,  64, buffer); // middle
        }
        else if (type == InventoryRenderType.DISPENSER)
        {
            RenderUtils.bindTexture(TEXTURE_DISPENSER);
            RenderUtils.drawTexturedRectBatched(x     , y     ,   0,   0,   7,  61, buffer); // left (top)
            RenderUtils.drawTexturedRectBatched(x +  7, y     , 115,   0,  61,   7, buffer); // top (right)
            RenderUtils.drawTexturedRectBatched(x     , y + 61,   0, 159,  61,   7, buffer); // bottom (left)
            RenderUtils.drawTexturedRectBatched(x + 61, y +  7, 169, 105,   7,  61, buffer); // right (bottom)
            RenderUtils.drawTexturedRectBatched(x +  7, y +  7,  61,  16,  54,  54, buffer); // middle
        }
        else if (type == InventoryRenderType.HOPPER)
        {
            RenderUtils.bindTexture(TEXTURE_HOPPER);
            RenderUtils.drawTexturedRectBatched(x      , y     ,   0,   0,   7,  25, buffer); // left (top)
            RenderUtils.drawTexturedRectBatched(x +   7, y     ,  79,   0,  97,   7, buffer); // top (right)
            RenderUtils.drawTexturedRectBatched(x      , y + 25,   0, 126,  97,   7, buffer); // bottom (left)
            RenderUtils.drawTexturedRectBatched(x +  97, y +  7, 169, 108,   7,  25, buffer); // right (bottom)
            RenderUtils.drawTexturedRectBatched(x +   7, y +  7,  43,  19,  90,  18, buffer); // middle
        }
        // Most likely a Villager, or possibly a Llama
        else if (type == InventoryRenderType.VILLAGER)
        {
            RenderUtils.bindTexture(TEXTURE_DOUBLE_CHEST);
            RenderUtils.drawTexturedRectBatched(x     , y     ,   0,   0,   7,  79, buffer); // left (top)
            RenderUtils.drawTexturedRectBatched(x +  7, y     , 133,   0,  43,   7, buffer); // top (right)
            RenderUtils.drawTexturedRectBatched(x     , y + 79,   0, 215,  43,   7, buffer); // bottom (left)
            RenderUtils.drawTexturedRectBatched(x + 43, y +  7, 169, 143,   7,  79, buffer); // right (bottom)
            RenderUtils.drawTexturedRectBatched(x +  7, y +  7,   7,  17,  36,  72, buffer); // 2x4 slots
        }
        else if (type == InventoryRenderType.FIXED_27)
        {
            renderInventoryBackground27(x, y, buffer, mc);
        }
        else if (type == InventoryRenderType.FIXED_54)
        {
            renderInventoryBackground54(x, y, buffer, mc);
        }
        else
        {
            RenderUtils.bindTexture(TEXTURE_DOUBLE_CHEST);

            // Draw the slot backgrounds according to how many slots there actually are
            int rows = (int) (Math.ceil((double) totalSlots / (double) slotsPerRow));
            int bgw = Math.min(totalSlots, slotsPerRow) * 18 + 7;
            int bgh = rows * 18 + 7;

            RenderUtils.drawTexturedRectBatched(x      , y      ,         0,         0,   7, bgh, buffer); // left (top)
            RenderUtils.drawTexturedRectBatched(x +   7, y      , 176 - bgw,         0, bgw,   7, buffer); // top (right)
            RenderUtils.drawTexturedRectBatched(x      , y + bgh,         0,       215, bgw,   7, buffer); // bottom (left)
            RenderUtils.drawTexturedRectBatched(x + bgw, y +   7,       169, 222 - bgh,   7, bgh, buffer); // right (bottom)

            for (int row = 0; row < rows; row++)
            {
                int rowLen = Mth.clamp(totalSlots - (row * slotsPerRow), 1, slotsPerRow);
                RenderUtils.drawTexturedRectBatched(x + 7, y + row * 18 + 7, 7, 17, rowLen * 18, 18, buffer);

                // Render the background for the last non-existing slots on the last row,
                // in two strips of the background texture from the double chest texture's top part.
                if (rows > 1 && rowLen < slotsPerRow)
                {
                    RenderUtils.drawTexturedRectBatched(x + rowLen * 18 + 7, y + row * 18 +  7, 7, 3, (slotsPerRow - rowLen) * 18, 9, buffer);
                    RenderUtils.drawTexturedRectBatched(x + rowLen * 18 + 7, y + row * 18 + 16, 7, 3, (slotsPerRow - rowLen) * 18, 9, buffer);
                }
            }
        }

        tessellator.end();
    }

    public static void renderInventoryBackground27(int x, int y, BufferBuilder buffer, Minecraft mc)
    {
        RenderUtils.bindTexture(TEXTURE_SINGLE_CHEST);
        RenderUtils.drawTexturedRectBatched(x      , y     ,   0,   0,   7,  61, buffer); // left (top)
        RenderUtils.drawTexturedRectBatched(x +   7, y     ,   7,   0, 169,   7, buffer); // top (right)
        RenderUtils.drawTexturedRectBatched(x      , y + 61,   0, 159, 169,   7, buffer); // bottom (left)
        RenderUtils.drawTexturedRectBatched(x + 169, y +  7, 169, 105,   7,  61, buffer); // right (bottom)
        RenderUtils.drawTexturedRectBatched(x +   7, y +  7,   7,  17, 162,  54, buffer); // middle
    }

    public static void renderInventoryBackground54(int x, int y, BufferBuilder buffer, Minecraft mc)
    {
        RenderUtils.bindTexture(TEXTURE_DOUBLE_CHEST);
        RenderUtils.drawTexturedRectBatched(x      , y      ,   0,   0,   7, 115, buffer); // left (top)
        RenderUtils.drawTexturedRectBatched(x +   7, y      ,   7,   0, 169,   7, buffer); // top (right)
        RenderUtils.drawTexturedRectBatched(x      , y + 115,   0, 215, 169,   7, buffer); // bottom (left)
        RenderUtils.drawTexturedRectBatched(x + 169, y +   7, 169, 107,   7, 115, buffer); // right (bottom)
        RenderUtils.drawTexturedRectBatched(x +   7, y +   7,   7,  17, 162, 108, buffer); // middle
    }

    public static void renderEquipmentOverlayBackground(int x, int y, LivingEntity entity, PoseStack matrixStack)
    {
        RenderUtils.color(1f, 1f, 1f, 1f);

        Tesselator tessellator = Tesselator.getInstance();
        BufferBuilder buffer = tessellator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

        RenderUtils.bindTexture(TEXTURE_DISPENSER);

        RenderUtils.drawTexturedRectBatched(x     , y     ,   0,   0, 50, 83, buffer); // top-left (main part)
        RenderUtils.drawTexturedRectBatched(x + 50, y     , 173,   0,  3, 83, buffer); // right edge top
        RenderUtils.drawTexturedRectBatched(x     , y + 83,   0, 163, 50,  3, buffer); // bottom edge left
        RenderUtils.drawTexturedRectBatched(x + 50, y + 83, 173, 163,  3,  3, buffer); // bottom right corner

        for (int i = 0, xOff = 7, yOff = 7; i < 4; ++i, yOff += 18)
        {
            RenderUtils.drawTexturedRectBatched(x + xOff, y + yOff, 61, 16, 18, 18, buffer);
        }

        // Main hand and offhand
        RenderUtils.drawTexturedRectBatched(x + 28, y + 2 * 18 + 7, 61, 16, 18, 18, buffer);
        RenderUtils.drawTexturedRectBatched(x + 28, y + 3 * 18 + 7, 61, 16, 18, 18, buffer);

        tessellator.end();

        RenderUtils.bindTexture(InventoryMenu.BLOCK_ATLAS);

        if (entity.getItemBySlot(EquipmentSlot.OFFHAND).isEmpty())
        {
            ResourceLocation texture = new ResourceLocation("minecraft:item/empty_armor_slot_shield");
            RenderUtils.renderSprite(x + 28 + 1, y + 3 * 18 + 7 + 1, 16, 16, InventoryMenu.BLOCK_ATLAS, texture, matrixStack);
        }

        for (int i = 0, xOff = 7, yOff = 7; i < 4; ++i, yOff += 18)
        {
            final EquipmentSlot eqSlot = VALID_EQUIPMENT_SLOTS[i];

            if (entity.getItemBySlot(eqSlot).isEmpty())
            {
                ResourceLocation texture = EMPTY_SLOT_TEXTURES[eqSlot.getIndex()];
                RenderUtils.renderSprite(x + xOff + 1, y + yOff + 1, 16, 16, InventoryMenu.BLOCK_ATLAS, texture, matrixStack);
            }
        }
    }

    public static InventoryRenderType getInventoryType(Container inv)
    {
        if (inv instanceof ShulkerBoxBlockEntity)
        {
            return InventoryRenderType.FIXED_27;
        }
        else if (inv instanceof CompoundContainer)
        {
            return InventoryRenderType.FIXED_54;
        }
        else if (inv instanceof FurnaceBlockEntity)
        {
            return InventoryRenderType.FURNACE;
        }
        else if (inv instanceof BrewingStandBlockEntity)
        {
            return InventoryRenderType.BREWING_STAND;
        }
        else if (inv instanceof DispenserBlockEntity) // this includes the Dropper as a sub class
        {
            return InventoryRenderType.DISPENSER;
        }
        else if (inv instanceof HopperBlockEntity)
        {
            return InventoryRenderType.HOPPER;
        }
        else if (inv.getClass() == SimpleContainer.class) // FIXME
        {
            return InventoryRenderType.HORSE;
        }
        else
        {
            return InventoryRenderType.GENERIC;
        }
    }

    public static InventoryRenderType getInventoryType(ItemStack stack)
    {
        Item item = stack.getItem();

        if (item instanceof BlockItem)
        {
            Block block = ((BlockItem) item).getBlock();

            if (block instanceof ShulkerBoxBlock || block instanceof ChestBlock)
            {
                return InventoryRenderType.FIXED_27;
            }
            else if (block instanceof FurnaceBlock)
            {
                return InventoryRenderType.FURNACE;
            }
            else if (block instanceof DispenserBlock) // this includes the Dropper as a sub class
            {
                return InventoryRenderType.DISPENSER;
            }
            else if (block instanceof HopperBlock)
            {
                return InventoryRenderType.HOPPER;
            }
            else if (block instanceof BrewingStandBlock)
            {
                return InventoryRenderType.BREWING_STAND;
            }
        }

        return InventoryRenderType.GENERIC;
    }

    /**
     * Returns the instance of the shared/temporary properties instance,
     * with the values set for the type of inventory provided.
     * Don't hold on to the instance, as the values will mutate when this
     * method is called again!
     * @param type
     * @param totalSlots
     * @return
     */
    public static InventoryProperties getInventoryPropsTemp(InventoryRenderType type, int totalSlots)
    {
        INV_PROPS_TEMP.totalSlots = totalSlots;

        if (type == InventoryRenderType.FURNACE)
        {
            INV_PROPS_TEMP.slotsPerRow = 1;
            INV_PROPS_TEMP.slotOffsetX = 0;
            INV_PROPS_TEMP.slotOffsetY = 0;
            INV_PROPS_TEMP.width = 96;
            INV_PROPS_TEMP.height = 68;
        }
        else if (type == InventoryRenderType.BREWING_STAND)
        {
            INV_PROPS_TEMP.slotsPerRow = 9;
            INV_PROPS_TEMP.slotOffsetX = 0;
            INV_PROPS_TEMP.slotOffsetY = 0;
            INV_PROPS_TEMP.width = 127;
            INV_PROPS_TEMP.height = 72;
        }
        else if (type == InventoryRenderType.DISPENSER)
        {
            INV_PROPS_TEMP.slotsPerRow = 3;
            INV_PROPS_TEMP.slotOffsetX = 8;
            INV_PROPS_TEMP.slotOffsetY = 8;
            INV_PROPS_TEMP.width = 68;
            INV_PROPS_TEMP.height = 68;
        }
        else if (type == InventoryRenderType.HORSE)
        {
            INV_PROPS_TEMP.slotsPerRow = Math.max(1, totalSlots / 3);
            INV_PROPS_TEMP.slotOffsetX = 8;
            INV_PROPS_TEMP.slotOffsetY = 8;
            INV_PROPS_TEMP.width = totalSlots * 18 / 3 + 14;
            INV_PROPS_TEMP.height = 68;
        }
        else if (type == InventoryRenderType.HOPPER)
        {
            INV_PROPS_TEMP.slotsPerRow = 5;
            INV_PROPS_TEMP.slotOffsetX = 8;
            INV_PROPS_TEMP.slotOffsetY = 8;
            INV_PROPS_TEMP.width = 105;
            INV_PROPS_TEMP.height = 32;
        }
        else if (type == InventoryRenderType.VILLAGER)
        {
            INV_PROPS_TEMP.slotsPerRow = 2;
            INV_PROPS_TEMP.slotOffsetX = 8;
            INV_PROPS_TEMP.slotOffsetY = 8;
            INV_PROPS_TEMP.width = 50;
            INV_PROPS_TEMP.height = 86;
        }
        else
        {
            if (type == InventoryRenderType.FIXED_27)
            {
                totalSlots = 27;
            }
            else if (type == InventoryRenderType.FIXED_54)
            {
                totalSlots = 54;
            }

            INV_PROPS_TEMP.slotsPerRow = 9;
            INV_PROPS_TEMP.slotOffsetX = 8;
            INV_PROPS_TEMP.slotOffsetY = 8;
            int rows = (int) (Math.ceil((double) totalSlots / (double) INV_PROPS_TEMP.slotsPerRow));
            INV_PROPS_TEMP.width = Math.min(INV_PROPS_TEMP.slotsPerRow, totalSlots) * 18 + 14;
            INV_PROPS_TEMP.height = rows * 18 + 14;
        }

        return INV_PROPS_TEMP;
    }

    public static void renderInventoryStacks(InventoryRenderType type, Container inv, int startX, int startY, int slotsPerRow, int startSlot, int maxSlots, Minecraft mc)
    {
        if (type == InventoryRenderType.FURNACE)
        {
            renderStackAt(inv.getItem(0), startX +   8, startY +  8, 1, mc);
            renderStackAt(inv.getItem(1), startX +   8, startY + 44, 1, mc);
            renderStackAt(inv.getItem(2), startX +  68, startY + 26, 1, mc);
        }
        else if (type == InventoryRenderType.BREWING_STAND)
        {
            renderStackAt(inv.getItem(0), startX +  47, startY + 42, 1, mc);
            renderStackAt(inv.getItem(1), startX +  70, startY + 49, 1, mc);
            renderStackAt(inv.getItem(2), startX +  93, startY + 42, 1, mc);
            renderStackAt(inv.getItem(3), startX +  70, startY +  8, 1, mc);
            renderStackAt(inv.getItem(4), startX +   8, startY +  8, 1, mc);
        }
        else
        {
            final int slots = inv.getContainerSize();
            int x = startX;
            int y = startY;

            if (maxSlots < 0)
            {
                maxSlots = slots;
            }

            for (int slot = startSlot, i = 0; slot < slots && i < maxSlots;)
            {
                for (int column = 0; column < slotsPerRow && slot < slots && i < maxSlots; ++column, ++slot, ++i)
                {
                    ItemStack stack = inv.getItem(slot);

                    if (stack.isEmpty() == false)
                    {
                        renderStackAt(stack, x, y, 1, mc);
                    }

                    x += 18;
                }

                x = startX;
                y += 18;
            }
        }
    }

    public static void renderEquipmentStacks(LivingEntity entity, int x, int y, Minecraft mc)
    {
        for (int i = 0, xOff = 7, yOff = 7; i < 4; ++i, yOff += 18)
        {
            final EquipmentSlot eqSlot = VALID_EQUIPMENT_SLOTS[i];
            ItemStack stack = entity.getItemBySlot(eqSlot);

            if (stack.isEmpty() == false)
            {
                renderStackAt(stack, x + xOff + 1, y + yOff + 1, 1, mc);
            }
        }

        ItemStack stack = entity.getItemBySlot(EquipmentSlot.MAINHAND);

        if (stack.isEmpty() == false)
        {
            renderStackAt(stack, x + 28, y + 2 * 18 + 7 + 1, 1, mc);
        }

        stack = entity.getItemBySlot(EquipmentSlot.OFFHAND);

        if (stack.isEmpty() == false)
        {
            renderStackAt(stack, x + 28, y + 3 * 18 + 7 + 1, 1, mc);
        }
    }

    public static void renderItemStacks(NonNullList<ItemStack> items, int startX, int startY, int slotsPerRow, int startSlot, int maxSlots, Minecraft mc)
    {
        final int slots = items.size();
        int x = startX;
        int y = startY;

        if (maxSlots < 0)
        {
            maxSlots = slots;
        }

        for (int slot = startSlot, i = 0; slot < slots && i < maxSlots;)
        {
            for (int column = 0; column < slotsPerRow && slot < slots && i < maxSlots; ++column, ++slot, ++i)
            {
                ItemStack stack = items.get(slot);

                if (stack.isEmpty() == false)
                {
                    renderStackAt(stack, x, y, 1, mc);
                }

                x += 18;
            }

            x = startX;
            y += 18;
        }
    }

    public static void renderStackAt(ItemStack stack, float x, float y, float scale, Minecraft mc)
    {
        PoseStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.pushPose();
        matrixStack.translate(x, y, 0);
        matrixStack.scale(scale, scale, 1);

        RenderUtils.enableDiffuseLightingGui3D();
        RenderUtils.color(1f, 1f, 1f, 1f);

        mc.getItemRenderer().blitOffset += 100;
        mc.getItemRenderer().renderAndDecorateFakeItem(stack, 0, 0);

        RenderUtils.color(1f, 1f, 1f, 1f);
        mc.getItemRenderer().renderGuiItemDecorations(mc.font, stack, 0, 0, null);
        mc.getItemRenderer().blitOffset -= 100;

        RenderUtils.disableDiffuseLighting();
        matrixStack.popPose();
    }

    public static void renderStackToolTip(int x, int y, ItemStack stack, Minecraft mc, PoseStack matrixStack)
    {
        List<Component> list = stack.getTooltipLines(mc.player, mc.options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL);
        List<String> lines = new ArrayList<>();

        for (int i = 0; i < list.size(); ++i)
        {
            if (i == 0)
            {
                lines.add(stack.getRarity().color + list.get(i).getString());
            }
            else
            {
                lines.add(GuiBase.TXT_DARK_GRAY + list.get(i).getString());
            }
        }

        RenderUtils.drawHoverText(x, y, lines, matrixStack);
    }

    public static class InventoryProperties
    {
        public int totalSlots = 1;
        public int width = 176;
        public int height = 83;
        public int slotsPerRow = 9;
        public int slotOffsetX = 8;
        public int slotOffsetY = 8;
    }

    public enum InventoryRenderType
    {
        BREWING_STAND,
        DISPENSER,
        FURNACE,
        HOPPER,
        HORSE,
        FIXED_27,
        FIXED_54,
        VILLAGER,
        GENERIC;
    }
}
