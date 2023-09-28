package com.nametagedit.plugin;

import com.nametagedit.plugin.api.data.FakeTeam;
import com.nametagedit.plugin.api.data.FakeTeamList;
import com.nametagedit.plugin.api.data.Nametag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NametagManager {

    //change to a map of player(packet receiver) to fake team
    private final Map<Player, FakeTeamList> TEAMS = new ConcurrentHashMap<>();

    private final Map<String, Nametag> GLOBAL_NAMETAGS = new ConcurrentHashMap<>();


    private final AtumNametagEdit plugin;

    public NametagManager(AtumNametagEdit plugin){
        this.plugin = plugin;
    }


    public void setNametag(String player, String prefix, String suffix, Player ... receivers) {
        setNametag(player, prefix, suffix, -1, receivers);
    }

    public void setNametag(String player, String prefix, String suffix, boolean visible, Player ... receivers) {
        setNametag(player, prefix, suffix, -1, false, visible, receivers);
    }


    void setNametag(String player, String prefix, String suffix, int sortPriority, Player ... receivers) {
        setNametag(player, prefix, suffix, sortPriority, false, true,receivers);
    }
    void setNametag(String player, String prefix, String suffix, int sortPriority, boolean playerTag, boolean visible, Player ... receivers) {
        GLOBAL_NAMETAGS.put(player, new Nametag(prefix, suffix, visible));
        if(receivers.length == 0) {
            receivers = Bukkit.getOnlinePlayers().toArray(new Player[0]);
        }
        for(Player receiver : receivers) {
            FakeTeamList teamList = TEAMS.get(receiver);
            if(teamList == null) {
                teamList = new FakeTeamList(receiver);
                TEAMS.put(receiver, teamList);
            }
            teamList.setNametag(player, prefix, suffix, sortPriority, playerTag, visible);
        }
    }
    public void applyGlobalToPlayer(Player player) {
       for(Map.Entry<String,Nametag> nametag : GLOBAL_NAMETAGS.entrySet()) {
           setNametag(
                   nametag.getKey(),
                   nametag.getValue().getPrefix(),
                   nametag.getValue().getSuffix(),
                   nametag.getValue().isVisible(),
                   player
           );
       }
    }
    public Nametag getGlobalNametag(String player) {
        return GLOBAL_NAMETAGS.getOrDefault(player, new Nametag("", "", true));
    }


    public void update(String player) {
        Nametag nametag = GLOBAL_NAMETAGS.get(player);
        if(nametag==null){
            nametag = new Nametag("", "", true);
        }
        for(FakeTeamList teamList : TEAMS.values()) {
            teamList.reset(player);
            teamList.setNametag(
                    player,
                    nametag.getPrefix(),
                    nametag.getSuffix(),
                    nametag.isVisible()
            );
        }
    }
    public void update() {
        for(Player player : Bukkit.getOnlinePlayers()) {
            Nametag nametag = GLOBAL_NAMETAGS.get(player.getName());
            if (nametag == null) {
                nametag = new Nametag("", "", true);
            }
            for(Player receiver : Bukkit.getOnlinePlayers()) {
                FakeTeamList teamList = TEAMS.get(receiver);
                if(teamList == null) {
                    teamList = new FakeTeamList(receiver);
                    TEAMS.put(receiver, teamList);
                }
                teamList.setNametag(player.getName(), nametag.getPrefix(), nametag.getSuffix(), nametag.isVisible());
            }
        }
    }
    public void reset(String player) {
        for(FakeTeamList teamList : TEAMS.values()) {
            teamList.reset(player);
        }
        GLOBAL_NAMETAGS.remove(player);
    }
    void reset() {
        for(FakeTeamList teamList : TEAMS.values()) {
            teamList.reset();
        }
        TEAMS.clear();
        GLOBAL_NAMETAGS.clear();
    }



}