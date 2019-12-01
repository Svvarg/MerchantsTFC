package com.aleksey.merchants.Containers;

import com.bioxx.tfc.Food.ItemSalad;
import com.bioxx.tfc.Items.ItemBlocks.ItemBarrels;
import com.bioxx.tfc.Items.ItemBlocks.ItemLargeVessel;
import com.bioxx.tfc.Items.ItemTerra;
import com.bioxx.tfc.Items.Tools.ItemCustomBucketMilk;
import static com.bioxx.tfc.api.Crafting.AnvilManager.getCraftTag;
import static com.bioxx.tfc.api.Crafting.AnvilManager.getDamageBuff;
import static com.bioxx.tfc.api.Crafting.AnvilManager.getDurabilityBuff;
import static com.bioxx.tfc.api.Crafting.AnvilManager.setDamageBuff;
import static com.bioxx.tfc.api.Crafting.AnvilManager.setDurabilityBuff;
import com.bioxx.tfc.api.Food;
import com.bioxx.tfc.api.Interfaces.IFood;
import java.util.Map;
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
                  
                  if ( st1.getItem() instanceof ItemTerra && st1.stackTagCompound.hasKey("craftingTag") )
                  {
                      return areSmithingItemEqual(st1,st2); 
                  }              
              }
              
              return st1.stackTagCompound.equals(st2.stackTagCompound);              
          }   
        }        
        return false;
    }        
    // for compare barrels and LargeVessel without sealTime value;
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
    * For compare Smithing items by bonus for can give goods with 
    * more bonus that stay at stall in the link item
    */ 
    public static boolean areSmithingItemEqual(ItemStack st1, ItemStack st2) {        
        //it`s check no needed
        if (st1 == null || st2 == null || st1.stackTagCompound == null || 
                st2.stackTagCompound == null)
            return true; 
        //if(is.hasTagCompound() && is.getTagCompound().hasKey("craftingTag"))
        NBTTagCompound craftTag1 = getCraftTag(st1);
        NBTTagCompound craftTag2 = getCraftTag(st2);
        
        if ( craftTag1 != null && craftTag2 != null )
        {
           float duraBuff1 = getDurabilityBuff(st1);
           float duraBuff2 = getDurabilityBuff(st2);           
           
           float damageBuff1 = getDamageBuff(st1);
           float damageBuff2 = getDamageBuff(st2);
           
           
           if (duraBuff1 == duraBuff2 && damageBuff1 == damageBuff2) {
               return st1.stackTagCompound.equals(st2.stackTagCompound);
           }
           
           if ( duraBuff1 > duraBuff2 || damageBuff1 > damageBuff2)
               return false;
           
           craftTag1.removeTag("durabuff");
           craftTag2.removeTag("durabuff");
           
           craftTag1.removeTag("damagebuff");
           craftTag2.removeTag("damagebuff");
                      
           
           boolean equal = st1.stackTagCompound.equals(st2.stackTagCompound);
           
           
           setDurabilityBuff(st1, duraBuff1);
           setDurabilityBuff(st2, duraBuff2);
                   
           setDamageBuff(st1, duraBuff1);
           setDamageBuff(st2, duraBuff2);
         
           return equal;
        }
        
        return st1.stackTagCompound.equals(st2.stackTagCompound);
    }    
    
}
