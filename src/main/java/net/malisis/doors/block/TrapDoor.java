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

import net.malisis.core.MalisisCore;
import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.block.IBoundingBox;
import net.malisis.core.block.IRegisterable;
import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.renderer.icon.IIconProvider;
import net.malisis.core.renderer.icon.IMetaIconProvider;
import net.malisis.core.renderer.icon.provider.DefaultIconProvider;
import net.malisis.core.util.AABBUtils;
import net.malisis.core.util.raytrace.RaytraceBlock;
import net.malisis.doors.DoorDescriptor;
import net.malisis.doors.DoorDescriptor.RedstoneBehavior;
import net.malisis.doors.TrapDoorDescriptor;
import net.malisis.doors.renderer.TrapDoorRenderer;
import net.malisis.doors.tileentity.DoorTileEntity;
import net.malisis.doors.tileentity.TrapDoorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
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
@MalisisRendered(TrapDoorRenderer.class)
public class TrapDoor extends BlockTrapDoor implements ITileEntityProvider, IBoundingBox, IMetaIconProvider, IRegisterable
{
	private TrapDoorDescriptor descriptor;
	@SideOnly(Side.CLIENT)
	private IIconProvider iconProvider;

	public TrapDoor(TrapDoorDescriptor desc)
	{
		super(desc.getMaterial());

		this.descriptor = desc;

		setHardness(desc.getHardness());
		setStepSound(desc.getSoundType());
		setUnlocalizedName(desc.getName());
		setCreativeTab(desc.getTab());

		disableStats();
	}

	public DoorDescriptor getDescriptor()
	{
		return descriptor;
	}

	@Override
	public String getRegistryName()
	{
		return descriptor.getName();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void createIconProvider(Object object)
	{
		iconProvider = DefaultIconProvider.from(descriptor.getModId() + ":" + descriptor.getTextureName());
	}

	@Override
	public IIconProvider getIconProvider()
	{
		return iconProvider;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote)
			return true;

		DoorTileEntity te = Door.getDoor(world, pos);
		if (te == null)
			return true;

		if (te.getDescriptor() == null)
			return true;

		if (te.getDescriptor().getRedstoneBehavior() == RedstoneBehavior.REDSTONE_ONLY)
			return true;

		//Not possible to set redstone behavior for trapdoors
		//		if (te.getDescriptor().getRedstoneBehavior() == RedstoneBehavior.REDSTONE_LOCK && te.isPowered())
		//			return true;

		te.openOrCloseDoor();
		return true;
	}

	@Override
	public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock)
	{
		if (world.isRemote)
			return;

		EnumFacing direction = (EnumFacing) state.getValue(FACING);
		BlockPos blockPos = pos.offset(((EnumFacing) state.getValue(FACING)).getOpposite());

		if (!(isValidSupportBlock(world.getBlockState(blockPos).getBlock()) || world.isSideSolid(blockPos, direction, true)))
		{
			world.setBlockToAir(pos);
			this.dropBlockAsItem(world, pos, state, 0);
			return;
		}

		DoorTileEntity te = Door.getDoor(world, pos);

		//Not possible to set redstone behavior for trapdoors
		//		if (te.getDescriptor().getRedstoneBehavior() == RedstoneBehavior.HAND_ONLY
		//				|| te.getDescriptor().getRedstoneBehavior() == RedstoneBehavior.REDSTONE_LOCK)
		//			return;

		boolean powered = world.isBlockPowered(pos);
		if (powered || neighborBlock.canProvidePower())
		{

			if (te != null)
				te.setPowered(powered);
		}

	}

	private static boolean isValidSupportBlock(Block blockIn)
	{
		if (disableValidation)
			return true;
		return blockIn.getMaterial().isOpaque() && blockIn.isFullCube() || blockIn == Blocks.glowstone || blockIn instanceof BlockSlab
				|| blockIn instanceof BlockStairs;
	}

	//#region BoundingBox

	@Override
	public AxisAlignedBB getBoundingBox(IBlockAccess world, BlockPos pos, BoundingBoxType type)
	{
		DoorTileEntity te = Door.getDoor(world, pos);
		if (te == null || te.isMoving() || te.getMovement() == null)
			return null;

		//TODO: closed BB
		AxisAlignedBB aabb = te.getMovement().getOpenBoundingBox(te, te.isTopBlock(pos), type);
		aabb = AABBUtils.rotate(aabb, te.getDirection());

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

	//#end BoudingBox

	@Override
	public TileEntity createNewTileEntity(World world, int metadata)
	{
		TrapDoorTileEntity te = new TrapDoorTileEntity();
		te.setDescriptor(descriptor);
		return te;
	}

	@Override
	public int getRenderType()
	{
		return MalisisCore.malisisRenderType;
	}
}
