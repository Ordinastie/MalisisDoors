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

package net.malisis.doors.door.block;

/**
 * @author Ordinastie
 *
 */

import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.malisis.doors.door.tileentity.FenceGateTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class FenceGate extends BlockFenceGate implements ITileEntityProvider
{
	public static int renderId = -1;

	public FenceGate()
	{
		setHardness(2.0F);
		setResistance(5.0F);
		setStepSound(soundTypeWood);
		setUnlocalizedName("fenceGate");
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack)
	{
		super.onBlockPlacedBy(world, x, y, z, player, itemStack);
		if (world.isRemote)
			return;

		DoorTileEntity te = Door.getDoor(world, x, y, z);
		if (te == null)
			return;

		((FenceGateTileEntity) te).updateCamo(world, x, y, z);
	}

	/**
	 * Called upon block activation (right click on the block.)
	 */
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote)
			return true;

		DoorTileEntity te = Door.getDoor(world, x, y, z);
		if (te == null)
			return true;

		boolean opened = te.isOpened();

		te.openOrCloseDoor();
		if (opened)
			return true;

		int dir = (MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3) % 4;
		if (dir == ((world.getBlockMetadata(x, y, z) & 3) + 2) % 4)
			world.setBlockMetadataWithNotify(x, y, z, dir, 2);

		te = te.getDoubleDoor();
		if (te != null)
		{
			if (dir == ((world.getBlockMetadata(te.xCoord, te.yCoord, te.zCoord) & 3) + 2) % 4)
				world.setBlockMetadataWithNotify(te.xCoord, te.yCoord, te.zCoord, dir, 2);
		}

		return true;
	}

	/**
	 * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are their own) Args: x,
	 * y, z, neighbor Block
	 */
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
	{
		DoorTileEntity te = Door.getDoor(world, x, y, z);
		if (te == null)
			return;

		((FenceGateTileEntity) te).updateCamo(world, x, y, z);

		if (world.isBlockIndirectlyGettingPowered(x, y, z) || block.canProvidePower())
			te.setPowered(te.isPowered());
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	{
		DoorTileEntity te = Door.getDoor(world, x, y, z);
		if (te == null || te.isMoving() || te.isOpened())
			return null;

		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2)
	{
		return new FenceGateTileEntity();
	}

	/**
	 * The type of render function that is called for this block
	 */
	@Override
	public int getRenderType()
	{
		return renderId;
	}

}
