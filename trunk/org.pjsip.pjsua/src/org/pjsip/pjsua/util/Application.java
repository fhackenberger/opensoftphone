package org.pjsip.pjsua.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.pjsip.pjsua.Callback;
import org.pjsip.pjsua.SWIGTYPE_p_pjsip_rx_data;
import org.pjsip.pjsua.pjsip_cred_data_type;
import org.pjsip.pjsua.pjsip_cred_info;
import org.pjsip.pjsua.pjsip_transport_type_e;
import org.pjsip.pjsua.pjsua;
import org.pjsip.pjsua.pjsuaConstants;
import org.pjsip.pjsua.pjsua_acc_config;
import org.pjsip.pjsua.pjsua_call_info;
import org.pjsip.pjsua.pjsua_call_media_status;
import org.pjsip.pjsua.pjsua_config;
import org.pjsip.pjsua.pjsua_logging_config;
import org.pjsip.pjsua.pjsua_transport_config;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {
	public static long UDP_BIND_PORT = 5062;
	public static String SIP_USER = "6002";
	public static String SIP_PASSWORD = "1234";
	public static String SIP_DOMAIN = "localhost";
	public static String AUTH_REALM = "asterisk";
	protected static String CALL_SIP_URL = "sip:6003@localhost";
	protected static String SIP_URL = "sip:6002@hlocalhost";
	
	class PjsuaCallback extends Callback {
		@Override
		public void on_incoming_call(int acc_id, int call_id, SWIGTYPE_p_pjsip_rx_data rdata) {
			// TODO Auto-generated method stub
			super.on_incoming_call(acc_id, call_id, rdata);
		}
		
		/* Callback called by the library when call's media state has changed */
		@Override
		public void on_call_media_state(int call_id)
		{
		    pjsua_call_info info = new pjsua_call_info();

		    pjsua.call_get_info(call_id, info);

		    if (info.getMedia_status() == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE) {
		        // When media is active, connect call to sound device.
		    	pjsua.conf_connect(info.getConf_slot(), 0);
		    	pjsua.conf_connect(0, info.getConf_slot());
		    }
		}
	}
	
	protected void error_exit(String message, int status) {
		System.out.println("Exit with status code: " + status + " and message: " + message);
		System.exit(status);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
	 */
	public Object start(IApplicationContext context) throws Exception {
	    int[] acc_id = new int[1];
	    int status;

	    /* Create pjsua first! */
	    status = pjsua.create();
	    if (status != pjsuaConstants.PJ_SUCCESS) {
	    	error_exit("Error in pjsua_create()", status);
	    }

	    /* If argument is specified, it's got to be a valid SIP URL */
        status = pjsua.verify_sip_url(CALL_SIP_URL);
        if (status != pjsuaConstants.PJ_SUCCESS) error_exit("Invalid URL in argv", status);

	    /* Init pjsua */
	    {
	        pjsua_config cfg = new pjsua_config();
	        pjsua_logging_config log_cfg = new pjsua_logging_config();

	        pjsua.config_default(cfg);
	        
	        cfg.setCb(pjsuaConstants.WRAPPER_CALLBACK_STRUCT);
	        pjsua.setCallbackObject(new PjsuaCallback());

	        pjsua.logging_config_default(log_cfg);
	        log_cfg.setConsole_level(4);

	        status = pjsua.init(cfg, log_cfg, null);
	        if (status != pjsuaConstants.PJ_SUCCESS) error_exit("Error in pjsua_init()", status);
	    }

	    /* Add UDP transport. */
	    {
	        pjsua_transport_config cfg = new pjsua_transport_config();

	        pjsua.transport_config_default(cfg);
	        cfg.setPort(UDP_BIND_PORT);
	        status = pjsua.transport_create(pjsip_transport_type_e.PJSIP_TRANSPORT_UDP, cfg, null);
	        if (status != pjsuaConstants.PJ_SUCCESS) error_exit("Error creating transport", status);
	    }

	    /* Initialization is done, now start pjsua */
	    status = pjsua.start();
	    if (status != pjsuaConstants.PJ_SUCCESS) error_exit("Error starting pjsua", status);

	    /* Register to SIP server by creating SIP account. */
	    {
	        pjsua_acc_config cfg = new pjsua_acc_config();

	        pjsua.acc_config_default(cfg);
	        cfg.setId(pjsua.pj_str_copy("sip:" + SIP_USER + "@" + SIP_DOMAIN));
	        cfg.setReg_uri(pjsua.pj_str_copy("sip:" + SIP_DOMAIN));
	        cfg.setCred_count(1);
	        pjsip_cred_info cred_info = cfg.getCred_info();
	        cred_info.setRealm(pjsua.pj_str_copy(AUTH_REALM));
	        cred_info.setScheme(pjsua.pj_str_copy("Digest"));
	        cred_info.setUsername(pjsua.pj_str_copy(SIP_USER));
	        cred_info.setData_type(pjsip_cred_data_type.PJSIP_CRED_DATA_PLAIN_PASSWD.swigValue());
	        cred_info.setData(pjsua.pj_str_copy(SIP_PASSWORD));

	        status = pjsua.acc_add(cfg, pjsuaConstants.PJ_TRUE, acc_id);
	        if (status != pjsuaConstants.PJ_SUCCESS) error_exit("Error adding account", status);
	    }

	    /* If URL is specified, make call to the URL. */
	    if (true) {
	    	int[] call_id = new int[1];
	        status = pjsua.call_make_call(acc_id[0], pjsua.pj_str_copy(CALL_SIP_URL), 0, null, null, call_id);
	        if (status != pjsuaConstants.PJ_SUCCESS) error_exit("Error making call", status);
	    }

	    /* Wait until user press "q" to quit. */
	    for (;;) {
	        System.out.println("Press 'h' to hangup all calls, 'q' to quit");
	        BufferedReader inBuffReader = new BufferedReader(new InputStreamReader(System.in));
	        String userInput = inBuffReader.readLine();

	        if (userInput.equals("q"))
	            break;

	        if (userInput.equals("h"))
	            pjsua.call_hangup_all();
	    }

	    /* Destroy pjsua */
	    pjsua.destroy();

		return IApplication.EXIT_OK;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {

	}
}
