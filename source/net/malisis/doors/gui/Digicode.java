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

import net.malisis.core.client.gui.GuiRenderer;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.renderer.font.FontRenderOptions;
import net.malisis.core.renderer.font.MalisisFont;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.network.DigicodeMessage;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import com.google.common.eventbus.Subscribe;

/**
 * @author Ordinastie
 *
 */
public class Digicode extends UIContainer<Digicode>
{
	private String enteredCode = "";
	private String expectedCode = null;

	public Digicode(MalisisGui gui, String expected)
	{
		super(gui);

		setSize(50, 80);
		expectedCode = expected;
		createButtons(gui);

	}

	public Digicode(MalisisGui gui)
	{
		this(gui, null);
	}

	public String getExpectedCode()
	{
		return expectedCode;
	}

	public String getEnteredCode()
	{
		return enteredCode;
	}

	public Digicode setEnteredCode(String code)
	{
		if (code.length() > 6)
			code = code.substring(0, 6);

		enteredCode = code;

		fireEvent(new CodeChangeEvent(this));

		return this;
	}

	public boolean isValidCode()
	{
		return !StringUtils.isEmpty(expectedCode) && expectedCode.equals(enteredCode);
	}

	private void createButtons(MalisisGui gui)
	{
		int oy = 16;
		int h = 14;
		for (int i = 1; i <= 9; i++)
		{
			int x = ((i - 1) % 3) * 17;
			int y = (i - 1) / 3 * (h + 2);

			UIButton b = new UIButton(gui, "" + i).setSize(16, h).setPosition(x, oy + y);
			b.setName("" + i);

			add(b);
		}

		add(new UIButton(gui, "C").setSize(16, h).setPosition(0, oy + 3 * (h + 2)).setName("C"));
		add(new UIButton(gui, "0").setSize(16, h).setPosition(17, oy + 3 * (h + 2)).setName("0"));
		if (!StringUtils.isEmpty(expectedCode))
			add(new UIButton(gui, "V").setSize(16, h).setPosition(34, oy + 3 * (h + 2)).setName("V"));
	}

	@Override
	public boolean onKeyTyped(char keyChar, int keyCode)
	{
		if (keyChar >= '0' && keyChar <= '9' && enteredCode.length() < 6)
		{
			setEnteredCode(enteredCode + keyChar);
			return true;
		}

		if (keyCode == Keyboard.KEY_BACK && enteredCode.length() > 0)
		{
			setEnteredCode(enteredCode.substring(0, enteredCode.length() - 1));
			return true;
		}

		return super.onKeyTyped(keyChar, keyCode);
	}

	@Override
	public void drawBackground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{}

	@Override
	public void drawForeground(GuiRenderer renderer, int mouseX, int mouseY, float partialTick)
	{
		super.drawForeground(renderer, mouseX, mouseY, partialTick);

		renderer.currentComponent = this;

		renderer.drawRectangle(0, 0, 0, getWidth(), 15, 0x191919, 255);

		MalisisFont font = MalisisDoors.digitalFont;
		FontRenderOptions fro = new FontRenderOptions();
		fro.fontScale = 2F;

		fro.color = 0x003300;
		renderer.drawText(font, "888888", fro);

		String code = enteredCode.length() < 6 ? StringUtils.repeat(' ', 6 - enteredCode.length()) + enteredCode : enteredCode;

		fro.color = 0x00CC00;
		fro.saveDefault();
		renderer.drawText(font, code, fro);
	}

	@Subscribe
	public void onButtonClick(UIButton.ClickEvent event)
	{
		switch (event.getComponent().getName())
		{
			case "C":
				setEnteredCode("");
				break;
			case "V":
				if (isValidCode())
				{
					getGui().close();
					DigicodeMessage.send(((DigicodeGui) getGui()).te);
				}
				break;
			default:
				setEnteredCode(enteredCode + event.getComponent().getName());
		}
	}

	public static class CodeChangeEvent extends ComponentEvent<Digicode>
	{
		private String code;

		public CodeChangeEvent(Digicode digicode)
		{
			super(digicode);
			this.code = digicode.getEnteredCode();
		}

		public String getCode()
		{
			return code;
		}
	}
}
