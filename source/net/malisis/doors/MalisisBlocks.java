package net.malisis.doors;


import net.malisis.doors.block.Door;
import net.malisis.doors.block.PlayerSensor;
import net.malisis.doors.block.SlidingDoor;
import net.malisis.doors.block.VanishingBlock;
import net.malisis.doors.item.VanishingBlockItem;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

public class MalisisBlocks
{
	public static Block doubleDoorWood;
	public static Block doubleDoorIron;
	public static Block woodSlidingDoor;
	public static Block ironSlidingDoor;
	public static Block playerSensor;
	public static Block vanishingBlock;

	public static void init()
	{
		instantiate();
		registerBlocks();
		registerNames();
		registerRecipes();
	}

	private static void instantiate()
	{
		// replace original doors
		Block.blocksList[64] = null;
		Block.blocksList[71] = null;
		doubleDoorWood = (new Door(64, Material.wood));
		doubleDoorIron = (new Door(71, Material.iron));
		// Sliding Door blocks
		woodSlidingDoor = (new SlidingDoor(500, Material.wood)).setUnlocalizedName("woodSlidingDoor");
		ironSlidingDoor = (new SlidingDoor(501, Material.iron)).setUnlocalizedName("ironSlidingDoor");

		playerSensor = (new PlayerSensor(502)).setUnlocalizedName("playerSensor");
		vanishingBlock = (new VanishingBlock(503)).setUnlocalizedName("vanishingBlock");
	}

	private static void registerBlocks()
	{
		GameRegistry.registerBlock(woodSlidingDoor, MalisisDoors.modid + woodSlidingDoor.getUnlocalizedName().substring(5));
		GameRegistry.registerBlock(ironSlidingDoor, MalisisDoors.modid + ironSlidingDoor.getUnlocalizedName().substring(5));
		GameRegistry.registerBlock(playerSensor, MalisisDoors.modid + playerSensor.getUnlocalizedName().substring(5));
		GameRegistry.registerBlock(vanishingBlock, VanishingBlockItem.class, MalisisDoors.modid + vanishingBlock.getUnlocalizedName().substring(5));
	}

	private static void registerNames()
	{
		LanguageRegistry.addName(woodSlidingDoor, "Wood Sliding Door");
		LanguageRegistry.addName(ironSlidingDoor, "Iron Sliding Door");
		LanguageRegistry.addName(playerSensor, "Player Sensor");

		LanguageRegistry.addName(new ItemStack(vanishingBlock, 1, 0), "Wood Vanishing Block");
		LanguageRegistry.addName(new ItemStack(vanishingBlock, 1, 1), "Iron Vanishing Block");
		LanguageRegistry.addName(new ItemStack(vanishingBlock, 1, 2), "Gold Vanishing Block");
	}

	private static void registerRecipes()
	{
		// Sensor recipe
		GameRegistry.addRecipe(new ItemStack(playerSensor), new Object[] { "ABA", "CCC", 'A', Item.ingotIron, 'B', Item.redstone, 'C',
				Block.glass });

		// Vanishing Block Recipes
		GameRegistry.addRecipe(new ItemStack(vanishingBlock, 4, 0), new Object[] { "ABA", "BCB", "ABA", 'A', Item.redstone, 'B',
				Item.stick, 'C', Item.enderPearl });
		GameRegistry.addRecipe(new ItemStack(vanishingBlock, 4, 1), new Object[] { "ABA", "BCB", "ABA", 'A', Item.redstone, 'B',
				Item.ingotIron, 'C', Item.enderPearl });
		GameRegistry.addRecipe(new ItemStack(vanishingBlock, 4, 2), new Object[] { "ABA", "BCB", "ABA", 'A', Item.redstone, 'B',
				Item.ingotGold, 'C', Item.enderPearl });
	}
}
