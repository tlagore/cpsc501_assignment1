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
import java.io.UnsupportedEncodingException;
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
	private final String CACHE_DIR = System.getProperty("user.dir") + "\\cache\\";

	private final int MAX_BYTES = 10*1024;
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
		UrlConnection connection = new UrlConnection(url);
		PrintWriter outputStream;
		String command;
		
		//get GET or conditional GET command
		command = getHttpGetCommand(connection);
		
		try
		{				
			connection.initializeSocket();
			outputStream = new PrintWriter(connection.get_Socket().getOutputStream());
			
			writeCommandToSocket(connection, outputStream, command);
			readSocketResponse(connection);

			outputStream.close();
			connection.closeSocket();
		}catch(Exception ex)
		{
			System.out.println("Error: " + ex.getMessage());
			ex.printStackTrace();
		}
	}
	
	/**
	 * Reads in the first 10240 bytes of the response of a socket who has assumed to have been written to.
	 * Extracts the header information and analyzes it to determine next steps.
	 * 
	 * @param connection The connection that maintains the socket that was written to
	 * @throws Exception IOException or FileNotFoundException if writing an object. Should not get FileNotFoundException as File is created.
	 */
	public void readSocketResponse(UrlConnection connection) throws Exception
	{
		byte[] input = new byte[MAX_BYTES];
		Socket socket = connection.get_Socket();
		String headerInfo = "";
		InputStream inputStream;
		int amountRead;
		HttpHeader header;
		
		try{
			inputStream = socket.getInputStream();
			amountRead = inputStream.read(input);	
			//if amountRead is not -1 or 0, there is a response
			if(amountRead != -1 && amountRead != 0)
			{
				headerInfo = extractHeaderInfo(input);
				header = new HttpHeader(headerInfo);
				
				if (header.get_StatusCode() == 200)
				{
					writeObject(connection, header, input, inputStream);
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
		}catch(Exception ex)
		{
			throw ex;
		}
	}
	
	/**
	 * Takes a portion of a read in object and writes it to file. Attempts to check if there is more unread data
	 * on the inputStream and appends it to the end of the file that is written.
	 * 
	 * @param connection the connection where the object was retrieved from
	 * @param header the Header information of the get request
	 * @param input the portion of the object that was read
	 * @param inputStream the input stream of the socket that the request was read from
	 * @throws Exception Possibly IOException or FileNotFound exception.  FileNotFound should not be thrown because file is created.
	 */
	public void writeObject(UrlConnection connection, HttpHeader header, byte[] input, InputStream inputStream) throws Exception
	{
		int amountRead;	
		File file = createDirectoryAndFile(connection.get_StandardizedUrl());
		
		try{
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
		}catch(Exception ex)
		{
			throw ex;
		}
	}
	
	/**
	 * Takes a command a UrlConnection and an outputStream and writes the command
	 * 
	 * @param connection the UrlConnection of the socket writing to
	 * @param outputStream the Output stream of the socket writing to
	 * @param command the Http command for the socket minus the host and new line to end the command
	 */
	public void writeCommandToSocket(UrlConnection connection, PrintWriter outputStream, String command)
	{
			//write command to socket outputstream
			outputStream.print(command);
			outputStream.print("Host: " + connection.get_Host() + "\r\n");
			outputStream.print("\r\n");
			outputStream.flush();
	}
	/**
	 * Receives a URL and checks if it exists in the cache.  If it does, it returns
	 * a GET command with an if-modified-since conditional.  If it does not exist, a regular
	 * GET command is returned.
	 * 
	 * @param url The url of the get command in question
	 * @return a regular GET command if the object is not in cache, or a GET if-modified-since if it is present
	 */
	private String getHttpGetCommand(UrlConnection connection) {
		String command;
		Long lastModified;
		
		//if we have the item cached, conditional get, else regular 
		try{
			lastModified = getLastModified(connection.get_OriginalUrl());
			
			//Format properly date properly for command
			SimpleDateFormat dateFormat = new SimpleDateFormat(
			        "EEE, dd MMM yyyy HH:mm:ss zzz");
			dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date dDate = new Date(lastModified);
			command = "GET " + connection.get_ObjectPath() + " HTTP/1.1\r\nIf-Modified-Since: " + dateFormat.format(dDate) + "\r\n";
		}catch(UrlCacheException ex)
		{
			System.out.println(ex.getMessage());
			command = "GET " + connection.get_ObjectPath() + " HTTP/1.1\r\n";
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
	public String extractHeaderInfo(byte[] data)
	{
		int bytesInHeader;
		String header = extractHeaderStringFromByteArray(data);
		try{
			//add 2 bytes to account for unread \r\n
			bytesInHeader = header.getBytes("UTF-8").length + 2;
			shiftByteArrayContents(data, bytesInHeader);
		} catch (UnsupportedEncodingException ex)
		{
			System.out.println("Error removing header from byte array: " + ex.getMessage());
		}
	
		return header;
	}
	
	/**
	 * Takes in a byte array considered to be an HTTP response to a GET command with the header at the top and extracts
	 * the header components, returning it in String format.
	 * 
	 * @param data the byte array containing the information
	 * @return The header information of the byte array
	 */
	public String extractHeaderStringFromByteArray(byte[] data)
	{
		String header = "";
		String line;
		InputStream inputStream = new ByteArrayInputStream(data);
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
		
		try
		{
			//read in until we find new line to signify end of header
			line = reader.readLine();
			while (line != null && !line.isEmpty())
			{
				header += line + "\r\n";
				line  = reader.readLine();
			}
		}catch(IOException ex)
		{
			System.out.println("Error: " + ex.getMessage());
		}
		
		return header;
	}
	
	/**
	 * Takes in a byte array and a number of bytes then shifts the contents of the array the specified number of bytes,
	 * destroying the information at the front of the array. It then sets the end "numBytes" to 0;
	 * 
	 * @param data the array that needs to have its contents shifted
	 * @param numBytes the number of bytes to shift the contents of the array
	 */
	public void shiftByteArrayContents(byte[] data, int numBytes)
	{
		//ensure supplied number of bytes does not exceed data length
		if (numBytes > data.length)
			numBytes = data.length;
			
		for (int i = 0; i < (data.length - numBytes); i++)
		{
			data[i] = data[i + numBytes];
		}
		
		for (int i = (data.length - numBytes); i < data.length; i++)
		{
			data[i] = 0;
		}
		
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
		UrlConnection connection = new UrlConnection(url);
		String standardizedUrl = connection.get_StandardizedUrl();
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
}
