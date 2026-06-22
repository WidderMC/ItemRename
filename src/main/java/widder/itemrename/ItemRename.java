package widder.itemrename;

import net.fabricmc.api.ModInitializer;

import net.minecraft.resources.Identifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemRename implements ModInitializer {
	public static final String MOD_ID = "itemrename";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Mod Initialized");
		Rename.RegisterCommand();
	}
}
