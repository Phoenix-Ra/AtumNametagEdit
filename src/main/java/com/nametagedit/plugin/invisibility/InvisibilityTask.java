package com.nametagedit.plugin.invisibility;

import com.nametagedit.plugin.AtumNametagEdit;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class InvisibilityTask extends BukkitRunnable {

    @Override
    public void run(){
        List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        if(players.isEmpty()){
            return;
        }

        players.forEach(player ->{
            if(player.hasPotionEffect(PotionEffectType.INVISIBILITY)){
                AtumNametagEdit.getApi().hideNametag(player);
            }else{
                AtumNametagEdit.getApi().showNametag(player);
            }
        });
    }

}