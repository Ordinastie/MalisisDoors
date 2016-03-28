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

import java.util.List;

import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.transformation.Translation;
import net.malisis.core.renderer.element.MergedVertex;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.doors.DoorState;
import net.malisis.doors.block.Door;
import net.malisis.doors.tileentity.DoorTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

/**
 * @author Ordinastie
 *
 */
public class CurtainMovement implements IDoorMovement
{

	@Override
	public AxisAlignedBB getOpenBoundingBox(DoorTileEntity tileEntity, boolean topBlock, BoundingBoxType type)
	{
		AxisAlignedBB aabb = IDoorMovement.getFullBoundingBox(topBlock, type);
		return new AxisAlignedBB(tileEntity.isHingeLeft() ? 1 - DOOR_WIDTH : aabb.minX, aabb.minY, aabb.minZ,
				tileEntity.isHingeLeft() ? aabb.maxX : DOOR_WIDTH, aabb.maxY, aabb.maxZ);
	}

	@Override
	public Animation<?>[] getAnimations(DoorTileEntity tileEntity, MalisisModel model, RenderParameters rp)
	{
		float x = 1 - Door.DOOR_WIDTH;
		String dir = "west";
		EnumFacing fd = EnumFacing.WEST;
		if (!tileEntity.isHingeLeft())
		{
			x = -1 + Door.DOOR_WIDTH;
			dir = "east";
			fd = EnumFacing.EAST;
		}

		Translation translation = new Translation(x, 0, 0);
		translation.reversed(tileEntity.getState() == DoorState.CLOSING || tileEntity.getState() == DoorState.CLOSED);
		translation.forTicks(tileEntity.getDescriptor().getOpeningTime());

		Shape top = model.getShape("top");
		Shape bottom = model.getShape("bottom");

		top.enableMergedVertexes();
		bottom.enableMergedVertexes();

		List<MergedVertex> vertexes = top.getMergedVertexes("bottom", dir);
		vertexes.addAll(bottom.getMergedVertexes(fd));

		Animation<?>[] anims = new Animation[vertexes.size()];
		int i = 0;
		for (MergedVertex mv : vertexes)
			anims[i++] = new Animation<>(mv, translation);

		return anims;
	}

	@Override
	public boolean isSpecial()
	{
		return true;
	}
}
