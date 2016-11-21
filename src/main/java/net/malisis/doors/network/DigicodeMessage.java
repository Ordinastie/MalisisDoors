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
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.block.Door;
import net.malisis.doors.tileentity.DoorTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * @author Ordinastie
 *
 */
@MalisisMessage
public class DigicodeMessage implements IMalisisMessageHandler<DigicodeMessage.Packet, IMessage>
{
	public DigicodeMessage()
	{
		MalisisDoors.network.registerMessage(this, DigicodeMessage.Packet.class, Side.SERVER);
	}

	@Override
	public void process(Packet message, MessageContext ctx)
	{
		World world = IMalisisMessageHandler.getWorld(ctx);
		DoorTileEntity te = Door.getDoor(world, message.pos);
		if (te == null)
			return;

		te.openOrCloseDoor();

		if (te.getDescriptor().getAutoCloseTime() > 0 && !te.isOpened())
			world.scheduleBlockUpdate(message.pos, world.getBlockState(message.pos).getBlock(), te.getDescriptor().getAutoCloseTime()
					+ te.getDescriptor().getOpeningTime(), 0);

		return;
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
		private BlockPos pos;

		public Packet(DoorTileEntity te)
		{
			pos = te.getPos();
		}

		public Packet()
		{}

		@Override
		public void fromBytes(ByteBuf buf)
		{
			pos = BlockPos.fromLong(buf.readLong());
		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			buf.writeLong(pos.toLong());
		}
	}

}
