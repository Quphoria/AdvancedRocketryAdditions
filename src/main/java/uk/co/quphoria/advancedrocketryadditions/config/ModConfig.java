package uk.co.quphoria.advancedrocketryadditions.config;

import java.io.File;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import uk.co.quphoria.advancedrocketryadditions.AdvancedRocketryAdditions;
import uk.co.quphoria.advancedrocketryadditions.Reference;

public class ModConfig {
	public static Configuration config;
	
	public static int ChunkloaderRadius = 2;
	
	public static void init(File file) {
		config = new Configuration(file);
		config.load();
		
		String category = "Chunkloader";
		config.addCustomCategoryComment(category, "Chunkloader Config Section");
		String name = "Chunkloader radius";
		String comment = "Chunkloader radius 0 - 2";
		ChunkloaderRadius = config.getInt(name, category, 2, 0, 2, comment);
	}
	
	public static void registerConfig(FMLPreInitializationEvent event) {
		AdvancedRocketryAdditions.config = new File(event.getModConfigurationDirectory(), Reference.MODID + ".cfg");
		init(AdvancedRocketryAdditions.config);
	}
}
