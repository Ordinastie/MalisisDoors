package net.malisis.doors.renderer;

import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.element.RenderParameters;
import net.malisis.core.renderer.preset.ShapePreset;
import net.malisis.doors.block.VanishingBlock;
import net.malisis.doors.entity.VanishingTileEntity;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;

public class VanishingBlockRenderer extends BaseRenderer
{
	public static int renderId;


	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
	{
		set(block, metadata);
		RenderParameters rp = new RenderParameters();
		rp.useBlockBounds = false;
		prepare(TYPE_ISBRH_INVENTORY);
		drawShape(ShapePreset.Cube(), rp);
		clean();
	}

	@Override
	public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
	{
		if ((world.getBlockMetadata(x, y, z) & (VanishingBlock.flagPowered | VanishingBlock.flagInTransition)) != 0)
			return true;
		
		set(world, block, x, y, z, world.getBlockMetadata(x, y, z));

		VanishingTileEntity te = (VanishingTileEntity) world.getTileEntity(x, y, z);
        if (te.copiedBlock != null)
        	set(te.copiedBlock, te.copiedMetadata);
		
        prepare(TYPE_WORLD);
        
        if(renderer.hasOverrideBlockTexture())
		{
			RenderParameters rp = RenderParameters.setDefault();
			rp.icon = renderer.overrideBlockTexture;
			drawShape(ShapePreset.Cube(), rp);
		}
        else        
        	drawShape(ShapePreset.Cube());
		clean();

		return true;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return true;
	}
}
