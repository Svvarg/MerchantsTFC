package com.aleksey.merchants.WarehouseContainers;

import com.aleksey.merchants.Extended.ExtendedLogic;
import java.util.ArrayList;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import com.aleksey.merchants.Helpers.ItemHelper;
import com.aleksey.merchants.Helpers.SmallVesselHelper;
import com.aleksey.merchants.api.IWarehouseContainer;
import com.aleksey.merchants.api.ItemSlot;
import com.aleksey.merchants.api.ItemTileEntity;
import com.bioxx.tfc.Items.Pottery.ItemPotterySmallVessel;
import static com.aleksey.merchants.Extended.ExtendedLogic.getNoSplitFoodWeight;

public abstract class Container implements IWarehouseContainer
{
    @Override
    public int searchItems(
            TileEntity tileEntity,
            ItemStack itemStack,
            int requiredQuantity,
            ArrayList<ItemTileEntity> resultList
            )
    {
        if(!canSearchItem(tileEntity))
            return 0;

        IInventory inventory = (IInventory)tileEntity;
        ItemTileEntity itemTileEntity = null;
        int quantity = requiredQuantity;

        for(int i = 0; i < inventory.getSizeInventory() && quantity > 0; i++)
        {
            ItemStack invItemStack = inventory.getStackInSlot(i);

            if(invItemStack == null)
                continue;

            int invQuantity = 0;

           // if(ItemHelper.areItemEquals(itemStack, invItemStack))

          //always first call StallFaceSlot in ELogic.areItemEquals
          //how i see searchItems call only for goodStack therefore invIStack first
          //for sell player itemsWithSmithing < that have StallFaceSlot
          if(ExtendedLogic.areItemEquals(itemStack, invItemStack ))
            {
                invQuantity = ItemHelper.getItemStackQuantity(invItemStack);

                int nsfoodWeight = getNoSplitFoodWeight(invItemStack);
                if (nsfoodWeight > 0 && invQuantity != nsfoodWeight)
                {
                    invQuantity = 0;
                }
            }
            else if(invItemStack.getItem() instanceof ItemPotterySmallVessel)
            //    invQuantity = SmallVesselHelper.getItemStackQuantity(itemStack, invItemStack);
                invQuantity = SmallVesselHelper.getItemStackQuantity(itemStack, invItemStack);

            if(invQuantity <= 0)
                continue;

            if(itemTileEntity == null)
                resultList.add(itemTileEntity = new ItemTileEntity(this, tileEntity));

            ItemSlot itemSlot = new ItemSlot(i, invQuantity < quantity ? invQuantity: quantity);

            itemTileEntity.Items.add(itemSlot);

            quantity -= itemSlot.Quantity;
        }

        return requiredQuantity - quantity;
    }

    @Override
    public int searchFreeSpaceInSmallVessels(
            TileEntity tileEntity,
            ItemStack itemStack,
            int requiredQuantity,
            ArrayList<ItemTileEntity> resultList
            )
    {
        if(!canSearchFreeSpace(tileEntity))
            return 0;

        IInventory inventory = (IInventory)tileEntity;
        ItemTileEntity itemTileEntity = resultList.size() > 0 ? resultList.get(resultList.size() - 1): null;

        if(itemTileEntity != null && itemTileEntity.TileEntity != tileEntity)
            itemTileEntity = null;

        int quantity = requiredQuantity;

        for(int i = 0; i < inventory.getSizeInventory() && quantity > 0; i++)
        {
            ItemStack invItemStack = inventory.getStackInSlot(i);

            if(invItemStack == null || !(invItemStack.getItem() instanceof ItemPotterySmallVessel))
                continue;

            //important inside ItemHelper.areItemEquals not ELogic.areItemEquals
            int addQuantity = SmallVesselHelper.getFreeSpace(itemStack, invItemStack);

            if(addQuantity <= 0)
                continue;

            if(itemTileEntity == null)
                resultList.add(itemTileEntity = new ItemTileEntity(this, tileEntity));

            ItemSlot itemSlot = new ItemSlot(i, addQuantity);

            if(itemSlot.Quantity > quantity)
                itemSlot.Quantity = quantity;

            itemTileEntity.Items.add(itemSlot);

            quantity -= itemSlot.Quantity;
        }

        return requiredQuantity - quantity;
    }

    protected boolean canSearchItem(TileEntity tileEntity)
    {
        return true;
    }

    protected boolean canSearchFreeSpace(TileEntity tileEntity)
    {
        return true;
    }
}
