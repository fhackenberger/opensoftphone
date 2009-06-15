package org.acoveo.callcenter.sipclient.view;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

public class ScrollingLabel {

	final static int LEFT_RIGHT_SCROLL_MARGINS = 4;
	
	final static int MIN_SLEEPING_INTERVALL = 10;
	final static int DIRECTION_CHANGE_SLEEPING_INTERVALL = 1000;
	final static int SLEEPING_INTERVALL_FACTOR = 50;
	int sleepingIntervall = MIN_SLEEPING_INTERVALL;

	String textToScroll;
	Label label;
	boolean newText = false;
	Thread scrollingThread = null;

	public Label getLabel() {
		return label;
	}
	
	public ScrollingLabel(Composite parent, int style) {
		label = new Label(parent, style);
		
		ControlListener contListener = new ControlListener() {

			@Override
			public void controlMoved(ControlEvent e) {
				// Do nothing
			}

			@Override
			public void controlResized(ControlEvent e) {
				label.pack();
				if (label.getSize().x > label.getParent().getSize().x) {
					setSleepingIntervall(new Float(1.0 * label.getParent().getSize().x / label.getSize().x * SLEEPING_INTERVALL_FACTOR).intValue());
					if(scrollingThread == null) {
						startLabelShifting();
					}
				} else {
					// didn't use thread.stop() because:
					// http://java.sun.com/j2se/1.5.0/docs/guide/misc/threadPrimitiveDeprecation.html
					scrollingThread = null;
					label.setLocation(LEFT_RIGHT_SCROLL_MARGINS, label.getLocation().y);
				}				
			}
		};
		label.getParent().addControlListener(contListener);
		label.addControlListener(contListener);
	}

	public void setText(String text) {
		label.setText(text);
		label.pack();
		label.setLocation(LEFT_RIGHT_SCROLL_MARGINS, label.getLocation().y);
		if (label.getSize().x > label.getParent().getSize().x) {
			setSleepingIntervall(new Float(1.0 * label.getParent().getSize().x / label.getSize().x * SLEEPING_INTERVALL_FACTOR).intValue());
			if(scrollingThread == null) {
				startLabelShifting();
			}
		} else {
			// didn't use threa.stop() because:
			// http://java.sun.com/j2se/1.5.0/docs/guide/misc/threadPrimitiveDeprecation.html
			scrollingThread = null;
		}
	}

	private void startLabelShifting() {
		scrollingThread = new Thread() {
			boolean leftScroll = true, changedScrollDirection = false;

			@Override
			public void run() {
				Thread thisThread = Thread.currentThread();
				try {
					Thread.sleep(sleepingIntervall);
					while (scrollingThread == thisThread && !label.isDisposed()) {
						if (changedScrollDirection) {
							Thread.sleep(DIRECTION_CHANGE_SLEEPING_INTERVALL);
							changedScrollDirection = false;
						}
						
						label.getDisplay().syncExec(new Runnable() {
							public void run() {
								if (leftScroll) {
									label.setLocation(label.getLocation().x - 1, label.getLocation().y);

									// check if end of scrolling in this
									// direction reached
									int labelEnd = label.getLocation().x + label.getSize().x + LEFT_RIGHT_SCROLL_MARGINS;
									if (labelEnd < label.getParent().getSize().x) {
										leftScroll = false;
										changedScrollDirection = true;
									}
								} else {
									label.setLocation(label.getLocation().x + 1, label.getLocation().y);

									// check if end of scrolling in this
									// direction reached
									if (label.getLocation().x >= LEFT_RIGHT_SCROLL_MARGINS) {
										leftScroll = true;
										changedScrollDirection = true;
									}
								}
							}
						});
						Thread.sleep(sleepingIntervall);
					}
				} catch (InterruptedException e) {
					// TODO
				}
			}
		};
		scrollingThread.start();
	}

	private void setSleepingIntervall(int value) {
		if(value < MIN_SLEEPING_INTERVALL) {
			sleepingIntervall = MIN_SLEEPING_INTERVALL;
			return;
		}
		sleepingIntervall = value;
	}
	
	public void setBackground(Color color) {
		label.setBackground(color);
	}

	public void setLayoutData(Object layoutData) {
		label.setLayoutData(layoutData);
	}

	public void addFocusListener(FocusListener listener) {
		label.addFocusListener(listener);
		
	}

	public Display getDisplay() {
		return label.getDisplay();
	}
	
}
