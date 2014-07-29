package net.malisis.doors.gui;

import net.malisis.core.client.gui.Anchor;
import net.malisis.core.client.gui.MalisisGui;
import net.malisis.core.client.gui.component.UISlot;
import net.malisis.core.client.gui.component.container.UIPlayerInventory;
import net.malisis.core.client.gui.component.container.UIWindow;
import net.malisis.core.client.gui.component.decoration.UIProgressBar;
import net.malisis.core.inventory.MalisisInventoryContainer;
import net.malisis.doors.entity.BlockMixerTileEntity;

public class BlockMixerGui extends MalisisGui
{
	private BlockMixerTileEntity tileEntity;

	private UIProgressBar progressBar;
	private UIProgressBar progressBarReversed;

	public BlockMixerGui(BlockMixerTileEntity tileEntity, MalisisInventoryContainer container)
	{
		setInventoryContainer(container);
		this.tileEntity = tileEntity;

		UIWindow window = new UIWindow("tile.block_mixer.name", 176, 166);

		UISlot firstInputSlot = new UISlot(tileEntity.firstInput).setPosition(-60, 34, Anchor.CENTER);
		UISlot secondInputSlot = new UISlot(tileEntity.secondInput).setPosition(60, 34, Anchor.CENTER);
		UISlot outputSlot = new UISlot(tileEntity.output).setPosition(0, 34, Anchor.CENTER);

		progressBar = new UIProgressBar().setPosition(-30, 35, Anchor.CENTER);
		progressBarReversed = new UIProgressBar().setPosition(30, 35, Anchor.CENTER).setReversed();

		UIPlayerInventory playerInv = new UIPlayerInventory(container.getPlayerInventory());

		window.add(firstInputSlot);
		window.add(secondInputSlot);
		window.add(outputSlot);

		window.add(progressBar);
		window.add(progressBarReversed);

		window.add(playerInv);

		addToScreen(window);
	}

	@Override
	public void updateScreen()
	{
		progressBar.setProgress(tileEntity.getMixTimer());
		progressBarReversed.setProgress(tileEntity.getMixTimer());
	}

}