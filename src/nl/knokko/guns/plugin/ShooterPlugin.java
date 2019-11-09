package nl.knokko.guns.plugin;

import nl.knokko.guns.command.*;
import nl.knokko.guns.data.DataManager;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ShooterPlugin extends JavaPlugin {
	
	private static ShooterPlugin instance;
	
	public static ShooterPlugin get(){
		return instance;
	}
	
	private final DataManager dataManager;
	private final ShooterEventHandler eventHandler;
	
	public ShooterPlugin(){
		dataManager = new DataManager(this);
		eventHandler = new ShooterEventHandler();
	}
	
	@Override
	public void onEnable(){
		instance = this;
		dataManager.load();
		getCommand("guns").setExecutor(new CommandGuns());
		getCommand("gunsop").setExecutor(new CommandGunsOP());
		getCommand("join").setExecutor(new CommandJoin());
		getCommand("tutorial").setExecutor(new CommandTutorial());
		getCommand("wavemonster").setExecutor(new CommandWaveMonster());
		Bukkit.getPluginManager().registerEvents(eventHandler, this);
		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Updater(), 0, 1);
	}
	
	@Override
	public void onDisable(){
		dataManager.save();
		instance = null;
	}
	
	public DataManager getDataManager(){
		return dataManager;
	}
	
	public ShooterEventHandler getEventHandler(){
		return eventHandler;
	}
	
	private static class Updater implements Runnable {

		@Override
		public void run() {
			get().getDataManager().update();
			get().getEventHandler().update();
		}
	}
}