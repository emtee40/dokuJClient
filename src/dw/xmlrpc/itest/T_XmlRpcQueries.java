package dw.xmlrpc.itest;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dw.xmlrpc.DokuJClient;
import dw.xmlrpc.LinkInfo;
import dw.xmlrpc.Page;
import dw.xmlrpc.PageChange;
import dw.xmlrpc.PageDW;
import dw.xmlrpc.PageInfo;
import dw.xmlrpc.PageVersion;
import dw.xmlrpc.SearchResult;

public class T_XmlRpcQueries {
	private DokuJClient _client;
	private DokuJClient _clientWriter;

	@org.junit.Before
	public void setup() throws MalformedURLException {
		_client = new DokuJClient(TestParams.url, TestParams.user, TestParams.password);
		_clientWriter = new DokuJClient(TestParams.url, TestParams.writerLogin, TestParams.writerPwd);
	}

	@org.junit.Test
	public void getVersion() throws Exception {
		assertEquals(TestParams.wikiVersion, _client.getVersion());
	}

	@org.junit.Test
	public void getPageInfo() throws Exception {
		String pageId = "rev:start";
		PageInfo pageInfo = _client.getPageInfo(pageId);
		System.out.println(pageInfo.toString());

		assertEquals(pageId, pageInfo.id());
		assertEquals("lulu", pageInfo.author());
		assertEquals((Integer) 1356218419, pageInfo.version());
		assertDatesNear(2012, 11, 22, 23, 20, 19, pageInfo.modified());
	}

	@org.junit.Test
	public void getPageInfoVersion() throws Exception {
		String pageId = "rev:start";
		Integer version = 1356218411;
		PageInfo pageInfo = _client.getPageInfoVersion(pageId, version);

		assertEquals(pageId, pageInfo.id());
		assertEquals("fifi", pageInfo.author());
		assertEquals(version, pageInfo.version());
		assertDatesNear(2012, 11, 22, 23, 20, 11, pageInfo.modified());
	}

	@org.junit.Test
	public void getPageVersions() throws Exception{
		String pageId = "rev:start";
		List<PageVersion> versions = _client.getPageVersions(pageId, 0);
		PageVersion version = versions.get(0);

		assertEquals((Integer) 1356218419, version.version());
		assertEquals(pageId, version.pageId());
		assertEquals("127.0.0.1", version.ip());
		assertEquals("E", version.type());
		assertEquals("lulu", version.author());
		assertEquals("edit 2", version.summary());
		assertDatesNear(2012, 11, 22, 23, 20, 19, version.modified());
	}

	/**
	 * Assert that the actual Date is equal to the expected one,
	 * although it may differ of a few milliseconds
	 */
	private void assertDatesNear(int year, int month, int day, int hour, int minute, int second, Date actual){
		Calendar cal = Calendar.getInstance();
		cal.set(year,  month, day, hour, minute, second);
		assertTrue(Math.abs(cal.getTime().getTime() - actual.getTime()) < 1000);
	}

	@org.junit.Test
	public void getPageVersion() throws Exception {
		String pageId = "rev:start";
		assertEquals("version 1", _client.getPageVersion(pageId, 1356218400));
		assertEquals("", _client.getPageVersion(pageId, 1356218401));
		assertEquals("v2", _client.getPageVersion(pageId, 1356218411));
		assertEquals("3rd version", _client.getPageVersion(pageId, 1356218419));
	}

	@org.junit.Test
	public void getRecentChanges() throws Exception {
		List<PageChange> changes = _client.getRecentChanges(1356218401);
		assertTrue(changes.size() > 0);

		PageChange change = changes.get(0);
		assertEquals("someuser", change.author());
		assertEquals((Integer) 1356218419, change.version());
		assertEquals("rev:start", change.pageId());
		assertTrue(change.perms() != null);
		assertTrue(change.lastModified() != null);
	}

	@org.junit.Test
	public void aclCheck() throws Exception {
		assertEquals((Integer) 255, _client.aclCheck("ns1:start"));
		assertEquals((Integer) 8, _clientWriter.aclCheck("ns1:start"));
	}

	@org.junit.Test
	public void getRPCVersionSupported() throws Exception {
		assertEquals(TestParams.rpcVersionSupported, _client.getRPCVersionSupported());
	}

	@org.junit.Test
	public void getXMLRPCAPIVersion() throws Exception {
		assertEquals(TestParams.apiVersion, _client.getXMLRPCAPIVersion());
	}

	@org.junit.Test
	public void time() throws Exception {
		//We proceed as follow to avoid having to use Sleep:
		// * edit a page to update its modification time and retrieve it
		// * retrieve server time
		// * edit the page again and retrieve
		// * make sure times are consistent
		PageDW page = _client.getPagelist("singlePage").get(0);
		_client.putPage(page.id(), "text before (time test)");

		page = _client.getPagelist("singlePage").get(0);
		Integer timeBefore = page.mtime();

		Integer serverTime = _client.getTime();

		_client.putPage(page.id(), "text after (time test)");
		page = _client.getPagelist("singlePage").get(0);
		Integer timeAfter = page.mtime();

		assertTrue(0 < timeBefore);
		assertTrue(timeBefore <= serverTime);
		assertTrue(serverTime <= timeAfter);
	}

	@org.junit.Test
	public void getPagelistInAFlatNamespace() throws Exception {
		List<String> expectedPages = new ArrayList<String>();
		expectedPages.add("ns1:start");
		expectedPages.add("ns1:dummy");

		List<PageDW> actualPages = _client.getPagelist("ns1");

		assertEquals(expectedPages.size(), actualPages.size());
		for (PageDW page : actualPages) {
			assertTrue(expectedPages.contains(page.id()));
		}
	}

	@org.junit.Test
	public void getPagelistCorrectlyBuildsPages() throws Exception{
		String namespace = "nswithanotherns:otherns";
		List<PageDW> pages = _client.getPagelist(namespace);
		assertEquals(1, pages.size());
		PageDW page = pages.get(0);
		assertEquals(namespace + ":page", page.id());
		assertEquals((Integer) 4, page.size());
		assertEquals((Integer) 1375372800, page.version());
		assertEquals((Integer) 1375372800, page.mtime());
	}


	@org.junit.Test
	public void getPagelistInANamespaceWithAnotherNamespace() throws Exception {
		// Check we get every pages with no max recursion level
		Set<String> expectedPages = new HashSet<String>();
		expectedPages.add("nswithanotherns:start");
		expectedPages.add("nswithanotherns:dummy");
		expectedPages.add("nswithanotherns:otherns:page");

		HashMap<String, Object> options = new HashMap<String, Object>();
		List<PageDW> actualPages = _client.getPagelist("nswithanotherns", options);

		assertEquals(expectedPages.size(), actualPages.size());
		for (PageDW page : actualPages) {
			assertTrue(expectedPages.contains(page.id()));
		}

		// And now makes sure we can limit this recursion level
		expectedPages = new HashSet<String>();
		expectedPages.add("nswithanotherns:start");
		expectedPages.add("nswithanotherns:dummy");

		options.put("depth", "2");
		actualPages = _client.getPagelist("nswithanotherns", options);

		assertEquals(expectedPages.size(), actualPages.size());
		for (PageDW page : actualPages) {
			assertTrue(expectedPages.contains(page.id()));
		}
	}



	@org.junit.Test
	public void genericQueryWithParameters() throws Exception {
		Object[] params = new Object[] { "ns1:start" };
		//255 because we make the query as an admin
		assertEquals(255, _client.genericQuery("wiki.aclCheck", params));
	}

	@org.junit.Test
	public void genericQueryWithoutParameters() throws Exception {
		assertEquals(TestParams.wikiVersion, _client.genericQuery("dokuwiki.getVersion"));
	}

	@org.junit.Test
	public void getTitle() throws Exception {
		assertEquals(TestParams.wikiTitle, _client.getTitle());
	}

	@org.junit.Test
	public void putAndGetPage() throws Exception {
		String pageId = "ns1:dummy";
		String content1 = "content1";
		String content2 = "content2";

		_client.putPage(pageId, content1);
		assertEquals(content1, _client.getPage(pageId));
		_client.putPage(pageId, content2);
		assertEquals(content2, _client.getPage(pageId));
	}

	@org.junit.Test
	public void appendPage() throws Exception {
		String pageId = "ns1:dummy";
		String initialContent = "put page in a clean state.";
		String append1 = "text appended.";
		String append2 = "final text";
		_client.putPage(pageId, initialContent);

		_client.appendPage(pageId, append1);
		assertEquals(initialContent + append1, _client.getPage(pageId));

		_client.appendPage(pageId, append2);
		assertEquals(initialContent + append1 + append2, _client.getPage(pageId));
	}

	@org.junit.Test
	public void getPageHTML() throws Exception {
		String pageId = "rev:start";
		assertEquals("\n<p>\n3rd version\n</p>\n", _client.getPageHTML(pageId));
	}

	@org.junit.Test
	public void getPageHTMLVersion() throws Exception{
		String pageId = "rev:start";
		Integer version = 1356218411;
		assertEquals("\n<p>\nv2\n</p>\n", _client.getPageHTMLVersion(pageId, version));
	}

	@org.junit.Test
	public void listLinks() throws Exception{
		List<LinkInfo> links = _client.listLinks("links:start");
		LinkInfo link0 = new LinkInfo(LinkInfo.Type.extern, "http://dokuwiki.org", "http://dokuwiki.org");
		LinkInfo link1 = new LinkInfo(LinkInfo.Type.extern, "http://github.com/gturri", "http://github.com/gturri");
		LinkInfo link2 = new LinkInfo(LinkInfo.Type.local, "ns1:dummy","/dokuwikiITestsForXmlRpcClient/doku.php?id=ns1:dummy" );

		assertEquals(link0, links.get(0));
		assertEquals(link1, links.get(1));
		assertEquals(link2, links.get(2));
	}

	@org.junit.Test
	public void getBackLinks() throws Exception{
		List<String> links = _client.getBackLinks("ns1:dummy");
		assertEquals("links:start", links.get(0));
	}

	@org.junit.Test
	public void getAllPages() throws Exception{
		List<Page> pages = _client.getAllPages();

		//Not an Equals assertion because other tests may create new pages
		assertTrue(pages.size() >= 12);

		//We check thoroughly an arbitrary page
		String pageId = "nssearch:page3";
		Page page = null;
		for(Page p : pages){
			if ( p.id().equals(pageId) ){
				if ( page != null ){
					fail("page " + pageId + " returned twice");
				}
				page = p;
			}
		}

		assertNotNull(page);
		assertEquals(pageId, page.id());
		assertEquals((Integer) 255, page.perms());
		assertDatesNear(2013, 7, 1, 17, 0, 0, page.lastModified());
		assertEquals((Integer) 197, page.size());
	}

	@org.junit.Test
	public void readPermsWithoutBeingAnAdmin() throws Exception {
		try {
			//Test to make sure we deal correctly with the fact that Dokuwiki may
			//either return an Integer or a String for the perms
			_clientWriter.getAllPages();
			_clientWriter.getRecentChanges(1356218401);
		} catch (ClassCastException e){
			fail();
		}
	}

	@org.junit.Test
	public void search() throws Exception {
		List<SearchResult> results = _client.search("amet");

		SearchResult sr = results.get(0);
		assertEquals("nssearch:page3", sr.id());
		assertEquals("Page 3 title", sr.title());
		assertEquals((Integer) 1375376400, sr.rev());
		assertEquals((Integer) 1375376400, sr.mtime());
		assertEquals((Integer) 2, sr.score());
		assertEquals((Integer) 197, sr.size());
		assertTrue(sr.snippet().contains("Amet"));

		assertEquals("nssearch:start", results.get(1).id());
		assertEquals((Integer) 1, results.get(1).score());
	}
}
