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

import java.util.Random;

import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.shape.Cube;
import net.malisis.doors.MalisisDoorsSettings;
import net.malisis.doors.ProxyAccess;
import net.malisis.doors.block.VanishingBlock;
import net.malisis.doors.entity.VanishingTileEntity;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

public class VanishingBlockRenderer extends MalisisRenderer
{
	public Random rand = new Random();

	@Override
	public void render()
	{
		if (renderType == RenderType.TESR_WORLD)
			renderVanishingTileEntity();
		else if (renderType == RenderType.ISBRH_INVENTORY)
		{
			RenderParameters rp = new RenderParameters();
			rp.useBlockBounds.set(false);
			drawShape(new Cube(), rp);
		}
		else if (renderType == RenderType.ISBRH_WORLD)
		{
			VanishingTileEntity te = (VanishingTileEntity) world.getTileEntity(x, y, z);

			if ((te.getBlockMetadata() & (VanishingBlock.flagPowered | VanishingBlock.flagInTransition)) != 0)
				return;

			if (te.copiedBlock != null)
			{
				tessellatorUnshift();
				renderBlocks.blockAccess = ProxyAccess.get(world);
				renderBlocks.renderAllFaces = true;
				try
				{
					if (te.copiedBlock.canRenderInPass(((VanishingBlock) block).renderPass))
						vertexDrawn = renderBlocks.renderBlockByRenderType(te.copiedBlock, x, y, z);
				}
				catch (Exception e)
				{

					tessellatorShift();
					drawShape(new Cube());
				}

				renderBlocks.renderAllFaces = false;
				renderBlocks.blockAccess = world;
			}
			else if (((VanishingBlock) block).renderPass == 0)
				drawShape(new Cube());
		}
	}

	private void renderVanishingTileEntity()
	{
		VanishingTileEntity te = (VanishingTileEntity) this.tileEntity;

		if (!te.inTransition && !te.vibrating)
		{
			if (!te.powered && te.copiedTileEntity != null)
			{
				clean();
				TileEntityRendererDispatcher.instance.renderTileEntity(te.copiedTileEntity, partialTick);
			}
			return;
		}

		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0);

		float fx = 0.0F;
		float fy = 0.0F;
		float fz = 0.0F;
		float scale = (float) (te.getDuration() - te.transitionTimer) / (float) te.getDuration();
		boolean rendered = te.copiedBlock != null;

		RenderParameters rp = new RenderParameters();
		rp.useBlockBounds.set(false);
		rp.interpolateUV.set(false);

		Shape shape = new Cube();
		// randomize position for vibrations
		if (!te.inTransition && !te.powered)
		{
			rp.alpha.set(200);
			fx = rand.nextFloat() * 0.05F;
			fy = rand.nextFloat() * 0.05F;
			fz = rand.nextFloat() * 0.05F;
			if (rand.nextBoolean())
				GL11.glTranslated(fx, fy, fz);
			else
				GL11.glRotatef(rand.nextInt(5), 1, 1, 1);
		}
		else
		{
			int alpha = te.copiedBlock != null ? 255 - (int) (scale * 255) : (int) (scale * 255);
			rp.alpha.set(alpha);
			shape.scale(scale - 0.001F);
		}

		if (te.copiedBlock != null)
		{
			RenderBlocks renderBlocks = new RenderBlocks(ProxyAccess.get(world));
			renderBlocks.renderAllFaces = true;
			try
			{
				boolean smbr = MalisisDoorsSettings.simpleMixedBlockRendering.get();
				MalisisDoorsSettings.simpleMixedBlockRendering.set(true);

				GL11.glPushMatrix();
				GL11.glTranslated(0.5F, 0.5F, 0.5F);
				GL11.glScalef(scale, scale, scale);
				GL11.glTranslated(-x - 0.5F, -y - 0.5F, -z - 0.5F);

				GL11.glBlendFunc(GL11.GL_CONSTANT_ALPHA, GL11.GL_ONE_MINUS_CONSTANT_ALPHA);
				GL14.glBlendColor(0, 0, 0, 1 - scale);
				renderBlocks.overrideBlockTexture = block.getIcon(blockMetadata, 0);
				rendered = renderBlocks.renderBlockByRenderType(te.copiedBlock, x, y, z);
				renderBlocks.overrideBlockTexture = null;
				next();

				if (te.copiedBlock.canRenderInPass(0))
				{
					GL14.glBlendColor(0, 0, 0, scale);
					rendered |= renderBlocks.renderBlockByRenderType(te.copiedBlock, x, y, z);
					next();
				}
				if (te.copiedBlock.canRenderInPass(1))
				{
					GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
					rendered |= renderBlocks.renderBlockByRenderType(te.copiedBlock, x, y, z);
					next();
				}

				if (!rendered)
					drawShape(shape, rp);

				GL11.glPopMatrix();

				if (te.copiedTileEntity != null)
				{
					clean();
					TileEntityRendererDispatcher.instance.renderTileEntity(te.copiedTileEntity, partialTick);
				}

				MalisisDoorsSettings.simpleMixedBlockRendering.set(smbr);

			}
			catch (Exception e)
			{
				drawShape(shape, rp);
			}

		}
		else
			drawShape(shape, rp);
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return true;
	}
}
