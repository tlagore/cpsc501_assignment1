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
	 * @throws IOException, UnkownHostException if socket information is bad
	 */
	public void initializeSocket() throws IOException, UnknownHostException
	{
		try{
			_Socket = new Socket(_Host, _Port);
		}catch(Exception ex)
		{
			throw ex;
		}
	}
	
	
	private void getPortFromUrl(String url)
	{
		url = url.toLowerCase();
		url = url.replace("https://", "").replace("http://", "");
		url = url.substring(0, url.indexOf("/") == -1 ? url.length() - 1 : url.indexOf("/"));	
		int indexOfColon = url.indexOf(":");
		
		try{
			_Port = Integer.parseInt(indexOfColon == -1 ? "" : url.substring(indexOfColon));
		}catch(Exception ex)
		{
			//failed to parseInt, bad port format for url, default to 80
			_Port = DEFAULT_HTTP_PORT;
		}
	}
	
	/**
	 * Given a full url, it returns the host name attached with the port number
	 * 
	 * @param url A url from which the host must be derived
	 * @return host name of the url with attached port number in the form host:port
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
	 * Given a full url, it returns the object path of the url
	 * 
	 * @param url a url from which the object path must be derived
	 * @return the object path of the url in the form /location/to/object.obj
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
	
	
	
}
