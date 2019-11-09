package nl.knokko.guns.command;

import nl.knokko.guns.plugin.ShooterPlugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandTutorial implements CommandExecutor {
	
	public static void start(Player player){
		if(player.getWorld().getName().equals("spawn")){
			//Location old = player.getLocation();
			player.sendTitle("Tutorial", "Welcome to the tutorial", 10, 50, 20);
			Delayer delayer = new Delayer();
			delayMessage(player, "Shoot", "Right click with your gun to shoot.", delayer);
			delayMessage(player, "Reload", "Press Q with your gun to reload it.", delayer);
			delayMessage(player, "Reload", "You can reload as often as you want.", delayer);
			delayMessage(player, "Reload", "So you can't get out of bullets.", delayer);
			delayMessage(player, "Reload", "Keep the gun in your hand while reloading.", delayer);
			delayMessage(player, "Zoom in", "Left click with your gun to zoom in.", delayer);
			delayMessage(player, "Zoom in", "Left click again to stop zooming in.", delayer);
			//delayTP(player, -349, 96, -916, -50f, 35f, delayer);
			delayMessage(player, "Join", "Right click on one of the signs to join a game.", delayer);
			delayMessage(player, "Choose", "Before you are teleported to the map,", delayer);
			delayMessage(player, "Choose", "you must select 2 guns.", delayer);
			delayMessage(player, "Choose", "At the beginning, you only have 2 guns.", delayer);
			delayMessage(player, "Choose", "So you don't have to select guns the first games.", delayer);
			delayMessage(player, "Choose", "You can buy more guns later.", delayer);
			delayMessage(player, "Join", "When you join a map,", delayer);
			delayMessage(player, "Join", "a counter will count down to 0.", delayer);
			delayMessage(player, "Join", "The counter will be set to 10 when the game is full.", delayer);
			delayMessage(player, "Start", "The game will start when the counter reaches 0.", delayer);
			delayMessage(player, "Game", "20 Waves of monsters will spawn during the game.", delayer);
			delayMessage(player, "Game", "Kill all the monsters to win the game with your team.", delayer);
			delayMessage(player, "Coins", "You will get coins for every monster you kill.", delayer);
			delayMessage(player, "Coins", "You can spend your coins in the shop.", delayer);
			//delayTP(player, -349, 107, -915, 117f, 25f, delayer);
			delayMessage(player, "Rank", "In order to buy a gun,", delayer);
			delayMessage(player, "Rank", "you need more than just the coins.", delayer);
			delayMessage(player, "Rank", "You will also need the right rank.", delayer);
			delayMessage(player, "Rank", "Killing monsters will also give you xp.", delayer);
			delayMessage(player, "Rank", "Your rank will increase if you have enough xp.", delayer);
			delayMessage(player, "Practice", "You can practice your shooting skills in the ranche.", delayer);
			//delayTP(player, -375, 109, -897, 135f, 45f, delayer);
			delayMessage(player, "Good luck", "This was the tutorial.", delayer);
			//delayTP(player, old.getBlockX(), old.getBlockY(), old.getBlockZ(), old.getYaw(), old.getPitch(), delayer);
		}
		else {
			player.sendMessage("You can only see the tutorial in the spawn world.");
		}
	}
	
	static void delayTP(final Player player, final int x, final int y, final int z, final float yaw, final float pitch, Delayer delayer){
		Bukkit.getScheduler().scheduleSyncDelayedTask(ShooterPlugin.get(), new Runnable(){

			@Override
			public void run() {
				if(player.getWorld().getName().equals("spawn"))//TODO right now, you will be teleported on 4 blocks rather than on 1 block...
					player.teleport(new Location(player.getWorld(), x + 0.5, y, z + 0.5, yaw, pitch));
			}
		}, delayer.delay);
	}
	
	private static void delayMessage(final Player player, final String title, final String subTitle, Delayer delayer){
		delayer.delay += 80;
		Bukkit.getScheduler().scheduleSyncDelayedTask(ShooterPlugin.get(), new Runnable(){

			@Override
			public void run() {
				if(player.getWorld().getName().equals("spawn"))
					player.sendTitle(title, subTitle, 10, 50, 20);
			}
		}, delayer.delay);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(sender instanceof Player){
			start((Player) sender);
		}
		else {
			sender.sendMessage("Only players can follow the tutorial.");
		}
		return false;
	}
	
	private static class Delayer {
		
		private int delay;
	}
}