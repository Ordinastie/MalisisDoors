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
import net.malisis.doors.block.GarageDoor;
import net.malisis.doors.block.MixedBlock;
import net.malisis.doors.block.PlayerSensor;
import net.malisis.doors.block.VanishingBlock;
import net.malisis.doors.block.VanishingDiamondBlock;
import net.malisis.doors.door.block.CustomDoor;
import net.malisis.doors.door.block.FactoryDoor;
import net.malisis.doors.door.block.FenceGate;
import net.malisis.doors.door.block.GlassDoor;
import net.malisis.doors.door.block.JailDoor;
import net.malisis.doors.door.block.LaboratoryDoor;
import net.malisis.doors.door.block.TrapDoor;
import net.malisis.doors.door.block.VanillaDoor;
import net.malisis.doors.door.item.CustomDoorItem;
import net.malisis.doors.door.item.JailDoorItem;
import net.malisis.doors.door.item.SlidingDoorItem;
import net.malisis.doors.door.item.LaboratoryDoorItem;
import net.malisis.doors.door.tileentity.CustomDoorTileEntity;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.malisis.doors.door.tileentity.FenceGateTileEntity;
import net.malisis.doors.door.tileentity.TrapDoorTileEntity;
import net.malisis.doors.entity.BlockMixerTileEntity;
import net.malisis.doors.entity.DoorFactoryTileEntity;
import net.malisis.doors.entity.GarageDoorTileEntity;
import net.malisis.doors.entity.MixedBlockTileEntity;
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

		registerSlidingDoors();
		registerPlayerSensor();

		if (MalisisDoorsSettings.enableVanishingBlocks.get())
			registerVanishingBlock();

		if (MalisisDoorsSettings.enableMixedBlocks.get())
			registerMixedBlock();

		registerGarageDoor();

		registerJailDoor();

		registerLaboratoryDoor();

		registerFactoryDoor();

		registerDoorFactory();

		registerCustomDoor();

		GameRegistry.registerTileEntity(DoorTileEntity.class, "doorTileEntity");
	}

	private static void registerVanillaDoors()
	{
		doubleDoorWood = new VanillaDoor(Material.wood);
		doubleDoorIron = new VanillaDoor(Material.iron);
		fenceGate = new FenceGate();
		trapDoor = new TrapDoor();

		MalisisCore.replaceVanillaBlock(64, "wooden_door", "field_150466_ao", doubleDoorWood, Blocks.wooden_door);
		MalisisCore.replaceVanillaBlock(71, "iron_door", "field_150454_av", doubleDoorIron, Blocks.iron_door);

		MalisisCore.replaceVanillaBlock(107, "fence_gate", "field_150396_be", fenceGate, Blocks.fence_gate);
		MalisisCore.replaceVanillaBlock(96, "trapdoor", "field_150415_aT", trapDoor, Blocks.trapdoor);

		GameRegistry.registerTileEntity(FenceGateTileEntity.class, "fenceGateTileEntity");
		GameRegistry.registerTileEntity(TrapDoorTileEntity.class, "trapDoorTileEntity");
	}

	private static void registerSlidingDoors()
	{
		// Sliding Door blocks
		woodSlidingDoor = new GlassDoor(Material.wood);
		ironSlidingDoor = new GlassDoor(Material.iron);

		GameRegistry.registerBlock(woodSlidingDoor, woodSlidingDoor.getUnlocalizedName().substring(5));
		GameRegistry.registerBlock(ironSlidingDoor, ironSlidingDoor.getUnlocalizedName().substring(5));

		// Sliding Door items
		woodSlidingDoorItem = new SlidingDoorItem(Material.wood);
		ironSlidingDoorItem = new SlidingDoorItem(Material.iron);

		GameRegistry.registerItem(woodSlidingDoorItem, woodSlidingDoorItem.getUnlocalizedName());
		GameRegistry.registerItem(ironSlidingDoorItem, ironSlidingDoorItem.getUnlocalizedName());

		// Sliding Door recipes
		GameRegistry
				.addRecipe(new ItemStack(woodSlidingDoorItem), new Object[] { "AB", "AB", "AB", 'A', Blocks.planks, 'B', Blocks.glass });
		GameRegistry.addRecipe(new ItemStack(ironSlidingDoorItem), new Object[] { "AB", "AB", "AB", 'A', Items.iron_ingot, 'B',
				Blocks.glass });

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
		vanishingBlock = (new VanishingBlock()).setBlockName("vanishing_block");
		vanishingDiamondBlock = (new VanishingDiamondBlock()).setBlockName("vanishing_block_diamond");

		GameRegistry.registerBlock(vanishingBlock, VanishingBlockItem.class, vanishingBlock.getUnlocalizedName().substring(5));
		GameRegistry.registerBlock(vanishingDiamondBlock, vanishingDiamondBlock.getUnlocalizedName().substring(5));

		// Vanishing Block Recipes
		GameRegistry.addRecipe(new ItemStack(vanishingBlock, 4, 0), new Object[] { "ABA", "BCB", "ABA", 'A', Items.redstone, 'B',
				Items.stick, 'C', Items.ender_pearl });
		GameRegistry.addRecipe(new ItemStack(vanishingBlock, 4, 1), new Object[] { "ABA", "BCB", "ABA", 'A', Items.redstone, 'B',
				Items.iron_ingot, 'C', Items.ender_pearl });
		GameRegistry.addRecipe(new ItemStack(vanishingBlock, 4, 2), new Object[] { "ABA", "BCB", "ABA", 'A', Items.redstone, 'B',
				Items.gold_ingot, 'C', Items.ender_pearl });
		GameRegistry.addRecipe(new ItemStack(vanishingDiamondBlock, 4, 3), new Object[] { "ABA", "BCB", "ABA", 'A', Items.redstone, 'B',
				Items.diamond, 'C', Items.ender_pearl });

		GameRegistry.registerTileEntity(VanishingTileEntity.class, "vanishingTileEntity");
		GameRegistry.registerTileEntity(VanishingDiamondTileEntity.class, "vanishingDiamondTileEntity");

	}

	private static void registerMixedBlock()
	{
		blockMixer = (new BlockMixer()).setBlockName("block_mixer");
		mixedBlock = (new MixedBlock()).setBlockName("mixed_block");

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

	private static void registerJailDoor()
	{
		//Block
		jailDoor = new JailDoor();
		GameRegistry.registerBlock(jailDoor, jailDoor.getUnlocalizedName().substring(5));

		//Item
		jailDoorItem = new JailDoorItem();
		GameRegistry.registerItem(jailDoorItem, jailDoorItem.getUnlocalizedName());

		//Recipes
		GameRegistry.addRecipe(new ItemStack(jailDoorItem), new Object[] { "AA", "AA", "AA", 'A', Blocks.iron_bars });
	}

	private static void registerLaboratoryDoor()
	{
		//Block
		laboratoryDoor = new LaboratoryDoor();
		GameRegistry.registerBlock(laboratoryDoor, laboratoryDoor.getUnlocalizedName().substring(5));

		//Item
		laboratoryDoorItem = new LaboratoryDoorItem(false);
		GameRegistry.registerItem(laboratoryDoorItem, laboratoryDoorItem.getUnlocalizedName());

		//Recipes
		GameRegistry.addRecipe(new ItemStack(laboratoryDoorItem), new Object[] { "AA", "BB", "BB", 'A', Items.gold_ingot, 'B',
				Items.iron_ingot });
	}

	private static void registerFactoryDoor()
	{
		//Block
		factoryDoor = new FactoryDoor();
		GameRegistry.registerBlock(factoryDoor, factoryDoor.getUnlocalizedName().substring(5));

		//Item
		facortyDoorItem = new LaboratoryDoorItem(true);
		GameRegistry.registerItem(facortyDoorItem, facortyDoorItem.getUnlocalizedName());

		//Recipes
		GameRegistry.addRecipe(new ItemStack(facortyDoorItem), new Object[] { "AA", "BB", "AA", 'A', Items.gold_ingot, 'B',
				Items.iron_ingot });
	}

	private static void registerDoorFactory()
	{
		doorFactory = new DoorFactory();
		GameRegistry.registerBlock(doorFactory, doorFactory.getUnlocalizedName().substring(5));

		GameRegistry.registerTileEntity(DoorFactoryTileEntity.class, "doorFactoryTileEntity");

		GameRegistry.addRecipe(new ItemStack(doorFactory), new Object[] { "ABA", "C C", "ADA", 'A', Blocks.planks, 'B', Items.iron_door,
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
}
