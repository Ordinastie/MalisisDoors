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
import net.malisis.core.renderer.animation.transformation.ParallelTransformation;
import net.malisis.core.renderer.animation.transformation.Rotation;
import net.malisis.core.renderer.animation.transformation.Transformation;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.core.util.MultiBlock;
import net.malisis.doors.door.DoorState;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.malisis.doors.door.tileentity.RustyHatchTileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @author Ordinastie
 *
 */
public class RustyHatchMovement implements IDoorMovement
{
	@Override
	public AxisAlignedBB getBoundingBox(DoorTileEntity tileEntity, boolean topBlock, BoundingBoxType type)
	{
		if (!(tileEntity instanceof RustyHatchTileEntity))
			return null;

		MultiBlock mb = ((RustyHatchTileEntity) tileEntity).getMultiBlock();
		if (mb == null)
			return null;

		float f = 0.125F;
		AxisAlignedBB aabb = mb.getWorldBounds();
		ForgeDirection dir = ForgeDirection.getOrientation(tileEntity.getDirection());

		if (!tileEntity.isOpened())
		{
			if (topBlock)
			{
				aabb.minY = aabb.minY + 1 - f;
				aabb.maxY = aabb.minY + f;
			}
			else
			{
				aabb.minY = aabb.minY + 2;
				aabb.maxY = aabb.minY + f;
			}
		}
		else
		{
			aabb.minX += f;
			aabb.maxX -= f;
			aabb.minZ += f;
			aabb.maxZ -= f;

			if (topBlock)
			{
				aabb.minY = aabb.minY + 1;
				aabb.maxY = aabb.maxY - 2 * f;
			}
			else
			{
				aabb.minY = aabb.minY + 2 * f;
				aabb.maxY = aabb.maxY - 1;
			}

			//MalisisCore.message(dir);
			if (dir == ForgeDirection.NORTH)
				aabb.minZ = aabb.maxZ - f;
			if (dir == ForgeDirection.SOUTH)
				aabb.maxZ = aabb.minZ + f;
			if (dir == ForgeDirection.EAST)
				aabb.maxX = aabb.minX + f;
			if (dir == ForgeDirection.WEST)
				aabb.minX = aabb.maxX - f;
		}

		return aabb;
	}

	private Transformation getDoorTransformation(DoorTileEntity tileEntity)
	{
		float f = -0.5F + 0.125F;
		float offX = f;
		float offY = f;
		float toAngle = 90;

		if (!tileEntity.isTopBlock(0, 0, 0))
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

	private Transformation getHandleTransformation(DoorTileEntity tileEntity)
	{
		int t = tileEntity.getDescriptor().getOpeningTime() / 2;
		Rotation rotation = new Rotation(400).aroundAxis(0, 1, 0).offset(0.5F, 0, 0.5F).movement(Transformation.SINUSOIDAL);
		if (tileEntity.getState() == DoorState.CLOSING || tileEntity.getState() == DoorState.CLOSED)
			rotation.delay(t).reversed(true);
		rotation.forTicks(t);

		return rotation;
	}

	@Override
	public Animation[] getAnimations(DoorTileEntity tileEntity, MalisisModel model, RenderParameters rp)
	{
		Transformation transform = new ParallelTransformation(getDoorTransformation(tileEntity), getHandleTransformation(tileEntity));
		return new Animation[] { new Animation(model.getShape("door"), getDoorTransformation(tileEntity)),
				new Animation(model.getShape("handle"), transform) };
	}

	@Override
	public boolean isSpecial()
	{
		return true;
	}
}
