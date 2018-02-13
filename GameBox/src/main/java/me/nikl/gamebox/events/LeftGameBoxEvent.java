package me.nikl.gamebox.events;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Created by Niklas
 *
 * Event called after a player left GameBox and
 * the players inventory was restored.
 */
public class LeftGameBoxEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Player player;

    public LeftGameBoxEvent(Player player){
        this.player = player;
        Bukkit.getPluginManager().callEvent(this);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList(){
        return handlers;
    }
}
