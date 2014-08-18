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
import java.util.Map.Entry;

import net.malisis.doors.door.movement.IDoorMovement;
import net.malisis.doors.door.movement.RotatingDoor;
import net.malisis.doors.door.movement.SlidingDoor;
import net.malisis.doors.door.movement.SlidingUpDoor;
import net.malisis.doors.door.movement.SplitDoor;
import net.malisis.doors.door.sound.GlassDoorSound;
import net.malisis.doors.door.sound.IDoorSound;
import net.malisis.doors.door.sound.JailDoorSound;
import net.malisis.doors.door.sound.SpaceDoorSound;
import net.malisis.doors.door.sound.VanillaDoorSound;

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
		registerMovement("rotating_door", new RotatingDoor());
		registerMovement("sliding_door", new SlidingDoor());
		registerMovement("sliding_up_door", new SlidingUpDoor());
		registerMovement("split_door", new SplitDoor());

		registerSound("vanilla_door", new VanillaDoorSound());
		registerSound("glass_door", new GlassDoorSound());
		registerSound("jail_door", new JailDoorSound());
		registerSound("space_door", new SpaceDoorSound());
	}

	//#region Movements
	/**
	 * Gets the movement register for the class
	 * 
	 * @param clazz
	 * @return
	 */
	public static IDoorMovement getMouvement(Class<? extends IDoorMovement> clazz)
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

	public static HashMap<String, IDoorMovement> listMovements()
	{
		return movements;
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
	public static IDoorSound getSoundId(String id)
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

	public static HashMap<String, IDoorSound> listSounds()
	{
		return sounds;
	}
	//#end Sounds
}
