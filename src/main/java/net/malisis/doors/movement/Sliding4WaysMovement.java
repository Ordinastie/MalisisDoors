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

package net.malisis.doors.movement;

import static net.malisis.doors.block.Door.*;
import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.transformation.Translation;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.doors.DoorState;
import net.malisis.doors.tileentity.DoorTileEntity;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * @author Ordinastie
 *
 */
public class Sliding4WaysMovement implements IDoorMovement
{

	@Override
	public AxisAlignedBB getOpenBoundingBox(DoorTileEntity tileEntity, boolean topBlock, BoundingBoxType type)
	{
		if (!tileEntity.isOpened())
			return IDoorMovement.getFullBoundingBox(topBlock, type);

		if (type == BoundingBoxType.COLLISION && !topBlock)
			return null;

		AxisAlignedBB aabb = IDoorMovement.getHalfBoundingBox();
		float x = 0, y = 0;
		if (topBlock != tileEntity.isHingeLeft())
			x = topBlock ? DOOR_WIDTH - 1 : 1 - DOOR_WIDTH;
		else
			y = topBlock ? 1 - DOOR_WIDTH : DOOR_WIDTH - 1;

		if (tileEntity.isOpened())
			aabb = aabb.offset(x, y, 0);

		return aabb;
	}

	private Translation getTransformation(DoorTileEntity tileEntity, boolean topBlock)
	{
		float dir = -1 + DOOR_WIDTH;
		float toX = 0;
		float toY = 0;
		if (tileEntity.isHingeLeft())
			dir = -dir;

		if (topBlock == tileEntity.isHingeLeft())
			toY = dir;
		else
			toX = dir;

		Translation translation = new Translation(toX, toY, 0);
		translation.reversed(tileEntity.getState() == DoorState.CLOSING || tileEntity.getState() == DoorState.CLOSED);
		translation.forTicks(tileEntity.getDescriptor().getOpeningTime());

		return translation;
	}

	@Override
	public Animation<?>[] getAnimations(DoorTileEntity tileEntity, MalisisModel model, RenderParameters rp)
	{
		return new Animation[] { new Animation<>(model.getShape("top"), getTransformation(tileEntity, true)),
				new Animation<>(model.getShape("bottom"), getTransformation(tileEntity, false)) };
	}

	@Override
	public boolean isSpecial()
	{
		return false;
	}
}
