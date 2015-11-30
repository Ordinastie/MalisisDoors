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
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.animation.AnimationRenderer;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.core.util.BlockState;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.door.block.BigDoor;
import net.malisis.doors.door.block.Door;
import net.malisis.doors.door.tileentity.BigDoorTileEntity;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

import org.lwjgl.opengl.GL11;

/**
 * @author Ordinastie
 *
 */
public class BigDoorRenderer extends MalisisRenderer
{
	private ResourceLocation rl;
	private MalisisModel model;
	private Shape frame;
	private Shape doorLeft;
	private Shape doorRight;
	private AnimationRenderer ar = new AnimationRenderer();
	private BigDoorTileEntity tileEntity;

	private ForgeDirection direction;

	public BigDoorRenderer()
	{
		getBlockDamage = true;
	}

	@Override
	protected void initialize()
	{
		rl = new ResourceLocation(MalisisDoors.modid, "models/big_door.obj");
		model = new MalisisModel(rl);
		frame = model.getShape("Frame");
		doorLeft = model.getShape("Left");
		doorRight = model.getShape("Right");

		rp = new RenderParameters();
		rp.useBlockBounds.set(false);
	}

	@Override
	public void render()
	{
		if (super.tileEntity == null)
			return;

		tileEntity = (BigDoorTileEntity) super.tileEntity;
		direction = Door.intToDir(tileEntity.getDirection());
		setup();

		if (renderType == RenderType.ISBRH_WORLD)
		{
			getBlockDamage = true;
			renderBlock();
		}
		else if (renderType == RenderType.TESR_WORLD)
			renderTileEntity();
	}

	private void renderBlock()
	{
		BlockState state = tileEntity.getFrameState();
		if (!state.getBlock().canRenderInPass(BigDoor.renderPass))
			return;

		set(state.getBlock(), state.getMetadata());
		//rp.icon.set(state.getBlock().getIcon(1, state.getMetadata()));
		rp.icon.reset();
		rp.useWorldSensitiveIcon.set(false);
		drawShape(frame, rp);
	}

	private void renderTileEntity()
	{
		ar.setStartTime(tileEntity.getTimer().getStart());

		if (tileEntity.getMovement() != null)
		{
			Animation[] anims = tileEntity.getMovement().getAnimations(tileEntity, model, rp);
			ar.animate(anims);
		}

		next(GL11.GL_POLYGON);
		rp.icon.reset();
		drawShape(doorLeft, rp);
		drawShape(doorRight, rp);
	}

	private void setup()
	{
		model.resetState();
		if (direction == ForgeDirection.SOUTH)
			model.rotate(180, 0, 1, 0, 0, 0, 0);
		else if (direction == ForgeDirection.EAST)
			model.rotate(-90, 0, 1, 0, 0, 0, 0);
		else if (direction == ForgeDirection.WEST)
			model.rotate(90, 0, 1, 0, 0, 0, 0);

		rp.brightness.set(block.getMixedBrightnessForBlock(world, x, y, z));
	}

	@Override
	protected boolean isCurrentBlockDestroyProgress(DestroyBlockProgress dbp)
	{
		//		MultiBlock mb = MultiBlock.getMultiBlock(world, dbp.getPartialBlockX(), dbp.getPartialBlockY(), dbp.getPartialBlockZ());
		//		return mb != null && mb.getX() == tileEntity.getMultiBlock().getX() && mb.getY() == tileEntity.getMultiBlock().getY()
		//				&& mb.getZ() == tileEntity.getMultiBlock().getZ();
		//TODO:
		//return super.isCurrentBlockDestroyProgress(dbp);
		return true;
	}

	@Override
	public boolean shouldRender3DInInventory(int modelId)
	{
		return false;
	}

}
