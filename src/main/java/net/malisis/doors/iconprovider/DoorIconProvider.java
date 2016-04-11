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

package net.malisis.doors.iconprovider;

import net.malisis.core.renderer.icon.ClippedIcon;
import net.malisis.core.renderer.icon.Icon;
import net.malisis.core.renderer.icon.provider.IIconProvider;
import net.malisis.doors.DoorDescriptor;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.descriptor.VanillaDoor;
import net.minecraft.util.EnumFacing;

/**
 * @author Ordinastie
 *
 */
public class DoorIconProvider implements IIconProvider
{
	protected Icon itemIcon;
	protected Icon top;
	protected Icon bottom;
	protected Icon side;
	protected Icon[] iconTop;
	protected Icon[] iconBottom;

	public DoorIconProvider(DoorDescriptor descriptor)
	{
		String modid = descriptor.getModId();
		String name = descriptor.getTextureName();

		itemIcon = Icon.from(modid + ":items/" + name);
		top = Icon.from(modid + ":blocks/" + name + "_upper");
		bottom = Icon.from(modid + ":blocks/" + name + "_lower");

		//for the side of vanilla doors, add MalisisDoors: to the name
		if (descriptor instanceof VanillaDoor)
			modid = MalisisDoors.modid;
		side = new Icon(modid + ":blocks/" + name + "_side");

		buildIcons();

	}

	private void buildIcons()
	{
		float w = 3F / 16F;
		iconTop = new Icon[6];
		iconTop[0] = new ClippedIcon(side, 0, 0, w, 1);
		iconTop[0].setRotation(1);
		iconTop[1] = iconTop[0];
		iconTop[2] = top;
		iconTop[3] = top;
		iconTop[4] = new ClippedIcon(side, w, 0, w, 1);
		iconTop[5] = new ClippedIcon(side, 2 * w, 0, w, 1);

		iconBottom = new Icon[6];
		iconBottom[0] = iconTop[0];
		iconBottom[1] = iconTop[0];
		iconBottom[2] = bottom;
		iconBottom[3] = bottom;
		iconBottom[4] = new ClippedIcon(side, 3 * w, 0, w, 1);
		iconBottom[5] = new ClippedIcon(side, 4 * w, 0, w, 1);

	}

	@Override
	public Icon getIcon()
	{
		//particles
		return top;
	}

	public Icon getIcon(boolean isTop, boolean isHingeLeft, EnumFacing side)
	{
		boolean flipH = false;
		boolean flipV = false;

		switch (side)
		{
			case WEST:
				side = isHingeLeft ? EnumFacing.WEST : EnumFacing.EAST;
				break;
			case EAST:
				side = isHingeLeft ? EnumFacing.EAST : EnumFacing.WEST;
				break;
			case UP:
			case DOWN:
				flipV = isHingeLeft;
			case NORTH:
			case SOUTH:
				flipH = isHingeLeft;
				break;
		}

		Icon icon = isTop ? iconTop[side.getIndex()] : iconBottom[side.getIndex()];
		icon.flip(flipH, flipV);

		return icon;
	}

}
