package org.swarg.merchants.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;

import org.swarg.merchants.containers.ContainerBigStallLimit;
import org.swarg.merchants.tileentities.TileEntityBigStall;

import com.bioxx.tfc.Core.Player.PlayerInventory;
import com.bioxx.tfc.GUI.GuiContainerTFC;

/**
 * 12-12-2024
 * @author Swarg
 */
public class GuiBigStallLimit extends GuiContainerTFC
{
    private static final ResourceLocation _texture =
            new ResourceLocation("merchants", "textures/gui/gui_stall_limit.png");
    private static final RenderItem _itemRenderer = new RenderItem();

    public static final int SlotSize = 18;
    public static final int SlotY = 17;
    public static final int PriceSlotX = 58;
    public static final int GoodSlotX = 102;
    public static final int WindowWidth = 176;
    public static final int WindowHeight = 99;

    private static final int titleX = 0;
    private static final int titleY = 4;
    private static final int limitLabelX = 6;
    private static final int limitLabelY = 43;
    private static final int limitLabelWidth = 47;

    private static final int limitTextFieldX = 58;
    private static final int limitTextFieldY = 38;
    private static final int limitTextFieldWidth = 60;

    private static final int buttonY = 69;
    private static final int applyButtonX = 37;
    private static final int cancelButtonX = 89;

    private static final int buttonId_applyButton = 0;
    private static final int buttonId_cancelButton = 1;

    private static final int colorDefaultText = 0x555555;

    private TileEntityBigStall stall;
    private int priceSlotIndex;
    private int goodSlotIndex;
    private GuiTextField limitTextField;


    public GuiBigStallLimit(
        InventoryPlayer inv, TileEntityBigStall stall, World world, int x, int y, int z
    ) {
        super(new ContainerBigStallLimit(inv, stall, world, x, y, z), WindowWidth, WindowHeight - 1);

        this.stall = stall;
        this.priceSlotIndex = stall.getActivePriceSlotIndex();
        this.goodSlotIndex = stall.getActiveGoodSlotIndex();
    }


    @Override
    public void updateScreen()
    {
        limitTextField.updateCursorCounter();
    }


    @Override
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
    }


    @Override
    public void initGui()
    {
        super.initGui();

        limitTextField = new GuiTextField(fontRendererObj, guiLeft + limitTextFieldX, guiTop + limitTextFieldY, limitTextFieldWidth, 20);
        limitTextField.setFocused(true);

        int limit = stall.getLimitByGoodSlotIndex(goodSlotIndex);

        if (limit > 0) {
            limitTextField.setText(String.valueOf(limit));
        }

        Keyboard.enableRepeatEvents(true);

        this.buttonList.add(new GuiButton(
            buttonId_applyButton,
            guiLeft + applyButtonX,
            guiTop + buttonY,
            50,
            20,
            StatCollector.translateToLocal("gui.StallLimit.Apply")
        ));
        this.buttonList.add(new GuiButton(
            buttonId_cancelButton,
            guiLeft + cancelButtonX,
            guiTop + buttonY,
            50,
            20,
            StatCollector.translateToLocal("gui.StallLimit.Cancel")
        ));
    }


    @Override
    protected void mouseClicked(int par1, int par2, int par3)
    {
        super.mouseClicked(par1, par2, par3);

        limitTextField.mouseClicked(par1, par2, par3);
    }


    @Override
    protected void keyTyped(char key, int par2)
    {
        if (key >= '0' && key <= '9'
            || key == '\u0008' // Backspace
            || key == '\u007F' // Delete
            )
        {
            limitTextField.textboxKeyTyped(key, par2);
        }
        else if (key == 13) {
            applyLimit();
        } else if (key == 27) {
            stall.actionSetLimit(goodSlotIndex, null);
        }
    }


    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        switch (guibutton.id)
        {
            case  buttonId_applyButton:
                applyLimit();
                break;
            case  buttonId_cancelButton:
                stall.actionSetLimit(goodSlotIndex, null);
                break;
        }
    }


    private void applyLimit()
    {
        String limitText = limitTextField.getText();
        int limit;

        try {
            limit = limitText == null || limitText.length() == 0 ? 0: Integer.parseInt(limitText);
        }
        catch (NumberFormatException ex) {
            limit = 0;
        }

        stall.actionSetLimit(goodSlotIndex, limit);
    }


    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY)
    {
        bindTexture(_texture);

        int w = (width - xSize) / 2;
        int h = (height - ySize) / 2;
        int v = 0;

        drawTexturedModalRect(w, h, 0, v, xSize, ySize);

        drawCenteredString(StatCollector.translateToLocal("gui.StallLimit.Title"), w + titleX, h + titleY, WindowWidth, colorDefaultText);
        drawRightAlignedString(StatCollector.translateToLocal("gui.StallLimit.Limit"), w + limitLabelX, h + limitLabelY, limitLabelWidth, colorDefaultText);

        limitTextField.drawTextBox();

        PlayerInventory.drawInventory(this, width, height, ySize - PlayerInventory.invYSize);
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
}
