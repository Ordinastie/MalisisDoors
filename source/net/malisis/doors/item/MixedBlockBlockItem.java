package net.malisis.doors.item;

import net.malisis.doors.MalisisDoors;
import net.malisis.doors.block.MixedBlock;
import net.malisis.doors.entity.MixedBlockTileEntity;
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
		if(!canBeMixed(block1, false) || !canBeMixed(block2, true))
			return null;
		
		int metadata1 = ((ItemBlock)is1.getItem()).getMetadata(is1.getItemDamage());
		int metadata2 = ((ItemBlock)is2.getItem()).getMetadata(is2.getItemDamage());
		
		if(block1 == block2 && metadata1 == metadata2)
			return null;
		
		ItemStack itemStack = new ItemStack(MalisisDoors.Blocks.mixedBlock, 1);
		itemStack.stackTagCompound = new NBTTagCompound();
		itemStack.stackTagCompound.setInteger("block1", Block.getIdFromBlock(block1));
		itemStack.stackTagCompound.setInteger("block2", Block.getIdFromBlock(block2));
		itemStack.stackTagCompound.setInteger("metadata1", metadata1);
		itemStack.stackTagCompound.setInteger("metadata2", metadata2);
				
		return itemStack;
	}
	
	public static boolean canBeMixed(Block block, boolean second)
	{
		return !(block instanceof MixedBlock) && (second || block.isOpaqueCube());
	}
	
	public static ItemStack fromTileEntity(MixedBlockTileEntity te)
	{
		ItemStack itemStack = new ItemStack(MalisisDoors.Blocks.mixedBlock, 1);
		itemStack.stackTagCompound = new NBTTagCompound();
		itemStack.stackTagCompound.setInteger("block1", Block.getIdFromBlock(te.block1));
		itemStack.stackTagCompound.setInteger("block2", Block.getIdFromBlock(te.block2));
		itemStack.stackTagCompound.setInteger("metadata1", te.metadata1);
		itemStack.stackTagCompound.setInteger("metadata2", te.metadata2);
		
		return itemStack;
	}
	
	
}
