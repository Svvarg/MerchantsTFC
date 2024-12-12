package org.swarg.merchants.gui;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import com.aleksey.merchants.Extended.AnimalInCrate;
import com.aleksey.merchants.Extended.EditPayParams;
import com.aleksey.merchants.Extended.EditPriceSlot;
import com.aleksey.merchants.Extended.ExtendedLogic;

import org.swarg.merchants.containers.ContainerBigStallSetPayItem;
import org.swarg.merchants.tileentities.TileEntityBigStall;

import com.bioxx.tfc.api.Food;
import com.bioxx.tfc.api.Interfaces.IFood;
import com.bioxx.tfc.Core.Player.PlayerInventory;
import com.bioxx.tfc.Food.ItemSalad;
import com.bioxx.tfc.Items.ItemBlocks.ItemBarrels;
import com.bioxx.tfc.Items.ItemBlocks.ItemCrucible;
import com.bioxx.tfc.Items.Pottery.ItemPotteryJug;
import com.bioxx.tfc.Items.Pottery.ItemPotterySmallVessel;
import com.bioxx.tfc.GUI.GuiContainerTFC;

import static com.aleksey.merchants.Extended.AnimalInCrate.isValidAnimalCrate;
import static com.aleksey.merchants.Extended.ExtendedLogic.strToInt;


/**
 * 12-12-2024
 * @author Swarg
 */
public class GuiBigStallSetPayItem extends GuiContainerTFC
{
    private static final ResourceLocation texture = new ResourceLocation("merchants", "textures/gui/gui_stall_payitem.png");
    private static final RenderItem itemRenderer = new RenderItem();

    public static final int SlotSize = 18;
    public static final int PriceSlotX = 55;
    public static final int PriceSlotY = 40;
    //public static final int GoodSlotX = 102;
    public static final int WindowWidth = 176;
    public static final int WindowHeight = 137;

    private static final int titleX = 0;
    private static final int titleY = 4;

    private static final int limitLabelWidth = 47;

    private static final int fieldWidth = 48;
    private static final int idTextFieldX = 38;
    private static final int idTextFieldY = 17;
    private static final int idTextFieldWidth = fieldWidth;

    private static final int metaTextFieldX = 90;
    private static final int metaTextFieldY = 17;
    private static final int metaTextFieldWidth = fieldWidth;

    private static final int countTextFieldX = 99;
    private static final int countTextFieldY = 39;
    private static final int countTextFieldWidth = 28;

    private static final int param1TextFieldX = 38;
    private static final int param1TextFieldY = 61;
    private static final int param1TextFieldWidth = fieldWidth;

    private static final int param2TextFieldX = 90;
    private static final int param2TextFieldY = 61;
    private static final int param2TextFieldWidth = fieldWidth;

    private static final int param3TextFieldX = 38;
    private static final int param3TextFieldY = 83;
    private static final int param3TextFieldWidth = fieldWidth;;

    private static final int param4TextFieldX = 90;
    private static final int param4TextFieldY = 83;
    private static final int param4TextFieldWidth = fieldWidth;;

    private static final int buttonY = 107;
    private static final int applyButtonX = 37;
    private static final int cancelButtonX = 89;

    private static final int buttonId_applyButton = 0;
    private static final int buttonId_cancelButton = 1;

    private static final int colorDefaultText = 0x555555;

    private TileEntityBigStall stall;
    private int priceSlotIndex;

    private GuiTextField idTextField;
    private GuiTextField metaTextField;
    private GuiTextField countTextField;
    private GuiTextField param1TextField;
    private GuiTextField param2TextField;
    private GuiTextField param3TextField;
    private GuiTextField param4TextField;

    private int payItemType;
    private static final int ITNO = 0;
    private static final int ITFORGED = 1;
    private static final int ITSIMPLEFOOD = 2;
    private static final int ITFOODSALAD = 3;
    private static final int ITFOODSANDWICH = 4;
    private static final int ITBARREL = 5;
    private static final int ITPOTTERYJUG = 6;
    private static final int ITPOTTERYSVESSEL = 7;
    private static final int ITANIMALCRATE = 8;
    private static final int ITCRUCIBLE = 9;

    public static final String[] simplefoodtTooltip = new String[] {
        "","FoodCookedLevel","FoodSalted","FoodDired","FoodBrinedPickledSmoked"
    };
    public static final String[] barreltTooltip = new String[] {
        "","BarrelSealed","BarrelSealTime","BarrelFluidID","BarrelAmount"
    };
    public static final String[] animalcrateTooltip = new String[] {
        "","ACrateAnimID","ACrateFamAndSex","ACrateJumpAndSpeed","ACrateVariant"
    };
    public static final String[] svesselTooltip = new String[] {
        "","SVesselUsedFlag","NoUsed","MetalID","MetalAmount"
    };
    public static final String[] crucibleTooltip = new String[]{
        "", "ContentType", "NoUsed", "MetalID", "MetalAmount"
    };

    private static final int FNO = 0;
    private static final int FID = 1;
    private static final int FMETA = 2;
    private static final int FCOUNT = 3;

    private static final int FP0 = 10;
    private static final int FP1 = 11;
    private static final int FP2 = 12;
    private static final int FP3 = 13;
    private static final int FP4 = 14;



    public GuiBigStallSetPayItem(
        InventoryPlayer inv, TileEntityBigStall stall,
        World world, int x, int y, int z
    ) {
        super(new ContainerBigStallSetPayItem(inv, stall, world, x, y, z),
            WindowWidth, WindowHeight - 1);

        this.stall = stall;
        this.priceSlotIndex = stall.getActivePriceSlotIndex();
    }


    @Override
    public void updateScreen()
    {
        this.idTextField.updateCursorCounter();
        this.metaTextField.updateCursorCounter();
        this.countTextField.updateCursorCounter();
        this.param1TextField.updateCursorCounter();
        this.param2TextField.updateCursorCounter();
        this.param3TextField.updateCursorCounter();
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

        idTextField = new GuiTextField(fontRendererObj, guiLeft + idTextFieldX, guiTop + idTextFieldY, idTextFieldWidth, 18);
        idTextField.setFocused(true);
        metaTextField = new GuiTextField(fontRendererObj, guiLeft + metaTextFieldX, guiTop + metaTextFieldY, metaTextFieldWidth, 18);
        countTextField = new GuiTextField(fontRendererObj, guiLeft + countTextFieldX, guiTop + countTextFieldY, countTextFieldWidth, 18);
        param1TextField = new GuiTextField(fontRendererObj, guiLeft + param1TextFieldX, guiTop + param1TextFieldY, param1TextFieldWidth, 18);
        param2TextField = new GuiTextField(fontRendererObj, guiLeft + param2TextFieldX, guiTop + param2TextFieldY, param2TextFieldWidth, 18);
        param3TextField = new GuiTextField(fontRendererObj, guiLeft + param3TextFieldX, guiTop + param3TextFieldY, param3TextFieldWidth, 18);
        param4TextField = new GuiTextField(fontRendererObj, guiLeft + param4TextFieldX, guiTop + param4TextFieldY, param4TextFieldWidth, 18);

        fillPayStackParamToField();

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


    private boolean fillFieldsByParams(EditPayParams params, int count )
    {
        countTextField.setText(Integer.toString(count));

        if (params == null) {
            return false;
        }

        if (params.p1 > 0) {
            param1TextField.setText(Integer.toString(params.p1));
        }
        if (params.p2 > 0) {
            param2TextField.setText(Integer.toString(params.p2));
        }
        if (params.p3 > 0) {
            param3TextField.setText(Integer.toString(params.p3));
        }
        if (params.p4 > 0) {
            param4TextField.setText(Integer.toString(params.p4));
        }
        return true;
    }


    /**
     * By PayItemStack from SlallSlot set params to fields
     * and tooltip for this type of payItem
     */
    private void fillPayStackParamToField(){
        this.payItemType = ITNO;

        if (priceSlotIndex > -1 && priceSlotIndex < stall.getSizeInventory())
        {
            ItemStack payStack = stall.getStackInSlot(priceSlotIndex);
            if (payStack == null) {
                return;
            }

            Item item = payStack.getItem();
            int id = Item.getIdFromItem( item );
            int meta = payStack.getItemDamage();
            int count = payStack.stackSize;
            idTextField.setText(Integer.toString(id));
            metaTextField.setText(Integer.toString(meta));
            countTextField.setText(Integer.toString(count));
            EditPayParams params = null;

            if (item instanceof ItemSalad)
            {
                this.payItemType = ITFOODSALAD;
                count = ExtendedLogic.SALADWEIGHT ;
                if (payStack.stackTagCompound != null)
                {
                    params = EditPriceSlot.getParamsForSalad(payStack);
                    fillFieldsByParams(params,count);
                }
            }
            else if (item instanceof IFood)//Simple TFC Food
            {
                this.payItemType = ITSIMPLEFOOD;
                if (payStack.stackTagCompound != null)
                {
                    float weight = Food.getWeight(payStack);
                    count = (weight > 0 && weight <=160) ? count = 10 * (int)(weight / 10) : 160;
                    params = EditPriceSlot.getParamsForTFCSimpleFood(payStack);
                }
            }
            else if (item instanceof ItemPotteryJug)
            {
                this.payItemType = ITPOTTERYJUG;
                if (payStack.stackTagCompound != null) {
                    params = new EditPayParams(1); // flag about used potteryJug
                }
            }
            else if (item instanceof ItemPotterySmallVessel)
            {
                this.payItemType = ITPOTTERYSVESSEL;
                if (payStack.stackTagCompound != null) {
                    params = EditPriceSlot.getParamsForSmallVessel(payStack);
                }
            }
            else if ( item instanceof ItemCrucible)
            {
                this.payItemType = ITCRUCIBLE;
                if (payStack.stackTagCompound != null) {
                    count = 1;
                    params = EditPriceSlot.getParamsForCrucible(payStack);
                }
            }
            else if (item instanceof ItemBarrels)
            {
                this.payItemType = ITBARREL;
                if ( payStack.stackTagCompound != null ) {
                    count = 1;
                    params = EditPriceSlot.getParamsForBarrel(payStack);
                }
            }
            else if ((payStack.stackTagCompound != null && payStack.stackTagCompound.hasKey("craftingTag")
                     )
                    || !EditPriceSlot.isNotForgedTFCItems(payStack) &&
                       EditPriceSlot.getTFCSmithingItemType(payStack) != EditPriceSlot.NOTFC
            ) {
                this.payItemType = ITFORGED;
                if (payStack.stackTagCompound != null) {
                    params = EditPriceSlot.getParamsForSmithingItem(payStack);
                }
            }
            else if (isValidAnimalCrate(payStack) )
            {
                this.payItemType = ITANIMALCRATE;
                if ( payStack.stackTagCompound != null ) {
                    params = AnimalInCrate.getParamsForAnimalCrate(payStack);
                }
            }

            fillFieldsByParams(params,count);
        }
    }


    @Override
    protected void mouseClicked(int par1, int par2, int par3)
    {
        super.mouseClicked(par1, par2, par3);

        idTextField.mouseClicked(par1, par2, par3);
        metaTextField.mouseClicked(par1, par2, par3);
        countTextField.mouseClicked(par1, par2, par3);
        param1TextField.mouseClicked(par1, par2, par3);
        param2TextField.mouseClicked(par1, par2, par3);
        param3TextField.mouseClicked(par1, par2, par3);
        param4TextField.mouseClicked(par1, par2, par3);
    }


    @Override
    protected void keyTyped(char key, int par2)
    {
        //int c=(int)key;//200 up 208 down 203 left 205 right 211 delete
        if(key >= '0' && key <= '9'
            || key == '\u0008' // Backspace
            || key == '\u007F' // Delete
            || par2 == 203 || par2 == 205 || par2 == 211
        ) {
            idTextField.textboxKeyTyped(key, par2);
            metaTextField.textboxKeyTyped(key, par2);
            countTextField.textboxKeyTyped(key, par2);
            param1TextField.textboxKeyTyped(key, par2);
            param2TextField.textboxKeyTyped(key, par2);
            param3TextField.textboxKeyTyped(key, par2);
            param4TextField.textboxKeyTyped(key, par2);
        }
        else if (key=='\u0009' || par2 == 208) // TAB or arrow down
        {
            if (idTextField.isFocused()) {
                idTextField.setFocused(false);
                metaTextField.setFocused(true);
            }
            else if (metaTextField.isFocused()) {
                metaTextField.setFocused(false);
                countTextField.setFocused(true);
            }
            else if (countTextField.isFocused()) {
                countTextField.setFocused(false);
                param1TextField.setFocused(true);
            }
            else if (param1TextField.isFocused()) {
                param1TextField.setFocused(false);
                param2TextField.setFocused(true);
            }
            else if (param2TextField.isFocused()) {
                param2TextField.setFocused(false);
                param3TextField.setFocused(true);
            }
            else if (param3TextField.isFocused()) {
                param3TextField.setFocused(false);
                param4TextField.setFocused(true);
            }
            else if (param4TextField.isFocused()) {
                param4TextField.setFocused(false);
                idTextField.setFocused(true);
            }
        }
        else if(par2 == 200) // arrow up
        {
            if (idTextField.isFocused()) {
                idTextField.setFocused(false);
                param4TextField.setFocused(true);
            }
            else if (metaTextField.isFocused()) {
                metaTextField.setFocused(false);
                idTextField.setFocused(true);
            }
            else if (countTextField.isFocused()) {
                countTextField.setFocused(false);
                metaTextField.setFocused(true);
            }
            else if (param1TextField.isFocused()) {
                param1TextField.setFocused(false);
                countTextField.setFocused(true);
            }
            else if (param2TextField.isFocused()) {
                param2TextField.setFocused(false);
                param1TextField.setFocused(true);
            }
            else if (param3TextField.isFocused()) {
                param3TextField.setFocused(false);
                param2TextField.setFocused(true);
            }
            else if (param4TextField.isFocused()) {
                param4TextField.setFocused(false);
                param3TextField.setFocused(true);
            }

        }
        else if(key == 13) { // enter
        // showing?
            int id = strToInt(idTextField.getText());
            if (id > 0 && id < 30000)  {
                applyPayItem();
            }
        }
        else if(key == 27) { //esc
          stall.actionSetSetPayItem(priceSlotIndex);
        }
    }


    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        switch(guibutton.id)
        {
            case  buttonId_applyButton:
                applyPayItem();
                break;
            case  buttonId_cancelButton:
                stall.actionSetSetPayItem(priceSlotIndex);//actionSetLimit
                break;
        }
    }


    private void applyPayItem()
    {
        stall.actionSetSetPayItem(priceSlotIndex,
                strToInt(idTextField.getText(),1),//id
                strToInt(metaTextField.getText()),//meta
                strToInt(countTextField.getText(),1),//count

                strToInt(param1TextField.getText()),//p1
                strToInt(param2TextField.getText()),//p2
                strToInt(param3TextField.getText()),//p3
                strToInt(param4TextField.getText()) //p4
        );
    }


    public void drawTooltipEx(int mx, int my, String text)
    {
        List<String> list = new ArrayList<String>();
        String[] a = text.split("\n");
        for (String line : a) {
            list.add(line);
        }

        this.drawHoveringText(list, mx, my + 15, this.fontRendererObj);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
    }


    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        // drawTooltip(mouseX - this.guiLeft, mouseY - this.guiTop, "ToolTip");
        int i = getFieldUnderMouse(mouseX , mouseY);
        String tooltip = "";

        if (i == FID) {
            tooltip =  StatCollector.translateToLocal("gui.StallSetPay.Tooltip.ID");
        } else if (i == FMETA) {
            tooltip =  StatCollector.translateToLocal("gui.StallSetPay.Tooltip.Meta");
        } else if (i == FCOUNT) {
            tooltip =  StatCollector.translateToLocal("gui.StallSetPay.Tooltip.Count");
        } else if (i >= FP1 && i<= FP4) {
            //depenging on the type of payItem
            tooltip = getToolTipFor( i - FP0 );
        }

        if (tooltip != null && !tooltip.isEmpty()) {
            drawTooltipEx(mouseX - this.guiLeft, mouseY - this.guiTop, tooltip);
        }
    }


    public String getToolTipFor(int p)
    {
        String bonus = "";
        if (this.payItemType == ITNO || p < 0 || p > 4) {
            return null;
        }

        String r = null;

        switch (this.payItemType)
        {
            case ITFORGED:
                r = ( p == 1) ? "SmithingBonus" : "NoUsed";
                break;

            case ITSIMPLEFOOD:
                if (p > 0 && p < simplefoodtTooltip.length) {
                    r = simplefoodtTooltip[p];
                }
                break;

            case ITFOODSALAD:
                r = "SaladFoodComponentID";
                break;

            case ITBARREL:
                if (p > 0&& p < barreltTooltip.length) {
                    r = barreltTooltip[p];
                }
                if (p == 3 && this.param3TextField.getText() != null
                           && !this.param3TextField.getText().isEmpty()) {
                    int p3 = strToInt(this.param3TextField.getText());
                    bonus = EditPriceSlot.getFluidNameByID(p3); // getValidFluidIDList();
                }
                break;

            case ITPOTTERYJUG:
                r = (p == 1) ? "PotteryJugUsed" : "NoUsed";
                if (p == 1) {
                    bonus = "0 new \n1 - used";
                }
                break;

            case ITPOTTERYSVESSEL:
                if (p > 0&& p < svesselTooltip.length) {
                    r = svesselTooltip[p];
                }
                /*if (p == 3 )// MetalID
                {
                    int p3 = strToInt(this.param3TextField.getText());
                    bonus = "Look NEI";
                }*/

                break;

            case ITCRUCIBLE:
                if (p > 0 && p < crucibleTooltip.length) {
                    r = crucibleTooltip[p];
                }
                break;

            case ITANIMALCRATE:
                if (p > 0&& p < animalcrateTooltip.length) {
                    r = animalcrateTooltip[p];
                    if (p == 1) { //Animal id
                        bonus = AnimalInCrate.getListOfAnimals();
                    } else if (p == 2) {
                        String sexx = "";
                        String s = this.param2TextField.getText().isEmpty() ? "0" : this.param2TextField.getText();
                        EditPayParams saf = AnimalInCrate.getAnimalSexAgeFamiliarity( s );
                        if (saf == null) {
                            return "";
                        }

                        if (saf.p1 == 0) { //sex
                            sexx = " - \u2642";
                        } else if (saf.p1 == 1) {
                            sexx = " - \u2640";
                        } else if (saf.p1 == 2) {
                            sexx = " - " + StatCollector.translateToLocal("gui.StallSetPay.Tooltip.AUknown");
                        }

                        String Age = "";
                        if (saf.p2 == 1) {
                            Age = "ABaby";
                        } else if (saf.p2 == 2) {
                            Age = "AAdult";
                        } else {
                            Age = "AUknown";
                        }

                        if (saf.p1 >= 0 && saf.p1 < 3 ) { // 2 any sex
                            bonus = saf.p3/*animal.familiarity*/ + " - " + StatCollector.translateToLocal("gui.StallSetPay.Tooltip.Familiarity") + " \n"
                                    + saf.p2/*animal.age*/ + " - " + StatCollector.translateToLocal("gui.StallSetPay.Tooltip.Age")+
                                                   " " + StatCollector.translateToLocal("gui.StallSetPay.Tooltip." + Age)+"\n"
                                    + saf.p1/*animal.sex*/ + sexx;
                        }
                    }
                    else if (p == 3
                            && this.param3TextField.getText() != null
                            && !this.param3TextField.getText().isEmpty())
                    {
                        EditPayParams js = AnimalInCrate.getAnimalJumpSpeed(this.param3TextField.getText());
                        if (js == null) {
                            return "";
                        }
                        float jumpH = js.p1;//animal.getJumpHX10();
                        jumpH = (jumpH > 0)? (float) jumpH /10 : 0;
                        float speed = js.p2;//animal.getSpeedX10();
                        speed = (speed>0)? (float) speed / 10 : 0;
                        if ( jumpH > 0 || speed > 0)
                            bonus =  String.format("J: %.1f m \nS: %.1f m/s", jumpH, speed );
                    }

                }
                break;
        }
        bonus =  bonus.isEmpty() ? "" : "\n"+bonus;
        return (r == null) ? null : StatCollector.translateToLocal("gui.StallSetPay.Tooltip." + r) + bonus;
    }


    public static boolean isCursonUnderField(GuiTextField field, int mouseX, int mouseY, int w , int h)
    {
        if (field == null || field.height == 0 || field.width == 0) {
            return false;
        }


        return (mouseY > field.yPosition && mouseY < field.yPosition + field.height)
            && (mouseX > field.xPosition && mouseX < field.xPosition + field.width);
    }


    public int getFieldUnderMouse(int mouseX, int mouseY)
    {
        int w = (width - xSize) / 2;
        int h = (height - ySize) / 2;

        if ( isCursonUnderField(this.idTextField, mouseX, mouseY, w, h))
            return FID;
        else if ( isCursonUnderField(this.metaTextField, mouseX, mouseY, w, h))
            return FMETA;
        else if ( isCursonUnderField(this.countTextField, mouseX, mouseY, w, h))
            return FCOUNT;
        else if ( isCursonUnderField(this.param1TextField, mouseX, mouseY, w, h))
            return FP1;
        else if ( isCursonUnderField(this.param2TextField, mouseX, mouseY, w, h))
            return FP2;
        else if ( isCursonUnderField(this.param3TextField, mouseX, mouseY, w, h))
            return FP3;
        else if ( isCursonUnderField(this.param4TextField, mouseX, mouseY, w, h))
            return FP4;

        return FNO;
    }


    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY)
    {
        bindTexture(texture);

        int w = (width - xSize) / 2;
        int h = (height - ySize) / 2;
        int v = 0;

        drawTexturedModalRect(w, h, 0, v, xSize, ySize);
        //+":" +_priceSlotIndex
        drawCenteredString(StatCollector.translateToLocal("gui.StallSetPayItem.Title"),
                w + titleX, h + titleY, WindowWidth, colorDefaultText);

        idTextField.drawTextBox();
        metaTextField.drawTextBox();
        countTextField.drawTextBox();
        param1TextField.drawTextBox();
        param2TextField.drawTextBox();
        param3TextField.drawTextBox();
        param4TextField.drawTextBox();

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

