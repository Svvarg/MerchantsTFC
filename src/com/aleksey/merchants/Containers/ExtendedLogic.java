package com.aleksey.merchants.Containers;

import com.bioxx.tfc.Items.Tools.ItemCustomBucketMilk;
import com.bioxx.tfc.api.Interfaces.IFood;
import net.minecraft.item.ItemStack;

/**
 *
 * @author Swarg
 */
public class ExtendedLogic {

    //private static final String BUCKETMILK = "com.bioxx.tfc.Items.Tools.ItemCustomBucketMilk";
    private static final String JUGMILK = "ItemCeramicJugMilk";
    private static final int BUCKETMILKWEIGHT = 20;
    private static final int JUGMILKWEIGHT = 80;
    public static final int PERMISSIBLEDECLAY = 3;
    

    /**
     * Check the ItemStack of the fact that it is Milk Jug or Bukket
     * is not split TFC Food
     */
    public static boolean isMilkContainer(ItemStack srcItemStack)
    {        
        if (srcItemStack == null) {
            return false;
        }
        
        Class<?> cls = srcItemStack.getItem().getClass();
        if ( cls == ItemCustomBucketMilk.class )
            return true;
        
        String itemClass = srcItemStack.getItem().getClass().toString();
        return itemClass != null && !itemClass.isEmpty() && 
                ( itemClass.contains(JUGMILK) /*|| itemClass.contains(BUCKETMILK)*/);
    }
    
    /**
     *  weight for no-split tfcFood such as milk jugs and buckets 
     */
    public static int getMilkConteinerWeight(ItemStack srcItemStack)
    {
        
        if (srcItemStack != null && srcItemStack.getItem() instanceof IFood )
        {       
            Class<?> cls = srcItemStack.getItem().getClass();
            if ( cls == ItemCustomBucketMilk.class )
                return BUCKETMILKWEIGHT;

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
    
    /**
     *  For Equal TFC items like burrel with date, tools-armor-weapons with SwimingBonus, food with temperature
     */
    public static boolean areItemStackTagsEqualEx(ItemStack st1, ItemStack st2)
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
          if ( st1.stackTagCompound == null && st2.stackTagCompound != null) 
          {
            return false;  
          }
          else 
          {
              return ( st1.stackTagCompound == null || st1.stackTagCompound.equals(st2.stackTagCompound) );              
          }   
        }        
        return false;
    }    
}
