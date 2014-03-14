package net.malisis.doors;

import java.util.List;

import net.malisis.core.MalisisCore;
import net.malisis.doors.renderer.DefaultBlockRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;

public class ShapedTestBlock extends Block
{

	public ShapedTestBlock(int id)
	{
		super(id, Material.grass);
		this.setCreativeTab(CreativeTabs.tabBlock);
		this.setTextureName("grass");
	}

	/**
	 * From the specified side and block metadata retrieves the blocks texture.
	 * Args: side, metadata
	 */
	public Icon getIcon(int side, int metadata)
	{
		return Block.dirt.getBlockTextureFromSide(side);
	}

	/**
	 * When this method is called, your block should register all the icons it
	 * needs with the given IconRegister. This is the only chance you get to
	 * register icons.
	 */
	public void registerIcons(IconRegister par1IconRegister)
	{

	}

	public AxisAlignedBB getBoundingBoxes()
	{
		return AxisAlignedBB.getBoundingBox(0.9F, 0, 0, 1, 1, 1);
	}

	@Override
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB mask, List list, Entity par7Entity)
	{
		int metadata = world.getBlockMetadata(x, y, z);
		AxisAlignedBB aabb = getBoundingBoxes();
		aabb = aabb.getOffsetBoundingBox(x, y, z);
		if (mask.intersectsWith(aabb))
		{
			MalisisCore.Message("Added " + aabb + "(mask : " + mask + ")");
			list.add(aabb);
		}
	}

	public void onBlockAdded(World world, int x, int y, int z)
	{
		// this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 2.0F, 1.0F);
		// world.markBlockForRenderUpdate(x, y, z);

	}

	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer p, int par6, float par7, float par8, float par9)
	{
		MalisisCore.Message(x + ", " + y + ", " + z);
		setBlockBounds(0, 0, 0, 1, 1, 1);

		return true;
	}

	public int getRenderType()
	{
		return DefaultBlockRenderer.renderId;
		// return 0;
	}

	public boolean canRenderInPass(int pass)
	{
		DefaultBlockRenderer.currentPass = pass;
		return true;
	}

	public int getRenderBlockPass()
	{
		return 0;
	}


	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

}
