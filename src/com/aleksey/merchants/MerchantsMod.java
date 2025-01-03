package com.aleksey.merchants;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;

import com.aleksey.merchants.Core.BlockList;
import com.aleksey.merchants.Core.ItemList;
import com.aleksey.merchants.Core.MerchantsTabs;
import com.aleksey.merchants.Core.Recipes;
import com.aleksey.merchants.Core.Player.PlayerTracker;
import com.aleksey.merchants.Extended.Integration;
import com.aleksey.merchants.Handlers.ChunkEventHandler;
import com.aleksey.merchants.Handlers.Network.DieCopyPacket;
import com.aleksey.merchants.Handlers.Network.InitClientWorldPacket;
import com.aleksey.merchants.Helpers.WarehouseManager;
import com.bioxx.tfc.TerraFirmaCraft;
import com.bioxx.tfc.api.TFCItems;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid="MerchantsTFC", name="Merchants", version="1.2.0", dependencies="after:TerraFirmaCraft")
public class MerchantsMod
{
    @Instance("MerchantsTFC")
    public static MerchantsMod instance;

    @SidedProxy(clientSide = "com.aleksey.merchants.ClientProxy", serverSide = "com.aleksey.merchants.CommonProxy")
    public static CommonProxy proxy;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        //LanternConfig.loadConfig(event);

        BlockList.loadBlocks();
        BlockList.registerBlocks();

        proxy.registerTickHandler();
        proxy.registerTileEntities();

        ItemList.Setup();

        MerchantsTabs.MainTab.setTabIconItemStack(new ItemStack(BlockList.Stalls[0]));
        MerchantsTabs.MainTab.setTabIconItemStack(new ItemStack(BlockList.BigStalls[0]));

        proxy.registerGuiHandler();
    }

    @EventHandler
    public void initialize(FMLInitializationEvent event)
    {
        TerraFirmaCraft.PACKET_PIPELINE.registerPacket(InitClientWorldPacket.class);
        TerraFirmaCraft.PACKET_PIPELINE.registerPacket(DieCopyPacket.class);

        FMLCommonHandler.instance().bus().register(new PlayerTracker());

        // Register the Chunk Load/Save Handler
        MinecraftForge.EVENT_BUS.register(new ChunkEventHandler());

        proxy.registerRenderInformation();

        OreDictionary.registerOre("materialCloth", new ItemStack(TFCItems.burlapCloth));
        Recipes.registerRecipes();

        WarehouseManager.init();

		//WAILA stuff
		proxy.registerWailaClasses();
		proxy.hideNEIItems();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        Integration.getIntegrationModeLoaded();
    }
}
