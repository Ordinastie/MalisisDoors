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
import net.malisis.core.util.replacement.ReplacementTool;
import net.malisis.doors.block.BlockMixer;
import net.malisis.doors.block.DoorFactory;
import net.malisis.doors.block.GarageDoor;
import net.malisis.doors.block.MixedBlock;
import net.malisis.doors.block.PlayerSensor;
import net.malisis.doors.block.RustyLadder;
import net.malisis.doors.block.VanishingBlock;
import net.malisis.doors.block.VanishingDiamondBlock;
import net.malisis.doors.door.DoorDescriptor;
import net.malisis.doors.door.block.BigDoor;
import net.malisis.doors.door.block.CustomDoor;
import net.malisis.doors.door.block.FenceGate;
import net.malisis.doors.door.block.FenceGate.Type;
import net.malisis.doors.door.block.ForcefieldDoor;
import net.malisis.doors.door.block.RustyHatch;
import net.malisis.doors.door.block.SaloonDoorBlock;
import net.malisis.doors.door.descriptor.Curtain;
import net.malisis.doors.door.descriptor.FactoryDoor;
import net.malisis.doors.door.descriptor.GlassDoor;
import net.malisis.doors.door.descriptor.JailDoor;
import net.malisis.doors.door.descriptor.LaboratoryDoor;
import net.malisis.doors.door.descriptor.SaloonDoor;
import net.malisis.doors.door.descriptor.ShojiDoor;
import net.malisis.doors.door.descriptor.VanillaDoor;
import net.malisis.doors.door.descriptor.WoodDoor;
import net.malisis.doors.door.item.CustomDoorItem;
import net.malisis.doors.door.item.DoorItem;
import net.malisis.doors.door.item.ForcefieldItem;
import net.malisis.doors.door.tileentity.BigDoorTileEntity;
import net.malisis.doors.door.tileentity.CustomDoorTileEntity;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.malisis.doors.door.tileentity.FenceGateTileEntity;
import net.malisis.doors.door.tileentity.ForcefieldTileEntity;
import net.malisis.doors.door.tileentity.RustyHatchTileEntity;
import net.malisis.doors.door.tileentity.SaloonDoorTileEntity;
import net.malisis.doors.entity.BlockMixerTileEntity;
import net.malisis.doors.entity.DoorFactoryTileEntity;
import net.malisis.doors.entity.GarageDoorTileEntity;
import net.malisis.doors.entity.MixedBlockTileEntity;
import net.malisis.doors.entity.VanishingDiamondTileEntity;
import net.malisis.doors.entity.VanishingTileEntity;
import net.malisis.doors.item.MixedBlockBlockItem;
import net.malisis.doors.item.VanishingBlockItem;
import net.malisis.doors.recipe.BigDoorRecipe;
import net.malisis.doors.trapdoor.descriptor.IronTrapDoor;
import net.malisis.doors.trapdoor.descriptor.SlidingTrapDoor;
import net.malisis.doors.trapdoor.descriptor.VanillaTrapDoor;
import net.malisis.doors.trapdoor.descriptor.WoodTrapDoor;
import net.malisis.doors.trapdoor.tileentity.TrapDoorTileEntity;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import cpw.mods.fml.common.registry.GameRegistry;

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

		registerWoodDoors();

		registerDoors();

		registerSaloonDoor();

		registerFenceGates();

		registerTrapDoors();

		registerPlayerSensor();

		if (MalisisDoorsSettings.enableVanishingBlocks.get())
			registerVanishingBlock();

		if (MalisisDoorsSettings.enableMixedBlocks.get())
			registerMixedBlock();

		registerGarageDoor();

		registerDoorFactory();

		registerCustomDoor();

		registerRustyHatch();

		registerBigDoors();

		registerForcefieldDoor();

		GameRegistry.registerTileEntity(DoorTileEntity.class, "doorTileEntity");
		GameRegistry.registerTileEntity(TrapDoorTileEntity.class, "trapDoorTileEntity");
		GameRegistry.registerTileEntity(FenceGateTileEntity.class, "fenceGateTileEntity");
	}

	private static void registerVanillaDoors()
	{
		VanillaDoor woodDoor = new VanillaDoor(Material.wood);
		woodDoor.create();
		ReplacementTool.replaceVanillaItem(324, "wooden_door", "field_151135_aq", woodDoor.getItem(), Items.wooden_door);
		ReplacementTool.replaceVanillaBlock(64, "wooden_door", "field_150466_ao", woodDoor.getBlock(), Blocks.wooden_door);

		VanillaDoor ironDoor = new VanillaDoor(Material.iron);
		ironDoor.create();
		ReplacementTool.replaceVanillaItem(330, "iron_door", "field_151139_aw", ironDoor.getItem(), Items.iron_door);
		ReplacementTool.replaceVanillaBlock(71, "iron_door", "field_150454_av", ironDoor.getBlock(), Blocks.iron_door);
	}

	private static void registerVanillaTrapDoor()
	{
		VanillaTrapDoor vanillaTrapDoor = new VanillaTrapDoor();
		vanillaTrapDoor.create();
		ReplacementTool.replaceVanillaBlock(96, "trapdoor", "field_150415_aT", vanillaTrapDoor.getBlock(), Blocks.trapdoor);
	}

	private static void registerVanillaFenceGate()
	{
		oakFenceGate = new FenceGate(Type.OAK);
		ReplacementTool.replaceVanillaBlock(107, "fence_gate", "field_150396_be", oakFenceGate, Blocks.fence_gate);
	}

	private static void registerWoodDoors()
	{
		DoorDescriptor desc;

		desc = new WoodDoor("door_acacia", 4).register();
		doorAcacia = desc.getBlock();
		doorAcaciaItem = desc.getItem();

		desc = new WoodDoor("door_birch", 2).register();
		doorBirch = desc.getBlock();
		doorBirchItem = desc.getItem();

		desc = new WoodDoor("door_dark_oak", 5).register();
		doorDarkOak = desc.getBlock();
		doorDarkOakItem = desc.getItem();

		desc = new WoodDoor("door_jungle", 3).register();
		doorJungle = desc.getBlock();
		doorJungleItem = desc.getItem();

		desc = new WoodDoor("door_spruce", 1).register();
		doorSpruce = desc.getBlock();
		doorSpruceItem = desc.getItem();
	}

	private static void registerDoors()
	{
		DoorDescriptor desc;

		//Glass Doors
		desc = new GlassDoor(Material.wood).register();
		woodSlidingDoor = desc.getBlock();
		woodSlidingDoorItem = desc.getItem();

		desc = new GlassDoor(Material.iron).register();
		ironSlidingDoor = desc.getBlock();
		ironSlidingDoorItem = desc.getItem();

		//Jail Door
		desc = new JailDoor().register();
		jailDoor = desc.getBlock();
		jailDoorItem = desc.getItem();

		//Laboratory Door
		desc = new LaboratoryDoor().register();
		laboratoryDoor = desc.getBlock();
		laboratoryDoorItem = desc.getItem();

		//Factory Door
		desc = new FactoryDoor().register();
		factoryDoor = desc.getBlock();
		factoryDoorItem = desc.getItem();

		//Shoji Door
		desc = new ShojiDoor().register();
		shojiDoor = desc.getBlock();
		shojiDoorItem = desc.getItem();

		//Curtains
		for (int i = 0; i < ItemDye.dyeIcons.length; i++)
			new Curtain(ItemDye.dyeIcons[i], ~i & 15).register();
	}

	private static void registerSaloonDoor()
	{
		DoorDescriptor desc = new SaloonDoor();
		saloonDoor = new SaloonDoorBlock(desc);
		saloonDoorItem = new DoorItem(desc);

		desc.set(saloonDoor, saloonDoorItem);
		desc.register();

		GameRegistry.registerTileEntity(SaloonDoorTileEntity.class, "saloonDoorTileEntity");
	}

	private static void registerFenceGates()
	{
		acaciaFenceGate = new FenceGate(FenceGate.Type.ACACIA).register();
		birchFenceGate = new FenceGate(FenceGate.Type.BIRCH).register();
		darkOakFenceGate = new FenceGate(FenceGate.Type.DARK_OAK).register();
		jungleFenceGate = new FenceGate(FenceGate.Type.JUNGLE).register();
		spruceFenceGate = new FenceGate(FenceGate.Type.SPRUCE).register();
		camoFenceGate = new FenceGate(FenceGate.Type.CAMO).register();
	}

	private static void registerTrapDoors()
	{
		DoorDescriptor desc;

		desc = new IronTrapDoor().register();
		ironTrapDoor = desc.getBlock();
		ironTrapDoorItem = desc.getItem();

		desc = new SlidingTrapDoor().register();
		slidingTrapDoor = desc.getBlock();
		slidingTrapDoorItem = desc.getItem();

		for (WoodTrapDoor.Type type : WoodTrapDoor.Type.values())
		{
			desc = new WoodTrapDoor(type).register();
		}
	}

	private static void registerPlayerSensor()
	{
		playerSensor = new PlayerSensor();

		GameRegistry.registerBlock(playerSensor, playerSensor.getUnlocalizedName().substring(5));

		// Sensor recipe
		GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(playerSensor), "ABA", "CCC", 'A', Items.iron_ingot, 'B', Items.redstone,
				'C', "blockGlassColorless"));
	}

	private static void registerVanishingBlock()
	{
		vanishingBlock = new VanishingBlock();
		vanishingDiamondBlock = new VanishingDiamondBlock();

		GameRegistry.registerBlock(vanishingBlock, VanishingBlockItem.class, vanishingBlock.getUnlocalizedName().substring(5));
		GameRegistry.registerBlock(vanishingDiamondBlock, vanishingDiamondBlock.getUnlocalizedName().substring(5));

		// Vanishing Block Recipes
		GameRegistry.addRecipe(new ItemStack(vanishingBlock, 4, 0), "ABA", "BCB", "ABA", 'A', Items.redstone, 'B', Items.stick, 'C',
				Items.ender_pearl);
		GameRegistry.addRecipe(new ItemStack(vanishingBlock, 4, 1), "ABA", "BCB", "ABA", 'A', Items.redstone, 'B', Items.iron_ingot, 'C',
				Items.ender_pearl);
		GameRegistry.addRecipe(new ItemStack(vanishingBlock, 4, 2), "ABA", "BCB", "ABA", 'A', Items.redstone, 'B', Items.gold_ingot, 'C',
				Items.ender_pearl);
		GameRegistry.addRecipe(new ItemStack(vanishingBlock, 4, 3), "ABA", "BCB", "ABA", 'A', Items.redstone, 'B', Items.diamond, 'C',
				Items.ender_pearl);

		GameRegistry.registerTileEntity(VanishingTileEntity.class, "vanishingTileEntity");
		GameRegistry.registerTileEntity(VanishingDiamondTileEntity.class, "vanishingDiamondTileEntity");

	}

	private static void registerMixedBlock()
	{
		blockMixer = new BlockMixer();
		mixedBlock = new MixedBlock();

		GameRegistry.registerBlock(blockMixer, blockMixer.getUnlocalizedName().substring(5));
		GameRegistry.registerBlock(mixedBlock, MixedBlockBlockItem.class, mixedBlock.getUnlocalizedName().substring(5));

		// Block Mixer recipe
		GameRegistry.addRecipe(new ItemStack(blockMixer), "AAA", "B B", "AAA", 'A', Items.iron_ingot, 'B', Blocks.piston);

		GameRegistry.registerTileEntity(BlockMixerTileEntity.class, "blockMixerTileEntity");
		GameRegistry.registerTileEntity(MixedBlockTileEntity.class, "mixedBlockTileEntity");
	}

	private static void registerGarageDoor()
	{
		garageDoor = new GarageDoor();

		GameRegistry.registerBlock(garageDoor, garageDoor.getUnlocalizedName().substring(5));

		GameRegistry.registerTileEntity(GarageDoorTileEntity.class, "garageDoorTileEntity");

		GameRegistry.addRecipe(new ItemStack(garageDoor), "ABA", "AAA", 'A', Blocks.planks, 'B', Blocks.glass);
	}

	private static void registerDoorFactory()
	{
		doorFactory = new DoorFactory();
		doorFactory.register();

		GameRegistry.registerTileEntity(DoorFactoryTileEntity.class, "doorFactoryTileEntity");

		GameRegistry.addRecipe(new ItemStack(doorFactory), "ABA", "C C", "ADA", 'A', Items.iron_ingot, 'B', Items.iron_door, 'C',
				Items.redstone, 'D', Blocks.piston);
	}

	private static void registerCustomDoor()
	{
		customDoor = new CustomDoor();
		GameRegistry.registerBlock(customDoor, customDoor.getUnlocalizedName().substring(5));

		customDoorItem = new CustomDoorItem();
		GameRegistry.registerItem(customDoorItem, customDoorItem.getUnlocalizedName());

		GameRegistry.registerTileEntity(CustomDoorTileEntity.class, "customDoorTileEntity");
	}

	private static void registerRustyHatch()
	{
		rustyHatch = new RustyHatch();
		rustyHatch.register();

		rustyHandle = new Item()
		{
			@Override
			public void registerIcons(IIconRegister register)
			{};
		}.setUnlocalizedName("rustyHandle").setCreativeTab(MalisisDoors.tab);
		GameRegistry.registerItem(rustyHandle, rustyHandle.getUnlocalizedName());

		GameRegistry.registerTileEntity(RustyHatchTileEntity.class, "rustyHatchTileEntity");

		GameRegistry.addRecipe(new ItemStack(rustyHandle), "AAA", " A ", 'A', Items.iron_ingot);
		GameRegistry.addRecipe(new ItemStack(rustyHatch), "A ", "AB", "A ", 'A', Items.iron_ingot, 'B', rustyHandle);

		rustyLadder = new RustyLadder();
		GameRegistry.registerBlock(rustyLadder, rustyLadder.getUnlocalizedName().substring(5));
		GameRegistry.addRecipe(new ItemStack(rustyLadder), "AAA", 'A', Items.iron_ingot);
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
	}

	private static void registerForcefieldDoor()
	{
		forcefieldDoor = new ForcefieldDoor();
		GameRegistry.registerBlock(forcefieldDoor, forcefieldDoor.getUnlocalizedName().substring(5));

		forcefieldItem = new ForcefieldItem();
		GameRegistry.registerItem(forcefieldItem, forcefieldItem.getUnlocalizedName());

		GameRegistry.registerTileEntity(ForcefieldTileEntity.class, "forcefieldTileEntity");
		GameRegistry.addRecipe(new ItemStack(forcefieldItem), "ABA", "CDC", "AEA", 'A', Items.diamond, 'B', Blocks.obsidian, 'C',
				Items.repeater, 'D', Items.ender_eye, 'E', Items.comparator);
	}
}
