package fi.dy.masa.malilib.util;

import javax.annotation.Nullable;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.DoubleSidedInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.state.properties.ChestType;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class InventoryUtils
{
    private static final NonNullList<ItemStack> EMPTY_LIST = NonNullList.create();

    /**
     * Check whether the stacks are identical otherwise, but ignoring the stack size
     * @param stack1
     * @param stack2
     * @return
     */
    public static boolean areStacksEqual(ItemStack stack1, ItemStack stack2)
    {
        return ItemStack.isSame(stack1, stack2) && ItemStack.tagMatches(stack1, stack2);
    }

    /**
     * Checks whether the stacks are identical otherwise, but ignoring the stack size,
     * and if the item is damageable, then ignoring the durability too.
     * @param stack1
     * @param stack2
     * @return
     */
    public static boolean areStacksEqualIgnoreDurability(ItemStack stack1, ItemStack stack2)
    {
        return ItemStack.isSameIgnoreDurability(stack1, stack2) && ItemStack.tagMatches(stack1, stack2);
    }

    /**
     * Swaps the stack from the slot <b>slotNum</b> to the given hotbar slot <b>hotbarSlot</b>
     * @param container
     * @param slotNum
     * @param hotbarSlot
     */
    public static void swapSlots(Container container, int slotNum, int hotbarSlot)
    {
        Minecraft mc = Minecraft.getInstance();
        mc.gameMode.handleInventoryMouseClick(container.containerId, slotNum, hotbarSlot, ClickType.SWAP, mc.player);
    }

    /**
     * Assuming that the slot is from the ContainerPlayer container,
     * returns whether the given slot number is one of the regular inventory slots.
     * This means that the crafting slots and armor slots are not valid.
     * @param slotNumber
     * @param allowOffhand
     * @return
     */
    public static boolean isRegularInventorySlot(int slotNumber, boolean allowOffhand)
    {
        return slotNumber > 8 && (allowOffhand || slotNumber < 45);
    }

    /**
     * Finds an empty slot in the player inventory. Armor slots are not valid for the return value of this method.
     * Whether or not the offhand slot is valid, depends on the <b>allowOffhand</b> argument.
     * @param containerPlayer
     * @param allowOffhand
     * @param reverse
     * @return the slot number, or -1 if none were found
     */
    public static int findEmptySlotInPlayerInventory(Container containerPlayer, boolean allowOffhand, boolean reverse)
    {
        final int startSlot = reverse ? containerPlayer.slots.size() - 1 : 0;
        final int endSlot = reverse ? -1 : containerPlayer.slots.size();
        final int increment = reverse ? -1 : 1;

        for (int slotNum = startSlot; slotNum != endSlot; slotNum += increment)
        {
            Slot slot = containerPlayer.slots.get(slotNum);
            ItemStack stackSlot = slot.getItem();

            // Inventory crafting, armor and offhand slots are not valid
            if (stackSlot.isEmpty() && isRegularInventorySlot(slot.index, allowOffhand))
            {
                return slot.index;
            }
        }

        return -1;
    }

    /**
     * Finds a slot with an identical item than <b>stackReference</b>, ignoring the durability
     * of damageable items. Does not allow crafting or armor slots or the offhand slot
     * in the ContainerPlayer container.
     * @param container
     * @param stackReference
     * @param reverse
     * @return the slot number, or -1 if none were found
     */
    public static int findSlotWithItem(Container container, ItemStack stackReference, boolean reverse)
    {
        final int startSlot = reverse ? container.slots.size() - 1 : 0;
        final int endSlot = reverse ? -1 : container.slots.size();
        final int increment = reverse ? -1 : 1;
        final boolean isPlayerInv = container instanceof PlayerContainer;

        for (int slotNum = startSlot; slotNum != endSlot; slotNum += increment)
        {
            Slot slot = container.slots.get(slotNum);

            if ((isPlayerInv == false || isRegularInventorySlot(slot.index, false)) &&
                areStacksEqualIgnoreDurability(slot.getItem(), stackReference))
            {
                return slot.index;
            }
        }

        return -1;
    }

    /**
     * Swap the given item to the player's main hand, if that item is found
     * in the player's inventory.
     * @param stackReference
     * @param mc
     * @return true if an item was swapped to the main hand, false if it was already in the hand, or was not found in the inventory
     */
    public static boolean swapItemToMainHand(ItemStack stackReference, Minecraft mc)
    {
        PlayerEntity player = mc.player;
        boolean isCreative = player.abilities.instabuild;

        // Already holding the requested item
        if (areStacksEqual(stackReference, player.getMainHandItem()))
        {
            return false;
        }

        if (isCreative)
        {
            player.inventory.setPickedItem(stackReference);
            mc.gameMode.handleCreativeModeItemAdd(player.getMainHandItem(), 36 + player.inventory.selected);
            return true;
        }
        else
        {
            int slot = findSlotWithItem(player.inventoryMenu, stackReference, true);

            if (slot != -1)
            {
                int currentHotbarSlot = player.inventory.selected;
                mc.gameMode.handleInventoryMouseClick(player.inventoryMenu.containerId, slot, currentHotbarSlot, ClickType.SWAP, mc.player);
                return true;
            }
        }

        return false;
    }

    /**
     * Gets the inventory at the given position, if any.
     * Combines chest inventories into double chest inventories when applicable.
     * @param world
     * @param pos
     * @return
     */
    @SuppressWarnings("deprecation")
    @Nullable
    public static IInventory getInventory(World world, BlockPos pos)
    {
        if (world.hasChunkAt(pos) == false)
        {
            return null;
        }

        // The method in World now checks that the caller is from the same thread...
        TileEntity te = world.getChunk(pos).getBlockEntity(pos);

        if (te instanceof IInventory)
        {
            IInventory inv = (IInventory) te;
            BlockState state = world.getBlockState(pos);

            if (state.getBlock() instanceof ChestBlock && te instanceof ChestTileEntity)
            {
                ChestType type = state.getValue(ChestBlock.TYPE);

                if (type != ChestType.SINGLE)
                {
                    BlockPos posAdj = pos.relative(ChestBlock.getConnectedDirection(state));

                    if (world.hasChunkAt(posAdj))
                    {
                        BlockState stateAdj = world.getBlockState(posAdj);
                        // The method in World now checks that the caller is from the same thread...
                        TileEntity te2 = world.getChunk(posAdj).getBlockEntity(posAdj);

                        if (stateAdj.getBlock() == state.getBlock() &&
                            te2 instanceof ChestTileEntity &&
                            stateAdj.getValue(ChestBlock.TYPE) != ChestType.SINGLE &&
                            stateAdj.getValue(ChestBlock.FACING) == state.getValue(ChestBlock.FACING))
                        {
                            IInventory invRight = type == ChestType.RIGHT ?              inv : (IInventory) te2;
                            IInventory invLeft  = type == ChestType.RIGHT ? (IInventory) te2 :             inv;
                            inv = new DoubleSidedInventory(invRight, invLeft);
                        }
                    }
                }
            }

            return inv;
        }

        return null;
    }

    /**
     * Checks if the given Shulker Box (or other storage item with the
     * same NBT data structure) currently contains any items.
     * @param stackShulkerBox
     * @return
     */
    public static boolean shulkerBoxHasItems(ItemStack stackShulkerBox)
    {
        CompoundNBT nbt = stackShulkerBox.getTag();

        if (nbt != null && nbt.contains("BlockEntityTag", Constants.NBT.TAG_COMPOUND))
        {
            CompoundNBT tag = nbt.getCompound("BlockEntityTag");

            if (tag.contains("Items", Constants.NBT.TAG_LIST))
            {
                ListNBT tagList = tag.getList("Items", Constants.NBT.TAG_COMPOUND);
                return tagList.size() > 0;
            }
        }

        return false;
    }

    /**
     * Returns the list of items currently stored in the given Shulker Box
     * (or other storage item with the same NBT data structure).
     * Does not keep empty slots.
     * @param stackIn The item holding the inventory contents
     * @return
     */
    public static NonNullList<ItemStack> getStoredItems(ItemStack stackIn)
    {
        CompoundNBT nbt = stackIn.getTag();

        if (nbt != null && nbt.contains("BlockEntityTag", Constants.NBT.TAG_COMPOUND))
        {
            CompoundNBT tagBlockEntity = nbt.getCompound("BlockEntityTag");

            if (tagBlockEntity.contains("Items", Constants.NBT.TAG_LIST))
            {
                NonNullList<ItemStack> items = NonNullList.create();
                ListNBT tagList = tagBlockEntity.getList("Items", Constants.NBT.TAG_COMPOUND);
                final int count = tagList.size();

                for (int i = 0; i < count; ++i)
                {
                    ItemStack stack = ItemStack.of(tagList.getCompound(i));

                    if (stack.isEmpty() == false)
                    {
                        items.add(stack);
                    }
                }

                return items;
            }
        }

        return NonNullList.create();
    }

    /**
     * Returns the list of items currently stored in the given Shulker Box
     * (or other storage item with the same NBT data structure).
     * Preserves empty slots.
     * @param stackIn The item holding the inventory contents
     * @param slotCount the maximum number of slots, and thus also the size of the list to create
     * @return
     */
    public static NonNullList<ItemStack> getStoredItems(ItemStack stackIn, int slotCount)
    {
        CompoundNBT nbt = stackIn.getTag();

        if (nbt != null && nbt.contains("BlockEntityTag", Constants.NBT.TAG_COMPOUND))
        {
            CompoundNBT tagBlockEntity = nbt.getCompound("BlockEntityTag");

            if (tagBlockEntity.contains("Items", Constants.NBT.TAG_LIST))
            {
                ListNBT tagList = tagBlockEntity.getList("Items", Constants.NBT.TAG_COMPOUND);
                final int count = tagList.size();
                int maxSlot = -1;

                if (slotCount <= 0)
                {
                    for (int i = 0; i < count; ++i)
                    {
                        CompoundNBT tag = tagList.getCompound(i);
                        int slot = tag.getByte("Slot");

                        if (slot > maxSlot)
                        {
                            maxSlot = slot;
                        }
                    }

                    slotCount = maxSlot + 1;
                }

                NonNullList<ItemStack> items = NonNullList.withSize(slotCount, ItemStack.EMPTY);

                for (int i = 0; i < count; ++i)
                {
                    CompoundNBT tag = tagList.getCompound(i);
                    ItemStack stack = ItemStack.of(tag);
                    int slot = tag.getByte("Slot");

                    if (slot >= 0 && slot < items.size() && stack.isEmpty() == false)
                    {
                        items.set(slot, stack);
                    }
                }

                return items;
            }
        }

        return EMPTY_LIST;
    }

    /**
     * Returns a map of the stored item counts in the given Shulker Box
     * (or other storage item with the same NBT data structure).
     * @param stackShulkerBox
     * @return
     */
    public static Object2IntOpenHashMap<ItemType> getStoredItemCounts(ItemStack stackShulkerBox)
    {
        Object2IntOpenHashMap<ItemType> map = new Object2IntOpenHashMap<>();
        NonNullList<ItemStack> items = getStoredItems(stackShulkerBox);

        for (int slot = 0; slot < items.size(); ++slot)
        {
            ItemStack stack = items.get(slot);

            if (stack.isEmpty() == false)
            {
                map.addTo(new ItemType(stack), stack.getCount());
            }
        }

        return map;
    }

    /**
     * Returns a map of the stored item counts in the given inventory.
     * This also counts the contents of any Shulker Boxes
     * (or other storage item with the same NBT data structure).
     * @param inv
     * @return
     */
    public static Object2IntOpenHashMap<ItemType> getInventoryItemCounts(IInventory inv)
    {
        Object2IntOpenHashMap<ItemType> map = new Object2IntOpenHashMap<>();
        final int slots = inv.getContainerSize();

        for (int slot = 0; slot < slots; ++slot)
        {
            ItemStack stack = inv.getItem(slot);

            if (stack.isEmpty() == false)
            {
                map.addTo(new ItemType(stack, false, true), stack.getCount());

                if (stack.getItem() instanceof BlockItem &&
                    ((BlockItem) stack.getItem()).getBlock() instanceof ShulkerBoxBlock &&
                    shulkerBoxHasItems(stack))
                {
                    Object2IntOpenHashMap<ItemType> boxCounts = getStoredItemCounts(stack);

                    for (ItemType type : boxCounts.keySet())
                    {
                        map.addTo(type, boxCounts.getInt(type));
                    }
                }
            }
        }

        return map;
    }

    /**
     * Returns the given list of items wrapped as an InventoryBasic
     * @param items
     * @return
     */
    public static IInventory getAsInventory(NonNullList<ItemStack> items)
    {
        Inventory inv = new Inventory(items.size());

        for (int slot = 0; slot < items.size(); ++slot)
        {
            inv.setItem(slot, items.get(slot));
        }

        return inv;
    }
}
