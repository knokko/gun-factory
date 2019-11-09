package nl.knokko.guns.command;

import nl.knokko.guns.data.WaveMonster;
import nl.knokko.guns.plugin.ShooterPlugin;

import org.bukkit.ChatColor;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class CommandWaveMonster implements CommandExecutor {
	
	private static void sendUseage(CommandSender sender){
		sender.sendMessage("Use one of the following commands:");
		sender.sendMessage("/wm type <new type>");
		sender.sendMessage("/wm customname <new custom name>");
		sender.sendMessage("/wm attributes");
		sender.sendMessage("/wm attribute <attribute name>");
		sender.sendMessage("/wm weapon");
		sender.sendMessage("/wm helmet");
		sender.sendMessage("/wm chestplate");
		sender.sendMessage("/wm leggings");
		sender.sendMessage("/wm boots");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender.isOp() && sender instanceof Player){
			Player player = (Player) sender;
			WaveMonster monster = ShooterPlugin.get().getDataManager().getPlayer(player.getUniqueId()).getBoundMonster();
			if(monster != null){
				if(args.length == 0){
					sendUseage(sender);
				}
				else {
					if(args[0].equals("type")){
						if(args.length == 2){
							try {
								EntityType type = EntityType.valueOf(args[1].toUpperCase());
								if(!type.isSpawnable())
									throw new IllegalArgumentException();
								monster.setType(type);
								sender.sendMessage("The type of your monster has been changed.");
							} catch(IllegalArgumentException ex){
								sender.sendMessage("Unknown type: '" + args[1] + "'");
								sender.sendMessage(ChatColor.WHITE + "Use " + ChatColor.YELLOW + "/gunsop monsters types" + ChatColor.WHITE + " for a list of available types.");
							}
						}
						else {
							sender.sendMessage("Use /wm type <type>");
						}
					}
					else if(args[0].equals("customname")){
						if(args.length == 2){
							monster.setCustomName(args[1]);
							sender.sendMessage("The customname of your monster has been changed.");
						}
						else {
							sender.sendMessage("Use /wm customname <new customname>");
						}
					}
					else if(args[0].equals("attributes")){
						Attribute[] attributes = Attribute.values();
						sender.sendMessage("Available attributes are:");
						for(Attribute attribute : attributes){
							sender.sendMessage(attribute.name().toLowerCase());
						}
					}
					else if(args[0].equals("attribute")){
						if(args.length == 3){
							try {
								Attribute attribute = Attribute.valueOf(args[1].toUpperCase());
								try {
									double value = Double.parseDouble(args[2]);
									monster.getAttributeMap().put(attribute, value);
									sender.sendMessage("The attribute has been changed.");
								} catch(NumberFormatException ex){
									sender.sendMessage("The attribute value (" + args[2] + ") should be a number."); 
								}
							} catch(IllegalArgumentException ex){
								sender.sendMessage("Unknown attribute: '" + args[1] + "'");
								sender.sendMessage(ChatColor.WHITE + "Use " + ChatColor.YELLOW + "/wm attributes" + ChatColor.WHITE + " for a list of attributes");
							}
						}
						else {
							sender.sendMessage("Use /wm attribute <attribute name> <attribute value>");
						}
					}
					else if(args[0].equals("weapon")){
						monster.setWeapon(player.getInventory().getItemInMainHand());
						sender.sendMessage("The weapon of your monster has been changed to the item in your main hand.");
					}
					else if(args[0].equals("helmet")){
						monster.setHelmet(player.getInventory().getItemInMainHand());
						sender.sendMessage("The helmet of your monster has been changed to the item in your main hand.");
					}
					else if(args[0].equals("chestplate")){
						monster.setChestplate(player.getInventory().getItemInMainHand());
						sender.sendMessage("The chestplate of your monster has been changed to the item in your main hand.");
					}
					else if(args[0].equals("leggings")){
						monster.setLeggings(player.getInventory().getItemInMainHand());
						sender.sendMessage("The leggings of your monster has been changed to the item in your main hand.");
					}
					else if(args[0].equals("boots")){
						monster.setBoots(player.getInventory().getItemInMainHand());
						sender.sendMessage("The boots of your monster has been changed to the item in your main hand.");
					}
					else {
						sendUseage(sender);
					}
				}
			}
			else {
				player.sendMessage("You need to bind a monster before you can use this command.");
				player.sendMessage("Use /gunsop monsters add <name> <type> if you haven't done this before");
				player.sendMessage("Then bind the monster with /gunsop monsters bind <name>");
			}
		}
		else {
			sender.sendMessage(ChatColor.DARK_RED + "Only player operators can use this command.");
		}
		return false;
	}
}