package html_client_tests;

import static org.junit.Assert.*;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.junit.Test;

import html_client.HttpHeader;

public class HttpHeaderTests {
	@Test
	public void testHtmlHeader() {
		HttpHeader header1 = new HttpHeader("");
		assertEquals(header1.get_Etag(), null);
		assertEquals(header1.get_StatusCode(), -1);
		assertEquals(header1.get_FileType(), null);
		assertEquals(header1.get_LastModified(), new Long(0));
		
		SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss");
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		long time;
		
		try{
			time = format.parse("Thu, 18 Sep 2014 22:24:31 GMT").getTime();
		}catch (Exception ex)
		{
			time = 0;
		}
		
		//actual http header from a get request
		HttpHeader header2 = new HttpHeader("HTTP/1.1 200 OK\r\nDate: Fri, 14 Oct 2016 01:40:12 GMT\r\nServer: Apache/2.0.52 (Red Hat)\r\nLast-Modified: Thu, 18 Sep 2014 22:24:31 GMT\r\nETag: 210027a-1706-72c6e5c0\r\nAccept-Ranges: bytes\r\nContent-Length: 5894\r\nConnection: close\r\nContent-Type: text/html\r\n");
		assertEquals(header2.get_Etag(), "210027a-1706-72c6e5c0");
		assertEquals(header2.get_StatusCode(), 200);
		assertEquals(header2.get_FileType(), "text/html");
		assertEquals(header2.get_LastModified(), new Long(time));
	
		//another http header missing LastModified and FileType
		HttpHeader header3 = new HttpHeader("HTTP/1.1 304 Not Modified\r\nDate: Fri, 14 Oct 2016 01:40:12 GMT\r\nServer: Apache/2.0.52 (Red Hat)\r\nETag: 560027a-5812-72c6f5a1\r\n");
		assertEquals(header3.get_Etag(), "560027a-5812-72c6f5a1");
		assertEquals(header3.get_StatusCode(), 304);
		assertEquals(header3.get_FileType(), null);
		assertEquals(header3.get_LastModified(), new Long(0));
	}

}
