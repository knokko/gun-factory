package nl.knokko.guns.data;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.server.v1_11_R1.NBTCompressedStreamTools;
import net.minecraft.server.v1_11_R1.NBTReadLimiter;
import net.minecraft.server.v1_11_R1.NBTTagCompound;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftItemStack;

public class WaveMonster {
	
	public static WaveMonster load04(DataInputStream input, String monsterName) throws IOException {
		WaveMonster monster = new WaveMonster(EntityType.valueOf(input.readUTF()), monsterName);
		String name = input.readUTF();
		if(name.equals("null"))
			name = null;
		monster.name = name;
		int size = input.readInt();
		for(int index = 0; index < size; index++){
			monster.attributeMap.put(Attribute.valueOf(input.readUTF()), input.readDouble());
		}
		monster.weapon = loadItemStack(input);
		monster.helmet = loadItemStack(input);
		monster.chestplate = loadItemStack(input);
		monster.leggings = loadItemStack(input);
		monster.boots = loadItemStack(input);
		return monster;
	}
	
	public static void saveItemStack(ItemStack stack, DataOutputStream output) throws IOException {
		net.minecraft.server.v1_11_R1.ItemStack nms = CraftItemStack.asNMSCopy(stack);
		NBTTagCompound nbt = new NBTTagCompound();
		if(nms != null)
			nms.save(nbt);
		NBTCompressedStreamTools.a(nbt, (DataOutput) output);
	}
	
	public static ItemStack loadItemStack(DataInputStream input) throws IOException {
		NBTTagCompound nbt = NBTCompressedStreamTools.a(input, NBTReadLimiter.a);
		if(nbt.isEmpty())
			return null;
		net.minecraft.server.v1_11_R1.ItemStack nms = new net.minecraft.server.v1_11_R1.ItemStack(nbt);
		return CraftItemStack.asBukkitCopy(nms);
	}
	
	private final String monsterName;
	
	private EntityType type;
	
	private final Map<Attribute,Double> attributeMap;
	
	private String name;
	
	private ItemStack weapon;
	private ItemStack helmet;
	private ItemStack chestplate;
	private ItemStack leggings;
	private ItemStack boots;
	
	public WaveMonster(EntityType type, String monsterName){
		setType(type);
		this.monsterName = monsterName;
		attributeMap = new HashMap<Attribute,Double>();
	}
	
	public void setType(EntityType type){
		if(type == null)
			throw new NullPointerException("type");
		this.type = type;
	}
	
	public EntityType getType(){
		return type;
	}
	
	public String getMonsterName(){
		return monsterName;
	}
	
	public void setCustomName(String name){
		this.name = name;
	}
	
	public Map<Attribute,Double> getAttributeMap(){
		return attributeMap;
	}
	
	public void setWeapon(ItemStack weapon){
		this.weapon = weapon;
	}
	
	public void setHelmet(ItemStack helmet){
		this.helmet = helmet;
	}
	
	public void setChestplate(ItemStack chestplate){
		this.chestplate = chestplate;
	}
	
	public void setLeggings(ItemStack leggings){
		this.leggings = leggings;
	}
	
	public void setBoots(ItemStack boots){
		this.boots = boots;
	}
	
	public void spawn(World world, int x, int y, int z){
		spawn(new Location(world, x + 0.5, y, z + 0.5));
	}
	
	public void spawn(Location location){
		LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, type);
		Set<Entry<Attribute,Double>> entrySet = attributeMap.entrySet();
		for(Entry<Attribute,Double> entry : entrySet)
			entity.getAttribute(entry.getKey()).setBaseValue(entry.getValue());
		entity.getEquipment().setItemInMainHand(weapon);
		entity.getEquipment().setHelmet(helmet);
		entity.getEquipment().setChestplate(chestplate);
		entity.getEquipment().setLeggings(leggings);
		entity.getEquipment().setBoots(boots);
		entity.setRemoveWhenFarAway(false);
		if(name != null){
			entity.setCustomNameVisible(true);
			entity.setCustomName(name);
		}
	}
	
	public void save04(DataOutputStream output) throws IOException {
		output.writeUTF(type.name());
		if(name != null)
			output.writeUTF(name);
		else
			output.writeUTF("null");
		output.writeInt(attributeMap.size());
		Set<Entry<Attribute,Double>> entries = attributeMap.entrySet();
		for(Entry<Attribute,Double> entry : entries){
			output.writeUTF(entry.getKey().name());
			output.writeDouble(entry.getValue());
		}
		saveItemStack(weapon, output);
		saveItemStack(helmet, output);
		saveItemStack(chestplate, output);
		saveItemStack(leggings, output);
		saveItemStack(boots, output);
	}
}