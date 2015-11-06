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

import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.shape.Cube;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.doors.door.block.Door;
import net.malisis.doors.door.tileentity.FenceGateTileEntity;

/**
 * @author Ordinastie
 *
 */
public class FenceGateRenderer extends DoorRenderer
{
	protected FenceGateTileEntity tileEntity;

	@Override
	protected void initialize()
	{
		float w = 0.125F; // fence depth and hinge width
		float w2 = 0.1875F; //

		Shape hinge = new Cube().setSize(w, 0.6875F, w);
		hinge.translate(0, 0.3125F, 0.5F - w / 2);
		Shape gateH = new Cube().setSize(w, w2 * 3, w);
		gateH.translate(0.5F - w, 0.375F, 0.5F - w / 2);
		Shape gateBottom = new Cube().setSize(2 * w, w2, w);
		gateBottom.translate(w, 0.375F, 0.5F - w / 2);
		Shape gateTop = new Shape(gateBottom);
		gateTop.translate(0, 2 * w2, 0);

		Shape right = Shape.fromShapes(hinge, gateH, gateBottom, gateTop);
		right.applyMatrix();
		right.interpolateUV();

		Shape left = new Shape(right);
		left.rotate(180, 0, 1, 0);

		model = new MalisisModel();
		model.addShape("right", right);
		model.addShape("left", left);

		model.storeState();

		initParams();
	}

	@Override
	protected void setTileEntity()
	{
		super.setTileEntity();
		this.tileEntity = (FenceGateTileEntity) super.tileEntity;
	}

	@Override
	protected void setup()
	{
		model.resetState();
		if (direction == Door.DIR_NORTH || direction == Door.DIR_SOUTH)
			model.rotate(90, 0, 1, 0, 0, 0, 0);

		if (tileEntity.isWall())
			model.translate(0, -.19F, 0);

		rp.colorMultiplier.set(tileEntity.getCamoColor());
		rp.brightness.set(block.getMixedBrightnessForBlock(world, x, y, z));
	}

	@Override
	public void render()
	{
		if (renderType == RenderType.ISBRH_INVENTORY)
		{
			model.resetState();
			model.rotate(90, 0, 1, 0, 0, 0, 0);
			model.render(this, rp);
			return;
		}
		super.render();
	}

	@Override
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

		model.render(this, rp);
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return true;
	}
}
