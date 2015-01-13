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
	public static Setting<Boolean> enhancedMixedBlockPlacement = new BooleanSetting("config.enhancedMixedBlockPlacement", true);
	@ConfigurationSetting
	public static Setting<Boolean> simpleMixedBlockRendering = new BooleanSetting("config.simpleMixedBlockRendering", false);
	@ConfigurationSetting
	public static Setting<Boolean> enableVanishingBlocks = new BooleanSetting("config.enableVanishingBlocks", true);
	@ConfigurationSetting
	public static Setting<Boolean> enableVanishingGlitch = new BooleanSetting("config.enableVanishingGlitch", true);
	@ConfigurationSetting
	public static Setting<Double> vanishingGlitchChance = new DoubleSetting("config.vanishingGlitchChance", 0.0005D);
	@ConfigurationSetting
	public static Setting<Boolean> enableCamoFenceGate = new BooleanSetting("config.enableCamoFenceGate", true);

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
		enhancedMixedBlockPlacement
				.setComment("config.enhancedMixedBlockPlacement.comment1", "config.enhancedMixedBlockPlacement.comment2");
		enableCamoFenceGate.setComment("config.enableCamoFenceGate.comment");
	}
}
