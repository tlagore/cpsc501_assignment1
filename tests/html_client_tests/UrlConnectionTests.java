package html_client_tests;

import static org.junit.Assert.*;

import java.net.UnknownHostException;

import html_client.UrlConnection;

import org.junit.Test;

public class UrlConnectionTests {

	@Test
	public void testUrlRequest() {
		UrlConnection request = new UrlConnection("https://www.hello.com:40/object/path.html");
		UrlConnection request2 = new UrlConnection("http://my.domain.co/no/port.gif");
		UrlConnection request3 = new UrlConnection("another.domain/obj.php");
		UrlConnection request4 = new UrlConnection("no.object.path");
		
		assertEquals(request.get_Host(), "www.hello.com");
		assertEquals(request.get_ObjectPath(), "/object/path.html");
		assertEquals(request.get_Port(), 40);
		
		assertEquals(request2.get_Host(), "my.domain.co");
		assertEquals(request2.get_ObjectPath(), "/no/port.gif");
		assertEquals(request2.get_Port(), 80);
		
		assertEquals(request3.get_Host(), "another.domain");
		assertEquals(request3.get_ObjectPath(), "/obj.php");
		assertEquals(request3.get_Port(), 80);
		
		assertEquals(request4.get_Host(), "no.object.path");
		assertEquals(request4.get_ObjectPath(), "");
		assertEquals(request4.get_Port(), 80);
		
		
	}
	
	@Test
	public void testInitializeSocket()
	{
		UrlConnection request = new UrlConnection("https://www.hello.com:80/object/path.html");
		try
		{
			//www.hello.com is a good domain and port 80 is default http port
			request.initializeSocket();
		}catch(Exception ex)
		{
			fail();
		}
	}

	@Test(expected = UnknownHostException.class)
	public void testInitializeSocketBadDomain() throws Exception 
	{
		UrlConnection request2 = new UrlConnection("https://this.is.a.bad.domain.i.think/html.html");
		try
		{
			request2.initializeSocket();
		}catch(Exception ex)
		{
			throw ex;
		}
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testInitializeSocketBadPort() throws Exception
	{
		UrlConnection request3 = new UrlConnection("https://www.google.ca:67000/bad_port_no.html");
		try
		{
			request3.initializeSocket();
		}catch(Exception ex)
		{
			throw ex;
		}
	}
}
