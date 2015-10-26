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
import net.malisis.doors.network.VanishingDiamondFrameMessage.DataType;
import net.minecraftforge.common.util.ForgeDirection;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;

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
	}

	@Override
	public void construct()
	{

		UIWindow window = new UIWindow(this, "tile.vanishing_block_diamond.name", 200, 220);

		window.add(new UILabel(this, "Direction").setPosition(0, 20));
		window.add(new UILabel(this, "Delay").setPosition(55, 20));
		window.add(new UILabel(this, "Inversed").setPosition(90, 20));

		int i = 0;
		for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS)
		{
			DirectionState state = tileEntity.getDirectionState(dir);
			int y = i * 14 + 30;
			UICheckBox cb = new UICheckBox(this, dir.name());
			cb.setPosition(2, y).setChecked(state.shouldPropagate).register(this);
			cb.attachData(Pair.of(dir, DataType.PROPAGATION));

			UITextField textField = new UITextField(this, "" + state.delay).setSize(27, 0).setPosition(55, y)
					.setDisabled(!state.shouldPropagate).register(this);
			textField.attachData(Pair.of(dir, DataType.DELAY));

			UICheckBox invCb = new UICheckBox(this).setPosition(105, y).setDisabled(!state.shouldPropagate).setChecked(state.inversed)
					.register(this);
			invCb.attachData(Pair.of(dir, DataType.INVERSED));

			window.add(cb);
			window.add(textField);
			window.add(invCb);

			configs.put(dir, new UIComponent[] { cb, textField, invCb });

			i++;
		}

		UIContainer cont = new UIContainer<UIContainer>(this, 50, 60).setPosition(0, 40, Anchor.RIGHT);

		duration = new UITextField(this, null).setSize(30, 0).setPosition(0, 10, Anchor.CENTER).register(this);
		duration.attachData(Pair.of(null, DataType.DURATION));
		cont.add(new UILabel(this, "Duration").setPosition(0, 0, Anchor.CENTER));
		cont.add(duration);

		UIInventory inv = new UIInventory(this, inventoryContainer.getInventory(1));
		inv.setPosition(0, 40, Anchor.CENTER);
		cont.add(new UILabel(this, "Block").setPosition(0, 30, Anchor.CENTER));
		cont.add(inv);

		window.add(cont);

		UIPlayerInventory playerInv = new UIPlayerInventory(this, inventoryContainer.getPlayerInventory());
		window.add(playerInv);

		addToScreen(window);

		TileEntityUtils.linkTileEntityToGui(tileEntity, this);
	}

	@Subscribe
	public void onConfigChanged(ComponentEvent.ValueChange event)
	{
		Pair<ForgeDirection, DataType> data = (Pair<ForgeDirection, DataType>) event.getComponent().getData();
		int time = event.getComponent() instanceof UITextField ? NumberUtils.toInt((String) event.getNewValue()) : 0;
		boolean checked = event.getComponent() instanceof UICheckBox ? (boolean) event.getNewValue() : false;
		VanishingDiamondFrameMessage.sendConfiguration(tileEntity, data.getLeft(), data.getRight(), time, checked);
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
