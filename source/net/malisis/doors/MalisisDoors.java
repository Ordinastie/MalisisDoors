package net.malisis.doors;

import net.malisis.doors.proxy.CommonProxy;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;


@Mod(modid = MalisisDoors.modid, name = MalisisDoors.modname, version = MalisisDoors.version)
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
public class MalisisDoors
{
	@SidedProxy(clientSide = "net.malisis.doors.proxy.ClientProxy", serverSide = "net.malisis.doors.proxy.CommonProxy" )
	public static CommonProxy proxy;

	public static final String modid = "malisisdoors";
	public static final String modname = "Malisis' Doors";
	public static final String version = "0.01";


	public static MalisisDoors instance;

//	public static Block blockTest;

	public MalisisDoors()
	{
		instance = this;
	}

	@EventHandler
	public static void preInit(FMLPreInitializationEvent event)
	{
		proxy.initRenderers();
		proxy.initSounds();
	}


	@EventHandler
	public void load(FMLInitializationEvent event)
	{
		MalisisBlocks.init();
		MalisisItems.init();
		MalisisEntities.init();


		// TESTING
//		blockTest = (new ShapedTestBlock(700)).setUnlocalizedName("blockTest");
//		GameRegistry.registerBlock(blockTest, modid + blockTest.getUnlocalizedName().substring(5));
//		LanguageRegistry.addName(blockTest, "Block Test");

	}

	public static void Message(Object text)
	{
		ChatMessageComponent msg = new ChatMessageComponent().addText(text.toString());
		MinecraftServer.getServer().getConfigurationManager().sendChatMsg(msg);
	}

}
