package net.malisis.doors.entity;

import net.malisis.doors.block.Door;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class DoorTileEntity extends TileEntity
{
    public Door blockType;
    public boolean moving = false;
    public boolean draw = false;
    public int state = 0;

    public float hingeOffsetX;
    public float hingeOffsetZ;
    public float angle;

    public int timer = 0;

    public void startAnimation(int state)
    {
    	//make sure we dont start the animation multiple times
        if(moving && state == this.state)
            return;

        getBlockType();//make sure blockType is loaded
        getBlockMetadata();
        
        if(!moving)
        	timer = state == Door.stateOpening ? 0 : Door.openingTime;
       
        moving = true;
        this.state = state;
    }

    /**
     * Specify the bounding box ourselves otherwise, the block bounding box would be use. (And it should be at this point {0, 0, 0})
     */
    public AxisAlignedBB getRenderBoundingBox()
    {
        return AxisAlignedBB.getAABBPool().getAABB(xCoord, yCoord, zCoord, xCoord + 1, yCoord + 2, zCoord + 1);
    }

    public void setSlidingDoorPosition(float partialTick)
    {
    	int dir = getBlockMetadata() & 3;
        boolean reversed = (getBlockMetadata() & Door.flagReversed) != 0;
        if(state == Door.stateClosing)
    		partialTick = -partialTick;
    	float s = (float) (timer + partialTick) / (float) Door.openingTime;
    	if(s >  1)
    		s = 1;
    	if(s < 0)
    		s = 0;
        
        hingeOffsetX = 0;
        hingeOffsetZ = 0;

        if(dir == Door.DIR_NORTH && reversed || dir == Door.DIR_SOUTH && !reversed)
        {
            hingeOffsetX = -s;	            
        }
        if(dir == Door.DIR_NORTH && !reversed || dir == Door.DIR_SOUTH && reversed)
        {
            hingeOffsetX = s;	            
        }
        if(dir == Door.DIR_WEST && !reversed || dir == Door.DIR_EAST && reversed)
        {
             hingeOffsetZ = -s;
        }
        if(dir == Door.DIR_WEST && reversed || dir == Door.DIR_EAST && !reversed)
        {
             hingeOffsetZ = s;
        }
        
        hingeOffsetX *= 1 - Door.DOOR_WIDTH;
        hingeOffsetZ *= 1 - Door.DOOR_WIDTH;
    }
    
    public void setRegularDoorPosition(float partialTick)
    {
        setDoorAngle(partialTick);
        setHingeOffset(partialTick);
    }

    /**
     * Set the door angle depending on timer and partialTick (for regular doors only)
     * @param partialTick
     */
    private void setDoorAngle(float partialTick)
    {
    	if(state == Door.stateClosing)
    		partialTick = -partialTick;
    	float a = (float) (timer + partialTick) / (float) Door.openingTime;
    	if(a >  1)
    		a = 1;
    	if(a < 0)
    		a = 0;
        if((blockMetadata & Door.flagReversed) != 0)
            a *= -1;
        
        angle = 90 * a;
    }

    public void setHingeOffset(float partialTick)
    {
        int dir = getBlockMetadata() & 3;
        boolean reversed = (getBlockMetadata() & Door.flagReversed) != 0;

        float f =  1 - Door.DOOR_WIDTH / 2;
        hingeOffsetX = Door.DOOR_WIDTH / 2;
        hingeOffsetZ = Door.DOOR_WIDTH / 2;

        if(dir == Door.DIR_SOUTH)
        {
             hingeOffsetZ = f;
             if(reversed)
                 hingeOffsetX = f;
        }
        if(dir == Door.DIR_NORTH && !reversed)
        {
            hingeOffsetX = f;
        }
        if(dir == Door.DIR_WEST && reversed)
        {
            hingeOffsetZ = f;
        }
        if(dir == Door.DIR_EAST)
        {
            hingeOffsetX = f;
            if(!reversed)
                hingeOffsetZ = f;
        }
    }

    public float brightnessFactor()
    {
        int dir = getBlockMetadata() & 3;
        float f = 0.2F * ((float) timer/ (float) Door.openingTime);
        if(dir == Door.DIR_SOUTH || dir == Door.DIR_NORTH)
            f = 0.8F - f;
        else
            f = 0.6F + f;

        return f;
    }

    @Override
    public void updateEntity()
    {
        if(!moving)
            return;

        if(state == Door.stateOpening)
        	timer++;
        else
        	timer--;
        
        if(timer > Door.openingTime || timer < 0)
        {
            moving = false;
            if(getBlockType() != null && !worldObj.isRemote)
            {
                blockType.setDoorState(worldObj, xCoord, yCoord, zCoord, state == Door.stateClosing ? Door.stateClose : Door.stateOpen);
            }
        }

    }

    public Door getBlockType()
    {
        if (blockType == null)
            blockType = (Door) worldObj.getBlock(xCoord, yCoord, zCoord);

        return blockType;
    }


    public int getBlockMetadata()
    {
        return getBlockMetadata(false);
    }

    public int getBlockMetadata(boolean top)
    {
    	//get full door metadata but discard opened state
        if (getBlockType() != null && blockMetadata == -1)
            blockMetadata = blockType.getFullMetadata(worldObj, xCoord, yCoord, zCoord) & ~Door.flagOpened;
        if(top)
            blockMetadata |= Door.flagTopBlock;

        return blockMetadata & ~Door.flagOpened;
    }

    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        timer = nbt.getInteger("timer");
        moving = nbt.getBoolean("moving");
        state = nbt.getInteger("state");

        if(worldObj != null)
        {
	        blockType = (Door) worldObj.getBlock(xCoord, yCoord, zCoord);
	        blockMetadata = blockType.getFullMetadata(worldObj, xCoord, yCoord, zCoord);
        }
    }

    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setInteger("timer", timer);
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
        //TODO
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);//, xCoord, yCoord, zCoord);
    }
}
