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

package net.malisis.doors.door;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.malisis.doors.door.movement.CarriageDoorMovement;
import net.malisis.doors.door.movement.CurtainMovement;
import net.malisis.doors.door.movement.DoubleRotateMovement;
import net.malisis.doors.door.movement.DoubleSlideMovement;
import net.malisis.doors.door.movement.FenceGateMovement;
import net.malisis.doors.door.movement.ForcefieldMovement;
import net.malisis.doors.door.movement.IDoorMovement;
import net.malisis.doors.door.movement.RotateAndPlaceMovement;
import net.malisis.doors.door.movement.RotateAndSlideMovement;
import net.malisis.doors.door.movement.RotateAroundMovement;
import net.malisis.doors.door.movement.Rotating4WaysMovement;
import net.malisis.doors.door.movement.RotatingDoorMovement;
import net.malisis.doors.door.movement.RotatingSplitMovement;
import net.malisis.doors.door.movement.RustyHatchMovement;
import net.malisis.doors.door.movement.SaloonDoorMovement;
import net.malisis.doors.door.movement.Sliding4WaysMovement;
import net.malisis.doors.door.movement.SlidingDoorMovement;
import net.malisis.doors.door.movement.SlidingSplitDoorMovement;
import net.malisis.doors.door.movement.SlidingUpDoorMovement;
import net.malisis.doors.door.movement.SpinningAroundDoorMovement;
import net.malisis.doors.door.movement.SpinningDoorMovement;
import net.malisis.doors.door.movement.VanishingDoorMovement;
import net.malisis.doors.door.movement.VaultDoorMovement;
import net.malisis.doors.door.sound.CarriageDoorSound;
import net.malisis.doors.door.sound.GlassDoorSound;
import net.malisis.doors.door.sound.IDoorSound;
import net.malisis.doors.door.sound.JailDoorSound;
import net.malisis.doors.door.sound.PneumaticSound;
import net.malisis.doors.door.sound.RustyHatchSound;
import net.malisis.doors.door.sound.ShojiDoorSound;
import net.malisis.doors.door.sound.SilentDoorSound;
import net.malisis.doors.door.sound.VanillaDoorSound;
import net.malisis.doors.trapdoor.movement.SlidingTrapDoorMovement;
import net.malisis.doors.trapdoor.movement.TrapDoorMovement;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

/**
 * @author Ordinastie
 *
 */
public class DoorRegistry
{
	private static HashMap<String, IDoorMovement> movements = new HashMap<>();
	private static HashMap<String, IDoorSound> sounds = new HashMap<>();
	static
	{
		registerMovement("rotating_door", new RotatingDoorMovement());
		registerMovement("sliding_door", new SlidingDoorMovement());
		registerMovement("sliding_up_door", new SlidingUpDoorMovement());
		registerMovement("sliding_split_door", new SlidingSplitDoorMovement());
		registerMovement("vault_door", new VaultDoorMovement());
		registerMovement("trap_door", new TrapDoorMovement());
		registerMovement("fence_gate", new FenceGateMovement());
		registerMovement("rotating_split_door", new RotatingSplitMovement());
		registerMovement("sliding_4ways", new Sliding4WaysMovement());
		registerMovement("rotating_4ways", new Rotating4WaysMovement());
		registerMovement("rotate_around", new RotateAroundMovement());
		registerMovement("rotate_slide", new RotateAndSlideMovement());
		registerMovement("rotate_place", new RotateAndPlaceMovement());
		registerMovement("rusty_hatch", new RustyHatchMovement());
		registerMovement("curtain", new CurtainMovement());
		registerMovement("carriageDoor", new CarriageDoorMovement());
		registerMovement("forcefieldDoor", new ForcefieldMovement());
		registerMovement("vanishing_door", new VanishingDoorMovement());
		registerMovement("spinning_door", new SpinningDoorMovement());
		registerMovement("spinning_around_door", new SpinningAroundDoorMovement());
		registerMovement("double_slide_left", new DoubleSlideMovement(false));
		registerMovement("double_slide_right", new DoubleSlideMovement(true));
		registerMovement("double_rotate_left", new DoubleRotateMovement(false));
		registerMovement("double_rotate_right", new DoubleRotateMovement(true));
		registerMovement("sliding_trapdoor", new SlidingTrapDoorMovement());
		registerMovement("saloon_door", new SaloonDoorMovement());

		registerSound("silent_door", new SilentDoorSound());
		registerSound("vanilla_door", new VanillaDoorSound());
		registerSound("glass_door", new GlassDoorSound());
		registerSound("jail_door", new JailDoorSound());
		registerSound("pneumatic_door", new PneumaticSound());
		registerSound("shoji_door", new ShojiDoorSound());
		registerSound("rusty_hatch", new RustyHatchSound());
		registerSound("carriage_door", new CarriageDoorSound());
	}

	//#region Movements
	/**
	 * Gets the movement register for the class
	 *
	 * @param clazz
	 * @return
	 */
	public static IDoorMovement getMovement(Class<? extends IDoorMovement> clazz)
	{
		for (IDoorMovement mvt : movements.values())
			if (mvt.getClass().equals(clazz))
				return mvt;

		throw new IllegalArgumentException(String.format("Door movement %s not found in the registry", clazz.getSimpleName()));
	}

	/**
	 * Registers a new movement type
	 *
	 * @param movement
	 */
	public static void registerMovement(String id, IDoorMovement movement)
	{
		if (movements.get(id) != null)
			throw new IllegalArgumentException(String.format("Door movement %s already in registry", id));
		movements.put(id, movement);
	}

	/**
	 * Gets the movement associated to an id in the registry
	 *
	 * @param id
	 * @return
	 */
	public static IDoorMovement getMovement(String id)
	{
		return movements.get(id);
	}

	/**
	 * Gets the id associated to a movement in the registry
	 *
	 * @param movement
	 * @return
	 */
	public static String getId(IDoorMovement movement)
	{
		if (movement == null)
			return null;

		for (Entry<String, IDoorMovement> entry : movements.entrySet())
		{
			if (entry.getValue() == movement)
				return entry.getKey();
		}

		throw new IllegalArgumentException(String.format("Door movement %s not found in the registry", movement.getClass().getSimpleName()));
	}

	public static Map<String, IDoorMovement> listMovements()
	{
		return Maps.filterValues(movements, new Predicate<IDoorMovement>()
		{
			@Override
			public boolean apply(IDoorMovement input)
			{
				return !input.isSpecial();
			}
		});

	}

	//#end Movements

	//#region Sounds
	/**
	 * Gets the movement register for the class
	 *
	 * @param clazz
	 * @return
	 */
	public static IDoorSound getSound(Class<? extends IDoorSound> clazz)
	{
		for (IDoorSound snd : sounds.values())
			if (snd.getClass().equals(clazz))
				return snd;

		throw new IllegalArgumentException(String.format("Door sound %s not found in the registry", clazz.getSimpleName()));
	}

	/**
	 * Registers a new Sound type
	 *
	 * @param Sound
	 */
	public static void registerSound(String id, IDoorSound Sound)
	{
		if (sounds.get(id) != null)
			throw new IllegalArgumentException(String.format("Door Sound %s already in registry", id));
		sounds.put(id, Sound);
	}

	/**
	 * Gets the Sound associated to an id in the registry
	 *
	 * @param id
	 * @return
	 */
	public static IDoorSound getSound(String id)
	{
		return sounds.get(id);
	}

	/**
	 * Gets the id associated to a Sound in the registry
	 *
	 * @param Sound
	 * @return
	 */
	public static String getId(IDoorSound Sound)
	{
		if (Sound == null)
			return null;

		for (Entry<String, IDoorSound> entry : sounds.entrySet())
		{
			if (entry.getValue() == Sound)
				return entry.getKey();
		}

		throw new IllegalArgumentException(String.format("Door sound %s not found in the registry", Sound.getClass().getSimpleName()));
	}

	public static Map<String, IDoorSound> listSounds()
	{
		return Maps.filterValues(sounds, new Predicate<IDoorSound>()
		{
			@Override
			public boolean apply(IDoorSound input)
			{
				return !(input instanceof RustyHatchSound);
			}
		});
	}
	//#end Sounds
}
