package ca.awoo.hopperfilter;

import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.ChatColor;

public class HopperListener implements Listener {

    HashMap<String, Pattern> patternCache;

    public HopperListener(){
        patternCache = new HashMap<>();
    }

    boolean filterMatch(String filterString, String fullItemName){
        Pattern p;
        if(patternCache.containsKey(filterString)){
            p = patternCache.get(filterString);
        }else {
            try {
                String regexString = filterStringToRegex(filterString);
                p = Pattern.compile(regexString);
                patternCache.put(filterString, p);
            }catch(PatternSyntaxException e){
                return false;
            }
        }
        Matcher m = p.matcher(fullItemName);
        return m.find();
    }

    String filterStringToRegex(String filterString){
        String[] sections = filterString.split(",");
        StringBuilder sb = new StringBuilder();
        for(String section : sections){
            sb.append(wildcardToRegex(section));
            //Append bar only if not last section
            if(!section.equals(sections[sections.length - 1])){
                sb.append("|");
            }
        }
        return sb.toString();
    }

    String wildcardToRegex(String query){
        String[] sections = query.split("[*?]");
        StringBuilder sb = new StringBuilder();
        sb.append("^");
        int pos = 0;
        for(String section : sections){
            sb.append(Pattern.quote(section));
            pos += section.length();
            try {
                char wildcard = query.charAt(pos);
                if(wildcard == '*'){
                    sb.append(".*");
                }else if(wildcard == '?'){
                    sb.append(".");
                }
            }catch(IndexOutOfBoundsException ex){}
        }
        sb.append("$");
        return sb.toString();
    }

    @EventHandler
    void onInventoryMoveItemEvent(InventoryMoveItemEvent event){
        if(event.getDestination().getType().equals(InventoryType.HOPPER) && event.getDestination().getHolder() instanceof Container){
            String customName = ((Container) event.getDestination().getHolder()).getCustomName();
            if(customName != null){
                String itemName = event.getItem().getType().getKey().getKey();
                if(!filterMatch(customName, itemName)){
                    //HopperFilter.loggerInstance.info("Item with name of " + itemName + " is not matched with the filter " + customName);
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    void onInventoryPickupItemEvent(InventoryPickupItemEvent event) {
        if(event.getInventory().getHolder() instanceof Container){
            String customName = ((Container)event.getInventory().getHolder()).getCustomName();
            if(customName != null){
                String itemName = event.getItem().getItemStack().getType().getKey().getKey();
                if(!filterMatch(customName, itemName)){
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if(event.getItemInHand().getType().equals(Material.HOPPER) && event.getItemInHand().getItemMeta().hasDisplayName()){
            String filterString = event.getItemInHand().getItemMeta().getDisplayName();
            // String customName = event.getItemInHand().getItemMeta().getDisplayName();
            // try{
            //     Pattern.compile(customName);
            // }catch(PatternSyntaxException e){
            //     event.getPlayer().sendMessage("Invalid regex: " + customName);
            // }

            Pattern p;
            if(patternCache.containsKey(filterString)){
                p = patternCache.get(filterString);
                //regex exists, let player know regex is already valid
                event.getPlayer().sendMessage("Regex exists in cache: " + p.pattern());
            }else {
                try {
                    String regexString = filterStringToRegex(filterString);
                    p = Pattern.compile(regexString);
                    patternCache.put(filterString, p);
                }catch(PatternSyntaxException e){
                    event.getPlayer().sendMessage("Invalid regex: " + filterString);
                    TextComponent errorComponent = parseException(e);
                    event.getPlayer().spigot().sendMessage(errorComponent);
                    return;
                }
            }
        }
    }

    //parse PatternSyntaxException into array of "net.md_5.bungee.api.chat.TextComponent", exposing "setColor" to set color
    private TextComponent parseException(PatternSyntaxException e){
        TextComponent error = new TextComponent(e.getDescription());
        error.setColor(ChatColor.RED);
        return error;
    }
    
}
