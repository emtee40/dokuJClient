package dw.cli.commands.itest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import dw.cli.Output;
import dw.cli.itest.TestHelper;
import dw.xmlrpc.itest.TestParams;

public class T_AttachmentPutterAndDeleter extends TestHelper {

	@org.junit.Test
	public void putAndDeleteAttachment() throws Exception {
		String ns = "putAndDelete_ns";

		assertFalse(runWithArguments("getAttachments", ns).out.contains("toto.gif"));

		Output outputPut = runWithArguments("putAttachment", ns + ":toto.gif", TestParams.localFileToUpload);
		assertEquals("", outputPut.out);
		assertEquals("", outputPut.err);
		assertEquals(0, outputPut.exitCode);

		assertTrue(runWithArguments("getAttachments", ns).out.contains("toto.gif"));

		Output outputDelete = runWithArguments("deleteAttachment", ns + ":toto.gif");
		assertEquals("", outputDelete.out);
		assertEquals("", outputDelete.err);
		assertEquals(0, outputDelete.exitCode);

		assertFalse(runWithArguments("getAttachments", ns).out.contains("toto.gif"));
	}
}