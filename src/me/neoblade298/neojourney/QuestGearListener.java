package me.neoblade298.neojourney;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.SmithingInventory;

import de.tr7zw.nbtapi.NBTItem;
import me.neoblade298.neocore.bukkit.util.BukkitUtil;


public class QuestGearListener implements Listener {


	@EventHandler(priority = EventPriority.HIGHEST)
	public void onEnchantItem(EnchantItemEvent e) {
		ItemStack item = e.getItem();
		if (item != null) {
			if (isQuestGear(item)) {
				e.setCancelled(true);
				BukkitUtil.msg(e.getEnchanter(), "&cYou cannot enchant RPG items!");
				return;
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPrepareAnvilEvent(PrepareAnvilEvent e) {
		ItemStack[] contents = e.getInventory().getContents();
		for (int i = 0; i < contents.length; i++) {
			ItemStack item = contents[i];
			if (item != null) {
				if (isQuestGear(item)) {
					e.setResult(null);
					return;
				}
			}
		}
	}

	@EventHandler
	public void onChangeWorlds(PlayerTeleportEvent e) {
		Player p = e.getPlayer();
		String from = e.getFrom().getWorld().getName();
		String to = e.getTo().getWorld().getName();

		// Only consider changing worlds
		if (!from.equals(to)) {
			if (!to.equals("Argyll") && !to.equals("ClassPVP")) {
				PlayerInventory inv = p.getInventory();
				ItemStack[] armor = inv.getArmorContents();
				for (int i = 0; i <= 3; i++) {
					if (armor[i] != null && isQuestGear(armor[i])) {
						if (inv.firstEmpty() != -1) {
							inv.addItem(armor[i]);
							armor[i] = null;
							p.sendMessage(
									"§c[§4§lMLMC§4] §cYour quest gear was removed, as it cannot be used in this world!");
						}
						else {
							e.setCancelled(true);
							p.sendMessage(
									"§c[§4§lMLMC§4] §cYou must take off your quest armor before changing worlds!");
							break;
						}
					}
				}
				inv.setArmorContents(armor);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInteract(PlayerInteractEvent e) {
		Player p = e.getPlayer();
		ItemStack item = e.getItem();
		if (item == null || item.getType().isAir())
			return;
		String world = p.getWorld().getName();
		if (!world.equals("Argyll") && !world.equals("ClassPVP") && !world.equals("Dev")) {
			if (isQuestGear(item)) {
				e.setUseItemInHand(Result.DENY);
				p.sendMessage("§c[§4§lMLMC§4] §cYou cannot use quest gear in this world!");
			}
		}
		else {
			if (e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if (isArmor(item)) {
					e.setUseItemInHand(Result.DENY);
					p.sendMessage("§c[§4§lMLMC§4] §cEquipping armor via right click is disabled in quest worlds!");
				}
			}
		}
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		String world = e.getEntity().getWorld().getName();
		if (!world.equals("Argyll") && !world.equals("ClassPVP") && !world.equals("Dev")) {
			if (e.getDamager() instanceof Player) {
				Player p = (Player) e.getDamager();
				ItemStack[] weapons = { p.getInventory().getItemInMainHand(), p.getInventory().getItemInOffHand() };
				for (ItemStack item : weapons) {
					if (item != null && !item.getType().isAir() && isQuestGear(item)) {
						e.setCancelled(true);
						p.sendMessage("§c[§4§lMLMC§4] §cYou cannot use quest gear in this world!");
						break;
					}
				}
			}
		}
	}

	@EventHandler
	public void onShoot(EntityShootBowEvent e) {
		String world = e.getEntity().getWorld().getName();
		if (!world.equals("Argyll") && !world.equals("ClassPVP") && !world.equals("Dev")) {
			if (e.getEntity() instanceof Player) {
				Player p = (Player) e.getEntity();
				ItemStack item = e.getBow();
				if (isQuestGear(item)) {
					e.setCancelled(true);
					p.sendMessage("§c[§4§lMLMC§4] §cYou cannot use quest gear in this world!");
				}
			}
		}
	}

	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		disableEquipArmor(e);
	}
	
	private boolean disableEquipArmor(InventoryClickEvent e) {
		String world = e.getView().getPlayer().getWorld().getName();
		if (!world.equals("Argyll") && !world.equals("ClassPVP") && !world.equals("Dev")) {
			PlayerInventory inv = (PlayerInventory) e.getView().getBottomInventory();
			InventoryAction action = e.getAction();

			// Disable shift clicking armor
			if (action.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) && e.isShiftClick()
					&& e.getView().getType().equals(InventoryType.CRAFTING)) {
				ItemStack item = e.getCurrentItem();
				if (item.getType().toString().endsWith("HELMET")) {
					if (inv.getContents()[39] == null && isQuestGear(item)) {
						e.setCancelled(true);
						return true;
					}
				}
				else if (item.getType().toString().endsWith("CHESTPLATE")) {
					if (inv.getContents()[38] == null && isQuestGear(item)) {
						e.setCancelled(true);
						return true;
					}
				}
				else if (item.getType().toString().endsWith("LEGGINGS")) {
					if (inv.getContents()[37] == null && isQuestGear(item)) {
						e.setCancelled(true);
						return true;
					}
				}
				else if (item.getType().toString().endsWith("BOOTS")) {
					if (inv.getContents()[36] == null && isQuestGear(item)) {
						e.setCancelled(true);
						return true;
					}
				}
			}

			// Disable hotbar keying armor
			else if (action.equals(InventoryAction.HOTBAR_SWAP) && e.getSlot() >= 36 && e.getSlot() <= 39) {
				ItemStack item = inv.getContents()[e.getHotbarButton()];
				if (isQuestGear(item)) {
					e.setCancelled(true);
					return true;
				}
			}

			// Disable dropping armor
			else if (action.equals(InventoryAction.PLACE_ALL) || action.equals(InventoryAction.PLACE_ONE)
					|| action.equals(InventoryAction.SWAP_WITH_CURSOR)) {
				if (e.getSlotType().equals(SlotType.ARMOR) && isQuestGear(e.getCursor())) {
					e.setCancelled(true);
					return true;
				}
			}
		}

		// Disable netheriting quest gear
		if (e.getView().getTopInventory().getType().equals(InventoryType.SMITHING)) {
			SmithingInventory smith = (SmithingInventory) e.getView().getTopInventory();
			if (isQuestGear(smith.getContents()[0]) && e.getSlot() == 2) {
				e.setCancelled(true);
				e.getView().getPlayer().sendMessage("§c[§4§lMLMC§4] §cYou cannot apply netherite to quest gear!");
				return true;
			}
		}
		return false;
	}

	public boolean isQuestGear(ItemStack item) {
		if (item == null || !item.hasItemMeta()) {
			return false;
		}
		if (new NBTItem(item).hasKey("gear")) {
			return true;
		}
		return item.getItemMeta().hasLore()
				&& item.getItemMeta().getLore().get(0).contains("Tier") && !item.getType().equals(Material.PLAYER_HEAD);
	}

	public boolean isArmor(ItemStack item) {
		if (item == null)
			return false;
		String mat = item.getType().name();
		return mat.contains("HELMET") || mat.contains("CHESTPLATE") || mat.contains("LEGGINGS")
				|| mat.contains("BOOTS");
	}

	@EventHandler
	public void onInventoryDrag(InventoryDragEvent e) {
		String world = e.getView().getPlayer().getWorld().getName();
		if (!world.equals("Argyll") && !world.equals("ClassPVP") && !world.equals("Dev")) {
			if (e.getInventorySlots().size() == 1) {
				for (Integer i : e.getInventorySlots()) {
					if (i >= 36 && i <= 39) {
						if (isQuestGear(e.getOldCursor())) {
							e.setCancelled(true);
							return;
						}
					}
				}
			}
		}
	}

	@EventHandler
	public void onDispenseArmor(BlockDispenseArmorEvent e) {
		String world = e.getBlock().getWorld().getName();
		if (!world.equals("Argyll") && !world.equals("ClassPVP") && !world.equals("Dev")) {
			if (e.getTargetEntity() instanceof Player) {
				if (isQuestGear(e.getItem())) {
					e.setCancelled(true);
					e.getTargetEntity().sendMessage("§4[§c§lMLMC§4] §cYou cannot use quest gear in this world!");
				}
			}
		}
	}

	@EventHandler
	public void onTridentThrow(ProjectileLaunchEvent e) {
		Projectile proj = e.getEntity();
		if (proj.getShooter() instanceof Player) {
			Player p = (Player) proj.getShooter();
			String world = e.getEntity().getLocation().getWorld().getName();
			if (world.equals("Argyll") || world.equals("ClassPVP") || world.equals("Dev")) {
				if (e.getEntity().getType().equals(EntityType.TRIDENT)) {
					e.setCancelled(true);
					p.sendMessage("§4[§c§lMLMC§4] §cYou cannot throw tridents in this world!");
				}
			}
		}
	}
}
