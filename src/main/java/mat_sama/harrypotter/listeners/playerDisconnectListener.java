package mat_sama.harrypotter.listeners;

import mat_sama.harrypotter.HarryPotter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

public class playerDisconnectListener implements Listener {

    //laisser 1min a l'utilisateur pour se reconnecter

    private final HarryPotter main;

    public playerDisconnectListener(HarryPotter main) {
        this.main = main;
    }

    @EventHandler
    public void onDisconnect(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if(!main.getGameStatus()) return;
        if(!main.isGameRunning() && Objects.equals(main.getGameAuthor(), player.getDisplayName())) {
            main.removeAllScoreboard();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (main.isPlayerInGame(p.getDisplayName())) {
                    p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&cPartie Annulée"), "/hpcreate pour créer un partie", 10, 70, 20);
                    main.clearInventory(p);
                    p.teleport(main.lobby);
                }
            }
            main.resetGame();
        } else if(!main.isGameRunning()) {
            main.removeGameUser(player);
            main.clearInventory(player);
            player.teleport(main.lobby);
            main.removeWaitingScoreboard(player);
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (main.isPlayerInGame(p.getDisplayName())) {
                    p.sendMessage(main.prefix + player.getDisplayName() + " vient de quitter la partie");
                }
            }
        }

        //ajouter la deconnexion en game

    }
}
