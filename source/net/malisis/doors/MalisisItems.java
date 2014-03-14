package net.malisis.doors;

import net.malisis.doors.item.SlidingDoorItem;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

public class MalisisItems
{

	public static Item woodSlidingDoorItem;
	public static Item ironSlidingDoorItem;

	public static void init()
	{
		instantiate();
		registerNames();
		registerRecipes();

	}

	private static void instantiate()
	{
		// Sliding Door items
		woodSlidingDoorItem = (new SlidingDoorItem(5000, Material.wood)).setUnlocalizedName("woodSlidingDoor");
		ironSlidingDoorItem = (new SlidingDoorItem(5001, Material.iron)).setUnlocalizedName("ironSlidingDoor");
	}

	private static void registerNames()
	{
		LanguageRegistry.addName(woodSlidingDoorItem, "Wood Sliding Door");
		LanguageRegistry.addName(ironSlidingDoorItem, "Iron Sliding Door");
	}

	private static void registerRecipes()
	{
		// Sliding Door recipes
		GameRegistry.addRecipe(new ItemStack(woodSlidingDoorItem), new Object[] { "AB", "AB", "AB", 'A', Block.planks, 'B', Block.glass });
		GameRegistry
				.addRecipe(new ItemStack(ironSlidingDoorItem), new Object[] { "AB", "AB", "AB", 'A', Item.ingotIron, 'B', Block.glass });

	}
}
