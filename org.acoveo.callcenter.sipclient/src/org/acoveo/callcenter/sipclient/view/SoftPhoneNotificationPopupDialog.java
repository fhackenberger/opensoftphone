package org.acoveo.callcenter.sipclient.view;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import org.acoveo.callcenter.iconstore.IconStore;
import org.acoveo.callcenter.nls.Messages;
import org.acoveo.callcenter.sipclient.Activator;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author tkenner
 */
public class SoftPhoneNotificationPopupDialog extends PopupDialog {

	private static final String NOTIFICATIONS_HIDDEN = Messages.SoftPhoneNotificationPopupDialog_0;

	private static final int NUM_NOTIFICATIONS_TO_DISPLAY = 3;

	private static final String LABEL_NOTIFICATION = Messages.SoftPhoneNotificationPopupDialog_1;

	private static final int TIMEOUT_SECONDS = 6;

	private Form form;

	private Rectangle bounds;

	private Collection<NotificationContent> notifications;

	private Composite sectionClient;

	private FormToolkit toolkit;
	
	private Control dialogArea = null;
	
	private int timeout = TIMEOUT_SECONDS;

	public SoftPhoneNotificationPopupDialog(Shell parent) {
		super(parent, PopupDialog.INFOPOPUP_SHELLSTYLE | SWT.ON_TOP, false, false, false, false, null, null);
		toolkit = new FormToolkit(parent.getDisplay());
	}
	
	public SoftPhoneNotificationPopupDialog(Shell parent, int timeoutSec) {
		this(parent);
		timeout = timeoutSec;
	}

	public void setContents(Collection<NotificationContent> notifications) {
		this.notifications = notifications;
	}

	@Override
	protected Control getFocusControl() {
		return dialogArea;
	}

	@Override
	protected Control createContents(Composite parent) {
		getShell().setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_RED));
		dialogArea = createDialogArea(parent);
		return dialogArea;
	}

	@Override
	public int open() {
		int result = super.open();
		// Install a timeout which closes the window
		final Display display = getShell().getDisplay();
		Timer closeTimer = new Timer();
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				display.asyncExec(new Runnable() {
					@Override
					public void run() {
						close();
					}
				});
			}
		};
		if(timeout > 0) {
			closeTimer.schedule(task, timeout * 1000);
		}
		return result;
	}

	@Override
	protected final Control createDialogArea(final Composite parent) {

		Color bgColor = getShell().getDisplay().getSystemColor(SWT.COLOR_RED);
		
		getShell().setText(LABEL_NOTIFICATION);

		form = toolkit.createForm(parent);
		form.getBody().setLayout(new GridLayout());
		form.setBackground(bgColor);
		form.getBody().setBackground(bgColor);
		
		Section section = toolkit.createSection(form.getBody(), Section.TITLE_BAR);

		section.setText(LABEL_NOTIFICATION);
		section.setLayout(new GridLayout());
		section.setBackground(bgColor);
		
		sectionClient = toolkit.createComposite(section);
		sectionClient.setBackground(bgColor);
		
		int count = 0;
		int columns = 0;
		for (final NotificationContent notification : notifications) {
			if (count < NUM_NOTIFICATIONS_TO_DISPLAY) {
				Label notificationLabelIcon = toolkit.createLabel(sectionClient, ""); //$NON-NLS-1$
				notificationLabelIcon.setImage(notification.image);
				columns++;
				toolkit.createLabel(sectionClient, notification.message);
				columns++;
				if (notification.description != null) {
					Label descriptionLabel = toolkit.createLabel(sectionClient, notification.description);
					GridDataFactory.fillDefaults().span(2, SWT.DEFAULT).applyTo(descriptionLabel);
					columns = columns + 2;
				}
				if (notification.actions != null) {
					for (ActionContributionItem actionItem : notification.actions) {
						actionItem.getAction().addPropertyChangeListener(new IPropertyChangeListener() {
							@Override
							public void propertyChange(PropertyChangeEvent event) {
								close();
							}
						});
						actionItem.fill(sectionClient);
						columns++;
					}
				}
			} else {
				int numNotificationsRemain = notifications.size() - count;
				Hyperlink remainingHyperlink = toolkit.createHyperlink(sectionClient, numNotificationsRemain
						+ NOTIFICATIONS_HIDDEN, SWT.NONE);
				GridDataFactory.fillDefaults().span(2, SWT.DEFAULT).applyTo(remainingHyperlink);
				remainingHyperlink.addHyperlinkListener(new HyperlinkAdapter() {

					@Override
					public void linkActivated(HyperlinkEvent e) {
						// TaskListView.openInActivePerspective().setFocus();
						// IWorkbenchWindow window =
						// PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						// if (window != null) {
						// Shell windowShell = window.getShell();
						// if (windowShell != null) {
						// windowShell.setMaximized(true);
						// windowShell.open();
						// }
						// }
					}
				});
				break;
			}
			count++;
		}
		sectionClient.setLayout(new GridLayout(columns, false));
		section.setClient(sectionClient);

		ImageHyperlink hyperlink = new ImageHyperlink(section, SWT.NONE);
		toolkit.adapt(hyperlink, true, true);
		hyperlink.setBackground(null);
		hyperlink.setImage(IconStore.getDefault().getImageRegistry().get(IconStore.NOTIFICATION_CLOSE));
		hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e) {
				close();
			}
		});

		section.setTextClient(hyperlink);

		parent.pack();
		return form;
	}

	/**
	 * Initialize the shell's bounds.
	 */
	@Override
	public void initializeBounds() {
		getShell().setBounds(restoreBounds());
	}

	private Rectangle restoreBounds() {
		bounds = getShell().getBounds();
		Activator.getLogger().info(bounds.toString());
		Rectangle maxBounds = null;

		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			maxBounds = window.getShell().getMonitor().getClientArea();
		} else {
			// fallback
			Display display = Display.getCurrent();
			if (display == null)
				display = Display.getDefault();
			if (display != null && !display.isDisposed())
				maxBounds = display.getPrimaryMonitor().getClientArea();
		}

		if (bounds.width > -1 && bounds.height > -1) {
			if (maxBounds != null) {
				bounds.width = Math.min(bounds.width, maxBounds.width);
				bounds.height = Math.min(bounds.height, maxBounds.height);
			}
			// Enforce an absolute minimal size
			bounds.width = Math.max(bounds.width, 400);
			bounds.height = Math.max(bounds.height, 250);
		}

		if (bounds.x > -1 && bounds.y > -1 && maxBounds != null) {
			// bounds.x = Math.max(bounds.x, maxBounds.x);
			// bounds.y = Math.max(bounds.y, maxBounds.y);

			if (bounds.width > -1 && bounds.height > -1) {
				bounds.x = maxBounds.x + maxBounds.width - bounds.width;
				bounds.y = maxBounds.y + maxBounds.height - bounds.height;
			}
		}

		return bounds;
	}

	@Override
	public boolean close() {
		if (toolkit != null) {
			if (toolkit.getColors() != null) {
				toolkit.dispose();
			}
		}
		return super.close();
	}
}
