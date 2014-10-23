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
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.door.CustomDoorItem;
import net.malisis.doors.door.DoorRegistry;
import net.malisis.doors.door.movement.IDoorMovement;
import net.malisis.doors.door.sound.IDoorSound;
import net.malisis.doors.gui.DoorFactoryGui;
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
	private boolean requireRedstone = false;
	private boolean doubleDoor = true;

	public MalisisSlot frameSlot;
	public MalisisSlot topMaterialSlot;
	public MalisisSlot bottomMaterialSlot;
	public MalisisSlot outputSlot;

	public DoorFactoryTileEntity()
	{
		frameSlot = new DoorFactorySlot(0);
		topMaterialSlot = new DoorFactorySlot(1);
		bottomMaterialSlot = new DoorFactorySlot(2);
		outputSlot = new MalisisSlot(3);
		outputSlot.setOutputSlot(true);

		inventory = new MalisisInventory(this, new MalisisSlot[] { frameSlot, topMaterialSlot, bottomMaterialSlot, outputSlot });
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

	public void createDoor()
	{
		if (getWorldObj().isRemote)
			return;

		if (!canCreateDoor())
			return;

		ItemStack expected = CustomDoorItem.fromDoorFactory(this);
		ItemStack output = outputSlot.getItemStack();
		if (expected == null)
			return;

		if (output != null && (!ItemStack.areItemStackTagsEqual(output, expected) || output.stackSize >= output.getMaxStackSize()))
			return;

		frameSlot.addItemStackSize(-1);
		topMaterialSlot.addItemStackSize(-1);
		bottomMaterialSlot.addItemStackSize(-1);

		if (output == null)
			outputSlot.setItemStack(expected);
		else
			outputSlot.addItemStackSize(1);
	}

	public boolean canCreateDoor()
	{
		if (doorMovement == null || doorSound == null || openingTime == 0)
			return false;

		if (frameSlot.getItemStack() == null)
			return false;
		if (topMaterialSlot.getItemStack() == null)
			return false;
		if (bottomMaterialSlot.getItemStack() == null)
			return false;

		return true;
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
		if (doorMovement != null)
			nbt.setString("doorMovement", DoorRegistry.getId(doorMovement));
		if (doorSound != null)
			nbt.setString("doorSound", DoorRegistry.getId(doorSound));
		nbt.setInteger("openingTime", openingTime);
		nbt.setBoolean("requireRedstone", requireRedstone);
		nbt.setBoolean("doubleDoor", doubleDoor);

		inventory.writeToNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		doorMovement = DoorRegistry.getMovement(nbt.getString("doorMovement"));
		doorSound = DoorRegistry.getSound(nbt.getString("doorSound"));
		openingTime = nbt.getInteger("openingTime");
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
		this.readFromNBT(packet.func_148857_g());
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
			return !isOutputSlot && CustomDoorItem.canBeUsedForDoor(itemStack, slotNumber == 0);
		}
	}

}
