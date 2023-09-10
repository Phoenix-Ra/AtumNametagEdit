package com.nametagedit.plugin.hooks;

import com.nametagedit.plugin.AtumNametagEdit;
import com.nametagedit.plugin.NametagHandler;
import com.nametagedit.plugin.packets.PacketWrapper;
import lombok.Getter;
import me.zeevss.gangs.Gang;
import me.zeevss.gangs.dataobject.GangUser;
import me.zeevss.gangs.event.GangAllianceBreakEvent;
import me.zeevss.gangs.event.GangAllianceFormedEvent;
import me.zeevss.gangs.event.PlayerGangChangeEvent;
import me.zeevss.gangs.utils.GangUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AtumGangsHook implements Listener {
    @Getter
    private static HashMap<Player, HashMap<String,String>> savedColors = new HashMap<>();

    @Getter
    private static HashMap<Player, List<String>> cacheForRemoval = new HashMap<>();

    private final String color_member = "§a";
    private final String color_ally = "§b";
    private final String color_neutral = "";

    private final NametagHandler handler;


    public AtumGangsHook(final NametagHandler handler) {
        this.handler = handler;
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        GangUser gangUser = Gang.getPlugin().getCacheManager().getGangUser(player.getUniqueId());
        if(gangUser == null) return;

        applyNewColors(player, gangUser);

    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event){
        savedColors.remove(event.getPlayer());
        cacheForRemoval.remove(event.getPlayer());
    }

    @EventHandler
    public void onPlayerGangChangeEvent(PlayerGangChangeEvent event) {
        Player player = event.getPlayer();
        GangUser gangUser = event.getGangUser();
        if(gangUser == null){
            String teamName = "gang_" + player.getName();
            //clear up
            for(Player source : Bukkit.getOnlinePlayers()){
                clearCacheTargetForSources(source, player);
                handler.setPlayerNameColorForSources(source, color_neutral, teamName, player);
            }
            for(Player target : Bukkit.getOnlinePlayers()){
                clearCacheSourceForTargets(target, player);
                handler.setPlayerNameColorForTargets(target, color_neutral, teamName, player);
            }
            return;
        }

        applyNewColors(player, gangUser);



    }
    @EventHandler
    public void onGangAllianceFormed(GangAllianceFormedEvent event){
        Player[] gangMembers1 = GangUtils.getOnlineGangMembers(event.getGang1()).toArray(new Player[0]);
        Player[] gangMembers2 = GangUtils.getOnlineGangMembers(event.getGang2()).toArray(new Player[0]);

        for(Player player : gangMembers1){
            String teamName = "gang_" + player.getName();
            cacheTargetForSources(player, color_ally, gangMembers2);
            handler.setPlayerNameColorForSources(player, color_ally, teamName, gangMembers2);
        }
        for(Player player : gangMembers2){
            String teamName = "gang_" + player.getName();
            cacheTargetForSources(player, color_ally, gangMembers1);
            handler.setPlayerNameColorForSources(player, color_ally, teamName, gangMembers1);
        }
    }
    @EventHandler
    public void onGangAllianceBreak(GangAllianceBreakEvent event){
        Player[] gangMembers1 = GangUtils.getOnlineGangMembers(event.getGang1()).toArray(new Player[0]);
        Player[] gangMembers2 = GangUtils.getOnlineGangMembers(event.getGang2()).toArray(new Player[0]);

        for(Player player : gangMembers1){
            String teamName = "gang_" + player.getName();
            clearCacheSourceForTargets(player, gangMembers2);
            handler.setPlayerNameColorForSources(player, color_neutral, teamName, gangMembers2);
        }
        for(Player player : gangMembers2){
            String teamName = "gang_" + player.getName();
            clearCacheSourceForTargets(player, gangMembers1);
            handler.setPlayerNameColorForSources(player, color_neutral, teamName, gangMembers1);
        }
    }

    private void applyNewColors(Player player, GangUser gangUser){
        List<Player> members = GangUtils.getOnlineGangMembers(gangUser);
        List<Player> allies = GangUtils.getOnlineGangAllyMembers(gangUser);

        String teamName = "gang_" + player.getName();

        Player[] array = members.toArray(new Player[0]);
        //color for player
        cacheSourceForTargets(player, color_member, array);
        handler.setPlayerNameColorForTargets(player, color_member, teamName, array);

        array = allies.toArray(new Player[0]);
        cacheSourceForTargets(player, color_ally, array);
        handler.setPlayerNameColorForTargets(player, color_ally, teamName, array);
        //neutrals
        array = Bukkit.getOnlinePlayers().stream().filter(
                p -> !members.contains(p) && !allies.contains(p) && !p.getUniqueId().equals(player.getUniqueId())
        ).toArray(Player[]::new);
        clearCacheSourceForTargets(player, array);
        handler.setPlayerNameColorForTargets(player, color_neutral, teamName, array);

        //color for others
        array = members.toArray(new Player[0]);
        cacheTargetForSources(player, color_member, array);
        handler.setPlayerNameColorForSources(player, color_member, teamName, array);

        array = allies.toArray(new Player[0]);
        cacheTargetForSources(player, color_ally, array);
        handler.setPlayerNameColorForSources(player, color_ally, teamName, array);

        //neutrals
        array = Bukkit.getOnlinePlayers().stream().filter(
                p -> !members.contains(p) && !allies.contains(p) && !p.getUniqueId().equals(player.getUniqueId())
        ).toArray(Player[]::new);
        clearCacheTargetForSources(player, array);
        handler.setPlayerNameColorForSources(player, color_neutral,teamName,array);
    }


    private void cacheTargetForSources(Player target, String color, Player ... sources){
        for(Player source : sources){
            HashMap<String,String> list = savedColors.get(source);
            if(list == null) list = new HashMap<>();
            list.put(target.getName(), color);
            savedColors.put(source, list);

            List<String> remList = cacheForRemoval.get(source);
            if(remList == null) remList = new ArrayList<>();
            remList.add("gang_" + target.getName());
            cacheForRemoval.put(source, remList);
        }
    }
    private void cacheSourceForTargets(Player source, String color, Player ... targets){
        HashMap<String,String> list = savedColors.get(source);
        if(list == null) list = new HashMap<>();
        List<String> remList = cacheForRemoval.get(source);
        if(remList == null) remList = new ArrayList<>();
        for(Player target : targets){
            list.put(target.getName(), color);
            remList.add("gang_" + target.getName());
        }
        savedColors.put(source, list);
        cacheForRemoval.put(source, remList);
    }
    private void clearCacheTargetForSources(Player target,Player ... sources){

        for(Player source : sources){
            HashMap<String,String> list = savedColors.get(source);
            if(list == null) continue;
            list.remove(target.getName());
            savedColors.put(source, list);

            List<String> remList = cacheForRemoval.get(source);
            if(remList == null) continue;
            remList.remove("gang_" + target.getName());
            cacheForRemoval.put(source, remList);
        }
    }
    private void clearCacheSourceForTargets(Player source,Player ... targets){
        HashMap<String,String> list = savedColors.get(source);
        if(list == null) return;
        for(Player target : targets){
            list.remove(target.getName());
        }
        savedColors.put(source, list);

        List<String> remList = cacheForRemoval.get(source);
        if(remList == null) return;
        for(Player target : targets){
            remList.remove("gang_" + target.getName());
        }
        cacheForRemoval.put(source, remList);
    }


    public static void sendPacketsPlayer(Player player){
        HashMap<String,String> list = savedColors.get(player);
        if(list == null) return;
        List<String> lRem = cacheForRemoval.get(player);
        if(lRem == null) lRem = new ArrayList<>();
        for(Map.Entry<String,String> entry : list.entrySet()){
            Player target = Bukkit.getPlayerExact(entry.getKey());
            if(target == null || !target.isOnline()) continue;
            AtumNametagEdit.getInstance().getHandler().setPlayerNameColorForTargets
                    (player, entry.getValue(), "gang_"+entry.getKey(), target);
            lRem.add("gang_" + target.getName());
        }
        cacheForRemoval.put(player, lRem);
    }
    public static void sendPacketsForAll(){
        for(Player player : savedColors.keySet()){
            if(!player.isOnline()) continue;
            sendPacketsPlayer(player);
        }
    }

    public static void clearPacketsPlayer(Player player){
        HashMap<String,String> list = savedColors.get(player);
        if(list == null) return;
        List<String> removal = cacheForRemoval.get(player);
        if(removal == null) return;
        for(String teamName : removal){
            new PacketWrapper(teamName,"","", 1, new ArrayList<>(),true).send(player);
        }
        cacheForRemoval.remove(player);

    }
    public static void clearPacketsForAll(){
        for(Player player : savedColors.keySet()){
            clearPacketsPlayer(player);
        }
    }

}
