package nl.knokko.guns;

import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;

public enum GunType {
	
	MAG7("Mag7", 0, 4, 40, 1, 1.5, 2.0, 0.1, 0, 3, 20, 0),
	P320_PISTOL("P320 Pistol", 0, 20, 60, 1, 2.5, 3.5, 0.3, 0, 2, 10, 0),
	P250("P250", 50, 18, 40, 1, 1.5, 3.0, 0.3, 0, 2, 10, 1),
	AR15("AR15", 100, 4, 60, 1, 3.0, 3.5, 0.1, 0, 2, 20, 2),
	SNIPER("Sniper", 100, 20, 40, 1, 4.5, 10.0, 0.4, 0, 7, 1, 2),
	XM1014("XM1014", 100, 40, 80, 2, 4.5, 6.0, 0.2, 5, 3, 4, 2),
	DESERT_EAGLE("Desert Eagle", 125, 10, 60, 1, 4.5, 6, 0.1, 8, 2, 10, 2),
	RIFLE("Rifle", 150, 50, 100, 1, 5, 7.5, 0.3, 0, 5, 5, 2),
	UMP50("UMP50", 175, 4, 120, 1, 1.5, 3.0, 0.04, 0, 3, 50, 3),
	GLOCK_18("Glock 18", 200, 20, 60, 1, 4.0, 6.5, 0.3, 0, 2, 15, 3),
	P238_PISTOL("P238 Pistol", 200, 10, 80, 1, 4.5, 5, 0.1, 7, 5, 15, 3),
	DUAL_BERETTA("Dual Beretta", 300, 10, 60, 2, 2.0, 2.5, 0.1, 5, 3, 40, 3),
	GALILAR("GalilAR", 150, 4, 100, 1, 2.0, 3.0, 0.1, 0, 3, 50, 4),
	REMINGTON("Remington", 400, 40, 100, 3, 6, 7, 0.1, 10, 2, 30, 4),
	AK47("AK47", 500, 4, 100, 1, 2.5, 4, 0.04, 0, 4, 75, 4),
	FAMAS("Famas", 650, 4, 100, 1, 2.0, 4.0, 0.1, 0, 3, 30, 4),
	DOUBLE_BARREL_SHOT_GUN("Double Barrel Shot Gun", 750, 10, 100, 5, 2, 4, 0.5, 10, 2, 30, 5),
	M4A1("M4A1", 1000, 4, 60, 1, 4.0, 7.5, 0.2, 0, 4, 20, 5),
	BOLT_ACTION_RIFLE("Bolt Action Rifle", 1000, 80, 140, 1, 7.5, 20, 0.5, 7, 5, 1, 6),
	M4A1S("M4A1S", 2000, 10, 100, 1, 5.0, 7.0, 0.4, 0, 4, 20, 6),
	ROCKET_LAUNCHER("Rocket Launcher", 2500, 1, 100, 1, -1, -1, -1, 0, 2, 1, 6);
	
	private final String string;
	
	private final int delay;
	private final int reloadTime;
	private final int bullets;
	private final double baseDamage;
	private final double baseHeadshotDamage;
	private final double knockback;
	private final double angle;
	private final double startSpeed;
	private final int baseAmmo;
	private final int requiredRank;
	private final int price;
	
	public static GunType fromString(String string){
		while(string.charAt(0) == ChatColor.COLOR_CHAR){
			string = string.substring(2);
		}
		return valueOf(GunType.class, string.toUpperCase().replaceAll(" ", "_"));
	}
	
	public static GunType fromItemStack(ItemStack item){
		return values()[item.getDurability() - 1];
	}
	
	GunType(String string, int price, int delay, int reloadTime, int bullets, double baseDamage, double baseHeadshotDamage, double knockback, double angle, double startSpeed, int baseAmmo, int requiredRank){
		this.string = string;
		this.delay = delay;
		this.price = price;
		this.reloadTime = reloadTime;
		this.bullets = bullets;
		this.baseDamage = baseDamage;
		this.baseHeadshotDamage = baseHeadshotDamage;
		this.knockback = knockback;
		this.angle = angle;
		this.startSpeed = startSpeed;
		this.baseAmmo = baseAmmo;
		this.requiredRank = requiredRank;
	}
	
	@Override
	public String toString(){
		return GunRank.values()[requiredRank] + string;
	}
	
	public int getDelay(){
		return delay;
	}
	
	public int getPrice(){
		return price;
	}
	
	public int getReloadTime(){
		return reloadTime;
	}
	
	public int getBulletsPerShot(){
		return bullets;
	}
	
	public double getDamage(int level){
		return baseDamage;
	}
	
	public double getHeadshotDamage(int level){
		return baseHeadshotDamage;
	}
	
	public double getSpreadAngle(){
		return angle;
	}
	
	public double getStartSpeed(){
		return startSpeed;
	}
	
	public double getKnockback(){
		return knockback;
	}
	
	public int getAmmo(int level){
		return baseAmmo;
	}
	
	public int getRequiredRank(){
		return requiredRank;
	}
}