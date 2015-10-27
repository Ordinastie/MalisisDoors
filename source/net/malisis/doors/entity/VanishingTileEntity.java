/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Ordinastie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.malisis.doors.entity;

import java.util.Random;

import net.malisis.doors.MalisisDoors;
import net.malisis.doors.MalisisDoorsSettings;
import net.malisis.doors.ProxyAccess;
import net.malisis.doors.block.VanishingBlock;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import org.apache.commons.lang3.ArrayUtils;

public class VanishingTileEntity extends TileEntity
{
	public final static int maxTransitionTime = 8;
	public final static int maxVibratingTime = 15;

	public Block copiedBlock;
	public int copiedMetadata;
	public TileEntity copiedTileEntity;
	public int frameType;
	public boolean powered;
	// animation purpose
	protected int duration = maxTransitionTime;
	public int transitionTimer;
	public boolean inTransition;
	public boolean vibrating;
	public int vibratingTimer;

	private final Random rand = new Random();

	private Block[] excludes = new Block[] { MalisisDoors.Blocks.vanishingBlock, Blocks.air, Blocks.ladder, Blocks.stone_button,
			Blocks.wooden_button, Blocks.lever, Blocks.vine };

	public VanishingTileEntity()
	{
		this.frameType = VanishingBlock.typeWoodFrame;
		ProxyAccess.get(getWorld());
	}

	public VanishingTileEntity(int frameType)
	{
		if (frameType < 0 || frameType > 3)
			frameType = 0;
		this.frameType = frameType;
	}

	@Override
	public void setWorldObj(World world)
	{
		worldObj = world;
		if (copiedTileEntity != null)
			copiedTileEntity.setWorldObj(((World) ProxyAccess.get(getWorld())));
	}

	public int getDuration()
	{
		return duration;
	}

	public boolean setBlock(ItemStack itemStack, EntityPlayer p, int side, float hitX, float hitY, float hitZ)
	{
		if (itemStack == null)
		{
			copiedBlock = null;
			copiedMetadata = 0;
			copiedTileEntity = null;
			return true;
		}

		Block block = Block.getBlockFromItem(itemStack.getItem());
		if (ArrayUtils.contains(excludes, block))
			return false;

		World proxy = (World) ProxyAccess.get(getWorld());
		copiedBlock = block;
		copiedMetadata = itemStack.getMetadata();
		initCopiedTileEntity();
		copiedMetadata = block.onBlockPlaced(proxy, xCoord, yCoord, zCoord, side, hitX, hitY, hitZ, copiedMetadata);
		if (p != null)
			block.onBlockPlacedBy(proxy, xCoord, yCoord, zCoord, p, itemStack);
		return true;
	}

	private void initCopiedTileEntity()
	{
		copiedTileEntity = copiedBlock.createTileEntity(getWorld(), copiedMetadata);
		if (copiedTileEntity != null)
		{
			copiedTileEntity.setWorldObj((World) ProxyAccess.get(getWorld()));
			copiedTileEntity.xCoord = xCoord;
			copiedTileEntity.yCoord = yCoord;
			copiedTileEntity.zCoord = zCoord;
		}

	}

	public boolean setPowerState(boolean powered)
	{
		if (powered == this.powered)
			return false;

		if (!inTransition)
			this.transitionTimer = powered ? 0 : duration;
		this.powered = powered;
		this.inTransition = true;
		worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, getBlockMetadata() | VanishingBlock.flagInTransition, 2);

		return true;
	}

	@Override
	public void updateEntity()
	{
		if (!inTransition && !powered)
		{
			float r = rand.nextFloat();
			boolean b = r < MalisisDoorsSettings.vanishingGlitchChance.get();
			if (b && MalisisDoorsSettings.enableVanishingGlitch.get())
			{
				vibrating = true;
				vibratingTimer = 0;
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
				if (transitionTimer >= duration)
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

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		int blockID = nbt.getInteger("BlockID");
		if (blockID != 0)
		{
			copiedBlock = Block.getBlockById(blockID);
			copiedMetadata = nbt.getInteger("BlockMetadata");
			if (nbt.hasKey("copiedTileEntity"))
			{
				initCopiedTileEntity();
				copiedTileEntity.readFromNBT(nbt.getCompoundTag("copiedTileEntity"));
			}
		}
		frameType = nbt.getInteger("FrameType");
		powered = nbt.getBoolean("Powered");
		duration = nbt.getInteger("Duration");
		inTransition = nbt.getBoolean("InTransition");
		transitionTimer = nbt.getInteger("TransitionTimer");
		vibrating = nbt.getBoolean("Vibrating");
		vibratingTimer = nbt.getInteger("VibratingTimer");

	}

	@Override
	public void writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		if (copiedBlock != null)
		{
			nbt.setInteger("BlockID", Block.blockRegistry.getIDForObject(copiedBlock));
			nbt.setInteger("BlockMetadata", copiedMetadata);
			if (copiedTileEntity != null)
			{
				NBTTagCompound teTag = new NBTTagCompound();
				copiedTileEntity.writeToNBT(teTag);
				nbt.setTag("copiedTileEntity", teTag);
			}
		}
		nbt.setInteger("FrameType", frameType);
		nbt.setBoolean("Powered", powered);
		nbt.setInteger("Duration", duration);
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
		return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, nbt);
	}

	@Override
	public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity packet)
	{
		this.readFromNBT(packet.getNbtCompound());
	}

}
