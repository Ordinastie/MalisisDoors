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

package net.malisis.doors.door.block;

import net.malisis.doors.door.DoorState;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.minecraft.block.material.Material;
import net.minecraft.util.AxisAlignedBB;

/**
 * @author Ordinastie
 * 
 */
public abstract class RotatingDoor extends Door
{
	public RotatingDoor(Material material)
	{
		super(material);
	}

	@Override
	public void setTileEntityInformations(DoorTileEntity te)
	{
		te.setRequireRedstone(blockMaterial == Material.iron);
	}

	@Override
	public AxisAlignedBB getBoundingBox(DoorTileEntity te)
	{
		int dir = te.getDirection();
		boolean opened = te.isOpened();
		boolean reversed = te.isReversed();

		float x = 0;
		float y = 0;
		float z = 0;
		float X = 1;
		float Y = 1;
		float Z = 1;

		if ((dir == DIR_NORTH && !opened) || (dir == DIR_WEST && opened && !reversed) || (dir == DIR_EAST && opened && reversed))
			Z = DOOR_WIDTH;
		else if ((dir == DIR_WEST && !opened) || (dir == DIR_SOUTH && opened && !reversed) || (dir == DIR_NORTH && opened && reversed))
			X = DOOR_WIDTH;
		else if ((dir == DIR_EAST && !opened) || (dir == DIR_NORTH && opened && !reversed) || (dir == DIR_SOUTH && opened && reversed))
			x = 1 - DOOR_WIDTH;
		else if ((dir == DIR_SOUTH && !opened) || (dir == DIR_EAST && opened && !reversed) || (dir == DIR_WEST && opened && reversed))
			z = 1 - DOOR_WIDTH;

		return AxisAlignedBB.getBoundingBox(x, y, z, X, Y, Z);
	}

	@Override
	public String getSoundPath(DoorState state)
	{
		if (state == DoorState.OPENING)
			return "random.door_open";
		else if (state == DoorState.CLOSED)
			return "random.door_close";

		return null;
	}

}
