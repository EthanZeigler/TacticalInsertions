package com.ethanzeigler.tactical_insertions.universal;
import com.ethanzeigler.tactical_insertions.Insertion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.particle.ParticleEffect;

import java.util.Map;

/**
 * Created by Ethan on 7/9/16.
 */
public class ParticleEffectManager {
    private Map<Location, Insertion> insertions;
    private JavaPlugin plugin;

    public ParticleEffectManager(Map<Location, Insertion> insertions, JavaPlugin plugin) {
        this.insertions = insertions;
        this.plugin = plugin;

        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Insertion insertion: insertions.values()) {
                ParticleEffect.CLOUD.send(Bukkit.getOnlinePlayers(), insertion.getLoc().clone().add(.5, 1, .5), 0, 0, 0, 0, 1);
            }
        }, 0, 60);
    }
}
