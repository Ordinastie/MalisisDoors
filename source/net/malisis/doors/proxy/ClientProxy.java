package net.malisis.doors.proxy;

import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.IBaseRendering;
import net.malisis.doors.MalisisBlocks;
import net.malisis.doors.entity.DoorTileEntity;
import net.malisis.doors.entity.VanishingTileEntity;
import net.malisis.doors.renderer.TileEntityRenderer;
import net.malisis.doors.renderer.VanishingBlockRenderer;
import net.malisis.doors.renderer.block.DoorRenderer;
import net.malisis.doors.renderer.block.MixedBlockRenderer;
import net.minecraft.item.Item;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy
{
	@Override
	public void initRenderers()
	{
		MixedBlockRenderer mbr = BaseRenderer.create(MixedBlockRenderer.class, (IBaseRendering) MalisisBlocks.mixedBlock);
		RenderingRegistry.registerBlockHandler(BaseRenderer.create(VanishingBlockRenderer.class,
				(IBaseRendering) MalisisBlocks.vanishingBlock));
		RenderingRegistry.registerBlockHandler(BaseRenderer.create(DoorRenderer.class, (IBaseRendering) MalisisBlocks.doubleDoorWood,
				(IBaseRendering) MalisisBlocks.doubleDoorWood, (IBaseRendering) MalisisBlocks.woodSlidingDoor,
				(IBaseRendering) MalisisBlocks.ironSlidingDoor));
		RenderingRegistry.registerBlockHandler(mbr);

		TileEntityRenderer ter = new TileEntityRenderer();
		ClientRegistry.bindTileEntitySpecialRenderer(VanishingTileEntity.class, ter);
		ClientRegistry.bindTileEntitySpecialRenderer(DoorTileEntity.class, ter);

		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(MalisisBlocks.mixedBlock), mbr);
	}

	@Override
	public void initSounds()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}
}
