package nl.knokko.guns.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;

import nl.knokko.guns.GunType;
import nl.knokko.guns.map.ShooterMap;
import nl.knokko.guns.plugin.ShooterPlugin;

public class DataManager {
	
	private static final byte VERSION_03 = 0;
	private static final byte VERSION_04 = 1;
	
	private final File file;
	
	private final Map<UUID,PlayerData> playerMap;
	
	private final Collection<PlayerEntry> onlinePlayers;
	private final Collection<ShooterMap> maps;
	
	private final MonsterMap monsters;
	
	private long currentTick;

	public DataManager(ShooterPlugin plugin) {
		plugin.getDataFolder().mkdirs();
		file = new File(plugin.getDataFolder() + "/guns.data");
		playerMap = new HashMap<UUID,PlayerData>();
		onlinePlayers = new ArrayList<PlayerEntry>();
		maps = new ArrayList<ShooterMap>();
		monsters = new MonsterMap();
	}
	
	public void load(){
		try {
			playerMap.clear();//in case this is not the first time load() is called
			FileInputStream fileInput = new FileInputStream(file);
			DataInputStream input = new DataInputStream(fileInput);
			byte encoding = input.readByte();
			if(encoding == VERSION_03)
				load03(input);
			else if(encoding == VERSION_04)
				load04(input);
			else
				Bukkit.getLogger().severe("Unknown shooter data encoding: " + encoding);
			fileInput.close();
		} catch(IOException ioex){
			Bukkit.getLogger().warning("Failed to load gun data of the players: " + ioex.getMessage());
			Bukkit.getLogger().warning("This is ok if this is the first time the plug-in is used.");
		}
	}
	
	private void load03(DataInputStream input) throws IOException {
		int size = input.readInt();
		for(int counter = 0; counter < size; counter++)
			playerMap.put(new UUID(input.readLong(), input.readLong()), new PlayerData(input));
		size = input.readInt();
		for(int counter = 0; counter < size; counter++)
			maps.add(ShooterMap.load03(input));
		//monsters stays empty
	}
	
	private void load04(DataInputStream input) throws IOException {
		int size = input.readInt();
		for(int counter = 0; counter < size; counter++)
			playerMap.put(new UUID(input.readLong(), input.readLong()), new PlayerData(input));
		size = input.readInt();
		for(int counter = 0; counter < size; counter++)
			maps.add(ShooterMap.load04(input));
		monsters.load04(input);
	}
	
	public void save(){
		try {
			FileOutputStream fileOutput = new FileOutputStream(file);
			DataOutputStream output = new DataOutputStream(fileOutput);
			save04(output);
			fileOutput.close();
		} catch(IOException ioex){
			Bukkit.getLogger().severe("Failed to save gun data of the players: " + ioex.getMessage());
		}
	}
	
	@SuppressWarnings("unused")
	private void save03(DataOutputStream output) throws IOException {
		output.writeByte(VERSION_03);
		output.writeInt(playerMap.size());
		Set<Entry<UUID,PlayerData>> entries = playerMap.entrySet();
		for(Entry<UUID,PlayerData> entry : entries)
			entry.getValue().save(output, entry.getKey());
		output.writeInt(maps.size());
		for(ShooterMap map : maps)
			map.save03(output);
	}
	
	private void save04(DataOutputStream output) throws IOException {
		output.writeByte(VERSION_04);
		output.writeInt(playerMap.size());
		Set<Entry<UUID,PlayerData>> entries = playerMap.entrySet();
		for(Entry<UUID,PlayerData> entry : entries)
			entry.getValue().save(output, entry.getKey());
		output.writeInt(maps.size());
		for(ShooterMap map : maps)
			map.save04(output);
		monsters.save04(output);
	}
	
	public void update(){
		currentTick++;
		for(ShooterMap map : maps)
			map.update();
	}
	
	public PlayerData getPlayer(UUID id){
		PlayerData entry = playerMap.get(id);
		if(entry == null){
			entry = new PlayerData();
			playerMap.put(id, entry);
		}
		return entry;
	}
	
	public long getTick(){
		return currentTick;
	}
	
	public MonsterMap getMonsters(){
		return monsters;
	}
	
	public Collection<PlayerEntry> getOnlinePlayers(){
		return onlinePlayers;
	}
	
	public void setOnline(UUID player){
		PlayerData data = getPlayer(player);
		data.online = true;
		onlinePlayers.add(new PlayerEntry(player, data));
	}
	
	public void setOffline(UUID player){
		getPlayer(player).online = false;
		Iterator<PlayerEntry> iterator = onlinePlayers.iterator();
		while(iterator.hasNext()){
			PlayerEntry entry = iterator.next();
			if(entry.getID() == player){
				iterator.remove();
				break;
			}
		}
		for(ShooterMap map : maps){
			map.onPlayerLeave(player);
		}
	}
	
	public ShooterMap getMap(int number){
		for(ShooterMap map : maps)
			if(map.getNumber() == number)
				return map;
		return null;
	}
	
	public void addMap(int number, int spawnX, int spawnY, int spawnZ){
		maps.add(new ShooterMap(number, spawnX, spawnY, spawnZ));
	}
	
	public static class PlayerEntry implements Entry<UUID,PlayerData> {
		
		private final UUID id;
		private final PlayerData data;
		
		public PlayerEntry(UUID id, PlayerData data){
			this.id = id;
			this.data = data;
		}

		@Override
		public UUID getKey() {
			return id;
		}

		@Override
		public PlayerData getValue() {
			return data;
		}

		@Override
		public PlayerData setValue(PlayerData value) {
			throw new UnsupportedOperationException();
		}
		
		public UUID getID(){
			return id;
		}
		
		public PlayerData getData(){
			return data;
		}
	}
	
	public static class PlayerData {
		
		private static void byteToBinary(byte number, boolean[] dest, int startIndex){
			int i = number & 0xFF;
			if(i >= 128){
				dest[startIndex] = true;
				i -= 128;
			}
			startIndex++;
			if(i >= 64){
				dest[startIndex] = true;
				i -= 64;
			}
			startIndex++;
			if(i >= 32){
				dest[startIndex] = true;
				i -= 32;
			}
			startIndex++;
			if(i >= 16){
				dest[startIndex] = true;
				i -= 16;
			}
			startIndex++;
			if(i >= 8){
				dest[startIndex] = true;
				i -= 8;
			}
			startIndex++;
			if(i >= 4){
				dest[startIndex] = true;
				i -= 4;
			}
			startIndex++;
			if(i >= 2){
				dest[startIndex] = true;
				i -= 2;
			}
			startIndex++;
			if(i >= 1)
				dest[startIndex] = true;
			//00001100
		}
		
		private static byte byteFromBinary(boolean[] source, int startIndex){
	    	short number = 0;
	    	if(source[startIndex++]) number += 128;//0
	    	if(source[startIndex++]) number += 64;//0
	    	if(source[startIndex++]) number += 32;//0
	    	if(source[startIndex++]) number += 16;//0
	    	if(source[startIndex++]) number += 8;//8
	    	if(source[startIndex++]) number += 4;//12
	    	if(source[startIndex++]) number += 2;//12
	    	if(source[startIndex++]) number++;//12
	    	return (byte) number;
	    }
		
		private int coins;
		private int xp;
		private byte rank;
		
		private int grenades;
		private int medkits;
		
		private boolean[] ownedGuns;
		
		//next variables do not need saving
		private long lastMainShootTick;
		private long nextMainShootTick;
		private long reloadMainTick;
		private boolean online;
		private WaveMonster boundMonster;
		
		public PlayerData(){
			rank = 0;
			grenades = 0;
			medkits = 0;
			coins = 0;
			ownedGuns = new boolean[24];//even though there are only 21 guns, this is easier for saving and loading
			ownedGuns[0] = true;
			ownedGuns[1] = true;
		}
		
		public PlayerData(DataInputStream input) throws IOException {
			coins = input.readInt();
			xp = input.readInt();
			rank = input.readByte();
			grenades = input.readInt();
			medkits = input.readInt();
			ownedGuns = new boolean[24];
			byteToBinary(input.readByte(), ownedGuns, 0);
			byteToBinary(input.readByte(), ownedGuns, 8);
			byteToBinary(input.readByte(), ownedGuns, 16);
		}
		
		public void save(DataOutputStream output, UUID id) throws IOException{
			output.writeLong(id.getMostSignificantBits());
			output.writeLong(id.getLeastSignificantBits());
			output.writeInt(coins);
			output.writeInt(xp);
			output.writeByte(rank);
			output.writeInt(grenades);
			output.writeInt(medkits);
			output.writeByte(byteFromBinary(ownedGuns, 0));
			output.writeByte(byteFromBinary(ownedGuns, 8));
			output.writeByte(byteFromBinary(ownedGuns, 16));
		}
		
		public int getCoins(){
			return coins;
		}
		
		public void addCoins(int amount){
			coins += amount;
		}
		
		public void removeCoins(int amount){
			coins -= amount;
		}
		
		public void setCoins(int amount){
			coins = amount;
		}
		
		public byte getRank(){
			return rank;
		}
		
		public void setRank(byte newRank){
			if(newRank < 1 || newRank > 6)
				throw new IllegalArgumentException("Invalid rank: " + newRank);
			rank = newRank;
			xp = 0;
		}
		
		public int getXP(){
			return xp;
		}
		
		public static final int XP_1 = 100;
		public static final int XP_2 = 300;
		public static final int XP_3 = 1000;
		public static final int XP_4 = 2500;
		public static final int XP_5 = 5000;
		public static final int XP_6 = 10000;
		
		public void addXP(int amount){
			xp += amount;//increase rank if xp is high enough
			if(rank == 0 && xp >= XP_1){
				rank++;
				xp -= XP_1;
			}
			if(rank == 1 && xp >= XP_2){
				rank++;
				xp -= XP_2;
			}
			if(rank == 2 && xp >= XP_3){
				rank++;
				xp -= XP_3;
			}
			if(rank == 3 && xp >= XP_4){
				rank++;
				xp -= XP_4;
			}
			if(rank == 4 && xp >= XP_5){
				rank++;
				xp -= XP_5;
			}
			if(rank == 5 && xp >= XP_6){
				rank++;
				xp -= XP_6;
			}
		}
		
		public void clearXP(){
			xp = 0;
		}
		
		public boolean owns(GunType type){
			return ownedGuns[type.ordinal()];
		}
		
		public void setOwned(GunType type){
			ownedGuns[type.ordinal()] = true;
			System.out.println("DataManager: ownedGuns is " + Arrays.toString(ownedGuns));
		}
		
		public boolean isShooting(){
			return lastMainShootTick + 10 >= ShooterPlugin.get().getDataManager().getTick();
		}
		
		public void setShooting(){
			lastMainShootTick = ShooterPlugin.get().getDataManager().currentTick;
		}
		
		public boolean isOnline(){
			return online;
		}
		
		public void setShootDelay(int delay){
			nextMainShootTick = ShooterPlugin.get().getDataManager().getTick() + delay;
		}
		
		public boolean canShootNow(){
			long tick = ShooterPlugin.get().getDataManager().getTick();
			return tick >= nextMainShootTick && tick > reloadMainTick;
		}
		
		public void setReloading(int reloadTime){
			reloadMainTick = ShooterPlugin.get().getDataManager().getTick() + reloadTime;
		}
		
		public boolean shouldReload(){
			return ShooterPlugin.get().getDataManager().getTick() == reloadMainTick;
		}
		
		public void addGrenade(){
			grenades++;
		}
		
		public void consumeGrenades(int amount){
			grenades -= amount;
		}
		
		public int getGrenades(){
			return grenades;
		}
		
		public void addMedkit(){
			medkits++;
		}
		
		public void consumeMedkits(int amount){
			medkits -= amount;
		}
		
		public int getMedkits(){
			return medkits;
		}
		
		public void bindMonster(WaveMonster monster){
			boundMonster = monster;
		}
		
		public WaveMonster getBoundMonster(){
			return boundMonster;
		}
	}
}