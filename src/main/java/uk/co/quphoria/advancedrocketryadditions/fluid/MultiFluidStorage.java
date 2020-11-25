package uk.co.quphoria.advancedrocketryadditions.fluid;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import uk.co.quphoria.advancedrocketryadditions.blocks.TileEntityMachine;

public class MultiFluidStorage extends FluidTank {
	
	int maxFluids = 1;
	List<FluidStack> fluids;

	public MultiFluidStorage(int capacity, int maxFluids) {
		super(capacity);
		this.maxFluids = maxFluids;
		this.canFill = false;	
		fluids = new ArrayList<FluidStack>();
	}
	
	@Override
	public FluidTank readFromNBT(NBTTagCompound nbt)
    {
		if (!nbt.hasKey("Fluids")) {
			fluids = new ArrayList<FluidStack>();
		} else {
			fluids = new ArrayList<FluidStack>();
			NBTTagList nbt_fluids = nbt.getTagList("Fluids", 10); // NBTBase type 10 = NBTTagCompound
			for (int i = 0; i < nbt_fluids.tagCount(); i++) {
				NBTTagCompound nbt_fluid = nbt_fluids.getCompoundTagAt(i);
				fluids.add(FluidStack.loadFluidStackFromNBT(nbt_fluid));
			}
		}
        return this;
    }

	@Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
		if (fluids.size() > 0) {
			NBTTagList nbt_fluids = new NBTTagList();
			for (FluidStack fluid : fluids) {
				NBTTagCompound nbt_fluid = new NBTTagCompound();
				fluid.writeToNBT(nbt_fluid);
				nbt_fluids.appendTag(nbt_fluid);
			}
			nbt.setTag("Fluids", nbt_fluids);
		}
        return nbt;
    }
	
	@Override
    @Nullable
    public FluidStack getFluid()
    {
		if (fluids.size() == 0) {
			return null;
		}
        return fluids.get(0);
    }
	
	@Override
    public int getFluidAmount()
    {
		int fluidamount = 0;
		for (FluidStack fluid : fluids) {
			fluidamount += fluid.amount;
		}
        return fluidamount;
    }
	
	public ItemStack getFluidBucket(FluidStack targetFluid) {
		for (FluidStack fluid : fluids) {
			if (fluid.getFluid().equals(targetFluid.getFluid())) {
				if (fluid.amount >= Fluid.BUCKET_VOLUME) {
					ItemStack bucket = FluidUtil.getFilledBucket(targetFluid);
					if (!bucket.isEmpty()) {
						fluid.amount -= Fluid.BUCKET_VOLUME;
						if (fluid.amount <= 0) {
							fluids.remove(fluid);
						}
						onContentsChanged();
						return bucket;
					}
					return ItemStack.EMPTY;
				}
			}
		}
		return ItemStack.EMPTY;
	}
	
	public List<FluidStack> getFluids() {
		List<FluidStack> copyFluids = new ArrayList<FluidStack>();
		for (FluidStack fluid : fluids) {
			copyFluids.add(new FluidStack(fluid.getFluid(), fluid.amount));
		}
		return copyFluids;
	}
	
	@Override
	public int fillInternal(FluidStack resource, boolean doFill)
    {
        if (resource == null || resource.amount <= 0)
        {
            return 0;
        }

        if (!doFill)
        {
            if (fluid == null)
            {
                return Math.min(capacity, resource.amount);
            }

            if (!fluid.isFluidEqual(resource))
            {
                if (maxFluids > fluids.size()) {
                	return Math.min(capacity - getFluidAmount(), resource.amount);
                } else {
                	return 0;
                }
            }
            return Math.min(capacity - getFluidAmount(), resource.amount);
        }
        for (FluidStack fluid : fluids) {
        	if (fluid.getFluid() == resource.getFluid()) {
        		int filled = capacity - getFluidAmount();

                if (resource.amount < filled)
                {
                    filled = resource.amount;
                }
                
                fluid.amount += filled;

                onContentsChanged();
                
                if (tile != null)
                {
                    FluidEvent.fireEvent(new FluidEvent.FluidFillingEvent(fluid, tile.getWorld(), tile.getPos(), this, filled));
                }
                return filled;
        	}
        }
        if (maxFluids > fluids.size())
        {
        	FluidStack fluid = new FluidStack(resource, Math.min(capacity - getFluidAmount(), resource.amount));

            onContentsChanged();
            
            fluids.add(fluid);
            if (tile != null)
            {
                FluidEvent.fireEvent(new FluidEvent.FluidFillingEvent(fluid, tile.getWorld(), tile.getPos(), this, fluid.amount));
            }
            return fluid.amount;
        }
        return 0;
    }
	
	@Override
	@Nullable
    public FluidStack drainInternal(FluidStack resource, boolean doDrain)
    {
        if (resource == null || !canDrainFluidType(resource))
        {
            return null;
        }
        
        return drainInternal(resource, resource.amount, doDrain);
    }
	
	@Nullable
    public FluidStack drainInternal(FluidStack resource, int maxDrain, boolean doDrain)
    {
        if (resource == null || maxDrain <= 0 || !canDrainFluidType(resource))
        {
            return null;
        }
        
        FluidStack fluid = null;
        for (FluidStack _fluid : fluids) {
        	if (_fluid.getFluid() == resource.getFluid()) {
        		fluid = _fluid;
        	}
        }
        if (fluid == null) {
        	return null;
        }

        int drained = maxDrain;
        if (fluid.amount < drained)
        {
            drained = fluid.amount;
        }

        FluidStack stack = new FluidStack(fluid, drained);
        if (doDrain)
        {
            fluid.amount -= drained;
            if (fluid.amount <= 0)
            {
            	fluids.remove(fluid);
            }

            onContentsChanged();

            if (tile != null)
            {
                FluidEvent.fireEvent(new FluidEvent.FluidDrainingEvent(fluid, tile.getWorld(), tile.getPos(), this, drained));
            }
        }
        return stack;
    }
	
	private void removeEmpty() {
		for (FluidStack fluid : fluids) {
			if (fluid == null || fluid.amount == 0) {
				fluids.remove(fluid);
			}
		}
	}
	
	@Override
    public FluidStack drain(FluidStack resource, boolean doDrain)
    {
        if (!canDrainFluidType(resource))
        {
            return null;
        }
        FluidStack dr = drainInternal(resource, doDrain);
        return dr;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain)
    {
    	FluidStack resource = getFluid();
        if (!canDrainFluidType(resource))
        {
            return null;
        }
        FluidStack dr = drainInternal(resource, maxDrain, doDrain);
        return dr;
    }
    
    @Override
    public boolean canDrainFluidType(@Nullable FluidStack resource)
    {
    	if (resource == null || !canDrain()) {
    		 return false;
    	}
    	for (FluidStack fluid : fluids) {
    		if (fluid.getFluid() == resource.getFluid()) {
    			return true;
    		}
    	}
    	return false;
    }
    @Override
    protected void onContentsChanged()
    {
    	removeEmpty();
    	if (tile != null) {
    		((TileEntityMachine)tile).setBlockToUpdate();
    	}
    }
}
