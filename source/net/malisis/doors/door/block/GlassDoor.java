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

package net.malisis.doors.door.block;

import java.util.Random;

import net.malisis.doors.MalisisDoors;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 * 
 */
public class GlassDoor extends SlidingDoor
{
	public GlassDoor(Material material)
	{
		super(material);
		if (material == Material.wood)
		{
			setHardness(2.0F);
			setStepSound(soundTypeWood);
			setBlockName("wood_sliding_door");
			setBlockTextureName(MalisisDoors.modid + ":sliding_door_wood");
		}
		else
		{
			setHardness(3.0F);
			setStepSound(soundTypeMetal);
			setBlockName("iron_sliding_door");
			setBlockTextureName(MalisisDoors.modid + ":sliding_door_iron");
		}
	}

	@Override
	public Item getItemDropped(int metadata, Random par2Random, int par3)
	{
		if ((metadata & 8) != 0)
			return null;

		if (this.blockMaterial == Material.iron)
			return MalisisDoors.Items.ironSlidingDoorItem;
		else
			return MalisisDoors.Items.woodSlidingDoorItem;
	}

	@Override
	public Item getItem(World world, int x, int y, int z)
	{
		return this.blockMaterial == Material.iron ? MalisisDoors.Items.ironSlidingDoorItem : MalisisDoors.Items.woodSlidingDoorItem;
	}
}
