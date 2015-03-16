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
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.UISlot;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.container.UIPlayerInventory;
import net.malisis.core.client.gui.component.container.UITabGroup;
import net.malisis.core.client.gui.component.container.UITabGroup.TabPosition;
import net.malisis.core.client.gui.component.container.UIWindow;
import net.malisis.core.client.gui.component.decoration.UIImage;
import net.malisis.core.client.gui.component.decoration.UILabel;
import net.malisis.core.client.gui.component.decoration.UITooltip;
import net.malisis.core.client.gui.component.interaction.UIButton;
import net.malisis.core.client.gui.component.interaction.UICheckBox;
import net.malisis.core.client.gui.component.interaction.UIRadioButton;
import net.malisis.core.client.gui.component.interaction.UISelect;
import net.malisis.core.client.gui.component.interaction.UITab;
import net.malisis.core.client.gui.component.interaction.UITextField;
import net.malisis.core.client.gui.event.ComponentEvent.ValueChange;
import net.malisis.core.client.gui.event.component.StateChangeEvent.ActiveStateChange;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.door.DoorRegistry;
import net.malisis.doors.door.movement.IDoorMovement;
import net.malisis.doors.door.sound.IDoorSound;
import net.malisis.doors.entity.DoorFactoryTileEntity;
import net.malisis.doors.network.DoorFactoryMessage;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;

import com.google.common.eventbus.Subscribe;

/**
 * @author Ordinastie
 *
 */
public class DoorFactoryGui extends MalisisGui
{
	public static ResourceLocation tabIconsRl = new ResourceLocation(MalisisDoors.modid, "textures/gui/doorFactoryTabIcons.png");
	public static GuiTexture tabTexture = new GuiTexture(tabIconsRl, 128, 64);
	public static IIcon propIcon = tabTexture.getIcon(0, 0, 64, 64);
	public static IIcon matIcon = tabTexture.getIcon(64, 0, 64, 64);

	private DoorFactoryTileEntity tileEntity;
	private UITab firstTab;
	private UISelect selDoorMovement;
	private UITextField tfOpenTime;
	private UITextField tfAutoCloseTime;
	private UICheckBox cbRedstone;
	private UICheckBox cbDoubleDoor;
	private UISelect selDoorSound;
	private UIRadioButton rbCreate;
	private UIRadioButton rbEdit;
	private UIContainer contCreate;
	private UIContainer contEdit;
	private UIButton btnCreate;

	private static boolean firstTabActive = true;

	public DoorFactoryGui(DoorFactoryTileEntity te, MalisisInventoryContainer container)
	{
		setInventoryContainer(container);
		tileEntity = te;

		UIWindow window = new UIWindow(this, "tile.door_factory.name", UIPlayerInventory.INVENTORY_WIDTH + 10, 240);

		UIContainer propContainer = getPropertiesContainer();
		UIContainer matContainer = getMaterialsContainer();

		UITabGroup tabGroup = new UITabGroup(this, TabPosition.LEFT).setPosition(0, 10);

		int a = 16;
		firstTab = new UITab(this, new UIImage(this, tabTexture, propIcon).setSize(a, a)).setTooltip(
				new UITooltip(this, "gui.door_factory.tab_properties")).register(this);
		UITab tab2 = new UITab(this, new UIImage(this, tabTexture, matIcon).setSize(a, a)).setTooltip(
				new UITooltip(this, "gui.door_factory.tab_materials")).register(this);
		tabGroup.addTab(firstTab, propContainer);
		tabGroup.addTab(tab2, matContainer);

		tabGroup.setActiveTab(firstTabActive ? firstTab : tab2);
		tabGroup.attachTo(window, false);

		btnCreate = new UIButton(this, "gui.door_factory.create_door").setSize(80).setPosition(0, 98, Anchor.CENTER).register(this);
		UISlot outputSlot = new UISlot(this, tileEntity.outputSlot).setPosition(0, 120, Anchor.CENTER);

		UIPlayerInventory playerInv = new UIPlayerInventory(this, container.getPlayerInventory());

		window.add(playerInv);

		window.add(propContainer);
		window.add(matContainer);

		window.add(btnCreate);
		window.add(outputSlot);

		addToScreen(tabGroup);
		addToScreen(window);

		TileEntityUtils.linkTileEntityToGui(tileEntity, this);
	}

	private UIContainer<UIContainer> getPropertiesContainer()
	{
		UIContainer propContainer = new UIContainer<>(this, UIComponent.INHERITED, 80).setPosition(0, 15);

		HashMap<IDoorMovement, String> listMvt = new HashMap<>();
		for (Entry<String, IDoorMovement> entry : DoorRegistry.listMovements().entrySet())
			listMvt.put(entry.getValue(), entry.getKey());
		selDoorMovement = new UISelect(this, 100, UISelect.Option.fromList(listMvt)).setPosition(0, 2, Anchor.RIGHT).register(this);
		selDoorMovement.setLabelPattern("door_movement.%s");

		tfOpenTime = new UITextField(this, null).setSize(30, 0).setPosition(-5, 14, Anchor.RIGHT).register(this);
		tfAutoCloseTime = new UITextField(this, null).setSize(30, 0).setPosition(-5, 26, Anchor.RIGHT).register(this);
		cbRedstone = new UICheckBox(this).setPosition(-15, 38, Anchor.RIGHT).register(this);
		cbDoubleDoor = new UICheckBox(this).setPosition(-15, 50, Anchor.RIGHT).register(this);

		HashMap<IDoorSound, String> listSounds = new HashMap<>();
		for (Entry<String, IDoorSound> entry : DoorRegistry.listSounds().entrySet())
			listSounds.put(entry.getValue(), entry.getKey());
		selDoorSound = new UISelect(this, 100, UISelect.Option.fromList(listSounds)).setPosition(0, 62, Anchor.RIGHT).register(this);
		selDoorSound.setLabelPattern("gui.door_factory.door_sound.%s");

		propContainer.add(new UILabel(this, "gui.door_factory.door_movement").setPosition(0, 4));
		propContainer.add(new UILabel(this, "gui.door_factory.door_open_time").setPosition(0, 16));
		propContainer.add(new UILabel(this, "gui.door_factory.door_auto_close_time").setPosition(0, 28));
		propContainer.add(new UILabel(this, "gui.door_factory.door_require_redstone").setPosition(0, 40));
		propContainer.add(new UILabel(this, "gui.door_factory.door_double_door").setPosition(0, 52));
		propContainer.add(new UILabel(this, "gui.door_factory.door_sound").setPosition(0, 64));

		propContainer.add(selDoorMovement);
		propContainer.add(tfOpenTime);
		propContainer.add(tfAutoCloseTime);
		propContainer.add(cbRedstone);
		propContainer.add(cbDoubleDoor);
		propContainer.add(selDoorSound);

		return propContainer;

	}

	private UIContainer getMaterialsContainer()
	{
		UIContainer matContainer = new UIContainer<>(this, UIComponent.INHERITED, 80).setPosition(0, 15);

		rbCreate = new UIRadioButton(this, "rbDoor", "gui.door_factory.rb_create").setPosition(30, 0).register(this);
		rbEdit = new UIRadioButton(this, "rbDoor", "gui.door_factory.rb_edit").setPosition(100, 0).register(this);

		matContainer.add(rbCreate);
		matContainer.add(rbEdit);

		contCreate = (UIContainer) new UIContainer(this).setPosition(0, 14);

		int y = 0;
		UISlot frameSlot = new UISlot(this, tileEntity.frameSlot).setPosition(-10, y, Anchor.RIGHT);
		UISlot topMaterialSlot = new UISlot(this, tileEntity.topMaterialSlot).setPosition(-10, y + 18, Anchor.RIGHT);
		UISlot bottomMaterialSlot = new UISlot(this, tileEntity.bottomMaterialSlot).setPosition(-10, y + 36, Anchor.RIGHT);

		contCreate.add(new UILabel(this, "gui.door_factory.frame_type").setPosition(0, y + 5));
		contCreate.add(new UILabel(this, "gui.door_factory.top_material").setPosition(0, y + 23));
		contCreate.add(new UILabel(this, "gui.door_factory.bottom_material").setPosition(0, y + 41));

		contCreate.add(frameSlot);
		contCreate.add(topMaterialSlot);
		contCreate.add(bottomMaterialSlot);

		contEdit = (UIContainer) new UIContainer(this).setPosition(0, 14);

		UISlot doorEditSlotSlot = new UISlot(this, tileEntity.doorEditSlot).setPosition(-10, 18, Anchor.RIGHT);
		contEdit.add(new UILabel(this, "gui.door_factory.door_edit_slot").setPosition(0, 23));
		contEdit.add(doorEditSlotSlot);

		matContainer.add(contCreate);
		matContainer.add(contEdit);

		return matContainer;
	}

	@Override
	public void updateGui()
	{
		boolean isCreate = tileEntity.isCreate();
		if (isCreate)
			rbCreate.setSelected();
		else
			rbEdit.setSelected();
		contCreate.setVisible(isCreate);
		contEdit.setVisible(!isCreate);
		btnCreate.setText(isCreate ? "gui.door_factory.create_door" : "gui.door_factory.edit_door");

		selDoorMovement.setSelectedOption(tileEntity.getDoorMovement());
		tfOpenTime.setText(Integer.toString(tileEntity.getOpeningTime()));
		tfAutoCloseTime.setText(Integer.toString(tileEntity.getAutoCloseTime()));
		cbRedstone.setChecked(tileEntity.requireRedstone());
		cbDoubleDoor.setChecked(tileEntity.isDoubleDoor());
		selDoorSound.setSelectedOption(tileEntity.getDoorSound());
	}

	@Subscribe
	public void onCheckedEvent(UICheckBox.CheckEvent event)
	{
		if (event.getComponent() == cbRedstone)
			tileEntity.setRequireRedstone(event.isChecked());
		else
			tileEntity.setDoubleDoor(event.isChecked());

		DoorFactoryMessage.sendDoorInformations(tileEntity);
	}

	@Subscribe
	public void onSelectEvent(UISelect.SelectEvent event)
	{
		if (event.getOption() == null)
			return;

		if (event.getComponent() == selDoorMovement)
			tileEntity.setDoorMovement((IDoorMovement) event.getOption().getKey());
		else
			tileEntity.setDoorSound((IDoorSound) event.getOption().getKey());

		DoorFactoryMessage.sendDoorInformations(tileEntity);
	}

	@Subscribe
	public void onRbSelectEvent(UIRadioButton.SelectEvent event)
	{
		boolean isCreate = event.getNewValue() == rbCreate;

		tileEntity.setCreate(isCreate);
		contCreate.setVisible(isCreate);
		contEdit.setVisible(!isCreate);
		btnCreate.setText(isCreate ? "gui.door_factory.create_door" : "gui.door_factory.edit_door");

		DoorFactoryMessage.sendDoorInformations(tileEntity);
	}

	@Subscribe
	public void onGuiChangeEvent(ValueChange<UIComponent, Object> event)
	{
		if (event.getComponent() != tfOpenTime && event.getComponent() != tfAutoCloseTime)
			return;

		try
		{
			//parse the value of the textfields
			tileEntity.setOpeningTime(Integer.parseInt(tfOpenTime.getText()));
			tileEntity.setAutoCloseTime(Integer.parseInt(tfAutoCloseTime.getText()));
			DoorFactoryMessage.sendDoorInformations(tileEntity);
		}
		catch (NumberFormatException e)
		{
			//parsing failed, replace the value of the textfield by the value already in the TE
			tfOpenTime.setText(Integer.toString(tileEntity.getOpeningTime()));
		}

	}

	@Subscribe
	public void onCreateDoor(UIButton.ClickEvent event)
	{
		DoorFactoryMessage.sendCreateDoor(tileEntity);
	}

	@Subscribe
	public void onTabActivation(ActiveStateChange<UITab> event)
	{
		if (event.getState())
			firstTabActive = event.getComponent() == firstTab;
	}

}
