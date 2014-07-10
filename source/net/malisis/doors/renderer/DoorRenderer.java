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

import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.animation.AnimationRenderer;
import net.malisis.core.renderer.animation.transformation.Rotation;
import net.malisis.core.renderer.animation.transformation.Transformation;
import net.malisis.core.renderer.animation.transformation.Translation;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.preset.ShapePreset;
import net.malisis.doors.block.doors.Door;
import net.malisis.doors.block.doors.DoorHandler;
import net.malisis.doors.block.doors.SlidingDoor;
import net.malisis.doors.entity.DoorTileEntity;

public class DoorRenderer extends BaseRenderer
{
	public static int renderId;
	protected DoorTileEntity tileEntity;
	protected int direction;
	protected boolean opened;
	protected boolean reversed;
	protected boolean topBlock;
	protected float width = DoorHandler.DOOR_WIDTH;

	protected Shape baseShape;
	protected Shape s;
	protected RenderParameters rp;
	protected AnimationRenderer ar = new AnimationRenderer(this);

	public DoorRenderer()
	{
		init();
	}

	protected void init()
	{
		//Shapes
		baseShape = ShapePreset.Cube();
		baseShape.setSize(1, 1, width);
		baseShape.scale(1, 1, 0.999F);

		//RenderParameters
		rp = new RenderParameters();
		rp.renderAllFaces.set(true);
		rp.calculateAOColor.set(false);
		rp.useBlockBounds.set(false);
		rp.useBlockBrightness.set(false);
		rp.interpolateUV.set(false);
	}

	@Override
	public void render()
	{
		init();
		if (renderType == TYPE_ISBRH_WORLD)
			return;

		int metadata = DoorHandler.getFullMetadata(world, x, y, z);
		direction = metadata & 3;
		opened = (metadata & DoorHandler.flagOpened) != 0;
		reversed = (metadata & DoorHandler.flagReversed) != 0;
		topBlock = (metadata & DoorHandler.flagTopBlock) != 0;
		this.tileEntity = (DoorTileEntity) super.tileEntity;

		//set rp
		rp.brightness.set(world.getLightBrightnessForSkyBlocks(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, 0));

		setup();
		renderTileEntity();
	}

	protected void setup()
	{
		this.tileEntity = (DoorTileEntity) super.tileEntity;

		//set shape
		s = new Shape(baseShape);

		if (direction == DoorHandler.DIR_SOUTH)
			s.rotate(180, 0, 1, 0);
		if (direction == DoorHandler.DIR_EAST)
			s.rotate(-90, 0, 1, 0);
		if (direction == DoorHandler.DIR_WEST)
			s.rotate(90, 0, 1, 0);

		//reset direction
		blockMetadata = 1 + (blockMetadata & (DoorHandler.flagTopBlock | DoorHandler.flagReversed));
	}

	public void renderTileEntity()
	{
		Transformation animation;
		if (block instanceof SlidingDoor)
		{
			float fromX = 0, toX = 1 - width;
			if (reversed)
			{
				fromX = 0;
				toX = -1 + width;
			}
			if (tileEntity.state == DoorHandler.stateClosing || tileEntity.state == DoorHandler.stateClose)
			{
				float tmp = fromX;
				fromX = toX;
				toX = tmp;
			}

			animation = new Translation(fromX, 0, 0, toX, 0, 0).forTicks(Door.openingTime);
		}
		else
		{
			float fromAngle = 0, toAngle = 90;
			float hinge = 0.5F - width / 2;
			float hingeZ = -0.5F + width / 2;

			if (reversed)
			{
				hinge = -hinge;
				toAngle = -90;
			}

			if (tileEntity.state == DoorHandler.stateClosing || tileEntity.state == DoorHandler.stateClose)
			{
				float tmp = toAngle;
				toAngle = fromAngle;
				fromAngle = tmp;
			}

			animation = new Rotation(fromAngle, toAngle).aroundAxis(0, 1, 0).offset(hinge, 0, hingeZ).forTicks(Door.openingTime);
		}

		ar.setStartTime(tileEntity.startTime);
		ar.animate(s, animation);

		drawShape(new Shape(s), rp);

		s.translate(0, 1F, 0);
		blockMetadata |= DoorHandler.flagTopBlock;
		drawShape(s, rp);
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return false;
	}

}
