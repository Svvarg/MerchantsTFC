package com.aleksey.merchants.Helpers;

import com.aleksey.merchants.Containers.ExtendedLogic;
import static com.aleksey.merchants.Containers.ExtendedLogic.PERMISSIBLEDECLAY;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.bioxx.tfc.Food.ItemFoodTFC;
import com.bioxx.tfc.TileEntities.TEIngotPile;
import com.bioxx.tfc.api.Food;
import com.bioxx.tfc.api.Interfaces.IFood;
import static com.aleksey.merchants.Containers.ExtendedLogic.isNoSplitFood;
import static com.aleksey.merchants.Containers.ExtendedLogic.getNoSplitFoodWeight;

public class ItemHelper {

    public static final boolean areItemEquals(ItemStack itemStack1, ItemStack itemStack2) {
        if (itemStack1 == null || itemStack2 == null) {
            return false;
        }

        if (itemStack1.getItem() != itemStack2.getItem() || itemStack1.getItemDamage() != itemStack2.getItemDamage()) {
            return false;
        }

        return itemStack1.getItem() instanceof IFood
                ? Food.areEqual(itemStack1, itemStack2)
                //: ItemStack.areItemStackTagsEqual(itemStack1, itemStack2);
                : ExtendedLogic.areItemStackTagsEqualEx(itemStack1, itemStack2);
    }

    public static final String getItemKey(ItemStack itemStack) {
        Item item = itemStack.getItem();
        String key = String.valueOf(Item.getIdFromItem(item)) + ":" + String.valueOf(itemStack.getItemDamage());

        if (!(item instanceof IFood)) {
            return key;
        }

        key += ":"
                + (Food.isBrined(itemStack) ? "1" : "0")
                + (Food.isPickled(itemStack) ? "1" : "0")
                + (Food.isCooked(itemStack) ? "1" : "0")
                + (Food.isDried(itemStack) ? "1" : "0")
                + (Food.isSmoked(itemStack) ? "1" : "0")
                + (Food.isSalted(itemStack) ? "1" : "0");

        return key;
    }

    public static final int getItemStackQuantity(ItemStack itemStack) {
        return getItemStackQuantity(itemStack, true);
    }

    public static final int getItemStackQuantity(ItemStack itemStack, boolean removeDecay) {
        if (itemStack.getItem() instanceof IFood) {
            IFood food = (IFood) itemStack.getItem();
            float foodDecay = removeDecay ? Math.max(Food.getDecay(itemStack), 0) : 0;
            
            //for no split tfcfood for possible trade with small-permissible decay value
            if (foodDecay <= PERMISSIBLEDECLAY && isNoSplitFood(itemStack) ) {
                foodDecay = 0;                
            }
            
            int quantity = (int) (Food.getWeight(itemStack) - foodDecay);
            

            return quantity > 0 ? quantity : 0;
        }

        return itemStack.stackSize;
    }

    public static final int getItemStackMaxQuantity(ItemStack itemStack, IInventory inventory) {
        Item item = itemStack.getItem();

        if (item instanceof IFood) {
            
            int nsFoodWeight = getNoSplitFoodWeight(itemStack);
            if ( nsFoodWeight > 0 ) {
                return nsFoodWeight;
            }
            return (int) ((IFood) itemStack.getItem()).getFoodMaxWeight(itemStack);
        }

        if (inventory instanceof TEIngotPile) {
            return inventory.getInventoryStackLimit();
        }

        return Math.min(itemStack.getMaxStackSize(), inventory.getInventoryStackLimit());
    }

    public static final int getItemStackMaxQuantity_SmallVessel(ItemStack itemStack) {
        Item item = itemStack.getItem();

        if (item instanceof IFood) {
            return (int) ((IFood) itemStack.getItem()).getFoodMaxWeight(itemStack) / 2;
        }

        return itemStack.getMaxStackSize();
    }

    public static final void increaseStackQuantity(ItemStack itemStack, int quantity) {
        if (itemStack.getItem() instanceof IFood) {
            IFood food = (IFood) itemStack.getItem();
            float newQuantity = Food.getWeight(itemStack) + quantity;

            Food.setWeight(itemStack, newQuantity);
        } else {
            itemStack.stackSize += quantity;
        }
    }

    public static final void setStackQuantity(ItemStack itemStack, int quantity) {
        if (itemStack.getItem() instanceof IFood) {
            ItemFoodTFC.createTag(itemStack, quantity);
        } else {
            itemStack.stackSize = quantity;
        }
    }
}
