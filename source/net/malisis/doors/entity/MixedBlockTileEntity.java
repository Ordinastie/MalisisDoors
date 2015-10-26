package net.malisis.doors.entity;

import net.malisis.doors.item.MixedBlockBlockItem;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import org.apache.commons.lang3.tuple.Pair;

public class MixedBlockTileEntity extends TileEntity
{
	private IBlockState state1;
	private IBlockState state2;

	public void set(ItemStack itemStack)
	{
		Pair<IBlockState, IBlockState> pair = MixedBlockBlockItem.readNBT(itemStack.getTagCompound());
		state1 = pair.getLeft();
		state2 = pair.getRight();
	}

	public IBlockState getState1()
	{
		return state1;
	}

	public IBlockState getState2()
	{
		return state2;
	}

	public boolean isOpaque()
	{
		return !(state1.getBlock() instanceof BlockBreakable || state2.getBlock() instanceof BlockBreakable);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		Pair<IBlockState, IBlockState> pair = MixedBlockBlockItem.readNBT(nbt);
		state1 = pair.getLeft();
		state2 = pair.getRight();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		MixedBlockBlockItem.writeNBT(nbt, state1, state2);
	}

	@Override
	public Packet getDescriptionPacket()
	{
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		return new S35PacketUpdateTileEntity(pos, 0, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
	{
		this.readFromNBT(packet.getNbtCompound());
	}

	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
	{
		return oldState.getBlock() != newSate.getBlock();
	}
}
