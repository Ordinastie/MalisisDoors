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

import static net.malisis.doors.block.DoorHandler.*;
import net.malisis.core.renderer.IBaseRendering;
import net.malisis.doors.entity.TrapDoorTileEntity;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 * 
 */
public class TrapDoor extends BlockTrapDoor implements ITileEntityProvider, IBaseRendering
{
	public static final int DIR_SOUTH = 0;
	public static final int DIR_NORTH = 1;
	public static final int DIR_EAST = 2;
	public static final int DIR_WEST = 3;

	private int renderId = -1;

	public TrapDoor()
	{
		super(Material.wood);
		setHardness(3.0F);
		setStepSound(soundTypeWood);
		setBlockName("trapdoor");
		disableStats();
		setBlockTextureName("trapdoor");
	}

	/**
	 * Called upon block activation (right click on the block.)
	 */
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		boolean opened = (getFullMetadata(world, x, y, z) & flagOpened) != 0;
		setDoorState(world, x, y, z, opened ? stateClosing : stateOpening);
		return true;
	}

	@Override
	public void func_150120_a(World world, int x, int y, int z, boolean opening)
	{
		boolean opened = (getFullMetadata(world, x, y, z) & flagOpened) != 0;
		if (opening && opened)
			return;

		setDoorState(world, x, y, z, opened ? stateClosing : stateOpening);
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		DoorHandler.setBlockBoundsBasedOnState(world, x, y, z, false);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
	{
		if (DoorHandler.setBlockBoundsBasedOnState(world, x, y, z, true))
			return getAABB(world, x, y, z);
		else
			return null;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	{
		if (DoorHandler.setBlockBoundsBasedOnState(world, x, y, z, false))
			return getAABB(world, x, y, z);
		else
			return null;
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 par5Vec3, Vec3 par6Vec3)
	{
		DoorHandler.setBlockBoundsBasedOnState(world, x, y, z, false);
		return super.collisionRayTrace(world, x, y, z, par5Vec3, par6Vec3);
	}

	public AxisAlignedBB getAABB(World world, int x, int y, int z)
	{
		return AxisAlignedBB.getAABBPool()
				.getAABB(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ);
	}

	@Override
	public int getRenderType()
	{
		return renderId;
	}

	@Override
	public void setRenderId(int id)
	{
		renderId = id;
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2)
	{
		return new TrapDoorTileEntity();
	}
}
