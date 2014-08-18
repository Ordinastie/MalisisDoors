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

import net.malisis.doors.door.DoorState;
import net.malisis.doors.door.block.Door;
import net.malisis.doors.door.movement.IDoorMovement;
import net.malisis.doors.door.sound.IDoorSound;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 * 
 */
public class DoorTileEntity extends TileEntity
{
	private IDoorMovement movement;
	private IDoorSound doorSound;
	private int openingTime = 6;
	private boolean doubleDoor = true;
	private boolean requireRedstone = false;

	private boolean initialized = false;
	private int lastMetadata = -1;
	private long startTime;
	private int timer = 0;
	private DoorState state = DoorState.CLOSED;
	private boolean moving;

	public void init()
	{
		if (!initialized)
		{
			Block block = getBlockType();
			if (block instanceof Door)
				((Door) block).setTileEntityInformations(this);
		}
	}

	//#region Getter/Setter
	public long getStartTime()
	{
		return startTime;
	}

	public void setStartTime(long startTime)
	{
		this.startTime = startTime;
	}

	public int getTimer()
	{
		return timer;
	}

	public void setTimer(int timer)
	{
		this.timer = timer;
	}

	public DoorState getState()
	{
		return state;
	}

	public void setState(DoorState state)
	{
		this.state = state;
	}

	public boolean isMoving()
	{
		return moving;
	}

	public void setMoving(boolean moving)
	{
		this.moving = moving;
	}

	public IDoorMovement getMovement()
	{
		return movement;
	}

	public void setMovement(IDoorMovement movement)
	{
		this.movement = movement;
	}

	public IDoorSound getDoorSound()
	{
		return doorSound;
	}

	public void setDoorSound(IDoorSound doorSound)
	{
		this.doorSound = doorSound;
	}

	public int getOpeningTime()
	{
		return openingTime;
	}

	public void setOpeningTime(int openingTime)
	{
		this.openingTime = openingTime;
	}

	public boolean isDoubleDoor()
	{
		return doubleDoor;
	}

	public void setDoubleDoor(boolean doubleDoor)
	{
		this.doubleDoor = doubleDoor;
	}

	public boolean requireRedstone()
	{
		return requireRedstone;
	}

	public void setRequireRedstone(boolean requireRedstone)
	{
		this.requireRedstone = requireRedstone;
	}

	public int getDirection()
	{
		return getBlockMetadata() & 3;
	}

	public boolean isTopBlock(int x, int y, int z)
	{
		return x == xCoord && y == yCoord + 1 && z == zCoord;
	}

	@Override
	public int getBlockMetadata()
	{
		if (lastMetadata != blockMetadata || blockMetadata == -1)
		{
			blockMetadata = Door.getFullMetadata(worldObj, xCoord, yCoord, zCoord);
			lastMetadata = blockMetadata;
		}

		return blockMetadata;
	}

	public boolean isOpened()
	{
		return (getBlockMetadata() & Door.FLAG_OPENED) != 0;
	}

	public boolean isReversed()
	{
		return (getBlockMetadata() & Door.FLAG_REVERSED) != 0;
	}

	public boolean isPowered()
	{
		return getWorldObj().isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)
				|| getWorldObj().isBlockIndirectlyGettingPowered(xCoord, yCoord + 1, zCoord);
	}

	//#end Getter/Setter

	/**
	 * Open or close this DoorTileEntity
	 */
	public void openOrCloseDoor()
	{
		DoorState newState = state == DoorState.OPENED ? DoorState.CLOSING : DoorState.OPENING;
		setDoorState(newState);

		DoorTileEntity te = getDoubleDoor();
		if (te != null)
			te.setDoorState(newState);
	}

	/**
	 * Change the current state of this DoorTileEntity
	 * 
	 * @param newSate
	 */
	public void setDoorState(DoorState newState)
	{
		if (state == newState)
			return;

		state = newState;
		if (getWorldObj() == null)
			return;

		if (state == DoorState.CLOSING || state == DoorState.OPENING)
		{
			timer = moving ? openingTime - timer : 0;
			startTime = worldObj.getTotalWorldTime() - timer;
			moving = true;
		}
		else
		{
			int metadata = getBlockMetadata();
			if (getBlockType() instanceof Door)
				metadata = metadata & 7;
			metadata = state == DoorState.OPENED ? metadata | Door.FLAG_OPENED : metadata & ~Door.FLAG_OPENED;
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, metadata, 2);
			moving = false;
		}

		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		playSound();
	}

	/**
	 * Play sound for the block
	 */
	public void playSound()
	{
		if (worldObj.isRemote)
			return;

		String soundPath = null;
		if (doorSound != null)
			soundPath = doorSound.getSoundPath(state);
		if (soundPath != null)
			getWorldObj().playSoundEffect(xCoord, yCoord, zCoord, soundPath, 1F, 1F);
	}

	/**
	 * Find the corresponding double door for this DoorTileEntity
	 * 
	 * @return
	 */
	public DoorTileEntity getDoubleDoor()
	{
		if (!isDoubleDoor())
			return null;

		int dir = getDirection();
		boolean reversed = isReversed();
		DoorTileEntity te;
		int x = xCoord;
		int z = zCoord;

		if (dir == Door.DIR_NORTH)
			x += (reversed ? 1 : -1);
		else if (dir == Door.DIR_SOUTH)
			x += (reversed ? -1 : 1);
		else if (dir == Door.DIR_EAST)
			z += (reversed ? 1 : -1);
		else if (dir == Door.DIR_WEST)
			z += (reversed ? -1 : 1);

		te = Door.getDoor(worldObj, x, yCoord, z);
		if (te != null && isMatchingDoubleDoor(te))
			return te;

		return null;
	}

	/**
	 * Is the DoorTileEntity passed a matching matching double door to this DoorTileEntity
	 * 
	 * @param te
	 * @return
	 */
	public boolean isMatchingDoubleDoor(DoorTileEntity te)
	{
		if (getBlockType() != te.getBlockType()) // different block
			return false;

		if (getDirection() != te.getDirection()) // different direction
			return false;

		if (getMovement() != te.getMovement()) //different movement type
			return false;

		if ((getBlockMetadata() & Door.FLAG_OPENED) != (te.getBlockMetadata() & Door.FLAG_OPENED)) // different state
			return false;

		if ((getBlockMetadata() & Door.FLAG_REVERSED) == (te.getBlockMetadata() & Door.FLAG_REVERSED)) // handle same side
			return false;

		return true;
	}

	/**
	 * Change the state of this DoorTileEntity based on powered
	 */
	public void setPowered(boolean powered)
	{
		if (isOpened() == powered && !isMoving())
			return;

		DoorTileEntity te = getDoubleDoor();
		if (!powered && te != null && te.isPowered())
			return;

		DoorState newState = powered ? DoorState.OPENING : DoorState.CLOSING;
		setDoorState(newState);

		if (te != null)
			te.setDoorState(newState);
	}

	@Override
	public void updateEntity()
	{
		if (!moving)
			return;

		timer++;
		if (startTime + openingTime < worldObj.getTotalWorldTime())
		{
			setDoorState(state == DoorState.CLOSING ? DoorState.CLOSED : DoorState.OPENED);
			timer = 0;
		}
	}

	//#region NBT/Network
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		setDoorState(DoorState.values()[nbt.getInteger("state")]);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("state", state.ordinal());
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
	{
		this.readFromNBT(packet.func_148857_g());
	}

	//#end NBT/Network

	/**
	 * Specify the bounding box ourselves otherwise, the block bounding box would be use. (And it should be at this point {0, 0, 0})
	 */
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 2, zCoord + 1);
	}

	@Override
	public boolean shouldRefresh(Block oldBlock, Block newBlock, int oldMeta, int newMeta, World world, int x, int y, int z)
	{
		return oldBlock != newBlock;
	}
}
