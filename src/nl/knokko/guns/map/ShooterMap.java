package nl.knokko.guns.map;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import nl.knokko.guns.data.DataManager.PlayerData;
import nl.knokko.guns.plugin.ShooterPlugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import static nl.knokko.guns.data.DataManager.PlayerData.*;

public class ShooterMap {
	
	private int number;
	
	private int spawnX;
	private int spawnY;
	private int spawnZ;
	
	private MapWave[] waves;
	
	//fields that do not need saving
	
	private int countDown;
	
	private int waveIndex;
	
	private int livingMobs;
	
	private List<Player> players;
	private List<PlayerData> playersData;
	private List<Player> spectators;
	private World world;
	
	private byte xpUpdater;
	
	public ShooterMap(int number, int spawnX, int spawnY, int spawnZ){
		this.number = number;
		this.spawnX = spawnX;
		this.spawnY = spawnY;
		this.spawnZ = spawnZ;
		players = new ArrayList<Player>(5);
		playersData = new ArrayList<PlayerData>(5);
		spectators = new ArrayList<Player>(4);
		countDown = 1200;
		waves = new MapWave[1];
	}
	
	public static ShooterMap load03(DataInputStream input) throws IOException {
		return new ShooterMap(input.readInt(), input.readInt(), input.readInt(), input.readInt());
	}
	
	public static ShooterMap load04(DataInputStream input) throws IOException {
		ShooterMap map = new ShooterMap(input.readInt(), input.readInt(), input.readInt(), input.readInt());
		map.waves = new MapWave[input.readInt()];
		for(int index = 0; index < map.waves.length; index++){
			if(input.readBoolean())
				map.waves[index] = MapWave.laod04(input);
		}
		return map;
	}
	
	public void save03(DataOutputStream output) throws IOException {
		output.writeInt(number);
		output.writeInt(spawnX);
		output.writeInt(spawnY);
		output.writeInt(spawnZ);
	}
	
	public void save04(DataOutputStream output) throws IOException {
		output.writeInt(number);
		output.writeInt(spawnX);
		output.writeInt(spawnY);
		output.writeInt(spawnZ);
		output.writeInt(waves.length);
		for(MapWave wave : waves){
			output.writeBoolean(wave != null);
			if(wave != null)
				wave.save04(output);
		}
	}
	
	//unused at the moment
	public void loadMap(){
		String worldName = "map" + number;
		
		try {
			JavaPlugin plugin = (JavaPlugin) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
			Object mvWorldManager = plugin.getClass().getMethod("getMVWorldManager").invoke(plugin);
			mvWorldManager.getClass().getMethod("loadWorld", String.class).invoke(mvWorldManager, worldName);
			Bukkit.getLogger().info("The plugin Shooter forced Multiverse to load world " + worldName);
		} catch(Exception ex){
			Bukkit.getLogger().info("Failed to let Multiverse load world " + worldName + ": " + ex.getMessage());
		}
	}
	
	public void setSpawn(int x, int y, int z){
		spawnX = x;
		spawnY = y;
		spawnZ = z;
	}
	
	public boolean addPlayer(Player player){
		if(isFull()){
			player.sendMessage("Map " + number + " is full.");
			return false;
		}
		if(countDown <= 0){
			player.sendMessage("Map " + number + " is not available");//in game
		}
		World world = Bukkit.getWorld("map" + number);
		if(world == null){
			Bukkit.broadcastMessage("The world is null");
			return false;
		}
		player.teleport(new Location(world, spawnX, spawnY, spawnZ));
		players.add(player);
		playersData.add(ShooterPlugin.get().getDataManager().getPlayer(player.getUniqueId()));
		if(players.size() == 5){
			countDown = 200;
		}
		return true;
	}
	
	public void onPlayerLeave(UUID id){
		if(world == null)
			return;
		boolean didRemove = false;
		Iterator<Player> iterator = players.iterator();
		int playerIndex = 0;
		while(iterator.hasNext()){
			Player next = iterator.next();
			if(next.getUniqueId().equals(id)){
				iterator.remove();
				next.getInventory().clear();
				didRemove = true;
				playersData.remove(playerIndex);
				break;
			}
			playerIndex++;
		}
		iterator = spectators.iterator();
		while(iterator.hasNext()){
			if(iterator.next().getUniqueId().equals(id)){
				iterator.remove();
				return;
			}
		}
		if(didRemove){
			if(players.isEmpty()){
				if(countDown <= 0)
					onDefeat();
				else
					resetMap();
			}
		}
	}
	
	public void onPlayerDie(Player player){
		if(world != null && players.remove(player)){
			playersData.remove(ShooterPlugin.get().getDataManager().getPlayer(player.getUniqueId()));//1 instance of PlayerData per player, so this should work
			if(players.isEmpty()){
				if(countDown <= 0){
					onDefeat();
				}
				else {
					resetMap();
				}
			}
			else {
				if(countDown <= 0){
					spectators.add(player);
					player.setGameMode(GameMode.SPECTATOR);
				}
				else {
					player.teleport(Bukkit.getWorld("spawn").getSpawnLocation());
				}
			}
		}
	}
	
	private void resetMap(){
		cleanMobs();
		countDown = 1200;
		waveIndex = 0;
		world = null;
	}
	
	private void onDefeat(){
		Bukkit.broadcastMessage(ChatColor.RED + "The players have lost on map " + number);
		resetMap();
		Location spawn = Bukkit.getWorld("spawn").getSpawnLocation();
		for(Player spec : spectators){
			spec.getInventory().clear();
			spec.setGameMode(GameMode.SURVIVAL);
			spec.teleport(spawn);
		}
		spectators.clear();
	}
	
	public void update(){
		if(countDown > 0){//game hasn't started
			if(!players.isEmpty()){
				if(world == null)
					world = Bukkit.getWorld("map" + number);
				countDown--;
				for(Player player : players){
					int seconds = countDown / 20;
					if(seconds * 20 != countDown)
						seconds++;
					player.setLevel(seconds);
				}
				if(countDown < 200 && countDown / 20 * 20 == countDown)
					broadcast("Game will start in " + (countDown / 20) + " seconds.");
			}
			else {
				countDown = 1200;
				world = null;
			}
		}
		else if(countDown == 0){//start game
			waveIndex = 0;
			countDown = -1;
			cleanMobs();
			for(Player player : players){
				player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
				player.setGameMode(GameMode.ADVENTURE);
				player.setFoodLevel(20);
			}
		}
		else {//in-game
			if(xpUpdater == 0){
				for(int index = 0; index < players.size(); index++){
					byte rank = playersData.get(index).getRank();
					int xp = playersData.get(index).getXP();
					players.get(index).setLevel(rank);
					int required;
					if(rank == 0)
						required = XP_1;
					else if(rank == 1)
						required = XP_2;
					else if(rank == 2)
						required = XP_3;
					else if(rank == 3)
						required = XP_4;
					else if(rank == 4)
						required = XP_5;
					else if(rank == 5)
						required = XP_6;
					else
						required = xp;
					players.get(index).setExp((float) xp / required);
					xpUpdater = 20;
				}
			}
			xpUpdater--;
			if(livingMobs == 0){
				if(waveIndex < waves.length){
					MapWave wave = waves[waveIndex++];
					if(wave != null){
						broadcast(ChatColor.YELLOW + "wave " + waveIndex);
						wave.spawn(world);
						if(waveIndex >= waves.length)
							broadcast(ChatColor.GREEN + "This is the last wave");
						livingMobs = wave.getAmount();
					}
					else {
						broadcast(ChatColor.RED + "It appears that there is no wave " + waveIndex);
					}
				}
				else {
					resetMap();
					broadcast(ChatColor.GREEN + "The players have won on map " + number);
					Location spawn = Bukkit.getWorld("spawn").getSpawnLocation();
					for(Player player : players){
						player.getInventory().clear();
						player.teleport(spawn);
					}
					players.clear();
					playersData.clear();
					for(Player player : spectators){
						player.getInventory().clear();
						player.setGameMode(GameMode.SURVIVAL);
						player.teleport(spawn);
					}
					spectators.clear();
				}
			}
		}
	}
	
	private void cleanMobs(){
		if(world == null)
			world = Bukkit.getWorld("map" + number);
		List<Entity> entities = world.getEntities();
		for(Entity entity : entities)
			if(entity instanceof LivingEntity && entity.getType() != EntityType.PLAYER)
				entity.remove();
		livingMobs = 0;
	}
	
	public void onMobDie(){
		livingMobs--;
		broadcast(livingMobs + " monsters left");
	}
	
	public void broadcast(String message){
		for(Player player : players)
			player.sendMessage(message);
	}
	
	public int getNumber(){
		return number;
	}
	
	public boolean isFull(){
		return players.size() >= 5;
	}
	
	public MapWave getWave(int waveNumber){
		if(waveNumber < 1)
			return null;
		if(waveNumber - 1 >= waves.length)
			waves = Arrays.copyOf(waves, waveNumber);
		MapWave wave = waves[waveNumber - 1];
		if(wave == null){
			wave = new MapWave();
			waves[waveNumber - 1] = wave;
		}
		return wave;
	}
}