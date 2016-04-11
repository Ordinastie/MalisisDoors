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
import net.malisis.core.util.MBlockState;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.core.util.chunkcollision.ChunkCollision;
import net.malisis.doors.DoorDescriptor;
import net.malisis.doors.DoorRegistry;
import net.malisis.doors.DoorState;
import net.malisis.doors.movement.CarriageDoorMovement;
import net.malisis.doors.sound.BigDoorSound;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import com.google.common.base.Objects;

/**
 * @author Ordinastie
 *
 */
public class BigDoorTileEntity extends DoorTileEntity
{
	private IBlockState frameState;

	public BigDoorTileEntity()
	{
		DoorDescriptor descriptor = new DoorDescriptor();
		descriptor.setMovement(DoorRegistry.getMovement(CarriageDoorMovement.class));
		descriptor.setSound(DoorRegistry.getSound(BigDoorSound.class));
		descriptor.setDoubleDoor(false);
		descriptor.setOpeningTime(20);
		setDescriptor(descriptor);

		frameState = Blocks.QUARTZ_BLOCK.getDefaultState();
	}

	public IBlockState getFrameState()
	{
		return frameState;
	}

	public void setFrameState(IBlockState state)
	{
		if (state != null)
			frameState = state;
	}

	@Override
	public EnumFacing getDirection()
	{
		return DirectionalComponent.getDirection(worldObj, pos);
	}

	@Override
	public IBlockState getBlockState()
	{
		return null;
	}

	@Override
	public boolean isOpened()
	{
		return state == DoorState.OPENED;
	}

	@Override
	public boolean isTopBlock(BlockPos pos)
	{
		return false;
	}

	@Override
	public boolean isHingeLeft()
	{
		return true;
	}

	@Override
	public boolean isPowered()
	{
		return false;
	}

	@Override
	public void setDoorState(DoorState newState)
	{
		boolean moving = this.moving;
		MBlockState state = null;
		if (getWorld() != null)
		{
			state = new MBlockState(pos, getBlockType());
			ChunkCollision.get().updateBlocks(getWorld(), state);
		}

		super.setDoorState(newState);
		if (getWorld() != null && moving && !this.moving)
			ChunkCollision.get().replaceBlocks(getWorld(), state);
	}

	public ItemStack getDroppedItemStack()
	{
		ItemStack itemStack = new ItemStack(getBlockType());
		NBTTagCompound nbt = new NBTTagCompound();
		MBlockState.toNBT(nbt, frameState);
		itemStack.setTagCompound(nbt);
		return itemStack;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		return TileEntityUtils.getRenderingBounds(this);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		MBlockState.toNBT(nbt, frameState);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		frameState = Objects.firstNonNull(MBlockState.fromNBT(nbt), Blocks.QUARTZ_BLOCK.getDefaultState());
	}
}
