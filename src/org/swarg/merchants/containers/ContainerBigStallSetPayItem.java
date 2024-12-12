package org.swarg.merchants.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.swarg.merchants.gui.GuiBigStallSetPayItem;
import org.swarg.merchants.tileentities.TileEntityBigStall;

import com.bioxx.tfc.Containers.ContainerTFC;
import com.bioxx.tfc.Containers.Slots.SlotForShowOnly;
import com.bioxx.tfc.Core.Player.PlayerInventory;


/**
 * 12-12-2024
 * @author Swarg
 */
public class ContainerBigStallSetPayItem extends ContainerTFC
{
    public ContainerBigStallSetPayItem(
        InventoryPlayer inv, TileEntityBigStall stall,
        World world, int x, int y, int z
    ) {
        addSlotToContainer(new SlotForShowOnly(
            stall, stall.getActivePriceSlotIndex(),
            GuiBigStallSetPayItem.PriceSlotX,
            GuiBigStallSetPayItem.PriceSlotY
        ));

        PlayerInventory.buildInventoryLayout(this, inv, 8,
            GuiBigStallSetPayItem.WindowHeight - 1 + 5, false, true);
    }

    @Override
    public boolean canInteractWith(EntityPlayer entityplayer)
    {
        return true;
    }

    @Override
    public ItemStack transferStackInSlotTFC(EntityPlayer player, int slotNumber)
    {
        if (slotNumber < 2) {
            return null;
        }

        Slot slot = (Slot)inventorySlots.get(slotNumber);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();

            if (!this.mergeItemStack(itemstack1, 2, this.inventorySlots.size(), true)) {
                return null;
            }

            if (itemstack1.stackSize == 0){
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }
        }

        return null;
    }
}

