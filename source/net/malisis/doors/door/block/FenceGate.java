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

/**
 * @author Ordinastie
 *
 */

import static net.malisis.doors.MalisisDoors.Blocks.*;
import net.malisis.core.renderer.icon.MalisisIcon;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.malisis.doors.door.tileentity.FenceGateTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import cpw.mods.fml.common.registry.GameRegistry;

public class FenceGate extends BlockFenceGate implements ITileEntityProvider
{
	public static enum Type
	{
		//{"oak", "spruce", "birch", "jungle", "acacia", "big_oak"};
		//@formatter:off
		OAK("fenceGate", 0),
		ACACIA("acaciaFenceGate", 4),
		BIRCH("birchFenceGate", 2),
		DARK_OAK("darkOakFenceGate", 5),
		JUNGLE("jungleFenceGate", 3),
		SPRUCE("spruceFenceGate", 1),
		CAMO("camoFenceGate", 0);

		//@formatter:on
		private int type;
		private String name;

		private Type(String name, int type)
		{
			this.name = name;
			this.type = type;
		}
	}

	private Type type;
	private IIcon camoIcon;
	public static int renderId = -1;

	public FenceGate(Type type)
	{
		this.type = type;
		setHardness(2.0F);
		setResistance(5.0F);
		setStepSound(soundTypeWood);
		setUnlocalizedName(type.name);

		if (type != Type.OAK)
			setCreativeTab(MalisisDoors.tab);
	}

	public FenceGate register()
	{
		GameRegistry.registerBlock(this, type.name);
		if (type == Type.CAMO)
			GameRegistry.addRecipe(new ItemStack(this), "ABC", 'A', acaciaFenceGate, 'B', jungleFenceGate, 'C', birchFenceGate);
		else
			GameRegistry.addRecipe(new ItemStack(this), "ABA", "ABA", 'A', Items.stick, 'B', new ItemStack(Blocks.planks, 1, type.type));
		return this;
	}

	@Override
	public void registerIcons(IIconRegister register)
	{
		if (type == Type.CAMO)
			camoIcon = new MalisisIcon(MalisisDoors.modid + ":camo_fencegate").register((TextureMap) register);
	}

	@Override
	public IIcon getIcon(int side, int meta)
	{
		return type == Type.CAMO ? camoIcon : Blocks.planks.getIcon(side, type.type);
	}

	@Override
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side)
	{
		if (type != Type.CAMO)
			return super.getIcon(world, x, y, z, side);

		FenceGateTileEntity te = TileEntityUtils.getTileEntity(FenceGateTileEntity.class, world, x, y, z);
		if (te == null)
			return super.getIcon(world, x, y, z, side);

		return te.getCamoIcon();
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack)
	{
		super.onBlockPlacedBy(world, x, y, z, player, itemStack);
		if (world.isRemote)
			return;

		FenceGateTileEntity te = TileEntityUtils.getTileEntity(FenceGateTileEntity.class, world, x, y, z);
		if (te == null)
			return;

		te.updateAll();
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

		boolean opened = te.isOpened();

		te.openOrCloseDoor();
		if (opened)
			return true;

		int dir = (MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3) % 4;
		if (dir == ((world.getBlockMetadata(x, y, z) & 3) + 2) % 4)
			world.setBlockMetadataWithNotify(x, y, z, dir, 2);

		te = te.getDoubleDoor();
		if (te != null)
		{
			if (dir == ((world.getBlockMetadata(te.xCoord, te.yCoord, te.zCoord) & 3) + 2) % 4)
				world.setBlockMetadataWithNotify(te.xCoord, te.yCoord, te.zCoord, dir, 2);
		}

		return true;
	}

	/**
	 * Lets the block know when one of its neighbor changes. Doesn't know which neighbor changed (coordinates passed are their own) Args: x,
	 * y, z, neighbor Block
	 */
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
	{
		FenceGateTileEntity te = TileEntityUtils.getTileEntity(FenceGateTileEntity.class, world, x, y, z);
		if (te == null)
			return;

		te.updateAll();

		if (world.isBlockIndirectlyGettingPowered(x, y, z) || block.canProvidePower())
			te.setPowered(te.isPowered());
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	{
		DoorTileEntity te = Door.getDoor(world, x, y, z);
		if (te == null || te.isMoving() || te.isOpened())
			return null;

		return super.getCollisionBoundingBoxFromPool(world, x, y, z);
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2)
	{
		return new FenceGateTileEntity();
	}

	/**
	 * The type of render function that is called for this block
	 */
	@Override
	public int getRenderType()
	{
		return renderId;
	}

}
