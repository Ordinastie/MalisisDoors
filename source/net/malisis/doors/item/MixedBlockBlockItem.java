package net.malisis.doors.item;

import net.malisis.doors.MalisisBlocks;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class MixedBlockBlockItem extends ItemBlock
{

	public MixedBlockBlockItem(Block block)
	{
		super(block);
	}
	

	@Override
	public void onCreated(ItemStack itemStack, World world, EntityPlayer player)
	{
		itemStack.stackTagCompound = new NBTTagCompound();
	}

	public static ItemStack fromItemStacks(ItemStack is1, ItemStack is2)
	{
		Block block1 = Block.getBlockFromItem(is1.getItem());
		Block block2 = Block.getBlockFromItem(is2.getItem());
		if(!block1.isNormalCube() || !block2.isNormalCube())
			return null;
		
		int metadata1 = ((ItemBlock)is1.getItem()).getMetadata(is1.getItemDamage());
		int metadata2 = ((ItemBlock)is2.getItem()).getMetadata(is2.getItemDamage());
		
		ItemStack itemStack = new ItemStack(MalisisBlocks.mixedBlock, 1);
		itemStack.stackTagCompound = new NBTTagCompound();
		itemStack.stackTagCompound.setInteger("block1", Block.getIdFromBlock(block1));
		itemStack.stackTagCompound.setInteger("block2", Block.getIdFromBlock(block2));
		itemStack.stackTagCompound.setInteger("metadata1", metadata1);
		itemStack.stackTagCompound.setInteger("metadata2", metadata2);
				
		return itemStack;
	}
	
	
}
