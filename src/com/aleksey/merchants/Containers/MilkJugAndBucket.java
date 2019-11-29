package com.aleksey.merchants.Containers;

import net.minecraft.item.ItemStack;

/**
 *
 * @author Swarg
 */
public class MilkJugAndBucket {

    private static final String BUCKETMILK = "com.bioxx.tfc.Items.Tools.ItemCustomBucketMilk";
    private static final String JUGMILK = "ItemCeramicJugMilk";
    private static final int BUCKETMILKWEIGHT = 20;
    private static final int JUGMILKWEIGHT = 80;
    

    /**
     * Check the ItemStack of the fact that it is Jug or Bukket with Milk
     */
    public static boolean isMilkContainer(ItemStack srcItemStack) {        
        if (srcItemStack == null) {
            return false;
        }
        String itemClass = srcItemStack.getItem().getClass().toString();

        return itemClass != null && (itemClass.contains(JUGMILK) || itemClass.contains(BUCKETMILK));
    }
    
    public static int getMilkConteinerWeight(ItemStack srcItemStack){
        if (isMilkContainer(srcItemStack)){
            String itemClass = srcItemStack.getItem().getClass().toString();
            if (itemClass == null){
                return 0;
            }
            if (itemClass.contains(JUGMILK)){
                return JUGMILKWEIGHT;
            }else if (itemClass.contains(BUCKETMILK)){
                return BUCKETMILKWEIGHT;
            }                
        }
        return 0;
    }
}
