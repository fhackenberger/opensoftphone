package org.acoveo.callcenter.guiLibrary;

import java.util.concurrent.Callable;

import org.eclipse.swt.widgets.Display;

/** Helper class for executing code from within the display thread
 * @author Florian Hackenberger (florian.hackenberger@acoveo.com)
 */
public class DisplayHelper {
	private static class Result<T> {
		T result;
		Exception exception;
	}
	
	/** Execute a callable within the display thread
	 * @param <T> The type of return value
	 * @param display The display to use for executing the callable
	 * @param callable The callable to execute
	 * @return Returns the return value from {@code Callable#call()}
	 * @throws Exception If {@code Callable#call()} throws an exception
	 */
	public static <T> T syncExec(Display display, final Callable<T> callable) throws Exception {
		final Result<T> result = new Result<T>();
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				try {
					result.result = callable.call();
				}catch(Exception e) {
					result.exception = e;
				}
			}
		});
		if(result.exception != null) {
			throw result.exception;
		}
		return result.result;
	}
}
