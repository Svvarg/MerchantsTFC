package org.swarg.merchants.tileentities;

import java.util.UUID;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;

import com.aleksey.merchants.Extended.EditPriceSlot;
import com.aleksey.merchants.Extended.ExtendedLogic;

import com.aleksey.merchants.MerchantsMod;
import com.aleksey.merchants.Core.WarehouseBookInfo;
import com.aleksey.merchants.Handlers.GuiHandler;
import com.aleksey.merchants.Helpers.ItemHelper;
import com.aleksey.merchants.Helpers.PrepareTradeResult;
import com.aleksey.merchants.Helpers.WarehouseManager;
import com.aleksey.merchants.Items.ItemWarehouseBook;

import com.bioxx.tfc.Core.Player.PlayerManagerTFC;
import com.bioxx.tfc.TileEntities.NetworkTileEntity;


/**
 * 12-12-2024
 * @author Swarg
 */
public class TileEntityBigStall extends NetworkTileEntity implements ISidedInventory//was IInventory //for dont pull items by hopper
{
    public static final int PriceCount = 5;
    public static final int ItemCount = 2 * PriceCount + 1;

    public static final int[] PricesSlotIndexes = new int[] { 0, 2, 4, 6, 8 };
    public static final int[] GoodsSlotIndexes = new int[] { 1, 3, 5, 7, 9 };

    public static final int[] emptyForHopper = new int[] {};

    private static final byte actionId_ClearPrices = 0;
    private static final byte actionId_Buy = 1;
    private static final byte actionId_SelectLimit = 2;
    private static final byte actionId_SetLimit = 3;
    private static final byte actionId_SelectSetPayItem = 4;
    private static final byte actionId_SetSetPayItem = 5;

    private ItemStack[] storage;
    private WarehouseManager warehouse;
    private int[] limits;
    private int activeGoodSlotIndex;
    private UUID ownerUserID;
    private String ownerUserName;
    private WarehouseBookInfo bookInfo;

    public ItemStack goodItemFromWarehouseContainer;
    public ItemStack payItemFromPlayerInventory;


    public TileEntityBigStall()
    {
        storage = new ItemStack[ItemCount];
        warehouse = new WarehouseManager();

        limits = new int[PriceCount];
    }


    public boolean getIsOwnerSpecified()
    {
        return ownerUserName != null;
    }


    public void setOwner(EntityPlayer player)
    {
        if (player != null) {
            ownerUserID = player.getPersistentID();
            ownerUserName = player.getCommandSenderName();
        }
        else {
            ownerUserID = null;
            ownerUserName = null;
        }

        bookInfo = null;
    }


    public boolean isOwner(EntityPlayer player)
    {
        if (ownerUserName == null) {
            return false;
        }

        return ownerUserID != null
                ? player.getPersistentID().equals(ownerUserID)
                : player.getCommandSenderName().equals(ownerUserName);
    }


    public String getOwnerUserName() {
        return ownerUserName;
    }


    public WarehouseBookInfo getBookInfo() {
        return bookInfo;
    }


    public void calculateQuantitiesInWarehouse()
    {
        if (ownerUserName == null) {
            return;
        }

        ItemStack itemStack = storage[ItemCount - 1];

        if (itemStack != null && itemStack.getItem() instanceof ItemWarehouseBook) {
            bookInfo = WarehouseBookInfo.readFromNBT(itemStack.getTagCompound());

            if (bookInfo != null) {
                if (warehouse.existWarehouse(this.xCoord, this.yCoord, this.zCoord, bookInfo, this.worldObj)) {
                    warehouse.searchContainerLocations(bookInfo, this.worldObj);
                } else {
                    this.bookInfo = null;
                }
            }
        }
        else {
            this.bookInfo = null;
        }

        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }


    public int getQuantityInWarehouse(ItemStack itemStack) {
        return warehouse.getQuantity(itemStack);
    }


    public int getContainersInWarehouse() {
        return warehouse.getContainers();
    }


    public PrepareTradeResult prepareTrade(
        int goodSlotIndex, ItemStack goodStack, ItemStack payStack
    ) {
        if (bookInfo == null || !warehouse.existWarehouse(this.xCoord, this.yCoord, this.zCoord, bookInfo, this.worldObj)) {
            return PrepareTradeResult.NoGoods;
        }

        int limit = getLimitByGoodSlotIndex(goodSlotIndex);

        if (payStack != null
            && limit > 0
            && limit < warehouse.getQuantity(payStack) + ItemHelper.getItemStackQuantity(payStack)
        ) {
            return PrepareTradeResult.NoPays;
        }

        PrepareTradeResult result = bookInfo != null && warehouse.existWarehouse(this.xCoord, this.yCoord, this.zCoord, bookInfo, this.worldObj)
            ? warehouse.prepareTrade(goodStack, payStack, bookInfo, this.worldObj)
            : PrepareTradeResult.NoGoods;

        /*
          For sale to the buyer item from warehouse containet, but not from Stall-Face-Slot (real nbt)
          compare itemStack From Stall-Face-Slot and Warehouse container at ExtendedLoqic
         */
        this.goodItemFromWarehouseContainer = (PrepareTradeResult.Success == result.Success) ?
                warehouse.getGoodItemStack() : null;

        return result;
    }


    public void confirmTrade() {
        warehouse.confirmTrade(this.worldObj);
    }


    public int getLimitByGoodSlotIndex(int goodSlotIndex)
    {
        for (int i = 0; i < GoodsSlotIndexes.length; i++) {
            if (GoodsSlotIndexes[i] == goodSlotIndex) {
                return limits[i];
            }
        }

        return 0;
    }


    public int getActiveGoodSlotIndex()
    {
        return activeGoodSlotIndex;
    }


    public int getActivePriceSlotIndex()
    {
        for (int i = 0; i < GoodsSlotIndexes.length; i++) {
            if (GoodsSlotIndexes[i] == activeGoodSlotIndex) {
                return PricesSlotIndexes[i];
            }
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

        for (int i = 0; i < storage.length; i++) {
            if (storage[i] != null) {
                newMeta = 1;
                break;
            }
        }

        int meta = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord);

        if (meta != newMeta) {
            this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, newMeta, 2);
        }
    }


    @Override
    public ItemStack decrStackSize(int i, int j)
    {
        if (storage[i] != null) {
            if (storage[i].stackSize <= j) {
                ItemStack is = storage[i];
                storage[i] = null;
                return is;
            }

            ItemStack isSplit = storage[i].splitStack(j);

            if (storage[i].stackSize == 0) {
                storage[i] = null;
            }

            return isSplit;
        }

        return null;
    }


    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public String getInventoryName() {
        return "gui.BigStall.Title";
    }

    @Override
    public int getSizeInventory() {
        return ItemCount;
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        return storage[i];
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int i) {
        return storage[i];
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer) {
        return false;
    }

    @Override
    public void openInventory() {
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack is)
    {
        if (!ItemStack.areItemStacksEqual(storage[i], is)) {
            storage[i] = is;
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


    // don`t hopper pull items from TEStall Inventory
    @Override
    public int[] getAccessibleSlotsFromSide(int i) {
        return this.emptyForHopper;
    }

    @Override // for hopper
    public boolean canInsertItem(int a, ItemStack is, int b) {
        return false;
    }

    @Override // for hopper
    public boolean canExtractItem(int a, ItemStack is, int b) {
        return false;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        writeStallToNBT(nbt);

        warehouse.writeToNBT(nbt);

        if (bookInfo != null) {
            NBTTagCompound bookTag = new NBTTagCompound();
            bookInfo.writeToNBT(bookTag);

            nbt.setTag("Book", bookTag);
        }
    }


    public void writeStallToNBT(NBTTagCompound nbt)
    {
        if (ownerUserName != null) {
            nbt.setString("OwnerUserName", ownerUserName);
        }

        if (ownerUserID != null) {
            nbt.setString("OwnerUserID", ownerUserID.toString());
        }

        //Items
        NBTTagList itemList = new NBTTagList();

        for (int i = 0; i < storage.length; i++) {
            if (storage[i] != null) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setByte("Slot", (byte) i);

                storage[i].writeToNBT(itemTag);

                itemList.appendTag(itemTag);
            }
        }

        nbt.setTag("Items", itemList);

        // Limits
        nbt.setIntArray("Limits", limits);

        nbt.setInteger("ActiveGoodSlotIndex", activeGoodSlotIndex);
    }


    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        readStallFromNBT(nbt);

        warehouse.readFromNBT(nbt);

        bookInfo = nbt.hasKey("Book") ? WarehouseBookInfo.readFromNBT(nbt.getCompoundTag("Book")): null;
    }


    public static String readOwnerUserNameFromNBT(NBTTagCompound nbt)
    {
    	return nbt.hasKey("OwnerUserName") ? nbt.getString("OwnerUserName"): null;
    }


    public void readStallFromNBT(NBTTagCompound nbt)
    {
        ownerUserName = readOwnerUserNameFromNBT(nbt);
        ownerUserID = nbt.hasKey("OwnerUserID") ? UUID.fromString(nbt.getString("OwnerUserID")): null;

        NBTTagList itemList = nbt.getTagList("Items", 10);

        for (int i = 0; i < itemList.tagCount(); i++) {
            NBTTagCompound itemTag = itemList.getCompoundTagAt(i);
            byte byte0 = itemTag.getByte("Slot");

            if (byte0 >= 0 && byte0 < storage.length) {
                setInventorySlotContents(byte0, ItemStack.loadItemStackFromNBT(itemTag));
            }
        }

        if (nbt.hasKey("Limits")) {
            limits = nbt.getIntArray("Limits");
        } else {
            for (int i = 0; i < limits.length; i++) {
                limits[i] = 0;
            }
        }

        activeGoodSlotIndex = nbt.hasKey("ActiveGoodSlotIndex") ? nbt.getInteger("ActiveGoodSlotIndex"): 0;
    }


    @Override
    public void handleInitPacket(NBTTagCompound nbt)
    {
    	readStallFromNBT(nbt);

        warehouse.readFromNBT(nbt);

        bookInfo = nbt.hasKey("Book") ? WarehouseBookInfo.readFromNBT(nbt.getCompoundTag("Book")): null;

        this.worldObj.func_147479_m(xCoord, yCoord, zCoord);
    }


    @Override
    public void createInitNBT(NBTTagCompound nbt)
    {
    	writeStallToNBT(nbt);

        warehouse.writeToNBT(nbt);

        if (bookInfo != null) {
            NBTTagCompound bookTag = new NBTTagCompound();
            bookInfo.writeToNBT(bookTag);

            nbt.setTag("Book", bookTag);
        }
    }

    @Override
    public void handleDataPacket(NBTTagCompound nbt)
    {
        if (!nbt.hasKey("Action")) {
            return;
        }

        byte action = nbt.getByte("Action");

        switch (action)
        {
            case actionId_ClearPrices:
                actionHandlerClearPrices();
                break;
            case actionId_Buy:
                actionHandlerBuy(nbt);
                break;
            case actionId_SelectLimit:
                actionHandlerSelectLimit(nbt);
                break;
            case actionId_SetLimit:
                actionHandlerSetLimit(nbt);
                break;
            case actionId_SelectSetPayItem:
                actionHandlerSelectSetPayItem(nbt);
                break;
            case actionId_SetSetPayItem:
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
        nbt.setByte("Action", actionId_ClearPrices);
        this.broadcastPacketInRange(this.createDataPacket(nbt));

        this.worldObj.func_147479_m(xCoord, yCoord, zCoord);
    }

    private void actionHandlerClearPrices()
    {
        for (int i = 0; i < storage.length - 1; i++) {
            storage[i] = null;
        }

        for (int i = 0; i < limits.length; i++) {
            limits[i] = 0;
        }

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
        nbt.setByte("Action", actionId_Buy);
        // здесь у автора ошибка! из-за которой у других игроков отображается купленный товар другого игрока!
        nbt.setString("playerID", PlayerManagerTFC.getInstance().getClientPlayer().playerUUID.toString());

        NBTTagCompound itemTag = new NBTTagCompound();
        itemStack.writeToNBT(itemTag);

        nbt.setTag("Item", itemTag);

        this.broadcastPacketInRange(this.createDataPacket(nbt));

        this.worldObj.func_147479_m(xCoord, yCoord, zCoord);//markBlockForRenderUpdate
    }

    /**
     * Заменено на stall.updateHeldItem(player);
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

    	if (!actionPlayerID.equals(playerID)) {
    		return;
        }

        NBTTagCompound itemTag = nbt.getCompoundTag("Item");
        ItemStack itemStack = ItemStack.loadItemStackFromNBT(itemTag);

        this.entityplayer.inventory.setItemStack(itemStack);
    }


    //Send action from client to server?
    public void actionSelectLimit(int goodSlotIndex)
    {
        activeGoodSlotIndex = goodSlotIndex;

        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("Action", actionId_SelectLimit);

        nbt.setString("playername", PlayerManagerTFC.getInstance().getClientPlayer().playerName);
        nbt.setInteger("GoodSlotIndex", goodSlotIndex);

        this.broadcastPacketInRange(this.createDataPacket(nbt));

        this.worldObj.func_147479_m(xCoord, yCoord, zCoord);
    }


    private void actionHandlerSelectLimit(NBTTagCompound nbt)
    {
        activeGoodSlotIndex = nbt.getInteger("GoodSlotIndex");

        this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);

        EntityPlayer player = worldObj.getPlayerEntityByName(nbt.getString("playername"));

        player.openGui(MerchantsMod.instance, GuiHandler.GuiOwnerBigStallLimit, worldObj, xCoord, yCoord, zCoord);
    }


    // Send action from client to server?
    public void actionSetLimit(int goodSlotIndex, Integer limit)
    {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("Action", actionId_SetLimit);

        nbt.setString("playername", PlayerManagerTFC.getInstance().getClientPlayer().playerName);
        nbt.setInteger("GoodSlotIndex", goodSlotIndex);

        if (limit != null) {
            nbt.setInteger("Limit", limit);
        }

        this.broadcastPacketInRange(this.createDataPacket(nbt));

        this.worldObj.func_147479_m(xCoord, yCoord, zCoord);
    }


    private void actionHandlerSetLimit(NBTTagCompound nbt)
    {
        if (nbt.hasKey("Limit")) {
            int goodSlotIndex = nbt.getInteger("GoodSlotIndex");
            int limit = nbt.getInteger("Limit");

            for (int i = 0; i < GoodsSlotIndexes.length; i++) {
                if (GoodsSlotIndexes[i] == goodSlotIndex) {
                    limits[i] = limit;
                    break;
                }
            }

            this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }

        EntityPlayer player = worldObj.getPlayerEntityByName(nbt.getString("playername"));

        player.openGui(MerchantsMod.instance, GuiHandler.GuiOwnerBigStall, worldObj, xCoord, yCoord, zCoord);
    }



    public void actionSelectSetPayItem (int goodSlotIndex)
    {
        activeGoodSlotIndex = goodSlotIndex;

        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("Action", actionId_SelectSetPayItem );

        nbt.setString("playername", PlayerManagerTFC.getInstance().getClientPlayer().playerName);
        nbt.setInteger("GoodSlotIndex", goodSlotIndex);

        this.broadcastPacketInRange(this.createDataPacket(nbt));

        this.worldObj.func_147479_m(xCoord, yCoord, zCoord);
    }


    public void actionSetSetPayItem(int priceSlotIndex )
    {
        actionSetSetPayItem(priceSlotIndex,0,0,0,0,0,0,0);
    }


    public void actionSetSetPayItem(
        int priceSlotIndex, int id, int meta, int count,
        int param1, int param2, int param3, int param4
    ) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("Action", actionId_SetSetPayItem);

        nbt.setString("playername", PlayerManagerTFC.getInstance().getClientPlayer().playerName);
        nbt.setInteger("PriceSlotIndex", priceSlotIndex);


        if (id != 0 && meta >= 0 && meta < 5000 )
        {
            if (count < 1 || count > 1000 ) {
                count = 1;
            }

            if (!EditPriceSlot.isValidToTFCPayItem(id,meta)) {
                return;
            }

            nbt.setBoolean("CreatePayItem", true);
            nbt.setInteger("id",id);
            nbt.setInteger("meta",meta);
            nbt.setInteger("count",count);

            if (param1 >0) {
                nbt.setInteger("p1",param1);
            }
            if (param2 > 0) {
                nbt.setInteger("p2",param2);
            }
            if (param3 > 0) {
                nbt.setInteger("p3",param3);
            }
            if (param4 > 0) {
                nbt.setInteger("p4",param4);
            }
        }

        this.broadcastPacketInRange(this.createDataPacket(nbt));

        this.worldObj.func_147479_m(xCoord, yCoord, zCoord);
    }


    //sw
    private void actionHandlerSelectSetPayItem (NBTTagCompound nbt)
    {
        activeGoodSlotIndex = nbt.getInteger("GoodSlotIndex");

        this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);

        EntityPlayer player = worldObj.getPlayerEntityByName(nbt.getString("playername"));

        player.openGui(MerchantsMod.instance, GuiHandler.GuiBigStallSetPayItem, worldObj, xCoord, yCoord, zCoord);
    }

    private void actionHandlerSetSetPayItem(NBTTagCompound nbt)
    {
        if (nbt.hasKey("CreatePayItem")) {
            int priceSlotIndex = nbt.getInteger("PriceSlotIndex");

            for (int i = 0; i < PricesSlotIndexes.length; i++) {
                if (PricesSlotIndexes[i] == priceSlotIndex) {
                    ItemStack payStack = EditPriceSlot.createItemStackByParams(nbt);
                    if (priceSlotIndex > -1 && priceSlotIndex < storage.length)
                      storage[priceSlotIndex] = payStack;
                    break;
                }
            }

            this.worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        }

        EntityPlayer player = worldObj.getPlayerEntityByName(nbt.getString("playername"));

        player.openGui(MerchantsMod.instance, GuiHandler.GuiOwnerBigStall, worldObj, xCoord, yCoord, zCoord);
    }



    /**
     * Кинуть в шину форжа событие торгового-обмена
     * @param buyer игрок пытающийся купить-обменять товар в данной лавке
     * @return true - обмен заблокирован для данной сделки!
     */
    public boolean fireTradeEvent(EntityPlayer buyer) {
        if (buyer != null && !buyer.worldObj.isRemote) {
            if (org.swarg.mcf.event.TradeEvent.fireTradeEvent(
                buyer,
                getOwnerUserName(),
                goodItemFromWarehouseContainer,
                payItemFromPlayerInventory)
            ) {
                // если обмен заблокирован неким специальным условием.
                // Например игрок-покупатель в черном списке или враг нации,
                // Вывод сообения о причине блокировки на коде обрабатывающем
                // данное событие!

                //?? насколько нужна эта очистка?
                this.goodItemFromWarehouseContainer = null;
                this.payItemFromPlayerInventory = null;
                return true; // cancelled
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
        if (player instanceof EntityPlayerMP
                && ((EntityPlayerMP)player).playerNetServerHandler != null)
        {
            // sendPacket(new S2FPacketSetSlot(-1, -1, inventoryplayer.getItemStack()));
            ((EntityPlayerMP) player).updateHeldItem();
        }
    }
}
