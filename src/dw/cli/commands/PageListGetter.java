package dw.cli.commands;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.martiansoftware.jsap.UnflaggedOption;

import dw.cli.Command;
import dw.cli.Output;
import dw.xmlrpc.DokuJClient;
import dw.xmlrpc.PageDW;
import dw.xmlrpc.exception.DokuException;

public class PageListGetter extends Command {

	@Override
	protected void registerParameters(JSAP jsap) throws JSAPException {
		jsap.registerParameter(new Switch("long").setShortFlag('l'));
		jsap.registerParameter(new UnflaggedOption("namespace").setRequired(true));
		jsap.registerParameter(new FlaggedOption("depth")
			.setStringParser(JSAP.INTEGER_PARSER)
			.setRequired(false)
			.setLongFlag("depth"));
	}

	@Override
	protected Output run(DokuJClient dokuClient, JSAPResult config) throws DokuException {
		Output result = new Output();
		Map<String, Object> clientOptions = buildClientOption(config);
		List<PageDW> pages = dokuClient.getPagelist(config.getString("namespace"), clientOptions);
		result.out = pagesToString(pages, config.getBoolean("long"));
		return result;
	}

	private Map<String, Object> buildClientOption(JSAPResult config) {
		Map<String, Object> result = new TreeMap<String, Object>();

		if ( config.contains("depth") ){
			result.put("depth",  config.getInt("depth"));
		}

		return result;
	}

	private String pagesToString(List<PageDW> pages, boolean longFormat) {
		String result = "";
		boolean firstLine = true;
		for(PageDW page : pages){
			if ( firstLine ){
				firstLine = false;
			} else {
				result += "\n";
			}
			if ( longFormat ){
				result += pageToLongString(page);
			} else {
				result += pageToString(page);
			}
		}
		return result;
	}

	private String pageToLongString(PageDW page) {
		return page.size()
				+ " " + page.mtime()
				+ " " + page.version()
				+ " " + page.id();
	}

	private String pageToString(PageDW page) {
		return page.id();
	}

}