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
import net.malisis.core.block.MalisisBlock;
import net.malisis.core.util.AABBUtils;
import net.malisis.core.util.BlockPos;
import net.malisis.core.util.BlockState;
import net.malisis.core.util.EntityUtils;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.core.util.chunkcollision.ChunkCollision;
import net.malisis.core.util.chunkcollision.IChunkCollidable;
import net.malisis.core.util.chunklistener.IBlockListener;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.door.tileentity.CarriageDoorTileEntity;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @author Ordinastie
 *
 */
public class CarriageDoor extends MalisisBlock implements ITileEntityProvider, IChunkCollidable, IBlockListener
{
	protected IIcon frameIcon;
	public static int renderId;

	private AxisAlignedBB defaultBoundingBox = AxisAlignedBB.getBoundingBox(0, 0, 1 - Door.DOOR_WIDTH, 4, 5, 1);

	public CarriageDoor()
	{
		super(Material.wood);
		setHardness(5.0F);
		setResistance(10.0F);
		setStepSound(soundTypeStone);
		setUnlocalizedName("carriage_door");
		setCreativeTab(MalisisDoors.tab);
	}

	@Override
	public void registerIcons(IIconRegister register)
	{
		blockIcon = register.registerIcon(MalisisDoors.modid + ":carriage_door");
		frameIcon = register.registerIcon(MalisisDoors.modid + ":carriage_frame");
	}

	public IIcon getFrameIcon()
	{
		return frameIcon;
	}

	@Override
	public String getItemIconName()
	{
		return MalisisDoors.modid + ":carriage_item";
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
		ForgeDirection dir = EntityUtils.getEntityFacing(player);
		int metadata = Door.dirToInt(dir);
		world.setBlockMetadataWithNotify(x, y, z, metadata, 2);

		ChunkCollision.get().replaceBlocks(world, new BlockState(world, x, y, z));
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote)
			return true;

		CarriageDoorTileEntity te = TileEntityUtils.getTileEntity(CarriageDoorTileEntity.class, world, x, y, z);
		if (te == null)
			return true;

		te.openOrCloseDoor();
		return true;
	}

	@Override
	public AxisAlignedBB[] getPlacedBoundingBox(IBlockAccess world, int x, int y, int z, int side, EntityPlayer player, ItemStack itemStack)
	{
		ForgeDirection dir = EntityUtils.getEntityFacing(player);
		return AABBUtils.rotate(new AxisAlignedBB[] { defaultBoundingBox.copy() }, dir);
	}

	@Override
	public AxisAlignedBB[] getBoundingBox(IBlockAccess world, int x, int y, int z, BoundingBoxType type)
	{
		CarriageDoorTileEntity te = TileEntityUtils.getTileEntity(CarriageDoorTileEntity.class, world, x, y, z);
		if (te == null)
			return AABBUtils.identities();

		//MalisisCore.message(te.getDirection());

		AxisAlignedBB[] aabbs = new AxisAlignedBB[] { defaultBoundingBox.copy() };
		if (type == BoundingBoxType.RENDER)
		{
			aabbs[0].minZ = -.5F;
		}
		else if ((type == BoundingBoxType.COLLISION || type == BoundingBoxType.CHUNKCOLLISION || type == BoundingBoxType.RAYTRACE)
				&& (te.isOpened() || te.isMoving()))
		{
			aabbs = new AxisAlignedBB[] { AxisAlignedBB.getBoundingBox(0, 0, -0.5F, 0.5F, 4, 1),
					AxisAlignedBB.getBoundingBox(3.5F, 0, -0.5F, 4, 4, 1), AxisAlignedBB.getBoundingBox(0, 4, 1 - Door.DOOR_WIDTH, 4, 5, 1) };
		}

		return AABBUtils.rotate(aabbs, Door.intToDir(te.getDirection()));
	}

	@Override
	public int blockRange()
	{
		return 5;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata)
	{
		return new CarriageDoorTileEntity();
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

	@Override
	public boolean onBlockSet(World world, BlockPos pos, BlockState state)
	{
		if (!state.getBlock().isReplaceable(world, state.getX(), state.getY(), state.getZ()))
			return true;

		for (AxisAlignedBB aabb : AABBUtils.getCollisionBoundingBoxes(world, new BlockState(pos, this), true))
		{
			if (state.getPos().isInside(aabb))
				return false;
		}

		return true;
	}

	@Override
	public boolean onBlockRemoved(World world, BlockPos pos, BlockPos blockPos)
	{
		return true;
	}
}
