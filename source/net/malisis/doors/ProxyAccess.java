package net.malisis.doors;

import java.util.HashMap;

import lombok.Delegate;
import net.malisis.doors.entity.VanishingTileEntity;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.WorldInfo;

public class ProxyAccess
{
	private static HashMap<IBlockAccess, IBlockAccess> cache = new HashMap<>();
	private static World tmpCache;

	private interface IProxyAccess
	{
		public Block getBlock(int x, int y, int z);

		public TileEntity getTileEntity(int var1, int var2, int var3);

		public int getBlockMetadata(int x, int y, int z);

		public boolean setBlockMetadataWithNotify(int x, int y, int z, int metadata, int flag);

		public boolean setBlock(int x, int y, int z, Block block, int metadata, int flag);

		public WorldInfo getWorldInfo();

		public WorldInfo getSeed();

		public void calculateInitialSkylight();

		public void calculateInitialWeatherBody();
	}

	public static IBlockAccess get(IBlockAccess world)
	{
		if (world == null)
			return null;

		IBlockAccess proxy = cache.get(world);
		if (proxy == null)
		{
			if (world instanceof World)
			{
				tmpCache = (World) world;
				proxy = new ProxyWorld((World) world);
				tmpCache = null;
			}
			else
				proxy = new ProxyBlockAccess(world);
			cache.put(world, proxy);
		}
		return proxy;
	}

	public static VanishingTileEntity getVanishingTileEntity(IBlockAccess world, int x, int y, int z)
	{
		TileEntity te = world.getTileEntity(x, y, z);
		if (te instanceof VanishingTileEntity)
			return (VanishingTileEntity) te;
		return null;
	}

	public static Block getBlock(IBlockAccess world, int x, int y, int z)
	{
		VanishingTileEntity te = getVanishingTileEntity(world, x, y, z);
		if (te != null)
			return te.copiedBlock != null ? te.copiedBlock : Blocks.air;
		return world.getBlock(x, y, z);
	}

	public static int getMetadata(IBlockAccess world, int x, int y, int z)
	{
		VanishingTileEntity te = getVanishingTileEntity(world, x, y, z);
		if (te != null)
			return te.copiedMetadata;
		return world.getBlockMetadata(x, y, z);
	}

	public static TileEntity getTileEntity(IBlockAccess world, int x, int y, int z)
	{
		VanishingTileEntity te = getVanishingTileEntity(world, x, y, z);
		if (te != null)
			return te.copiedTileEntity;
		return world.getTileEntity(x, y, z);
	}

	/**
	 * ProxyBlockAccess
	 */
	private static class ProxyBlockAccess implements IBlockAccess
	{
		@Delegate(excludes = IProxyAccess.class)
		public IBlockAccess original;

		public ProxyBlockAccess(IBlockAccess world)
		{
			original = world;
		}

		@Override
		public Block getBlock(int x, int y, int z)
		{
			return ProxyAccess.getBlock(original, x, y, z);
		}

		@Override
		public TileEntity getTileEntity(int x, int y, int z)
		{
			return ProxyAccess.getTileEntity(original, x, y, z);
		}

		@Override
		public int getBlockMetadata(int x, int y, int z)
		{
			return ProxyAccess.getMetadata(original, x, y, z);
		}
	}

	/**
	 * ProxyWorld
	 */
	private static class ProxyWorld extends World
	{
		@Delegate(excludes = IProxyAccess.class)
		public World original;

		public ProxyWorld(World world)
		{
			super(world.getSaveHandler(), null, new WorldSettings(world.getWorldInfo()), world.provider, (Profiler) null);
			original = world;
			// reset back the world for the provider
			provider.worldObj = world;
		}

		@Override
		public Block getBlock(int x, int y, int z)
		{
			return ProxyAccess.getBlock(original, x, y, z);
		}

		@Override
		public TileEntity getTileEntity(int x, int y, int z)
		{
			return ProxyAccess.getTileEntity(original, x, y, z);
		}

		@Override
		public int getBlockMetadata(int x, int y, int z)
		{
			return ProxyAccess.getMetadata(original, x, y, z);
		}

		@Override
		public boolean setBlock(int x, int y, int z, Block block, int metadata, int flag)
		{
			VanishingTileEntity te = ProxyAccess.getVanishingTileEntity(original, x, y, z);
			if (te != null)
			{
				te.copiedBlock = block;
				te.copiedMetadata = metadata;
				return true;
			}
			return original.setBlock(x, y, z, block, metadata, flag);
		}

		@Override
		public boolean setBlockMetadataWithNotify(int x, int y, int z, int metadata, int flag)
		{
			VanishingTileEntity te = ProxyAccess.getVanishingTileEntity(original, x, y, z);
			if (te != null && te.copiedBlock != null)
			{
				te.copiedMetadata = metadata;
				notifyBlocksOfNeighborChange(x, y, z, te.copiedBlock);
				return true;
			}
			return original.setBlockMetadataWithNotify(x, y, z, metadata, flag);
		}

		@Override
		protected IChunkProvider createChunkProvider()
		{
			return null;
		}

		@Override
		public WorldInfo getWorldInfo()
		{
			// called from within super(), so we can't use original
			return tmpCache.getWorldInfo();
		}

		@Override
		public long getSeed()
		{
			// called from within super(), so we can't use original
			return tmpCache.getSeed();
		}

		@Override
		public void calculateInitialSkylight()
		{}

		@Override
		public void calculateInitialWeatherBody()
		{}

	}
}