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

import net.malisis.core.util.TileEntityUtils;
import net.malisis.core.util.Timer;
import net.malisis.doors.DoorDescriptor;
import net.malisis.doors.DoorState;
import net.malisis.doors.block.Door;
import net.malisis.doors.movement.IDoorMovement;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author Ordinastie
 *
 */
public class DoorTileEntity extends TileEntity implements ITickable
{
	protected DoorDescriptor descriptor;
	protected int lastMetadata = -1;
	protected Timer timer = new Timer();
	protected DoorState state = DoorState.CLOSED;
	protected boolean moving;
	protected boolean centered = false;
	protected PropertyBool openProperty = BlockDoor.OPEN;

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

	public IBlockState getBlockState()
	{
		IBlockState state = worldObj.getBlockState(pos);
		if (state.getBlock() != getBlockType() || getBlockType() == null)
			return null;

		return state.getBlock().getActualState(state, worldObj, pos);
	}

	public EnumFacing getDirection()
	{
		return BlockDoor.getFacing(worldObj, pos);
	}

	public boolean isTopBlock(BlockPos pos)
	{
		return this.pos.up().equals(pos);
	}

	public boolean isOpened()
	{
		IBlockState state = getBlockState();
		return state != null && state.getValue(BlockDoor.OPEN);
	}

	public boolean isHingeLeft()
	{
		return getBlockType() instanceof Door && getBlockState().getValue(BlockDoor.HINGE) == BlockDoor.EnumHingePosition.LEFT;
	}

	public boolean isPowered()
	{
		return getWorld().isBlockIndirectlyGettingPowered(pos) + getWorld().isBlockIndirectlyGettingPowered(pos.up()) != 0;
	}

	public boolean isCentered()
	{
		return centered;
	}

	public boolean shouldCenter()
	{
		if (getMovement() == null /*|| !getMovement().canCenter()*/)
			return false;

		EnumFacing offset = getDirection().rotateY();
		Block b1 = worldObj.getBlockState(pos.offset(offset, 1)).getBlock();
		Block b2 = worldObj.getBlockState(pos.offset(offset, -1)).getBlock();

		return ArrayUtils.contains(Door.centerBlocks, b1) || ArrayUtils.contains(Door.centerBlocks, b2);
	}

	public boolean setCentered(boolean centered)
	{
		this.centered = centered;
		TileEntityUtils.notifyUpdate(this);
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
			IBlockState state = getBlockState();
			if (state != null)
				worldObj.setBlockState(pos, state.withProperty(openProperty, newState == DoorState.OPENED));
			moving = false;
		}

		TileEntityUtils.notifyUpdate(this);
		playSound();
	}

	/**
	 * Play sound for the block
	 */
	public void playSound()
	{
		if (worldObj.isRemote)
			return;

		SoundEvent sound = null;
		if (descriptor.getSound() != null)
			sound = descriptor.getSound().getSound(state);

		if (sound != null)
			getWorld().playSound(null, pos, sound, SoundCategory.BLOCKS, 1F, 1F);
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

		EnumFacing offset = getDirection().rotateYCCW();
		if (isHingeLeft())
			offset = offset.getOpposite();

		DoorTileEntity te = Door.getDoor(worldObj, pos.offset(offset));
		if (isMatchingDoubleDoor(te))
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
		if (te == null)
			return false;

		if (getBlockType() != te.getBlockType()) // different block
			return false;

		if (getDirection() != te.getDirection()) // different direction
			return false;

		if (getMovement() != te.getMovement()) //different movement type
			return false;

		if (isOpened() != te.isOpened()) // different state
			return false;

		if (isHingeLeft() == te.isHingeLeft()) // handle same side
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
	public void update()
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
	public Packet<INetHandlerPlayClient> getDescriptionPacket()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		return new SPacketUpdateTileEntity(pos, 0, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity packet)
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
		return new AxisAlignedBB(pos, pos.add(1, 2, 1));
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
	{
		return oldState.getBlock() != newState.getBlock();
	}
}
