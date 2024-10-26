package io.github.hashibutogarasu.mla;

import io.github.hashibutogarasu.mla.sounds.ModSounds;
import net.fabricmc.api.ModInitializer;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class MojangLogoAnimation implements ModInitializer {

	@Override
	public void onInitialize() {
		Registry.register(Registries.SOUND_EVENT, "mla:mojang_sound", ModSounds.MOJANG_LOGO_SOUND_EVENT);
		Registry.register(Registries.SOUND_EVENT, "mla:mojang_april_fool_sound", ModSounds.MOJANG_APRIL_FOOL_SOUND_EVENT);
	}
}