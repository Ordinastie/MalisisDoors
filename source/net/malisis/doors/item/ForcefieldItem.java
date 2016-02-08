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

import net.malisis.core.item.MalisisItem;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.malisis.core.renderer.icon.provider.IItemIconProvider;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.core.util.multiblock.AABBMultiBlock;
import net.malisis.core.util.multiblock.MultiBlockComponent;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.block.Forcefield;
import net.malisis.doors.tileentity.ForcefieldTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
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
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void createIconProvider(Object object)
	{
		iconProvider = new ForcefieldItemIconProvider();
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
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (getEnergy(itemStack) < getMaxEnergy())
			return true;

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
		if (!world.canBlockBePlaced(block, pos, false, side, player, itemStack))
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
	public boolean doesSneakBypassUse(World world, BlockPos pos, EntityPlayer player)
	{
		return true;
	}

	protected boolean isStartSet(ItemStack itemStack)
	{
		return getNBT(itemStack).hasKey("start");
	}

	protected boolean setStartPosition(ItemStack itemStack, BlockPos pos, long time)
	{
		getNBT(itemStack).setLong("start", pos.toLong());
		getNBT(itemStack).setLong("time", time);
		return true;
	}

	protected BlockPos getStartPosition(ItemStack itemStack)
	{
		return BlockPos.fromLong(getNBT(itemStack).getLong("start"));
	}

	protected boolean clearStartPosition(ItemStack itemStack)
	{
		getNBT(itemStack).removeTag("start");
		getNBT(itemStack).removeTag("time");
		return true;
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
		protected MalisisIcon itemIcon = new MalisisIcon(MalisisDoors.modid + ":items/forcefielditem");
		protected MalisisIcon yellowIcon = new MalisisIcon(MalisisDoors.modid + ":items/forcefielditem_yellow");
		protected MalisisIcon redIcon = new MalisisIcon(MalisisDoors.modid + ":items/forcefielditem_red");;
		protected MalisisIcon greenIcon = new MalisisIcon(MalisisDoors.modid + ":items/forcefielditem_green");;
		protected MalisisIcon disabledIcon = new MalisisIcon(MalisisDoors.modid + ":items/forcefielditem_disabled");

		@Override
		public void registerIcons(TextureMap map)
		{
			itemIcon = itemIcon.register(map);
			yellowIcon = yellowIcon.register(map);
			redIcon = redIcon.register(map);
			greenIcon = greenIcon.register(map);
			disabledIcon = disabledIcon.register(map);
		}

		@Override
		public MalisisIcon getIcon()
		{
			return itemIcon;
		}

		@Override
		public MalisisIcon getIcon(ItemStack itemStack)
		{
			if (getEnergy(itemStack) < getMaxEnergy())
				return disabledIcon;
			if (!isStartSet(itemStack))
				return itemIcon;

			MovingObjectPosition mop = Minecraft.getMinecraft().objectMouseOver;
			if (mop.typeOfHit != MovingObjectType.BLOCK)
				return yellowIcon;

			BlockPos pos = mop.getBlockPos().offset(mop.sideHit);
			BlockPos start = getStartPosition(itemStack);

			AxisAlignedBB aabb = getBoundingBox(start, pos);
			int size = getDoorSize(aabb);
			if (size <= 0 || getEnergy(itemStack) < size * 20)
				return redIcon;

			return greenIcon;
		}
	}
}
