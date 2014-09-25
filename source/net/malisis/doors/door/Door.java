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

package net.malisis.doors.door;

import java.util.Random;

import net.malisis.core.renderer.icon.MalisisIcon;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 * 
 */
public class Door extends BlockDoor implements ITileEntityProvider
{
	public static final int DIR_WEST = 0;
	public static final int DIR_NORTH = 1;
	public static final int DIR_EAST = 2;
	public static final int DIR_SOUTH = 3;

	public static final float DOOR_WIDTH = 0.1875F;

	public static final int FLAG_OPENED = 1 << 2;
	public static final int FLAG_TOPBLOCK = 1 << 3;
	public static final int FLAG_REVERSED = 1 << 4;

	protected MalisisIcon iconTop;
	protected MalisisIcon iconBottom;
	protected MalisisIcon iconSide;
	protected String soundPath;

	private DoorDescriptor descriptor;

	public Door(DoorDescriptor desc)
	{
		super(desc.getMaterial());

		this.descriptor = desc;

		setHardness(desc.getHardness());
		setStepSound(desc.getSoundType());
		setBlockName(desc.getName());
		setBlockTextureName(desc.getTextureName());
	}

	public Door()
	{
		super(Material.wood);
	}

	public DoorDescriptor getDescriptor()
	{
		return descriptor;
	}

	// #region Icons
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister register)
	{
		String textureName = getTextureName();
		iconTop = new MalisisIcon(textureName + "_upper").register((TextureMap) register);
		iconBottom = new MalisisIcon(textureName + "_lower").register((TextureMap) register);
		//for the side of vanilla doors, add MalisisDoors: to the name
		if (textureName.equals("door_wood") || textureName.equals("door_iron"))
			textureName = MalisisDoors.modid + ":" + textureName;
		iconSide = new MalisisIcon(textureName + "_side").register((TextureMap) register);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(int side, int metadata)
	{
		boolean topBlock = (metadata & FLAG_TOPBLOCK) != 0;
		boolean reversed = (metadata & FLAG_REVERSED) != 0;
		MalisisIcon icon = iconBottom;

		switch (side)
		{
			case 0:
			case 1:
				icon = iconSide.clone();
				icon.clip(0, 0, 3, 16);
				icon.setRotation(1);
				return icon;
			case 4:
				icon = iconSide.clone();
				icon.clip(topBlock ? 3 : 9, 0, 3, 16);
				return icon;
			case 5:
				icon = iconSide.clone();
				icon.clip(topBlock ? 6 : 12, 0, 3, 16);
				return icon;
			case 2:
				icon = topBlock ? iconTop : iconBottom;
				icon.flip(reversed, false);
				return icon;
			case 3:
				icon = topBlock ? iconTop : iconBottom;
				icon.flip(!reversed, false);
				return icon;
			default:
				return icon;
		}
	}

	// #end

	// #region Events
	/**
	 * Called when right clicked by the player
	 */
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer p, int par6, float par7, float par8, float par9)
	{
		if (world.isRemote)
			return true;

		DoorTileEntity te = getDoor(world, x, y, z);
		if (te == null)
			return true;

		if (te.getDescriptor().requireRedstone())
			return true;

		te.openOrCloseDoor();
		return true;
	}

	/**
	 * Called from villagers AI opening doors
	 */
	@Override
	public void func_150014_a(World world, int x, int y, int z, boolean opening)
	{
		DoorTileEntity te = getDoor(world, x, y, z);
		if (te == null)
			return;

		if (te.getDescriptor().requireRedstone())
			return;

		if (opening && te.isOpened())
			return;

		te.openOrCloseDoor();
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
	{
		int metadata = world.getBlockMetadata(x, y, z);

		if ((metadata & FLAG_TOPBLOCK) == 0)
		{
			boolean flag = false;
			ItemStack itemStack = getDoorItemStack(world, x, y, z);
			if (world.getBlock(x, y + 1, z) != this)
			{
				world.setBlockToAir(x, y, z);
				flag = true;
			}

			if (!World.doesBlockHaveSolidTopSurface(world, x, y - 1, z))
			{
				world.setBlockToAir(x, y, z);
				flag = true;

				if (world.getBlock(x, y + 1, z) == this)
					world.setBlockToAir(x, y + 1, z);
			}

			if (flag)
			{
				if (!world.isRemote && itemStack != null)
					dropBlockAsItem(world, x, y, z, itemStack);
			}
			else
			{
				DoorTileEntity te = getDoor(world, x, y, z);
				if (te == null)
					return;

				boolean powered = te.isPowered();
				if ((powered || block.canProvidePower()) && block != this)
					te.setPowered(powered);
			}
		}
		else
		{
			if (world.getBlock(x, y - 1, z) != this)
				world.setBlockToAir(x, y, z);

			if (block != this)
				onNeighborBlockChange(world, x, y - 1, z, block);
		}
	}

	// #end Events

	protected ItemStack getDoorItemStack(IBlockAccess world, int x, int y, int z)
	{
		return new ItemStack(descriptor.getItem(), 1);
	}

	@Override
	public Item getItemDropped(int metadata, Random random, int fortune)
	{
		return (metadata & FLAG_TOPBLOCK) != 0 ? null : descriptor.getItem();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Item getItem(World p_149694_1_, int p_149694_2_, int p_149694_3_, int p_149694_4_)
	{
		return descriptor.getItem();
	}

	//#region BoundingBox
	protected AxisAlignedBB setBlockBounds(AxisAlignedBB aabb)
	{
		if (aabb == null)
			aabb = AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);

		setBlockBounds((float) aabb.minX, (float) aabb.minY, (float) aabb.minZ, (float) aabb.maxX, (float) aabb.maxY, (float) aabb.maxZ);
		return aabb;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		DoorTileEntity te = getDoor(world, x, y, z);
		if (te == null || te.isMoving() || te.getMovement() == null)
			return;

		setBlockBounds(te.getMovement().getBoundingBox(te, te.isTopBlock(x, y, z), false));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
	{
		DoorTileEntity te = getDoor(world, x, y, z);
		if (te == null || te.isMoving() || te.getMovement() == null)
			return AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);

		AxisAlignedBB aabb = te.getMovement().getBoundingBox(te, te.isTopBlock(x, y, z), true);
		if (aabb == null)
			return AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);

		return aabb.offset(x, y, z);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	{
		DoorTileEntity te = getDoor(world, x, y, z);
		if (te == null || te.isMoving() || te.getMovement() == null)
			return null;

		AxisAlignedBB aabb = te.getMovement().getBoundingBox(te, te.isTopBlock(x, y, z), false);
		if (aabb == null)
			return null;
		return setBlockBounds(aabb.offset(x, y, z));
	}

	//#end BoudingBox

	@Override
	public TileEntity createNewTileEntity(World world, int metadata)
	{
		if ((metadata & FLAG_TOPBLOCK) != 0)
			return null;

		DoorTileEntity te = new DoorTileEntity();
		te.setDescriptor(descriptor);
		return te;
	}

	@Override
	public int getRenderType()
	{
		return -1;
	}

	/**
	 * Get door tile entity at x, y, z event if the position is the top half of the door
	 * 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static DoorTileEntity getDoor(IBlockAccess world, int x, int y, int z)
	{
		Block block = world.getBlock(x, y, z);
		int metadata = getFullMetadata(world, x, y, z);
		if (block instanceof Door)
			y -= (metadata & Door.FLAG_TOPBLOCK) != 0 ? 1 : 0;

		return TileEntityUtils.getTileEntity(DoorTileEntity.class, world, x, y, z);
	}

	/**
	 * Get the full metadata for the door
	 * 
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static int getFullMetadata(IBlockAccess world, int x, int y, int z)
	{
		Block block = world.getBlock(x, y, z);
		int metadata = world.getBlockMetadata(x, y, z);

		if (!(block instanceof BlockDoor))
			return metadata;

		boolean blockTop = (metadata & Door.FLAG_TOPBLOCK) != 0;
		int bottomMetadata;
		int topMetadata;

		if (blockTop)
		{
			bottomMetadata = world.getBlockMetadata(x, y - 1, z);
			topMetadata = metadata;
		}
		else
		{
			bottomMetadata = metadata;
			topMetadata = world.getBlockMetadata(x, y + 1, z);
		}

		boolean flag1 = (topMetadata & 1) != 0;
		return bottomMetadata & 7 | (blockTop ? Door.FLAG_TOPBLOCK : 0) | (flag1 ? Door.FLAG_REVERSED : 0);
	}
}
