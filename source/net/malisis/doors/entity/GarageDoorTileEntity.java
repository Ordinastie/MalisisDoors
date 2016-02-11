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

package net.malisis.doors.entity;

import java.util.HashSet;
import java.util.Set;

import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.block.GarageDoor;
import net.malisis.doors.door.DoorState;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @author Ordinastie
 *
 */
public class GarageDoorTileEntity extends DoorTileEntity
{
	public static final int maxOpenTime = 10;

	public GarageDoorTileEntity()
	{

	}

	public boolean isTopDoor()
	{
		return this == getTopDoor();
	}

	@Override
	public int getOpeningTime()
	{
		return getDoors().size() * maxOpenTime;
	}

	public GarageDoorTileEntity getTopDoor()
	{
		GarageDoorTileEntity topDoor = getGarageDoor(ForgeDirection.UP);
		return topDoor != null ? topDoor.getTopDoor() : this;
	}

	public Set<GarageDoorTileEntity> getDoors()
	{
		Set<GarageDoorTileEntity> childDoors = new HashSet<>();
		getTopDoor().addChildDoors(childDoors);
		return childDoors;
	}

	public void addChildDoors(Set<GarageDoorTileEntity> childDoors)
	{
		childDoors.add(this);
		GarageDoorTileEntity bottomDoor = getGarageDoor(ForgeDirection.DOWN);
		if (bottomDoor != null)
			bottomDoor.addChildDoors(childDoors);
	}

	public GarageDoorTileEntity getGarageDoor(ForgeDirection dir)
	{
		GarageDoorTileEntity te = TileEntityUtils.getTileEntity(GarageDoorTileEntity.class, getWorld(), xCoord + dir.offsetX, yCoord
				+ dir.offsetY, zCoord + dir.offsetZ);
		if (te == null)
			return null;
		if (te.getDirection() != getDirection())
			return null;

		return te;
	}

	@Override
	public void setPowered(boolean powered)
	{
		if (isMoving())
			return;
		if (isOpened() == powered)
			return;
		if ((state == DoorState.OPENING && powered) || (state == DoorState.CLOSING && !powered))
			return;

		DoorState newState = powered ? DoorState.OPENING : DoorState.CLOSING;
		for (GarageDoorTileEntity te : getDoors())
		{
			te.setDoorState(newState);
		}

		GarageDoorTileEntity te = getGarageDoor(GarageDoor.isEastOrWest(blockMetadata) ? ForgeDirection.NORTH : ForgeDirection.EAST);
		if (te != null)
			te.setPowered(powered);
		te = getGarageDoor(GarageDoor.isEastOrWest(blockMetadata) ? ForgeDirection.SOUTH : ForgeDirection.WEST);
		if (te != null)
			te.setPowered(powered);
	}

	@Override
	public void playSound()
	{}

	@Override
	public void updateEntity()
	{
		if (state == DoorState.CLOSED || state == DoorState.OPENED)
			return;

		if (timer.elapsedTick() > getOpeningTime())
			setDoorState(state == DoorState.CLOSING ? DoorState.CLOSED : DoorState.OPENED);
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		Set<GarageDoorTileEntity> childDoors = getDoors();
		return AxisAlignedBB.getBoundingBox(xCoord - childDoors.size(), yCoord - childDoors.size(), zCoord - childDoors.size(), xCoord
				+ childDoors.size() + 1, yCoord + 1, zCoord + childDoors.size() + 1);
	}

}
