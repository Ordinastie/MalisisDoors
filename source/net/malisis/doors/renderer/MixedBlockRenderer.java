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

import java.util.List;

import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.MergedVertex;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.shape.Cube;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.MalisisDoorsSettings;
import net.malisis.doors.entity.MixedBlockTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGrass;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.opengl.GL11;

public class MixedBlockRenderer extends MalisisRenderer
{
	private int mixedBlockMetadata;
	private MixedBlockTileEntity tileEntity;
	private Shape simpleShape;
	private Shape[][] shapes;
	private Block block1;
	private Block block2;
	private int metadata1;
	private int metadata2;

	@Override
	protected void initialize()
	{
		simpleShape = new Cube();

		shapes = new Shape[][] { new Shape[6], new Shape[6] };
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			Shape s0 = new Cube();
			Shape s1 = new Cube();
			s0.enableMergedVertexes();
			s1.enableMergedVertexes();
			shapes[0][dir.ordinal()] = s0.removeFace(s0.getFace(Face.nameFromDirection(dir))).storeState();
			shapes[1][dir.ordinal()] = s1.shrink(dir, 0.999F).removeFace(s1.getFace(Face.nameFromDirection(dir))).storeState();
		}

		rp = new RenderParameters();
		rp.useBlockBounds.set(false);
		rp.usePerVertexAlpha.set(true);
		rp.useWorldSensitiveIcon.set(false);
	}

	private boolean setup()
	{
		if (renderType == RenderType.ITEM_INVENTORY)
		{
			if (!itemStack.hasTagCompound())
				return false;
			block1 = Block.getBlockById(itemStack.getTagCompound().getInteger("block1"));
			block2 = Block.getBlockById(itemStack.getTagCompound().getInteger("block2"));

			metadata1 = itemStack.getTagCompound().getInteger("metadata1");
			metadata2 = itemStack.getTagCompound().getInteger("metadata2");

			mixedBlockMetadata = 3;
		}
		else if (renderType == RenderType.ISBRH_WORLD)
		{
			tileEntity = TileEntityUtils.getTileEntity(MixedBlockTileEntity.class, world, x, y, z);
			if (tileEntity == null)
				return false;

			block1 = tileEntity.block1;
			block2 = tileEntity.block2;

			metadata1 = tileEntity.metadata1;
			metadata2 = tileEntity.metadata2;

			mixedBlockMetadata = blockMetadata;
		}

		if (block1 == null || block2 == null)
			return false;

		return true;
	}

	@Override
	public void render()
	{
		if (!setup())
			return;

		if (renderType == RenderType.ITEM_INVENTORY)
		{
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.0F);
			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			GL11.glShadeModel(GL11.GL_SMOOTH);
			enableBlending();
		}

		if (MalisisDoorsSettings.simpleMixedBlockRendering.get() || !Minecraft.getMinecraft().gameSettings.fancyGraphics)
		{
			renderSimple();
			return;
		}

		set(block1, metadata1);
		drawPass(true);
		set(block2, metadata2);
		drawPass(false);
	}

	private void setColor()
	{
		int color = renderType == RenderType.ISBRH_WORLD ? block.colorMultiplier(world, x, y, z) : block.getBlockColor();
		rp.colorMultiplier.set(color);
		shape.setParameters("Top", rp, true);
		if (block instanceof BlockGrass)
		{
			rp.colorMultiplier.set(color);
			shape.setParameters("Top", rp, true);
			rp.colorMultiplier.set(0xFFFFFF);
		}
		//else

	}

	private void renderSimple()
	{
		boolean reversed = false;
		float width = 1;
		float height = 1;
		float depth = 1;
		float offsetX = 0;
		float offestY = 0;
		float offsetZ = 0;
		ForgeDirection dir = ForgeDirection.getOrientation(mixedBlockMetadata);

		if (dir == ForgeDirection.DOWN || dir == ForgeDirection.UP)
		{
			height = 0.5F;
			offestY = 0.5F;
			if (dir == ForgeDirection.UP)
				reversed = true;
		}
		if (dir == ForgeDirection.WEST || dir == ForgeDirection.EAST)
		{
			width = 0.5F;
			offsetX = 0.5F;
			if (dir == ForgeDirection.EAST)
				reversed = true;
		}
		if (dir == ForgeDirection.NORTH || dir == ForgeDirection.SOUTH)
		{
			depth = 0.5F;
			offsetZ = 0.5F;
			if (dir == ForgeDirection.SOUTH)
				reversed = true;
		}

		Block b = reversed ? block2 : block1;
		int m = reversed ? metadata2 : metadata1;
		set(b, m);
		setColor();

		simpleShape.resetState().setSize(width, height, depth);
		drawShape(simpleShape, rp);

		b = reversed ? block1 : block2;
		m = reversed ? metadata1 : metadata2;
		set(b, m);
		setColor();
		simpleShape.resetState().setSize(width, height, depth).translate(offsetX, offestY, offsetZ);
		drawShape(simpleShape, rp);
	}

	private void drawPass(boolean firstBlock)
	{
		ForgeDirection dir = ForgeDirection.getOrientation(mixedBlockMetadata);
		if (firstBlock)
			dir = dir.getOpposite();

		shape = shapes[firstBlock && renderType == RenderType.ISBRH_WORLD ? 1 : 0][dir.ordinal()];
		shape.resetState();

		if (shouldShadeFace(firstBlock))
		{
			List<MergedVertex> vertexes = shape.getMergedVertexes(dir);
			for (MergedVertex v : vertexes)
				v.setAlpha(0);
		}

		setColor();
		drawShape(shape, rp);
	}

	protected boolean shouldShadeFace(Boolean firstBlock)
	{
		Block[] shaded = new Block[] { Blocks.glass, Blocks.leaves, Blocks.leaves2 };
		if (block.canRenderInPass(1))
			return true;
		Block other = firstBlock ? block2 : block1;
		if (other.canRenderInPass(1))
			return true;
		if (ArrayUtils.contains(shaded, block) || ArrayUtils.contains(shaded, other))
			return true;

		return !firstBlock;
	}

	@Override
	protected boolean shouldRenderFace(Face face)
	{
		if (renderType != RenderType.ISBRH_WORLD || world == null || block == null)
			return true;
		if (rp != null && rp.renderAllFaces.get())
			return true;
		if (renderBlocks != null && renderBlocks.renderAllFaces == true)
			return true;
		RenderParameters p = face.getParameters();
		if (p.direction.get() == null)
			return true;

		boolean b = MalisisDoors.Blocks.mixedBlock.shouldSideBeRendered(world, x + p.direction.get().offsetX,
				y + p.direction.get().offsetY, z + p.direction.get().offsetZ, p.direction.get().ordinal());
		return b;
	}
}
