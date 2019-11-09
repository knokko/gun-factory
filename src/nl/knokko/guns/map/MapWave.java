package nl.knokko.guns.map;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;

import nl.knokko.guns.data.WaveMonster;
import nl.knokko.guns.plugin.ShooterPlugin;

public class MapWave {
	
	private List<Entry> monsters;
	
	public MapWave(){
		this(5);
	}
	
	public MapWave(int capacity){
		monsters = new ArrayList<Entry>(capacity);
	}
	
	public static MapWave laod04(DataInputStream input) throws IOException {
		int size = input.readInt();
		MapWave wave = new MapWave(size);
		for(int count = 0; count < size; count++)
			wave.monsters.add(Entry.load04(input));
		return wave;
	}
	
	public void save04(DataOutputStream output) throws IOException {
		output.writeInt(monsters.size());
		for(Entry monster : monsters)
			monster.save04(output);
	}
	
	public void spawn(World world){
		for(Entry monster : monsters){
			monster.spawn(world);
		}
	}
	
	public void add(String monsterName, int x, int y, int z){
		monsters.add(new Entry(monsterName, x, y, z));
	}
	
	public int getAmount(){
		return monsters.size();
	}
	
	public static class Entry {
		
		private int x;
		private int y;
		private int z;
		
		private String monsterName;
		
		public Entry(String monster, int x, int y, int z){
			monsterName = monster;
			this.x = x;
			this.y = y;
			this.z = z;
		}
		
		public void spawn(World world){
			WaveMonster monster = ShooterPlugin.get().getDataManager().getMonsters().get(monsterName);
			if(monster != null)
				monster.spawn(world, x, y, z);
			else
				Bukkit.broadcastMessage("The monster with name '" + monsterName + "' is missing!");
		}
		
		public static Entry load04(DataInputStream input) throws IOException {
			return new Entry(input.readUTF(), input.readInt(), input.readInt(), input.readInt());
		}
		
		public void save04(DataOutputStream output) throws IOException {
			output.writeUTF(monsterName);
			output.writeInt(x);
			output.writeInt(y);
			output.writeInt(z);
		}
	}
}