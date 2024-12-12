package org.swarg.merchants.containers;

import com.aleksey.merchants.Extended.ExtendedLogic;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import com.aleksey.merchants.Containers.Slots.SlotStall;
import com.aleksey.merchants.Containers.Slots.SlotStallBook;
import com.aleksey.merchants.Helpers.ItemHelper;
import com.aleksey.merchants.Helpers.PrepareTradeResult;
import com.aleksey.merchants.Items.ItemWarehouseBook;

import org.swarg.merchants.gui.GuiBigStall;
import org.swarg.merchants.tileentities.TileEntityBigStall;

import com.bioxx.tfc.Containers.ContainerTFC;
import com.bioxx.tfc.Containers.Slots.SlotForShowOnly;
import com.bioxx.tfc.Core.Player.PlayerInventory;
import com.bioxx.tfc.Food.ItemFoodTFC;
import com.bioxx.tfc.api.Interfaces.IFood;
import com.bioxx.tfc.api.Food;

import static com.aleksey.merchants.Extended.ExtendedLogic.getNoSplitFoodWeight;
import static com.aleksey.merchants.Extended.ExtendedLogic.isNoSplitFood;

// slotClick is based on
// https://github.com/Mr-J/AdvancedBackpackMod/blob/master/unrelated/slotClick%2BComments%2BRename%2BHelpers.java.txt

/**
 * 12-12-2024
 * @author Swarg
 */
public class ContainerBigStall extends ContainerTFC
{
    private TileEntityBigStall stall;
    private boolean isOwnerMode;
    private ArrayList<Integer> paySlotIndexes;
    private World world;

    /*
     * slotClick fields
     *
     * these 3 variables are private in container, so we can not access them here
     * we mirror and rename these locally here
     * private int field_94536_g = 0;
     * seems to hold the state of the dragged multislot placement of an itemstack
     * renamed to distributeState
     * 0 = not started
     * 1 = currently placing
     * 2 = drag operation done, place into the slots
     * private final Set field_94537_h = new HashSet();
    */
    private int pressedKeyInRange = -1;
    private int distributeState = 0;
    private final Set distributeSlotSet = new HashSet();


    public ContainerBigStall(
        InventoryPlayer inv, TileEntityBigStall stall, boolean isOwnerMode,
        World world, int x, int y, int z
    ) {
        this.stall = stall;
        this.isOwnerMode = isOwnerMode;
        this.world = world;

        buildLayout();

        PlayerInventory.buildInventoryLayout(
            this, inv, 8, GuiBigStall.WindowHeight - 1 + 5, false, true
        );
    }

    private void buildLayout()
    {
        int y = GuiBigStall.TopSlotY;
        int index = 0;

        for (int i = 0; i < TileEntityBigStall.PriceCount; i++) {
            if (isOwnerMode) {
                addSlotToContainer(new SlotStall(stall, index++, GuiBigStall.PricesSlotX, y));
            } else {
                addSlotToContainer(new SlotForShowOnly(stall, index++, GuiBigStall.PricesSlotX, y));
            }
            addSlotToContainer(new SlotStall(stall, index++, GuiBigStall.GoodsSlotX, y));

            y += GuiBigStall.SlotSize;
        }

        if (isOwnerMode) {
            addSlotToContainer(new SlotStallBook(
                stall, index, GuiBigStall.BookSlotX, GuiBigStall.BookSlotY));
        }
    }

    @Override
    public void onContainerClosed(EntityPlayer entityplayer)
    {
        super.onContainerClosed(entityplayer);

        if (!world.isRemote) {
            stall.closeInventory();
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer)
    {
        return true;
    }

    @Override
    public ItemStack transferStackInSlotTFC(EntityPlayer entityplayer, int slotNumber)
    {
        if (isOwnerMode) {
            return transferStackInSlotTFC_OwnerMode(entityplayer, slotNumber);
        } else {
            return transferStackInSlotTFC_BuyerMode(entityplayer, slotNumber);
        }
    }

    private ItemStack transferStackInSlotTFC_OwnerMode(EntityPlayer player, int slotNumber)
    {
        Slot slot = (Slot)inventorySlots.get(slotNumber);

        if (slot == null || !slot.getHasStack()) {
            return null;
        }

        ItemStack itemstack = slot.getStack();

        if (slotNumber < TileEntityBigStall.ItemCount) {
            if (isBookSlot(slotNumber)) {
                if (this.mergeItemStack(itemstack, TileEntityBigStall.ItemCount, this.inventorySlots.size(), true))
                {
                    stall.setOwner(null);

                    world.markBlockForUpdate(stall.xCoord, stall.yCoord, stall.zCoord);
                }
                else
                    return null;
            }
            else {
                itemstack.stackSize = 0;
            }
        }
        else
        {
            int bookSlotIndex = getBookSlotIndex();
            Slot bookSlot = (Slot)this.inventorySlots.get(bookSlotIndex);

            if (!(itemstack.getItem() instanceof ItemWarehouseBook) || bookSlot.getStack() != null) {
                return null;
            }

            bookSlot.putStack(itemstack.splitStack(1));

            if (!player.worldObj.isRemote) {
                stall.setOwner(player);
                stall.calculateQuantitiesInWarehouse();

                world.markBlockForUpdate(stall.xCoord, stall.yCoord, stall.zCoord);
            }

            player.onUpdate();
        }

        if (itemstack.stackSize == 0) {
            slot.putStack(null);
        } else {
            slot.onSlotChanged();
        }

        return null;
    }

    private ItemStack transferStackInSlotTFC_BuyerMode(EntityPlayer player, int slotNumber)
    {
        if (player.worldObj.isRemote || slotNumber >= TileEntityBigStall.ItemCount) {
            return null;
        }

        Slot slot = (Slot)inventorySlots.get(slotNumber);

        if (slot == null || !slot.getHasStack()) {
            return null;
        }

        ItemStack goodItemStack = slot.getStack();

        int goodSlotIndex = slot.getSlotIndex();
        int priceSlotIndex = getPriceSlotIndex(goodSlotIndex);
        ItemStack payItemStack = stall.getStackInSlot(priceSlotIndex);

        InventoryPlayer inventoryPlayer = player.inventory;

        if (!preparePayAndTrade(goodSlotIndex, goodItemStack, payItemStack, player)) {
            return null;
        }

        ArrayList<Integer> slotIndexes = new ArrayList<Integer>();

        if (!prepareTransferGoods(goodItemStack, inventoryPlayer, slotIndexes)) {
            return null;
        }

        confirmPay(payItemStack, inventoryPlayer);

        stall.confirmTrade();

        confirmTransferGoods(goodItemStack, inventoryPlayer, slotIndexes);

        player.worldObj.markBlockForUpdate(stall.xCoord, stall.yCoord, stall.zCoord);

        player.onUpdate();

        return null;
    }

    /**
     * the slotClick method (from net.minecraft.inventory.Container.java)
     * handles every click performed on a container, this happens normally
     * in a gui
     *
     * @param targetSlotID the ID of the clicked slot, the slotClick method is performed on this slot
     * @param mouseButtonPressed the pressed mouse button when slotClick was invoked, notice that this has not to be the "real" mouse
     * @param flag a range of flag indicating different things
     * @param player the player performing the click
     *
     * values for mouseButtonPressed:
     * 0 = left button clicked
     * 1 = right button clicked
     * 2 = middle (third) button clicked / also left button pressed & hold (only with item@cursor)
     * 6 = right button pressed & hold
     *
     * values for flag:
     * 0 = standard single click
     * 1 = single click + shift modifier
     * 2 = hotbar key is pressed (keys 0-9)
     * 3 = click with the middle button
     * 4 = click outside of the current gui window
     * 5 = button pressed & hold with the cursor holding an itemstack
     * 6 = double left click
     **/
    @Override
    public ItemStack slotClick(int targetSlotID, int mouseButtonPressed, int flag, EntityPlayer player)
    {
        ItemStack returnStack = null;
        InventoryPlayer inventoryplayer = player.inventory;
        //kind of a multipurpose variable
        int sizeOrID;
        ItemStack movedItemStack;

        /*
         * PART 1: DRAGGED DISTRIBUTION
         * This is a special case where the itemStack the mouseCursor currently holds
         * is distributed over several fields of a container, which is only
         * done if the a mouse button is pressed and hold (flag == 5)
         */
        if (flag == 5)
        {
            int currentDistributeState = distributeState;
            distributeState = checkForPressedButton(mouseButtonPressed);

            /*
             * if distributeState is neither 1 nor 2 AND
             * currentDistributeState != distributestate
             * then reset the distributestate and the distributeSlotSet
             */
            if ((currentDistributeState != 1 || distributeState != 2)
                   && currentDistributeState != distributeState)
            {
                this.resetDistributionVariables();
            }
            /*
             * else if the player current hold nothing
             * on his mouse cursor (no stack picked up)
             */
            else if (inventoryplayer.getItemStack() == null) {
                this.resetDistributionVariables();
            }
            else if (distributeState == 0)
            {
                pressedKeyInRange = checkForPressedButton2(mouseButtonPressed);

                //true for 0 or 1
                if (checkValue(pressedKeyInRange)) {
                    distributeState = 1;
                    distributeSlotSet.clear();
                }
                else {
                    this.resetDistributionVariables();
                }
            }
            else if (distributeState == 1)
            {
                //get the slot for which the click is performed
                Slot currentTargetSlot = (Slot)this.inventorySlots.get(targetSlotID);

                if (currentTargetSlot != null &&
                stackFitsInSlot(currentTargetSlot, inventoryplayer.getItemStack(), true) &&
                currentTargetSlot.isItemValid(inventoryplayer.getItemStack()) &&
                inventoryplayer.getItemStack().stackSize > distributeSlotSet.size())

                {
                    /*
                     * add the slot to the set
                     * (to which the itemstack will be distributed)
                     */
                    distributeSlotSet.add(currentTargetSlot);
                }
            }
            else if (distributeState == 2)
            {
                if (!distributeSlotSet.isEmpty()) {
                    putItemToDistributeSlotSet(mouseButtonPressed, player);
                }

                this.resetDistributionVariables();
            }
            else
            {
                this.resetDistributionVariables();
            }
        }
        else if (distributeState != 0)
        {
            this.resetDistributionVariables();
        }
        /*
         * PART 2: NORMAL SLOTCLICK
         * this part handles all other slotClicks which do
         * not distribute an itemstack over several slots
         */
        else
        {
            /*
             *multipurpose variable, mostly used for holding
             *the number of items to be transfered, if used
             *otherwise it will be commented seperately
             */
            Slot targetSlotCopy;

            int transferItemCount;
            ItemStack targetSlotItemStack;

            /*
             *only for a standard or shift click AND
             *a left or right button click
             */
            if ((flag == 0 || flag == 1) && (mouseButtonPressed == 0 || mouseButtonPressed == 1)) {
                //if the targetSlotID is not valid
                if (targetSlotID == -999) {
                    if (inventoryplayer.getItemStack() != null && targetSlotID == -999) {
                        /*
                         * on leftclick drop the complete itemstack from the inventory
                         * on rightclick drop a single item from the itemstack
                         */
                        if (mouseButtonPressed == 0) {
                            player.dropPlayerItemWithRandomChoice(inventoryplayer.getItemStack(), false);
                            inventoryplayer.setItemStack((ItemStack)null);
                        }

                        if (mouseButtonPressed == 1) {
                            player.dropPlayerItemWithRandomChoice(inventoryplayer.getItemStack().splitStack(1), false);

                            if (inventoryplayer.getItemStack().stackSize == 0)
                            {
                                inventoryplayer.setItemStack((ItemStack)null);
                            }
                        }
                    }
                }
                //if a click with shift modifier is performed (clicking while holding down shift)
                else if (flag == 1)
                {
                    //for an invalid slot return null
                    if (targetSlotID < 0)
                    {
                        return null;
                    }
                    targetSlotCopy = (Slot)this.inventorySlots.get(targetSlotID);

                    //if targetSlotCopy is not null and the stack inside the slot can be moved
                    if (targetSlotCopy != null && targetSlotCopy.canTakeStack(player))
                    {
                        //transfer the picked up stack to targetSlotID in the targetinventory
                        movedItemStack = this.transferStackInSlot(player, targetSlotID);
                        //if the movedItemStack was not transferred completely
                        if (movedItemStack != null)
                        {
                            //here used to store an ID
                            Item movedItem = movedItemStack.getItem();
                            //set the return value to the rest
                            returnStack = movedItemStack.copy();

                            if (targetSlotCopy != null && targetSlotCopy.getStack() != null && targetSlotCopy.getStack().getItem() == movedItem)
                            {
                                //retry with the shift-click
                                this.retrySlotClick(targetSlotID, mouseButtonPressed, true, player);
                            }
                        }
                    }
                }
                //if a click with NO shift modifier is performed
                else
                {
                    if (targetSlotID < 0)
                    {
                        return null;
                    }
                    targetSlotCopy = (Slot)this.inventorySlots.get(targetSlotID);
                    /*
                     * if the target slot is not empty
                     * save its itemstack
                     * save the itemstack to be transferred in cursorItemStack
                     */
                    if (targetSlotCopy != null)
                    {
                        /*
                         * movedItemStack is here used to store the target Slot stack
                         * instead of the currently moved itemstack
                         */
                        movedItemStack = targetSlotCopy.getStack();
                        ItemStack cursorItemStack = inventoryplayer.getItemStack();
                        /*
                         * if the targetSlot contains an itemstack,
                         * exchange it with the currently picked up stack
                         */
                        if (movedItemStack != null)
                        {
                            returnStack = movedItemStack.copy();
                        }

                        //if the target slot is empty
                        if (movedItemStack == null)
                        {
                            this.putItemToEmptySlot(targetSlotCopy, mouseButtonPressed, player);
                        }
                        /*
                         * if the target slot is not empty AND
                         * if the stack in the target slot can be moved (always true in Container.java)
                         */
                        else if (targetSlotCopy.canTakeStack(player))
                        {
                            this.putItemToNonEmptySlot(targetSlotCopy, mouseButtonPressed, player);
                        }

                        //update the target slot
                        targetSlotCopy.onSlotChanged();
                    }
                }
            }
            /*
             * if a hotbar key is pressed (flag == 2)
             */
            else if (flag == 2 && mouseButtonPressed >= 0 && mouseButtonPressed < 9)
            {

            }
            /*
             * if the pressed mouse button is the middle button and
             * the player is in creative mode and
             * has currently no stack in his hand and
             * the target slot is greater/equal to zero
             */
            else if (flag == 3 && player.capabilities.isCreativeMode && inventoryplayer.getItemStack() == null && targetSlotID >= 0)
            {

            }
            /*
             * if the player clicks outside of the gui and
             * he has an itemstack in his hands and
             * and the targetslot is greater/equal to zero
             */
            else if (flag == 4 && inventoryplayer.getItemStack() == null && targetSlotID >= 0)
            {
                targetSlotCopy = (Slot)this.inventorySlots.get(targetSlotID);

                /*
                 * if there is a stack in the targetslot
                 * moveItemStack size is 1 if leftclick or the stacksize of targetSlotCopy if rightclicked
                 * update targetSlotCopy
                 * drop movedItemStack at players position
                 */
                if (targetSlotCopy != null && targetSlotCopy.getHasStack())
                {
                    movedItemStack = targetSlotCopy.decrStackSize(mouseButtonPressed == 0 ? 1 : targetSlotCopy.getStack().stackSize);
                    targetSlotCopy.onPickupFromSlot(player, movedItemStack);
                    player.dropPlayerItemWithRandomChoice(movedItemStack, false);
                }
            }
            /*
             * if the player performs a double leftclick and
             * the targetslot is greater/equal to zero
             */
            else if (flag == 6 && targetSlotID >= 0)
            {

            }
        }
        // return any remains of the operation
        return returnStack;
    }

    /*
     * need to overwrite the container.java method to call
     * the modified slotClick instead of the container method
     */
    @Override
    protected void retrySlotClick(
        int targetSlotID, int mouseButtonPressed, boolean flag, EntityPlayer ep
    ) {
        //a retry of slotClick with flag 1 (shift click)
        this.slotClick(targetSlotID, mouseButtonPressed, 1, ep);
    }

    /**
     * This is a renamed version of the method
     * func_94532_c in net.minecraft.inventory.Container.java
     * this is not needed but i dont like non-sense method names
     *
     * @param mouseButtonPressed can be {0,1,2,6} from what i observed
     * @return 2 if mouseButtonPressed is 6, 0 else
     *
     * if there are other values possible for mouseButtonPressed
     * the method returns the following:
     * 0  -> 0
     * 1  -> 1
     * 2  -> 2
     * 3  -> 3
     * for values over 3 the assignments restarts from the top
     * (so 4 is 0, 5 is 1...)
     */
    public static int checkForPressedButton(int mouseButtonPressed)
    {
        return mouseButtonPressed & 3;
    }

    /**
     * This is a renamed version of the method
     * func_94533_d in net.minecaft.inventory.Container.java
     * this is not needed but i dont like non-sense method names
     */
    protected void resetDistributionVariables()
    {
        distributeState = 0;
        distributeSlotSet.clear();
    }


    /**
     * This is a renamed version of the method
     * func_94529_b in net.minecraft.inventory.Container.java
     * this is not needed but i dont like non-sense method names
     *
     * @param mouseButtonPressed can be {0,1,2,6} from what i observed
     * @return 1 if mouseButtonPressed is 6, 0 else
     *
     * if there are other values possible for mouseButtonPressed
     * the method returns the following:
     * 0-3  -> 0
     * 4-7  -> 1
     * 8-11 -> 2
     * 12+  -> 3
     */
    public static int checkForPressedButton2(int mouseButtonPressed)
    {
        return mouseButtonPressed >> 2 & 3;
    }

    /**
     * This is a renamed version of the method
     * func_94528_d in net.minecraft.inventory.Container.java
     * this is not needed but i dont like non-sense method names
     *
     * @param value
     * @return
     */
    public static boolean checkValue(int value)
    {
        return value == 0 || value == 1;
    }

    /**
     * This is a renamed version of the method
     * func_94527_a in net.minecraft.inventory.Container.java
     * this is not needed but i dont like non-sense method names
     *
     * The method return a bool if a given itemstack fits into
     * a given slot, the bool input argument rules if the size of
     * the stack matters or not
     *
     * @param slot is the target slot
     * @param itemStack is the itemstack which should fit into slot
     * @param sizeMatters rules if the size of itemstack matters
     * @return true if the stack fits
     */
    public static boolean stackFitsInSlot(Slot slot, ItemStack itemStack, boolean sizeMatters)
    {
        boolean flag = slot == null || !slot.getHasStack();

        if (slot != null && slot.getHasStack()
            && itemStack != null && itemStack.isItemEqual(slot.getStack())
            && ItemStack.areItemStackTagsEqual(slot.getStack(), itemStack)
        ) {
            int i = sizeMatters ? 0 : itemStack.stackSize;
            flag |= slot.getStack().stackSize + i <= itemStack.getMaxStackSize();
        }

        return flag;
    }

    /**
     * This is a renamed version of the method
     * func_94525_a in net.minecraft.inventory.Container.java
     * this is not needed but i dont like non-sense method names
     *
     * @param slotSet is the set of slots for the current distribution
     * @param stackSizeSelector is the number which is added to the current processed stack
     * @param stackToResize is stack that will be placed in the processed slot
     * @param currentSlotStackSize is the size of the itemstack in the current slot
     */
    public static void setSlotStack(Set slotSet, int stackSizeSelector, ItemStack stackToResize, int currentSlotStackSize)
    {
        switch (stackSizeSelector)
        {
            case 0:
                stackToResize.stackSize = MathHelper.floor_float((float)stackToResize.stackSize / (float)slotSet.size());
                break;
            case 1:
                stackToResize.stackSize = 1;
        }

        stackToResize.stackSize += currentSlotStackSize;
    }

    private void putItemToDistributeSlotSet(int mouseButton, EntityPlayer player)
    {
        if (distributeSlotSet.size() == 1)
        {
            Slot slot = (Slot)distributeSlotSet.iterator().next();

            if (!isPlayerSlot(slot.slotNumber))
            {
                if (slot.getStack() == null)
                    putItemToEmptySlot(slot, pressedKeyInRange, player);
                else
                    putItemToNonEmptySlot(slot, pressedKeyInRange, player);

                return;
            }
        }

        InventoryPlayer inventoryplayer = player.inventory;
        ItemStack playerItemStack = inventoryplayer.getItemStack().copy();
        int size = inventoryplayer.getItemStack().stackSize;

        Iterator iterator = distributeSlotSet.iterator();

        while (iterator.hasNext())
        {
            Slot currentSlotOfSet = (Slot)iterator.next();

            if (!isPlayerSlot(currentSlotOfSet.slotNumber))
                continue;

            if (currentSlotOfSet != null
                    && stackFitsInSlot(currentSlotOfSet, inventoryplayer.getItemStack(), true)
                    && currentSlotOfSet.isItemValid(inventoryplayer.getItemStack())
                    && inventoryplayer.getItemStack().stackSize >= distributeSlotSet.size()
                    )
            {
                ItemStack targetSlotNewStack = playerItemStack.copy();
                int currentSlotStackSize = currentSlotOfSet.getHasStack() ? currentSlotOfSet.getStack().stackSize : 0;

                setSlotStack(distributeSlotSet, pressedKeyInRange, targetSlotNewStack, currentSlotStackSize);

                if (targetSlotNewStack.stackSize > targetSlotNewStack.getMaxStackSize())
                    targetSlotNewStack.stackSize = targetSlotNewStack.getMaxStackSize();

                if (targetSlotNewStack.stackSize > currentSlotOfSet.getSlotStackLimit())
                    targetSlotNewStack.stackSize = currentSlotOfSet.getSlotStackLimit();

                size -= targetSlotNewStack.stackSize - currentSlotStackSize;

                currentSlotOfSet.putStack(targetSlotNewStack);
            }
        }

        //set the stacksize of the picked up stack to the rest number
        playerItemStack.stackSize = size;

        if (playerItemStack.stackSize <= 0)
            playerItemStack = null;

        inventoryplayer.setItemStack(playerItemStack);
    }

    private void putItemToEmptySlot(Slot slot, int mouseButton, EntityPlayer player)
    {
        boolean isPlayerSlot = isPlayerSlot(slot.slotNumber);

        if (!isOwnerMode && !isPlayerSlot)
            return;

        InventoryPlayer inventoryplayer = player.inventory;
        ItemStack playerItemStack = inventoryplayer.getItemStack();

        if (playerItemStack == null || !slot.isItemValid(playerItemStack))
            return;

        boolean isBookSlot = isBookSlot(slot.slotNumber);
        boolean isFood = !isPlayerSlot && playerItemStack.getItem() instanceof IFood;

        int sizeToPut = mouseButton == 0 && !isBookSlot && !isFood ? playerItemStack.stackSize : 1;

        if (sizeToPut > slot.getSlotStackLimit())
            sizeToPut = slot.getSlotStackLimit();

        if (sizeToPut > 0 && playerItemStack.stackSize >= sizeToPut)
        {
            ItemStack stack;

            if (isFood)
                stack = getFoodItemStack(playerItemStack, mouseButton == 0);
            else
            {
                stack = isBookSlot || isPlayerSlot
                    ? playerItemStack.splitStack(sizeToPut)
                    : playerItemStack.copy().splitStack(sizeToPut);
            }

            slot.putStack(stack);

            if (isBookSlot)
            {
                stall.setOwner(player);
                stall.calculateQuantitiesInWarehouse();

                world.markBlockForUpdate(stall.xCoord, stall.yCoord, stall.zCoord);
            }
        }

        if (playerItemStack.stackSize == 0)
            inventoryplayer.setItemStack((ItemStack)null);
    }

    private void putItemToNonEmptySlot(Slot slot, int mouseButton, EntityPlayer player)
    {
        if (isOwnerMode || isPlayerSlot(slot.slotNumber))
            putItemToNonEmptySlotOwner(slot, mouseButton, player);
        else
            putItemToNonEmptySlotBuyer(slot, mouseButton, player);
    }

    private void putItemToNonEmptySlotOwner(Slot slot, int mouseButton, EntityPlayer player)
    {
        InventoryPlayer inventoryplayer = player.inventory;
        ItemStack playerItemStack = inventoryplayer.getItemStack();
        ItemStack slotItemStack = slot.getStack();
        boolean isPlayerSlot = isPlayerSlot(slot.slotNumber);
        boolean isBookSlot = isBookSlot(slot.slotNumber);
        boolean isFood = !isPlayerSlot && slotItemStack.getItem() instanceof IFood;

        if (playerItemStack == null)
        {
            if (isFood)
            {
                if (mouseButton == 0)
                    slot.putStack((ItemStack)null);
                else
                    slot.putStack(splitFoodWeight(slotItemStack));
            }
            else
            {
                int sizeToGet = mouseButton == 0 ? slotItemStack.stackSize : (slotItemStack.stackSize + 1) / 2;

                if (sizeToGet == 0)
                    return;

                ItemStack itemToGet = slot.decrStackSize(sizeToGet);

                if (isPlayerSlot || isBookSlot)
                    inventoryplayer.setItemStack(itemToGet);

                if (isBookSlot)
                {
                    stall.setOwner(null);

                    world.markBlockForUpdate(stall.xCoord, stall.yCoord, stall.zCoord);
                }

                if (slotItemStack.stackSize == 0)
                    slot.putStack((ItemStack)null);

                if (isPlayerSlot || isBookSlot)
                    slot.onPickupFromSlot(player, inventoryplayer.getItemStack());
            }

            return;
        }

        if (isBookSlot || !slot.isItemValid(playerItemStack))
            return;

        if (ItemHelper.areItemEquals(slotItemStack, playerItemStack))
        {
            if (isFood)
                addFoodWeight(slotItemStack, playerItemStack, mouseButton == 0);
            else
            {
                int sizeToPut = mouseButton == 0 && !isBookSlot && !isFood ? playerItemStack.stackSize : 1;

                if (sizeToPut > slot.getSlotStackLimit() - slotItemStack.stackSize)
                    sizeToPut = slot.getSlotStackLimit() - slotItemStack.stackSize;

                if (sizeToPut > playerItemStack.getMaxStackSize() - slotItemStack.stackSize)
                    sizeToPut = playerItemStack.getMaxStackSize() - slotItemStack.stackSize;

                if (sizeToPut == 0)
                    return;

                if (isPlayerSlot)
                {
                    playerItemStack.splitStack(sizeToPut);

                    if (playerItemStack.stackSize == 0)
                        inventoryplayer.setItemStack((ItemStack)null);
                }

                slotItemStack.stackSize += sizeToPut;
            }
        }
        else if (isPlayerSlot)
        {
            slot.putStack(playerItemStack);
            inventoryplayer.setItemStack(slotItemStack);
        }
        else
        {
            if (playerItemStack.getItem() instanceof IFood)
                slot.putStack(getFoodItemStack(playerItemStack, true));
            else
                slot.putStack(playerItemStack.copy());
        }
    }

    private ItemStack splitFoodWeight(ItemStack itemStack)
    {
        if ( isNoSplitFood(itemStack) ){
            return itemStack; // no split
        }

        IFood food = (IFood)itemStack.getItem();

        float newWeight;

        newWeight = Food.getWeight(itemStack) / 2;
        newWeight = 10 * (int)(newWeight / 10);

        if (newWeight == 0)
            return null;

        ItemFoodTFC.createTag(itemStack, newWeight);

        return itemStack;
    }

    private ItemStack addFoodWeight(ItemStack slotItemStack, ItemStack playerItemStack, boolean isAll)
    {
        if (isNoSplitFood(slotItemStack)){
            // no add
            return slotItemStack;
        }

        IFood food = (IFood)slotItemStack.getItem();

        float playerWeight;

        if (isAll)
        {
            playerWeight = Food.getWeight(playerItemStack);
            playerWeight = 10 * (int)(playerWeight / 10);

            if (playerWeight == 0)
                playerWeight = 10;
        }
        else
            playerWeight = 10;

        float newSlotWeight = Food.getWeight(slotItemStack) + playerWeight;

        if (newSlotWeight > food.getFoodMaxWeight(slotItemStack))
            newSlotWeight = food.getFoodMaxWeight(slotItemStack);

        ItemFoodTFC.createTag(slotItemStack, newSlotWeight);

        return slotItemStack;
    }

    private ItemStack getFoodItemStack(ItemStack srcItemStack, boolean isAll)
    {
        float weight;

        weight = getNoSplitFoodWeight(srcItemStack);// no split & add
        if (weight == 0)
        {
            if (isAll)
            {
                IFood food = (IFood)srcItemStack.getItem();
                weight = Food.getWeight(srcItemStack);
                weight = 10 * (int)(weight / 10);
                if (weight == 0) {
                    weight = 10;
                }
            }
            else {
                weight = 10;
            }
        }

        ItemStack resultItemStack = srcItemStack.copy();

        ItemFoodTFC.createTag(resultItemStack, weight);

        return resultItemStack;
    }

    private void putItemToNonEmptySlotBuyer(Slot slot, int mouseButton, EntityPlayer player)
    {
        if (player.worldObj.isRemote) {
            return;
        }

        int goodSlotIndex = slot.getSlotIndex();
        int priceSlotIndex = getPriceSlotIndex(goodSlotIndex);

        if (priceSlotIndex < 0) {
            return;
        }

        InventoryPlayer inventoryplayer = player.inventory;
        ItemStack playerItemStack = inventoryplayer.getItemStack();
        ItemStack goodItemStack = slot.getStack();
        ItemStack payItemStack = stall.getStackInSlot(priceSlotIndex);

        if (playerItemStack == null)
        {
            if (!preparePayAndTrade(goodSlotIndex, goodItemStack, payItemStack, player)) {
               return;
            }

            confirmPay(payItemStack, inventoryplayer);

            stall.confirmTrade();

            //ItemStack newItemStack = goodItemStack.copy();
            ItemStack newItemStack = stall.goodItemFromWarehouseContainer;
            //this shoult not happen. It for insurance and safety
            if ( newItemStack == null
                    || !ExtendedLogic.areItemEquals(goodItemStack, newItemStack)
            ) {
                newItemStack = goodItemStack.copy();
            }

            /* this check occurs when assigning values for _goodItemFromWarehouseContainer;
             if (newItemStack.getItem() instanceof IFood && !isNoSplitFood(newItemStack) )
                ItemFoodTFC.createTag(newItemStack, Food.getWeight(newItemStack));*/

            inventoryplayer.setItemStack(newItemStack);

            player.worldObj.markBlockForUpdate(stall.xCoord, stall.yCoord, stall.zCoord);

            player.onUpdate();

            //_stall.actionBuy(inventoryplayer.getItemStack());

            stall.updateHeldItem(player);

            return;
        }

        int goodQuantity = ItemHelper.getItemStackQuantity(goodItemStack);

        if (!slot.isItemValid(playerItemStack)
            || !ItemHelper.areItemEquals(goodItemStack, playerItemStack)
            || goodQuantity + ItemHelper.getItemStackQuantity(playerItemStack)
               > ItemHelper.getItemStackMaxQuantity(playerItemStack, inventoryplayer)
            || !preparePayAndTrade(goodSlotIndex, goodItemStack, payItemStack, player)
            )
        {
            return;
        }

        // check real giving goodStackItem form Warehouse container with itemStack at player cursor
        // example buy a pick head with diffrent durabuff bonus
        // dont allow stacked it;
        ItemStack newItemStack2 = stall.goodItemFromWarehouseContainer;
        if ( !ItemHelper.areItemEquals(newItemStack2, playerItemStack) ) {
            return;
        }

        confirmPay(payItemStack, inventoryplayer);

        stall.confirmTrade();

        ItemHelper.increaseStackQuantity(playerItemStack, goodQuantity);

        player.worldObj.markBlockForUpdate(stall.xCoord, stall.yCoord, stall.zCoord);

        player.onUpdate();

        //_stall.actionBuy(inventoryplayer.getItemStack());

        stall.updateHeldItem(player);
    }

    private void sendChatMsg(EntityPlayer player, String message) {
        player.addChatComponentMessage(new ChatComponentTranslation(message, new Object[0]));
    }

    private boolean preparePayAndTrade(
        int goodSlotIndex, ItemStack goodItemStack, ItemStack payItemStack, EntityPlayer player
    ) {
        if (!preparePay(payItemStack, player.inventory)) {
            sendChatMsg(player, "gui.Stall.Message.NoPays");
            return false;
        }

        // for put Stack to Warehouse not from stall price-slot but from player inventory (NBT)
        // example for selling items with smithingBonus
        stall.payItemFromPlayerInventory =
                ExtendedLogic.getFirstPayItemStackFromPlayer( payItemStack, player, paySlotIndexes );

        //PrepareTradeResult result = _stall.prepareTrade(goodSlotIndex, goodItemStack, payItemStack);
        PrepareTradeResult result =
                stall.prepareTrade(goodSlotIndex, goodItemStack, stall.payItemFromPlayerInventory);

        if (result == PrepareTradeResult.Success) {
            // true - если событие не отменено при запрете на обмен
            // вывод сообщения на обработчике данного события!
            final boolean cancelled = stall.fireTradeEvent(player);
            return !cancelled; // return true;
        }

        if (result == PrepareTradeResult.NoGoods) {
            sendChatMsg(player, "gui.Stall.Message.NoGoods");
        } else if (result == PrepareTradeResult.NoLastIngot) {
            sendChatMsg(player, "gui.Stall.Message.NoLastIngot");
        } else {
            sendChatMsg(player, "gui.Stall.Message.NoPaysSpace");
        }

        return false;
    }

    private boolean preparePay(ItemStack payItemStack, InventoryPlayer inv)
    {
        if (payItemStack == null)
            return false;

        int quantity = ItemHelper.getItemStackQuantity(payItemStack);

        if (quantity == 0)
            return false;

        paySlotIndexes = new ArrayList<Integer>();

        for (int i = 0; i < inv.getSizeInventory() && quantity > 0; i++)
        {
            ItemStack invItemStack = inv.getStackInSlot(i);

            //if (invItemStack == null || !ItemHelper.areItemEquals(payItemStack, invItemStack))
            if (invItemStack == null || !ExtendedLogic.areItemEquals(payItemStack, invItemStack, true)) {
                continue;
            }

            int invQuantity = ItemHelper.getItemStackQuantity(invItemStack);

            int nsFoodWeight = getNoSplitFoodWeight(invItemStack);
            if ( nsFoodWeight > 0 && invQuantity != nsFoodWeight ) {
                continue;
            }

            if (invQuantity == 0) {
                continue;
            }

            paySlotIndexes.add(i);

            quantity -= invQuantity;
        }

        return quantity <= 0;
    }

    private void confirmPay(ItemStack payItemStack, InventoryPlayer inventoryplayer)
    {
        int quantity = ItemHelper.getItemStackQuantity(payItemStack);

        for (int i = 0; i < paySlotIndexes.size(); i++)
        {
            int slotIndex = paySlotIndexes.get(i);
            ItemStack invItemStack = inventoryplayer.getStackInSlot(slotIndex);
            int invQuantity = ItemHelper.getItemStackQuantity(invItemStack);

            int sizeToGet = invQuantity > quantity ? quantity: invQuantity;

            ItemHelper.increaseStackQuantity(invItemStack, -sizeToGet);

            if (invItemStack.stackSize == 0) {
                inventoryplayer.setInventorySlotContents(slotIndex, (ItemStack)null);
            }

            inventoryplayer.markDirty();

            quantity -= sizeToGet;
        }

        paySlotIndexes = null;
    }

    private void confirmTransferGoods(
        ItemStack itemStack, InventoryPlayer inventoryPlayer, ArrayList<Integer> slotIndexes
    ) {
        IInventory inventory = (IInventory)inventoryPlayer;
        int requiredQuantity = ItemHelper.getItemStackQuantity(itemStack);
        int maxStackQuantity = ItemHelper.getItemStackMaxQuantity(itemStack, inventory);

        for (int i = 0; i < slotIndexes.size(); i++)
        {
            int slotIndex = slotIndexes.get(i);
            ItemStack invItemStack = inventory.getStackInSlot(slotIndex);

            if (invItemStack == null)
            {
                invItemStack = itemStack.copy();

                ItemHelper.setStackQuantity(invItemStack, requiredQuantity);

                inventory.setInventorySlotContents(slotIndex, invItemStack);

                requiredQuantity = 0;
            }
            else
            {
                int invQuantity = ItemHelper.getItemStackQuantity(invItemStack);
                int quantity = Math.min(requiredQuantity, maxStackQuantity - invQuantity);

                ItemHelper.increaseStackQuantity(invItemStack, quantity);

                requiredQuantity -= quantity;
            }
        }
    }

    private boolean prepareTransferGoods(
        ItemStack itemStack, InventoryPlayer inventoryPlayer, ArrayList<Integer> slotIndexes
    ) {
        int requiredQuantity = ItemHelper.getItemStackQuantity(itemStack);

        int quantity = searchTransferGoods_NonEmptySlots(itemStack, requiredQuantity, inventoryPlayer, slotIndexes);
        quantity = searchTransferGoods_emptySlots(itemStack, quantity, inventoryPlayer, slotIndexes);

        return quantity == 0;
    }

    private int searchTransferGoods_NonEmptySlots(
        ItemStack itemStack, int quantity, InventoryPlayer inv, ArrayList<Integer> slotIndexes
    ) {
        IInventory inventory = (IInventory)inv;
        int maxStackQuantity = ItemHelper.getItemStackMaxQuantity(itemStack, inventory);

        for (int i = 0; i < inventory.getSizeInventory() && quantity > 0; i++)
        {
            ItemStack invItemStack = inventory.getStackInSlot(i);

            if (invItemStack == null || !ItemHelper.areItemEquals(itemStack, invItemStack)) {
                continue;
            }

            int invQuantity = ItemHelper.getItemStackQuantity(invItemStack);

            if (invQuantity >= maxStackQuantity) {
                continue;
            }

            int preparedQuantity = maxStackQuantity - invQuantity;

            if (preparedQuantity > quantity) {
                preparedQuantity = quantity;
            }

            slotIndexes.add(i);

            quantity -= preparedQuantity;
        }

        return quantity;
    }

    private int searchTransferGoods_emptySlots(
        ItemStack itemStack, int quantity, InventoryPlayer inv, ArrayList<Integer> slotIndexes
    ) {
        IInventory inventory = (IInventory)inv;

        for (int i = 0; i < inventory.getSizeInventory() && quantity > 0; i++)
        {
            ItemStack invItemStack = inventory.getStackInSlot(i);

            if (invItemStack != null) {
                continue;
            }

            slotIndexes.add(i);

            quantity = 0;
        }

        return quantity;
    }

    private int getPriceSlotIndex(int goodSlotIndex)
    {
        for (int i = 0; i < TileEntityBigStall.GoodsSlotIndexes.length; i++)
        {
            if (TileEntityBigStall.GoodsSlotIndexes[i] == goodSlotIndex) {
                return TileEntityBigStall.PricesSlotIndexes[i];
            }
        }

        return -1;
    }

    private boolean isPlayerSlot(int slotNumber)
    {
        return isOwnerMode
            ? slotNumber >= TileEntityBigStall.ItemCount
            : slotNumber >= TileEntityBigStall.ItemCount - 1;
    }

    private boolean isBookSlot(int slotNumber)
    {
        return isOwnerMode && slotNumber == TileEntityBigStall.ItemCount - 1;
    }

    private int getBookSlotIndex()
    {
        return TileEntityBigStall.ItemCount - 1;
    }

    public boolean isGoodsSlot(int slotNumber)
    {
        return slotNumber < 2 * TileEntityBigStall.PriceCount && slotNumber % 2 == 1;
    }
 }
