package net.malisis.doors.sound;

import net.malisis.core.registry.MalisisRegistry;
import net.malisis.doors.DoorState;
import net.malisis.doors.MalisisDoors;
import net.minecraft.util.SoundEvent;

public class RustyDoorSound implements IDoorSound
{
	private SoundEvent opening;
	private SoundEvent closing;
	private SoundEvent closed;

	@Override
	public void register()
	{
		opening = MalisisRegistry.registerSound(MalisisDoors.modid, "rustydoor_opening");
		closing = MalisisRegistry.registerSound(MalisisDoors.modid, "rustydoor_closing");
		closed = MalisisRegistry.registerSound(MalisisDoors.modid, "rustydoor_closed");
	}

	@Override
	public SoundEvent getSound(DoorState doorState)
	{
		switch (doorState)
		{
			case OPENING:
				return opening;
			case CLOSING:
				return closing;
			case CLOSED:
				return closed;
			default:
				return null;
		}
	}
}
