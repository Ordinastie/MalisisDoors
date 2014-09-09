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

package net.malisis.doors.door.renderer;

import net.malisis.core.MalisisCore;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.preset.ShapePreset;
import net.malisis.doors.door.Door;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.RenderBlocks;

/**
 * @author Ordinastie
 * 
 */
public class FenceGateRenderer extends DoorRenderer
{
	@Override
	protected void initShapes()
	{
		float w = 0.125F; // fence depth and hinge width
		float w2 = 0.1875F; //

		Shape hinge = ShapePreset.Cube().setSize(w, 0.6875F, w).translate(0, 0.3125F, 0.5F - w / 2);
		Shape gateH = ShapePreset.Cube().setSize(w, w2 * 3, w).translate(0.5F - w, 0.375F, 0.5F - w / 2);
		Shape gateBottom = ShapePreset.Cube().setSize(2 * w, w2, w).translate(w, 0.375F, 0.5F - w / 2);
		Shape gateTop = new Shape(gateBottom).translate(0, 2 * w2, 0);

		shape = Shape.fromShapes(hinge, gateH, gateBottom, gateTop);
		shape.applyMatrix();
		shape.interpolateUV();
		shape.storeState();
	}

	@Override
	protected void setup(boolean leftPart)
	{
		shape.resetState();

		if (direction == Door.DIR_NORTH || direction == Door.DIR_SOUTH)
			shape.rotate(90, 0, 1, 0);

		if (leftPart)
			shape.scale(-1, 1, -1);
	}

	@Override
	public void renderDestroyProgress()
	{
		rp.icon.set(damagedIcons[destroyBlockProgress.getPartialBlockDamage()]);
		setup(false);
		if (tileEntity.getMovement() != null)
			ar.animate(shape, tileEntity.getMovement().getBottomTransformation(tileEntity));
		drawShape(shape, rp);

		setup(true);
		if (tileEntity.getMovement() != null)
			ar.animate(shape, tileEntity.getMovement().getTopTransformation(tileEntity));
		drawShape(shape, rp);
	}

	@Override
	protected boolean isCurrentBlockDestroyProgress(DestroyBlockProgress dbp)
	{
		return dbp.getPartialBlockX() == x && (dbp.getPartialBlockY() == y || dbp.getPartialBlockY() == y + 1)
				&& dbp.getPartialBlockZ() == z;
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
