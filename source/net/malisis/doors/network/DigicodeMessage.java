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
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.door.block.Door;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.minecraft.world.World;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;

/**
 * @author Ordinastie
 *
 */
@MalisisMessage
public class DigicodeMessage implements IMessageHandler<DigicodeMessage.Packet, IMessage>
{
	public DigicodeMessage()
	{
		MalisisDoors.network.registerMessage(this, DigicodeMessage.Packet.class, Side.SERVER);
	}

	@Override
	public IMessage onMessage(Packet message, MessageContext ctx)
	{
		World world = ctx.getServerHandler().playerEntity.worldObj;
		DoorTileEntity te = Door.getDoor(world, message.x, message.y, message.z);
		if (te == null)
			return null;

		te.openOrCloseDoor();

		if (te.getDescriptor().getAutoCloseTime() > 0 && !te.isOpened())
			world.scheduleBlockUpdate(message.x, message.y, message.z, world.getBlock(message.x, message.y, message.z), te.getDescriptor()
					.getAutoCloseTime() + te.getDescriptor().getOpeningTime());

		return null;

	}

	public static void send(DoorTileEntity te)
	{
		MalisisDoors.network.sendToServer(new Packet(te));
	}

	/**
	 * @author Ordinastie
	 *
	 */
	public static class Packet implements IMessage
	{
		private int x, y, z;

		public Packet(DoorTileEntity te)
		{
			x = te.xCoord;
			y = te.yCoord;
			z = te.zCoord;
		}

		public Packet()
		{}

		@Override
		public void fromBytes(ByteBuf buf)
		{
			x = buf.readInt();
			y = buf.readInt();
			z = buf.readInt();
		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			buf.writeInt(x);
			buf.writeInt(y);
			buf.writeInt(z);
		}
	}

}
