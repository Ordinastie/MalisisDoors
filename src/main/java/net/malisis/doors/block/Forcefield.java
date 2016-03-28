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
import net.malisis.core.renderer.DefaultRenderer;
import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.util.AABBUtils;
import net.malisis.core.util.EntityUtils;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.core.util.multiblock.AABBMultiBlock;
import net.malisis.core.util.multiblock.MultiBlock;
import net.malisis.core.util.multiblock.MultiBlockComponent;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.MalisisDoors.Items;
import net.malisis.doors.tileentity.ForcefieldTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
@MalisisRendered(DefaultRenderer.Null.class)
public class Forcefield extends MalisisBlock
{

	public Forcefield()
	{
		super(Material.anvil);
		setResistance(60000000);
		setBlockUnbreakable();
		setSoundType(SoundType.ANVIL);
		setName("forcefieldDoor");
		setTexture(MalisisDoors.modid + ":blocks/forcefield");

		addComponent(new MultiBlockComponent(this::getMultiBlock));
	}

	@Override
	public Item getItem(Block block)
	{
		return null;
	}

	public AABBMultiBlock getMultiBlock(IBlockAccess world, BlockPos pos, IBlockState state, ItemStack itemStack)
	{
		ForcefieldTileEntity te = getForcefield(world, pos);
		return te != null ? te.getMultiBlock() : null;
	}

	//@Override
	public EnumFacing getPlacingDirection(EnumFacing side, EntityLivingBase placer)
	{
		return EnumFacing.SOUTH;
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side)
	{
		return true;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (!EntityUtils.isEquipped(player, MalisisDoors.Items.forcefieldItem, hand))
			return true;

		ForcefieldTileEntity te = getForcefield(world, pos);
		if (te == null)
			return true;

		if (player.isSneaking())
		{
			te.getMultiBlock().breakBlocks(world, pos, getDefaultState());
			MalisisDoors.Items.forcefieldItem.setEnergy(player.getHeldItem(hand), 0);
		}
		else
			te.switchForcefield();

		return true;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockAccess world, BlockPos pos, IBlockState state, BoundingBoxType type)
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
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
	{
		MultiBlock multiBlock = MultiBlockComponent.getMultiBlock(world, pos, world.getBlockState(pos), null);
		if (multiBlock != null)
			multiBlock.breakBlocks(world, pos, getDefaultState()); //use default state because no rotation
		world.setBlockToAir(pos);
		return true;
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return MultiBlockComponent.isOrigin(state);
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		return MultiBlockComponent.isOrigin(state) ? new ForcefieldTileEntity() : null;
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
	{
		return new ItemStack(Items.forcefieldItem);
	}

	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state)
	{
		return false;
	}

	public static ForcefieldTileEntity getForcefield(IBlockAccess world, BlockPos pos)
	{
		BlockPos origin = MultiBlock.getOrigin(world, pos);
		return TileEntityUtils.getTileEntity(ForcefieldTileEntity.class, world, origin != null ? origin : pos);
	}

}
