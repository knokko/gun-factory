package nl.knokko.guns.command;

import nl.knokko.guns.GunType;
import nl.knokko.guns.map.ShooterMap;
import nl.knokko.guns.menu.MenuFactory;
import nl.knokko.guns.plugin.ShooterPlugin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandJoin implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length == 1){
			if(sender instanceof Player){
				Player player = (Player) sender;
				try {
					int mapNumber = Integer.parseInt(args[0]);
					ShooterMap map = ShooterPlugin.get().getDataManager().getMap(mapNumber);
					if(map != null){
						if(!map.isFull()){
							player.openInventory(MenuFactory.createMapEquipment(mapNumber, GunType.MAG7, GunType.P320_PISTOL));
						}
						else {
							sender.sendMessage(ChatColor.RED + "Map " + mapNumber + " is full.");
						}
					}
					else {
						sender.sendMessage(ChatColor.RED + "There is no map " + mapNumber);
					}
				} catch(NumberFormatException nfe){
					sender.sendMessage(ChatColor.RED + "The map number must be a number.");
				}
			}
			else {
				sender.sendMessage("Only players can join a map!");
			}
		}
		else {
			sender.sendMessage(ChatColor.RED + "You should use /join <map number>");
		}
		return false;
	}

}
