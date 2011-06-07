package tzer0.AuthorGreeter;



import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

/**
 * A plugin which welcomes plugin authors!
 * 
 * @author TZer0
 */
public class AuthorGreeter extends JavaPlugin {
    private final AuthorGreeterPlayerListener listener = new AuthorGreeterPlayerListener();
    PluginDescriptionFile pdfFile;
    Configuration conf;
    PermissionHandler permissions;
    boolean adminOnly;
    boolean longVersion;
    @SuppressWarnings("unused")
    private final String name = "AuthorGreeter";

    /* (non-Javadoc)
     * @see org.bukkit.plugin.Plugin#onDisable()
     */
    public void onDisable() {
        System.out.println(pdfFile.getName() + " disabled.");
        getServer().getScheduler().cancelTasks(this);
    }

    /* (non-Javadoc)
     * @see org.bukkit.plugin.Plugin#onEnable()
     */
    public void onEnable() {
        setupPermissions();
        conf = getConfiguration();
        pdfFile = getDescription();
        adminOnly = conf.getBoolean("adminOnly", false);
        longVersion = conf.getBoolean("longVersion", true);
        listener.setPointers(conf, this, permissions);
        PluginManager tmp = getServer().getPluginManager();
        tmp.registerEvent(Event.Type.PLAYER_JOIN, listener, Priority.Normal, this);
        System.out.println(pdfFile.getName() + " version "
                + pdfFile.getVersion() + " is enabled!");
    }
    /* (non-Javadoc)
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    public boolean onCommand(CommandSender sender, Command cmd,
            String commandLabel, String[] args) {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }
        int l = args.length;
        boolean help = false;
        if (player != null) {
            // user-accessible commands
            if (!(permissions == null || (permissions != null && permissions.has(player, "ag.admin")))) {
                player.sendMessage(ChatColor.RED + "You do not have access to this command.");
                return true;
            }
        }
        if (l >= 1) {
            if (args[0].equalsIgnoreCase("adminonly") || args[0].equalsIgnoreCase("ao")) {
                if (l == 2) {
                    adminOnly = args[1].equalsIgnoreCase("t") || args[1].equalsIgnoreCase("true");
                    conf.setProperty("adminOnly", adminOnly);
                    conf.save();
                }
                String state = "";
                if (adminOnly) {
                    state = "on";
                } else {
                    state = "off";
                }
                sender.sendMessage(ChatColor.YELLOW + String.format("Admin only mode is now %s", state));
            } else if (args[0].equalsIgnoreCase("longversion") || args[0].equalsIgnoreCase("lv")) {
                if (l == 2) {
                    longVersion = args[1].equalsIgnoreCase("t") || args[1].equalsIgnoreCase("true");
                    conf.setProperty("longVersion", longVersion);
                    conf.save();
                }
                String state = "";
                if (longVersion) {
                    state = "on";
                } else {
                    state = "off";
                }
                sender.sendMessage(ChatColor.YELLOW + String.format("Long version mode is now %s", state));
            }
        }
        if (help) {
        }
        return true;
    }
    /**
     * Basic Permissions-setup, see more here: https://github.com/TheYeti/Permissions/wiki/API-Reference
     */
    private void setupPermissions() {
        Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");

        if (this.permissions == null) {
            if (test != null) {
                this.permissions = ((Permissions) test).getHandler();
            } else {
                System.out.println(ChatColor.YELLOW
                        + "Permissons not detected - defaulting to OP!");
            }
        }
    }
}