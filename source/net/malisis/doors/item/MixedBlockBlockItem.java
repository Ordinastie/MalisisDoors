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

import java.util.List;

import net.malisis.core.util.ItemUtils;
import net.malisis.core.util.MBlockState;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.block.MixedBlock;
import net.malisis.doors.entity.MixedBlockTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Objects;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

public class MixedBlockBlockItem extends ItemBlock
{
	private static BiMap<Item, IBlockState> itemsAllowed = HashBiMap.create();
	static
	{
		itemsAllowed.put(Items.ender_pearl, Blocks.portal.getDefaultState());
		itemsAllowed.put(Items.water_bucket, Blocks.water.getDefaultState());
		itemsAllowed.put(Items.lava_bucket, Blocks.lava.getDefaultState());
	}

	public MixedBlockBlockItem(Block block)
	{
		super(block);
	}

	@Override
	public void onCreated(ItemStack itemStack, World world, EntityPlayer player)
	{
		itemStack.setTagCompound(new NBTTagCompound());
	}

	public static boolean canBeMixed(ItemStack itemStack)
	{
		if (itemsAllowed.get(itemStack.getItem()) != null)
			return true;

		Block block = Block.getBlockFromItem(itemStack.getItem());
		return block != null && !(block instanceof MixedBlock) && block.getRenderType() != -1;
	}

	public static ItemStack fromItemStacks(ItemStack is1, ItemStack is2)
	{
		if (!canBeMixed(is1) || !canBeMixed(is2))
			return null;

		IBlockState state1 = Objects.firstNonNull(itemsAllowed.get(is1.getItem()), ItemUtils.getStateFromItemStack(is1));
		IBlockState state2 = Objects.firstNonNull(itemsAllowed.get(is2.getItem()), ItemUtils.getStateFromItemStack(is2));

		//last check
		if (state1 == null || state2 == null || state1.equals(state2))
			return null;

		//nbt
		ItemStack itemStack = new ItemStack(MalisisDoors.Blocks.mixedBlock, 1);
		itemStack.setTagCompound(writeNBT(new NBTTagCompound(), state1, state2));
		return itemStack;
	}

	public static ItemStack fromTileEntity(MixedBlockTileEntity te)
	{
		ItemStack itemStack = new ItemStack(MalisisDoors.Blocks.mixedBlock, 1);
		itemStack.setTagCompound(writeNBT(new NBTTagCompound(), te.getState1(), te.getState2()));
		return itemStack;
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean advancedTooltip)
	{
		if (itemStack.getTagCompound() == null)
			return;

		Pair<IBlockState, IBlockState> pair = readNBT(itemStack.getTagCompound());

		Item item = itemsAllowed.inverse().get(pair.getLeft());
		ItemStack is1 = item != null ? new ItemStack(item) : ItemUtils.getItemStackFromState(pair.getLeft());
		item = itemsAllowed.inverse().get(pair.getRight());
		ItemStack is2 = item != null ? new ItemStack(item) : ItemUtils.getItemStackFromState(pair.getRight());

		list.addAll(is1.getTooltip(player, advancedTooltip));
		list.addAll(is2.getTooltip(player, advancedTooltip));
	}

	public static Pair<IBlockState, IBlockState> readNBT(NBTTagCompound nbt)
	{
		return new ImmutablePair(MBlockState.fromNBT(nbt, "block1", "metadata1"), MBlockState.fromNBT(nbt, "block2", "metadata2"));
	}

	public static NBTTagCompound writeNBT(NBTTagCompound nbt, IBlockState state1, IBlockState state2)
	{
		if (state1 == null)
			state1 = Blocks.stone.getDefaultState();
		if (state2 == null)
			state2 = Blocks.stone.getDefaultState();

		MBlockState.toNBT(nbt, state1, "block1", "metadata1");
		MBlockState.toNBT(nbt, state2, "block2", "metadata2");

		return nbt;
	}
}
