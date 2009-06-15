package org.acoveo.callcenter.sipclient;

import java.util.List;
import java.util.concurrent.Callable;

import org.pjsip.pjsua.pj_pool_t;
import org.pjsip.pjsua.pjmedia_port;
import org.pjsip.pjsua.pjmedia_tone_desc;
import org.pjsip.pjsua.pjsua;
import org.pjsip.pjsua.pjsuaConstants;
import org.pjsip.pjsua.pjsua_media_config;

public abstract class AbstractRingtone implements IRingtone {
	// The default number of bits per sample
	public static int DEFAULT_BITS_PER_SAMPLE = 16;
	// Hold the sample rate which was used to create the tone
	protected long ringToneSampleRate = -1;
	// Holds the number of samples per frame which was used to create this ringtone
	protected long samplesPerFrame = -1;
	// The ring port (holds the ringtone)
	protected pjmedia_port ringPort = null;
	// The memory pool which was used to create this tone
	protected pj_pool_t memoryPool;
	// The media config to retrieve certain standard parameters
	protected pjsua_media_config mediaConfig;
	// Whether the ringtone is playing
	protected boolean isRinging = false;
	// The parameters of the ringtone
	protected List<RingtoneSpecification> ringParams;
	
	public AbstractRingtone(pj_pool_t memoryPool, pjsua_media_config mediaConfig, long sampleRate, long samplesPerFrame, List<RingtoneSpecification> ringParams) {
		this.memoryPool = memoryPool;
		this.mediaConfig = mediaConfig;
		this.ringToneSampleRate = sampleRate;
		this.samplesPerFrame = samplesPerFrame;
		this.ringParams = ringParams;
	}
	
	private boolean toneCreated = false;

	/* (non-Javadoc)
	 * @see org.acoveo.callcenter.sipclient.IRingtone#createTone()
	 */
	public synchronized void createTone() throws Exception {
		if(toneCreated) {
			// Nothing to do
			return;
		}
		if(ringToneSampleRate < 0 || samplesPerFrame < 0) {
			// XXX Throw an exception
			return;
		}
		int toneDescCount = 0;
		for(RingtoneSpecification ringSpec : ringParams) {
			toneDescCount += ringSpec.ringCount;
		}
		final pjmedia_tone_desc tones[] = new pjmedia_tone_desc[toneDescCount];

		/* Ring tone (call is ringing) */
		ringPort = new pjmedia_port();
		int status = PjsuaClient.pjsuaWorker.syncExec(new Callable<Integer>() {
			@Override
			public Integer call() {
				return PjsuaClient.pjmedia_tonegen_create2(memoryPool, pjsua.pj_str_copy("ring"), ringToneSampleRate, //$NON-NLS-1$
						mediaConfig.getChannel_count(), samplesPerFrame, DEFAULT_BITS_PER_SAMPLE, pjsuaConstants.PJMEDIA_TONEGEN_LOOP, ringPort);
			}
		});
		if (status != pjsuaConstants.PJ_SUCCESS) {
			// XXX Implement proper exception handling
			ringPort = null;
			throw new Exception("Creating the ringtone failed because pjmedia_tonegen_create2 returned " + status); //$NON-NLS-1$
		}
		
		int toneIndex = 0;
		for(RingtoneSpecification ringSpec : ringParams) {
			for (int i = 0; i < ringSpec.ringCount; ++i) {
				pjmedia_tone_desc tone = new pjmedia_tone_desc();
				tone.setFreq1(ringSpec.freq1);
				tone.setFreq2(ringSpec.freq2);
				tone.setOn_msec(ringSpec.onMs);
				tone.setOff_msec(ringSpec.offMs);
				tone.setVolume(ringSpec.volume);
				tones[toneIndex] = tone;
				toneIndex++;
			}
		}
		
		tones[toneDescCount - 1].setOff_msec(ringParams.get(ringParams.size()-1).intervalMs);

		final int finalToneDescCount = toneDescCount;
		status = PjsuaClient.pjsuaWorker.syncExec(new Callable<Integer>() {
			@Override
			public Integer call() {
				return PjsuaClient.pjmedia_tonegen_play(ringPort, finalToneDescCount, tones, pjsuaConstants.PJMEDIA_TONEGEN_LOOP);
			}
		});
		createToneExtended();
		toneCreated = true;
	}
	
	protected abstract void createToneExtended() throws Exception;
	
	/* (non-Javadoc)
	 * @see org.acoveo.callcenter.sipclient.IRingtone#destroyTone()
	 */
	public synchronized void destroyTone() {
		destroyToneExtended();
		
		if(ringPort != null) {
			PjsuaClient.pjsuaWorker.syncExec(new Runnable() {
				@Override
				public void run() {
					PjsuaClient.pjmedia_port_destroy(ringPort);
				}
			});
			ringPort = null;
		}
		toneCreated = false;
	}
	
	protected abstract void destroyToneExtended();
	
	/* (non-Javadoc)
	 * @see org.acoveo.callcenter.sipclient.IRingtone#startRinging()
	 */
	public synchronized void startRinging() {
		if(!toneCreated) {
			Activator.getLogger().error("startRinging called, although the tone was not created", new RuntimeException()); //$NON-NLS-1$
			return;
		}
		isRinging = true;
		startRingingExtended();
	}
	
	protected abstract void startRingingExtended();
	
	/* (non-Javadoc)
	 * @see org.acoveo.callcenter.sipclient.IRingtone#stopRinging()
	 */
	public synchronized void stopRinging() {
		if(!toneCreated) {
			Activator.getLogger().error("startRinging called, although the tone was not created", new RuntimeException()); //$NON-NLS-1$
			return;
		}
		stopRingingExtended();
		PjsuaClient.pjsuaWorker.syncExec(new Runnable() {
			@Override
			public void run() {
				PjsuaClient.pjmedia_tonegen_rewind(ringPort);
			}
		});
		isRinging = false;
	}
	protected abstract void stopRingingExtended();
	
	/* (non-Javadoc)
	 * @see org.acoveo.callcenter.sipclient.IRingtone#isRinging()
	 */
	public boolean isRinging() {
		return isRinging;
	}
}
