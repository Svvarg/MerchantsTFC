package com.aleksey.merchants.Helpers;

import com.aleksey.merchants.Extended.ExtendedLogic;
import java.util.ArrayList;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.bioxx.tfc.api.TFCItems;
import com.bioxx.tfc.Items.ItemMeltedMetal;
import com.bioxx.tfc.Items.Pottery.ItemPotteryBase;
import com.bioxx.tfc.api.Constant.Global;
import com.bioxx.tfc.api.Enums.EnumSize;
import com.bioxx.tfc.api.Interfaces.IBag;
import com.bioxx.tfc.api.Interfaces.IFood;
import com.bioxx.tfc.api.Interfaces.ISize;
import static com.aleksey.merchants.Extended.ExtendedLogic.isNoSplitFood;
import static com.aleksey.merchants.Extended.ExtendedLogic.getNoSplitFoodWeight;

public class SmallVesselHelper
{
    private static final ArrayList<Item> _exceptions = new ArrayList<Item>();
    private static final EnumSize _vesselSlotSize = EnumSize.SMALL;

    public static void decreaseItemStackQuantity(int quantity, ItemStack itemStack, ItemStack vessel)
    {
        ItemStack[] vesselItemStacks = getVesselItemStacks(vessel);

        //hey man! new small vessel have null nbt tag!
        if (vesselItemStacks == null)
            return;

        for(int i = 0; i < vesselItemStacks.length && quantity > 0; i++)
        {
            ItemStack invItemStack = vesselItemStacks[i];

            //if(invItemStack == null || !ItemHelper.areItemEquals(invItemStack, itemStack))

            //importance of following the sequence of Stacks! First itemStack - item frome Stall Slot, after vesselIStack
            //This for correct work trade with diff smithing bonus(can buy and sell items with more bonus that stay at stall
            if(invItemStack == null || !ExtendedLogic.areItemEquals(itemStack, invItemStack))

                continue;

            int invQuantity = ItemHelper.getItemStackQuantity(invItemStack);

            // dont allow cut buckets and Jugs at small vessels on give goods from storage
            int nsFoodWeight = getNoSplitFoodWeight(invItemStack);
            if (nsFoodWeight > 0  &&  nsFoodWeight != invQuantity )
                continue;

            int decQuantity = invQuantity < quantity ? invQuantity: quantity;

            ItemHelper.increaseStackQuantity(invItemStack, -decQuantity);

            quantity -= decQuantity;
        }

        setVesselItemStacks(vesselItemStacks, vessel);
    }

    public static void increaseItemStackQuantity(int quantity, ItemStack itemStack, ItemStack vessel)
    {
        ItemStack[] vesselItemStacks = getVesselItemStacks(vessel);

        if ( vesselItemStacks == null )
            vesselItemStacks = new ItemStack[4];

        quantity = increaseItemStackQuantity_NonEmptySlots(quantity, itemStack, vesselItemStacks);

        if(quantity > 0)
            increaseItemStackQuantity_EmptySlots(quantity, itemStack, vesselItemStacks);

        setVesselItemStacks(vesselItemStacks, vessel);
    }

    private static int increaseItemStackQuantity_NonEmptySlots(int quantity, ItemStack itemStack, ItemStack[] vesselItemStacks)
    {
        int maxQuantity = ItemHelper.getItemStackMaxQuantity_SmallVessel(itemStack);

        for(int i = 0; i < vesselItemStacks.length && quantity > 0; i++)
        {
            ItemStack invItemStack = vesselItemStacks[i];

            if(invItemStack == null || !ItemHelper.areItemEquals(invItemStack, itemStack))
                continue;

            int invQuantity = ItemHelper.getItemStackQuantity(invItemStack);
            int addQuantity = invQuantity + quantity > maxQuantity ? maxQuantity - invQuantity: quantity;

            ItemHelper.increaseStackQuantity(invItemStack, addQuantity);

            quantity -= addQuantity;
        }

        return quantity;
    }

    private static int increaseItemStackQuantity_EmptySlots(int quantity, ItemStack itemStack, ItemStack[] vesselItemStacks)
    {
        if ( vesselItemStacks == null )
            return 0;

        int maxQuantity = ItemHelper.getItemStackMaxQuantity_SmallVessel(itemStack);

        for(int i = 0; i < vesselItemStacks.length && quantity > 0; i++)
        {
            if(vesselItemStacks[i] != null)
                continue;

            int addQuantity = quantity > maxQuantity ? maxQuantity: quantity;

            ItemStack invItemStack = itemStack.copy();

            ItemHelper.setStackQuantity(invItemStack, addQuantity);

            vesselItemStacks[i] = invItemStack;

            quantity -= addQuantity;
        }

        return quantity;
    }

    public static int getItemStackQuantity(ItemStack itemStack, ItemStack vessel)
    {
        if(itemStack == null || vessel == null)
            return 0;

        ItemStack[] vesselItemStacks = getVesselItemStacks(vessel);
        // hey man! new small Vessel have null nbt tag!
        if (vesselItemStacks == null )
            return 0;

        int quantity = 0;

        for(int i = 0; i < vesselItemStacks.length; i++)
        {
            ItemStack vesselItemStack = vesselItemStacks[i];

            //if(vesselItemStack != null && ItemHelper.areItemEquals(vesselItemStack, itemStack))

            //importance of following the sequence of Stacks! First itemStack - itemStack frome StallFaceSlot, after vesselIStack
            //This for correct work trade with diff smithing bonus(can buy and sell items with more bonus that stay at stall
            if(vesselItemStack != null && ExtendedLogic.areItemEquals(itemStack, vesselItemStack ))

            {
                quantity += ItemHelper.getItemStackQuantity(vesselItemStack);

                // noSplitFood from ExtendedLogic dont allow put this food type on smallvessel by stall

            }
        }

        return quantity;
    }

    public static int getFreeSpace(ItemStack itemStack, ItemStack vessel)
    {
        if(vessel == null || itemStack == null || !isItemValid(itemStack))
            return 0;

        ItemStack[] vesselItemStacks = getVesselItemStacks(vessel);

        int maxQuantity = ItemHelper.getItemStackMaxQuantity_SmallVessel(itemStack);

        if(vesselItemStacks == null)
            return maxQuantity;

        int freeSpace = 0;

        for(int i = 0; i < vesselItemStacks.length; i++)
        {
            ItemStack vesselItemStack = vesselItemStacks[i];

            if(vesselItemStack != null && !ItemHelper.areItemEquals(vesselItemStack, itemStack))
                continue;

            int invQuantity = vesselItemStack != null ? ItemHelper.getItemStackQuantity(vesselItemStack): 0;

            freeSpace += maxQuantity - invQuantity;
        }

        return freeSpace;
    }

    public static ItemStack[] getVesselItemStacks(ItemStack vessel)
    {
        if(vessel == null || !vessel.hasTagCompound())
            return null;

        ItemStack[] result = new ItemStack[4];

        NBTTagList nbttaglist = vessel.getTagCompound().getTagList("Items", 10);

        if ( nbttaglist == null)
            return null;

        for(int i = 0; i < nbttaglist.tagCount(); i++)
        {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            byte byte0 = nbttagcompound1.getByte("Slot");

            if(byte0 >= 0 && byte0 < 4)
            {
                ItemStack is = ItemStack.loadItemStackFromNBT(nbttagcompound1);

                if(is.stackSize >= 1)
                    result[byte0] = is;
            }
        }

        return result;
    }

    private static void setVesselItemStacks(ItemStack[] itemStacks, ItemStack vessel)
    {
        if(itemStacks == null || vessel == null)
            return;

        NBTTagList nbttaglist = new NBTTagList();

        for(int i = 0; i < itemStacks.length; i++)
        {
            if(itemStacks[i] != null && itemStacks[i].getItem() instanceof IFood)
            {
                NBTTagCompound nbt = itemStacks[i].getTagCompound();

                if(nbt.hasKey("foodDecay") && nbt.getFloat("foodDecay") / Global.FOOD_MAX_WEIGHT > 0.9f)
                    itemStacks[i] = null;
            }

            if(itemStacks[i] != null)
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte)i);
                itemStacks[i].writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        if(!vessel.hasTagCompound())
            vessel.setTagCompound(new NBTTagCompound());

        vessel.getTagCompound().setTag("Items", nbttaglist);
    }

    private static boolean isItemValid(ItemStack itemstack)
    {
        if(_exceptions == null)
        {
            _exceptions.add(TFCItems.bismuthIngot);
            _exceptions.add(TFCItems.bismuthBronzeIngot);
            _exceptions.add(TFCItems.blackBronzeIngot);
            _exceptions.add(TFCItems.blackSteelIngot);
            _exceptions.add(TFCItems.blueSteelIngot);
            _exceptions.add(TFCItems.brassIngot);
            _exceptions.add(TFCItems.bronzeIngot);
            _exceptions.add(TFCItems.copperIngot);
            _exceptions.add(TFCItems.goldIngot);
            _exceptions.add(TFCItems.wroughtIronIngot);
            _exceptions.add(TFCItems.leadIngot);
            _exceptions.add(TFCItems.nickelIngot);
            _exceptions.add(TFCItems.pigIronIngot);
            _exceptions.add(TFCItems.platinumIngot);
            _exceptions.add(TFCItems.redSteelIngot);
            _exceptions.add(TFCItems.roseGoldIngot);
            _exceptions.add(TFCItems.silverIngot);
            _exceptions.add(TFCItems.steelIngot);
            _exceptions.add(TFCItems.bismuthIngot);
            _exceptions.add(TFCItems.sterlingSilverIngot);
            _exceptions.add(TFCItems.tinIngot);
            _exceptions.add(TFCItems.zincIngot);
        }

        Item item = itemstack.getItem();

        if(item instanceof IBag || item instanceof ItemMeltedMetal || item instanceof ItemPotteryBase)
            return false;

        if(item instanceof IFood && !isNoSplitFood(itemstack) )
            return true;

        if (item instanceof ISize && ((ISize)item).getSize(itemstack).stackSize >= _vesselSlotSize.stackSize)
                return false;

        boolean except = _exceptions.contains(itemstack.getItem());

        if(item instanceof ISize && ((ISize)item).getSize(itemstack).stackSize >= _vesselSlotSize.stackSize && !except)
            return true;

        if (!(item instanceof ISize) && !except)
            return true;

        return false;
    }
}
