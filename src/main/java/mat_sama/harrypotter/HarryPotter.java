package mat_sama.harrypotter;

import mat_sama.harrypotter.commands.hpCreate;
import mat_sama.harrypotter.commands.hpJoin;

import mat_sama.harrypotter.commands.hpLeave;
import mat_sama.harrypotter.listeners.*;
import org.bukkit.*;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.chat.TextComponent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public final class HarryPotter extends JavaPlugin {

    public Configuration config;
    public String prefix;
    public Location spawn;
    public Location lobby;
    public Location game;
    private boolean gameStatus = false;
    private boolean gameIsRunning = false;
    private String gameAuthor;
    private String voldemort;
    private HashMap<String, Boolean> gameMembers = new HashMap<>(); //Pseudo - isAlive()
    private CountdownTask countdownTask;
    private BeginGame BeginGame;

    @Override
    public void onEnable() {
        // Plugin startup logic

        this.saveDefaultConfig();
        this.getConfig().options().copyDefaults(true);

        CountdownTask countdownTask;

        config = getConfig();
        prefix = ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(config.getString("prefix")));

        double spawnX = config.getDouble("spawn.x");
        double spawnY = config.getDouble("spawn.y");
        double spawnZ = config.getDouble("spawn.z");
        float spawnYaw = (float) config.getDouble("spawn.pitch");
        float spawnPitch = (float) config.getDouble("spawn.yaw");

        spawn = new Location(Bukkit.getWorld(config.getString("world")), spawnX, spawnY, spawnZ, spawnYaw, spawnPitch);

        double lobbyX = config.getDouble("lobby.x");
        double lobbyY = config.getDouble("lobby.y");
        double lobbyZ = config.getDouble("lobby.z");
        float lobbyYaw = (float) config.getDouble("lobby.pitch");
        float lobbyPitch = (float) config.getDouble("lobby.yaw");

        lobby = new Location(Bukkit.getWorld(config.getString("world")), lobbyX, lobbyY, lobbyZ, lobbyYaw, lobbyPitch);

        double gameX = config.getDouble("game.x");
        double gameY = config.getDouble("game.y");
        double gameZ = config.getDouble("game.z");
        float gameYaw = (float) config.getDouble("game.pitch");
        float gamePitch = (float) config.getDouble("game.yaw");

        game = new Location(Bukkit.getWorld(config.getString("world")), gameX, gameY, gameZ, gameYaw, gamePitch);

        getLogger().info("Coordonnées de spawn : " + spawn.toString());
        System.out.println("HarryPotter Plugin Launched !");

        //enregistrement des events
        getServer().getPluginManager().registerEvents(new PlayerAttackListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerBlockPlaceListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerBlockBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerTakeDamageListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(this), this);
        getServer().getPluginManager().registerEvents(new playerDisconnectListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerFoodListener(this), this);
        getServer().getPluginManager().registerEvents(new playerDropItemListener(this), this);

        //enregistrement des commandes
        Objects.requireNonNull(getCommand("hpcreate")).setExecutor(new hpCreate(this));
        Objects.requireNonNull(getCommand("hpjoin")).setExecutor(new hpJoin(this));
        Objects.requireNonNull(getCommand("hpleave")).setExecutor(new hpLeave(this));

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        System.out.println("HarryPotter Plugin Stopped");
    }

    /*
     * ================================
     * DEFINED DATA
     * ================================
    * */

    public void createGame(Player player) {
        gameStatus = true;
        gameAuthor = player.getDisplayName();
        gameMembers.put(gameAuthor, true);
        setGamemode(player);
    }

    public void addGameUser(Player player) {
        String playerName = player.getDisplayName();
        gameMembers.put(playerName, true);
        setGamemode(player);
    }

    public void removeGameUser(Player player) {
        String playerName = player.getDisplayName();
        gameMembers.remove(playerName);
    }

    public void setGameDeath(Player player) {
        String playerName = player.getDisplayName();
        gameMembers.put(playerName, false);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
    }

    public void resetGame() {
        gameMembers.clear();
        gameIsRunning = false;
        gameStatus = false;
        gameAuthor = null;
        voldemort = null;
    }

    /*
    * ================================
    * GET DATA
    * ================================
    * */

    public boolean isGameRunning() {
        return gameIsRunning;
    }

    public String getGameAuthor() {
        return gameAuthor;
    }

    public boolean isPlayerInGame(String playerName) {
        return gameMembers.getOrDefault(playerName, false);
    }

    public boolean getGameStatus() {
        return gameStatus;
    }

    public void startGame() {
        gameIsRunning = true;
    }

    public int getGameMembersSize() {
        return gameMembers.size();
    }

    public int getGameMembersSizeAlive() {
        int count = 0;

        for (boolean alive : gameMembers.values()) {
            if (alive) {
                count++;
            }
        }

        return count - 1;
    }

    private String getVoldemort() {
        if (gameMembers.isEmpty()) {
            return null;
        }

        Object[] keys = gameMembers.keySet().toArray();

        Random random = new Random();
        Object randomKey = keys[random.nextInt(keys.length)];

        return randomKey.toString();
    }

    /*
     * ================================
     * IN-GAME ACTIONS
     * ================================
    * */

    public void setGamemode(Player player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20.0);
        player.setFoodLevel(20);
    }

    public Scoreboard waitingScoreboard(Player player, int... time) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        Objective objective = scoreboard.registerNewObjective("title", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&8    - &6&lPoudlard &r&8-    "));
        Score topEmptyLineScore = objective.getScore(ChatColor.RESET.toString());
        topEmptyLineScore.setScore(10);

        Score waitingScore = objective.getScore(ChatColor.translateAlternateColorCodes('&', "&8  &e&lSalle d'attente&r&8  "));
        waitingScore.setScore(9);

        Score playersScore = objective.getScore(ChatColor.translateAlternateColorCodes('&', "&f• Joueurs: &b" + getGameMembersSize() + "&f/&e16"));
        playersScore.setScore(7);

        Score hostScore = objective.getScore(ChatColor.translateAlternateColorCodes('&', "&f• Host: &b" + gameAuthor));
        hostScore.setScore(6);

        Score EmptyLineScore = objective.getScore(" ");
        EmptyLineScore.setScore(5);

        String timeMessage = (time.length > 0) ? String.valueOf(time[0]) : "En Attente";
        Score timeScore = objective.getScore(ChatColor.translateAlternateColorCodes('&', "&f• Démarrage: &c" + timeMessage));
        timeScore.setScore(4);

        return scoreboard;
    }

    public Scoreboard gameScoreboard(int time, Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

        Objective objective = scoreboard.registerNewObjective("title", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        objective.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&8    - &6&lPoudlard &r&8-    "));
        Score topEmptyLineScore = objective.getScore(ChatColor.RESET.toString());
        topEmptyLineScore.setScore(10);

        Score waitingScore = objective.getScore(ChatColor.translateAlternateColorCodes('&', "&8  &e&lEn Partie&r&8  "));
        waitingScore.setScore(9);

        Score playersScore = objective.getScore(ChatColor.translateAlternateColorCodes('&', "&f• Joueurs en vie: &b" + (getGameMembersSizeAlive())));
        playersScore.setScore(7);

        String rank = (player.getDisplayName().equals(voldemort)) ? "&cVoldemort" : "&aÉtudiant";
        Score roleScore = objective.getScore(ChatColor.translateAlternateColorCodes('&', "&f• Rôle: " + rank));
        roleScore.setScore(6);

        Score hostScore = objective.getScore(ChatColor.translateAlternateColorCodes('&', "&f• Temps Restant: &c" + time));
        hostScore.setScore(6);

        return scoreboard;
    }

    /*public void addWaitingScoreboard(Player player) {
        player.setScoreboard(waitingScoreboard(player));
    }*/

    public void removeWaitingScoreboard(Player player) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isPlayerInGame(p.getDisplayName())) {
                p.setScoreboard(waitingScoreboard(p));
            }
        }
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }

    public void removeAllScoreboard() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isPlayerInGame(p.getDisplayName())) {
                p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
        }
    }

    public void addWaitingScoreboard(int... time) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isPlayerInGame(p.getDisplayName())) {
                p.setScoreboard(waitingScoreboard(p, time));
            }
        }
    }

    public void clearInventory(Player player) {
        PlayerInventory inventory = player.getInventory();

        // Clear the main inventory slots
        inventory.clear();

        // Clear the hotbar slots
        inventory.setHeldItemSlot(0);
        inventory.setItemInOffHand(null);
    }

    public ItemStack startItem = new ItemStack(Material.EMERALD_BLOCK);

    public void giveItemsStart(Player player) {
        ItemMeta startItemMeta = startItem.getItemMeta();

        List<String> startItemLore = new ArrayList<>();
        startItemLore.add(ChatColor.translateAlternateColorCodes('&', "&f[&6?&f] &7lancer la partie"));
        startItemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&aDémarrer &8| &7 Clic-droit"));

        startItemMeta.setLore(startItemLore);
        startItem.setItemMeta(startItemMeta);

        ItemStack disbandItem = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta disbandItemMeta = disbandItem.getItemMeta(); // Correction ici

        List<String> disbandItemLore = new ArrayList<>();
        disbandItemLore.add(ChatColor.translateAlternateColorCodes('&', "&f[&6?&f] &7annuler la partie"));
        disbandItemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&cAnnuler &8| &7 Clic-droit"));

        disbandItemMeta.setLore(disbandItemLore);
        disbandItem.setItemMeta(disbandItemMeta);

        player.getInventory().setItem(0, startItem);
        player.getInventory().setItem(8, disbandItem);
    }

    public void giveItemsJoin(Player player) {
        ItemStack leaveItem = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta leaveItemMeta = leaveItem.getItemMeta(); // Correction ici

        List<String> leaveItemLore = new ArrayList<>();
        leaveItemLore.add(ChatColor.translateAlternateColorCodes('&', "&f[&6?&f] &7quitter la partie"));
        leaveItemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&cQuitter &8| &7 Clic-droit"));

        leaveItemMeta.setLore(leaveItemLore);
        leaveItem.setItemMeta(leaveItemMeta);

        player.getInventory().setItem(8, leaveItem);
    }

    public void giveItemBaguette(Player player, boolean isVoldemort) {
        if(isVoldemort) {
            ItemStack baguetteItem = new ItemStack(Material.STICK);
            ItemMeta baguetteMeta = baguetteItem.getItemMeta(); // Correction ici

            List<String> baguetteLore = new ArrayList<>();
            baguetteLore.add(ChatColor.translateAlternateColorCodes('&', "&f[&6?&f] &7tuer les joueurs"));
            baguetteMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&c&lBaguette &8| &7 Clic-droit"));

            baguetteMeta.setLore(baguetteLore);
            baguetteItem.setItemMeta(baguetteMeta);

            player.getInventory().setItem(0, baguetteItem);
        } else {
            ItemStack baguetteItem = new ItemStack(Material.FEATHER);
            ItemMeta baguetteMeta = baguetteItem.getItemMeta(); // Correction ici

            List<String> baguetteLore = new ArrayList<>();
            baguetteLore.add(ChatColor.translateAlternateColorCodes('&', "&f[&6?&f] &7pose un bouclier"));
            baguetteMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&9&lBouclier &8| &7 Clic-droit"));

            baguetteMeta.setLore(baguetteLore);
            baguetteItem.setItemMeta(baguetteMeta);

            player.getInventory().setItem(0, baguetteItem);
        }
    }
    public void sendParticleEffect(Player player, Effect particle, int count, double offset) {
        Location playerLocation = player.getEyeLocation();
        Vector direction = playerLocation.getDirection().normalize();

        for (int i = 0; i < count; i++) {
            double x = playerLocation.getX() + offset * direction.getX() * i;
            double y = playerLocation.getY() + offset * direction.getY() * i;
            double z = playerLocation.getZ() + offset * direction.getZ() * i;

            Location particleLocation = new Location(playerLocation.getWorld(), x, y, z);

            // Utilisez la méthode display() avec la durée de vie spécifiée (en ticks)
            //player.spawnParticle(particle, x, y, z, size);
            player.playEffect(particleLocation, particle, 1);
        }
    }

    public static void hidePlayerName(Player joueur) {
        try {
            Object entityPlayer = getNMSPlayer(joueur);

            if (entityPlayer == null)
                return;

            // Utilisation de la réflexion pour accéder aux champs
            Field field = entityPlayer.getClass().getDeclaredField("listName");
            field.setAccessible(true);

            // Définir le listName sur un composant de texte vide
            field.set(entityPlayer, new TextComponent(""));

            // Actualiser l'apparition du joueur pour les autres
            updatePlayerAppearance(joueur);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Méthode pour obtenir l'objet EntityPlayer du joueur
    private static Object getNMSPlayer(Player joueur) {
        try {
            Method getHandle = joueur.getClass().getMethod("getHandle");
            return getHandle.invoke(joueur);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Méthode pour actualiser l'apparence du joueur pour les autres
    private static void updatePlayerAppearance(Player joueur) {
        for (Player otherPlayer : Bukkit.getOnlinePlayers()) {
            if (!otherPlayer.equals(joueur)) {
                otherPlayer.hidePlayer(joueur);
                otherPlayer.showPlayer(joueur);
            }
        }
    }

    // Variable de contrôle pour le début du jeu

    /*
     * ================================
     * BukkitRunnable DECOMPTE GAME
     * ================================
    * */

    public void startCountdown(Player player) {
        this.countdownTask = new CountdownTask();
        countdownTask.runTaskTimer(this, 0, 20);

        ItemStack Item = new ItemStack(Material.REDSTONE_BLOCK);
        ItemMeta ItemMeta = Item.getItemMeta(); // Correction ici

        List<String> ItemLore = new ArrayList<>();
        ItemLore.add(ChatColor.translateAlternateColorCodes('&', "&f[&6?&f] &7stopper le démarrage"));
        ItemMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&cStopper &8| &7 Clic-droit"));

        ItemMeta.setLore(ItemLore);
        Item.setItemMeta(ItemMeta);

        player.getInventory().setItem(0, Item);// Run every second (20 ticks)
    }

    public void cancelCountdown(Player player) {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
            if(player != null) {
                System.out.println("test");
                player.getInventory().setItem(0, startItem);
                addWaitingScoreboard();

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (isPlayerInGame(p.getDisplayName())) {
                        p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&cDémarrage Annulé"), "/hpleave pour quitter la partie !", 10, 70, 20);
                    }
                }
            }
        }
    }

    public void titleCount(Player player, int i) {
        player.sendTitle(ChatColor.translateAlternateColorCodes('&', "&c" + i), "Préparez vous !", 10, 70, 20);
        addWaitingScoreboard(i);
    }

    private class CountdownTask extends BukkitRunnable {

        private int countdown = 5;

        @Override
        public void run() {
            // Announce the countdown to all online players
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (isPlayerInGame(p.getDisplayName())) {
                    titleCount(p, countdown);
                }
            }

            // Decrement the countdown
            countdown--;

            // Check if the countdown has reached 0
            if (countdown < 0) {
                startingGame(); // Teleport all players to the game location
                cancelCountdown(null); // Cancel the countdown task
            }
        }
    }

    private void cancelGame() {
        if (BeginGame != null) {
            BeginGame.cancel();
            BeginGame = null;
        }
    }

    private class BeginGame extends  BukkitRunnable {

        private int startTime = 900;
        private int time = startTime; //900

        @Override
        public void run(){

            //update scoreboard
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (isPlayerInGame(p.getDisplayName())) {
                    hidePlayerName(p);
                    p.setScoreboard(gameScoreboard(time, p));
                }
            }

            time--;

            if(time == startTime - 1) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (isPlayerInGame(p.getDisplayName())) {
                        if(!(p.getDisplayName().equals(voldemort))) {
                            System.out.println("gentil ajouté");
                        } else {
                            System.out.println("mechant ajouté");
                        }
                    }
                }
            }

            if(time == startTime - 15 ) {
                Player player = Bukkit.getPlayerExact(voldemort);
                player.teleport(game);

                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (isPlayerInGame(p.getDisplayName())) {
                        if(!(p.getDisplayName().equals(voldemort))) {
                            PotionEffect effetBlindness = new PotionEffect(PotionEffectType.BLINDNESS, time * 20, 1);
                            p.addPotionEffect(effetBlindness);
                            p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&cTéléportation de Voldemort"), "Courez vous !", 10, 70, 20);
                        } else {
                            p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&cVous avez été Téléporté"), "Chassez les étudiants !", 10, 70, 20);
                        }
                    }
                }
            }

            if(getGameMembersSizeAlive() < 1) {
                cancelGame();
                finishGame();
            }

            if (time < 0) {
                cancelGame();
                finishGame();
            }
        }

    }

    private void startingGame() {
        //choix du voldemort aleatoire
        voldemort = getVoldemort();
        //reset scoreboard
        //clear inventaires
        //tp tout le monde sauf le voldemort

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isPlayerInGame(p.getDisplayName())) {
                removeWaitingScoreboard(p);
                clearInventory(p);
                if(p.getDisplayName().equals(voldemort)) {
                    p.setPlayerListName(ChatColor.translateAlternateColorCodes('&', "&c&lVoldemort &r&f" + p.getDisplayName()));
                    p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&cVous êtes Voldemort"), "Attendez 15 sec !", 10, 70, 20);

                    //give items
                    giveItemBaguette(p, true);
                } else {
                    p.setPlayerListName(ChatColor.translateAlternateColorCodes('&', "&a&lÉtudiant   &r&f" + p.getDisplayName()));
                    p.sendTitle(ChatColor.translateAlternateColorCodes('&', "&aVous êtes Étudiant"), "Courez vous cacher !", 10, 70, 20);
                    p.teleport(game);

                    //give items
                    giveItemBaguette(p, false);
                }
            }
        }

        //defined la game en running
        startGame();

        this.BeginGame = new BeginGame();
        BeginGame.runTaskTimer(this, 0, 20);
    }

    public void finishGame() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (isPlayerInGame(p.getDisplayName())) {
                boolean isVoldemortWinner = getGameMembersSizeAlive() < 1;

                if(p.getDisplayName().equals(voldemort)) {
                    String res = isVoldemortWinner ? "&aVous Avez Gagné" : "&cVous Avez Perdu";
                    p.sendTitle(ChatColor.translateAlternateColorCodes('&', res), "Bravo à tous !", 10, 70, 20);
                } else {
                    String res = isVoldemortWinner ? "&cVous Avez Perdu" : "&aVous Avez Gagné";
                    p.sendTitle(ChatColor.translateAlternateColorCodes('&', res), "Bravo à tous !", 10, 70, 20);
                    p.teleport(game);
                }
                p.teleport(lobby);
                p.setPlayerListName(p.getDisplayName());
                //visibilityUsername(p, true);
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', prefix + (isVoldemortWinner ? ("Le gagnant est &cVoldemort &d(&f" + voldemort + "&d) !") : "Les gagnants sont &ales Étudiants &d!")));
                p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                p.removePotionEffect(PotionEffectType.BLINDNESS);
                setGamemode(p);
            }
        }

        resetGame();
    }

}









