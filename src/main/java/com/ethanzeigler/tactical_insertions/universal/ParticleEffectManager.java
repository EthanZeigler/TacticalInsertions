package com.ethanzeigler.tactical_insertions.universal;
import com.ethanzeigler.tactical_insertions.Insertion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.inventivetalent.particle.ParticleEffect;

import java.util.Collection;
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
            Collection<? extends Player> players = Bukkit.getOnlinePlayers();
            for (Insertion insertion: insertions.values()) {
                ParticleEffect.CLOUD.send(players, insertion.getLoc().clone().add(.5, 1.2, .5), 0, 0, 0, .075, 3, 50);
            }
        }, 0, 60);
    }
}
