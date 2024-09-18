package mat_sama.harrypotter.commands;

import mat_sama.harrypotter.HarryPotter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class hpJoin implements CommandExecutor {

    private final HarryPotter main;
    public hpJoin(final HarryPotter main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        if(!main.getGameStatus()) sender.sendMessage(main.prefix + ChatColor.translateAlternateColorCodes('&', "&cAucune partie n'est en cours '/hpcreate' pour en créer une !"));
        else if(main.isGameRunning()) sender.sendMessage(main.prefix + ChatColor.translateAlternateColorCodes('&', "&cUne partie est déjà en cours, veuillez patienter !"));
        else if(main.isPlayerInGame(((Player) sender).getDisplayName())) sender.sendMessage(main.prefix + ChatColor.translateAlternateColorCodes('&', "&cVous êtes déjà en partie !"));
        else {
            main.addGameUser((Player) sender);
            ((Player) sender).teleport(main.spawn);
            main.clearInventory((Player) sender);
            main.giveItemsJoin((Player) sender);
            main.addWaitingScoreboard();

            for (Player p : Bukkit.getOnlinePlayers()) {
                if (main.isPlayerInGame(p.getDisplayName())) {
                    p.sendMessage(main.prefix + player.getDisplayName() + " vient de rejoindre la partie");
                }
            }
        }

        return true;
    }

}
