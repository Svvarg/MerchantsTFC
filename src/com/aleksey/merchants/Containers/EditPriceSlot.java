package com.aleksey.merchants.Containers;

import static com.aleksey.merchants.Containers.AnimalInCrate.isValidAnimalCrate;
import static com.aleksey.merchants.Containers.ExtendedLogic.setCookedLevel;
import com.bioxx.tfc.Core.TFC_Time;
import com.bioxx.tfc.Food.ItemFoodMeat;
import com.bioxx.tfc.Food.ItemFoodTFC;
import com.bioxx.tfc.Food.ItemSalad;
import com.bioxx.tfc.Food.ItemSandwich;
import com.bioxx.tfc.Items.ItemBlocks.ItemBarrels;
import com.bioxx.tfc.Items.ItemBlocks.ItemTerraBlock;
import com.bioxx.tfc.Items.ItemTFCArmor;
import com.bioxx.tfc.Items.ItemTerra;
import com.bioxx.tfc.Items.Tools.ItemCustomSword;
import com.bioxx.tfc.Items.Tools.ItemMiscToolHead;
import com.bioxx.tfc.Items.Tools.ItemTerraTool;
import com.bioxx.tfc.Items.Tools.ItemWeapon;
import static com.bioxx.tfc.api.Crafting.AnvilManager.setDamageBuff;
import static com.bioxx.tfc.api.Crafting.AnvilManager.setDurabilityBuff;
import com.bioxx.tfc.api.Enums.EnumFoodGroup;
import com.bioxx.tfc.api.Food;
import com.bioxx.tfc.api.FoodRegistry;
import com.bioxx.tfc.api.Interfaces.IFood;
import com.bioxx.tfc.api.TFCItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

/**
 *
 * @author Swarg
 */

public class EditPriceSlot {
    // items have craftingTag makeupitem by param
    private static final int NOTFC = 0;//without damage
    private static final int TFCTOOLS = 1;//without damage
    private static final int TFCARMOR = 2;//without damage
    private static final int TFCTOOLSADAMAGE = 3;//without damage
    private static final int TFCWEAPON = 4;//without damage

    
    /**
     * check a Valid to TFC before set item to StallPaySlot
     */
    public static boolean isValidToTFCPayItem(int id, int meta)
    {
        //minecraft vanilla items dont uses at TFC:
        boolean isVanillaItem = ( ( ( id >= 256 && id <=408) || ( id>=417 && id <=422) || ( id >=2256  && id <= 2267 ) ) &&
                (  id != 318//flint
                 && id != 324//door
                 && id != 328//minecart
                 && id != 329//saddle
                 && id != 330//irondoor
                 && id != 331//redstone
                 && id != 332//snowball
                 && id != 333//boat
                 && id != 336//brick
                 && id != 339//paper
                 && id != 340//book
                 && id != 345//compass
                 && id != 347//clock
                 && id != 352//bone
                 && id != 355//bed
                 && id != 356//repeater
                 && id != 358//map
                 && id != 367//rotten_flesh
                 //373Pottion зелья ---        
                 && id != 375//spider_eye
                 && id != 380//counldron
                 //383 - spawn egg ---
                 && id != 386//writablebook
                 && id != 395//map
                 && id != 401//fireworks
                 && id != 402//fw star
                 && id != 404//compatator               
                 && id != 406//quartz
                )); 
            //minecrat blocks dont use at TFC
           boolean isVanillaBlock=  ( (id >= 0 && id <= 164 || id >=170 &&id <=175) && ( 
                   id != 20//glass
                && id != 27//golden Rail
                && id != 28//detector rail
                && id != 29//sticky_piston
                && id != 33//piston
                && id != 35//wool
                && id != 45//brick_block 
                && id != 49//obsidian
                && id != 65//ladder
                && id != 66//rail
                && id != 69//level
                && id != 76//redstone torch
                && id != 80//snow
                && id != 101//ironbars
                && id != 102//glass_pane
                && id != 108//brick_stairs
                && id != 131//tripwire_hook
                && id != 133//emerald_block
                //&& id != 151//daylightdetector??
                && id != 152//redstoneblock
                && id != 155//quartz_one
                && id != 157//activator_rail
                && id != 159//stained hardened clay
                && id != 160//stained glass pane
                && id != 171//carpet
                && id != 173//Coal_block
                )
                );
           if (isVanillaItem || isVanillaBlock)
            return false;
        
        Item item = Item.getItemById(id);
        
        if (item==null)
            return false;
        
        item = null;
        return true;        
    }
       
    
    public static int getValidMetaForItem(Item item, int meta)
    {
        if (item == null & meta < 0 || item instanceof ItemSandwich)
        //dont work with sandwich
            return 0;
        
        int maxMeta = 0;        
        
               
        if (item instanceof ItemTerra  ) //int maxMeta = 15;//Exception UdaryMod! RichLimonite
        {
            if (item instanceof ItemFoodTFC && !(item instanceof ItemSalad) && !(item instanceof ItemSandwich) ) {
                return 0;
            }
             try 
             {
                 String[] metaNames = ((ItemTerra) item).metaNames;
                 if ( metaNames != null ) 
                     maxMeta = metaNames.length-1;                     
             }
             catch (Exception e)
             {
                 maxMeta = 0;
             }        
            //Exception
            //cookedLimonit Udary has some fun metaData for items like 10 15 25 35 for limonite ore
            if (maxMeta == 3 && item.getClass().toString().contains("udary.tfcudarymod.items.ores.ItemOre"))
            {
                if ( meta != 10 && meta != 15 && meta != 25 && meta != 35)
                    return 25;
                else 
                    return meta;                                
            }
            else if (maxMeta ==1 && item.getClass().toString().contains("udary.tfcudarymod.items.ores.ItemOreFlake"))
            {
                if ( meta != 1 && meta != 10)
                    return 1;
                else 
                    return meta;                                                    
            }
           
        } 
        else if (item instanceof ItemTerraBlock)
        {
             try 
             {
                 String[] metaNames = ((ItemTerraBlock) item).metaNames;
                 if ( metaNames != null ) 
                     maxMeta = metaNames.length-1;                     
             }
             catch (Exception e)
             {
                 maxMeta = 0;
             }                    
        }        
        else if ( item.isDamageable() ) 
        {
            int maxDamage = item.getMaxDamage();
            return meta >= maxDamage ? maxDamage-1 : meta;
        }//.... antiqua atlas... meta is number of atlas
        else if (!item.getHasSubtypes())        
            return 0;        
            
        meta = meta > maxMeta ? meta = 0 : meta;
        return meta;
    }
    
    
    /**
     * create itemStack for price slot by GuiStallSetPayItem params
     */
    public static ItemStack createItemStackByParams (NBTTagCompound nbt)
    {
        //NBTTagCompound nbt
        if (! nbt.hasKey("CreatePayItem") )
            return null;
        
        int id = nbt.getInteger("id");
        int meta = nbt.getInteger("meta");
        int count = nbt.getInteger("count");

        if (id<=0 || meta<0 || meta> 5000 || count <= 0 || count >=1000)
            return null;

        int p1 = nbt.getInteger("p1");
        int p2 = nbt.getInteger("p2");
        int p3 = nbt.getInteger("p3");
        int p4 = nbt.getInteger("p4");
        
        Item item = Item.getItemById(id);            
        
        meta = getValidMetaForItem(item, meta);
        
        ItemStack payStack = new ItemStack(item,1 ,meta );
        if (payStack.getItem() instanceof IFood)
        {
            //payStack.stackSize = 1;//TFCfood amount at nbttag foodWeight
            
            if ( payStack.getItem() instanceof ItemSalad)
            {                
                payStack = createSaladNBTByParam(payStack, count, p1, p2, p3, p4);
            } 
            else
                //dont work with ItemSandwich return null
                payStack = createSimpleTFCFoodNBTByParam(payStack, count, p1, p2, p3, p4);//can be null!
        } 
        else if (payStack.getItem() instanceof ItemBarrels)
        {
            payStack.stackSize = 1;
            createBarrelNBTByParam(payStack,p1 ,p2 ,p3 ,p4);
        }
        else if (isValidAnimalCrate(payStack) )
        {
            AnimalInCrate a = new AnimalInCrate(p1, p2, p3, p4);
            payStack.stackTagCompound = a.writeToNBT();
        }
        else
        {
            int maxCount = payStack.getMaxStackSize();
            count =  count >= maxCount ? maxCount : count;
            payStack.stackSize = count;
            
            if ( !isNotForgedTFCItems(payStack) && p1 > 0 )//p1 - smithing bonus
                createSmithingNBTByParam(payStack, p1);
        }    
        //animalcrate
        return payStack;        
    }
    
    public static boolean createSmithingNBTByParam(ItemStack iStack, int p1)
    {
        if ( iStack==null || p1 <= 0 )
            return false;
        
        int itype = getTFCSmithingItemType(iStack);// 0 NOtfc 1 tool without AD 2 Armor 3 toolwithAD 4 Weapon
        
        if ( itype > NOTFC )
        {
            float buff = ( p1 >= 100 ) ? 1 : (float)p1 / 100;
            setDurabilityBuff(iStack, buff);        
            
            if (itype == TFCTOOLSADAMAGE || itype == TFCWEAPON)
            {
                setDamageBuff(iStack, buff);
            }       
            return true;
        }
        return false;
    }
  
    public static boolean isNotForgedTFCItems(ItemStack iStack)
    {
        if (iStack == null) 
            return false;
        
        Item i = iStack.getItem();
        
        boolean r =
                (   i == TFCItems.flintSteel 
                || i == TFCItems.fireStarter
                || i == TFCItems.bow
                || i == TFCItems.fishingRod
                || i == TFCItems.spindle
                || i == TFCItems.stoneHammer
                || i == TFCItems.stoneHammerHead
                || i == TFCItems.stoneKnife
                || i == TFCItems.stoneKnifeHead                
                );
        if (!r)
        {
            if (i instanceof ItemMiscToolHead)
            {  //stone tools
               Item.ToolMaterial material = ((ItemMiscToolHead) (i)).getMaterial();
               if (material != null && (material == TFCItems.igInToolMaterial 
                       || material == TFCItems.sedToolMaterial
                       || material == TFCItems.igExToolMaterial                       
                       || material == TFCItems.mMToolMaterial)  )
                   return true;
            }
        }
        return r;
    }
        
    //tools armor tools with AttackDamage weapon
    public static int getTFCSmithingItemType(ItemStack iStack)
    {
        String[] noAttackDamage ={"Saw", "Hoe", "Chisel", "Propick"};
        if (iStack == null)
            return NOTFC;
        
        Item item = iStack.getItem();        
        String itemName = item.getUnlocalizedName();
        
        if (itemName.isEmpty())
            return NOTFC;
        
        if (item instanceof ItemWeapon ||
                item instanceof ItemCustomSword)
        {          
            return TFCWEAPON;//Weapon
        }
        else if (item instanceof ItemTerraTool || item instanceof ItemMiscToolHead )
        {         
            for (String s :noAttackDamage){
                if (itemName.contains(s))
                    return TFCTOOLS;//tools withount attackdamage
            }            
            return TFCTOOLSADAMAGE;//Tools with attackdamage
        } 
        
        if ( item instanceof ItemTFCArmor ||  
                //Shield from AutomatedBellowsAddon have duraBuff
                item.getClass().toString().endsWith("Shield"))
            return TFCARMOR;
        
        return (item != null) && item.getClass().toString().endsWith(" Blade")? TFCWEAPON : NOTFC;//Weapon  saw blade...      
    }

    
    public static ItemStack createSimpleTFCFoodNBTByParam(ItemStack food, int count, int p1, int p2,int p3, int p4)
    {
      if (food == null || food.getItem() instanceof ItemSandwich )   
          return null;
            
      float weight = ( count < 10 || count >= 160 )? 160 : 10 * (int)(count / 10);
      if (ExtendedLogic.isNoSplitFood(food)){
          weight = ExtendedLogic.getNoSplitFoodWeight(food);
      }
      ItemFoodTFC.createTag( food, weight);      
      p1 = (p1 < 0 && p1 > 5) ? 0 : p1;//Cooked +1 on ItemFoodTFC standart
      
      if ( setCookedLevel(food, p1) && Food.isCooked( food))
      {
          int[] cookedTasteProfile = new int[] { 0, 0, 0, 0, 0 };
          Food.setCookedProfile( food, cookedTasteProfile);
          Food.setFuelProfile(food, cookedTasteProfile);                              
      }
      
      if (p2 > 0 && isItemValidToSalted(food) ) 
        Food.setSalted( food, true);
      
      if (p3 > 0)
      {
        Food.setDried( food, Food.DRYHOURS);
        //diried protein it is the smoked have fuelProfile
      }
      
      if (p4 > 0)
          setTFCFoodParams(food,p4);//brined pickled smokeCounter        
      
      return food;
    }    
    
    public static boolean isItemValidToSalted(ItemStack foodStack)
    {
        if (foodStack == null)
            return false;
        Item f = foodStack.getItem();
        return (f == TFCItems.venisonRaw 
                || f == TFCItems.beefRaw
                || f == TFCItems.chickenRaw
                || f == TFCItems.porkchopRaw
                || f == TFCItems.fishRaw
                || f == TFCItems.calamariRaw
                || f == TFCItems.muttonRaw
                || f == TFCItems.horseMeatRaw               
                );//instanceof ItemFoodMeat...
    }

    // brined 1x pickled 10x smoked 100x
    public static int getTFCFoodParams(ItemStack food)
    {
        if (food==null || ! food.hasTagCompound() )  
            return 0;
        //int salted =  Food.isSalted(food) ? 1 : 0;     // p2
        //int dired = Food.isDried(food) ? 1 : 0;//4     // p3
        int brined = Food.isBrined(food)? 1 : 0;
        int pickled = Food.isPickled(food) ? 1 : 0;
        int smoked = Food.getSmokeCounter(food)>=12 ? 1 : 0;//not FuelProfile! for protein pirepit + wool      
        //return salted + dired * 10 + brined *100 + pickled * 1000 + smoked*10000;
        return brined + pickled * 10 + smoked * 100;     //p4
    }
    
    public static boolean setTFCFoodParams(ItemStack food,int param4)
    {
        if (param4<=0)
            return false;
        int brined = param4 % 10;
        int pickled = (int) Math.floor(  param4 % 100 / 10);
        int smoked =  (int) Math.floor(  param4 % 1000 / 100);
        
        if (brined > 0)
        {
            Food.setBrined(food, true);
            if (pickled > 0)// can be pickled only brined
                Food.setPickled(food, true);
        }
        
        if ( smoked > 0 && isItemValidToSmoked(food) )
        {
            Food.setSmokeCounter(food, Food.SMOKEHOURS);//default is 12 then smoked is done                    
            //not real fuelProfile only for check isSmoked and for showing "Smoked" at display name
            int[] fuelProfile = new int[] { 1, 1, 1, 1, 1 };
            Food.setFuelProfile(food, fuelProfile);
        }
        return true;        
    }
    
    /**
     * From  com.bioxx.tfc.Blocks.BlockSmokeRack
     */
    public static boolean isItemValidToSmoked(ItemStack is)
    {
        if(is == null)
            return false;
        if(is.getItem() instanceof ItemFoodMeat)
        {
            if(!Food.isCooked(is) && Food.isBrined(is))
                return true;
        }
        else if(is.getItem() == TFCItems.cheese)
        {
            if(!Food.isCooked(is))
                return true;
        }
        return false;
    }

        
    public static ItemStack createSaladNBTByParam(ItemStack food, int count, int p1, int p2,int p3, int p4)
    {
      if (food==null || ! (food.getItem() instanceof ItemSalad) )
          return null;
      
      int weight = 20;//can change depends on salad component count          
      //simple protection of input errors 
      if (p1 == p2 || p2 == p3 || p3 == p4 
              || p1 == p3 || p1 == p4 || p2 == p4
              || p1 < 0 || p2<0 || p3<0 || p4<0 )
          return null;
      
      if ( EnumFoodGroup.None == FoodRegistry.getInstance().getFoodGroup(p1) 
              || EnumFoodGroup.None == FoodRegistry.getInstance().getFoodGroup(p2) 
              || EnumFoodGroup.None == FoodRegistry.getInstance().getFoodGroup(p3) 
              || EnumFoodGroup.None == FoodRegistry.getInstance().getFoodGroup(p4) 
              )
          return null;
      
      int[] foodGroups = new int[] { p1, p2, p3, p4}; 
      ItemFoodTFC.createTag( food, weight);
      Food.setFoodGroups(food, foodGroups);          
      return food;             
    }
            
    
    /**
     * 
     * @param payStack
     * @param p1 Sealed
     * @param p2 SealTime
     * @param p3 FluidID
     * @param p4 FluidAmount
     * @return 
     */
    public static ItemStack createBarrelNBTByParam(ItemStack payStack, int p1, int p2, int p3, int p4)
    {
        if (payStack == null || p1 != 1 || p3<0)
            return payStack;
        boolean sealed = ( p1 == 1);
        int sealTime =  getHoursForYear(p2);
        if (sealed) {
            if (payStack.stackTagCompound==null)
                payStack.stackTagCompound = new NBTTagCompound(); 
            payStack.stackTagCompound.setBoolean("Sealed", sealed);
            payStack.stackTagCompound.setInteger("SealTime", sealTime);
            //payStack.stackTagCompound.setInteger("barrelType", barrelType); 
            payStack = setFluidID(payStack, p3, p4);
        }        
        return payStack;
    }
        
    public static int getYearFromHours(int tHours)
    {
        int tDays = tHours / TFC_Time.HOURS_IN_DAY;        
        int tMonths = tDays / TFC_Time.daysInMonth;
        int year = tMonths / 12;
        return 1000 + year;
    }
    
    public static int getHoursForYear(int year)
    {
        year = year - 1000;
        if (year<=0)
            return 0;
        int tMonths = year * 12;
        int tDays = tMonths * TFC_Time.daysInMonth;
        int tHours = tDays * TFC_Time.HOURS_IN_DAY;
        return tHours;
    }
        
    //fluid.getFluidID & fluid.amount 
    public static FluidStack getFluidID(ItemStack barrel)
    {
        if (barrel==null || !barrel.hasTagCompound())
            return null;//-1?
        //NBTTagCompound fluidNBT = barrel.stackTagCompound.getTag("fluidNBT");
        FluidStack fluid = FluidStack.loadFluidStackFromNBT(
                barrel.stackTagCompound.getCompoundTag("fluidNBT") );
        if (fluid == null)
            return null;
        //fluid.amount;
        return fluid;//.getFluidID();
        
    }
    
    public static ItemStack setFluidID(ItemStack barrel, int fluidID, int amount )
    {
        if (barrel==null || fluidID < 4 || amount<1 )
            return null;
        
       amount = (amount >= 10)? 10000: amount*1000;//ItemBarrels.MAX_LIQUID
       
       Fluid fluid = FluidRegistry.getFluid(fluidID);
       if (fluid == null)
           return null;
       
       FluidStack fluidStack = new FluidStack(fluid, amount);       
       NBTTagCompound fluidNBT = new NBTTagCompound();
       if( fluidStack != null )
           fluidStack.writeToNBT(fluidNBT);       
       if (barrel.stackTagCompound==null)
       {           
           barrel.stackTagCompound = new NBTTagCompound();
       }
       barrel.stackTagCompound.setTag("fluidNBT", fluidNBT);
       return barrel;
    }
    
    
}
