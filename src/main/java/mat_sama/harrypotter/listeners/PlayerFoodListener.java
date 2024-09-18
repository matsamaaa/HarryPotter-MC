package mat_sama.harrypotter.listeners;

import mat_sama.harrypotter.HarryPotter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class PlayerFoodListener implements Listener {

    private final HarryPotter main;

    public PlayerFoodListener(final HarryPotter main) {
        this.main = main;
    }

    @EventHandler
    public void onFood(FoodLevelChangeEvent e) {
        if(!(e.getEntity() instanceof Player)) return;
        if(!main.isPlayerInGame(((Player) e.getEntity()).getDisplayName())) return;

        e.setCancelled(true);
    }

}
