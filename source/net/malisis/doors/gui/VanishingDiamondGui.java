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

import java.util.HashMap;
import java.util.Map.Entry;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.container.UIInventory;
import net.malisis.core.client.gui.component.container.UIPlayerInventory;
import net.malisis.core.client.gui.component.container.UIWindow;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.entity.VanishingDiamondTileEntity;
import net.malisis.doors.entity.VanishingDiamondTileEntity.DirectionState;
import net.malisis.doors.network.VanishingDiamondFrameMessage;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.eventbus.Subscribe;

/**
 * @author Ordinastie
 *
 */
public class VanishingDiamondGui extends MalisisGui
{
	protected VanishingDiamondTileEntity tileEntity;

	protected UITextField duration;
	protected HashMap<ForgeDirection, UIComponent[]> configs = new HashMap<>();

	public VanishingDiamondGui(VanishingDiamondTileEntity te, MalisisInventoryContainer container)
	{
		setInventoryContainer(container);
		this.tileEntity = te;

		UIWindow window = new UIWindow("tile.vanishing_block_diamond.name", 200, 220);

		window.add(new UILabel("Direction").setPosition(0, 20));
		window.add(new UILabel("Delay").setPosition(55, 20));
		window.add(new UILabel("Inversed").setPosition(90, 20));

		int i = 0;
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			DirectionState state = te.getDirectionState(dir);
			int y = i * 14 + 30;
			UICheckBox cb = new UICheckBox(dir.name());
			cb.setPosition(2, y).setChecked(state.shouldPropagate).register(this);

			UIContainer cbCont = new UIContainer(70, 12);
			cbCont.setPosition(55, y);

			UITextField textField = new UITextField(27, "" + state.delay).setPosition(0, 0).setDisabled(!state.shouldPropagate)
					.setFilter("\\d+").register(this);

			UICheckBox invCb = new UICheckBox().setPosition(50, 0).setDisabled(!state.shouldPropagate).setChecked(state.inversed)
					.register(this);
			cbCont.add(textField);
			cbCont.add(invCb);

			window.add(cb);
			window.add(cbCont);

			configs.put(dir, new UIComponent[] { cb, textField, invCb });

			i++;
		}

		UIContainer cont = (UIContainer) new UIContainer(50, 60).setPosition(0, 40, Anchor.RIGHT);

		duration = new UITextField(30).setPosition(0, 10, Anchor.CENTER).setFilter("\\d+").register(this);
		cont.add(new UILabel("Duration").setPosition(0, 0, Anchor.CENTER));
		cont.add(duration);

		UIInventory inv = new UIInventory(container.getInventory(1));
		inv.setPosition(0, 40, Anchor.CENTER);
		cont.add(new UILabel("Block").setPosition(0, 30, Anchor.CENTER));
		cont.add(inv);

		window.add(cont);

		UIPlayerInventory playerInv = new UIPlayerInventory(container.getPlayerInventory());
		window.add(playerInv);

		addToScreen(window);

		TileEntityUtils.linkTileEntityToGui(tileEntity, this);
	}

	@Subscribe
	public void onChecked(ComponentEvent.ValueChanged<UICheckBox, Boolean> event)
	{
		for (Entry<ForgeDirection, UIComponent[]> entry : configs.entrySet())
		{
			if (entry.getValue()[0] == event.getComponent())
			{
				entry.getValue()[1].setDisabled(!event.getNewValue()); // Textfield
				entry.getValue()[2].setDisabled(!event.getNewValue()); // Checkbox
			}
		}

		updateConfig();
	}

	@Subscribe
	public void onTextChanged(ComponentEvent.ValueChanged<UITextField, String> event)
	{
		updateConfig();
	}

	public void updateConfig()
	{
		VanishingDiamondFrameMessage.sendConfiguration(tileEntity, Integer.parseInt(duration.getText()), configs);
	}

	@Override
	public void updateGui()
	{
		if (!duration.isFocused())
			duration.setText("" + tileEntity.getDuration());
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			DirectionState state = tileEntity.getDirectionState(dir);
			((UICheckBox) configs.get(dir)[0]).setChecked(state.shouldPropagate);
			UITextField tf = (UITextField) configs.get(dir)[1];
			tf.setDisabled(!state.shouldPropagate);
			if (!tf.isFocused())
				tf.setText("" + state.delay);
			((UICheckBox) configs.get(dir)[2]).setDisabled(!state.shouldPropagate).setChecked(state.inversed);
		}

	}

}
