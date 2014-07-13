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

package net.malisis.doors.renderer.block;

import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.Vertex;
import net.malisis.core.renderer.preset.FacePreset;
import net.malisis.core.renderer.preset.ShapePreset;
import net.malisis.doors.MalisisDoorsSettings;
import net.malisis.doors.entity.MixedBlockTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

public class MixedBlockRenderer extends BaseRenderer
{
	public static int renderId;
	private static int currentPass;

	@Override
	public void render()
	{
		if (renderType == TYPE_ITEM_INVENTORY)
			renderItem();
		else if (renderType == TYPE_ISBRH_WORLD)
			renderWorld();

	}

	private void renderItem()
	{
		if (!itemStack.hasTagCompound())
			return;

		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		enableBlending();

		Block b1 = Block.getBlockById(itemStack.getTagCompound().getInteger("block1"));
		Block b2 = Block.getBlockById(itemStack.getTagCompound().getInteger("block2"));

		int metadata1 = itemStack.getTagCompound().getInteger("metadata1");
		int metadata2 = itemStack.getTagCompound().getInteger("metadata2");

		if (MalisisDoorsSettings.simpleMixedBlockRendering.get())
		{
			renderSimple(b1, metadata1, b2, metadata2, ForgeDirection.WEST);
		}
		else
		{
			set(b1, metadata1);
			drawPass(0, null);
			set(b2, metadata2);
			drawPass(1, ForgeDirection.WEST);
		}

	}

	private void renderWorld()
	{
		MixedBlockTileEntity te = (MixedBlockTileEntity) world.getTileEntity(x, y, z);
		if (te == null)
			return;

		if (MalisisDoorsSettings.simpleMixedBlockRendering.get())
			renderSimple(te.block1, te.metadata1, te.block2, te.metadata2, ForgeDirection.getOrientation(blockMetadata));
		else
		{
			int metadata = blockMetadata;
			if (currentPass == 0)
				set(te.block1, te.metadata1);
			else
			{
				GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);
				set(te.block2, te.metadata2);
			}
			if (block instanceof Block)
				drawPass(currentPass, ForgeDirection.getOrientation(metadata));
		}
	}

	private void renderSimple(Block b1, int m1, Block b2, int m2, ForgeDirection dir)
	{
		boolean reversed = false;
		float width = 0.5F;
		float depth = 1;
		float offsetX = 0.5F;
		float offsetZ = 0;

		if (dir == ForgeDirection.NORTH || dir == ForgeDirection.SOUTH)
		{
			width = 1;
			depth = 0.5F;
			offsetX = 0;
			offsetZ = 0.5F;
		}

		if (dir == ForgeDirection.EAST || dir == ForgeDirection.SOUTH)
			reversed = true;

		Shape shape = ShapePreset.Cube();
		shape.setSize(width, 1, depth);

		RenderParameters rp = new RenderParameters();
		// rp.interpolateUV.set(false);

		set(reversed ? b2 : b1, reversed ? m2 : m1);
		drawShape(shape, rp);

		shape.translate(offsetX, 0, offsetZ);
		set(reversed ? b1 : b2, reversed ? m1 : m2);
		drawShape(shape, rp);
	}

	private void drawPass(int pass, ForgeDirection dir)
	{
		RenderParameters rp = new RenderParameters();
		rp.usePerVertexAlpha.set(true);
		rp.useBlockBounds.set(false);
		Shape cube = ShapePreset.Cube();
		int color = renderType == TYPE_ISBRH_WORLD ? block.colorMultiplier(world, x, y, z) : block.getBlockColor();

		if (block instanceof BlockGrass)
		{
			RenderParameters rpGrass = new RenderParameters();
			rpGrass.colorMultiplier.set(color);
			rpGrass.usePerVertexAlpha.set(true);
			rpGrass.useBlockBounds.set(false);
			cube.setParameters(FacePreset.Top(), rpGrass, true);
		}
		else
			rp.colorMultiplier.set(color);

		if (pass == 1)
		{
			for (Face f : cube.getFaces())
			{
				for (Vertex v : f.getVertexes())
				{
					if (v.name().toLowerCase().contains(dir.name().toLowerCase()))
						v.setAlpha(0);
				}
			}
		}

		drawShape(cube, rp);
	}

	public static void setRenderPass(int pass)
	{
		currentPass = pass;
	}

}
