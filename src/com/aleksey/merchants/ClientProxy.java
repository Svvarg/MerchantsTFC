package com.aleksey.merchants;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import com.aleksey.merchants.Core.BlockList;
import com.aleksey.merchants.Core.ItemList;
import com.aleksey.merchants.Render.Blocks.RenderAnvilDie;
import com.aleksey.merchants.Render.Blocks.RenderStall;
import com.aleksey.merchants.Render.Blocks.RenderStorageRack;
import com.aleksey.merchants.Render.Blocks.RenderWarehouse;
import com.aleksey.merchants.TESR.TESRStall;
import com.aleksey.merchants.TESR.TESRStorageRack;
import com.aleksey.merchants.TileEntities.TileEntityStall;
import com.aleksey.merchants.TileEntities.TileEntityStorageRack;

import org.swarg.merchants.tesr.TESRBigStall;
import org.swarg.merchants.render.blocks.RenderBigStall;
import org.swarg.merchants.tileentities.TileEntityBigStall;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Loader;

public class ClientProxy extends CommonProxy
{
    @Override
    public boolean isRemote()
    {
        return true;
    }

    @Override
    public void registerRenderInformation()
    {
        RenderingRegistry.registerBlockHandler(BlockList.StallRenderId = RenderingRegistry.getNextAvailableRenderId(), new RenderStall());
        RenderingRegistry.registerBlockHandler(BlockList.BigStallRenderId = RenderingRegistry.getNextAvailableRenderId(), new RenderBigStall());
        RenderingRegistry.registerBlockHandler(BlockList.WarehouseRenderId = RenderingRegistry.getNextAvailableRenderId(), new RenderWarehouse());
        RenderingRegistry.registerBlockHandler(BlockList.AnvilDieRenderId = RenderingRegistry.getNextAvailableRenderId(), new RenderAnvilDie());
        RenderingRegistry.registerBlockHandler(BlockList.StorageRackRenderId = RenderingRegistry.getNextAvailableRenderId(), new RenderStorageRack());
    }

    @Override
    public void registerTileEntities()
    {
        registerCommonTileEntities();

        ClientRegistry.registerTileEntity(TileEntityStall.class, "TileEntityStall", new TESRStall());
        ClientRegistry.registerTileEntity(TileEntityBigStall.class, "TileEntityBigStall", new TESRBigStall());
        ClientRegistry.registerTileEntity(TileEntityStorageRack.class, "TileEntityStorageRack", new TESRStorageRack());
    }

	@Override
	public void hideNEIItems()
	{
		String mod = "NotEnoughItems";

		if (Loader.isModLoaded(mod))
		{
			codechicken.nei.api.API.hideItem(new ItemStack(ItemList.Coin, 1, OreDictionary.WILDCARD_VALUE));
			codechicken.nei.api.API.hideItem(new ItemStack(ItemList.WarehouseBook));

			for(int i = 0; i < BlockList.AnvilDies.length; i++)
				codechicken.nei.api.API.hideItem(new ItemStack(BlockList.AnvilDies[i], 1, OreDictionary.WILDCARD_VALUE));
		}
	}
}
