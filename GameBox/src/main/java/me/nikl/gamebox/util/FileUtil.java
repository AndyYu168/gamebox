package me.nikl.gamebox.util;

import me.nikl.gamebox.GameBox;
import me.nikl.gamebox.Module;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

/**
 * Created by nikl on 23.10.17.
 *
 * Utility class for language related stuff
 */
public class FileUtil {

    /**
     * Copy all default language files to the plugin folder
     *
     * This method checks for every .yml in the language folder
     * whether it is already present in the plugins language folder.
     * If not it is copied.
     */
    public static void copyDefaultLanguageFiles(){
        URL main = GameBox.class.getResource("GameBox.class");

        try {
            JarURLConnection connection = (JarURLConnection) main.openConnection();

            JarFile jar = new JarFile(connection.getJarFileURL().getFile());
            Plugin gameBox = Bukkit.getPluginManager().getPlugin("GameBox");
            for (Enumeration list = jar.entries(); list.hasMoreElements(); ) {
                JarEntry entry = (JarEntry) list.nextElement();
                if(entry.getName().split(File.separator)[0].equals("language")) {

                    String[] pathParts = entry.getName().split(File.separator);

                    if (pathParts.length < 2 || !entry.getName().endsWith(".yml")){
                        continue;
                    }

                    File file = new File(gameBox.getDataFolder().toString() + File.separatorChar + entry.getName());
                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        gameBox.saveResource(entry.getName(), false);
                    }
                }
            }
            jar.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void copyExternalResources(GameBox gameBox, Module module){
        JavaPlugin external = module.getExternalPlugin();
        if(external == null) return;
        URL main = external.getClass().getResource(module.getClassPath()
                .split("\\.")[module.getClassPath().split("\\.").length - 1] + ".class");

        try {
            JarURLConnection connection = (JarURLConnection) main.openConnection();

            JarFile jar = new JarFile(connection.getJarFileURL().getFile());
            for (Enumeration list = jar.entries(); list.hasMoreElements(); ) {
                JarEntry entry = (JarEntry) list.nextElement();

                String[] pathParts = entry.getName().split(File.separator);
                String folderName = pathParts[0];

                if(folderName.equals("language")) {

                    // ToDo: make sure that every module has a default file and force module folder for config and language!

                    if (pathParts.length < 2 || !entry.getName().endsWith(".yml")){
                        continue;
                    }

                    // length = 2 is ok, because all lang files belong to the one module then
                    if(pathParts.length >= 3){
                        // subfolder in language folder
                        if(!pathParts[1].equalsIgnoreCase(module.getModuleID())) continue;
                    }

                    String fileName = pathParts[pathParts.length - 1];
                    String gbPath = gameBox.getDataFolder().toString() + File.separatorChar
                            + "language" + File.separatorChar
                            + module.getModuleID() + File.separatorChar + fileName;
                    File file = new File(gbPath);

                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        saveResourceToGBFolder(entry.getName(), gbPath, external);
                    }

                } else if(folderName.equalsIgnoreCase("games")){
                    if(entry.isDirectory()) continue;

                    // length = 2 is ok, because all lang files belong to the one module then
                    if(pathParts.length >= 3){
                        // subfolder in language folder
                        if(!pathParts[1].equalsIgnoreCase(module.getModuleID())){
                            // resource of other module
                            continue;
                        }
                    }

                    StringBuilder builder = new StringBuilder();
                    for(int i = 1; i < pathParts.length; i++){
                        builder.append(pathParts[i]);
                        if(i+1 < pathParts.length){
                            builder.append(File.separatorChar);
                        }
                    }
                    String fileName = builder.toString();

                    String gbPath = gameBox.getDataFolder().toString() + File.separatorChar
                            + "games" + File.separatorChar
                            + module.getModuleID() + File.separatorChar + fileName;
                    File file = new File(gbPath);

                    if (!file.exists()) {
                        file.getParentFile().mkdirs();
                        saveResourceToGBFolder(entry.getName(), gbPath, external);
                    }
                }
            }
            jar.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static private void saveResourceToGBFolder(String resourcePath, String gbPath, JavaPlugin plugin) {
        if(resourcePath == null || resourcePath.isEmpty()) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        Plugin gameBox = Bukkit.getPluginManager().getPlugin("GameBox");
        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = plugin.getResource(resourcePath);
        if(in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + plugin.getName());
        }

        File outFile = new File(gbPath);
        int lastIndex = resourcePath.lastIndexOf(47);
        File outDir = new File(gameBox.getDataFolder(), resourcePath.substring(0, lastIndex >= 0?lastIndex:0));
        if(!outDir.exists()) {
            outDir.mkdirs();
        }

        if(outFile.exists()) return;

        try {
            OutputStream out = new FileOutputStream(outFile);
            byte[] buf = new byte[1024];

            int len;
            while((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            out.close();
            in.close();
        } catch (IOException var10) {
            gameBox.getLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, var10);
        }
    }
}
