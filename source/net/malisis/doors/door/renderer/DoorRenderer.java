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

import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.animation.AnimationRenderer;
import net.malisis.core.renderer.animation.transformation.Rotation;
import net.malisis.core.renderer.animation.transformation.Transformation;
import net.malisis.core.renderer.animation.transformation.Translation;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.preset.ShapePreset;
import net.malisis.doors.door.DoorMouvement;
import net.malisis.doors.door.DoorState;
import net.malisis.doors.door.block.Door;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.minecraft.client.renderer.DestroyBlockProgress;

public class DoorRenderer extends BaseRenderer
{
	public static int renderId;
	protected DoorTileEntity tileEntity;
	protected int direction;
	protected boolean opened;
	protected boolean reversed;
	protected boolean topBlock;
	protected float width = Door.DOOR_WIDTH;

	protected Shape baseShape;
	protected Shape s;
	protected RenderParameters rp;
	protected AnimationRenderer ar = new AnimationRenderer(this);

	public DoorRenderer()
	{
		initShape();
		initRenderParameters();

		getBlockDamage = true;
	}

	protected void initShape()
	{
		baseShape = ShapePreset.Cube();
		baseShape.setSize(1, 1, width);
		baseShape.scale(1, 1, 0.999F);
	}

	protected void initRenderParameters()
	{
		rp = new RenderParameters();
		rp.renderAllFaces.set(true);
		rp.calculateAOColor.set(false);
		rp.useBlockBounds.set(false);
		rp.useBlockBrightness.set(false);
		rp.interpolateUV.set(false);
	}

	@Override
	public void render()
	{
		if (renderType == TYPE_ISBRH_WORLD)
			return;

		blockMetadata = Door.getFullMetadata(world, x, y, z);
		direction = blockMetadata & 3;
		opened = (blockMetadata & Door.FLAG_OPENED) != 0;
		reversed = (blockMetadata & Door.FLAG_REVERSED) != 0;
		topBlock = (blockMetadata & Door.FLAG_TOPBLOCK) != 0;

		//set rp
		rp.brightness.set(world
				.getLightBrightnessForSkyBlocks(super.tileEntity.xCoord, super.tileEntity.yCoord, super.tileEntity.zCoord, 0));
		rp.icon.set(null);

		setTileEntity();
		setup();
		renderTileEntity();
	}

	protected void setTileEntity()
	{
		this.tileEntity = (DoorTileEntity) super.tileEntity;
	}

	protected void setup()
	{
		//set shape
		s = new Shape(baseShape);

		if (direction == Door.DIR_SOUTH)
			s.rotate(180, 0, 1, 0);
		if (direction == Door.DIR_EAST)
			s.rotate(-90, 0, 1, 0);
		if (direction == Door.DIR_WEST)
			s.rotate(90, 0, 1, 0);

	}

	protected void renderTileEntity()
	{
		Transformation animation = null;
		if (tileEntity.getMouvement() == DoorMouvement.SLIDING)
		{
			float fromX = 0, toX = 1 - width;
			if (reversed)
			{
				fromX = 0;
				toX = -1 + width;
			}
			if (tileEntity.getState() == DoorState.CLOSING || tileEntity.getState() == DoorState.CLOSED)
			{
				float tmp = fromX;
				fromX = toX;
				toX = tmp;
			}

			animation = new Translation(fromX, 0, 0, toX, 0, 0).forTicks(tileEntity.getOpeningTime());
		}
		else if (tileEntity.getMouvement() == DoorMouvement.ROTATING)
		{
			float fromAngle = 0, toAngle = 90;
			float hinge = 0.5F - width / 2;
			float hingeZ = -0.5F + width / 2;

			if (reversed)
			{
				hinge = -hinge;
				toAngle = -90;
			}

			if (tileEntity.getState() == DoorState.CLOSING || tileEntity.getState() == DoorState.CLOSED)
			{
				float tmp = toAngle;
				toAngle = fromAngle;
				fromAngle = tmp;
			}

			animation = new Rotation(fromAngle, toAngle).aroundAxis(0, 1, 0).offset(hinge, 0, hingeZ).forTicks(tileEntity.getOpeningTime());
		}

		ar.setStartTime(tileEntity.getStartTime());
		ar.animate(s, animation);

		drawShape(new Shape(s), rp);

		s.translate(0, 1F, 0);
		blockMetadata |= Door.FLAG_TOPBLOCK;
		drawShape(new Shape(s), rp);
	}

	@Override
	public void renderDestroyProgress()
	{
		//rp.icon.set(damagedIcons[destroyBlockProgress.getPartialBlockDamage()]);
		rp.icon.set(damagedIcons[8]);
		rp.applyTexture.set(true);
		s.translate(0, -.5F, 0.005F);
		s.scale(1.011F);
		s.applyMatrix();
		Shape shape = new Shape(new Face[] { s.getFaces()[0], s.getFaces()[1] });
		drawShape(shape, rp);
	}

	@Override
	protected boolean isCurrentBlockDestroyProgress(DestroyBlockProgress dbp)
	{
		return dbp.getPartialBlockX() == x && (dbp.getPartialBlockY() == y || dbp.getPartialBlockY() == y + 1)
				&& dbp.getPartialBlockZ() == z;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return false;
	}

}
