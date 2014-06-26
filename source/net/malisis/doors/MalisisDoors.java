package net.malisis.doors;

import net.malisis.core.IMalisisMod;
import net.malisis.core.MalisisCore;
import net.malisis.core.configuration.Settings;
import net.malisis.doors.network.NetworkHandler;
import net.malisis.doors.proxy.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = MalisisDoors.modid, name = MalisisDoors.modname, version = MalisisDoors.version)
public class MalisisDoors implements IMalisisMod
{
	@SidedProxy(clientSide = "net.malisis.doors.proxy.ClientProxy", serverSide = "net.malisis.doors.proxy.CommonProxy")
	public static CommonProxy proxy;

	public static final String modid = "malisisdoors";
	public static final String modname = "Malisis' Doors";
	public static final String version = "1.7.2-0.6.5";

	public static MalisisDoors instance;
	public static MalisisDoorsSettings settings;

	// public static Block blockTest;

	public MalisisDoors()
	{
		instance = this;
		MalisisCore.registerMod(this);
	}

	@Override
	public String getModId()
	{
		return modid;
	}

	@Override
	public String getName()
	{
		return modname;
	}

	@Override
	public String getVersion()
	{
		return version;
	}

	@Override
	public Settings getSettings()
	{
		return settings;
	}

	@EventHandler
	public static void preInit(FMLPreInitializationEvent event)
	{
		settings = new MalisisDoorsSettings(event.getSuggestedConfigurationFile());

		Registers.init();

		proxy.initRenderers();
		proxy.initSounds();
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		NetworkHandler.init(modid);
	}

	public static class Blocks
	{
		public static Block doubleDoorWood;
		public static Block doubleDoorIron;
		public static Block fenceGate;
		public static Block trapDoor;
		public static Block woodSlidingDoor;
		public static Block ironSlidingDoor;
		public static Block playerSensor;
		public static Block vanishingBlock;
		public static Block vanishingDiamondBlock;
		public static Block blockMixer;
		public static Block mixedBlock;
	}

	public static class Items
	{
		public static Item woodSlidingDoorItem;
		public static Item ironSlidingDoorItem;
	}

}
