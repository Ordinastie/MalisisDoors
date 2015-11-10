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
import net.malisis.core.renderer.animation.transformation.Transformation;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.doors.DoorState;
import net.malisis.doors.tileentity.DoorTileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing.Axis;

/**
 * @author Ordinastie
 *
 */
public class FenceGateMovement implements IDoorMovement
{

	@Override
	public AxisAlignedBB getOpenBoundingBox(DoorTileEntity tileEntity, boolean topBlock, BoundingBoxType type)
	{
		//never called
		return null;
	}

	public Transformation getTransformation(DoorTileEntity tileEntity, boolean left)
	{
		boolean reversedOpen = ((tileEntity.getBlockMetadata() >> 1) & 1) == 1;

		float hinge = -0.5F + 0.125F / 2;
		float angle = 90;
		if (tileEntity.getDirection().getAxis() == Axis.X)
			angle = -angle;
		if (!reversedOpen)
			angle = -angle;
		if (left)
		{
			angle = -angle;
			hinge = -hinge;
		}

		Rotation rotation = new Rotation(angle).aroundAxis(0, 1, 0).offset(hinge, 0, 0);
		rotation.reversed(tileEntity.getState() == DoorState.CLOSING || tileEntity.getState() == DoorState.CLOSED);
		rotation.forTicks(tileEntity.getDescriptor().getOpeningTime());

		return rotation;
	}

	@Override
	public Animation[] getAnimations(DoorTileEntity tileEntity, MalisisModel model, RenderParameters rp)
	{
		DoorTileEntity doubleDoor = tileEntity.getDoubleDoor();
		if (doubleDoor != null)
		{
			boolean left = true;
			if (tileEntity.getDirection().getAxis() == Axis.X)
				left = tileEntity.getPos().getZ() < doubleDoor.getPos().getZ();
			else
				left = tileEntity.getPos().getX() > doubleDoor.getPos().getX();
			return new Animation[] { new Animation(model, getTransformation(tileEntity, left)) };
		}

		return new Animation[] { new Animation(model.getShape("left"), getTransformation(tileEntity, true)),
				new Animation(model.getShape("right"), getTransformation(tileEntity, false)) };
	}

	@Override
	public boolean isSpecial()
	{
		return true;
	}
}
