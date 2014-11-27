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

package net.malisis.doors.door.movement;

import static net.malisis.doors.door.block.Door.*;
import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.transformation.Rotation;
import net.malisis.core.renderer.animation.transformation.Transformation;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.doors.door.DoorState;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.minecraft.util.AxisAlignedBB;

/**
 * @author Ordinastie
 *
 */
public class RotateAroundMovement implements IDoorMovement
{

	@Override
	public AxisAlignedBB getBoundingBox(DoorTileEntity te, boolean topBlock, BoundingBoxType type)
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

		if (type == BoundingBoxType.SELECTION)
		{
			if (!topBlock)
				Y++;
			else
				y--;
		}

		return AxisAlignedBB.getBoundingBox(x, y, z, X, Y, Z);
	}

	private Transformation getTransformation(DoorTileEntity tileEntity)
	{
		float angle = -90;
		if (tileEntity.isReversed())
			angle = -angle;

		Transformation transformation = new Rotation(angle).aroundAxis(0, 1, 0);
		transformation.reversed(tileEntity.getState() == DoorState.CLOSING || tileEntity.getState() == DoorState.CLOSED);

		return transformation.forTicks(tileEntity.getDescriptor().getOpeningTime());
	}

	@Override
	public Animation[] getAnimations(DoorTileEntity tileEntity, MalisisModel model, RenderParameters rp)
	{
		return new Animation[] { new Animation(model, getTransformation(tileEntity)) };
	}

	@Override
	public boolean isSpecial()
	{
		return false;
	}
}
