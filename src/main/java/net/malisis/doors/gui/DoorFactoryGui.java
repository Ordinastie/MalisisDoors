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

import java.util.Set;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.ComponentPosition;
import net.malisis.core.client.gui.GuiTexture;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UIComponent;
import net.malisis.core.client.gui.component.UISlot;
import net.malisis.core.client.gui.component.container.UIContainer;
import net.malisis.core.client.gui.component.container.UIPlayerInventory;
import net.malisis.core.client.gui.component.container.UITabGroup;
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
import net.malisis.core.renderer.icon.Icon;
import net.malisis.core.util.TileEntityUtils;
import net.malisis.doors.DoorDescriptor.RedstoneBehavior;
import net.malisis.doors.DoorRegistry;
import net.malisis.doors.MalisisDoors;
import net.malisis.doors.network.DoorFactoryMessage;
import net.malisis.doors.tileentity.DoorFactoryTileEntity;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

/**
 * @author Ordinastie
 *
 */
public class DoorFactoryGui extends MalisisGui
{
	public static ResourceLocation tabIconsRl = new ResourceLocation(MalisisDoors.modid, "textures/gui/doorfactory_tabicons.png");
	public static GuiTexture tabTexture = new GuiTexture(tabIconsRl, 128, 128);
	public static Icon propIcon = tabTexture.getIcon(0, 0, 64, 64);
	public static Icon matIcon = tabTexture.getIcon(64, 0, 64, 64);
	public static Icon dcIcon = tabTexture.getIcon(0, 64, 64, 64);

	private DoorFactoryTileEntity tileEntity;
	private UISelect<String> selDoorMovement;
	private UITextField tfOpenTime;
	private UITextField tfAutoCloseTime;
	private UICheckBox cbDoubleDoor;
	private UICheckBox cbProximity;
	private UISelect<RedstoneBehavior> selRedstone;
	private UISelect<String> selDoorSound;
	private UIRadioButton rbCreate;
	private UIRadioButton rbEdit;
	private UIContainer<?> contCreate;
	private UIContainer<?> contEdit;
	private UIButton btnCreate;
	private Digicode digicode;

	private static String activeTab;

	public DoorFactoryGui(DoorFactoryTileEntity te, MalisisInventoryContainer container)
	{
		setInventoryContainer(container);
		tileEntity = te;
	}

	@Override
	public void construct()
	{
		UIWindow window = new UIWindow(this, "tile.door_factory.name", UIPlayerInventory.INVENTORY_WIDTH + 80, 255);

		UIContainer<?> propContainer = getPropertiesContainer();
		UIContainer<?> matContainer = getMaterialsContainer();
		UIContainer<?> dcContainer = getDigicodeContainer();

		UITabGroup tabGroup = new UITabGroup(this, ComponentPosition.LEFT).setPosition(0, 10);

		int a = 16;
		UITab tabProp = new UITab(this, new UIImage(this, tabTexture, propIcon).setSize(a, a)).setName("tab_prop");
		tabProp.setTooltip(new UITooltip(this, "gui.door_factory.tab_properties")).register(this);
		UITab tabMat = new UITab(this, new UIImage(this, tabTexture, matIcon).setSize(a, a)).setName("tab_mat");
		tabMat.setTooltip(new UITooltip(this, "gui.door_factory.tab_materials")).register(this);
		UITab tabDc = new UITab(this, new UIImage(this, tabTexture, dcIcon).setSize(a, a)).setName("tab_dc");
		tabDc.setTooltip(new UITooltip(this, "gui.door_factory.tab_digicode")).register(this);

		tabGroup.addTab(tabProp, propContainer);
		tabGroup.addTab(tabMat, matContainer);
		tabGroup.addTab(tabDc, dcContainer);

		tabGroup.setActiveTab(activeTab != null ? activeTab : "tab_prop");
		tabGroup.attachTo(window, false);

		btnCreate = new UIButton(this, "gui.door_factory.create_door").setSize(80).setPosition(0, 110, Anchor.CENTER).register(this);
		UISlot outputSlot = new UISlot(this, tileEntity.outputSlot).setPosition(0, 132, Anchor.CENTER);

		UIPlayerInventory playerInv = new UIPlayerInventory(this, inventoryContainer.getPlayerInventory());

		window.add(playerInv);

		window.add(propContainer);
		window.add(matContainer);
		window.add(dcContainer);

		window.add(btnCreate);
		window.add(outputSlot);

		addToScreen(tabGroup);
		addToScreen(window);

		TileEntityUtils.linkTileEntityToGui(tileEntity, this);
	}

	private UIContainer<?> getPropertiesContainer()
	{
		UIContainer<?> propContainer = new UIContainer<>(this, UIComponent.INHERITED, 95).setPosition(0, 15);

		//Door movement
		int y = 2;
		selDoorMovement = new UISelect<>(this, 100, getSortedList(DoorRegistry.listMovements().keySet(), "door_movement."));
		selDoorMovement.setPosition(0, y, Anchor.RIGHT);
		selDoorMovement.setLabelPattern("door_movement.%s").register(this);
		propContainer.add(new UILabel(this, "gui.door_factory.door_movement").setPosition(0, y + 2));
		propContainer.add(selDoorMovement);

		//Opening time
		y += 12;
		tfOpenTime = new UITextField(this, null).setSize(30, 0).setPosition(-5, y, Anchor.RIGHT).register(this);
		propContainer.add(new UILabel(this, "gui.door_factory.door_open_time").setPosition(0, y + 2));
		propContainer.add(tfOpenTime);

		//Auto close time
		y += 12;
		tfAutoCloseTime = new UITextField(this, null).setSize(30, 0).setPosition(-5, y, Anchor.RIGHT).register(this);
		propContainer.add(new UILabel(this, "gui.door_factory.door_auto_close_time").setPosition(0, y + 2));
		propContainer.add(tfAutoCloseTime);

		//Double door
		y += 12;
		cbDoubleDoor = new UICheckBox(this).setPosition(-15, y, Anchor.RIGHT).register(this);
		propContainer.add(new UILabel(this, "gui.door_factory.door_double_door").setPosition(0, y + 2));
		propContainer.add(cbDoubleDoor);

		//Proximity detection
		y += 12;
		cbProximity = new UICheckBox(this).setPosition(-15, y, Anchor.RIGHT).register(this);
		propContainer.add(new UILabel(this, "gui.door_factory.proximity_detection").setPosition(0, y + 2));
		propContainer.add(cbProximity);

		//Redstone behavior
		y += 12;
		selRedstone = new UISelect<>(this, 100, Lists.newArrayList(RedstoneBehavior.values()));
		selRedstone.setPosition(0, y, Anchor.RIGHT);
		selRedstone.setLabelPattern("gui.door_factory.redstone_behavior.%s").register(this);
		propContainer.add(new UILabel(this, "gui.door_factory.redstone_behavior").setPosition(0, y + 2));
		propContainer.add(selRedstone);

		//Door sound
		y += 12;
		selDoorSound = new UISelect<>(this, 100, getSortedList(DoorRegistry.listSounds().keySet(), "gui.door_factory.door_sound."));
		selDoorSound.setPosition(0, y, Anchor.RIGHT);
		selDoorSound.setLabelPattern("gui.door_factory.door_sound.%s").register(this);
		propContainer.add(new UILabel(this, "gui.door_factory.door_sound").setPosition(0, y + 2));
		propContainer.add(selDoorSound);

		return propContainer;
	}

	private ImmutableList<String> getSortedList(Set<String> set, String prefix)
	{
		return FluentIterable.from(set).toSortedList((String s1, String s2) -> {
			return I18n.format(prefix + s1).compareTo(I18n.format(prefix + s2));
		});
	}

	private UIContainer<?> getMaterialsContainer()
	{
		UIContainer<?> matContainer = new UIContainer<>(this, UIComponent.INHERITED, 80).setPosition(0, 15);

		rbCreate = new UIRadioButton(this, "rbDoor", "gui.door_factory.rb_create").setPosition(30, 0).register(this);
		rbEdit = new UIRadioButton(this, "rbDoor", "gui.door_factory.rb_edit").setPosition(100, 0).register(this);

		matContainer.add(rbCreate);
		matContainer.add(rbEdit);

		contCreate = new UIContainer<>(this).setPosition(0, 14);

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

		contEdit = new UIContainer<>(this).setPosition(0, 14);

		UISlot doorEditSlotSlot = new UISlot(this, tileEntity.doorEditSlot).setPosition(-10, 18, Anchor.RIGHT);
		contEdit.add(new UILabel(this, "gui.door_factory.door_edit_slot").setPosition(0, 23));
		contEdit.add(doorEditSlotSlot);

		matContainer.add(contCreate);
		matContainer.add(contEdit);

		return matContainer;
	}

	private UIContainer<?> getDigicodeContainer()
	{
		UIContainer<?> dcContainer = new UIContainer<>(this, UIComponent.INHERITED, 80).setPosition(0, 15);

		digicode = new Digicode(this).setAnchor(Anchor.CENTER).register(this);
		dcContainer.add(digicode);

		return dcContainer;
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

		selDoorMovement.setSelectedOption(DoorRegistry.getId(tileEntity.getDoorMovement()));
		tfOpenTime.setText(Integer.toString(tileEntity.getOpeningTime()));
		tfAutoCloseTime.setText(Integer.toString(tileEntity.getAutoCloseTime()));
		cbDoubleDoor.setChecked(tileEntity.isDoubleDoor());
		cbProximity.setChecked(tileEntity.hasProximityDetection());
		selRedstone.select(tileEntity.getRedstoneBehavior());
		selDoorSound.setSelectedOption(DoorRegistry.getId(tileEntity.getDoorSound()));
	}

	@Subscribe
	public void onCheckedEvent(UICheckBox.CheckEvent event)
	{
		if (event.getComponent() == cbDoubleDoor)
			tileEntity.setDoubleDoor(event.isChecked());
		else if (event.getComponent() == cbProximity)
			tileEntity.setProximityDetection(event.isChecked());

		DoorFactoryMessage.sendDoorInformations(tileEntity);
	}

	@Subscribe
	public void onSelectEvent(UISelect.SelectEvent<?> event)
	{
		if (event.getNewValue() == null)
			return;

		if (event.getComponent() == selRedstone)
			tileEntity.setRedstoneBehavior((RedstoneBehavior) event.getNewValue());
		else if (event.getComponent() == selDoorMovement)
			tileEntity.setDoorMovement(DoorRegistry.getMovement((String) event.getNewValue()));
		else if (event.getComponent() == selDoorSound)
			tileEntity.setDoorSound(DoorRegistry.getSound((String) event.getNewValue()));

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
	public void onGuiChangeEvent(ValueChange<?, ?> event)
	{
		if (event.getComponent() != tfOpenTime && event.getComponent() != tfAutoCloseTime)
			return;

		try
		{
			int value = Integer.decode((String) event.getNewValue());

			if (event.getComponent() == tfOpenTime)
				tileEntity.setOpeningTime(value);
			if (event.getComponent() == tfAutoCloseTime)
				tileEntity.setAutoCloseTime(value);

			DoorFactoryMessage.sendDoorInformations(tileEntity);
		}
		catch (NumberFormatException e)
		{
			//parsing failed, replace the value of the textfield by the value already in the TE
			//tfOpenTime.setText(Integer.toString(tileEntity.getOpeningTime()));
		}
	}

	@Subscribe
	public void onDigicodeChange(Digicode.CodeChangeEvent event)
	{
		tileEntity.setCode(event.getCode());
		DoorFactoryMessage.sendDoorInformations(tileEntity);
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
		{
			activeTab = event.getComponent().getName();

			if ("tab_dc".equals(activeTab))
				registerKeyListener(digicode);
			else
				unregisterKeyListener(digicode);
		}
	}

}
