package com.aleksey.merchants.Helpers;

import com.aleksey.merchants.Extended.ExtendedLogic;
import static com.aleksey.merchants.Extended.ExtendedLogic.getFirstItemStackFromItemTileEntity;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.aleksey.merchants.Core.WarehouseBookInfo;
import com.aleksey.merchants.TileEntities.TileEntityWarehouse;
import com.aleksey.merchants.WarehouseContainers.BarrelContainer;
import com.aleksey.merchants.WarehouseContainers.ChestContainer;
import com.aleksey.merchants.WarehouseContainers.IngotPileContainer;
import com.aleksey.merchants.WarehouseContainers.LogPileContainer;
import com.aleksey.merchants.WarehouseContainers.ToolRackContainer;
import com.aleksey.merchants.api.IWarehouseContainer;
import com.aleksey.merchants.api.ItemTileEntity;
import com.aleksey.merchants.api.Point;
import com.aleksey.merchants.api.WarehouseContainerList;
import com.bioxx.tfc.Items.Pottery.ItemPotterySmallVessel;
import com.bioxx.tfc.api.Interfaces.IFood;
import static com.aleksey.merchants.Extended.ExtendedLogic.getNoSplitFoodWeight;
import com.bioxx.tfc.Items.ItemBlocks.ItemBarrels;
import com.bioxx.tfc.Items.ItemBlocks.ItemLargeVessel;
import com.bioxx.tfc.Items.ItemIngot;

public class WarehouseManager
{
    private static final int _searchContainerRadius = 3;
    private static final int _searchWarehouseDistance = 14;

    private ArrayList<Point> _containerLocations;
    private Hashtable<String, Integer> _quantities;
    private ItemStack _goodItemStack;
    private ItemStack _payItemStack;
    public ItemStack _goodItemFromWarehouseContainer;
    private ArrayList<ItemTileEntity> _goodList;
    private ArrayList<ItemTileEntity> _payList;

    public static void init()
    {
        WarehouseContainerList.addContainer(new ChestContainer());
        WarehouseContainerList.addContainer(new LogPileContainer());
        WarehouseContainerList.addContainer(new IngotPileContainer());
        WarehouseContainerList.addContainer(new ToolRackContainer());
        WarehouseContainerList.addContainer(new BarrelContainer());
    }

    public WarehouseManager()
    {
        _containerLocations = new ArrayList<Point>();
        _quantities = new Hashtable<String, Integer>();
    }

    public int getContainers()
    {
        return _containerLocations.size();
    }

    public int getQuantity(ItemStack itemStack)
    {
        String itemKey = ItemHelper.getItemKey(itemStack);

        // for ability byu and sell zeroSealTime barrels
        if (itemStack.getItem() instanceof ItemBarrels
                || itemStack.getItem() instanceof ItemLargeVessel)
            return ExtendedLogic.getCorrectBarrelsQuantityOnWarehouse(_quantities, itemStack, itemKey);

        return _quantities.containsKey(itemKey) ? _quantities.get(itemKey): 0;
    }

    public void confirmTrade(World world)
    {
        confirmTradeGoods(world);

        String goodKey = ItemHelper.getItemKey(_goodItemStack);

        if (_quantities.containsKey(goodKey))//hello zeroSealTime Barrel
            _quantities.put(goodKey, _quantities.get(goodKey) - ItemHelper.getItemStackQuantity(_goodItemStack));
        _goodList = null;

        confirmTradePays(world);

        String payKey = ItemHelper.getItemKey(_payItemStack);
        int currentQuantity = _quantities.containsKey(payKey) ? _quantities.get(payKey): 0;
        _quantities.put(payKey, currentQuantity + ItemHelper.getItemStackQuantity(_payItemStack));
        _payList = null;
    }

    private void confirmTradeGoods(World world)
    {
        for(int i = 0; i < _goodList.size(); i++)
        {
            ItemTileEntity goodTileEntity = _goodList.get(i);

            goodTileEntity.Container.confirmTradeGoods(world, goodTileEntity, _goodItemStack);

            world.markBlockForUpdate(goodTileEntity.TileEntity.xCoord, goodTileEntity.TileEntity.yCoord, goodTileEntity.TileEntity.zCoord);
        }
    }

    private void confirmTradePays(World world)
    {
        for(int i = 0; i < _payList.size(); i++)
        {
            ItemTileEntity payTileEntity = _payList.get(i);

            payTileEntity.Container.confirmTradePays(world, payTileEntity, _payItemStack, _containerLocations);

            world.markBlockForUpdate(payTileEntity.TileEntity.xCoord, payTileEntity.TileEntity.yCoord, payTileEntity.TileEntity.zCoord);
        }
    }

    public PrepareTradeResult prepareTrade(ItemStack goodStack, ItemStack payStack, WarehouseBookInfo info, World world)
    {
        int goodQuantity = ItemHelper.getItemStackQuantity(goodStack);
        int payQuantity = ItemHelper.getItemStackQuantity(payStack);

        int goodQuantityOnWarehouse = getQuantity(goodStack);
        if (goodQuantityOnWarehouse > 0 && goodStack.getItem() instanceof ItemIngot)
        {
            //not sell the last ingot from the warehouse
            if (goodQuantityOnWarehouse == 1 || ( goodQuantityOnWarehouse  == goodQuantity ) )
                return PrepareTradeResult.NoLastIngot;

            goodQuantityOnWarehouse -= 1;
        }

        //if(goodQuantity == 0 || getQuantity(goodStack) < goodQuantity)
        if(goodQuantity == 0 || goodQuantityOnWarehouse < goodQuantity)
            return PrepareTradeResult.NoGoods;

        _goodList = new ArrayList<ItemTileEntity>();
        _payList = new ArrayList<ItemTileEntity>();

        if(payStack.getItem() instanceof IFood)
        {
            for(int i = 0; i < _containerLocations.size() && payQuantity > 0; i++)
            {
                Point p = _containerLocations.get(i);
                TileEntity tileEntity = world.getTileEntity(p.X, p.Y, p.Z);
                IWarehouseContainer container = WarehouseContainerList.getContainer(tileEntity);

                if(container != null)
                    payQuantity -= container.searchFreeSpaceInSmallVessels(tileEntity, payStack, payQuantity, _payList);
            }
        }

        int extendLimitY = info.Y + _searchContainerRadius;

        for(int i = 0; i < _containerLocations.size() && (goodQuantity > 0 || payQuantity > 0); i++)
        {
            Point p = _containerLocations.get(i);
            TileEntity tileEntity = world.getTileEntity(p.X, p.Y, p.Z);
            IWarehouseContainer container = WarehouseContainerList.getContainer(tileEntity);

            if(container == null)
                continue;

            if(goodQuantity > 0)
                goodQuantity -= container.searchItems(tileEntity, goodStack, goodQuantity, _goodList);

            if(payQuantity > 0)
                payQuantity -= container.searchFreeSpace(world, tileEntity, payStack, payQuantity, extendLimitY, _payList);
        }

        _goodItemStack = goodStack.copy();
        _payItemStack = payStack.copy();

        this._goodItemFromWarehouseContainer = null;

        if(goodQuantity == 0 && payQuantity == 0)
        {
            this._goodItemFromWarehouseContainer = getFirstItemStackFromItemTileEntity(_goodList,goodStack);
            return PrepareTradeResult.Success;
        }

        return goodQuantity > 0 ? PrepareTradeResult.NoGoods: PrepareTradeResult.NoPays;
    }

    public boolean existWarehouse(int stallX, int stallY, int stallZ, WarehouseBookInfo info, World world)
    {
        double distance = Math.sqrt(Math.pow(info.X - stallX, 2) + Math.pow(info.Y - stallY, 2) + Math.pow(info.Z - stallZ, 2));

        if(distance > _searchWarehouseDistance)
            return false;

        TileEntity tileEntity = world.getTileEntity(info.X, info.Y, info.Z);

        return tileEntity instanceof TileEntityWarehouse && ((TileEntityWarehouse)tileEntity).getKey() == info.Key;
    }

    public void searchContainerLocations(WarehouseBookInfo info, World world)
    {
        _containerLocations.clear();
        _quantities.clear();

        int startX = info.X - _searchContainerRadius;
        int endX = info.X + _searchContainerRadius;
        int startY = info.Y - _searchContainerRadius;
        int endY = info.Y + _searchContainerRadius;
        int startZ = info.Z - _searchContainerRadius;
        int endZ = info.Z + _searchContainerRadius;

        int wareHouseChunkX = info.X >> 4;
        int wareHouseChunkZ = info.Z >> 4;

        for(int x = startX; x <= endX; x++)
        {
            for(int y = startY; y <= endY; y++)
            {
                for(int z = startZ; z <= endZ; z++)
                {

                    TileEntity tileEntity = world.getTileEntity(x, y, z);

                    if(WarehouseContainerList.getContainer(tileEntity) != null)
                    {
                        if (ExtendedLogic.seeContainersOnlyWarehouseChunk)
                        {
                            //dont see containers on difrent chunk for towny
                            int chunkX = x >> 4;
                            int chunkZ = z >> 4;
                            if (chunkX != wareHouseChunkX || chunkZ != wareHouseChunkZ )
                                continue;
                        }

                        _containerLocations.add(new Point(x, y, z));

                        calculateQuantities((IInventory)tileEntity);
                    }
                }
            }
        }
    }

    private void calculateQuantities(IInventory inventory)
    {
        for(int i = 0; i < inventory.getSizeInventory(); i++)
        {
            ItemStack itemStack = inventory.getStackInSlot(i);

            if(itemStack == null)
                continue;

            if(itemStack.getItem() instanceof ItemPotterySmallVessel)
            {
                ItemStack[] vesselItemStacks = SmallVesselHelper.getVesselItemStacks(itemStack);

                if(vesselItemStacks == null
                        || vesselItemStacks[0] == null && vesselItemStacks[1] == null && vesselItemStacks[2] == null && vesselItemStacks[3] == null
                        )
                {
                    addItemStackQuantity(itemStack);
                }
                else
                {
                    for(int k = 0; k < vesselItemStacks.length; k++)
                    {
                        if(vesselItemStacks[k] != null)
                            addItemStackQuantity(vesselItemStacks[k]);
                    }
                    // case then in smVessel locate some Items and this smVessel is a good from StallFaceSlot
                    // if no empty another smVessels this can`t sell to player as goods. Therefore added his to quantity
                    addItemStackQuantity(itemStack);
                }
            }
            else
                addItemStackQuantity(itemStack);
        }
    }

    private void addItemStackQuantity(ItemStack itemStack)
    {
        int quantity = ItemHelper.getItemStackQuantity(itemStack);

        // stall not split milkjug and bucket and Salad
        //therefore cant combine 18oz+18oz for giving 20 oz bucket as good
        //for correct displaying quantity milk jug and bucket, ignore incomplete milk containers
        int nsFoodWeight = getNoSplitFoodWeight(itemStack);
        if (nsFoodWeight > 0 && quantity != nsFoodWeight) {
            quantity = 0;
        }

        String itemKey = ItemHelper.getItemKey(itemStack);

        if(_quantities.containsKey(itemKey))
            quantity += _quantities.get(itemKey);

        _quantities.put(itemKey, quantity);
    }

    public void writeToNBT(NBTTagCompound nbt)
    {
        NBTTagList containerList = new NBTTagList();

        for(int i = 0; i < _containerLocations.size(); i++)
        {
            Point p = _containerLocations.get(i);

            NBTTagCompound pointTag = new NBTTagCompound();
            pointTag.setInteger("X", p.X);
            pointTag.setInteger("Y", p.Y);
            pointTag.setInteger("Z", p.Z);

            containerList.appendTag(pointTag);
        }

        nbt.setTag("Containers", containerList);

        NBTTagList quantityList = new NBTTagList();
        Iterator<Entry<String, Integer>> quantityIterator = _quantities.entrySet().iterator();

        while(quantityIterator.hasNext())
        {
            Entry<String, Integer> qty = quantityIterator.next();

            NBTTagCompound qtyTag = new NBTTagCompound();
            qtyTag.setString("Key", qty.getKey());
            qtyTag.setInteger("Value", qty.getValue());

            quantityList.appendTag(qtyTag);
        }

        nbt.setTag("Quantities", quantityList);
    }

    public void readFromNBT(NBTTagCompound nbt)
    {
        _containerLocations.clear();
        _quantities.clear();

        if(nbt.hasKey("Containers"))
        {
            NBTTagList containerList = nbt.getTagList("Containers", 10);

            for(int i = 0; i < containerList.tagCount(); i++)
            {
                NBTTagCompound containerTag = containerList.getCompoundTagAt(i);
                Point p = new Point(containerTag.getInteger("X"), containerTag.getInteger("Y"), containerTag.getInteger("Z"));

                _containerLocations.add(p);
            }
        }

        if(nbt.hasKey("Quantities"))
        {
            NBTTagList quantityList = nbt.getTagList("Quantities", 10);

            for(int i = 0; i < quantityList.tagCount(); i++)
            {
                NBTTagCompound qtyTag = quantityList.getCompoundTagAt(i);

                _quantities.put(qtyTag.getString("Key"), qtyTag.getInteger("Value"));
            }
        }
    }

    /**
     * For sale to the buyer item from warehouse containet, but not from Stall-Face-Slot (real nbt)
     * compare itemStack From Stall-Face-Slot and Warehouse container at ExtendedLoqic
     * this for barrel with different sealTime and items have craftingTag = Smithing Bonus(TFC tools-armor-weapon)
     */
    public ItemStack getGoodItemStack() {
        return this._goodItemFromWarehouseContainer;
    }
}
