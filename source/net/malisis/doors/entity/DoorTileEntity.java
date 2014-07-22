package net.malisis.doors.entity;

import static net.malisis.doors.block.doors.DoorHandler.*;
import net.malisis.doors.block.doors.Door;
import net.malisis.doors.block.doors.DoorHandler;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class DoorTileEntity extends TileEntity
{
	public boolean moving = false;
	public int state = 0;

	public long startTime;
	public int timer = 0;

	public void setDoorState(int newState)
	{
		if (state == newState)
			return;

		state = newState;
		if (getWorldObj() == null)
			return;

		if (state == stateClosing || state == stateOpening)
		{
			timer = moving ? Door.openingTime - timer : 0;
			startTime = worldObj.getTotalWorldTime() - timer;
			moving = true;
		}
		else
		{
			if (getWorldObj() == null)
				return;

			int metadata = getBlockMetadata();
			if (getBlockType() instanceof Door)
				metadata = metadata & 7;
			metadata = state == stateOpen ? metadata | flagOpened : metadata & ~flagOpened;
			worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, metadata, 2);
			moving = false;
		}

		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		if (!worldObj.isRemote)
			DoorHandler.playSound(worldObj, xCoord, yCoord, zCoord, state);
	}

	@Override
	public void updateEntity()
	{
		if (!moving)
			return;

		timer++;
		if (startTime + Door.openingTime < worldObj.getTotalWorldTime())
		{
			setDoorState(state == DoorHandler.stateClosing ? DoorHandler.stateClose : DoorHandler.stateOpen);
			timer = 0;
		}
	}

	@Override
	public int getBlockMetadata()
	{
		if (getBlockType() != null && blockMetadata == -1)
			blockMetadata = DoorHandler.getFullMetadata(worldObj, xCoord, yCoord, zCoord);

		return blockMetadata;
	}

	@Override
	public boolean shouldRefresh(Block oldBlock, Block newBlock, int oldMeta, int newMeta, World world, int x, int y, int z)
	{
		return (oldBlock != newBlock);
	}

	/**
	 * Specify the bounding box ourselves otherwise, the block bounding box would be use. (And it should be at this point {0, 0, 0})
	 */
	@Override
	public AxisAlignedBB getRenderBoundingBox()
	{
		return AxisAlignedBB.getBoundingBox(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 2, zCoord + 1);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		setDoorState(nbt.getInteger("state"));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("state", state);
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
		this.readFromNBT(packet.func_148857_g());
	}
}
