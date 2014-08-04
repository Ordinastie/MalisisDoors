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
import net.malisis.core.client.gui.component.UISlot;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.container.UIPlayerInventory;
import net.malisis.core.client.gui.component.container.UIWindow;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UIRadioButton;
import net.malisis.core.client.gui.event.ComponentEvent;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.entity.DoorFactoryTileEntity;

/**
 * @author Ordinastie
 * 
 */
public class DoorFactoryGui extends MalisisGui
{
	private DoorFactoryTileEntity tileEntity;
	private UIRadioButton rbRotating;
	private UIRadioButton rbSliding;

	public DoorFactoryGui(DoorFactoryTileEntity te, MalisisInventoryContainer container)
	{
		setInventoryContainer(container);
		tileEntity = te;

		UIWindow window = new UIWindow("tile.door_factory.name", 280, 235);

		UIContainer uicont = new UIContainer(180, 155);

		UILabel lblDoorType = new UILabel("gui.door_factory.door_type").setPosition(5, 20);
		rbRotating = new UIRadioButton("doorType", "gui.door_factory.door_type_rotating").setPosition(lblDoorType.getWidth() + 15, 20)
				.register(this);
		rbSliding = new UIRadioButton("doorType", "gui.door_factory.door_type_sliding").setPosition(
				lblDoorType.getWidth() + rbRotating.getWidth() + 25, 20).register(this);

		UILabel lblFrameType = new UILabel("gui.door_factory.frame_type").setPosition(5, 35);
		UISlot frameSlot = new UISlot(tileEntity.frameSlot).setPosition(-20, 30, Anchor.RIGHT);

		UILabel lblTopMaterial = new UILabel("gui.door_factory.top_material").setPosition(5, 53);
		UISlot topMaterialSlot = new UISlot(tileEntity.topMaterialSlot).setPosition(-20, 48, Anchor.RIGHT);

		UILabel lblBottomMaterial = new UILabel("gui.door_factory.bottom_material").setPosition(5, 71);
		UISlot bottomMaterialSlot = new UISlot(tileEntity.bottomMaterialSlot).setPosition(-20, 66, Anchor.RIGHT);

		UIButton btnCreate = new UIButton("gui.door_factory.create_door").setPosition(0, 90, Anchor.CENTER);
		UISlot outputSlot = new UISlot(tileEntity.output).setPosition(0, 115, Anchor.CENTER);

		UIPlayerInventory playerInv = new UIPlayerInventory(container.getPlayerInventory());

		uicont.add(lblDoorType);
		uicont.add(rbRotating);
		uicont.add(rbSliding);

		uicont.add(lblFrameType);
		uicont.add(frameSlot);

		uicont.add(lblTopMaterial);
		uicont.add(topMaterialSlot);

		uicont.add(lblBottomMaterial);
		uicont.add(bottomMaterialSlot);

		uicont.add(btnCreate);
		uicont.add(outputSlot);

		window.add(playerInv);

		window.add(uicont);

		addToScreen(window);

		TileEntityUtils.linkTileEntityToGui(tileEntity, this);
	}

	@Override
	public void updateGui()
	{
		(tileEntity.getDoorType() == DoorFactoryTileEntity.TYPE_ROTATING ? rbRotating : rbSliding).setSelected();
	}

	public void doorTypeChange(ComponentEvent.ValueChanged<UIRadioButton, UIRadioButton> event)
	{
		int type = event.getNewValue() == rbRotating ? DoorFactoryTileEntity.TYPE_ROTATING : DoorFactoryTileEntity.TYPE_SLIDING;

	}
}
