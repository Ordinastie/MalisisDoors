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

import net.malisis.doors.door.block.Door;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author Ordinastie
 *
 */
public class CustomDoorTileEntity extends DoorTileEntity
{
	private Block frame;
	private Block topMaterial;
	private Block bottomMaterial;

	private int frameMetadata;
	private int topMaterialMetadata;
	private int bottomMaterialMetadata;

	//#region Getters/setters
	public Block getFrame()
	{
		return frame;
	}

	public void setFrame(Block frame)
	{
		this.frame = frame;
	}

	public Block getTopMaterial()
	{
		return topMaterial;
	}

	public void setTopMaterial(Block topMaterial)
	{
		this.topMaterial = topMaterial;
	}

	public Block getBottomMaterial()
	{
		return bottomMaterial;
	}

	public void setBottomMaterial(Block bottomMaterial)
	{
		this.bottomMaterial = bottomMaterial;
	}

	public int getFrameMetadata()
	{
		return frameMetadata;
	}

	public void setFrameMetadata(int frameMetadata)
	{
		this.frameMetadata = frameMetadata;
	}

	public int getTopMaterialMetadata()
	{
		return topMaterialMetadata;
	}

	public void setTopMaterialMetadata(int topMaterialMetadata)
	{
		this.topMaterialMetadata = topMaterialMetadata;
	}

	public int getBottomMaterialMetadata()
	{
		return bottomMaterialMetadata;
	}

	public void setBottomMaterialMetadata(int bottomMaterialMetadata)
	{
		this.bottomMaterialMetadata = bottomMaterialMetadata;
	}

	//#end Getters/setters

	@Override
	public void onBlockPlaced(Door door, ItemStack itemStack)
	{
		super.onBlockPlaced(door, itemStack);

		NBTTagCompound nbt = itemStack.stackTagCompound;

		frame = Block.getBlockById(nbt.getInteger("frame"));
		topMaterial = Block.getBlockById(nbt.getInteger("topMaterial"));
		bottomMaterial = Block.getBlockById(nbt.getInteger("bottomMaterial"));

		frameMetadata = nbt.getInteger("frameMetadata");
		topMaterialMetadata = nbt.getInteger("topMaterialMetadata");
		bottomMaterialMetadata = nbt.getInteger("bottomMaterialMetadata");

		setCentered(shouldCenter());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);

		frame = Block.getBlockById(nbt.getInteger("frame"));
		topMaterial = Block.getBlockById(nbt.getInteger("topMaterial"));
		bottomMaterial = Block.getBlockById(nbt.getInteger("bottomMaterial"));

		frameMetadata = nbt.getInteger("frameMetadata");
		topMaterialMetadata = nbt.getInteger("topMaterialMetadata");
		bottomMaterialMetadata = nbt.getInteger("bottomMaterialMetadata");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);

		nbt.setInteger("frame", Block.getIdFromBlock(frame));
		nbt.setInteger("topMaterial", Block.getIdFromBlock(topMaterial));
		nbt.setInteger("bottomMaterial", Block.getIdFromBlock(bottomMaterial));

		nbt.setInteger("frameMetadata", frameMetadata);
		nbt.setInteger("topMaterialMetadata", topMaterialMetadata);
		nbt.setInteger("bottomMaterialMetadata", bottomMaterialMetadata);

	}
}
