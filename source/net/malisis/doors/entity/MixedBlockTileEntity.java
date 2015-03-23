package net.malisis.doors.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBreakable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class MixedBlockTileEntity extends TileEntity
{
	public Block block1;
	public Block block2;
	public int metadata1;
	public int metadata2;

	public void set(ItemStack itemStack)
	{
		block1 = Block.getBlockById(itemStack.stackTagCompound.getInteger("block1"));
		block2 = Block.getBlockById(itemStack.stackTagCompound.getInteger("block2"));
		metadata1 = itemStack.stackTagCompound.getInteger("metadata1");
		metadata2 = itemStack.stackTagCompound.getInteger("metadata2");
	}

	public boolean isOpaque()
	{
		return !(block1 instanceof BlockBreakable || block2 instanceof BlockBreakable);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		block1 = Block.getBlockById(nbt.getInteger("block1"));
		block2 = Block.getBlockById(nbt.getInteger("block2"));
		metadata1 = nbt.getInteger("metadata1");
		metadata2 = nbt.getInteger("metadata2");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		if (block1 != null && block2 != null)
		{
			nbt.setInteger("block1", Block.getIdFromBlock(block1));
			nbt.setInteger("block2", Block.getIdFromBlock(block2));
			nbt.setInteger("metadata1", metadata1);
			nbt.setInteger("metadata2", metadata2);
		}

	}

	@Override
	public boolean canUpdate()
	{
		return false;
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
	{
		this.readFromNBT(packet.getNbtCompound());
	}

}
