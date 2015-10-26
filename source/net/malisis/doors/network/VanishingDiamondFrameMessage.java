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
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.entity.VanishingDiamondTileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

/**
 * @author Ordinastie
 *
 */
@MalisisMessage
public class VanishingDiamondFrameMessage implements IMalisisMessageHandler<VanishingDiamondFrameMessage.Packet, IMessage>
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
	public void process(Packet message, MessageContext ctx)
	{
		World world = ctx.getServerHandler().playerEntity.worldObj;
		VanishingDiamondTileEntity te = TileEntityUtils.getTileEntity(VanishingDiamondTileEntity.class, world, message.pos);
		if (te == null)
			return;

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
		world.markBlockForUpdate(message.pos);
	}

	public static void sendConfiguration(VanishingDiamondTileEntity te, EnumFacing facing, DataType type, int time, boolean checked)
	{
		Packet packet = new Packet(te.getPos(), type, facing, time, checked);
		MalisisDoors.network.sendToServer(packet);
	}

	public static class Packet implements IMessage
	{
		protected BlockPos pos;
		protected DataType type;
		protected EnumFacing facing;
		protected int time;
		protected boolean checked;

		public Packet()
		{}

		public Packet(BlockPos pos, DataType type, EnumFacing facing, int time, boolean checked)
		{
			this.pos = pos;
			this.type = type;
			this.facing = facing;
			this.time = time;
			this.checked = checked;
		}

		@Override
		public void fromBytes(ByteBuf buf)
		{
			pos = BlockPos.fromLong(buf.readLong());
			type = DataType.values()[buf.readByte()];
			if (type != DataType.DURATION)
				facing = EnumFacing.values()[buf.readByte()];
			if (type == DataType.PROPAGATION || type == DataType.INVERSED)
				checked = buf.readBoolean();
			else
				time = buf.readInt();
		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			buf.writeLong(pos.toLong());
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
