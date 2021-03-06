package dw.xmlrpc.utest;

import static org.junit.Assert.*;

import java.util.Date;

import dw.xmlrpc.PageInfo;

public class Test_PageInfo {
	@org.junit.Test(expected=IllegalArgumentException.class)
	public void ShouldntAcceptToBuildWithNullId(){
		new PageInfo(null, new Date(), "author", 123456789);
	}

	@org.junit.Test
	public void toStringShouldntThrowNullRef(){
		PageInfo page = new PageInfo("id", null, null, null);
		assertNotNull(page.toString());
	}
}