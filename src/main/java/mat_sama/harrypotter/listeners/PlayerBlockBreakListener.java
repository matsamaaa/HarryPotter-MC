package mat_sama.harrypotter.listeners;

import mat_sama.harrypotter.HarryPotter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class PlayerBlockBreakListener implements Listener {

    private final HarryPotter main;

    public PlayerBlockBreakListener(final HarryPotter main) {
        this.main = main;
    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent e) {
        if(!main.isPlayerInGame(e.getPlayer().getDisplayName())) return;
        Player player = (Player) e.getPlayer();
        player.sendMessage(main.prefix + ChatColor.translateAlternateColorCodes('&', "&cVous ne pouvez pas casser de blocks en partie !"));
        e.setCancelled(true);
    }

}
