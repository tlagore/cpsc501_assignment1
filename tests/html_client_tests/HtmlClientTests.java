package html_client_tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.junit.Test;

import html_client.HttpHeader;
import html_client.UrlCache;
import html_client.UrlCacheException;

public class HtmlClientTests {
	private final String CACHE_DIR = System.getProperty("user.dir") + "\\cache\\";
	
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
	
	@Test
	public void testGetObject(){
		//NOTE: running this test will create a file directory under the working directory
		String url = "people.ucalgary.ca/~mghaderi/index.html";
		String url2 = "this.should.fail/stuff/index.html";
		
		Path path = Paths.get(CACHE_DIR + url);
		Path path2 = Paths.get(CACHE_DIR + url2);
		
		try {
			UrlCache cache = new UrlCache();
			cache.getObject(url);
			assertTrue(Files.exists(path));
			
			cache.getObject(url2);
			assertTrue(!Files.exists(path2));
		}catch(UrlCacheException ex)
		{
			//Shouldn't get here
			fail("Caught UrlCacheException");
		}
	}
}
