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

package net.malisis.doors.gui;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIWindow;
import net.malisis.doors.door.tileentity.DoorTileEntity;
import net.malisis.doors.network.DigicodeMessage;

import org.lwjgl.input.Keyboard;

/**
 * @author Ordinastie
 *
 */
public class DigicodeGui extends MalisisGui
{
	DoorTileEntity te;
	Digicode digicode;
	String expected;

	public DigicodeGui(DoorTileEntity te)
	{
		this.te = te;
		expected = te.getDescriptor().getCode();
	}

	@Override
	public void construct()
	{
		digicode = new Digicode(this, expected).setAnchor(Anchor.MIDDLE | Anchor.CENTER).register(this);

		UIWindow window = new UIWindow(this, digicode.getWidth() + 20, digicode.getHeight() + 20);
		window.add(digicode);

		addToScreen(window);

		registerKeyListener(digicode);
	}

	@Override
	protected void keyTyped(char keyChar, int keyCode)
	{
		super.keyTyped(keyChar, keyCode);

		if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER)
		{
			if (digicode.isValidCode())
			{
				close();
				DigicodeMessage.send(te);
			}
		}
	}
}
