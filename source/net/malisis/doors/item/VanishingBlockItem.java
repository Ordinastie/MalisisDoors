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

package net.malisis.doors.item;

import net.malisis.doors.MalisisDoors;
import net.malisis.doors.block.VanishingBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class VanishingBlockItem extends ItemBlock
{
	String[] names = { "wood", "iron", "gold", "diamond" };

	public VanishingBlockItem(Block block)
	{
		super(block);
		setHasSubtypes(true);
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		int i = itemstack.getMetadata();
		if (i < 0 || i >= names.length)
			i = 0;
		return getUnlocalizedName() + "_" + names[i];
	}

	@Override
	public int getMetadata(int metadata)
	{
		return metadata;
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
	{
		Block block = blockInstance;
		if ((metadata & 3) == VanishingBlock.typeDiamondFrame)
			block = MalisisDoors.Blocks.vanishingDiamondBlock;

		if (!world.setBlock(x, y, z, block, metadata, 3))
			return false;

		if (world.getBlock(x, y, z) == block)
		{
			block.onBlockPlacedBy(world, x, y, z, player, stack);
			block.onPostBlockPlaced(world, x, y, z, metadata);
		}

		return true;
	}
}
