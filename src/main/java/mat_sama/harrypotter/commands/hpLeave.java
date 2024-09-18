package mat_sama.harrypotter.commands;

import mat_sama.harrypotter.HarryPotter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class hpLeave implements CommandExecutor {

    private final HarryPotter main;
    public hpLeave(final HarryPotter main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return false;

        Player player = (Player) sender;

        if(!main.getGameStatus()) sender.sendMessage(main.prefix + ChatColor.translateAlternateColorCodes('&', "&cAucune partie n'est en cours '/hpcreate' pour en créer une !"));
        else if(main.isPlayerInGame(((Player) sender).getDisplayName())) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (main.isPlayerInGame(p.getDisplayName())) {
                    p.sendMessage(main.prefix + player.getDisplayName() + " vient de quitter la partie");
                }
            }

            main.removeGameUser((Player) sender);
            main.clearInventory((Player) sender);
            main.removeWaitingScoreboard((Player) sender);
            ((Player) sender).teleport(main.lobby);
            sender.sendMessage(main.prefix + "Vous avez quitté la partie");
        }

        return true;
    }

}
