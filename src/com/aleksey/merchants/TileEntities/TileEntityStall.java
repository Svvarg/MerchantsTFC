package com.aleksey.merchants.TileEntities;

import com.aleksey.merchants.Extended.EditPriceSlot;
import com.aleksey.merchants.Extended.ExtendedLogic;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;

import com.aleksey.merchants.MerchantsMod;
import com.aleksey.merchants.Core.WarehouseBookInfo;
import com.aleksey.merchants.Handlers.GuiHandler;
import com.aleksey.merchants.Helpers.ItemHelper;
import com.aleksey.merchants.Helpers.PrepareTradeResult;
import com.aleksey.merchants.Helpers.WarehouseManager;
import com.aleksey.merchants.Items.ItemWarehouseBook;
import com.bioxx.tfc.Core.Player.PlayerManagerTFC;
import com.bioxx.tfc.TileEntities.NetworkTileEntity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;

public class TileEntityStall extends NetworkTileEntity implements ISidedInventory//was IInventory //for dont pull items by hopper
{
    public static final int PriceCount = 5;
    public static final int ItemCount = 2 * PriceCount + 1;

    public static final int[] PricesSlotIndexes = new int[] { 0, 2, 4, 6, 8 };
    public static final int[] GoodsSlotIndexes = new int[] { 1, 3, 5, 7, 9 };

    public static final int[] emptyForHopper = new int[] {};

    private static final byte _actionId_ClearPrices = 0;
    private static final byte _actionId_Buy = 1;
    private static final byte _actionId_SelectLimit = 2;
    private static final byte _actionId_SetLimit = 3;
    private static final byte _actionId_SelectSetPayItem = 4;//
    private static final byte _actionId_SetSetPayItem = 5;//

    private ItemStack[] _storage;
    private WarehouseManager _warehouse;
    private int[] _limits;
    private int _activeGoodSlotIndex;
    private UUID _ownerUserID;
    private String _ownerUserName;
    private WarehouseBookInfo _bookInfo;

    public ItemStack _goodItemFromWarehouseContainer;
    public ItemStack _payItemFromPlayerInventory;

    public TileEntityStall()
    {
        _storage = new ItemStack[ItemCount];
        _warehouse = new WarehouseManager();

        _limits = new int[PriceCount];
    }

    public boolean getIsOwnerSpecified()
    {
        return _ownerUserName != null;
    }

    public void setOwner(EntityPlayer player)
    {
        if(player != null)
        {
            _ownerUserID = player.getPersistentID();
            _ownerUserName = player.getCommandSenderName();
        }
        else
        {
            _ownerUserID = null;
            _ownerUserName = null;
        }

        _bookInfo = null;
    }

    public boolean isOwner(EntityPlayer player)
    {
        if(_ownerUserName == null)
            return false;

        return _ownerUserID != null
                ? player.getPersistentID().equals(_ownerUserID)
                : player.getCommandSenderName().equals(_ownerUserName);
    }

    public String getOwnerUserName()
    {
        return _ownerUserName;
    }

    public WarehouseBookInfo getBookInfo()
    {
        return _bookInfo;
    }

    public void calculateQuantitiesInWarehouse()
    {
        if(_ownerUserName == null)
            return;

        ItemStack itemStack = _storage[ItemCount - 1];

        if(itemStack != null && itemStack.getItem() instanceof ItemWarehouseBook)
        {
            _bookInfo = WarehouseBookInfo.readFromNBT(itemStack.getTagCompound());

            if(_bookInfo != null)
            {
                if(_warehouse.existWarehouse(this.xCoord, this.yCoord, this.zCoord, _bookInfo, this.worldObj))
                    _warehouse.searchContainerLocations(_bookInfo, this.worldObj);
                else
                    _bookInfo = null;
            }
        }
        else
            _bookInfo = null;

        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }

    public int getQuantityInWarehouse(ItemStack itemStack)
    {
        return _warehouse.getQuantity(itemStack);
    }

    public int getContainersInWarehouse()
    {
        return _warehouse.getContainers();
    }

    public PrepareTradeResult prepareTrade(int goodSlotIndex, ItemStack goodStack, ItemStack payStack)
    {
        if(_bookInfo == null || !_warehouse.existWarehouse(this.xCoord, this.yCoord, this.zCoord, _bookInfo, this.worldObj))
            return PrepareTradeResult.NoGoods;

        int limit = getLimitByGoodSlotIndex(goodSlotIndex);

        if(payStack != null
            && limit > 0
            && limit < _warehouse.getQuantity(payStack) + ItemHelper.getItemStackQuantity(payStack)
            )
        {
            return PrepareTradeResult.NoPays;
        }

        PrepareTradeResult result = _bookInfo != null && _warehouse.existWarehouse(this.xCoord, this.yCoord, this.zCoord, _bookInfo, this.worldObj)
            ? _warehouse.prepareTrade(goodStack, payStack, _bookInfo, this.worldObj)
            : PrepareTradeResult.NoGoods;

        /*
          For sale to the buyer item from warehouse containet, but not from Stall-Face-Slot (real nbt)
          compare itemStack From Stall-Face-Slot and Warehouse container at ExtendedLoqic
         */
        this._goodItemFromWarehouseContainer = (PrepareTradeResult.Success == result.Success) ?
                _warehouse.getGoodItemStack() : null;

        return result;
    }

    public void confirmTrade()
    {
        _warehouse.confirmTrade(this.worldObj);
    }

    public int getLimitByGoodSlotIndex(int goodSlotIndex)
    {
        for(int i = 0; i < GoodsSlotIndexes.length; i++)
        {
            if(GoodsSlotIndexes[i] == goodSlotIndex)
                return _limits[i];
        }

        return 0;
    }

    public int getActiveGoodSlotIndex()
    {
        return _activeGoodSlotIndex;
    }

    public int getActivePriceSlotIndex()
    {
        for(int i = 0; i < GoodsSlotIndexes.length; i++)
        {
            if(GoodsSlotIndexes[i] == _activeGoodSlotIndex)
                return PricesSlotIndexes[i];
        }

        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox()
    {
        AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
        return bb;
    }

    @Override
    public void closeInventory()
    {
        int newMeta = 0;

        for(int i = 0; i < _storage.length; i++)
        {
            if(_storage[i] != null)
            {
                newMeta = 1;
                break;
            }
        }

        int meta = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);

        if(meta != newMeta)
            this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, newMeta, 2);
    }

    @Override
    public ItemStack decrStackSize(int i, int j)
    {
        if (_storage[i] != null)
        {
            if (_storage[i].stackSize <= j)
            {
                ItemStack is = _storage[i];
                _storage[i] = null;
                return is;
            }

            ItemStack isSplit = _storage[i].splitStack(j);

            if (_storage[i].stackSize == 0)
                _storage[i] = null;

            return isSplit;
        }
        else
        {
            return null;
        }
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public String getInventoryName()
    {
        return "gui.Stall.Title";
    }

    @Override
    public int getSizeInventory()
    {
        return ItemCount;
    }

    @Override
    public ItemStack getStackInSlot(int i)
    {
        return _storage[i];
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int i)
    {
        return _storage[i];
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer)
    {
        return false;
    }

    @Override
    public void openInventory()
    {
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack is)
    {
        if (!ItemStack.areItemStacksEqual(_storage[i], is))
        {
            _storage[i] = is;
        }
    }

    @Override
    public boolean hasCustomInventoryName()
    {
        return false;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack)
    {
        return false;
    }


    //don`t hopper pull items from TEStall Inventory
    @Override
    public int[] getAccessibleSlotsFromSide(int p_94128_1_)
    {
        return this.emptyForHopper;
    }

    @Override//for hopper
    public boolean canInsertItem(int p_102007_1_, ItemStack p_102007_2_, int p_102007_3_)
    {
        return false;
    }

    @Override//for hopper
    public boolean canExtractItem(int p_102008_1_, ItemStack p_102008_2_, int p_102008_3_)
    {
        return false;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        writeStallToNBT(nbt);

        _warehouse.writeToNBT(nbt);

        if(_bookInfo != null)
        {
            NBTTagCompound bookTag = new NBTTagCompound();
            _bookInfo.writeToNBT(bookTag);

            nbt.setTag("Book", bookTag);
        }
    }

    public void writeStallToNBT(NBTTagCompound nbt)
    {
        if(_ownerUserName != null)
            nbt.setString("OwnerUserName", _ownerUserName);

        if(_ownerUserID != null)
            nbt.setString("OwnerUserID", _ownerUserID.toString());

        //Items
        NBTTagList itemList = new NBTTagList();

        for (int i = 0; i < _storage.length; i++)
        {
            if (_storage[i] != null)
            {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setByte("Slot", (byte) i);

                _storage[i].writeToNBT(itemTag);

                itemList.appendTag(itemTag);
            }
        }

        nbt.setTag("Items", itemList);

        //Limits
        nbt.setIntArray("Limits", _limits);

        nbt.setInteger("ActiveGoodSlotIndex", _activeGoodSlotIndex);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        readStallFromNBT(nbt);

        _warehouse.readFromNBT(nbt);

        _bookInfo = nbt.hasKey("Book") ? WarehouseBookInfo.readFromNBT(nbt.getCompoundTag("Book")): null;
    }

    public static String readOwnerUserNameFromNBT(NBTTagCompound nbt)
    {
    	return nbt.hasKey("OwnerUserName") ? nbt.getString("OwnerUserName"): null;
    }

    public void readStallFromNBT(NBTTagCompound nbt)
    {
        _ownerUserName = readOwnerUserNameFromNBT(nbt);
        _ownerUserID = nbt.hasKey("OwnerUserID") ? UUID.fromString(nbt.getString("OwnerUserID")): null;

        NBTTagList itemList = nbt.getTagList("Items", 10);

        for (int i = 0; i < itemList.tagCount(); i++)
        {
            NBTTagCompound itemTag = itemList.getCompoundTagAt(i);
            byte byte0 = itemTag.getByte("Slot");

            if (byte0 >= 0 && byte0 < _storage.length)
                setInventorySlotContents(byte0, ItemStack.loadItemStackFromNBT(itemTag));
        }

        if(nbt.hasKey("Limits"))
        {
            _limits = nbt.getIntArray("Limits");
        }
        else
        {
            for(int i = 0; i < _limits.length; i++)
                _limits[i] = 0;
        }

        _activeGoodSlotIndex = nbt.hasKey("ActiveGoodSlotIndex") ? nbt.getInteger("ActiveGoodSlotIndex"): 0;
    }

    @Override
    public void handleInitPacket(NBTTagCompound nbt)
    {
    	readStallFromNBT(nbt);

        _warehouse.readFromNBT(nbt);

        _bookInfo = nbt.hasKey("Book") ? WarehouseBookInfo.readFromNBT(nbt.getCompoundTag("Book")): null;

        this.worldObj.func_147479_m(xCoord, yCoord, zCoord);
    }

    @Override
    public void createInitNBT(NBTTagCompound nbt)
    {
    	writeStallToNBT(nbt);

        _warehouse.writeToNBT(nbt);

        if(_bookInfo != null)
        {
            NBTTagCompound bookTag = new NBTTagCompound();
            _bookInfo.writeToNBT(bookTag);

            nbt.setTag("Book", bookTag);
        }
    }

    @Override
    public void handleDataPacket(NBTTagCompound nbt)
    {
        if (!nbt.hasKey("Action"))
            return;

        byte action = nbt.getByte("Action");

        switch (action)
        {
            case _actionId_ClearPrices:
                actionHandlerClearPrices();
                break;
            case _actionId_Buy:
                actionHandlerBuy(nbt);
                break;
            case _actionId_SelectLimit:
                actionHandlerSelectLimit(nbt);
                break;
            case _actionId_SetLimit:
                actionHandlerSetLimit(nbt);
                break;
            case _actionId_SelectSetPayItem:
                actionHandlerSelectSetPayItem(nbt);
                break;
            case _actionId_SetSetPayItem:
                actionHandlerSetSetPayItem(nbt);
                break;

        }
    }

    @Override
    public void updateEntity()
    {
    }

    //Send action to server
    public void actionClearPrices()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("Action", _actionId_ClearPrices);
        this.broadcastPacketInRange(this.createDataPacket(nbt));

        this.worldObj.func_147479_m(xCoord, yCoord, zCoord);
    }

    private void actionHandlerClearPrices()
    {
        for (int i = 0; i < _storage.length - 1; i++)
            _storage[i] = null;

        for(int i = 0; i < _limits.length; i++)
            _limits[i] = 0;

        this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    /**
     * Send action to client
     * Похоже автор этим методом хотел обновлять на клиенте содержимое курсора
     * помещая туда купленный товар. Но данный метод не работает как надо
     * из-за getClientPlayer() - Здесь нужно было передавать инстанс конкретного
     * игрока + Этота пара методов с actionHandlerBuy избыточны. Задачу обновления
     * предмета в курсоре легко можно решить через стандартный метод
     * ((EntityPlayerMP)p).updateHeldItem(); // sendPacket(new S2FPacketSetSlot...
     * @param itemStack
     * @deprecated
     */
    @Deprecated
    public void actionBuy(ItemStack itemStack)
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("Action", _actionId_Buy);
        //здесь у автора ошибка! из-за которой у других игроков отображается купленный товар другого игрока!
        nbt.setString("playerID", PlayerManagerTFC.getInstance().getClientPlayer().playerUUID.toString());

        NBTTagCompound itemTag = new NBTTagCompound();
        itemStack.writeToNBT(itemTag);

        nbt.setTag("Item", itemTag);

        this.broadcastPacketInRange(this.createDataPacket(nbt));

        this.worldObj.func_147479_m(xCoord, yCoord, zCoord);//markBlockForRenderUpdate
    }

    /**
     * Заменено на _stall.updateHeldItem(player);
     * В оригинале по задумке автора этот хэндл должен обновлять содержимое
     * курсора игрока после покупки
     * @param nbt
     * @deprecated
     */
    @Deprecated
    private void actionHandlerBuy(NBTTagCompound nbt)
    {
    	UUID actionPlayerID = UUID.fromString(nbt.getString("playerID"));
    	UUID playerID = PlayerManagerTFC.getInstance().getPlayerInfoFromPlayer(this.entityplayer).playerUUID;

    	if(!actionPlayerID.equals(playerID))
    		return;

        NBTTagCompound itemTag = nbt.getCompoundTag("Item");
        ItemStack itemStack = ItemStack.loadItemStackFromNBT(itemTag);

        this.entityplayer.inventory.setItemStack(itemStack);
    }

    //Send action from client to server?
    public void actionSelectLimit(int goodSlotIndex)
    {
        _activeGoodSlotIndex = goodSlotIndex;

        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("Action", _actionId_SelectLimit);

        nbt.setString("playername", PlayerManagerTFC.getInstance().getClientPlayer().playerName);
        nbt.setInteger("GoodSlotIndex", goodSlotIndex);

        this.broadcastPacketInRange(this.createDataPacket(nbt));

        this.worldObj.func_147479_m(xCoord, yCoord, zCoord);
    }

    private void actionHandlerSelectLimit(NBTTagCompound nbt)
    {
        _activeGoodSlotIndex = nbt.getInteger("GoodSlotIndex");

        this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);

        EntityPlayer player = worldObj.getPlayerEntityByName(nbt.getString("playername"));

        player.openGui(MerchantsMod.instance, GuiHandler.GuiOwnerStallLimit, worldObj, xCoord, yCoord, zCoord);
    }

    //Send action from client to server?
    public void actionSetLimit(int goodSlotIndex, Integer limit)
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("Action", _actionId_SetLimit);

        nbt.setString("playername", PlayerManagerTFC.getInstance().getClientPlayer().playerName);
        nbt.setInteger("GoodSlotIndex", goodSlotIndex);

        if(limit != null)
            nbt.setInteger("Limit", limit);

        this.broadcastPacketInRange(this.createDataPacket(nbt));

        this.worldObj.func_147479_m(xCoord, yCoord, zCoord);
    }

    private void actionHandlerSetLimit(NBTTagCompound nbt)
    {
        if(nbt.hasKey("Limit"))
        {
            int goodSlotIndex = nbt.getInteger("GoodSlotIndex");
            int limit = nbt.getInteger("Limit");

            for(int i = 0; i < GoodsSlotIndexes.length; i++)
            {
                if(GoodsSlotIndexes[i] == goodSlotIndex)
                {
                    _limits[i] = limit;
                    break;
                }
            }

            this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }

        EntityPlayer player = worldObj.getPlayerEntityByName(nbt.getString("playername"));

        player.openGui(MerchantsMod.instance, GuiHandler.GuiOwnerStall, worldObj, xCoord, yCoord, zCoord);
    }



    public void actionSelectSetPayItem (int goodSlotIndex)
    {
        _activeGoodSlotIndex = goodSlotIndex;

        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("Action", _actionId_SelectSetPayItem );

        nbt.setString("playername", PlayerManagerTFC.getInstance().getClientPlayer().playerName);
        nbt.setInteger("GoodSlotIndex", goodSlotIndex);

        this.broadcastPacketInRange(this.createDataPacket(nbt));

        this.worldObj.func_147479_m(xCoord, yCoord, zCoord);
    }

    public void actionSetSetPayItem(int priceSlotIndex )
    {
        actionSetSetPayItem(priceSlotIndex,0,0,0,0,0,0,0);
    }

    public void actionSetSetPayItem(int priceSlotIndex, int id, int meta, int count, int param1, int param2, int param3, int param4 )
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("Action", _actionId_SetSetPayItem);

        nbt.setString("playername", PlayerManagerTFC.getInstance().getClientPlayer().playerName);
        nbt.setInteger("PriceSlotIndex", priceSlotIndex);


        if (id != 0 && meta >= 0 && meta < 5000 )
        {
            if (  count < 1 || count > 1000 )
                count = 1;

            if (!EditPriceSlot.isValidToTFCPayItem(id,meta))
                return;

            nbt.setBoolean("CreatePayItem", true);
            nbt.setInteger("id",id);
            nbt.setInteger("meta",meta);
            nbt.setInteger("count",count);

            if (param1>0)
                nbt.setInteger("p1",param1);
            if (param2>0)
                nbt.setInteger("p2",param2);
            if (param3>0)
                nbt.setInteger("p3",param3);
            if (param4>0)
                nbt.setInteger("p4",param4);
        }

        this.broadcastPacketInRange(this.createDataPacket(nbt));

        this.worldObj.func_147479_m(xCoord, yCoord, zCoord);
    }


    //sw
    private void actionHandlerSelectSetPayItem (NBTTagCompound nbt)
    {
        _activeGoodSlotIndex = nbt.getInteger("GoodSlotIndex");

        this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);

        EntityPlayer player = worldObj.getPlayerEntityByName(nbt.getString("playername"));

        player.openGui(MerchantsMod.instance, GuiHandler.GuiStallSetPayItem, worldObj, xCoord, yCoord, zCoord);
    }

    private void actionHandlerSetSetPayItem(NBTTagCompound nbt)
    {
        if(nbt.hasKey("CreatePayItem"))
        {
            int priceSlotIndex = nbt.getInteger("PriceSlotIndex");

            for(int i = 0; i < PricesSlotIndexes.length; i++)
            {
                if(PricesSlotIndexes[i] == priceSlotIndex)
                {
                    ItemStack payStack = EditPriceSlot.createItemStackByParams(nbt);
                    if (priceSlotIndex>-1 && priceSlotIndex < _storage.length)
                      _storage[priceSlotIndex] = payStack;
                    break;
                }
            }

            this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }

        EntityPlayer player = worldObj.getPlayerEntityByName(nbt.getString("playername"));

        player.openGui(MerchantsMod.instance, GuiHandler.GuiOwnerStall, worldObj, xCoord, yCoord, zCoord);
    }



    /**
     * Кинуть в шину форжа событие торгового-обмена
     * @param buyer игрок пытающийся купить-обменять товар в данной лавке
     * @return true - обмен заблокирован для данной сделки!
     */
    public boolean fireTradeEvent(EntityPlayer buyer) {
        if (buyer != null && !buyer.worldObj.isRemote) {
            if (org.swarg.mcf.event.TradeEvent.fireTradeEvent(buyer, getOwnerUserName(), _goodItemFromWarehouseContainer, _payItemFromPlayerInventory)) {
                //если обмен заблокирован неким специальным условием. Например игрок-покупатель в черном списке или враг нации, Вывод сообения о причине блокировке на коде обрабатывающем данное событие!
                //?? насколько нужна эта очистка?
                this._goodItemFromWarehouseContainer = null;
                this._payItemFromPlayerInventory = null;
                return true;//cancelled
            }
        }
        return false;
    }

    /**
     * [ServerSide] отослать пакет обновления предмета в курсоре контейнера
     * конкретному игроку. (Замена избыточной пары методов actionBuy и actionHandlerBuy)
     * @param player
     */
    public void updateHeldItem(EntityPlayer player) {
        if (player instanceof EntityPlayerMP && ((EntityPlayerMP)player).playerNetServerHandler != null) {
            ((EntityPlayerMP) player).updateHeldItem();//sendPacket(new S2FPacketSetSlot(-1, -1, inventoryplayer.getItemStack()));
        }
    }
}
