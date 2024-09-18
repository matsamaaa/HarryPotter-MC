package mat_sama.harrypotter.listeners;

import mat_sama.harrypotter.HarryPotter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;


public class playerDropItemListener implements Listener {

    private final HarryPotter main;

    public playerDropItemListener(HarryPotter main) {
        this.main = main;
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if(!(e.getPlayer() instanceof Player)) return;
        if(!main.isPlayerInGame(((Player) e.getPlayer()).getDisplayName())) return;

        e.setCancelled(true);
    }
}
