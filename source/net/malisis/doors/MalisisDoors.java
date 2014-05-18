package net.malisis.doors;

import net.malisis.doors.proxy.CommonProxy;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;


@Mod(modid = MalisisDoors.modid, name = MalisisDoors.modname, version = MalisisDoors.version)
public class MalisisDoors
{
	@SidedProxy(clientSide = "net.malisis.doors.proxy.ClientProxy", serverSide = "net.malisis.doors.proxy.CommonProxy" )
	public static CommonProxy proxy;

	public static final String modid = "malisisdoors";
	public static final String modname = "Malisis' Doors";
	public static final String version = "1.7.2-0.6.3";


	public static MalisisDoors instance;

//	public static Block blockTest;

	public MalisisDoors()
	{
		instance = this;
	}

	@EventHandler
	public static void preInit(FMLPreInitializationEvent event)
	{
		MalisisBlocks.init();
		MalisisItems.init();
		MalisisEntities.init();
		
		proxy.initRenderers();
		proxy.initSounds();
	}


	@EventHandler
	public void load(FMLInitializationEvent event)
	{
	    NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
	    
	}

}
