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

import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;

import net.malisis.core.renderer.DefaultRenderer;
import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.AnimationRenderer;
import net.malisis.core.renderer.animation.transformation.ITransformable;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.element.shape.Cube;
import net.malisis.core.renderer.icon.IIconProvider;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.doors.MalisisDoors.Blocks;
import net.malisis.doors.MalisisDoorsSettings;
import net.malisis.doors.block.Door;
import net.malisis.doors.iconprovider.DoorIconProvider;
import net.malisis.doors.tileentity.DoorTileEntity;
import net.minecraft.block.BlockDoor;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.TRSRTransformation;

import org.apache.commons.lang3.ArrayUtils;

@SuppressWarnings("deprecation")
public class DoorRenderer extends MalisisRenderer<DoorTileEntity>
{
	protected EnumFacing direction;
	protected boolean opened;
	protected boolean hingeLeft;
	protected boolean topBlock;

	protected MalisisModel model;
	protected RenderParameters rp;
	protected AnimationRenderer ar = new AnimationRenderer();

	protected Matrix4f gui = new TRSRTransformation(new Vector3f(.15F, -0.2F, 0), TRSRTransformation.quatFromYXZDegrees(new Vector3f(0, 90,
			0)), new Vector3f(.75F, .75F, .75F), null).getMatrix();
	protected Matrix4f thirdPerson = new TRSRTransformation(new Vector3f(-0, 0, -0.25F),
			TRSRTransformation.quatFromYXZDegrees(new Vector3f(90, 0, 0)), new Vector3f(0.3F, 0.3F, 0.3F), null).getMatrix();
	protected Matrix4f firstPerson = new TRSRTransformation(null, TRSRTransformation.quatFromYXZDegrees(new Vector3f(0, 90, 0)),
			new Vector3f(0.9F, 0.8F, 1), null).getMatrix();

	public DoorRenderer()
	{
		registerFor(DoorTileEntity.class);
	}

	public DoorRenderer(boolean noRegister)
	{}

	@Override
	protected void initialize()
	{
		Shape bottom = new Cube();
		bottom.setSize(1, 1, Door.DOOR_WIDTH);
		bottom.scale(1, 1, 0.995F);
		Shape top = new Shape(bottom);
		top.translate(0, 1, 0);

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
	public boolean isGui3d()
	{
		return MalisisDoorsSettings.use3DItems.get();
	}

	@Override
	public Matrix4f getTransform(TransformType tranformType)
	{
		if (!isGui3d())
			return DefaultRenderer.item.getTransform(tranformType);

		switch (tranformType)
		{
			case GUI:
				return gui;
			case FIRST_PERSON:
				return firstPerson;
			case THIRD_PERSON:
				return thirdPerson;
			default:
				return null;
		}
	}

	@Override
	public void render()
	{
		if (renderType == RenderType.BLOCK)
			return;

		rp.icon.set(null);
		if (renderType == RenderType.TILE_ENTITY)
		{
			setTileEntity();
			setup();
			renderTileEntity();
		}

		if (renderType == RenderType.ITEM)
		{
			if (!isGui3d())
			{
				DefaultRenderer.item.setTransformType(tranformType);
				DefaultRenderer.item.renderItem(itemStack, partialTick);
				return;
			}
			setItem();
			setup();
			renderItem();
		}

	}

	protected void setup()
	{
		model.resetState();

		if (tileEntity != null && tileEntity.isCentered())
			model.translate(0, 0, 0.5F - Door.DOOR_WIDTH / 2);

		if (direction == EnumFacing.NORTH)
			model.rotate(180, 0, 1, 0, 0, 0, 0);
		if (direction == EnumFacing.WEST)
			model.rotate(-90, 0, 1, 0, 0, 0, 0);
		if (direction == EnumFacing.EAST)
			model.rotate(90, 0, 1, 0, 0, 0, 0);

	}

	protected void setTileEntity()
	{
		direction = tileEntity.getDirection();
		opened = tileEntity.isOpened();
		hingeLeft = tileEntity.isHingeLeft();
	}

	protected void renderTileEntity()
	{
		enableBlending();
		ar.setStartTime(tileEntity.getTimer().getStart());

		List<ITransformable> toRender = new ArrayList<>();
		if (tileEntity.getMovement() != null)
		{
			Animation<?>[] anims = tileEntity.getMovement().getAnimations(tileEntity, model, rp);
			toRender = ar.animate(anims);
			if (!ArrayUtils.isEmpty(anims) && toRender.size() == 0)
				return;
		}

		//model.render(this, rp);
		topBlock = false;
		rp.brightness.set(block.getMixedBrightnessForBlock(world, pos));
		drawShape(model.getShape("bottom"), rp);

		topBlock = true;
		set(pos.up());
		set(blockState.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER));
		rp.brightness.set(block.getMixedBrightnessForBlock(world, pos));
		drawShape(model.getShape("top"), rp);
		set(pos.down());
	}

	@Override
	protected boolean shouldRenderFace(Face face, RenderParameters params)
	{
		if (block != Blocks.doorOak && block != Blocks.doorAcacia && block != Blocks.doorBirch && block != Blocks.doorDarkOak
				&& block != Blocks.doorJungle && block != Blocks.doorSpruce && block != Blocks.doorIron)
			return super.shouldRenderFace(face, params);

		if (!topBlock && face.name().equals("Top"))
			return false;

		if (topBlock && face.name().equals("Bottom"))
			return false;

		return super.shouldRenderFace(face, params);
	}

	protected void setItem()
	{
		direction = EnumFacing.SOUTH;
		hingeLeft = true;
	}

	protected void renderItem()
	{
		enableBlending();
		topBlock = false;
		drawShape(model.getShape("bottom"), rp);
		topBlock = true;
		drawShape(model.getShape("top"), rp);

		return;
	}

	@Override
	protected MalisisIcon getIcon(Face face, RenderParameters params)
	{
		if (params.icon.get() != null)
			return params.icon.get();

		IIconProvider iconProvider = getIconProvider(params);
		if (!(iconProvider instanceof DoorIconProvider))
			return super.getIcon(face, params);

		return ((DoorIconProvider) iconProvider).getIcon(topBlock, hingeLeft, params.textureSide.get());
	}

}
