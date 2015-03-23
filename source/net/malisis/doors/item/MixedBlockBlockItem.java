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

import java.util.HashMap;
import java.util.List;

import net.malisis.doors.MalisisDoors;
import net.malisis.doors.block.MixedBlock;
import net.malisis.doors.entity.MixedBlockTileEntity;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class MixedBlockBlockItem extends ItemBlock
{
	private static HashMap<Item, Block> itemsAllowed = new HashMap<>();
	static
	{
		itemsAllowed.put(Items.ender_pearl, Blocks.portal);
		itemsAllowed.put(Items.water_bucket, Blocks.water);
		itemsAllowed.put(Items.lava_bucket, Blocks.lava);
	}

	public MixedBlockBlockItem(Block block)
	{
		super(block);
	}

	@Override
	public void onCreated(ItemStack itemStack, World world, EntityPlayer player)
	{
		itemStack.stackTagCompound = new NBTTagCompound();
	}

	public static ItemStack fromItemStacks(ItemStack is1, ItemStack is2)
	{
		if (!canBeMixed(is1) || !canBeMixed(is2))
			return null;

		//Blocks
		Block block1 = itemsAllowed.get(is1.getItem());
		if (block1 == null)
			block1 = Block.getBlockFromItem(is1.getItem());
		Block block2 = itemsAllowed.get(is2.getItem());
		if (block2 == null)
			block2 = Block.getBlockFromItem(is2.getItem());

		//metadatas
		int metadata1 = is1.getMetadata();
		if (is1.getItem() instanceof ItemBlock)
			metadata1 = ((ItemBlock) is1.getItem()).getMetadata(is1.getMetadata());

		int metadata2 = is2.getMetadata();
		if (is2.getItem() instanceof ItemBlock)
			metadata2 = ((ItemBlock) is2.getItem()).getMetadata(is2.getMetadata());

		//last check
		if (block1 == block2 && metadata1 == metadata2)
			return null;

		//nbt
		ItemStack itemStack = new ItemStack(MalisisDoors.Blocks.mixedBlock, 1);
		itemStack.stackTagCompound = new NBTTagCompound();
		itemStack.stackTagCompound.setInteger("block1", Block.getIdFromBlock(block1));
		itemStack.stackTagCompound.setInteger("block2", Block.getIdFromBlock(block2));
		itemStack.stackTagCompound.setInteger("metadata1", metadata1);
		itemStack.stackTagCompound.setInteger("metadata2", metadata2);

		return itemStack;
	}

	public static boolean canBeMixed(ItemStack itemStack)
	{
		if (itemsAllowed.get(itemStack.getItem()) != null)
			return true;

		Block block = Block.getBlockFromItem(itemStack.getItem());
		return !(block instanceof MixedBlock) && block.getRenderType() != -1;
	}

	public static ItemStack fromTileEntity(MixedBlockTileEntity te)
	{
		ItemStack itemStack = new ItemStack(MalisisDoors.Blocks.mixedBlock, 1);
		itemStack.stackTagCompound = new NBTTagCompound();
		itemStack.stackTagCompound.setInteger("block1", Block.getIdFromBlock(te.block1));
		itemStack.stackTagCompound.setInteger("block2", Block.getIdFromBlock(te.block2));
		itemStack.stackTagCompound.setInteger("metadata1", te.metadata1);
		itemStack.stackTagCompound.setInteger("metadata2", te.metadata2);

		return itemStack;
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean advancedTooltip)
	{
		if (itemStack.stackTagCompound == null)
			return;
		Block block1 = Block.getBlockById(itemStack.stackTagCompound.getInteger("block1"));
		int metadata1 = itemStack.stackTagCompound.getInteger("metadata1");
		ItemStack is1 = new ItemStack(block1, 0, metadata1);

		Block block2 = Block.getBlockById(itemStack.stackTagCompound.getInteger("block2"));
		int metadata2 = itemStack.stackTagCompound.getInteger("metadata2");
		ItemStack is2 = new ItemStack(block2, 0, metadata2);

		if (block1 != Blocks.air)
			list.addAll(is1.getTooltip(player, advancedTooltip));
		if (block2 != Blocks.air)
			list.addAll(is2.getTooltip(player, advancedTooltip));

	}
}
