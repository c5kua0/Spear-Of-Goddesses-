package me.reno.spear;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Iterator;
import java.util.Objects;

public class SpearPlugin extends JavaPlugin implements Listener {

    private static final String OWNER_NAME = "TUKOSHIBU";
    private static final String WEAPON_NAME = "§6§lSpear of Goddesses";

    @Override
    public void onEnable() {

        getServer().getPluginManager().registerEvents(this, this);

        Objects.requireNonNull(getCommand("spear")).setExecutor(
                (sender, command, label, args) -> {

                    if (!(sender instanceof Player player)) {
                        sender.sendMessage("Only players can use this command.");
                        return true;
                    }

                    // OWNER CHECK
                    if (!player.getName().equalsIgnoreCase(OWNER_NAME)) {
                        player.sendMessage(
                                "§cThis weapon belongs to §e" + OWNER_NAME + "§c."
                        );
                        return true;
                    }

                    // GIVE WEAPON
                    ItemStack sword = createWeapon();

                    player.getInventory().addItem(sword);

                    player.sendMessage(
                            "§6§lYou received the Spear of Goddesses!"
                    );

                    return true;
                }
        );

        getLogger().info("Spear of Goddesses enabled!");
    }

    // =========================================================
    // CREATE WEAPON
    // =========================================================

    private ItemStack createWeapon() {

        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);

        ItemMeta meta = sword.getItemMeta();

        if (meta != null) {

            meta.setDisplayName(WEAPON_NAME);

            // UNBREAKABLE
            meta.setUnbreakable(true);

            sword.setItemMeta(meta);
        }

        return sword;
    }

    // =========================================================
    // CHECK CUSTOM WEAPON
    // =========================================================

    private boolean isWeapon(ItemStack item) {

        if (item == null)
            return false;

        if (item.getType() != Material.DIAMOND_SWORD)
            return false;

        if (!item.hasItemMeta())
            return false;

        ItemMeta meta = item.getItemMeta();

        if (meta == null)
            return false;

        if (!meta.hasDisplayName())
            return false;

        return WEAPON_NAME.equals(meta.getDisplayName());
    }

    // =========================================================
    // MELEE ATTACK
    // =========================================================

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {

        // Attacker must be player
        if (!(event.getDamager() instanceof Player player))
            return;

        // Must hold custom Diamond Sword
        ItemStack weapon =
                player.getInventory().getItemInMainHand();

        if (!isWeapon(weapon))
            return;

        // Target must be living entity
        if (!(event.getEntity() instanceof LivingEntity target))
            return;

        // =====================================================
        // POISON
        // =====================================================

        target.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.POISON,
                        80,
                        0
                )
        );

        // =====================================================
        // FIRE
        // =====================================================

        target.setFireTicks(60);

        // =====================================================
        // 5% DIVINE LIGHTNING
        // =====================================================

        if (Math.random() < 0.05) {

            target.getWorld().strikeLightningEffect(
                    target.getLocation()
            );

            player.sendMessage(
                    "§e§l⚡ DIVINE LIGHTNING!"
            );
        }
    }

    // =========================================================
    // CANNOT DROP
    // =========================================================

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {

        Player player = event.getPlayer();

        ItemStack item =
                event.getItemDrop().getItemStack();

        if (!isWeapon(item))
            return;

        event.setCancelled(true);

        player.sendMessage(
                "§c§lThe Spear of Goddesses cannot be dropped!"
        );
    }

    // =========================================================
    // CANNOT STORE / MOVE
    // =========================================================

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player))
            return;

        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        boolean currentIsWeapon = isWeapon(current);
        boolean cursorIsWeapon = isWeapon(cursor);

        if (!currentIsWeapon && !cursorIsWeapon)
            return;

        /*
         * Prevent moving the weapon to another inventory
         */
        if (event.getClickedInventory() != null
                && event.getClickedInventory() != player.getInventory()) {

            event.setCancelled(true);
            return;
        }

        /*
         * Prevent shift-click storage
         */
        if (event.isShiftClick()) {

            event.setCancelled(true);
            return;
        }

        /*
         * Prevent putting weapon on cursor into another inventory
         */
        if (cursorIsWeapon) {

            event.setCancelled(true);
        }
    }

    // =========================================================
    // CANNOT DRAG INTO CONTAINERS
    // =========================================================

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {

        if (!(event.getWhoClicked() instanceof Player))
            return;

        if (!isWeapon(event.getOldCursor()))
            return;

        event.setCancelled(true);
    }

    // =========================================================
    // KEEP WEAPON AFTER DEATH
    // =========================================================

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();

        if (!player.getName().equalsIgnoreCase(OWNER_NAME))
            return;

        Iterator<ItemStack> iterator =
                event.getDrops().iterator();

        boolean found = false;

        while (iterator.hasNext()) {

            ItemStack item = iterator.next();

            if (isWeapon(item)) {

                iterator.remove();
                found = true;
            }
        }

        /*
         * Give the weapon back after respawn.
         */
        if (found) {

            Bukkit.getScheduler().runTask(
                    this,
                    () -> player.getInventory().addItem(createWeapon())
            );
        }
    }
}