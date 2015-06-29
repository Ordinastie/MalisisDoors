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
public class DoubleSlideMovement implements IDoorMovement
{
	boolean rightDirection = false;

	public DoubleSlideMovement(boolean right)
	{
		this.rightDirection = right;
	}

	@Override
	public AxisAlignedBB getBoundingBox(DoorTileEntity tileEntity, boolean topBlock, BoundingBoxType type)
	{
		if ((tileEntity.isReversed() != rightDirection) && tileEntity.isOpened())
			return null;

		AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, DOOR_WIDTH);
		if (type == BoundingBoxType.SELECTION)
		{
			if (!topBlock)
				aabb.maxY++;
			else
				aabb.minY--;
		}

		if (tileEntity.isOpened())
			aabb.offset(rightDirection ? -1 + DOOR_WIDTH : 1 - DOOR_WIDTH, 0, 0);

		return aabb;
	}

	private Transformation getTransformation(DoorTileEntity tileEntity)
	{
		float x = 1 - DOOR_WIDTH;
		if (tileEntity.isReversed() != rightDirection)
			x -= -1;
		if (rightDirection)
			x *= -1;

		Translation translation = new Translation(0, 0, 0, x, 0, 0);
		translation.reversed(tileEntity.getState() == DoorState.CLOSING || tileEntity.getState() == DoorState.CLOSED);
		translation.forTicks(tileEntity.getDescriptor().getOpeningTime());

		return translation;
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
