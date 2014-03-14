package net.malisis.doors.entity;

import java.util.Random;

import net.malisis.doors.block.VanishingBlock;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet132TileEntityData;
import net.minecraft.tileentity.TileEntity;

public class VanishingTileEntity extends TileEntity
{
	public final static int maxTransitionTime = 6;
    public final static int maxVibratingTime = 15;

    public int copiedBlockID;
    public Block copiedBlock;
    public int copiedMetadata;
    public int frameType;
    public boolean powered;
    // animation purpose
    public int transitionTimer;
    public boolean inTransition;
    public boolean vibrating;
    public int vibratingTimer;

    private final Random rand = new Random();

    public VanishingTileEntity()
    {
        this.frameType = VanishingBlock.typeWoodFrame;
    }
    public VanishingTileEntity(int frameType)
    {
        if(frameType < 0 || frameType > 2)
        	frameType = 0;
        this.frameType = frameType;
    }

    public void setBlock(int id, int metadata)
    {
        copiedBlockID = id;
        copiedMetadata = metadata;

        copiedBlock = Block.blocksList[copiedBlockID];
    }

    public void setPowerState(boolean powered)
    {
        if (powered == this.powered)
            return;

        if (!inTransition)
            this.transitionTimer = powered ? 0 : maxTransitionTime;
        this.powered = powered;
        this.inTransition = true;
        worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, getBlockMetadata() | VanishingBlock.flagInTransition, 2);
    }

    @Override
    public void updateEntity()
    {
        boolean test = false;
        if(test)
            return;

        if (!inTransition && !powered)
        {
            float r = rand.nextFloat();
            boolean b = r > 0.9995F;
            if (b)
            {
                vibrating = true;
                worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, getBlockMetadata() | VanishingBlock.flagInTransition, 2);
            }

            if (vibrating && vibratingTimer++ >= maxVibratingTime)
            {
                vibrating = false;
                vibratingTimer = 0;
                worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, getBlockMetadata() & ~VanishingBlock.flagInTransition, 2);
            }

        }
        else if (inTransition)
        {
            vibrating = false;
            vibratingTimer = 0;

            if (powered) // powering => going invisible
            {
                transitionTimer++;
                if (transitionTimer >= maxTransitionTime)
                {
                    inTransition = false;
                    worldObj.spawnParticle("smoke", xCoord + 0.5F, yCoord + 0.5F, zCoord + 0.5F, 0.0F, 0.0F, 0.0F);
                    worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, getBlockMetadata() & ~VanishingBlock.flagInTransition, 2);
                }
            }
            else
            // shutting down => going visible
            {
                transitionTimer--;
                if (transitionTimer <= 0)
                {
                    inTransition = false;
                    worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, getBlockMetadata() & ~VanishingBlock.flagInTransition, 2);
                }
            }
        }
    }

    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        copiedBlockID = nbt.getInteger("BlockID");
        copiedMetadata = nbt.getInteger("BlockMetadata");
        frameType = nbt.getInteger("FrameType");
        powered = nbt.getBoolean("Powered");
        inTransition = nbt.getBoolean("InTransition");
        transitionTimer = nbt.getInteger("TransitionTimer");
        vibrating = nbt.getBoolean("Vibrating");
        vibratingTimer = nbt.getInteger("VibratingTimer");

        copiedBlock = Block.blocksList[copiedBlockID];
    }

    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setInteger("BlockID", copiedBlockID);
        nbt.setInteger("BlockMetadata", copiedMetadata);
        nbt.setInteger("FrameType", frameType);
        nbt.setBoolean("Powered", powered);
        nbt.setBoolean("InTransition", inTransition);
        nbt.setInteger("TransitionTimer", transitionTimer);
        nbt.setBoolean("Vibrating", vibrating);
        nbt.setInteger("VibratingTimer", vibratingTimer);
    }

    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);
        return new Packet132TileEntityData(this.xCoord, this.yCoord, this.zCoord, 0, nbt);
    }

    @Override
    public void onDataPacket(INetworkManager net, Packet132TileEntityData packet)
    {
        this.readFromNBT(packet.data);
    }

}
