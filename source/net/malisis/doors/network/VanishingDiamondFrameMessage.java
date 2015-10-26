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

package net.malisis.doors.network;

import io.netty.buffer.ByteBuf;
import net.malisis.core.network.MalisisMessage;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.entity.VanishingDiamondTileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

/**
 * @author Ordinastie
 *
 */
@MalisisMessage
public class VanishingDiamondFrameMessage implements IMessageHandler<VanishingDiamondFrameMessage.Packet, IMessage>
{
	public static enum DataType
	{
		PROPAGATION, DELAY, INVERSED, DURATION;
	}

	public VanishingDiamondFrameMessage()
	{
		MalisisDoors.network.registerMessage(this, VanishingDiamondFrameMessage.Packet.class, Side.SERVER);
	}

	@Override
	public IMessage onMessage(Packet message, MessageContext ctx)
	{
		World world = ctx.getServerHandler().playerEntity.worldObj;
		VanishingDiamondTileEntity te = TileEntityUtils.getTileEntity(VanishingDiamondTileEntity.class, world, message.x, message.y,
				message.z);
		if (te == null)
			return null;

		switch (message.type)
		{
			case PROPAGATION:
				te.getDirectionState(message.facing).shouldPropagate = message.checked;
				break;
			case DELAY:
				te.getDirectionState(message.facing).delay = message.time;
				break;
			case INVERSED:
				te.getDirectionState(message.facing).inversed = message.checked;
				break;
			case DURATION:
				te.setDuration(message.time);
				break;
		}
		world.markBlockForUpdate(message.x, message.y, message.z);

		return null;
	}

	public static void sendConfiguration(VanishingDiamondTileEntity te, ForgeDirection facing, DataType type, int time, boolean checked)
	{
		Packet packet = new Packet(te.xCoord, te.yCoord, te.zCoord, type, facing, time, checked);
		MalisisDoors.network.sendToServer(packet);
	}

	public static class Packet implements IMessage
	{
		protected int x;
		protected int y;
		protected int z;
		protected DataType type;
		protected ForgeDirection facing;
		protected int time;
		protected boolean checked;

		public Packet()
		{}

		public Packet(int x, int y, int z, DataType type, ForgeDirection facing, int time, boolean checked)
		{
			this.x = x;
			this.y = y;
			this.z = z;
			this.type = type;
			this.facing = facing;
			this.time = time;
			this.checked = checked;
		}

		@Override
		public void fromBytes(ByteBuf buf)
		{
			x = buf.readInt();
			y = buf.readInt();
			z = buf.readInt();
			type = DataType.values()[buf.readByte()];
			if (type != DataType.DURATION)
				facing = ForgeDirection.values()[buf.readByte()];
			if (type == DataType.PROPAGATION || type == DataType.INVERSED)
				checked = buf.readBoolean();
			else
				time = buf.readInt();
		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			buf.writeInt(x);
			buf.writeInt(y);
			buf.writeInt(z);
			buf.writeByte(type.ordinal());
			if (type != DataType.DURATION)
				buf.writeByte(facing.ordinal());
			if (type == DataType.PROPAGATION || type == DataType.INVERSED)
				buf.writeBoolean(checked);
			else
				buf.writeInt(time);
		}
	}

}
