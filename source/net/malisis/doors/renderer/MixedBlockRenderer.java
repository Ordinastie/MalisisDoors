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

import net.malisis.core.block.IBlockDirectional;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.MergedVertex;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.shape.Cube;
import net.malisis.core.renderer.icon.VanillaIcon;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.MalisisDoorsSettings;
import net.malisis.doors.block.MixedBlock;
import net.malisis.doors.entity.MixedBlockTileEntity;
import net.malisis.doors.item.MixedBlockBlockItem;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

public class MixedBlockRenderer extends MalisisRenderer
{
	private IBlockState mixedBlockState;
	private MixedBlockTileEntity tileEntity;
	private Shape shape;
	private Shape simpleShape;
	private Shape[][] shapes;
	private RenderParameters rp;
	private IBlockState state1;
	private IBlockState state2;

	@Override
	protected void initialize()
	{
		simpleShape = new Cube();

		shapes = new Shape[][] { new Shape[6], new Shape[6] };
		for (EnumFacing dir : EnumFacing.VALUES)
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
		if (renderType == RenderType.ITEM)
		{
			if (!itemStack.hasTagCompound())
				return false;

			Pair<IBlockState, IBlockState> pair = MixedBlockBlockItem.readNBT(itemStack.getTagCompound());
			state1 = pair.getLeft();
			state2 = pair.getRight();

			mixedBlockState = ((MixedBlock) block).getDefaultState().withProperty(IBlockDirectional.ALL, EnumFacing.SOUTH);
		}
		else if (renderType == RenderType.BLOCK)
		{
			tileEntity = TileEntityUtils.getTileEntity(MixedBlockTileEntity.class, world, pos);
			state1 = tileEntity.getState1();
			state2 = tileEntity.getState2();

			mixedBlockState = blockState;
		}

		if (state1 == null || state2 == null)
			return false;
		return true;
	}

	@Override
	public void render()
	{
		if (!setup())
			return;

		if (renderType == RenderType.ITEM)
		{
			GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
			GlStateManager.enableColorMaterial();
			GlStateManager.shadeModel(GL11.GL_SMOOTH);
			enableBlending();
		}

		if (MalisisDoorsSettings.simpleMixedBlockRendering.get() || !Minecraft.getMinecraft().gameSettings.fancyGraphics)
		{
			renderSimple();
			return;
		}

		set(state1);
		drawPass(true);
		set(state2);
		drawPass(false);
	}

	private void setColor()
	{
		int color = renderType == RenderType.BLOCK ? block.colorMultiplier(world, pos) : block.getBlockColor();
		rp.colorMultiplier.set(block instanceof BlockGrass ? 0xFFFFFF : color);
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
		EnumFacing dir = IBlockDirectional.getDirection(mixedBlockState);

		if (dir == EnumFacing.DOWN || dir == EnumFacing.UP)
		{
			height = 0.5F;
			offestY = 0.5F;
			if (dir == EnumFacing.UP)
				reversed = true;
		}
		if (dir == EnumFacing.WEST || dir == EnumFacing.EAST)
		{
			width = 0.5F;
			offsetX = 0.5F;
			if (dir == EnumFacing.EAST)
				reversed = true;
		}
		if (dir == EnumFacing.NORTH || dir == EnumFacing.SOUTH)
		{
			depth = 0.5F;
			offsetZ = 0.5F;
			if (dir == EnumFacing.SOUTH)
				reversed = true;
		}

		shape = simpleShape;
		set(reversed ? state2 : state1);
		shape.resetState().setSize(width, height, depth);
		rp.icon.set(new VanillaIcon(blockState));
		setColor();
		drawShape(shape, rp);

		set(reversed ? state1 : state2);
		shape.resetState().setSize(width, height, depth).translate(offsetX, offestY, offsetZ);
		rp.icon.set(new VanillaIcon(blockState));
		setColor();
		drawShape(shape, rp);
	}

	private void drawPass(boolean firstBlock)
	{
		EnumFacing dir = IBlockDirectional.getDirection(mixedBlockState);
		if (firstBlock)
			dir = dir.getOpposite();

		shape = shapes[firstBlock && renderType == RenderType.BLOCK ? 1 : 0][dir.ordinal()];
		shape.resetState();

		if (shouldShadeFace(firstBlock))
		{
			List<MergedVertex> vertexes = shape.getMergedVertexes(dir);
			for (MergedVertex v : vertexes)
				v.setAlpha(0);
		}

		rp.icon.set(new VanillaIcon(blockState));
		setColor();

		drawShape(shape, rp);
	}

	protected boolean shouldShadeFace(Boolean firstBlock)
	{
		if (block.canRenderInLayer(EnumWorldBlockLayer.TRANSLUCENT) || block.canRenderInLayer(EnumWorldBlockLayer.CUTOUT)
				|| block.canRenderInLayer(EnumWorldBlockLayer.CUTOUT_MIPPED))
			return true;

		IBlockState other = firstBlock ? state2 : state1;
		if (other.getBlock().canRenderInLayer(EnumWorldBlockLayer.TRANSLUCENT)
				|| other.getBlock().canRenderInLayer(EnumWorldBlockLayer.CUTOUT)
				|| other.getBlock().canRenderInLayer(EnumWorldBlockLayer.CUTOUT_MIPPED))
			return true;

		return !firstBlock;
	}

	@Override
	protected boolean shouldRenderFace(Face face, RenderParameters params)
	{
		if (renderType != RenderType.BLOCK || world == null || block == null)
			return true;
		if (params != null && params.renderAllFaces.get())
			return true;

		RenderParameters p = face.getParameters();
		if (p.direction.get() == null)
			return true;

		return mixedBlockState.getBlock().shouldSideBeRendered(world, pos.offset(p.direction.get()), p.direction.get());
	}
}
