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

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.base.MoreObjects;

import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.util.ItemUtils;
import net.malisis.core.util.MBlockState;
import net.malisis.doors.DoorDescriptor;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.renderer.CustomDoorRenderer;
import net.malisis.doors.tileentity.DoorFactoryTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.world.World;

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
		itemsAllowed.put(Items.FLINT_AND_STEEL, Blocks.FIRE.getDefaultState());
		itemsAllowed.put(Items.ENDER_PEARL, Blocks.PORTAL.getDefaultState());
		itemsAllowed.put(Items.WATER_BUCKET, Blocks.WATER.getDefaultState());
		itemsAllowed.put(Items.LAVA_BUCKET, Blocks.LAVA.getDefaultState());
	}

	public CustomDoorItem()
	{
		super();
		setUnlocalizedName("custom_door");
		setRegistryName("customdoor");
		this.maxStackSize = 64;
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
		IBlockState top = MoreObjects.firstNonNull(itemsAllowed.get(isTop.getItem()), ItemUtils.getStateFromItemStack(isTop));
		IBlockState bottom = MoreObjects.firstNonNull(itemsAllowed.get(isBottom.getItem()), ItemUtils.getStateFromItemStack(isBottom));

		//NBT
		NBTTagCompound nbt = new NBTTagCompound();
		te.buildDescriptor(MalisisDoors.Blocks.customDoor, MalisisDoors.Items.customDoorItem).writeNBT(nbt);
		writeNBT(nbt, frame, top, bottom);

		//ItemStack
		ItemStack is = new ItemStack(MalisisDoors.Items.customDoorItem, 1);
		is.setTagCompound(nbt);
		return is;
	}

	public static boolean canBeUsedForDoor(ItemStack itemStack, boolean frame)
	{
		if (!frame && itemsAllowed.get(itemStack.getItem()) != null)
			return true;

		IBlockState state = ItemUtils.getStateFromItemStack(itemStack);
		return state != null && !state.getBlock().getUnlocalizedName().equals("tile.mixed_block")
				&& state.getRenderType() == EnumBlockRenderType.MODEL;
	}

	@Override
	public EnumRarity getRarity(ItemStack par1ItemStack)
	{
		return EnumRarity.RARE;
	}

	@Override
	public void addInformation(ItemStack itemStack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag tooltipFlag)
	{
		super.addInformation(itemStack, worldIn, tooltip, tooltipFlag);
		if (itemStack.getTagCompound() == null)
			return;

		Triple<IBlockState, IBlockState, IBlockState> triple = CustomDoorItem.readNBT(itemStack.getTagCompound());
		ItemStack frame = ItemUtils.getItemStackFromState(triple.getLeft());
		ItemStack top = ItemUtils.getItemStackFromState(triple.getMiddle());
		ItemStack bottom = ItemUtils.getItemStackFromState(triple.getRight());

		if (!frame.isEmpty()) //should never happen
			tooltip.addAll(frame.getTooltip(null, tooltipFlag));
		if (!top.isEmpty()) //ex: water
			tooltip.addAll(top.getTooltip(null, tooltipFlag));
		if (!bottom.isEmpty())
			tooltip.addAll(bottom.getTooltip(null, tooltipFlag));
	}

	public static Triple<IBlockState, IBlockState, IBlockState> readNBT(NBTTagCompound nbt)
	{
		IBlockState frame = MBlockState.fromNBT(nbt, "frame", "frameMetadata");
		IBlockState top = MBlockState.fromNBT(nbt, "topMaterial", "topMaterialMetadata");
		IBlockState bottom = MBlockState.fromNBT(nbt, "bottomMaterial", "bottomMaterialMetadata");

		if (frame == null)
			frame = Blocks.PLANKS.getDefaultState();
		if (top == null)
			top = Blocks.GLASS.getDefaultState();
		if (bottom == null)
			bottom = Blocks.GLASS.getDefaultState();

		return new ImmutableTriple<>(frame, top, bottom);
	}

	public static NBTTagCompound writeNBT(NBTTagCompound nbt, IBlockState frame, IBlockState top, IBlockState bottom)
	{
		if (frame == null)
			frame = Blocks.PLANKS.getDefaultState();
		if (top == null)
			top = Blocks.GLASS.getDefaultState();
		if (top == null)
			top = Blocks.GLASS.getDefaultState();

		MBlockState.toNBT(nbt, frame, "frame", "frameMetadata");
		MBlockState.toNBT(nbt, top, "topMaterial", "topMaterialMetadata");
		MBlockState.toNBT(nbt, bottom, "bottomMaterial", "bottomMaterialMetadata");

		return nbt;
	}
}
