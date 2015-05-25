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
import net.malisis.doors.door.DoorRegistry;
import net.malisis.doors.entity.DoorFactoryTileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

/**
 * @author Ordinastie
 *
 */
@MalisisMessage
public class DoorFactoryMessage implements IMessageHandler<DoorFactoryMessage.Packet, IMessage>
{
	public DoorFactoryMessage()
	{
		MalisisDoors.network.registerMessage(this, DoorFactoryMessage.Packet.class, Side.SERVER);
	}

	@Override
	public IMessage onMessage(Packet message, MessageContext ctx)
	{
		World world = ctx.getServerHandler().playerEntity.worldObj;
		DoorFactoryTileEntity te = TileEntityUtils.getTileEntity(DoorFactoryTileEntity.class, world, message.x, message.y, message.z);
		if (te == null)
			return null;

		if (message.type == Packet.TYPE_DOORINFOS)
		{
			te.setCreate(message.isCreate);
			te.setDoorMovement(DoorRegistry.getMovement(message.movement));
			te.setDoorSound(DoorRegistry.getSound(message.sound));
			te.setOpeningTime(message.openTime);
			te.setAutoCloseTime(message.autoCloseTime);
			te.setRequireRedstone(message.redstone);
			te.setDoubleDoor(message.doubleDoor);
			te.setCode(message.code);
		}
		else
			te.createDoor();

		return null;
	}

	public static void sendDoorInformations(DoorFactoryTileEntity te)
	{
		Packet packet = new Packet(Packet.TYPE_DOORINFOS, te.xCoord, te.yCoord, te.zCoord);
		packet.setDoorInfos(te);
		MalisisDoors.network.sendToServer(packet);
	}

	public static void sendCreateDoor(DoorFactoryTileEntity te)
	{
		Packet packet = new Packet(Packet.TYPE_CREATEDOOR, te.xCoord, te.yCoord, te.zCoord);
		MalisisDoors.network.sendToServer(packet);
	}

	public static class Packet implements IMessage
	{
		private static int TYPE_DOORINFOS = 0;
		private static int TYPE_CREATEDOOR = 1;
		private int x, y, z;
		private int type;
		private boolean isCreate;
		private String movement;
		private String sound;
		private int openTime;
		private int autoCloseTime;
		private boolean redstone;
		private boolean doubleDoor;
		private String code;

		public Packet()
		{}

		public Packet(int type, int x, int y, int z)
		{
			this.type = type;
			this.x = x;
			this.y = y;
			this.z = z;
		}

		public void setDoorInfos(DoorFactoryTileEntity te)
		{
			String movement = DoorRegistry.getId(te.getDoorMovement());
			String sound = DoorRegistry.getId(te.getDoorSound());

			this.isCreate = te.isCreate();
			this.movement = movement;
			this.sound = sound;
			this.openTime = te.getOpeningTime();
			this.autoCloseTime = te.getAutoCloseTime();
			this.redstone = te.requireRedstone();
			this.doubleDoor = te.isDoubleDoor();
			this.code = te.getCode();
		}

		@Override
		public void fromBytes(ByteBuf buf)
		{
			x = buf.readInt();
			y = buf.readInt();
			z = buf.readInt();
			type = buf.readInt();
			if (type == TYPE_DOORINFOS)
			{
				isCreate = buf.readBoolean();
				movement = ByteBufUtils.readUTF8String(buf);
				if (movement.equals(""))
					movement = null;
				sound = ByteBufUtils.readUTF8String(buf);
				if (sound.equals(""))
					sound = null;
				openTime = buf.readInt();
				autoCloseTime = buf.readInt();
				redstone = buf.readBoolean();
				doubleDoor = buf.readBoolean();
				code = ByteBufUtils.readUTF8String(buf);
			}
		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			buf.writeInt(x);
			buf.writeInt(y);
			buf.writeInt(z);
			buf.writeInt(type);
			if (type == TYPE_DOORINFOS)
			{
				buf.writeBoolean(isCreate);
				ByteBufUtils.writeUTF8String(buf, movement != null ? movement : "");
				ByteBufUtils.writeUTF8String(buf, sound != null ? sound : "");
				buf.writeInt(openTime);
				buf.writeInt(autoCloseTime);
				buf.writeBoolean(redstone);
				buf.writeBoolean(doubleDoor);
				ByteBufUtils.writeUTF8String(buf, code != null ? code : "");
			}
		}

	}

}
