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

package net.malisis.doors.renderer;

import net.malisis.core.MalisisCore;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.Rotation;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.preset.ShapePreset;
import net.malisis.doors.block.Door;
import net.malisis.doors.block.DoorHandler;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;

/**
 * @author Ordinastie
 * 
 */
public class FenceGateRenderer extends DoorRenderer
{
	public static int renderId;
	private Shape baseLeft, baseRight;
	private Shape left, right;
	private boolean opened, reversedOpen;
	private int direction;
	private float hingeOffset = -0.5F + 0.125F / 2;

	public FenceGateRenderer()
	{
		buildShapes();
	}

	private void buildShapes()
	{
		float w = 0.125F; // fence depth and hinge width
		float w2 = 0.1875F; //

		Shape hingeLeft = ShapePreset.Cube().setSize(w, 0.6875F, w).translate(0, 0.3125F, 0.5F - w / 2);
		Shape hingeRight = new Shape(hingeLeft).translate(1 - w, 0, 0);

		Shape gateHLeft = ShapePreset.Cube().setSize(w, w2 * 3, w).translate(0.5F - w, 0.375F, 0.5F - w / 2);
		Shape gateHRight = new Shape(gateHLeft).translate(w, 0, 0);

		Shape gateBottomLeft = ShapePreset.Cube().setSize(2 * w, w2, w).translate(w, 0.375F, 0.5F - w / 2);
		Shape gateTopLeft = new Shape(gateBottomLeft).translate(0, 2 * w2, 0);
		Shape gateBottomRight = new Shape(gateBottomLeft).translate(4 * w, 0, 0);
		Shape gateTopRight = new Shape(gateTopLeft).translate(4 * w, 0, 0);

		baseLeft = Shape.fromShapes(hingeLeft, gateHLeft, gateBottomLeft, gateTopLeft);
		baseRight = Shape.fromShapes(hingeRight, gateHRight, gateBottomRight, gateTopRight);
	}

	@Override
	protected void setupShape()
	{
		// work on copies
		left = new Shape(baseLeft);
		right = new Shape(baseRight);

		opened = (blockMetadata & DoorHandler.flagOpened) != 0;
		reversedOpen = ((blockMetadata >> 1) & 1) == 1;
		direction = blockMetadata & 3;

		if (direction == DoorHandler.DIR_NORTH || direction == DoorHandler.DIR_SOUTH)
		{
			reversedOpen = !reversedOpen;
			left.rotate(90, 0, 1, 0);
			right.rotate(90, 0, 1, 0);
		}

		applyTexture(left);
		applyTexture(right);

	}

	@Override
	protected void setupRenderParameters()
	{
		rp = new RenderParameters();
		rp.renderAllFaces.set(true);
		rp.useBlockBounds.set(false);
		rp.useBlockBrightness.set(true);
		rp.applyTexture.set(false);
		rp.brightness.set(world.getLightBrightnessForSkyBlocks(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, 0));
	}

	@Override
	public void renderBlock()
	{
		if (tileEntity.moving)
		{
			tileEntity.draw = true;
			return;
		}

		buildShapes();

		if (opened)
		{
			float angle = reversedOpen ? 90 : -90;
			float hingeX = hingeOffset;
			float hingeZ = 0;
			if (direction == DoorHandler.DIR_NORTH || direction == DoorHandler.DIR_SOUTH)
			{
				hingeX = 0;
				hingeZ = -hingeOffset;
			}
			left.rotate(angle, 0, 1, 0, hingeX, 0, hingeZ);
			right.rotate(-angle, 0, 1, 0, -hingeX, 0, -hingeZ);
		}

		drawShape(left, rp);
		drawShape(right, rp);
	}

	@Override
	public void renderTileEntity()
	{
		if (!tileEntity.draw)
			return;

		float fromAngle = 0, toAngle = 90;
		float hingeX = hingeOffset;
		float hingeZ = 0;
		if (direction == DoorHandler.DIR_NORTH || direction == DoorHandler.DIR_SOUTH)
		{
			hingeX = 0;
			hingeZ = -hingeOffset;
		}

		if (!reversedOpen)
			toAngle = -90;
		if (tileEntity.state == DoorHandler.stateClosing)
		{
			float tmp = fromAngle;
			fromAngle = toAngle;
			toAngle = tmp;
		}

		Animation animationLeft = new Rotation(fromAngle, toAngle).aroundAxis(0, 1, 0).offset(hingeX, 0, hingeZ);
		Animation animationRight = new Rotation(-fromAngle, -toAngle).aroundAxis(0, 1, 0).offset(-hingeX, 0, -hingeZ);

		ar.setStartTime(tileEntity.startTime);
		ar.render(animationLeft.forTicks(Door.openingTime, 0), left, null);
		ar.render(animationRight.forTicks(Door.openingTime, 0), right, null);

		drawShape(left, rp);
		drawShape(right, rp);
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer)
	{
		renderer.renderBlockAsItem(MalisisCore.orignalBlock(block), 0, 1);
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return true;
	}
}
