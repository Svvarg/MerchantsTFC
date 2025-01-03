package com.aleksey.merchants.Render.Items;

import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.IItemRenderer;

import org.lwjgl.opengl.GL11;

import com.aleksey.merchants.Core.Constants;
import com.aleksey.merchants.Helpers.CoinHelper;

public class CoinItemRenderer implements IItemRenderer
{
    private static final double _dieX = 5;
    private static final double _dieY = 5;

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type)
    {
        return type == ItemRenderType.INVENTORY;
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
    {
        return false;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack is, Object... data)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL11.GL_BLEND);

        renderIcon(0, 0, is.getItem().getIconIndex(is), 16, 16);

        renderDie(is);

        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private static void renderDie(ItemStack is)
    {
        if(!is.hasTagCompound() || !is.getTagCompound().hasKey(CoinHelper.TagName_Key))
            return;

        int color = Constants.Coins[is.getItemDamage()].DieColor;
        byte[] dieBytes = CoinHelper.getCoinDie(is);
        boolean[] dieBits = CoinHelper.unpackDie(dieBytes);

        int index = 0;
        double y = _dieY;

        for(int row = 0; row < CoinHelper.DieStride; row++)
        {
            double x = _dieX;

            for(int col = 0; col < CoinHelper.DieStride; col++)
            {
                if(dieBits[index])
                    renderQuad(x, y, 0.5, 0.5, color);

                index++;

                x += 0.5;
            }

            y += 0.5;
        }
    }

    private static void renderIcon(int x, int y, IIcon icon, int sizeX, int sizeY)
    {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((double)(x + 0), (double)(y + sizeY), (double)0, (double)icon.getMinU(), (double)icon.getMaxV());
        tessellator.addVertexWithUV((double)(x + sizeX), (double)(y + sizeY), (double)0, (double)icon.getMaxU(), (double)icon.getMaxV());
        tessellator.addVertexWithUV((double)(x + sizeX), (double)(y + 0), (double)0, (double)icon.getMaxU(), (double)icon.getMinV());
        tessellator.addVertexWithUV((double)(x + 0), (double)(y + 0), (double)0, (double)icon.getMinU(), (double)icon.getMinV());
        tessellator.draw();
    }

    private static void renderQuad(double x, double y, double sizeX, double sizeY, int color)
    {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.setColorOpaque_I(color);
        tess.addVertex((double)(x + 0), (double)(y + 0), 0.0D);
        tess.addVertex((double)(x + 0), (double)(y + sizeY), 0.0D);
        tess.addVertex((double)(x + sizeX), (double)(y + sizeY), 0.0D);
        tess.addVertex((double)(x + sizeX), (double)(y + 0), 0.0D);
        tess.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
}
