package uk.co.quphoria.advancedrocketryadditions.network;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class CreateBucketNetworkHandler implements IMessageHandler<CreateBucketPacket, IMessage> {
	@Override
	public IMessage onMessage(CreateBucketPacket message, MessageContext ctx) {
		// This is the player the packet was sent to the server from
		EntityPlayerMP serverPlayer = ctx.getServerHandler().player;
		// The value that was sent
		BlockPos pos = message.getBlockPos();
		FluidStack fluid = message.getFluidStack();
		// Execute the action on the main server thread by adding it as a scheduled task
		serverPlayer.getServerWorld().addScheduledTask(() -> {
			World world = serverPlayer.world;
			TileEntity tile = world.getTileEntity(pos);
//			if (tile instanceof TileEntityVoidPump) {
//				((TileEntityVoidPump)tile).getFluidBucket(fluid, serverPlayer);
//			}
		});
		// No response packet
		return null;
	}
}