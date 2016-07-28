package com.ethanzeigler.tactical_insertions;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ethan on 6/29/16.
 */
public class TacStackFactory {
    private static ItemStack seed;
    private static Material seedMaterial = Material.REDSTONE_TORCH_ON;

    public static ItemStack getTacStack() {
        if (seed == null) {
            seed = new ItemStack(seedMaterial);
            // todo make stack material changeable
            ItemMeta meta = seed.getItemMeta();
            meta.setDisplayName(String.format("%s%sTactical Insertion", ChatColor.GOLD, ChatColor.BOLD));
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GREEN + "Place to respawn at the tactical insertion's location!");
            lore.add(ChatColor.GREEN + "When you respawn the tactical insertion will break");
            meta.setLore(lore);
            seed.setItemMeta(meta);

            return seed.clone();
        } else {
            return seed.clone();
        }
    }

    public static boolean isTacStack(ItemStack stack) {
        if (stack != null) {
            ItemStack changedStack = getTacStack();
            changedStack.setAmount(stack.getAmount());

            return stack.equals(changedStack);
        } else {
            return false;
        }
    }

    public static boolean containsTacStack(Inventory inv) {
        for (ItemStack stack : inv.getStorageContents()) {
            if (isTacStack(stack)) {
                return true;
            }
        }
        return false;
    }

    public static void setSeedMaterial(Material seedMaterial) {
        TacStackFactory.seedMaterial = seedMaterial;
        seed = null;
    }

    public static Material getSeedMaterial() {
        return seedMaterial;
    }
}
