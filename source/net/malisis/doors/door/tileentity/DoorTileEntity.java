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

import net.malisis.core.util.Timer;
import net.malisis.doors.door.DoorDescriptor;
import net.malisis.doors.door.DoorState;
import net.malisis.doors.door.block.Door;
import net.malisis.doors.door.movement.IDoorMovement;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author Ordinastie
 *
 */
public class DoorTileEntity extends TileEntity
{
	protected DoorDescriptor descriptor;
	protected int lastMetadata = -1;
	protected Timer timer = new Timer();
	protected DoorState state = DoorState.CLOSED;
	protected boolean moving;
	protected boolean centered = false;

	//#region Getter/Setter
	public DoorDescriptor getDescriptor()
	{
		if (descriptor == null || descriptor.getMovement() == null)
		{
			if (getBlockType() == null)
				return descriptor;

			if (getBlockType() instanceof Door)
				descriptor = ((Door) getBlockType()).getDescriptor();
		}
		return descriptor;
	}

	public void setDescriptor(DoorDescriptor descriptor)
	{
		this.descriptor = descriptor;
	}

	public Timer getTimer()
	{
		return timer;
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
		return getDescriptor() != null ? getDescriptor().getMovement() : null;
	}

	public int getOpeningTime()
	{
		return getDescriptor() != null ? getDescriptor().getOpeningTime() : 6;
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
		if (lastMetadata != blockMetadata || blockMetadata == -1 && getBlockType() != null)
		{
			blockMetadata = Door.fullMetadata(worldObj, xCoord, yCoord, zCoord);
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
		return getWorld().isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord)
				|| getWorld().isBlockIndirectlyGettingPowered(xCoord, yCoord + 1, zCoord);
	}

	public boolean isCentered()
	{
		return centered;
	}

	public boolean shouldCenter()
	{
		if (getMovement() == null /*|| !getMovement().canCenter()*/)
			return false;

		int ox = 0, oz = 0;
		if (getDirection() == Door.DIR_NORTH || getDirection() == Door.DIR_SOUTH)
			ox = 1;
		else
			oz = 1;

		Block b1 = worldObj.getBlock(xCoord - ox, yCoord, zCoord - oz);
		Block b2 = worldObj.getBlock(xCoord + ox, yCoord, zCoord + oz);

		return ArrayUtils.contains(Door.centerBlocks, b1) || ArrayUtils.contains(Door.centerBlocks, b2);
	}

	public boolean setCentered(boolean centered)
	{
		this.centered = centered;
		if (worldObj != null)
			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		return centered;
	}

	//#end Getter/Setter

	public void onBlockPlaced(Door door, ItemStack itemStack)
	{
		DoorDescriptor desc = itemStack.getTagCompound() != null ? new DoorDescriptor(itemStack.getTagCompound()) : door.getDescriptor();
		setDescriptor(desc);
	}

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
		if (getWorld() == null)
			return;

		if (state == DoorState.CLOSING || state == DoorState.OPENING)
		{
			if (moving)
			{
				long s = timer.elapsedTime() - Timer.tickToTime(getOpeningTime());
				timer.setRelativeStart(s);
			}
			else
			{
				timer.start();
				moving = true;
			}

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
		if (descriptor.getSound() != null)
			soundPath = descriptor.getSound().getSoundPath(state);
		if (soundPath != null)
			getWorld().playSoundEffect(xCoord, yCoord, zCoord, soundPath, 1F, 1F);
	}

	/**
	 * Find the corresponding double door for this DoorTileEntity
	 *
	 * @return
	 */
	public DoorTileEntity getDoubleDoor()
	{
		if (!descriptor.isDoubleDoor())
			return null;

		int dir = getDirection();
		boolean reversed = isReversed();

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

		TileEntity te = worldObj.getTileEntity(x, yCoord, z);
		if (te instanceof DoorTileEntity && isMatchingDoubleDoor((DoorTileEntity) te))
			return (DoorTileEntity) te;

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

		if (timer.elapsedTick() > descriptor.getOpeningTime())
			setDoorState(state == DoorState.CLOSING ? DoorState.CLOSED : DoorState.OPENED);
	}

	//#region NBT/Network
	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		//if (descriptor == null)
		descriptor = new DoorDescriptor(nbt);
		setDoorState(DoorState.values()[nbt.getInteger("state")]);
		setCentered(nbt.getBoolean("centered"));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		if (descriptor != null)
			descriptor.writeNBT(nbt);
		nbt.setInteger("state", state.ordinal());
		nbt.setBoolean("centered", centered);
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
		this.readFromNBT(packet.getNbtCompound());
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
