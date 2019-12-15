/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aleksey.merchants.GUI;

import com.aleksey.merchants.Extended.AnimalInCrate;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;

import com.aleksey.merchants.Containers.ContainerStallSetPayItem;
import static com.aleksey.merchants.Extended.AnimalInCrate.isValidAnimalCrate;
import com.aleksey.merchants.Extended.EditPayParams;
import com.aleksey.merchants.Extended.EditPriceSlot;
import com.aleksey.merchants.Extended.ExtendedLogic;
import static com.aleksey.merchants.Extended.ExtendedLogic.strToInt;
import com.aleksey.merchants.TileEntities.TileEntityStall;
import com.bioxx.tfc.Core.Player.PlayerInventory;
import com.bioxx.tfc.Food.ItemSalad;
import com.bioxx.tfc.GUI.GuiContainerTFC;
import com.bioxx.tfc.Items.ItemBlocks.ItemBarrels;
import com.bioxx.tfc.Items.Pottery.ItemPotteryJug;
import com.bioxx.tfc.Items.Pottery.ItemPotterySmallVessel;
import static com.bioxx.tfc.api.Crafting.AnvilManager.getDurabilityBuff;
import com.bioxx.tfc.api.Food;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.bioxx.tfc.api.Interfaces.IFood;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraftforge.fluids.FluidStack;
import org.lwjgl.opengl.GL11;
import static com.aleksey.merchants.Extended.EditPriceSlot.getParamsForSmallVessel;

/**
 *
 * @author Swarg
 */

public class GuiStallSetPayItem extends GuiContainerTFC  
{
    private static final ResourceLocation _texture = new ResourceLocation("merchants", "textures/gui/gui_stall_payitem.png");
    private static final RenderItem _itemRenderer = new RenderItem();
    
    public static final int SlotSize = 18;
    public static final int PriceSlotX = 55;
    public static final int PriceSlotY = 40;    
    //public static final int GoodSlotX = 102;
    public static final int WindowWidth = 176;
    public static final int WindowHeight = 137;
    
    private static final int _titleX = 0;
    private static final int _titleY = 4;
    
    private static final int _limitLabelWidth = 47;

    private static final int _fieldWidth = 48;
    private static final int _idTextFieldX = 38;
    private static final int _idTextFieldY = 17;
    private static final int _idTextFieldWidth = _fieldWidth;
    
    private static final int _metaTextFieldX = 90;
    private static final int _metaTextFieldY = 17;
    private static final int _metaTextFieldWidth = _fieldWidth;
    
    private static final int _countTextFieldX = 99;
    private static final int _countTextFieldY = 39;
    private static final int _countTextFieldWidth = 28;
    
    private static final int _param1TextFieldX = 38;
    private static final int _param1TextFieldY = 61;
    private static final int _param1TextFieldWidth = _fieldWidth;
    
    private static final int _param2TextFieldX = 90;
    private static final int _param2TextFieldY = 61;
    private static final int _param2TextFieldWidth = _fieldWidth;

    private static final int _param3TextFieldX = 38;
    private static final int _param3TextFieldY = 83;
    private static final int _param3TextFieldWidth = _fieldWidth;;
    
    private static final int _param4TextFieldX = 90;
    private static final int _param4TextFieldY = 83;
    private static final int _param4TextFieldWidth = _fieldWidth;;
    
    private static final int _buttonY = 107;
    private static final int _applyButtonX = 37;
    private static final int _cancelButtonX = 89;
    
    private static final int _buttonId_applyButton = 0;
    private static final int _buttonId_cancelButton = 1;
    
    private static final int _colorDefaultText = 0x555555;

    private TileEntityStall _stall;
    private int _priceSlotIndex;

    private GuiTextField _idTextField;
    private GuiTextField _metaTextField;
    private GuiTextField _countTextField;
    private GuiTextField _param1TextField;
    private GuiTextField _param2TextField;
    private GuiTextField _param3TextField;
    private GuiTextField _param4TextField;
    
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
    
    public static final String[] simplefoodtTooltip = new String[] {"","FoodCookedLevel","FoodSalted","FoodDired","FoodBrinedPickledSmoked"};                
    public static final String[] barreltTooltip = new String[] {"","BarrelSealed","BarrelSealTime","BarrelFluidID","BarrelAmount"};
    public static final String[] animalcrateTooltip = new String[] {"","ACrateAnimID","ACrateFamAndSex","ACrateJumpAndSpeed","ACrateVariant"};
    public static final String[] svesselTooltip = new String[] {"","SVesselUsedFlag","NoUsed","MetalID","MetalAmount"};
    
    private static final int FNO = 0;
    private static final int FID = 1;
    private static final int FMETA = 2;
    private static final int FCOUNT = 3;
    
    private static final int FP0 = 10;
    private static final int FP1 = 11;
    private static final int FP2 = 12;
    private static final int FP3 = 13;
    private static final int FP4 = 14;
    
    //private AnimalInCrate animal; 
    //private ContainerStallSetPayItem inventory;
    

    public  GuiStallSetPayItem(InventoryPlayer inventoryplayer, TileEntityStall stall, World world, int x, int y, int z)
    {
        //inventory = new ContainerStallSetPayItem(inventoryplayer, stall, world, x, y, z);
        super( new ContainerStallSetPayItem(inventoryplayer, stall, world, x, y, z), WindowWidth, WindowHeight - 1);

        _stall = stall;
        _priceSlotIndex = stall.getActivePriceSlotIndex();
        //_goodSlotIndex = stall.getActiveGoodSlotIndex();
    }
    
    @Override
    public void updateScreen()
    {        
        this._idTextField.updateCursorCounter();
        this._metaTextField.updateCursorCounter();
        this._countTextField.updateCursorCounter();
        this._param1TextField.updateCursorCounter();
        this._param2TextField.updateCursorCounter();
        this._param3TextField.updateCursorCounter();
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
        
        _idTextField = new GuiTextField(fontRendererObj, guiLeft + _idTextFieldX, guiTop + _idTextFieldY, _idTextFieldWidth, 18);        
        _idTextField.setFocused(true);
        _metaTextField = new GuiTextField(fontRendererObj, guiLeft + _metaTextFieldX, guiTop + _metaTextFieldY, _metaTextFieldWidth, 18);        
        _countTextField = new GuiTextField(fontRendererObj, guiLeft + _countTextFieldX, guiTop + _countTextFieldY, _countTextFieldWidth, 18);        
        _param1TextField = new GuiTextField(fontRendererObj, guiLeft + _param1TextFieldX, guiTop + _param1TextFieldY, _param1TextFieldWidth, 18);        
        _param2TextField = new GuiTextField(fontRendererObj, guiLeft + _param2TextFieldX, guiTop + _param2TextFieldY, _param2TextFieldWidth, 18);        
        _param3TextField = new GuiTextField(fontRendererObj, guiLeft + _param3TextFieldX, guiTop + _param3TextFieldY, _param3TextFieldWidth, 18);        
        _param4TextField = new GuiTextField(fontRendererObj, guiLeft + _param4TextFieldX, guiTop + _param4TextFieldY, _param4TextFieldWidth, 18);        
        
        fillPayStackParamToField();
        
        Keyboard.enableRepeatEvents(true);
        
        this.buttonList.add(new GuiButton(_buttonId_applyButton, guiLeft + _applyButtonX, guiTop + _buttonY, 50, 20, StatCollector.translateToLocal("gui.StallLimit.Apply")));
        this.buttonList.add(new GuiButton(_buttonId_cancelButton, guiLeft + _cancelButtonX, guiTop + _buttonY, 50, 20, StatCollector.translateToLocal("gui.StallLimit.Cancel")));
    }
    
    private boolean fillFieldsByParams(EditPayParams params,int count )
    {
        _countTextField.setText(Integer.toString(count));
        
        if (params == null)
            return false;
        
        if ( params.p1 > 0 ) 
            _param1TextField.setText(Integer.toString(params.p1));
        if ( params.p2 > 0 ) 
            _param2TextField.setText(Integer.toString(params.p2));
        if ( params.p3 > 0 ) 
            _param3TextField.setText(Integer.toString(params.p3));
        if ( params.p4 > 0 ) 
             _param4TextField.setText(Integer.toString(params.p4));        
        return true;
    }
    
    
    /**
     * By PayItemStack from SlallSlot set params to fields
     * and tooltip for this type of payItem
     */
    private void fillPayStackParamToField(){
        this.payItemType = ITNO;
        
                
        if (_priceSlotIndex>-1&& _priceSlotIndex < _stall.getSizeInventory())
        {
            ItemStack payStack = _stall.getStackInSlot(_priceSlotIndex);
            if (payStack == null)
                return;
            
            Item item = payStack.getItem();
            int id = Item.getIdFromItem( item );
            int meta = payStack.getItemDamage();
            int count = payStack.stackSize;
            _idTextField.setText(Integer.toString(id));
            _metaTextField.setText(Integer.toString(meta));
            _countTextField.setText(Integer.toString(count));
            EditPayParams params = null;

            if ( item instanceof ItemSalad )
            {
                this.payItemType = ITFOODSALAD;
                count = ExtendedLogic.SALADWEIGHT ;
                if ( payStack.stackTagCompound != null )
                {
                    params = EditPriceSlot.getParamsForSalad(payStack);
                    fillFieldsByParams(params,count);                
                }
            }
            else if(item instanceof IFood)//Simple TFC Food
            {
                this.payItemType = ITSIMPLEFOOD;
                if ( payStack.stackTagCompound != null )
                {
                    float weight = Food.getWeight(payStack);
                    count = ( weight > 0 && weight <=160) ? count = 10 * (int)(weight / 10) : 160;
                    params = EditPriceSlot.getParamsForTFCSimpleFood(payStack);
                }                
            }            
            else if ( item instanceof ItemPotteryJug)
            {
                this.payItemType = ITPOTTERYJUG;
                if ( payStack.stackTagCompound != null )
                    params = new EditPayParams(1);//flag about used potteryJug
            } 
            else if (item instanceof ItemPotterySmallVessel)
            {
                this.payItemType = ITPOTTERYSVESSEL;
                if ( payStack.stackTagCompound != null )
                {                    
                    params = EditPriceSlot.getParamsForSmallVessel(payStack);
                }    
            }             
            else if ( item instanceof ItemBarrels )
            { 
                this.payItemType = ITBARREL;
                if ( payStack.stackTagCompound != null )
                {
                    count = 1;
                    params = EditPriceSlot.getParamsForBarrel(payStack);
                }    
            }
            else if ( (payStack.stackTagCompound!=null && payStack.stackTagCompound.hasKey("craftingTag")) 
                    || !EditPriceSlot.isNotForgedTFCItems(payStack) && 
                       EditPriceSlot.getTFCSmithingItemType(payStack) != EditPriceSlot.NOTFC )
            {
                this.payItemType = ITFORGED;
                if ( payStack.stackTagCompound != null )
                    params = EditPriceSlot.getParamsForSmithingItem(payStack);
            }
            else if (isValidAnimalCrate(payStack) )
            {
                this.payItemType = ITANIMALCRATE;
                if ( payStack.stackTagCompound != null )
                    params = AnimalInCrate.getParamsForAnimalCrate(payStack);
            }
            
            fillFieldsByParams(params,count);
        }                     
    }
    
    @Override
    protected void mouseClicked(int par1, int par2, int par3)
    {
        super.mouseClicked(par1, par2, par3);
                
        _idTextField.mouseClicked(par1, par2, par3);
        _metaTextField.mouseClicked(par1, par2, par3);
        _countTextField.mouseClicked(par1, par2, par3);
        _param1TextField.mouseClicked(par1, par2, par3);
        _param2TextField.mouseClicked(par1, par2, par3);
        _param3TextField.mouseClicked(par1, par2, par3);
        _param4TextField.mouseClicked(par1, par2, par3);
    }

    @Override
    protected void keyTyped(char key, int par2)
    {
        //int c=(int)key;//200 up 208 down 203 left 205 right 211 delete
        if(key >= '0' && key <= '9'
            || key == '\u0008'//Backspace
            || key == '\u007F'//Delete
            || par2 == 203 || par2 == 205 || par2 == 211   
            )
        {
            _idTextField.textboxKeyTyped(key, par2);
            _metaTextField.textboxKeyTyped(key, par2);
            _countTextField.textboxKeyTyped(key, par2);
            _param1TextField.textboxKeyTyped(key, par2);
            _param2TextField.textboxKeyTyped(key, par2);
            _param3TextField.textboxKeyTyped(key, par2);
            _param4TextField.textboxKeyTyped(key, par2);
        }
        else if (key=='\u0009' || par2 == 208)//TAB or arrow down
        {
            if (_idTextField.isFocused())
            {
                _idTextField.setFocused(false);
                _metaTextField.setFocused(true);
            }    
            else if (_metaTextField.isFocused())
            {
                _metaTextField.setFocused(false);
                _countTextField.setFocused(true);
            }    
            else if (_countTextField.isFocused())
            {
                _countTextField.setFocused(false);
                _param1TextField.setFocused(true);
            }                
            else if (_param1TextField.isFocused())
            {
                _param1TextField.setFocused(false);
                _param2TextField.setFocused(true);
            }
            else if (_param2TextField.isFocused())
            {
                _param2TextField.setFocused(false);
                _param3TextField.setFocused(true);
            }                
            else if (_param3TextField.isFocused())
            {
                _param3TextField.setFocused(false);
                _param4TextField.setFocused(true);            
            }
            else if (_param4TextField.isFocused())
            {
                _param4TextField.setFocused(false);
                _idTextField.setFocused(true);
            }    
        }
        else if(par2 == 200)// arrow up
        {
            if (_idTextField.isFocused())
            {
                _idTextField.setFocused(false);
                _param4TextField.setFocused(true);
            }    
            else if (_metaTextField.isFocused())
            {
                _metaTextField.setFocused(false);
                _idTextField.setFocused(true);
            }    
            else if (_countTextField.isFocused())
            {
                _countTextField.setFocused(false);
                _metaTextField.setFocused(true);
            }                
            else if (_param1TextField.isFocused())
            {
                _param1TextField.setFocused(false);
                _countTextField.setFocused(true);                
            }
            else if (_param2TextField.isFocused())
            {
                _param2TextField.setFocused(false);
                _param1TextField.setFocused(true);
            }                
            else if (_param3TextField.isFocused())
            {
                _param3TextField.setFocused(false);
                _param2TextField.setFocused(true);            
            }
            else if (_param4TextField.isFocused())
            {
                _param4TextField.setFocused(false);
                _param3TextField.setFocused(true);
            }    
        
        }
        else if(key == 13)//enter
        {// showing?             
            int id = strToInt(_idTextField.getText());            
            if ( id > 0 && id < 30000)  {
                applyPayItem();
            }             
        }   
        else if(key == 27)//esc
          _stall.actionSetSetPayItem(_priceSlotIndex);
    }
    
    @Override
    protected void actionPerformed(GuiButton guibutton)
    {
        switch(guibutton.id)
        {
            case  _buttonId_applyButton:
                applyPayItem();
                break;
            case  _buttonId_cancelButton:
                _stall.actionSetSetPayItem(_priceSlotIndex);//actionSetLimit
                break;
        }
    }
    
    
    
    private void applyPayItem()
    {
        _stall.actionSetSetPayItem( _priceSlotIndex, 
                strToInt(_idTextField.getText(),1),//id
                strToInt(_metaTextField.getText()),//meta
                strToInt(_countTextField.getText(),1),//count
                
                strToInt(_param1TextField.getText()),//p1
                strToInt(_param2TextField.getText()),//p2
                strToInt(_param3TextField.getText()),//p3
                strToInt(_param4TextField.getText()) //p4
        );
    }
    
    public void drawTooltipEx(int mx, int my, String text)
    {
        List<String> list = new ArrayList<String>();
        String[] a = text.split("\n");
        for (String line : a)
            list.add(line);        
        this.drawHoveringText(list, mx, my + 15, this.fontRendererObj);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        //GL11.glDisable(GL11.GL_DEPTH_TEST);
    }    
    
    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
         //drawTooltip(mouseX - this.guiLeft, mouseY - this.guiTop, "ToolTip");
         int i = getFieldUnderMouse(mouseX , mouseY);
         String tooltip = "";
         if (i == FID)
             tooltip =  StatCollector.translateToLocal("gui.StallSetPay.Tooltip.ID");
         else if (i == FMETA)
             tooltip =  StatCollector.translateToLocal("gui.StallSetPay.Tooltip.Meta");
         else if (i == FCOUNT)
             tooltip =  StatCollector.translateToLocal("gui.StallSetPay.Tooltip.Count");
         else if (i >= FP1 && i<=FP4)
         {
             //depenging on the type of payItem
             tooltip = getToolTipFor( i - FP0 );
         }
         if (tooltip != null && !tooltip.isEmpty())
             drawTooltipEx(mouseX - this.guiLeft, mouseY - this.guiTop, tooltip);
    }

        
    public String getToolTipFor(int p)
    {
        String bonus ="";
        if (this.payItemType == ITNO || p <0 || p>4)
            return null;    
        
        String r = null;
        
        switch (this.payItemType)
        {
            case ITFORGED:
                r = ( p == 1) ? "SmithingBonus" : "NoUsed";
                break;
                
            case ITSIMPLEFOOD:
                if (p > 0 && p < simplefoodtTooltip.length)                     
                    r = simplefoodtTooltip[p];                
                break;
                
            case ITFOODSALAD:
                r = "SaladFoodComponentID";
                break;
                
            case ITBARREL:
                if (p > 0&& p < barreltTooltip.length)
                    r = barreltTooltip[p];
                if (p == 3 && this._param3TextField.getText()!=null && !this._param3TextField.getText().isEmpty())
                {
                    int p3 = strToInt(this._param3TextField.getText());
                    bonus = EditPriceSlot.getFluidNameByID(p3);//getValidFluidIDList();
                }
                break;
                
            case ITPOTTERYJUG:
                r = ( p == 1) ? "PotteryJugUsed" : "NoUsed";
                if (p==1)
                    bonus = "0 new \n1 - used";
                break;
                
            case ITPOTTERYSVESSEL:
                if (p > 0&& p < svesselTooltip.length)
                    r = svesselTooltip[p];
                /*if (p == 3 )// MetalID
                {
                    int p3 = strToInt(this._param3TextField.getText());
                    bonus = "Look NEI";
                }*/
                
                break;
                
            case ITANIMALCRATE:
                if (p > 0&& p < animalcrateTooltip.length)
                {
                    r = animalcrateTooltip[p];
                    if (p==1)//Animal id
                        bonus = AnimalInCrate.getListOfAnimals();
                    else if (p==2)
                            //&& this._param2TextField.getText()!=null 
                            //&& !this._param2TextField.getText().isEmpty())
                    {
                        String sexx ="";
                        String s = this._param2TextField.getText().isEmpty()? "0" : this._param2TextField.getText();
                        EditPayParams saf = AnimalInCrate.getAnimalSexAgeFamiliarity( s );
                        if (saf == null)
                            return "";
                        
                        if (saf.p1 == 0) //sex
                            sexx = " - \u2642";
                        else if (saf.p1 == 1)
                            sexx = " - \u2640";
                        else if (saf.p1 == 2)
                            sexx = " - " + StatCollector.translateToLocal("gui.StallSetPay.Tooltip.AUknown");
                        
                        String Age = "";
                        if (saf.p2 == 1)
                            Age = "ABaby";
                        else if (saf.p2 == 2)
                            Age = "AAdult";
                        else 
                            Age = "AUknown";
                                    
                        if (saf.p1 >= 0 && saf.p1<3 ) //2 any sex
                            bonus = saf.p3/*animal.familiarity*/ + " - " + StatCollector.translateToLocal("gui.StallSetPay.Tooltip.Familiarity") +" \n" 
                                    + saf.p2/*animal.age*/ + " - "+StatCollector.translateToLocal("gui.StallSetPay.Tooltip.Age")+
                                                   " "+ StatCollector.translateToLocal("gui.StallSetPay.Tooltip."+Age)+"\n"
                                    + saf.p1/*animal.sex*/ + sexx;                            
                    }                            
                    else if (p==3
                            && this._param3TextField.getText()!=null 
                            && !this._param3TextField.getText().isEmpty())
                            
                    {
                        EditPayParams js = AnimalInCrate.getAnimalJumpSpeed(this._param3TextField.getText());
                        if (js==null)
                            return "";
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
        return ( r == null )? null : StatCollector.translateToLocal("gui.StallSetPay.Tooltip."+r) + bonus;    
    }
            
    public static boolean isCursonUnderField(GuiTextField field, int mouseX, int mouseY, int w , int h)
    {
        if (field == null || field.height == 0 || field.width ==0 )
            return false;
        
        
        return (mouseY > field.yPosition && mouseY < field.yPosition + field.height )
            && (mouseX > field.xPosition && mouseX < field.xPosition + field.width);
    }
            
    public int getFieldUnderMouse(int mouseX, int mouseY)
    {
        int w = (width - xSize) / 2;
        int h = (height - ySize) / 2;
        
        if ( isCursonUnderField(this._idTextField, mouseX, mouseY, w, h))
            return FID;
        else if ( isCursonUnderField(this._metaTextField, mouseX, mouseY, w, h))
            return FMETA;
        else if ( isCursonUnderField(this._countTextField, mouseX, mouseY, w, h))
            return FCOUNT;
        else if ( isCursonUnderField(this._param1TextField, mouseX, mouseY, w, h))
            return FP1;
        else if ( isCursonUnderField(this._param2TextField, mouseX, mouseY, w, h))
            return FP2;
        else if ( isCursonUnderField(this._param3TextField, mouseX, mouseY, w, h))
            return FP3;
        else if ( isCursonUnderField(this._param4TextField, mouseX, mouseY, w, h))
            return FP4;
            
        return FNO;
    }        
    
    @Override
    protected void drawGuiContainerBackgroundLayer(float f, int mouseX, int mouseY)
    {
        bindTexture(_texture);
        
        int w = (width - xSize) / 2;
        int h = (height - ySize) / 2;
        int v = 0;

        drawTexturedModalRect(w, h, 0, v, xSize, ySize);
        //+":" +_priceSlotIndex
        drawCenteredString(StatCollector.translateToLocal("gui.StallSetPayItem.Title"),
                w + _titleX, h + _titleY, WindowWidth, _colorDefaultText);
        
        _idTextField.drawTextBox();
        _metaTextField.drawTextBox();
        _countTextField.drawTextBox();
        _param1TextField.drawTextBox();
        _param2TextField.drawTextBox();
        _param3TextField.drawTextBox();
        _param4TextField.drawTextBox();
        
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
    

