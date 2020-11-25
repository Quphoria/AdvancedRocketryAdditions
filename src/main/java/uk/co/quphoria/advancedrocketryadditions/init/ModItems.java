package uk.co.quphoria.advancedrocketryadditions.init;

import uk.co.quphoria.advancedrocketryadditions.Reference;
import uk.co.quphoria.advancedrocketryadditions.items.ItemBasic;
import zmaster587.advancedRocketry.api.fuel.FuelRegistry;
import zmaster587.advancedRocketry.api.fuel.FuelRegistry.FuelType;

import java.util.Collections;
import java.util.Comparator;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid=Reference.MODID)
public class ModItems {
	
	static ItemBasic BlackHolePowder;
	
	public static final CreativeTabs tabAdvancedRocketryAdditions = (new CreativeTabs("tabAdvancedRocketryAdditions") {

		@Override
		public ItemStack createIcon() {
			return new ItemStack(Item.getByNameOrId("stone"));
		}
		
		@Override
		public void displayAllRelevantItems(NonNullList<ItemStack> items) {
			super.displayAllRelevantItems(items);
			items.add(ModFluids.hyperfuelbucket);

			// Sort the item list using the ItemSorter instance
			Collections.sort(items, itemSorter);
		}
		
		private ItemSorter itemSorter = new ItemSorter();

		// Sorts items in alphabetical order using their display names
		class ItemSorter implements Comparator<ItemStack> {

			@Override
			public int compare(ItemStack o1, ItemStack o2) {
				Item item1 = o1.getItem();
				Item item2 = o2.getItem();
				
				// If item1 is a block and item2 isn't, sort item1 before item2
				if (((item1 instanceof ItemBlock)) && (!(item2 instanceof ItemBlock))) {
					return -1;
				}

				// If item2 is a block and item1 isn't, sort item1 after item2
				if (((item2 instanceof ItemBlock)) && (!(item1 instanceof ItemBlock))) {
					return 1;
				}

				// If item1 is a block and item2 isn't, sort item1 before item2
				if (((item1 instanceof ItemBasic)) && (!(item2 instanceof ItemBasic))) {
					return -1;
				}

				// If item2 is a block and item1 isn't, sort item1 after item2
				if (((item2 instanceof ItemBasic)) && (!(item1 instanceof ItemBasic))) {
					return 1;
				}
				
				if (item1 == item2) {
					return item1.getMetadata(o1) < item2.getMetadata(o2) ? -1 : 1;
				}

				String displayName1 = o1.getDisplayName();
				String displayName2 = o2.getDisplayName();

				int result = displayName1.compareToIgnoreCase(displayName2);
				return result;
			}
		}
	});
	
	public static void preInit() {
		BlackHolePowder = new ItemBasic("blackholepowder");
	}
	
	public static void init() {
		BlackHolePowder.setCreativeTab(tabAdvancedRocketryAdditions);
		FuelRegistry.instance.registerFuel(FuelType.NUCLEAR, new ItemStack(BlackHolePowder), 100f);
	}
	
	@SubscribeEvent
	@SideOnly(Side.CLIENT)
    public static void itemColorHandlers(ColorHandlerEvent.Item event) {
        ItemColors colors = event.getItemColors();
        
    }
	
	@SubscribeEvent
	public static void registerItems(RegistryEvent.Register<Item> event) {
		event.getRegistry().registerAll(BlackHolePowder);
	}
	
	@SubscribeEvent
	public static void registerRenders(ModelRegistryEvent event) {
		
	}
	
	private static void registerRender(Item item) {
		ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation( item.getRegistryName(), "inventory"));
	}
	
	private static void registerMultipleRenders(Item item, int variants) {
		for (int i = 0; i < variants; i++) {
			ModelLoader.setCustomModelResourceLocation(item, i, new ModelResourceLocation( item.getRegistryName(), "inventory-" + i));
		}
	}
}
