package nl.knokko.guns.plugin;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.v1_12_R1.EntitySnowball;
import net.minecraft.server.v1_12_R1.World;
import nl.knokko.guns.Gun;
import nl.knokko.guns.GunType;
import nl.knokko.guns.command.CommandTutorial;
import nl.knokko.guns.data.DataManager.PlayerData;
import nl.knokko.guns.data.DataManager.PlayerEntry;
import nl.knokko.guns.data.WaveMonster;
import nl.knokko.guns.map.MapWave;
import nl.knokko.guns.map.ShooterMap;
import nl.knokko.guns.menu.MenuFactory;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LargeFireball;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.google.common.collect.Lists;

public class ShooterEventHandler implements Listener {
	
	private final Map<UUID,Bullet> bulletMap = new HashMap<UUID,Bullet>();
	
	public static void giveWaveStaff(Player player, int mapNumber, int waveNumber){
		ItemStack staff = new ItemStack(Material.BLAZE_ROD);
		ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.BLAZE_ROD);
		meta.setDisplayName("Monster Placer");
		meta.setLore(Lists.newArrayList(waveLore(mapNumber, waveNumber)));
		staff.setItemMeta(meta);
		player.getInventory().addItem(staff);
	}
	
	private static String waveLore(int mapNumber, int waveNumber){
		return "Map " + mapNumber + " wave " + waveNumber;
	}
	
	@EventHandler
	public void clickWithItem(PlayerInteractEvent event){
		ItemStack item = event.getItem();
		if(item != null && event.getHand() == EquipmentSlot.HAND){
			Gun gun = Gun.fromItem(item);
			if(gun != null){
				event.setCancelled(true);
				if(event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK){
					ShooterPlugin.get().getDataManager().getPlayer(event.getPlayer().getUniqueId()).setShooting();
				}
				else {
					gun.toggleZoom();
					Player player = event.getPlayer();
					event.getPlayer().getInventory().setItemInMainHand(gun.createItem());
					if (gun.isZooming()) {
						player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30_000, 5));
					} else {
						player.removePotionEffect(PotionEffectType.SLOW);
					}
				}
			}
			else if(item.getType() == Material.BLAZE_ROD && event.getAction() == Action.RIGHT_CLICK_BLOCK){
				ItemMeta meta = item.getItemMeta();
				if(meta != null && meta.hasDisplayName() && meta.getDisplayName().equals("Monster Placer")){
					List<String> lore = meta.getLore();
					if(lore != null && lore.size() > 0){
						String l = lore.get(0);
						int space1 = l.indexOf(" ");
						int space2 = l.indexOf(" ", space1 + 1);
						int space3 = l.indexOf(" ", space2 + 1);
						try {
							int mapNumber = Integer.parseInt(l.substring(space1 + 1, space2));
							int waveNumber = Integer.parseInt(l.substring(space3 + 1));
							if(l.equals(waveLore(mapNumber, waveNumber))){
								MapWave wave = ShooterPlugin.get().getDataManager().getMap(mapNumber).getWave(waveNumber);
								WaveMonster monster = ShooterPlugin.get().getDataManager().getPlayer(event.getPlayer().getUniqueId()).getBoundMonster();
								if(monster != null){
									int x = event.getClickedBlock().getX() + event.getBlockFace().getModX();
									int y = event.getClickedBlock().getY() + event.getBlockFace().getModY();
									int z = event.getClickedBlock().getZ() + event.getBlockFace().getModZ();
									event.getPlayer().sendMessage("Added " + monster.getMonsterName() + " at (" + x + "," + y + "," + z + ")");
									wave.add(monster.getMonsterName(), x, y, z);
								}
								else {
									event.getPlayer().sendMessage("You can bind a monster to place with /gunsop monsters bind <name>");
								}
							}
							else {
								System.out.println("No monster places");
							}
						} catch(NumberFormatException ignored){}//someone has another blaze rod with lore
					}
				}
			}
			else if(item.getType() == Material.LAVA_BUCKET){
				ItemMeta meta = item.getItemMeta();
				if(meta != null && meta.hasDisplayName() && meta.getDisplayName().equals(SUICIDE_NAME)){
					List<String> lore = meta.getLore();
					if(lore.size() == 1 && lore.get(0).equals(SUICIDE_LORE)){
						event.setCancelled(true);
						event.getPlayer().setHealth(0);
					}
				}
			}
		}
	}
	
	private static final String SUICIDE_NAME = ChatColor.DARK_RED + "Commit Suicide";
	private static final String SUICIDE_LORE = ChatColor.RED + "Click with this item to commit suicide";
	
	@EventHandler
	public void onDie(final EntityDeathEvent event){
		String worldName = event.getEntity().getWorld().getName();
		if(worldName.startsWith("map")){
			try {
				int number = Integer.parseInt(worldName.substring(3));
				ShooterMap map = ShooterPlugin.get().getDataManager().getMap(number);
				if(map != null){
					event.setDroppedExp(0);
					event.getDrops().clear();
					if(event.getEntity() instanceof Player){
						map.onPlayerDie((Player) event.getEntity());
					}
					else {
						map.onMobDie();
						if(event.getEntity().getKiller() != null){
							PlayerData data = ShooterPlugin.get().getDataManager().getPlayer(event.getEntity().getKiller().getUniqueId());
							data.addCoins((int) (event.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / 20));
							data.addXP((int)(event.getEntity().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue() / 15));
						}
					}
				}
			} catch(NumberFormatException nfe){}
		}
	}
	
	@EventHandler
	public void onPlayerItemHoldEvent(PlayerItemHeldEvent event){ 
		if(event.getPlayer().hasPotionEffect(PotionEffectType.SLOW))
			event.getPlayer().removePotionEffect(PotionEffectType.SLOW);
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEntityHurt(EntityDamageEvent event){
		if(event.getEntityType() == EntityType.VILLAGER && MenuFactory.GUN_MERCHANT.equals(event.getEntity().getCustomName()))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractAtEntityEvent event){
		Entity entity = event.getRightClicked();
		String name = entity.getCustomName();
		if(entity.getType() == EntityType.VILLAGER && name != null){
			if(name.equals(MenuFactory.GUN_MERCHANT)){
				event.setCancelled(true);
				openMenu(event.getPlayer(), MenuFactory.createGunShop(ShooterPlugin.get().getDataManager().getPlayer(event.getPlayer().getUniqueId())));
			}
			else if(name.equals(MenuFactory.ITEM_MERCHANT)){
				event.setCancelled(true);
				openMenu(event.getPlayer(), MenuFactory.createItemShop(ShooterPlugin.get().getDataManager().getPlayer(event.getPlayer().getUniqueId())));
			}
			else if(name.contains("Tutorial")){
				event.setCancelled(true);
				CommandTutorial.start(event.getPlayer());
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event){
		ShooterPlugin.get().getDataManager().setOnline(event.getPlayer().getUniqueId());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerQuit(PlayerQuitEvent event){
		ShooterPlugin.get().getDataManager().setOffline(event.getPlayer().getUniqueId());
	}
	
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerSwitchWorld(PlayerChangedWorldEvent event){
		String worldName = event.getFrom().getName();
		if(worldName.startsWith("map")){
			try {
				int number = Integer.parseInt(worldName.substring(3));
				ShooterMap map = ShooterPlugin.get().getDataManager().getMap(number);
				if(map != null)
					map.onPlayerLeave(event.getPlayer().getUniqueId());
			} catch(NumberFormatException nfe){}
		}
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onDropItem(PlayerDropItemEvent event){
		Item item = event.getItemDrop();
		Gun gun = Gun.fromItem(item.getItemStack());
		if(gun != null){
			PlayerInventory inv = event.getPlayer().getInventory();
			if(inv.getItemInMainHand().getType() == Material.AIR){
				//event.setCancelled(true);
				item.remove();
				inv.setItemInMainHand(item.getItemStack());
				ShooterPlugin.get().getDataManager().getPlayer(event.getPlayer().getUniqueId()).setReloading(gun.getType().getReloadTime());
			}
		}
	}
	
	public void update(){
		Collection<PlayerEntry> players = ShooterPlugin.get().getDataManager().getOnlinePlayers();
		for(PlayerEntry entry : players){
			Gun gun1 = null;
			Player player = null;
			if(entry.getData().shouldReload()){
				player = Bukkit.getPlayer(entry.getID());
				gun1 = Gun.fromItem(player.getInventory().getItemInMainHand());
				if(gun1 != null){//the player might have equipped another item in the meantime
					gun1.setAmmo(gun1.getMaxAmmo());
					player.getInventory().setItemInMainHand(gun1.createItem());
				}
			}
			boolean mainShooting = entry.getData().isShooting() && entry.getData().canShootNow();
			if(mainShooting){
				if(player == null)
					player = Bukkit.getPlayer(entry.getID());
				if(gun1 == null)
					gun1 = Gun.fromItem(player.getInventory().getItemInMainHand());
				if(gun1 != null){
					int delay = updateShooting(gun1, entry.getID(), player, player.getInventory().getItemInMainHand());
					if(delay > 0)
						entry.getData().setShootDelay(delay);
				}
			}
		}
	}
	
	private int updateShooting(Gun gun, UUID id, Player player, ItemStack weapon){
		int count = gun.getType().getBulletsPerShot();
		if(gun.getCurrentAmmo() >= count){
			World world = ((CraftWorld)player.getWorld()).getHandle();
			Location loc = player.getLocation();
			float yaw = loc.getYaw();
			float pitch = loc.getPitch();
			if(gun.getType() == GunType.ROCKET_LAUNCHER){
				LargeFireball rocket = player.getWorld().spawn(player.getEyeLocation(), LargeFireball.class);
				rocket.setShooter(player);
				double ry = Math.toRadians(yaw);
				double rp = Math.toRadians(pitch);
				double s = gun.getType().getStartSpeed();
				rocket.setDirection(new Vector(-s * Math.sin(ry) * Math.cos(rp), -s * Math.sin(rp), s * Math.cos(ry) * Math.cos(rp)));
				//((CraftLargeFireball)rocket).getHandle();
				rocket.setYield(1);
				rocket.setIsIncendiary(false);
			}
			else {
				double a = gun.getType().getSpreadAngle();
				if(count == 1){
					fireBullet(world, id, player, loc, gun, yaw, pitch);
				}
				else if(count == 2){
					fireBullet(world, id, player, loc, gun, yaw + a, pitch);
					fireBullet(world, id, player, loc, gun, yaw - a, pitch);
				}
				else if(count == 3){
					fireBullet(world, id, player, loc, gun, yaw, pitch + a);
					fireBullet(world, id, player, loc, gun, yaw + a * 0.866025, pitch - a / 2);
					fireBullet(world, id, player, loc, gun, yaw - a * 0.866025, pitch - a / 2);
				}
				else if (count == 4) {
					fireBullet(world, id, player, loc, gun, yaw + a, pitch);
					fireBullet(world, id, player, loc, gun, yaw, pitch + a);
					fireBullet(world, id, player, loc, gun, yaw - a, pitch);
					fireBullet(world, id, player, loc, gun, yaw, pitch - a);
				}
				else if(count == 5){
					fireBullet(world, id, player, loc, gun, yaw, pitch);
					fireBullet(world, id, player, loc, gun, yaw + a, pitch);
					fireBullet(world, id, player, loc, gun, yaw, pitch + a);
					fireBullet(world, id, player, loc, gun, yaw - a, pitch);
					fireBullet(world, id, player, loc, gun, yaw, pitch - a);
				}
				else {
					throw new IllegalArgumentException("Unknown count: " + count);
				}
			}
			gun.setAmmo(gun.getCurrentAmmo() - count);
			//weapon.setItemMeta(gun.createItemMeta());
			player.getInventory().setItemInMainHand(gun.createItem());
		}
		return gun.getType().getDelay();
	}
	
	private void fireBullet(World world, UUID id, Player player, Location loc, Gun gun, double yaw, double pitch){
		EntitySnowball bullet = new EntitySnowball(world, loc.getX(), loc.getY() + player.getEyeHeight(), loc.getZ());
		bullet.shooter = ((CraftPlayer)player).getHandle();
		bullet.attachedToPlayer = true;
		double speed = gun.getType().getStartSpeed();
		bullet.motX = -speed * Math.sin(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
		bullet.motY = -speed * Math.sin(Math.toRadians(pitch));
		bullet.motZ = speed * Math.cos(Math.toRadians(yaw)) * Math.cos(Math.toRadians(pitch));
		bulletMap.put(bullet.getUniqueID(), new Bullet(id, gun.getDamage(), gun.getType().getHeadshotDamage(gun.getLevel()), gun.getType().getKnockback()));
		world.addEntity(bullet);
	}
	
	@EventHandler
	public void onProjectileHit(ProjectileHitEvent event){
		if(event.getEntityType() == EntityType.SNOWBALL){
			Bullet bullet = bulletMap.get(event.getEntity().getUniqueId());
			if(bullet != null){
				if(event.getHitEntity() instanceof LivingEntity){
					LivingEntity living = (LivingEntity) event.getHitEntity();
					Vector motion = event.getEntity().getVelocity();
					double total = Math.sqrt(motion.getX() * motion.getX() + motion.getY() * motion.getY() + motion.getZ() * motion.getZ());
					Player player = Bukkit.getPlayer(bullet.getShooter());
					double k = bullet.getKnockback();
					double damage = bullet.getDamage();
					//next code is for headshot damage
					Location locB = event.getEntity().getLocation();
					Location locH = living.getLocation();
					double distX = locH.getX() - locB.getX();
					double distZ = locH.getZ() - locB.getZ();
					double distHor = Math.sqrt(distX * distX + distZ * distZ);
					double part;
					if(total != 0)
						part = distHor / total;
					else
						part = 0;
					double bulletY = locB.getY() + part * motion.getY();
					if(Math.abs(bulletY - locH.getY() - living.getEyeHeight()) <= 0.3){
						Bukkit.getPlayer(bullet.getShooter()).sendMessage(ChatColor.YELLOW + "Headshot!");
						damage = bullet.getHeadshotDamage();
					}
					living.setNoDamageTicks(0);
					if(player != null)
						living.damage(damage, player);
					else
						living.damage(damage);
					living.setVelocity(living.getVelocity().add(new Vector(k * motion.getX() / total, k * motion.getY() / total, k * motion.getZ() / total)));
				}
				bulletMap.remove(bullet.shooter);
			}
		}
	}
	
	private static ItemStack createSuicideItem(){
		ItemStack stack = new ItemStack(Material.LAVA_BUCKET);
		ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.LAVA_BUCKET);
		meta.setDisplayName(SUICIDE_NAME);
		meta.setLore(Lists.newArrayList(SUICIDE_LORE));
		stack.setItemMeta(meta);
		return stack;
	}
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event){
		if(event.getWhoClicked() instanceof Player){
			Player player = (Player) event.getWhoClicked();
			Inventory inv = event.getInventory();
			String invName = inv.getName();
			if(invName.equals(MenuFactory.GUNS_TITLE)){
				event.setCancelled(true);
				if(event.getCurrentItem() != null){
					if(event.getCurrentItem().getType() == Material.BARRIER){
						closeMenu(player);
						return;
					}
					GunType type = GunType.fromString(event.getCurrentItem().getItemMeta().getDisplayName());
					PlayerData data = ShooterPlugin.get().getDataManager().getPlayer(player.getUniqueId());
					if(data.owns(type))
						return;
					if(data.getRank() < type.getRequiredRank()){
						player.sendMessage(ChatColor.RED + "You need rank " + type.getRequiredRank() + " to buy this gun.");
						return;
					}
					if(data.getCoins() < type.getPrice()){
						player.sendMessage(ChatColor.RED + "You need " + type.getPrice() + " coins to buy this gun, but you only have " + data.getCoins() + ".");
						return;
					}
					data.setOwned(type);
					data.removeCoins(type.getPrice());
					closeMenu(player);
					return;
				}
			}
			else if(invName.startsWith(MenuFactory.SELECT_EQUIPMENT)){
				event.setCancelled(true);
				if(event.getCurrentItem() != null){
					int mapNumber = Integer.parseInt(invName.substring(MenuFactory.SELECT_EQUIPMENT.length()));
					String item = event.getCurrentItem().getItemMeta().getDisplayName();
					if(item.equals(MenuFactory.EQUIPMENT_CANCEL))
						closeMenu(player);
					else if(item.equals(MenuFactory.EQUIPMENT_FIRST_GUN))
						openMenu(player, MenuFactory.createGunSelect(ShooterPlugin.get().getDataManager().getPlayer(player.getUniqueId()), 1, mapNumber, GunType.fromItemStack(inv.getItem(2)), GunType.fromItemStack(inv.getItem(3))));
					else if(item.equals(MenuFactory.EQUIPMENT_SECOND_GUN))
						openMenu(player, MenuFactory.createGunSelect(ShooterPlugin.get().getDataManager().getPlayer(player.getUniqueId()), 2, mapNumber, GunType.fromItemStack(inv.getItem(2)), GunType.fromItemStack(inv.getItem(3))));
					else if(item.equals(MenuFactory.EQUIPMENT_SPECIAL)){//TODO store the state of special items...
						player.sendMessage(ChatColor.RED + "Special items are not yet available.");
					}
					else if(item.equals(MenuFactory.EQUIPMENT_START)){
						ShooterMap map = ShooterPlugin.get().getDataManager().getMap(mapNumber);
						if(map == null){
							Bukkit.broadcastMessage("There is no map " + mapNumber);
							return;
						}
						if(map.addPlayer(player)){
							GunType gun1 = GunType.fromItemStack(inv.getItem(2));
							GunType gun2 = GunType.fromItemStack(inv.getItem(3));
							if(gun1 != gun2){
								player.getInventory().clear();
								player.getInventory().addItem(new Gun(gun1, 1).createItem());
								player.getInventory().addItem(new Gun(gun2, 1).createItem());
								player.getInventory().setItem(8, createSuicideItem());
								player.setGameMode(GameMode.ADVENTURE);
								player.setFoodLevel(20);
								player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
								closeMenu(player);
							}
							else {
								player.sendMessage(ChatColor.RED + "You must choose 2 different guns.");
							}
						}
					}
					else
						throw new IllegalStateException();
				}
			}
			else if(invName.startsWith(MenuFactory.SELECT_GUN)){
				event.setCancelled(true);
				if(event.getCurrentItem() != null){
					Material item = event.getCurrentItem().getType();
					int mapNumber = Integer.parseInt(invName.substring(invName.lastIndexOf(" ") + 1));
					if(item == Material.BARRIER)
						openMenu(player, MenuFactory.createMapEquipment(mapNumber, GunType.fromItemStack(inv.getItem(9)), GunType.fromItemStack(inv.getItem(18))));
					else if(event.getSlot() != 9 && event.getSlot() != 18){
						int gunIndex = Integer.parseInt(invName.substring(MenuFactory.SELECT_GUN.length(), invName.indexOf(" f")));
						if(gunIndex == 1)
							openMenu(player, MenuFactory.createMapEquipment(mapNumber, GunType.fromItemStack(event.getCurrentItem()), GunType.fromItemStack(inv.getItem(18))));
						else if(gunIndex == 2)
							openMenu(player, MenuFactory.createMapEquipment(mapNumber, GunType.fromItemStack(inv.getItem(9)), GunType.fromItemStack(event.getCurrentItem())));
						else
							throw new IllegalStateException("gunIndex is " + gunIndex);
					}
				}
			}
			else if(invName.equals(MenuFactory.GUN_SPAWN)){
				event.setCancelled(true);
				if(event.getCurrentItem() != null){
					Material item = event.getCurrentItem().getType();
					if(item != Material.BARRIER){
						player.getInventory().addItem(new Gun(GunType.fromItemStack(event.getCurrentItem()), 1).createItem());
					}
					closeMenu(player);
					return;
				}
			}
			else if(invName.equals(MenuFactory.ITEM_MERCHANT)){
				event.setCancelled(true);
				if(event.getCurrentItem() != null){
					Material item = event.getCurrentItem().getType();
					if(item == Material.BARRIER){
						closeMenu(player);
						return;
					}
					PlayerData data = ShooterPlugin.get().getDataManager().getPlayer(player.getUniqueId());
					if(item == Material.EGG){
						if(data.getCoins() >= MenuFactory.GRENADE_PRICE){
							data.removeCoins(MenuFactory.GRENADE_PRICE);
							data.addGrenade();
							openMenu(player, MenuFactory.createItemShop(data));
						}
					}
					else if(item == Material.POTION){
						if(data.getCoins() >= MenuFactory.MEDKIT_PRICE){
							data.removeCoins(MenuFactory.MEDKIT_PRICE);
							data.addMedkit();
							openMenu(player, MenuFactory.createItemShop(data));
						}
					}
				}
			}
		}
	}
	
	private void closeMenu(final Player player){
		Bukkit.getScheduler().scheduleSyncDelayedTask(ShooterPlugin.get(), new Runnable(){

			@Override
			public void run() {
				player.closeInventory();
			}
		});
	}
	
	private void openMenu(final Player player, final Inventory menu){
		Bukkit.getScheduler().scheduleSyncDelayedTask(ShooterPlugin.get(), new Runnable(){

			@Override
			public void run() {
				player.openInventory(menu);
			}
		});
	}
	
	private static class Bullet {
		
		private final double damage;
		private final double headshotDamage;
		private final double knockback;
		
		private final UUID shooter;
		
		private Bullet(UUID shooter, double damage, double headshotDamage, double knockback){
			this.shooter = shooter;
			this.damage = damage;
			this.headshotDamage = headshotDamage;
			this.knockback = knockback;
		}
		
		public double getDamage(){
			return damage;
		}
		
		public double getHeadshotDamage(){
			return headshotDamage;
		}
		
		public double getKnockback(){
			return knockback;
		}
		
		public UUID getShooter(){
			return shooter;
		}
	}
}