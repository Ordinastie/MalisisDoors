package net.malisis.doors;

import net.malisis.doors.entity.BlockMixerTileEntity;
import net.malisis.doors.entity.DoorTileEntity;
import net.malisis.doors.entity.MixedBlockTileEntity;
import net.malisis.doors.entity.VanishingTileEntity;
import cpw.mods.fml.common.registry.GameRegistry;

public class MalisisEntities
{



	public static void init()
	{
		registerTileEntities();
		registerEntities();
	}

	private static void registerTileEntities()
	{
		GameRegistry.registerTileEntity(VanishingTileEntity.class, "vanishingTileEntity");
		GameRegistry.registerTileEntity(DoorTileEntity.class, "doorTileEntity");
		GameRegistry.registerTileEntity(BlockMixerTileEntity.class, "blockMixerTileEntity");
		GameRegistry.registerTileEntity(MixedBlockTileEntity.class, "mixedBlockTileEntity");
	}

	private static void registerEntities()
	{
	//	EntityRegistry.registerModEntity(DoorTileEntity.class, "doorEntity", 1, MalisisMod.instance, 80, 3, true);

	}

}
