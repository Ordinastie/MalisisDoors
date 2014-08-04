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
import net.malisis.doors.gui.DoorFactoryGui;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

/**
 * @author Ordinastie
 * 
 */
public class DoorFactoryTileEntity extends TileEntity implements IInventoryProvider
{
	public static int TYPE_ROTATING = 0;
	public static int TYPE_SLIDING = 1;

	private MalisisInventory inventory;
	private int doorType = TYPE_ROTATING;

	public MalisisSlot frameSlot;
	public MalisisSlot topMaterialSlot;
	public MalisisSlot bottomMaterialSlot;
	public MalisisSlot output;

	public DoorFactoryTileEntity()
	{
		frameSlot = new MalisisSlot(0);
		topMaterialSlot = new MalisisSlot(1);
		bottomMaterialSlot = new MalisisSlot(2);
		output = new MalisisSlot(3);
		output.setOutputSlot(true);

		inventory = new MalisisInventory(this, new MalisisSlot[] { frameSlot, topMaterialSlot, bottomMaterialSlot, output });
	}

	public int getDoorType()
	{
		return doorType;
	}

	public void setDoorType(int doorType)
	{
		this.doorType = doorType;
	}

	public void createDoor()
	{

	}

	public boolean canCreateDoor()
	{

		return true;
	}

	@Override
	public MalisisInventory getInventory()
	{
		return inventory;
	}

	@Override
	public MalisisGui getGui(MalisisInventoryContainer container)
	{
		return new DoorFactoryGui(this, container);
	}

	@Override
	public void writeToNBT(NBTTagCompound tagCompound)
	{
		super.writeToNBT(tagCompound);
		tagCompound.setInteger("doorType", doorType);
		inventory.writeToNBT(tagCompound);
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound)
	{
		super.readFromNBT(tagCompound);
		doorType = tagCompound.getInteger("doorType");
		inventory.readFromNBT(tagCompound);
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

}
