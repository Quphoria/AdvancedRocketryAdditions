package uk.co.quphoria.advancedrocketryadditions.blocks;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockMachineTileEntity<TE extends TileEntityMachine> extends TransparentBlockBasic {

	public static final PropertyBool RUNNING = PropertyBool.create("running");
	
	public BlockMachineTileEntity(String name, Material material) {
		super(name, material);
		this.setHardness(1.5f).setLightLevel(1.0f);
		this.setHarvestLevel("pickaxe", 2);
		this.setDefaultState(this.blockState.getBaseState().withProperty(RUNNING, false));
	}
	
	public abstract Class<TE> getTileEntityClass();
	
	@SuppressWarnings("unchecked")
	public TE getTileEntity(IBlockAccess world, BlockPos pos) {
		return (TE)world.getTileEntity(pos);
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state) {
		return true;
	}
	
	@Nullable
	@Override
	public abstract TE createTileEntity(World world, IBlockState state);
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer.Builder(this).add(new IProperty[] {FACING}).add(new IProperty[] {RUNNING}).build();
	}

}