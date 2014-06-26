package net.malisis.doors;

import java.io.File;

import net.malisis.core.configuration.ConfigurationSetting;
import net.malisis.core.configuration.Settings;
import net.malisis.core.configuration.setting.BooleanSetting;
import net.malisis.core.configuration.setting.DoubleSetting;
import net.malisis.core.configuration.setting.Setting;

public class MalisisDoorsSettings extends Settings
{
	@ConfigurationSetting
	public static Setting<Boolean> modifyVanillaDoors = new BooleanSetting("config.modifyVanillaDoors", true);
	@ConfigurationSetting
	public static Setting<Boolean> enableMixedBlocks = new BooleanSetting("config.enableMixedBlocks", true);
	@ConfigurationSetting
	public static Setting<Boolean> simpleMixedBlockRendering = new BooleanSetting("config.simpleMixedBlockRendering", true);
	@ConfigurationSetting
	public static Setting<Boolean> enableVanishingBlocks = new BooleanSetting("config.enableVanishingBlocks", true);
	@ConfigurationSetting
	public static Setting<Boolean> enableVanishingGlitch = new BooleanSetting("config.enableVanishingGlitch", true);
	@ConfigurationSetting
	public static Setting<Double> vanishingGlitchChance = new DoubleSetting("config.vanishingGlitchChance", 0.0005D);

	public MalisisDoorsSettings(File file)
	{
		super(file);
	}

	@Override
	protected void initSettings()
	{
		modifyVanillaDoors.setComment("config.modifyVanillaDoors.comment1", "config.modifyVanillaDoors.comment2");
		simpleMixedBlockRendering.setComment("config.simpleMixedBlockRendering.comment1", "config.simpleMixedBlockRendering.comment2");
		enableVanishingGlitch.setComment("config.enableVanishingGlitch.comment");
		vanishingGlitchChance.setComment("config.vanishingGlitchChance.comment");
	}
}
