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

import java.util.ArrayList;
import java.util.List;

import net.malisis.core.block.IBlockDirectional;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.util.EntityUtils;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.MalisisDoorsSettings;
import net.malisis.doors.entity.MixedBlockTileEntity;
import net.malisis.doors.item.MixedBlockBlockItem;
import net.malisis.doors.renderer.MixedBlockRenderer;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@MalisisRendered(MixedBlockRenderer.class)
public class MixedBlock extends MalisisBlock implements ITileEntityProvider, IBlockDirectional
{
	public MixedBlock()
	{
		super(Material.rock);
		setName("mixed_block");
		setHardness(0.7F);
	}

	@Override
	public Class<? extends ItemBlock> getItemClass()
	{
		return MixedBlockBlockItem.class;
	}

	@Override
	public PropertyDirection getPropertyDirection()
	{
		return IBlockDirectional.ALL;
	}

	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		return getDefaultState().withProperty(IBlockDirectional.ALL, facing.getOpposite());
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack itemStack)
	{
		if (!(itemStack.getItem() instanceof MixedBlockBlockItem))
			return;

		MixedBlockTileEntity te = TileEntityUtils.getTileEntity(MixedBlockTileEntity.class, world, pos);
		if (te == null)
			return;
		te.set(itemStack);

		if (MalisisDoorsSettings.enhancedMixedBlockPlacement.get())
		{
			EnumFacing dir = EntityUtils.getEntityFacing(placer, true);
			if (!placer.isSneaking())
				dir = dir.getOpposite();
			world.setBlockState(pos, state.withProperty(IBlockDirectional.ALL, dir));
		}
		else
			world.notifyBlockOfStateChange(pos, this);
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, BlockPos pos, EntityPlayer player)
	{
		MixedBlockTileEntity te = TileEntityUtils.getTileEntity(MixedBlockTileEntity.class, world, pos);
		if (te == null)
			return null;
		return MixedBlockBlockItem.fromTileEntity(te);
	}

	@Override
	public int getLightValue(IBlockAccess world, BlockPos pos)
	{
		MixedBlockTileEntity te = TileEntityUtils.getTileEntity(MixedBlockTileEntity.class, world, pos);
		if (te == null || te.getState1() == null || te.getState2() == null)
			return 0;

		return Math.max(te.getState1().getBlock().getLightValue(), te.getState2().getBlock().getLightValue());
	}

	@Override
	public boolean canProvidePower()
	{
		return true;
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side)
	{
		MixedBlockTileEntity te = TileEntityUtils.getTileEntity(MixedBlockTileEntity.class, world, pos);
		if (te == null || te.getState1() == null || te.getState2() == null)
			return 0;

		return te.getState1().getBlock() == Blocks.redstone_block || te.getState2().getBlock() == Blocks.redstone_block ? 15 : 0;
	}

	@Override
	public boolean addHitEffects(World world, MovingObjectPosition target, EffectRenderer effectRenderer)
	{
		MixedBlockTileEntity te = (MixedBlockTileEntity) world.getTileEntity(target.getBlockPos());
		if (te == null || te.getState1() == null || te.getState2() == null)
			return true;

		EntityUtils.addHitEffects(world, target, effectRenderer, te.getState1(), te.getState2());

		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean addDestroyEffects(World world, BlockPos pos, EffectRenderer effectRenderer)
	{
		MixedBlockTileEntity te = (MixedBlockTileEntity) world.getTileEntity(pos);
		if (te == null || te.getState1() == null || te.getState2() == null)
			return true;

		EntityUtils.addDestroyEffects(world, pos, effectRenderer, te.getState1(), te.getState2());

		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata)
	{
		return new MixedBlockTileEntity();
	}

	@Override
	public boolean removedByPlayer(World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
	{
		if (!player.capabilities.isCreativeMode)
		{
			MixedBlockTileEntity te = TileEntityUtils.getTileEntity(MixedBlockTileEntity.class, world, pos);
			if (te != null)
				spawnAsEntity(world, pos, MixedBlockBlockItem.fromTileEntity(te));
		}
		return super.removedByPlayer(world, pos, player, willHarvest);
	}

	@Override
	public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
	{
		return new ArrayList<ItemStack>();
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean canRenderInLayer(EnumWorldBlockLayer layer)
	{
		return layer == EnumWorldBlockLayer.TRANSLUCENT;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		if (world.isAirBlock(pos))
			return true;

		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() != this && !(state.getBlock() instanceof BlockBreakable))
			return !state.getBlock().isOpaqueCube();

		MixedBlockTileEntity current = TileEntityUtils.getTileEntity(MixedBlockTileEntity.class, world, pos.offset(side.getOpposite()));
		return current != null && !isOpaque(world, pos) && current.isOpaque();
	}

	public static boolean isOpaque(IBlockAccess world, BlockPos pos)
	{
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof BlockBreakable)
			return true;

		MixedBlockTileEntity te = TileEntityUtils.getTileEntity(MixedBlockTileEntity.class, world, pos);
		return te != null && te.isOpaque();
	}
}
