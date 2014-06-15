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
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.AnimationRenderer2;
import net.malisis.core.renderer.animation.Rotation;
import net.malisis.core.renderer.animation.Translation;
import net.malisis.core.renderer.preset.ShapePreset;
import net.malisis.doors.block.Door;
import net.malisis.doors.block.DoorHandler;
import net.malisis.doors.block.SlidingDoor;
import net.malisis.doors.entity.DoorTileEntity;

import org.lwjgl.opengl.GL11;

public class DoorRenderer extends BaseRenderer
{
	public static int renderId;
	protected DoorTileEntity tileEntity;
	int direction;
	boolean opened;
	boolean reversed;
	boolean topBlock;
	float width = DoorHandler.DOOR_WIDTH;

	RenderParameters rp;

	AnimationRenderer2 ar = new AnimationRenderer2(this);

	protected void setupShape()
	{
		direction = blockMetadata & 3;
		opened = (blockMetadata & DoorHandler.flagOpened) != 0;
		reversed = (blockMetadata & DoorHandler.flagReversed) != 0;
		topBlock = (blockMetadata & DoorHandler.flagTopBlock) != 0;

		shape = ShapePreset.Cube();
		shape.setSize(1, 1, width);

		if (block instanceof SlidingDoor)
			shape.scale(1, 1, 0.999F);

		blockMetadata = 1 + (blockMetadata & (DoorHandler.flagTopBlock | DoorHandler.flagReversed));
		applyTexture(shape);

		if (direction == DoorHandler.DIR_SOUTH)
			shape.rotate(180, 0, 1, 0);
		if (direction == DoorHandler.DIR_EAST)
			shape.rotate(-90, 0, 1, 0);
		if (direction == DoorHandler.DIR_WEST)
			shape.rotate(90, 0, 1, 0);
	}

	protected void setupRenderParameters()
	{
		rp = new RenderParameters();
		rp.renderAllFaces.set(true);
		rp.calculateAOColor.set(false);
		rp.useBlockBounds.set(false);
		rp.useBlockBrightness.set(false);
		rp.applyTexture.set(false);
		rp.brightness.set(world.getLightBrightnessForSkyBlocks(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, 0));
	}

	@Override
	public void render()
	{
		blockMetadata = DoorHandler.getFullMetadata(world, x, y, z);
		boolean topBlock = (blockMetadata & DoorHandler.flagTopBlock) != 0;
		tileEntity = (DoorTileEntity) world.getTileEntity(x, y - (topBlock ? 1 : 0), z);

		if (tileEntity == null)
			return;

		setupShape();
		setupRenderParameters();

		if (typeRender == TYPE_WORLD)
			renderBlock();
		else if (typeRender == TYPE_TESR_WORLD)
			renderTileEntity();
	}

	public void renderBlock()
	{
		if (tileEntity.moving)
		{
			tileEntity.draw = true;
			return;
		}

		if (opened)
		{
			if (block instanceof SlidingDoor)
				shape.translate(reversed ? -1 + width : 1 - width, 0, 0);
			else
				shape.rotate(reversed ? -90 : 90, 0, 1, 0, reversed ? -0.5F + width / 2 : 0.5F - width / 2, 0, -0.5F + width / 2);
		}

		if (renderBlocks.hasOverrideBlockTexture())
			GL11.glTranslatef(0, (blockMetadata & DoorHandler.flagTopBlock) != 0 ? -0.5F : 0.5F, 0);

		drawShape(shape, rp);
		tileEntity.draw = false;
	}

	public void renderTileEntity()
	{
		if (!tileEntity.draw)
			return;

		Animation animation;
		if (block instanceof SlidingDoor)
		{
			float fromX = 0, toX = 1 - width;
			if (reversed)
			{
				fromX = 0;
				toX = -1 + width;
			}
			if (tileEntity.state == DoorHandler.stateClosing)
			{
				float tmp = fromX;
				fromX = toX;
				toX = tmp;
			}

			animation = new Translation(fromX, 0, 0, toX, 0, 0);
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

			if (tileEntity.state == DoorHandler.stateClosing)
			{
				float tmp = toAngle;
				toAngle = fromAngle;
				fromAngle = tmp;
			}

			animation = new Rotation(fromAngle, toAngle).aroundAxis(0, 1, 0).offset(hinge, 0, hingeZ);
		}

		ar.setStartTime(tileEntity.startTime);
		ar.render(animation.forTicks(Door.openingTime, 0), shape, null);

		drawShape(shape, rp);
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return false;
	}

}
