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

import net.malisis.core.util.syncer.Syncable;
import net.malisis.doors.block.Door;
import net.malisis.doors.item.CustomDoorItem;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.apache.commons.lang3.tuple.Triple;

/**
 * @author Ordinastie
 *
 */
@Syncable("TileEntity")
public class CustomDoorTileEntity extends DoorTileEntity
{
	private IBlockState frame = Blocks.PLANKS.getDefaultState();
	private IBlockState top = Blocks.GLASS.getDefaultState();
	private IBlockState bottom = Blocks.GLASS.getDefaultState();

	//#region Getters/setters
	public IBlockState getFrame()
	{
		return frame;
	}

	public IBlockState getTop()
	{
		return top;
	}

	public IBlockState getBottom()
	{
		return bottom;
	}

	public int getLightValue()
	{
		if (frame == null)
			return 0;
		return Math.max(Math.max(frame.getLightValue(worldObj, pos), top.getLightValue(worldObj, pos)), bottom.getLightValue(worldObj, pos));
	}

	@Override
	public ItemStack getItemStack()
	{
		ItemStack itemStack = super.getItemStack();
		CustomDoorItem.writeNBT(itemStack.getTagCompound(), frame, top, bottom);
		return itemStack;
	}

	//#end Getters/setters

	@Override
	public void onBlockPlaced(Door door, ItemStack itemStack)
	{
		super.onBlockPlaced(door, itemStack);

		Triple<IBlockState, IBlockState, IBlockState> triple = CustomDoorItem.readNBT(itemStack.getTagCompound());
		frame = triple.getLeft();
		top = triple.getMiddle();
		bottom = triple.getRight();

		setCentered(shouldCenter());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		Triple<IBlockState, IBlockState, IBlockState> triple = CustomDoorItem.readNBT(nbt);
		frame = triple.getLeft();
		top = triple.getMiddle();
		bottom = triple.getRight();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		CustomDoorItem.writeNBT(nbt, frame, top, bottom);
		return nbt;
	}
}
