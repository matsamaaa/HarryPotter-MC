package mat_sama.harrypotter.listeners;

import mat_sama.harrypotter.HarryPotter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class PlayerBlockPlaceListener implements Listener {

    private final HarryPotter main;

    public PlayerBlockPlaceListener(final HarryPotter main) {
        this.main = main;
    }

    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent e) {
        if(!main.isPlayerInGame(e.getPlayer().getDisplayName())) return;
        Player player = (Player) e.getPlayer();
        player.sendMessage(main.prefix + ChatColor.translateAlternateColorCodes('&', "&cVous ne pouvez pas poser de blocks en partie !"));
        e.setCancelled(true);
    }

}
