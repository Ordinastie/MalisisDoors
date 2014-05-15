package net.malisis.doors.item;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class VanishingBlockItem extends ItemBlock
{
	String[] names = { "wood", "iron", "gold" };

	public VanishingBlockItem(Block block)
	{
		super(block);
		setHasSubtypes(true);
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		int i = itemstack.getItemDamage();
		if(i < 0 || i >= names.length)
			i = 0;
		return getUnlocalizedName() + "_"  + names[i];
	}

	@Override
	public int getMetadata(int metadata)
	{
		return metadata;
	}

}
