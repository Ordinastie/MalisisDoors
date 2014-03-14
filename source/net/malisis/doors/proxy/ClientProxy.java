package net.malisis.doors.proxy;

import net.malisis.doors.MalisisDoors;
import net.malisis.doors.entity.DoorTileEntity;
import net.malisis.doors.entity.VanishingTileEntity;
import net.malisis.doors.renderer.DefaultBlockRenderer;
import net.malisis.doors.renderer.TileEntityRenderer;
import net.malisis.doors.renderer.block.DoorRenderer;
import net.minecraftforge.client.event.sound.SoundLoadEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy
{

	@Override
	public void initRenderers()
	{
		DefaultBlockRenderer.renderId = RenderingRegistry.getNextAvailableRenderId();
		DefaultBlockRenderer.vanishingBlockRenderId = RenderingRegistry.getNextAvailableRenderId();
		DoorRenderer.renderId = RenderingRegistry.getNextAvailableRenderId();


		RenderingRegistry.registerBlockHandler(DefaultBlockRenderer.renderId, new DefaultBlockRenderer());
		RenderingRegistry.registerBlockHandler(DefaultBlockRenderer.vanishingBlockRenderId, new DefaultBlockRenderer());
		RenderingRegistry.registerBlockHandler(DoorRenderer.renderId, new DoorRenderer());

		ClientRegistry.bindTileEntitySpecialRenderer(VanishingTileEntity.class,  new TileEntityRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(DoorTileEntity.class,  new TileEntityRenderer());
		//RenderingRegistry.registerEntityRenderingHandler(DoorTileEntity.class, new EntityRenderer());
	}

	@Override
	public void initSounds()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	@ForgeSubscribe
	public void onSound(SoundLoadEvent event)
	{
		event.manager.addSound(MalisisDoors.modid + ":" + "slidingdoor.ogg");
		event.manager.addSound(MalisisDoors.modid + ":" + "factory.ogg");
		event.manager.addSound(MalisisDoors.modid + ":" + "slidingdooro.wav");
		event.manager.addSound(MalisisDoors.modid + ":" + "slidingdoorc.wav");
		event.manager.addSound(MalisisDoors.modid + ":" + "portal.ogg");
	}
}
