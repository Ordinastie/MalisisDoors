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

import java.util.HashSet;
import java.util.Set;

import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.animation.AnimationRenderer;
import net.malisis.core.renderer.animation.transformation.ChainedTransformation;
import net.malisis.core.renderer.animation.transformation.ParallelTransformation;
import net.malisis.core.renderer.animation.transformation.Rotation;
import net.malisis.core.renderer.animation.transformation.Transformation;
import net.malisis.core.renderer.animation.transformation.Translation;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.preset.ShapePreset;
import net.malisis.doors.block.doors.DoorHandler;
import net.malisis.doors.entity.GarageDoorTileEntity;

/**
 * @author Ordinastie
 * 
 */
public class GarageDoorRenderer extends BaseRenderer
{
	public static int renderId;
	private GarageDoorTileEntity tileEntity;
	private AnimationRenderer ar = new AnimationRenderer(this);
	private RenderParameters rp;
	private Shape baseShape;

	private void setupShape()
	{
		baseShape = ShapePreset.Cube();
		baseShape.setSize(DoorHandler.DOOR_WIDTH, 1, 1);
		baseShape.rotate(-90 * tileEntity.getDirection(), 0, 1, 0);
		baseShape.translate(0.5F - DoorHandler.DOOR_WIDTH / 2, 0, 0);
	}

	private void setupRenderParameters()
	{
		rp = new RenderParameters();
		rp.calculateAOColor.set(false);
		rp.useBlockBounds.set(false);
		rp.interpolateUV.set(false);
		rp.renderAllFaces.set(true);
		rp.useBlockBrightness.set(false);
		rp.brightness.set(world.getLightBrightnessForSkyBlocks(x, y, z, 0));
	}

	@Override
	public void render()
	{
		tileEntity = (GarageDoorTileEntity) world.getTileEntity(x, y, z);
		if (tileEntity == null)
			return;

		setupShape();
		setupRenderParameters();

		if (renderType == TYPE_ISBRH_WORLD)
			renderBlock();
		else if (renderType == TYPE_TESR_WORLD)
			renderTileEntity();
	}

	private void renderBlock()
	{
		if (tileEntity.getState() != DoorHandler.stateClose)
		{
			tileEntity.draw = true;
			return;
		}

		drawShape(baseShape, rp);
		tileEntity.draw = false;
	}

	private void renderTileEntity()
	{
		if (!tileEntity.isTopDoor() || !tileEntity.draw)
			return;

		int t = GarageDoorTileEntity.maxOpenTime;
		//set the start timer
		ar.setStartTime(tileEntity.startTime);

		//create door list from childs + top
		Set<GarageDoorTileEntity> doors = new HashSet<>(tileEntity.getChildDoors());
		doors.add(tileEntity);

		for (GarageDoorTileEntity te : doors)
		{
			blockMetadata = te.blockMetadata;
			int delta = tileEntity.yCoord - te.yCoord;
			int delta2 = doors.size() - (delta + 1);

			Transformation verticalAnim = new Translation(0, -delta, 0, 0, 0, 0).forTicks(t * delta, 0);
			//@formatter:off
			Transformation topRotate = new ParallelTransformation(
					new Translation(0, 1, 0).forTicks(t, 0), 
					new Rotation(0, -90).aroundAxis(0, 0, 1).offset(-0.5F, -0.5F, 0).forTicks(t, 0)
			);
			//@formatter:on
			Transformation horizontalAnim = new Translation(0, 0, 0, 0, delta2, 0).forTicks(t * delta2, 0);

			Transformation chained = new ChainedTransformation(verticalAnim, topRotate, horizontalAnim);
			if (tileEntity.getState() == DoorHandler.stateClosing || tileEntity.getState() == DoorHandler.stateClose)
				chained.reversed(true);

			Shape s = new Shape(baseShape);
			ar.animate(s, chained);
			drawShape(s, rp);
		}

	}
}
