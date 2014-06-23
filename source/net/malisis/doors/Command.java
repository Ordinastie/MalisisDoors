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

package net.malisis.doors;

import java.util.List;

import net.malisis.core.MalisisCore;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

/**
 * @author Ordinastie
 * 
 */
public class Command extends CommandBase
{

	@Override
	public String getCommandName()
	{
		return "malisisdoors";
	}

	@Override
	public String getCommandUsage(ICommandSender var1)
	{
		return "malisisdoors.command.usage";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] params)
	{
		if (params[0].equals("config"))
		{
			MalisisDoors.proxy.openConfigurationGui();
		}
		else if (params[0].equals("version"))
		{
			MalisisCore.message("Current version : MalisisDoors " + MalisisDoors.version);
		}

	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender icommandsender)
	{
		return true;
	}

	@Override
	public List addTabCompletionOptions(ICommandSender icommandsender, String[] astring)
	{
		if (astring.length == 1)
			return getListOfStringsMatchingLastWord(astring, new String[] { "config", "version" });
		else
			return null;
	}

	@Override
	public boolean isUsernameIndex(String[] astring, int i)
	{
		return false;
	}

}
