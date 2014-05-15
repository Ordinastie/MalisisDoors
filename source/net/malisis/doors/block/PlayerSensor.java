package net.malisis.doors.block;

import static net.minecraftforge.common.util.ForgeDirection.*;

import java.util.List;
import java.util.Random;

import net.malisis.doors.MalisisDoors;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PlayerSensor extends Block
{

	//dir : 1 > West / 2 > South / 3 > North /  4 > East

	public PlayerSensor()
	{
		super(Material.circuits);
		this.setCreativeTab(CreativeTabs.tabRedstone);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public void registerBlockIcons(IIconRegister iconRegister)
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
		return (dir == NORTH && world.isSideSolid(x, y, z + 1, NORTH)) || (dir == SOUTH && world.isSideSolid(x, y, z - 1, SOUTH))
				|| (dir == WEST && world.isSideSolid(x + 1, y, z, WEST)) || (dir == EAST && world.isSideSolid(x - 1, y, z, EAST));
	}

	@Override
	public boolean canPlaceBlockAt(World world, int x, int y, int z)
	{
		return (world.isSideSolid(x - 1, y, z, EAST)) || (world.isSideSolid(x + 1, y, z, WEST)) || (world.isSideSolid(x, y, z - 1, SOUTH))
				|| (world.isSideSolid(x, y, z + 1, NORTH));
	}

	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
	{
		metadata = world.getBlockMetadata(x, y, z);
		int k1 = metadata & 8;
		metadata &= 7;

		ForgeDirection dir = ForgeDirection.getOrientation(side);

		if (dir == NORTH && world.isSideSolid(x, y, z + 1, NORTH))
			metadata = 4;
		else if (dir == SOUTH && world.isSideSolid(x, y, z - 1, SOUTH))
			metadata = 3;
		else if (dir == WEST && world.isSideSolid(x + 1, y, z, WEST))
			metadata = 2;
		else if (dir == EAST && world.isSideSolid(x - 1, y, z, EAST))
			metadata = 1;
		else
			metadata = this.getOrientation(world, x, y, z);

		return metadata + k1;
	}

	private int getOrientation(World world, int x, int y, int z)
	{
		if (world.isSideSolid(x - 1, y, z, EAST))
			return 1;
		if (world.isSideSolid(x + 1, y, z, WEST))
			return 2;
		if (world.isSideSolid(x, y, z - 1, SOUTH))
			return 3;
		if (world.isSideSolid(x, y, z + 1, NORTH))
			return 4;
		return 1;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z)
	{
		int metadata = world.getBlockMetadata(x, y, z);
		float f = 0.125F;
		int dir = metadata & 7;

		if (dir == 1)
		{
			this.setBlockBounds(0.0F, f, f, f, 2.0F * f, 1.0F - f);
		}
		else if (dir == 2)
		{
			this.setBlockBounds(1.0F - f, f, f, 1.0F, 2.0F * f, 1.0F - f);
		}
		else if (dir == 3)
		{
			this.setBlockBounds(f, f, 0.0F, 1.0F - f, 2.0F * f, f);
		}
		else if (dir == 4)
		{
			this.setBlockBounds(f, f, 1.0F - f, 1.0F - f, 2.0F * f, 1.0F);
		}
	}

	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block)
	{
		if (this.redundantCanPlaceBlockAt(world, x, y, z))
		{
			int dir = world.getBlockMetadata(x, y, z) & 7;
			boolean drop = false;

			if (!world.isSideSolid(x - 1, y, z, EAST) && dir == 1)
				drop = true;

			if (!world.isSideSolid(x + 1, y, z, WEST) && dir == 2)
				drop = true;

			if (!world.isSideSolid(x, y, z - 1, SOUTH) && dir == 3)
				drop = true;

			if (!world.isSideSolid(x, y, z + 1, NORTH) && dir == 4)
				drop = true;

			if (drop)
			{
				this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
				world.setBlockToAir(x, y, z);
			}
		}
	}

	/**
	 * This method is redundant, check it out...
	 */
	private boolean redundantCanPlaceBlockAt(World world, int x, int y, int z)
	{
		if (!this.canPlaceBlockAt(world, x, y, z))
		{
			this.dropBlockAsItem(world, x, y, z, world.getBlockMetadata(x, y, z), 0);
			world.setBlockToAir(x, y, z);
			return false;
		}
		else
		{
			return true;
		}
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int metadata)
	{
		if ((metadata & 8) > 0)
		{
			int j1 = metadata & 7;
			this.notifyPower(world, x, y, z, j1);
		}

		super.breakBlock(world, x, y, z, block, metadata);
	}

	@Override
	public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side)
	{
		return (world.getBlockMetadata(x, y, z) & 8) != 0 ? 15 : 0;
	}

	@Override
	public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side)
	{
		int metadata = world.getBlockMetadata(x, y, z);

		if ((metadata & 8) == 0)
			return 0;

		int dir = metadata & 7;
		return dir == 5 && side == 1 ? 15 : (dir == 4 && side == 2 ? 15 : (dir == 3 && side == 3 ? 15 : (dir == 2 && side == 4 ? 15 : (dir == 1 && side == 5 ? 15 : 0))));

	}

	@Override
	public boolean canProvidePower()
	{
		return true;
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z)
	{
		world.scheduleBlockUpdate(x, y, z, this, this.tickRate(world));
	}

	private AxisAlignedBB getDetectionBox(World world, int x, int y, int z)
	{
		int dir = world.getBlockMetadata(x, y, z) & 7;
		double x1 = (double) x, x2 = (double) x;
		double z1 = (double) z, z2 = (double) z;
		int yOffset = 1;
		boolean isAir = world.isAirBlock(x, y -1, z);


		if(dir == 1)
		{
			x1 -=1;
			x2 += 2;
			z2 += 1;
		}
		else if(dir == 2)
		{
			x1 -= 1;
			x2 += 2;
			z2 += 1;
		}
		else if(dir == 3)
		{
			x2 += 1;
			z1 -= 1;
			z2 += 2;
		}
		else if(dir == 4)
		{
			x2 += 1;
			z1 -= 1;
			z2 += 2;
		}

		while(isAir && yOffset < 6)
			isAir = world.isAirBlock(x, y - yOffset++, z);

		AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(x1, (double) y - yOffset, z1, x2, (double) y, z2);
		return aabb;
	}

	/**
	 * Ticks the block if it's been scheduled
	 */
	public void updateTick(World world, int x, int y, int z, Random rand)
	{
		if (!world.isRemote)
		{
			world.scheduleBlockUpdate(x, y, z, this, this.tickRate(world));

			List list = world.getEntitiesWithinAABB(EntityPlayer.class, this.getDetectionBox(world, x, y, z));
			int metadata = world.getBlockMetadata(x, y, z);

			if (list != null && !list.isEmpty())
			{
				if((metadata & 8) != 0) //already active
					return;
				metadata |= 8;
				world.setBlockMetadataWithNotify(x, y, z, metadata, 3);
				this.notifyPower(world, x, y, z,  metadata);
			}
			else if((metadata & 8) != 0) // active
			{
				metadata &= ~8;
				world.setBlockMetadataWithNotify(x, y, z, metadata, 3);
				this.notifyPower(world, x, y, z,  metadata);
			}

		}
	}

	/**
	 * Sets the block's bounds for rendering it as an item
	 */
	public void setBlockBoundsForItemRender()
	{
		float f = 0.125F;
		this.setBlockBounds(f, 0.5F - f, 0.5F - f, 1.0F - f, 0.5F + f, 0.5F + f);
	}


	protected void func_82535_o(World par1World, int par2, int par3, int par4)
	{
		int l = par1World.getBlockMetadata(par2, par3, par4);
		int i1 = l & 7;
		boolean flag = (l & 8) != 0;
		// this.func_82534_e(l);
		List list = par1World.getEntitiesWithinAABB(
				EntityArrow.class,
				AxisAlignedBB.getAABBPool().getAABB((double) par2 + this.minX, (double) par3 + this.minY, (double) par4 + this.minZ, (double) par2 + this.maxX,
						(double) par3 + this.maxY, (double) par4 + this.maxZ));
		boolean flag1 = !list.isEmpty();

		if (flag1 && !flag)
		{
			par1World.setBlockMetadataWithNotify(par2, par3, par4, i1 | 8, 3);
			this.notifyPower(par1World, par2, par3, par4, i1);
			par1World.markBlockRangeForRenderUpdate(par2, par3, par4, par2, par3, par4);
			par1World.playSoundEffect((double) par2 + 0.5D, (double) par3 + 0.5D, (double) par4 + 0.5D, "random.click", 0.3F, 0.6F);
		}

		if (!flag1 && flag)
		{
			par1World.setBlockMetadataWithNotify(par2, par3, par4, i1, 3);
			this.notifyPower(par1World, par2, par3, par4, i1);
			par1World.markBlockRangeForRenderUpdate(par2, par3, par4, par2, par3, par4);
			par1World.playSoundEffect((double) par2 + 0.5D, (double) par3 + 0.5D, (double) par4 + 0.5D, "random.click", 0.3F, 0.5F);
		}

		if (flag1)
		{
			par1World.scheduleBlockUpdate(par2, par3, par4, this, this.tickRate(par1World));
		}
	}

	private void notifyPower(World world, int x, int y, int z, int dir)
	{
		world.notifyBlocksOfNeighborChange(x, y, z, this);

		if (dir == 1)
			world.notifyBlocksOfNeighborChange(x - 1, y, z, this);
		else if (dir == 2)
			world.notifyBlocksOfNeighborChange(x + 1, y, z, this);
		else if (dir == 3)
			world.notifyBlocksOfNeighborChange(x, y, z - 1, this);
		else if (dir == 4)
			world.notifyBlocksOfNeighborChange(x, y, z + 1, this);
		else
			world.notifyBlocksOfNeighborChange(x, y - 1, z, this);
	}

	/**
	 * How many world ticks before ticking
	 */
	public int tickRate(World par1World)
	{
		return 5;
	}
}
