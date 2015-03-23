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

package net.malisis.doors.door.block;

import net.malisis.core.block.BoundingBoxType;
import net.malisis.core.util.EntityUtils;
import net.malisis.core.util.MultiBlock;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.MalisisDoors.Items;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.malisis.doors.door.tileentity.ForcefieldTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
public class ForcefieldDoor extends Block implements ITileEntityProvider
{
	public static int renderId = -1;

	public ForcefieldDoor()
	{
		super(Material.anvil);
		setResistance(60000000);
		setBlockUnbreakable();
		setStepSound(soundTypePiston);
		setUnlocalizedName("forcefieldDoor");
		setTextureName(MalisisDoors.modid + ":forcefield");
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side)
	{
		if (side != 1)
			return false;

		ForgeDirection dir = ForgeDirection.getOrientation(side).getOpposite();
		return world.getBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ).isSideSolid(world, x, y, z, dir);
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack)
	{
		ForgeDirection side = EntityUtils.getEntityFacing(player);
		AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(0, 0, 0, 3, 3, 1);
		MultiBlock mb = new MultiBlock(world, x, y, z);
		mb.setDirection(side);
		mb.setBounds(aabb);
		if (!mb.placeBlocks())
			itemStack.stackSize++;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote)
			return true;

		if (!EntityUtils.isEquipped(player, MalisisDoors.Items.forcefieldItem))
			return true;

		ForcefieldTileEntity te = TileEntityUtils.getTileEntity(ForcefieldTileEntity.class, world, x, y, z);
		if (te == null)
			return true;

		if (player.isSneaking())
		{
			MultiBlock.destroy(world, x, y, z);
			MalisisDoors.Items.forcefieldItem.setEnergy(player.getCurrentEquippedItem(), 0);
		}
		else
			te.openOrCloseDoor();

		return true;
	}

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
		if (te == null || te.getMovement() == null)
		{
			setBlockBounds(AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1));
			return;
		}
		if (te.isMoving())
			return;

		AxisAlignedBB aabb = te.getMovement().getBoundingBox(te, false, BoundingBoxType.RAYTRACE);
		if (aabb == null)
			aabb = AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);
		//aabb = AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1);
		aabb.offset(-x, -y, -z);
		setBlockBounds(aabb);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
	{
		DoorTileEntity te = Door.getDoor(world, x, y, z);
		if (te == null || te.getMovement() == null)
			return AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1);
		if (te.isMoving())
			return AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);

		AxisAlignedBB aabb = te.getMovement().getBoundingBox(te, false, BoundingBoxType.SELECTION);
		if (aabb == null)
			return AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);

		return aabb;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	{
		DoorTileEntity te = Door.getDoor(world, x, y, z);
		if (te == null || te.getMovement() == null)
			return AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1);
		if (te.isMoving())
			return null;

		AxisAlignedBB aabb = te.getMovement().getBoundingBox(te, false, BoundingBoxType.COLLISION);
		if (aabb == null)
			return null;

		return setBlockBounds(aabb);
	}

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z)
	{
		MultiBlock.destroy(world, x, y, z);
		world.setBlockToAir(x, y, z);
		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_)
	{
		return new ForcefieldTileEntity();
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		return new ItemStack(Items.forcefieldItem);
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return false;
	}

	@Override
	public int getRenderType()
	{
		return renderId;
	}

}
