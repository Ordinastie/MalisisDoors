package net.malisis.doors.entity;

import net.malisis.core.tileentity.TileEntityInventory;
import net.malisis.doors.item.MixedBlockBlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class BlockMixerTileEntity extends TileEntityInventory
{
	private int mixTimer = 0;
	private int mixTotalTime = 100;
	
	public void updateEntity()
	{
		if(slots[0] == null || slots[1] == null)
		{
			mixTimer = 0;
			return;
		}
		
		ItemStack expected = MixedBlockBlockItem.fromItemStacks(slots[0], slots[1]);
		if(expected == null)
		{
			mixTimer = 0;
			return;
		}
		
		if(slots[2] != null)
		{
			if(!ItemStack.areItemStackTagsEqual(slots[2], expected) || slots[2].stackSize >= slots[2].getMaxStackSize())
			{
				mixTimer = 0;
				return;
			}
		}
				
		
		mixTimer++;
		
		if(mixTimer > mixTotalTime)
		{
			mixTimer = 0;
			decrStackSize(0, 1);
			decrStackSize(1, 1);
			
			if(slots[2] == null)
				setInventorySlotContents(2, expected);
			else
				slots[2].stackSize++;
		}
	}
	
	public void resetMixTimer()
	{
		mixTimer = 0;
	}
	public float getMixTimer()
	{
		return (float) mixTimer / (float) mixTotalTime;
	}
	
	@Override
	public void writeToNBT(NBTTagCompound tagCompound)
	{
		super.writeToNBT(tagCompound);
		tagCompound.setInteger("mixTimer", mixTimer);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tagCompound)
	{
		super.readFromNBT(tagCompound);
		mixTimer = tagCompound.getInteger("mixTimer");
	}

	
	@Override
	public int getSizeInventory()
	{
		return 3;
	}

}
