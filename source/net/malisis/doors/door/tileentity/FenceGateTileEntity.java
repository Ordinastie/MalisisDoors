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

import net.malisis.doors.MalisisDoorsSettings;
import net.malisis.doors.door.DoorDescriptor;
import net.malisis.doors.door.DoorRegistry;
import net.malisis.doors.door.block.Door;
import net.malisis.doors.door.movement.FenceGateMovement;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 */
public class FenceGateTileEntity extends DoorTileEntity
{
	private Block camoBlock = Blocks.planks;
	private int camoMeta = 0;
	private boolean isCamo = false;
	private boolean isWall = false;

	public FenceGateTileEntity()
	{
		DoorDescriptor descriptor = new DoorDescriptor();
		descriptor.setMovement(DoorRegistry.getMovement(FenceGateMovement.class));
		setDescriptor(descriptor);
	}

	public boolean isWall()
	{
		return isWall;
	}

	public IIcon getCamoIcon()
	{
		return camoBlock.getIcon(2, camoMeta);
	}

	public int getCamoColor()
	{
		int ox = 0;
		int oz = 0;

		if (getDirection() == Door.DIR_NORTH || getDirection() == Door.DIR_SOUTH)
			oz = 1;
		else
			ox = 1;

		return isCamo ? camoBlock.colorMultiplier(getWorld(), xCoord - ox, yCoord, zCoord - oz) : 0xFFFFFF;
	}

	/**
	 * Specify the bounding box ourselves otherwise, the block bounding box would be use. (And it should be at this point {0, 0, 0})
	 */
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
	}

	public void updateCamo(World world, int x, int y, int z)
	{
		int ox = 0;
		int oz = 0;

		if (getDirection() == Door.DIR_NORTH || getDirection() == Door.DIR_SOUTH)
			oz = 1;
		else
			ox = 1;

		Block b1 = world.getBlock(xCoord - ox, y, zCoord - oz);
		Block b2 = world.getBlock(xCoord + ox, y, zCoord + oz);
		int meta1 = world.getBlockMetadata(xCoord - ox, y, zCoord - oz);
		int meta2 = world.getBlockMetadata(xCoord + ox, y, zCoord + oz);

		isWall = (b1 == Blocks.cobblestone_wall || b2 == Blocks.cobblestone_wall);

		if (MalisisDoorsSettings.enableCamoFenceGate.get() && b1 == b2 && meta1 == meta2 && (isWall || b1.renderAsNormalBlock())
				&& !b1.isAir(world, this.xCoord - ox, y, this.zCoord - oz))
		{
			isCamo = true;
			camoBlock = b1;
			camoMeta = meta1;

		}
		else
		{
			isCamo = false;
			camoBlock = Blocks.planks;
			camoMeta = 0;
		}

		//world.notifyBlockChange(xCoord, yCoord, zCoord, getBlockType());
		world.markBlockForUpdate(x, y, z);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		isWall = nbt.getBoolean("isWall");

		int blockID = nbt.getInteger("camoBlock");
		if (blockID == 0 || !MalisisDoorsSettings.enableCamoFenceGate.get())
		{
			isCamo = false;
			camoBlock = Blocks.planks;
			camoMeta = 0;

		}
		else
		{
			isCamo = nbt.getBoolean("isCamo");
			camoBlock = Block.getBlockById(blockID);
			camoMeta = nbt.getInteger("camoMeta");
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setBoolean("isCamo", isCamo);
		nbt.setInteger("camoBlock", Block.blockRegistry.getIDForObject(camoBlock));
		nbt.setInteger("camoMeta", camoMeta);
		nbt.setBoolean("isWall", isWall);
	}

}
