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
		url = url.toLowerCase();
		url = url.replace("https://", "").replace("http://", "");
		url = url.substring(0, url.indexOf("/") == -1 ? url.length() - 1 : url.indexOf("/"));	
		int indexOfColon = url.indexOf(":");
		
		try{
			port = url.substring(indexOfColon + 1);
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
		url = url.toLowerCase();
		url = url.replace("https://", "").replace("http://", "");
		url = url.substring(0, url.indexOf("/") == -1 ? url.length() - 1 : url.indexOf("/"));		
		url = url.indexOf(":") == -1 ? url : url.substring(0, url.indexOf(":"));
		_Host = url;
	}
	
	/**
	 * Given a full url, it sets the object path of the url
	 *
	 * @param url a url from which the object path must be derived,  Expected in the format domain:[port]/object/path
	 */
	private void getObjectPathFromUrl(String url)
	{
		url = url.toLowerCase();
		url = url.replace("https://", "").replace("http://", "");
		_ObjectPath = url.indexOf("/") == -1 ? "" : url.substring(url.indexOf("/"), url.length());
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
