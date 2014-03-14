package net.malisis.doors.renderer;

import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.element.RenderParameters;
import net.malisis.core.renderer.element.Shape;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.ShapedTestBlock;
import net.malisis.doors.block.VanishingBlock;
import net.malisis.doors.entity.VanishingTileEntity;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class DefaultBlockRenderer extends BaseRenderer implements ISimpleBlockRenderingHandler
{
	public static int renderId;
	public static int vanishingBlockRenderId;
	public static int currentPass;

	public DefaultBlockRenderer()
	{
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
	{
		if (block == MalisisDoors.blockTest)
		{
			//BaseRenderer mrenderer = new BaseRenderer(block, metadata);
			//mrenderer.renderInventory();
		}

		if (block.getRenderType() == vanishingBlockRenderId)
		{
			set(block, metadata);
			RenderParameters rp = new RenderParameters();
			rp.useBlockBounds = false;
			renderInventoryBlock(rp);
		}
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		set(world, block, x, y, z, world.getBlockMetadata(x, y, z));

		if (block.getRenderType() == vanishingBlockRenderId)
		{
			if ((world.getBlockMetadata(x, y, z) & (VanishingBlock.flagPowered | VanishingBlock.flagInTransition)) != 0)
				return true;

			VanishingTileEntity te = (VanishingTileEntity) world.getBlockTileEntity(x, y, z);
	        if (te.copiedBlock != null)
	        	set(te.copiedBlock, te.copiedMetadata);
			super.renderWorldBlock();
		}

		else if (block == MalisisDoors.blockTest)
		{
			prepare(TYPE_WORLD);
			
			set(world, block, x, y, z, world.getBlockMetadata(x, y, z));
			
			AxisAlignedBB aabb = ((ShapedTestBlock) block).getBoundingBoxes();
			block.setBlockBounds((float) aabb.minX, (float) aabb.minY, (float) aabb.minZ, (float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ);
			drawShape(Shape.Cube);
			
			clean();

		}

		return true;
	}

	@Override
	public boolean shouldRender3DInInventory()
	{
		return true;
	}

	@Override
	public int getRenderId()
	{
		return DefaultBlockRenderer.renderId;
	}

}
