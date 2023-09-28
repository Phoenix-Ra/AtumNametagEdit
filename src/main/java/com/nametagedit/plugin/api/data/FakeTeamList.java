package com.nametagedit.plugin.api.data;


import com.nametagedit.plugin.AtumNametagEdit;
import com.nametagedit.plugin.api.events.NametagEvent;
import com.nametagedit.plugin.hooks.AtumGangsHook;
import com.nametagedit.plugin.packets.PacketWrapper;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FakeTeamList {
    private final AtumNametagEdit plugin = AtumNametagEdit.getInstance();
    private final Player receiver;

    private final Map<String, FakeTeam> TEAMS = new ConcurrentHashMap<>();
    private final Map<String, FakeTeam> CACHED_FAKE_TEAMS = new ConcurrentHashMap<>();

    public FakeTeamList(Player receiver) {
        this.receiver = receiver;
    }

    private FakeTeam getFakeTeam(String prefix, String suffix, boolean visible) {
        return TEAMS.values().stream().filter(fakeTeam -> fakeTeam.isSimilar(prefix, suffix, visible)).findFirst().orElse(null);
    }
    private void addPlayerToTeam(String player, String prefix, String suffix, int sortPriority, boolean playerTag, boolean visible) {

        NametagEvent event = new NametagEvent(
                player,
                receiver,
                new Nametag(prefix, suffix, visible),
                NametagEvent.ChangeType.UNKNOWN
        );
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        prefix = event.getNametag().getPrefix();
        suffix = event.getNametag().getSuffix();
        visible = event.getNametag().isVisible();
        FakeTeam previous = getFakeTeam(player);

        if (previous != null && previous.isSimilar(prefix, suffix, visible)) {
            return;
        }

        reset(player);

        FakeTeam joining = getFakeTeam(prefix, suffix, visible);
        if (joining != null) {
            joining.addMember(player);
        } else {
            joining = new FakeTeam(prefix, suffix, sortPriority, playerTag);
            joining.setVisible(visible);
            joining.addMember(player);
            TEAMS.put(joining.getName(), joining);
            addTeamPackets(joining);
        }

        Player adding = Bukkit.getPlayerExact(player);
        if (adding != null) {
            addPlayerToTeamPackets(joining, adding.getName());
            cache(adding.getName(), joining);
        } else {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
            addPlayerToTeamPackets(joining, offlinePlayer.getName());
            cache(offlinePlayer.getName(), joining);
        }
    }
    public FakeTeam reset(String player) {
        return reset(player, decache(player));
    }

    private FakeTeam reset(String player, FakeTeam fakeTeam) {
        if (fakeTeam != null && fakeTeam.getMembers().remove(player)) {
            boolean delete;
            Player removing = Bukkit.getPlayerExact(player);
            if (removing != null) {
                delete = removePlayerFromTeamPackets(fakeTeam, removing.getName());
            } else {
                OfflinePlayer toRemoveOffline = Bukkit.getOfflinePlayer(player);
                delete = removePlayerFromTeamPackets(fakeTeam, toRemoveOffline.getName());
            }

            if (delete) {
                removeTeamPackets(fakeTeam);
                TEAMS.remove(fakeTeam.getName());
            }
        }

        return fakeTeam;
    }

    // ==============================================================
    // Below are public methods to modify the cache
    // ==============================================================
    private FakeTeam decache(String player) {
        return CACHED_FAKE_TEAMS.remove(player);
    }

    public FakeTeam getFakeTeam(String player) {
        return CACHED_FAKE_TEAMS.get(player);
    }

    private void cache(String player, FakeTeam fakeTeam) {
        CACHED_FAKE_TEAMS.put(player, fakeTeam);
    }

    // ==============================================================
    // Below are public methods to modify certain data
    // ==============================================================
    public void setNametag(String player, String prefix, String suffix) {
        setNametag(player, prefix, suffix, -1);
    }

    public void setNametag(String player, String prefix, String suffix, boolean visible) {
        setNametag(player, prefix, suffix, -1, false, visible);
    }


    void setNametag(String player, String prefix, String suffix, int sortPriority) {
        setNametag(player, prefix, suffix, sortPriority, false, true);
    }

    public void setNametag(String player, String prefix, String suffix, int sortPriority, boolean playerTag, boolean visible) {
        addPlayerToTeam(player, prefix != null ? prefix : "", suffix != null ? suffix : "", sortPriority, playerTag, visible);
    }

    public void reset() {
        for (FakeTeam fakeTeam : TEAMS.values()) {
            removePlayerFromTeamPackets(fakeTeam, fakeTeam.getMembers());
            removeTeamPackets(fakeTeam);
        }
        CACHED_FAKE_TEAMS.clear();
        TEAMS.clear();
    }

    // ==============================================================
    // Below are private methods to construct a new Scoreboard packet
    // ==============================================================
    private void removeTeamPackets(FakeTeam fakeTeam) {
        new PacketWrapper(fakeTeam.getName(), fakeTeam.getPrefix(), fakeTeam.getSuffix(), 1, new ArrayList<>(), fakeTeam.isVisible()).send(receiver);
    }

    private boolean removePlayerFromTeamPackets(FakeTeam fakeTeam, String... players) {
        return removePlayerFromTeamPackets(fakeTeam, Arrays.asList(players));
    }

    private boolean removePlayerFromTeamPackets(FakeTeam fakeTeam, List<String> players) {
        new PacketWrapper(fakeTeam.getName(), 4, players).send(receiver);
        fakeTeam.getMembers().removeAll(players);
        return fakeTeam.getMembers().isEmpty();
    }

    private void addTeamPackets(FakeTeam fakeTeam) {
        new PacketWrapper(fakeTeam.getName(), fakeTeam.getPrefix(), fakeTeam.getSuffix(), 0, fakeTeam.getMembers(), fakeTeam.isVisible()).send(receiver);
    }

    private void addPlayerToTeamPackets(FakeTeam fakeTeam, String player) {
        new PacketWrapper(fakeTeam.getName(), 3, Collections.singletonList(player)).send(receiver);
    }
}
