package net.malisis.doors.block;

import static net.minecraftforge.common.util.ForgeDirection.*;

import java.util.List;
import java.util.Random;

import net.malisis.doors.MalisisDoors;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PlayerSensor extends Block
{
	public static int FLAG_POWERED = 8;

	public PlayerSensor()
	{
		super(Material.circuits);
		setCreativeTab(MalisisDoors.tab);
		setUnlocalizedName("player_sensor");
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IIconRegister iconRegister)
	{
		this.blockIcon = iconRegister.registerIcon(MalisisDoors.modid + ":" + (this.getUnlocalizedName().substring(5)));
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
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
	{
		return null;
	}

	@Override
	public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int d)
	{
		ForgeDirection dir = ForgeDirection.getOrientation(d);
		return world.isSideSolid(x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ, dir);
	}

	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z)
	{
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
			if (world.isSideSolid(x - dir.offsetX, y - dir.offsetY, z + dir.offsetZ, dir))
				return true;

		return false;
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
	{
		return side + (world.getBlockMetadata(x, y, z) & FLAG_POWERED);
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		float f = 0.125F;
		ForgeDirection dir = getDirection(world, x, y, z);

		if (dir == EAST)
			setBlockBounds(0, f, f, f, 2.0F * f, 1.0F - f);
		else if (dir == WEST)
			setBlockBounds(1 - f, f, f, 1, 2 * f, 1 - f);
		else if (dir == SOUTH)
			setBlockBounds(f, f, 0, 1 - f, 2 * f, f);
		else if (dir == NORTH)
			setBlockBounds(f, f, 1 - f, 1 - f, 2 * f, 1);
		else if (dir == DOWN)
			setBlockBounds(0.5F - f, 1 - f / 2, 0.5F - f, 0.5F + f, 1, 0.5F + f);
		else if (dir == UP)
			setBlockBounds(0.5F - f, 0, 0.5F - f, 0.5F + f, f / 2, 0.5F + f);
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
	{
		ForgeDirection dir = getDirection(world, x, y, z);

		if (!world.isSideSolid(x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ, dir))
		{
			dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
			world.setBlockToAir(x, y, z);
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int metadata)
	{
		if (isPowered(metadata))
			this.notifyPower(world, x, y, z);

		super.breakBlock(world, x, y, z, block, metadata);
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side)
	{
		return isPowered(world.getBlockMetadata(x, y, z)) ? 15 : 0;
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side)
	{
		if (isPowered(world.getBlockMetadata(x, y, z)) && getDirection(world, x, y, z).ordinal() == side)
			return 15;
		return 0;

	}

	@Override
	public boolean canProvidePower()
	{
		return true;
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		world.scheduleBlockUpdate(x, y, z, this, tickRate(world));
	}

	private AxisAlignedBB getDetectionBox(World world, int x, int y, int z)
	{
		ForgeDirection dir = getDirection(world, x, y, z);
		double x1 = x, x2 = x;
		double z1 = z, z2 = z;
		int yOffset = 1;
		int factor = -1;

		if (dir == EAST)
		{
			x1 -= 1;
			x2 += 2;
			z2 += 1;
		}
		else if (dir == WEST)
		{
			x1 -= 1;
			x2 += 2;
			z2 += 1;
		}
		else if (dir == NORTH)
		{
			x2 += 1;
			z1 -= 1;
			z2 += 2;
		}
		else if (dir == SOUTH)
		{
			x2 += 1;
			z1 -= 1;
			z2 += 2;
		}
		else if (dir == UP)
		{
			x2 += 1;
			z2 += 1;
			factor = 1;
		}
		else if (dir == DOWN)
		{
			x2 += 1;
			z2 += 1;
		}

		boolean isAir = world.isAirBlock(x, y + 1 * factor, z);
		while (isAir && yOffset < 6)
			isAir = world.isAirBlock(x, y + (factor * yOffset++), z);

		int y2 = Math.max(y, y + (factor * yOffset));
		y = Math.min(y, y + (factor * yOffset));
		AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(x1, y, z1, x2, y2, z2);
		return aabb;
	}

	/**
	 * Ticks the block if it's been scheduled
	 */
	@Override
	public void updateTick(World world, int x, int y, int z, Random rand)
	{
		if (!world.isRemote)
		{
			world.scheduleBlockUpdate(x, y, z, this, tickRate(world));

			List list = world.getEntitiesWithinAABB(EntityPlayer.class, this.getDetectionBox(world, x, y, z));
			int metadata = world.getBlockMetadata(x, y, z);

			if (list != null && !list.isEmpty())
			{
				if (isPowered(metadata)) //already active
					return;

				metadata |= FLAG_POWERED;
				world.setBlockMetadataWithNotify(x, y, z, metadata, 3);
				notifyPower(world, x, y, z);
			}
			else if ((metadata & 8) != 0) // active
			{
				metadata &= ~FLAG_POWERED;
				world.setBlockMetadataWithNotify(x, y, z, metadata, 3);
				notifyPower(world, x, y, z);
			}

		}
	}

	/**
	 * Sets the block's bounds for rendering it as an item
	 */
	@Override
	public void setBlockBoundsForItemRender()
	{
		float f = 0.125F;
		this.setBlockBounds(f, 0.5F - f, 0.5F - f, 1.0F - f, 0.5F + f, 0.5F + f);
	}

	private void notifyPower(World world, int x, int y, int z)
	{
		world.notifyBlocksOfNeighborChange(x, y, z, this);

		ForgeDirection dir = getDirection(world, x, y, z);
		world.notifyBlocksOfNeighborChange(x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ, this);
	}

	/**
	 * How many world ticks before ticking
	 */
	@Override
	public int tickRate(World par1World)
	{
		return 5;
	}

	public static boolean isPowered(int metadata)
	{
		return (metadata & FLAG_POWERED) != 0;
	}

	public static ForgeDirection getDirection(IBlockAccess world, int x, int y, int z)
	{
		return ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z) & 7);
	}
}
