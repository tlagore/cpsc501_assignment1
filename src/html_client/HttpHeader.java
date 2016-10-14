package html_client;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public class HttpHeader {
	private int _HttpStatusCode;
	private Calendar _LastModified;
	private String _HttpFileType;
	private String _HttpEtag;
	
	/**
	 * Constructor for HtmlHeader
	 * @param headerText A formated string in the format of an HttpHeader. Assumed to be in proper format.
	 */
	public HttpHeader(String headerText)
	{
		_HttpStatusCode = -1;
		_HttpFileType = null;
		_LastModified = null;
		_HttpEtag = null;
		
		parseHeaderInfo(headerText);
	}

	private void parseHeaderInfo(String headerText)
	{
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
	
	
	public int get_HttpStatusCode() {
		return _HttpStatusCode;
	}

	public Long get_LastModified() {
		return _LastModified == null ? 0 : _LastModified.getTimeInMillis();
	}

	public String get_HttpFileType() {
		return _HttpFileType;
	}

	public String get_HttpEtag() {
		return _HttpEtag;
	}
}
