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

import javax.vecmath.Matrix4f;

import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.shape.Cube;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.block.Door;
import net.malisis.doors.tileentity.TrapDoorTileEntity;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.util.EnumFacing;

/**
 * @author Ordinastie
 *
 */
@SuppressWarnings("deprecation")
public class TrapDoorRenderer extends DoorRenderer
{
	public static TrapDoorRenderer instance = new TrapDoorRenderer();
	RenderParameters rpTop;
	MalisisModel trapDoorModel;
	MalisisModel slidingTrapDoorModel;

	public TrapDoorRenderer()
	{
		super(false);
		registerFor(TrapDoorTileEntity.class);
	}

	@Override
	protected void initialize()
	{
		Shape s = new Cube();
		s.setBounds(0, 1 - Door.DOOR_WIDTH, 0, 1, 1, 1);
		s.translate(0, -1 + Door.DOOR_WIDTH, 0);
		s.interpolateUV();

		trapDoorModel = new MalisisModel();
		trapDoorModel.addShape("shape", s);
		trapDoorModel.storeState();

		s.getFace(Face.nameFromDirection(EnumFacing.UP)).getParameters().calculateAOColor.set(true);

		s = new Cube();
		s.setSize(1, Door.DOOR_WIDTH / 2, 1);
		s.interpolateUV();

		slidingTrapDoorModel = new MalisisModel();
		slidingTrapDoorModel.addShape("shape", s);
		slidingTrapDoorModel.storeState();

		initParams();
	}

	@Override
	public boolean isGui3d()
	{
		return true;
	}

	@Override
	public Matrix4f getTransform(TransformType tranformType)
	{
		return null;
	}

	@Override
	public void render()
	{
		initialize();
		if (renderType == RenderType.BLOCK)
			return;

		if (renderType == RenderType.ITEM)
		{
			model = block == MalisisDoors.Blocks.slidingTrapDoor ? slidingTrapDoorModel : trapDoorModel;
			model.resetState();
			model.render(this, rp);
			return;
		}

		super.render();
	}

	@Override
	protected void setup()
	{
		model = block == MalisisDoors.Blocks.slidingTrapDoor ? slidingTrapDoorModel : trapDoorModel;
		model.resetState();

		float angle = 0;
		if (direction == EnumFacing.SOUTH)
			angle = 180;
		else if (direction == EnumFacing.WEST)
			angle = 90;
		else if (direction == EnumFacing.EAST)
			angle = 270;
		model.rotate(angle, 0, 1, 0, 0, 0, 0);

		if (((TrapDoorTileEntity) tileEntity).isTop())
			model.translate(0, 1 - Door.DOOR_WIDTH, 0);

		rp.brightness.set(block.getMixedBrightnessForBlock(world, pos));
		model.getShape("shape").deductParameters();
	}

	@Override
	protected void renderTileEntity()
	{
		ar.setStartTime(tileEntity.getTimer().getStart());

		setup();

		if (tileEntity.getMovement() != null)
		{
			Animation[] anims = tileEntity.getMovement().getAnimations(tileEntity, model, rp);
			ar.animate(anims);
		}

		//Shape s = model.getShape("shape");
		//		Face f = s.getFace(Face.nameFromDirection(EnumFacing.UP));
		//		s.applyMatrix();
		//		f.getParameters().aoMatrix.set(f.calculateAoMatrix(EnumFacing.UP));

		model.render(this, rp);
	}
}
