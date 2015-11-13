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

import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.animation.AnimationRenderer;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.Vertex;
import net.malisis.core.renderer.element.face.NorthFace;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.core.util.MultiBlock;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.door.tileentity.ForcefieldTileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

/**
 * @author Ordinastie
 *
 */
public class ForcefieldRenderer extends MalisisRenderer
{
	protected static ResourceLocation[] rl;
	protected MalisisModel model;
	protected ForcefieldTileEntity tileEntity;
	protected AnimationRenderer ar = new AnimationRenderer();
	protected ForgeDirection direction;

	@Override
	protected void initialize()
	{
		rl = new ResourceLocation[50];
		for (int i = 0; i < 50; i++)
			rl[i] = new ResourceLocation(String.format(MalisisDoors.modid + ":textures/blocks/forcefield/forcefield%02d.png", i));

		Shape shape = new Shape(new NorthFace());
		shape.scale(1, 1, 0);

		model = new MalisisModel();
		model.addShape(shape);
		model.storeState();

		rp = new RenderParameters();
		rp.interpolateUV.set(false);
		rp.useBlockBounds.set(false);
		rp.calculateAOColor.set(false);
		rp.setBrightness(Vertex.BRIGHTNESS_MAX);
		rp.useEnvironmentBrightness.set(false);
		rp.calculateBrightness.set(false);
		rp.applyTexture.set(false);
		rp.alpha.set(50);
		//rp.colorMultiplier.set(0xAAAAFF);

	}

	@Override
	public void render()
	{
		tileEntity = MultiBlock.getOriginProvider(ForcefieldTileEntity.class, world, x, y, z);
		if (tileEntity == null || tileEntity.isOpened() || !MultiBlock.isOrigin(world, x, y, z))
			return;

		enableBlending();

		tileEntity = (ForcefieldTileEntity) super.tileEntity;
		direction = ForgeDirection.getOrientation(tileEntity.getDirection());

		model.resetState();

		//ar.setStartTime(tileEntity.getStartNanoTime());
		if (tileEntity.getMovement() == null)
			return;

		AxisAlignedBB aabb = tileEntity.getMovement().getBoundingBox(tileEntity, false, BoundingBoxType.COLLISION);
		if (aabb == null)
			return;

		aabb.offset(-x, -y, -z);
		rp.renderBounds.set(aabb);
		direction = ForgeDirection.NORTH;
		if ((int) aabb.minY == (int) aabb.maxY)
		{
			direction = ForgeDirection.UP;
			model.rotate(90, 1, 0, 0, 0, 0, 0);
		}
		else if ((int) aabb.minX == (int) aabb.maxX)
		{
			direction = ForgeDirection.EAST;
			model.rotate(90, 0, 1, 0, 0, 0, 0);
		}

		setTextureMatrix();
		GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
		GL11.glDisable(GL11.GL_CULL_FACE);

		model.render(this, rp);
		next();

		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glMatrixMode(GL11.GL_TEXTURE);
		GL11.glLoadIdentity();
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glDisable(GL11.GL_BLEND);
	}

	private void setTextureMatrix()
	{
		long elapsed = ar.getElapsedTime() / 50;
		int n = (int) (elapsed % 50);

		bindTexture(rl[n]);
		GL11.glMatrixMode(GL11.GL_TEXTURE);
		GL11.glLoadIdentity();

		AxisAlignedBB aabb = tileEntity.getMultiBlock().getBounds();
		double scaleX = 1, scaleY = 1;
		if (direction == ForgeDirection.UP)
		{
			scaleX = aabb.maxX - aabb.minX;
			scaleY = aabb.maxZ - aabb.minZ;

			if (aabb.maxZ - aabb.minZ > aabb.maxX - aabb.minX)
				GL11.glRotatef(90, 0, 0, 1);
		}
		else
		{
			scaleY = aabb.maxY - aabb.minY;
			if (direction == ForgeDirection.EAST)
				scaleX = aabb.maxZ - aabb.minZ;
			else if (direction == ForgeDirection.NORTH)
				scaleX = aabb.maxX - aabb.minX;
		}

		//GL11.glTranslatef(fx, fy, 0);
		GL11.glScaled(scaleX / 3, scaleY / 3, 1);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}
}
