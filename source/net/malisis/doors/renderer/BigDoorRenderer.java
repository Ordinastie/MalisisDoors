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

import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.AnimationRenderer;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.icon.VanillaIcon;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.block.BigDoor;
import net.malisis.doors.block.BigDoor.BigDoorIconProvider;
import net.malisis.doors.tileentity.BigDoorTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

/**
 * @author Ordinastie
 *
 */
public class BigDoorRenderer extends MalisisRenderer
{
	private BigDoor block;
	private BigDoorTileEntity tileEntity;

	private ResourceLocation rl;
	private MalisisModel model;
	private Shape frame;
	private Shape doorLeft;
	private Shape doorRight;
	private RenderParameters rp;
	private AnimationRenderer ar = new AnimationRenderer();

	private EnumFacing direction;

	public BigDoorRenderer()
	{
		registerFor(BigDoorTileEntity.class);
	}

	@Override
	protected void initialize()
	{
		rl = new ResourceLocation(MalisisDoors.modid, "models/big_door.obj");
		model = new MalisisModel(rl);
		frame = model.getShape("Frame");
		doorLeft = model.getShape("Left");
		doorRight = model.getShape("Right");

		rp = new RenderParameters();
		rp.useBlockBounds.set(false);
		rp.calculateAOColor.set(false);
		rp.calculateBrightness.set(false);
	}

	@Override
	public void render()
	{
		block = (BigDoor) super.block;
		tileEntity = (BigDoorTileEntity) super.tileEntity;
		if (super.tileEntity == null)
			return;

		setup();

		if (renderType == RenderType.BLOCK)
		{
			//initialize();
			renderBlock();
			//	drawShape(new Cube(), rp);
		}

		else if (renderType == RenderType.TILE_ENTITY)
			renderTileEntity();
	}

	private void renderBlock()
	{
		IBlockState state = tileEntity.getFrameState();

		if (!state.getBlock().canRenderInLayer(getRenderLayer()))
			return;

		rp.icon.set(new VanillaIcon(state));
		set(state);
		drawShape(frame, rp);
	}

	private void renderTileEntity()
	{
		ar.setStartTime(tileEntity.getTimer().getStart());

		if (tileEntity.getMovement() != null)
		{
			Animation[] anims = tileEntity.getMovement().getAnimations(tileEntity, model, rp);
			ar.animate(anims);
		}

		next(GL11.GL_POLYGON);
		rp.icon.set(((BigDoorIconProvider) block.getIconProvider()).getDoorIcon());
		drawShape(doorLeft, rp);
		drawShape(doorRight, rp);
	}

	private void setup()
	{
		direction = tileEntity.getDirection();

		model.resetState();
		if (direction == EnumFacing.NORTH)
			model.rotate(180, 0, 1, 0, 0, 0, 0);
		else if (direction == EnumFacing.EAST)
			model.rotate(90, 0, 1, 0, 0, 0, 0);
		else if (direction == EnumFacing.WEST)
			model.rotate(-90, 0, 1, 0, 0, 0, 0);

		rp.brightness.set(block.getMixedBrightnessForBlock(world, pos));
	}
}
