package org.swarg.merchants.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import com.aleksey.merchants.MerchantsMod;
import com.aleksey.merchants.Core.WarehouseBookInfo;
import com.aleksey.merchants.GUI.Buttons.GuiLimitButton;
import com.aleksey.merchants.GUI.Buttons.GuiSetPayButton;
import com.aleksey.merchants.Helpers.ItemHelper;

import org.swarg.merchants.containers.ContainerBigStall;
import org.swarg.merchants.tileentities.TileEntityBigStall;

import com.bioxx.tfc.TerraFirmaCraft;
import com.bioxx.tfc.Core.Player.PlayerInventory;
import com.bioxx.tfc.GUI.GuiContainerTFC;

/**
 * 12-12-2024
 * @author Swarg
 */
public class GuiBigStall extends GuiContainerTFC
{
    private class QuantityInfo
    {
        public int Quantity;
        public int Color;
        public String ToolTip;
    }

    private static final ResourceLocation texture =
            new ResourceLocation("merchants", "textures/gui/gui_stall.png");

    public static final int SlotSize = 18;
    public static final int WindowWidth = 176;
    public static final int WindowHeight = 127;

    public static final int TopSlotY = 32;
    public static final int PricesSlotX = 18;
    public static final int GoodsSlotX = 62;
    public static final int BookSlotX = 127;
    public static final int BookSlotY = 32;

    private static final int titleX = 0;
    private static final int titleY = 4;
    private static final int pricesTitleX = 9;
    private static final int pricesTitleY = 17;
    private static final int goodsTitleX = 50;
    private static final int goodsTitleY = 17;
    private static final int columnTitleWidth = 40;
    private static final int warehouseTitleX = 97;
    private static final int warehouseTitleY = 17;
    private static final int columnWarehouseWidth = 76;
    private static final int warehouseCoordsX = 97;
    private static final int warehouseCoordsY = 52;
    private static final int clearButtonX = 110;
    private static final int clearButtonY = 102;
    private static final int quantityX = 81;
    private static final int setPayItemX = 9;


    private static final int buttonId_clearButton = 0;
    private static final int buttonId_firstLimitButton = 1;
    private static final int buttonId_setPayItemButton = 100;


    private static final int colorDefaultText = 0x555555;
    private static final int colorSuccessText = 0x00AA00;
    private static final int colorFailedText = 0xAA0000;

    private TileEntityBigStall stall;
    private EntityPlayer player;
    private boolean isOwnerMode;
    private QuantityInfo[] quantities;
    private GuiLimitButton[] limitButtons;
    private GuiSetPayButton[] setPayButtons;

    /**
     *
     */
    public GuiBigStall(InventoryPlayer inv, TileEntityBigStall stall,
        boolean isOwnerMode, World world, int x, int y, int z
    ) {
        super(new ContainerBigStall(inv, stall, isOwnerMode, world, x, y, z),
            WindowWidth, WindowHeight - 1
        );

        this.stall = stall;
        this.player = inv.player;
        this.isOwnerMode = isOwnerMode;
    }


    @Override
    public void initGui()
    {
        super.initGui();

        if (!isOwnerMode) {
            return;
        }

        buttonList.add(new GuiButton(buttonId_clearButton,
            guiLeft + clearButtonX, guiTop + clearButtonY, 50, 20,
            StatCollector.translateToLocal("gui.Stall.Clear")
        ));

        int y = guiTop + TopSlotY;

        limitButtons = new GuiLimitButton[stall.GoodsSlotIndexes.length];
        setPayButtons = new GuiSetPayButton[stall.GoodsSlotIndexes.length];

        for (int i = 0; i < limitButtons.length; i++)
        {
            buttonList.add(limitButtons[i] = new GuiLimitButton(buttonId_firstLimitButton + i, guiLeft + quantityX, y));

            buttonList.add( setPayButtons[i] = new GuiSetPayButton(buttonId_setPayItemButton + i, guiLeft + setPayItemX, y ));
            // pricesTitleX
            y += SlotSize;
        }
    }


    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        if (guibutton.id == buttonId_clearButton)
        {
            stall.actionClearPrices();
        }
        else if (guibutton.id >= buttonId_firstLimitButton && guibutton.id< buttonId_setPayItemButton)
        {
            stall.actionSelectLimit(stall.GoodsSlotIndexes[guibutton.id - buttonId_firstLimitButton]);
        }
        else if (guibutton.id >= buttonId_setPayItemButton)
        {
            //call SetPayItem
            int index = guibutton.id - buttonId_setPayItemButton;
            if (index>-1 && index < stall.GoodsSlotIndexes.length) {
                stall.actionSelectSetPayItem(stall.GoodsSlotIndexes[index]);
            }
        }
    }


    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        QuantityInfo info = getQuantityInfo(mouseX, mouseY);

        if (info != null) {
            drawTooltip(mouseX - this.guiLeft, mouseY - this.guiTop, info.ToolTip);
        } else {
            String limitTooltip = getLimitTooltip(mouseX, mouseY);

            if (limitTooltip != null) {
                drawTooltip(mouseX - this.guiLeft, mouseY - this.guiTop, limitTooltip);
            }

            String setPayTooltip = getSetPayItemTooltip(mouseX, mouseY);

            if (setPayTooltip != null) {
                drawTooltip(mouseX - this.guiLeft, mouseY - this.guiTop, setPayTooltip);
            }
        }
    }


    private QuantityInfo getQuantityInfo(int mouseX, int mouseY)
    {
        if (!getQuantities()) {
            return null;
        }

        int w = (width - xSize) / 2;
        int h = (height - ySize) / 2;

        if (mouseX < w + quantityX) {
            return null;
        }

        int y = h + TopSlotY + SlotSize - this.fontRendererObj.FONT_HEIGHT;

        for (int i = 0; i < quantities.length; i++)
        {
            if (mouseY < y) {
                return null;
            }

            if (mouseY < y + 7) {
                QuantityInfo info = quantities[i];

                if (info == null) {
                    return null;
                }

                int textWidth = this.fontRendererObj.getStringWidth(String.valueOf(info.Quantity));

                return mouseX < w + quantityX + textWidth ? info: null;
            }

            y += SlotSize;
        }

        return null;
    }


    private String getLimitTooltip(int mouseX, int mouseY)
    {
        if (!isOwnerMode) {
            return null;
        }

        int w = (width - xSize) / 2;
        int h = (height - ySize) / 2;

        if (mouseX < w + quantityX) {
            return null;
        }

        int y = h + TopSlotY;

        for (int i = 0; i < limitButtons.length; i++)
        {
            if (mouseY < y) {
                return null;
            }

            if (mouseY < y + 7)
            {
                int limit = stall.getLimitByGoodSlotIndex(stall.GoodsSlotIndexes[i]);
                String limitText = limit > 0 ? String.valueOf(limit) : StatCollector.translateToLocal("gui.Stall.NA");

                int textWidth = this.fontRendererObj.getStringWidth(limitText);

                return mouseX < w + quantityX + textWidth ? StatCollector.translateToLocal("gui.Stall.Tooltip.LimitButton"): null;
            }

            y += SlotSize;
        }

        return null;
    }


    private String getSetPayItemTooltip(int mouseX, int mouseY)
    {
        if (!isOwnerMode) {
            return null;
        }

        int w = (width - xSize) / 2;
        int h = (height - ySize) / 2;

        if (mouseX < w + setPayItemX || mouseX > w + setPayItemX + GuiSetPayButton._setPayItemWeight) {
            return null;
        }

        int y = h + TopSlotY;

        for (int i = 0; i < setPayButtons.length; i++) {
            if (mouseY < y) {
                return null;
            }

            if (mouseY < y + 7) {
                //int limit = _stall.getLimitByGoodSlotIndex(_stall.GoodsSlotIndexes[i]);
                //String limitText = limit > 0 ? String.valueOf(limit): StatCollector.translateToLocal("gui.Stall.NA");
                String toolTip = StatCollector.translateToLocal("gui.Stall.Tooltip.SetPayButton");
                int textWidth = this.fontRendererObj.getStringWidth(toolTip);

                return mouseX < w + setPayItemX + textWidth ? StatCollector.translateToLocal("gui.Stall.Tooltip.SetPayButton"): null;
            }

            y += SlotSize;
        }

        return null;
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY)
    {
        resetQuantities();

        bindTexture(texture);

        int w = (width - xSize) / 2;
        int h = (height - ySize) / 2;
        int v = isOwnerMode ? 0: WindowHeight;

        drawTexturedModalRect(w, h, 0, v, xSize, ySize);
        String inventoryName = StatCollector.translateToLocal(stall.getInventoryName());
        String title = !stall.getIsOwnerSpecified() ? inventoryName : inventoryName + " (" + stall.getOwnerUserName() + ")";

        drawCenteredString(title, w + titleX, h + titleY, WindowWidth, colorDefaultText);
        drawCenteredString(StatCollector.translateToLocal("gui.Stall.Prices"), w + pricesTitleX, h + pricesTitleY, columnTitleWidth, colorDefaultText);
        drawCenteredString(StatCollector.translateToLocal("gui.Stall.Goods"), w + goodsTitleX, h + goodsTitleY, columnTitleWidth, colorDefaultText);

        drawWarehouseText(w, h);
        drawQuantities(w, h);

        if (isOwnerMode) {
            for (int i = 0; i < stall.GoodsSlotIndexes.length; i++) {
                int limit = stall.getLimitByGoodSlotIndex(stall.GoodsSlotIndexes[i]);
                String limitText = limit > 0 ? String.valueOf(limit): StatCollector.translateToLocal("gui.Stall.NA");

                limitButtons[i].setText(limitText, this.fontRendererObj);
            }
        }

        PlayerInventory.drawInventory(this, width, height, ySize - PlayerInventory.invYSize);
    }


    private void drawWarehouseText(int w, int h)
    {
        if (!isOwnerMode) {
            return;
        }

        drawCenteredString(StatCollector.translateToLocal("gui.Stall.Warehouse"), w + warehouseTitleX, h + warehouseTitleY, columnWarehouseWidth, colorDefaultText);

        if (!stall.getIsOwnerSpecified()) {
            return;
        }

        WarehouseBookInfo info = stall.getBookInfo();

        if (info != null)
        {
            String coordXText = String.valueOf(info.X);
            String coordYText = String.valueOf(info.Y);
            String coordZText = String.valueOf(info.Z);

            int coordXTextWidth = this.fontRendererObj.getStringWidth("X: " + coordXText);
            int coordYTextWidth = this.fontRendererObj.getStringWidth("Y: " + coordYText);
            int coordZTextWidth = this.fontRendererObj.getStringWidth("Z: " + coordZText);
            int coordTextWidth = coordXTextWidth;

            if (coordTextWidth < coordYTextWidth)
                coordTextWidth = coordYTextWidth;

            if (coordTextWidth < coordZTextWidth)
                coordTextWidth = coordZTextWidth;

            int x = w + warehouseCoordsX + (columnWarehouseWidth - coordTextWidth) / 2;
            int y1 = h + warehouseCoordsY;
            int y2 = y1 + this.fontRendererObj.FONT_HEIGHT;
            int y3 = y2 + this.fontRendererObj.FONT_HEIGHT;

            fontRendererObj.drawString("X: ", x, y1, colorDefaultText);
            drawRightAlignedString(coordXText, x, y1, coordTextWidth, colorDefaultText);
            fontRendererObj.drawString("Y: ", x, y2, colorDefaultText);
            drawRightAlignedString(coordYText, x, y2, coordTextWidth, colorDefaultText);
            fontRendererObj.drawString("Z: ", x, y3, colorDefaultText);
            drawRightAlignedString(coordZText, x, y3, coordTextWidth, colorDefaultText);

            //fontRendererObj.drawString("C: " + String.valueOf(_stall.getContainersInWarehouse()), x, y3 + this.fontRendererObj.FONT_HEIGHT, _colorDefaultText);
        }
        else
        {
            drawCenteredString(StatCollector.translateToLocal("gui.BigStall.NoWarehouse"), w + warehouseCoordsX, h + warehouseCoordsY, columnWarehouseWidth, colorFailedText);
        }
    }

    private void drawQuantities(int w, int h)
    {
        if (!getQuantities()) {
            return;
        }

        int y = TopSlotY + SlotSize - this.fontRendererObj.FONT_HEIGHT;

        for (int i = 0; i < quantities.length; i++)
        {
            QuantityInfo info = quantities[i];

            if (info != null)
                fontRendererObj.drawString(String.valueOf(info.Quantity), w + quantityX, h + y, info.Color);

            y += SlotSize;
        }
    }

    private void drawCenteredString(String s, int x, int y, int columnWidth, int color)
    {
        int offset = (columnWidth - this.fontRendererObj.getStringWidth(s)) / 2;

        fontRendererObj.drawString(s, x + offset, y, color);
    }

    private void drawRightAlignedString(String s, int x, int y, int columnWidth, int color)
    {
        int offset = columnWidth - this.fontRendererObj.getStringWidth(s);

        fontRendererObj.drawString(s, x + offset, y, color);
    }

    private void resetQuantities()
    {
        quantities = null;
    }

    private boolean getQuantities()
    {
        if (stall.getBookInfo() == null)
        {
            quantities = null;
            return false;
        }

        if (quantities != null) {
            return true;
        }

        quantities = new QuantityInfo[TileEntityBigStall.GoodsSlotIndexes.length];

        for (int i = 0; i < TileEntityBigStall.GoodsSlotIndexes.length; i++)
        {
            int goodSlotIndex = TileEntityBigStall.GoodsSlotIndexes[i];
            ItemStack goodItemStack = stall.getStackInSlot(goodSlotIndex);

            if (goodItemStack == null) {
                continue;
            }

            int priceSlotIndex = TileEntityBigStall.PricesSlotIndexes[i];
            ItemStack priceItemStack = stall.getStackInSlot(priceSlotIndex);
            int limit = stall.getLimitByGoodSlotIndex(goodSlotIndex);

            QuantityInfo info = new QuantityInfo();
            info.Quantity = stall.getQuantityInWarehouse(goodItemStack);

            if (info.Quantity < ItemHelper.getItemStackQuantity(goodItemStack)) {
                info.Color = colorFailedText;
                info.ToolTip = StatCollector.translateToLocal("gui.Stall.Tooltip.NoGoods");
            }
            else if (priceItemStack != null
                    && limit > 0
                    && limit < stall.getQuantityInWarehouse(priceItemStack) + ItemHelper.getItemStackQuantity(priceItemStack)
                    )
            {
                info.Color = colorFailedText;
                info.ToolTip = StatCollector.translateToLocal("gui.Stall.Tooltip.NoPaysSpace");
            }
            else
            {
                info.Color = colorSuccessText;
                info.ToolTip = StatCollector.translateToLocal("gui.Stall.Tooltip.CanBuy");
            }

            quantities[i] = info;
        }

        return true;
    }
}
