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

import java.util.HashMap;
import java.util.Map.Entry;

import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.network.MalisisMessage;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.entity.VanishingDiamondTileEntity;
import net.minecraft.tileentity.TileEntity;
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
	public VanishingDiamondFrameMessage()
	{
		MalisisDoors.network.registerMessage(this, VanishingDiamondFrameMessage.Packet.class, Side.SERVER);
	}

	@Override
	public IMessage onMessage(Packet message, MessageContext ctx)
	{
		World world = ctx.getServerHandler().playerEntity.worldObj;
		TileEntity te = world.getTileEntity(message.x, message.y, message.z);
		if (te == null || !(te instanceof VanishingDiamondTileEntity))
			return null;

		((VanishingDiamondTileEntity) te).setDuration(message.duration);
		for (int i = 0; i < 6; i++)
			((VanishingDiamondTileEntity) te).getDirectionState(ForgeDirection.getOrientation(i)).update(message.shouldPropagate[i],
					message.delays[i], message.inverses[i]);
		world.markBlockForUpdate(message.x, message.y, message.z);

		return null;
	}

	public static void sendConfiguration(VanishingDiamondTileEntity te, int duration, HashMap<ForgeDirection, UIComponent[]> config)
	{
		Packet packet = new Packet(te.xCoord, te.yCoord, te.zCoord, duration);
		for (Entry<ForgeDirection, UIComponent[]> entry : config.entrySet())
		{
			boolean shouldPropagate = ((UICheckBox) entry.getValue()[0]).isChecked();
			int delay = Integer.valueOf(((UITextField) entry.getValue()[1]).getText());
			boolean inversed = ((UICheckBox) entry.getValue()[2]).isChecked();

			packet.setConfig(entry.getKey().ordinal(), shouldPropagate, delay, inversed);
		}

		MalisisDoors.network.sendToServer(packet);
	}

	public static class Packet implements IMessage
	{
		protected int x, y, z;
		protected int duration;
		protected boolean[] shouldPropagate = new boolean[6];
		protected int[] delays = new int[6];
		protected boolean[] inverses = new boolean[6];

		public Packet()
		{}

		public Packet(int x, int y, int z, int duration)
		{
			this.x = x;
			this.y = y;
			this.z = z;
			this.duration = duration;
		}

		public void setConfig(int dir, boolean shouldPropagate, int delay, boolean inversed)
		{
			this.shouldPropagate[dir] = shouldPropagate;
			this.delays[dir] = delay;
			this.inverses[dir] = inversed;
		}

		@Override
		public void fromBytes(ByteBuf buf)
		{
			x = buf.readInt();
			y = buf.readInt();
			z = buf.readInt();
			duration = buf.readInt();
			for (int i = 0; i < 6; i++)
			{
				shouldPropagate[i] = buf.readBoolean();
				delays[i] = buf.readInt();
				inverses[i] = buf.readBoolean();
			}
		}

		@Override
		public void toBytes(ByteBuf buf)
		{
			buf.writeInt(x);
			buf.writeInt(y);
			buf.writeInt(z);
			buf.writeInt(duration);
			for (int i = 0; i < 6; i++)
			{
				buf.writeBoolean(shouldPropagate[i]);
				buf.writeInt(delays[i]);
				buf.writeBoolean(inverses[i]);
			}

		}

	}

}
