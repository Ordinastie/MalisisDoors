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

import java.util.HashMap;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.inventory.IInventoryProvider;
import net.malisis.core.inventory.InventoryEvent;
import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.inventory.MalisisSlot;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.block.VanishingBlock;
import net.malisis.doors.gui.VanishingDiamondGui;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
public class VanishingDiamondTileEntity extends VanishingTileEntity implements IInventoryProvider
{
	protected MalisisInventory inventory;
	protected MalisisSlot slot;
	protected int changedPowerStateTimer;
	protected HashMap<ForgeDirection, DirectionState> directionStates = new HashMap<>();

	public VanishingDiamondTileEntity()
	{
		super(VanishingBlock.typeDiamondFrame);
		inventory = new MalisisInventory(this, 1);
		inventory.setInventoryStackLimit(1);

		slot = inventory.getSlot(0);
		slot.register(this);
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			directionStates.put(dir, new DirectionState(dir));
	}

	public void setDuration(int duration)
	{
		this.duration = duration;
	}

	@Override
	public boolean setPowerState(boolean powered)
	{
		if (!super.setPowerState(powered))
			return false;

		changedPowerStateTimer = 0;
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			directionStates.get(dir).resetPropagationState();
			directionStates.get(dir).propagateState(changedPowerStateTimer);
		}

		return true;
	}

	public DirectionState getDirectionState(ForgeDirection dir)
	{
		return directionStates.get(dir);
	}

	@Override
	public void updateEntity()
	{
		changedPowerStateTimer++;

		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			directionStates.get(dir).propagateState(changedPowerStateTimer);

		super.updateEntity();
	}

	@Subscribe
	public void onSlotChanged(InventoryEvent.SlotChanged event)
	{
		setBlock(event.getSlot().getItemStack(), null, 0, 0.5F, 0.5F, 0.5F);
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		inventory.readFromNBT(nbt);

		NBTTagList dirList = nbt.getTagList("Directions", NBT.TAG_COMPOUND);
		for (int i = 0; i < dirList.tagCount(); ++i)
		{
			NBTTagCompound tag = dirList.getCompoundTagAt(i);
			ForgeDirection dir = ForgeDirection.getOrientation(tag.getInteger("direction"));
			directionStates.get(dir).readFromNBT(tag);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		inventory.writeToNBT(nbt);

		NBTTagList dirList = new NBTTagList();
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			dirList.appendTag(directionStates.get(dir).writeToNBT(new NBTTagCompound()));

		nbt.setTag("Directions", dirList);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
	{
		super.onDataPacket(net, packet);
		TileEntityUtils.updateGui(this);
	}

	@Override
	public MalisisInventory[] getInventories(Object... data)
	{
		return new MalisisInventory[] { inventory };
	}

	@Override
	public MalisisInventory[] getInventories(ForgeDirection side, Object... data)
	{
		return getInventories(data);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public MalisisGui getGui(MalisisInventoryContainer container)
	{
		return new VanishingDiamondGui(this, container);
	}

	public class DirectionState
	{
		public ForgeDirection direction;
		public boolean shouldPropagate;
		public int delay;
		public boolean inversed;
		public boolean propagated;

		public DirectionState(ForgeDirection direction, boolean shouldPropagate, int delay, boolean inversed)
		{
			this.direction = direction;
			update(shouldPropagate, delay, inversed);
		}

		public DirectionState(ForgeDirection direction)
		{
			this(direction, false, 0, false);
		}

		public void update(boolean shouldPropagate, int delay, boolean inversed)
		{
			this.shouldPropagate = shouldPropagate;
			this.delay = delay;
			this.inversed = inversed;
		}

		public void resetPropagationState()
		{
			this.propagated = false;
		}

		public boolean propagateState(int timer)
		{
			if (!shouldPropagate || propagated || timer < delay)
				return false;

			Block block = worldObj.getBlock(xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord + direction.offsetZ);
			if (block instanceof VanishingBlock)
				((VanishingBlock) block).setPowerState(worldObj, xCoord + direction.offsetX, yCoord + direction.offsetY, zCoord
						+ direction.offsetZ, inversed ? !powered : powered);
			propagated = true;

			return false;
		}

		public void readFromNBT(NBTTagCompound nbt)
		{
			update(nbt.getBoolean("shouldPropagate"), nbt.getInteger("delay"), nbt.getBoolean("inversed"));
		}

		public NBTTagCompound writeToNBT(NBTTagCompound nbt)
		{
			nbt.setInteger("direction", direction.ordinal());
			nbt.setBoolean("shouldPropagate", shouldPropagate);
			nbt.setInteger("delay", delay);
			nbt.setBoolean("inversed", inversed);

			return nbt;
		}
	}

}
