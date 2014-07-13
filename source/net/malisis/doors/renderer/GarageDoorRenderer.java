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

import net.malisis.core.renderer.animation.transformation.ChainedTransformation;
import net.malisis.core.renderer.animation.transformation.ParallelTransformation;
import net.malisis.core.renderer.animation.transformation.Rotation;
import net.malisis.core.renderer.animation.transformation.Transformation;
import net.malisis.core.renderer.animation.transformation.Translation;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.preset.ShapePreset;
import net.malisis.doors.block.doors.DoorHandler;
import net.malisis.doors.entity.GarageDoorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.RenderBlocks;

/**
 * @author Ordinastie
 * 
 */
public class GarageDoorRenderer extends DoorRenderer
{
	public static int renderId;
	private GarageDoorTileEntity tileEntity;

	@Override
	protected void initShape()
	{
		baseShape = ShapePreset.Cube();
		baseShape.setSize(width, 1, 1);
	}

	@Override
	public void render()
	{
		if (renderType == TYPE_ITEM_INVENTORY)
		{
			s = new Shape(baseShape);
			rp.icon.set(null);
			//s.rotate(90, 0, 1, 0);
			s.translate(0.5F - width / 2, 0, 0);
			drawShape(s, rp);
			return;
		}

		if (!((GarageDoorTileEntity) world.getTileEntity(x, y, z)).isTopDoor())
		{
			getBlockDamage = false;
			return;
		}

		getBlockDamage = true;
		super.render();
	}

	@Override
	protected void setTileEntity()
	{
		tileEntity = (GarageDoorTileEntity) world.getTileEntity(x, y, z);
	}

	@Override
	protected void setup()
	{
		initShape();
		s = new Shape(baseShape);
		s.rotate(-90 * tileEntity.getDirection(), 0, 1, 0);
		s.translate(0.5F - width / 2, 0, 0);
	}

	@Override
	protected void renderTileEntity()
	{

		if (!tileEntity.isTopDoor())
			return;

		enableBlending();

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

			Shape tempShape = new Shape(s);
			ar.animate(tempShape, chained);
			drawShape(tempShape, rp);
		}
	}

	@Override
	public void renderDestroyProgress()
	{
		rp.icon.set(damagedIcons[destroyBlockProgress.getPartialBlockDamage()]);
		int y = this.y - destroyBlockProgress.getPartialBlockY();
		s.translate(0.005F, -y, 0);
		s.scale(1.011F);
		s.applyMatrix();
		Shape tempShape = new Shape(new Face[] { s.getFaces()[2], s.getFaces()[3] });
		drawShape(tempShape, rp);
	}

	@Override
	protected boolean isCurrentBlockDestroyProgress(DestroyBlockProgress dbp)
	{
		if (dbp.getPartialBlockX() == x && dbp.getPartialBlockY() == y && dbp.getPartialBlockZ() == z)
			return true;

		for (GarageDoorTileEntity te : tileEntity.getChildDoors())
		{
			if (dbp.getPartialBlockX() == te.xCoord && dbp.getPartialBlockY() == te.yCoord && dbp.getPartialBlockZ() == te.zCoord)
				return true;
		}
		return false;
	}

	@Override
	public void renderInventoryBlock(Block block, int metadata, int modelId, RenderBlocks renderer)
	{
		// TODO Auto-generated method stub
		super.renderInventoryBlock(block, metadata, modelId, renderer);
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return true;
	}
}
