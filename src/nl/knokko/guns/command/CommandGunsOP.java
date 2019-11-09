package nl.knokko.guns.command;

import java.util.List;

import nl.knokko.guns.data.DataManager;
import nl.knokko.guns.data.MonsterMap;
import nl.knokko.guns.data.WaveMonster;
import nl.knokko.guns.map.MapWave;
import nl.knokko.guns.map.ShooterMap;
import nl.knokko.guns.menu.MenuFactory;
import nl.knokko.guns.plugin.ShooterEventHandler;
import nl.knokko.guns.plugin.ShooterPlugin;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

public class CommandGunsOP implements CommandExecutor {
	
	private static void sendUseage(CommandSender sender){
		sender.sendMessage("Command useages:");
		sender.sendMessage("/gunsop killmerchants");
		sender.sendMessage("/gunsop merchant [x] [y] [z]");
		sender.sendMessage("/gunsop itemmerchant [x] [y] [z]");
		sender.sendMessage("/gunsop coins add/set/remove <amount>");
		sender.sendMessage("/gunsop maps add <number> <spawnX> <spawnY> <spawnZ>");
		sender.sendMessage("/gunsop monsters add/bind/delete/spawn");
		sender.sendMessage("/gunsop waves edit/test <map number> <wave number>");
	}
	
	private static void spawnMerchant(Location location){
		Villager villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
		villager.setCustomNameVisible(true);
		villager.setCustomName(MenuFactory.GUN_MERCHANT);
		//villager.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
		villager.setAI(false);
		villager.setCollidable(false);
	}
	
	private static void spawnItemMerchant(Location location){
		Villager villager = (Villager) location.getWorld().spawnEntity(location, EntityType.VILLAGER);
		villager.setCustomNameVisible(true);
		villager.setCustomName(MenuFactory.ITEM_MERCHANT);
		//villager.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(0);
		villager.setAI(false);
		villager.setCollidable(false);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.isOp()){
			sender.sendMessage(ChatColor.DARK_RED + "Only operators can use this command.");
			return false;
		}
		if(args.length == 0){
			sendUseage(sender);
			return false;
		}
		if(args[0].equals("merchant") || args[0].equals("itemmerchant")){
			if(sender instanceof Player){
				Player player = (Player) sender;
				if(args.length == 1){
					if(args[0].equals("merchant"))
						spawnMerchant(player.getLocation());
					else
						spawnItemMerchant(player.getLocation());
				}
				else if(args.length == 4){
					try {
						double x = Double.parseDouble(args[1]);
						try {
							double y = Double.parseDouble(args[2]);
							try {
								double z = Double.parseDouble(args[3]);
								if(args[0].equals("merchant"))
									spawnMerchant(new Location(player.getWorld(), x, y, z));
								else
									spawnItemMerchant(new Location(player.getWorld(), x, y, z));
							} catch(NumberFormatException ex){
								sender.sendMessage("'" + args[3] + "' should be a number");
							}
						} catch(NumberFormatException ex){
							sender.sendMessage("'" + args[2] + "' should be a number");
						}
					} catch(NumberFormatException ex){
						sender.sendMessage("'" + args[1] + "' should be a number");
					}
				}
				else {
					sendUseage(sender);
				}
			}
			else {
				sender.sendMessage("Only player operators can spawn merchants.");
			}
		}
		else if(args[0].equals("killmerchants")){
			if(sender instanceof Player){
				Player player = (Player) sender;
				List<Entity> entities = player.getWorld().getEntities();
				for(Entity entity : entities){
					if(entity.getType() == EntityType.VILLAGER && (MenuFactory.GUN_MERCHANT.equals(entity.getCustomName()) || MenuFactory.ITEM_MERCHANT.equals(entity.getCustomName()))){
						entity.remove();
					}
				}
			}
			else {
				sender.sendMessage("Only player operators can kill merchants in their worlds.");
			}
		}
		else if(args[0].equals("coins")){
			if(sender instanceof Player){
				Player player = (Player) sender;
				if(args.length == 3){
					if(args[1].equals("add")){
						try {
							ShooterPlugin.get().getDataManager().getPlayer(player.getUniqueId()).addCoins(Integer.parseInt(args[2]));
						} catch(NumberFormatException ex){
							sender.sendMessage("'" + args[2] + "' should be an integer.");
						}
					}
					else if(args[1].equals("set")){
						try {
							ShooterPlugin.get().getDataManager().getPlayer(player.getUniqueId()).setCoins(Integer.parseInt(args[2]));
						} catch(NumberFormatException ex){
							sender.sendMessage("'" + args[2] + "' should be an integer.");
						}
					}
					else if(args[1].equals("remove")){
						try {
							ShooterPlugin.get().getDataManager().getPlayer(player.getUniqueId()).removeCoins(Integer.parseInt(args[2]));
						} catch(NumberFormatException ex){
							sender.sendMessage("'" + args[2] + "' should be an integer.");
						}
					}
					else {
						sendUseage(sender);
					}
				}
				else {
					sendUseage(sender);
				}
			}
			else {
				sender.sendMessage("Only player operators can modify their coins.");
			}
		}
		else if(args[0].equals("rank")){
			if(sender instanceof Player){
				Player player = (Player) sender;
				if(args.length == 2){
					try {
						ShooterPlugin.get().getDataManager().getPlayer(player.getUniqueId()).setRank(Byte.parseByte(args[1]));
					} catch(NumberFormatException ex){
						sender.sendMessage("'" + args[1] + "' should be an integer between 0 and 6.");
					}
				}
				else {
					sendUseage(sender);
				}
			}
			else {
				sender.sendMessage("Only player operators can modify their coins.");
			}
		}
		else if(args[0].equals("maps")){
			if(args.length == 6){
				if(args[1].equals("add")){
					try {
						int mapNumber = Integer.parseInt(args[2]);
						try {
							int x = Integer.parseInt(args[3]);
							try {
								int y = Integer.parseInt(args[4]);
								try {
									int z = Integer.parseInt(args[5]);
									DataManager dm = ShooterPlugin.get().getDataManager();
									if(dm.getMap(mapNumber) == null){
										dm.addMap(mapNumber, x, y, z);
										sender.sendMessage(ChatColor.GREEN + "The map has been added!");
									}
									else {
										sender.sendMessage("There is already a map " + mapNumber);
									}
								} catch(NumberFormatException nfe){
									sender.sendMessage("The startZ should be an integer.");
								}
							} catch(NumberFormatException nfe){
								sender.sendMessage("The startY should be an integer.");
							}
						} catch(NumberFormatException nfe){
							sender.sendMessage("The startX should be an integer.");
						}
					} catch(NumberFormatException nfe){
						sender.sendMessage("The map number should be an integer.");
					}
				}
				else if(args[1].equals("setspawn")){
					try {
						int mapNumber = Integer.parseInt(args[2]);
						try {
							int x = Integer.parseInt(args[3]);
							try {
								int y = Integer.parseInt(args[4]);
								try {
									int z = Integer.parseInt(args[5]);
									DataManager dm = ShooterPlugin.get().getDataManager();
									ShooterMap map = dm.getMap(mapNumber);
									if(map != null){
										map.setSpawn(x, y, z);
										sender.sendMessage("The spawn of map " + mapNumber + " has been set to (" + x + ", " + y + ", " + z + "");
									}
									else {
										sender.sendMessage("There is no map " + mapNumber);
									}
								} catch(NumberFormatException nfe){
									sender.sendMessage("The startZ should be an integer.");
								}
							} catch(NumberFormatException nfe){
								sender.sendMessage("The startY should be an integer.");
							}
						} catch(NumberFormatException nfe){
							sender.sendMessage("The startX should be an integer.");
						}
					} catch(NumberFormatException nfe){
						sender.sendMessage("The map number should be an integer.");
					}
				}
				else {
					sender.sendMessage("Use one of these commands:");
					sender.sendMessage("/gunsop maps add <number> <spawnX> <spawnY> <spawnZ>");
					sender.sendMessage("/gunsop maps setspawn <number> <x> <y> <z>");
				}
			}
			else {
				sender.sendMessage("Use one of these commands:");
				sender.sendMessage("/gunsop maps add <number> <spawnX> <spawnY> <spawnZ>");
				sender.sendMessage("/gunsop maps setspawn <number> <x> <y> <z>");
			}
		}
		else if(args[0].equals("monsters")){
			if(args.length == 1){
				sender.sendMessage("Useages:");
				sender.sendMessage("/gunsop monsters add <name> <type>");
				sender.sendMessage("/gunsop monsters bind <name>");
				sender.sendMessage("/gunsop monsters delete <name>");
				sender.sendMessage("/gunsop monsters spawn <name>");
				return false;
			}
			if(args[1].equals("add")){
				if(args.length == 4){
					try {
						EntityType type = EntityType.valueOf(args[3].toUpperCase());
						if(!type.isSpawnable())
							throw new IllegalArgumentException();
						MonsterMap monsters = ShooterPlugin.get().getDataManager().getMonsters();
						if(!monsters.containsKey(args[2])){
							monsters.put(args[2], new WaveMonster(type, args[2]));
							sender.sendMessage(ChatColor.WHITE + "Monster has been created, use " + ChatColor.YELLOW + "/gunsop monsters bind " + args[2] + ChatColor.WHITE + " and " + ChatColor.YELLOW + "/wm ..." + ChatColor.WHITE + " to modify the monster.");
						}
						else {
							sender.sendMessage("There is already a monster with name '" + args[2] + "'");
						}
					} catch(IllegalArgumentException ex){
						sender.sendMessage("Unknown type: '" + args[3] + "'");
						sender.sendMessage(ChatColor.WHITE + "Use " + ChatColor.YELLOW + "/gunsop monsters types" + ChatColor.WHITE + " for a list of available types.");
					}
				}
				else {
					sender.sendMessage("Use /gunsop monsters add <name> <type>");
				}
			}
			else if(args[1].equals("types")){
				EntityType[] types = EntityType.values();
				sender.sendMessage("Available types are:");
				for(EntityType type : types){
					if(type.isSpawnable()){
						sender.sendMessage(type.name().toLowerCase());
					}
				}
			}
			else if(args[1].equals("bind")){
				if(args.length == 3){
					if(sender instanceof Player){
						Player player = (Player) sender;
						DataManager dm = ShooterPlugin.get().getDataManager();
						WaveMonster monster = dm.getMonsters().get(args[2]);
						if(monster != null){
							dm.getPlayer(player.getUniqueId()).bindMonster(monster);
							sender.sendMessage("Monster has been bound, use /wm ... to modify it.");
						}
						else {
							sender.sendMessage("There is no monster with name '" + args[2] + "', but you could create it with /gunsop monsters add " + args[2] + " <type>");
						}
					}
					else {
						sender.sendMessage("Only players can bind monsters");
					}
				}
				else {
					sender.sendMessage("Use /gunsop monsters bind <name>");
				}
			}
			else if(args[1].equals("delete")){
				if(args.length == 3){
					MonsterMap monsters = ShooterPlugin.get().getDataManager().getMonsters();
					if(monsters.containsKey(args[2])){
						monsters.remove(args[2]);
						sender.sendMessage("Monster has been removed");
					}
					else {
						sender.sendMessage("There is no monster with name '" + args[2] + "'");
					}
				}
				else {
					sender.sendMessage("Use /gunsop monsters delete <name>");
				}
			}
			else if(args[1].equals("spawn")){
				if(args.length == 3){
					if(sender instanceof Player){
						Player player = (Player) sender;
						WaveMonster monster = ShooterPlugin.get().getDataManager().getMonsters().get(args[2]);
						if(monster != null){
							monster.spawn(player.getLocation());
							sender.sendMessage("Monster has been spawned at your location.");
						}
						else {
							sender.sendMessage("There is no monster with name '" + args[2] + "'.");
						}
					}
					else {
						sender.sendMessage("Only players can spawn monsters at their location.");
					}
				}
				else {
					sender.sendMessage("Use /gunsop monsters spawn <name>");
				}
			}
		}
		else if(args[0].equals("waves")){
			if(!(sender instanceof Player)){
				sender.sendMessage("Only players can edit or test waves.");
				return false;
			}
			Player player = (Player) sender;
			if(args.length == 4){
				if(args[1].equals("edit") || args[1].equals("test")){
					try {
						int mapNumber = Integer.parseInt(args[2]);
						try {
							int waveNumber = Integer.parseInt(args[3]);
							if(waveNumber >= 1){
								ShooterMap map = ShooterPlugin.get().getDataManager().getMap(mapNumber);
								if(map != null){
									MapWave wave = map.getWave(waveNumber);
									if(args[1].equals("edit")){
										ShooterEventHandler.giveWaveStaff(player, mapNumber, waveNumber);
									}
									else {//test
										wave.spawn(player.getWorld());
									}
								}
								else {
									sender.sendMessage("There is no map" + mapNumber);
								}
							}
							else {
								sender.sendMessage("The waveNumber must be at least 1");
							}
						} catch(NumberFormatException nfe){
							sender.sendMessage("The wave number must be an integer.");
						}
					} catch(NumberFormatException nfe){
						sender.sendMessage("The map number must be an integer.");
					}
				}
				else {
					sender.sendMessage("Use /gunsop waves edit/test <map number> <wave number>");
				}
			}
			else {
				sender.sendMessage("Use /gunsop waves edit/test <map number> <wave number>");
			}
		}
		else {
			sendUseage(sender);
		}
		return false;
	}
}