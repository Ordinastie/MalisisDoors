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

import net.malisis.core.renderer.IBaseRendering;
import net.malisis.doors.door.Door;
import net.malisis.doors.door.DoorDescriptor;
import net.malisis.doors.door.DoorRegistry;
import net.malisis.doors.door.movement.TrapDoorMovement;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.malisis.doors.door.tileentity.TrapDoorTileEntity;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
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

	private DoorDescriptor descriptor;

	public TrapDoor()
	{
		super(Material.wood);
		disableStats();
		setHardness(3.0F);
		setStepSound(soundTypeWood);
		setBlockName("trapdoor");
		setBlockTextureName("trapdoor");

		descriptor = new DoorDescriptor();
		descriptor.setMovement(DoorRegistry.getMouvement(TrapDoorMovement.class));
	}

	/**
	 * Called upon block activation (right click on the block.)
	 */
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote)
			return true;

		DoorTileEntity te = Door.getDoor(world, x, y, z);
		if (te == null)
			return true;

		te.openOrCloseDoor();
		return true;
	}

	@Override
	public void func_150120_a(World world, int x, int y, int z, boolean opening)
	{
		DoorTileEntity te = Door.getDoor(world, x, y, z);
		if (te != null)
			te.setPowered(opening);
	}

	//#region BoundingBox
	protected AxisAlignedBB setBlockBounds(AxisAlignedBB aabb)
	{
		if (aabb == null)
			return null;
		setBlockBounds((float) aabb.minX, (float) aabb.minY, (float) aabb.minZ, (float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ);
		return aabb;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		DoorTileEntity te = Door.getDoor(world, x, y, z);
		if (te == null || te.isMoving())
			return;

		setBlockBounds(te.getDescriptor().getMovement().getBoundingBox(te, te.isTopBlock(x, y, z), false));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
	{
		DoorTileEntity te = Door.getDoor(world, x, y, z);
		if (te == null || te.isMoving())
			return AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);

		AxisAlignedBB aabb = te.getDescriptor().getMovement().getBoundingBox(te, te.isTopBlock(x, y, z), true);
		if (aabb == null)
			return AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);

		return aabb.offset(x, y, z);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	{
		DoorTileEntity te = Door.getDoor(world, x, y, z);
		if (te == null || te.isMoving())
			return null;

		AxisAlignedBB aabb = te.getDescriptor().getMovement().getBoundingBox(te, te.isTopBlock(x, y, z), false);
		if (aabb == null)
			return null;
		return setBlockBounds(aabb.offset(x, y, z));
	}

	//#end BoudingBox

	@Override
	public TileEntity createNewTileEntity(World var1, int var2)
	{
		TrapDoorTileEntity te = new TrapDoorTileEntity();
		te.setDescriptor(descriptor);
		return te;
	}

	/**
	 * The type of render function that is called for this block
	 */
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

}
