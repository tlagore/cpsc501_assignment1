package html_client_tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.junit.Test;

import html_client.HttpHeader;
import html_client.UrlCache;
import html_client.UrlCacheException;
import html_client.UrlConnection;

public class UrlCacheTests {
	private final String CACHE_DIR = System.getProperty("user.dir") + "\\cache\\";
	
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
	
	@Test
	public void testwriteCommandToSocket()
	{
		String url = "people.ucalgary.ca/~mghaderi/test/uc.gif";
		Path path = Paths.get(CACHE_DIR + url);
		
		try{
		UrlCache cache = new UrlCache();
		UrlConnection connection = new UrlConnection(url);
		connection.initializeSocket();
		
		String command = "GET " + connection.get_ObjectPath() + " HTTP/1.1\r\n";
		PrintWriter outputStream = new PrintWriter(connection.get_Socket().getOutputStream());
		
		cache.writeCommandToSocket(connection, outputStream, command);
		cache.readSocketResponse(connection);
		
		assertTrue(Files.exists(path));
		
		outputStream.close();
		connection.closeSocket();		
		}catch(Exception ex)
		{
			fail();
		}
	}
	
	@Test
	public void testExtractHeaderInfo() {
		try{
			UrlCache cache = new UrlCache();
			
			//Test 1 
			String str = "HTTP/1.1 200 OK\r\nLast-Modified:SomeDate\r\nAnother field: something\r\n\r\nBeginning of data";
			String header;
			byte[] arr1 = str.getBytes();
			
			//get what should be header info
			header = cache.extractHeaderInfo(arr1);
			
			//assert that it's as we suspect
			assertEquals(header, "HTTP/1.1 200 OK\r\nLast-Modified:SomeDate\r\nAnother field: something\r\n");
			
			byte[] remainingInfo = "Beginning of data".getBytes();
			//assert that every byte of the beginning of the original array is as expected.
			for(int i = 0; i < remainingInfo.length; i++)
			{
				assertEquals(remainingInfo[i], arr1[i]);
			}
			
			//Test 2
			String str2 = "HTTP/1.1 304 NOT MODIFIED\r\nLast-Modified:SomeDate\r\n\r\nEarly end Another field: something\r\n\r\nBeginning of data";
			String header2;
			byte[] arr2 = str2.getBytes();
			
			//get what should be header info
			header2 = cache.extractHeaderInfo(arr2);
			
			//assert that it's as we suspect
			assertEquals(header2, "HTTP/1.1 304 NOT MODIFIED\r\nLast-Modified:SomeDate\r\n");
			
			byte[] remainingInfo2 = "Early end Another field: something\r\n\r\nBeginning of data".getBytes();
			//assert that every byte of the beginning of the original array is as expected.
			for(int i = 0; i < remainingInfo2.length; i++)
			{
				assertEquals(remainingInfo2[i], arr2[i]);
			}
		}catch(Exception ex)
		{
			fail();
		}
	}

	@Test
	public void testExtractHeaderStringFromByteArray() {
		try{
			UrlCache cache = new UrlCache();
			String str = "HTTP/1.1 200 OK\r\nLast-Modified:SomeDate\r\nAnother field: something\r\n\r\nBeginning of data";
			String header;
			byte[] arr1 = str.getBytes();
			byte[] arr2 = str.getBytes();
			
			//get what should be header info
			header = cache.extractHeaderStringFromByteArray(arr1);
			//assert that it's as we suspect
			assertEquals(header, "HTTP/1.1 200 OK\r\nLast-Modified:SomeDate\r\nAnother field: something\r\n");
			
			//byte array unchanged
			for(int i = 0; i < arr1.length; i++)
				assertEquals(arr1[i], arr2[i]);
			
		}catch(Exception ex)
		{
			fail();
		}
	}

	@Test
	public void testShiftByteArrayContents() {
		try{
			UrlCache cache = new UrlCache();
			byte[] arr1 = new byte[] {'a','b','c','d','e','f','g'};
			
			cache.shiftByteArrayContents(arr1, 3);
			
			assertEquals(arr1[0],'d');
			assertEquals(arr1[1],'e');
			assertEquals(arr1[2],'f');
			assertEquals(arr1[3],'g');
			assertEquals(arr1[4], 0);
			assertEquals(arr1[5], 0);
			
			//too many bytes
			byte arr2[] = new byte[] {'a','b','c','d','e' };
			
			cache.shiftByteArrayContents(arr2, 50);
			
			assertEquals(arr2[0], 0);
			assertEquals(arr2[1], 0);
			assertEquals(arr2[2], 0);
			assertEquals(arr2[3], 0);
			assertEquals(arr2[4], 0);

//			for(int i = 0; i < remainingInfo2.length; i++)
//			{
//				assertEquals(remainingInfo2[i], arr2[i]);
//			}
		}catch(Exception ex)
		{
			fail();
		}
	}
}
