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
import net.malisis.core.renderer.animation.AnimationRenderer;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.Vertex;
import net.malisis.core.renderer.element.face.NorthFace;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.door.tileentity.ForcefieldTileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

/**
 * @author Ordinastie
 *
 */
public class ForcefieldRenderer extends MalisisRenderer
{
	protected static ResourceLocation[] rl;
	protected ForcefieldTileEntity tileEntity;
	protected AxisAlignedBB aabb;
	protected EnumFacing direction;
	protected double scaleX, scaleY;

	protected Shape shape;
	protected RenderParameters rp;
	protected AnimationRenderer ar = new AnimationRenderer();

	public ForcefieldRenderer()
	{
		registerFor(ForcefieldTileEntity.class);
	}

	@Override
	protected void initialize()
	{
		rl = new ResourceLocation[50];
		for (int i = 0; i < 50; i++)
			rl[i] = new ResourceLocation(String.format(MalisisDoors.modid + ":textures/blocks/forcefield%02d.png", i));

		shape = new Shape(new NorthFace());

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

	private void setDirection()
	{
		if (aabb.maxY - aabb.minY == 0)
			direction = EnumFacing.UP;
		else if (aabb.maxX - aabb.minX == 0)
			direction = EnumFacing.EAST;
		else
			direction = EnumFacing.NORTH;
	}

	private void setScale()
	{
		scaleX = 1;
		scaleY = 1;
		if (direction == EnumFacing.UP)
		{
			scaleX = aabb.maxX - aabb.minX;
			scaleY = aabb.maxZ - aabb.minZ;
		}
		else
		{
			scaleY = aabb.maxY - aabb.minY;
			if (direction == EnumFacing.EAST)
				scaleX = aabb.maxZ - aabb.minZ;
			else if (direction == EnumFacing.NORTH)
				scaleX = aabb.maxX - aabb.minX;
		}
	}

	@Override
	public void render()
	{
		tileEntity = (ForcefieldTileEntity) super.tileEntity;
		if (tileEntity == null || tileEntity.isOpened())
			return;

		aabb = tileEntity.getRenderBoundingBox().offset(-pos.getX(), -pos.getY(), -pos.getZ());
		setDirection();
		setScale();

		enableBlending();

		shape = new Shape(new NorthFace());
		if (direction == EnumFacing.UP)
			shape.rotate(90, 1, 0, 0);
		else if (direction == EnumFacing.EAST)
			shape.rotate(90, 0, 1, 0);

		shape.translate(0, 0, 0.5F);
		shape.scale((float) scaleX, (float) scaleY, 1, direction == EnumFacing.EAST ? 0.5F : -0.5F, -0.5F, 0.5F);

		setTextureMatrix();
		GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
		GL11.glDisable(GL11.GL_CULL_FACE);

		drawShape(shape, rp);
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

		if (direction == EnumFacing.UP && aabb.maxZ - aabb.minZ > aabb.maxX - aabb.minX)
			GL11.glRotatef(90, 0, 0, 1);

		//GL11.glTranslatef(fx, fy, 0);
		GL11.glScaled(scaleX / 3, scaleY / 3, 1);

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
	}
}
