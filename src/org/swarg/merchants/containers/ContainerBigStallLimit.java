package org.swarg.merchants.containers;

import net.minecraft.world.World;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;

import org.swarg.merchants.gui.GuiBigStallLimit;
import org.swarg.merchants.tileentities.TileEntityBigStall;

import com.bioxx.tfc.Containers.ContainerTFC;
import com.bioxx.tfc.Containers.Slots.SlotForShowOnly;
import com.bioxx.tfc.Core.Player.PlayerInventory;

/**
 * 12-12-2024
 * @author Swarg
 */
public class ContainerBigStallLimit extends ContainerTFC
{
    public ContainerBigStallLimit(
        InventoryPlayer inv, TileEntityBigStall stall,
        World world, int x, int y, int z
    ) {
        addSlotToContainer(new SlotForShowOnly(
            stall, stall.getActivePriceSlotIndex(),
            GuiBigStallLimit.PriceSlotX, GuiBigStallLimit.SlotY
        ));
        addSlotToContainer(new SlotForShowOnly(
            stall, stall.getActiveGoodSlotIndex(),
            GuiBigStallLimit.GoodSlotX, GuiBigStallLimit.SlotY
        ));

        PlayerInventory.buildInventoryLayout(
            this, inv, 8, GuiBigStallLimit.WindowHeight - 1 + 5, false, true);
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

        Slot slot = (Slot)this.inventorySlots.get(slotNumber);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();

            if (!this.mergeItemStack(itemstack1, 2, this.inventorySlots.size(), true)) {
                return null;
            }

            if (itemstack1.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }
        }

        return null;
    }
}
