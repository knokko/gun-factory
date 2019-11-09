package nl.knokko.guns.command;

import nl.knokko.guns.menu.MenuFactory;
import nl.knokko.guns.plugin.ShooterPlugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandGuns implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player){
			Player player = (Player) sender;
			if(player.getWorld().getName().equals("spawn")){
				player.openInventory(MenuFactory.createGunSpawn(ShooterPlugin.get().getDataManager().getPlayer(player.getUniqueId())));
			}
			else {
				player.sendMessage("You can only get free guns in the spawn.");
			}
		}
		else
			sender.sendMessage("Only players can get guns");
		return false;
	}

}