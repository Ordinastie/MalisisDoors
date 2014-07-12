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

package net.malisis.doors.block.doors;

import net.malisis.doors.MalisisDoors;
import net.malisis.doors.entity.DoorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @author Ordinastie
 * 
 */
public class DoorHandler
{
	public static final int DIR_WEST = 0;
	public static final int DIR_NORTH = 1;
	public static final int DIR_EAST = 2;
	public static final int DIR_SOUTH = 3;

	public static final float DOOR_WIDTH = 0.1875F;

	public static final int flagOpened = 1 << 2;
	public static final int flagTopBlock = 1 << 3;
	public static final int flagReversed = 1 << 4;

	public static final int stateClose = 0;
	public static final int stateClosing = 1;
	public static final int stateOpen = 2;
	public static final int stateOpening = 3;

	public static String state(int state)
	{
		return (new String[] { "close", "closing", "open", "opening" })[state];
	}

	public static DoorTileEntity getTileEntity(IBlockAccess world, int x, int y, int z)
	{
		Block block = world.getBlock(x, y, z);
		int metadata = getFullMetadata(world, x, y, z);
		if (block instanceof Door)
			y -= (metadata & flagTopBlock) != 0 ? 1 : 0;

		TileEntity te = world.getTileEntity(x, y, z);
		return (DoorTileEntity) te;
	}

	public static int getFullMetadata(IBlockAccess world, int x, int y, int z)
	{
		Block block = world.getBlock(x, y, z);
		int metadata = world.getBlockMetadata(x, y, z);

		if (!(block instanceof BlockDoor))
			return metadata;

		boolean blockTop = (metadata & flagTopBlock) != 0;
		int bottomMetadata;
		int topMetadata;

		if (blockTop)
		{
			bottomMetadata = world.getBlockMetadata(x, y - 1, z);
			topMetadata = metadata;
		}
		else
		{
			bottomMetadata = metadata;
			topMetadata = world.getBlockMetadata(x, y + 1, z);
		}

		boolean flag1 = (topMetadata & 1) != 0;
		return bottomMetadata & 7 | (blockTop ? flagTopBlock : 0) | (flag1 ? flagReversed : 0);
	}

	public static void setDoorState(World world, int x, int y, int z, int state)
	{
		DoorTileEntity te = getTileEntity(world, x, y, z);
		if (te == null)
			return;
		te.setDoorState(state);
	}

	public static void playSound(World world, int x, int y, int z, int state)
	{
		Block block = world.getBlock(x, y, z);

		if (block instanceof SlidingDoor)
		{
			if (state == stateOpening || state == stateClosing)
				world.playSoundEffect(x, y, z, MalisisDoors.modid + ":slidingdooro", 1F, 1F);
		}
		else
		{
			if (state == stateOpening)
				world.playSoundEffect(x, y, z, "random.door_open", 1.0F, 1.0F);
			else if (state == stateClose)
				world.playSoundEffect(x, y, z, "random.door_close", 1.0F, 1.0F);
		}
	}

	public static void onPoweredBlockChange(World world, int x, int y, int z, boolean powered)
	{
		boolean opened = (getFullMetadata(world, x, y, z) & flagOpened) != 0;
		DoorTileEntity te = (DoorTileEntity) world.getTileEntity(x, y, z);
		if ((opened != powered) || (te != null && te.moving))
		{
			if (!powered && isDoubleDoorPowered(world, x, y, z))
				return;

			openDoubleDoor(world, x, y, z, powered ? stateOpening : stateClosing);
		}
	}

	/**
	 * Check if the bloc is part of double door
	 * 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @param metadata of the original door activated
	 * @return
	 */
	public static boolean isDoubleDoor(World world, int x, int y, int z, Block block, int metadata)
	{
		int metadata2 = getFullMetadata(world, x, y, z);

		if (world.getBlock(x, y, z) != block) // different block
			return false;

		if ((metadata & 3) != (metadata2 & 3)) // different direction
			return false;

		if ((metadata & flagOpened) != (metadata2 & flagOpened)) // different state
			return false;

		if ((metadata & flagReversed) == (metadata2 & flagReversed)) // handle same side
			return false;

		return true;
	}

	public static ForgeDirection findDoubleDoor(World world, int x, int y, int z, Block block)
	{
		if (!(block instanceof Door))
			return null;

		int metadata = getFullMetadata(world, x, y, z);
		int dir = metadata & 3;

		if (dir == DIR_NORTH || dir == DIR_SOUTH)
		{
			if (isDoubleDoor(world, x - 1, y, z, block, metadata))
				return ForgeDirection.WEST;
			if (isDoubleDoor(world, x + 1, y, z, block, metadata))
				return ForgeDirection.EAST;
		}
		else if (dir == DIR_EAST || dir == DIR_WEST)
		{
			if (isDoubleDoor(world, x, y, z - 1, block, metadata))
				return ForgeDirection.NORTH;
			if (isDoubleDoor(world, x, y, z + 1, block, metadata))
				return ForgeDirection.SOUTH;
		}

		return null;
	}

	/**
	 * Open/close double doors
	 * 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 */
	public static void openDoubleDoor(World world, int x, int y, int z, int state)
	{
		setDoorState(world, x, y, z, state);

		Block block = world.getBlock(x, y, z);
		ForgeDirection d = findDoubleDoor(world, x, y, z, block);
		if (d != null)
			setDoorState(world, x + d.offsetX, y + d.offsetY, z + d.offsetZ, state);
	}

	/**
	 * 
	 */
	public static boolean isDoubleDoorPowered(World world, int x, int y, int z)
	{
		Block block = world.getBlock(x, y, z);
		ForgeDirection d = findDoubleDoor(world, x, y, z, block);
		if (d != null)
			return world.isBlockIndirectlyGettingPowered(x + d.offsetX, y + d.offsetY, z + d.offsetZ)
					|| world.isBlockIndirectlyGettingPowered(x + d.offsetX, y + d.offsetY + 1, z + d.offsetZ);
		return false;
	}

	public static boolean setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z, boolean selBox)
	{
		Block block = world.getBlock(x, y, z);
		int metadata = getFullMetadata(world, x, y, z);
		DoorTileEntity te = getTileEntity(world, x, y, z);
		if (te == null)
			return true;

		if (te != null && te.moving)
		{
			block.setBlockBounds(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
			return false;
		}

		float[][] bounds = calculateBlockBounds(block, metadata, selBox);
		if (bounds != null)
			block.setBlockBounds(bounds[0][0], bounds[0][1], bounds[0][2], bounds[1][0], bounds[1][1], bounds[1][2]);
		return true;
	}

	public static double[][] calculateBlockBoundsD(Block block, int metadata, boolean selBox)
	{
		float[][] bounds = calculateBlockBounds(block, metadata, selBox);
		return new double[][] { { bounds[0][0], bounds[0][1], bounds[0][2] }, { bounds[1][0], bounds[1][1], bounds[1][2] } };
	}

	public static float[][] calculateBlockBounds(Block block, int metadata, boolean selBox)
	{
		if (block instanceof SlidingDoor)
			return calculateSlidingDoorBounds(metadata, selBox);
		else if (block instanceof BlockDoor)
			return calculateBlockDoorBounds(metadata, selBox);
		else if (block instanceof TrapDoor)
			return calculateBlockTrapDoorBounds(metadata, selBox);
		return null;
	}

	public static float[][] calculateBlockDoorBounds(int metadata, boolean selBox)
	{
		float f = DOOR_WIDTH;
		int dir = metadata & 3;
		boolean topBlock = (metadata & flagTopBlock) != 0;
		boolean opened = (metadata & flagOpened) != 0;
		boolean reversed = (metadata & flagReversed) != 0;

		float x = 0;
		float y = 0;
		float z = 0;
		float X = 1;
		float Y = 1;
		float Z = 1;

		if (selBox)
		{
			y -= (topBlock ? 1 : 0);
			Y += (topBlock ? 0 : 1);
		}

		if ((dir == DIR_NORTH && !opened) || (dir == DIR_WEST && opened && !reversed) || (dir == DIR_EAST && opened && reversed))
			Z = f;
		else if ((dir == DIR_WEST && !opened) || (dir == DIR_SOUTH && opened && !reversed) || (dir == DIR_NORTH && opened && reversed))
			X = f;
		else if ((dir == DIR_EAST && !opened) || (dir == DIR_NORTH && opened && !reversed) || (dir == DIR_SOUTH && opened && reversed))
			x = 1 - f;
		else if ((dir == DIR_SOUTH && !opened) || (dir == DIR_EAST && opened && !reversed) || (dir == DIR_WEST && opened && reversed))
			z = 1 - f;

		return new float[][] { { x, y, z }, { X, Y, Z } };
	}

	public static float[][] calculateSlidingDoorBounds(int metadata, boolean selBox)
	{
		float f = DOOR_WIDTH;
		int dir = metadata & 3;
		boolean topBlock = (metadata & flagTopBlock) != 0;
		boolean opened = (metadata & flagOpened) != 0;
		boolean reversed = (metadata & flagReversed) != 0;
		float left = -1 + f;
		float right = 1 - f;

		float x = 0;
		float y = 0;
		float z = 0;
		float X = 1;
		float Y = 1;
		float Z = 1;

		if (selBox)
		{
			y -= (topBlock ? 1 : 0);
			Y += (topBlock ? 0 : 1);
		}

		if (dir == DIR_NORTH)
		{
			z = 0.01F;
			Z = f;
			if (opened)
			{
				x += reversed ? left : right;
				X += reversed ? left : right;
			}
		}
		if (dir == DIR_SOUTH)
		{
			z = 1 - f;
			Z = 0.99F;
			if (opened)
			{
				x += reversed ? right : left;
				X += reversed ? right : left;
			}
		}
		if (dir == DIR_WEST)
		{
			x = 0.01F;
			X = f;
			if (opened)
			{
				z += reversed ? right : left;
				Z += reversed ? right : left;
			}
		}
		if (dir == DIR_EAST)
		{
			x = 1 - f;
			X = 0.99F;
			if (opened)
			{
				z += reversed ? left : right;
				Z += reversed ? left : right;
			}
		}

		return new float[][] { { x, y, z }, { X, Y, Z } };
	}

	public static float[][] calculateBlockTrapDoorBounds(int metadata, boolean selBox)
	{
		float f = DOOR_WIDTH;

		float x = 0;
		float y = 0;
		float z = 0;
		float X = 1;
		float Y = 1;
		float Z = 1;

		if ((metadata & flagOpened) == 0)
		{
			if ((metadata & flagTopBlock) != 0)
				y = 1 - f;
			else
				Y = f;
		}
		else
		{
			int dir = metadata & 3;
			if (dir == TrapDoor.DIR_NORTH)
				Z = f;
			if (dir == TrapDoor.DIR_SOUTH)
				z = 1 - f;
			if (dir == TrapDoor.DIR_EAST)
				x = 1 - f;
			if (dir == TrapDoor.DIR_WEST)
				X = f;
		}

		return new float[][] { { x, y, z }, { X, Y, Z } };
	}
}
