package org.acoveo.callcenter.sipclient;

import java.util.List;
import java.util.concurrent.Callable;

import org.pjsip.pjsua.pj_pool_t;
import org.pjsip.pjsua.pjsua;
import org.pjsip.pjsua.pjsuaConstants;
import org.pjsip.pjsua.pjsua_media_config;

public class ConferenceBridgeRingtone extends AbstractRingtone {
	// The conference slot of the ring port tone
	protected int ringSlot = pjsuaConstants.PJSUA_INVALID_ID;

	public ConferenceBridgeRingtone(pj_pool_t memoryPool, pjsua_media_config mediaConfig, long sampleRate, long samplesPerFrame, List<RingtoneSpecification> ringParams) {
		super(memoryPool, mediaConfig, sampleRate, samplesPerFrame, ringParams);
	}

	@Override
	protected void createToneExtended() throws Exception {
		if(ringSlot != pjsuaConstants.PJSUA_INVALID_ID) {
			// Already added
			return;
		}
		final int[] ringSlotArray = { 0 };
		int status = PjsuaClient.pjsuaWorker.syncExec(new Callable<Integer>() {
			@Override
			public Integer call() {
				return pjsua.conf_add_port(memoryPool, ringPort, ringSlotArray);
			}
		});
		if (status != pjsuaConstants.PJ_SUCCESS) {
			throw new Exception("Adding the ringtone port failed because conf_add_port returned " + status); //$NON-NLS-1$
		}
		ringSlot = ringSlotArray[0];
	}

	@Override
	protected void destroyToneExtended() {
		if(ringSlot == pjsuaConstants.PJSUA_INVALID_ID) {
			// Not added
			return;
		}
		PjsuaClient.pjsuaWorker.syncExec(new Runnable() {
			@Override
			public void run() {
				if(isRinging) {
					PjsuaClient.conf_disconnect(ringSlot, 0);
				}
				PjsuaClient.conf_remove_port(ringSlot);
			}
		});
		ringSlot = pjsuaConstants.PJSUA_INVALID_ID;
	}

	@Override
	protected void startRingingExtended() {
		if(ringSlot != pjsuaConstants.PJSUA_INVALID_ID) {
			PjsuaClient.pjsuaWorker.syncExec(new Runnable() {
				@Override
				public void run() {
					PjsuaClient.conf_connect(ringSlot, 0);
				}
			});
		}
	}

	@Override
	protected void stopRingingExtended() {
		if(ringSlot != pjsuaConstants.PJSUA_INVALID_ID) {
			PjsuaClient.pjsuaWorker.syncExec(new Runnable() {
				@Override
				public void run() {
					PjsuaClient.conf_disconnect(ringSlot, 0);
				}
			});
		}
	}

}
