package net.malisis.doors.proxy;

import static net.malisis.doors.MalisisDoors.Blocks.*;
import net.malisis.core.configuration.ConfigurationGui;
import net.malisis.core.renderer.BaseRenderer;
import net.malisis.core.renderer.IBaseRendering;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.entity.DoorTileEntity;
import net.malisis.doors.entity.FenceGateTileEntity;
import net.malisis.doors.entity.GarageDoorTileEntity;
import net.malisis.doors.entity.TrapDoorTileEntity;
import net.malisis.doors.entity.VanishingTileEntity;
import net.malisis.doors.renderer.DoorRenderer;
import net.malisis.doors.renderer.FenceGateRenderer;
import net.malisis.doors.renderer.GarageDoorRenderer;
import net.malisis.doors.renderer.TrapDoorRenderer;
import net.malisis.doors.renderer.VanishingBlockRenderer;
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
		// doors
		IBaseRendering[] blocks = new IBaseRendering[] { (IBaseRendering) doubleDoorWood, (IBaseRendering) doubleDoorIron,
				(IBaseRendering) woodSlidingDoor, (IBaseRendering) ironSlidingDoor };
		DoorRenderer doorRenderer = BaseRenderer.create(DoorRenderer.class, blocks);

		RenderingRegistry.registerBlockHandler(doorRenderer);
		ClientRegistry.bindTileEntitySpecialRenderer(DoorTileEntity.class, doorRenderer);

		// fence gate
		FenceGateRenderer fenceGateRenderer = BaseRenderer.create(FenceGateRenderer.class, (IBaseRendering) fenceGate);
		RenderingRegistry.registerBlockHandler(fenceGateRenderer);
		ClientRegistry.bindTileEntitySpecialRenderer(FenceGateTileEntity.class, fenceGateRenderer);

		// trapdoor
		TrapDoorRenderer trapDoorRenderer = BaseRenderer.create(TrapDoorRenderer.class, (IBaseRendering) trapDoor);
		RenderingRegistry.registerBlockHandler(trapDoorRenderer);
		ClientRegistry.bindTileEntitySpecialRenderer(TrapDoorTileEntity.class, trapDoorRenderer);

		// mixed blocks
		MixedBlockRenderer mbr = BaseRenderer.create(MixedBlockRenderer.class, (IBaseRendering) mixedBlock);
		RenderingRegistry.registerBlockHandler(mbr);
		MinecraftForgeClient.registerItemRenderer(Item.getItemFromBlock(mixedBlock), mbr);

		// vanishing blocks
		VanishingBlockRenderer vbr = BaseRenderer.create(VanishingBlockRenderer.class, (IBaseRendering) vanishingBlock,
				(IBaseRendering) vanishingDiamondBlock);
		RenderingRegistry.registerBlockHandler(vbr);
		ClientRegistry.bindTileEntitySpecialRenderer(VanishingTileEntity.class, vbr);

		// garage door
		GarageDoorRenderer gdr = BaseRenderer.create(GarageDoorRenderer.class, (IBaseRendering) garageDoor);
		RenderingRegistry.registerBlockHandler(gdr);
		ClientRegistry.bindTileEntitySpecialRenderer(GarageDoorTileEntity.class, gdr);
	}

	@Override
	public void initSounds()
	{
		MinecraftForge.EVENT_BUS.register(this);
	}

	@Override
	public void openConfigurationGui()
	{
		(new ConfigurationGui(MalisisDoors.settings)).display();
	}
}
