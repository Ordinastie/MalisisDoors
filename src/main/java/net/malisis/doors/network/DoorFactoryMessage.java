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
import net.malisis.core.network.IMalisisMessageHandler;
import net.malisis.core.network.MalisisMessage;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.DoorDescriptor.RedstoneBehavior;
import net.malisis.doors.DoorRegistry;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.tileentity.DoorFactoryTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * @author Ordinastie
 *
 */
@MalisisMessage
public class DoorFactoryMessage implements IMalisisMessageHandler<DoorFactoryMessage.Packet, IMessage>
{
	public DoorFactoryMessage()
	{
		MalisisDoors.network.registerMessage(this, DoorFactoryMessage.Packet.class, Side.SERVER);
	}

	@Override
	public void process(Packet message, MessageContext ctx)
	{
		World world = IMalisisMessageHandler.getWorld(ctx);
		DoorFactoryTileEntity te = TileEntityUtils.getTileEntity(DoorFactoryTileEntity.class, world, message.pos);
		if (te == null)
			return;

		if (message.type == Packet.TYPE_DOORINFOS)
		{
			te.setCreate(message.isCreate);
			te.setDoorMovement(DoorRegistry.getMovement(message.movement));
			te.setDoorSound(DoorRegistry.getSound(message.sound));
			te.setOpeningTime(message.openTime);
			te.setAutoCloseTime(message.autoCloseTime);
			te.setRedstoneBehavior(RedstoneBehavior.values()[message.redstoneBehavior]);
			te.setDoubleDoor(message.doubleDoor);
			te.setCode(message.code);
		}
		else
			te.createDoor();

		return;
	}

	public static void sendDoorInformations(DoorFactoryTileEntity te)
	{
		Packet packet = new Packet(Packet.TYPE_DOORINFOS, te.getPos());
		packet.setDoorInfos(te);
		MalisisDoors.network.sendToServer(packet);
	}

	public static void sendCreateDoor(DoorFactoryTileEntity te)
	{
		Packet packet = new Packet(Packet.TYPE_CREATEDOOR, te.getPos());
		MalisisDoors.network.sendToServer(packet);
	}

	public static class Packet implements IMessage
	{
		private static int TYPE_DOORINFOS = 0;
		private static int TYPE_CREATEDOOR = 1;
		private BlockPos pos;
		private int type;
		private boolean isCreate;
		private String movement;
		private String sound;
		private int openTime;
		private int autoCloseTime;
		private int redstoneBehavior;
		private boolean doubleDoor;
		private String code;

		public Packet()
		{}

		public Packet(int type, BlockPos pos)
		{
			this.type = type;
			this.pos = pos;
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
			this.redstoneBehavior = te.getRedstoneBehavior().ordinal();
			this.doubleDoor = te.isDoubleDoor();
			this.code = te.getCode();
		}

		@Override
		public void fromBytes(ByteBuf buf)
		{
			pos = BlockPos.fromLong(buf.readLong());
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
				redstoneBehavior = buf.readInt();
				doubleDoor = buf.readBoolean();
				code = ByteBufUtils.readUTF8String(buf);
			}
		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			buf.writeLong(pos.toLong());
			buf.writeInt(type);
			if (type == TYPE_DOORINFOS)
			{
				buf.writeBoolean(isCreate);
				ByteBufUtils.writeUTF8String(buf, movement != null ? movement : "");
				ByteBufUtils.writeUTF8String(buf, sound != null ? sound : "");
				buf.writeInt(openTime);
				buf.writeInt(autoCloseTime);
				buf.writeInt(redstoneBehavior);
				buf.writeBoolean(doubleDoor);
				ByteBufUtils.writeUTF8String(buf, code != null ? code : "");
			}
		}

	}

}
