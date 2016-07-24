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

import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.animation.Animation;
import net.malisis.core.renderer.element.Face;
import net.malisis.core.renderer.icon.Icon;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.core.util.TransformBuilder;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.MalisisDoorsSettings;
import net.malisis.doors.block.Door;
import net.malisis.doors.block.VerticalHatchDoor;
import net.malisis.doors.tileentity.VerticalHatchTileEntity;
import net.minecraft.util.ResourceLocation;

import org.lwjgl.opengl.GL11;

/**
 * @author Ordinastie
 *
 */
public class VerticalHatchRenderer extends DoorRenderer
{

	public VerticalHatchRenderer()
	{
		super(false);
		registerFor(VerticalHatchTileEntity.class);
		//make sure the icon gets registered
		//		Icon.from(MalisisDoors.modid + ":blocks/verticalHatch");
	}

	@Override
	protected void initialize()
	{
		ResourceLocation rl = new ResourceLocation(MalisisDoors.modid + ":models/verticalHatch.obj");
		model = new MalisisModel(rl);

		initParams();

		gui = new TransformBuilder().translate(.0F, -0.28F, 0).rotate(0, 0, 0).scale(.7F, .45F, 1).get();
		firstPerson = new TransformBuilder().translate(.0F, -0.28F, 0).rotate(0, -45, 0).scale(.5F).get();

		ensureBlock(VerticalHatchDoor.class);
	}

	@Override
	public void render()
	{

		if (renderType == RenderType.BLOCK)
		{
			if (!Door.isTop(blockState))
			{
				setTileEntity();
				setup();
				drawShape(model.getShape("Frame"), rp);
			}
			return;
		}

		super.render();
	}

	@Override
	protected void renderTileEntity()
	{
		enableBlending();
		ar.setStartTime(tileEntity.getTimer().getStart());

		if (tileEntity.getMovement() != null)
		{
			Animation<?>[] anims = tileEntity.getMovement().getAnimations(tileEntity, model, rp);
			ar.animate(anims);
		}

		next(GL11.GL_POLYGON);
		//model.render(this, rp);
		rp.brightness.set(blockState.getPackedLightmapCoords(world, pos));
		drawShape(model.getShape("Door"), rp);
		drawShape(model.getShape(MalisisDoorsSettings.use3branchgHandle.get() ? "Handle3" : "Handle4"), rp);
	}

	@Override
	public boolean isGui3d()
	{
		return true;
	}

	@Override
	protected void renderItem()
	{
		next(GL11.GL_POLYGON);
		drawShape(model.getShape("Door"), rp);
		drawShape(model.getShape(MalisisDoorsSettings.use3branchgHandle.get() ? "Handle3" : "Handle4"), rp);
	}

	@Override
	protected Icon getIcon(Face face, RenderParameters params)
	{
		return MalisisDoors.Blocks.verticalHatch.getIconProvider().getIcon();
	}
}
