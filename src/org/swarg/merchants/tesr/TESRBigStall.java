package org.swarg.merchants.tesr;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import org.lwjgl.opengl.GL11;

import org.swarg.merchants.render.blocks.RenderBigStall;
import org.swarg.merchants.tileentities.TileEntityBigStall;

import com.bioxx.tfc.Items.ItemBlocks.ItemAnvil;
import com.bioxx.tfc.Render.TESR.TESRBase;


/**
 * 12-12-2024
 * @author Swarg
 */
public class TESRBigStall extends TESRBase
{
	public TESRBigStall()
	{
	}

	public void renderAt(TileEntityBigStall te, double x, double y, double z, float f)
	{
		if (te.getWorldObj() == null || !RenderManager.instance.options.fancyGraphics) {
			return;
        }

		EntityItem customItem = new EntityItem(field_147501_a.field_147550_f); // tileEntityRenderer.worldObj
		customItem.hoverStart = 0f;

		ItemStack good1 = te.getStackInSlot(1);
		ItemStack good2 = te.getStackInSlot(3);
		ItemStack good3 = te.getStackInSlot(5);
		ItemStack good4 = te.getStackInSlot(7);
		ItemStack good5 = te.getStackInSlot(9);

        float goodY = (float)y + (float)(RenderBigStall.voxelSizeScaled * 9);

        if (good1 != null) {
            drawItem(good1, customItem, (float)x + 0.25F, goodY, (float)z + 0.25F);
        }

		if (good2 != null) {
		    drawItem(good2, customItem, (float)x + 0.75F, goodY, (float)z + 0.25F);
        }

		if (good3 != null) {
		    drawItem(good3, customItem, (float)x + 0.5F, goodY, (float)z + 0.5F);
        }

		if (good4 != null) {
		    drawItem(good4, customItem, (float)x + 0.25F, goodY, (float)z + 0.75F);
        }

		if (good5 != null) {
		    drawItem(good5, customItem, (float)x + 0.75F, goodY, (float)z + 0.75F);
        }
	}


	private void drawItem(
        ItemStack itemStack, EntityItem customitem, double x, double y, double z
    ) {
        float blockScale = itemStack.getItem() instanceof ItemBlock ? 1: 0.6F;

        GL11.glPushMatrix(); // start

        if (itemStack.getItem() instanceof ItemAnvil) {
            blockScale = 1;
            GL11.glTranslatef((float)x - 0.125f, (float)y - 0.125f, (float)z - 0.125f);
        }
        else {
            float timeD = (float) (360.0 * (System.currentTimeMillis() & 0x3FFFL) / 0x3FFFL);

            blockScale = 0.6F;

            GL11.glTranslatef((float)x, (float)y, (float)z);
            GL11.glRotatef(timeD, 0.0F, 1.0F, 0.0F);
        }

        GL11.glScalef(blockScale, blockScale, blockScale);
        customitem.setEntityItemStack(itemStack);
        itemRenderer.doRender(customitem, 0, 0, 0, 0, 0);
        GL11.glPopMatrix(); //end
	}


	@Override
	public void renderTileEntityAt(
        TileEntity par1TileEntity, double x, double y, double z, float f
    ) {
		this.renderAt((TileEntityBigStall)par1TileEntity, x, y, z, f);
	}
}
