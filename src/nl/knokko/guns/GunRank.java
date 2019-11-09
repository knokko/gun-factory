package nl.knokko.guns;

import org.bukkit.ChatColor;

public enum GunRank {
	
	R0(ChatColor.WHITE),
	R1(ChatColor.GREEN),
	R2(ChatColor.YELLOW),
	R3(ChatColor.GOLD),
	R4(ChatColor.RED),
	R5(ChatColor.DARK_RED),
	R6(ChatColor.DARK_PURPLE);
	
	private final ChatColor color;
	
	GunRank(ChatColor color){
		this.color = color;
	}
	
	@Override
	public String toString(){
		return color + "" + ChatColor.BOLD;
	}
}