package net.malisis.doors.entity;

import net.malisis.core.util.ItemUtils;
import net.malisis.doors.item.MixedBlockBlockItem;
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

		addSlotToContainer(new MixerSlot(te, 0, 22, 35, MixerSlot.FIRST));
		addSlotToContainer(new MixerSlot(te, 1, 139, 35, MixerSlot.SECOND));
		addSlotToContainer(new MixerSlot(te, 2, 81, 35, MixerSlot.OUTPUT));

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
		Slot slotObject = (Slot) inventorySlots.get(slot);
		if (slotObject == null)
			return null;
		ItemStack stackInSlot = slotObject.getStack();
		if (stackInSlot == null)
			return null;

		ItemStack stackMoved = stackInSlot.copy();
		if (slot < 3)
		{
			if (!this.mergeItemStack(stackInSlot, 3, 38, true))
				return null;
		}
		// places it into the tileEntity is possible since its in the player inventory
		else if (!this.mergeItemStack(stackInSlot, 0, 2, false))
			return null;

		if (stackInSlot.stackSize == 0)
			slotObject.putStack(null);
		else
			slotObject.onSlotChanged();

		if (stackInSlot.stackSize == stackInSlot.stackSize)
			return null;
		slotObject.onPickupFromSlot(player, stackInSlot);

		return stackMoved;
	}

	@Override
	protected boolean mergeItemStack(ItemStack itemStack, int startSlot, int endSlot, boolean reversed)
	{
		boolean flag1 = false;
		int current = reversed ? endSlot : startSlot;

		ItemStack into;
		Slot slot;

		while(itemStack.stackSize > 0 && current >= startSlot && current <= endSlot)
		{
			slot = (Slot) inventorySlots.get(current);
			if(slot.isItemValid(itemStack))
			{
				into = slot.getStack();
				ItemUtils.ItemStacksMerger ism = new ItemUtils.ItemStacksMerger(itemStack, into);
				flag1 = ism.merge();
				itemStack = ism.merge;
				putStackInSlot(current, ism.into);
			}
			current += reversed ? -1 : 1;
		}

		return flag1;
	}

	public class MixerSlot extends Slot
	{
		public static final int FIRST = 0;
		public static final int SECOND = 2;
		public static final int OUTPUT = 3;

		int slotType;

		public MixerSlot(IInventory par1iInventory, int par2, int par3, int par4, int type)
		{
			super(par1iInventory, par2, par3, par4);
			slotType = type;
		}

		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			if (slotType == OUTPUT)
				return false;
			Block block = Block.getBlockFromItem(itemStack.getItem());
			return MixedBlockBlockItem.canBeMixed(block, slotType == SECOND);
		}
	}
}