package uk.co.quphoria.advancedrocketryadditions.chunkloader;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ListMultimap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import uk.co.quphoria.advancedrocketryadditions.AdvancedRocketryAdditions;
import uk.co.quphoria.advancedrocketryadditions.config.ModConfig;

public class ChunkLoaderManager implements ForgeChunkManager.LoadingCallback, ForgeChunkManager.PlayerOrderedLoadingCallback {
	List<Ticket> ChunkTickets = new ArrayList<Ticket>();
	
	WorldSavedDataChunks chunkData;
	
	public ChunkLoaderManager() {}
	
	public void AddChunkTicket(World world, Chunk chunk, String lastPlayer) {
		chunkData = WorldSavedDataChunks.get(world);
		Ticket newTicket = ForgeChunkManager.requestPlayerTicket(AdvancedRocketryAdditions.instance, lastPlayer, world, ForgeChunkManager.Type.NORMAL);
		if (newTicket != null) {
			NBTTagCompound nbt = newTicket.getModData();
	        nbt.setInteger("ChunkX", chunk.getPos().x);
	        nbt.setInteger("ChunkZ", chunk.getPos().z);
	        int radius = ModConfig.ChunkloaderRadius;
			for (int x = chunk.getPos().x - radius; x <= chunk.getPos().x + radius; x++) {
	            for (int z = chunk.getPos().z - radius; z <= chunk.getPos().z + radius; z++) {
	            	ForgeChunkManager.forceChunk(newTicket, new ChunkPos(x, z));
	            }
			}
			ChunkTickets.add(newTicket);
		} else {
			String position = ((chunk.x << 4) + 7) + ", " + ((chunk.z << 4) + 7);
			TextComponentString message = new TextComponentString("Player chunk limit reached, unable to load chunks around " + position);
			message.getStyle().setColor(TextFormatting.RED);
			world.getMinecraftServer().getPlayerList().getPlayerByUsername(lastPlayer).sendMessage(message);
		}
	}
	
	public void AddExistingTicket(Ticket ticket, World world, int ChunkX, int ChunkZ) {
		int radius = ModConfig.ChunkloaderRadius;
		for (int x = ChunkX - radius; x <= ChunkX + radius; x++) {
            for (int z = ChunkZ - radius; z <= ChunkZ + radius; z++) {
            	ForgeChunkManager.forceChunk(ticket, new ChunkPos(x, z));
            }
		}
	}
	
	public void RemoveBlockTicket(World world, Chunk chunk) {
		validateTickets(world);
	}
	
	public void AddChunkloader(World world, BlockPos pos, String lastPlayer) {
		if (!world.isRemote) {
			chunkData = WorldSavedDataChunks.get(world);
			Chunk chunk = world.getChunk(pos);
			if (!chunkData.getBlockData(chunk.getPos(), pos, "Chunkloader")) {
				int chunkCount = chunkData.getChunkCount(world, chunk.getPos(), "Chunkloader");
				if (chunkCount == 0) {
					AddChunkTicket(world, chunk, lastPlayer);
				}
				chunkData.setBlockData(chunk.getPos(), pos, "Chunkloader", true);
			}
		}
	}
	
	public void RemoveChunkloader(World world, BlockPos pos) {
		if (!world.isRemote) {
			chunkData = WorldSavedDataChunks.get(world);
			Chunk chunk = world.getChunk(pos);
			if (chunkData.getBlockData(chunk.getPos(), pos, "Chunkloader")) {
				chunkData.setBlockData(chunk.getPos(), pos, "Chunkloader", false);
				int chunkCount = chunkData.getChunkCount(world, chunk.getPos(), "Chunkloader");
				if (chunkCount == 0) {
					RemoveBlockTicket(world, chunk);
				}
			}
		}
	}

	@Override
	public void ticketsLoaded(List<Ticket> tickets, World world) {
		ChunkTickets = new ArrayList<Ticket>(tickets);
		for (Ticket ticket : ChunkTickets) {
			NBTTagCompound nbt = ticket.getModData();
			if (nbt != null) {
				int ChunkX = nbt.getInteger("ChunkX");
				int ChunkZ = nbt.getInteger("ChunkZ");
				AddExistingTicket(ticket, world, ChunkX, ChunkZ);
			}
		}
		validateTickets(world);
	}
	
	@Override
	public ListMultimap<String, Ticket> playerTicketsLoaded(ListMultimap<String, Ticket> tickets, World world) {
		return tickets;
	}
	
	public void validateTickets(World world) {
		chunkData = WorldSavedDataChunks.get(world);
		List<Ticket> invalidTickets = new ArrayList<Ticket>();
		
		if (!world.isRemote) {
			for (int i = 0; i < ChunkTickets.size(); i++) {
				ChunkPos[] chunks = ChunkTickets.get(i).getChunkList().toArray(new ChunkPos[0]);
				boolean validTicket = false;
				for (int j = 0; j < chunks.length; j++) {
					validTicket |= chunkData.getChunkCount(world, chunks[j], "Chunkloader") > 0;
				}
				if (!validTicket) {
					invalidTickets.add(ChunkTickets.get(i));
				}
			}
			
			for (int i = 0; i < invalidTickets.size(); i++) {
				Ticket ticket = invalidTickets.get(i);
				ForgeChunkManager.releaseTicket(ticket);
				ChunkTickets.remove(ticket);
			}
		}
	}
}
