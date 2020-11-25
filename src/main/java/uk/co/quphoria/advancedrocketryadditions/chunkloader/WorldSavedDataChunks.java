package uk.co.quphoria.advancedrocketryadditions.chunkloader;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import uk.co.quphoria.advancedrocketryadditions.Reference;
import uk.co.quphoria.advancedrocketryadditions.blocks.BlockMachineTileEntity;

public class WorldSavedDataChunks extends WorldSavedData {
	public static final String NAME = Reference.MODID + "_WorldData";

	public Map<ChunkPos, Map<BlockPos, NBTTagCompound>> worldData;

	public WorldSavedDataChunks(String name) {
		super(name);

		if (worldData == null) {
			worldData = new HashMap<ChunkPos, Map<BlockPos, NBTTagCompound>>();
		}
	}

	public WorldSavedDataChunks() {
		super(NAME);
		if (worldData == null) {
			worldData = new HashMap<ChunkPos, Map<BlockPos, NBTTagCompound>>();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		NBTTagCompound chunks = nbt.getCompoundTag("chunkloaders");
		worldData = new HashMap<ChunkPos, Map<BlockPos, NBTTagCompound>>();
		for(String c_pos : chunks.getKeySet()) {
			Map<BlockPos, NBTTagCompound> chunkData = new HashMap<BlockPos, NBTTagCompound>();
			NBTTagCompound chunkNBT = chunks.getCompoundTag(c_pos);
			for (String pos : chunkNBT.getKeySet()) {
				chunkData.put(parseBlockString(pos), chunkNBT.getCompoundTag(pos));
			}
			worldData.put(parseChunkString(c_pos), chunkData);
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		NBTTagCompound chunks = new NBTTagCompound();

		for(ChunkPos c_pos : worldData.keySet()) {
			Map<BlockPos, NBTTagCompound> chunkData = worldData.get(c_pos);
			NBTTagCompound chunkNBT = new NBTTagCompound();
			for(BlockPos pos : chunkData.keySet()) {
				chunkNBT.setTag(cleanBlockPos(pos), chunkData.get(pos));
			}
			chunks.setTag(c_pos.toString(), chunkNBT);
		}

		nbt.setTag("chunkloaders", chunks);

		return nbt;
	}
	
	public void setBlockData(ChunkPos c_pos, BlockPos pos, String key, boolean data) {
		Map<BlockPos, NBTTagCompound> chunkData = worldData.get(c_pos);
		if (chunkData == null) {
			chunkData = new HashMap<BlockPos, NBTTagCompound>();
		}
		NBTTagCompound nbt = chunkData.get(pos);
		if (nbt == null && data) {
			nbt = new NBTTagCompound();
		}
		if (data) {
			nbt.setBoolean(key, true);
			chunkData.put(pos, nbt);
		} else {
			chunkData.remove(pos);
		}
		if (chunkData.size() == 0) {
			worldData.remove(c_pos);
		} else {
			worldData.put(c_pos, chunkData);
		}
		this.markDirty();
	}

	public boolean getBlockData(ChunkPos c_pos, BlockPos pos, String key) {
		Map<BlockPos, NBTTagCompound> chunkData = worldData.get(c_pos);
		if (chunkData == null) {
			return false;
		}
		NBTTagCompound nbt = chunkData.get(pos);
		if(nbt == null) {
			return false;
		}
		return nbt.getBoolean(key);
	}
	
	public int getChunkCount(World world, ChunkPos c_pos, String key) {
		Map<BlockPos, NBTTagCompound> chunkData = worldData.get(c_pos);
		if (chunkData == null) {
			return 0;
		}
		int blockCount = 0;
		for(BlockPos pos : chunkData.keySet()) {
			if (chunkData.get(pos).getBoolean(key)) {
				// Check if the block is still there
				if (world.getBlockState(pos).getBlock() instanceof BlockMachineTileEntity) {
					blockCount++;
				} else {
					NBTTagCompound nbt = chunkData.get(pos);
					chunkData.remove(pos, nbt);
					if (chunkData.size() == 0) {
						worldData.remove(c_pos);
					} else {
						worldData.put(c_pos, chunkData);
					}
					this.markDirty();
				}
			}
		}
		return blockCount;
	}

	public static WorldSavedDataChunks get(World w) {
		MapStorage s = w.getPerWorldStorage();
		WorldSavedDataChunks d = (WorldSavedDataChunks) s.getOrLoadData(WorldSavedDataChunks.class, NAME);

		if(d == null) {
			d = new WorldSavedDataChunks();
			s.setData(NAME, d);
		}

		return d;
	}
	
	private ChunkPos parseChunkString(String position) {
		position = position.replaceAll("[\\[\\]]", "");
		String[] coordinates = position.split(", ");
		return new ChunkPos(Integer.parseInt(coordinates[0], 10), Integer.parseInt(coordinates[1], 10));
	}
	
	private String cleanBlockPos(BlockPos pos) {
		return "[" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + "]";
	}
	
	private BlockPos parseBlockString(String position) {
		position = position.replaceAll("[\\[\\]]", "");
		String[] coordinates = position.split(", ");
		return new BlockPos(Integer.parseInt(coordinates[0], 10), Integer.parseInt(coordinates[1], 10), Integer.parseInt(coordinates[2], 10));
	}
}