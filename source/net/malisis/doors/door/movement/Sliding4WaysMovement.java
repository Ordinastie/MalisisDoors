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
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.transformation.Transformation;
import net.malisis.core.renderer.animation.transformation.Translation;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.doors.door.DoorState;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.minecraft.util.AxisAlignedBB;

/**
 * @author Ordinastie
 *
 */
public class Sliding4WaysMovement implements IDoorMovement
{

	@Override
	public AxisAlignedBB getBoundingBox(DoorTileEntity tileEntity, boolean topBlock, boolean selBox)
	{
		int dir = tileEntity.getDirection();
		boolean opened = tileEntity.isOpened();
		boolean reversed = tileEntity.isReversed();
		float left = -1 + DOOR_WIDTH;
		float right = 1 - DOOR_WIDTH;

		float x = 0;
		float y = 0;
		float z = 0;
		float X = 1;
		float Y = 1;
		float Z = 1;

		if (dir == DIR_NORTH)
		{
			Z = DOOR_WIDTH;
			if (opened && topBlock == reversed)
			{
				x += reversed ? left : right;
				X += reversed ? left : right;
			}
		}
		if (dir == DIR_SOUTH)
		{
			z = 1 - DOOR_WIDTH;
			if (opened && topBlock == reversed)
			{
				x += reversed ? right : left;
				X += reversed ? right : left;
			}
		}
		if (dir == DIR_WEST)
		{
			X = DOOR_WIDTH;
			if (opened && topBlock == reversed)
			{
				z += reversed ? right : left;
				Z += reversed ? right : left;
			}
		}
		if (dir == DIR_EAST)
		{
			x = 1 - DOOR_WIDTH;
			if (opened && topBlock == reversed)
			{
				z += reversed ? left : right;
				Z += reversed ? left : right;
			}
		}

		if (opened && (topBlock == !reversed))
		{
			y += reversed ? left : right;
			if (topBlock || selBox)
				Y += reversed ? left : right;
			else
				Y = 0;
		}

		if (selBox && !opened)
		{
			if (!topBlock)
				Y++;
			else
				y--;
		}

		return AxisAlignedBB.getBoundingBox(x, y, z, X, Y, Z);
	}

	private Transformation getTransformation(DoorTileEntity tileEntity, boolean topBlock)
	{
		float dir = 1 - DOOR_WIDTH;
		float toX = 0;
		float toY = 0;
		if (tileEntity.isReversed())
			dir = -dir;

		if (topBlock != tileEntity.isReversed())
			toY = dir;
		else
			toX = dir;

		Translation translation = new Translation(toX, toY, 0);
		translation.reversed(tileEntity.getState() == DoorState.CLOSING || tileEntity.getState() == DoorState.CLOSED);
		translation.forTicks(tileEntity.getDescriptor().getOpeningTime());

		return translation;
	}

	@Override
	public Animation[] getAnimations(DoorTileEntity tileEntity, MalisisModel model, RenderParameters rp)
	{
		return new Animation[] { new Animation(model.getShape("top"), getTransformation(tileEntity, true)),
				new Animation(model.getShape("bottom"), getTransformation(tileEntity, false)) };
	}

	public boolean isSpecial()
	{
		return false;
	}

}
