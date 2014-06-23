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
import net.malisis.core.renderer.preset.ShapePreset;
import net.malisis.doors.block.doors.Door;
import net.malisis.doors.block.doors.DoorHandler;
import net.malisis.doors.block.doors.TrapDoor;
import net.malisis.doors.entity.DoorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;

/**
 * @author Ordinastie
 * 
 */
public class TrapDoorRenderer extends DoorRenderer
{
	public static int renderId;

	public TrapDoorRenderer()
	{

	}

	@Override
	public void render()
	{
		blockMetadata = DoorHandler.getFullMetadata(world, x, y, z);
		tileEntity = (DoorTileEntity) world.getTileEntity(x, y, z);

		if (tileEntity == null)
			return;

		setupShape();
		setupRenderParameters();

		if (renderType == TYPE_ISBRH_WORLD)
			renderBlock();
		else if (renderType == TYPE_TESR_WORLD)
			renderTileEntity();
	}

	@Override
	protected void setupShape()
	{
		direction = blockMetadata & 3;
		opened = (blockMetadata & DoorHandler.flagOpened) != 0;
		topBlock = (blockMetadata & DoorHandler.flagTopBlock) != 0;

		shape = ShapePreset.Cube();
		shape.setSize(1, width, 1);

		applyTexture(shape);

		float angle = 0;
		if (direction == TrapDoor.DIR_NORTH)
			angle = 180;
		else if (direction == TrapDoor.DIR_EAST)
			angle = 90;
		else if (direction == TrapDoor.DIR_WEST)
			angle = 270;
		shape.rotate(angle, 0, 1, 0);

		if (topBlock)
			shape.translate(0, 1 - width, 0);

	}

	@Override
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
	public void renderBlock()
	{
		if (tileEntity.moving)
		{
			tileEntity.draw = true;
			return;
		}

		float f = 0.5F - width / 2;
		if (opened)
			shape.rotate(topBlock ? -90 : 90, 1, 0, 0, 0, -f, f);

		drawShape(shape, rp);
	}

	@Override
	public void renderTileEntity()
	{
		if (!tileEntity.draw)
			return;

		Animation animation;
		float f = 0.5F - width / 2;
		float fromAngle = 0, toAngle = 90;

		if (topBlock)
			toAngle = -toAngle;

		if (tileEntity.state == DoorHandler.stateClosing)
		{
			float tmp = toAngle;
			toAngle = fromAngle;
			fromAngle = tmp;
		}

		animation = new Rotation(fromAngle, toAngle).aroundAxis(1, 0, 0).offset(0, -f, f);

		ar.setStartTime(tileEntity.startTime);
		ar.render(animation.forTicks(Door.openingTime, 0), shape, null);

		drawShape(shape, rp);
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
