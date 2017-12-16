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

package net.malisis.doors.tileentity;

import org.apache.commons.lang3.tuple.Pair;

import net.malisis.core.renderer.MalisisRenderer;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.core.util.syncer.Syncable;
import net.malisis.doors.DoorDescriptor;
import net.malisis.doors.DoorRegistry;
import net.malisis.doors.block.FenceGate;
import net.malisis.doors.movement.FenceGateMovement;
import net.malisis.doors.sound.FenceGateSound;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

/**
 * @author Ordinastie
 */
@Syncable("TileEntity")
public class FenceGateTileEntity extends DoorTileEntity
{
	private static DoorDescriptor FENCE_GATE_DESCRIPTOR;

	private IBlockState camoState;
	private int camoColor = -1;
	private boolean isWall = false;

	public FenceGateTileEntity()
	{
		//can't initialize statically because class is loaded from MalisisCore (@Sync) and triggers unavoidable warnings
		if (FENCE_GATE_DESCRIPTOR == null)
		{
			FENCE_GATE_DESCRIPTOR = new DoorDescriptor();
			FENCE_GATE_DESCRIPTOR.setMovement(DoorRegistry.getMovement(FenceGateMovement.class));
			FENCE_GATE_DESCRIPTOR.setSound(DoorRegistry.getSound(FenceGateSound.class));
		}

		setDescriptor(FENCE_GATE_DESCRIPTOR);
		openProperty = BlockFenceGate.OPEN;
	}

	@Override
	public IBlockState getBlockState()
	{
		return world.getBlockState(pos);
	}

	public IBlockState getCamoState()
	{
		return camoState;
	}

	public int getCamoColor()
	{
		return camoColor;
	}

	public boolean isWall()
	{
		return isWall;
	}

	@Override
	public EnumFacing getDirection()
	{
		return getBlockState().getValue(BlockTrapDoor.FACING);
	}

	public void updateAll()
	{
		if (!world.isRemote)
			return;

		Pair<IBlockState, Integer> pair = updateCamo();
		camoState = pair.getLeft();
		camoColor = pair.getRight();
		isWall = updateWall();

		TileEntityUtils.notifyUpdate(this);
	}

	private Pair<IBlockState, Integer> updateCamo()
	{
		EnumFacing dir = getDirection().rotateY();

		BlockPos p = pos.offset(dir);
		FenceGateTileEntity te = TileEntityUtils.getTileEntity(FenceGateTileEntity.class, world, p);
		if (isMatchingDoubleDoor(te))
			p = p.offset(dir);

		IBlockState state1 = world.getBlockState(p);
		int color1 = MalisisRenderer.colorMultiplier(world, p, state1);
		if (state1.getBlock().isAir(state1, world, p))
			return Pair.of(getBlockState(), -1);

		dir = dir.getOpposite();
		p = pos.offset(dir);

		te = TileEntityUtils.getTileEntity(FenceGateTileEntity.class, world, p);
		if (isMatchingDoubleDoor(te))
			p = p.offset(dir);

		IBlockState state2 = world.getBlockState(p);
		int color2 = MalisisRenderer.colorMultiplier(world, p, state2);
		if (state1.getBlock().isAir(state2, world, p))
			return Pair.of(getBlockState(), -1);

		if (!state1.equals(state2) || color1 != color2)
			return Pair.of(getBlockState(), -1);

		return Pair.of(state1, color1);
	}

	private boolean updateWall()
	{
		EnumFacing dir = getDirection().rotateY();
		IBlockState state = world.getBlockState(pos.offset(dir));
		if (state.getBlock() == Blocks.COBBLESTONE_WALL)
			return true;
		state = world.getBlockState(pos.offset(dir.getOpposite()));
		if (state.getBlock() == Blocks.COBBLESTONE_WALL)
			return true;
		return false;
	}

	@Override
	public FenceGateTileEntity getDoubleDoor()
	{
		if (!descriptor.isDoubleDoor())
			return null;

		if (!(getBlockState().getBlock() instanceof FenceGate)) //for some reason, this get called when the block at the position is not a FenceGate
			return null;
		EnumFacing dir = getDirection().rotateY();
		FenceGateTileEntity te = TileEntityUtils.getTileEntity(FenceGateTileEntity.class, world, pos.offset(dir));
		if (te != null && isMatchingDoubleDoor(te))
			return te;

		te = TileEntityUtils.getTileEntity(FenceGateTileEntity.class, world, pos.offset(dir.getOpposite()));
		if (te instanceof FenceGateTileEntity && isMatchingDoubleDoor(te))
			return te;

		return null;
	}

	@Override
	public boolean isMatchingDoubleDoor(DoorTileEntity te)
	{
		if (te == null)
			return false;

		if (getBlockType() != te.getBlockType()) // different block
			return false;

		if (getDirection().getAxis() != te.getDirection().getAxis()) // different direction
			return false;

		//		if (isOpened() != te.isOpened()) // different state
		//			return false;

		return true;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		return new AxisAlignedBB(pos, pos.add(1, 1, 1));
	}
}
