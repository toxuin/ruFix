package me.repeat.ruFix;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.*;

import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ruFix extends JavaPlugin {

	private final ruFixPlayerListener PlayerListener = new ruFixPlayerListener(this);
	private final ruFixServerListener ServerListener = new ruFixServerListener(this);

	public static boolean ruFixDebug = false;
	public static String ruFixConsole = "UTF-8";
	public static String ruFixLogFile = "UTF-8";
	
	public static boolean parseConsole = true;
	public static boolean parseLogFile = true;

	public static File directory;

	public static char[] fromGame = {};
	public static char[] toGame = {};
	
	public static String prefix = null;

	private FileConfiguration config = null;
	private File configFile = null;
	
	@Override
	public void onEnable() { 
		
		directory = this.getDataFolder();

        PluginDescriptionFile pdfFile = this.getDescription();
		prefix = "[" + pdfFile.getName()+ "]";

		Logger.getLogger("Minecraft").info( prefix + " version " + pdfFile.getVersion() + " is enabled!" );

		readConfig();		

		try {
			ruFixHandler.handlerChange();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		PluginManager pm = this.getServer().getPluginManager();

// в PLAYER_CHAT Event.Priority.Lowest - важен для выполнения перекодировки и применения её для всех эвентов		
// Lowest - выполняется одним из первых
// Highest - выполняется одним из последних
		
		pm.registerEvents(PlayerListener, this);
		pm.registerEvents(ServerListener, this);

		readTables();
	}

	@Override
	public void onDisable(){ 
		Logger.getLogger("Minecraft").info( prefix + " is disabled!" );
	}
	
	 public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
	        return readCommand(sender, commandLabel, args);
	    }

    public boolean readCommand(CommandSender sender, String command, String[] args) {
        if((command.equalsIgnoreCase("rufixreload")) && (sender instanceof ConsoleCommandSender)) {
        	this.reloadConfig();
        	readConfig();
        	readTables();
        	if (ruFixDebug) sender.sendMessage("ParseConsole is now " + parseConsole );
        	return true;
        }
        return false;
    }
	
    public void saveConfig() {
	     if (config == null || configFile == null) {
	     return;
	     }
	     try {
 	         config.save(configFile);
 	     } catch (IOException ex) {
	         Logger.getLogger(JavaPlugin.class.getName()).log(Level.SEVERE, "Could not save config to " + configFile, ex);
	     }
	}
    
    private void createNewConfig() {
    	configFile = new File(directory,"config_new.yml");
        config = YamlConfiguration.loadConfiguration(configFile);
        
        String[] listOfTables = {"ru"};
        config.set("Tables", Arrays.asList(listOfTables));
        config.set("Debug", false);
        config.set("LogFile", "UTF-8");
        config.set("Console", "UTF-8");
        config.set("ParseConsole", true);
        config.set("ParseLogFile", true);
        
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Win")) {
			config.set("Console", "CP866");
        } else if (osName.startsWith("Linux")) {
        	config.set("Console", "UTF-8");
        	config.set("ParseConsole", false);
        }
        
        try {
    		config.save(configFile);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    private void placeFiles() {

        boolean configExists = false;
        boolean langTableExists = false;
        
        if (directory.exists()) {
            for (String f : directory.list()) {
                if (f.equalsIgnoreCase("config.yml"))
                	configExists = true;
                if (f.equalsIgnoreCase("ru.tbl"))
                	langTableExists = true;
            }
        } else {
        	directory.mkdir();
        }
        
        if (!configExists) {
        	Logger.getLogger("Minecraft").info(prefix+": WRITING DEFAULT CONFIG");
        	createNewConfig();
        }
        
        if (!langTableExists) {
                boolean writeFile = true;
                try {
                    InputStream defaultStream = null;
                    File conf = null;

                	Logger.getLogger("Minecraft").info(prefix+ ": WRITING DEFAULT LANGUAGE TABLE (RU)");
                    defaultStream = this.getClass().getResourceAsStream("/ru.tbl");
                    conf = new File(directory + File.separator +"ru.tbl");
                    if (langTableExists) writeFile = false;

                    if (writeFile) {
                        directory.mkdir();
                        conf.createNewFile();
                        
                        OutputStream confStream = new FileOutputStream(conf);
                        
                        byte buf[] = new byte[1024];
                        int len;
                        
                        while ((len = defaultStream.read(buf)) > 0) {
                            confStream.write(buf, 0, len);
                        }
                        
                        defaultStream.close();
                        confStream.close();
                    }
                    
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            
        }
    }
	
	private void readConfig() {
    	
		placeFiles();
    	File configFile = new File(directory, "config.yml");
    	
    	config = YamlConfiguration.loadConfiguration(configFile);
    	
    	saveConfig();

    	ruFixDebug = config.getBoolean("Debug", false);
    	parseConsole = config.getBoolean("ParseConsole", true);
    	parseLogFile = config.getBoolean("ParseLogFile", true);
    	ruFixConsole = config.getString("Console", "UTF-8");
    	ruFixLogFile = config.getString("LogFile", "UTF-8");
    	
    	
/*    	String[] split = config.getString("Vanilla.hiddenplugins", "").split(",");
    	for (int i = 0; i < split.length; i++){
    		hiddenPlugins.add(split[i].toLowerCase());
    	}
*/
    }

	private void readTables() {
		
		File configFile = new File(directory, "config.yml");
	    config = YamlConfiguration.loadConfiguration(configFile);

		List<String> fixTables = config.getStringList("Tables");
    	
    	if (fixTables != null) {
    		for (String fixTable : fixTables)
            {
    			File file = new File(directory, fixTable + ".tbl");
    	    	FileReader input;
				try {
					input = new FileReader(file);
	    	    	BufferedReader bufRead = new BufferedReader(input);
	    	    	
	    	    	// read remark
	    	    	bufRead.readLine();

	    	    	fromGame = bufRead.readLine().toCharArray();
	    	    	toGame = bufRead.readLine().toCharArray();

//	    	    	System.out.print(line);
//	    	    	System.out.print(new String (fromGame));
//	    	    	System.out.print(fromGame.length);
//	    	    	System.out.print(new String (toGame));
//	    	    	System.out.print(toGame.length);

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    	    }
     	}
	}
	
	public static String fixUseTable(String msg){
		for (int n = 0; n < msg.length(); n++) {
			char t = msg.charAt(n);
//System.out.println("char: " + t);
//System.out.println("int: " + (int)t);
			int idx = Arrays.binarySearch(ruFix.fromGame, t);
//System.out.println("idx: " + idx);
			if (idx > -1){
				msg = msg.replace(t, ruFix.toGame[idx]);
//System.out.println("charf: " + ruFix.toGame[idx]);
			}
		}
		return msg;
	}
}
