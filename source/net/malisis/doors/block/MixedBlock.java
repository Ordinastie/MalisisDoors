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

import java.util.ArrayList;

import net.malisis.core.util.EntityUtils;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.MalisisDoorsSettings;
import net.malisis.doors.entity.MixedBlockTileEntity;
import net.malisis.doors.item.MixedBlockBlockItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class MixedBlock extends Block implements ITileEntityProvider
{
	public static int renderId = -1;

	public MixedBlock()
	{
		super(Material.rock);
		setHardness(0.7F);
		setUnlocalizedName("mixed_block");
	}

	@Override
	public void registerIcons(IIconRegister p_149651_1_)
	{}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
	{
		return side;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack)
	{
		if (!(itemStack.getItem() instanceof MixedBlockBlockItem))
			return;

		MixedBlockTileEntity te = TileEntityUtils.getTileEntity(MixedBlockTileEntity.class, world, x, y, z);
		if (te == null)
			return;
		te.set(itemStack);

		if (MalisisDoorsSettings.enhancedMixedBlockPlacement.get())
		{
			ForgeDirection dir = EntityUtils.getEntityFacing(player, true);
			if (!player.isSneaking())
				dir = dir.getOpposite();
			world.setBlockMetadataWithNotify(x, y, z, dir.ordinal(), 3);
		}
		else
			world.notifyBlockChange(x, y, z, this);
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		MixedBlockTileEntity te = TileEntityUtils.getTileEntity(MixedBlockTileEntity.class, world, x, y, z);
		if (te == null)
			return null;
		return MixedBlockBlockItem.fromTileEntity(te);
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z)
	{
		MixedBlockTileEntity te = TileEntityUtils.getTileEntity(MixedBlockTileEntity.class, world, x, y, z);
		if (te == null || te.block1 == null || te.block2 == null)
			return 0;

		return Math.max(te.block1.getLightValue(), te.block2.getLightValue());
	}

	@Override
	public boolean canProvidePower()
	{
		return true;
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side)
	{
		MixedBlockTileEntity te = TileEntityUtils.getTileEntity(MixedBlockTileEntity.class, world, x, y, z);
		if (te == null)
			return 0;

		return te.block1 == Blocks.redstone_block || te.block2 == Blocks.redstone_block ? 15 : 0;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean addHitEffects(World world, MovingObjectPosition target, EffectRenderer effectRenderer)
	{
		int x = target.blockX;
		int y = target.blockY;
		int z = target.blockZ;

		MixedBlockTileEntity te = (MixedBlockTileEntity) world.getTileEntity(x, y, z);
		if (te == null || te.block1 == null || te.block2 == null)
			return true;

		Block[] blocks = { te.block1, te.block2 };
		int[] metadata = { te.metadata1, te.metadata2 };

		ForgeDirection side = ForgeDirection.getOrientation(target.sideHit);

		double fxX = x + world.rand.nextDouble();
		double fxY = y + world.rand.nextDouble();
		double fxZ = z + world.rand.nextDouble();

		switch (side)
		{
			case DOWN:
				fxY = y + getBlockBoundsMinY() - 0.1F;
				break;
			case UP:
				fxY = y + getBlockBoundsMaxY() + 0.1F;
				break;
			case NORTH:
				fxZ = z + getBlockBoundsMinZ() - 0.1F;
				break;
			case SOUTH:
				fxZ = z + getBlockBoundsMaxY() + 0.1F;
				break;
			case EAST:
				fxX = x + getBlockBoundsMaxX() + 0.1F;
				break;
			case WEST:
				fxX = x + getBlockBoundsMinX() + 0.1F;
				break;
			default:
				break;
		}

		int i = world.rand.nextBoolean() ? 0 : 1;

		EntityDiggingFX fx = new EntityDiggingFX(world, fxX, fxY, fxZ, 0.0D, 0.0D, 0.0D, blocks[i], metadata[i]);
		fx.multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F);
		effectRenderer.addEffect(fx);

		return true;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean addDestroyEffects(World world, int x, int y, int z, int meta, EffectRenderer effectRenderer)
	{
		byte nb = 4;
		EntityDiggingFX fx;

		MixedBlockTileEntity te = (MixedBlockTileEntity) world.getTileEntity(x, y, z);
		if (te == null || te.block1 == null || te.block2 == null)
			return true;

		Block[] blocks = { te.block1, te.block2 };
		int[] metadata = { te.metadata1, te.metadata2 };

		for (int i = 0; i < nb; ++i)
		{
			for (int j = 0; j < nb; ++j)
			{
				for (int k = 0; k < nb; ++k)
				{
					double fxX = x + (i + 0.5D) / nb;
					double fxY = y + (j + 0.5D) / nb;
					double fxZ = z + (k + 0.5D) / nb;
					int l = (i + j + k) % 2;
					fx = new EntityDiggingFX(world, fxX, fxY, fxZ, fxX - x - 0.5D, fxY - y - 0.5D, fxZ - z - 0.5D, blocks[l], metadata[l]);
					effectRenderer.addEffect(fx);
				}
			}
		}

		return true;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata)
	{
		return new MixedBlockTileEntity();
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z)
	{
		if (!player.capabilities.isCreativeMode)
		{
			MixedBlockTileEntity te = TileEntityUtils.getTileEntity(MixedBlockTileEntity.class, world, x, y, z);
			if (te != null)
				dropBlockAsItem(world, x, y, z, MixedBlockBlockItem.fromTileEntity(te));
		}
		return super.removedByPlayer(world, player, x, y, z);
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
	{
		return new ArrayList<ItemStack>();
	}

	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}

	@Override
	public int getRenderType()
	{
		return renderId;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderBlockPass()
	{
		return 1;
	}

	@Override
	public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side)
	{
		if (world.isAirBlock(x, y, z))
			return true;

		Block block = world.getBlock(x, y, z);
		if (block != this && !(block instanceof BlockBreakable))
			return !block.isOpaqueCube();

		ForgeDirection op = ForgeDirection.getOrientation(side).getOpposite();
		MixedBlockTileEntity current = TileEntityUtils.getTileEntity(MixedBlockTileEntity.class, world, x + op.offsetX, y + op.offsetY, z
				+ op.offsetZ);

		return !isOpaque(world, x, y, z) && current.isOpaque();
	}

	public static boolean isOpaque(IBlockAccess world, int x, int y, int z)
	{
		Block block = world.getBlock(x, y, z);
		if (block instanceof BlockBreakable)
			return true;

		MixedBlockTileEntity te = TileEntityUtils.getTileEntity(MixedBlockTileEntity.class, world, x, y, z);
		return te != null && te.isOpaque();
	}
}
