package mat_sama.harrypotter.listeners;

import mat_sama.harrypotter.HarryPotter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInteractListener implements Listener {

    final private HarryPotter main;

    public PlayerInteractListener(HarryPotter main) {
        this.main = main;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack itemInHand = player.getItemInHand();
        Action action = e.getAction();

        //if(!main.getGameStatus()) return;

        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        if (!itemInHand.hasItemMeta()) {
            return;
        }

        if (itemInHand.getType() == Material.REDSTONE_BLOCK && itemInHand.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&cAnnuler &8| &7 Clic-droit"))) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (main.isPlayerInGame(p.getDisplayName())) {
                    p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&cPartie Annulée"), "/hpcreate pour créer un partie", 10, 70, 20);
                    main.clearInventory(p);
                    main.removeWaitingScoreboard(p);
                    p.teleport(main.lobby);
                }
            }
            main.resetGame();
        } else if (itemInHand.getType() == Material.REDSTONE_BLOCK && itemInHand.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&cQuitter &8| &7 Clic-droit"))) {
            main.removeGameUser(player);
            player.sendTitle(ChatColor.translateAlternateColorCodes('&', "&cPartie Quitté"), "/hpcreate pour créer un partie", 10, 70, 20);
            main.clearInventory(player);
            player.teleport(main.lobby);
            main.removeWaitingScoreboard(player);
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (main.isPlayerInGame(p.getDisplayName())) {
                    p.sendMessage(main.prefix + player.getDisplayName() + " vient de quitter la partie");
                }
            }
        } else if (itemInHand.getType() == Material.EMERALD_BLOCK && itemInHand.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&aDémarrer &8| &7 Clic-droit"))) {
            if(main.getGameMembersSize() < 2) player.sendMessage(main.prefix + org.bukkit.ChatColor.translateAlternateColorCodes('&', "&cIl n'y a pas assez de joueurs pour démarrer !"));
            else main.startCountdown(player);
        } else if (itemInHand.getType() == Material.REDSTONE_BLOCK && itemInHand.getItemMeta().getDisplayName().equals(ChatColor.translateAlternateColorCodes('&', "&cStopper &8| &7 Clic-droit"))) {
            main.cancelCountdown(player);
        }



        //baguette
        else if(itemInHand.getType() == Material.STICK) {
            boolean testDeath = false;
            Player playerDeath = null;

            main.sendParticleEffect(player, Effect.ENDER_SIGNAL, 10, 0.5);

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (main.isPlayerInGame(p.getDisplayName())) {
                    if(!p.equals(player)) {

                        Location locationDuJoueur = player.getLocation();
                        Location locationRegard = locationDuJoueur.add(locationDuJoueur.getDirection());
                        Location locationAutreJoueur = p.getLocation();

                        if (locationRegard.distance(locationAutreJoueur) < 5) {
                            p.setGameMode(GameMode.SPECTATOR);
                            testDeath = true;

                            double x = p.getEyeLocation().getX();
                            double y = p.getEyeLocation().getY();
                            double z = p.getEyeLocation().getZ();

                            Location location = new Location(p.getWorld(), x, y, z);
                            playerDeath = p;
                            p.spawnParticle(Particle.DRAGON_BREATH, location, 20);
                            main.setGameDeath(p);
                        }
                    }

                    if(testDeath) {
                        p.sendMessage(main.prefix + ChatColor.translateAlternateColorCodes('&', "&c" + playerDeath.getDisplayName() + " vient d'être tué"));
                    }
                }
            }

            if(testDeath) {
                player.sendMessage(main.prefix + ChatColor.translateAlternateColorCodes('&', "&c" + "Vous venez de tuer " + playerDeath.getDisplayName()));
            }

        }
    }
}
