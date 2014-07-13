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
import net.malisis.core.renderer.animation.transformation.Rotation;
import net.malisis.core.renderer.animation.transformation.Transformation;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.preset.ShapePreset;
import net.malisis.doors.block.doors.Door;
import net.malisis.doors.block.doors.DoorHandler;
import net.malisis.doors.block.doors.TrapDoor;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.RenderBlocks;

/**
 * @author Ordinastie
 * 
 */
public class TrapDoorRenderer extends DoorRenderer
{
	public static int renderId;

	@Override
	protected void setup()
	{
		s = ShapePreset.Cube();
		s.setSize(1, width, 1);

		applyTexture(s);
		rp.applyTexture.set(false);

		float angle = 0;
		if (direction == TrapDoor.DIR_NORTH)
			angle = 180;
		else if (direction == TrapDoor.DIR_EAST)
			angle = 90;
		else if (direction == TrapDoor.DIR_WEST)
			angle = 270;
		s.rotate(angle, 0, 1, 0);

		if (topBlock)
			s.translate(0, 1 - width, 0);

	}

	@Override
	public void renderTileEntity()
	{
		Transformation animation;
		float f = 0.5F - width / 2;
		float fromAngle = 0, toAngle = 90;

		if (topBlock)
			toAngle = -toAngle;

		if (tileEntity.state == DoorHandler.stateClosing || tileEntity.state == DoorHandler.stateClose)
		{
			float tmp = toAngle;
			toAngle = fromAngle;
			fromAngle = tmp;
		}

		animation = new Rotation(fromAngle, toAngle).aroundAxis(1, 0, 0).offset(0, -f, f).forTicks(Door.openingTime);

		ar.setStartTime(tileEntity.startTime);
		ar.animate(s, animation);

		drawShape(s, rp);
	}

	@Override
	public void renderDestroyProgress()
	{
		rp.icon.set(damagedIcons[destroyBlockProgress.getPartialBlockDamage()]);
		rp.applyTexture.set(true);
		s = new Shape(new Face[] { s.getFaces()[4].setStandardUV(), s.getFaces()[5].setStandardUV() });
		drawShape(s, rp);
	}

	@Override
	protected boolean isCurrentBlockDestroyProgress(DestroyBlockProgress dbp)
	{
		return dbp.getPartialBlockX() == x && dbp.getPartialBlockY() == y && dbp.getPartialBlockZ() == z;
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
