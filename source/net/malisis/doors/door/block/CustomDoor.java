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

import java.util.ArrayList;

import net.malisis.doors.door.item.CustomDoorItem;
import net.malisis.doors.door.tileentity.CustomDoorTileEntity;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
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
public class CustomDoor extends Door
{
	public CustomDoor()
	{
		super(Material.wood);
		setHardness(3.0F);
		setStepSound(soundTypeWood);
	}

	@Override
	public void registerIcons(IIconRegister register)
	{}

	@Override
	public IIcon getIcon(int side, int metadata)
	{
		return null;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata)
	{
		if ((metadata & FLAG_TOPBLOCK) != 0)
			return null;

		return new CustomDoorTileEntity();
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z)
	{
		DoorTileEntity te = Door.getDoor(world, x, y, z);
		if (!(te instanceof CustomDoorTileEntity))
			return null;

		return CustomDoorItem.fromTileEntity((CustomDoorTileEntity) te);
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z)
	{
		if (!player.capabilities.isCreativeMode)
		{
			DoorTileEntity te = Door.getDoor(world, x, y, z);
			if (!(te instanceof CustomDoorTileEntity))
				return true;
			if (!te.isTopBlock(x, y, z))
				dropBlockAsItem(world, x, y, z, CustomDoorItem.fromTileEntity((CustomDoorTileEntity) te));
		}
		return super.removedByPlayer(world, player, x, y, z);
	}

	@Override
	protected ItemStack getDoorItemStack(IBlockAccess world, int x, int y, int z)
	{
		DoorTileEntity te = Door.getDoor(world, x, y, z);
		if (!(te instanceof CustomDoorTileEntity))
			return null;
		return CustomDoorItem.fromTileEntity((CustomDoorTileEntity) te);
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune)
	{
		return new ArrayList<ItemStack>();
	}

	@SideOnly(Side.CLIENT)
	@Override
	public boolean addHitEffects(World world, MovingObjectPosition target, EffectRenderer effectRenderer)
	{
		int x = target.blockX;
		int y = target.blockY;
		int z = target.blockZ;

		CustomDoorTileEntity te = (CustomDoorTileEntity) Door.getDoor(world, x, y, z);
		if (te == null)
			return true;

		Block[] blocks = { te.getFrame(), te.getTopMaterial(), te.getBottomMaterial() };
		int[] metadata = { te.getFrameMetadata(), te.getTopMaterialMetadata(), te.getBottomMaterialMetadata() };

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

		int i = world.rand.nextInt(blocks.length);
		if (blocks[i] == null)
			blocks[i] = Blocks.planks;

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

		CustomDoorTileEntity te = (CustomDoorTileEntity) Door.getDoor(world, x, y, z);
		if (te == null)
			return true;

		Block[] blocks = { te.getFrame(), te.getTopMaterial(), te.getBottomMaterial() };
		int[] metadata = { te.getFrameMetadata(), te.getTopMaterialMetadata(), te.getBottomMaterialMetadata() };

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
					if (blocks[l] == null)
						blocks[l] = Blocks.planks;
					fx = new EntityDiggingFX(world, fxX, fxY, fxZ, fxX - x - 0.5D, fxY - y - 0.5D, fxZ - z - 0.5D, blocks[l], metadata[l]);
					effectRenderer.addEffect(fx);
				}
			}
		}

		return true;
	}

	@Override
	public int getLightValue(IBlockAccess world, int x, int y, int z)
	{
		CustomDoorTileEntity te = (CustomDoorTileEntity) Door.getDoor(world, x, y, z);
		if (te == null || te.getFrame() == null)
			return 0;

		return Math.max(Math.max(te.getFrame().getLightValue(), te.getTopMaterial().getLightValue()), te.getBottomMaterial()
				.getLightValue());
	}
}
