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

import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.AnimationRenderer;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.shape.Cube;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.doors.door.block.Door;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.minecraft.client.renderer.DestroyBlockProgress;

public class DoorRenderer extends MalisisRenderer
{
	protected DoorTileEntity tileEntity;
	protected MalisisModel model;
	protected int direction;
	protected boolean opened;
	protected boolean reversed;
	protected boolean topBlock;

	protected Shape shape;
	protected RenderParameters rp;
	protected AnimationRenderer ar = new AnimationRenderer();

	public DoorRenderer()
	{
		getBlockDamage = true;
	}

	@Override
	protected void initialize()
	{
		Shape bottom = new Cube();
		bottom.setSize(1, 1, Door.DOOR_WIDTH);
		bottom.scale(1, 1, 0.995F);
		Shape top = new Shape(bottom);
		top.translate(0, 1, 0);

		shape = bottom;
		model = new MalisisModel();
		model.addShape("bottom", bottom);
		model.addShape("top", top);

		model.storeState();

		initParams();
	}

	protected void initParams()
	{
		rp = new RenderParameters();
		rp.renderAllFaces.set(true);
		rp.calculateAOColor.set(false);
		rp.useBlockBounds.set(false);
		rp.useEnvironmentBrightness.set(false);
		rp.calculateBrightness.set(false);
		rp.interpolateUV.set(false);
	}

	@Override
	public void render()
	{
		if (renderType == RenderType.ISBRH_WORLD)
			return;

		setTileEntity();

		direction = tileEntity.getDirection();
		opened = tileEntity.isOpened();
		reversed = tileEntity.isReversed();
		topBlock = tileEntity.isTopBlock(x, y, z);

		rp.icon.set(null);

		renderTileEntity();
	}

	protected void setTileEntity()
	{
		this.tileEntity = (DoorTileEntity) super.tileEntity;
	}

	protected void setup()
	{
		model.resetState();

		if (direction == Door.DIR_SOUTH)
			model.rotate(180, 0, 1, 0, 0, 0, 0);
		if (direction == Door.DIR_EAST)
			model.rotate(-90, 0, 1, 0, 0, 0, 0);
		if (direction == Door.DIR_WEST)
			model.rotate(90, 0, 1, 0, 0, 0, 0);

		if (tileEntity.isCentered())
			model.translate(0, 0, 0.5F - Door.DOOR_WIDTH / 2);
	}

	protected void renderTileEntity()
	{
		enableBlending();
		ar.setStartTime(tileEntity.getTimer().getStart());

		setup();

		if (tileEntity.getMovement() != null)
		{
			Animation[] anims = tileEntity.getMovement().getAnimations(tileEntity, model, rp);
			ar.animate(anims);
		}

		//model.render(this, rp);
		rp.brightness.set(block.getMixedBrightnessForBlock(world, x, y, z));
		drawShape(model.getShape("bottom"), rp);

		blockMetadata |= Door.FLAG_TOPBLOCK;
		y++;
		rp.brightness.set(block.getMixedBrightnessForBlock(world, x, y, z));
		drawShape(model.getShape("top"), rp);
		y--;
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
