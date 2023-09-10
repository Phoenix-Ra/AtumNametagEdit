package com.nametagedit.plugin.hooks;

import com.nametagedit.plugin.NametagHandler;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.group.GroupDataRecalculateEvent;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class HookLuckPerms implements Listener {

    private final NametagHandler handler;

    public HookLuckPerms(NametagHandler handler) {
        this.handler = handler;
        EventBus eventBus = LuckPermsProvider.get().getEventBus();
        eventBus.subscribe(handler.getPlugin(), UserDataRecalculateEvent.class, this::onUserDataRecalculateEvent);
        eventBus.subscribe(handler.getPlugin(), GroupDataRecalculateEvent.class, this::onGroupDataRecalculateEvent);
    }

    private void onUserDataRecalculateEvent(UserDataRecalculateEvent event) {
        User user = event.getUser();
        Player player = Bukkit.getPlayer(user.getUniqueId());
        if (player != null) {
            handler.getPlugin().getManager().reset(player.getName());
            new BukkitRunnable() {
                @Override
                public void run() {
                    handler.getAbstractConfig().load(player, true);
                    AtumGangsHook.sendPacketsForAll();
                }
            }.runTaskLaterAsynchronously(handler.getPlugin(), 3);
        }
    }

    private void onGroupDataRecalculateEvent(GroupDataRecalculateEvent event) {
        Group group = event.getGroup();
        for(Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("group."+group.getName())) {
                handler.getPlugin().getManager().reset(player.getName());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        handler.getAbstractConfig().load(player, true);
                        AtumGangsHook.sendPacketsForAll();
                    }
                }.runTaskLaterAsynchronously(handler.getPlugin(), 3);
            }
        }
    }
}
