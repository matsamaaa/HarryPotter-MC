package mat_sama.harrypotter.listeners;

import mat_sama.harrypotter.HarryPotter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerTakeDamageListener implements Listener {

    private final HarryPotter main;

    public PlayerTakeDamageListener(final HarryPotter main) {
        this.main = main;
    }

    @EventHandler
    public void onTakeDamage(EntityDamageEvent e) {
        if(!(e.getEntity() instanceof Player)) return;

        Player player = (Player) e.getEntity();
        if(main.isPlayerInGame(player.getDisplayName()) && e.getCause() == EntityDamageEvent.DamageCause.FALL) {
            e.setCancelled(true);
        }
    }

}
