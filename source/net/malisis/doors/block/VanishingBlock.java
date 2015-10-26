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

import java.util.List;
import java.util.Random;

import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.IBoundingBox;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.renderer.icon.provider.PropertyEnumIconProvider;
import net.malisis.core.util.AABBUtils;
import net.malisis.core.util.IMSerializable;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.ProxyAccess;
import net.malisis.doors.entity.VanishingTileEntity;
import net.malisis.doors.item.VanishingBlockItem;
import net.malisis.doors.renderer.VanishingBlockRenderer;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
@MalisisRendered(VanishingBlockRenderer.class)
public class VanishingBlock extends MalisisBlock implements ITileEntityProvider
{
	public enum Type implements IMSerializable
	{
		WOOD, IRON, GOLD, DIAMOND;
	};

	public static final PropertyEnum TYPE = PropertyEnum.create("type", Type.class);
	public static PropertyBool POWERED = PropertyBool.create("powered");
	public static PropertyBool TRANSITION = PropertyBool.create("transition");

	public static int renderId = -1;
	public int renderPass = -1;

	public VanishingBlock()
	{
		super(Material.wood);
		setName("vanishing_block");
		setCreativeTab(MalisisDoors.tab);
		setHardness(0.5F);

		setDefaultState(blockState.getBaseState().withProperty(TYPE, Type.WOOD).withProperty(POWERED, false)
				.withProperty(TRANSITION, false));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void createIconProvider(Object object)
	{
		PropertyEnumIconProvider<Type> ip = new PropertyEnumIconProvider<>(TYPE, Type.class, MalisisDoors.modid
				+ ":blocks/vanishing_block_wood");
		for (Type type : Type.values())
			ip.setIcon(type, MalisisDoors.modid + ":blocks/vanishing_block_" + type.getName().toLowerCase());
		iconProvider = ip;
	}

	@Override
	public Class<? extends ItemBlock> getItemClass()
	{
		return VanishingBlockItem.class;
	}

	@Override
	protected BlockState createBlockState()
	{
		return new BlockState(this, TYPE, POWERED, TRANSITION);
	}

	@Override
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
	{
		return super.getActualState(state, worldIn, pos);
	}

	/**
	 * Check if block at x, y, z is a powered VanishingBlock
	 */
	public boolean isPowered(World world, BlockPos pos)
	{
		return isPowered(world.getBlockState(pos));
	}

	public boolean isPowered(IBlockState state)
	{
		return state.getBlock() == this && (boolean) state.getValue(POWERED);
	}

	public boolean shouldDefer(VanishingTileEntity te)
	{
		return te != null && te.getCopiedState() != null && !te.isPowered() && !te.isInTransition();
	}

	/**
	 * Set the power state for the block at x, y, z
	 */
	public void setPowerState(World world, BlockPos pos, boolean powered)
	{
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() != this) // block is VanishingBlock ?
			return;
		if (isPowered(state) == powered) // same power state?
			return;

		VanishingTileEntity te = TileEntityUtils.getTileEntity(VanishingTileEntity.class, world, pos);
		if (te == null)
			return;

		te.setPowerState(powered);
		world.setBlockState(pos, state.withProperty(POWERED, powered));
		world.scheduleBlockUpdate(pos, this, 1, 0);
	}

	/**
	 * Check if the block is available for propagation of power state
	 */
	public boolean shouldPropagate(World world, BlockPos pos, VanishingTileEntity source)
	{
		IBlockState state = world.getBlockState(pos);
		if (state.getBlock() != this) // block is VanishingBlock ?
			return false;

		Type sourceType = source.getType();
		if (sourceType == Type.WOOD)
			return true;

		VanishingTileEntity dest = TileEntityUtils.getTileEntity(VanishingTileEntity.class, world, pos);
		if (dest == null)
			return false;

		if (source.getCopiedState() == null || dest.getCopiedState() == null)
			return true;

		if (sourceType == Type.IRON && source.getCopiedState().getBlock() == dest.getCopiedState().getBlock())
			return true;

		if (sourceType == Type.GOLD && source.getCopiedState().equals(dest.getCopiedState()))
			return true;

		return false;
	}

	/**
	 * Propagate power state in all six direction
	 */
	public void propagateState(World world, BlockPos pos)
	{
		VanishingTileEntity te = TileEntityUtils.getTileEntity(VanishingTileEntity.class, world, pos);
		for (EnumFacing dir : EnumFacing.values())
		{
			if (shouldPropagate(world, pos.offset(dir), te))
				this.setPowerState(world, pos.offset(dir), te.isPowered());
		}
	}

	// #region Events

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		ItemStack is = player.getHeldItem();
		if (is == null)
			return false;

		VanishingTileEntity te = TileEntityUtils.getTileEntity(VanishingTileEntity.class, world, pos);
		if (te == null || te.getCopiedState() != null)
			return false;

		if (!te.setBlockState(is, player, side, hitX, hitY, hitZ))
			return false;

		if (!player.capabilities.isCreativeMode)
			is.stackSize--;

		world.markBlockForUpdate(pos);
		((World) ProxyAccess.get(world)).notifyNeighborsOfStateChange(pos, te.getCopiedState().getBlock());
		return true;
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block block)
	{
		if (world.isRemote)
			return;

		boolean powered = world.isBlockIndirectlyGettingPowered(pos) != 0;
		if (powered || (block.canProvidePower() && block != this))
		{
			if (isPowered(world, pos) != powered)
				world.playSoundEffect(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, MalisisDoors.modid + ":portal", 0.3F, 0.5F);
			this.setPowerState(world, pos, powered);
		}
	}

	@Override
	public void updateTick(World world, BlockPos pos, IBlockState state, Random rand)
	{
		this.propagateState(world, pos);
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state)
	{
		VanishingTileEntity te = TileEntityUtils.getTileEntity(VanishingTileEntity.class, world, pos);
		if (te != null && te.getCopiedState() != null)
			te.getCopiedState().getBlock().dropBlockAsItem(world, pos, te.getCopiedState(), 0);

		world.removeTileEntity(pos);
	}

	// #end Events

	@Override
	public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		return false;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockAccess world, BlockPos pos, BoundingBoxType type)
	{
		VanishingTileEntity te = TileEntityUtils.getTileEntity(VanishingTileEntity.class, world, pos);
		if (te == null || te.isPowered() || te.isInTransition())
			return null;

		return AABBUtils.identity();
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state)
	{
		VanishingTileEntity te = TileEntityUtils.getTileEntity(VanishingTileEntity.class, world, pos);
		if (!shouldDefer(te))
			return super.getCollisionBoundingBox(world, pos, state);

		return te.getCopiedState().getBlock().getCollisionBoundingBox((World) ProxyAccess.get(world), pos, state);

	}

	@Override
	public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB mask, List list, Entity collidingEntity)
	{
		VanishingTileEntity te = TileEntityUtils.getTileEntity(VanishingTileEntity.class, world, pos);
		if (!shouldDefer(te))
		{
			super.addCollisionBoxesToList(world, pos, state, mask, list, collidingEntity);
			return;
		}

		te.getCopiedState().getBlock().addCollisionBoxesToList((World) ProxyAccess.get(world), pos, state, mask, list, collidingEntity);
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos)
	{
		VanishingTileEntity te = TileEntityUtils.getTileEntity(VanishingTileEntity.class, world, pos);
		if (!shouldDefer(te))
		{
			super.setBlockBoundsBasedOnState(world, pos);
			return;
		}

		te.getCopiedState().getBlock().setBlockBoundsBasedOnState(ProxyAccess.get(world), pos);

	}

	@Override
	public AxisAlignedBB getSelectedBoundingBox(World world, BlockPos pos)
	{
		VanishingTileEntity te = TileEntityUtils.getTileEntity(VanishingTileEntity.class, world, pos);
		if (!shouldDefer(te))
			return super.getSelectedBoundingBox(world, pos);

		return te.getCopiedState().getBlock().getSelectedBoundingBox((World) ProxyAccess.get(world), pos);
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World world, BlockPos pos, Vec3 src, Vec3 dest)
	{
		VanishingTileEntity te = TileEntityUtils.getTileEntity(VanishingTileEntity.class, world, pos);
		if (!shouldDefer(te))
			return super.collisionRayTrace(world, pos, src, dest);

		World proxy = (World) ProxyAccess.get(world);
		//prevent infinite recursion
		if (proxy == world && te.getCopiedState().getBlock() instanceof IBoundingBox)
			return super.collisionRayTrace(world, pos, src, dest);

		return te.getCopiedState().getBlock().collisionRayTrace(proxy, pos, src, dest);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Item getItem(World worldIn, BlockPos pos)
	{
		// VanishingDiamondBlock has its own unused itemBlock, but we don't want it
		return Item.getItemFromBlock(MalisisDoors.Blocks.vanishingBlock);
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List list)
	{
		for (Type type : Type.values())
			list.add(new ItemStack(item, 1, type.ordinal()));
	}

	@Override
	public int damageDropped(IBlockState state)
	{
		return ((Type) state.getValue(TYPE)).ordinal();
	}

	@Override
	public boolean isNormalCube()
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public float getAmbientOcclusionLightValue()
	{
		return 0.9F;
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return getDefaultState().withProperty(TYPE, Type.values()[meta & 3]).withProperty(POWERED, (meta & 8) != 0);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return ((Type) state.getValue(TYPE)).ordinal() + ((boolean) state.getValue(POWERED) ? 8 : 0);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata)
	{
		return new VanishingTileEntity((Type) getStateFromMeta(metadata).getValue(TYPE));
	}

	@Override
	public boolean canRenderInLayer(EnumWorldBlockLayer layer)
	{
		return true;
	}
}
