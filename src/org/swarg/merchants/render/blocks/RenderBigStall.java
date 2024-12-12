package org.swarg.merchants.render.blocks;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import org.lwjgl.opengl.GL11;

import com.aleksey.merchants.Render.Blocks.Bound;
import org.swarg.merchants.blocks.devices.BlockBigStall;

import com.bioxx.tfc.api.TFCBlocks;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;


/**
 * 12-12-2024
 * @author Swarg
 */
public class RenderBigStall implements ISimpleBlockRenderingHandler
{
    public static final double voxelSizeScaled = 0.0625; // 1/16

    private static final Bound[] caseBounds = new Bound[] {
        new Bound(0, 0, 0, 1, voxelSizeScaled, 1), // bottom
        new Bound(0, voxelSizeScaled, voxelSizeScaled, voxelSizeScaled, 10 * voxelSizeScaled, 1 - voxelSizeScaled), // left
        new Bound(0, voxelSizeScaled, 0, 1, 10 * voxelSizeScaled, voxelSizeScaled), // back
        new Bound(1 - voxelSizeScaled, voxelSizeScaled, voxelSizeScaled, 1, 10 * voxelSizeScaled, 1 - voxelSizeScaled), // right
        new Bound(0, voxelSizeScaled, 1 - voxelSizeScaled, 1, 10 * voxelSizeScaled, 1), // forward
    };


    private static final Bound topBound =
            new Bound(voxelSizeScaled, 6 * voxelSizeScaled, voxelSizeScaled, 1 - voxelSizeScaled, 7 * voxelSizeScaled, 1 - voxelSizeScaled);


    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
    {
        renderer.overrideBlockTexture = getWoodTexture(block);

        for (int i = 0; i < caseBounds.length; i++) {
            setBound(caseBounds[i], renderer);

            renderInvBlock(block, metadata, renderer);
        }

        renderer.clearOverrideBlockTexture();

        setBound(topBound, renderer);

        renderInvBlock(block, 0, renderer);
    }


    @Override
    public boolean renderWorldBlock(
        IBlockAccess world, int x, int y, int z,
        Block block, int modelId, RenderBlocks renderer
    ) {
        renderer.overrideBlockTexture = getWoodTexture(block);

        for (int i = 0; i < caseBounds.length; i++) {
            setBound(caseBounds[i], renderer);

            renderer.renderStandardBlock(block, x, y, z);
        }

        renderer.clearOverrideBlockTexture();

        setBound(topBound, renderer);

        renderer.renderStandardBlock(block, x, y, z);

        return true;
    }


    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }


    @Override
    public int getRenderId() {
        return 0;
    }


    private static void renderInvBlock(Block block, int m, RenderBlocks renderer)
    {
        Tessellator t = Tessellator.instance;

        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        t.startDrawingQuads();
        t.setNormal(0.0F, -1.0F, 0.0F);
        renderer.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(0, m));
        t.draw();
        t.startDrawingQuads();
        t.setNormal(0.0F, 1.0F, 0.0F);
        renderer.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(1, m));
        t.draw();
        t.startDrawingQuads();
        t.setNormal(-1.0F, 0.0F, 0.0F);
        renderer.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(2, m));
        t.draw();
        t.startDrawingQuads();
        t.setNormal(0.0F, 0.0F, -1.0F);
        renderer.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(3, m));
        t.draw();
        t.startDrawingQuads();
        t.setNormal(-1.0F, 0.0F, 0.0F);
        renderer.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(4, m));
        t.draw();
        t.startDrawingQuads();
        t.setNormal(0.0F, 0.0F, 1.0F);
        renderer.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(5, m));
        t.draw();
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
    }


    private static void setBound(Bound bound, RenderBlocks renderer) {
        renderer.setRenderBounds(bound.MinX, bound.MinY, bound.MinZ, bound.MaxX, bound.MaxY, bound.MaxZ);
    }


    private static IIcon getWoodTexture(Block block) {
        BlockBigStall stall = (BlockBigStall)block;
        int woodIndex = stall.getWoodIndex();

        return woodIndex < 16
                ? TFCBlocks.planks.getIcon(0, woodIndex)
                : TFCBlocks.planks2.getIcon(0, woodIndex - 16);
    }
}
