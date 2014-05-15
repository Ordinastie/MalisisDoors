package net.malisis.doors.entity;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class BlockMixerContainer extends Container
{

	protected BlockMixerTileEntity te;

	public BlockMixerContainer(InventoryPlayer inventoryPlayer, BlockMixerTileEntity te)
	{
		this.te = te;

		addSlotToContainer(new MixerSlot(te, 0, 22, 35, true));
		addSlotToContainer(new MixerSlot(te, 1, 139, 35, true));
		addSlotToContainer(new MixerSlot(te, 2, 81, 35, false));
	
		bindPlayerInventory(inventoryPlayer);
	}

	@Override
	public boolean canInteractWith(EntityPlayer player)
	{
		return te.isUseableByPlayer(player);
	}

	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer)
	{
		for (int i = 0; i < 3; i++)
		{
			for (int j = 0; j < 9; j++)
			{
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}

		for (int i = 0; i < 9; i++)
		{
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot)
	{
		ItemStack stack = null;
		Slot slotObject = (Slot) inventorySlots.get(slot);

		if (slotObject != null && slotObject.getHasStack())
		{
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy();

			if (slot < 9)
			{
				if (!this.mergeItemStack(stackInSlot, 0, 35, true))
				{
					return null;
				}
			}
			// places it into the tileEntity is possible since its in the player inventory
			else if (!this.mergeItemStack(stackInSlot, 0, 3, false))
			{
				return null;
			}

			if (stackInSlot.stackSize == 0)
			{
				slotObject.putStack(null);
			}
			else
			{
				slotObject.onSlotChanged();
			}

			if (stackInSlot.stackSize == stack.stackSize)
			{
				return null;
			}
			slotObject.onPickupFromSlot(player, stackInSlot);
		}
		return stack;
	}
	
	
	public class MixerSlot extends Slot
	{
		boolean inputSlot;
		public MixerSlot(IInventory par1iInventory, int par2, int par3, int par4, boolean input)
		{
			super(par1iInventory, par2, par3, par4);
			inputSlot = input;			
		}
		
		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			if(!inputSlot)
				return false;
			Block block = Block.getBlockFromItem(itemStack.getItem());
			return block.isNormalCube();
		}
		
		@Override
	    public void onSlotChanged()
	    {
			super.onSlotChanged();
			if(inputSlot)
				te.resetMixTimer();
	    }
	}
}