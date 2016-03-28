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

import java.util.Set;

import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.animation.AnimationRenderer;
import net.malisis.core.renderer.animation.transformation.ChainedTransformation;
import net.malisis.core.renderer.animation.transformation.ParallelTransformation;
import net.malisis.core.renderer.animation.transformation.Rotation;
import net.malisis.core.renderer.animation.transformation.Translation;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.shape.Cube;
import net.malisis.core.util.EnumFacingUtils;
import net.malisis.doors.DoorState;
import net.malisis.doors.block.Door;
import net.malisis.doors.tileentity.GarageDoorTileEntity;
import net.minecraft.util.EnumFacing;

/**
 * @author Ordinastie
 *
 */
public class GarageDoorRenderer extends MalisisRenderer<GarageDoorTileEntity>
{
	protected EnumFacing direction;
	protected boolean opened;
	protected boolean topBlock;

	protected Shape shape;
	protected RenderParameters rp;
	protected AnimationRenderer ar = new AnimationRenderer();

	public GarageDoorRenderer()
	{
		registerFor(GarageDoorTileEntity.class);
	}

	@Override
	protected void initialize()
	{
		shape = new Cube().setBounds(0, 0, 0.5F - Door.DOOR_WIDTH / 2, 1, 1, 0.5F + Door.DOOR_WIDTH / 2);
		shape.storeState();

		rp = new RenderParameters();
		rp.renderAllFaces.set(true);
		rp.calculateAOColor.set(false);
		rp.useBlockBounds.set(false);
		rp.useEnvironmentBrightness.set(false);
		rp.calculateBrightness.set(false);
		rp.interpolateUV.set(false);
		rp.useWorldSensitiveIcon.set(false);

	}

	@Override
	public void render()
	{
		if (tileEntity == null || !tileEntity.isTop())
			return;

		direction = tileEntity.getDirection();

		enableBlending();
		renderTileEntity();
	}

	protected void renderTileEntity()
	{
		int t = GarageDoorTileEntity.maxOpenTime;
		//set the start timer
		ar.setStartTime(tileEntity.getTimer().getStart());

		//create door list from childs + top
		Set<GarageDoorTileEntity> doors = tileEntity.getDoors();

		for (GarageDoorTileEntity te : doors)
		{
			shape.resetState();
			shape.rotate(EnumFacingUtils.getRotationCount(tileEntity.getDirection()) * 90, 0, 1, 0);

			pos = te.getPos();
			set(world.getBlockState(pos));
			int delta = tileEntity.getPos().getY() - pos.getY();
			int delta2 = doors.size() - (delta + 1);

			Translation verticalAnim = new Translation(0, -delta, 0, 0, 0, 0).forTicks(t * delta, 0);
			//@formatter:off
			ParallelTransformation topRotate = new ParallelTransformation(
					new Translation(0, 1, 0).forTicks(t, 0),
					new Rotation(0, -90).aroundAxis(1, 0, 0).offset(-0.5F, -0.5F, 0).forTicks(t, 0)
			);
			//@formatter:on
			Translation horizontalAnim = new Translation(0, 0, 0, 0, delta2, 0).forTicks(t * delta2, 0);

			ChainedTransformation chained = new ChainedTransformation(verticalAnim, topRotate, horizontalAnim);
			if (tileEntity.getState() == DoorState.CLOSING || tileEntity.getState() == DoorState.CLOSED)
				chained.reversed(true);

			rp.brightness.set(blockState.getPackedLightmapCoords(world, pos));
			rp.rotateIcon.set(false);

			ar.animate(shape, chained);
			drawShape(shape, rp);
		}
		//restore correct y coord
		pos = tileEntity.getPos();
	}

}
