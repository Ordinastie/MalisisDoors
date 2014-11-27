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

import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.renderer.RenderParameters;
import net.malisis.core.renderer.RenderType;
import net.malisis.core.renderer.element.Shape;
import net.malisis.core.renderer.model.MalisisModel;
import net.malisis.doors.MalisisDoors;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @author Ordinastie
 *
 */
public class RustyLadderRenderer extends MalisisRenderer
{
	private Shape ladder;

	@Override
	protected void initialize()
	{
		ResourceLocation rl = new ResourceLocation(MalisisDoors.modid, "models/rustyhatch.obj");
		MalisisModel model = new MalisisModel(rl);
		ladder = model.getShape("ladder");

		rp = new RenderParameters();
		rp.useBlockBounds.set(false);
	}

	@Override
	public void render()
	{
		ladder.resetState();

		ForgeDirection dir = ForgeDirection.getOrientation(blockMetadata);
		switch (dir)
		{
			case NORTH:
				ladder.rotate(-90, 0, 1, 0);
				break;
			case SOUTH:
				ladder.rotate(90, 0, 1, 0);
				break;
			case EAST:
				ladder.rotate(180, 0, 1, 0);
				break;
			case WEST:
			default:
				break;
		}

		if (renderType == RenderType.ITEM_INVENTORY)
		{
			if (itemRenderType == ItemRenderType.INVENTORY)
			{
				ladder.rotate(-45, 0, 1, 0);
				ladder.scale(1.5F);
				ladder.translate(0, .15F, 0);
			}
			else if (itemRenderType == ItemRenderType.ENTITY)
			{
				ladder.translate(-1, 0, -0.5F);
				ladder.scale(1.5F);
			}
		}

		ladder.translate(-1, 0, 0);

		drawShape(ladder, rp);
		return;
	}

}
