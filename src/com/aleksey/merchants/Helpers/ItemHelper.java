package com.aleksey.merchants.Helpers;

import com.aleksey.merchants.Extended.AnimalInCrate;
import com.aleksey.merchants.Extended.ExtendedLogic;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.bioxx.tfc.Food.ItemFoodTFC;
import com.bioxx.tfc.TileEntities.TEIngotPile;
import com.bioxx.tfc.api.Food;
import com.bioxx.tfc.api.Interfaces.IFood;
import static com.aleksey.merchants.Extended.ExtendedLogic.isNoSplitFood;
import static com.aleksey.merchants.Extended.ExtendedLogic.getNoSplitFoodWeight;
import com.aleksey.merchants.Extended.Integration;
import com.bioxx.tfc.Items.ItemBlocks.ItemBarrels;
import com.bioxx.tfc.Items.ItemBlocks.ItemLargeVessel;
import com.bioxx.tfc.Items.Pottery.ItemPotteryJug;
import com.bioxx.tfc.Items.Pottery.ItemPotterySmallVessel;


public class ItemHelper {

    public static final boolean areItemEquals(ItemStack itemStack1, ItemStack itemStack2 ) {
        if (itemStack1 == null || itemStack2 == null) {
            return false;
        }

        if (itemStack1.getItem() != itemStack2.getItem() || itemStack1.getItemDamage() != itemStack2.getItemDamage()) {
            return false;
        }

        return itemStack1.getItem() instanceof IFood
                //? Food.areEqual(itemStack1, itemStack2)
                
                // consider CookedLevel ignore CookedProFile & FuelProFile
                ? ExtendedLogic.areFoodEqual(itemStack1, itemStack2) 
                
                : ItemStack.areItemStackTagsEqual(itemStack1, itemStack2);                
    }

    public static final String getItemKey(ItemStack itemStack) {
        Item item = itemStack.getItem();
        String key = "";
        if (ExtendedLogic.IGNOREBARRELWOODTYPE && item instanceof ItemBarrels)
        {
            //ignore wood material(metadata) of burrels
            key = String.valueOf(Item.getIdFromItem(item)); 
        }
        else
            key = String.valueOf(Item.getIdFromItem(item)) + ":" + String.valueOf(itemStack.getItemDamage());        
        
        if (item instanceof ItemBarrels || item instanceof ItemLargeVessel)
        {   //Extended key type for barrels and vessels sealed\no date fluid
            key = ExtendedLogic.getKeyForBarrel(key, itemStack);
        }

        //for correct quantity display of new and used jug and smallvessel
        //used jugs have "blowTime" tag with random value  
        //used vessels have empty "Items" tag
        if (item instanceof ItemPotteryJug || item instanceof ItemPotterySmallVessel)
        {
            key =  itemStack.hasTagCompound() ? key+":1" : key;
            if ( item instanceof ItemPotterySmallVessel)
                key =  ExtendedLogic.getKeyForSmallVessel(key,itemStack);
            return key;
        }
        
        if ( item.getClass() == Integration.ItemCrateClass )
        { 
            // Added info to key - animal ID
            key = AnimalInCrate.getItemKeyForAnimalCrate(itemStack, key);
            return key;
        }
            
        
        if (!(item instanceof IFood)) {
            key = ExtendedLogic.getKeyForSmithingItem(itemStack,key);
            return key;
        }
        
       
        key += ":"
                + (Food.isBrined(itemStack) ? "1" : "0")
                + (Food.isPickled(itemStack) ? "1" : "0")
                //+ (Food.isCooked(itemStack) ? "1" : "0")
                + ExtendedLogic.getCookedLevel(itemStack)// 0 - not Cooked 1 - light 5 - Dark
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
            if (foodDecay <= ExtendedLogic.PERMISSIBLEDECLAY && isNoSplitFood(itemStack) ) {
                foodDecay = 0;  //only for the calculations of quantity
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
            
            // destroy garbage decay food scraps 
            if ( newQuantity < 5 &&  newQuantity - Food.getDecay(itemStack) < 2 )    
            //if ( newQuantity < 2 && Food.getDecay(itemStack) > 0.4f )
                itemStack.stackSize = 0;
            
        } else {
            itemStack.stackSize += quantity;
        }
    }

    public static final void setStackQuantity(ItemStack itemStack, int quantity) {
        if (itemStack.getItem() instanceof IFood) {
            if (ExtendedLogic.isNoSplitFood(itemStack))
            {
                //pass unchanged as is
            }
            else
                ItemFoodTFC.createTag(itemStack, quantity);
        } else {
            itemStack.stackSize = quantity;
        }
    }
}
