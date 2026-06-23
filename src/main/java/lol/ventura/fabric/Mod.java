package lol.ventura.fabric;

import lol.ventura.VenturaClient;
import lol.ventura.features.combat.CombatService;
import lol.ventura.foundation.Keyboard;
import lol.ventura.foundation.command.CommandRepository;
import lol.ventura.foundation.event.EventBus;
import lol.ventura.foundation.module.ModuleRepository;
import lol.ventura.foundation.property.PropertyRepository;
import lol.ventura.foundation.rotation.RotationService;
import lol.ventura.misc.font.FontRepository;
import lol.ventura.misc.render.FramebufferManager;
import lol.ventura.misc.spotify.SpotifyService;
import lol.ventura.misc.storage.StorageService;
import net.fabricmc.api.ModInitializer;

public class Mod implements ModInitializer {
	public static final String MOD_ID = "ventura";

	@Override
	public void onInitialize() {
        try {
            VenturaClient.create(
                    StorageService.class,
                    Keyboard.class,
                    EventBus.class,
                    PropertyRepository.class,
                    ModuleRepository.class,
                    FontRepository.class,
                    SpotifyService.class,
                    CommandRepository.class,
                    RotationService.class,
                    CombatService.class,
                    FramebufferManager.class
                  //  BrowserService.class
            );
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Failed to initialize VenturaClient");
        }
    }
}