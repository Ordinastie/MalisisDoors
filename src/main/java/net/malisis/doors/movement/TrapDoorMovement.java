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

import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.transformation.Rotation;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.core.util.AABBUtils;
import net.malisis.doors.DoorState;
import net.malisis.doors.block.Door;
import net.malisis.doors.tileentity.DoorTileEntity;
import net.malisis.doors.tileentity.TrapDoorTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * @author Ordinastie
 *
 */
public class TrapDoorMovement implements IDoorMovement
{
	@Override
	public AxisAlignedBB getOpenBoundingBox(DoorTileEntity tileEntity, boolean topBlock, BoundingBoxType type)
	{
		AxisAlignedBB aabb = new AxisAlignedBB(0, 0, 0, 1, Door.DOOR_WIDTH, 1);
		boolean top = ((TrapDoorTileEntity) tileEntity).isTop();
		if (top)
			aabb = aabb.offset(0, 1 - Door.DOOR_WIDTH, 0);
		if (tileEntity.isOpened())
			aabb = AABBUtils.rotate(aabb, top ? 1 : -1, EnumFacing.Axis.X);

		return aabb;
	}

	private Rotation getTransformation(DoorTileEntity tileEntity)
	{
		float f = 0.5F - Door.DOOR_WIDTH / 2;
		float fromAngle = 0, toAngle = 90;

		if (((TrapDoorTileEntity) tileEntity).isTop())
			toAngle = -toAngle;

		if (tileEntity.getState() == DoorState.CLOSING || tileEntity.getState() == DoorState.CLOSED)
		{
			float tmp = toAngle;
			toAngle = fromAngle;
			fromAngle = tmp;
		}

		return new Rotation(fromAngle, toAngle).aroundAxis(1, 0, 0).offset(0, -f, f).forTicks(tileEntity.getDescriptor().getOpeningTime());
	}

	@Override
	public Animation<?>[] getAnimations(DoorTileEntity tileEntity, MalisisModel model, RenderParameters rp)
	{
		return new Animation[] { new Animation<>(model, getTransformation(tileEntity)) };
	}

	@Override
	public boolean isSpecial()
	{
		return true;
	}
}
