package uk.co.quphoria.advancedrocketryadditions;

import uk.co.quphoria.advancedrocketryadditions.proxies.IProxy;
import uk.co.quphoria.advancedrocketryadditions.gui.ModGUIHandler;
import uk.co.quphoria.advancedrocketryadditions.init.ModBlocks;
import uk.co.quphoria.advancedrocketryadditions.init.ModFluids;
import uk.co.quphoria.advancedrocketryadditions.init.ModItems;
import uk.co.quphoria.advancedrocketryadditions.network.PacketHandler;
import uk.co.quphoria.advancedrocketryadditions.chunkloader.ChunkLoaderManager;
import uk.co.quphoria.advancedrocketryadditions.config.ModConfig;

import java.io.File;

import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod(modid = Reference.MODID, useMetadata=true, name=Reference.MODNAME, version=Reference.VERSION, dependencies=Reference.DEPENDENCIES, acceptedMinecraftVersions=Reference.ACCEPTED_MINECRAFT_VERSIONS)
public class AdvancedRocketryAdditions {
	
	@Instance
	public static AdvancedRocketryAdditions instance;
	
	public static File config;
	
	public ChunkLoaderManager chunkManager; // Reference as instance.chunkManager
	
	@SidedProxy(modId=Reference.MODID,clientSide=Reference.CLIENTPROXY, serverSide=Reference.SERVERPROXY)
	public static IProxy proxy;
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		ModItems.preInit();
		ModFluids.preInit();
		ModConfig.registerConfig(event);
		chunkManager = new ChunkLoaderManager();
		ForgeChunkManager.setForcedChunkLoadingCallback(AdvancedRocketryAdditions.instance, chunkManager);
		
		NetworkRegistry.INSTANCE.registerGuiHandler(this, new ModGUIHandler());
		proxy.preInit(event);
	}
	
	@EventHandler
	public void init(FMLInitializationEvent event) {
		ModBlocks.init();
		ModItems.init();
		ModFluids.init();
		PacketHandler.init();
		proxy.init(event);
	}
	
	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {		
		proxy.postInit(event);
	}
	
}
