package dw.cli.commands;

import dw.xmlrpc.DokuJClient;
import dw.xmlrpc.exception.DokuException;

public class TimeGetter extends SimpleCommand {
	@Override
	protected String run(DokuJClient dokuClient)	throws DokuException {
		return dokuClient.getTime().toString();
	}
}