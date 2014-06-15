package net.malisis.doors;

import net.minecraftforge.common.config.Configuration;

public class Settings
{
	public static boolean modifyVanillaDoors = true;
	public static boolean enableMixedBlocks = true;
	public static boolean enableVanishingBlocks = true;
	public static boolean enableVanishingGlitch = true;
	public static double vanishingGlitchChance = 0.0005D;
	
	public Settings(Configuration config)
	{
		config.load();
		modifyVanillaDoors = config.get("General", "Enable vanilla doors improvement", true).getBoolean(true);
		enableMixedBlocks = config.get("General", "Enable Block Mixer and Mixed Blocks", true).getBoolean(true);
		enableVanishingBlocks = config.get("General", "Enable Vanishing Frames", true).getBoolean(true);
		enableVanishingGlitch = config.get("General", "Enable Vanishing Frames glitch animation", true).getBoolean(true);
		vanishingGlitchChance = config.get("General", "Vanishing Frames glitch frequency", 0.0005D).getDouble(0.0005D);
		config.save();
	}
}
