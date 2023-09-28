package com.nametagedit.plugin.hooks;

import com.nametagedit.plugin.NametagHandler;
import com.nametagedit.plugin.api.events.NametagEvent;
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
import java.util.List;
import java.util.stream.Collectors;

//@TODO change current approach, cause it is quite resources consuming
// and not very efficient, i.e. O(n^2) complexity where n is the number of players
public class AtumGangsHook implements Listener {

    private final String color_member = "§a";
    private final String color_ally = "§b";
    private final String color_neutral = "";

    private final NametagHandler handler;


    public AtumGangsHook(final NametagHandler handler) {
        this.handler = handler;
    }

    @EventHandler
    public void onNameTagApply(NametagEvent event){
        Player player = Bukkit.getPlayerExact(event.getPlayer());
        if(player == null || !player.isOnline()) return;
        GangUser gangUser = Gang.getPlugin().getCacheManager().getGangUser(
                event.getReceiver().getUniqueId()
        );
        if(gangUser == null) return;
        List<String> members = Gang.getPlugin().getCacheManager().getMembers(gangUser.getGangId())
                .stream().map(GangUser::getPlayerName).collect(Collectors.toList());
        List<Player> allies = GangUtils.getOnlineGangAllyMembers(gangUser);
        if(members.contains(player.getName())) {
            event.getNametag().setPrefix(event.getNametag().getPrefix()+color_member);
            return;
        }
        if(allies.contains(player)) {
            event.getNametag().setPrefix(event.getNametag().getPrefix()+color_ally);
            return;
        }
        event.getNametag().setPrefix(event.getNametag().getPrefix()+color_neutral);
    }
    @EventHandler
    public void onPlayerGangChangeEvent(PlayerGangChangeEvent event) {
        handler.getNametagManager().update();
    }
    @EventHandler
    public void onGangAllianceFormed(GangAllianceFormedEvent event){
        handler.getNametagManager().update();
    }
    @EventHandler
    public void onGangAllianceBreak(GangAllianceBreakEvent event){
        handler.getNametagManager().update();
    }

}
