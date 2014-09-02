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
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.preset.ShapePreset;
import net.malisis.doors.door.Door;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.minecraft.client.renderer.DestroyBlockProgress;

public class DoorRenderer extends BaseRenderer
{
	protected DoorTileEntity tileEntity;
	protected int direction;
	protected boolean opened;
	protected boolean reversed;
	protected boolean topBlock;

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
		baseShape.setSize(1, 1, Door.DOOR_WIDTH);
		baseShape.scale(1, 1, 0.995F);
		baseShape.applyMatrix();
	}

	protected void initRenderParameters()
	{
		rp = new RenderParameters();
		rp.renderAllFaces.set(true);
		rp.calculateAOColor.set(false);
		rp.useBlockBounds.set(false);
		rp.useBlockBrightness.set(false);
		rp.calculateBrightness.set(false);
		rp.interpolateUV.set(false);
	}

	@Override
	public void render()
	{
		if (renderType == TYPE_ISBRH_WORLD)
			return;

		setTileEntity();

		direction = tileEntity.getDirection();
		opened = tileEntity.isOpened();
		reversed = tileEntity.isReversed();
		topBlock = tileEntity.isTopBlock(x, y, z);

		rp.brightness.set(world.getLightBrightnessForSkyBlocks(tileEntity.xCoord, tileEntity.yCoord, tileEntity.zCoord, 0));
		rp.icon.set(null);
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
		enableBlending();
		ar.setStartTime(tileEntity.getStartTime());

		Shape tmp = new Shape(s).translate(0, 1F, 0);

		if (tileEntity.getMovement() != null)
			ar.animate(s, tileEntity.getMovement().getBottomTransformation(tileEntity));
		drawShape(new Shape(s), rp);

		blockMetadata |= Door.FLAG_TOPBLOCK;
		if (tileEntity.getMovement() != null)
			ar.animate(tmp, tileEntity.getMovement().getTopTransformation(tileEntity));
		drawShape(tmp, rp);
	}

	@Override
	public void renderDestroyProgress()
	{
		rp.icon.set(damagedIcons[destroyBlockProgress.getPartialBlockDamage()]);

		s.translate(0, 0.5F, 0.005F);
		s.scale(1.011F);
		s.applyMatrix();
		drawShape(new Shape(new Face[] { s.getFaces()[0], s.getFaces()[1] }), rp);
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
