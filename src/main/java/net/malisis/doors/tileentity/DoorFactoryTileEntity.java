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

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.inventory.IInventoryProvider.IDirectInventoryProvider;
import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.inventory.MalisisSlot;
import net.malisis.core.util.ItemUtils;
import net.malisis.core.util.ItemUtils.ItemStacksMerger;
import net.malisis.doors.DoorDescriptor;
import net.malisis.doors.DoorDescriptor.RedstoneBehavior;
import net.malisis.doors.DoorRegistry;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.gui.DoorFactoryGui;
import net.malisis.doors.item.CustomDoorItem;
import net.malisis.doors.item.DoorItem;
import net.malisis.doors.movement.IDoorMovement;
import net.malisis.doors.sound.IDoorSound;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
public class DoorFactoryTileEntity extends TileEntity implements IDirectInventoryProvider
{
	private MalisisInventory inventory;
	private IDoorMovement doorMovement;
	private IDoorSound doorSound;
	private int openingTime = 6;
	private int autoCloseTime = 0;
	private RedstoneBehavior redstoneBehavior = RedstoneBehavior.STANDARD;
	private boolean doubleDoor = true;
	private boolean proximityDetection = false;
	private String code;

	private boolean isCreate = true;

	public MalisisSlot frameSlot;
	public MalisisSlot topMaterialSlot;
	public MalisisSlot bottomMaterialSlot;
	public MalisisSlot doorEditSlot;
	public MalisisSlot outputSlot;

	public DoorFactoryTileEntity()
	{
		frameSlot = new DoorFactorySlot(true);
		topMaterialSlot = new DoorFactorySlot(false);
		bottomMaterialSlot = new DoorFactorySlot(false);
		doorEditSlot = new DoorEditSlot();
		outputSlot = new MalisisSlot();
		outputSlot.setOutputSlot();

		inventory = new MalisisInventory(this, frameSlot, topMaterialSlot, bottomMaterialSlot, doorEditSlot, outputSlot);
	}

	public boolean isCreate()
	{
		return isCreate;
	}

	public void setCreate(boolean isCreate)
	{
		this.isCreate = isCreate;
	}

	public IDoorMovement getDoorMovement()
	{
		return doorMovement;
	}

	public void setDoorMovement(IDoorMovement doorMovement)
	{
		this.doorMovement = doorMovement;
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

	public int getAutoCloseTime()
	{
		return autoCloseTime;
	}

	public void setAutoCloseTime(int autoCloseTime)
	{
		this.autoCloseTime = autoCloseTime;
	}

	public RedstoneBehavior getRedstoneBehavior()
	{
		return redstoneBehavior;
	}

	public void setRedstoneBehavior(RedstoneBehavior redstoneBehavior)
	{
		this.redstoneBehavior = redstoneBehavior;
	}

	public boolean isDoubleDoor()
	{
		return doubleDoor;
	}

	public void setDoubleDoor(boolean doubleDoor)
	{
		this.doubleDoor = doubleDoor;
	}

	public boolean hasProximityDetection()
	{
		return proximityDetection;
	}

	public void setProximityDetection(boolean proximity)
	{
		proximityDetection = proximity;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	@Override
	public MalisisInventory getInventory()
	{
		return inventory;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public MalisisGui getGui(MalisisInventoryContainer container)
	{
		return new DoorFactoryGui(this, container);
	}

	public void createDoor()
	{
		if (getWorld().isRemote)
			return;

		if (!canCreateDoor())
			return;

		if (isCreate)
		{
			ItemStack expected = CustomDoorItem.fromDoorFactory(this);
			ItemStack output = outputSlot.getItemStack();
			if (expected == null)
				return;

			if (output.getCount() >= output.getMaxStackSize() || !new ItemStacksMerger(expected, output).canMerge())
				return;

			frameSlot.extract(1);
			topMaterialSlot.extract(1);
			bottomMaterialSlot.extract(1);
			outputSlot.insert(expected);
		}
		else
		{
			ItemStack doors = doorEditSlot.extract(ItemUtils.FULL_STACK);
			NBTTagCompound nbt = doors.getTagCompound();
			if (nbt == null)
				nbt = new NBTTagCompound();

			DoorDescriptor desc = ((DoorItem) doors.getItem()).getDescriptor(doors);
			buildDescriptor(desc.getBlock(), desc.getItem()).writeNBT(nbt);

			doors.setTagCompound(nbt);

			outputSlot.insert(doors);
		}
	}

	public boolean canCreateDoor()
	{
		if (doorMovement == null || doorSound == null || openingTime == 0)
			return false;

		if (isCreate)
		{
			if (frameSlot.getItemStack().isEmpty())
				return false;
			if (topMaterialSlot.getItemStack().isEmpty())
				return false;
			if (bottomMaterialSlot.getItemStack().isEmpty())
				return false;
		}
		else
		{
			if (doorEditSlot.getItemStack().isEmpty())
				return false;
		}

		return true;
	}

	public DoorDescriptor buildDescriptor(Block block, Item item)
	{
		DoorDescriptor desc = new DoorDescriptor();
		desc.set(block, item);
		desc.setMovement(getDoorMovement());
		desc.setSound(getDoorSound());
		desc.setOpeningTime(getOpeningTime());
		desc.setAutoCloseTime(getAutoCloseTime());
		desc.setDoubleDoor(isDoubleDoor());
		desc.setProximityDetection(hasProximityDetection());
		desc.setRedstoneBehavior(getRedstoneBehavior());
		desc.setCode(getCode());

		return desc;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		nbt.setBoolean("isCreate", isCreate);

		if (doorMovement != null)
			nbt.setString("doorMovement", DoorRegistry.getId(doorMovement));
		if (doorSound != null)
			nbt.setString("doorSound", DoorRegistry.getId(doorSound));
		nbt.setInteger("openingTime", openingTime);
		nbt.setInteger("autoCloseTime", autoCloseTime);
		nbt.setInteger("redstoneBehavior", getRedstoneBehavior().ordinal());
		nbt.setBoolean("doubleDoor", doubleDoor);
		nbt.setBoolean("proximityDetection", proximityDetection);

		inventory.writeToNBT(nbt);

		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		isCreate = nbt.getBoolean("isCreate");
		doorMovement = DoorRegistry.getMovement(nbt.getString("doorMovement"));
		doorSound = DoorRegistry.getSound(nbt.getString("doorSound"));
		openingTime = nbt.getInteger("openingTime");
		autoCloseTime = nbt.getInteger("autoCloseTime");
		redstoneBehavior = RedstoneBehavior.values()[nbt.getInteger("redstoneBehavior")];
		doubleDoor = nbt.getBoolean("doubleDoor");
		proximityDetection = nbt.getBoolean("proximityDetection");

		inventory.readFromNBT(nbt);
	}

	@Override
	public NBTTagCompound getUpdateTag()
	{
		return writeToNBT(new NBTTagCompound());
	}

	private class DoorFactorySlot extends MalisisSlot
	{
		private boolean forFrame;

		public DoorFactorySlot(boolean frame)
		{
			this.forFrame = frame;
		}

		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			return CustomDoorItem.canBeUsedForDoor(itemStack, forFrame);
		}
	}

	private class DoorEditSlot extends MalisisSlot
	{
		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			if (itemStack.getItem() == MalisisDoors.Items.verticalHatchItem)
				return false;
			return itemStack.getItem() instanceof DoorItem || itemStack.getItem() instanceof ItemDoor;
		}
	}
}
