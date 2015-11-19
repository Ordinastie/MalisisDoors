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

package net.malisis.doors.door.tileentity;

import net.malisis.core.util.BlockPos;
import net.malisis.core.util.BlockState;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.door.DoorDescriptor;
import net.malisis.doors.door.DoorRegistry;
import net.malisis.doors.door.block.Door;
import net.malisis.doors.door.movement.FenceGateMovement;
import net.minecraft.init.Blocks;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.tuple.Pair;

/**
 * @author Ordinastie
 */
public class FenceGateTileEntity extends DoorTileEntity
{
	private BlockState camoState;
	private int camoColor;
	private boolean isWall;

	public FenceGateTileEntity()
	{
		DoorDescriptor descriptor = new DoorDescriptor();
		descriptor.setMovement(DoorRegistry.getMovement(FenceGateMovement.class));
		setDescriptor(descriptor);
	}

	public BlockState getCamoState()
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

	public void updateAll()
	{
		if (!worldObj.isRemote)
			return;

		Pair<BlockState, Integer> pair = updateCamo();
		camoState = pair.getLeft();
		camoColor = pair.getRight();
		isWall = updateWall();

		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	private Pair<BlockState, Integer> updateCamo()
	{
		ForgeDirection dir = getDirection() == 0 || getDirection() == 2 ? ForgeDirection.EAST : ForgeDirection.NORTH;
		BlockPos pos = new BlockPos(xCoord, yCoord, zCoord);

		BlockPos p = pos.offset(dir);
		FenceGateTileEntity te = TileEntityUtils.getTileEntity(FenceGateTileEntity.class, worldObj, p);
		if (te != null && isMatchingDoubleDoor(te))
			p = p.offset(dir);

		BlockState state1 = new BlockState(worldObj, p);
		int color1 = state1.getBlock().colorMultiplier(worldObj, p.getX(), p.getY(), p.getZ());
		if (state1.getBlock().isAir(worldObj, p.getX(), p.getY(), p.getZ()))
			return Pair.of(new BlockState(worldObj, pos), -1);

		dir = dir.getOpposite();
		p = pos.offset(dir);

		te = TileEntityUtils.getTileEntity(FenceGateTileEntity.class, worldObj, p);
		if (te != null && isMatchingDoubleDoor(te))
			p = p.offset(dir);

		BlockState state2 = new BlockState(worldObj, p);
		int color2 = state2.getBlock().colorMultiplier(worldObj, p.getX(), p.getY(), p.getZ());
		if (state1.getBlock().isAir(worldObj, p.getX(), p.getY(), p.getZ()))
			return Pair.of(new BlockState(worldObj, pos), -1);

		if (state1.getBlock() != state2.getBlock() || state1.getMetadata() != state2.getMetadata() || color1 != color2)
			return Pair.of(new BlockState(worldObj, pos), -1);

		return Pair.of(state1, color1);
	}

	private boolean updateWall()
	{
		ForgeDirection dir = getDirection() == Door.DIR_NORTH || getDirection() == Door.DIR_SOUTH ? ForgeDirection.EAST : ForgeDirection.NORTH;
		BlockPos pos = new BlockPos(xCoord, yCoord, zCoord);

		BlockState state = new BlockState(worldObj, pos.offset(dir));
		if (state.getBlock() == Blocks.cobblestone_wall)
			return true;
		state = new BlockState(worldObj, pos.offset(dir.getOpposite()));
		if (state.getBlock() == Blocks.cobblestone_wall)
			return true;
		return false;
	}

	public IIcon getCamoIcon()
	{
		if (camoState == null)
			return getBlockType().getIcon(0, 0);

		return camoState.getBlock().getIcon(0, 0);
	}

	/**
	 * Overriden from DoorTileEntity because metadata doesn't match Door's
	 */
	@Override
	public FenceGateTileEntity getDoubleDoor()
	{
		if (!descriptor.isDoubleDoor())
			return null;

		int dir = getDirection();

		TileEntity te = null;
		if (dir == Door.DIR_NORTH || dir == Door.DIR_SOUTH)
		{
			te = worldObj.getTileEntity(xCoord, yCoord, zCoord + 1);
			if (te instanceof FenceGateTileEntity && isMatchingDoubleDoor((FenceGateTileEntity) te))
				return (FenceGateTileEntity) te;
			te = worldObj.getTileEntity(xCoord, yCoord, zCoord - 1);
			if (te instanceof DoorTileEntity && isMatchingDoubleDoor((DoorTileEntity) te))
				return (FenceGateTileEntity) te;
		}
		else
		{
			te = worldObj.getTileEntity(xCoord + 1, yCoord, zCoord);
			if (te instanceof FenceGateTileEntity && isMatchingDoubleDoor((DoorTileEntity) te))
				return (FenceGateTileEntity) te;
			te = worldObj.getTileEntity(xCoord - 1, yCoord, zCoord);
			if (te instanceof FenceGateTileEntity && isMatchingDoubleDoor((DoorTileEntity) te))
				return (FenceGateTileEntity) te;
		}

		return null;
	}

	/**
	 * Overriden from DoorTileEntity because reverse flag doesn't need to match
	 *
	 * @param te
	 * @return
	 */
	@Override
	public boolean isMatchingDoubleDoor(DoorTileEntity te)
	{
		if (getBlockType() != te.getBlockType()) // different block
			return false;

		if ((getDirection() == Door.DIR_NORTH || getDirection() == Door.DIR_SOUTH) != (te.getDirection() == Door.DIR_NORTH || te
				.getDirection() == Door.DIR_SOUTH)) // different direction
			return false;

		if (getMovement() != te.getMovement()) //different movement type
			return false;

		if ((getBlockMetadata() & Door.FLAG_OPENED) != (te.getBlockMetadata() & Door.FLAG_OPENED)) // different state
			return false;

		return true;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 1, zCoord + 1);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
	{
		super.onDataPacket(net, packet);
		updateAll();
	}
}
