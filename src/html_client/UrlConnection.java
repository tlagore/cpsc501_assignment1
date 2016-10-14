package html_client;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class UrlConnection {
	private Socket _Socket;
	private String _OriginalUrl;
	private String _Host;
	private String _ObjectPath;
	private int _Port;
	private final int DEFAULT_HTTP_PORT = 80;
	
	public UrlConnection(String url)
	{
		//not used, but maintained for future use if necessary
		_OriginalUrl = url;
		getPortFromUrl(url);
		getHostnameFromUrl(url);
		getObjectPathFromUrl(url);
		_Socket = null;
	}
	
	/**
	 * Attempts to initialize the socket for the HttpRequest.  Returns true if socket was successful
	 * @throws IOException if an IOException occurs
	 * @throws UnkownHostException If the domain cannot be reached
	 * @throws IllegalArgumentException If socket information is bad (0-65535)
	 */
	public void initializeSocket() throws IOException, UnknownHostException, IllegalArgumentException
	{
		try{
			_Socket = new Socket(_Host, _Port);
		}catch(Exception ex)
		{
			throw ex;
		}
	}
	
	/**
	 * Given a full url, it sets the specified port if it exists, or a default of 80 
	 * 
	 * @param url A url from which the port must be derived, Expected in the format domain:[port]/object/path
	 */
	private void getPortFromUrl(String url)
	{
		String port;
		String standardizedDomain = standardizeDomain(url);	
		
		int indexOfColon = standardizedDomain.indexOf(":");
		
		try{
			port = standardizedDomain.substring(indexOfColon + 1);
			_Port = Integer.parseInt(port);
		}catch(Exception ex)
		{
			//failed to parseInt, bad port format for url, default to 80
			_Port = DEFAULT_HTTP_PORT;
		}
	}
	
	/**
	 * Given a full url, it sets the host name
	 * 
	 *  Expected in the format domain:[port]/object/path
	 * @param url A url from which the host must be derived, Expected in the format domain:[port]/object/path
	 */
	private void getHostnameFromUrl(String url)
	{		
		String standardizedDomain = standardizeDomain(url);	
		
		//remove port if it exists
		standardizedDomain = standardizedDomain.indexOf(":") == -1 ? standardizedDomain : standardizedDomain.substring(0, standardizedDomain.indexOf(":"));
		_Host = standardizedDomain;
	}
	
	/**
	 * Standardizes a url to just the domain information, ie, domain:(port)
	 * @param url The url from which the standardized domain must be derived
	 * @return url stripped of http, https, sent to lower case, and object path removed (if it exists)
	 */
	private String standardizeDomain(String url)
	{
		String standardizedUrl = standardizeUrl(url);
		return standardizedUrl.substring(0, standardizedUrl.indexOf("/") == -1 ? standardizedUrl.length() - 1 : standardizedUrl.indexOf("/"));		
	}
	
	/**
	 * Given a full url, it sets the object path of the url
	 *
	 * @param url a url from which the object path must be derived,  Expected in the format domain:[port]/object/path
	 */
	private void getObjectPathFromUrl(String url)
	{
		String standardizedUrl = standardizeUrl(url);
		_ObjectPath = standardizedUrl.indexOf("/") == -1 ? "" : standardizedUrl.substring(standardizedUrl.indexOf("/"), standardizedUrl.length());
	}
	
	/**
	 * Standardizes a url to remove http, https and ensure lower case
	 * @param url The url that needs to be standardized in the form http(s)://domain:(port)/object/path
	 * @return url stripped of http, https, and sent to lower case
	 */
	private String standardizeUrl(String url)
	{
		String standardizedUrl = url.toLowerCase();
		standardizedUrl = standardizedUrl.replace("https://", "").replace("http://", "");
		return standardizedUrl;		
	}
	
	/**
	 * Attempts to close the socket
	 * @throws IOException if an I/O error occurs when closing this socket.
	 */
	public void closeSocket() throws IOException
	{
		try{
			_Socket.close();
		}catch(IOException ex)
		{
			throw ex;
		}
	}
	
	public String get_OriginalUrl() {
		return _OriginalUrl;
	}

	public Socket get_Socket() {
		return _Socket;
	}
	public String get_Host() {
		return _Host;
	}
	public String get_ObjectPath() {
		return _ObjectPath;
	}
	public int get_Port() {
		return _Port;
	}
	
	public String get_StandardizedUrl(){
		return _Host + _ObjectPath;
	}
	
	
	
}
