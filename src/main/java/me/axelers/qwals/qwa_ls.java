package me.axelers.qwals;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class qwa_ls extends JavaPlugin implements Listener {

    private final List<Player> queue = new ArrayList<>();
    private final Random random = new Random();
    private final Map<Player, Integer> credits = new HashMap<>();
    private final Map<Player, String> roles = new HashMap<>(); // Track player roles

    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic, if needed
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();



        ItemStack startEmerald = createStartEmerald();
        // Set the Start Emerald in the first hotbar slot
        player.getInventory().setItem(0, startEmerald);

        // Create the sidebar for the player
        updateSidebar(player);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        Material materialInHand = itemInHand.getType();

        // Check if the player is interacting with an emerald
        if (event.getHand() == EquipmentSlot.HAND && materialInHand == Material.EMERALD) {
            if (!queue.contains(player)) {
                queue.add(player);
                player.sendMessage("You have been added to the queue!");

                // Replace the emerald with a scute named "Cancel"
                player.getInventory().setItemInMainHand(createCancelScute());

                // If there are two players in the queue, start the game
                if (queue.size() == 2) {
                    startGame(queue.get(0), queue.get(1));
                    queue.clear(); // Clear the queue after starting the game
                }
            } else {
                player.sendMessage("You are already in the queue!");
            }
        }
        // Check if the player is interacting with a scute
        else if (event.getHand() == EquipmentSlot.HAND && materialInHand == Material.SCUTE) {
            ItemMeta meta = itemInHand.getItemMeta();
            if (meta != null && "§cCancel".equals(meta.getDisplayName())) {
                if (queue.contains(player)) {
                    queue.remove(player);
                    player.sendMessage("You have been removed from the queue!");

                    // Remove the scute from the player's inventory
                    player.getInventory().setItemInMainHand(createStartEmerald());
                }
            }
        }
    }

    private void startGame(Player player1, Player player2) {
        // Define teleport locations
        Location breakersLocation = new Location(player1.getWorld(), 81, -50, 33);
        Location placersLocation = new Location(player2.getWorld(), 57, -50, 143);

        // Randomly assign roles
        if (random.nextBoolean()) {
            assignRole(player1, "breaker", breakersLocation);
            assignRole(player2, "placer", placersLocation);
        } else {
            assignRole(player1, "placer", placersLocation);
            assignRole(player2, "breaker", breakersLocation);
        }

        // Equip both players with the necessary items
        equipPlayer(player1, roles.get(player1));
        equipPlayer(player2, roles.get(player2));

        credits.put(player1, 1000); // Give 1000 credits to the player
        credits.put(player2, 1000); // Give 1000 credits to the player
        // Update the sidebar for both players
        updateSidebar(player1);
        updateSidebar(player2);

    }




    private void assignRole(Player player, String role, Location location) {
        player.teleport(location);
        player.sendMessage("You are a " + role + "!");
        roles.put(player, role); // Store the player's role
    }

    private void equipPlayer(Player player, String role) {
        player.getInventory().setItem(0, createStoneSword());
        player.getInventory().setItem(1, createBow());
        player.getInventory().setItem(2, createArrows());

        // Equip the player with armor
        if (role.equals("breaker")) {
            equipArmor(player, Color.LIME, Enchantment.PROTECTION_ENVIRONMENTAL);
            player.getInventory().setItem(3, createStonePickaxe());
        } else if (role.equals("placer")) {
            equipArmor(player, Color.RED, Enchantment.PROTECTION_ENVIRONMENTAL);
        }

        // Add a diamond named "Shop" with custom model data 1 to the last slot (index 8)
        player.getInventory().setItem(8, createShopDiamond());
    }

    private void equipArmor(Player player, Color color, Enchantment enchantment) {
        player.getInventory().setHelmet(createColoredLeatherArmor(Material.LEATHER_HELMET, color, enchantment));
        player.getInventory().setChestplate(createColoredLeatherArmor(Material.LEATHER_CHESTPLATE, color, enchantment));
        player.getInventory().setLeggings(createColoredLeatherArmor(Material.LEATHER_LEGGINGS, color, enchantment));
        player.getInventory().setBoots(createColoredLeatherArmor(Material.LEATHER_BOOTS, color, enchantment));
    }

    private ItemStack createColoredLeatherArmor(Material material, Color color, Enchantment enchantment) {
        ItemStack armorPiece = new ItemStack(material);
        LeatherArmorMeta meta = (LeatherArmorMeta) armorPiece.getItemMeta();
        if (meta != null) {
            meta.setColor(color);
            meta.addEnchant(enchantment, 1, true);  // Add Protection I enchantment
            armorPiece.setItemMeta(meta);
        }
        return armorPiece;
    }

    private ItemStack createCancelScute() {
        ItemStack scute = new ItemStack(Material.SCUTE);
        ItemMeta meta = scute.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§cCancel");  // §c is the color code for red
            meta.setCustomModelData(1);
            scute.setItemMeta(meta);
        }
        return scute;
    }

    private ItemStack createStartEmerald() {
        ItemStack emerald = new ItemStack(Material.EMERALD);
        ItemMeta meta = emerald.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§6Start");  // §6 is the color code for gold
            meta.setCustomModelData(1);
            emerald.setItemMeta(meta);
        }
        return emerald;
    }

    private ItemStack createStoneSword() {
        return new ItemStack(Material.STONE_SWORD);
    }

    private ItemStack createBow() {
        return new ItemStack(Material.BOW);
    }

    private ItemStack createArrows() {
        return new ItemStack(Material.ARROW, 6); // Give 6 arrows
    }

    private ItemStack createStonePickaxe() {
        return new ItemStack(Material.STONE_PICKAXE);
    }

    private ItemStack createShopDiamond() {
        ItemStack diamond = new ItemStack(Material.DIAMOND);
        ItemMeta meta = diamond.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§fShop");  // §f is the color code for white
            meta.setCustomModelData(1); // Set custom model data to 1
            diamond.setItemMeta(meta);
        }
        return diamond;
    }

    // Methods to manage credits
    public int getCredits(Player player) {
        return credits.getOrDefault(player, 0);
    }

    public void setCredits(Player player, int amount) {
        credits.put(player, amount);
    }

    public void addCredits(Player player, int amount) {
        credits.put(player, getCredits(player) + amount);
    }

    public void removeCredits(Player player, int amount) {
        credits.put(player, getCredits(player) - amount);
    }

    // Method to create and update the sidebar
    private void updateSidebar(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getNewScoreboard();

        Objective objective = scoreboard.registerNewObjective("sidebar", "dummy", ChatColor.BOLD +  "" + ChatColor.GREEN + "LIGHTSTRIKE");
        objective.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);

        Score blankLine1 = objective.getScore("");
        blankLine1.setScore(3);

        String role = roles.get(player);
        if (role == null) {
            role = "§fSpectator";
        }

        // Role display
        ChatColor roleColor = role.equals("breaker") ? ChatColor.DARK_GREEN : ChatColor.RED;
        Score roleScore = objective.getScore("Team: " + roleColor + role.substring(0, 1).toUpperCase() + role.substring(1));
        roleScore.setScore(2);

        // Credits display
        Score creditsScore = objective.getScore(ChatColor.AQUA + "Credits: " + getCredits(player));
        creditsScore.setScore(1);

        // Blank lines
        Score blankLine2 = objective.getScore("");
        blankLine2.setScore(0);

        Score blankLine3 = objective.getScore(" ");
        blankLine3.setScore(-1);

        // Instructions
        Score instructions = objective.getScore(ChatColor.GREEN + "Place on the sites");
        instructions.setScore(-2);

        player.setScoreboard(scoreboard);
    }
}









