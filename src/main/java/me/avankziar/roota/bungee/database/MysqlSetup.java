package main.java.me.avankziar.roota.bungee.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;

import main.java.me.avankziar.roota.bungee.RootA;

public class MysqlSetup 
{
	private String host;
	private int port;
	private String database;
	private String user;
	private String password;
	private boolean isAutoConnect;
	private boolean isVerifyServerCertificate;
	private boolean isSSLEnabled;
	
	public MysqlSetup(RootA plugin, boolean adm, String path)
	{
		if(adm)
		{
			plugin.getLogger().log(Level.INFO, "Using IFH Administration");
		}
		host = adm ? plugin.getAdministration().getHost(path)
				: plugin.getYamlHandler().getConfig().getString("Mysql.Host");
		port = adm ? plugin.getAdministration().getPort(path)
				: plugin.getYamlHandler().getConfig().getInt("Mysql.Port", 3306);
		database = adm ? plugin.getAdministration().getDatabase(path)
				: plugin.getYamlHandler().getConfig().getString("Mysql.DatabaseName");
		user = adm ? plugin.getAdministration().getUsername(path)
				: plugin.getYamlHandler().getConfig().getString("Mysql.User");
		password = adm ? plugin.getAdministration().getPassword(path)
				: plugin.getYamlHandler().getConfig().getString("Mysql.Password");
		isAutoConnect = adm ? plugin.getAdministration().isAutoReconnect(path)
				: plugin.getYamlHandler().getConfig().getBoolean("Mysql.AutoReconnect", true);
		isVerifyServerCertificate = adm ? plugin.getAdministration().isVerifyServerCertificate(path)
				: plugin.getYamlHandler().getConfig().getBoolean("Mysql.VerifyServerCertificate", false);
		isSSLEnabled = adm ? plugin.getAdministration().useSSL(path)
				: plugin.getYamlHandler().getConfig().getBoolean("Mysql.SSLEnabled", false);
		loadMysqlSetup();
	}
	
	public boolean connectToDatabase() 
	{
		RootA.log.info("Connecting to the database...");
		try
		{
			getConnection();
			RootA.log.info("Database connection successful!");
			return true;
		} catch(Exception e) 
		{
			RootA.log.log(Level.WARNING, "Could not connect to Database!", e);
			return false;
		}
	}
	
	public Connection getConnection() throws SQLException
	{
		return reConnect();
	}
	
	private Connection reConnect() throws SQLException
	{
		String url = "jdbc:mysql://";
		boolean bool = false;
	    try
	    {
	    	// Load new Drivers for papermc
	    	Class.forName("com.mysql.cj.jdbc.Driver");
	    	bool = true;
	    } catch (Exception e)
	    {
	    	bool = false;
	    } 
	    if (bool == false)
    	{
    		// Load old Drivers for spigot
    		try
    		{
    			Class.forName("com.mysql.jdbc.Driver");
    		}  catch (Exception e) {}
    	}
        Properties properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", password);
        properties.setProperty("autoReconnect", String.valueOf(isAutoConnect));
        properties.setProperty("verifyServerCertificate", String.valueOf(isVerifyServerCertificate));
        properties.setProperty("useSSL", String.valueOf(isSSLEnabled));
        properties.setProperty("requireSSL", String.valueOf(isSSLEnabled));
        //Connect to database
        Connection conn = DriverManager.getConnection(url + host + ":" + port + "/" + database, properties);
        return conn;
	}
	
	private boolean baseSetup(String data) 
	{
		try (Connection conn = getConnection(); PreparedStatement query = conn.prepareStatement(data))
		{
			query.execute();
		} catch (SQLException e) 
		{
			RootA.log.log(Level.WARNING, "Could not build data source. Or connection is null", e);
		}
		return true;
	}
	
	public boolean loadMysqlSetup()
	{
		if(!connectToDatabase())
		{
			return false;
		}
		if(!setupDatabaseI())
		{
			return false;
		}
		return true;
	}
	
	private boolean setupDatabaseI()
	{
		String data = "CREATE TABLE IF NOT EXISTS `" + MysqlHandler.Type.PLAYERLOCATION.getValue()
		+ "` (id int AUTO_INCREMENT PRIMARY KEY,"
		+ " player_uuid char(36) NOT NULL UNIQUE,"
		+ " player_name varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,"
		+ " server_name text);";
		baseSetup(data);
		return true;
	}
}