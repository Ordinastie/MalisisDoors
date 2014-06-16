package net.malisis.doors.entity;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.inventory.IInventoryProvider;
import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.inventory.MalisisSlot;
import net.malisis.doors.gui.BlockMixerGui;
import net.malisis.doors.item.MixedBlockBlockItem;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockMixerTileEntity extends TileEntity implements IInventoryProvider
{
	private MalisisInventory inventory;
	private int mixTimer = 0;
	private int mixTotalTime = 100;
	public MixerSlot firstInput;
	public MixerSlot secondInput;
	public MixerSlot output;

	public BlockMixerTileEntity()
	{
		firstInput = new MixerSlot(0, MixerSlot.FIRST);
		secondInput = new MixerSlot(1, MixerSlot.SECOND);
		output = new MixerSlot(2, MixerSlot.OUTPUT);
		inventory = new MalisisInventory(this, new MalisisSlot[] { firstInput, secondInput, output });
	}

	@Override
	public void updateEntity()
	{
		ItemStack firstItemStack = firstInput.getItemStack();
		ItemStack secondItemStack = secondInput.getItemStack();
		ItemStack outputItemStack = output.getItemStack();
		if (firstItemStack == null || secondItemStack == null)
		{
			mixTimer = 0;
			return;
		}

		ItemStack expected = MixedBlockBlockItem.fromItemStacks(firstItemStack, secondItemStack);
		if (expected == null)
		{
			mixTimer = 0;
			return;
		}

		if (outputItemStack != null)
		{
			if (!ItemStack.areItemStackTagsEqual(outputItemStack, expected)
					|| outputItemStack.stackSize >= outputItemStack.getMaxStackSize())
			{
				mixTimer = 0;
				return;
			}
		}

		mixTimer++;

		if (mixTimer > mixTotalTime)
		{
			mixTimer = 0;
			firstInput.addItemStackSize(-1);
			secondInput.addItemStackSize(-1);

			if (outputItemStack == null)
				output.setItemStack(expected);
			else
				output.addItemStackSize(1);
		}
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
		inventory.writeToNBT(tagCompound);
	}

	@Override
	public void readFromNBT(NBTTagCompound tagCompound)
	{
		super.readFromNBT(tagCompound);
		mixTimer = tagCompound.getInteger("mixTimer");
		inventory.readFromNBT(tagCompound);
	}

	@Override
	public MalisisInventory getInventory()
	{
		return inventory;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public MalisisGui getGui(MalisisInventoryContainer container)
	{
		return new BlockMixerGui(this, container);
	}

	public class MixerSlot extends MalisisSlot
	{
		public static final int FIRST = 0;
		public static final int SECOND = 2;
		public static final int OUTPUT = 3;

		int slotType;

		public MixerSlot(int index, int type)
		{
			super(index);
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
