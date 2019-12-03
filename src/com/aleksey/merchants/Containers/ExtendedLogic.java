package com.aleksey.merchants.Containers;

import com.aleksey.merchants.Helpers.ItemHelper;
import static com.aleksey.merchants.Helpers.SmallVesselHelper.getVesselItemStacks;
import com.aleksey.merchants.api.ItemSlot;
import com.aleksey.merchants.api.ItemTileEntity;
import com.bioxx.tfc.Core.TFC_Time;
import com.bioxx.tfc.Food.ItemFoodTFC;
import com.bioxx.tfc.Food.ItemSalad;
import com.bioxx.tfc.Items.ItemBlocks.ItemBarrels;
import com.bioxx.tfc.Items.ItemBlocks.ItemLargeVessel;
import com.bioxx.tfc.Items.Pottery.ItemPotterySmallVessel;
import com.bioxx.tfc.Items.Tools.ItemCustomBucketMilk;
import static com.bioxx.tfc.api.Crafting.AnvilManager.getCraftTag;
import static com.bioxx.tfc.api.Crafting.AnvilManager.getDamageBuff;
import static com.bioxx.tfc.api.Crafting.AnvilManager.getDurabilityBuff;
import static com.bioxx.tfc.api.Crafting.AnvilManager.setDamageBuff;
import static com.bioxx.tfc.api.Crafting.AnvilManager.setDurabilityBuff;
import com.bioxx.tfc.api.Food;
import com.bioxx.tfc.api.Interfaces.IFood;
import java.util.ArrayList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
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
    
    public static final String CRAFTINGTAG = "craftingTag";
    public static final String DURABUFF = "durabuff";
    public static final String DAMAGEBUFF = "damagebuff";
    public static final float permissibleSmithingBonus = 0.1f;//24*365ч
    
    public static final String SEALTIME = "SealTime";
    public static final String SEALED = "Sealed";
    public static final String BARRELTYPE = "barrelType";
    
    
    
    
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
    
    
    public static final boolean areItemEquals(ItemStack itemStack1, ItemStack itemStack2, boolean payMode) 
    {
        if (itemStack1 == null || itemStack2 == null) {
            return false;
        }
        // on barrel itemDamage=meta type of barrel can ignore this
        if (itemStack1.getItem() != itemStack2.getItem() || itemStack1.getItemDamage() != itemStack2.getItemDamage()) {
            return false;
        }

        return itemStack1.getItem() instanceof IFood
                ? Food.areEqual(itemStack1, itemStack2)
                //: ItemStack.areItemStackTagsEqual(itemStack1, itemStack2);
                : ExtendedLogic.areItemStackTagsEqualEx(itemStack1, itemStack2 , payMode);
    }
    
    public static final boolean areItemEquals(ItemStack itemStack1, ItemStack itemStack2) {
       return areItemEquals( itemStack1, itemStack2, false);     
    } 
    
    
    /**
     *  For Equal TFC items like burrel with date, tools-armor-weapons with SwimingBonus, (food with temperature not realized)
     *                
     * @param st1  StallFaceSlot pay(payMode) or goods
     * @param st2  warehouse or player inventory   
     * @param payMode then check payStallFaceSlot(st1) and Player Inventory(st2)
     * @return 
     */
    public static boolean areItemStackTagsEqualEx(ItemStack st1, ItemStack st2, boolean payMode)  
    {
        if (st1 == null && st2 == null ) 
        {
            return true;
        } 
        else if ( st1 != null && st2 != null ) 
        {
          /*if ( st1.stackTagCompound == null && st2.stackTagCompound != null && !payMode) 
          {
            return false;  
          }*/
          if ( st1.stackTagCompound == null && st2.stackTagCompound == null) 
          {
            return true;  
          }
          else 
          {                  
              // Don`t allow to combine at stack items, according to the logic suitable for trade
              // then stall search place for price-item in container to put it
              // at searchFreeSpace_NonEmptySlot 
              if ( st1.stackSize == 1 && st2.stackSize == 1)
              {
                 
                  //Compare barrel largeVessel and food
                  Class<?> cls = st1.getItem().getClass();
                  
                  if (cls == ItemBarrels.class || cls == ItemLargeVessel.class )
                  {
                      return areBarrelsEqual(st1, st2);   
                  }                  
                  
                  //compare TFC smithingItem by bonus 
                  if ( ( st1.hasTagCompound() && st1.stackTagCompound.hasKey(CRAFTINGTAG) ) 
                         || (st2.hasTagCompound() && st2.stackTagCompound.hasKey(CRAFTINGTAG) ) )
                  {
                      return areSmithingItemEqual(st1, st2, payMode); 
                  }              
              }
              
              return st1.stackTagCompound.equals(st2.stackTagCompound);              
          }   
        }        
        return false;
    }        
    
    /**
     * Compare barrels and LargeVessel by sealTime new Trade Logic
     * ignore burrelType value
     * st1 StallFaceSlot pay or goods
     * st2 onPayMode - Stack from playerInventory 
     *    else Stack from Warehouse Container
     */ 
    public static boolean areBarrelsEqual(ItemStack st1, ItemStack st2) {                
        if (st1 == null || st2 == null )
            return false;
        
        if (st1.stackTagCompound == null && st2.stackTagCompound == null)
            return true; 
        
        if (st1.stackTagCompound == null || st2.stackTagCompound == null)
            return false;
        
        if ( st1.stackTagCompound.getBoolean(SEALED) &&
                st2.stackTagCompound.getBoolean(SEALED) )
        {
            int sealTime1 = 0;
            int sealTime2 = -1;
            int barrelType1 = 0;
            int barrelType2 = 0;
            
            //from Stall-Face-Slot dont change
            if ( st1.stackTagCompound.hasKey(SEALTIME) )
            {
                sealTime1 = st1.stackTagCompound.getInteger(SEALTIME);
                barrelType1 = st1.stackTagCompound.getInteger(BARRELTYPE);                
            } //no else! for variable trade (buying and selling) pattern
            
            //from player or warehouse container
            if ( st2.stackTagCompound.hasKey(SEALTIME) )
            {
                sealTime2 = st2.stackTagCompound.getInteger(SEALTIME);
                barrelType2 = st2.stackTagCompound.getInteger(BARRELTYPE);                
            } else 
            {
                /*
                If barrel or largeVessel fill from another barrel on player hand
                this barrel dont have sealedTime and barrelType. 
                Therefore set sealTime on moment of trade
                */
                sealTime2 = (int)TFC_Time.getTotalHours();                
                barrelType2 = st2.getItemDamage();
            }
            
            if ( sealTime1 != 0 && sealTime1 == sealTime2 )//&& barrelType1 == barrelType2 )
                return st1.stackTagCompound.equals(st2.stackTagCompound);
            
            /*
              Considered fit for trade the barrel with a difference the sealTime 
              is not more than a permissibleSealTimeHours (year).
              Or if StallFaceSlot have barrel withount SealTime for 
              trade(sealing-buing) anyone barrel sealTime
            */
            if (sealTime1 != 0 && permissibleSealTimeHours < Math.abs( sealTime2 - sealTime1 ) )
                return false;
            
            //remove tags for compare
            if ( st1.stackTagCompound.hasKey(SEALTIME)) {
                st1.stackTagCompound.removeTag(SEALTIME);
                st1.stackTagCompound.removeTag(BARRELTYPE);
            }    
            
            if ( st2.stackTagCompound.hasKey(SEALTIME)) {
                st2.stackTagCompound.removeTag(SEALTIME);
                st2.stackTagCompound.removeTag(BARRELTYPE);
            }
            
            boolean equal = st1.stackTagCompound.equals(st2.stackTagCompound);
           
            //back tags after compare if they were given
            if ( sealTime1 > 0 ) {
                st1.stackTagCompound.setInteger(SEALTIME, sealTime1);
                st1.stackTagCompound.setInteger(BARRELTYPE, barrelType1);
            }
                
            if (sealTime2 > -1 ) {
                st2.stackTagCompound.setInteger(SEALTIME, sealTime2);
                st2.stackTagCompound.setInteger(BARRELTYPE, barrelType2);
            }
            
            return equal;            
        }
        
        return st1.stackTagCompound.equals(st2.stackTagCompound);
    }

    
    public static void removeCraftingTag(NBTTagCompound craftTag, float damageBuff){
        if (craftTag != null)
        {
            craftTag.removeTag(DURABUFF);
            if ( damageBuff > 0)//armor
                craftTag.removeTag(DAMAGEBUFF);
        }        
    }
    
    public static void restoreBackCraftingtag(ItemStack st, NBTTagCompound craftTag,float duraBuff, float damageBuff){
        if (craftTag != null)
        {
            if ( damageBuff > 0)
                setDamageBuff(st, damageBuff);
            
            if ( duraBuff > 0 )
                setDurabilityBuff(st, duraBuff);
        }            
    }
    
    /** 
    * For compare Smithing items by Smithing Bonus                                  rewrite!!!
    * Allow buying goods with less or equal bonus that stay at StallFaceSlot
    * but with a difference of not more than permissibleSmithingBonus
    * And allow selling pay with more or equal bounus than have StallFaceSlot    
    * 
    * Important sequence of arguments 
    *    st1 - StallFaceSlot (pay and goods)
    *    st2 - out Stack from warehouse  - no PayMode 
    *          and Stack from Player Inventory in payMode
    * 
    * @param payMode  st1 - payStallFaceSlot  st2 - from player inventory 
    *        allows to sell SmItem with smithing bonus higher than in the PayStallFaceSlot
    */ 
    public static boolean areSmithingItemEqual(ItemStack st1, ItemStack st2, boolean payMode) {            
        
        if (st1 == null || st2 == null ) 
            return false;       
        
        NBTTagCompound craftTag1 = null;
        NBTTagCompound craftTag2 = null;
        float duraBuff1 = 0;
        float damageBuff1 = 0;
        float duraBuff2 = 0;        
        float damageBuff2 = 0;
        
        //Stall-Face-Slot 
        if (st1.hasTagCompound() && st1.getTagCompound().hasKey(CRAFTINGTAG))
        {
            craftTag1 = getCraftTag(st1);
            duraBuff1 = getDurabilityBuff(st1);
            damageBuff1 = getDamageBuff(st1);                      
        }
        
        //out from player or warehouse container 
        if (st2.hasTagCompound() && st2.getTagCompound().hasKey(CRAFTINGTAG))
        {
            craftTag2 = getCraftTag(st2);
            duraBuff2 = getDurabilityBuff(st2);
            damageBuff2 = getDamageBuff(st2);                      
        } 
                 
        if (st1.hasTagCompound() && duraBuff1 == duraBuff2 && damageBuff1 == damageBuff2) 
        {
               return st1.hasTagCompound() && st1.stackTagCompound.equals(st2.stackTagCompound);
        }
        
                           
        if (payMode) 
        {   /*
                Then Check condition st1 - PayStallFaceSlot st2- Stack From PlayerInventory
                can sell forged item with smithing bonus more that have payStallFaceSlot item
            */
            if ( duraBuff1 > duraBuff2 ) 
                return false;
            
            if (!st1.hasTagCompound() && st2.hasTagCompound())
                return true;
        }
        else
        {   /* 
                Buy forged item with smithing bonus
                can buy that item which have bonus less that have goodStallFaceSlot
                but with a difference of not more than permissibleSmithingBonus
                1- Stall  2 - Warehouse
            */
            boolean suitableForTradeBonus =  ( duraBuff1 - duraBuff2 < permissibleSmithingBonus );
            
            if (craftTag1 != null && craftTag2 == null && suitableForTradeBonus )
                return true;        
            
            if (!suitableForTradeBonus || duraBuff1 < duraBuff2 || damageBuff1 < damageBuff2 )
                return false;
        }
        
        removeCraftingTag(craftTag1,damageBuff1);
        removeCraftingTag(craftTag2,damageBuff2);
           
        boolean equal = st1.hasTagCompound() && st1.stackTagCompound.equals(st2.stackTagCompound);
           
        restoreBackCraftingtag(st1, craftTag1, duraBuff1, damageBuff1);
        restoreBackCraftingtag(st2, craftTag2, duraBuff2, damageBuff2);
        
        return equal;                        
    }    
    
    
    /**
     * For case then item take from warehouse and it locate insade smallVesell on which refer List ItemTileEntitys 
     * @param goodStack - good From StallFaceSlot
     * @param vessel - contained  item-goods from warehouse container slot
     * @return 
     */
    public static ItemStack getItemStackFromSmallVessel(ItemStack goodStack, ItemStack vessel)
    {
        if(goodStack == null || vessel == null)
            return null;
                
        ItemStack[] vesselItemStacks = getVesselItemStacks(vessel);
        if (vesselItemStacks == null )
            return null;
        
        int quantity = 0;
        int needQuantity = ItemHelper.getItemStackQuantity(goodStack);
        
        for(int i = 0; i < vesselItemStacks.length; i++)
        {
            ItemStack vesselItemStack = vesselItemStacks[i];
            if(vesselItemStack != null && ExtendedLogic.areItemEquals(goodStack, vesselItemStack ))                
            {    
                quantity += ItemHelper.getItemStackQuantity(vesselItemStack);
                if (quantity >= needQuantity) 
                    return vesselItemStack ;
            }    
        }        
        return null;
    }
    
    
    /**
     * get first itemStack From Warehouse Container List
     * for sell itemStack to player not from StallFaceSlot, 
     * but from warehouse container ( "real" nbt)
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
                
        if (iStack.getItem() instanceof ItemPotterySmallVessel && !(goodStack.getItem() instanceof ItemPotterySmallVessel) )
        { 
           iStack = getItemStackFromSmallVessel(goodStack,iStack);
           if (iStack == null)
               return null;
        }
        
        
        iStack = iStack.copy();
        
        //set good quantity from StallFaceSlot
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
     * For put itemStack to warehouse container with real nbt tag 
     * from player inventory, not from StallFaceSlot 
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
      
      //set pay quantity from StallFaceSlot
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
