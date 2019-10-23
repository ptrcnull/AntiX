package ml.bjorn.antix;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public final class AntiX extends JavaPlugin implements Listener {

    // @TODO: allow to customize in the config
    private Set<Material> materials = new HashSet<>(Arrays.asList(
        Material.IRON_ORE,
        Material.GOLD_ORE,
        Material.DIAMOND_ORE,
//        Material.LAPIS_ORE,
//        Material.REDSTONE_ORE,
//        Material.COAL_ORE,
        Material.EMERALD_ORE
//        Material.NETHER_QUARTZ_ORE
    ));

    private Map<String, Map<Material, AtomicInteger>> cache = new HashMap<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().scheduleSyncRepeatingTask(this, this::dispose, 0L, 600L);
        getCommand("antix").setExecutor((sender, command, label, args) -> {
            if (!sender.hasPermission("antix.notify")) {
                return true;
            }
            dispose();
            return true;
        });
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void dispose() {
        for (String user : cache.keySet()) {
            Map<Material, AtomicInteger> materials = cache.get(user);
            String ans = materials.entrySet()
                .stream()
                .map(entry -> String.format("&d%s&6x &c%s", entry.getValue().get(), entry.getKey().toString()))
                .collect(Collectors.joining(", "));

            getServer().broadcast(
                ChatColor.translateAlternateColorCodes(
                    '&',
                    // @TODO: replace hardcoded Polish strings with config-defined ones
                    String.format("&6[AntiX] &c%s &6wykopał %s!", user, ans)
                ),
                "antix.notify"
            );
        }
        cache = new HashMap<>();
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Material mat = event.getBlock().getType();
        String playerName = event.getPlayer().getName();
        if (materials.contains(mat)) {
            Map<Material, AtomicInteger> materials;
            if (!cache.containsKey(playerName)) {
                materials = new HashMap<>();
                cache.put(playerName, materials);
            } else {
                materials = cache.get(playerName);
            }

            AtomicInteger counter;
            if (!materials.containsKey(mat)) {
                counter = new AtomicInteger();
                materials.put(mat, counter);
            } else {
                counter = materials.get(mat);
            }

            counter.incrementAndGet();
        }
    }
}
