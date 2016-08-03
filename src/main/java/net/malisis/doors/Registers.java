/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.malisis.doors;

import static net.malisis.doors.MalisisDoors.Blocks.*;
import static net.malisis.doors.MalisisDoors.Items.*;
import static net.minecraftforge.oredict.RecipeSorter.Category.*;
import net.malisis.core.MalisisCore;
import net.malisis.core.MalisisRegistry;
import net.malisis.core.item.MalisisItem;
import net.malisis.core.util.replacement.ReplacementTool;
import net.malisis.doors.block.BigDoor;
import net.malisis.doors.block.CustomDoor;
import net.malisis.doors.block.Door;
import net.malisis.doors.block.DoorFactory;
import net.malisis.doors.block.FenceGate;
import net.malisis.doors.block.Forcefield;
import net.malisis.doors.block.GarageDoor;
import net.malisis.doors.block.RustyHatch;
import net.malisis.doors.block.RustyLadder;
import net.malisis.doors.block.SaloonDoorBlock;
import net.malisis.doors.block.TrapDoor;
import net.malisis.doors.block.VerticalHatchDoor;
import net.malisis.doors.descriptor.Curtain;
import net.malisis.doors.descriptor.FactoryDoor;
import net.malisis.doors.descriptor.GlassDoor;
import net.malisis.doors.descriptor.JailDoor;
import net.malisis.doors.descriptor.LaboratoryDoor;
import net.malisis.doors.descriptor.SaloonDoor;
import net.malisis.doors.descriptor.ShojiDoor;
import net.malisis.doors.descriptor.SlidingTrapDoor;
import net.malisis.doors.descriptor.VanillaDoor;
import net.malisis.doors.descriptor.VanillaTrapDoor;
import net.malisis.doors.descriptor.VanillaTrapDoor.Type;
import net.malisis.doors.descriptor.VerticalHatch;
import net.malisis.doors.descriptor.WoodTrapDoor;
import net.malisis.doors.item.CustomDoorItem;
import net.malisis.doors.item.DoorItem;
import net.malisis.doors.item.ForcefieldItem;
import net.malisis.doors.item.SaloonDoorItem;
import net.malisis.doors.item.VerticalHatchItem;
import net.malisis.doors.recipe.BigDoorRecipe;
import net.malisis.doors.tileentity.BigDoorTileEntity;
import net.malisis.doors.tileentity.CustomDoorTileEntity;
import net.malisis.doors.tileentity.DoorFactoryTileEntity;
import net.malisis.doors.tileentity.DoorTileEntity;
import net.malisis.doors.tileentity.FenceGateTileEntity;
import net.malisis.doors.tileentity.ForcefieldTileEntity;
import net.malisis.doors.tileentity.GarageDoorTileEntity;
import net.malisis.doors.tileentity.RustyHatchTileEntity;
import net.malisis.doors.tileentity.SaloonDoorTileEntity;
import net.malisis.doors.tileentity.TrapDoorTileEntity;
import net.malisis.doors.tileentity.VerticalHatchTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.RecipeSorter;

public class Registers
{
	public static void init()
	{
		if (MalisisDoorsSettings.modifyVanillaDoors.get())
		{
			registerVanillaDoors();
			registerVanillaTrapDoor();
			registerVanillaFenceGate();
		}

		registerDoors();

		registerCustomDoor();

		registerSaloonDoor();

		registerVerticalHatch();

		registerBigDoors();

		registerTrapDoors();

		registerCamoFenceGate();

		registerGarageDoor();

		registerDoorFactory();

		registerRustyHatch();

		registerForcefield();

		GameRegistry.registerTileEntity(DoorTileEntity.class, "doorTileEntity");
		GameRegistry.registerTileEntity(TrapDoorTileEntity.class, "trapDoorTileEntity");
		GameRegistry.registerTileEntity(FenceGateTileEntity.class, "fenceGateTileEntity");
	}

	private static void registerVanillaDoors()
	{
		//Note : for 1.9.4/1.10.2 compat, doors can't be referenced directly (field type changed from Block to BlockDoor)

		VanillaDoor oakDoor = new VanillaDoor(VanillaDoor.Type.OAK);
		oakDoor.create();
		doorOak = (Door) oakDoor.getBlock();
		doorOakItem = (DoorItem) oakDoor.getItem();
		ReplacementTool.replaceVanillaItem(324, "wooden_door", "OAK_DOOR", "field_179570_aq", oakDoor.getItem(), Items.OAK_DOOR);
		ReplacementTool.replaceVanillaBlock(64,
				"wooden_door",
				"OAK_DOOR",
				"field_180413_ao",
				oakDoor.getBlock(),
				Block.getBlockFromName("wooden_door"));

		VanillaDoor acaciaDoor = new VanillaDoor(VanillaDoor.Type.ACACIA);
		acaciaDoor.create();
		doorAcacia = (Door) acaciaDoor.getBlock();
		doorAcaciaItem = (DoorItem) acaciaDoor.getItem();
		ReplacementTool.replaceVanillaItem(430, "acacia_door", "ACACIA_DOOR", "field_179572_au", acaciaDoor.getItem(), Items.ACACIA_DOOR);
		ReplacementTool.replaceVanillaBlock(196,
				"acacia_door",
				"ACACIA_DOOR",
				"field_180410_as",
				acaciaDoor.getBlock(),
				Block.getBlockFromName("acacia_door"));

		VanillaDoor birchDoor = new VanillaDoor(VanillaDoor.Type.BIRCH);
		birchDoor.create();
		doorBirch = (Door) birchDoor.getBlock();
		doorBirchItem = (DoorItem) birchDoor.getItem();
		ReplacementTool.replaceVanillaItem(428, "birch_door", "BIRCH_DOOR", "field_179568_as", birchDoor.getItem(), Items.BIRCH_DOOR);
		ReplacementTool.replaceVanillaBlock(194,
				"birch_door",
				"BIRCH_DOOR",
				"field_180412_aq",
				birchDoor.getBlock(),
				Block.getBlockFromName("birch_door"));

		VanillaDoor darkOakDoor = new VanillaDoor(VanillaDoor.Type.DARK_OAK);
		darkOakDoor.create();
		doorDarkOak = (Door) darkOakDoor.getBlock();
		doorDarkOakItem = (DoorItem) darkOakDoor.getItem();
		ReplacementTool.replaceVanillaItem(431,
				"dark_oak_door",
				"DARK_OAK_DOOR",
				"field_179571_av",
				darkOakDoor.getItem(),
				Items.DARK_OAK_DOOR);
		ReplacementTool.replaceVanillaBlock(197,
				"dark_oak_door",
				"DARK_OAK_DOOR",
				"field_180409_at",
				darkOakDoor.getBlock(),
				Block.getBlockFromName("dark_oak_door"));

		VanillaDoor jungleDoor = new VanillaDoor(VanillaDoor.Type.JUNGLE);
		jungleDoor.create();
		doorJungle = (Door) jungleDoor.getBlock();
		doorJungleItem = (DoorItem) jungleDoor.getItem();
		ReplacementTool.replaceVanillaItem(429, "jungle_door", "JUNGLE_DOOR", "field_179567_at", jungleDoor.getItem(), Items.JUNGLE_DOOR);
		ReplacementTool.replaceVanillaBlock(195,
				"jungle_door",
				"JUNGLE_DOOR",
				"field_180411_ar",
				jungleDoor.getBlock(),
				Block.getBlockFromName("jungle_door"));

		VanillaDoor spruceDoor = new VanillaDoor(VanillaDoor.Type.SPRUCE);
		spruceDoor.create();
		doorSpruce = (Door) spruceDoor.getBlock();
		doorSpruceItem = (DoorItem) spruceDoor.getItem();
		ReplacementTool.replaceVanillaItem(427, "spruce_door", "SPRUCE_DOOR", "field_179569_ar", spruceDoor.getItem(), Items.SPRUCE_DOOR);
		ReplacementTool.replaceVanillaBlock(193,
				"spruce_door",
				"SPRUCE_DOOR",
				"field_180414_ap",
				spruceDoor.getBlock(),
				Block.getBlockFromName("spruce_door"));

		VanillaDoor ironDoor = new VanillaDoor(VanillaDoor.Type.IRON);
		ironDoor.create();
		doorIron = (Door) ironDoor.getBlock();
		doorIronItem = (DoorItem) ironDoor.getItem();
		ReplacementTool.replaceVanillaItem(330, "iron_door", "IRON_DOOR", "field_151139_aw", ironDoor.getItem(), Items.IRON_DOOR);
		ReplacementTool.replaceVanillaBlock(71,
				"iron_door",
				"IRON_DOOR",
				"field_150454_av",
				ironDoor.getBlock(),
				Block.getBlockFromName("iron_door"));

		if (MalisisCore.isClient())
		{
			MalisisRegistry.registerItemModel(doorOakItem, "minecraft:oak_door");
			MalisisRegistry.registerItemModel(doorAcaciaItem, "minecraft:acacia_door");
			MalisisRegistry.registerItemModel(doorBirchItem, "minecraft:birch_door");
			MalisisRegistry.registerItemModel(doorDarkOakItem, "minecraft:dark_oak_door");
			MalisisRegistry.registerItemModel(doorJungleItem, "minecraft:jungle_door");
			MalisisRegistry.registerItemModel(doorSpruceItem, "minecraft:spruce_door");
			MalisisRegistry.registerItemModel(doorIronItem, "minecraft:iron_door");
		}
	}

	private static void registerVanillaTrapDoor()
	{
		VanillaTrapDoor trapDoorOak = new VanillaTrapDoor(Type.WOOD);
		trapDoorOak.create();
		oakTrapDoor = (TrapDoor) trapDoorOak.getBlock();
		ReplacementTool.replaceVanillaBlock(96, "trapdoor", "TRAPDOOR", "field_150415_aT", trapDoorOak.getBlock(), Blocks.TRAPDOOR);

		VanillaTrapDoor trapDoorIron = new VanillaTrapDoor(Type.IRON);
		trapDoorIron.create();
		ironTrapDoor = (TrapDoor) trapDoorIron.getBlock();
		ReplacementTool.replaceVanillaBlock(167,
				"iron_trapdoor",
				"IRON_TRAPDOOR",
				"field_180400_cw",
				trapDoorIron.getBlock(),
				Blocks.IRON_TRAPDOOR);
	}

	private static void registerVanillaFenceGate()
	{
		oakFenceGate = new FenceGate(FenceGate.Type.OAK);
		ReplacementTool.replaceVanillaBlock(107, "fence_gate", "OAK_FENCE_GATE", "field_180390_bo", oakFenceGate, Blocks.OAK_FENCE_GATE);

		acaciaFenceGate = new FenceGate(FenceGate.Type.ACACIA);
		ReplacementTool.replaceVanillaBlock(187,
				"acacia_fence_gate",
				"ACACIA_FENCE_GATE",
				"field_180387_bt",
				acaciaFenceGate,
				Blocks.ACACIA_FENCE_GATE);

		birchFenceGate = new FenceGate(FenceGate.Type.BIRCH);
		ReplacementTool.replaceVanillaBlock(184,
				"birch_fence_gate",
				"BIRCH_FENCE_GATE",
				"field_180392_bq",
				birchFenceGate,
				Blocks.BIRCH_FENCE_GATE);

		darkOakFenceGate = new FenceGate(FenceGate.Type.DARK_OAK);
		ReplacementTool.replaceVanillaBlock(186,
				"dark_oak_fence_gate",
				"DARK_OAK_FENCE_GATE",
				"field_180385_bs",
				darkOakFenceGate,
				Blocks.DARK_OAK_FENCE_GATE);

		jungleFenceGate = new FenceGate(FenceGate.Type.JUNGLE);
		ReplacementTool.replaceVanillaBlock(185,
				"jungle_fence_gate",
				"JUNGLE_FENCE_GATE",
				"field_180386_br",
				jungleFenceGate,
				Blocks.JUNGLE_FENCE_GATE);

		spruceFenceGate = new FenceGate(FenceGate.Type.SPRUCE);
		ReplacementTool.replaceVanillaBlock(183,
				"spruce_fence_gate",
				"SPRUCE_FENCE_GATE",
				"field_180391_bp",
				spruceFenceGate,
				Blocks.SPRUCE_FENCE_GATE);
	}

	private static void registerDoors()
	{
		DoorDescriptor desc;

		//Glass Doors
		desc = new GlassDoor(Material.WOOD).register();
		woodSlidingDoor = (Door) desc.getBlock();
		woodSlidingDoorItem = (DoorItem) desc.getItem();

		desc = new GlassDoor(Material.IRON).register();
		ironSlidingDoor = (Door) desc.getBlock();
		ironSlidingDoorItem = (DoorItem) desc.getItem();

		//Jail Door
		desc = new JailDoor().register();
		jailDoor = (Door) desc.getBlock();
		jailDoorItem = (DoorItem) desc.getItem();

		//Laboratory Door
		desc = new LaboratoryDoor().register();
		laboratoryDoor = (Door) desc.getBlock();
		laboratoryDoorItem = (DoorItem) desc.getItem();

		//Factory Door
		desc = new FactoryDoor().register();
		factoryDoor = (Door) desc.getBlock();
		factoryDoorItem = (DoorItem) desc.getItem();

		//Shoji Door
		desc = new ShojiDoor().register();
		shojiDoor = (Door) desc.getBlock();
		shojiDoorItem = (DoorItem) desc.getItem();

		//Curtains
		for (EnumDyeColor color : EnumDyeColor.values())
			new Curtain(color).register();
	}

	private static void registerSaloonDoor()
	{
		DoorDescriptor desc = new SaloonDoor();
		saloonDoor = new SaloonDoorBlock(desc);
		saloonDoorItem = new SaloonDoorItem(desc);

		desc.set(saloonDoor, saloonDoorItem);
		desc.register();

		GameRegistry.registerTileEntity(SaloonDoorTileEntity.class, "saloonDoorTileEntity");
	}

	private static void registerVerticalHatch()
	{
		DoorDescriptor desc = new VerticalHatch();
		verticalHatch = new VerticalHatchDoor(desc);
		verticalHatchItem = new VerticalHatchItem(desc);

		desc.set(verticalHatch, verticalHatchItem);
		desc.register();

		GameRegistry.registerTileEntity(VerticalHatchTileEntity.class, "verticalHatchTileEntity");
	}

	private static void registerTrapDoors()
	{
		DoorDescriptor desc = new SlidingTrapDoor().register();
		slidingTrapDoor = (TrapDoor) desc.getBlock();
		slidingTrapDoorItem = desc.getItem();

		for (WoodTrapDoor.Type type : WoodTrapDoor.Type.values())
		{
			desc = new WoodTrapDoor(type).register();
		}
	}

	private static void registerCamoFenceGate()
	{
		camoFenceGate = new FenceGate(FenceGate.Type.CAMO);
		camoFenceGate.register();
		GameRegistry.addRecipe(new ItemStack(camoFenceGate),
				"ABC",
				'A',
				Blocks.ACACIA_FENCE_GATE,
				'B',
				Blocks.JUNGLE_FENCE_GATE,
				'C',
				Blocks.BIRCH_FENCE_GATE);
	}

	private static void registerGarageDoor()
	{
		garageDoor = new GarageDoor();
		garageDoor.register();

		GameRegistry.registerTileEntity(GarageDoorTileEntity.class, "garageDoorTileEntity");

		GameRegistry.addRecipe(new ItemStack(garageDoor), "ABA", "AAA", 'A', Blocks.PLANKS, 'B', Blocks.GLASS);
	}

	private static void registerDoorFactory()
	{
		doorFactory = new DoorFactory();
		doorFactory.register();

		GameRegistry.registerTileEntity(DoorFactoryTileEntity.class, "doorFactoryTileEntity");

		GameRegistry.addRecipe(new ItemStack(doorFactory),
				"ABA",
				"C C",
				"ADA",
				'A',
				Items.IRON_INGOT,
				'B',
				Items.IRON_DOOR,
				'C',
				Items.REDSTONE,
				'D',
				Blocks.PISTON);
	}

	private static void registerCustomDoor()
	{
		customDoor = new CustomDoor();
		customDoor.register();

		customDoorItem = new CustomDoorItem();
		customDoorItem.register();

		GameRegistry.registerTileEntity(CustomDoorTileEntity.class, "customDoorTileEntity");
	}

	private static void registerRustyHatch()
	{
		rustyHatch = new RustyHatch();
		rustyHatch.register();

		rustyHandle = new MalisisItem().setName("rustyHandle");
		rustyHandle.setCreativeTab(MalisisDoors.tab);
		rustyHandle.register();

		GameRegistry.registerTileEntity(RustyHatchTileEntity.class, "rustyHatchTileEntity");

		GameRegistry.addRecipe(new ItemStack(rustyHandle), "AAA", " A ", 'A', Items.IRON_INGOT);
		GameRegistry.addRecipe(new ItemStack(rustyHatch), "A ", "AB", "A ", 'A', Items.IRON_INGOT, 'B', rustyHandle);

		rustyLadder = new RustyLadder();
		rustyLadder.register();
		GameRegistry.addRecipe(new ItemStack(rustyLadder), "AAA", 'A', Items.IRON_INGOT);
	}

	private static void registerBigDoors()
	{
		carriageDoor = new BigDoor(BigDoor.Type.CARRIAGE);
		carriageDoor.register();

		medievalDoor = new BigDoor(BigDoor.Type.MEDIEVAL);
		medievalDoor.register();

		GameRegistry.registerTileEntityWithAlternatives(BigDoorTileEntity.class, "bigDoorTileEntity", "carriageDoorTileEntity");

		GameRegistry.addRecipe(new BigDoorRecipe(BigDoor.Type.CARRIAGE));
		GameRegistry.addRecipe(new BigDoorRecipe(BigDoor.Type.MEDIEVAL));

		RecipeSorter.register("malisisdoors:bigdoorsrecipe", BigDoorRecipe.class, SHAPELESS, "after:minecraft:shapeless");
	}

	private static void registerForcefield()
	{
		forcefieldDoor = new Forcefield();
		forcefieldDoor.register();

		forcefieldItem = new ForcefieldItem();
		forcefieldItem.register();

		GameRegistry.registerTileEntity(ForcefieldTileEntity.class, "forcefieldTileEntity");
		GameRegistry.addRecipe(new ItemStack(forcefieldItem),
				"ABA",
				"CDC",
				"AEA",
				'A',
				Items.DIAMOND,
				'B',
				Blocks.OBSIDIAN,
				'C',
				Items.REPEATER,
				'D',
				Items.ENDER_EYE,
				'E',
				Items.COMPARATOR);
	}
}
