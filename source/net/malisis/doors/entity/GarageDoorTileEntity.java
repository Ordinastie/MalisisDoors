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

import net.malisis.doors.block.doors.DoorHandler;
import net.malisis.doors.block.doors.GarageDoor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @author Ordinastie
 * 
 */
public class GarageDoorTileEntity extends TileEntity
{
	public static final int maxOpenTime = 10;

	protected boolean removed = false;
	protected GarageDoorTileEntity topDoor;
	protected Set<GarageDoorTileEntity> childDoors = new HashSet<>();
	protected int state;
	public boolean draw = false;
	public long startTime;

	public GarageDoorTileEntity()
	{

	}

	public int getDirection()
	{
		return blockMetadata & 3;
	}

	public int getState()
	{
		return state;
	}

	public boolean isTopDoor()
	{
		return this == getTopDoor();
	}

	public GarageDoorTileEntity getTopDoor()
	{
		if (topDoor == null)
			add();
		return topDoor;
	}

	public Set<GarageDoorTileEntity> getChildDoors()
	{
		return childDoors;
	}

	public GarageDoorTileEntity getGarageDoor(ForgeDirection dir)
	{
		TileEntity te = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
		if (te == null)
			return null;
		if (!(te instanceof GarageDoorTileEntity))
			return null;
		GarageDoorTileEntity door = (GarageDoorTileEntity) te;
		if (door.getDirection() != getDirection())
			return null;
		if (door.removed)
			return null;

		return (GarageDoorTileEntity) te;
	}

	public GarageDoorTileEntity findTopBlock()
	{
		GarageDoorTileEntity te = this;
		GarageDoorTileEntity ret = this;
		while ((te = te.getGarageDoor(ForgeDirection.UP)) != null)
			ret = te;

		return ret;
	}

	public void setTopBlock(GarageDoorTileEntity topDoor)
	{
		childDoors.clear();
		this.topDoor = topDoor;
		if (isTopDoor())
		{
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, getBlockMetadata() | DoorHandler.flagTopBlock, 2);
			GarageDoorTileEntity te = this;
			while ((te = te.getGarageDoor(ForgeDirection.DOWN)) != null)
			{
				te.setTopBlock(this);
				childDoors.add(te);
			}
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		}
		else
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, getBlockMetadata() & ~DoorHandler.flagTopBlock, 2);
	}

	public void add()
	{
		GarageDoorTileEntity te = findTopBlock();
		te.setTopBlock(te);
	}

	public void remove()
	{
		this.removed = true;
		GarageDoorTileEntity te = getGarageDoor(ForgeDirection.DOWN);
		if (te != null)
			te.setTopBlock(te);

		te = getGarageDoor(ForgeDirection.UP);
		if (te != null)
		{
			te = te.findTopBlock();
			te.setTopBlock(te);
		}
	}

	public void changeState()
	{
		if (!isTopDoor())
		{
			getTopDoor().changeState();
			return;
		}

		if (state == DoorHandler.stateOpening || state == DoorHandler.stateClosing)
			return;

		startTime = worldObj.getTotalWorldTime();
		if (state == DoorHandler.stateClose)
			setState(DoorHandler.stateOpening);
		else if (state == DoorHandler.stateOpen)
			setState(DoorHandler.stateClosing);

		GarageDoorTileEntity te = getGarageDoor(GarageDoor.isEastOrWest(blockMetadata) ? ForgeDirection.NORTH : ForgeDirection.EAST);
		if (te != null)
			te.changeState();
		te = getGarageDoor(GarageDoor.isEastOrWest(blockMetadata) ? ForgeDirection.SOUTH : ForgeDirection.WEST);
		if (te != null)
			te.changeState();

		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	private void setState(int newState)
	{
		if (state == newState)
			return;

		state = newState;

		if (getWorldObj() != null)
		{
			if (state == DoorHandler.stateClose)
				worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, blockMetadata & ~DoorHandler.flagOpened, 2);
			else
				worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, blockMetadata | DoorHandler.flagOpened, 2);
		}

		if (isTopDoor())
		{
			for (GarageDoorTileEntity te : childDoors)
				te.setState(newState);
		}
	}

	@Override
	public void updateEntity()
	{
		if (!isTopDoor())
			return;

		if (state == DoorHandler.stateClose || state == DoorHandler.stateOpen)
			return;

		if (startTime + (childDoors.size() + 1) * maxOpenTime < worldObj.getTotalWorldTime())
		{
			if (state == DoorHandler.stateClosing)
				setState(DoorHandler.stateClose);
			else if (state == DoorHandler.stateOpening)
				setState(DoorHandler.stateOpen);
		}
	}

	/**
	 * Specify the bounding box ourselves otherwise, the block bounding box would be use. (And it should be at this point {0, 0, 0})
	 */
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		return AxisAlignedBB.getBoundingBox(xCoord - childDoors.size(), yCoord - childDoors.size(), zCoord - childDoors.size(), xCoord
				+ childDoors.size() + 1, yCoord + 1, zCoord + childDoors.size() + 1);
	}

	@Override
	public void writeToNBT(NBTTagCompound tag)
	{
		super.writeToNBT(tag);
		tag.setInteger("state", state);
		tag.setLong("startTime", startTime);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag)
	{
		super.readFromNBT(tag);
		state = tag.getInteger("state");
		startTime = tag.getLong("startTime");
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		add();
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, nbt);

	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
	{
		this.readFromNBT(packet.func_148857_g());
		add();
	}

}
