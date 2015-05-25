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

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.inventory.IInventoryProvider;
import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.inventory.MalisisSlot;
import net.malisis.core.util.ItemUtils;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.door.DoorDescriptor;
import net.malisis.doors.door.DoorRegistry;
import net.malisis.doors.door.item.CustomDoorItem;
import net.malisis.doors.door.item.DoorItem;
import net.malisis.doors.door.movement.IDoorMovement;
import net.malisis.doors.door.sound.IDoorSound;
import net.malisis.doors.gui.DoorFactoryGui;
import net.minecraft.item.ItemDoor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
public class DoorFactoryTileEntity extends TileEntity implements IInventoryProvider
{
	private MalisisInventory inventory;
	private IDoorMovement doorMovement;
	private IDoorSound doorSound;
	private int openingTime = 6;
	private int autoCloseTime = 0;
	private boolean requireRedstone = false;
	private boolean doubleDoor = true;
	private String code;

	private boolean isCreate = true;

	public MalisisSlot frameSlot;
	public MalisisSlot topMaterialSlot;
	public MalisisSlot bottomMaterialSlot;
	public MalisisSlot doorEditSlot;
	public MalisisSlot outputSlot;

	public DoorFactoryTileEntity()
	{
		frameSlot = new DoorFactorySlot(0);
		topMaterialSlot = new DoorFactorySlot(1);
		bottomMaterialSlot = new DoorFactorySlot(2);
		doorEditSlot = new DoorEditSlot(3);
		outputSlot = new MalisisSlot(4);
		outputSlot.setOutputSlot();

		inventory = new MalisisInventory(this,
				new MalisisSlot[] { frameSlot, topMaterialSlot, bottomMaterialSlot, doorEditSlot, outputSlot });
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

	public boolean requireRedstone()
	{
		return requireRedstone;
	}

	public void setRequireRedstone(boolean requireRedstone)
	{
		this.requireRedstone = requireRedstone;
	}

	public boolean isDoubleDoor()
	{
		return doubleDoor;
	}

	public void setDoubleDoor(boolean doubleDoor)
	{
		this.doubleDoor = doubleDoor;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
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

			if (output != null && (!ItemStack.areItemStackTagsEqual(output, expected) || output.stackSize >= output.getMaxStackSize()))
				return;

			frameSlot.extract(1);
			topMaterialSlot.extract(1);
			bottomMaterialSlot.extract(1);
			outputSlot.insert(expected);
		}
		else
		{
			ItemStack doors = doorEditSlot.extract(ItemUtils.FULL_STACK);
			NBTTagCompound nbt = doors.stackTagCompound;
			if (nbt == null)
				nbt = new NBTTagCompound();

			buildDescriptor().writeNBT(nbt);

			doors.stackTagCompound = nbt;

			outputSlot.insert(doors);
		}
	}

	public boolean canCreateDoor()
	{
		if (doorMovement == null || doorSound == null || openingTime == 0)
			return false;

		if (isCreate)
		{
			if (frameSlot.getItemStack() == null)
				return false;
			if (topMaterialSlot.getItemStack() == null)
				return false;
			if (bottomMaterialSlot.getItemStack() == null)
				return false;
		}
		else
		{
			if (doorEditSlot.getItemStack() == null)
				return false;
		}

		return true;
	}

	public DoorDescriptor buildDescriptor()
	{
		DoorDescriptor desc = new DoorDescriptor();
		desc.set(MalisisDoors.Blocks.customDoor, MalisisDoors.Items.customDoorItem);
		desc.setMovement(getDoorMovement());
		desc.setSound(getDoorSound());
		desc.setOpeningTime(getOpeningTime());
		desc.setAutoCloseTime(getAutoCloseTime());
		desc.setRequireRedstone(requireRedstone());
		desc.setDoubleDoor(isDoubleDoor());
		desc.setCode(getCode());

		return desc;
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
		return new DoorFactoryGui(this, container);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		nbt.setBoolean("isCreate", isCreate);

		if (doorMovement != null)
			nbt.setString("doorMovement", DoorRegistry.getId(doorMovement));
		if (doorSound != null)
			nbt.setString("doorSound", DoorRegistry.getId(doorSound));
		nbt.setInteger("openingTime", openingTime);
		nbt.setInteger("autoCloseTime", autoCloseTime);
		nbt.setBoolean("requireRedstone", requireRedstone);
		nbt.setBoolean("doubleDoor", doubleDoor);

		inventory.writeToNBT(nbt);
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
		requireRedstone = nbt.getBoolean("requireRedstone");
		doubleDoor = nbt.getBoolean("doubleDoor");

		inventory.readFromNBT(nbt);
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
		TileEntityUtils.updateGui(this);
	}

	private class DoorFactorySlot extends MalisisSlot
	{
		public DoorFactorySlot(int index)
		{
			super(index);
		}

		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			return CustomDoorItem.canBeUsedForDoor(itemStack, slotNumber == 0);
		}
	}

	private class DoorEditSlot extends MalisisSlot
	{
		public DoorEditSlot(int index)
		{
			super(index);
		}

		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			return itemStack.getItem() instanceof DoorItem || itemStack.getItem() instanceof ItemDoor;
		}
	}
}
