package uk.co.quphoria.advancedrocketryadditions.init;

import uk.co.quphoria.advancedrocketryadditions.Reference;
import uk.co.quphoria.advancedrocketryadditions.fuels.HyperFuel;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import zmaster587.advancedRocketry.api.fuel.FuelRegistry;
import zmaster587.advancedRocketry.api.fuel.FuelRegistry.FuelType;

@Mod.EventBusSubscriber(modid=Reference.MODID)
public class ModFluids {
	
	static HyperFuel hyperfuel;
	static ItemStack hyperfuelbucket;
	
	public static void preInit() {
		hyperfuel = new HyperFuel();
	}
	
	public static void init() {
//		FluidUtil.getFilledBucket(targetFluid);
		FuelRegistry.instance.registerFuel(FuelType.LIQUID, hyperfuel, 100000f);
		hyperfuelbucket = FluidUtil.getFilledBucket(new FluidStack(hyperfuel, Fluid.BUCKET_VOLUME));
	}
	
	@SubscribeEvent
	public static void registerFluids(RegistryEvent.Register<Item> event) {
		//FluidRegistry.registerFluid(hyperfuel);
		FluidRegistry.addBucketForFluid(hyperfuel);
	}
}
