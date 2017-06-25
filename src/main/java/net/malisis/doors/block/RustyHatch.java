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

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.Lists;

import net.malisis.core.MalisisCore;
import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.renderer.DefaultRenderer;
import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.renderer.icon.Icon;
import net.malisis.core.renderer.icon.provider.IIconProvider;
import net.malisis.core.util.AABBUtils;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.core.util.multiblock.AABBMultiBlock;
import net.malisis.core.util.multiblock.MultiBlock;
import net.malisis.core.util.multiblock.MultiBlockComponent;
import net.malisis.core.util.raytrace.RaytraceBlock;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.renderer.RustyHatchRenderer;
import net.malisis.doors.tileentity.RustyHatchTileEntity;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

/**
 * @author Ordinastie
 *
 */
@MalisisRendered(block = RustyHatchRenderer.class, item = DefaultRenderer.Item.class)
public class RustyHatch extends MalisisBlock
{
	private AABBMultiBlock bottomMultiBlock = new AABBMultiBlock(this, new AxisAlignedBB(-1, -2, 0, 1, 1, 2));
	private AABBMultiBlock topMultiBlock = new AABBMultiBlock(this, new AxisAlignedBB(-1, 0, 0, 1, 3, 2));
	public static PropertyBool TOP = PropertyBool.create("top");

	public RustyHatch()
	{
		super(Material.IRON);
		setHardness(3.0F);
		setResistance(10000);
		setSoundType(SoundType.METAL);
		setName("rustyHatch");
		setCreativeTab(MalisisDoors.tab);

		bottomMultiBlock.setBulkProcess(true, true);
		topMultiBlock.setBulkProcess(true, true);

		addComponent(new MultiBlockComponent((world, pos, state, itemStack) -> isTop(state) ? topMultiBlock : bottomMultiBlock));

		if (MalisisCore.isClient())
			addComponent(RustyHatchIconProvider.get());

	}

	@Override
	protected List<IProperty<?>> getProperties()
	{
		return Lists.newArrayList(TOP);
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side)
	{
		if (side == EnumFacing.UP || side == EnumFacing.DOWN)
			return false;

		pos = pos.offset(side.getOpposite());
		return world.getBlockState(pos).isSideSolid(world, pos, side);
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
	{
		return super.getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer, hand).withProperty(TOP, hitY > 0.5F);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote)
			return true;

		RustyHatchTileEntity te = getRustyHatch(world, pos);
		if (te == null)
			return true;

		te.openOrCloseDoor();
		return true;
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockAccess world, BlockPos pos, IBlockState state, BoundingBoxType type)
	{
		RustyHatchTileEntity te = getRustyHatch(world, pos);
		if (te == null || te.getMovement() == null)
			return AABBUtils.identity();
		if (te.isMoving())
			return null;

		AxisAlignedBB aabb = te.isOpened()	? te.getMovement().getOpenBoundingBox(te, te.isTop(), type)
											: te.getMovement().getClosedBoundingBox(te, te.isTop(), type);

		if (aabb == null)
			return null;

		//rotate before origin offset
		aabb = AABBUtils.rotate(aabb, te.getDirection());
		//returned AABB expected to be relative to this blockPos, but it's relative to TE's pos, so wee need to offset
		aabb = aabb.offset(te.getPos().subtract(pos));

		return aabb;
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity, boolean useActualState)
	{
		AxisAlignedBB[] aabbs = getBoundingBoxes(world, pos, state, BoundingBoxType.COLLISION);
		for (AxisAlignedBB aabb : AABBUtils.offset(pos, aabbs))
		{
			if (aabb != null && mask.intersects(aabb))
				list.add(aabb);
		}
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World world, BlockPos pos)
	{
		AxisAlignedBB[] aabbs = getBoundingBoxes(world, pos, state, BoundingBoxType.SELECTION);
		if (ArrayUtils.isEmpty(aabbs) || aabbs[0] == null)
			return AABBUtils.empty(pos);

		return AABBUtils.offset(pos, aabbs)[0];
	}

	@Override
	public AxisAlignedBB[] getRayTraceBoundingBox(IBlockAccess world, BlockPos pos, IBlockState state)
	{
		AxisAlignedBB[] aabbs = getBoundingBoxes(world, pos, state, BoundingBoxType.RAYTRACE);
		return aabbs;
	}

	@Override
	public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d src, Vec3d dest)
	{
		return new RaytraceBlock(world, src, dest, pos).trace();
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return MultiBlockComponent.isOrigin(state);
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		return MultiBlockComponent.isOrigin(state) ? new RustyHatchTileEntity() : null;
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return super.getStateFromMeta(meta).withProperty(TOP, (meta & 4) != 0);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return super.getMetaFromState(state) + (state.getValue(TOP) ? 4 : 0);
	}

	@Override
	public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity)
	{
		RustyHatchTileEntity te = getRustyHatch(world, pos);
		if (te == null)
			return false;

		return te.shouldLadder(pos);
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

	public static boolean isTop(IBlockState state)
	{
		return state.getValue(TOP);
	}

	public static RustyHatchTileEntity getRustyHatch(IBlockAccess world, BlockPos pos)
	{
		BlockPos origin = MultiBlock.getOrigin(world, pos);
		return TileEntityUtils.getTileEntity(RustyHatchTileEntity.class, world, origin != null ? origin : pos);
	}

	public static class RustyHatchIconProvider implements IIconProvider
	{
		private Icon hatchIcon = Icon.from(MalisisDoors.modid + ":blocks/rusty_hatch");
		private Icon handleIcon = Icon.from(MalisisDoors.modid + ":blocks/rusty_hatch_handle");
		private Icon hatchItemIcon = Icon.from(MalisisDoors.modid + ":items/rusty_hatch_item");

		@Override
		public Icon getIcon()
		{
			return hatchItemIcon;
		}

		public Icon getHandleIcon()
		{
			return handleIcon;
		}

		public Icon getHatchIcon()
		{
			return hatchIcon;
		}

		public static RustyHatchIconProvider get()
		{
			return new RustyHatchIconProvider();
		}

	}
}
