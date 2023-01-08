package me.neoblade298.neojourney;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import com.songoda.epichoppers.EpicHoppers;
import com.songoda.epichoppers.api.events.HopperPlaceEvent;
import com.songoda.epichoppers.hopper.Hopper;
import com.songoda.epichoppers.hopper.HopperBuilder;

import de.tr7zw.nbtapi.NBTItem;

public class EpicHoppersListener implements Listener {

	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e) {
		ItemStack item = e.getItemInHand();
		NBTItem nbti = new NBTItem(item);
		
		// Temporary epichoppers fix
		if (item.getType() == Material.HOPPER && nbti.hasKey("level")) {
			Player p = e.getPlayer();
			EpicHoppers inst = EpicHoppers.getInstance();
		    Hopper hopper = inst.getHopperManager().addHopper((new HopperBuilder(e
		          .getBlock()))
		        .setLevel(inst.getLevelManager().getLevel(item))
		        .setPlacedBy(p)
		        .setLastPlayerOpened(p).build());
		    HopperPlaceEvent hopperPlaceEvent = new HopperPlaceEvent(p, hopper);
		    Bukkit.getPluginManager().callEvent((Event)hopperPlaceEvent);
		    EpicHoppers.getInstance().getDataManager().createHopper(hopper);
		}
	}
}
