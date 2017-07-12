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
import net.malisis.core.block.IBoundingBox;
import net.malisis.core.block.IComponent;
import net.malisis.core.block.IComponentProvider;
import net.malisis.core.block.IRegisterable;
import net.malisis.core.inventory.MalisisTab;
import net.malisis.core.renderer.MalisisRendered;
import net.malisis.core.renderer.icon.provider.IIconProvider;
import net.malisis.core.util.AABBUtils;
import net.malisis.core.util.raytrace.RaytraceBlock;
import net.malisis.doors.DoorDescriptor;
import net.malisis.doors.DoorDescriptor.RedstoneBehavior;
import net.malisis.doors.TrapDoorDescriptor;
import net.malisis.doors.renderer.TrapDoorRenderer;
import net.malisis.doors.tileentity.DoorTileEntity;
import net.malisis.doors.tileentity.TrapDoorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
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
@MalisisRendered(TrapDoorRenderer.class)
public class TrapDoor extends BlockTrapDoor implements ITileEntityProvider, IBoundingBox, IComponentProvider, IRegisterable
{
	private TrapDoorDescriptor descriptor;
	protected final List<IComponent> components = Lists.newArrayList();

	public TrapDoor(TrapDoorDescriptor desc)
	{
		super(desc.getMaterial());

		this.descriptor = desc;

		setHardness(desc.getHardness());
		setSoundType(desc.getSoundType());
		setUnlocalizedName(desc.getUnlocalizedName());
		setRegistryName(desc.getRegistryName());
		setCreativeTab(desc.getTab());

		disableStats();

		if (MalisisCore.isClient())
			addComponent(IIconProvider.create(descriptor.getModId() + ":" + descriptor.getTextureName()).build());

	}

	@Override
	public TrapDoor setCreativeTab(CreativeTabs tab)
	{
		super.setCreativeTab(tab);
		if (tab instanceof MalisisTab)
			((MalisisTab) tab).addItem(this);
		return this;
	}

	public DoorDescriptor getDescriptor()
	{
		return descriptor;
	}

	@Override
	public String getName()
	{
		return descriptor.getRegistryName();
	}

	@Override
	public void addComponent(IComponent component)
	{
		components.add(component);
	}

	@Override
	public List<IComponent> getComponents()
	{
		return components;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ)
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
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos fromPos)
	{
		if (world.isRemote)
			return;

		//Note : redstone behavior is disabled for trapdoors

		boolean powered = world.isBlockPowered(pos);
		if (powered || neighborBlock.getDefaultState().canProvidePower())
		{
			DoorTileEntity te = Door.getDoor(world, pos);
			if (te != null)
				te.setPowered(powered);
		}

	}

	//#region BoundingBox

	@Override
	public AxisAlignedBB getBoundingBox(IBlockAccess world, BlockPos pos, IBlockState state, BoundingBoxType type)
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
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity entity, boolean useActualState)
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
	public RayTraceResult collisionRayTrace(IBlockState blockState, World world, BlockPos pos, Vec3d start, Vec3d end)
	{
		return new RaytraceBlock(world, start, end, pos).trace();
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
	public EnumBlockRenderType getRenderType(IBlockState state)
	{
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}
}
