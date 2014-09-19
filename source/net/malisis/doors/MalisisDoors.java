package net.malisis.doors;

import net.malisis.core.IMalisisMod;
import net.malisis.core.MalisisCore;
import net.malisis.core.configuration.Settings;
import net.malisis.doors.network.NetworkHandler;
import net.malisis.doors.proxy.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
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
	public static final String version = "${version}";

	public static MalisisDoors instance;
	public static MalisisDoorsSettings settings;

	public static CreativeTabs tab = new MalisisDoorsTab();

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
	public void preInit(FMLPreInitializationEvent event)
	{
		settings = new MalisisDoorsSettings(event.getSuggestedConfigurationFile());

		Registers.init();

		proxy.initRenderers();
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
		public static Block garageDoor;
		public static Block jailDoor;
		public static Block doorFactory;
		public static Block customDoor;
		public static Block laboratoryDoor;
		public static Block factoryDoor;
		public static Block shojiDoor;
	}

	public static class Items
	{
		public static Item woodSlidingDoorItem;
		public static Item ironSlidingDoorItem;
		public static Item jailDoorItem;
		public static Item customDoorItem;
		public static Item laboratoryDoorItem;
		public static Item facortyDoorItem;
		public static Item shojiDoorItem;
	}

}
