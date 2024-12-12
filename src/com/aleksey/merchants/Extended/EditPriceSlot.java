package com.aleksey.merchants.Extended;

import com.bioxx.tfc.Core.Metal.MetalRegistry;
import com.bioxx.tfc.Core.TFC_Time;
import com.bioxx.tfc.Food.ItemFoodMeat;
import com.bioxx.tfc.Food.ItemFoodTFC;
import com.bioxx.tfc.Food.ItemSalad;
import com.bioxx.tfc.Food.ItemSandwich;
import com.bioxx.tfc.Items.ItemBlocks.ItemBarrels;
import com.bioxx.tfc.Items.ItemBlocks.ItemTerraBlock;
import com.bioxx.tfc.Items.ItemTFCArmor;
import com.bioxx.tfc.Items.ItemTerra;
import com.bioxx.tfc.Items.Pottery.ItemPotterySmallVessel;
import com.bioxx.tfc.Items.Tools.ItemCustomSword;
import com.bioxx.tfc.Items.Tools.ItemMiscToolHead;
import com.bioxx.tfc.Items.Tools.ItemWeapon;
import com.bioxx.tfc.api.Armor;
import com.bioxx.tfc.api.Constant.Global;
import com.bioxx.tfc.api.Enums.EnumFoodGroup;
import com.bioxx.tfc.api.Food;
import com.bioxx.tfc.api.FoodRegistry;
import com.bioxx.tfc.api.Interfaces.IFood;
import com.bioxx.tfc.api.Metal;
import com.bioxx.tfc.api.TFCItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import static com.bioxx.tfc.api.Crafting.AnvilManager.getDurabilityBuff;
import static com.bioxx.tfc.api.Crafting.AnvilManager.setDamageBuff;
import static com.bioxx.tfc.api.Crafting.AnvilManager.setDurabilityBuff;
import static com.aleksey.merchants.Extended.AnimalInCrate.isValidAnimalCrate;
import static com.aleksey.merchants.Extended.ExtendedLogic.setCookedLevel;
import com.bioxx.tfc.Items.ItemBlocks.ItemCrucible;
import net.minecraft.nbt.NBTTagList;

/**
 *
 * @author Swarg
 */

public class EditPriceSlot {
    // items have craftingTag makeupitem by param
    public static final int NOTFC = 0;//without damage
    public static final int TFCTOOLS = 1;//without damage
    public static final int TFCARMOR = 2;//without damage
    public static final int TFCTOOLSADAMAGE = 3;//without damage
    static final int TFCWEAPON = 4;//without damage

    public static String TFCFluidsList = "";


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
        int minMeta = 0;


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
                 else if ( item instanceof com.bioxx.tfc.Items.ItemBloom)
                 {
                     maxMeta = 840;
                     minMeta = 100;
                 }
             }
             catch (Exception e)
             {
                 maxMeta = 0;
             }
            //Exception
            //cookedLimonit Udary has some fun metaData for items like 10 15 25 35 for limonite ore
            //if (maxMeta == 3 && item.getClass().toString().contains("udary.tfcudarymod.items.ores.ItemOre") )
            if (maxMeta == 3 && item.getClass() == Integration.UdaryItemOreClass)
            {
                if ( meta != 10 && meta != 15 && meta != 25 && meta != 35)
                    return 25;
                else
                    return meta;
            }
            //else if (maxMeta == 1 && item.getClass().toString().contains("udary.tfcudarymod.items.ores.ItemOreFlake"))
            //OreFlake nikel of silver have 1 or 10 units
            else if (maxMeta == 1 && item.getClass()== Integration.UdaryItemOreFlakeClass)
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

        if ( meta < minMeta )
            meta = minMeta;

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

        // meta 0 is unfired vessel cant have nbt
        if (payStack.getItem() instanceof ItemPotterySmallVessel && meta > 0)
        {
            payStack = createSmallVesellNBTByParam(payStack, count, p1, p2, p3, p4);
        }
        else if (payStack.getItem() instanceof ItemCrucible)
        {
            payStack = createCrucibleNBTByParam(payStack, count, p1, p2, p3, p4);
        }
        else if (payStack.getItem() instanceof IFood)
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
            // have are restriction on set of any type of Entity
            payStack.stackTagCompound = a.writeToNBT();//can return null
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

    public static EditPayParams getParamsForSmithingItem(ItemStack iStack)
    {
        if ( iStack == null || !iStack.stackTagCompound.hasKey("craftingTag"))
            return null;

        int duraBuff = (int) Math.floor( getDurabilityBuff(iStack) * 100 );
        duraBuff = (duraBuff < 0)? 0 : duraBuff;

        return new EditPayParams(duraBuff);
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

        if (i instanceof ItemTFCArmor )
        {
            Armor armor = ((ItemTFCArmor) i ).armorTypeTFC;
            if (armor != null)
            {
                return ( armor.metaltype != null && !armor.metaltype.isEmpty()
                        && armor.metaltype.contains("Leather") );
            }
        }

        boolean r =
                (  i == TFCItems.flintSteel
                || i == TFCItems.fireStarter
                || i == TFCItems.bow
                || i == TFCItems.fishingRod
                || i == TFCItems.spindle
                || i == TFCItems.stoneHammer
                || i == TFCItems.stoneHammerHead
                || i == TFCItems.stoneKnife
                || i == TFCItems.stoneKnifeHead
                || i == TFCItems.arrow
                || i == TFCItems.igInAxe
                || i == TFCItems.igInHoe
                || i == TFCItems.sedShovel
                || i == TFCItems.sedAxe
                || i == TFCItems.sedHoe
                || i == TFCItems.igExShovel
                || i == TFCItems.igExAxe
                || i == TFCItems.igExHoe
                || i == TFCItems.mMShovel
                || i == TFCItems.mMAxe
                || i == TFCItems.mMHoe
                || i == TFCItems.igInStoneJavelin
                || i == TFCItems.sedStoneJavelin
                || i == TFCItems.igExStoneJavelin
                || i == TFCItems.mMStoneJavelin
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
        if (iStack == null)
            return NOTFC;

        Item item = iStack.getItem();

        if (item instanceof ItemWeapon
                || item instanceof ItemCustomSword
                //tfcm warhammer and halberd
                || Integration.isTerraMiscModLoaded()
                   && item.getClass() == Integration.TerraMiscItemCustomToolHeadClass)
            return TFCWEAPON;//Weapon

        if ( item instanceof com.bioxx.tfc.Items.Tools.ItemCustomSaw
                || item instanceof com.bioxx.tfc.Items.Tools.ItemCustomHoe
                || item instanceof com.bioxx.tfc.Items.Tools.ItemChisel
                || item instanceof com.bioxx.tfc.Items.Tools.ItemProPick
                )
            return TFCTOOLS;

        if (item instanceof com.bioxx.tfc.Items.Tools.ItemCustomAxe
                || item instanceof com.bioxx.tfc.Items.Tools.ItemCustomPickaxe
                || item instanceof com.bioxx.tfc.Items.Tools.ItemCustomScythe
                || item instanceof com.bioxx.tfc.Items.Tools.ItemCustomShovel
                || item instanceof com.bioxx.tfc.Items.Tools.ItemHammer
                )
        return TFCTOOLSADAMAGE;

        if (item instanceof ItemTFCArmor ||
                //Shield from AutomatedBellowsAddon have duraBuff
                item.getClass() == Integration.ABellowsItemRoundShieldClass )
            return TFCARMOR;

        if ( item.getClass() == Integration.TerraMiscItemCustomLongbowClass )
            return NOTFC;//no durabuff bonus;

        if (item instanceof com.bioxx.tfc.Items.Tools.ItemTerraTool ||
                item instanceof ItemMiscToolHead )
        {
            String[] noAttackDamage = {"Saw", "Hoe", "Chisel", "Propick"};
            String itemName = item.getUnlocalizedName();
            if ( itemName.isEmpty() )
                return NOTFC;

            for (String s :noAttackDamage)
            {
                if (itemName.contains(s))
                    //tools withount attackdamage
                    return TFCTOOLS;
            }
            return TFCTOOLSADAMAGE;//Tools with attackdamage and blade of weapons
        }

        return NOTFC;
    }


    public static EditPayParams getParamsForTFCSimpleFood(ItemStack iStack)
    {
       if (iStack == null || iStack.stackTagCompound==null)
           return null;

       int p1 = ExtendedLogic.getCookedLevel(iStack);
       int p2 = Food.isSalted(iStack)? 1 : 0;
       int p3 = Food.isDried(iStack) ? 1 : 0;
       //brined pickled smoked at one param
       int p4 = getTFCFoodParams(iStack);

       return new EditPayParams(p1,p2,p3,p4);
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
        int brined = param4<10 ? param4 : param4 % 10;
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


    public static EditPayParams getParamsForSmallVessel(ItemStack svessel)
    {
        if (svessel == null || !(svessel.getItem() instanceof ItemPotterySmallVessel)
                || svessel.stackTagCompound == null)
            return null;

        EditPayParams params = new EditPayParams();

        if  ( svessel.stackTagCompound.hasKey("Items") )
        {
            params.p1 = 1;
        }
        else if ( svessel.stackTagCompound.hasKey("TempTimer") )
        {
            //TempTimer:5448L,MetalAmount:560,MetalType:"Copper"

            String metalTypeName = svessel.stackTagCompound.getString("MetalType");
            if (metalTypeName==null || metalTypeName.isEmpty())
                return params;
            Metal metal = MetalRegistry.instance.getMetalFromString(metalTypeName);
            if (metal ==null)
                return params;
            params.p3 = Item.getIdFromItem(metal.ingot);
            params.p4 = svessel.stackTagCompound.getInteger("MetalAmount");
       }
        return params;
    }


    /**
     *  //TempTimer:5448L,MetalAmount:560,MetalType:"Copper"
     */
    public static ItemStack createSmallVesellNBTByParam(ItemStack svessel, int count, int p1, int p2,int p3, int p4)
    {
      if (svessel == null || !(svessel.getItem() instanceof ItemPotterySmallVessel) )
          return null;
      //p3 - metall type
      //p4 - metallamount
      if (p4 > 2240)
          p4 = 2240;//max;
      if (p4<1)
          p4 = 100;

      if (p3>0 && p4 > 0)
      {
          Item ingot = Item.getItemById(p3);
          if (ingot == null)
              return svessel;
          Metal metal = MetalRegistry.instance.getMetalFromItem(ingot);
          if (metal == null)
              return svessel;
          svessel.stackTagCompound = new NBTTagCompound();
          svessel.stackTagCompound.setLong("TempTimer", 0);
          svessel.stackTagCompound.setString("MetalType", metal.name);
          svessel.stackTagCompound.setInteger("MetalAmount",p4);
          svessel.setItemDamage(2);
      }
      return svessel;
    }

    /**
     * p1 - type of content 1-Metall  2-Items(ingots)
     * @param crucible
     * @return
     */
    public static EditPayParams getParamsForCrucible(ItemStack crucible) {
        if (crucible != null && crucible.stackTagCompound != null) {

            NBTTagCompound nbt = crucible.stackTagCompound;
            if (nbt != null && nbt.hasKey("Metals")) {
                NBTTagList nbttaglist = nbt.getTagList("Metals", 10);
                final int sz = nbttaglist.tagCount();

                //Work only with simple mettal not alloy!
                if (sz == 1) {
                    EditPayParams params = new EditPayParams();
                    params.p1 = 1;// type of crucible content or metallally or bag

                    NBTTagCompound nbtMetall = nbttaglist.getCompoundTagAt(0);
                    params.p3 = /*id*/ nbtMetall.getInteger("ID");
                    params.p4 = /*amount*/ (int) nbtMetall.getFloat("AmountF");//"Amount"
                    return params;
                } else {
                    int p1 = 3;//multi-alloy not Support!
                    //return null
                }
            }

            if  (nbt.hasKey("Items") ) {
                NBTTagList items = crucible.getTagCompound().getTagList("Items", 10/*COMPOUND*/);
                if (items != null && items.tagCount() > 0) {
                    EditPayParams params = new EditPayParams();
                    NBTTagCompound el = items.getCompoundTagAt(0);
                    if (el != null) {
                        ItemStack item = ItemStack.loadItemStackFromNBT(el);
                        if (item != null) {
                            params.p1 = 2;//ContentType item inside cruicible
                            //params.p2 = /*meta*/item.getItemDamage();// no id on ingot
                            params.p3 = /*id*/Item.getIdFromItem(item.getItem());
                            params.p4 = /*count*/item.stackSize;
                            return params;
                        }
                    }
                }
            }
        }
        return null;
    }
    /**
     * From params to ItemStack nbt
     * @param crucible
     * @param count
     * @param p1 type of content  1-metall, 2-Items
     * @param p2 t1-mettalId                t2 itemId(Ingot)
     * @param p3 t1-mettalAmount            t2 itemMeta
     * @param p4                            t3 count
     * @return
     */
    public static ItemStack createCrucibleNBTByParam(ItemStack crucible, int count, int p1, int p2,int p3, int p4) {
        if (crucible != null && crucible.getItem() instanceof ItemCrucible) {
            if (p1 == 1 && p3 > 0 && p4 > 0) {//Metalls
                crucible.stackTagCompound = new NBTTagCompound();
                crucible.stackTagCompound.setInteger("temp", 0);//for pass nbtcheck by stall
                NBTTagList nbtMetalls = new NBTTagList();
                crucible.stackTagCompound.setTag("Metals", nbtMetalls);
                NBTTagCompound nbtMetall = new NBTTagCompound();
                nbtMetall.setInteger("ID", p3);
                nbtMetall.setFloat("AmountF", Math.min(3000,p4));
                nbtMetalls.appendTag(nbtMetall);
                //for pass nbtcheck via stall
                //crucible.stackTagCompound.setInteger("temp", 0);
                //crucible.stackTagCompound.setTag("Items", new NBTTagList());
            }
            else if (p1 == 2) {//Items
                Item item = Item.getItemById(p3);
                if (item != null) {
                    ItemStack is = new ItemStack(item);
                    //is.setItemDamage(p3); Not Used
                    int max = is.getMaxStackSize();
                    is.stackSize = Math.min(max, p4);

                    crucible.stackTagCompound = new NBTTagCompound();
                    NBTTagList nbttaglist = new NBTTagList();
                    NBTTagCompound nbtItem = new NBTTagCompound();
                    nbtItem.setByte("Slot", (byte)0);
                    is.writeToNBT(nbtItem);
                    nbttaglist.appendTag(nbtItem);
                    crucible.stackTagCompound.setTag("Items", nbttaglist);
                    //for pass nbtcheck via stall
                    //crucible.stackTagCompound.setInteger("temp", 0);
                    //crucible.stackTagCompound.setTag("Metals", new NBTTagList());
                }
            }
            return crucible;
        }
        return null;
    }


    public static EditPayParams getParamsForSalad(ItemStack iStack)
    {
       if (iStack == null || !(iStack.getItem() instanceof ItemSalad)
               || iStack.stackTagCompound == null)
           return null;

       int [] fg = Food.getFoodGroups(iStack);
       if ( fg.length == 4){
           return new EditPayParams(fg[0],fg[1],fg[2],fg[3]);
       }
       return null;
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

    public static EditPayParams getParamsForBarrel(ItemStack iStack)
    {
        if (iStack==null || iStack.stackTagCompound==null)
            return null;
        int p1 = iStack.stackTagCompound.getBoolean("Sealed") ? 1 : 0;
        int p2 = iStack.stackTagCompound.getInteger("SealTime");
        p2 = getYearFromHours(p2);//zeroSealTimeBarrel or year of seal
        int p3 = 0;
        int p4 = 0;
        FluidStack fluidStack = EditPriceSlot.getFluid(iStack);
        if (fluidStack != null)
        {
            p3 = fluidStack.getFluidID();
            if (fluidStack.amount > 0 )
                p4 = (int) Math.floor(fluidStack.amount / 1000);
        }
        return new EditPayParams(p1,p2,p3,p4);
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
            //payStack.stackTagCompound.setInteger("barrelType", barrelType); ignore at compare
            payStack = setFluidID(payStack, p3, p4);
        }
        return payStack;
    }

    public static int getYearFromHours(int tHours)
    {
        return getYearFromHours(tHours, false);
    }

    //real=true -only for short barrelKey on quantity
    public static int getYearFromHours(int tHours, boolean real)
    {
        if ( tHours == 0)
            return 0;

        int tDays = tHours / TFC_Time.HOURS_IN_DAY;
        int tMonths = tDays / TFC_Time.daysInMonth;
        int year = tMonths / 12;

        return real ? year : year + 1000;
    }

    public static int getHoursForYear(int year)
    {
        if ( year == 1000)
            return 24;
        year = year - 1000;
        if (year<=0)
            return 0;
        int tMonths = year * 12;
        int tDays = tMonths * TFC_Time.daysInMonth;
        int tHours = tDays * TFC_Time.HOURS_IN_DAY;
        return tHours;
    }

    //fluid.getFluidID & fluid.amount
    public static FluidStack getFluid(ItemStack barrel)
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
        if (barrel==null || fluidID < 0 || amount<1 )
            return null;

       amount = (amount >= 10)? 10000: amount*1000;//ItemBarrels.MAX_LIQUID

       Fluid fluid = FluidRegistry.getFluid(fluidID);
       // don`t allow to set vanilla water & lava
       if (fluid == null || fluid == FluidRegistry.WATER || fluid == FluidRegistry.LAVA)
           return barrel;

       FluidStack fluidStack = new FluidStack(fluid, amount);

       if (fluidStack.getFluid() != null && fluidStack.getFluid().getTemperature(fluidStack) > Global.HOT_LIQUID_TEMP)
           return barrel;

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

    public static String getFluidNameByID(int id)
    {
        if ( id<0 || id>FluidRegistry.getMaxID())
            return "";
        Fluid fluid = FluidRegistry.getFluid(id);
        String un = (fluid == null) ? "" : fluid.getUnlocalizedName();
        return (un==null || un.isEmpty() )? "": StatCollector.translateToLocal(un);
    }

    public static String getValidFluidIDList()
    {
        if (TFCFluidsList==null || TFCFluidsList.isEmpty())
        {
            String r = "";
            int maxID = FluidRegistry.getMaxID();
            int id = 0;
            while (id <= maxID)
            {
                Fluid fluid = FluidRegistry.getFluid(id);
                id++;
                if (fluid==null)
                    continue;
                String name = fluid.getName();
                if (name==null || name.isEmpty())
                    continue;
                r += String.format("%s  %s\n",String.valueOf(id-1), name);
            }
            TFCFluidsList = r;
        }
        return TFCFluidsList;
    }


}
