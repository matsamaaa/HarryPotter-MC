package mat_sama.harrypotter.commands;

import mat_sama.harrypotter.HarryPotter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class hpCreate implements CommandExecutor {

    private final HarryPotter main;
    public hpCreate(final HarryPotter main) {
        this.main = main;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) return false;

        //tp player au spawn
        if(main.getGameStatus() && main.isGameRunning()) sender.sendMessage(main.prefix + ChatColor.translateAlternateColorCodes('&', "Une partie est déjà en cours, attend la fin de celle-ci"));
        else if(main.getGameStatus()) sender.sendMessage(main.prefix + ChatColor.translateAlternateColorCodes('&', "Une partie est en attente de joueurs '/hpjoin' pour la rejoindre"));
        else {
            main.createGame((Player) sender);

            ((Player) sender).teleport(main.spawn);

            main.clearInventory((Player) sender); //clear la hotbar
            main.giveItemsStart((Player) sender); //give items

            main.addWaitingScoreboard();

            sender.sendMessage(main.prefix + "Vous venez de créer une partie");
            String joinMessage = main.prefix + ChatColor.translateAlternateColorCodes('&', ((Player) sender).getDisplayName() + " vient de créer une partie! &l&f[&r&l&6Rejoindre&l&f]");

            for(Player player : Bukkit.getOnlinePlayers()) {
                if(!player.getDisplayName().equals(main.getGameAuthor())) {
                    player.sendMessage(joinMessage);
                }
            }
        }

        return true;
    }

}
