package com.nametagedit.plugin.hooks;

import com.nametagedit.plugin.NametagHandler;
import me.zeevss.gangs.Gang;
import me.zeevss.gangs.dataobject.GangUser;
import me.zeevss.gangs.event.PlayerGangChangeEvent;
import me.zeevss.gangs.utils.GangUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.HashMap;
import java.util.List;


public class AtumGangsHook implements Listener {
    private final NametagHandler handler;

    private HashMap<String,String> savedTeams = new HashMap<>();

    public AtumGangsHook(final NametagHandler handler) {
        this.handler = handler;
    }

    @EventHandler
    public void onPlayerGangChangeEvent(PlayerGangChangeEvent event) {
        Player player = event.getPlayer();
        GangUser gangUser = event.getGangUser();

        List<Player> members = GangUtils.getOnlineGangMembers(gangUser);
        List<Player> allies = GangUtils.getOnlineGangAllyMembers(gangUser);

        String teamName = "gang_" + player.getName();
        savedTeams.put(player.getName(), teamName);
        //color for player
        handler.setPlayerNameColorForTargets(player, "&a", teamName, members.toArray(new Player[0]));
        handler.setPlayerNameColorForTargets(player, "&b", teamName, allies.toArray(new Player[0]));
        //neutrals
        handler.setPlayerNameColorForTargets(player, "", teamName, Bukkit.getOnlinePlayers().stream().filter(
                p -> !members.contains(p) && !allies.contains(p) && !p.getUniqueId().equals(player.getUniqueId())
        ).toArray(Player[]::new));

        //color for others
        handler.setPlayerNameColorForSources(player, "&a", teamName, members.toArray(new Player[0]));
        handler.setPlayerNameColorForSources(player, "&b", teamName, allies.toArray(new Player[0]));
        //neutrals
        handler.setPlayerNameColorForSources(player, "",teamName, Bukkit.getOnlinePlayers().stream().filter(
                p -> !members.contains(p) && !allies.contains(p) && !p.getUniqueId().equals(player.getUniqueId())
        ).toArray(Player[]::new));



    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        GangUser gangUser = Gang.getPlugin().getCacheManager().getGangUser(player.getUniqueId());
        if(gangUser == null) return;


        List<Player> members = GangUtils.getOnlineGangMembers(gangUser);
        List<Player> allies = GangUtils.getOnlineGangAllyMembers(gangUser);

        String teamName = "gang_" + player.getName();
        savedTeams.put(player.getName(), teamName);
        //color for player
        handler.setPlayerNameColorForTargets(player, "&a", teamName, members.toArray(new Player[0]));
        handler.setPlayerNameColorForTargets(player, "&b", teamName, allies.toArray(new Player[0]));
        //neutrals
        handler.setPlayerNameColorForTargets(player, "", teamName, Bukkit.getOnlinePlayers().stream().filter(
                p -> !members.contains(p) && !allies.contains(p) && !p.getUniqueId().equals(player.getUniqueId())
        ).toArray(Player[]::new));

        //color for others
        handler.setPlayerNameColorForSources(player, "&a", teamName, members.toArray(new Player[0]));
        handler.setPlayerNameColorForSources(player, "&b", teamName, allies.toArray(new Player[0]));
        //neutrals
        handler.setPlayerNameColorForSources(player, "",teamName, Bukkit.getOnlinePlayers().stream().filter(
                p -> !members.contains(p) && !allies.contains(p) && !p.getUniqueId().equals(player.getUniqueId())
        ).toArray(Player[]::new));

    }


}
