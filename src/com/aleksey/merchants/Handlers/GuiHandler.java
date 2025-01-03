package com.aleksey.merchants.Handlers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.aleksey.merchants.Containers.ContainerAnvilDie;
import com.aleksey.merchants.Containers.ContainerStall;
import com.aleksey.merchants.Containers.ContainerStallLimit;
import com.aleksey.merchants.Containers.ContainerStallSetPayItem;
import com.aleksey.merchants.Containers.ContainerStorageRack;
import com.aleksey.merchants.Containers.ContainerTrussel;
import com.aleksey.merchants.Containers.ContainerWarehouse;
import com.aleksey.merchants.GUI.GuiAnvilDie;
import com.aleksey.merchants.GUI.GuiStall;
import com.aleksey.merchants.GUI.GuiStallLimit;
import com.aleksey.merchants.GUI.GuiStallSetPayItem;
import com.aleksey.merchants.GUI.GuiStorageRack;
import com.aleksey.merchants.GUI.GuiTrussel;
import com.aleksey.merchants.GUI.GuiTrusselCreate;
import com.aleksey.merchants.GUI.GuiWarehouse;
import com.aleksey.merchants.TileEntities.TileEntityAnvilDie;
import com.aleksey.merchants.TileEntities.TileEntityStall;
import com.aleksey.merchants.TileEntities.TileEntityStorageRack;
import com.aleksey.merchants.TileEntities.TileEntityWarehouse;

import org.swarg.merchants.gui.GuiBigStall;
import org.swarg.merchants.gui.GuiBigStallLimit;
import org.swarg.merchants.gui.GuiBigStallSetPayItem;
import org.swarg.merchants.tileentities.TileEntityBigStall;
import org.swarg.merchants.containers.ContainerBigStall;
import org.swarg.merchants.containers.ContainerBigStallLimit;
import org.swarg.merchants.containers.ContainerBigStallSetPayItem;

import cpw.mods.fml.common.network.IGuiHandler;

import static com.aleksey.merchants.Handlers.GuiHandler.GuiStallSetPayItem;


public class GuiHandler implements IGuiHandler
{
    public static final int GuiOwnerStall = 0;
    public static final int GuiBuyerStall = 1;
    public static final int GuiOwnerStallLimit = 2;
    public static final int GuiWarehouse = 3;
    public static final int GuiTrusselCreate = 4;
    public static final int GuiTrussel = 5;
    public static final int GuiAnvilDie = 6;
    public static final int GuiStorageRack = 7;
    public static final int GuiStallSetPayItem = 8;

    public static final int GuiOwnerBigStall = 10;
    public static final int GuiBuyerBigStall = 11;
    public static final int GuiOwnerBigStallLimit = 12;
    public static final int GuiBigStallSetPayItem = 13;


    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntity te = world.getTileEntity(x, y, z);

        switch(id)
        {
            case GuiOwnerStall:
                return new ContainerStall(player.inventory, (TileEntityStall)te, true, world, x, y, z);
            case GuiBuyerStall:
                return new ContainerStall(player.inventory, (TileEntityStall)te, false, world, x, y, z);
            case GuiOwnerStallLimit:
                return new ContainerStallLimit(player.inventory, (TileEntityStall)te, world, x, y, z);
            case GuiWarehouse:
                return new ContainerWarehouse(player.inventory, (TileEntityWarehouse)te, world, x, y, z);
            case GuiTrusselCreate:
                return null;
            case GuiTrussel:
                return new ContainerTrussel(player.inventory, world, x, y, z);
            case GuiAnvilDie:
                return new ContainerAnvilDie(player.inventory, (TileEntityAnvilDie)te, world, x, y, z);
            case GuiStorageRack:
                return new ContainerStorageRack(player.inventory, (TileEntityStorageRack)te, world, x, y, z);

            case GuiStallSetPayItem:
                return new ContainerStallSetPayItem(player.inventory, (TileEntityStall)te, world, x, y, z);

            // Big Stall
            case GuiOwnerBigStall:
                return new ContainerBigStall(player.inventory, (TileEntityBigStall)te, true, world, x, y, z);
            case GuiBuyerBigStall:
                return new ContainerBigStall(player.inventory, (TileEntityBigStall)te, false, world, x, y, z);
            case GuiOwnerBigStallLimit:
                return new ContainerBigStallLimit(player.inventory, (TileEntityBigStall)te, world, x, y, z);
            case GuiBigStallSetPayItem:
                return new ContainerBigStallSetPayItem(player.inventory, (TileEntityBigStall)te, world, x, y, z);

            default:
                return null;
        }
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z)
    {
        TileEntity te;

        try
        {
            te = world.getTileEntity(x, y, z);
        }
        catch(Exception e)
        {
            te = null;
        }

        switch(id)
        {
            case GuiOwnerStall:
                return new GuiStall(player.inventory, (TileEntityStall)te, true, world, x, y, z);
            case GuiBuyerStall:
                return new GuiStall(player.inventory, (TileEntityStall)te, false, world, x, y, z);

            case GuiOwnerStallLimit:
                return new GuiStallLimit(player.inventory, (TileEntityStall)te, world, x, y, z);
            case GuiWarehouse:
                return new GuiWarehouse(player.inventory, (TileEntityWarehouse)te, world, x, y, z);
            case GuiTrusselCreate:
                return new GuiTrusselCreate(player.inventory, world);
            case GuiTrussel:
                return new GuiTrussel(player.inventory, world, x, y, z);
            case GuiAnvilDie:
                return new GuiAnvilDie(player.inventory, (TileEntityAnvilDie)te, world, x, y, z);
            case GuiStorageRack:
                return new GuiStorageRack(player.inventory, (TileEntityStorageRack)te, world, x, y, z);

            case GuiStallSetPayItem:
                return new GuiStallSetPayItem(player.inventory, (TileEntityStall)te, world, x, y, z);

            // Big Stall
            case GuiOwnerBigStall:
                return new GuiBigStall(player.inventory, (TileEntityBigStall)te, true, world, x, y, z);
            case GuiBuyerBigStall:
                return new GuiBigStall(player.inventory, (TileEntityBigStall)te, false, world, x, y, z);
            case GuiOwnerBigStallLimit:
                return new GuiBigStallLimit(player.inventory, (TileEntityBigStall)te, world, x, y, z);
            case GuiBigStallSetPayItem:
                return new GuiBigStallSetPayItem(player.inventory, (TileEntityBigStall)te, world, x, y, z);

            default:
                return null;
        }
    }
}
