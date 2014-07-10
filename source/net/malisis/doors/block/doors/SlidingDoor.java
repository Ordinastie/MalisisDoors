package net.malisis.doors.block.doors;

import java.util.Random;

import net.malisis.doors.MalisisDoors;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.world.World;

public class SlidingDoor extends Door
{

	public SlidingDoor(Material material)
	{
		super(material);
		float f = 0.5F;
		float f1 = 1.0F;
		setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, f1, 0.5F + f);
		setHardness(2.0F);
		if (material == Material.wood)
			setBlockTextureName(MalisisDoors.modid + ":sliding_door_wood");
		else
			setBlockTextureName(MalisisDoors.modid + ":sliding_door_iron");

	}

	@Override
	public Item getItemDropped(int metadata, Random par2Random, int par3)
	{
		if ((metadata & 8) != 0)
			return null;

		if (this.blockMaterial == Material.iron)
			return MalisisDoors.Items.ironSlidingDoorItem;
		else
			return MalisisDoors.Items.woodSlidingDoorItem;
	}

	@Override
	public Item getItem(World world, int x, int y, int z)
	{
		return this.blockMaterial == Material.iron ? MalisisDoors.Items.ironSlidingDoorItem : MalisisDoors.Items.woodSlidingDoorItem;
	}

}
