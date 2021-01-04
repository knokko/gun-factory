package nl.knokko.guns;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class Gun {
	
	public static Gun fromItem(ItemStack item){
		ItemMeta meta = item.getItemMeta();
		if(item.getType() == Material.DIAMOND_HOE && meta != null && meta.isUnbreakable()){
			List<String> lore = meta.getLore();
			if(lore != null && lore.size() == 1 && meta.getDisplayName() != null){
				try {
					String n = meta.getDisplayName();
					int indexToken = n.indexOf("<");
					int indexSlash = n.indexOf("/");
					//int indexSpace = n.lastIndexOf(" ", indexSlash);
					GunType type = GunType.fromString(n.substring(0, indexToken - 1));
					int level = Integer.parseInt(lore.get(0).substring(6));
					int ammo = Integer.parseInt(n.substring(indexToken + 1, indexSlash));
					boolean isZooming = item.getDurability() % 2 == 0;
					return new Gun(type, level, ammo, isZooming);
				} catch(Exception ex){
					//this is no gun
				}
			}
		}
		return null;
	}
	
	private GunType type;
	
	private int level;
	
	private int currentAmmo;

	private boolean isZooming;

	public Gun(GunType type, int level, int currentAmmo, boolean isZooming) {
		this.type = type;
		this.level = level;
		this.currentAmmo = currentAmmo;
		this.isZooming = isZooming;
	}

	public void toggleZoom() {
		isZooming = !isZooming;
	}
	
	public Gun(GunType type, int level){
		this(type, level, type.getAmmo(level), false);
	}

	public boolean isZooming() {
		return isZooming;
	}
	
	public GunType getType(){
		return type;
	}
	
	public int getLevel(){
		return level;
	}
	
	public double getDamage(){
		return type.getDamage(level);
	}
	
	public int getMaxAmmo(){
		return type.getAmmo(level);
	}
	
	public int getCurrentAmmo(){
		return currentAmmo;
	}
	
	public void setAmmo(int newAmmo){
		currentAmmo = newAmmo;
	}
	
	public ItemStack createItem(){
		ItemStack item = new ItemStack(Material.DIAMOND_HOE);
		item.setItemMeta(createItemMeta());
		item.setDurability((short) (1 + 2 * type.ordinal() + (isZooming ? 1 : 0)));
		return item;
	}
	
	public ItemMeta createItemMeta(){
		ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_HOE);
		meta.setDisplayName(type.toString() + " <" + currentAmmo + "/" + type.getAmmo(level) + ">");
		meta.setUnbreakable(true);
		List<String> lore = new ArrayList<String>(1);
		lore.add("level " + level);
		meta.setLore(lore);
		return meta;
	}
}