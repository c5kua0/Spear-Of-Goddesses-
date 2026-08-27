package me.reno.spear;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class SpearPlugin extends JavaPlugin implements Listener {

    private static final String OWNER_NAME = "TUKOSHIBU";
    private static final String WEAPON_NAME = "§6§lSpear of Goddesses";

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        Objects.requireNonNull(getCommand("spear")).setExecutor((sender, command, label, args) -> {

            if (!(sender instanceof Player player))
                return true;

            if (!player.getName().equalsIgnoreCase(OWNER_NAME)) {
                player.sendMessage("§cThis weapon belongs to " + OWNER_NAME + ".");
                return true;
            }

            ItemStack sword = createWeapon();

            player.getInventory().addItem(sword);
            player.sendMessage("§6§lYou received the Spear of Goddesses!");

            return true;
        });
    }

    // CREATE WEAPON
    private ItemStack createWeapon() {
        ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = sword.getItemMeta();

        if (meta != null) {
            meta.setDisplayName(WEAPON_NAME);
            meta.setUnbreakable(true);
            sword.setItemMeta(meta);
        }

        return sword;
    }

    // CHECK IF IT IS THE CUSTOM WEAPON
    private boolean isWeapon(ItemStack item) {
        if (item == null || item.getType() != Material.DIAMOND_SWORD)
            return false;

        if (!item.hasItemMeta())
            return false;

        ItemMeta meta = item.getItemMeta();

        return meta.hasDisplayName()
                && WEAPON_NAME.equals(meta.getDisplayName());
    }

    // MELEE HIT
    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player player))
            return;

        ItemStack weapon = player.getInventory().getItemInMainHand();

        if (!isWeapon(weapon))
            return;

        if (!(event.getEntity() instanceof LivingEntity target))
            return;

        // POISON
        target.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.POISON,
                        80,
                        0
                )
        );

        // FIRE
        target.setFireTicks(60);

        // 5% LIGHTNING
        if (Math.random() < 0.05) {

            target.getWorld().strikeLightningEffect(
                    target.getLocation()
            );

            player.sendMessage(
                    "§e§l⚡ DIVINE LIGHTNING!"
            );
        }
    }

    // CANNOT DROP
    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {

        Player player = event.getPlayer();

        if (!player.getName().equalsIgnoreCase(OWNER_NAME))
            return;

        if (isWeapon(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);

            player.sendMessage(
                    "§c§lThe Spear of Goddesses cannot be dropped!"
            );
        }
    }

    // CANNOT MOVE INTO CHESTS / CONTAINERS
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        if (!(event.getWhoClicked() instanceof Player player))
            return;

        if (!isWeapon(event.getCurrentItem())
                && !isWeapon(event.getCursor()))
            return;

        // Prevent putting the weapon into another inventory
        if (event.getClickedInventory() != player.getInventory()) {
            event.setCancelled(true);
            return;
        }

        // Prevent shift-clicking it into a container
        if (event.isShiftClick()) {
            event.setCancelled(true);
        }
    }

    // CANNOT DRAG INTO CONTAINER
    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {

        if (!(event.getWhoClicked() instanceof Player player))
            return;

        if (!isWeapon(event.getOldCursor()))
            return;

        event.setCancelled(true);
    }

    // KEEP WEAPON AFTER DEATH
    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();

        if (!player.getName().equalsIgnoreCase(OWNER_NAME))
            return;

        Iterator<ItemStack> iterator =
                event.getDrops().iterator();

        while (iterator.hasNext()) {

            ItemStack item = iterator.next();

            if (isWeapon(item)) {
                iterator.remove();

                // Put it back after death
                Bukkit.getScheduler().runTask(
                        this,
                        () -> player.getInventory().addItem(createWeapon())
                );
            }
        }
    }
}