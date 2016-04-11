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

import javax.vecmath.Matrix4f;

import net.malisis.core.block.IComponent;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.AnimationRenderer;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.icon.VanillaIcon;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.core.util.TransformBuilder;
import net.malisis.core.util.multiblock.MultiBlock;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.block.RustyHatch;
import net.malisis.doors.block.RustyHatch.RustyHatchIconProvider;
import net.malisis.doors.tileentity.RustyHatchTileEntity;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

/**
 * @author Ordinastie
 *
 */
public class RustyHatchRenderer extends MalisisRenderer<RustyHatchTileEntity>
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
	private RustyHatchIconProvider iconProvider;

	private boolean topBlock;
	private EnumFacing direction;

	private Matrix4f gui = new TransformBuilder().translate(-0.2F, 0.5F, 0.15F).rotate(90, 0, 0).get();
	private Matrix4f thirdPerson = new TransformBuilder().translate(0, 0.15F, -0.25F).rotate(0, -45, 0).scale(0.25F).get();

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

		iconProvider = IComponent.getComponent(RustyHatchIconProvider.class, MalisisDoors.Blocks.rustyHatch);
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
		rp.brightness.set(blockState.getPackedLightmapCoords(world, pos));
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

			rp.icon.set(iconProvider.getHandleIcon());
			drawShape(ladder, rp);
		}
		else
		{
			if (frame == null)
				return;
			frame.resetState();
			setup(frame);
			rp.icon.set(new VanillaIcon(Blocks.FURNACE));
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
			Animation<?>[] anims = tileEntity.getMovement().getAnimations(tileEntity, model, rp);
			ar.animate(anims);
		}

		next(GL11.GL_POLYGON);
		rp.icon.set(iconProvider.getHatchIcon());
		drawShape(hatch, rp);

		rp.icon.set(iconProvider.getHandleIcon());
		drawShape(handle, rp);
	}

	@Override
	public Matrix4f getTransform(TransformType tranformType)
	{
		switch (tranformType)
		{
			case GUI:
				return gui;
			case THIRD_PERSON_RIGHT_HAND:
			case THIRD_PERSON_LEFT_HAND:
				return thirdPerson;
			default:
				return null;
		}
	}

	private void renderItem()
	{
		bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		//MalisisCore.message(item.getUnlocalizedName());
		Shape shape = item == MalisisDoors.Items.rustyHandle ? handle : ladder;
		shape.resetState();
		shape.scale(1.5F);

		rp.icon.set(iconProvider.getHandleIcon());
		drawShape(shape, rp);
	}
}
