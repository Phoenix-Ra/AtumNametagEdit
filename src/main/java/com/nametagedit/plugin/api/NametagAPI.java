package com.nametagedit.plugin.api;

import com.nametagedit.plugin.NametagHandler;
import com.nametagedit.plugin.NametagManager;
import com.nametagedit.plugin.api.data.FakeTeam;
import com.nametagedit.plugin.api.data.GroupData;
import com.nametagedit.plugin.api.data.Nametag;
import com.nametagedit.plugin.api.events.NametagEvent;
import lombok.AllArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Implements the INametagAPI interface. There only
 * exists one instance of this class.
 */
@AllArgsConstructor
public final class NametagAPI implements INametagApi {

    private final NametagHandler handler;
    private final NametagManager manager;

    @Override
    public Nametag getNametag(Player player) {
        return manager.getGlobalNametag(player.getName());
    }

    @Override
    public void clearNametag(Player player) {
        if (shouldFireEvent(player, NametagEvent.ChangeType.CLEAR)) {
            manager.reset(player.getName());
        }
    }

    @Override
    public void reloadNametag(Player player) {
        if (shouldFireEvent(player, NametagEvent.ChangeType.RELOAD)) {
            handler.applyTagToPlayer(player, false);
        }
    }

    @Override
    public void clearNametag(String player) {
        manager.reset(player);
    }

    @Override
    public void setPrefix(Player player, String prefix) {
        Nametag nametag = manager.getGlobalNametag(player.getName());
        setNametagAlt(player, prefix, nametag.getSuffix());
    }

    @Override
    public void setSuffix(Player player, String suffix) {
        Nametag nametag = manager.getGlobalNametag(player.getName());
        setNametagAlt(player, nametag.getPrefix(), suffix);
    }

    @Override
    public void setPrefix(String player, String prefix) {
        Nametag nametag = manager.getGlobalNametag(player);
        manager.setNametag(player, prefix, nametag.getSuffix());
    }

    @Override
    public void setSuffix(String player, String suffix) {
        Nametag nametag = manager.getGlobalNametag(player);
        manager.setNametag(player, nametag.getPrefix(), suffix);
    }

    @Override
    public void setNametag(Player player, String prefix, String suffix) {
        setNametagAlt(player, prefix, suffix);
    }

    @Override
    public void setNametag(String player, String prefix, String suffix) {
        manager.setNametag(player, prefix, suffix);
    }

    @Override
    public void hideNametag(Player player) {
        Nametag nametag = manager.getGlobalNametag(player.getName());
        manager.setNametag(player.getName(), nametag.getPrefix(), nametag.getSuffix(), false);
    }

    @Override
    public void hideNametag(String player) {
        Nametag nametag = manager.getGlobalNametag(player);
        manager.setNametag(player, nametag.getPrefix(), nametag.getSuffix(), false);
    }

    @Override
    public void showNametag(Player player) {
        Nametag nametag = manager.getGlobalNametag(player.getName());
        manager.setNametag(player.getName(), nametag.getPrefix(), nametag.getSuffix(), true);
    }

    @Override
    public void showNametag(String player) {
        Nametag nametag = manager.getGlobalNametag(player);
        manager.setNametag(player, nametag.getPrefix(), nametag.getSuffix(), false);
    }

    @Override
    public List<GroupData> getGroupData() {
        return handler.getGroupData();
    }

    @Override
    public void saveGroupData(GroupData... groupData) {
        handler.getAbstractConfig().save(groupData);
    }

    @Override
    public void applyTags() {
        handler.applyTags();
    }

    @Override
    public void applyTagToPlayer(Player player, boolean loggedIn) {
        handler.applyTagToPlayer(player,loggedIn);
    }

    @Override
    public void updatePlayerPrefix(String target, String prefix) {
        handler.save(target, NametagEvent.ChangeType.PREFIX, prefix);
    }

    @Override
    public void updatePlayerSuffix(String target, String suffix) {
        handler.save(target, NametagEvent.ChangeType.SUFFIX, suffix);
    }

    @Override
    public void updatePlayerNametag(String target, String prefix, String suffix) {
        handler.save(target, prefix, suffix);
    }

    /**
     * Private helper function to reduce redundancy
     */
    private boolean shouldFireEvent(Player player, NametagEvent.ChangeType type) {
        return true;
    }

    /**
     * Private helper function to reduce redundancy
     */
    private void setNametagAlt(Player player, String prefix, String suffix) {
        Nametag nametag = new Nametag(
                handler.formatWithPlaceholders(player, prefix, true),
                handler.formatWithPlaceholders(player, suffix, true)
        );
        manager.setNametag(player.getName(), nametag.getPrefix(), nametag.getSuffix());
    }

}