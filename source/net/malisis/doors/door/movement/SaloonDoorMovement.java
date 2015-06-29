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

import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.transformation.ChainedTransformation;
import net.malisis.core.renderer.animation.transformation.Rotation;
import net.malisis.core.renderer.animation.transformation.Transformation;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.malisis.doors.door.tileentity.SaloonDoorTileEntity;
import net.minecraft.util.AxisAlignedBB;

/**
 * @author Ordinastie
 *
 */
public class SaloonDoorMovement implements IDoorMovement
{

	@Override
	public AxisAlignedBB getBoundingBox(DoorTileEntity te, boolean topBlock, BoundingBoxType type)
	{
		if (type == BoundingBoxType.COLLISION || te.isMoving())
			return null;

		float f = 1 / 16F;
		AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(0, 0.25F, .5F - f / 2, 1, 1, .5F + f / 2);
		if (topBlock)
			aabb.offset(0, -0.25F, 0);

		if (type == BoundingBoxType.SELECTION)
		{
			if (topBlock)
				aabb.minY -= .75F;
			else
				aabb.maxY += .75F;
		}

		return aabb;
	}

	private Transformation getTransformation(DoorTileEntity tileEntity)
	{
		float f = 1 / 16F;
		float angle = 90;
		float hingeX = 0.5F - f / 2;
		//float hingeZ = -0.5F + DOOR_WIDTH / 2;

		if (tileEntity.isReversed())
			angle = -angle;

		if (((SaloonDoorTileEntity) tileEntity).isBackward())
			angle = -angle;

		int t = tileEntity.getDescriptor().getOpeningTime() / 4;
		Rotation r1 = new Rotation(angle);
		r1.aroundAxis(0, 1, 0).offset(hingeX, 0, 0);
		r1.movement(Transformation.SINUSOIDAL);
		//r1.reversed(tileEntity.getState() == DoorState.CLOSING || tileEntity.getState() == DoorState.CLOSED);
		r1.forTicks(t);

		Rotation r2 = new Rotation(-angle * 1.5F);
		r2.aroundAxis(0, 1, 0).offset(hingeX, 0, 0);
		r2.movement(Transformation.SINUSOIDAL);
		//r1.reversed(tileEntity.getState() == DoorState.CLOSING || tileEntity.getState() == DoorState.CLOSED);
		r2.forTicks(t);

		Rotation r3 = new Rotation(angle * .75F);
		r3.aroundAxis(0, 1, 0).offset(hingeX, 0, 0);
		r3.movement(Transformation.SINUSOIDAL);
		//r1.reversed(tileEntity.getState() == DoorState.CLOSING || tileEntity.getState() == DoorState.CLOSED);
		r3.forTicks(t);

		Rotation r4 = new Rotation(-angle * .25F);
		r4.aroundAxis(0, 1, 0).offset(hingeX, 0, 0);
		r4.movement(Transformation.SINUSOIDAL);
		//r1.reversed(tileEntity.getState() == DoorState.CLOSING || tileEntity.getState() == DoorState.CLOSED);
		r4.forTicks(t);

		ChainedTransformation ct = new ChainedTransformation(r1, r2, r3, r4);

		return ct;
	}

	@Override
	public Animation[] getAnimations(DoorTileEntity tileEntity, MalisisModel model, RenderParameters rp)
	{
		return new Animation[] { new Animation(model, getTransformation(tileEntity)) };
	}

	@Override
	public boolean isSpecial()
	{
		return true;
	}
}
