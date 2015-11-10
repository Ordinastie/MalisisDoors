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

import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.IBlockDirectional;
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.renderer.DefaultRenderer;
import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.malisis.core.renderer.icon.provider.IBlockIconProvider;
import net.malisis.core.util.AABBUtils;
import net.malisis.core.util.RaytraceBlock;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.core.util.multiblock.AABBMultiBlock;
import net.malisis.core.util.multiblock.IMultiBlock;
import net.malisis.core.util.multiblock.MultiBlock;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.renderer.RustyHatchRenderer;
import net.malisis.doors.tileentity.RustyHatchTileEntity;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author Ordinastie
 *
 */
@MalisisRendered(block = RustyHatchRenderer.class, item = DefaultRenderer.Item.class)
public class RustyHatch extends MalisisBlock implements IMultiBlock
{
	private AABBMultiBlock bottomMultiBlock = new AABBMultiBlock(this, new AxisAlignedBB(-1, -2, 0, 1, 1, 2));
	private AABBMultiBlock topMultiBlock = new AABBMultiBlock(this, new AxisAlignedBB(-1, 0, 0, 1, 3, 2));
	public static PropertyBool TOP = PropertyBool.create("top");

	public RustyHatch()
	{
		super(Material.iron);
		setHardness(3.0F);
		setResistance(10000);
		setStepSound(soundTypeMetal);
		setName("rustyHatch");
		setCreativeTab(MalisisDoors.tab);

		bottomMultiBlock.setBulkProcess(true, true);
		topMultiBlock.setBulkProcess(true, true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void createIconProvider(Object object)
	{
		iconProvider = new RustyHatchIconProvider();
	}

	@Override
	protected BlockState createBlockState()
	{
		return new BlockState(this, IBlockDirectional.HORIZONTAL, IMultiBlock.ORIGIN, TOP);
	}

	@Override
	public MultiBlock getMultiBlock(IBlockAccess world, BlockPos pos, IBlockState state, ItemStack itemStack)
	{
		return (boolean) state.getValue(TOP) ? topMultiBlock : bottomMultiBlock;
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, BlockPos pos, EnumFacing side)
	{
		if (side == EnumFacing.UP || side == EnumFacing.DOWN)
			return false;

		return world.getBlockState(pos.offset(side.getOpposite())).getBlock().isSideSolid(world, pos, side);
	}

	@Override
	public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		return getDefaultState().withProperty(ORIGIN, true).withProperty(HORIZONTAL, facing).withProperty(TOP, hitY > 0.5F);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumFacing side, float hitX, float hitY, float hitZ)
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
	public AxisAlignedBB getBoundingBox(IBlockAccess world, BlockPos pos, BoundingBoxType type)
	{
		RustyHatchTileEntity te = getRustyHatch(world, pos);
		if (te == null || te.getMovement() == null)
			return AABBUtils.identity();
		if (te.isMoving())
			return null;

		AxisAlignedBB aabb = te.isOpened() ? te.getMovement().getOpenBoundingBox(te, te.isTop(), type) : te.getMovement()
				.getClosedBoundingBox(te, te.isTop(), type);

		if (aabb == null)
			return null;

		//rotate before origin offset
		aabb = AABBUtils.rotate(aabb, te.getDirection());

		pos = pos.subtract(te.getPos());
		aabb = aabb.offset(-pos.getX(), -pos.getY(), -pos.getZ());

		return aabb;
	}

	@Override
	public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB mask, List list, Entity collidingEntity)
	{
		AxisAlignedBB[] aabbs = getBoundingBoxes(world, pos, BoundingBoxType.COLLISION);
		for (AxisAlignedBB aabb : AABBUtils.offset(pos, aabbs))
		{
			if (aabb != null && mask.intersectsWith(aabb))
				list.add(aabb);
		}
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBox(World world, BlockPos pos)
	{
		AxisAlignedBB[] aabbs = getBoundingBoxes(world, pos, BoundingBoxType.SELECTION);
		if (ArrayUtils.isEmpty(aabbs) || aabbs[0] == null)
			return AABBUtils.empty(pos);

		return AABBUtils.offset(pos, aabbs)[0];
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World world, BlockPos pos, Vec3 src, Vec3 dest)
	{
		return new RaytraceBlock(world, src, dest, pos).trace();
	}

	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return IMultiBlock.isOrigin(state);
	}

	@Override
	public TileEntity createTileEntity(World world, IBlockState state)
	{
		return IMultiBlock.isOrigin(state) ? new RustyHatchTileEntity() : null;
	}

	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return super.getStateFromMeta(meta).withProperty(TOP, (meta & 4) != 0);
	}

	@Override
	public int getMetaFromState(IBlockState state)
	{
		return super.getMetaFromState(state) + ((boolean) state.getValue(TOP) ? 4 : 0);
	}

	@Override
	public boolean isLadder(IBlockAccess world, BlockPos pos, EntityLivingBase entity)
	{
		RustyHatchTileEntity te = getRustyHatch(world, pos);
		if (te == null)
			return false;

		return te.shouldLadder(pos);
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

	public static RustyHatchTileEntity getRustyHatch(IBlockAccess world, BlockPos pos)
	{
		BlockPos origin = MultiBlock.getOrigin(world, pos);
		return TileEntityUtils.getTileEntity(RustyHatchTileEntity.class, world, origin != null ? origin : pos);
	}

	public static class RustyHatchIconProvider implements IBlockIconProvider
	{
		private MalisisIcon hatchIcon = new MalisisIcon(MalisisDoors.modid + ":blocks/rusty_hatch");
		private MalisisIcon handleIcon = new MalisisIcon(MalisisDoors.modid + ":blocks/rusty_hatch_handle");
		private MalisisIcon hatchItemIcon = new MalisisIcon(MalisisDoors.modid + ":items/rusty_hatch_item");

		@Override
		public void registerIcons(TextureMap map)
		{
			hatchIcon = hatchIcon.register(map);
			handleIcon = handleIcon.register(map);
			hatchItemIcon = hatchItemIcon.register(map);
		}

		@Override
		public MalisisIcon getIcon()
		{
			return hatchItemIcon;
		}

		public MalisisIcon getHandleIcon()
		{
			return handleIcon;
		}

		public MalisisIcon getHatchIcon()
		{
			return hatchIcon;
		}

	}
}
