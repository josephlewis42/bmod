package bmod.database;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import bmod.Constants;
import bmod.ExtensionPoints;
import bmod.database.objects.BuildingDependentRecord;
import bmod.database.objects.Record;
import bmod.plugin.loader.CSVRecordLoader;

/**
 * This is a basic interface for all CRUD mechanisms.
 * It will provides a nice abstraction between things like
 * SELECT, INSERT, UPDATE, DELETE methods in traditional
 * SQL based databases and even our home-rolled DB that uses
 * interlinked classes.
 * 
 * @author Joseph Lewis <joehms22@gmail.com>
 */
public class DatabaseQueryMechanism 
{
	public static final byte[] GZIP_MAGIC_NUMBERS =new byte[]{0x1f, (byte) 0x8B};
	private static Logger m_logger = Logger.getLogger("DatabaseQueryMechanism");
	protected static int m_dbCommitNum = Integer.MIN_VALUE;
	private static Connection connection = null;
	private static final Path m_databaseFile = ExtensionPoints.getBmodDirectory("database.script");
	private static final Path m_databasePropertiesFile = ExtensionPoints.getBmodDirectory("database.properties");
	private static final String HSQL_DATABASE_PATH = "/static/bmod_app/database.script";
	
	public DatabaseQueryMechanism()
	{
		loadDatabase();
	}
	
	/**
	 * Updates all of the records in the DB by calling the updateTable on each
	 * CSVRecord.
	 */
	public void updateDatabase()
	{
		CSVRecordLoader ldr = new CSVRecordLoader();
		for(Record<?> cr : ldr.getRecordPluginManagers())
		{
			cr.updateTable();
		}
	}
	
	/**
	 * Resets the database, and starts it anew from whatever condition it is
	 * normally at. e.g. if normally blank, delete it. If normally from web, do
	 * that.
	 */
	public void resetDB()
	{
		shutdown();
		
		if(Files.exists(m_databaseFile))
		{
			try
			{
				Files.deleteIfExists(m_databaseFile);
			} catch (IOException e)
			{
			}
		}
		
		loadDatabase();
		updateCommitNumber();
		updateDatabase();
	}
	
	/**
	 * Checks the integrity of a database, returns any errors or warnings found.
	 * @return
	 */
	public final DBWarningsList getIntegrityCheck(long buildingId)
	{
		DBWarningsList dwl = new DBWarningsList();

		m_logger.debug("Doing database integrity check");		
		dwl.addInfo("Info: Started checking database on: " + new Date());
		
		
		for(Record<?> tmp : new CSVRecordLoader().getPlugins())
		{
			m_logger.debug("Started integrity checking: " + tmp.getClass().getCanonicalName());
			
			if(tmp instanceof BuildingDependentRecord<?>)
			{
				m_logger.debug("\t is a BuildingDepCSVRecord!");
				BuildingDependentRecord<?> btmp = (BuildingDependentRecord<?>) tmp;
				
				for(Record<?> itemp : btmp.readBuildingDep(buildingId))
					itemp.reportIntegrityErrors(dwl);
			}
			else
			{
				for(Record<?> itemp : tmp.readAll())
					itemp.reportIntegrityErrors(dwl);
			}
		}
		
		m_logger.debug("Database integrity check finished.");
		return dwl;
	}
	
	public void createRecord(Record<?> record)
	{
		updateCommitNumber();
		
		try
		{			
			genersateStatement(record).executeUpdate();
		} catch(SQLIntegrityConstraintViolationException ex) {
			record.update();
		}catch (SQLException e)
		{
			Database.handleCriticalError(new DatabaseIntegrityException(e));
		}
	}
	
	/**
	 * Does cleanup for the environment.
	 */
	public void shutdown()
	{
		try
		{
			save();
			getPreparedStatement("SHUTDOWN").executeUpdate();
			getConnection().close();
		} 
		catch (SQLException e)
		{
			m_logger.error("Couldn't cleanly shutdown.", e);
		}
	}	
	
	
	/**
	 * Gets a prepared statement for the given database.
	 * @param query
	 * @return
	 */
	private HashMap<String, PreparedStatement> stmts = new HashMap<String, PreparedStatement>();
	private int lastStmtRev = getCommitNumber();
	public PreparedStatement getPreparedStatement(String query) throws SQLException
	{
		if(lastStmtRev != getCommitNumber())
		{
			stmts = new HashMap<String, PreparedStatement>();
			lastStmtRev = getCommitNumber();
		}
		
		PreparedStatement stmt = stmts.get(query + Thread.currentThread().getId());
		if(stmt != null)
			return stmt;
		
		stmt = connection.prepareStatement(query);
		stmts.put(query + Thread.currentThread().getId(), stmt );
		return stmt;
	}
	
	/**
	 * Gets the database connection, it is a very bad idea to use this method
	 * directly, as the connection is able to change throughout the process'
	 * execution cycle.
	 * 
	 * @return
	 */
	public Connection getConnection()
	{
		return connection;
	}	
	
	/**
	 * Reads all elements from a given table and returns the results 
	 * as objects using the given RecordFactory to convert them.
	 * @param <T>
	 * @throws DatabaseIntegrityException 
	 */
	public <T extends Record<T>> Collection<T> readTable(PreparedStatement s, T m_instance, String uuid)
	{
		LinkedList<T> l = new LinkedList<T>();
    	Object[] thisrow = new Object[m_instance.getSQLColTypes().length];

		try
		{
			ResultSet rs = s.executeQuery();
			while (rs.next()) 
	        {
	        	for (int i = 0; i < rs.getMetaData().getColumnCount(); ++i)
	        	{
	        		Object tmp = rs.getObject(i + 1);
	                if(tmp != null)
	                {
	                	thisrow[i] = rs.getObject(i + 1); // in SQL cols start at one
	                }
	                else
	                {
	                	thisrow[i] = "";
	                }
	            }
	        	
	        	
	        	try
	        	{
	        		T tmp = m_instance.fromSQL(thisrow);
	        		if(tmp != null)
	        		{
	        			l.add(tmp);
	        		}
	        	
	        	}
	        	catch(Exception e)
	        	{
	    			m_logger.error("Couldn't load object: "+Arrays.toString(thisrow), e);
	        	}
	        }
	        
		} catch (SQLException e)
		{
			Database.handleCriticalError(new DatabaseIntegrityException(e));
			return null; // should never happen
		}
		return l;
	}
	
	public static String[] dumpHeader(ResultSet rs)
	{
        try
		{
			ResultSetMetaData meta   = rs.getMetaData();
			String[] header = new String[meta.getColumnCount()];
			for(int i = 1; i <= meta.getColumnCount(); i++)
				header[i - 1] = meta.getColumnName(i);
			
			return header;
			
		} catch (SQLException e)
		{
			return new String[]{};
		}
	}
	
	
	public static Object[][] dump(ResultSet rs) throws SQLException {

		ArrayList<Object[]> items = new ArrayList<Object[]>();
        
        while (rs.next()) 
        {
        	ArrayList<Object> thisrow = new ArrayList<Object>();
            
        	for (int i = 0; i < rs.getMetaData().getColumnCount(); ++i)
            {
                if(rs.getObject(i + 1) != null)
                	thisrow.add(rs.getObject(i + 1)); // in SQL cols start at one
                else
                	thisrow.add("");
            }
        	
            items.add(thisrow.toArray(new Object[thisrow.size()]));
        }
        
        return items.toArray(new Object[items.size()][]);
    }
	
	public ResultSet executeSQLQuery(String sql) throws SQLException
	{
		Statement s = getConnection().createStatement();
		updateCommitNumber();
		return s.executeQuery(sql);
	}
	
	public int executeSQLResult(String sql) throws SQLException
	{
		Statement s = getConnection().createStatement();
		updateCommitNumber();
		return s.executeUpdate(sql);
	}
	
	/**
	 * Gets the current database version.
	 * @return
	 */
	public synchronized int getCommitNumber()
	{
		return m_dbCommitNum;
	}
	
	/**
	 * Sets the current database version.
	 * @return
	 */
	public synchronized void updateCommitNumber()
	{
		m_dbCommitNum++;
	}

	/**
	 * Writes the data file.
	 */
	public void save()
	{
		try
		{
			getConnection().commit();
			getPreparedStatement("SET FILES SCRIPT FORMAT COMPRESSED").execute();
			// causes a checkpoint automatically.
		} catch (SQLException e)
		{
			m_logger.error("Couldn't cleanly save.", e);
		}
	}
	
	/**
	 * Deletes tables that are no longer referenced in Bmod from the database.
	 * 
	 * @return the number of deleted tables.
	 */
	public int deleteUnusedTables()
	{
		try
		{
		
			String query = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.SYSTEM_TABLES"
							+ " WHERE TABLE_TYPE='TABLE' AND TABLE_SCHEM='PUBLIC'";
			
			ResultSet tables = executeSQLQuery(query);
			Object[][] items = dump(tables);
			
			Set<String> knownTables = new HashSet<String>();
			
			for(Record<?> tmp : new CSVRecordLoader().getPlugins())
			{
				knownTables.add(tmp.getTableName());
			}
			
			int unknown = 0;
			
			for(Object[] item : items)
			{
				if(! knownTables.contains(item[0].toString()))
				{
					unknown++;
					String dropQuery = "DROP TABLE \"" + item[0].toString() + "\"";
					executeSQLResult(dropQuery);
				}
			}		
			
			return unknown;
		}catch(SQLException e)
		{
			m_logger.error("Error dropping unused tables",e);
			return -1;
		}
	}
	
	/**
	 * Loads the database from a file, or the web if none is found locally.
	 */
	private void loadDatabase()
	{
		try
		{
			try
			{
				// Check to make sure the Bmod Database is there, if not, try 
				// downloading from the website.
				if(!Files.exists(m_databaseFile))
				{
					Files.createDirectories(m_databaseFile.getParent());
				
					byte[] file = ExtensionPoints.readURLAsBytes("http://" + Constants.API_HOST + HSQL_DATABASE_PATH);
					if(file.length != 0)
					{
						Files.deleteIfExists(m_databaseFile);
						Files.write(m_databaseFile, file, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
					}
				}
				
				// make sure we have the proper scriptformat.
				setScriptFormatCorrectly();
			}catch(NullPointerException|IOException ex)
			{
				m_logger.error("Could not fetch database from web.", ex);
			}
			
	
			
			Class.forName("org.hsqldb.jdbcDriver");
		
			
			String db = ExtensionPoints.getBmodDirectory("database").toUri().getPath();
			connection = DriverManager.getConnection("jdbc:hsqldb:file:" + db, "sa", "");
			
			// Setup Connection
			CSVRecordLoader ldr = new CSVRecordLoader();
		
			// Load all of the building independent plugins
			for(Record<?> cp : ldr.getRecordPluginManagers())
			{
				m_logger.debug("Creating table: " + cp.getTableName());
				// Create a new table
				try
				{
					getPreparedStatement(cp.getSQLTableDefn()).execute();
				} catch(SQLException ex)
				{
					// Table exists
				}
				
				try
				{
					m_logger.debug("Doing index:" + cp.getIndexDefn());
					if(cp.getIndexDefn() != null)
						getPreparedStatement(cp.getIndexDefn()).execute();
				}catch(SQLException ex)
				{	// index exists
				}
				
			}
			

		} catch (Exception ex)
		{
			ex.printStackTrace();
			if(ex instanceof DatabaseIntegrityException)
				Database.handleCriticalError((DatabaseIntegrityException)ex);
			else
				Database.handleCriticalError(new DatabaseIntegrityException(ex));
		}
		
	}
	
	/**
	 * Sets the script format to text or compressed so the HSQLDB can read it,
	 * being they didn't just check the db when loaded...will allow us to 
	 * transition to gzipped databases easily.
	 */
	private void setScriptFormatCorrectly()
	{
		int scriptFormatNeeded = 0;
		if(isMime(m_databaseFile, GZIP_MAGIC_NUMBERS))
		{
			m_logger.debug("Database is gzipped");
			scriptFormatNeeded = 3;
		}
		
		boolean propsExists = Files.exists(m_databasePropertiesFile);
		
		Properties properties = new Properties();
		try {
		    properties.load(new FileInputStream(m_databasePropertiesFile.toFile()));
		} catch (IOException e) {
		}
		
		properties.setProperty("hsqldb.script_format", "" + scriptFormatNeeded);
		if(! propsExists) // write other info
		{
			properties.setProperty("version","2.2.8");
			properties.setProperty("modified", "yes");
		}
		
		// Write properties file.
		try {
		    properties.store(new FileOutputStream(m_databasePropertiesFile.toFile()), null);
		} catch (IOException e) {
		}
		
	}
	
	/**
	 * Is a magic number?
	 */
	public static boolean isMime(Path f, byte[] magicNumbers)
	{
		try
		{
			byte[] toTest = Files.readAllBytes(f);
			
			// If smaller file, obviously won't match.
			if(toTest.length < magicNumbers.length) {
				return false;
			}
			
			for(int i = 0; i < magicNumbers.length; i++)
			{
				if(toTest[i] != magicNumbers[i])
				{
					return false;
				}
			}
			
			return true;
			
		} catch (IOException e)
		{
			return false;
		}
	}
	
	
	/**
	 * Creates an INSERT statement for the given record.
	 * 
	 * @param record - the record to create
	 * @return a PreparedStatement already prepared with the information needed.
	 * @throws SQLException - If the preparedStatement cannot be generated
	 */
	public PreparedStatement genersateStatement(Record<?> record) throws SQLException
	{
		final Object[] representations = record.toSQL();
		
		// Generate the statement
		String query = "INSERT INTO \"" + record.getTableName() + "\" VALUES (";
		for(int i = 0; i < representations.length; i++)
		{
			if(i != 0)
			{
				query += ",";
			}
			query += "?";
		}
		
		query += ")";
		
		
		// Get and fill the statement
		PreparedStatement ps = getPreparedStatement(query);
		for(int i = 0; i < representations.length; i++)
		{
			ps.setObject(i + 1, representations[i]);
		}
		
		return ps;
	}
}