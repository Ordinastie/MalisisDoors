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
import net.malisis.core.renderer.animation.transformation.ParallelTransformation;
import net.malisis.core.renderer.animation.transformation.Rotation;
import net.malisis.core.renderer.animation.transformation.Transformation;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.core.util.AABBUtils;
import net.malisis.doors.DoorState;
import net.malisis.doors.MalisisDoorsSettings;
import net.malisis.doors.block.Door;
import net.malisis.doors.tileentity.DoorTileEntity;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * @author Ordinastie
 *
 */
public class VerticalHatchMovement implements IDoorMovement
{
	@Override
	public AxisAlignedBB getClosedBoundingBox(DoorTileEntity te, boolean topBlock, BoundingBoxType type)
	{
		return IDoorMovement.super.getClosedBoundingBox(te, topBlock, type);
	}

	@Override
	public AxisAlignedBB getOpenBoundingBox(DoorTileEntity tileEntity, boolean topBlock, BoundingBoxType type)
	{
		AxisAlignedBB aabb = new AxisAlignedBB(0, topBlock && type == BoundingBoxType.SELECTION ? -1 : 0, 0, 1, !topBlock
				&& type == BoundingBoxType.SELECTION ? 2 : 1, Door.DOOR_WIDTH);
		return AABBUtils.rotate(aabb.offset(tileEntity.isHingeLeft() ? DOOR_WIDTH : -DOOR_WIDTH, 0, 0), tileEntity.isHingeLeft() ? -1 : 1);
	}

	private Rotation getDoorTransformation(DoorTileEntity tileEntity)
	{
		float angle = -90;
		float hingeX = -0.5F;
		int t = tileEntity.getDescriptor().getOpeningTime() / 2;

		if (tileEntity.isHingeLeft())
		{
			hingeX = -hingeX;
			angle = -angle;
		}

		Rotation rotation = new Rotation(angle);
		rotation.aroundAxis(0, 1, 0).offset(hingeX, 0, -0.5F + DOOR_WIDTH);
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
		Rotation rotation = new Rotation(45, 600).aroundAxis(0, 0, 1).offset(0, .53125F, 0).movement(Transformation.SINUSOIDAL);
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
		return new Animation[] { new Animation<>(model.getShape("Door"), getDoorTransformation(tileEntity)),
				new Animation<>(model.getShape(MalisisDoorsSettings.use3branchgHandle.get() ? "Handle3" : "Handle4"), transform) };
	}

	@Override
	public boolean isSpecial()
	{
		return true;
	}
}
