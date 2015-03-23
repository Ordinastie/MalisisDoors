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

import net.malisis.core.util.MultiBlock;
import net.malisis.doors.MalisisDoors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @author Ordinastie
 *
 */
public class ForcefieldItem extends Item
{
	protected IIcon yellowIcon;
	protected IIcon redIcon;
	protected IIcon greenIcon;
	protected IIcon disabledIcon;

	public ForcefieldItem()
	{
		setUnlocalizedName("forcefieldItem");
		setCreativeTab(MalisisDoors.tab);
		setMaxDurability(0);
	}

	@Override
	public void registerIcons(IIconRegister register)
	{
		itemIcon = register.registerIcon(MalisisDoors.modid + ":forcefielditem");
		yellowIcon = register.registerIcon(MalisisDoors.modid + ":forcefielditem_yellow");
		redIcon = register.registerIcon(MalisisDoors.modid + ":forcefielditem_red");
		greenIcon = register.registerIcon(MalisisDoors.modid + ":forcefielditem_green");
		disabledIcon = register.registerIcon(MalisisDoors.modid + ":forcefielditem_disabled");
	}

	@Override
	public IIcon getIcon(ItemStack itemStack, int pass)
	{
		if (getEnergy(itemStack) < getMaxEnergy())
			return disabledIcon;
		if (!isStartSet(itemStack))
			return itemIcon;

		MovingObjectPosition mop = Minecraft.getMinecraft().objectMouseOver;
		if (mop.typeOfHit != MovingObjectType.BLOCK)
			return yellowIcon;

		ForgeDirection dir = ForgeDirection.getOrientation(mop.sideHit);
		ChunkPosition pos = new ChunkPosition(mop.blockX + dir.offsetX, mop.blockY + dir.offsetY, mop.blockZ + dir.offsetZ);
		ChunkPosition start = getStartPosition(itemStack);
		if (start.chunkPosY > pos.chunkPosY)
		{
			ChunkPosition tmp = start;
			start = pos;
			pos = tmp;
		}

		AxisAlignedBB aabb = getBoundingBox(start, pos);
		if (aabb.minX < aabb.maxX - 1 && aabb.minY < aabb.maxY - 1 && aabb.minZ < aabb.maxZ - 1)
			return redIcon;

		int size = getDoorSize(aabb);
		if (size <= 0 || getEnergy(itemStack) < size * 20)
			return redIcon;

		return greenIcon;

	}

	@Override
	public IIcon getIconIndex(ItemStack itemStack)
	{
		return getIcon(itemStack, 0);
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
			energy = getMaxDurability();
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
	public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		if (getEnergy(itemStack) < getMaxEnergy())
			return true;

		ForgeDirection dir = ForgeDirection.getOrientation(side);
		ChunkPosition pos = new ChunkPosition(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);
		if (!isStartSet(itemStack))
		{
			setStartPosition(itemStack, pos, world.getTotalWorldTime());
			return true;
		}

		ChunkPosition start = getStartPosition(itemStack);
		if (start.chunkPosY > pos.chunkPosY)
		{
			ChunkPosition tmp = start;
			start = pos;
			pos = tmp;
		}

		AxisAlignedBB aabb = getBoundingBox(start, pos);
		if (aabb.minX < aabb.maxX - 1 && aabb.minY < aabb.maxY - 1 && aabb.minZ < aabb.maxZ - 1)
		{
			clearStartPosition(itemStack);
			return true;
		}

		int size = getDoorSize(aabb);
		if (size <= 0 || getEnergy(itemStack) < size * 20)
		{
			clearStartPosition(itemStack);
			return true;
		}

		MultiBlock multiBlock = new MultiBlock(world, (int) aabb.minX, (int) aabb.minY, (int) aabb.minZ);
		multiBlock.setBlock(MalisisDoors.Blocks.forcefieldDoor);
		multiBlock.setBounds(aabb.offset(-aabb.minX, -aabb.minY, -aabb.minZ));
		multiBlock.setDirection(ForgeDirection.UNKNOWN);
		if (multiBlock.placeBlocks(true))
			drainEnergy(itemStack, size * 20, world.getTotalWorldTime());

		clearStartPosition(itemStack);
		return true;
	}

	@Override
	public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player)
	{
		return true;
	}

	protected boolean isStartSet(ItemStack itemStack)
	{
		return getNBT(itemStack).hasKey("x");
	}

	protected void setStartPosition(ItemStack itemStack, ChunkPosition position, long time)
	{
		getNBT(itemStack).setInteger("x", position.chunkPosX);
		getNBT(itemStack).setInteger("y", position.chunkPosY);
		getNBT(itemStack).setInteger("z", position.chunkPosZ);
		getNBT(itemStack).setLong("time", time);
	}

	protected ChunkPosition getStartPosition(ItemStack itemStack)
	{
		return new ChunkPosition(getNBT(itemStack).getInteger("x"), getNBT(itemStack).getInteger("y"), getNBT(itemStack).getInteger("z"));
	}

	protected void clearStartPosition(ItemStack itemStack)
	{
		getNBT(itemStack).removeTag("x");
		getNBT(itemStack).removeTag("y");
		getNBT(itemStack).removeTag("z");
		getNBT(itemStack).removeTag("time");
	}

	protected AxisAlignedBB getBoundingBox(ChunkPosition start, ChunkPosition end)
	{
		int minX, minY, minZ, maxX, maxY, maxZ;
		minX = Math.min(start.chunkPosX, end.chunkPosX);
		maxX = Math.max(start.chunkPosX, end.chunkPosX);
		minY = Math.min(start.chunkPosY, end.chunkPosY);
		maxY = Math.max(start.chunkPosY, end.chunkPosY);
		minZ = Math.min(start.chunkPosZ, end.chunkPosZ);
		maxZ = Math.max(start.chunkPosZ, end.chunkPosZ);

		return AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
	}

	protected int getDoorSize(AxisAlignedBB aabb)
	{
		int diffX = (int) (aabb.maxX - aabb.minX);
		int diffY = (int) (aabb.maxY - aabb.minY);
		int diffZ = (int) (aabb.maxZ - aabb.minZ);

		if (diffY != 1 && diffX == 1 && diffZ == 1)
			return -1;

		return diffX * diffY * diffZ;
	}

	protected ForgeDirection getOrientation(ChunkPosition start, ChunkPosition end)
	{
		//East/west
		if (start.chunkPosX == end.chunkPosX)
			return start.chunkPosZ < end.chunkPosZ ? ForgeDirection.EAST : ForgeDirection.WEST;
		else if (start.chunkPosZ == end.chunkPosZ)
			return start.chunkPosX < end.chunkPosX ? ForgeDirection.NORTH : ForgeDirection.SOUTH;
		else
			return ForgeDirection.UNKNOWN;
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
	public int getMaxItemUseDuration(ItemStack itemStack)
	{
		return 1;
	}
}
