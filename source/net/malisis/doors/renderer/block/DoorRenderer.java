package net.malisis.doors.renderer.block;

import org.lwjgl.opengl.GL11;

import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.element.RenderParameters;
import net.malisis.core.renderer.preset.ShapePreset;
import net.malisis.doors.block.Door;
import net.malisis.doors.entity.DoorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

public class DoorRenderer extends BaseRenderer
{
	public static int renderId;
	public Door block;

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		set(world, block, x, y, z, ((Door) block).getFullMetadata(world, x, y, z));
		this.block = (Door) block;
		boolean topBlock = (blockMetadata & Door.flagTopBlock) != 0;
		DoorTileEntity te = (DoorTileEntity) world.getTileEntity(x, y - (topBlock ? 1 : 0), z);
		if (te != null && te.moving)
		{
			te.draw = true;
			return false;
		}	

		prepare(TYPE_WORLD);

				
		RenderParameters rp = RenderParameters.setDefault();
		rp.calculateAOColor = false;
		rp.useBlockBounds = false;
		rp.renderBounds = this.block.calculateBlockBoundsD(blockMetadata, false);

		if(renderer.hasOverrideBlockTexture())
		{
			GL11.glTranslatef(0, (blockMetadata & Door.flagTopBlock) != 0 ? -0.5F : 0.5F, 0);			
			rp.icon = renderer.overrideBlockTexture;
		}
		
		drawShape(ShapePreset.Cube(), rp);

		clean();
		return true;
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
	{

	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return false;
	}

}
