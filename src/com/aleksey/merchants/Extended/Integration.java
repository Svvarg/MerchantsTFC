package com.aleksey.merchants.Extended;

import cpw.mods.fml.common.Loader;
import net.minecraft.item.ItemStack;

/**
 *
 * @author Swarg
 */
public class Integration {
    
    public static final int MODUKNOWN = 0;
    public static final int MODLOADED = 1;
    public static final int MODNOTLOADED = 2;
    
    //reflection
    public static final String modAnimalCrateName = "animalcrate";    
    public static int isAnimalCrateModLoaded = 0 ;//0 not checked 1 true 2 false
    public static Class ItemCrateClass;
    public static final String ItemCrateClassFullName = "taeog.animalcrate.item.ItemCrate";
    
    public static int isUdaryModLoaded = 0 ;
    public static final String modUdaryName = "tfcudarymod";         
    public static Class UdaryItemOreClass;     
    public static Class UdaryItemOreFlakeClass; 
    public static Class UdaryItemCeramicJugMilkClass;
    public static Class UdaryItemOreCookerClass;
    public static final String UdaryItemOreClassFullName = "udary.tfcudarymod.items.ores.ItemOre";
    public static final String UdaryItemOreFlakeClassFullName = "udary.tfcudarymod.items.ores.ItemOreFlake";    
    public static final String UdaryItemCeramicJugMilkClassFullName = "udary.tfcudarymod.items.fluids.ItemCeramicJugMilk";
    public static final String UdaryItemOreCookerClassFullName = "udary.tfcudarymod.items.devices.ItemOreCooker";
        
    public static int isAutomatedBellowsModLoaded = 0;
    public static final String modAutomatedBellowsName = "tfcautomatedbellows";         
    public static Class ABellowsItemRoundShieldClass;
    public static final String ABellowsItemRoundShieldClassFullName = "sladki.tfc.ab.Items.Armor.ItemRoundShield";    
    public static Class ABellowsBlockWaterFilterClass;
    public static final String ABellowsBlockWaterFilterClassFullName = "sladki.tfc.ab.Items.ItemBlockWaterFilter";    
    public static Class ABellowsBlockSteamBoilerClass;
    public static final String ABellowsBlockSteamBoilerClassFullName = "sladki.tfc.ab.Items.ItemBlockSteamBoiler";    
    public static Class ABellowsBlockPotteryKilnChamberClass;
    public static final String ABellowsBlockPotteryKilnChamberClassFullName = "sladki.tfc.ab.Items.ItemBlockPotteryKilnChamber";    
    public static Class ABellowsBlockPotteryKilnClass;
    public static final String ABellowsBlockPotteryKilnClassFullName = "sladki.tfc.ab.Items.ItemBlockPotteryKiln";    
    public static Class ABellowsBlockAutomatedBellowsClass;
    public static final String ABellowsBlockAutomatedBellowsClassFullName = "sladki.tfc.ab.Items.ItemBlockAutomatedBellows";
    
    public static int isTerraMiscModLoaded = 0;
    public static final String modTFCMiscName = "tfcm";         
    public static Class TerraMiscItemCustomToolHeadClass;
    public static Class TerraMiscItemCustomArrowClass;
    public static Class TerraMiscItemCustomLongbowClass;
    
    //public static final String ABellowsItemRoundShieldClassFullName = "sladki.tfc.ab.Items.Armor.ItemRoundShield";    
    
    
    
    public static boolean isAnimalCrateModLoaded()
    {
      if (isAnimalCrateModLoaded == MODUKNOWN)
      {          
         isAnimalCrateModLoaded = Loader.isModLoaded(modAnimalCrateName) 
                 ? MODLOADED : MODNOTLOADED ;        
         if (isAnimalCrateModLoaded == MODLOADED )
          try {
              ItemCrateClass = Class.forName( ItemCrateClassFullName );
          } catch (ClassNotFoundException ex) {
              
          }
      }  
      return isAnimalCrateModLoaded == MODLOADED;
    } 
    
    public static boolean isUdaryModLoaded()
    {
        if ( isUdaryModLoaded == MODUKNOWN ) 
        {
            isUdaryModLoaded = Loader.isModLoaded(modUdaryName)? MODLOADED : MODNOTLOADED;
            if ( isUdaryModLoaded == MODLOADED )
            {
                try {
                    UdaryItemOreClass = Class.forName( UdaryItemOreClassFullName );
                    UdaryItemOreFlakeClass = Class.forName( UdaryItemOreFlakeClassFullName );
                    UdaryItemCeramicJugMilkClass = Class.forName(UdaryItemCeramicJugMilkClassFullName);
                    UdaryItemOreCookerClass = Class.forName(UdaryItemOreCookerClassFullName);
                } catch (ClassNotFoundException ex) {
                    
                }
            }            
        }
        return isUdaryModLoaded == MODLOADED;
    }
    
    public static boolean isUdaryJugMilk(Class cls)
    {
       return isUdaryModLoaded() &&  UdaryItemCeramicJugMilkClass != null &&
               cls == UdaryItemCeramicJugMilkClass ;
    }
    
    
    public static boolean isABellowsModLoaded()
    {
        if ( isAutomatedBellowsModLoaded==MODUKNOWN )
        {
            isAutomatedBellowsModLoaded = Loader.isModLoaded(modAutomatedBellowsName)
                    ? MODLOADED : MODNOTLOADED;
            if (isAutomatedBellowsModLoaded==MODLOADED)
            {
                try {
                    ABellowsItemRoundShieldClass = Class.forName( ABellowsItemRoundShieldClassFullName );
                    ABellowsBlockWaterFilterClass = Class.forName(ABellowsBlockWaterFilterClassFullName);
                    ABellowsBlockSteamBoilerClass = Class.forName(ABellowsBlockSteamBoilerClassFullName);
                    ABellowsBlockPotteryKilnChamberClass = Class.forName(ABellowsBlockPotteryKilnChamberClassFullName);
                    ABellowsBlockPotteryKilnClass = Class.forName(ABellowsBlockPotteryKilnClassFullName);
                    ABellowsBlockAutomatedBellowsClass = Class.forName(ABellowsBlockAutomatedBellowsClassFullName);
    
                } catch (ClassNotFoundException ex) {}                
            }            
        }
        return isAutomatedBellowsModLoaded==MODLOADED;
    }
    
    public static boolean isTerraMiscModLoaded()
    {
        if (isTerraMiscModLoaded == MODUKNOWN)
        {
            isTerraMiscModLoaded =  Loader.isModLoaded(modTFCMiscName)
                    ? MODLOADED : MODNOTLOADED;
            if (isTerraMiscModLoaded == MODLOADED)
            try {
                TerraMiscItemCustomToolHeadClass = Class.forName("terramisc.items.tools.ItemCustomToolHead");
                TerraMiscItemCustomArrowClass = Class.forName("terramisc.items.tools.ItemCustomArrow");
                TerraMiscItemCustomLongbowClass = Class.forName("terramisc.items.tools.ItemCustomLongbow");
            }catch (Exception e){}
        }
        return isTerraMiscModLoaded == MODLOADED;
    }

    public static void getIntegrationModeLoaded()
    {
        isAnimalCrateModLoaded();
        isUdaryModLoaded();
        isABellowsModLoaded();
        isTerraMiscModLoaded();    
    }
}
