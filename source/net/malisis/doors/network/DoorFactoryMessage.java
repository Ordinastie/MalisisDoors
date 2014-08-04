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
import net.malisis.doors.entity.DoorFactoryTileEntity;
import net.malisis.doors.entity.VanishingDiamondTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

/**
 * @author Ordinastie
 * 
 */
public class DoorFactoryMessage implements IMessageHandler<DoorFactoryMessage.Packet, IMessage>
{
	@Override
	public IMessage onMessage(Packet message, MessageContext ctx)
	{
		World world = ctx.getServerHandler().playerEntity.worldObj;
		TileEntity te = world.getTileEntity(message.x, message.y, message.z);
		if (te == null || !(te instanceof VanishingDiamondTileEntity))
			return null;

		if (message.type == Packet.TYPE_DOORTYPE)
			((DoorFactoryTileEntity) te).setDoorType(message.doorType);
		else
			((DoorFactoryTileEntity) te).createDoor();

		return null;
	}

	public static void sendDoorType(DoorFactoryTileEntity te, int doorType)
	{
		Packet packet = new Packet(Packet.TYPE_DOORTYPE, te.xCoord, te.yCoord, te.zCoord, doorType);
		NetworkHandler.network.sendToServer(packet);
	}

	public static void sendCreateDoor(DoorFactoryTileEntity te)
	{
		Packet packet = new Packet(Packet.TYPE_CREATEDOOR, te.xCoord, te.yCoord, te.zCoord);
		NetworkHandler.network.sendToServer(packet);
	}

	public static class Packet implements IMessage
	{
		private static int TYPE_DOORTYPE = 0;
		private static int TYPE_CREATEDOOR = 1;
		private int x, y, z;
		private int type;
		private int doorType;

		public Packet()
		{}

		public Packet(int type, int x, int y, int z)
		{
			this.type = type;
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public Packet(int type, int x, int y, int z, int doorType)
		{
			this.type = type;
			this.x = x;
			this.y = y;
			this.z = z;
			this.doorType = doorType;
		}

		@Override
		public void fromBytes(ByteBuf buf)
		{
			x = buf.readInt();
			y = buf.readInt();
			z = buf.readInt();
			type = buf.readInt();
			if (type == TYPE_DOORTYPE)
				this.doorType = buf.readInt();

		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			buf.writeInt(x);
			buf.writeInt(y);
			buf.writeInt(z);
			buf.writeInt(type);
			if (type == TYPE_DOORTYPE)
				buf.writeInt(doorType);
		}

	}

}
