package net.malisis.doors.item;

import net.malisis.doors.MalisisDoors;
import net.malisis.doors.block.VanishingBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class VanishingBlockItem extends ItemBlock
{
	String[] names = { "wood", "iron", "gold", "diamond" };

	public VanishingBlockItem(Block block)
	{
		super(block);
		setHasSubtypes(true);
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack)
	{
		int i = itemstack.getItemDamage();
		if (i < 0 || i >= names.length)
			i = 0;
		return getUnlocalizedName() + "_" + names[i];
	}

	@Override
	public int getMetadata(int metadata)
	{
		return metadata;
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
	{
		Block block = field_150939_a;
		if ((metadata & 3) == VanishingBlock.typeDiamondFrame)
			block = MalisisDoors.Blocks.vanishingDiamondBlock;

		if (!world.setBlock(x, y, z, block, metadata, 3))
			return false;

		if (world.getBlock(x, y, z) == block)
		{
			block.onBlockPlacedBy(world, x, y, z, player, stack);
			block.onPostBlockPlaced(world, x, y, z, metadata);
		}

		return true;
	}
}
