package org.acoveo.callcenter.sipclient;

import java.util.List;

import org.pjsip.pjsua.pj_pool_t;
import org.pjsip.pjsua.pjmedia_snd_port;
import org.pjsip.pjsua.pjsua;
import org.pjsip.pjsua.pjsua_media_config;

public class AudioDeviceRingtone extends AbstractRingtone {
	protected pjmedia_snd_port ringSoundPort;
	
	public AudioDeviceRingtone(pj_pool_t memoryPool, pjsua_media_config mediaConfig, long sampleRate, long samplesPerFrame, List<RingtoneSpecification> ringParams, pjmedia_snd_port ringSoundPort) {
		super(memoryPool, mediaConfig, sampleRate, samplesPerFrame, ringParams);
		this.ringSoundPort = ringSoundPort;
	}

	@Override
	protected void createToneExtended() {
	}

	@Override
	protected void destroyToneExtended() {
		if(isRinging && ringSoundPort != null) {
			PjsuaClient.pjsuaWorker.syncExec(new Runnable() {
				@Override
				public void run() {
					pjsua.pjmedia_snd_port_disconnect(ringSoundPort);
				}
			});
		}
	}

	@Override
	protected void startRingingExtended() {
		if(ringSoundPort != null) {
			PjsuaClient.pjsuaWorker.syncExec(new Runnable() {
				@Override
				public void run() {
					pjsua.pjmedia_snd_port_connect(ringSoundPort, ringPort);
				}
			});
		}
	}

	@Override
	protected void stopRingingExtended() {
		if(ringSoundPort != null) {
			PjsuaClient.pjsuaWorker.syncExec(new Runnable() {
				@Override
				public void run() {
					pjsua.pjmedia_snd_port_disconnect(ringSoundPort);
				}
			});
		}
	}

}
