package net.malisis.doors;

import net.malisis.core.IMalisisMod;
import net.malisis.core.MalisisCore;
import net.malisis.core.configuration.Settings;
import net.malisis.core.inventory.MalisisTab;
import net.malisis.core.item.MalisisItem;
import net.malisis.core.network.MalisisNetwork;
import net.malisis.core.registry.MalisisRegistry;
import net.malisis.core.renderer.font.MalisisFont;
import net.malisis.core.util.modmessage.ModMessageManager;
import net.malisis.doors.block.BigDoor;
import net.malisis.doors.block.CustomDoor;
import net.malisis.doors.block.Door;
import net.malisis.doors.block.DoorFactory;
import net.malisis.doors.block.FenceGate;
import net.malisis.doors.block.Forcefield;
import net.malisis.doors.block.GarageDoor;
import net.malisis.doors.block.ModelDoor;
import net.malisis.doors.block.RustyHatch;
import net.malisis.doors.block.RustyLadder;
import net.malisis.doors.block.SaloonDoorBlock;
import net.malisis.doors.block.TrapDoor;
import net.malisis.doors.block.VerticalHatchDoor;
import net.malisis.doors.item.CustomDoorItem;
import net.malisis.doors.item.DoorItem;
import net.malisis.doors.item.ForcefieldItem;
import net.malisis.doors.item.SaloonDoorItem;
import net.malisis.doors.item.VerticalHatchItem;
import net.malisis.doors.renderer.ForcefieldRenderer;
import net.malisis.doors.renderer.RustyHatchRenderer;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(	modid = MalisisDoors.modid,
		name = MalisisDoors.modname,
		version = MalisisDoors.version,
		dependencies = "required-after:malisiscore",
		acceptedMinecraftVersions = "[1.12, 1.13)")
public class MalisisDoors implements IMalisisMod
{
	public static final String modid = "malisisdoors";
	public static final String modname = "Malisis' Doors";
	public static final String version = "${version}";

	public static MalisisDoors instance;
	public static MalisisNetwork network;
	public static MalisisDoorsSettings settings;

	public static MalisisTab tab = new MalisisTab(MalisisDoors.modid, () -> Items.jailDoorItem);
	public static MalisisFont digitalFont;

	public MalisisDoors()
	{
		instance = this;
		network = new MalisisNetwork(this);
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

		ModMessageManager.register(this, DoorDescriptor.class);

		Registers.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		if (MalisisCore.isClient())
		{
			new ForcefieldRenderer();

			MalisisRegistry.registerItemRenderer(Items.rustyHandle, RustyHatchRenderer.instance);

			ResourceLocation rl = new ResourceLocation(MalisisDoors.modid + ":fonts/digital-7 (mono).ttf");
			MalisisDoors.digitalFont = new MalisisFont(rl);
		}
	}

	public static class Blocks
	{
		//Vanilla Blocks
		public static Door doorOak;
		public static Door doorAcacia;
		public static Door doorBirch;
		public static Door doorDarkOak;
		public static Door doorJungle;
		public static Door doorSpruce;
		public static Door doorIron;

		public static TrapDoor oakTrapDoor;
		public static TrapDoor ironTrapDoor;

		public static FenceGate oakFenceGate;
		public static FenceGate acaciaFenceGate;
		public static FenceGate birchFenceGate;
		public static FenceGate darkOakFenceGate;
		public static FenceGate jungleFenceGate;
		public static FenceGate spruceFenceGate;

		//MalisisDoors doors
		public static Door woodSlidingDoor;
		public static Door ironSlidingDoor;
		public static Door jailDoor;
		public static Door laboratoryDoor;
		public static Door factoryDoor;
		public static Door shojiDoor;
		public static Door curtains;

		//MalisisDoors trapdoors
		public static TrapDoor trapdoorAcacia;
		public static TrapDoor trapdoorBirch;
		public static TrapDoor trapdoorDarkOak;
		public static TrapDoor trapdoorJungle;
		public static TrapDoor trapdoorSpruce;

		//Special doors
		public static CustomDoor customDoor;
		public static SaloonDoorBlock saloonDoor;
		public static BigDoor carriageDoor;
		public static BigDoor medievalDoor;
		public static VerticalHatchDoor verticalHatch;

		public static TrapDoor slidingTrapDoor;
		public static FenceGate camoFenceGate;
		public static Forcefield forcefieldDoor;
		public static RustyHatch rustyHatch;
		public static GarageDoor garageDoor;

		//MalisisDoors blocks
		public static DoorFactory doorFactory;
		public static RustyLadder rustyLadder;

		//3x2 door
		public static ModelDoor modelDoor;
	}

	public static class Items
	{
		//Vanilla items
		public static DoorItem doorOakItem;
		public static DoorItem doorAcaciaItem;
		public static DoorItem doorBirchItem;
		public static DoorItem doorDarkOakItem;
		public static DoorItem doorJungleItem;
		public static DoorItem doorSpruceItem;
		public static DoorItem doorIronItem;

		//MalisisDoors door items
		public static DoorItem woodSlidingDoorItem;
		public static DoorItem ironSlidingDoorItem;
		public static DoorItem jailDoorItem;
		public static DoorItem laboratoryDoorItem;
		public static DoorItem factoryDoorItem;
		public static DoorItem shojiDoorItem;
		public static DoorItem curtainsItem;

		//MalisisDoors trapdoors items
		public static Item trapdoorAcaciaItem;
		public static Item trapdoorBirchItem;
		public static Item trapdoorDarkOakItem;
		public static Item trapdoorJungleItem;
		public static Item trapdoorSpruceItem;
		public static Item slidingTrapDoorItem;

		//Special door items
		public static CustomDoorItem customDoorItem;
		public static SaloonDoorItem saloonDoorItem;
		public static VerticalHatchItem verticalHatchItem;

		//Other items
		public static MalisisItem rustyHandle;
		public static ForcefieldItem forcefieldItem;
	}

}
