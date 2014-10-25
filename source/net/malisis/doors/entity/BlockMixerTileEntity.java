package net.malisis.doors.entity;

import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.inventory.IInventoryProvider;
import net.malisis.core.inventory.MalisisInventory;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.inventory.MalisisSlot;
import net.malisis.doors.gui.BlockMixerGui;
import net.malisis.doors.item.MixedBlockBlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class BlockMixerTileEntity extends TileEntity implements IInventoryProvider
{
	private MalisisInventory inventory;
	private int mixTimer = 0;
	private int mixTotalTime = 100;
	public MixerSlot firstInput;
	public MixerSlot secondInput;
	public MalisisSlot output;

	public BlockMixerTileEntity()
	{
		firstInput = new MixerSlot(0);
		secondInput = new MixerSlot(1);
		output = new MalisisSlot(2);
		output.setOutputSlot();
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
			firstInput.extract(1);
			secondInput.extract(1);
			output.insert(expected);
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
	public MalisisInventory[] getInventories(Object... data)
	{
		return new MalisisInventory[] { inventory };
	}

	@Override
	public MalisisInventory[] getInventories(ForgeDirection side, Object... data)
	{
		return getInventories(data);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public MalisisGui getGui(MalisisInventoryContainer container)
	{
		return new BlockMixerGui(this, container);
	}

	public class MixerSlot extends MalisisSlot
	{
		public MixerSlot(int index)
		{
			super(index);
		}

		@Override
		public boolean isItemValid(ItemStack itemStack)
		{
			return MixedBlockBlockItem.canBeMixed(itemStack);
		}
	}
}
