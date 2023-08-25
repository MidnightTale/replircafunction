package xyz.hynse.replircafunction;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public final class Replircafunction extends JavaPlugin implements CommandExecutor {

    // Define a boolean variable for dev mode
    private boolean devMode = false;

    @Override
    public void onEnable() {
        // Register the command
        getCommand("functionreplica").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /functionreplica <namespace:function_path>");
            return true;
        }

        String functionPath = args[0];

        // Execute the specified function
        executeFunction(sender, functionPath);

        return true;
    }

    private void executeFunction(CommandSender sender, String functionPath) {
        String functionsDirectory = "world/datapacks/";
        String[] namespacePath = functionPath.split(":");

        if (namespacePath.length != 2) {
            sender.sendMessage(ChatColor.RED + "Invalid function path format: " + functionPath);
            return;
        }

        String namespace = namespacePath[0];
        String path = namespacePath[1].replace("/", File.separator) + ".mcfunction";

        File datapacksDir = new File(functionsDirectory);
        File[] datapackDirs = datapacksDir.listFiles(File::isDirectory);

        if (datapackDirs == null) {
            sender.sendMessage(ChatColor.RED + "No datapacks found in " + functionsDirectory);
            return;
        }

        for (File datapackDir : datapackDirs) {
            File functionFile = new File(datapackDir, "data" + File.separator + namespace + File.separator + "functions" + File.separator + path);

            if (functionFile.exists()) {
                if (devMode) {
                    sender.sendMessage(ChatColor.GREEN + "Function file found: " + functionFile.getPath());
                    sender.sendMessage(ChatColor.BLUE + "Executing function...");
                }

                try {
                    List<String> lines = Files.readAllLines(functionFile.toPath(), StandardCharsets.UTF_8);

                    for (String line : lines) {
                        line = line.trim();

                        if (!line.isEmpty() && !line.startsWith("#")) {
                            if (devMode) {
                                sender.sendMessage(ChatColor.GRAY + "Executing: " + line);
                            }
                            String finalLine = line;
                            Bukkit.getGlobalRegionScheduler().run(this, scheduledTask -> {
                            getServer().dispatchCommand(getServer().getConsoleSender(), finalLine);
                            });
                        }
                    }

                    if (devMode) {
                        sender.sendMessage(ChatColor.GREEN + "Function executed successfully: " + functionPath);
                    }

                    return;
                } catch (IOException e) {
                    if (devMode) {
                        sender.sendMessage(ChatColor.RED + "Error reading or executing function: " + e.getMessage());
                    }
                    return;
                }
            }
        }

        if (devMode) {
            sender.sendMessage(ChatColor.RED + "Function file not found: " + functionPath);
        }
    }
}
