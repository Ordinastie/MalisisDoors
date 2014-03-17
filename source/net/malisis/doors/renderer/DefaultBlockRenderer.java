package net.malisis.doors.renderer;

import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.element.RenderParameters;
import net.malisis.doors.block.VanishingBlock;
import net.malisis.doors.entity.VanishingTileEntity;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
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
//		if (block == MalisisDoors.blockTest)
//		{
//			//BaseRenderer mrenderer = new BaseRenderer(block, metadata);
//			//mrenderer.renderInventory();
//		}

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

//		else if (block == MalisisDoors.blockTest)
//		{
//			set(world, block, x, y, z, world.getBlockMetadata(x, y, z));
//			
//			prepare(TYPE_WORLD);
//			
//			if(blockMetadata == 0)
//			{
//				drawShape(Shape.TopSouthEast);
//			}
//			else
//			{
//			 	
//				final int START = 0;
//		    	final int MIN =  0;
//		    	final int END = 1;
//		    	final int MAX = 1;
//		    	boolean vertical = true;
//		    	int slices = 8;
//		    	float d = 1 / (float) slices;
//		    	float[][] fx = {{0, 1}, {0, 1}};
//		    	float[][] fy = {{0, 1}, {0, 1}};
//		    	float[][] fz = {{0, 1}, {0, 1}};
//
//		    	fx[START][MAX] = 0.6F;
//		    	fz[START][MAX] = 0.6F;
//				fz[END][MAX] = 0;
//				fx[END][MAX] = 0;
//		    	
//		    	AxisAlignedBB[] aabb = new AxisAlignedBB[slices];
//		    	for(int i = 0; i < slices; i++)
//		    	{
//		    		float bx = fx[START][MIN] + (fx[END][MIN] - fx[START][MIN]) * i * d;
//		    		float bX = fx[START][MAX] + (fx[END][MAX] - fx[START][MAX]) * i * d;
//		    		float by = fy[START][MIN] + (fy[END][MIN] - fy[START][MIN]) * i * d;
//		    		float bY = fy[START][MAX] + (fy[END][MAX] - fy[START][MAX]) * i * d;
//		    		float bz = fz[START][MIN] + (fz[END][MIN] - fz[START][MIN]) * i * d;
//		    		float bZ = fz[START][MAX] + (fz[END][MAX] - fz[START][MAX]) * i * d;
//		    		        	
//		    		if(vertical)
//		    		{
//			    		by = i * d;
//			    		bY = by + d;
//		    		}
//		    		else
//		    		{
//		    			bx = i * d;
//		    			bX = bx + d;
//		    		}
//		    		
//		    		aabb[i] = AxisAlignedBB.getBoundingBox(bx, by, bz, bX, bY, bZ);
//		    	}
//		    	
//		    	  for(AxisAlignedBB a : aabb)
//		          {
//		      //    	a = a.getOffsetBoundingBox(x, y, z);
//		  	        block.setBlockBounds((float) a.minX, (float) a.minY, (float) a.minZ, (float) a.maxX, (float) a.maxY, (float) a.maxZ);
//		  	        drawShape(Shape.Cube);
//		          }
//		    	  
//		    	  block.setBlockBounds(0, 0, 0, 1, 1, 1);
//		    	  drawShape(Shape.TopSouthEast);
//			}
//			
//
//			t.draw();
//			IModelCustom model = AdvancedModelLoader.loadModel("assets/" + MalisisDoors.modid  + "/models/block_test2.obj");
//			model.renderAll();
//			t.startDrawingQuads();
//			
//			
//			clean();
//
//		}

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
