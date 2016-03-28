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

package net.malisis.doors.tileentity;

import net.malisis.core.block.component.DirectionalComponent;
import net.malisis.doors.DoorDescriptor;
import net.malisis.doors.DoorRegistry;
import net.malisis.doors.DoorState;
import net.malisis.doors.block.RustyHatch;
import net.malisis.doors.movement.RustyHatchMovement;
import net.malisis.doors.sound.RustyHatchSound;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

/**
 * @author Ordinastie
 *
 */
public class RustyHatchTileEntity extends DoorTileEntity
{
	private boolean isTop = false;

	public RustyHatchTileEntity()
	{
		DoorDescriptor descriptor = new DoorDescriptor();
		descriptor.setMovement(DoorRegistry.getMovement(RustyHatchMovement.class));
		descriptor.setSound(DoorRegistry.getSound(RustyHatchSound.class));
		descriptor.setDoubleDoor(false);
		descriptor.setOpeningTime(60);
		setDescriptor(descriptor);
	}

	@Override
	public IBlockState getBlockState()
	{
		return null;
	}

	public boolean isTop()
	{
		IBlockState state = worldObj.getBlockState(pos);
		return state.getBlock() == getBlockType() && (boolean) state.getValue(RustyHatch.TOP);
	}

	@Override
	public EnumFacing getDirection()
	{
		return DirectionalComponent.getDirection(worldObj, pos);
	}

	@Override
	public boolean isOpened()
	{
		return state == DoorState.OPENED;
	}

	@Override
	public boolean isPowered()
	{
		return false;
	}

	public boolean shouldLadder(BlockPos pos)
	{
		//		if (!isOpened()/* && y == multiBlock.getY()*/)
		//			return false;

		EnumFacing dir = getDirection();

		if (pos.getZ() == this.pos.getZ() && (dir == EnumFacing.NORTH || dir == EnumFacing.SOUTH))
			return false;
		if (pos.getX() == this.pos.getX() && (dir == EnumFacing.WEST || dir == EnumFacing.EAST))
			return false;

		return getWorld().isSideSolid(pos.offset(dir), dir.getOpposite());
	}

	@Override
	public void openOrCloseDoor()
	{
		if (getState() != DoorState.CLOSED && getState() != DoorState.OPENED)
			return;

		super.openOrCloseDoor();
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		return INFINITE_EXTENT_AABB;
		//return TileEntityUtils.getRenderingBounds(this);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		isTop = nbt.getBoolean("top");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setBoolean("top", isTop);
	}
}
