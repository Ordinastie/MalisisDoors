package net.malisis.doors.proxy;

import static net.malisis.doors.MalisisDoors.Blocks.*;
import static net.malisis.doors.MalisisDoors.Items.*;
import net.malisis.doors.block.FenceGate;
import net.malisis.doors.block.MixedBlock;
import net.malisis.doors.block.RustyHatch;
import net.malisis.doors.block.TrapDoor;
import net.malisis.doors.block.VanishingBlock;
import net.malisis.doors.block.VanishingDiamondBlock;
import net.malisis.doors.door.renderer.CustomDoorRenderer;
import net.malisis.doors.door.renderer.DoorRenderer;
import net.malisis.doors.door.renderer.FenceGateRenderer;
import net.malisis.doors.door.renderer.TrapDoorRenderer;
import net.malisis.doors.door.tileentity.CustomDoorTileEntity;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.malisis.doors.door.tileentity.FenceGateTileEntity;
import net.malisis.doors.door.tileentity.TrapDoorTileEntity;
import net.malisis.doors.entity.GarageDoorTileEntity;
import net.malisis.doors.entity.RustyHatchTileEntity;
import net.malisis.doors.entity.VanishingTileEntity;
import net.malisis.doors.renderer.GarageDoorRenderer;
import net.malisis.doors.renderer.MixedBlockRenderer;
import net.malisis.doors.renderer.RustyHatchRenderer;
import net.malisis.doors.renderer.VanishingBlockRenderer;
import net.minecraft.item.Item;

public class ClientProxy extends CommonProxy
{
	@Override
	public void initRenderers()
	{
		// doors
		new DoorRenderer().registerFor(DoorTileEntity.class);

		// fence gate
		FenceGateRenderer fgr = new FenceGateRenderer();
		fgr.registerFor(FenceGate.class, FenceGateTileEntity.class);

		// trapdoor
		TrapDoorRenderer tdr = new TrapDoorRenderer();
		tdr.registerFor(TrapDoor.class, TrapDoorTileEntity.class);

		// mixed blocks
		MixedBlockRenderer mbr = new MixedBlockRenderer();
		mbr.registerFor(MixedBlock.class);
		mbr.registerFor(Item.getItemFromBlock(mixedBlock));

		// vanishing blocks
		VanishingBlockRenderer vbr = new VanishingBlockRenderer();
		vbr.registerFor(VanishingBlock.class, VanishingDiamondBlock.class, VanishingTileEntity.class);

		// garage door
		GarageDoorRenderer gdr = new GarageDoorRenderer();
		gdr.registerFor(GarageDoorTileEntity.class);
		gdr.registerFor(Item.getItemFromBlock(garageDoor));

		// custom doors
		CustomDoorRenderer cdr = new CustomDoorRenderer();
		cdr.registerFor(CustomDoorTileEntity.class);
		cdr.registerFor(customDoorItem);

		//rusty hatch
		RustyHatchRenderer rhr = new RustyHatchRenderer();
		rhr.registerFor(RustyHatch.class, RustyHatchTileEntity.class);
		rhr.registerFor(rustyHandle);
	}
}
