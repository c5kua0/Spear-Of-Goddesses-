package me.reno.spear;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public class SpearPlugin extends JavaPlugin implements Listener {

    private final Map<UUID, Long> cooldown = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);

        Objects.requireNonNull(
                getCommand("spear")
        ).setExecutor((sender, command, label, args) -> {

            if (!(sender instanceof Player player)) return true;

            if (!player.getName().equalsIgnoreCase("TUKOSHIBO")) {
                player.sendMessage("§cThis spear belongs to TUKOSHIBO.");
                return true;
            }

            ItemStack spear = new ItemStack(Material.SPEAR);
            ItemMeta meta = spear.getItemMeta();

            meta.setDisplayName("§6§lSpear of Goddesses");
            spear.setItemMeta(meta);

            player.getInventory().addItem(spear);
            player.sendMessage("§6You received the Spear of Goddesses!");

            return true;
        });
    }

    // LUNGE
    @EventHandler
    public void rightClick(PlayerInteractEvent event) {

        Player player = event.getPlayer();

        if (player.getInventory().getItemInMainHand().getType()
                != Material.SPEAR) return;

        long now = System.currentTimeMillis();

        if (cooldown.getOrDefault(player.getUniqueId(), 0L) > now)
            return;

        cooldown.put(player.getUniqueId(), now + 5000);

        Vector velocity =
                player.getLocation().getDirection().normalize();

        velocity.multiply(2.2);
        velocity.setY(0.35);

        player.setVelocity(velocity);

        player.getWorld().spawnParticle(
                Particle.CLOUD,
                player.getLocation(),
                15
        );
    }

    // SPEAR HIT EFFECTS
    @EventHandler
    public void hit(EntityDamageByEntityEvent event) {

        if (!(event.getDamager() instanceof Player player))
            return;

        if (player.getInventory().getItemInMainHand().getType()
                != Material.SPEAR) return;

        if (!(event.getEntity() instanceof LivingEntity target))
            return;

        // Poison
        target.addPotionEffect(
                new PotionEffect(
                        PotionEffectType.POISON,
                        80,
                        0
                )
        );

        // Flame
        target.setFireTicks(60);

        // 5% Lightning
        if (Math.random() < 0.05) {
            target.getWorld().strikeLightningEffect(
                    target.getLocation()
            );

            player.sendMessage(
                    "§e§l⚡ DIVINE LIGHTNING!"
            );
        }
    }
  }
