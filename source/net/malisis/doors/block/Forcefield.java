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

package net.malisis.doors.block;

import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.util.AABBUtils;
import net.malisis.core.util.EntityUtils;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.core.util.multiblock.AABBMultiBlock;
import net.malisis.core.util.multiblock.IMultiBlock;
import net.malisis.core.util.multiblock.MultiBlock;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.MalisisDoors.Items;
import net.malisis.doors.tileentity.ForcefieldTileEntity;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
public class Forcefield extends MalisisBlock implements IMultiBlock
{

	public Forcefield()
	{
		super(Material.anvil);
		setResistance(60000000);
		setBlockUnbreakable();
		setStepSound(soundTypePiston);
		setName("forcefieldDoor");
		setTexture(MalisisDoors.modid + ":blocks/forcefield");
	}

	@Override
	public Class<? extends ItemBlock> getItemClass()
	{
		return null;
	}

	@Override
	public EnumFacing getPlacingDirection(EnumFacing side, EntityLivingBase placer)
	{
		return EnumFacing.SOUTH;
	}

	@Override
	public AABBMultiBlock getMultiBlock(IBlockAccess world, BlockPos pos, IBlockState state, ItemStack itemStack)
	{
		ForcefieldTileEntity te = getForcefield(world, pos);
		return te != null ? te.getMultiBlock() : null;
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side)
	{
		return true;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (!EntityUtils.isEquipped(player, MalisisDoors.Items.forcefieldItem))
			return true;

		ForcefieldTileEntity te = getForcefield(world, pos);
		if (te == null)
			return true;

		if (player.isSneaking())
		{
			te.getMultiBlock().breakBlocks(world, pos, getDefaultState());
			MalisisDoors.Items.forcefieldItem.setEnergy(player.getCurrentEquippedItem(), 0);
		}
		else
			te.switchForcefield();

		return true;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockAccess world, BlockPos pos, BoundingBoxType type)
	{
		ForcefieldTileEntity te = getForcefield(world, pos);
		if (te == null || te.getMultiBlock() == null || type == BoundingBoxType.RAYTRACE)
			return AABBUtils.identity();

		//MalisisCore.message(world.getClass() + " > " + te.isOpened());
		if (te.isOpened() && type == BoundingBoxType.COLLISION)
			return null;

		AxisAlignedBB aabb = te.getMultiBlock().getRelativeBoundingBox(pos, te.getPos());

		if (aabb.maxY - aabb.minY == 1)
			aabb = new AxisAlignedBB(aabb.minX, 0.5F, aabb.minZ, aabb.maxX, 0.5F, aabb.maxZ);
		else if (aabb.maxX - aabb.minX == 1)
			aabb = new AxisAlignedBB(0.5F, aabb.minY, aabb.minZ, 0.5F, aabb.maxY, aabb.maxZ);
		else
			aabb = new AxisAlignedBB(aabb.minX, aabb.minY, 0.5F, aabb.maxX, aabb.maxY, 0.5F);

		return aabb;
	}

	@Override
	public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
	{
		ForcefieldTileEntity te = TileEntityUtils.getTileEntity(ForcefieldTileEntity.class, world, pos);
		if (te == null || te.getMultiBlock() == null)
			world.setBlockToAir(pos);
		else
			te.getMultiBlock().breakBlocks(world, pos, getDefaultState());
		return true;
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return IMultiBlock.isOrigin(state);
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		return IMultiBlock.isOrigin(state) ? new ForcefieldTileEntity() : null;
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos, EntityPlayer player)
	{
		return new ItemStack(Items.forcefieldItem);
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean isFullCube()
	{
		return false;
	}

	@Override
	public int getRenderType()
	{
		return -1;
	}

	public static ForcefieldTileEntity getForcefield(IBlockAccess world, BlockPos pos)
	{
		BlockPos origin = MultiBlock.getOrigin(world, pos);
		return TileEntityUtils.getTileEntity(ForcefieldTileEntity.class, world, origin != null ? origin : pos);
	}

}
