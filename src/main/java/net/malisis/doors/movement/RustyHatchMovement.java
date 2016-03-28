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
import net.malisis.core.renderer.animation.transformation.ParallelTransformation;
import net.malisis.core.renderer.animation.transformation.Rotation;
import net.malisis.core.renderer.animation.transformation.Transformation;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.doors.DoorState;
import net.malisis.doors.tileentity.DoorTileEntity;
import net.malisis.doors.tileentity.RustyHatchTileEntity;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * @author Ordinastie
 *
 */
public class RustyHatchMovement implements IDoorMovement
{
	@Override
	public AxisAlignedBB getClosedBoundingBox(DoorTileEntity te, boolean topBlock, BoundingBoxType type)
	{
		return new AxisAlignedBB(-1, topBlock ? 0.875F : 0, 0, 1, topBlock ? 1 : 0.125F, 2);
	}

	@Override
	public AxisAlignedBB getOpenBoundingBox(DoorTileEntity tileEntity, boolean topBlock, BoundingBoxType type)
	{
		float f = 0.125F;
		return new AxisAlignedBB(-1 + f, topBlock ? 1 : -2 + 2 * f, f, 1 - f, topBlock ? 3 - 2 * f : 0, 2 * f);
	}

	private Rotation getDoorTransformation(DoorTileEntity tileEntity)
	{
		float f = -0.5F + 0.125F;
		float offX = f;
		float offY = f;
		float toAngle = 90;

		if (!((RustyHatchTileEntity) tileEntity).isTop())
		{
			toAngle = -toAngle;
			offY = -0.5F;
		}

		int t = tileEntity.getDescriptor().getOpeningTime() / 2;
		Rotation rotation = new Rotation(toAngle).aroundAxis(0, 0, 1).offset(offX, offY, 0).movement(Transformation.SINUSOIDAL);

		if (tileEntity.getState() == DoorState.CLOSING || tileEntity.getState() == DoorState.CLOSED)
			rotation.reversed(true);
		else
			rotation.delay(t);
		rotation.forTicks(t);

		return rotation;
	}

	private Rotation getHandleTransformation(DoorTileEntity tileEntity)
	{
		int t = tileEntity.getDescriptor().getOpeningTime() / 2;
		Rotation rotation = new Rotation(400).aroundAxis(0, 1, 0).offset(0.5F, 0, 0.5F).movement(Transformation.SINUSOIDAL);
		if (tileEntity.getState() == DoorState.CLOSING || tileEntity.getState() == DoorState.CLOSED)
			rotation.delay(t).reversed(true);
		rotation.forTicks(t);

		return rotation;
	}

	@Override
	public Animation<?>[] getAnimations(DoorTileEntity tileEntity, MalisisModel model, RenderParameters rp)
	{
		ParallelTransformation transform = new ParallelTransformation(getDoorTransformation(tileEntity),
				getHandleTransformation(tileEntity));
		return new Animation[] { new Animation<>(model.getShape("door"), getDoorTransformation(tileEntity)),
				new Animation<>(model.getShape("handle"), transform) };
	}

	@Override
	public boolean isSpecial()
	{
		return true;
	}
}
