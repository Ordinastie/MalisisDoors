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

import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.animation.AnimationRenderer;
import net.malisis.core.renderer.animation.transformation.ChainedTransformation;
import net.malisis.core.renderer.animation.transformation.ParallelTransformation;
import net.malisis.core.renderer.animation.transformation.Rotation;
import net.malisis.core.renderer.animation.transformation.Transformation;
import net.malisis.core.renderer.animation.transformation.Translation;
import net.malisis.core.renderer.element.shape.Cube;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.door.DoorState;
import net.malisis.doors.door.block.Door;
import net.malisis.doors.entity.GarageDoorTileEntity;
import net.minecraft.client.renderer.DestroyBlockProgress;

/**
 * @author Ordinastie
 *
 */
public class GarageDoorRenderer extends MalisisRenderer
{
	private GarageDoorTileEntity tileEntity;
	protected int direction;
	protected boolean opened;
	protected boolean reversed;
	protected boolean topBlock;
	protected Set<GarageDoorTileEntity> childDoors = new HashSet<>();

	protected AnimationRenderer ar = new AnimationRenderer();

	@Override
	protected void initialize()
	{
		shape = new Cube().setSize(Door.DOOR_WIDTH, 1, 1);
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

		if (renderType == RenderType.ITEM_INVENTORY)
		{
			enableBlending();

			shape.resetState();
			shape.translate(0.5F - Door.DOOR_WIDTH / 2, 0, 0);
			rp.icon.set(null);
			blockMetadata = Door.FLAG_TOPBLOCK;
			drawShape(shape, rp);
			return;
		}

		tileEntity = TileEntityUtils.getTileEntity(GarageDoorTileEntity.class, world, x, y, z);
		if (tileEntity == null || !tileEntity.isTopDoor())
		{
			getBlockDamage = false;
			return;
		}

		getBlockDamage = true;

		direction = tileEntity.getDirection();
		opened = tileEntity.isOpened();
		reversed = tileEntity.isReversed();

		rp.icon.set(null);

		enableBlending();
		renderTileEntity();
	}

	protected void renderTileEntity()
	{
		int t = GarageDoorTileEntity.maxOpenTime;
		//set the start timer
		ar.setStartTime(tileEntity.getTimer().getStart());

		//create door list from childs + top
		childDoors.clear();
		tileEntity.addChildDoors(childDoors);
		for (GarageDoorTileEntity te : childDoors)
		{
			shape.resetState();
			shape.rotate(-90 * tileEntity.getDirection(), 0, 1, 0);
			shape.translate(0.5F - Door.DOOR_WIDTH / 2, 0, 0);

			y = te.yCoord;
			int delta = tileEntity.yCoord - te.yCoord;
			int delta2 = childDoors.size() - (delta + 1);

			if (delta == 0)
				blockMetadata |= Door.FLAG_TOPBLOCK;
			else
				blockMetadata &= ~Door.FLAG_TOPBLOCK;

			Transformation verticalAnim = new Translation(0, -delta, 0, 0, 0, 0).forTicks(t * delta, 0);
			//@formatter:off
			Transformation topRotate = new ParallelTransformation(
					new Translation(0, 1, 0).forTicks(t, 0),
					new Rotation(0, -90).aroundAxis(0, 0, 1).offset(-0.5F, -0.5F, 0).forTicks(t, 0)
			);
			//@formatter:on
			Transformation horizontalAnim = new Translation(0, 0, 0, 0, delta2, 0).forTicks(t * delta2, 0);

			Transformation chained = new ChainedTransformation(verticalAnim, topRotate, horizontalAnim);
			if (tileEntity.getState() == DoorState.CLOSING || tileEntity.getState() == DoorState.CLOSED)
				chained.reversed(true);

			rp.brightness.set(block.getMixedBrightnessForBlock(world, x, y, z));

			ar.animate(shape, chained);
			drawShape(shape, rp);
		}
		//restore correct y coord
		y = tileEntity.yCoord;
	}

	@Override
	public void renderDestroyProgress()
	{
		rp.icon.set(damagedIcons[destroyBlockProgress.getPartialBlockDamage()]);
		int y = this.y - destroyBlockProgress.getPartialBlockY();
		shape.resetState();
		shape.rotate(-90 * tileEntity.getDirection(), 0, 1, 0);
		shape.translate(0.505F - Door.DOOR_WIDTH / 2, -y, 0);
		shape.scale(1.011F);
		drawShape(shape, rp);
	}

	@Override
	protected boolean isCurrentBlockDestroyProgress(DestroyBlockProgress dbp)
	{
		if (dbp.getPartialBlockX() == x && dbp.getPartialBlockY() == y && dbp.getPartialBlockZ() == z)
			return true;

		for (GarageDoorTileEntity te : childDoors)
		{
			if (dbp.getPartialBlockX() == te.xCoord && dbp.getPartialBlockY() == te.yCoord && dbp.getPartialBlockZ() == te.zCoord)
				return true;
		}
		return false;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return true;
	}
}
