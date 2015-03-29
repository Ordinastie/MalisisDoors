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

import net.malisis.core.util.MultiBlock;
import net.malisis.doors.door.DoorDescriptor;
import net.malisis.doors.door.DoorRegistry;
import net.malisis.doors.door.DoorState;
import net.malisis.doors.door.block.Door;
import net.malisis.doors.door.movement.ForcefieldMovement;
import net.malisis.doors.door.sound.SilentDoorSound;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
public class ForcefieldTileEntity extends DoorTileEntity implements MultiBlock.IProvider
{
	private MultiBlock multiBlock;

	public ForcefieldTileEntity()
	{
		DoorDescriptor descriptor = new DoorDescriptor();
		descriptor.setMovement(DoorRegistry.getMovement(ForcefieldMovement.class));
		descriptor.setSound(DoorRegistry.getSound(SilentDoorSound.class));
		descriptor.setDoubleDoor(false);
		descriptor.setOpeningTime(0);
		setDescriptor(descriptor);
	}

	private int getOriginMetadata()
	{
		if (multiBlock == null)
			return 0;
		return getWorld().getBlockMetadata(multiBlock.getX(), multiBlock.getY(), multiBlock.getZ());
	}

	@Override
	public boolean isTopBlock(int x, int y, int z)
	{
		return false;
	}

	@Override
	public int getDirection()
	{
		return multiBlock.getDirection().ordinal();
	}

	@Override
	public boolean isOpened()
	{
		return (getOriginMetadata() & Door.FLAG_OPENED) != 0;
	}

	@Override
	public boolean isReversed()
	{
		return false;
	}

	@Override
	public boolean isPowered()
	{
		return false;
	}

	@Override
	public DoorState getState()
	{
		ForcefieldTileEntity te = MultiBlock.getOriginProvider(this);
		if (te == null)
			return DoorState.CLOSED;

		if (te != this)
			return te.getState();

		return super.getState();
	}

	@Override
	public void openOrCloseDoor()
	{
		ForcefieldTileEntity te = MultiBlock.getOriginProvider(this);
		if (te == null)
			return;

		if (te != this)
		{
			te.openOrCloseDoor();
			return;
		}

		if (getState() != DoorState.CLOSED && getState() != DoorState.OPENED)
			return;

		super.openOrCloseDoor();
	}

	@Override
	public void setMultiBlock(MultiBlock multiBlock)
	{
		this.multiBlock = multiBlock;
	}

	@Override
	public MultiBlock getMultiBlock()
	{
		return multiBlock;
	}

	@Override
	public void setWorldObj(World world)
	{
		super.setWorldObj(world);
		if (multiBlock != null)
			multiBlock.setWorld(world);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);
		multiBlock = new MultiBlock(tag);
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
		if (multiBlock != null)
			multiBlock.writeToNBT(tag);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		if (multiBlock != null)
			return multiBlock.getWorldBounds();
		return super.getRenderBoundingBox();
	}

}
