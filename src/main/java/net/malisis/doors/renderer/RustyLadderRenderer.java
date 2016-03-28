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

import net.malisis.core.block.component.DirectionalComponent;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.core.util.TransformBuilder;
import net.malisis.doors.MalisisDoors;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

/**
 * @author Ordinastie
 *
 */
public class RustyLadderRenderer extends MalisisRenderer<TileEntity>
{
	private Shape ladder;
	private RenderParameters rp;
	private Matrix4f gui = new TransformBuilder().translate(0, 0.1F, 0).rotate(0, 45, 0).scale(1.5F).get();
	private Matrix4f thirdPerson = new TransformBuilder().translate(0, 0, -0.20F).rotate(0, 110, 0).scale(-0.5F, 0.5F, 0.5F).get();

	@Override
	protected void initialize()
	{
		ResourceLocation rl = new ResourceLocation(MalisisDoors.modid, "models/rustyhatch.obj");
		MalisisModel model = new MalisisModel(rl);
		ladder = model.getShape("ladder");

		rp = new RenderParameters();
		rp.useBlockBounds.set(false);
		rp.calculateBrightness.set(false);
	}

	@Override
	public Matrix4f getTransform(TransformType tranformType)
	{
		switch (tranformType)
		{
			case GUI:
				return gui;
			case THIRD_PERSON_RIGHT_HAND:
			case THIRD_PERSON_LEFT_HAND:
				return thirdPerson;
			default:
				return null;
		}
	}

	@Override
	public void render()
	{
		ladder.resetState();

		if (renderType == RenderType.BLOCK)
		{
			EnumFacing dir = DirectionalComponent.getDirection(blockState);
			if (dir == EnumFacing.NORTH)
				ladder.rotate(-90, 0, 1, 0);
			else if (dir == EnumFacing.SOUTH)
				ladder.rotate(90, 0, 1, 0);
			else if (dir == EnumFacing.EAST)
				ladder.rotate(180, 0, 1, 0);
		}

		ladder.translate(-1, 0, 0);

		drawShape(ladder, rp);
	}
}
