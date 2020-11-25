package uk.co.quphoria.advancedrocketryadditions.network;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import uk.co.quphoria.advancedrocketryadditions.Reference;

public class PacketHandler {
	public static final SimpleNetworkWrapper INSTANCE = NetworkRegistry.INSTANCE.newSimpleChannel(Reference.MODID);

	public static void init() {
		INSTANCE.registerMessage(CreateBucketNetworkHandler.class, CreateBucketPacket.class, 0, Side.SERVER);
	}
}
