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
import net.malisis.core.MalisisCore;
import net.malisis.doors.block.BlockMixer;
import net.malisis.doors.block.DoorFactory;
import net.malisis.doors.block.FenceGate;
import net.malisis.doors.block.GarageDoor;
import net.malisis.doors.block.MixedBlock;
import net.malisis.doors.block.PlayerSensor;
import net.malisis.doors.block.RustyHatch;
import net.malisis.doors.block.TrapDoor;
import net.malisis.doors.block.VanishingBlock;
import net.malisis.doors.block.VanishingDiamondBlock;
import net.malisis.doors.door.CustomDoor;
import net.malisis.doors.door.CustomDoorItem;
import net.malisis.doors.door.Door;
import net.malisis.doors.door.DoorDescriptor;
import net.malisis.doors.door.descriptor.FactoryDoor;
import net.malisis.doors.door.descriptor.GlassDoor;
import net.malisis.doors.door.descriptor.JailDoor;
import net.malisis.doors.door.descriptor.LaboratoryDoor;
import net.malisis.doors.door.descriptor.ShojiDoor;
import net.malisis.doors.door.descriptor.VanillaDoor;
import net.malisis.doors.door.tileentity.CustomDoorTileEntity;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.malisis.doors.door.tileentity.FenceGateTileEntity;
import net.malisis.doors.door.tileentity.TrapDoorTileEntity;
import net.malisis.doors.entity.BlockMixerTileEntity;
import net.malisis.doors.entity.DoorFactoryTileEntity;
import net.malisis.doors.entity.GarageDoorTileEntity;
import net.malisis.doors.entity.MixedBlockTileEntity;
import net.malisis.doors.entity.RustyHatchTileEntity;
import net.malisis.doors.entity.VanishingDiamondTileEntity;
import net.malisis.doors.entity.VanishingTileEntity;
import net.malisis.doors.item.MixedBlockBlockItem;
import net.malisis.doors.item.VanishingBlockItem;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.GameRegistry;

public class Registers
{
	public static void init()
	{
		if (MalisisDoorsSettings.modifyVanillaDoors.get())
			registerVanillaDoors();

		registerDoors();

		registerPlayerSensor();

		if (MalisisDoorsSettings.enableVanishingBlocks.get())
			registerVanishingBlock();

		if (MalisisDoorsSettings.enableMixedBlocks.get())
			registerMixedBlock();

		registerGarageDoor();

		registerDoorFactory();

		registerCustomDoor();

		registerRustyHatch();

		GameRegistry.registerTileEntity(DoorTileEntity.class, "doorTileEntity");
	}

	private static void registerVanillaDoors()
	{
		VanillaDoor woodDoor = new VanillaDoor(Material.wood);
		woodDoor.set(new Door(woodDoor), Items.wooden_door);
		MalisisCore.replaceVanillaBlock(64, "wooden_door", "field_150466_ao", woodDoor.getBlock(), Blocks.wooden_door);
		doubleDoorWood = woodDoor.getBlock();

		VanillaDoor ironDoor = new VanillaDoor(Material.iron);
		ironDoor.set(new Door(ironDoor), Items.iron_door);
		MalisisCore.replaceVanillaBlock(71, "iron_door", "field_150454_av", ironDoor.getBlock(), Blocks.iron_door);
		doubleDoorIron = ironDoor.getBlock();

		fenceGate = new FenceGate();
		trapDoor = new TrapDoor();

		MalisisCore.replaceVanillaBlock(107, "fence_gate", "field_150396_be", fenceGate, Blocks.fence_gate);
		MalisisCore.replaceVanillaBlock(96, "trapdoor", "field_150415_aT", trapDoor, Blocks.trapdoor);

		GameRegistry.registerTileEntity(FenceGateTileEntity.class, "fenceGateTileEntity");
		GameRegistry.registerTileEntity(TrapDoorTileEntity.class, "trapDoorTileEntity");
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
		facortyDoorItem = desc.getItem();

		desc = new ShojiDoor().register();
		shojiDoor = desc.getBlock();
		shojiDoorItem = desc.getItem();

	}

	private static void registerPlayerSensor()
	{
		playerSensor = new PlayerSensor();

		GameRegistry.registerBlock(playerSensor, playerSensor.getUnlocalizedName().substring(5));

		// Sensor recipe
		GameRegistry.addRecipe(new ItemStack(playerSensor), new Object[] { "ABA", "CCC", 'A', Items.iron_ingot, 'B', Items.redstone, 'C',
				Blocks.glass });
	}

	private static void registerVanishingBlock()
	{
		vanishingBlock = new VanishingBlock();
		vanishingDiamondBlock = new VanishingDiamondBlock();

		GameRegistry.registerBlock(vanishingBlock, VanishingBlockItem.class, vanishingBlock.getUnlocalizedName().substring(5));
		GameRegistry.registerBlock(vanishingDiamondBlock, vanishingDiamondBlock.getUnlocalizedName().substring(5));

		// Vanishing Block Recipes
		GameRegistry.addRecipe(new ItemStack(vanishingBlock, 4, 0), new Object[] { "ABA", "BCB", "ABA", 'A', Items.redstone, 'B',
				Items.stick, 'C', Items.ender_pearl });
		GameRegistry.addRecipe(new ItemStack(vanishingBlock, 4, 1), new Object[] { "ABA", "BCB", "ABA", 'A', Items.redstone, 'B',
				Items.iron_ingot, 'C', Items.ender_pearl });
		GameRegistry.addRecipe(new ItemStack(vanishingBlock, 4, 2), new Object[] { "ABA", "BCB", "ABA", 'A', Items.redstone, 'B',
				Items.gold_ingot, 'C', Items.ender_pearl });
		GameRegistry.addRecipe(new ItemStack(vanishingBlock, 4, 3), new Object[] { "ABA", "BCB", "ABA", 'A', Items.redstone, 'B',
				Items.diamond, 'C', Items.ender_pearl });

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
		GameRegistry.addRecipe(new ItemStack(blockMixer), new Object[] { "AAA", "B B", "AAA", 'A', Items.iron_ingot, 'B', Blocks.piston });

		GameRegistry.registerTileEntity(BlockMixerTileEntity.class, "blockMixerTileEntity");
		GameRegistry.registerTileEntity(MixedBlockTileEntity.class, "mixedBlockTileEntity");
	}

	private static void registerGarageDoor()
	{
		garageDoor = new GarageDoor();

		GameRegistry.registerBlock(garageDoor, garageDoor.getUnlocalizedName().substring(5));

		GameRegistry.registerTileEntity(GarageDoorTileEntity.class, "garageDoorTileEntity");

		GameRegistry.addRecipe(new ItemStack(garageDoor), new Object[] { "ABA", "AAA", 'A', Blocks.planks, 'B', Blocks.glass });
	}

	private static void registerDoorFactory()
	{
		doorFactory = new DoorFactory();
		GameRegistry.registerBlock(doorFactory, doorFactory.getUnlocalizedName().substring(5));

		GameRegistry.registerTileEntity(DoorFactoryTileEntity.class, "doorFactoryTileEntity");

		GameRegistry.addRecipe(new ItemStack(doorFactory), new Object[] { "ABA", "C C", "ADA", 'A', Items.iron_ingot, 'B', Items.iron_door,
				'C', Items.redstone, 'D', Blocks.piston });
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
		GameRegistry.registerBlock(rustyHatch, rustyHatch.getUnlocalizedName().substring(5));

		GameRegistry.registerTileEntity(RustyHatchTileEntity.class, "rustyHatchTileEntity");

	}
}
