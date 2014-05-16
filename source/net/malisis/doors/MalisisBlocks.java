package net.malisis.doors;


import net.malisis.core.MalisisCore;
import net.malisis.doors.block.BlockMixer;
import net.malisis.doors.block.Door;
import net.malisis.doors.block.MixedBlock;
import net.malisis.doors.block.PlayerSensor;
import net.malisis.doors.block.SlidingDoor;
import net.malisis.doors.block.VanishingBlock;
import net.malisis.doors.item.MixedBlockBlockItem;
import net.malisis.doors.item.VanishingBlockItem;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.GameRegistry;

public class MalisisBlocks
{
	public static Block doubleDoorWood;
	public static Block doubleDoorIron;
	public static Block woodSlidingDoor;
	public static Block ironSlidingDoor;
	public static Block playerSensor;
	public static Block vanishingBlock;
	public static Block blockMixer;
	public static Block mixedBlock;

	public static void init()
	{
		instantiate();
		registerBlocks();
		registerRecipes();
	}

	private static void instantiate()
	{
		// replace original doors
		doubleDoorWood = (new Door(Material.wood));
		doubleDoorIron = (new Door(Material.iron));
		// Sliding Door blocks
		woodSlidingDoor = (new SlidingDoor(Material.wood)).setBlockName("wood_sliding_door");
		ironSlidingDoor = (new SlidingDoor(Material.iron)).setBlockName("iron_sliding_door");

		playerSensor = (new PlayerSensor()).setBlockName("player_sensor");
		vanishingBlock = (new VanishingBlock()).setBlockName("vanishing_block");
		blockMixer = (new BlockMixer()).setBlockName("block_mixer");
		
		mixedBlock = (new MixedBlock()).setBlockName("mixed_block");
	}

	private static void registerBlocks()
	{
		MalisisCore.replaceVanillaBlock(64, "wooden_door", "field_150466_ao", doubleDoorWood, Blocks.wooden_door);
		MalisisCore.replaceVanillaBlock(71, "iron_door", "field_150454_av", doubleDoorIron, Blocks.iron_door);
		
		GameRegistry.registerBlock(woodSlidingDoor, woodSlidingDoor.getUnlocalizedName().substring(5));
		GameRegistry.registerBlock(ironSlidingDoor, ironSlidingDoor.getUnlocalizedName().substring(5));
		
		GameRegistry.registerBlock(playerSensor, playerSensor.getUnlocalizedName().substring(5));		
		GameRegistry.registerBlock(vanishingBlock, VanishingBlockItem.class, vanishingBlock.getUnlocalizedName().substring(5));
		GameRegistry.registerBlock(blockMixer, blockMixer.getUnlocalizedName().substring(5));
		
		GameRegistry.registerBlock(mixedBlock, MixedBlockBlockItem.class, mixedBlock.getUnlocalizedName().substring(5));
	}

	private static void registerRecipes()
	{
		// Sensor recipe
		GameRegistry.addRecipe(new ItemStack(playerSensor), new Object[] { "ABA", "CCC", 'A', Items.iron_ingot, 'B', Items.redstone, 'C',
				Blocks.glass });

		// Vanishing Block Recipes
		GameRegistry.addRecipe(new ItemStack(vanishingBlock, 4, 0), new Object[] { "ABA", "BCB", "ABA", 'A', Items.redstone, 'B',
				Items.stick, 'C', Items.ender_pearl });
		GameRegistry.addRecipe(new ItemStack(vanishingBlock, 4, 1), new Object[] { "ABA", "BCB", "ABA", 'A', Items.redstone, 'B',
				Items.iron_ingot, 'C', Items.ender_pearl });
		GameRegistry.addRecipe(new ItemStack(vanishingBlock, 4, 2), new Object[] { "ABA", "BCB", "ABA", 'A', Items.redstone, 'B',
				Items.gold_ingot, 'C', Items.ender_pearl });
	}
}
