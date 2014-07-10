/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.malisis.doors.block.doors;

import net.malisis.core.renderer.IBaseRendering;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.entity.GarageDoorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 * 
 */
public class GarageDoor extends Block implements ITileEntityProvider, IBaseRendering
{
	private IIcon topBlockIcon;
	private int renderId = -1;

	public GarageDoor()
	{
		super(Material.wood);
		setCreativeTab(CreativeTabs.tabRedstone);
	}

	@Override
	public void registerBlockIcons(IIconRegister iconRegister)
	{
		this.blockIcon = iconRegister.registerIcon(MalisisDoors.modid + ":" + (this.getUnlocalizedName().substring(5)));
		this.topBlockIcon = iconRegister.registerIcon(MalisisDoors.modid + ":" + (this.getUnlocalizedName().substring(5)) + "_top");
	}

	@Override
	public IIcon getIcon(int side, int metadata)
	{
		if ((metadata & DoorHandler.flagTopBlock) != 0)
			return topBlockIcon;
		else
			return blockIcon;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack)
	{
		int metadata = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F - 0.5F) & 3;
		Block block = world.getBlock(x, y + 1, z);
		if (block instanceof GarageDoor)
			metadata = world.getBlockMetadata(x, y + 1, z) & 3;
		else
		{
			block = world.getBlock(x, y - 1, z);
			if (block instanceof GarageDoor)
				metadata = world.getBlockMetadata(x, y - 1, z) & 3;
		}

		world.setBlockMetadataWithNotify(x, y, z, metadata, 2);
		setBlockBoundsBasedOnState(world, x, y, z);

		TileEntity te = world.getTileEntity(x, y, z);
		if (te == null)
			return;
		((GarageDoorTileEntity) te).add();
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
	{
		if ((world.isBlockIndirectlyGettingPowered(x, y, z) || block.canProvidePower()) && block != this)
		{
			TileEntity te = world.getTileEntity(x, y, z);
			if (te != null)
			{
				((GarageDoorTileEntity) te).changeState();
			}
		}

	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int metadata)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if (te == null || !(te instanceof GarageDoorTileEntity))
			return;

		((GarageDoorTileEntity) te).remove();

		world.removeTileEntity(x, y, z);
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		int metadata = world.getBlockMetadata(x, y, z);
		float w = DoorHandler.DOOR_WIDTH / 2;
		if ((metadata & DoorHandler.flagOpened) != 0)
			setBlockBounds(0, 0, 0, 0, 0, 0);
		else if (isEastOrWest(metadata))
			setBlockBounds(0.5F - w, 0, 0, 0.5F + w, 1, 1);
		else
			setBlockBounds(0, 0, 0.5F - w, 1, 1, 0.5F + w);
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 src, Vec3 dest)
	{
		setBlockBoundsBasedOnState(world, x, y, z);
		return super.collisionRayTrace(world, x, y, z, src, dest);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
	{
		setBlockBoundsBasedOnState(world, x, y, z);
		return super.getSelectedBoundingBoxFromPool(world, x, y, z);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	{
		int metadata = world.getBlockMetadata(x, y, z);
		if ((metadata & DoorHandler.flagOpened) != 0)
			return null;

		setBlockBoundsBasedOnState(world, x, y, z);
		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata)
	{
		return new GarageDoorTileEntity();
	}

	@Override
	public boolean isNormalCube(IBlockAccess world, int x, int y, int z)
	{
		return false;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public void setRenderId(int id)
	{
		renderId = id;
	}

	@Override
	public int getRenderType()
	{
		return renderId;
	}

	public static boolean isEastOrWest(int metadata)
	{
		return (metadata & 3) == DoorHandler.DIR_EAST || (metadata & 3) == DoorHandler.DIR_WEST;
	}
}
