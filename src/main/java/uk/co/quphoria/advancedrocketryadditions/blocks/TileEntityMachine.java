package uk.co.quphoria.advancedrocketryadditions.blocks;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import uk.co.quphoria.advancedrocketryadditions.AdvancedRocketryAdditions;
import uk.co.quphoria.advancedrocketryadditions.Reference;
import uk.co.quphoria.advancedrocketryadditions.chunkloader.ChunkLoaderManager;
import uk.co.quphoria.advancedrocketryadditions.energy.MachineEnergyStorage;

public abstract class TileEntityMachine extends TileEntity implements ITickable {

	protected boolean shouldUpdate = true;
	protected ItemStackHandler upgradeInventory = new ItemStackHandler(4);
	protected MachineEnergyStorage energyStorage = new MachineEnergyStorage(1024 * 10, 1024 * 64 * 2, 0);
	protected Random rand;
	
	protected int lastEnergy = 0;
	protected int lastUpgradeInventory = 0;
	
	protected int maxEnergyUsage = 1024;
	protected int maxWaitTime = 126;
	protected int energyUsage = 0;
	protected int waitTime = 0;
	protected int remainingTime = 0;
	protected String lastPlayer = "";
	protected Boolean usedBefore = false;
	
	protected ChunkLoaderManager chunkloaderManager;
	
	public TileEntityMachine() {
		super();
		energyUsage = maxEnergyUsage;
		waitTime = maxWaitTime;
		chunkloaderManager = AdvancedRocketryAdditions.instance.chunkManager;
		rand = new Random();
	}
	
	public void setBlockToUpdate() {
		sendUpdates();
		shouldUpdate  = false;
	}
	
	protected void sendUpdates() {
		world.markBlockRangeForRenderUpdate(pos, pos);
		world.notifyBlockUpdate(pos, getState(), getState(), 3);
		world.scheduleBlockUpdate(pos,this.getBlockType(),0,0);
		calculateUpgrades();
		markDirty();
	}
	
	private IBlockState getState() {
		return world.getBlockState(pos);
	}
	
	@Override
	@Nullable
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 3, this.getUpdateTag());
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(pkt.getNbtCompound());
	}
	
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
		return oldState.getBlock() != newState.getBlock();
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound = energyStorage.writeToNBT(compound);
		compound.setTag("upgradeInventory", upgradeInventory.serializeNBT());
		if (!world.isRemote) {
			compound.setInteger("remainingTime", remainingTime);
		}
		if (lastPlayer != null && lastPlayer != "") {
			compound.setString("lastPlayer", lastPlayer);
		}
		if (usedBefore) {
			compound.setBoolean("usedBefore", true);
		}
		return super.writeToNBT(compound);
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		energyStorage.readFromNBT(compound);
		upgradeInventory.deserializeNBT(compound.getCompoundTag("upgradeInventory"));
		remainingTime = compound.getInteger("remainingTime");
		if (compound.hasKey("lastPlayer")) {
			lastPlayer = compound.getString("lastPlayer");
		}
		if (compound.hasKey("usedBefore")) {
			usedBefore = compound.getBoolean("usedBefore");
		}
		super.readFromNBT(compound);
		shouldUpdate = true;
		lastEnergy = energyStorage.getEnergyStored();
		lastUpgradeInventory = upgradeInventory.hashCode();
		shouldUpdate = true;
		calculateUpgrades();
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY && facing != EnumFacing.DOWN) {
			return true;
		}
		return super.hasCapability(capability, facing);
	}
	
	@SuppressWarnings("unchecked")
	@Nullable
	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
		if (capability == CapabilityEnergy.ENERGY && facing != EnumFacing.DOWN) {
			return (T)energyStorage;
		}
		return super.getCapability(capability, facing);
	}
	
	public int getEnergy() {
		return energyStorage.getEnergyStored();
	}
	
	public int getMaxEnergy() {
		return energyStorage.getMaxEnergyStored();
	}
	
	public int useEnergy(int energy, boolean simulate) {
		return energyStorage.extractInternalEnergy(energy, simulate);
	}
	
	protected void dropItemStacks(World world, BlockPos pos, ItemStackHandler istack) {
		for(int i = 0; i < istack.getSlots(); i++) {
			ItemStack stack = istack.getStackInSlot(i);
			if (!stack.isEmpty()) {
				EntityItem item = new EntityItem(world, pos.getX(), pos.getY(), pos.getZ(), stack);
				world.spawnEntity(item);
			}
		}		
	}
	
	public void setLastPlayer(String name) {
		lastPlayer = name;
	}
	
	public double getEnergyFraction() {
		return energyStorage.getEnergyFraction();
	}
	
	protected int limit(int value, int min, int max) {
	    return Math.max(min, Math.min(value, max));
	}
	
	protected int hasUpgrade(ResourceLocation item) {
		int amount = 0;
		for (int i = 0; i < upgradeInventory.getSlots(); i++) {
			if (!upgradeInventory.getStackInSlot(i).isEmpty()) {
				if (upgradeInventory.getStackInSlot(i).getItem().equals(Item.REGISTRY.getObject(item))) {
					amount += upgradeInventory.getStackInSlot(i).getCount();
				}
			}
		}
		return amount;
	}
	
	public IItemHandler getUpgradeInventory() {
		return upgradeInventory;
	}
	
	public void breakBlock(World world, BlockPos pos) {
		dropItemStacks(world, pos, upgradeInventory);
		if (chunkloaderManager != null) {
			chunkloaderManager.RemoveChunkloader(getWorld(), pos);
		}
	}
	
	public boolean isRunning() {
		return energyStorage.getEnergyStored() >= energyUsage && canRun();
	}
	
	protected void calculateUpgrades() {
		waitTime = maxWaitTime;
		
		double newEnergyUsage = (double)maxEnergyUsage / Math.pow(2, (double)limit(hasUpgrade(new ResourceLocation("mekanism","energyupgrade")), 0, 24));
		newEnergyUsage *= Math.pow(2, limit(hasUpgrade(new ResourceLocation("mekanism","speedupgrade")), 0, 24) / 4);
		if (newEnergyUsage < 1) {
			newEnergyUsage = 1;
		}
		energyUsage = (int)Math.floor(newEnergyUsage);
		energyStorage.setCapacity(energyUsage * 10);
		waitTime -= 5 * limit(hasUpgrade(new ResourceLocation("mekanism","speedupgrade")), 0, 24);
		if (hasUpgrade(new ResourceLocation("mekanism","filterupgrade")) > 0 && hasFilterItem()) {
			waitTime -= 4;
		}
		
		World worldIn = getWorld();
		if (hasUpgrade(new ResourceLocation("mekanism","anchorupgrade")) > 0) {
			if (chunkloaderManager != null && worldIn != null && lastPlayer != null && lastPlayer != "") {
				chunkloaderManager.AddChunkloader(worldIn, pos, lastPlayer);
			}
		} else {
			if (chunkloaderManager != null && worldIn != null) {
				chunkloaderManager.RemoveChunkloader(worldIn, pos);
			}
		}
		
		if (waitTime < remainingTime) {
			remainingTime = waitTime;
		}
	}

	protected abstract boolean hasFilterItem();

	protected abstract boolean canRun();
	
	protected abstract boolean runMachine();
	
	@Override
	public void update() {
		if (lastUpgradeInventory != upgradeInventory.hashCode()
				|| lastEnergy != energyStorage.getEnergyStored()) {
			shouldUpdate = true;
		}
		
		if (!world.isRemote) {
			if (energyStorage.getEnergyStored() >= energyUsage && canRun()) {
				if (remainingTime <= 0) {
					remainingTime = waitTime;
					boolean collected = runMachine();
					if (collected) {
						energyStorage.extractInternalEnergy(energyUsage, false);
					} else {
						remainingTime = 0;
					}
					shouldUpdate = true;
				} else {
					energyStorage.extractInternalEnergy(energyUsage, false);
					remainingTime--;
				}
			} else {
				remainingTime = waitTime;
			}
		} else {
			// Do this for the progress bar
			if (energyStorage.getEnergyStored() >= energyUsage && canRun()) {
				remainingTime--;
			}
		}
		
		
		if (shouldUpdate) {
			setBlockToUpdate();
		}
	}

	public double getProgressFraction() {
		if (waitTime <= 0) {
			return 1;
		}
		return Math.min(1, Math.max(0, ((double)waitTime - (double)remainingTime) / (double)waitTime));
	}
}
