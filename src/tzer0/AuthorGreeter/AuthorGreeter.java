package tzer0.AuthorGreeter;



import java.util.LinkedList;

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
    LinkedList<String> ignore;
    @SuppressWarnings("unused")
    private final String name = "AuthorGreeter";

    /* (non-Javadoc)
     * @see org.bukkit.plugin.Plugin#onDisable()
     */
    public void onDisable() {
        System.out.println(pdfFile.getName() + " disabled.");
    }

    /* (non-Javadoc)
     * @see org.bukkit.plugin.Plugin#onEnable()
     */
    public void onEnable() {
        setupPermissions();
        conf = getConfiguration();
        ignore = new LinkedList<String>();
        for (String str : conf.getStringList("ignore", null)) {
            ignore.add(str.toLowerCase());
        }
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
            if (!(permissions == null || (permissions != null && permissions.has(player, "authorgreeter.admin")))) {
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
            } else if (args[0].equalsIgnoreCase("addignore") || args[0].equalsIgnoreCase("ai")) {
                if (l == 2) {
                    ignore.add(args[1].toLowerCase());
                    conf.setProperty("ignore", ignore.toArray());
                    conf.save();
                    sender.sendMessage(ChatColor.GREEN + "Done.");
                } else {
                    sender.sendMessage(ChatColor.RED + "Please provide a name");
                }
            } else if (args[0].equalsIgnoreCase("removeignore") || args[0].equalsIgnoreCase("ri")) {
                if (l == 2) {
                    ignore.remove(args[1].toLowerCase());
                    conf.setProperty("ignore", ignore.toArray());
                    conf.save();
                    sender.sendMessage(ChatColor.GREEN + "Done.");
                } else {
                    sender.sendMessage(ChatColor.RED + "Please provide a name");
                }
            } else if (args[0].equalsIgnoreCase("listignore") || args[0].equalsIgnoreCase("li")) {
                int page = 0;
                if (l == 2) {
                    page = toInt(args[1]);
                }
                sender.sendMessage(ChatColor.GREEN + String.format("Ignore authors (showing: %d to %d):", 
                        page*10, Math.min(((page+1)*10), ignore.size())));
                int i = 0;
                for (String str : ignore) {
                    if (i >= page*10) {
                        sender.sendMessage(ChatColor.GREEN + str);
                    } else if (i > (page+1)*10) {
                        break;
                    }
                    i++;
                }
                if (i < ignore.size()-1) {
                    sender.sendMessage(ChatColor.GREEN + String.format("Next page: /ag li %d", page+1));
                }
            }
        } else {
            help = true;
        }
        if (help) {
            sender.sendMessage(ChatColor.YELLOW + pdfFile.getFullName() + ChatColor.YELLOW +  " by TZer0");
            sender.sendMessage(ChatColor.GREEN + "[] denote optional arguments, () alises");
            sender.sendMessage(ChatColor.GREEN + "/ag (a)dmin(o)nly [t/f] " + ChatColor.YELLOW + "- show or toggle adminonly mode");
            sender.sendMessage(ChatColor.GREEN + "/ag (l)ong(v)ersion [t/f] " + ChatColor.YELLOW + "- show or toggle longversion mode");
            sender.sendMessage(ChatColor.GREEN + "/ag (a)dd(i)gnore name " + ChatColor.YELLOW + "- add someone to the ignore-list");
            sender.sendMessage(ChatColor.GREEN + "/ag (r)emove(i)gnore " + ChatColor.YELLOW + "- remove someone to the ignore-list");
            sender.sendMessage(ChatColor.GREEN + "/ag (l)ist(i)gnore " + ChatColor.YELLOW + "- list ignored authors");
        }
        return true;
    }
    public int toInt(String in) {
        int out = 0;
        if (checkInt(in)) {
            out = Integer.parseInt(in);
        }
        return out;
    }
    /**
     * Check if the string is valid as an int (accepts signs).
     *
     * @param in The string to be checked
     * @return boolean Success
     */
    public boolean checkInt(String in) {
        char chars[] = in.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (!(Character.isDigit(chars[i]) || (i == 0 && chars[i] == '-'))) {
                return false;
            }
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