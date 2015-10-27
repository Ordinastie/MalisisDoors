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

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.AnimationRenderer;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.icon.VanillaIcon;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.core.util.multiblock.MultiBlock;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.door.block.RustyHatch;
import net.malisis.doors.door.block.RustyHatch.RustyHatchIconProvider;
import net.malisis.doors.door.tileentity.RustyHatchTileEntity;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.TRSRTransformation;

import org.lwjgl.opengl.GL11;

/**
 * @author Ordinastie
 *
 */
@SuppressWarnings("deprecation")
public class RustyHatchRenderer extends MalisisRenderer
{
	public static RustyHatchRenderer instance;
	private ResourceLocation rl;
	private MalisisModel model;
	private Shape frame;
	private Shape hatch;
	private Shape handle;
	private Shape ladder;
	private RenderParameters rp = new RenderParameters();
	private AnimationRenderer ar = new AnimationRenderer();
	private RustyHatchTileEntity tileEntity;

	private boolean topBlock;
	private EnumFacing direction;

	public RustyHatchRenderer()
	{
		registerFor(RustyHatchTileEntity.class);
		instance = this;
	}

	@Override
	protected void initialize()
	{
		rl = new ResourceLocation(MalisisDoors.modid, "models/rustyhatch.obj");
		model = new MalisisModel(rl);
		frame = model.getShape("frame");
		hatch = model.getShape("door");
		handle = model.getShape("handle");
		ladder = model.getShape("ladder");

		rp = new RenderParameters();
		rp.useBlockBounds.set(false);
		rp.calculateBrightness.set(false);
	}

	private void setup(Shape s)
	{
		if (topBlock && s != ladder)
			s.translate(0, 1 - 0.125F, 0);

		if (direction == EnumFacing.SOUTH)
			s.rotate(-90, 0, 1, 0);
		else if (direction == EnumFacing.NORTH)
			s.rotate(90, 0, 1, 0);
		else if (direction == EnumFacing.WEST)
			s.rotate(180, 0, 1, 0);
	}

	@Override
	public void render()
	{
		if (renderType == RenderType.ITEM)
		{
			renderItem();
			return;
		}

		tileEntity = RustyHatch.getRustyHatch(world, pos);
		if (tileEntity == null)
			return;

		direction = tileEntity.getDirection();
		rp.brightness.set(block.getMixedBrightnessForBlock(world, pos));
		topBlock = tileEntity.isTop();
		if (renderType == RenderType.BLOCK)
		{
			getBlockDamage = true;
			renderBlock();
		}
		else if (renderType == RenderType.TILE_ENTITY)
			renderTileEntity();
	}

	private void renderBlock()
	{
		if (!MultiBlock.isOrigin(world, pos))
		{
			if (!tileEntity.shouldLadder(pos)/* || y == tileEntity.getMultiBlock().getY()*/)
				return;
			if (ladder == null)
				return;

			ladder.resetState();
			setup(ladder);
			ladder.translate(-1, topBlock ? -0 : 0, 0);
			//ladder.translate(direction.getOpposite().getFrontOffsetX(), topBlock ? -1 : 0, direction.getOpposite().getFrontOffsetZ());

			rp.icon.set(((RustyHatchIconProvider) ((RustyHatch) block).getIconProvider()).getHandleIcon());
			drawShape(ladder, rp);
		}
		else
		{
			if (frame == null)
				return;
			frame.resetState();
			setup(frame);
			rp.icon.set(new VanillaIcon(Blocks.furnace));
			drawShape(frame, rp);
		}
	}

	private void renderTileEntity()
	{
		if (hatch == null || handle == null)
			return;

		hatch.resetState();
		handle.resetState();
		setup(hatch);
		setup(handle);

		ar.setStartTime(tileEntity.getTimer().getStart());

		if (tileEntity.getMovement() != null)
		{
			Animation[] anims = tileEntity.getMovement().getAnimations(tileEntity, model, rp);
			ar.animate(anims);
		}

		next(GL11.GL_POLYGON);
		rp.icon.set(((RustyHatchIconProvider) ((RustyHatch) block).getIconProvider()).getHatchIcon());
		drawShape(hatch, rp);

		rp.icon.set(((RustyHatchIconProvider) ((RustyHatch) block).getIconProvider()).getHandleIcon());
		drawShape(handle, rp);
	}

	@Override
	public Matrix4f getTransform(TransformType tranformType)
	{
		Matrix4f gui = new TRSRTransformation(new Vector3f(-0.2F, 0.5F, 0.15F),
				TRSRTransformation.quatFromYXZDegrees(new Vector3f(90, 0, 0)), null, null).getMatrix();
		Matrix4f thirdPerson = new TRSRTransformation(new Vector3f(-0.00F, 0, -0.20F), TRSRTransformation.quatFromYXZDegrees(new Vector3f(
				0, 110, 0)), new Vector3f(-0.5F, 0.5F, 0.5F), null).getMatrix();

		if (tranformType == TransformType.GUI)
			return gui;

		return super.getTransform(tranformType);
		//		switch (itemRenderType)
		//		{
		//			case INVENTORY:
		//				handle.translate(0.20F, 0, 0.15F);
		//				handle.rotate(90, 0, 0, 1);
		//				break;
		//			case EQUIPPED:
		//				handle.translate(-0.5F, -.75F, 0);
		//				handle.rotate(100, 0, 0, 1);
		//				break;
		//			case EQUIPPED_FIRST_PERSON:
		//				handle.translate(0, -0.1F, -0.2F);
		//				handle.rotate(90, 0, 0, 1);
		//			default:
		//				break;
		//		}
	}

	private void renderItem()
	{
		bindTexture(TextureMap.locationBlocksTexture);
		//MalisisCore.message(item.getUnlocalizedName());
		Shape shape = item == MalisisDoors.Items.rustyHandle ? handle : ladder;
		shape.resetState();
		shape.scale(1.5F);

		RustyHatchIconProvider iconProvider = (RustyHatchIconProvider) MalisisDoors.Blocks.rustyHatch.getIconProvider();
		rp.icon.set(iconProvider.getHandleIcon());
		//rp.icon.set(((RustyHatchIconProvider) ((RustyHatch) block).getIconProvider()).getHandleIcon());
		drawShape(shape, rp);
	}
}
