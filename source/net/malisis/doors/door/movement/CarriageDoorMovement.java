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
import net.malisis.core.renderer.animation.transformation.Rotation;
import net.malisis.core.renderer.animation.transformation.Transformation;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.core.util.MultiBlock;
import net.malisis.doors.door.DoorState;
import net.malisis.doors.door.block.Door;
import net.malisis.doors.door.tileentity.CarriageDoorTileEntity;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @author Ordinastie
 *
 */
public class CarriageDoorMovement implements IDoorMovement
{
	@Override
	public AxisAlignedBB getBoundingBox(DoorTileEntity tileEntity, boolean left, BoundingBoxType type)
	{
		if (!(tileEntity instanceof CarriageDoorTileEntity))
			return null;

		MultiBlock mb = ((CarriageDoorTileEntity) tileEntity).getMultiBlock();
		if (mb == null)
			return null;

		float f = Door.DOOR_WIDTH;
		AxisAlignedBB aabb = mb.getWorldBounds();
		ForgeDirection dir = ForgeDirection.getOrientation(tileEntity.getDirection());

		//MalisisCore.message(dir);
		if (dir == ForgeDirection.NORTH)
			aabb.minZ = aabb.maxZ - f;
		if (dir == ForgeDirection.SOUTH)
			aabb.maxZ = aabb.minZ + f;
		if (dir == ForgeDirection.EAST)
			aabb.maxX = aabb.minX + f;
		if (dir == ForgeDirection.WEST)
			aabb.minX = aabb.maxX - f;

		if (tileEntity.getState() != DoorState.CLOSED && type != BoundingBoxType.SELECTION)
		{

			if ((dir == ForgeDirection.NORTH && left) || (dir == ForgeDirection.SOUTH && !left))
				aabb.maxX = aabb.minX + 0.5F;
			if ((dir == ForgeDirection.SOUTH && left) || (dir == ForgeDirection.NORTH && !left))
				aabb.minX = aabb.maxX - 0.5F;
			if ((dir == ForgeDirection.EAST && left) || (dir == ForgeDirection.WEST && !left))
				aabb.maxZ = aabb.minZ + 0.5F;
			if ((dir == ForgeDirection.WEST && left) || (dir == ForgeDirection.EAST && !left))
				aabb.minZ = aabb.maxZ - 0.5F;

		}

		return aabb;
	}

	/**
	 * @param b
	 * @return
	 */
	private Transformation getRotation(DoorTileEntity tileEntity, boolean right)
	{
		float fz = 0.5F - Door.DOOR_WIDTH / 2;
		float fx = 0;
		float angle = 105;
		if (right)
		{
			fx = 3;
			angle = -angle;
		}

		Rotation rotation = new Rotation(angle).aroundAxis(0, 1, 0).offset(fx, 0, fz);
		rotation.reversed(tileEntity.getState() == DoorState.CLOSING || tileEntity.getState() == DoorState.CLOSED);
		rotation.forTicks(tileEntity.getDescriptor().getOpeningTime());

		return rotation;
	}

	@Override
	public Animation[] getAnimations(DoorTileEntity tileEntity, MalisisModel model, RenderParameters rp)
	{
		return new Animation[] { new Animation(model.getShape("left"), getRotation(tileEntity, false)),
				new Animation(model.getShape("right"), getRotation(tileEntity, true)) };
	}

	@Override
	public boolean isSpecial()
	{
		return true;
	}

}
