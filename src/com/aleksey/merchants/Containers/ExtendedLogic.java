package com.aleksey.merchants.Containers;

import static com.aleksey.merchants.Containers.ExtendedLogic.setCookedLevel;
import com.aleksey.merchants.Helpers.ItemHelper;
import static com.aleksey.merchants.Helpers.SmallVesselHelper.getVesselItemStacks;
import com.aleksey.merchants.api.ItemSlot;
import com.aleksey.merchants.api.ItemTileEntity;
import com.bioxx.tfc.Core.TFC_Time;
import com.bioxx.tfc.Food.ItemFoodTFC;
import com.bioxx.tfc.Food.ItemSalad;
import com.bioxx.tfc.Food.ItemSandwich;
import com.bioxx.tfc.Items.ItemBlocks.ItemBarrels;
import com.bioxx.tfc.Items.ItemBlocks.ItemLargeVessel;
import com.bioxx.tfc.Items.ItemBlocks.ItemTerraBlock;
import com.bioxx.tfc.Items.ItemGem;
import com.bioxx.tfc.Items.ItemOre;
import com.bioxx.tfc.Items.ItemOreSmall;
import com.bioxx.tfc.Items.ItemRawHide;
import com.bioxx.tfc.Items.ItemTFCArmor;
import com.bioxx.tfc.Items.ItemTerra;
import com.bioxx.tfc.Items.Pottery.ItemPotterySmallVessel;
import com.bioxx.tfc.Items.Tools.ItemCustomBucketMilk;
import com.bioxx.tfc.Items.Tools.ItemCustomSword;
import com.bioxx.tfc.Items.Tools.ItemMiscToolHead;
import com.bioxx.tfc.Items.Tools.ItemTerraTool;
import com.bioxx.tfc.Items.Tools.ItemWeapon;
import static com.bioxx.tfc.api.Crafting.AnvilManager.getCraftTag;
import static com.bioxx.tfc.api.Crafting.AnvilManager.getDamageBuff;
import static com.bioxx.tfc.api.Crafting.AnvilManager.getDurabilityBuff;
import static com.bioxx.tfc.api.Crafting.AnvilManager.setDamageBuff;
import static com.bioxx.tfc.api.Crafting.AnvilManager.setDurabilityBuff;
import com.bioxx.tfc.api.Food;
import static com.bioxx.tfc.api.Food.getInfusion;
import static com.bioxx.tfc.api.Food.isBrined;
import static com.bioxx.tfc.api.Food.isCooked;
import static com.bioxx.tfc.api.Food.isDried;
import static com.bioxx.tfc.api.Food.isInfused;
import static com.bioxx.tfc.api.Food.isPickled;
import static com.bioxx.tfc.api.Food.isSalted;
import static com.bioxx.tfc.api.Food.isSameSmoked;
import static com.bioxx.tfc.api.Food.isSmoked;
import com.bioxx.tfc.api.Interfaces.IFood;
import com.bioxx.tfc.api.TFCItems;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

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
    public static final int PERMISSIBLEDECLAY = 2;//milk salad!
   // public static final int permissibleSealTimeHours = 8760;//24*365ч new way
    public static final boolean ToNonEmptySlot = true;
    public static final boolean ToEmptySlot = false;
    
    public static final String CRAFTINGTAG = "craftingTag";
    public static final String DURABUFF = "durabuff";
    public static final String DAMAGEBUFF = "damagebuff";
    //public static final float permissibleSmithingBonus = 0.1f;
    
    public static final String SEALTIME = "SealTime";
    public static final String SEALED = "Sealed";
    public static final String BARRELTYPE = "barrelType";
    public static final boolean IGNOREBARRELWOODTYPE = true;//allow trade any one barrel wood type
    
    
    
    
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
    
    public static boolean areItemEquals(ItemStack itemStack1, ItemStack itemStack2) {
       return areItemEquals( itemStack1, itemStack2, false);     
    } 
    
    public static final boolean areItemEquals(ItemStack itemStack1, ItemStack itemStack2, boolean payMode) 
    {
        if (itemStack1 == null || itemStack2 == null) {
            return false;
        }
        
        // on barrel itemDamage=meta type of wood barrel Can ignore this for payMode
        if  ( IGNOREBARRELWOODTYPE && 
                itemStack1.getItem() instanceof ItemBarrels 
                && itemStack2.getItem() instanceof ItemBarrels 
                && itemStack1.getItem() == itemStack2.getItem() )
        {
            return areItemStackTagsEqualEx(itemStack1, itemStack2, payMode);
        }
        
        if (itemStack1.getItem() != itemStack2.getItem() || itemStack1.getItemDamage() != itemStack2.getItemDamage()) {
            return false;
        }
        /*
        return itemStack1.getItem() instanceof IFood
                ? Food.areEqual(itemStack1, itemStack2)
                //: ItemStack.areItemStackTagsEqual(itemStack1, itemStack2);
                : ExtendedLogic.areItemStackTagsEqualEx(itemStack1, itemStack2 , payMode*/
        boolean equals = false;
 
        if (itemStack1.getItem() instanceof IFood)
        {
            //equals = Food.areEqual(itemStack1, itemStack2);
            equals = areFoodEqual(itemStack1, itemStack2);
        } 
        else if (payMode && 
                //how better check item class if 
                itemStack1.getItem().getClass().toString().contains("taeog.animalcrate.item.ItemCrate") 
                )
        {   //only for payMode for allow sell animals with different characteristics
            equals = areAnimalsAtCrateEqual(itemStack1, itemStack2, payMode);
        }
        else
        {                    //: ItemStack.areItemStackTagsEqual(itemStack1, itemStack2);
            equals =  ExtendedLogic.areItemStackTagsEqualEx(itemStack1, itemStack2 , payMode);
        }    
        return equals;
        
    }
    
    //+1 to ItemFoodTFC standart  here 0 is not cooked
    public static int getCookedLevel(ItemStack food)
    {
        if (food == null || !Food.isCooked(food) )
            return 0;
        int cooked = (int) Food.getCooked(food);
        if (cooked  < 600)
            return 0;//
        
        int cl = (int) Math.floor( ( cooked - 600 )/120 ) + 1;
        cl = (cl < 1 || cl > 5 ) ? 0 : cl;
        return cl; //1-5 (0-4 in ItemFoodTFC) +1 (0 - notCooked)
    }
    
    /**
     *  For create simple TFC Cooked Food
     * @param food ItemStack
     * @param cookedLevel +1 to ItemFoodTFC standart (0 - not cooked)
     * @return 
     */    
    public static boolean setCookedLevel(ItemStack food, int cookedLevel)
    {
        if (food == null || cookedLevel < 1 || cookedLevel > 5 )
            return false;        
        
        float cooked = 600 + 120 * ( cookedLevel - 1 );
        cooked = (cooked == 600)? 601 : cooked;// (isCooked float > 600)
        Food.setCooked(food, cooked);
        return true;        
    }
    
    /** 
     * com.bioxx.tfc.api.Food without personal test factor like CookedProfile & FuelProfile
     * consider CookedLevel, ignore CookedProFile & FuelProFile
     * Smart compare GroupedFood such as salad and sandwich
     */ 
    public static boolean areFoodEqual(ItemStack is1, ItemStack is2)
    {
        
        if (is1== null || is2 == null)
            return false;
        
        if (is1.getItem() instanceof ItemSandwich 
                || is1.getItem() instanceof ItemSalad  ) 
        {
            return areGroupedFoodEquals(is1,is2);
        }
        
        
        boolean brined = isBrined(is1) == isBrined(is2);//рассол
        boolean pickled = isPickled(is1) == isPickled(is2);//маринованный brined+vineger
        
        boolean isC1 = isCooked(is1);
        boolean isC2 = isCooked(is2);
        boolean cooked = ( !isC1 && !isC2 ) || 
                //access to trade food with equal Cooked temp area value 
                ( isC1 && isC2 && (  getCookedLevel(is1) == getCookedLevel(is2)  ) );
        
        boolean dried = isDried(is1) == isDried(is2);
        boolean salted = isSalted(is1) == isSalted(is2);
        boolean infused = (isInfused(is1) && isInfused(is2) && getInfusion(is1).equals(getInfusion(is2)) || !isInfused(is1) && !isInfused(is2));        
        
        //ignore
        //boolean isSmoked = (isSmoked(is1) && isSmoked(is2) && isSameSmoked(is1, is2) || !isSmoked(is1) && !isSmoked(is2);// //FuelProfile 
        
        return brined && pickled && cooked && dried && salted && infused;//&& isSmoked;
    }
    
    public static boolean areGroupedFoodEquals(ItemStack is1, ItemStack is2)
    {
        if (is1==null|| is2==null)
            return false;
        
        int[] fg1 = Food.getFoodGroups(is1);
        int[] fg2 = Food.getFoodGroups(is2);
        
        
        if ( fg1==null || fg2 == null || fg1.length != fg2.length)
            return false;
                
        int h = fg1.length;
        int c = 0;
        //check FoodIDs with any sequence
        for (int i = 0; i < fg1.length; i++)
	{
            for (int j = 0; j< fg2.length; j++)
            {
                if (fg1[i] == fg2[j])
                {
                    c++;
                    break;// 
                }
            }    
	}
        return (c == fg1.length);
    }
    
    
    

    
    
    /**
     *  For Equal TFC items like burrel with date, tools-armor-weapons with SwimingBonus, (food with temperature not realized)
     *                
     * @param st1  StallFaceSlot pay(payMode) or goods    (Always!)
     * @param st2  warehouse or player inventory   
     * @param payMode then check payStallFaceSlot(st1) and Player Inventory(st2) (preparePay)
     * @return 
     * Important sequence of arguments 
     *    st1 - StallFaceSlot (pay and goods)
     *    st2 - out Stack from warehouse  - no PayMode 
     *          and Stack from Player Inventory in payMode
     * 
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
              if (true)//( st1.stackSize == 1 && st2.stackSize == 1)
              {
                 
                  //Compare barrel largeVessel and food
                  //Class<?> cls = st1.getItem().getClass();                  
                  //if (cls == ItemBarrels.class || cls == ItemLargeVessel.class )
                  
                  Item item1 = st1.getItem();
                  
                  if (  item1 instanceof ItemBarrels || item1 instanceof  ItemLargeVessel )
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
              
              return st1.stackTagCompound != null && st2.stackTagCompound != null && st1.stackTagCompound.equals(st2.stackTagCompound);
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
            int sealTime2 = 0;
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
            } 
            
            //Burrel from playerinventory  as pay  or from warehouse container as goods
            //if dont have time set now game time moment of trade
            if (sealTime2 <= 0)
            {
                /*
                If barrel or largeVessel fill from another barrel on player hand
                this barrel dont have sealedTime and barrelType. 
                Therefore set sealTime on this moment (trade)
                */
                sealTime2 = (int)TFC_Time.getTotalHours();                
                barrelType2 = st2.getItemDamage();
            }
            
            //case then sealtime = 0 and have nbt tag with sealTime=0
            // but secand burrel dont have this tag 
            // or if equal SealTime compare withount remove NBTtags
            if ( sealTime1 != 0 && sealTime1 == sealTime2 ) // ignore barrelType
                return st1.stackTagCompound.equals(st2.stackTagCompound);
            
            /*
              Considered fit for trade the barrel with a difference the sealTime 
              is not more than a permissibleSealTimeHours (year).
              Or if StallFaceSlot have barrel withount SealTime for 
              trade(sealing-buing) anyone barrel sealTime
            */
            int sealYear1 =  EditPriceSlot.getYearFromHours(sealTime1);
            int sealYear2 =  EditPriceSlot.getYearFromHours(sealTime2);
            
            //if (sealTime1 != 0 && permissibleSealTimeHours < Math.abs( sealTime2 - sealTime1 ) )
            if (sealTime1 != 0 && sealYear1 != sealYear2 )
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
                
            if (sealTime2 > 0 ) {
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
    * For compare Smithing items by Smithing Bonus                                  
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
                can sell forged item with smithing bonus more that have item on payStallFaceSlot
            */
            if ( duraBuff1 > duraBuff2 ) 
                return false;
            
            if (!st1.hasTagCompound() && st2.hasTagCompound())
                return true;
        }
        else
        {   /* Buy forged item with smithing bonus
            
                allow trade forged item with diffent bonus.
                Compare goodStallFaceSlot and item from warehouseContainer 
                by smithing area bonus                
                1- Stall  2 - Warehouse
                        
             smithing bonus areas 0-<10% 1:10%-<20% 2: 20%-<30% .. 9:90-100%
             smelted Tools withount bonus is a 0 area (0-9.99% bonus)
             Correct showing the quantity of goods at Stall by area bonus
            */         
            int smArea1 = getSmithingNumArea(duraBuff1);
            int smArea2 = getSmithingNumArea(duraBuff2);
            
            if (smArea1 != smArea2)
                return false;            
        }
        
        removeCraftingTag(craftTag1,damageBuff1);
        removeCraftingTag(craftTag2,damageBuff2);
           
        boolean equal = st1.hasTagCompound() && st1.stackTagCompound.equals(st2.stackTagCompound);
        
        //duraBuff2 = (float) Math.random();for debug and check
        
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
    
    /**
     * More items to StorageRack
     * @param item
     * @return 
     */
    public static boolean isValidItemForStorageRack(Item item) 
    {
        
        if (item==null)
            return false;
        
        String iClass = item.getClass().toString();
        
        if (iClass == null || iClass.isEmpty() ) {
                return false;
        }
        return (
                ( iClass.startsWith("sladki.tfc.ab") && 
                    ( 
                       iClass.contains("ItemBlockPotteryKiln") 
                    || iClass.contains("ItemBlockPotteryKilnChamber")
                    || iClass.contains("ItemBlockSteamBoiler")
                    || iClass.contains("ItemBlockAutomatedBellows")
                    || iClass.contains("ItemBlockWaterFilter")              
                    )
                ) 
                || iClass.contains("taeog.animalcrate.item.ItemCrate")
                || iClass.contains("udary.tfcudarymod.items.devices.ItemOreCooker")
                || iClass.contains("tfctech.items.ItemBlocks.ItemWireDrawBench")
                );
    }
    
    
    //for compare tools weapon armor and for correct quantiti on warehouse
    public static int getSmithingNumArea(float buff)
    {
        if (buff < 0.1) 
            return 0;
        if (buff >= 0.9)
            return 9;
        return (int) Math.floor( buff * 10 );
    }
    
    /**
     * For display correct quantity smithing item with different bonus 
     * id:meta:0     0: 0-<10% 1:10-<20% 2:20-<30% .. 9:90-100%
     * @return 
     */
    public static String getKeyForSmithingItem(ItemStack itemStack,String key)
    {
        if (itemStack == null || key == null || key.isEmpty() ) 
            return "";
        //Item item = itemStack.getItem();
        //String.valueOf(Item.getIdFromItem(item)) + ":" + String.valueOf(itemStack.getItemDamage());
        if ( !( itemStack.hasTagCompound() && itemStack.stackTagCompound.hasKey(CRAFTINGTAG) ))
            return key;       
        float duraBuff = getDurabilityBuff(itemStack);   
        if (duraBuff <=0 )
            return key;
        int sna = getSmithingNumArea(duraBuff);
        
        return key+":"+sna;
    }
    
    public static int strToInt(String s, int error)
    {
        
        try 
        {
            return (s == null || s.length() == 0 ) ? error: Integer.parseInt(s);
        } 
        catch(NumberFormatException e) 
        {
            return error;            
        }
    }
    
    public static int strToInt(String s){
        return strToInt(s,0);
    }
    
    public static boolean isChildClass(Class child, Class parent)
    {
        Class[] cList = child.getClasses();
        if (cList==null)
            return false;
        
        for (Class c: cList)
        {
           if (c==parent) 
               return true;
        }
        return false;
    }
    
    
    /**
     * AnimalCrate mod compare by AnimalID, Age,  sex, familiarity
     *  for horse MateSpeed MateJump 
     * @param st1
     * @param st2
     * @return 
     */
    public static boolean areAnimalsAtCrateEqual(ItemStack st1, ItemStack st2, boolean payMode)
    {
        if (st1==null || st2==null)
            return false;
        
        if (st1.stackTagCompound==null && st2.stackTagCompound==null)
            return true;//empty crate
        
        if (st1.stackTagCompound==null || st2.stackTagCompound==null)
            return false;
        
        boolean equal = false;
        
        if (payMode)
        {
            AnimalInCrate a1 = new AnimalInCrate(st1.stackTagCompound);
            AnimalInCrate a2 = new AnimalInCrate(st2.stackTagCompound);
            equal = a1.isAnimalEqual(a2);
        }
        else
            equal = st1.stackTagCompound.equals(st2.stackTagCompound); 
                      
        return equal;
    }
    
    /**
     * uses at ItemHelper getItemKey for correct display quantity of barrels 
     * and large vessels by sealYear fluidId and fluidAmount
     */
    public static String getKeyForBarrel(String key, ItemStack itemStack)
    {
        if (itemStack==null || !itemStack.hasTagCompound())
                return key;            
      
        FluidStack fluidStack = EditPriceSlot.getFluid(itemStack);
        if (fluidStack != null)
        {
            int fluidID = fluidStack.getFluidID();
            int amount = 0;
            if (fluidStack.amount > 0 )
                amount = (int) Math.floor(fluidStack.amount / 1000);
            key += ":"+fluidID+":"+amount;                
        } 
        //import position at the end of key-name
        int sealTime =  itemStack.stackTagCompound.getInteger(ExtendedLogic.SEALTIME);            
        int sealYear = (sealTime <= 0)? 0 : EditPriceSlot.getYearFromHours(sealTime,true);
        key += ":"+sealYear;//date+flag has NBT
            
        return key;
    }
    
    public static int getBarrelQuantity(Hashtable<String, Integer> quantities, ItemStack iStack, String itemKey)
    {
        if (quantities==null || quantities.isEmpty() || iStack == null || 
                itemKey==null || itemKey.isEmpty() )
            return 0;        
                
        if (iStack.stackTagCompound == null || 
                iStack.stackTagCompound.getInteger(ExtendedLogic.SEALTIME)>0 )
        {
            return quantities.containsKey(itemKey) ? quantities.get(itemKey): 0;
        }
        
        // case then need calculate all barrels\largeVessels with duifferent sealYear        
        int q = 0;
        String zeroItemKey = "";        
        int p = itemKey.lastIndexOf(":0");//SealTime is 0
        if ( p>0 && p < itemKey.length() )
        {
            zeroItemKey = itemKey.substring(0, p);
        }
        else 
            return 0;
            
        for (Map.Entry <String, Integer> entry : quantities.entrySet()) {
            String key = entry.getKey();
            if (key != null && !key.isEmpty() && key.startsWith(zeroItemKey) )
            {
                int v = (Integer) entry.getValue();
                q += v;
            }
        }            
        
        return q;
    }
    
}
                
