package mat_sama.harrypotter.listeners;

import mat_sama.harrypotter.HarryPotter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class PlayerAttackListener implements Listener {

    private final HarryPotter main;

    public PlayerAttackListener(final HarryPotter main) {
        this.main = main;
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent e) {
        if((e.getDamager() instanceof Player) && (e.getEntity() instanceof Player)) {
            if(!main.isPlayerInGame(((Player) e.getDamager()).getDisplayName())) return;
            if(!main.isGameRunning()) {
                Player player = (Player) e.getDamager();
                player.sendMessage(main.prefix + ChatColor.translateAlternateColorCodes('&', "&cVous ne pouvez pas encore taper les joueurs !"));
                e.setCancelled(true);
            }

        }
    }

}
