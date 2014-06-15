package net.malisis.doors;

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
	private static ProxyBlockAccess proxyBlockAccess;
	private static ProxyWorld proxyWorld;
	public static World cacheWorld;

	private interface IProxyAccess
	{
		public Block getBlock(int x, int y, int z);

		public TileEntity getTileEntity(int var1, int var2, int var3);

		public int getBlockMetadata(int x, int y, int z);

		public WorldInfo getWorldInfo();

		public void calculateInitialSkylight();

		public void calculateInitialWeatherBody();

		public boolean setBlockMetadataWithNotify(int x, int y, int z, int metadata, int flag);

		public boolean setBlock(int x, int y, int z, Block block, int metadata, int flag);
	}

	public static IBlockAccess get(IBlockAccess world)
	{
		if (world == null)
			return null;

		if (world instanceof World)
		{
			cacheWorld = (World) world;
			if (proxyWorld == null)
				proxyWorld = new ProxyWorld((World) world);
			return proxyWorld;
		}

		if (proxyBlockAccess == null)
			proxyBlockAccess = new ProxyBlockAccess(world);
		return proxyBlockAccess;
	}

	public static VanishingTileEntity getVanishingTileEntity(int x, int y, int z)
	{
		TileEntity te = cacheWorld.getTileEntity(x, y, z);
		if (te instanceof VanishingTileEntity)
			return (VanishingTileEntity) te;
		return null;
	}

	public static Block getBlock(IBlockAccess world, int x, int y, int z)
	{
		VanishingTileEntity te = getVanishingTileEntity(x, y, z);
		if (te != null)
			return te.copiedBlock != null ? te.copiedBlock : Blocks.air;
		return world.getBlock(x, y, z);
	}

	public static int getMetadata(IBlockAccess world, int x, int y, int z)
	{
		VanishingTileEntity te = getVanishingTileEntity(x, y, z);
		if (te != null)
			return te.copiedMetadata;
		return world.getBlockMetadata(x, y, z);
	}

	public static TileEntity getTileEntity(IBlockAccess world, int x, int y, int z)
	{
		VanishingTileEntity te = getVanishingTileEntity(x, y, z);
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
			this.original = world;
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
			original = cacheWorld;
		}

		@Override
		public Block getBlock(int x, int y, int z)
		{
			return ProxyAccess.getBlock(cacheWorld, x, y, z);
		}

		@Override
		public TileEntity getTileEntity(int x, int y, int z)
		{
			return ProxyAccess.getTileEntity(cacheWorld, x, y, z);
		}

		@Override
		public int getBlockMetadata(int x, int y, int z)
		{
			return ProxyAccess.getMetadata(cacheWorld, x, y, z);
		}

		@Override
		public boolean setBlock(int x, int y, int z, Block block, int metadata, int flag)
		{
			VanishingTileEntity te = ProxyAccess.getVanishingTileEntity(x, y, z);
			if (te != null)
			{
				te.copiedBlock = block;
				te.copiedMetadata = metadata;
				return true;
			}
			return cacheWorld.setBlock(x, y, z, block, metadata, flag);
		}

		@Override
		public boolean setBlockMetadataWithNotify(int x, int y, int z, int metadata, int flag)
		{
			VanishingTileEntity te = ProxyAccess.getVanishingTileEntity(x, y, z);
			if (te != null && te.copiedBlock != null)
			{
				te.copiedMetadata = metadata;
				notifyBlocksOfNeighborChange(x, y, z, te.copiedBlock);
				return true;
			}
			return cacheWorld.setBlockMetadataWithNotify(x, y, z, metadata, flag);
		}

		@Override
		protected IChunkProvider createChunkProvider()
		{
			return null;
		}

		@Override
		public WorldInfo getWorldInfo()
		{
			return cacheWorld.getWorldInfo();
		}

		@Override
		public void calculateInitialSkylight()
		{}

		@Override
		public void calculateInitialWeatherBody()
		{}

	}
}