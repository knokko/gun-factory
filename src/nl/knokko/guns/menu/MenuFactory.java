package nl.knokko.guns.menu;

import nl.knokko.guns.GunType;
import nl.knokko.guns.data.DataManager.PlayerData;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.Lists;

public class MenuFactory {
	
	public static final String GUN_MERCHANT = ChatColor.AQUA + "" + ChatColor.BOLD + "Gunshop";
	public static final String ITEM_MERCHANT = ChatColor.AQUA + "" + ChatColor.BOLD + "Itemshop";
	public static final String GUNS_TITLE = "<-- Guns -->";
	public static final String ITEMS_TITLE = "<-- Items -->";
	public static final String GUN_SPAWN = "Get a gun";
	public static final String SELECT_GUN = "Select gun ";
	public static final String SELECT_EQUIPMENT = "Select your equipment for map ";
	
	public static final String selectGun(int gunIndex, int mapNumber){
		return SELECT_GUN + gunIndex + " for map " + mapNumber;
	}
	
	private static String shorten(String original){
		int indexDot = original.indexOf(".");
		if(indexDot != -1){
			int length = Math.min(indexDot + 4, original.length());
			return original.substring(0, length);
		}
		return original;
	}
	
	public static Inventory createGunSpawn(PlayerData data){
		Inventory menu = Bukkit.createInventory(null, 27, GUN_SPAWN);
		menu.setItem(0, createOption(Material.BARRIER, "Close", "Close this menu"));
		int x = 2;
		int y = 0;
		GunType[] types = GunType.values();
		for(GunType type : types){
			menu.setItem(x + 9 * y, createGunOption(type,
					type.getDamage(1) >= 0 ? type.getDamage(1) + " damage" : "Fires explosive rockets",
					type.getHeadshotDamage(1) >= 0 ? type.getHeadshotDamage(1) + " headshot damage" : "No headshot bonus",
					shorten("" + 20.0 / type.getDelay()) + " shots per second",
					type.getBulletsPerShot() + " bullets per shot",
					type.getAmmo(1) + " ammo",
					(type.getReloadTime() / 20.0) + "s reload time",
					ChatColor.GREEN + "Click to get (only in spawn)"
			));
			x++;
			if(x >= 9){
				x = 2;
				y++;
			}
		}
		return menu;
	}
	
	public static Inventory createGunShop(PlayerData data){
		Inventory menu = Bukkit.createInventory(null, 27, GUNS_TITLE);
		menu.setItem(0, createOption(Material.BARRIER, "Close", "Close this menu"));
		int x = 2;
		int y = 0;
		GunType[] types = GunType.values();
		for(GunType type : types){
			menu.setItem(x + 9 * y, createGunOption(type,
					type.getDamage(1) >= 0 ? type.getDamage(1) + " damage" : "Fires explosive rockets",
					type.getHeadshotDamage(1) >= 0 ? type.getHeadshotDamage(1) + " headshot damage" : "No headshot bonus",
					shorten("" + 20.0 / type.getDelay()) + " shots per second",
					type.getBulletsPerShot() + " bullets per shot",
					type.getAmmo(1) + " ammo",
					(type.getReloadTime() / 20.0) + "s reload time",
					(data.getRank() >= type.getRequiredRank() ? ChatColor.GREEN : ChatColor.RED) + "requires rank " + type.getRequiredRank(),
					(data.getCoins() >= type.getPrice() ? ChatColor.GREEN : ChatColor.RED) + "Price: " + type.getPrice() + " coins",
					data.owns(type) ? "Owned" : "Click to buy!"
			));
			x++;
			if(x >= 9){
				x = 2;
				y++;
			}
		}
		return menu;
	}
	
	public static final int GRENADE_PRICE = 100;
	public static final int MEDKIT_PRICE = 100;
	
	public static Inventory createItemShop(PlayerData data){
		Inventory menu = Bukkit.createInventory(null, 9, ITEMS_TITLE);
		menu.setItem(0, createOption(Material.BARRIER, "Close", "Leave the item shop"));
		boolean grenade = data.getCoins() >= GRENADE_PRICE;
		menu.setItem(2, createOption(Material.EGG, "Grenade", 
				"This grenade can be thrown", 
				"and will explode on impact", 
				"If you buy a grenade, you can choose to take", 
				"it when selecting your weapons before joining a game.", 
				"You can only use every grenade once.",
				"Currently, you have " + data.getGrenades() + " grenades",
				(grenade ? ChatColor.GREEN : ChatColor.RED) + "Price: " + GRENADE_PRICE + " coins",
				grenade ? ChatColor.GOLD + "Click to buy!" : ChatColor.RED + "You don't have enough coins to buy this"
		));
		boolean medkit = data.getCoins() >= MEDKIT_PRICE;
		menu.setItem(5, createOption(Material.POTION, "Medkit",
				"A medkit can be consumed to regain health.",
				"If you buy a medkit, you can choose to take", 
				"it when selecting your weapons before joining a game.",
				"You can only use every medkit once.",
				"Currently, you have " + data.getMedkits() + " medkits",
				(medkit ? ChatColor.GREEN : ChatColor.RED) + "Price: " + MEDKIT_PRICE + " coins",
				medkit ? ChatColor.GOLD + "Click to buy!" : ChatColor.RED + "You don't have enough coins to buy this"
		));
		return menu;
	}
	
	public static final String EQUIPMENT_CANCEL = "Cancel";
	public static final String EQUIPMENT_FIRST_GUN = "Your first gun";
	public static final String EQUIPMENT_SECOND_GUN = "Your second gun";
	public static final String EQUIPMENT_SPECIAL = "Special";
	public static final String EQUIPMENT_START = "Start";
	
	public static Inventory createMapEquipment(int mapNumber, GunType gun1, GunType gun2){
		Inventory menu = Bukkit.createInventory(null, 9, SELECT_EQUIPMENT + mapNumber);
		menu.setItem(0, createOption(Material.BARRIER, EQUIPMENT_CANCEL, "Don't play this map"));
		menu.setItem(2, createNamedGunOption(gun1, EQUIPMENT_FIRST_GUN, "Your first gun", "Click to change"));
		menu.setItem(3, createNamedGunOption(gun2, EQUIPMENT_SECOND_GUN, "Your second gun", "Click to change"));
		menu.setItem(5, createOption(Material.EGG, EQUIPMENT_SPECIAL, "Not yet available"));
		menu.setItem(8, createOption(Material.DIAMOND_SWORD, EQUIPMENT_START, "Join the game with", "the weapons you selected"));
		return menu;
	}
	
	public static Inventory createGunSelect(PlayerData data, int gunIndex, int mapNumber, GunType gun1, GunType gun2){
		Inventory menu = Bukkit.createInventory(null, 27, selectGun(gunIndex, mapNumber));
		menu.setItem(0, createOption(Material.BARRIER, "Back", "Back to map equipment"));
		menu.setItem(9, createGunOption(gun1, gunIndex == 1 ? "Your previous gun" : "Your other gun"));
		menu.setItem(18, createGunOption(gun2, gunIndex == 2 ? "Your previous gun" : "Your other gun"));
		int x = 2;
		int y = 0;
		GunType[] types = GunType.values();
		for(GunType type : types){
			if(data.owns(type)){
				menu.setItem(x + 9 * y, createGunOption(type,
						type.getDamage(1) + " damage",
						type.getHeadshotDamage(1) + " headshot damage",
						(1.0 / type.getDelay()) + " shots per second",
						type.getBulletsPerShot() + " bullets per shot",
						type.getAmmo(1) + " ammo",
						(type.getReloadTime() / 20.0) + "s reload time",
						ChatColor.GOLD + "Click to select!"
				));
			}
			x++;
			if(x >= 9){
				x = 2;
				y++;
			}
		}
		return menu;
	}
	
	private static ItemStack createOption(Material icon, String name, String... lore){
		ItemStack stack = new ItemStack(icon);
		ItemMeta meta = Bukkit.getItemFactory().getItemMeta(icon);
		meta.setDisplayName(name);
		if(lore.length > 0)
			meta.setLore(Lists.newArrayList(lore));
		stack.setItemMeta(meta);
		return stack;
	}
	
	private static ItemStack createGunOption(GunType type, String...lore){
		return createNamedGunOption(type, type.toString(), lore);
	}
	
	private static ItemStack createNamedGunOption(GunType type, String name, String...lore){
		ItemStack item = new ItemStack(Material.DIAMOND_HOE);
		ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.DIAMOND_HOE);
		meta.setUnbreakable(true);
		meta.setDisplayName(name);
		if(lore.length > 0)
			meta.setLore(Lists.newArrayList(lore));
		item.setItemMeta(meta);
		
		item.setDurability((short) (1 + type.ordinal()));
        return item;
	}
}