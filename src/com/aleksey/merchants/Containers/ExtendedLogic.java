package com.aleksey.merchants.Containers;

import com.aleksey.merchants.Helpers.ItemHelper;
import com.aleksey.merchants.api.ItemSlot;
import com.aleksey.merchants.api.ItemTileEntity;
import com.bioxx.tfc.Food.ItemFoodTFC;
import com.bioxx.tfc.Food.ItemSalad;
import com.bioxx.tfc.Items.ItemBlocks.ItemBarrels;
import com.bioxx.tfc.Items.ItemBlocks.ItemLargeVessel;
import com.bioxx.tfc.Items.ItemTFCArmor;
import com.bioxx.tfc.Items.ItemTerra;
import com.bioxx.tfc.Items.Tools.ItemCustomBucketMilk;
import com.bioxx.tfc.Items.Tools.ItemWeapon;
import static com.bioxx.tfc.api.Crafting.AnvilManager.getCraftTag;
import static com.bioxx.tfc.api.Crafting.AnvilManager.getDamageBuff;
import static com.bioxx.tfc.api.Crafting.AnvilManager.getDurabilityBuff;
import static com.bioxx.tfc.api.Crafting.AnvilManager.setDamageBuff;
import static com.bioxx.tfc.api.Crafting.AnvilManager.setDurabilityBuff;
import com.bioxx.tfc.api.Food;
import com.bioxx.tfc.api.Interfaces.IFood;
import java.util.ArrayList;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 *
 * @author Swarg
 */
public class ExtendedLogic {

    //private static final String BUCKETMILK = "com.bioxx.tfc.Items.Tools.ItemCustomBucketMilk";
    private static final String JUGMILK = "ItemCeramicJugMilk";
    private static final int BUCKETMILKWEIGHT = 20;
    private static final int SALADWEIGHT = 20;
    private static final int JUGMILKWEIGHT = 80;
    public static final int PERMISSIBLEDECLAY = 3;
    public static final int permissibleSealTimeHours = 8760;//24*365ч
    public static final boolean ToNonEmptySlot = true;
    public static final boolean ToEmptySlot = false;
    
    //for towny private system. dont work with containers on another chunks
    public static boolean seeContainersOnlyWarehouseChunk = true; 
    

    /**
     * Check the ItemStack of the fact that it is Milk Jug, Bukket or Salad
     * is not split TFC Food
     */
    public static boolean isNoSplitFood(ItemStack srcItemStack)
    {        
        if (srcItemStack == null) {
            return false;
        }
        
        Class<?> cls = srcItemStack.getItem().getClass();
        if ( cls == ItemCustomBucketMilk.class || cls == ItemSalad.class )
            return true;
        
        String itemClass = srcItemStack.getItem().getClass().toString();
        return itemClass != null && !itemClass.isEmpty() && 
                ( itemClass.contains(JUGMILK) /*|| itemClass.contains(BUCKETMILK)*/);
    }
    
    /**
     *  base weight for no-split tfcFood such as milk jugs, buckets or Salad
     */
    public static int getNoSplitFoodWeight(ItemStack srcItemStack)
    {
        
        if (srcItemStack != null && srcItemStack.getItem() instanceof IFood )
        {       
            Class<?> cls = srcItemStack.getItem().getClass();
            if ( cls == ItemCustomBucketMilk.class )
                return BUCKETMILKWEIGHT;
            
            if (cls == ItemSalad.class)
                return SALADWEIGHT;

            String itemClass = srcItemStack.getItem().getClass().toString();
            if (itemClass == null) {
                return 0;
            }
            if (itemClass.contains(JUGMILK))
            {
                return JUGMILKWEIGHT;
            }                
        }
        return 0;
    }
    
    
    public static final boolean areItemEquals(ItemStack itemStack1, ItemStack itemStack2) //, boolean putToNonEmptySlot ) 
    {
        if (itemStack1 == null || itemStack2 == null) {
            return false;
        }

        if (itemStack1.getItem() != itemStack2.getItem() || itemStack1.getItemDamage() != itemStack2.getItemDamage()) {
            return false;
        }

        return itemStack1.getItem() instanceof IFood
                ? Food.areEqual(itemStack1, itemStack2)
                //: ItemStack.areItemStackTagsEqual(itemStack1, itemStack2);
                : ExtendedLogic.areItemStackTagsEqualEx(itemStack1, itemStack2);//, putToNonEmptySlot);
    }
    
    
    
    /**
     *  For Equal TFC items like burrel with date, tools-armor-weapons with SwimingBonus, food with temperature
     */
    public static boolean areItemStackTagsEqualEx(ItemStack st1, ItemStack st2)  //, boolean putToNonEmptySlot)
    {
        /*return st1 == null && st2 == null ? true : 
                   (st1 != null && st2 != null ? (st1.stackTagCompound == null && st2.stackTagCompound != null ? false : st1.stackTagCompound == null || st1.stackTagCompound.equals(st2.stackTagCompound)) : false);
        */
        if (st1 == null && st2 == null ) 
        {
            return true;
        } 
        else if ( st1 != null && st2 != null ) 
        {
          if ( st1.stackTagCompound == null && st2.stackTagCompound != null ) 
          {
            if ( st2.stackTagCompound.hasKey("craftingTag") )
            {  //at Stall withount bonus and outItem with bonus
                return true;//areSmithingItemEqual( st1, st2 ); 
            }    
            return false;  
          }
          else 
          {
              if (st1.stackTagCompound == null)
                  return true; 
              //return ( st1.stackTagCompound == null || st1.stackTagCompound.equals(st2.stackTagCompound) );              
               
              // Don`t allow to combine at stack items, according to the logic suitable for trade
              // then stall search place for price-item in container to put it
              // at searchFreeSpace_NonEmptySlot 
              if (true)//!putToNonEmptySlot)
              {
                 
                  //place for check barrel smelting items and food
                  Class<?> cls = st1.getItem().getClass();
                  
                  if (cls == ItemBarrels.class || cls == ItemLargeVessel.class )
                  {
                      return areBarrelsEqual(st1,st2);   
                  }                  
                  
                  //TFC smithingItem 
                  if ( st1.stackTagCompound.hasKey("craftingTag") || st2.stackTagCompound.hasKey("craftingTag") )
                  {
                      return areSmithingItemEqual(st1,st2); 
                  }              
              }
              
              return st1.stackTagCompound.equals(st2.stackTagCompound);              
          }   
        }        
        return false;
    }        
    
    /**
     * for compare barrels and LargeVessel without sealTime value;
     */ 
    public static boolean areBarrelsEqual(ItemStack st1, ItemStack st2) {        
        //it`s check no needed
        if (st1 == null || st2 == null || st1.stackTagCompound == null || 
                st2.stackTagCompound == null)
            return true; 
        
        if ( st1.stackTagCompound.getBoolean("Sealed") &&
                st2.stackTagCompound.getBoolean("Sealed") )
        {
            int sealTime1 = st1.stackTagCompound.getInteger("SealTime");
            int sealTime2 = st2.stackTagCompound.getInteger("SealTime");
            
            if ( sealTime1 == sealTime2 )
                return st1.stackTagCompound.equals(st2.stackTagCompound);
            
            //considered barrels closing time more than a year in different
            if (permissibleSealTimeHours < Math.abs( sealTime1 - sealTime2 ) )
                return false;
        
            st1.stackTagCompound.removeTag("SealTime");
            st2.stackTagCompound.removeTag("SealTime");
            boolean equal = st1.stackTagCompound.equals(st2.stackTagCompound);
            if ( sealTime1 > 0 ) 
                st1.stackTagCompound.setInteger("SealTime", sealTime1);
            if (sealTime2 > 0 )
                st2.stackTagCompound.setInteger("SealTime", sealTime2);
            return equal;
            //покупка бочки на лице 803, скалд 1028  st1 - лицо st2 - на складе
            //выдал бочку с лица лавки 803 хотя внутри стояла бочка 1028
            
            //продаю бочку st1 = 803 лицо
            //1044 ст2 в руках игрока
            //на склад прошла 803
        }
        
        return st1.stackTagCompound.equals(st2.stackTagCompound);
    }

    /** 
    * For compare Smithing items by Smithing Bonus 
    * for can buy goods with less or equal bonus that stay at stall face in the Slot
    * or for sell goods with above or equal bounus than have StallFaceSlot
    * import Stack1 - is Stack From Face-Stall-Slot
    */ 
    public static boolean areSmithingItemEqual(ItemStack st1, ItemStack st2) {        
        //it`s check no needed but...
        if (st1 == null || st2 == null ) 
            return false;       
        
        NBTTagCompound craftTag1 = null;
        NBTTagCompound craftTag2 = null;
        float duraBuff1 = 0;
        float damageBuff1 = 0;
        float duraBuff2 = 0;        
        float damageBuff2 = 0;
        
        // can be null
        if (st1.hasTagCompound() && st1.getTagCompound().hasKey("craftingTag"))
        {
            craftTag1 = getCraftTag(st1);
            duraBuff1 = getDurabilityBuff(st1);
            damageBuff1 = getDamageBuff(st1);                      
        }
        
        // can`t be null 
        if (st2.hasTagCompound() && st2.getTagCompound().hasKey("craftingTag"))
        {
            craftTag2 = getCraftTag(st2);
            duraBuff2 = getDurabilityBuff(st2);
            damageBuff2 = getDamageBuff(st2);                      
        } else
            return false;
                 
        if (duraBuff1 == duraBuff2 && damageBuff1 == damageBuff2) 
        {
               return st1.stackTagCompound.equals(st2.stackTagCompound);
        }
                
        if (craftTag1 == null && craftTag2 != null)
            return true;
        
        if ( duraBuff1 > duraBuff2 || damageBuff1 > damageBuff2)
            return false;
        
        if (craftTag1 != null)
        {
            craftTag1.removeTag("durabuff");
            if ( damageBuff1 > 0)//armor
                craftTag1.removeTag("damagebuff");
        }
        
        if (craftTag2 != null)
        {
            craftTag2.removeTag("durabuff");
            if ( damageBuff2 > 0)//armor
                craftTag2.removeTag("damagebuff");
        }
           
           
        boolean equal = st1.stackTagCompound.equals(st2.stackTagCompound);
           
        
        if (craftTag1 != null)
        {
            if (damageBuff1 > 0)
                setDamageBuff(st1, damageBuff1);
            
            if ( duraBuff1 > 0 ) 
                setDurabilityBuff(st1, duraBuff1);
        }    
        
        if (craftTag2 != null)
        {
            if ( damageBuff2 > 0)
                setDamageBuff(st2, damageBuff2);
            
            if ( duraBuff2 > 0 )
                setDurabilityBuff(st2, duraBuff2);
        }    
        
        return equal;                        
    }    
    
    
    /**
     * get first itemStack From Warehouse Container List
     * for sell itemStack not from StallFaceSlot, but from warehouse container 
     */    
    public static ItemStack getFirstItemStackFromItemTileEntity( 
            ArrayList<ItemTileEntity> list, 
            ItemStack goodStack 
            ) 
    {
        if ( list==null|| list.isEmpty() )
            return null;
        
        ItemTileEntity iTE = list.get(0);
        if (iTE == null || iTE.Container == null ||iTE.TileEntity == null || iTE.Items == null || iTE.Items.isEmpty())
            return null;
        ItemSlot Slot = iTE.Items.get(0);
        IInventory inv = (IInventory) iTE.TileEntity;
                  
        if (Slot == null || inv == null || Slot.SlotIndex < 0 || Slot.SlotIndex >= inv.getSizeInventory() )
            return null;
        
        ItemStack iStack = inv.getStackInSlot( Slot.SlotIndex );
        
        if (iStack == null)
            return null;
        
        iStack = iStack.copy();
        
        if (iStack.getItem() instanceof IFood )
        {   //cut decay inside
            ItemFoodTFC.createTag(iStack, Food.getWeight(goodStack) );
        }
        else 
        {
            iStack.stackSize = ItemHelper.getItemStackQuantity(goodStack);
        }
        
        return iStack.copy();
    }
    
    /**
     * for put itemStack to warehouse container with real nbt tag 
     * from player, not from StallFaceSlot 
     */
    public static ItemStack getFirstPayItemStackFromPlayer (
            ItemStack payItemStack,//for set count
            EntityPlayer player, 
            ArrayList<Integer> paySlotIndexes             
            )
    {
      if (player == null || player.inventory==null || paySlotIndexes == null )
          return null;
      
      int firstSlotIndex =  paySlotIndexes.get(0);
      
      if (firstSlotIndex < 0 || firstSlotIndex >= player.inventory.getSizeInventory() )
          return null;
                  
      ItemStack payStack = player.inventory.getStackInSlot( firstSlotIndex );
      
      if ( payStack == null || payItemStack == null ) 
          return null;
      
      payStack = payStack.copy();
      
      if (payStack.getItem() instanceof IFood )
        {   //cut decay inside
            ItemFoodTFC.createTag(payStack, Food.getWeight(payItemStack) );
        }
        else 
        {
            payStack.stackSize = payItemStack.stackSize ;
        }
      
      return payStack;
    }
}
