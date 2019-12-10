/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.aleksey.merchants.GUI;

import com.aleksey.merchants.Containers.AnimalInCrate;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import org.lwjgl.input.Keyboard;

import com.aleksey.merchants.Containers.ContainerStallSetPayItem;
import static com.aleksey.merchants.Containers.AnimalInCrate.isValidAnimalCrate;
import com.aleksey.merchants.Containers.EditPriceSlot;
import com.aleksey.merchants.Containers.ExtendedLogic;
import static com.aleksey.merchants.Containers.ExtendedLogic.strToInt;
import com.aleksey.merchants.TileEntities.TileEntityStall;
import com.bioxx.tfc.Containers.ContainerTFC;
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
import net.minecraftforge.fluids.FluidStack;

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
//    public static final int GoodSlotX = 102;
    public static final int WindowWidth = 176;
    public static final int WindowHeight = 137;
    
    private static final int _titleX = 0;
    private static final int _titleY = 4;
    
    private static final int _limitLabelX = 6;
    private static final int _limitLabelY = 43;
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
    //private int _goodSlotIndex;
    
    //private GuiTextField _limitTextField;
    private GuiTextField _idTextField;
    private GuiTextField _metaTextField;
    private GuiTextField _countTextField;
    private GuiTextField _param1TextField;
    private GuiTextField _param2TextField;
    private GuiTextField _param3TextField;
    private GuiTextField _param4TextField;
    
    private ContainerStallSetPayItem inventory;
    /*
    private int payId;
    private int payMeta;
    private int payCount;
    private int payParam1;
    private int payParam2;
    private int payParam3;
    private int payParam4;
    */
    

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

    private void fillPayStackParamToField(){
        
        if (_priceSlotIndex>-1&& _priceSlotIndex < _stall.getSizeInventory())
        {
            ItemStack payStack = _stall.getStackInSlot(_priceSlotIndex);
            if (payStack == null)
                return;
            
            Item item = payStack.getItem();
            int id = Item.getIdFromItem( item ); //for debug payStack.getItem());
            
            int meta = payStack.getItemDamage();
            int count = payStack.stackSize;
            _idTextField.setText(Integer.toString(id));
            _metaTextField.setText(Integer.toString(meta));
            _countTextField.setText(Integer.toString(count));
            
            if (payStack.hasTagCompound())
            {
                int p1 = 0;
                int p2 = 0;
                int p3 = 0;
                int p4 = 0;
                if ( payStack.stackTagCompound.hasKey("craftingTag") )
                {
                    int duraBuff = (int) Math.floor( getDurabilityBuff(payStack) * 100 );
                    if (duraBuff >0)
                        p1 = duraBuff;//_param1TextField.setText(Integer.toString(duraBuff));
                   
                }
                else if(payStack.getItem() instanceof IFood)
                {
                    float weight = Food.getWeight(payStack);                    
                    
                    if ( item instanceof ItemSalad )
                    {
                        int maxSaladWeight = 20;
                        int [] fg = Food.getFoodGroups(payStack);
                        if ( fg.length == 4){
                            p1 = fg[0];
                            p2 = fg[1];
                            p3 = fg[2];
                            p4 = fg[3];
                        }
                       count = ( weight > 0 && weight < maxSaladWeight ) ? count = 10 * (int)(weight / 10) : maxSaladWeight;
                    }
                    else //Simple TFC Food
                    {
                        count = ( weight > 0 && weight <=160) ? count = 10 * (int)(weight / 10) : 160;
                        p1 = ExtendedLogic.getCookedLevel(payStack);                       
                        p2 = Food.isSalted(payStack)? 1 : 0;                   
                        p3 = Food.isDried(payStack) ? 1 : 0;
                        //brined pickled smoked at one param
                        p4 = EditPriceSlot.getTFCFoodParams(payStack);// 
                    }
                }
                //only saled barrels (have nbt)
                else if ( item instanceof ItemBarrels )
                {                    
                    _countTextField.setText(Integer.toString(1));
                    p1 = (payStack.stackTagCompound.getBoolean("Sealed"))?1:0;
                    p2 = payStack.stackTagCompound.getInteger("SealTime");
                    p2 = EditPriceSlot.getYearFromHours(p2);
                    FluidStack fluidStack = EditPriceSlot.getFluidID(payStack);
                    if (fluidStack != null)
                    {
                        p3 = fluidStack.getFluidID();
                        if (fluidStack.amount > 0 )
                            p4 = (int) Math.floor(fluidStack.amount / 1000);
                    }                        
                }
                else if ( item instanceof ItemPotteryJug ||
                        item instanceof ItemPotterySmallVessel)
                {
                    p1 = 1;//flag about not new potteryJug or smallVessel
                } 
                else if (payStack.hasTagCompound() && isValidAnimalCrate(payStack) )
                {
                    AnimalInCrate a = new AnimalInCrate(payStack.stackTagCompound);
                    if (a.id > 0 )
                    {
                        p1 = a.id;
                        p2 = a.sex + a.familiarity * 10; // 351 35-famil 1-sex(invert 1-man 0-female)
                        int speed = a.getSpeedX10();
                        int jump = a.getJumpHX10();
                        p3 = speed + jump  * 1000;//45103 jump 4.5m speed 10.3m/s
                        p4 = a.variant;
                    }
                }
                
                _countTextField.setText(Integer.toString(count));
                
                if ( p1 > 0 ) 
                    _param1TextField.setText(Integer.toString(p1));
                if ( p2 > 0 ) 
                    _param2TextField.setText(Integer.toString(p2));
                if ( p3 > 0 ) 
                    _param3TextField.setText(Integer.toString(p3));
                if ( p4 > 0 ) 
                    _param4TextField.setText(Integer.toString(p4));
                
            }//end have nbt---
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
        
        if(key >= '0' && key <= '9'
            || key == '\u0008'//Backspace
            || key == '\u007F'//Delete
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
        else if (key=='\u0009')//TAB
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
        
        drawCenteredString(StatCollector.translateToLocal("gui.StallSetPayItem.Title")+":" +_priceSlotIndex , w + _titleX, h + _titleY, WindowWidth, _colorDefaultText);
        
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
    

