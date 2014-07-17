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

package net.malisis.doors.block.doors;

import static net.malisis.doors.block.doors.DoorHandler.*;
import net.malisis.core.renderer.MalisisIcon;
import net.malisis.core.renderer.TextureIcon;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.entity.DoorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class Door extends BlockDoor implements ITileEntityProvider
{
	protected MalisisIcon iconTop;
	protected MalisisIcon iconBottom;
	protected MalisisIcon iconSide;
	protected String soundPath;

	public static final int openingTime = 6;

	public Door(Material material)
	{
		super(material);
		disableStats();
		this.isBlockContainer = true;
		// wood
		if (material == Material.wood)
		{
			setHardness(3.0F);
			setStepSound(soundTypeWood);
			setBlockName("doorWood");
			setBlockTextureName("door_wood");
		}
		else
		{
			setHardness(5.0F);
			setStepSound(soundTypeMetal);
			setBlockName("doorIron");
			setBlockTextureName("door_iron");
		}
	}

	// #region Icons
	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister register)
	{
		String textureName = getTextureName();
		iconTop = new TextureIcon((TextureAtlasSprite) register.registerIcon(textureName + "_upper"));
		iconBottom = new TextureIcon((TextureAtlasSprite) register.registerIcon(textureName + "_lower"));
		if (textureName.equals("door_wood") || textureName.equals("door_iron"))
			textureName = MalisisDoors.modid + ":" + textureName;
		iconSide = new TextureIcon((TextureAtlasSprite) register.registerIcon(textureName + "_side"));
	}

	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(int side, int metadata)
	{
		boolean topBlock = (metadata & flagTopBlock) != 0;
		boolean reversed = (metadata & flagReversed) != 0;
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
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer p, int par6, float par7, float par8, float par9)
	{
		if (blockMaterial == Material.iron)
			return false;

		if (world.isRemote)
			return true;

		boolean opened = (getFullMetadata(world, x, y, z) & flagOpened) != 0;
		openDoubleDoor(world, x, y, z, opened ? stateClosing : stateOpening);
		return true;
	}

	/**
	 * Call from villagers AI opening doors
	 */
	@Override
	public void func_150014_a(World world, int x, int y, int z, boolean opening)
	{
		boolean opened = (getFullMetadata(world, x, y, z) & flagOpened) != 0;
		if (opening && opened)
			return;

		openDoubleDoor(world, x, y, z, opened ? stateClosing : stateOpening);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
	{
		int metadata = world.getBlockMetadata(x, y, z);

		if ((metadata & flagTopBlock) == 0)
		{
			boolean flag = false;

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
				if (!world.isRemote)
					dropBlockAsItem(world, x, y, z, metadata, 0);
			}
			else
			{
				boolean flag1 = world.isBlockIndirectlyGettingPowered(x, y, z) || world.isBlockIndirectlyGettingPowered(x, y + 1, z);

				if ((flag1 || block.canProvidePower()) && block != this)
					onPoweredBlockChange(world, x, y, z, flag1);
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
		return AxisAlignedBB.getBoundingBox(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ);
	}

	@Override
	public int getRenderType()
	{
		return -1;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata)
	{
		return (metadata & flagTopBlock) == 0 ? new DoorTileEntity() : null;
	}
}
