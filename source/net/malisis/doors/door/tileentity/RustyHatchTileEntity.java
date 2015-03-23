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
import net.malisis.doors.door.movement.RustyHatchMovement;
import net.malisis.doors.door.sound.RustyHatchSound;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @author Ordinastie
 *
 */
public class RustyHatchTileEntity extends DoorTileEntity implements MultiBlock.IProvider
{
	private MultiBlock multiBlock;

	public RustyHatchTileEntity()
	{
		DoorDescriptor descriptor = new DoorDescriptor();
		descriptor.setMovement(DoorRegistry.getMovement(RustyHatchMovement.class));
		descriptor.setSound(DoorRegistry.getSound(RustyHatchSound.class));
		descriptor.setDoubleDoor(false);
		descriptor.setOpeningTime(60);
		setDescriptor(descriptor);
	}

	private int getOriginMetadata()
	{
		if (getWorld() == null || multiBlock == null)
			return 0;
		return getWorld().getBlockMetadata(multiBlock.getX(), multiBlock.getY(), multiBlock.getZ());
	}

	@Override
	public boolean isTopBlock(int x, int y, int z)
	{
		return (getOriginMetadata() & Door.FLAG_TOPBLOCK) != 0;
	}

	@Override
	public int getDirection()
	{
		return (getOriginMetadata() & 3) + 2;
	}

	@Override
	public boolean isOpened()
	{
		return (getOriginMetadata() & Door.FLAG_OPENED) != 0;
	}

	@Override
	public boolean isReversed()
	{
		return (getOriginMetadata() & Door.FLAG_REVERSED) != 0;
	}

	@Override
	public boolean isPowered()
	{
		return false;
	}

	public boolean shouldLadder(int x, int y, int z)
	{
		//		if (!isOpened()/* && y == multiBlock.getY()*/)
		//			return false;

		ForgeDirection dir = ForgeDirection.getOrientation(getDirection());

		if (z == zCoord && (dir == ForgeDirection.NORTH || dir == ForgeDirection.SOUTH))
			return false;
		if (x == xCoord && (dir == ForgeDirection.WEST || dir == ForgeDirection.EAST))
			return false;

		return getWorld().isSideSolid(x + dir.offsetX, y, z + dir.offsetZ, dir.getOpposite());
	}

	@Override
	public void openOrCloseDoor()
	{
		RustyHatchTileEntity te = MultiBlock.getOriginProvider(this);
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
