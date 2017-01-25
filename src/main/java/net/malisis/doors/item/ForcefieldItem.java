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

import net.malisis.core.MalisisCore;
import net.malisis.core.item.MalisisItem;
import net.malisis.core.renderer.icon.Icon;
import net.malisis.core.renderer.icon.provider.IIconProvider;
import net.malisis.core.renderer.icon.provider.IItemIconProvider;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.core.util.multiblock.AABBMultiBlock;
import net.malisis.core.util.multiblock.MultiBlockComponent;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.block.Forcefield;
import net.malisis.doors.tileentity.ForcefieldTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
public class ForcefieldItem extends MalisisItem
{
	public static AxisAlignedBB lastAABB;

	public ForcefieldItem()
	{
		setName("forcefieldItem");
		setCreativeTab(MalisisDoors.tab);
		setMaxDamage(0);

		if (MalisisCore.isClient())
			addComponent(getIconProvider());
	}

	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider()
	{
		return new ForcefieldItemIconProvider();
	}

	public int getEnergy(ItemStack itemStack)
	{
		return getNBT(itemStack).getInteger("energy");
	}

	public void setEnergy(ItemStack itemStack, int energy)
	{
		if (energy < 0)
			energy = 0;
		else if (energy > getMaxEnergy())
			energy = getMaxEnergy();
		getNBT(itemStack).setInteger("energy", energy);
	}

	public void drainEnergy(ItemStack itemStack, int energy, long time)
	{
		setEnergy(itemStack, getEnergy(itemStack) - energy);
	}

	protected int getMaxEnergy()
	{
		return 2000;
	}

	protected NBTTagCompound getNBT(ItemStack itemStack)
	{
		if (itemStack.getTagCompound() == null)
			itemStack.setTagCompound(new NBTTagCompound());

		return itemStack.getTagCompound();
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		ItemStack itemStack = player.getHeldItem(hand);
		if (getEnergy(itemStack) < getMaxEnergy())
			return EnumActionResult.FAIL;

		pos = pos.offset(side);
		if (!isStartSet(itemStack))
			return setStartPosition(itemStack, pos, world.getTotalWorldTime());

		BlockPos start = getStartPosition(itemStack);
		AxisAlignedBB aabb = getBoundingBox(start, pos);
		pos = new BlockPos(aabb.minX, aabb.minY, aabb.minZ);

		int size = getDoorSize(aabb);
		if (size <= 0 || getEnergy(itemStack) < size * 20)
			return clearStartPosition(itemStack);

		if (!player.canPlayerEdit(pos, side, itemStack))
			return clearStartPosition(itemStack);

		Forcefield block = MalisisDoors.Blocks.forcefieldDoor;
		if (!world.mayPlace(block, pos, false, side, player))
			return clearStartPosition(itemStack);

		IBlockState state = block.getDefaultState().withProperty(MultiBlockComponent.ORIGIN, true);
		if (!world.setBlockState(pos, state, 3))
			return clearStartPosition(itemStack);

		//make sure the right state was set
		state = world.getBlockState(pos);
		if (state.getBlock() != block)
			return clearStartPosition(itemStack);

		AABBMultiBlock multiBlock = new AABBMultiBlock(block, aabb.offset(-pos.getX(), -pos.getY(), -pos.getZ()));
		multiBlock.setBulkProcess(true, true);
		ForcefieldTileEntity te = TileEntityUtils.getTileEntity(ForcefieldTileEntity.class, world, pos);
		if (te != null)
			te.setMultiBlock(multiBlock);

		//place the multiblock
		block.onBlockPlacedBy(world, pos, state.withProperty(MultiBlockComponent.ORIGIN, false), player, itemStack);
		//TODO: forcefield sound ?
		//		world.playSoundEffect(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, block.stepSound.getPlaceSound(),
		//				(block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getFrequency() * 0.8F);
		drainEnergy(itemStack, size * 20, world.getTotalWorldTime());

		return clearStartPosition(itemStack);
	}

	private static AxisAlignedBB getBoundingBox(BlockPos start, BlockPos end)
	{
		return new AxisAlignedBB(start, end).offset(.5F, .5F, .5F).expand(.5F, .5F, .5F);
	}

	@Override
	public boolean doesSneakBypassUse(ItemStack stack, IBlockAccess world, BlockPos pos, EntityPlayer player)
	{
		return true;
	}

	protected boolean isStartSet(ItemStack itemStack)
	{
		return getNBT(itemStack).hasKey("start");
	}

	protected EnumActionResult setStartPosition(ItemStack itemStack, BlockPos pos, long time)
	{
		getNBT(itemStack).setLong("start", pos.toLong());
		getNBT(itemStack).setLong("time", time);
		return EnumActionResult.SUCCESS;
	}

	protected BlockPos getStartPosition(ItemStack itemStack)
	{
		return BlockPos.fromLong(getNBT(itemStack).getLong("start"));
	}

	protected EnumActionResult clearStartPosition(ItemStack itemStack)
	{
		getNBT(itemStack).removeTag("start");
		getNBT(itemStack).removeTag("time");
		return EnumActionResult.SUCCESS;
	}

	protected int getDoorSize(AxisAlignedBB aabb)
	{
		int diffX = (int) (aabb.maxX - aabb.minX);
		int diffY = (int) (aabb.maxY - aabb.minY);
		int diffZ = (int) (aabb.maxZ - aabb.minZ);

		if (diffX == 1 && diffZ != 1)
			return diffY * diffZ;
		else if (diffY == 1)
			return diffX * diffZ;
		else if (diffZ == 1 && diffX != 1)
			return diffX * diffY;
		else
			return -1;
	}

	@Override
	public void onUpdate(ItemStack itemStack, World world, Entity entity, int p_77663_4_, boolean p_77663_5_)
	{
		if (world.isRemote)
			return;

		if (isStartSet(itemStack))
		{
			if (world.getTotalWorldTime() - getNBT(itemStack).getLong("time") > 100)
				clearStartPosition(itemStack);
		}

		if (getEnergy(itemStack) >= getMaxEnergy())
			return;

		int energy = getEnergy(itemStack) + 1;
		if (((EntityPlayer) entity).capabilities.isCreativeMode)
			energy += 19;
		setEnergy(itemStack, energy);
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack)
	{
		return true;
	}

	@Override
	public double getDurabilityForDisplay(ItemStack itemStack)
	{
		return 1 - ((double) getEnergy(itemStack) / getMaxEnergy());
	}

	@Override
	public int getItemStackLimit(ItemStack stack)
	{
		return 1;
	}

	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
	{
		return oldStack == null || newStack == null || oldStack.getItem() != newStack.getItem();
	}

	@SideOnly(Side.CLIENT)
	public class ForcefieldItemIconProvider implements IItemIconProvider
	{
		protected Icon itemIcon = Icon.from(MalisisDoors.modid + ":items/forcefielditem");
		protected Icon yellowIcon = Icon.from(MalisisDoors.modid + ":items/forcefielditem_yellow");
		protected Icon redIcon = Icon.from(MalisisDoors.modid + ":items/forcefielditem_red");;
		protected Icon greenIcon = Icon.from(MalisisDoors.modid + ":items/forcefielditem_green");;
		protected Icon disabledIcon = Icon.from(MalisisDoors.modid + ":items/forcefielditem_disabled");

		@Override
		public Icon getIcon()
		{
			return itemIcon;
		}

		@Override
		public Icon getIcon(ItemStack itemStack)
		{
			if (getEnergy(itemStack) < getMaxEnergy())
				return disabledIcon;
			if (!isStartSet(itemStack))
				return itemIcon;

			RayTraceResult result = Minecraft.getMinecraft().objectMouseOver;
			if (result.typeOfHit != RayTraceResult.Type.BLOCK)
				return yellowIcon;

			BlockPos pos = result.getBlockPos().offset(result.sideHit);
			BlockPos start = getStartPosition(itemStack);

			AxisAlignedBB aabb = getBoundingBox(start, pos);
			int size = getDoorSize(aabb);
			if (size <= 0 || getEnergy(itemStack) < size * 20)
				return redIcon;

			return greenIcon;
		}
	}
}
