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
