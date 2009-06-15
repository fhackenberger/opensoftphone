package org.acoveo.callcenter.sipclient.preferences.shortcutbuttons;

public class IllegalShortcutButtonTableColumnException extends Exception {
	public IllegalShortcutButtonTableColumnException() {
		super("This ShortcutButtonTableColumn ist not allowed to be used here!");
	}

	public IllegalShortcutButtonTableColumnException(String string) {
		super(string);
	}

	private static final long serialVersionUID = 2330407116923830045L;
}
