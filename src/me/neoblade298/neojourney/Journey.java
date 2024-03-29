package me.neoblade298.neojourney;

import java.io.File;
import java.util.HashSet;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.NeoCore;
import me.neoblade298.neocore.bukkit.blockdata.CustomBlockData;
import me.neoblade298.neocore.bukkit.util.Util;
import me.neoblade298.neocore.shared.exceptions.NeoIOException;

import net.Indyuce.mmoitems.MMOItems;
import net.Indyuce.mmoitems.gui.CraftingStationView;

public class Journey extends JavaPlugin implements org.bukkit.event.Listener {
	private HashSet<String> craftingStations = new HashSet<String>();
	private static final double DURABILITY_LIMIT_PCT = 0.1;
	private NamespacedKey stationKey = new NamespacedKey(this, "crafting-station");

	public void onEnable() {
		Bukkit.getServer().getLogger().info("NeoJourney Enabled");
		getServer().getPluginManager().registerEvents(this, this);
		getServer().getPluginManager().registerEvents(new QuestGearListener(), this);
		if (Bukkit.getPluginManager().isPluginEnabled("EpicHoppers"))
		// this.getCommand("njourney").setExecutor(new Commands(this));
		CustomBlockData.registerListener(this);
		
		try {
			NeoCore.loadFiles(new File(getDataFolder(), "config.yml"), (cfg, file) -> {
				for (String station : cfg.getStringList("crafting-stations")) {
					craftingStations.add(station);
				}
			});
		} catch (NeoIOException e) {
			e.printStackTrace();
		}
	}

	public void onDisable() {
		org.bukkit.Bukkit.getServer().getLogger().info("NeoJourney Disabled");
		super.onDisable();
	}

	@EventHandler
	public void onMend(PlayerItemMendEvent e) {
		e.setCancelled(true);
	}

	@EventHandler
	public void onDurability(PlayerItemDamageEvent e) {
		if (e.getItem().hasItemMeta() && e.getItem().getItemMeta().hasCustomModelData()) {
			e.setDamage((e.getDamage() + 1) / 2);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onRevive(EntityResurrectEvent e) {
		if (e.getEntity() instanceof Player) {
			String w = e.getEntity().getWorld().getName();
			if (w.equalsIgnoreCase("Argyll") || w.equalsIgnoreCase("ClassPVP")) {
				e.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		new BukkitRunnable() {
			public void run() {
				Player p = e.getEntity();
				if (p.isDead()) {
					e.getEntity().spigot().respawn();
				}
			}
		}.runTaskLater(this, 20L);
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		ItemStack item = e.getItemInHand();
		NBTItem nbti = new NBTItem(item);
		String id = nbti.getString("MMOITEMS_ITEM_ID");
		if (craftingStations.contains(id)) {
			Block b = e.getBlockPlaced();
			PersistentDataContainer data = new CustomBlockData(b, this);
			data.set(stationKey, PersistentDataType.STRING, id);
			Bukkit.getLogger().info("[NeoJourney] Created station " + id + " at " + Util.locToString(b.getLocation(), true, false));
		}
	}
	
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockInteract(final PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getHand() != EquipmentSlot.HAND) return;
        
        final Block block = e.getClickedBlock();
        if (block == null) return;

    	Player p = e.getPlayer();
        final PersistentDataContainer data = new CustomBlockData(block, this);
        if (!data.has(stationKey, PersistentDataType.STRING)) return;
        
        String id = data.get(stationKey, PersistentDataType.STRING);
        id = id.toLowerCase().replaceAll("_", "-");
        CraftingStationView view = new CraftingStationView(p, MMOItems.plugin.getCrafting().getStation(id), 1);
        view.open();
        e.setCancelled(true);
    }

	@EventHandler(ignoreCancelled = true)
	public void onDurabilityLoss(PlayerItemDamageEvent e) {
		ItemStack item = e.getItem();
		ItemMeta meta = item.getItemMeta();
		if (meta instanceof Damageable) {
			Player p = e.getPlayer();
			Damageable dm = (Damageable) meta;
			
			double max = item.getType().getMaxDurability();
			double before = max - dm.getDamage();
			double after = before - e.getDamage();
			double beforePct = before / max;
			double afterPct = after / max;
			if (beforePct > DURABILITY_LIMIT_PCT && afterPct <= DURABILITY_LIMIT_PCT && p.hasPermission("donator.warndurability")) {
				String display = meta.hasDisplayName() ? meta.getDisplayName() : item.getType().name();
				Util.msg(p, "&4WARNING: Your item, " + display + "&4, is below " + ((int) (DURABILITY_LIMIT_PCT * 100)) + "% durability!");
			}
		}
	}
}
