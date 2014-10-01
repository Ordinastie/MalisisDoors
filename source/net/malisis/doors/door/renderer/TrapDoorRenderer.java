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
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.preset.FacePreset;
import net.malisis.core.renderer.preset.ShapePreset;
import net.malisis.doors.block.TrapDoor;
import net.malisis.doors.door.Door;
import net.malisis.doors.door.movement.IDoorMovement;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @author Ordinastie
 * 
 */
public class TrapDoorRenderer extends DoorRenderer
{
	RenderParameters rpTop;

	@Override
	protected void initShapes()
	{
		shape = ShapePreset.Cube();
		shape.setSize(1, Door.DOOR_WIDTH, 1);
		shape.interpolateUV();
		shape.storeState();

		shape.getFace(ForgeDirection.UP).getParameters().calculateAOColor.set(true);
	}

	@Override
	protected void setup(boolean topBlock)
	{
		initParameters();
		shape.resetState();
		float angle = 0;
		if (direction == TrapDoor.DIR_NORTH)
			angle = 180;
		else if (direction == TrapDoor.DIR_EAST)
			angle = 90;
		else if (direction == TrapDoor.DIR_WEST)
			angle = 270;
		shape.rotate(angle, 0, 1, 0);

		if (topBlock)
			shape.translate(0, 1 - Door.DOOR_WIDTH, 0);
	}

	@Override
	public void renderTileEntity()
	{
		setup(topBlock);
		ar.setStartTime(tileEntity.getStartTime());

		IDoorMovement mvt = tileEntity.getMovement();
		if (mvt != null)
			ar.animate(shape, topBlock ? mvt.getTopTransformation(tileEntity) : mvt.getBottomTransformation(tileEntity));

		Face f = shape.getFace(ForgeDirection.UP);
		shape.applyMatrix();
		f.getParameters().aoMatrix.set(FacePreset.calculateAoMatrix(f, ForgeDirection.UP));

		drawShape(shape, rp);
	}

	@Override
	public void renderDestroyProgress()
	{
		setup(topBlock);
		IDoorMovement mvt = tileEntity.getMovement();
		if (mvt != null)
			ar.animate(shape, topBlock ? mvt.getTopTransformation(tileEntity) : mvt.getBottomTransformation(tileEntity));
		rp.icon.set(damagedIcons[destroyBlockProgress.getPartialBlockDamage()]);
		drawShape(shape, rp);
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
