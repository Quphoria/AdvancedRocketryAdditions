package uk.co.quphoria.advancedrocketryadditions.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class CreateBucketPacket implements IMessage {
	
	private NBTTagCompound data;
	private BlockPos blockPos;
	private FluidStack targetFluid;
	
	public CreateBucketPacket(){}

	public CreateBucketPacket(BlockPos blockPos, FluidStack targetFluid) {
		data = new NBTTagCompound();
		targetFluid.writeToNBT(data);
		this.blockPos = blockPos;
		this.targetFluid = targetFluid;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		data = new NBTTagCompound();
		data.setInteger("x", blockPos.getX());
		data.setInteger("y", blockPos.getY());
		data.setInteger("z", blockPos.getZ());
		targetFluid.writeToNBT(data);
		ByteBufUtils.writeTag(buf, data);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		data = ByteBufUtils.readTag(buf);
		blockPos = new BlockPos(data.getInteger("x"), data.getInteger("y"), data.getInteger("z"));
		targetFluid = FluidStack.loadFluidStackFromNBT(data);
	}

	public FluidStack getFluidStack() {
		return targetFluid;
	}

	public BlockPos getBlockPos() {
		return blockPos;
	}
}