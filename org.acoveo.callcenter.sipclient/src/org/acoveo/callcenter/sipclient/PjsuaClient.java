/*
  PjsuaClient.java
  Copyright (C) 2004-2005  Mikael Magnusson <mikma@users.sourceforge.net>
  
  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.acoveo.callcenter.sipclient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.acoveo.callcenter.sipclient.preferences.AccountPreferencePage;
import org.acoveo.callcenter.sipclient.preferences.SoftPhonePreferencePage;
import org.acoveo.callcenter.sipclient.preferences.forwardcall.ForwardCallData.ForwardCallEntry;
import org.acoveo.tools.RunnableLoopWorker;
import org.acoveo.tools.Tuple;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.pjsip.pjsua.Callback;
import org.pjsip.pjsua.SWIGTYPE_p_int;
import org.pjsip.pjsua.SWIGTYPE_p_p_pjmedia_port;
import org.pjsip.pjsua.SWIGTYPE_p_pj_stun_nat_detect_result;
import org.pjsip.pjsua.SWIGTYPE_p_pjmedia_session;
import org.pjsip.pjsua.SWIGTYPE_p_pjsip_rx_data;
import org.pjsip.pjsua.SWIGTYPE_p_pjsip_status_code;
import org.pjsip.pjsua.SWIGTYPE_p_pjsip_transaction;
import org.pjsip.pjsua.SWIGTYPE_p_pjsip_tx_data;
import org.pjsip.pjsua.pj_pool_t;
import org.pjsip.pjsua.pj_str_t;
import org.pjsip.pjsua.pjmedia_snd_dev_info;
import org.pjsip.pjsua.pjmedia_snd_port;
import org.pjsip.pjsua.pjsip_cred_data_type;
import org.pjsip.pjsua.pjsip_cred_info;
import org.pjsip.pjsua.pjsip_event;
import org.pjsip.pjsua.pjsip_generic_string_hdr;
import org.pjsip.pjsua.pjsip_inv_state;
import org.pjsip.pjsua.pjsip_status_code;
import org.pjsip.pjsua.pjsip_transport_type_e;
import org.pjsip.pjsua.pjsua;
import org.pjsip.pjsua.pjsuaConstants;
import org.pjsip.pjsua.pjsua_acc_config;
import org.pjsip.pjsua.pjsua_acc_info;
import org.pjsip.pjsua.pjsua_call_info;
import org.pjsip.pjsua.pjsua_call_media_status;
import org.pjsip.pjsua.pjsua_config;
import org.pjsip.pjsua.pjsua_logging_config;
import org.pjsip.pjsua.pjsua_media_config;
import org.pjsip.pjsua.pjsua_msg_data;
import org.pjsip.pjsua.pjsua_transport_config;
import org.pjsip.pjsua.pjsua_transport_info;

/**
 * For debugging pjsip you can for example run the following regex search replace
 * on pjsua.java:
 * search: ([\w_]+)\(.*\{
 * replace: $0\nif\(runThread != null && runThread != Thread\.currentThread\(\)\) \{ System\.out\.println("$1"); \}
 * and then add a static member: static Thread runThread to the class
 * You would have to set the runThread variable to the pjsuaWorkerThread at startup
 * That will print a warning whenever a foreign thread calls a library function.
 * You can also increase the log level by setting the constant {@code PjsuaClient#PJSIP_LOG_LEVEL}
 * @author Florian Hackenberger (florian.hackenberger@acoveo.com)
 *
 */
public class PjsuaClient extends pjsua {
	private static ListenerList callbackListeners = new ListenerList();
	
	// The robot for stress testing (operates the phone randomly)
	private static PhoneTester phoneTester = new PhoneTester();
	
	// A timer task which tries to reregister to the server if the connection is lost
	private static ReconnectSipTimer reconnectSipTimer = new ReconnectSipTimer();
	
	// The loglevel for the PJSIP library (logged to the console)
	private static int PJSIP_LOG_LEVEL = 1;
	
	// A flag which indicated whether PjsuaClient is initialized
	private static boolean isPjsuaInitialised = false;
	
	// The memory pool for the application
	private static pj_pool_t memoryPool;
	
	// The media configuration (clock rate, channel count, etc.)
	private static pjsua_media_config mediaConfig;
	
	private static int DEFAULT_BITS_PER_SAMPLE = 16;
	
	// Hold the sample rate which was used to create the tones (ring, ringback...) for the ringing device
	private static IRingtone ringToneBridge = null;
	private static IRingtone ringBackToneBridge = null;
	private static IRingtone specialToneBridge = null;
	private static IRingtone ringToneRingingDevice = null;
	private static IRingtone ringBackToneRingingDevice = null;
	private static IRingtone specialToneRingingDevice = null;

	// This sound port is bound to a separate sound device for ringing
	private static pjmedia_snd_port ringSoundPort = null;
	
	// Whether to ring the pc speaker
	private static boolean ringPcSpeaker = false;
	// The worker class for beeping
	private static SystemBellWorker pcSpeakerWorker = new SystemBellWorker();
	// The worker thread for beeping
	private static Thread pcSpeakerWorkerThread = new Thread(pcSpeakerWorker);
	
	// The default sound rx/tx level
	private static float DEFAULT_SOUND_LEVEL = 1.0f;
	// The default rec/play latency
	private static int DEFAULT_SOUND_LATENCY = 100;
	
	// The regular expression for extracting the name 
	private static Pattern SIP_CONTACT_NAME_PATTERN = Pattern.compile("\\s*\"([^\"]+)\"");
	// The regular expression for extracting the sip user
	private static Pattern SIP_CONTACT_USER_PATTERN = Pattern.compile(".*sip:([^@]+)@.*");

	/** The worker for interacting with pjsua from foreign threads
	 * ATTENTION: The contract for using this worker is that you MUST NOT schedule a runnable
	 * in a synchronous fashion from within the display thread.
	 * The reason is that the {@code IPhoneStateListener}s are notified from within
	 * this thread.
	 */
	public static RunnableLoopWorker pjsuaWorker = new RunnableLoopWorker();
	// The worker thread for interacting with pjsua using other threads than the thread
	// which called initialise()
	private static Thread pjsuaWorkerThread = new Thread(pjsuaWorker, "pjsua worker");
	// A memory area which hold the thread description
//	private static int[] pjsuaWorkderThreadDesc = new int[pjsuaConstants.PJ_THREAD_DESC_SIZE];

	private static PjsuaCallback CALLBACK_OBJECT = new PjsuaCallback();
	
	public static class ContactInfo {
		public String name;
		public String number;
		public String displayString;
		public String sipUrl;
	}
	
	// Pjsip hangs if we call any library function from the worker thread,
	// while we are in a callback, therefore we don't use the pjsuaWorker here
	static class PjsuaCallback extends Callback {
		@Override
		public void on_incoming_call(int acc_id, final int call_id, SWIGTYPE_p_pjsip_rx_data rdata) {
			pjsuaWorker.asyncExec(new Runnable() {
				@Override
				public void run() {
					phoneState.newIncomingCall(call_id);
				}
			});
			for (Object listener : callbackListeners.getListeners()) {
				((Callback) listener).on_incoming_call(acc_id, call_id, rdata);
			}
		}

		@Override
		public void on_buddy_state(int buddy_id) {
			for (Object listener : callbackListeners.getListeners()) {
				((Callback) listener).on_buddy_state(buddy_id);
			}
		}

		@Override
		public void on_call_media_state(final int call_id) {
			PjsuaClient.pjsuaWorker.asyncExec(new Runnable() {
				@Override
				public void run() {
					pjsua_call_info info = new pjsua_call_info();
					PjsuaClient.call_get_info(call_id, info);
					if (info.getMedia_status() == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE) {
						ringBackStop();
						
						// If media is active, connect call to sound device.
						PjsuaClient.conf_connect(info.getConf_slot(), 0);
						PjsuaClient.conf_connect(0, info.getConf_slot());
					}
				}
			});

			for (Object listener : callbackListeners.getListeners()) {
				((Callback) listener).on_call_media_state(call_id);
			}
		}

		@Override
		public void on_call_replace_request(int call_id, SWIGTYPE_p_pjsip_rx_data rdata, SWIGTYPE_p_int st_code,
				pj_str_t st_text) {
			for (Object listener : callbackListeners.getListeners()) {
				((Callback) listener).on_call_replace_request(call_id, rdata, st_code, st_text);
			}
		}

		@Override
		public void on_call_replaced(int old_call_id, int new_call_id) {
			for (Object listener : callbackListeners.getListeners()) {
				((Callback) listener).on_call_replaced(old_call_id, new_call_id);
			}
		}

		@Override
		public void on_call_state(final int call_id, pjsip_event e) {
			final pjsua_call_info info = new pjsua_call_info();
			PjsuaClient.call_get_info(call_id, info);
			pjsuaWorker.asyncExec(new Runnable() {
				@Override
				public void run() {
					if(info.getState() == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
						phoneState.callDisconnected(call_id, info.getLast_status());
					}else if(info.getState() == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
						phoneState.callConnected(call_id);
					}
				}
			});
			
			for (Object listener : callbackListeners.getListeners()) {
				((Callback) listener).on_call_state(call_id, e);
			}
		}

		@Override
		public void on_call_transfer_request(int call_id, pj_str_t dst, SWIGTYPE_p_pjsip_status_code code) {
			for (Object listener : callbackListeners.getListeners()) {
				((Callback) listener).on_call_transfer_request(call_id, dst, code);
			}
		}

		@Override
		public void on_call_transfer_status(final int call_id, final int st_code, pj_str_t st_text, final int final_, SWIGTYPE_p_int p_cont) {
			final int activeCallId = phoneState.getActiveCallId();
			if(call_id == activeCallId) { // Blind transfer
				if(st_code == pjsip_status_code.PJSIP_SC_ACCEPTED.swigValue() ||
						st_code == pjsip_status_code.PJSIP_SC_OK.swigValue()) {
					pjsuaWorker.asyncExec(new Runnable() {
						@Override
						public void run() {
							// Transfer successful
							phoneState.callHangUp();
						}
					});
				}else if(final_ == pjsuaConstants.PJ_TRUE) {
					//XXX We should do something here, maybe reinvite?
					//PjsuaClient.call_reinvite(call_id, pjsuaConstants.PJ_FALSE, null);
				}
			}else if(call_id == onHoldCallId) {
				if(st_code == pjsip_status_code.PJSIP_SC_ACCEPTED.swigValue() ||
						st_code == pjsip_status_code.PJSIP_SC_OK.swigValue()) {
					// Transfer successful
					pjsua_call_info info = new pjsua_call_info();
					PjsuaClient.call_get_info(onHoldCallId, info);
					final pjsip_status_code onHoldStatus = info.getLast_status();
					PjsuaClient.call_get_info(activeCallId, info);
					final pjsip_status_code activeStatus = info.getLast_status();
					pjsuaWorker.asyncExec(new Runnable() {
						@Override
						public void run() {
							phoneState.callDisconnected(onHoldCallId, onHoldStatus);
							phoneState.callDisconnected(activeCallId, activeStatus);
						}
					});
				}else if(final_ == pjsuaConstants.PJ_TRUE) {
					//XXX We should do something here, maybe reinvite?
					//PjsuaClient.call_reinvite(call_id, pjsuaConstants.PJ_FALSE, null);
				}
			}
			
			for (Object listener : callbackListeners.getListeners()) {
				((Callback) listener).on_call_transfer_status(call_id, st_code, st_text, final_, p_cont);
			}
		}

		@Override
		public void on_call_tsx_state(int call_id, SWIGTYPE_p_pjsip_transaction tsx, pjsip_event e) {
			for (Object listener : callbackListeners.getListeners()) {
				((Callback) listener).on_call_tsx_state(call_id, tsx, e);
			}
		}

		@Override
		public void on_dtmf_digit(int call_id, int digit) {
			for (Object listener : callbackListeners.getListeners()) {
				((Callback) listener).on_dtmf_digit(call_id, digit);
			}
		}

		@Override
		public void on_nat_detect(SWIGTYPE_p_pj_stun_nat_detect_result res) {
			for (Object listener : callbackListeners.getListeners()) {
				((Callback) listener).on_nat_detect(res);
			}
		}

		@Override
		public void on_pager_status(int call_id, pj_str_t to, pj_str_t body, pjsip_status_code status, pj_str_t reason) {
			for (Object listener : callbackListeners.getListeners()) {
				((Callback) listener).on_pager_status(call_id, to, body, status, reason);
			}
		}

		@Override
		public void on_pager_status2(int call_id, pj_str_t to, pj_str_t body, pjsip_status_code status,
				pj_str_t reason, SWIGTYPE_p_pjsip_tx_data tdata, SWIGTYPE_p_pjsip_rx_data rdata) {
			for (Object listener : callbackListeners.getListeners()) {
				((Callback) listener).on_pager_status2(call_id, to, body, status, reason, tdata, rdata);
			}
		}

		@Override
		public void on_pager(int call_id, pj_str_t from, pj_str_t to, pj_str_t contact, pj_str_t mime_type,
				pj_str_t body) {
			String message = body.getPtr();
			Activator.getLogger().debug("Received pager message: \"" + message + "\""); //$NON-NLS-1$ //$NON-NLS-2$

			if(isProtocolMessage(message)) {
				// Currently nothing to do
			}

			for (Object listener : callbackListeners.getListeners()) {
				((Callback) listener).on_pager(call_id, from, to, contact, mime_type, body);
			}
		}

		@Override
		public void on_pager2(int call_id, pj_str_t from, pj_str_t to, pj_str_t contact, pj_str_t mime_type,
				pj_str_t body, SWIGTYPE_p_pjsip_rx_data rdata) {
			for (Object listener : callbackListeners.getListeners()) {
				((Callback) listener).on_pager2(call_id, from, to, contact, mime_type, body, rdata);
			}
		}

		@Override
		public void on_reg_state(final int acc_id) {
			final pjsua_acc_info info = new pjsua_acc_info();
			PjsuaClient.acc_get_info(acc_id, info);
			pjsuaWorker.asyncExec(new Runnable() {
				@Override
				public void run() {
					if(info.getHas_registration() == pjsuaConstants.PJ_TRUE && info.getStatus() == pjsip_status_code.PJSIP_SC_OK) {
						PhoneState currentPhoneState = phoneState;
						phoneState.registrationSuccessful(acc_id);
					}else {
						phoneState.registrationFailed(acc_id);
						reconnectSipTimer.scheduleReconnect();
					}
				}
			});
			
			for (Object listener : callbackListeners.getListeners()) {
				((Callback) listener).on_reg_state(acc_id);
			}
		}

		@Override
		public void on_stream_created(int call_id, SWIGTYPE_p_pjmedia_session sess, long stream_idx,
				SWIGTYPE_p_p_pjmedia_port p_port) {
			for (Object listener : callbackListeners.getListeners()) {
				((Callback) listener).on_stream_created(call_id, sess, stream_idx, p_port);
			}
		}

		@Override
		public void on_stream_destroyed(int call_id, SWIGTYPE_p_pjmedia_session sess, long stream_idx) {
			for (Object listener : callbackListeners.getListeners()) {
				((Callback) listener).on_stream_destroyed(call_id, sess, stream_idx);
			}
		}

		@Override
		public void on_typing(int call_id, pj_str_t from, pj_str_t to, pj_str_t contact, int is_typing) {
			for (Object listener : callbackListeners.getListeners()) {
				((Callback) listener).on_typing(call_id, from, to, contact, is_typing);
			}
		}

	}
	// The SIP status code to send if the user answeres a call
	private static int ANSWER_STATUS_CODE = pjsip_status_code.PJSIP_SC_OK.swigValue();
	// The SIP status code to send to indicate ringing
	private static int RINGING_STATUS_CODE = pjsip_status_code.PJSIP_SC_RINGING.swigValue();
	
	// Extension to dial in order to get the state of the agent sent by text
	protected static String[] internalNumbers = {};
	
	// The shared dial destination string buffer
	private static DialDestination dialDestination = new DialDestination();
	
	// The values from the LoginDialog will be stored in the following variables:
	private static String sipUser = ""; //$NON-NLS-1$
	private static String sipPwd = ""; //$NON-NLS-1$
	
	public enum CallDirection {
		INVALID,
		OUTBOUND,
		INBOUND
	}
	private static int defaultAccountId = -1;
	private static int activeCallId = -1;
	private static CallInformation activeCallInfo;
	private static int ringingCallId = -1;
	private static CallInformation ringingCallInfo;
	private static int onHoldCallId = -1;
	private static CallInformation onHoldCallInfo;
	
	private static PhoneState phoneState = PhoneState.UNREGISTERED;
	private static PhoneState previousPhoneState = PhoneState.UNREGISTERED;
	private static PhoneStateTransition phoneStateTransition = PhoneStateTransition.INVALID;
	private static boolean notifyListeners = false;
	
	private static ListenerList phoneStateListeners = new ListenerList();
	
	public enum PhoneStateTransition {
		INVALID,
		REGISTRATION_SUCCESSFUL,
		REGISTRATION_FAILED,
		NEW_INCOMING_CALL,
		NUMBER_DIALLED,
		DIAL_CANCELLED,
		CALL_CONNECTED,
		CALL_DISCONNECTED,
		CALL_DISCONNECTED_NOT_FOUND,
		CALL_ANSWERED,
		CALL_REJECTED,
		CALL_HUNGUP,
		CALL_HOLD,
		CALL_UNHOLD,
		CALL_FORWARDED;
	}
	
	private static List<ForwardCallEntry> forwards;
	
	public static void setForwardCallEntry(ForwardCallEntry forwardCallEntry) {
		if(forwards == null) {
			 forwards = new ArrayList<ForwardCallEntry>();
		}
		if(forwardCallEntry == null) {
			forwards.clear();
			return;
		}
		if(forwards.size() > 0) {
			forwards.set(0, forwardCallEntry);
		}
		else {
			forwards.add(forwardCallEntry);
		}
	}
	public static ForwardCallEntry getForwardCallEntry() {
		if(forwards == null) {
			 forwards = new ArrayList<ForwardCallEntry>();
		}
		if(forwards.size() <= 0) {
			return null;
		}
		return forwards.get(0);
	}

	public enum PhoneState {
		/** The phone is not registered to the server */
		UNREGISTERED {
			@Override
			protected void registrationSuccessful(int accountId) {
				synchronized (phoneState) {
					if(phoneState == this) {
						defaultAccountId = accountId;
						makeStateTransition(IDLE,PjsuaClient.PhoneStateTransition.REGISTRATION_SUCCESSFUL);
					}
				}
				notifyPhoneStateListeners();
			}
		},
		/** The phone is registered and there is no active or ringing call */
		IDLE {
			@Override
			protected void registrationFailed(int accountId) {
				synchronized (phoneState) {
					if(phoneState == this) {
						if(accountId == defaultAccountId) {
							defaultAccountId = -1;
							makeStateTransition(UNREGISTERED,PjsuaClient.PhoneStateTransition.REGISTRATION_FAILED);
						}else {
							logInvalidStateTransition(PhoneStateTransition.REGISTRATION_FAILED, "Registration failed for unknown account id " + accountId); //$NON-NLS-1$
						}
					}
				}
				notifyPhoneStateListeners();
			}
			@Override
			public void newIncomingCall(int callId) {
				synchronized (phoneState) {
					if(phoneState == this) {
						boolean letItRing = true;
						ForwardCallEntry forward = getForwardCallEntry();
						if(forward != null && forward.isIfBusy() == false){
							if(forward.getRingCount() > 0) {
								forwardCallDelayedIfRinging(callId, forward);
							} else {
								letItRing = false;
								forwardCall(callId, forward);
							}
						}
						if(letItRing){
							ringingCallId = callId;
							ringingCallInfo = getCallInfoHelper(ringingCallId, CallDirection.INBOUND);
							makeStateTransition(INCOMING_CALL,PjsuaClient.PhoneStateTransition.NEW_INCOMING_CALL);
						}
					}
				}
				notifyPhoneStateListeners();
			}
			@Override
			public void dialNumber(final String number) {
				pjsuaWorker.asyncExec(new Runnable() {
					@Override
					public void run() {
						synchronized (phoneState) {
							if(phoneState == IDLE) {
								try {
									ringingCallId = dialNumberHelper(number);
									ringingCallInfo = getCallInfoHelper(ringingCallId, CallDirection.OUTBOUND);
									makeStateTransition(OUTGOING_CALL,PjsuaClient.PhoneStateTransition.NUMBER_DIALLED);
								}catch(PjsipException e) {
									// Error logged in helper
								}catch(Exception e) {
									e.printStackTrace();
								}
							}
						}
						notifyPhoneStateListeners();
					}
				});
			}
		},
		/** An incoming call is ringing. There could another call on hold.*/
		INCOMING_CALL {
			@Override
			public void callReject() {
				pjsuaWorker.asyncExec(new Runnable() {
					@Override
					public void run() {
						synchronized (phoneState) {
							if (phoneState == INCOMING_CALL) {
								// TODO check forward code
								ForwardCallEntry forward = getForwardCallEntry();
								if (forward != null && forward.isIfBusy()) {
									// forward if a "busy forward" is active
									forwardCall(ringingCallId, forward);
									makeStateTransition(
											IDLE,
											PjsuaClient.PhoneStateTransition.CALL_FORWARDED);
								} else {
									try {
										callHangupHelper(ringingCallId);
									} catch (PjsipException e) {
										// Error logged in helper
									}
									makeStateTransition(
											IDLE,
											PjsuaClient.PhoneStateTransition.CALL_REJECTED);
								}
								invalidateRingingCallData();
							}
							notifyPhoneStateListeners();
						}
					}
				});
			}
			@Override
			public void callAnswer() {
				pjsuaWorker.asyncExec(new Runnable() {
					@Override
					public void run() {
						synchronized (phoneState) {
							if(phoneState == INCOMING_CALL) {
								int status = PjsuaClient.call_answer(ringingCallId, ANSWER_STATUS_CODE, null, null);
								if(status != pjsuaConstants.PJ_SUCCESS) {
									Activator.getLogger().error("Answering call number " + activeCallId + " failed with status " + status); //$NON-NLS-1$ //$NON-NLS-2$
								}else {
									swapRingingAndActiveCallData();
									invalidateRingingCallData();
									makeStateTransition(ACTIVE_CALL,PjsuaClient.PhoneStateTransition.CALL_ANSWERED);
								}
							}
						}
						notifyPhoneStateListeners();
					}
				});
			}
			@Override
			public void callIndicateRinging() {
				pjsuaWorker.asyncExec(new Runnable() {
					@Override
					public void run() {
						synchronized (phoneState) {
							if(phoneState == INCOMING_CALL) {
								int status = PjsuaClient.call_answer(ringingCallId, RINGING_STATUS_CODE, null, null);
								if(status != pjsuaConstants.PJ_SUCCESS) {
									Activator.getLogger().error("Indicating ringing for call number " + ringingCallId + " failed with status " + status); //$NON-NLS-1$ //$NON-NLS-2$
								}
							}
						}
					}
				});
			}
			@Override
			public void callDisconnected(int callId, pjsip_status_code lastStatus) {
				synchronized (phoneState) {
					if(phoneState == this) {
						if(ringingCallId == callId) {
							invalidateRingingCallData();
							makeStateTransition(IDLE,PjsuaClient.PhoneStateTransition.CALL_DISCONNECTED);
						}
					}
				}
				notifyPhoneStateListeners();
			}
			@Override
			public int getRingingCallId() {
				return ringingCallId;
			}
			@Override
			public CallInformation getCallerInformation() { 
				return ringingCallInfo; 
			}
		},
		/** An outgoing call is ringing. There could be another call on hold */
		OUTGOING_CALL {
			@Override
			public void dialCancel() {
				pjsuaWorker.asyncExec(new Runnable() {
					@Override
					public void run() {
						synchronized (phoneState) {
							if(phoneState == OUTGOING_CALL) {
								try {
									callHangupHelper(ringingCallId);
								}catch(PjsipException e) {
									// Error logged in helper
								}
								invalidateRingingCallData();
								if(previousPhoneState == CALL_ON_HOLD) {
									makeStateTransition(CALL_ON_HOLD,PjsuaClient.PhoneStateTransition.DIAL_CANCELLED);
								}else {
									makeStateTransition(IDLE,PjsuaClient.PhoneStateTransition.DIAL_CANCELLED);
								}
							}
						}
						notifyPhoneStateListeners();
					}
				});
			}
			@Override
			public void callConnected(int callId) {
				synchronized (phoneState) {
					if(phoneState == this) {
						if(ringingCallId == callId) {
							swapRingingAndActiveCallData();
							invalidateRingingCallData();
							if(previousPhoneState == CALL_ON_HOLD) {
								makeStateTransition(ACTIVE_CALL_AND_ON_HOLD,PjsuaClient.PhoneStateTransition.CALL_CONNECTED);
							}else {
								makeStateTransition(ACTIVE_CALL,PjsuaClient.PhoneStateTransition.CALL_CONNECTED);
							}
						}else {
							logInvalidStateTransition(PhoneStateTransition.CALL_CONNECTED, "call connect event for unknown call id " + callId); //$NON-NLS-1$
						}
					}
				}
				notifyPhoneStateListeners();
			}
			@Override
			public void callDisconnected(int callId, pjsip_status_code lastStatus) {
				synchronized (phoneState) {
					if(phoneState == this) {
						if(ringingCallId == callId) {
							invalidateRingingCallData();
							PjsuaClient.PhoneStateTransition transitionReason = PhoneStateTransition.CALL_DISCONNECTED;
							if(lastStatus == pjsip_status_code.PJSIP_SC_NOT_FOUND || lastStatus == pjsip_status_code.PJSIP_SC_ADDRESS_INCOMPLETE) {
								transitionReason = PhoneStateTransition.CALL_DISCONNECTED_NOT_FOUND;
							}
							if(previousPhoneState == CALL_ON_HOLD) {
								makeStateTransition(CALL_ON_HOLD, transitionReason);
							}else {
								makeStateTransition(IDLE, transitionReason);
							}
						}else {
							logInvalidStateTransition(PhoneStateTransition.CALL_DISCONNECTED, "call disconnect event for unknown call id " + callId); //$NON-NLS-1$
						}
					}
				}
				notifyPhoneStateListeners();
			}
			@Override
			public int getRingingCallId() {
				return ringingCallId;
			}
			@Override
			public CallInformation getCallerInformation() { 
				return ringingCallInfo; 
			}
		},
		/** There is one call established */
		ACTIVE_CALL {
			@Override
			protected void newIncomingCall(final int callId) {
				pjsuaWorker.asyncExec(new Runnable() {
					@Override
					public void run() {
						synchronized (phoneState) {
							if (phoneState == ACTIVE_CALL) {
								ForwardCallEntry forward = getForwardCallEntry();
								if (forward != null && forward.isIfBusy()) {
									// forward if a "busy forward" is active
									forwardCall(callId, forward);
								} else {
									// Hang-up silently
									try {
										callHangupHelper(callId);
									} catch (Exception e) {
										// Error logged in helper
									}
								}
							}
						}
					}
				});
			}
			public void callHangUp() {
				pjsuaWorker.asyncExec(new Runnable() {
					@Override
					public void run() {
						synchronized (phoneState) {
							if(phoneState == ACTIVE_CALL) {
								try {
									callHangupHelper(activeCallId);
								}catch (PjsipException e) {
									// Error logged in helper
								}
								invalidateActiveCallData();
								makeStateTransition(IDLE,PjsuaClient.PhoneStateTransition.CALL_HUNGUP);
							}
						}
						notifyPhoneStateListeners();
					}
				});
			}
			
			@Override
			public void callDisconnected(int callId, pjsip_status_code lastStatus) {
				synchronized (phoneState) {
					if(phoneState == this) {
						if(activeCallId == callId) {
							invalidateActiveCallData();
							makeStateTransition(IDLE,PjsuaClient.PhoneStateTransition.CALL_DISCONNECTED);
						}else {
							logInvalidStateTransition(PhoneStateTransition.CALL_DISCONNECTED, "call disconnect event for unknown call id " + callId); //$NON-NLS-1$
						}
					}
				}
				notifyPhoneStateListeners();
			}
			@Override
			public void callHold() {
				pjsuaWorker.asyncExec(new Runnable() {
					@Override
					public void run() {
						synchronized (phoneState) {
							if(phoneState == ACTIVE_CALL) {
								try {
									callHoldHelper(activeCallId);
									swapActiveAndOnHoldCallData();
									invalidateActiveCallData();
									makeStateTransition(CALL_ON_HOLD,PjsuaClient.PhoneStateTransition.CALL_HOLD);
								}catch(PjsipException e) {
									// Error logged in helper
								}
							}
						}
						notifyPhoneStateListeners();
					}
				});
			}
			@Override
			public int getActiveCallId() {
				return activeCallId;
			}
			@Override
			public CallInformation getCallerInformation() { 
				return activeCallInfo; 
			}
		},
		/** There is one call on hold */
		CALL_ON_HOLD {
			@Override
			public void callUnhold() {
				pjsuaWorker.asyncExec(new Runnable() {
					@Override
					public void run() {
						synchronized (phoneState) {
							if(phoneState == CALL_ON_HOLD) {
								try {
									callUnholdHelper(onHoldCallId);
									swapActiveAndOnHoldCallData();
									invalidateOnHoldCallData();
									makeStateTransition(ACTIVE_CALL,PjsuaClient.PhoneStateTransition.CALL_UNHOLD);
								}catch(PjsipException e) {
									// Error logged in helper
								}
							}
						}
						notifyPhoneStateListeners();
					}
				});
			}
			
			@Override
			protected void callDisconnected(int callId, pjsip_status_code lastStatus) {
				synchronized (phoneState) {
					if(phoneState == this) {
						if(onHoldCallId == callId) {
							invalidateOnHoldCallData();
							makeStateTransition(IDLE, PjsuaClient.PhoneStateTransition.CALL_DISCONNECTED);
						}else {
							logInvalidStateTransition(PhoneStateTransition.CALL_DISCONNECTED, "call disconnect event for unknown call id " + callId); //$NON-NLS-1$
						}
					}
				}
				notifyPhoneStateListeners();
			}

			@Override
			public void dialNumber(final String number) {
				pjsuaWorker.asyncExec(new Runnable() {
					@Override
					public void run() {
						synchronized (phoneState) {
							try {
								ringingCallId = dialNumberHelper(number);
								ringingCallInfo = getCallInfoHelper(ringingCallId, CallDirection.OUTBOUND);
								makeStateTransition(OUTGOING_CALL,PjsuaClient.PhoneStateTransition.NUMBER_DIALLED);
							}catch(PjsipException e) {
								// Error logged in helper
							}
						}
						notifyPhoneStateListeners();
					}
				});
			}
			
			@Override
			protected void newIncomingCall(int callId) {
				synchronized (phoneState) {
					// Hang-up silently
					if(phoneState == this) {
						try {
							callHangupHelper(callId);
						}catch(PjsipException e) {
							// Error logged in helper
						}
					}
				}
			}

			@Override
			public int getOnHoldCallId() {
				return onHoldCallId;
			}
			@Override
			public CallInformation getCallerInformation() { 
				return onHoldCallInfo; 
			}
		},
		/** There is one active call and another on hold */
		ACTIVE_CALL_AND_ON_HOLD {
			/** Stores whether conferencing is enabled or not
			 * Make sure we reset it to false when leaving this state */
			private boolean conferencingEnabled = false;
			@Override
			public void callHangUp() {
				pjsuaWorker.asyncExec(new Runnable() {
					@Override
					public void run() {
						synchronized (phoneState) {
							if(phoneState == ACTIVE_CALL_AND_ON_HOLD) {
								try {
									callHangupHelper(activeCallId);
								}catch (PjsipException e) {
									// Error logged in helper
								}
								invalidateActiveCallData();
								conferencingEnabled = false;
								makeStateTransition(CALL_ON_HOLD,PjsuaClient.PhoneStateTransition.CALL_HUNGUP);
							}
						}
						notifyPhoneStateListeners();
					}
				});
			}
			@Override
			public void callDisconnected(int callId, pjsip_status_code lastStatus) {
				synchronized (phoneState) {
					if(phoneState == this) {
						if(activeCallId == callId && !conferencingEnabled) {
							invalidateActiveCallData();
							conferencingEnabled = false;
							makeStateTransition(CALL_ON_HOLD,PjsuaClient.PhoneStateTransition.CALL_DISCONNECTED);
						}else if(activeCallId == callId && conferencingEnabled) {
							swapActiveAndOnHoldCallData();
							invalidateOnHoldCallData();
							makeStateTransition(ACTIVE_CALL, PjsuaClient.PhoneStateTransition.CALL_DISCONNECTED);
						}else if(onHoldCallId == callId) {
							invalidateOnHoldCallData();
							conferencingEnabled = false;
							makeStateTransition(ACTIVE_CALL, PjsuaClient.PhoneStateTransition.CALL_DISCONNECTED);
						}else {
							logInvalidStateTransition(PhoneStateTransition.CALL_DISCONNECTED, " could not find call with id " + callId); //$NON-NLS-1$
						}
					}
				}
				notifyPhoneStateListeners();
			}
			@Override
			public int getActiveCallId() {
				return activeCallId;
			}
			@Override
			public int getOnHoldCallId() {
				return onHoldCallId;
			}
			@Override
			public void enableConferencing() {
				pjsuaWorker.asyncExec(new Runnable() {
					@Override
					public void run() {
						synchronized (phoneState) {
							if(phoneState == ACTIVE_CALL_AND_ON_HOLD && !conferencingEnabled) {
								// Reinvite the on hold call
								try {
									callUnholdHelper(onHoldCallId);
									PjsuaClient.pjsuaWorker.syncExec(new Runnable() {
										@Override
										public void run() {
											// Wait for held call to become active
											pjsua_call_info callInfo = new pjsua_call_info();
											PjsuaClient.call_get_info(onHoldCallId, callInfo);
											while(callInfo.getMedia_status() == pjsua_call_media_status.PJSUA_CALL_MEDIA_LOCAL_HOLD) {
												try {
													Thread.sleep(100);
												} catch (InterruptedException e) {
													// Ignore
												}
												PjsuaClient.call_get_info(onHoldCallId, callInfo);
											}
											int confPortActiveCall = PjsuaClient.call_get_conf_port(onHoldCallId);
											// Connect call ports bidirectionally
											int[] callids = new int[50];
											long[] count = new long[] {callids.length};
											PjsuaClient.enum_calls(callids, count);
											for(int i = 0; i < count[0]; i++) {
												int currCallid = callids[i];
												if(currCallid == confPortActiveCall) {
													continue;
												}
												if(PjsuaClient.call_has_media(currCallid) == 0) {
													Activator.getLogger().debug("skipping callid " + currCallid + " has no media");
													continue;
												}
												int confPortCurrCall = PjsuaClient.call_get_conf_port(currCallid);
												if(confPortCurrCall == pjsuaConstants.PJSUA_INVALID_ID) {
													Activator.getLogger().error("failed to get conf port for callid: " + currCallid);
													return;
												}
												Activator.getLogger().debug("Bridging " + activeCallId + " (port " + confPortActiveCall + ") to " + currCallid + " (port " + confPortCurrCall + ")");
												PjsuaClient.conf_connect(confPortActiveCall, confPortCurrCall);
												PjsuaClient.conf_connect(confPortCurrCall, confPortActiveCall);
											}
											conferencingEnabled = true;
										}
									});
									notifyConferenceListeners(conferencingEnabled);
								} catch (PjsipException e1) {
									// Already logged, nothing we can do, ignore it
								}
							}
						}
					}
				});
			}
			@Override
			public void disableConferencing() {
				pjsuaWorker.asyncExec(new Runnable() {
					@Override
					public void run() {
						synchronized (phoneState) {
							if(phoneState == ACTIVE_CALL_AND_ON_HOLD && conferencingEnabled) {
								try {
									// Put to originally held call back on hold
									callHoldHelper(onHoldCallId);
									PjsuaClient.pjsuaWorker.syncExec(new Runnable() {
										@Override
										public void run() {
											// Wait for call to become held
											pjsua_call_info callInfo = new pjsua_call_info();
											PjsuaClient.call_get_info(onHoldCallId, callInfo);
											while(callInfo.getMedia_status() != pjsua_call_media_status.PJSUA_CALL_MEDIA_LOCAL_HOLD) {
												try {
													Thread.sleep(100);
												} catch (InterruptedException e) {
													// Ignore
												}
												PjsuaClient.call_get_info(onHoldCallId, callInfo);
											}
											int activeCallid = PjsuaClient.getPhoneState().getActiveCallId();
											int confPortActiveCall = PjsuaClient.call_get_conf_port(activeCallid);
											// Disconnect call ports from other call ports
											int[] callids = new int[50];
											long[] count = new long[] {callids.length};
											PjsuaClient.enum_calls(callids, count);
											for(int i = 0; i < count[0]; i++) {
												int currCallid = callids[i];
												if(currCallid == confPortActiveCall) {
													continue;
												}
												if(PjsuaClient.call_has_media(currCallid) == 0) {
													Activator.getLogger().debug("skipping callid " + currCallid + " has no media");
													continue;
												}
												int confPortCurrCall = PjsuaClient.call_get_conf_port(currCallid);
												if(confPortCurrCall == pjsuaConstants.PJSUA_INVALID_ID) {
													Activator.getLogger().error("failed to get conf port for callid: " + currCallid);
													return;
												}
												Activator.getLogger().debug("Disable Bridging " + activeCallid + " (port " + confPortActiveCall + ") to " + currCallid + " (port " + confPortCurrCall + ")");
												PjsuaClient.conf_disconnect(confPortActiveCall, confPortCurrCall);
												PjsuaClient.conf_disconnect(confPortCurrCall, confPortActiveCall);
											}
											conferencingEnabled = false;
										}
									});
									notifyConferenceListeners(conferencingEnabled);
								}catch(PjsipException e) {
									// Already logged, nothing we can do, ignore it
								}
							}
						}
					}
				});
			}
			@Override
			public boolean isConferencingEnabled() {
				return conferencingEnabled;
			}
			@Override
			protected void registrationFailed(int accountId) {
				if(accountId == defaultAccountId) {
					conferencingEnabled = false;
				}
				super.registrationFailed(accountId);
			}
			@Override
			public CallInformation getCallerInformation() { 
				return activeCallInfo; 
			}
		};
		protected void registrationSuccessful(int accountId) {
			if(accountId == defaultAccountId) {
				// The sip server notifies us contiuously of our active registration
			}
			logInvalidStateTransition(PhoneStateTransition.REGISTRATION_SUCCESSFUL, "not handled by state class"); //$NON-NLS-1$
		}

		public static class ForwardCallJob extends Job {
			private int callId;
			private ForwardCallEntry forward;
			public ForwardCallJob(int callId, ForwardCallEntry forward) {
				super("Delayed call forwarding");
				this.callId = callId;
				this.forward = forward;
			}
			
			public long getDelay() {
				return (long) forward.getRingCount() * 1000;
			}
			
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if(ringingCallId == callId) {
					// still ringing, forward now
					pjsuaWorker.syncExec(new Runnable() {
						@Override
						public void run() {
							PjsuaClient.phoneState.forwardCall(callId, forward);
						}
					});
				}
				return null;
			}
			
		}

		public CallInformation getCallerInformation() { return null; }
		
		protected void registrationFailed(int accountId) {
			if(accountId == defaultAccountId) {
				makeStateTransition(UNREGISTERED,PjsuaClient.PhoneStateTransition.REGISTRATION_FAILED);
				ACTIVE_CALL_AND_ON_HOLD.
				notifyPhoneStateListeners();
			}
		}
		/** Reject a ringing call */
		public void callReject() {
			logInvalidStateTransition(PhoneStateTransition.CALL_REJECTED, "not handled by state class"); //$NON-NLS-1$
		}
		/** Answer a ringing call */
		public void callAnswer() {
			logInvalidStateTransition(PhoneStateTransition.CALL_ANSWERED, "not handled by state class"); //$NON-NLS-1$
		}
		/** Indicate ringing on an incoming call
		 * 
		 * Does not cause a state transition
		 */
		public void callIndicateRinging() {
			// Do nothing, as this does not cause a state transition
		}
		
		/** Hangup an active call */
		public void callHangUp() {
			logInvalidStateTransition(PhoneStateTransition.CALL_HUNGUP, "not handled by state class"); //$NON-NLS-1$
		}
		/** Retrieve an active call from hold */
		public void callUnhold() {
			logInvalidStateTransition(PhoneStateTransition.CALL_UNHOLD, "not handled by state class"); //$NON-NLS-1$
		}
		/** Put an active call on hold */
		public void callHold() {
			logInvalidStateTransition(PhoneStateTransition.CALL_HOLD, "not handled by state class"); //$NON-NLS-1$
		}
		/** Cancel an outgoing (but not yet active) call */
		public void dialCancel() {
			logInvalidStateTransition(PhoneStateTransition.DIAL_CANCELLED, "not handled by state class"); //$NON-NLS-1$
		}
		/** Dial the given number
		 * 
		 * @param number The number to dial (should be a SIP URL)
		 */
		public void dialNumber(String number) {
			logInvalidStateTransition(PhoneStateTransition.NUMBER_DIALLED, "not handled by state class"); //$NON-NLS-1$
		}
		/** Retrieve the call id of an active call
		 * 
		 * @return -1 if there is not active call, the call id of an active call otherwise
		 */
		public int getActiveCallId() { return -1; }
		/** Retrieve the call id of a ringing call
		 * 
		 * @return -1 if there is no ringing call, the call id of a ringing call otherwise
		 */
		public int getRingingCallId() { return -1; }
		/** Retrieve the call id of a call on hold
		 * 
		 * @return -1 if there is no call on hold, the call id of a call on hold otherwise
		 */
		public int getOnHoldCallId() { return -1; }
		/** Enable conferencing
		 */
		public void enableConferencing() {}
		/** Disable conferencing
		 */
		public void disableConferencing() {}
		/** Whether conferencing is active
		 * @return whether conferencing is active
		 */
		public boolean isConferencingEnabled() { return false; }
		
		protected CallInformation getCallInfoHelper(final int callId, final CallDirection callDirection) {
			if(callId == pjsuaConstants.PJSUA_INVALID_ID) {
				return null;
			}
			return PjsuaClient.pjsuaWorker.syncExecNoExc(new Callable<CallInformation>() {
				@Override
				public CallInformation call() {
					final pjsua_call_info info = new pjsua_call_info();
					String sipUrl = null;
					String sipInfo = null;
					int status = PjsuaClient.PJ_SUCCESS;
					// calling call_get_info immediately after establishing the call sometimes returns null
					// wait for the info to be available
					while(sipUrl == null && sipInfo == null && status == PjsuaClient.PJ_SUCCESS) {
						status = PjsuaClient.call_get_info(callId, info);
						sipInfo = info.getRemote_info().getPtr();
						sipUrl = info.getRemote_contact().getPtr();
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// Ignore
						}
					}
					if(sipInfo == null) {
						sipInfo = sipUrl;
					}
					if(sipUrl == null) {
						sipUrl = sipInfo;
					}
					String name = extractNameFromSipContact(sipInfo);
					String number = extractUserFromSipContact(sipUrl);
					String display = PjsuaClient.buildDisplayString(number, name);
					CallInformation callerInfo = new CallInformation(null, name, number, display, sipUrl, callDirection);
					System.out.println("name: " + name + " number: " + number + " display: " + display);
					return callerInfo;
				}
			});
		}
		
		protected void invalidateActiveCallData() {
			activeCallId = -1;
			activeCallInfo = null;
		}
		
		protected void invalidateOnHoldCallData() {
			onHoldCallId = -1;
			onHoldCallInfo = null;
		}
		
		protected void invalidateRingingCallData() {
			ringingCallId = -1;
			ringingCallInfo = null;
		}
		
		protected void swapActiveAndOnHoldCallData() {
			int tempCallId = activeCallId;
			CallInformation tempCallInfo = activeCallInfo;
			activeCallId = onHoldCallId;
			activeCallInfo = onHoldCallInfo;
			onHoldCallId = tempCallId;
			onHoldCallInfo = tempCallInfo;
		}
		
		protected void swapRingingAndActiveCallData() {
			int tempCallId = ringingCallId;
			CallInformation tempCallInfo = ringingCallInfo;
			ringingCallId = activeCallId;
			ringingCallInfo = activeCallInfo;
			activeCallId = tempCallId;
			activeCallInfo = tempCallInfo;
		}
		
		protected void newIncomingCall(int callId) {
			logInvalidStateTransition(PhoneStateTransition.NEW_INCOMING_CALL, "not handled by state class"); //$NON-NLS-1$
			Activator.getLogger().info("Busy: hanging up unhandled incoming call"); //$NON-NLS-1$
			try {
				callHangupHelper(callId);
			}catch(Exception e) {
				// Already logged in Helper
			}
		}
		protected void callDisconnected(int callId, pjsip_status_code lastStatus) {
			logInvalidStateTransition(PhoneStateTransition.CALL_DISCONNECTED, "not handled by state class"); //$NON-NLS-1$
		}
		protected void callConnected(int callId) {
			logInvalidStateTransition(PhoneStateTransition.CALL_CONNECTED, "not handled by state class"); //$NON-NLS-1$
		}
		/** This method MUST be called from within the pjsuaWorker Thread
		 */
		protected void forwardCall(int callId, ForwardCallEntry forward) {
			pjsua_msg_data msgData = new pjsua_msg_data();
			msg_data_init(msgData);
			pjsua_call_info info = new pjsua_call_info();
			call_get_info(callId, info);
			//info.getLocal_contact().getPtr(); // <sip:6001@127.0.0.1:44056;transport=UDP>
			//info.getLocal_info().getPtr(); // <sip:6001@127.0.0.1>
			pjsip_generic_string_hdr headerDiversion = new pjsip_generic_string_hdr();
			pjsip_generic_string_hdr_init2(headerDiversion,
					pj_str_copy("Diversion"), pj_str_copy(info
							.getLocal_contact().getPtr()
							+ ";reason=\"unconditional\""));
			pjsip_msg_add_hdr(msgData, headerDiversion);
			pjsip_generic_string_hdr headerContact = new pjsip_generic_string_hdr();
			String sipNumber = PjsuaClient.createSipUrlFromNumber(forward
					.getNumber());
			pjsip_generic_string_hdr_init2(headerContact,
					pj_str_copy("Contact"), pj_str_copy("<" + sipNumber
							+ ">;reason=\"unconditional\""));
			pjsip_msg_add_hdr(msgData, headerContact);
			call_answer(callId, 302, null, msgData);
		}

		protected static void forwardCallDelayedIfRinging(int callId, ForwardCallEntry forward) {
			ForwardCallJob job = new ForwardCallJob(callId, forward);
			job.schedule(job.getDelay());
		}

		protected int dialNumberHelper(final String number) throws PjsipException {
			final int[] call_id = new int[1];
			int status = PjsuaClient.pjsuaWorker.syncExecNoExc(new Callable<Integer>() {
				@Override
				public Integer call() {
					return pjsua.call_make_call(defaultAccountId, pjsua.pj_str_copy(number), 0, null, null, call_id);
				}
			});
			if (status != pjsuaConstants.PJ_SUCCESS) {
				Activator.getLogger().error("Could not make call to " + number + ". status: " + status); //$NON-NLS-1$ //$NON-NLS-2$
				throw new PjsipException();
			}
			return call_id[0];
		}
		protected void callHangupHelper(final int callId) throws PjsipException {
			int status = PjsuaClient.pjsuaWorker.syncExecNoExc(new Callable<Integer>() {
				@Override
				public Integer call() {
					return call_hangup(callId, 0, null, null);
				}
			});
			if (status != pjsuaConstants.PJ_SUCCESS) {
				Activator.getLogger().error("Could not hangup call " + callId + ". status: " + status); //$NON-NLS-1$ //$NON-NLS-2$
				throw new PjsipException();
			}
		}
		protected void callHoldHelper(final int callId) throws PjsipException {
			int status = PjsuaClient.pjsuaWorker.syncExecNoExc(new Callable<Integer>() {
				@Override
				public Integer call() {
					return call_set_hold(callId, null);
				}
			});
			if (status != pjsuaConstants.PJ_SUCCESS) {
				Activator.getLogger().error("Could not put call " + callId + "on hold. status: " + status); //$NON-NLS-1$ //$NON-NLS-2$
				throw new PjsipException();
			}
		}
		protected void callUnholdHelper(final int callId) throws PjsipException {
			int status = PjsuaClient.pjsuaWorker.syncExecNoExc(new Callable<Integer>() {
				@Override
				public Integer call() {
					return call_reinvite(callId, pjsuaConstants.PJ_TRUE, null);
				}
			});
			if (status != pjsuaConstants.PJ_SUCCESS) {
				Activator.getLogger().error("Could not release hold for call " + callId + ". status: " + status); //$NON-NLS-1$ //$NON-NLS-2$
				throw new PjsipException();
			}
		}
		protected void makeStateTransition(PhoneState newState, PhoneStateTransition transition) {
			if(newState != phoneState) {
				System.out.println("trans to: " + newState);
				synchronized (phoneState) {
					previousPhoneState = phoneState;
					phoneState = newState;
					phoneStateTransition = transition;
					Activator.getLogger().info("Transition from phone state: " + previousPhoneState + " to: " + phoneState + " reason: " + transition.toString()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
				notifyListeners = true;
			}
		}
		protected void notifyPhoneStateListeners() {
			PjsuaClient.PhoneState previous = null;
			PjsuaClient.PhoneState current = null;
			synchronized (phoneState) {
				if(notifyListeners) {
					notifyListeners = false;
					previous = previousPhoneState;
					current = phoneState;
				}
			}
			if(current != null) {
				for (Object listener : phoneStateListeners.getListeners()) {
					((IPhoneStateListener) listener).stateChanged(previous, current, phoneStateTransition);
				}
			}
		}
		protected void notifyCallerInformationListeners() {
			//if(callerInformation != null) {
			for(Object listener: phoneStateListeners.getListeners()) {
				((IPhoneStateListener) listener).callerInformationChanged(activeCallInfo);
			}
			//}
		}
		protected void notifyConferenceListeners(boolean newEnabled) {
			for(Object listener: phoneStateListeners.getListeners()) {
				((IPhoneStateListener) listener).conferencingChanged(newEnabled);
			}
		}
		protected void logInvalidStateTransition(PhoneStateTransition transition, String message) {
			logInvalidStateTransition(transition, message, new RuntimeException());
		}
		protected void logInvalidStateTransition(PhoneStateTransition transition, String message, Throwable exception) {
			Activator.getLogger().error("Invalid PhoneStateTransition " + transition.name() + " in state " + phoneState.name() + " message: " + message, exception); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
	}

	
	/** Checks if a given message (SIP pager, IAX message) is an internal protocol message
	 * 
	 * @param message The message as received
	 * @return Whether it is internal and should be ignored (not shown to the user)
	 */
	public static boolean isProtocolMessage(String message) {
		return false;
	}

	/** Returns the shared dial destination string builder
	 * @return The dial destination string builder
	 */
	public static DialDestination getDialDestination() {
		return dialDestination;
	}

	public static synchronized String getSipUser() {
		return sipUser;
	}
	
	public static synchronized void setSipUser(String sipUser) {
		PjsuaClient.sipUser = sipUser;
	}
	
	public static synchronized String getSipPwd() {
		return sipPwd;
	}
	
	public static synchronized void setSipPwd(String sipPwd) {
		PjsuaClient.sipPwd = sipPwd;
	}
	
	public static PhoneState getPhoneState() {
		synchronized (phoneState) {
			return phoneState;
		}
	}
	
	public static void addPhoneStateListener(IPhoneStateListener listener) {
		phoneStateListeners.add(listener);
	}

	public static void removePhoneStateListener(IPhoneStateListener listener) {
		phoneStateListeners.remove(listener);
	}
	
	public static synchronized void initialisePjsua() throws RuntimeException {
		pjsuaWorker.assertPriorityViolation(true);
		if(!pjsuaWorkerThread.isAlive()) {
			pjsuaWorkerThread.start();
		}
		pjsuaWorker.syncExec(new Runnable() {
			@Override
			public void run() {
				/* Create pjsua first! */
				int status = create();
				if (status != pjsuaConstants.PJ_SUCCESS) {
					throw new RuntimeException("Error in pjsua_create(): " + status); //$NON-NLS-1$
				}
				
				/* Create pool for application */
				memoryPool = pjsua_pool_create("pjsua", 1000, 1000); //$NON-NLS-1$

				/* Init pjsua */
				pjsua_config cfg = new pjsua_config();
				pjsua_logging_config log_cfg = new pjsua_logging_config();

				config_default(cfg);
				
				cfg.setCb(pjsuaConstants.WRAPPER_CALLBACK_STRUCT);
				setCallbackObject(CALLBACK_OBJECT);
				
				mediaConfig = new pjsua_media_config();
				media_config_default(mediaConfig);
				// Disable silence suppression because of bug #330, see also:
				// http://bugs.digium.com/view.php?id=5374
				// XXX Re-enable as soon as that bug is solved
				mediaConfig.setNo_vad(pjsuaConstants.PJ_TRUE);

				logging_config_default(log_cfg);
				log_cfg.setConsole_level(PJSIP_LOG_LEVEL);

				status = init(cfg, log_cfg, mediaConfig);
				if (status != pjsuaConstants.PJ_SUCCESS) {
					throw new RuntimeException("Error in pjsua_init(): " + status); //$NON-NLS-1$
				}
				
				// Set sound levels and latency
				conf_adjust_rx_level(0, DEFAULT_SOUND_LEVEL);
				conf_adjust_tx_level(0, DEFAULT_SOUND_LEVEL);
				pjmedia_snd_set_latency(DEFAULT_SOUND_LATENCY, DEFAULT_SOUND_LATENCY);
				
				pcSpeakerWorkerThread.start();
				
//				final Integer[] result = new Integer[] {null};
//				pjsuaWorker.syncExec(new Runnable() {
//					@Override
//					public void run() {
//						result[0] = PjsuaClient.pj_thread_register("RunnableLoopWorker", pjsuaWorkderThreadDesc, new pj_thread_t());
//						thread = Thread.currentThread();
//					}
//				});
//				if (result[0] != pjsuaConstants.PJ_SUCCESS) {
//					throw new RuntimeException("Error in pj_thread_register(): " + result[0]); //$NON-NLS-1$
//				}
				
				isPjsuaInitialised = true;
			}
		});
	}
	
	public static synchronized void shutdownPjsua() {
		if(isPjsuaInitialised) {
			destroyTones();
			PjsuaClient.pjsuaWorker.syncExec(new Runnable() {
				@Override
				public void run() {
					if(ringSoundPort != null) {
						pjmedia_snd_port_destroy(ringSoundPort);
						ringSoundPort = null;
					}
					// Release memory
					pj_pool_release(memoryPool);
					// Destroy pjsua
					PjsuaClient.destroy();
				}
			});
		}
	}
	
	/** ATTENTION: This method MUST NOT be called from within the display thread
	 * in order to prevent deadlocks
	 */
	public static synchronized void connectToSipServer() throws RuntimeException {
		if (!isPjsuaInitialised) {
			initialisePjsua();
		}
		// Disable the phone tester
		removePhoneStateListener(phoneTester);
		
		// Put the phone into deregistered state
		phoneState.registrationFailed(defaultAccountId);
		
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		if (store == null) {
			throw new RuntimeException("Could not get the preference store"); //$NON-NLS-1$
		}

		// Runtime exceptions thrown within the runnable are thrown by the following call
		PjsuaClient.pjsuaWorker.syncExec(new Runnable() {
			@Override
			public void run() {
				String host = store.getString(AccountPreferencePage.PREF_SIP_HOST);
				int port = store.getInt(AccountPreferencePage.PREF_SIP_SERVER_PORT);
				String hostString = host + ":" + port; //$NON-NLS-1$

				String authRealm = store.getString(AccountPreferencePage.PREF_SIP_AUTH_REALM);

				/* Remove all accounts and all transports */
				//XXX To port Maybe we should hangup all calls too?
				Collection<pjsua_acc_info> accInfos = getAllAccountInfo();
				for(pjsua_acc_info info : accInfos) {
					acc_del(info.getId());
				}

				String publicIp = store.getString(AccountPreferencePage.PREF_SIP_NETWORK_ADDRESS);

				//XXX We only configure the network once on startup (removing transports did not work last time I checked)
				Collection<pjsua_transport_info> transportInfos = getAllTransportInfo();
				if(transportInfos.isEmpty()) {
					/* Add UDP transport. */
					pjsua_transport_config transp_cfg = new pjsua_transport_config();
					transport_config_default(transp_cfg);
					if(!publicIp.isEmpty()) {
						transp_cfg.setPublic_addr(pj_str_copy(publicIp));
					}
					int status = transport_create(pjsip_transport_type_e.PJSIP_TRANSPORT_UDP, transp_cfg, null);
					if (status != pjsuaConstants.PJ_SUCCESS) {
						throw new RuntimeException("Error creating SIP transport: " + status + " the UDP bind port you selected is probably already in use."); //$NON-NLS-1$ //$NON-NLS-2$
					}
					
					// We only add a media transport if we do not use the default IP
					// It is created automatically for us if we do not add it manually
					if(!publicIp.isEmpty()) {
						transp_cfg = new pjsua_transport_config();
						transport_config_default(transp_cfg);
						transp_cfg.setPublic_addr(pj_str_copy(publicIp));
						transp_cfg.setPort(4000); // This is just the port to start the search for a free port
						status = media_transports_create(transp_cfg);
						if (status != pjsuaConstants.PJ_SUCCESS) {
							throw new RuntimeException("Error creating RTP transport: " + status + "."); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				}

				/* Initialization is done, now start pjsua */
				int status = start();
				if (status != pjsuaConstants.PJ_SUCCESS) {
					throw new RuntimeException("Error starting pjsua: " + status); //$NON-NLS-1$
				}
				

				/* Register to SIP server by creating SIP account. */
				pjsua_acc_config cfg = new pjsua_acc_config();

				acc_config_default(cfg);
				cfg.setId(pj_str_copy("sip:" + sipUser + "@" + hostString)); //$NON-NLS-1$ //$NON-NLS-2$
				cfg.setReg_uri(pj_str_copy("sip:" + hostString)); //$NON-NLS-1$
				cfg.setCred_count(1);
				pjsip_cred_info cred_info = cfg.getCred_info();
				cred_info.setRealm(pj_str_copy(authRealm));
				cred_info.setScheme(pj_str_copy("Digest")); //$NON-NLS-1$
				cred_info.setUsername(pj_str_copy(sipUser));
				cred_info.setData_type(pjsip_cred_data_type.PJSIP_CRED_DATA_PLAIN_PASSWD.swigValue());
				cred_info.setData(pj_str_copy(sipPwd));

				int[] acc_id = new int[1];
				status = pjsua.acc_add(cfg, pjsuaConstants.PJ_TRUE, acc_id);
				if (status != pjsuaConstants.PJ_SUCCESS) {
					Activator.getLogger().error("Error adding account: " + status); //$NON-NLS-1$
				}
			}
		});

		
		// Attach the phone tester if enabled
		if(store.getBoolean(SoftPhonePreferencePage.PREF_STRESS_TEST_MODE)) {
			addPhoneStateListener(phoneTester);
		}
	}

	/** Attach a pjsua callback listener
	 * This method is deprecated, because calling library functions from within the
	 * listener can lead to deadlocks. You should use the phone state listener support.
	 * @param listener
	 */
	@Deprecated
	public static void addCallbackListener(Callback listener) {
//		callbackListeners.add(listener);
	}

	/** Remove a pjsua callback listener
	 * This method is deprecated, because calling library functions from within the
	 * listener can lead to deadlocks. You should use the phone state listener support.
	 * @param listener
	 */
	@Deprecated
	public static void removeCallbackListener(Callback listener) {
//		callbackListeners.remove(listener);
	}

	/** ATTENTION: This method MUST be executed within the {@code PjsuaClient#pjsuaWorker}
	 */
	public static synchronized void setAudioDevices() throws RuntimeException {
		if(Thread.currentThread() != pjsuaWorkerThread) {
			assert false : "setAudioDevices() MUST be executed within the pjsuaWorker thread";
			return;
		}
		if (!isPjsuaInitialised) {
			initialisePjsua();
		}
		PjsuaClient.pjsuaWorker.syncExec(new Runnable() {
			@Override
			public void run() {
				if(ringSoundPort != null) {
					pjmedia_snd_port_destroy(ringSoundPort);
					ringSoundPort = null;
				}
				IPreferenceStore store = Activator.getDefault().getPreferenceStore();
				if (store == null) {
					throw new RuntimeException("Could not get the preference store"); //$NON-NLS-1$
				}
				ringPcSpeaker = store.getBoolean(SoftPhonePreferencePage.PREF_RING_PCSPEAKER);
				long requiredToneSampleRate = -1;
				String inputDevicePreference = store.getString(SoftPhonePreferencePage.PREF_INPUT_DEVICE);
				String outputDevicePreference = store.getString(SoftPhonePreferencePage.PREF_OUTPUT_DEVICE);
				String ringDevicePreference = store.getString(SoftPhonePreferencePage.PREF_RING_DEVICE);
				Collection<Tuple<Integer, pjmedia_snd_dev_info>> audioDevices = getAllSoundCardInfo();
				if(!audioDevices.isEmpty() && 
						!inputDevicePreference.equals(SoftPhonePreferencePage.NULL_SOUND_DEVICE) &&
						!outputDevicePreference.equals(SoftPhonePreferencePage.NULL_SOUND_DEVICE)) {
					int[] inputDevice = {0};
					int[] outputDevice = {0};
					int[] ringDevice = {-1};
					pjmedia_snd_dev_info ringDeviceInfo = null;
					get_snd_dev(inputDevice, outputDevice);
					for(Tuple<Integer, pjmedia_snd_dev_info> device : audioDevices) {
						if(device.getSecond().getName().equals(inputDevicePreference)) {
							inputDevice[0] = device.getFirst();
						}
						if(device.getSecond().getName().equals(outputDevicePreference)) {
							outputDevice[0] = device.getFirst();
						}
						if(device.getSecond().getName().equals(ringDevicePreference)) {
							ringDevice[0] = device.getFirst();
							ringDeviceInfo = device.getSecond();
						}
					}
					set_snd_dev(inputDevice[0], outputDevice[0]);
					// Create the sound port for ringing on a separate device
					if(ringDevice[0] != -1) {
						ringSoundPort = new pjmedia_snd_port();
						requiredToneSampleRate = ringDeviceInfo.getDefault_samples_per_sec();
						int result = pjmedia_snd_port_create_player(memoryPool, ringDevice[0], ringDeviceInfo.getDefault_samples_per_sec(), mediaConfig.getChannel_count(), getDefaultSamplesPerFrame(), DEFAULT_BITS_PER_SAMPLE, 0, ringSoundPort);
						if(result != PJ_SUCCESS) {
							ringSoundPort = null;
							Activator.getLogger().error("Could not create player sound port for ringing device " + ringDevicePreference); //$NON-NLS-1$
						}
					}
				}else {
					// Disable sound IO
					set_null_snd_dev();
				}
				
				// Set sound levels and latency
				conf_adjust_rx_level(0, DEFAULT_SOUND_LEVEL);
				conf_adjust_tx_level(0, DEFAULT_SOUND_LEVEL);
				pjmedia_snd_set_latency(DEFAULT_SOUND_LATENCY, DEFAULT_SOUND_LATENCY);
				
				// Generate ring tones
				generateTones(requiredToneSampleRate);
			}
		});
	}

	/** ATTENTION: This method MUST be executed within the {@code PjsuaClient#pjsuaWorker}
	 */
	private static Collection<pjsua_call_info> getAllCallInfo() {
		int numCalls = (int) call_get_count();
		List<pjsua_call_info> callInfoList = new ArrayList<pjsua_call_info>(numCalls);
		if(numCalls > 0) {
			long[] count = new long[1];
			count[0] = numCalls;
			int[] callIds = new int[numCalls];
			int status = enum_calls(callIds, count);
			if (status != pjsuaConstants.PJ_SUCCESS) {
				throw new RuntimeException("pjsua enum_calls failed: " + status); //$NON-NLS-1$
			}
			for (int id : callIds) {
				pjsua_call_info info = new pjsua_call_info();
				status = call_get_info(id, info);
				if (status != pjsuaConstants.PJ_SUCCESS) {
					throw new RuntimeException("pjsua call_get_info failed: " + status); //$NON-NLS-1$
				}
				callInfoList.add(info);
			}
		}
		return callInfoList;
	}
	
	/** ATTENTION: This method MUST be executed within the {@code PjsuaClient#pjsuaWorker}
	 */
	private static Collection<pjsua_acc_info> getAllAccountInfo() {
		int numAccounts = (int)acc_get_count();
		List<pjsua_acc_info> accInfoList = new ArrayList<pjsua_acc_info>(numAccounts);
		if(numAccounts > 0) {
			long[] count = new long[1];
			count[0] = numAccounts;
			int[] accountIds = new int[numAccounts];
			int status = enum_accs(accountIds, count);
			if (status != pjsuaConstants.PJ_SUCCESS) {
				throw new RuntimeException("pjsua enum_accs failed: " + status); //$NON-NLS-1$
			}
			for (int id : accountIds) {
				pjsua_acc_info info = new pjsua_acc_info();
				status = acc_get_info(id, info);
				if (status != pjsuaConstants.PJ_SUCCESS) {
					throw new RuntimeException("pjsua acc_get_info failed: " + status); //$NON-NLS-1$
				}
				accInfoList.add(info);
			}
		}
		return accInfoList;
	}
	
	/** ATTENTION: This method MUST be executed within the {@code PjsuaClient#pjsuaWorker}
	 */
	private static Collection<pjsua_transport_info> getAllTransportInfo() {
		int numTransports = (int)transport_get_count();
		List<pjsua_transport_info> transportInfoList = new ArrayList<pjsua_transport_info>(numTransports);
		if(numTransports > 0) {
			long[] count = new long[1];
			count[0] = numTransports;
			int[] transportIds = new int[numTransports];
			int status = enum_transports(transportIds, count);
			if (status != pjsuaConstants.PJ_SUCCESS) {
				throw new RuntimeException("pjsua enum_accs failed: " + status); //$NON-NLS-1$
			}
			for (int id : transportIds) {
				pjsua_transport_info info = new pjsua_transport_info();
				status = transport_get_info(id, info);
				if (status != pjsuaConstants.PJ_SUCCESS) {
					throw new RuntimeException("pjsua acc_get_info failed: " + status); //$NON-NLS-1$
				}
				transportInfoList.add(info);
			}
		}
		return transportInfoList;
	}
	
	/** ATTENTION: This method MUST be executed within the {@code PjsuaClient#pjsuaWorker}
	 */
	public static Collection<Tuple<Integer, pjmedia_snd_dev_info>> getAllSoundCardInfo() {
		if(Thread.currentThread() != pjsuaWorkerThread) {
			assert false : "setAudioDevices() MUST be executed within the pjsuaWorker thread";
			return null;
		}
		int numDevices = (int)snd_get_dev_count();
		List<Tuple<Integer, pjmedia_snd_dev_info>> soundDeviceInfoList = new ArrayList<Tuple<Integer, pjmedia_snd_dev_info>>(numDevices);
		if(numDevices > 0) {
			int status = 0;
			for (int id = 0; id < numDevices; id++) {
				pjmedia_snd_dev_info info = new pjmedia_snd_dev_info();
				status = get_snd_dev_info(info, id);
				if (status != pjsuaConstants.PJ_SUCCESS) {
					throw new RuntimeException("pjsua acc_get_info failed: " + status); //$NON-NLS-1$
				}
				soundDeviceInfoList.add(new Tuple<Integer, pjmedia_snd_dev_info>(id, info));
			}
		}
		return soundDeviceInfoList;
	}
	
	/** Try to create a valid sip URL from the given number
	 * 
	 * This method uses the server the phone is registered to in
	 * order to create the '@HOSTNAME' portion and prepends 'sip:'
	 * if that is missing.
	 * @param number The number to process
	 * @return An improved (and probably valid) SIP URL
	 */
	public static String createSipUrlFromNumber(String number) {
		String sipUrl = number;

		if (!number.contains("/") && !number.contains("@")) { //$NON-NLS-1$ //$NON-NLS-2$
			IPreferenceStore store = Activator.getDefault().getPreferenceStore();
			String host = store.getString(AccountPreferencePage.PREF_SIP_HOST);
			int port = store.getInt(AccountPreferencePage.PREF_SIP_SERVER_PORT);
			sipUrl = "sip:" + number + "@" + host + ":" + port; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}else if(!number.startsWith("sip:")){ //$NON-NLS-1$
			sipUrl = "sip:" + number; //$NON-NLS-1$
		}
		return sipUrl;
	}
	
	/** Try to simplify the given sip URL
	 * 
	 * This method uses the server the phone is registered to in
	 * order to strip unneeded parts from the given sip url.
	 * @param sipUrl The URL to simplify
	 * @return A stripped sip URL number
	 */
	public static String simplifySipUrl(String sipUrl) {
		if(sipUrl != null) {
			StringBuilder simpleNumber = new StringBuilder(sipUrl);
			if(sipUrl.startsWith("<") && sipUrl.endsWith(">")) { //$NON-NLS-1$ //$NON-NLS-2$
				simpleNumber.replace(0, 1, ""); //$NON-NLS-1$
				simpleNumber.replace(simpleNumber.length()-1, simpleNumber.length(), ""); //$NON-NLS-1$
			}
			if(simpleNumber.toString().startsWith("sip:")) { //$NON-NLS-1$
				simpleNumber.replace(0, "sip:".length(), ""); //$NON-NLS-1$ //$NON-NLS-2$
			}
			if(sipUrl.contains("@")) { //$NON-NLS-1$
				int atSignIndex = simpleNumber.indexOf("@"); //$NON-NLS-1$
				if(atSignIndex >= 0) {
					String hostPart = simpleNumber.substring(atSignIndex+1, simpleNumber.length());
					IPreferenceStore store = Activator.getDefault().getPreferenceStore();
					String host = store.getString(AccountPreferencePage.PREF_SIP_HOST);
					int portSignIndex = simpleNumber.indexOf(":", atSignIndex); //$NON-NLS-1$
					if(portSignIndex > 0) {
						host += ":" + store.getInt(AccountPreferencePage.PREF_SIP_SERVER_PORT); //$NON-NLS-1$
					}
					if(hostPart.equals(host)) {
						simpleNumber.replace(atSignIndex, simpleNumber.length(), ""); //$NON-NLS-1$
					}else if(hostPart.equals("127.0.0.1")) { //$NON-NLS-1$
						simpleNumber.replace(atSignIndex, simpleNumber.length(), ""); //$NON-NLS-1$
					}
				}
			}
			return simpleNumber.toString();
		}else {
			return null;
		}
	}
	
	/** Extracts the name part from a sip contact string like this:
	 * "Agent7705" <sip:7705@127.0.0.1>
	 * If there is no name part, this method returns the username (7705)
	 * @param sipContact The sip contact to parse
	 * @return The name part if it exists, otherwise the user or null if the sip URL is invalid
	 */
	public static String extractNameFromSipContact(String sipContact) {
		String trimmedSip = sipContact.trim();
		Matcher matcher = SIP_CONTACT_NAME_PATTERN.matcher(trimmedSip);
		if(matcher.find() && matcher.groupCount() > 0) {
			return matcher.group(1);
		}
		matcher = SIP_CONTACT_USER_PATTERN.matcher(trimmedSip);
		if(matcher.find() && matcher.groupCount() > 0) {
			return matcher.group(1);
		}
		// Invalid sip url
		return null;
	}
	
	/** Extracts the user part from a sip contact string like this:
	 * "Agent7705" <sip:7705@127.0.0.1>
	 * @param sipContact The sip contact to parse
	 * @return The user part if it exists or null if the sip URL is invalid
	 */
	public static String extractUserFromSipContact(String sipContact) {
		String trimmedSip = sipContact.trim();
		Matcher matcher = SIP_CONTACT_USER_PATTERN.matcher(trimmedSip);
		if(matcher.find() && matcher.groupCount() > 0) {
			return matcher.group(1);
		}
		// Invalid sip url
		return null;
	}
	
	public static String buildDisplayString(String callerId, String callerName) {
		if(callerId == null) {
			callerId = "";
		}
		if(callerName == null) {
			callerName = callerId;
		}
		StringBuilder display = new StringBuilder();
		if(!callerName.isEmpty()) {
			display.append("\"").append(callerName).append("\" ");
		}
		display.append(callerId);
		return display.toString();
	}
	
	protected static long getDefaultSamplesPerFrame() {
		return mediaConfig.getAudio_frame_ptime() * mediaConfig.getClock_rate()
		* mediaConfig.getChannel_count() / 1000;
	}
	
	private static void destroyTones() {
		if(ringToneBridge != null) {
			ringToneBridge.destroyTone();
		}
		if(ringToneRingingDevice != null) {
			ringToneRingingDevice.destroyTone();
		}
		if(ringBackToneBridge != null) {
			ringBackToneBridge.destroyTone();
		}
		if(ringBackToneRingingDevice != null) {
			ringBackToneRingingDevice.destroyTone();
		}
	}
	
	/** Creates the tones for the phone with the specified sample rate for the ringing device
	 * 
	 * The default sample rate is used for the ring tones which are connected to the conference bridge
	 * @param ringingDeviceSampleRate If -1 the default sample rate will be used. Specified in kHz.
	 */
	protected static void generateTones(long ringingDeviceSampleRate) {
		destroyTones();
		if(ringingDeviceSampleRate < 0) {
			ringingDeviceSampleRate = mediaConfig.getClock_rate();
		}
		long samples_per_frame = getDefaultSamplesPerFrame();
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		List<RingtoneSpecification> ringtoneSpecs = new LinkedList<RingtoneSpecification>();
		
		ringtoneSpecs.add(new RingtoneSpecification(store, Arrays.asList(SoftPhonePreferencePage.RING_TONE_PARAMETERS)));
		ringToneBridge = new ConferenceBridgeRingtone(memoryPool, mediaConfig, mediaConfig.getClock_rate(),
				samples_per_frame, ringtoneSpecs);
		try {
			ringToneBridge.createTone();
		}catch(Exception e) {
			Activator.getLogger().error("Creating the ring tone for the conference bridge failed", e); //$NON-NLS-1$
		}
		ringToneRingingDevice = new AudioDeviceRingtone(memoryPool, mediaConfig, ringingDeviceSampleRate,
				samples_per_frame, ringtoneSpecs, ringSoundPort);
		try {
			ringToneRingingDevice.createTone();
		}catch(Exception e) {
			Activator.getLogger().error("Creating the ring tone for the ringing device failed", e); //$NON-NLS-1$
		}
		
		ringtoneSpecs.clear();
		ringtoneSpecs.add(new RingtoneSpecification(store, Arrays.asList(SoftPhonePreferencePage.RING_BACK_TONE_PARAMETERS)));
		ringBackToneBridge = new ConferenceBridgeRingtone(memoryPool, mediaConfig, mediaConfig.getClock_rate(),
				samples_per_frame, ringtoneSpecs);
		try {
			ringBackToneBridge.createTone();
		}catch(Exception e) {
			Activator.getLogger().error("Creating the ring back tone for the conference bridge failed", e); //$NON-NLS-1$
		}
		ringBackToneRingingDevice = new AudioDeviceRingtone(memoryPool, mediaConfig, ringingDeviceSampleRate,
				samples_per_frame, ringtoneSpecs, ringSoundPort);
		try {
			ringBackToneRingingDevice.createTone();
		}catch(Exception e) {
			Activator.getLogger().error("Creating the ring back tone for the ringing device failed", e); //$NON-NLS-1$
		}

		ringtoneSpecs.clear();
		RingtoneSpecification spec = new RingtoneSpecification();
		spec.freq1 = (short)store.getInt(SoftPhonePreferencePage.PREF_SPECIALTONE_FREQ1);
		spec.freq2 = 0;
		spec.onMs = (short)store.getInt(SoftPhonePreferencePage.PREF_SPECIALTONE_ON_MS);
		spec.offMs = 0;
		spec.ringCount = 1;
		spec.intervalMs = 0;
		spec.volume = (short)store.getInt(SoftPhonePreferencePage.PREF_SPECIALTONE_VOLUME);
		ringtoneSpecs.add(spec);
		spec = new RingtoneSpecification();
		spec.freq1 = (short)store.getInt(SoftPhonePreferencePage.PREF_SPECIALTONE_FREQ2);
		spec.freq2 = 0;
		spec.onMs = (short)store.getInt(SoftPhonePreferencePage.PREF_SPECIALTONE_ON_MS);
		spec.offMs = 0;
		spec.ringCount = 1;
		spec.intervalMs = 0;
		spec.volume = (short)store.getInt(SoftPhonePreferencePage.PREF_SPECIALTONE_VOLUME);
		ringtoneSpecs.add(spec);
		spec = new RingtoneSpecification();
		spec.freq1 = (short)store.getInt(SoftPhonePreferencePage.PREF_SPECIALTONE_FREQ3);
		spec.freq2 = 0;
		spec.onMs = (short)store.getInt(SoftPhonePreferencePage.PREF_SPECIALTONE_ON_MS);
		spec.offMs = 0;
		spec.ringCount = 1;
		spec.intervalMs = (short)store.getInt(SoftPhonePreferencePage.PREF_SPECIALTONE_INTERVAL_MS);
		spec.volume = (short)store.getInt(SoftPhonePreferencePage.PREF_SPECIALTONE_VOLUME);
		ringtoneSpecs.add(spec);
		specialToneBridge = new ConferenceBridgeRingtone(memoryPool, mediaConfig, mediaConfig.getClock_rate(),
				samples_per_frame, ringtoneSpecs);
		try {
			specialToneBridge.createTone();
		}catch(Exception e) {
			Activator.getLogger().error("Creating the congestion tone for the conference bridge failed", e);
		}
		specialToneRingingDevice = new AudioDeviceRingtone(memoryPool, mediaConfig, ringingDeviceSampleRate,
				samples_per_frame, ringtoneSpecs, ringSoundPort);
		try {
			specialToneRingingDevice.createTone();
		}catch(Exception e) {
			Activator.getLogger().error("Creating the congestion tone for the ringing device failed", e);
		}
}

	public static void ringBackStop() {
		ringBackToneBridge.stopRinging();
		ringBackToneRingingDevice.stopRinging();
	}
	
	public static void ringBackStart() {
		ringBackToneBridge.startRinging();
		ringBackToneRingingDevice.startRinging();
	}

	public static void ringStop() {
		ringToneBridge.stopRinging();
		ringToneRingingDevice.stopRinging();
		pcSpeakerWorker.beepOff();
	}
	
	public static void ringStart() {
		ringToneBridge.startRinging();
		ringToneRingingDevice.startRinging();
		if(ringPcSpeaker) {
			pcSpeakerWorker.setInterval(2000);
			pcSpeakerWorker.beepOn();
		}
	}
	
	public static void specialToneStop() {
		specialToneBridge.stopRinging();
		specialToneRingingDevice.stopRinging();
		pcSpeakerWorker.beepOff();
	}
	
	public static void specialToneStart() {
		specialToneBridge.startRinging();
		specialToneRingingDevice.startRinging();
		if(ringPcSpeaker) {
			pcSpeakerWorker.setInterval(300);
			pcSpeakerWorker.beepOn();
		}
	}
	
	public static Collection<String> getInternalNumbers() {
		return Arrays.asList(internalNumbers);
	}
}