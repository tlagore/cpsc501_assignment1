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
		
	private int _HttpStatusCode;
	private Calendar _LastModified;
	private String _HttpFileType;
	private String _HttpEtag;
	
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
		Socket socket;
		InputStream inputStream;
		PrintWriter outputStream;
		String headerInfo = "", 
			command,
			host = getHostnameFromUrl(url),
			objectPath = getObjectPathFromUrl(url);
		int amountRead;
		host = host.indexOf(":") == -1 ? host : host.substring(0, host.indexOf(":"));
		String standardizedUrl = host + objectPath;
		
			
		//get GET or conditional GET command
		command = getHttpGetCommand(url);
		
		try
		{				
			socket = getSocket(url);
			outputStream = new PrintWriter(socket.getOutputStream());
			inputStream = socket.getInputStream();

			//write command to socket outputstream
			outputStream.print(command);
			outputStream.print("Host: " + host + "\r\n");
			outputStream.print("\r\n");
			outputStream.flush();
			
			amountRead = inputStream.read(input);
			
			//if amountRead is not -1 or 0, there is a response
			if(amountRead != -1 && amountRead != 0)
			{
				headerInfo = extractHeaderInfo(input);
				extractHeaderContents(headerInfo);
				
				if (_HttpStatusCode == 200)
				{
					File file = createDirectoryAndFile(standardizedUrl);
					FileOutputStream fileOut = new FileOutputStream(file, true);
					fileOut.write(input);
					
					//read/write rest of data to file
					while((amountRead = inputStream.read(input)) != -1)
					{
						fileOut.write(input);
					}
					
					fileOut.close();
					_Catalog.put(standardizedUrl, _LastModified == null ? 0 : _LastModified.getTimeInMillis());
					writeCatalog();
				}else if (_HttpStatusCode == 304)
				{
					System.out.println("Http: " + _HttpStatusCode + ". Same or newer file stored in cache.");
				}else
				{
					//some other Http code
					System.out.println("Error. Http status code: " + _HttpStatusCode + ".");
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
	
	private void extractHeaderContents(String headerText)
	{
		//set all to null, if any information is missing from header, value will be null
				_HttpStatusCode = -1;
				_HttpFileType = null;
				_LastModified = null;
				_HttpEtag = null;
				
				String[] lines = headerText.split(System.getProperty("line.separator"));
				for(int i = 0; i < lines.length; i++)
				{
					//HTTP status message is in the form HTTP/1.1 XXX Readable Message
					//By removing "HTTP/1.1 " then taking the next 3 characters and converting it to an int, we can obtain the status code
					if (lines[i].contains("HTTP/1.1"))
						_HttpStatusCode = Integer.parseInt(lines[i].replace("HTTP/1.1 ","").substring(0,3));	
					
					if(lines[i].contains("Content-Type: "))
						_HttpFileType = lines[i].replace("Content-Type: ", "");
					
					if(lines[i].contains("ETag: "))
						_HttpEtag = lines[i].replace("ETag: ", "");	
					
					if(lines[i].contains("Last-Modified: "))
					{
						try{
							SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
							format.setTimeZone(TimeZone.getTimeZone("GMT"));
							String dateModified = lines[i].replaceAll("Last-Modified: ", "");
							
							_LastModified = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
							_LastModified.setTime(format.parse(dateModified));
						}catch(ParseException ex)
						{
							_LastModified.setTimeInMillis(0);
						}
					}
				}
	}
	
	/**
	 * Lists the contents of the Catalog to screen.
	 */
	public void listCatalogContents()
	{
		
	}
	
	/**
	 * 
	 * Receives the unmodified url in the form host:port/location/of/file and opens a socket
	 * to the specified host on the specified port.  If no port is specified, a default port of 80 is 
	 * used.
	 * 
	 * @param url the unmodified url of the request
	 * @return the created Socket if successful
	 * @throws IOException If the socket cannot be created due to a malformed url or bad connection, an exception will be thrown.
	 */
	private Socket getSocket(String url) throws IOException
	{
		Socket socket = null;
		String host = getHostnameFromUrl(url);
		int indexOfColon = host.indexOf(":");
		int port;
		
		//tries to parse the port number from the url, otherwise sets it to -1
		try{
			port = Integer.parseInt(indexOfColon == -1 ? "" : host.substring(indexOfColon));
		}catch(Exception ex)
		{
			port = -1;
		}
		
		host = indexOfColon == -1 ? host : host.substring(0, indexOfColon);
			
		//if port = -1, then no port was specified or was in bad format. Default port is used.
		try{
			socket = new Socket(host, port == -1 ? DEFAULT_HTTP_PORT : port);
		}catch(IOException ex)
		{
			throw ex;
		}
		
		return socket;
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
