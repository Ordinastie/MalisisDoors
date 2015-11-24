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
import java.util.Random;

import net.malisis.core.block.IBoundingBox;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.ProxyAccess;
import net.malisis.doors.entity.VanishingTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * @author Ordinastie
 *
 */
public class VanishingBlock extends BlockContainer
{
	public static final int typeWoodFrame = 0;
	public static final int typeIronFrame = 1;
	public static final int typeGoldFrame = 2;
	public static final int typeDiamondFrame = 3;

	public static final int flagPowered = 1 << 2;
	public static final int flagInTransition = 1 << 3;

	private static IIcon[] icons = new IIcon[4];

	public static int renderId = -1;
	public int renderPass = -1;

	public VanishingBlock()
	{
		super(Material.wood);
		setUnlocalizedName("vanishing_block");
		setCreativeTab(MalisisDoors.tab);
		setHardness(0.5F);
	}

	// #region Icons
	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister register)
	{
		icons[typeWoodFrame] = register.registerIcon(MalisisDoors.modid + ":vanishing_block_wood");
		icons[typeIronFrame] = register.registerIcon(MalisisDoors.modid + ":vanishing_block_iron");
		icons[typeGoldFrame] = register.registerIcon(MalisisDoors.modid + ":vanishing_block_gold");
		icons[typeDiamondFrame] = register.registerIcon(MalisisDoors.modid + ":vanishing_block_diamond");
	}

	@Override
	public IIcon getIcon(int side, int frameType)
	{
		return icons[frameType & 3];
	}

	// #end Icons

	/**
	 * Check if block at x, y, z is a powered VanishingBlock
	 */
	public boolean isPowered(World world, int x, int y, int z)
	{
		return world.getBlock(x, y, z) == this && (world.getBlockMetadata(x, y, z) & flagPowered) != 0;
	}

	/**
	 * Set the power state for the block at x, y, z
	 */
	public void setPowerState(World world, int x, int y, int z, boolean powered)
	{
		if (world.getBlock(x, y, z) != this) // block is VanishingBlock ?
			return;
		if (isPowered(world, x, y, z) == powered) // same power state?
			return;

		VanishingTileEntity te = TileEntityUtils.getTileEntity(VanishingTileEntity.class, world, x, y, z);
		if (te == null)
			return;
		te.setPowerState(powered);

		if (powered)
			world.setBlockMetadataWithNotify(x, y, z, te.blockMetadata | flagPowered, 2);
		else
			world.setBlockMetadataWithNotify(x, y, z, te.blockMetadata & ~flagPowered, 2);

		world.scheduleBlockUpdate(x, y, z, this, 1);
	}

	/**
	 * Check if the block is available for propagation of power state
	 */
	public boolean shouldPropagate(World world, int x, int y, int z, VanishingTileEntity source)
	{
		if (world.getBlock(x, y, z) != this) // block is VanishingBlock ?
			return false;

		if ((source.getBlockMetadata() & 3) == typeWoodFrame)
			return true;

		VanishingTileEntity dest = TileEntityUtils.getTileEntity(VanishingTileEntity.class, world, x, y, z);
		if (dest == null)
			return false;

		if (source.copiedBlock == null || dest.copiedBlock == null)
			return true;

		if ((source.getBlockMetadata() & 3) == typeIronFrame && source.copiedBlock == dest.copiedBlock)
			return true;

		if ((source.getBlockMetadata() & 3) == typeGoldFrame && source.copiedBlock == dest.copiedBlock
				&& source.copiedMetadata == dest.copiedMetadata)
			return true;

		return false;
	}

	/**
	 * Propagate power state in all six direction
	 */
	public void propagateState(World world, int x, int y, int z)
	{
		VanishingTileEntity te = TileEntityUtils.getTileEntity(VanishingTileEntity.class, world, x, y, z);
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			if (shouldPropagate(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, te))
				this.setPowerState(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, te.powered);
		}
	}

	// #region Events
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer p, int side, float hitX, float hitY, float hitZ)
	{
		ItemStack is = p.getHeldItem();
		if (is == null)
			return false;

		VanishingTileEntity te = TileEntityUtils.getTileEntity(VanishingTileEntity.class, world, x, y, z);
		if (te == null || te.copiedBlock != null)
			return false;

		if (!te.setBlock(is, p, side, hitX, hitY, hitZ))
			return false;

		if (!p.capabilities.isCreativeMode)
			is.stackSize--;

		world.markBlockForUpdate(x, y, z);
		((World) ProxyAccess.get(world)).notifyBlocksOfNeighborChange(x, y, z, te.copiedBlock);
		return true;
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
	{
		if (world.isRemote)
			return;

		boolean powered = world.isBlockIndirectlyGettingPowered(x, y, z);
		if (powered || (block.canProvidePower() && block != this))
		{
			if (isPowered(world, x, y, z) != powered)
				world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, MalisisDoors.modid + ":portal", 0.3F, 0.5F);
			this.setPowerState(world, x, y, z, powered);
		}
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random rand)
	{
		this.propagateState(world, x, y, z);
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int j)
	{
		VanishingTileEntity te = (VanishingTileEntity) world.getTileEntity(x, y, z);
		if (te != null && te.copiedBlock != null)
			te.copiedBlock.dropBlockAsItem(world, x, y, z, te.copiedMetadata, 0);

		world.removeTileEntity(x, y, z);
	}

	// #end Events

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side)
	{
		return false;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z)
	{
		if ((world.getBlockMetadata(x, y, z) & (flagPowered | flagInTransition)) != 0)
			return null;
		else
		{
			VanishingTileEntity te = TileEntityUtils.getTileEntity(VanishingTileEntity.class, world, x, y, z);
			if (te == null || te.copiedBlock == null)
				return AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1);
			else
				return te.copiedBlock.getCollisionBoundingBoxFromPool((World) ProxyAccess.get(world), x, y, z);
		}
	}

	@Override
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB mask, List list, Entity entity)
	{
		if ((world.getBlockMetadata(x, y, z) & (flagPowered | flagInTransition)) != 0)
			return;

		VanishingTileEntity te = TileEntityUtils.getTileEntity(VanishingTileEntity.class, world, x, y, z);
		if (te == null || te.copiedBlock == null)
		{
			super.addCollisionBoxesToList(world, x, y, z, mask, list, entity);
			return;
		}
		else
			te.copiedBlock.addCollisionBoxesToList((World) ProxyAccess.get(world), x, y, z, mask, list, entity);
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		if ((world.getBlockMetadata(x, y, z) & (flagPowered | flagInTransition)) != 0)
			setBlockBounds(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
		else
		{
			VanishingTileEntity te = TileEntityUtils.getTileEntity(VanishingTileEntity.class, world, x, y, z);
			if (te == null || te.copiedBlock == null)
				setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
			else
				te.copiedBlock.setBlockBoundsBasedOnState(ProxyAccess.get(world), x, y, z);
		}
	}

	@Override
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World world, int x, int y, int z)
	{
		VanishingTileEntity te = TileEntityUtils.getTileEntity(VanishingTileEntity.class, world, x, y, z);
		if (te == null || te.powered || te.inTransition)
			return AxisAlignedBB.getBoundingBox(0, 0, 0, 0, 0, 0);
		else
		{
			setBlockBoundsBasedOnState(world, x, y, z);
			if (te == null || te.copiedBlock == null)
				return AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1);
			else
				return te.copiedBlock.getSelectedBoundingBoxFromPool((World) ProxyAccess.get(world), x, y, z);
		}
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 src, Vec3 dest)
	{
		VanishingTileEntity te = TileEntityUtils.getTileEntity(VanishingTileEntity.class, world, x, y, z);
		if (te == null || te.powered || te.inTransition)
		{
			setBlockBounds(0, 0, 0, 0, 0, 0);
			return super.collisionRayTrace(world, x, y, z, src, dest);
		}
		else
		{

			if (te == null || te.copiedBlock == null)
				return super.collisionRayTrace(world, x, y, z, src, dest);
			else
			{
				World proxy = (World) ProxyAccess.get(world);
				if (proxy == world && te.copiedBlock instanceof IBoundingBox)
					return super.collisionRayTrace(world, x, y, z, src, dest);
				else
					return te.copiedBlock.collisionRayTrace(proxy, x, y, z, src, dest);
			}

		}
	}

	@SideOnly(Side.CLIENT)
	@Override
	public Item getItem(World p_149694_1_, int p_149694_2_, int p_149694_3_, int p_149694_4_)
	{
		// VanishingDiamondBlock has its own unused itemBlock, but we don't want it
		return Item.getItemFromBlock(MalisisDoors.Blocks.vanishingBlock);
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List list)
	{
		list.add(new ItemStack(item, 1, typeWoodFrame));
		list.add(new ItemStack(item, 1, typeIronFrame));
		list.add(new ItemStack(item, 1, typeGoldFrame));
		list.add(new ItemStack(item, 1, typeDiamondFrame));
	}

	@Override
	public int damageDropped(int metadata)
	{
		return metadata;
	}

	@Override
	public boolean isNormalCube()
	{
		return false;
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public float getAmbientOcclusionLightValue()
	{
		return 0.9F;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata)
	{
		return new VanishingTileEntity(metadata);
	}

	@Override
	public boolean canRenderInPass(int pass)
	{
		renderPass = pass;
		return true;
	}

	@Override
	public int getRenderBlockPass()
	{
		return 1;
	}

	@Override
	public int getRenderType()
	{
		return renderId;
	}

}
