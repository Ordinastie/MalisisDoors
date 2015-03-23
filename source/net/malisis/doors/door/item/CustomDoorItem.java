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

package net.malisis.doors.door.item;

import java.util.HashMap;
import java.util.List;

import net.malisis.doors.MalisisDoors;
import net.malisis.doors.block.MixedBlock;
import net.malisis.doors.door.DoorDescriptor;
import net.malisis.doors.door.tileentity.CustomDoorTileEntity;
import net.malisis.doors.entity.DoorFactoryTileEntity;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
public class CustomDoorItem extends DoorItem
{
	private static HashMap<Item, Block> itemsAllowed = new HashMap<>();
	static
	{
		itemsAllowed.put(Items.flint_and_steel, Blocks.fire);
		itemsAllowed.put(Items.ender_pearl, Blocks.portal);
		itemsAllowed.put(Items.water_bucket, Blocks.water);
		itemsAllowed.put(Items.lava_bucket, Blocks.lava);
	}

	public CustomDoorItem()
	{
		super();
		setUnlocalizedName("custom_door");
		this.maxStackSize = 16;
		setCreativeTab(null);
	}

	@Override
	public DoorDescriptor getDescriptor(ItemStack itemStack)
	{
		return new DoorDescriptor(itemStack.stackTagCompound);
	}

	@Override
	public void registerIcons(IIconRegister par1IconRegister)
	{

	}

	@Override
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float par8, float par9, float par10)
	{
		boolean b = super.onItemUse(itemStack, player, world, x, y, z, side, par8, par9, par10);
		//		if (b)
		//		{
		//			DoorTileEntity te = Door.getDoor(world, x, y + 1, z);
		//			if (te instanceof CustomDoorTileEntity)
		//				((CustomDoorTileEntity) te).onBlockPlaced(itemStack);
		//		}

		return b;
	}

	public static ItemStack fromDoorFactory(DoorFactoryTileEntity te)
	{
		if (te.getDoorMovement() == null || te.getDoorSound() == null)
			return null;

		ItemStack frameItemStack = te.frameSlot.getItemStack();
		ItemStack topMaterialItemStack = te.topMaterialSlot.getItemStack();
		ItemStack bottomMaterialItemStack = te.bottomMaterialSlot.getItemStack();
		if (!canBeUsedForDoor(frameItemStack, true) || !canBeUsedForDoor(topMaterialItemStack, false)
				|| !canBeUsedForDoor(bottomMaterialItemStack, false))
			return null;

		//frame
		Block frameBlock = Block.getBlockFromItem(frameItemStack.getItem());
		int frameMetadata = ((ItemBlock) frameItemStack.getItem()).getMetadata(frameItemStack.getMetadata());

		//top material
		Block topMaterialBlock = itemsAllowed.get(topMaterialItemStack.getItem());
		if (topMaterialBlock == null)
			topMaterialBlock = Block.getBlockFromItem(topMaterialItemStack.getItem());

		int topMaterialMetadata = topMaterialItemStack.getMetadata();
		if (topMaterialItemStack.getItem() instanceof ItemBlock)
			topMaterialMetadata = ((ItemBlock) topMaterialItemStack.getItem()).getMetadata(topMaterialItemStack.getMetadata());

		//bottom material
		Block bottomMaterialBlock = itemsAllowed.get(bottomMaterialItemStack.getItem());
		if (bottomMaterialBlock == null)
			bottomMaterialBlock = Block.getBlockFromItem(bottomMaterialItemStack.getItem());

		int bottomMaterialMetadata = bottomMaterialItemStack.getMetadata();
		if (bottomMaterialItemStack.getItem() instanceof ItemBlock)
			bottomMaterialMetadata = ((ItemBlock) bottomMaterialItemStack.getItem()).getMetadata(bottomMaterialItemStack.getMetadata());

		//NBT
		NBTTagCompound nbt = new NBTTagCompound();

		te.buildDescriptor().writeNBT(nbt);

		nbt.setInteger("frame", Block.getIdFromBlock(frameBlock));
		nbt.setInteger("topMaterial", Block.getIdFromBlock(topMaterialBlock));
		nbt.setInteger("bottomMaterial", Block.getIdFromBlock(bottomMaterialBlock));
		nbt.setInteger("frameMetadata", frameMetadata);
		nbt.setInteger("topMaterialMetadata", topMaterialMetadata);
		nbt.setInteger("bottomMaterialMetadata", bottomMaterialMetadata);

		//ItemStack
		ItemStack is = new ItemStack(MalisisDoors.Items.customDoorItem, 1);
		is.stackTagCompound = nbt;
		return is;
	}

	public static ItemStack fromTileEntity(CustomDoorTileEntity te)
	{
		NBTTagCompound nbt = new NBTTagCompound();

		if (te.getDescriptor() != null)
			te.getDescriptor().writeNBT(nbt);

		nbt.setInteger("frame", Block.getIdFromBlock(te.getFrame()));
		nbt.setInteger("topMaterial", Block.getIdFromBlock(te.getTopMaterial()));
		nbt.setInteger("bottomMaterial", Block.getIdFromBlock(te.getBottomMaterial()));

		nbt.setInteger("frameMetadata", te.getFrameMetadata());
		nbt.setInteger("topMaterialMetadata", te.getTopMaterialMetadata());
		nbt.setInteger("bottomMaterialMetadata", te.getBottomMaterialMetadata());

		ItemStack is = new ItemStack(MalisisDoors.Items.customDoorItem, 1);
		is.stackTagCompound = nbt;
		return is;
	}

	public static boolean canBeUsedForDoor(ItemStack itemStack, boolean frame)
	{
		if (!frame && itemsAllowed.get(itemStack.getItem()) != null)
			return true;

		Block block = Block.getBlockFromItem(itemStack.getItem());
		return !(block instanceof MixedBlock) && block.getRenderType() != -1;
	}

	@Override
	public String getItemStackDisplayName(ItemStack par1ItemStack)
	{
		return super.getItemStackDisplayName(par1ItemStack);
	}

	@Override
	public EnumRarity getRarity(ItemStack par1ItemStack)
	{
		return EnumRarity.rare;
	}

	@Override
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean advancedTooltip)
	{
		if (itemStack.stackTagCompound == null)
			return;

		Block frame = Block.getBlockById(itemStack.stackTagCompound.getInteger("frame"));
		int frameMetadata = itemStack.stackTagCompound.getInteger("frameMetadata");
		ItemStack isFrame = new ItemStack(frame, 0, frameMetadata);

		Block topMaterial = Block.getBlockById(itemStack.stackTagCompound.getInteger("topMaterial"));
		int topMaterialMetadata = itemStack.stackTagCompound.getInteger("topMaterialMetadata");
		ItemStack istopMaterial = new ItemStack(topMaterial, 0, topMaterialMetadata);

		Block bottomMaterial = Block.getBlockById(itemStack.stackTagCompound.getInteger("bottomMaterial"));
		int bottomMaterialMetadata = itemStack.stackTagCompound.getInteger("bottomMaterialMetadata");
		ItemStack isBottomMaterial = new ItemStack(bottomMaterial, 0, bottomMaterialMetadata);

		list.add(EnumChatFormatting.WHITE
				+ StatCollector.translateToLocal("door_movement." + itemStack.stackTagCompound.getString("movement")));
		list.addAll(isFrame.getTooltip(player, advancedTooltip));
		list.addAll(istopMaterial.getTooltip(player, advancedTooltip));
		list.addAll(isBottomMaterial.getTooltip(player, advancedTooltip));
	}
}
