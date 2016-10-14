package html_client;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;


/**
 * UrlCache Class
 * 
 * @author 	Tyrone Lagore
 * @version	1.2, October 5, 2016
 *
 */
public class UrlCache {
	private HashMap<String, Long> _Catalog;
	private final int DEFAULT_HTTP_PORT = 80;
	private final String CACHE_DIR = System.getProperty("user.dir") + "\\cache\\";
	private final TimeZone _SystemTimeZone = TimeZone.getDefault();
	
    /**
     * Default constructor to initialize data structures used for caching/etc
	 * If the cache already exists then load it. If any errors then throw exception.
	 *
     * @throws UrlCacheException if encounters any errors/exceptions
     */
	@SuppressWarnings("unchecked")
	public UrlCache() throws UrlCacheException {
		String catalogDir = CACHE_DIR + "catalog";
		Path path = Paths.get(catalogDir);
		
		System.out.println("Checking if catalog exists...");
		if(Files.exists(path))
		{
			//file exists, read in catalog
			System.out.println("Catalog exists, reading in cache catalog.");
			try{
				FileInputStream fileIn = new FileInputStream(catalogDir);
				ObjectInputStream objIn = new ObjectInputStream(fileIn);
				
				_Catalog = (HashMap<String, Long>)objIn.readObject();
				
				objIn.close();
				fileIn.close();
				System.out.println("Catalog successfully read. Ready to proceed.");
			}catch(FileNotFoundException ex)
			{
				//file not found, shouldn't happen, we just checked if it existed.
				System.out.println("Error reading catalog.  Error:" + ex.getMessage() + ". Creating empty catalog.");
				_Catalog = new HashMap<String, Long>();
			}catch(IOException ex)
			{
				//IOException opening input stream
				System.out.println("Error reading catalog. Error:" + ex.getMessage() + ". Creating empty catalog.");
				_Catalog = new HashMap<String, Long>();
			}catch(ClassNotFoundException ex)
			{
				//ClassNotFoundException, shouldn't happen
				System.out.println("Error reading catalog. Error:" + ex.getMessage() + ". Creating empty catalog.");
				_Catalog = new HashMap<String, Long>();
			}
		}else
		{
			_Catalog = new HashMap<String, Long>();
			//create new catalog
		}
		
	}
	
    /**
     * Downloads the object specified by the parameter url if the local copy is out of date.
	 *
     * @param url	URL of the object to be downloaded. It is a fully qualified URL.
     * @throws UrlCacheException if encounters any errors/exceptions
     */
	public void getObject(String url) throws UrlCacheException {
		byte[] input = new byte[10*1024];
		UrlConnection connection = new UrlConnection(url);
		Socket socket;
		HttpHeader header;
		InputStream inputStream;
		PrintWriter outputStream;
		String headerInfo = "", 
			command;
		
		int amountRead;

		//get GET or conditional GET command
		command = getHttpGetCommand(url);
		
		try
		{				
			connection.initializeSocket();
			socket = connection.get_Socket();
			outputStream = new PrintWriter(socket.getOutputStream());
			inputStream = socket.getInputStream();

			//write command to socket outputstream
			outputStream.print(command);
			outputStream.print("Host: " + connection.get_Host() + "\r\n");
			outputStream.print("\r\n");
			outputStream.flush();
			
			amountRead = inputStream.read(input);
			
			//if amountRead is not -1 or 0, there is a response
			if(amountRead != -1 && amountRead != 0)
			{
				headerInfo = extractHeaderInfo(input);
				header = new HttpHeader(headerInfo);
				
				if (header.get_StatusCode() == 200)
				{
					File file = createDirectoryAndFile(connection.get_StandardizedUrl());
					FileOutputStream fileOut = new FileOutputStream(file, true);
					fileOut.write(input);
					
					//read/write rest of data to file
					while((amountRead = inputStream.read(input)) != -1)
					{
						fileOut.write(input);
					}
					
					fileOut.close();
					_Catalog.put(connection.get_StandardizedUrl(), header.get_LastModified() == null ? 0 : header.get_LastModified());
					writeCatalog();
				}else if (header.get_StatusCode()  == 304)
				{
					System.out.println("Http: " + header.get_StatusCode()  + ". Same or newer file stored in cache.");
				}else
				{
					//some other Http code
					System.out.println("Error. Http status code: " + header.get_StatusCode()  + ".");
				}
			}else
			{
				System.out.println("No data read from response.");
			}
		
			outputStream.close();
			socket.close();
		}catch(Exception ex)
		{
			System.out.println("Error: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	/**
	 * Receives a URL and checks if it exists in the cache.  If it does, it returns
	 * a GET command with an if-modified-since conditional.  If it does not exist, a regular
	 * GET command is returned.
	 * 
	 * @param url The url of the get command in question
	 * @return a regular GET command if the object is not in cache, or a GET if-modified-since if it is present
	 */
	private String getHttpGetCommand(String url) {
		String command;
		Long lastModified;
		
		//if we have the item cached, conditional get, else regular 
		try{
			lastModified = getLastModified(url);
			
			//Format properly date properly for command
			SimpleDateFormat dateFormat = new SimpleDateFormat(
			        "EEE, dd MMM yyyy HH:mm:ss zzz");
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date dDate = new Date(lastModified);
			command = "GET " + getObjectPathFromUrl(url) + " HTTP/1.1\r\nIf-Modified-Since: " + dateFormat.format(dDate) + "\r\n";
		}catch(UrlCacheException ex)
		{
			System.out.println(ex.getMessage());
			command = "GET " + getObjectPathFromUrl(url) + " HTTP/1.1\r\n";
		}
		return command;
	}
	
	
	/**
	 * extractHeaderInfo takes in an HTTP get response in a byte array format and
	 * extracts the header information.
	 * 
	 * It is assumed that the response is in correct format (Ie, header at the beginning of the byte
	 * array, ended by the presence of a blank line)
	 * 
	 * @param data The http get response in byte format
	 * @return returns the header information in string format
	 */
	private String extractHeaderInfo(byte[] data)
	{
		String header = "";
		String line;
		InputStream inputStream = new ByteArrayInputStream(data);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		int dataLength = data.length;
		
		try
		{
			line = reader.readLine();
			while (line != null && !line.isEmpty())
			{
				header += line + "\r\n";
				line  = reader.readLine();
			}
			
			// add 2 to account for \r\n line that was not read in
			int bytesInHeader = header.getBytes("UTF-8").length + 2;
			
			for (int i = 0; i < (dataLength - bytesInHeader); i++)
			{
				data[i] = data[i + bytesInHeader];
			}
			
			for (int i = (dataLength - bytesInHeader); i < dataLength; i++)
			{
				data[i] = 0;
			}
		}catch(IOException ex)
		{
			System.out.println("Error: " + ex.getMessage());
		}
		
		
		return header;
	}
	
	/**
	 * Takes the standardized url (url minus https:// or http://) and creates the file and directory for it
	 * 
	 * @param urlPath the full object path of the item on the server
	 * @return returns the File that was created for the url
	 */
	private File createDirectoryAndFile(String urlPath)
	{
		String dirPath = CACHE_DIR + urlPath;
		File file = null;
		try{
			file = new File(dirPath);
			file.delete();
			file.getParentFile().mkdirs();
			file.createNewFile();
		}catch(Exception ex)
		{
			System.out.println("Error creating file directory.  Error:" + ex.getMessage() + ". File not saved.");
		}
		
		return file;
	}
	
    /**
     * Returns the Last-Modified time associated with the object specified by the parameter url.
	 *
     * @param url 	URL of the object 
	 * @return the Last-Modified time in millisecond as in Date.getTime()
     * @throws UrlCacheException if the specified url is not in the cache, or there are other errors/exceptions
     */
	public long getLastModified(String url) throws UrlCacheException {
		String standardizedUrl = getHostnameFromUrl(url) + getObjectPathFromUrl(url);
		if(_Catalog.containsKey(standardizedUrl))
			return _Catalog.get(standardizedUrl);
		else
			throw new UrlCacheException("URL not in cache.");
	}
	
	/**
	 * Writes the current contents of the catalog to file using ObjectOutputStream.
	 */
	public void writeCatalog()
	{
		Path path = Paths.get(CACHE_DIR);
		
		if(!Files.exists(path))
		{
			try{
				Files.createDirectories(path);
			}catch(IOException ex)
			{
				System.out.println("Error creating catalog directory.  Error:" + ex.getMessage() + ". Reopening this application will create new catalog.");
			}
		}
		
		try{
			FileOutputStream fileOut = new FileOutputStream(CACHE_DIR + "\\catalog");
			ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
			
			objOut.writeObject(_Catalog);
			
			objOut.close();
			fileOut.close();
		}catch(IOException ex)
		{
			System.out.println("Error writing catalog to file.  Error:" + ex.getMessage() + ". Reopening this application will create new catalog.");
		}
	}
	
	/**
	 * Given a full url, it returns the host name attached with the port number
	 * 
	 * @param url A url from which the host must be derived
	 * @return host name of the url with attached port number in the form host:port
	 */
	private String getHostnameFromUrl(String url)
	{
		url = url.toLowerCase();
		url = url.replace("https://", "").replace("http://", "");
		url = url.substring(0, url.indexOf("/") == -1 ? url.length() - 1 : url.indexOf("/"));
		return url;
	}
	
	/**
	 * Given a full url, it returns the object path of the url
	 * 
	 * @param url a url from which the object path must be derived
	 * @return the object path of the url in the form /location/to/object.obj
	 */
	private String getObjectPathFromUrl(String url)
	{
		url = url.toLowerCase();
		url = url.replace("https://", "").replace("http://", "");
		return url.indexOf("/") == -1 ? "" : url.substring(url.indexOf("/"), url.length());
	}
}
