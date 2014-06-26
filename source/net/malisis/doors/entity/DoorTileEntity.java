package net.malisis.doors.entity;

import net.malisis.doors.block.doors.Door;
import net.malisis.doors.block.doors.DoorHandler;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class DoorTileEntity extends TileEntity
{
	public Block blockType;
	public boolean moving = false;
	public boolean draw = false;
	public int state = 0;

	public float hingeOffsetX;
	public float hingeOffsetZ;
	public float angle;

	public long startTime;
	public int timer = 0;

	public void startAnimation(int state)
	{
		// make sure we don't start the animation multiple times
		if (moving && state == this.state)
			return;

		getBlockType();// make sure blockType is loaded
		getBlockMetadata();

		timer = moving ? Door.openingTime - timer : 0;

		startTime = worldObj.getTotalWorldTime() - timer;

		moving = true;
		this.state = state;
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
	public void updateEntity()
	{
		if (!moving)
			return;

		timer++;
		if (startTime + Door.openingTime < worldObj.getTotalWorldTime())
		{
			moving = false;
			if (getBlockType() != null && !worldObj.isRemote)
			{
				DoorHandler.setDoorState(worldObj, xCoord, yCoord, zCoord,
						state == DoorHandler.stateClosing ? DoorHandler.stateClose : DoorHandler.stateOpen);
			}
			timer = 0;
		}

	}

	@Override
	public Block getBlockType()
	{
		if (blockType == null)
			blockType = worldObj.getBlock(xCoord, yCoord, zCoord);

		return blockType;
	}

	@Override
	public int getBlockMetadata()
	{
		return getBlockMetadata(false);
	}

	public int getBlockMetadata(boolean top)
	{
		// get full door metadata but discard opened state
		if (getBlockType() != null && blockMetadata == -1)
			blockMetadata = DoorHandler.getFullMetadata(worldObj, xCoord, yCoord, zCoord) & ~DoorHandler.flagOpened;
		if (top)
			blockMetadata |= DoorHandler.flagTopBlock;

		return blockMetadata & ~DoorHandler.flagOpened;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		timer = nbt.getInteger("timer");
		startTime = nbt.getLong("startTime");
		moving = nbt.getBoolean("moving");
		state = nbt.getInteger("state");

		if (worldObj != null)
		{
			blockType = worldObj.getBlock(xCoord, yCoord, zCoord);
			blockMetadata = DoorHandler.getFullMetadata(worldObj, xCoord, yCoord, zCoord);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("timer", timer);
		nbt.setLong("startTime", startTime);
		nbt.setBoolean("moving", moving);
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
		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}
}
