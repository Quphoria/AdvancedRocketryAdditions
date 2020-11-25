package uk.co.quphoria.advancedrocketryadditions.energy;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.energy.EnergyStorage;

public class MachineEnergyStorage extends EnergyStorage {
	public MachineEnergyStorage(int capacity, int maxReceive, int energy)
    {
		super(capacity, maxReceive, 0, energy);
        this.maxReceive = maxReceive;
    }
	
    public int getMaxEnergyReceive()
    {
        return maxReceive;
    }
    
    public int extractInternalEnergy(int maxExtract, boolean simulate)
    {
        if (this.capacity <= 0)
            return 0;

        int energyExtracted = Math.min(energy, maxExtract);
        if (!simulate)
            energy -= energyExtracted;
        return energyExtracted;
    }
    
    public double getEnergyFraction() {
		if (energy == 0 || this.capacity == 0) {
			return 0;
		}
		return Math.min(Math.max(((double)energy) / ((double)this.capacity), 0), 1);
	}
    
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("energyLevel", this.energy);
		//compound.setInteger("energyCapacity", this.capacity);
		//compound.setInteger("energyReceive", this.maxReceive);
		return compound;
	}
	
	public void readFromNBT(NBTTagCompound compound) {
		this.energy = compound.getInteger("energyLevel");
		//this.capacity = compound.getInteger("energyCapacity");
		//this.maxReceive = compound.getInteger("energyReceive");
	}
	
	public void setCapacity(int capacity) {
		this.capacity = capacity;
		this.energy = Math.min(this.energy, capacity);
	}
}
