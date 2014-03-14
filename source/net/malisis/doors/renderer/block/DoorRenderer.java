package net.malisis.doors.renderer.block;

import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.element.RenderParameters;
import net.malisis.core.renderer.element.Shape;
import net.malisis.doors.block.Door;
import net.malisis.doors.entity.DoorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.world.IBlockAccess;
import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;

public class DoorRenderer extends BaseRenderer implements ISimpleBlockRenderingHandler
{
    public static int renderId;
    public Door block;
    

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId, RenderBlocks renderer)
    {
        set(world, block, x, y, z, ((Door) block).getFullMetadata(world, x, y, z));
        this.block = (Door) block;
    	boolean topBlock = (blockMetadata & Door.flagTopBlock) != 0;
		DoorTileEntity te = (DoorTileEntity) world.getBlockTileEntity(x, y - (topBlock ? 1 : 0), z);
		if(te != null && te.moving)
			return true;

        prepare(TYPE_WORLD);

        RenderParameters rp = RenderParameters.Default();
        rp.calculateAOColor = false;
        rp.useBlockBounds = false;
        rp.renderBounds = this.block.calculateBlockBoundsD(blockMetadata,  false);

        drawShape(Shape.Cube, rp);

        clean();

        return false;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer)
    {

    }

    @Override
    public boolean shouldRender3DInInventory()
    {
        return false;
    }

    @Override
    public int getRenderId()
    {
        return 0;
    }

}
