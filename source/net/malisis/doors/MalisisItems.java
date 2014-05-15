package net.malisis.doors;

import net.malisis.doors.item.SlidingDoorItem;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import cpw.mods.fml.common.registry.GameRegistry;

public class MalisisItems
{

	public static Item woodSlidingDoorItem;
	public static Item ironSlidingDoorItem;

	public static void init()
	{
		instantiate();
		registerItems();
		registerRecipes();

	}

	private static void instantiate()
	{
		// Sliding Door items
		woodSlidingDoorItem = (new SlidingDoorItem(Material.wood)).setUnlocalizedName("wood_sliding_door");
		ironSlidingDoorItem = (new SlidingDoorItem(Material.iron)).setUnlocalizedName("iron_sliding_door");
	}

	private static void registerItems()
	{
		GameRegistry.registerItem(woodSlidingDoorItem, woodSlidingDoorItem.getUnlocalizedName());	
		GameRegistry.registerItem(ironSlidingDoorItem, ironSlidingDoorItem.getUnlocalizedName());
	}
	
	private static void registerRecipes()
	{
		// Sliding Door recipes
		GameRegistry.addRecipe(new ItemStack(woodSlidingDoorItem), new Object[] { "AB", "AB", "AB", 'A', Blocks.planks, 'B', Blocks.glass });
		GameRegistry.addRecipe(new ItemStack(ironSlidingDoorItem), new Object[] { "AB", "AB", "AB", 'A', Items.iron_ingot, 'B', Blocks.glass });

	}
}
