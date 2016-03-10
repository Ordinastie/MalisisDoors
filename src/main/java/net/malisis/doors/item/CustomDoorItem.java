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

import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.renderer.icon.IIconProvider;
import net.malisis.core.util.ItemUtils;
import net.malisis.core.util.MBlockState;
import net.malisis.doors.DoorDescriptor;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.renderer.CustomDoorRenderer;
import net.malisis.doors.tileentity.CustomDoorTileEntity;
import net.malisis.doors.tileentity.DoorFactoryTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.base.Objects;

/**
 * @author Ordinastie
 *
 */
@MalisisRendered(CustomDoorRenderer.class)
public class CustomDoorItem extends DoorItem
{
	private static HashMap<Item, IBlockState> itemsAllowed = new HashMap<>();
	static
	{
		itemsAllowed.put(Items.flint_and_steel, Blocks.fire.getDefaultState());
		itemsAllowed.put(Items.ender_pearl, Blocks.portal.getDefaultState());
		itemsAllowed.put(Items.water_bucket, Blocks.water.getDefaultState());
		itemsAllowed.put(Items.lava_bucket, Blocks.lava.getDefaultState());
	}

	public CustomDoorItem()
	{
		super();
		setUnlocalizedName("customDoorItem");
		this.maxStackSize = 16;
		setCreativeTab(null);
	}

	@Override
	public DoorDescriptor getDescriptor(ItemStack itemStack)
	{
		return new DoorDescriptor(itemStack.getTagCompound());
	}

	@Override
	public String getName()
	{
		return "customDoorItem";
	}

	@Override
	public IIconProvider getIconProvider()
	{
		return null;
	}

	public static ItemStack fromDoorFactory(DoorFactoryTileEntity te)
	{
		if (te.getDoorMovement() == null || te.getDoorSound() == null)
			return null;

		ItemStack isFrame = te.frameSlot.getItemStack();
		ItemStack isTop = te.topMaterialSlot.getItemStack();
		ItemStack isBottom = te.bottomMaterialSlot.getItemStack();
		if (!canBeUsedForDoor(isFrame, true) || !canBeUsedForDoor(isTop, false) || !canBeUsedForDoor(isBottom, false))
			return null;

		IBlockState frame = ItemUtils.getStateFromItemStack(isFrame);
		IBlockState top = Objects.firstNonNull(itemsAllowed.get(isTop.getItem()), ItemUtils.getStateFromItemStack(isTop));
		IBlockState bottom = Objects.firstNonNull(itemsAllowed.get(isBottom.getItem()), ItemUtils.getStateFromItemStack(isBottom));

		//NBT
		NBTTagCompound nbt = new NBTTagCompound();
		te.buildDescriptor().writeNBT(nbt);
		writeNBT(nbt, frame, top, bottom);

		//ItemStack
		ItemStack is = new ItemStack(MalisisDoors.Items.customDoorItem, 1);
		is.setTagCompound(nbt);
		return is;
	}

	public static ItemStack fromTileEntity(CustomDoorTileEntity te)
	{
		NBTTagCompound nbt = new NBTTagCompound();
		if (te.getDescriptor() != null)
			te.getDescriptor().writeNBT(nbt);
		else
			new DoorDescriptor().writeNBT(nbt);

		writeNBT(nbt, te.getFrame(), te.getTop(), te.getBottom());
		ItemStack is = new ItemStack(MalisisDoors.Items.customDoorItem, 1);
		is.setTagCompound(nbt);
		return is;
	}

	public static boolean canBeUsedForDoor(ItemStack itemStack, boolean frame)
	{
		if (!frame && itemsAllowed.get(itemStack.getItem()) != null)
			return true;

		Block block = Block.getBlockFromItem(itemStack.getItem());
		return block != null && !block.getUnlocalizedName().equals("tile.mixed_block") && block.getRenderType() != -1;
	}

	@Override
	public EnumRarity getRarity(ItemStack par1ItemStack)
	{
		return EnumRarity.RARE;
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List<String> tooltip, boolean advancedTooltip)
	{
		super.addInformation(itemStack, player, tooltip, advancedTooltip);
		if (itemStack.getTagCompound() == null)
			return;

		Triple<IBlockState, IBlockState, IBlockState> triple = CustomDoorItem.readNBT(itemStack.getTagCompound());
		ItemStack frame = ItemUtils.getItemStackFromState(triple.getLeft());
		ItemStack top = ItemUtils.getItemStackFromState(triple.getMiddle());
		ItemStack bottom = ItemUtils.getItemStackFromState(triple.getRight());

		tooltip.addAll(frame.getTooltip(player, advancedTooltip));
		tooltip.addAll(top.getTooltip(player, advancedTooltip));
		tooltip.addAll(bottom.getTooltip(player, advancedTooltip));
	}

	public static Triple<IBlockState, IBlockState, IBlockState> readNBT(NBTTagCompound nbt)
	{
		IBlockState frame = MBlockState.fromNBT(nbt, "frame", "frameMetadata");
		IBlockState top = MBlockState.fromNBT(nbt, "topMaterial", "topMaterialMetadata");
		IBlockState bottom = MBlockState.fromNBT(nbt, "bottomMaterial", "bottomMaterialMetadata");

		if (frame == null)
			frame = Blocks.planks.getDefaultState();
		if (top == null)
			top = Blocks.glass.getDefaultState();
		if (bottom == null)
			bottom = Blocks.glass.getDefaultState();

		return new ImmutableTriple<>(frame, top, bottom);
	}

	public static NBTTagCompound writeNBT(NBTTagCompound nbt, IBlockState frame, IBlockState top, IBlockState bottom)
	{
		if (frame == null)
			frame = Blocks.planks.getDefaultState();
		if (top == null)
			top = Blocks.glass.getDefaultState();
		if (top == null)
			top = Blocks.glass.getDefaultState();

		MBlockState.toNBT(nbt, frame, "frame", "frameMetadata");
		MBlockState.toNBT(nbt, top, "topMaterial", "topMaterialMetadata");
		MBlockState.toNBT(nbt, bottom, "bottomMaterial", "bottomMaterialMetadata");

		return nbt;
	}
}
