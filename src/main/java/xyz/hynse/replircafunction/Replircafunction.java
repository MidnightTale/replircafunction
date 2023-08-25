package xyz.hynse.replircafunction;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

public final class Replircafunction extends JavaPlugin implements CommandExecutor {

    @Override
    public void onEnable() {
        // Register the command
        Objects.requireNonNull(getCommand("functionreplica")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
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

        // Define a boolean variable for dev mode
        boolean devMode = true;
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
                            if (line.startsWith("functionreplica ")) {
                                // Extract the nested function path
                                String nestedFunctionPath = line.substring("functionreplica ".length());
                                executeFunction(sender, nestedFunctionPath);
                            } else {
                                if (devMode) {
                                    sender.sendMessage(ChatColor.GRAY + "Executing: " + line);
                                }

                                if (line.startsWith("/")) {
                                    // Regular command, execute it
                                    String finalLine = line;
                                    Bukkit.getGlobalRegionScheduler().run(this, scheduledTask -> {
                                        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Executing command: " + finalLine);
                                        getServer().dispatchCommand(getServer().getConsoleSender(), finalLine.substring(1)); // Remove the leading "/"
                                    });
                                } else {
                                    // Function, execute it
                                    String finalLine1 = line;
                                    Bukkit.getGlobalRegionScheduler().run(this, scheduledTask -> {
                                        Bukkit.getConsoleSender().sendMessage(ChatColor.YELLOW + "Executing command: function " + finalLine1);
                                        getServer().dispatchCommand(getServer().getConsoleSender(), "function " + finalLine1);
                                    });
                                }
                            }
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
