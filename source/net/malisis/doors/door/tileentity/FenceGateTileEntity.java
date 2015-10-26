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

package net.malisis.doors.door.tileentity;

import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.door.DoorDescriptor;
import net.malisis.doors.door.DoorRegistry;
import net.malisis.doors.door.movement.FenceGateMovement;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;

/**
 * @author Ordinastie
 */
public class FenceGateTileEntity extends DoorTileEntity
{
	public FenceGateTileEntity()
	{
		DoorDescriptor descriptor = new DoorDescriptor();
		descriptor.setMovement(DoorRegistry.getMovement(FenceGateMovement.class));
		setDescriptor(descriptor);
	}

	@Override
	public IBlockState getBlockState()
	{
		return worldObj.getBlockState(pos);
	}

	@Override
	public EnumFacing getDirection()
	{
		return (EnumFacing) getBlockState().getValue(BlockTrapDoor.FACING);
	}

	public boolean isWall()
	{
		EnumFacing dir = getDirection().rotateY();
		IBlockState state = worldObj.getBlockState(pos.offset(dir));
		if (state.getBlock() == Blocks.cobblestone_wall)
			return true;
		state = worldObj.getBlockState(pos.offset(dir.getOpposite()));
		if (state.getBlock() == Blocks.cobblestone_wall)
			return true;
		return false;
	}

	public IBlockState getNeighborsState()
	{
		EnumFacing dir = getDirection().rotateY();

		IBlockState state1 = worldObj.getBlockState(pos.offset(dir));
		FenceGateTileEntity te = TileEntityUtils.getTileEntity(FenceGateTileEntity.class, worldObj, pos.offset(dir));
		if (isMatchingDoubleDoor(te))
			state1 = worldObj.getBlockState(pos.offset(dir, 2));

		IBlockState state2 = worldObj.getBlockState(pos.offset(dir.getOpposite()));
		te = TileEntityUtils.getTileEntity(FenceGateTileEntity.class, worldObj, pos.offset(dir.getOpposite()));
		if (isMatchingDoubleDoor(te))
			state2 = worldObj.getBlockState(pos.offset(dir.getOpposite(), 2));

		return state1.equals(state2) && state1.getBlock() != Blocks.air ? state1 : null;
	}

	@Override
	public FenceGateTileEntity getDoubleDoor()
	{
		if (!descriptor.isDoubleDoor())
			return null;

		EnumFacing dir = getDirection().rotateY();
		TileEntity te = TileEntityUtils.getTileEntity(FenceGateTileEntity.class, worldObj, pos.offset(dir));
		if (te != null && isMatchingDoubleDoor((FenceGateTileEntity) te))
			return (FenceGateTileEntity) te;

		te = TileEntityUtils.getTileEntity(FenceGateTileEntity.class, worldObj, pos.offset(dir.getOpposite()));
		if (te != null && isMatchingDoubleDoor((FenceGateTileEntity) te))
			return (FenceGateTileEntity) te;

		return null;
	}

	@Override
	public boolean isMatchingDoubleDoor(DoorTileEntity te)
	{
		if (te == null)
			return false;

		if (getBlockType() != te.getBlockType()) // different block
			return false;

		if (getDirection().getAxis() != te.getDirection().getAxis()) // different direction
			return false;

		if (isOpened() != te.isOpened()) // different state
			return false;

		return true;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		return new AxisAlignedBB(pos, pos.add(1, 1, 1));
	}
}
