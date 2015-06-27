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
import net.minecraft.entity.Entity;

/**
 * @author Ordinastie
 *
 */
public class SaloonDoorTileEntity extends DoorTileEntity
{
	private boolean openBackward = false;

	public boolean isBackward()
	{
		return openBackward;
	}

	public void setBackward(boolean backward)
	{
		this.openBackward = backward;
	}

	public void setOpenDirection(Entity entity)
	{
		double entityPos = 0;;
		float tePos = 0;

		switch (Door.intToDir(getDirection()))
		{
			case NORTH:
				entityPos = entity.posZ;
				tePos = zCoord + 0.5F;
				break;
			case SOUTH:
				entityPos = -entity.posZ;
				tePos = -zCoord - 0.5F;
				break;
			case EAST:
				entityPos = -entity.posX;
				tePos = -xCoord - 0.5F;
				break;
			case WEST:
				entityPos = entity.posX;
				tePos = xCoord + 0.5F;
			default:
				break;
		}

		openBackward = entityPos > tePos;
		//	MalisisCore.message(getDirection() + "  = B ? " + openBackward + " (" + entityPos + " > " + (tePos) + ")");
	}

	@Override
	public DoorTileEntity getDoubleDoor()
	{
		SaloonDoorTileEntity te = (SaloonDoorTileEntity) super.getDoubleDoor();
		if (te != null)
			te.setBackward(openBackward);
		return te;
	}
}
