/* File : pjsua.i */
%module (directors="1") pjsua
%include "typemaps.i"
%include "enums.swg"
%include "arrays_java.i";
%include "carrays.i";

/* void* shall be handled as byte arrays */
%typemap(jni) void * "void *"
%typemap(jtype) void * "byte[]"
%typemap(jstype) void * "byte[]"
%typemap(javain) void * "$javainput"
%typemap(in) void * %{
	$1 = $input;
%}
// Does not work yet (maybe a bug?)
// This could prove useful for the implementation:
// http://www.nabble.com/c-function-returning-an-array-(access-from-java)-td4242120.html#a4287671
/* %typemap(directorin,descriptor="[B") void * %{
	$input = new ByteArray();
%} */
%typemap(javadirectorin) void * "$jniinput"
%typemap(out) void * %{ 
	$result = $1; 
%}
/* %typemap(directorout) void * %{
	$result = (void *)$input;
%}*/
%typemap(javaout) void * {
	return $jnicall;
}

/////////////////////////////////////
// Typemaps for pjmedia_port
/////////////////////////////////////
// Do not generate the default proxy constructor or destructor
%nodefaultctor pjmedia_port;
%nodefaultdtor pjmedia_port;

// Add in pure Java code proxy constructor
%typemap(javacode) pjmedia_port %{
  /** This constructor creates the proxy which initially does not create nor own any C memory */
  public pjmedia_port() {
    this(0, false);
  }
%}

// Type typemaps for marshalling pjmedia_port **
%typemap(jni) pjmedia_port **p_port "jobject"
%typemap(jtype) pjmedia_port **p_port "pjmedia_port"
%typemap(jstype) pjmedia_port **p_port "pjmedia_port"

// Typemaps for pjmedia_port ** as a parameter output type
%typemap(in) pjmedia_port **p_port (pjmedia_port *ppMediaPort = 0) %{
  $1 = &ppMediaPort;
%}
%typemap(argout) pjmedia_port **p_port {
  // Give Java proxy the C pointer (of newly created object)
  jclass clazz = jenv->FindClass("org/pjsip/pjsua/pjmedia_port");
  jfieldID fid = jenv->GetFieldID(clazz, "swigCPtr", "J");
  jlong cPtr = 0;
  *(pjmedia_port **)&cPtr = *$1;
  jenv->SetLongField($input, fid, cPtr);
}
%typemap(javain) pjmedia_port **p_port "$javainput"

/////////////////////////////////////
// Typemaps for pjmedia_snd_port
/////////////////////////////////////
// Do not generate the default proxy constructor or destructor
%nodefaultctor pjmedia_snd_port;
%nodefaultdtor pjmedia_snd_port;

// Add in pure Java code proxy constructor
%typemap(javacode) pjmedia_snd_port %{
  /** This constructor creates the proxy which initially does not create nor own any C memory */
  public pjmedia_snd_port() {
    this(0, false);
  }
%}

// Type typemaps for marshalling pjmedia_snd_port **
%typemap(jni) pjmedia_snd_port **p_port "jobject"
%typemap(jtype) pjmedia_snd_port **p_port "pjmedia_snd_port"
%typemap(jstype) pjmedia_snd_port **p_port "pjmedia_snd_port"

// Typemaps for pjmedia_snd_port ** as a parameter output type
%typemap(in) pjmedia_snd_port **p_port (pjmedia_snd_port *ppMediaPort = 0) %{
  $1 = &ppMediaPort;
%}
%typemap(argout) pjmedia_snd_port **p_port {
  // Give Java proxy the C pointer (of newly created object)
  jclass clazz = jenv->FindClass("org/pjsip/pjsua/pjmedia_snd_port");
  jfieldID fid = jenv->GetFieldID(clazz, "swigCPtr", "J");
  jlong cPtr = 0;
  *(pjmedia_snd_port **)&cPtr = *$1;
  jenv->SetLongField($input, fid, cPtr);
}
%typemap(javain) pjmedia_snd_port **p_port "$javainput"

/////////////////////////////////////
// Typemaps for pj_thread_t
/////////////////////////////////////
// Do not generate the default proxy constructor or destructor
%nodefaultctor pj_thread_t;
%nodefaultdtor pj_thread_t;

// Add in pure Java code proxy constructor
%typemap(javacode) pj_thread_t %{
  /** This constructor creates the proxy which initially does not create nor own any C memory */
  public pj_thread_t() {
    this(0, false);
  }
%}

// Type typemaps for marshalling pj_thread_t **
%typemap(jni) pj_thread_t **thread "jobject"
%typemap(jtype) pj_thread_t **thread "pj_thread_t"
%typemap(jstype) pj_thread_t **thread "pj_thread_t"

// Typemaps for pj_thread_t ** as a parameter output type
%typemap(in) pj_thread_t **thread (pj_thread_t *ppThread = 0) %{
  $1 = &ppThread;
%}
%typemap(argout) pj_thread_t **p_port {
  // Give Java proxy the C pointer (of newly created object)
  jclass clazz = jenv->FindClass("org/pjsip/pjsua/pj_thread_t");
  jfieldID fid = jenv->GetFieldID(clazz, "swigCPtr", "J");
  jlong cPtr = 0;
  *(pj_thread_t **)&cPtr = *$1;
  jenv->SetLongField($input, fid, cPtr);
}
%typemap(javain) pj_thread_t **thread "$javainput"


// The following typemap does not work yet
// see http://www.nabble.com/pointer-to-pointer-is-returned-object-td13480782.html#a13480782
/* typemap for pointer to pointer parameters */
/* pointer to pointer is returning a pointer to an object */
//%define %pp_out(OBJTYPE)
//%typemap(in,numinputs=0,noblock) OBJTYPE** ($*1_type retval) {
//    $1 = &retval;
//}
//%typemap(argout) OBJTYPE**
//{
//   %append_output(SWIG_NewPointerObj(SWIG_as_voidptr(retval$argnum),
//$*1_descriptor, 0));
//}%enddef

// From pjlib/include/pj/os.h:68
#define PJ_THREAD_DESC_SIZE (64)
// From pjlib/include/pj/os.h:76
typedef long pj_thread_desc[PJ_THREAD_DESC_SIZE];

/* Arguments like 'pjsua_acc_id *p_acc_id' should be considered output args */
%apply pjsua_acc_id *OUTPUT { pjsua_acc_id *p_acc_id };
%apply pjsua_call_id *OUTPUT { pjsua_call_id *p_call_id };
%apply unsigned *INOUT { unsigned *count };
%apply int *OUTPUT { int *capture_dev };
%apply int *OUTPUT { int *playback_dev };
%apply pjsua_conf_port_id *OUTPUT { pjsua_conf_port_id *p_id };

//%pp_out(pjmedia_port)
/* We need to be able to pass arrays of pjmedia_tone_desc to pjmedia */
/* The array elements are passed by value (copied) */
JAVA_ARRAYSOFCLASSES(pjmedia_tone_desc)
%apply pjmedia_tone_desc[] {const pjmedia_tone_desc tones[]};

%header %{
#include <pjsua-lib/pjsua.h>
#include <pjmedia.h>

class Callback {
public:
	virtual ~Callback() {}
	virtual void on_call_state (pjsua_call_id call_id, pjsip_event *e) {}
	virtual void on_incoming_call (pjsua_acc_id acc_id, pjsua_call_id call_id,
		pjsip_rx_data *rdata) {}
	virtual void on_call_tsx_state (pjsua_call_id call_id, 
		pjsip_transaction *tsx,
		pjsip_event *e) {}
	virtual void on_call_media_state (pjsua_call_id call_id) {}
	virtual void on_stream_created (pjsua_call_id call_id, 
		pjmedia_session *sess,
		unsigned stream_idx, 
		pjmedia_port **pm_port) {}
	virtual void on_stream_destroyed (pjsua_call_id call_id,
		pjmedia_session *sess, 
		unsigned stream_idx) {}
	virtual void on_dtmf_digit (pjsua_call_id call_id, int digit) {}
	virtual void on_call_transfer_request (pjsua_call_id call_id,
		const pj_str_t *dst,
		pjsip_status_code *code) {}
	virtual void on_call_transfer_status (pjsua_call_id call_id,
		int st_code,
		const pj_str_t *st_text,
		pj_bool_t final_,
		pj_bool_t *p_cont) {}
	virtual void on_call_replace_request (pjsua_call_id call_id,
		pjsip_rx_data *rdata,
		int *st_code,
		pj_str_t *st_text) {}
	virtual void on_call_replaced (pjsua_call_id old_call_id,
		pjsua_call_id new_call_id) {}
	virtual void on_reg_state (pjsua_acc_id acc_id) {}
	virtual void on_buddy_state (pjsua_buddy_id buddy_id) {}
	virtual void on_pager (pjsua_call_id call_id, const pj_str_t *from,
		const pj_str_t *to, const pj_str_t *contact,
		const pj_str_t *mime_type, const pj_str_t *body) {}
	virtual void on_pager2 (pjsua_call_id call_id, const pj_str_t *from,
		const pj_str_t *to, const pj_str_t *contact,
		const pj_str_t *mime_type, const pj_str_t *body,
		pjsip_rx_data *rdata) {}
	virtual void on_pager_status (pjsua_call_id call_id,
		const pj_str_t *to,
		const pj_str_t *body,
/*XXX		void *user_data,*/
		pjsip_status_code status,
		const pj_str_t *reason) {}
	virtual void on_pager_status2 (pjsua_call_id call_id,
		const pj_str_t *to,
		const pj_str_t *body,
/*XXX		void *user_data,*/
		pjsip_status_code status,
		const pj_str_t *reason,
		pjsip_tx_data *tdata,
		pjsip_rx_data *rdata) {}
	virtual void on_typing (pjsua_call_id call_id, const pj_str_t *from,
		const pj_str_t *to, const pj_str_t *contact,
		pj_bool_t is_typing) {}
	virtual void on_nat_detect (const pj_stun_nat_detect_result *res) {}
};

static Callback* registeredCallbackObject = NULL;

extern "C" {
void on_call_state_wrapper(pjsua_call_id call_id, pjsip_event *e) {
	registeredCallbackObject->on_call_state(call_id, e);
}
    
void on_incoming_call_wrapper (pjsua_acc_id acc_id, pjsua_call_id call_id,
	pjsip_rx_data *rdata) {
	registeredCallbackObject->on_incoming_call(acc_id, call_id, rdata);
}
    
void on_call_tsx_state_wrapper (pjsua_call_id call_id, 
		pjsip_transaction *tsx,
		pjsip_event *e) {
	registeredCallbackObject->on_call_tsx_state(call_id, tsx, e);
}
    
void on_call_media_state_wrapper (pjsua_call_id call_id) {
	registeredCallbackObject->on_call_media_state(call_id);
}
 

void on_stream_created_wrapper (pjsua_call_id call_id, 
		pjmedia_session *sess,
		unsigned stream_idx, 
		pjmedia_port **pm_port) {
	registeredCallbackObject->on_stream_created(call_id, sess, stream_idx, pm_port);
}

void on_stream_destroyed_wrapper (pjsua_call_id call_id,
	pjmedia_session *sess, 
	unsigned stream_idx) {
	registeredCallbackObject->on_stream_destroyed(call_id, sess, stream_idx);
}

void on_dtmf_digit_wrapper (pjsua_call_id call_id, int digit) {
	registeredCallbackObject->on_dtmf_digit(call_id, digit);
}

void on_call_transfer_request_wrapper (pjsua_call_id call_id,
	const pj_str_t *dst,
	pjsip_status_code *code) {
	registeredCallbackObject->on_call_transfer_request(call_id, dst, code);
}

void on_call_transfer_status_wrapper (pjsua_call_id call_id,
	int st_code,
	const pj_str_t *st_text,
	pj_bool_t final_,
	pj_bool_t *p_cont) {
	registeredCallbackObject->on_call_transfer_status(call_id, st_code, st_text, final_, p_cont);
}

void on_call_replace_request_wrapper (pjsua_call_id call_id,
	pjsip_rx_data *rdata,
	int *st_code,
	pj_str_t *st_text) {
	registeredCallbackObject->on_call_replace_request(call_id, rdata, st_code, st_text);
}

void on_call_replaced_wrapper (pjsua_call_id old_call_id,
	pjsua_call_id new_call_id) {
	registeredCallbackObject->on_call_replaced(old_call_id, new_call_id);
}

void on_reg_state_wrapper (pjsua_acc_id acc_id) {
	registeredCallbackObject->on_reg_state(acc_id);
}

void on_incoming_subscribe_wrapper (pjsua_acc_id acc_id, pjsua_srv_pres *srv_pres,
	pjsua_buddy_id buddy_id, const pj_str_t *from,
	pjsip_rx_data *rdata, pjsip_status_code *code,
	pj_str_t *reason, pjsua_msg_data *msg_data) {
	// XXX To implement
}

void on_srv_subscribe_state_wrapper (pjsua_acc_id acc_id,
	pjsua_srv_pres *srv_pres, const pj_str_t *remote_uri,
	pjsip_evsub_state state, pjsip_event *event) {
	// XXX To implement
}

void on_buddy_state_wrapper (pjsua_buddy_id buddy_id) {
	registeredCallbackObject->on_buddy_state(buddy_id);
}

void on_pager_wrapper (pjsua_call_id call_id, const pj_str_t *from,
	const pj_str_t *to, const pj_str_t *contact,
	const pj_str_t *mime_type, const pj_str_t *body) {
	registeredCallbackObject->on_pager(call_id, from, to, contact, mime_type, body);
}

void on_pager2_wrapper (pjsua_call_id call_id, const pj_str_t *from,
	const pj_str_t *to, const pj_str_t *contact,
	const pj_str_t *mime_type, const pj_str_t *body,
	pjsip_rx_data *rdata, pjsua_acc_id acc_id) {
	// XXX pass acc_id
	registeredCallbackObject->on_pager2(call_id, from, to, contact, mime_type, body, rdata);
}

void on_pager_status_wrapper (pjsua_call_id call_id,
	const pj_str_t *to,
	const pj_str_t *body,
	void *user_data,
	pjsip_status_code status,
	const pj_str_t *reason) {
	registeredCallbackObject->on_pager_status(call_id, to, body, /*XXX user_data,*/ status, reason);
}

void on_pager_status2_wrapper (pjsua_call_id call_id,
	const pj_str_t *to,
	const pj_str_t *body,
	void *user_data,
	pjsip_status_code status,
	const pj_str_t *reason,
	pjsip_tx_data *tdata,
	pjsip_rx_data *rdata,
	pjsua_acc_id acc_id) {
	// XXX pass acc_id
	registeredCallbackObject->on_pager_status2(call_id, to, body, /*XXX user_data,*/ status, reason, tdata, rdata);
}

void on_typing_wrapper (pjsua_call_id call_id, const pj_str_t *from,
	const pj_str_t *to, const pj_str_t *contact,
	pj_bool_t is_typing) {
	registeredCallbackObject->on_typing(call_id, from, to, contact, is_typing);
}

void on_typing2_wrapper (pjsua_call_id call_id, const pj_str_t *from,
	const pj_str_t *to, const pj_str_t *contact,
	pj_bool_t is_typing, pjsip_rx_data *rdata,
	pjsua_acc_id acc_id) {
	// XXX To implement
}

void on_nat_detect_wrapper (const pj_stun_nat_detect_result *res) {
	registeredCallbackObject->on_nat_detect(res);
}
}

static struct pjsua_callback wrapper_callback_struct = {
	&on_call_state_wrapper,
	&on_incoming_call_wrapper,
	&on_call_tsx_state_wrapper,
	&on_call_media_state_wrapper,
	&on_stream_created_wrapper,
	&on_stream_destroyed_wrapper,
	&on_dtmf_digit_wrapper,
	&on_call_transfer_request_wrapper,
	&on_call_transfer_status_wrapper,
	&on_call_replace_request_wrapper,
	&on_call_replaced_wrapper,
	&on_reg_state_wrapper,
	&on_incoming_subscribe_wrapper,
	&on_srv_subscribe_state_wrapper,
	&on_buddy_state_wrapper,
	&on_pager_wrapper,
	&on_pager2_wrapper,
	&on_pager_status_wrapper,
	&on_pager_status2_wrapper,
	&on_typing_wrapper,
	&on_typing2_wrapper,
	&on_nat_detect_wrapper
};

void setCallbackObject(Callback* callback) {
	registeredCallbackObject = callback;
}

%}

%inline %{
// This structure is a hack, as it shadows the original structure which
// is not public. The typemaps for pjmedia_snd_port above make sure
// that this structure can only be allocated withing pjsip and therefore
// this hack should be safe
struct pjmedia_snd_port {
};

// This structure is a hack, as it shadows the original structure which
// is not public. The typemaps for pj_thread_t above make sure
// that this structure can only be allocated withing pjsip and therefore
// this hack should be safe
struct pj_thread_t {
};

pj_str_t pj_str_copy(const char *str) {
	size_t length = strlen(str) + 1;
	char* copy = (char*)calloc(length, sizeof(char));
	copy = strncpy(copy, str, length);
	return pj_str(copy);
}

pj_status_t get_snd_dev_info( pjmedia_snd_dev_info* info, int id )
{
    unsigned dev_count;
	const pjmedia_snd_dev_info *ci;

    dev_count = pjmedia_snd_get_dev_count();

	PJ_ASSERT_RETURN(id >= 0 && id < dev_count, PJ_EINVAL);
	
	ci = pjmedia_snd_get_dev_info(id);
	pj_memcpy(info, ci, sizeof(*ci));

    return PJ_SUCCESS;
}

%}
/* turn on director wrapping Callback */
%feature("director") Callback;

class Callback {
public:
	virtual ~Callback();
	virtual void on_call_state (pjsua_call_id call_id, pjsip_event *e);
	virtual void on_incoming_call (pjsua_acc_id acc_id, pjsua_call_id call_id,
		pjsip_rx_data *rdata);
	virtual void on_call_tsx_state (pjsua_call_id call_id, 
		pjsip_transaction *tsx,
		pjsip_event *e);
	virtual void on_call_media_state (pjsua_call_id call_id);
	virtual void on_stream_created (pjsua_call_id call_id, 
		pjmedia_session *sess,
		unsigned stream_idx, 
		pjmedia_port **pm_port);
	virtual void on_stream_destroyed (pjsua_call_id call_id,
		pjmedia_session *sess, 
		unsigned stream_idx);
	virtual void on_dtmf_digit (pjsua_call_id call_id, int digit);
	virtual void on_call_transfer_request (pjsua_call_id call_id,
		const pj_str_t *dst,
		pjsip_status_code *code);
	virtual void on_call_transfer_status (pjsua_call_id call_id,
		int st_code,
		const pj_str_t *st_text,
		pj_bool_t final_,
		pj_bool_t *p_cont);
	virtual void on_call_replace_request (pjsua_call_id call_id,
		pjsip_rx_data *rdata,
		int *st_code,
		pj_str_t *st_text);
	virtual void on_call_replaced (pjsua_call_id old_call_id,
		pjsua_call_id new_call_id);
	virtual void on_reg_state (pjsua_acc_id acc_id);
	virtual void on_buddy_state (pjsua_buddy_id buddy_id);
	virtual void on_pager (pjsua_call_id call_id, const pj_str_t *from,
		const pj_str_t *to, const pj_str_t *contact,
		const pj_str_t *mime_type, const pj_str_t *body);
	virtual void on_pager2 (pjsua_call_id call_id, const pj_str_t *from,
		const pj_str_t *to, const pj_str_t *contact,
		const pj_str_t *mime_type, const pj_str_t *body,
		pjsip_rx_data *rdata);
	virtual void on_pager_status (pjsua_call_id call_id,
		const pj_str_t *to,
		const pj_str_t *body,
/*XXX		void *user_data,*/
		pjsip_status_code status,
		const pj_str_t *reason);
	virtual void on_pager_status2 (pjsua_call_id call_id,
		const pj_str_t *to,
		const pj_str_t *body,
/*XXX		void *user_data,*/
		pjsip_status_code status,
		const pj_str_t *reason,
		pjsip_tx_data *tdata,
		pjsip_rx_data *rdata);
	virtual void on_typing (pjsua_call_id call_id, const pj_str_t *from,
		const pj_str_t *to, const pj_str_t *contact,
		pj_bool_t is_typing);
	virtual void on_nat_detect (const pj_stun_nat_detect_result *res);
};

%constant struct pjsua_callback* WRAPPER_CALLBACK_STRUCT = &wrapper_callback_struct;

void setCallbackObject(Callback* callback);

#define PJ_DECL(type) extern type

// The public API does not use lists, therefore we define it to nothing
// From pjlib/include/pj/list.h
#define PJ_DECL_LIST_MEMBER(type)

// Swig requires that structures which are mapped to
// classes are specified in this file, therefore I had
// to copy them from the original header files.

// The following typedefs have been found using:
// find -iname '*.h' | xargs grep 'typedef.*TYPENAME'
// The beginning typedef keyword and the repeated structure
// / enum name have been removed

// From pjlib/include/pj/types.h:63
typedef int               pj_status_t;
// From pjlib/include/pj/types.h:66
typedef int               pj_bool_t;
// From pjlib/include/pj/types.h:48
typedef unsigned short    pj_uint16_t;
// From pjlib/include/pj/types.h:42
typedef unsigned int      pj_uint32_t;
// From pjlib/include/pj/types.h:54
typedef unsigned char     pj_uint8_t;

// From pjlib/include/pj/types.h:86
/** Status is OK. */
#define PJ_SUCCESS  0
/** True value. */
#define PJ_TRUE     1
/** False value. */
#define PJ_FALSE    0

// From pjlib/include/pj/types.h:57
typedef size_t pj_size_t;

// From pjlib/include/pj/types.h:111
struct pj_str_t
{
    /** Buffer pointer, which is by convention NOT null terminated. */
    char       *ptr;

    /** The length of the string. */
    pj_ssize_t  slen;
};


// From pjmedia/include/pjmedia/codec.h:261
struct pjmedia_codec_param
{
    /**
     * The "info" part of codec param describes the capability of the codec,
     * and the value should NOT be changed by application.
     */
    struct {
       unsigned    clock_rate;          /**< Sampling rate in Hz            */
       unsigned    channel_cnt;         /**< Channel count.                 */
       pj_uint32_t avg_bps;             /**< Average bandwidth in bits/sec  */
       pj_uint32_t max_bps;             /**< Maximum bandwidth in bits/sec  */
       pj_uint16_t frm_ptime;           /**< Decoder frame ptime in msec.   */
       pj_uint16_t enc_ptime;           /**< Encoder ptime, or zero if it's
                                             equal to decoder ptime.        */
       pj_uint8_t  pcm_bits_per_sample; /**< Bits/sample in the PCM side    */
       pj_uint8_t  pt;                  /**< Payload type.                  */
    } info;

    /**
     * The "setting" part of codec param describes various settings to be
     * applied to the codec. When the codec param is retrieved from the codec
     * or codec factory, the values of these will be filled by the capability
     * of the codec. Any features that are supported by the codec (e.g. vad
     * or plc) will be turned on, so that application can query which
     * capabilities are supported by the codec. Application may change the
     * settings here before instantiating the codec/stream.
     */
    struct {
        pj_uint8_t  frm_per_pkt;    /**< Number of frames per packet.   */
        unsigned    vad:1;          /**< Voice Activity Detector.       */
        unsigned    cng:1;          /**< Comfort Noise Generator.       */
        unsigned    penh:1;         /**< Perceptual Enhancement         */
        unsigned    plc:1;          /**< Packet loss concealment        */
        unsigned    reserved:1;     /**< Reserved, must be zero.        */
        pjmedia_codec_fmtp enc_fmtp;/**< Encoder's fmtp params.         */
        pjmedia_codec_fmtp dec_fmtp;/**< Decoder's fmtp params.         */
    } setting;
};

// From pjmedia/include/pjmedia/types.h:97
enum pjmedia_dir
{
    /** None */
    PJMEDIA_DIR_NONE = 0,

    /** Encoding (outgoing to network) stream */
    PJMEDIA_DIR_ENCODING = 1,

    /** Decoding (incoming from network) stream. */
    PJMEDIA_DIR_DECODING = 2,

    /** Incoming and outgoing stream. */
    PJMEDIA_DIR_ENCODING_DECODING = 3

};

// From pjmedia/include/pjmedia/port.h:205
struct pjmedia_port_info
{
    pj_str_t        name;               /**< Port name.                     */
    pj_uint32_t     signature;          /**< Port signature.                */
    pjmedia_type    type;               /**< Media type.                    */
    pj_bool_t       has_info;           /**< Has info?                      */
    pj_bool_t       need_info;          /**< Need info on connect?          */
    unsigned        pt;                 /**< Payload type (can be dynamic). */
    pj_str_t        encoding_name;      /**< Encoding name.                 */
    unsigned        clock_rate;         /**< Sampling rate.                 */
    unsigned        channel_count;      /**< Number of channels.            */
    unsigned        bits_per_sample;    /**< Bits/sample                    */
    unsigned        samples_per_frame;  /**< No of samples per frame.       */
    unsigned        bytes_per_frame;    /**< No of samples per frame.       */
};

// From pjmedia/include/pjmedia/port.h:253
struct pjmedia_port
{
    pjmedia_port_info    info;              /**< Port information.  */

    /** Port data can be used by the port creator to attach arbitrary
     *  value to be associated with the port.
     */
    struct port_data {
        void            *pdata;             /**< Pointer data.      */
        long             ldata;             /**< Long data.         */
    } port_data;

    /**
     * Sink interface.
     * This should only be called by #pjmedia_port_put_frame().
     */
    pj_status_t (*put_frame)(struct pjmedia_port *this_port,
                             const pjmedia_frame *frame);

    /**
     * Source interface.
     * This should only be called by #pjmedia_port_get_frame().
     */
    pj_status_t (*get_frame)(struct pjmedia_port *this_port,
                             pjmedia_frame *frame);

    /**
     * Called to destroy this port.
     */
    pj_status_t (*on_destroy)(struct pjmedia_port *this_port);

};



// From pjsip/include/pjsip/sip_auth.h:108
struct pjsip_cred_info
{
    pj_str_t    realm;          /**< Realm. Use "*" to make a credential that
                                     can be used to authenticate against any
                                     challenges.                            */
    pj_str_t    scheme;         /**< Scheme (e.g. "digest").                */
    pj_str_t    username;       /**< User name.                             */
    int         data_type;      /**< Type of data (0 for plaintext passwd). */
    pj_str_t    data;           /**< The data, which can be a plaintext
                                     password or a hashed digest.           */

    /** Extended data */
    union {
        /** Digest AKA credential information. Note that when AKA credential
         *  is being used, the \a data field of this #pjsip_cred_info is
         *  not used, but it still must be initialized to an empty string.
         * Please see \ref PJSIP_AUTH_AKA_API for more information.
         */
        struct {
            pj_str_t      k;    /**< Permanent subscriber key.          */
            pj_str_t      op;   /**< Operator variant key.              */
            pj_str_t      amf;  /**< Authentication Management Field    */
            pjsip_cred_cb cb;   /**< Callback to create AKA digest.     */
        } aka;

    } ext;
};

// From pjsip/include/pjsip/sip_auth.h:50
enum pjsip_cred_data_type
{
    PJSIP_CRED_DATA_PLAIN_PASSWD=0, /**< Plain text password.           */
    PJSIP_CRED_DATA_DIGEST      =1, /**< Hashed digest.                 */

    PJSIP_CRED_DATA_EXT_AKA     =16 /**< Extended AKA info is available */

};

// From pjsip/include/pjsip/sip_event.h:79
struct pjsip_event
{
    /** This is necessary so that we can put events as a list. */
    PJ_DECL_LIST_MEMBER(struct pjsip_event);

    /** The event type, can be any value of \b pjsip_event_id_e.
     */
    pjsip_event_id_e type;

    /**
     * The event body as union, which fields depends on the event type.
     * By convention, the first member of each struct in the union must be
     * the pointer which is relevant to the event.
     */
    union
    {
        /** Timer event. */
        struct
        {
            pj_timer_entry *entry;      /**< The timer entry.           */
        } timer;

        /** Transaction state has changed event. */
        struct
        {
            union
            {
                pjsip_rx_data   *rdata; /**< The incoming message.      */
                pjsip_tx_data   *tdata; /**< The outgoing message.      */
                pj_timer_entry  *timer; /**< The timer.                 */
                pj_status_t      status;/**< Transport error status.    */
                void            *data;  /**< Generic data.              */
            } src;
            pjsip_transaction   *tsx;   /**< The transaction.           */
            int                  prev_state; /**< Previous state.       */
            pjsip_event_id_e     type;  /**< Type of event source:
                                         *      - PJSIP_EVENT_TX_MSG
                                         *      - PJSIP_EVENT_RX_MSG,
                                         *      - PJSIP_EVENT_TRANSPORT_ERROR
                                         *      - PJSIP_EVENT_TIMER
                                         *      - PJSIP_EVENT_USER
                                         */
        } tsx_state;

        /** Message transmission event. */
        struct
        {
            pjsip_tx_data       *tdata; /**< The transmit data buffer.  */

        } tx_msg;

        /** Transmission error event. */
        struct
        {
            pjsip_tx_data       *tdata; /**< The transmit data.         */
            pjsip_transaction   *tsx;   /**< The transaction.           */
        } tx_error;

        /** Message arrival event. */
        struct
        {
            pjsip_rx_data       *rdata; /**< The receive data buffer.   */
        } rx_msg;

        /** User event. */
        struct
        {
            void                *user1; /**< User data 1.               */
            void                *user2; /**< User data 2.               */
            void                *user3; /**< User data 3.               */
            void                *user4; /**< User data 4.               */
        } user;

    } body;
};

// From pjmedia/include/pjmedia/sound.h:69
struct pjmedia_snd_dev_info
{
    char        name[64];               /**< Device name.                   */
    unsigned    input_count;            /**< Max number of input channels.  */
    unsigned    output_count;           /**< Max number of output channels. */
    unsigned    default_samples_per_sec;/**< Default sampling rate.         */
};

// From pjlib/include/pj/pool.h:312
struct pj_pool_t
{
    PJ_DECL_LIST_MEMBER(struct pj_pool_t);  /**< Standard list elements.    */

    /** Pool name */
    char            obj_name[PJ_MAX_OBJ_NAME];

    /** Pool factory. */
    pj_pool_factory *factory;

    /** Data put by factory */
    void            *factory_data;

    /** Current capacity allocated by the pool. */
    pj_size_t       capacity;

    /** Size of memory block to be allocated when the pool runs out of memory */
    pj_size_t       increment_size;

    /** List of memory blocks allcoated by the pool. */
    pj_pool_block   block_list;

    /** The callback to be called when the pool is unable to allocate memory. */
    pj_pool_callback *callback;

};

// From pjsip/include/pjsip/sip_event.h:41
enum pjsip_event_id_e
{
    /** Unidentified event. */
    PJSIP_EVENT_UNKNOWN,

    /** Timer event, normally only used internally in transaction. */
    PJSIP_EVENT_TIMER,

    /** Message transmission event. */
    PJSIP_EVENT_TX_MSG,

    /** Message received event. */
    PJSIP_EVENT_RX_MSG,

    /** Transport error event. */
    PJSIP_EVENT_TRANSPORT_ERROR,

    /** Transaction state changed event. */
    PJSIP_EVENT_TSX_STATE,

    /** Indicates that the event was triggered by user action. */
    PJSIP_EVENT_USER

};

// From pjsip/include/pjsip/sip_types.h:61
enum pjsip_transport_type_e
{
    /** Unspecified. */
    PJSIP_TRANSPORT_UNSPECIFIED,

    /** UDP. */
    PJSIP_TRANSPORT_UDP,

    /** TCP. */
    PJSIP_TRANSPORT_TCP,

    /** TLS. */
    PJSIP_TRANSPORT_TLS,

    /** SCTP. */
    PJSIP_TRANSPORT_SCTP,

    /** Loopback (stream, reliable) */
    PJSIP_TRANSPORT_LOOP,

    /** Loopback (datagram, unreliable) */
    PJSIP_TRANSPORT_LOOP_DGRAM,

    /** Start of user defined transport */
    PJSIP_TRANSPORT_START_OTHER,

    /** Start of IPv6 transports */
    PJSIP_TRANSPORT_IPV6    = 128,

    /** UDP over IPv6 */
    PJSIP_TRANSPORT_UDP6 = PJSIP_TRANSPORT_UDP + PJSIP_TRANSPORT_IPV6,

    /** TCP over IPv6 */
    PJSIP_TRANSPORT_TCP6 = PJSIP_TRANSPORT_TCP + PJSIP_TRANSPORT_IPV6

};

// From pjsip/include/pjsip-ua/sip_inv.h:87
enum pjsip_inv_state
{
    PJSIP_INV_STATE_NULL,           /**< Before INVITE is sent or received  */
    PJSIP_INV_STATE_CALLING,        /**< After INVITE is sent               */
    PJSIP_INV_STATE_INCOMING,       /**< After INVITE is received.          */
    PJSIP_INV_STATE_EARLY,          /**< After response with To tag.        */
    PJSIP_INV_STATE_CONNECTING,     /**< After 2xx is sent/received.        */
    PJSIP_INV_STATE_CONFIRMED,      /**< After ACK is sent/received.        */
    PJSIP_INV_STATE_DISCONNECTED,   /**< Session is terminated.             */
};

// From pjsip/include/pjsip/sip_msg.h:402
enum pjsip_status_code
{
    PJSIP_SC_TRYING = 100,
    PJSIP_SC_RINGING = 180,
    PJSIP_SC_CALL_BEING_FORWARDED = 181,
    PJSIP_SC_QUEUED = 182,
    PJSIP_SC_PROGRESS = 183,

    PJSIP_SC_OK = 200,
    PJSIP_SC_ACCEPTED = 202,

    PJSIP_SC_MULTIPLE_CHOICES = 300,
    PJSIP_SC_MOVED_PERMANENTLY = 301,
    PJSIP_SC_MOVED_TEMPORARILY = 302,
    PJSIP_SC_USE_PROXY = 305,
    PJSIP_SC_ALTERNATIVE_SERVICE = 380,

    PJSIP_SC_BAD_REQUEST = 400,
    PJSIP_SC_UNAUTHORIZED = 401,
    PJSIP_SC_PAYMENT_REQUIRED = 402,
    PJSIP_SC_FORBIDDEN = 403,
    PJSIP_SC_NOT_FOUND = 404,
    PJSIP_SC_METHOD_NOT_ALLOWED = 405,
    PJSIP_SC_NOT_ACCEPTABLE = 406,
    PJSIP_SC_PROXY_AUTHENTICATION_REQUIRED = 407,
    PJSIP_SC_REQUEST_TIMEOUT = 408,
    PJSIP_SC_GONE = 410,
    PJSIP_SC_REQUEST_ENTITY_TOO_LARGE = 413,
    PJSIP_SC_REQUEST_URI_TOO_LONG = 414,
    PJSIP_SC_UNSUPPORTED_MEDIA_TYPE = 415,
    PJSIP_SC_UNSUPPORTED_URI_SCHEME = 416,
    PJSIP_SC_BAD_EXTENSION = 420,
    PJSIP_SC_EXTENSION_REQUIRED = 421,
    PJSIP_SC_SESSION_TIMER_TOO_SMALL = 422,
    PJSIP_SC_INTERVAL_TOO_BRIEF = 423,
    PJSIP_SC_TEMPORARILY_UNAVAILABLE = 480,
    PJSIP_SC_CALL_TSX_DOES_NOT_EXIST = 481,
    PJSIP_SC_LOOP_DETECTED = 482,
    PJSIP_SC_TOO_MANY_HOPS = 483,
    PJSIP_SC_ADDRESS_INCOMPLETE = 484,
    PJSIP_AC_AMBIGUOUS = 485,
    PJSIP_SC_BUSY_HERE = 486,
    PJSIP_SC_REQUEST_TERMINATED = 487,
    PJSIP_SC_NOT_ACCEPTABLE_HERE = 488,
    PJSIP_SC_BAD_EVENT = 489,
    PJSIP_SC_REQUEST_UPDATED = 490,
    PJSIP_SC_REQUEST_PENDING = 491,
    PJSIP_SC_UNDECIPHERABLE = 493,

    PJSIP_SC_INTERNAL_SERVER_ERROR = 500,
    PJSIP_SC_NOT_IMPLEMENTED = 501,
    PJSIP_SC_BAD_GATEWAY = 502,
    PJSIP_SC_SERVICE_UNAVAILABLE = 503,
    PJSIP_SC_SERVER_TIMEOUT = 504,
    PJSIP_SC_VERSION_NOT_SUPPORTED = 505,
    PJSIP_SC_MESSAGE_TOO_LARGE = 513,
    PJSIP_SC_PRECONDITION_FAILURE = 580,

    PJSIP_SC_BUSY_EVERYWHERE = 600,
    PJSIP_SC_DECLINE = 603,
    PJSIP_SC_DOES_NOT_EXIST_ANYWHERE = 604,
    PJSIP_SC_NOT_ACCEPTABLE_ANYWHERE = 606,

    PJSIP_SC_TSX_TIMEOUT = PJSIP_SC_REQUEST_TIMEOUT,
    /*PJSIP_SC_TSX_RESOLVE_ERROR = 702,*/
    PJSIP_SC_TSX_TRANSPORT_ERROR = PJSIP_SC_SERVICE_UNAVAILABLE

};

// From pjsip/sip_msg.h:198
enum pjsip_hdr_e
{
    
    PJSIP_H_ACCEPT,
    PJSIP_H_ACCEPT_ENCODING_UNIMP,	
    PJSIP_H_ACCEPT_LANGUAGE_UNIMP,	
    PJSIP_H_ALERT_INFO_UNIMP,		
    PJSIP_H_ALLOW,
    PJSIP_H_AUTHENTICATION_INFO_UNIMP,	
    PJSIP_H_AUTHORIZATION,
    PJSIP_H_CALL_ID,
    PJSIP_H_CALL_INFO_UNIMP,		
    PJSIP_H_CONTACT,
    PJSIP_H_CONTENT_DISPOSITION_UNIMP,	
    PJSIP_H_CONTENT_ENCODING_UNIMP,	
    PJSIP_H_CONTENT_LANGUAGE_UNIMP,	
    PJSIP_H_CONTENT_LENGTH,
    PJSIP_H_CONTENT_TYPE,
    PJSIP_H_CSEQ,
    PJSIP_H_DATE_UNIMP,			
    PJSIP_H_ERROR_INFO_UNIMP,		
    PJSIP_H_EXPIRES,
    PJSIP_H_FROM,
    PJSIP_H_IN_REPLY_TO_UNIMP,		
    PJSIP_H_MAX_FORWARDS,
    PJSIP_H_MIME_VERSION_UNIMP,		
    PJSIP_H_MIN_EXPIRES,
    PJSIP_H_ORGANIZATION_UNIMP,		
    PJSIP_H_PRIORITY_UNIMP,		
    PJSIP_H_PROXY_AUTHENTICATE,
    PJSIP_H_PROXY_AUTHORIZATION,
    PJSIP_H_PROXY_REQUIRE_UNIMP,	
    PJSIP_H_RECORD_ROUTE,
    PJSIP_H_REPLY_TO_UNIMP,		
    PJSIP_H_REQUIRE,
    PJSIP_H_RETRY_AFTER,
    PJSIP_H_ROUTE,
    PJSIP_H_SERVER_UNIMP,		
    PJSIP_H_SUBJECT_UNIMP,		
    PJSIP_H_SUPPORTED,
    PJSIP_H_TIMESTAMP_UNIMP,		
    PJSIP_H_TO,
    PJSIP_H_UNSUPPORTED,
    PJSIP_H_USER_AGENT_UNIMP,		
    PJSIP_H_VIA,
    PJSIP_H_WARNING_UNIMP,		
    PJSIP_H_WWW_AUTHENTICATE,
    PJSIP_H_OTHER
};
struct pjsip_hdr_vptr
{
    
    void *(*clone)(pj_pool_t *pool, const void *hdr);
    
    void *(*shallow_clone)(pj_pool_t *pool, const void *hdr);
    
    int (*print_on)(void *hdr, char *buf, pj_size_t len);
};
#define PJSIP_DECL_HDR_MEMBER(hdr)   \
    	\
    PJ_DECL_LIST_MEMBER(hdr);	\
    		\
    pjsip_hdr_e	    type;	\
    		\
    pj_str_t	    name;	\
    	\
    pj_str_t	    sname;		\
    	\
    pjsip_hdr_vptr *vptr
struct pjsip_hdr
{
    PJSIP_DECL_HDR_MEMBER(struct pjsip_hdr);
};

// From pjsip/sip_msg.h:920
typedef struct pjsip_generic_string_hdr
{
    /** Standard header field. */
    PJSIP_DECL_HDR_MEMBER(struct pjsip_generic_string_hdr);
    /** hvalue */
    pj_str_t hvalue;
} pjsip_generic_string_hdr;
PJ_DECL(pjsip_generic_string_hdr*)
pjsip_generic_string_hdr_create( pj_pool_t *pool,
                                 const pj_str_t *hname,
                                 const pj_str_t *hvalue);
                                 

%inline %{
// Adapted from pjsip/sip_msg.h:831
// The following function is a modified version of the original one
// which accepts a pjsip_generic_string_hdr directly as casting from
// pjsip_generic_string_hdr to pjsip_hdr in Java is difficult
void pjsip_msg_add_hdr( pjsua_msg_data *msg_data, pjsip_generic_string_hdr *hdr )
{
    pj_list_push_back(&msg_data->hdr_list, hdr);
}

%}

/** The following functions are not exposed in pjsua.h, but we need them */
// From pjsip/include/pjsua-lib/pjsua.h:1342
PJ_DECL(pj_pool_t*) pjsua_pool_create(const char *name, pj_size_t init_size, pj_size_t increment);
// From pjlib/include/pj/pool.h:390
PJ_DECL(void) pj_pool_release( pj_pool_t *pool );
// From pjmedia/include/pjmedia/sound.h:144
%rename(snd_get_dev_count) pjmedia_snd_get_dev_count;
PJ_DECL(int) pjmedia_snd_get_dev_count(void);
// From pjmedia/include/pjmedia/sound.h:174
PJ_DECL(pj_status_t) pjmedia_snd_set_latency(unsigned input_latency, unsigned output_latency);
// From pjmedia/include/pjmedia/port.h:340
PJ_DECL(pj_status_t) pjmedia_port_destroy( pjmedia_port *port );
// From pjlib/include/pj/os.h:127
PJ_DECL(pj_status_t) pj_thread_register ( const char *thread_name, pj_thread_desc desc, pj_thread_t **thread);
// From pjlib/include/pj/os.h:136
PJ_DECL(pj_bool_t) pj_thread_is_registered(void);
// From pjsip/sip_msg.h:983
PJ_DECL(void) pjsip_generic_string_hdr_init2(pjsip_generic_string_hdr *h,
                                             pj_str_t *hname,
                                             pj_str_t *hvalue); 
#define PJSUA_INVALID_ID	    (-1)
typedef int pjsua_call_id;
typedef int pjsua_acc_id;
typedef int pjsua_buddy_id;
typedef int pjsua_player_id;
typedef int pjsua_recorder_id;
typedef int pjsua_conf_port_id;
typedef struct pjsua_srv_pres pjsua_srv_pres;
#ifndef PJSUA_ACC_MAX_PROXIES
#   define PJSUA_ACC_MAX_PROXIES    8
#endif
#if defined(PJMEDIA_HAS_SRTP) && (PJMEDIA_HAS_SRTP != 0)
#ifndef PJSUA_DEFAULT_USE_SRTP
    #define PJSUA_DEFAULT_USE_SRTP  PJMEDIA_SRTP_DISABLED
#endif
#ifndef PJSUA_DEFAULT_SRTP_SECURE_SIGNALING
    #define PJSUA_DEFAULT_SRTP_SECURE_SIGNALING 1
#endif
#endif
struct pjsua_logging_config
{
    
    pj_bool_t	msg_logging;
    
    unsigned	level;
    
    unsigned	console_level;
    
    unsigned	decor;
    
    pj_str_t	log_filename;
    
    void       (*cb)(int level, const char *data, int len);
};
%rename(logging_config_default) pjsua_logging_config_default;
%javamethodmodifiers pjsua_logging_config_default(pjsua_logging_config *cfg) "public synchronized";
%javamethodmodifiers pjsua_logging_config_default(pjsua_logging_config *cfg) "public synchronized";
PJ_DECL(void) pjsua_logging_config_default(pjsua_logging_config *cfg);
%rename(logging_config_dup) pjsua_logging_config_dup;
%javamethodmodifiers pjsua_logging_config_dup(pj_pool_t *pool,
				       pjsua_logging_config *dst,
				       const pjsua_logging_config *src) "public synchronized";
%javamethodmodifiers pjsua_logging_config_dup(pj_pool_t *pool,
				       pjsua_logging_config *dst,
				       const pjsua_logging_config *src) "public synchronized";
PJ_DECL(void) pjsua_logging_config_dup(pj_pool_t *pool,
				       pjsua_logging_config *dst,
				       const pjsua_logging_config *src);
struct pjsua_callback
{
    
    void (*on_call_state)(pjsua_call_id call_id, pjsip_event *e);
    
    void (*on_incoming_call)(pjsua_acc_id acc_id, pjsua_call_id call_id,
			     pjsip_rx_data *rdata);
    
    void (*on_call_tsx_state)(pjsua_call_id call_id, 
			      pjsip_transaction *tsx,
			      pjsip_event *e);
    
    void (*on_call_media_state)(pjsua_call_id call_id);
 
    
    void (*on_stream_created)(pjsua_call_id call_id, 
			      pjmedia_session *sess,
                              unsigned stream_idx, 
			      pjmedia_port **p_port);
    
    void (*on_stream_destroyed)(pjsua_call_id call_id,
                                pjmedia_session *sess, 
				unsigned stream_idx);
    
    void (*on_dtmf_digit)(pjsua_call_id call_id, int digit);
    
    void (*on_call_transfer_request)(pjsua_call_id call_id,
				     const pj_str_t *dst,
				     pjsip_status_code *code);
    
    void (*on_call_transfer_status)(pjsua_call_id call_id,
				    int st_code,
				    const pj_str_t *st_text,
				    pj_bool_t final,
				    pj_bool_t *p_cont);
    
    void (*on_call_replace_request)(pjsua_call_id call_id,
				    pjsip_rx_data *rdata,
				    int *st_code,
				    pj_str_t *st_text);
    
    void (*on_call_replaced)(pjsua_call_id old_call_id,
			     pjsua_call_id new_call_id);
    
    void (*on_reg_state)(pjsua_acc_id acc_id);
    
    void (*on_incoming_subscribe)(pjsua_acc_id acc_id,
				  pjsua_srv_pres *srv_pres,
				  pjsua_buddy_id buddy_id,
				  const pj_str_t *from,
				  pjsip_rx_data *rdata,
				  pjsip_status_code *code,
				  pj_str_t *reason,
				  pjsua_msg_data *msg_data);
    
    void (*on_srv_subscribe_state)(pjsua_acc_id acc_id,
				   pjsua_srv_pres *srv_pres,
				   const pj_str_t *remote_uri,
				   pjsip_evsub_state state,
				   pjsip_event *event);
    
    void (*on_buddy_state)(pjsua_buddy_id buddy_id);
    
    void (*on_pager)(pjsua_call_id call_id, const pj_str_t *from,
		     const pj_str_t *to, const pj_str_t *contact,
		     const pj_str_t *mime_type, const pj_str_t *body);
    
    void (*on_pager2)(pjsua_call_id call_id, const pj_str_t *from,
		      const pj_str_t *to, const pj_str_t *contact,
		      const pj_str_t *mime_type, const pj_str_t *body,
		      pjsip_rx_data *rdata, pjsua_acc_id acc_id);
    
    void (*on_pager_status)(pjsua_call_id call_id,
			    const pj_str_t *to,
			    const pj_str_t *body,
			    void *user_data,
			    pjsip_status_code status,
			    const pj_str_t *reason);
    
    void (*on_pager_status2)(pjsua_call_id call_id,
			     const pj_str_t *to,
			     const pj_str_t *body,
			     void *user_data,
			     pjsip_status_code status,
			     const pj_str_t *reason,
			     pjsip_tx_data *tdata,
			     pjsip_rx_data *rdata,
			     pjsua_acc_id acc_id);
    
    void (*on_typing)(pjsua_call_id call_id, const pj_str_t *from,
		      const pj_str_t *to, const pj_str_t *contact,
		      pj_bool_t is_typing);
    
    void (*on_typing2)(pjsua_call_id call_id, const pj_str_t *from,
		       const pj_str_t *to, const pj_str_t *contact,
		       pj_bool_t is_typing, pjsip_rx_data *rdata,
		       pjsua_acc_id acc_id);
    
    void (*on_nat_detect)(const pj_stun_nat_detect_result *res);
};
struct pjsua_config
{
    
    unsigned	    max_calls;
    
    unsigned	    thread_cnt;
    
    unsigned	    nameserver_count;
    
    pj_str_t	    nameserver[4];
    
    pj_bool_t	    force_lr;
    
    unsigned	    outbound_proxy_cnt;
    
    pj_str_t	    outbound_proxy[4];
    
    pj_str_t	    stun_domain;
    
    pj_str_t	    stun_host;
    
    int		    nat_type_in_sdp;
    
    pj_bool_t	    require_100rel;
    
    unsigned	    cred_count;
    
    pjsip_cred_info cred_info[PJSUA_ACC_MAX_PROXIES];
    
    pjsua_callback  cb;
    
    pj_str_t	    user_agent;
#if defined(PJMEDIA_HAS_SRTP) && (PJMEDIA_HAS_SRTP != 0)
    
    pjmedia_srtp_use	use_srtp;
    
    int		     srtp_secure_signaling;
#endif
    
    pj_bool_t	     hangup_forked_call;
};
%rename(config_default) pjsua_config_default;
%javamethodmodifiers pjsua_config_default(pjsua_config *cfg) "public synchronized";
%javamethodmodifiers pjsua_config_default(pjsua_config *cfg) "public synchronized";
PJ_DECL(void) pjsua_config_default(pjsua_config *cfg);
#define pjsip_cred_dup	pjsip_cred_info_dup
%rename(config_dup) pjsua_config_dup;
%javamethodmodifiers pjsua_config_dup(pj_pool_t *pool,
			       pjsua_config *dst,
			       const pjsua_config *src) "public synchronized";
%javamethodmodifiers pjsua_config_dup(pj_pool_t *pool,
			       pjsua_config *dst,
			       const pjsua_config *src) "public synchronized";
PJ_DECL(void) pjsua_config_dup(pj_pool_t *pool,
			       pjsua_config *dst,
			       const pjsua_config *src);
struct pjsua_msg_data
{
    
    pjsip_hdr	hdr_list;
    
    pj_str_t	content_type;
    
    pj_str_t	msg_body;
};
%rename(msg_data_init) pjsua_msg_data_init;
%javamethodmodifiers pjsua_msg_data_init(pjsua_msg_data *msg_data) "public synchronized";
%javamethodmodifiers pjsua_msg_data_init(pjsua_msg_data *msg_data) "public synchronized";
PJ_DECL(void) pjsua_msg_data_init(pjsua_msg_data *msg_data);
%rename(create) pjsua_create;
%javamethodmodifiers pjsua_create(void) "public synchronized";
%javamethodmodifiers pjsua_create(void) "public synchronized";
PJ_DECL(pj_status_t) pjsua_create(void);
%rename(init) pjsua_init;
%javamethodmodifiers pjsua_init(const pjsua_config *ua_cfg,
				const pjsua_logging_config *log_cfg,
				const pjsua_media_config *media_cfg) "public synchronized";
%javamethodmodifiers pjsua_init(const pjsua_config *ua_cfg,
				const pjsua_logging_config *log_cfg,
				const pjsua_media_config *media_cfg) "public synchronized";
PJ_DECL(pj_status_t) pjsua_init(const pjsua_config *ua_cfg,
				const pjsua_logging_config *log_cfg,
				const pjsua_media_config *media_cfg);
%rename(start) pjsua_start;
%javamethodmodifiers pjsua_start(void) "public synchronized";
%javamethodmodifiers pjsua_start(void) "public synchronized";
PJ_DECL(pj_status_t) pjsua_start(void);
%rename(destroy) pjsua_destroy;
%javamethodmodifiers pjsua_destroy(void) "public synchronized";
%javamethodmodifiers pjsua_destroy(void) "public synchronized";
PJ_DECL(pj_status_t) pjsua_destroy(void);
%rename(handle_events) pjsua_handle_events;
%javamethodmodifiers pjsua_handle_events(unsigned msec_timeout) "public synchronized";
%javamethodmodifiers pjsua_handle_events(unsigned msec_timeout) "public synchronized";
PJ_DECL(int) pjsua_handle_events(unsigned msec_timeout);
%rename(pool_create) pjsua_pool_create;
%javamethodmodifiers pjsua_pool_create(const char *name, pj_size_t init_size,
				      pj_size_t increment) "public synchronized";
%javamethodmodifiers pjsua_pool_create(const char *name, pj_size_t init_size,
				      pj_size_t increment) "public synchronized";
PJ_DECL(pj_pool_t*) pjsua_pool_create(const char *name, pj_size_t init_size,
				      pj_size_t increment);
%rename(reconfigure_logging) pjsua_reconfigure_logging;
%javamethodmodifiers pjsua_reconfigure_logging(const pjsua_logging_config *c) "public synchronized";
%javamethodmodifiers pjsua_reconfigure_logging(const pjsua_logging_config *c) "public synchronized";
PJ_DECL(pj_status_t) pjsua_reconfigure_logging(const pjsua_logging_config *c);
%rename(get_pjsip_endpt) pjsua_get_pjsip_endpt;
%javamethodmodifiers pjsua_get_pjsip_endpt(void) "public synchronized";
%javamethodmodifiers pjsua_get_pjsip_endpt(void) "public synchronized";
PJ_DECL(pjsip_endpoint*) pjsua_get_pjsip_endpt(void);
%rename(get_pjmedia_endpt) pjsua_get_pjmedia_endpt;
%javamethodmodifiers pjsua_get_pjmedia_endpt(void) "public synchronized";
%javamethodmodifiers pjsua_get_pjmedia_endpt(void) "public synchronized";
PJ_DECL(pjmedia_endpt*) pjsua_get_pjmedia_endpt(void);
%rename(get_pool_factory) pjsua_get_pool_factory;
%javamethodmodifiers pjsua_get_pool_factory(void) "public synchronized";
%javamethodmodifiers pjsua_get_pool_factory(void) "public synchronized";
PJ_DECL(pj_pool_factory*) pjsua_get_pool_factory(void);
%rename(detect_nat_type) pjsua_detect_nat_type;
%javamethodmodifiers pjsua_detect_nat_type(void) "public synchronized";
%javamethodmodifiers pjsua_detect_nat_type(void) "public synchronized";
PJ_DECL(pj_status_t) pjsua_detect_nat_type(void);
%rename(get_nat_type) pjsua_get_nat_type;
%javamethodmodifiers pjsua_get_nat_type(pj_stun_nat_type *type) "public synchronized";
%javamethodmodifiers pjsua_get_nat_type(pj_stun_nat_type *type) "public synchronized";
PJ_DECL(pj_status_t) pjsua_get_nat_type(pj_stun_nat_type *type);
%rename(verify_sip_url) pjsua_verify_sip_url;
%javamethodmodifiers pjsua_verify_sip_url(const char *url) "public synchronized";
%javamethodmodifiers pjsua_verify_sip_url(const char *url) "public synchronized";
PJ_DECL(pj_status_t) pjsua_verify_sip_url(const char *url);
%rename(perror) pjsua_perror;
%javamethodmodifiers pjsua_perror(const char *sender, const char *title, 
			   pj_status_t status) "public synchronized";
%javamethodmodifiers pjsua_perror(const char *sender, const char *title, 
			   pj_status_t status) "public synchronized";
PJ_DECL(void) pjsua_perror(const char *sender, const char *title, 
			   pj_status_t status);
%rename(dump) pjsua_dump;
%javamethodmodifiers pjsua_dump(pj_bool_t detail) "public synchronized";
%javamethodmodifiers pjsua_dump(pj_bool_t detail) "public synchronized";
PJ_DECL(void) pjsua_dump(pj_bool_t detail);
typedef int pjsua_transport_id;
struct pjsua_transport_config
{
    
    unsigned		port;
    
    pj_str_t		public_addr;
    
    pj_str_t		bound_addr;
    
    pjsip_tls_setting	tls_setting;
};
%rename(transport_config_default) pjsua_transport_config_default;
%javamethodmodifiers pjsua_transport_config_default(pjsua_transport_config *cfg) "public synchronized";
%javamethodmodifiers pjsua_transport_config_default(pjsua_transport_config *cfg) "public synchronized";
PJ_DECL(void) pjsua_transport_config_default(pjsua_transport_config *cfg);
%rename(transport_config_dup) pjsua_transport_config_dup;
%javamethodmodifiers pjsua_transport_config_dup(pj_pool_t *pool,
					 pjsua_transport_config *dst,
					 const pjsua_transport_config *src) "public synchronized";
%javamethodmodifiers pjsua_transport_config_dup(pj_pool_t *pool,
					 pjsua_transport_config *dst,
					 const pjsua_transport_config *src) "public synchronized";
PJ_DECL(void) pjsua_transport_config_dup(pj_pool_t *pool,
					 pjsua_transport_config *dst,
					 const pjsua_transport_config *src);
struct pjsua_transport_info
{
    
    pjsua_transport_id	    id;
    
    pjsip_transport_type_e  type;
    
    pj_str_t		    type_name;
    
    pj_str_t		    info;
    
    unsigned		    flag;
    
    unsigned		    addr_len;
    
    pj_sockaddr		    local_addr;
    
    pjsip_host_port	    local_name;
    
    unsigned		    usage_count;
};
%rename(transport_create) pjsua_transport_create;
%javamethodmodifiers pjsua_transport_create(pjsip_transport_type_e type,
					    const pjsua_transport_config *cfg,
					    pjsua_transport_id *p_id) "public synchronized";
%javamethodmodifiers pjsua_transport_create(pjsip_transport_type_e type,
					    const pjsua_transport_config *cfg,
					    pjsua_transport_id *p_id) "public synchronized";
PJ_DECL(pj_status_t) pjsua_transport_create(pjsip_transport_type_e type,
					    const pjsua_transport_config *cfg,
					    pjsua_transport_id *p_id);
%rename(transport_register) pjsua_transport_register;
%javamethodmodifiers pjsua_transport_register(pjsip_transport *tp,
					      pjsua_transport_id *p_id) "public synchronized";
%javamethodmodifiers pjsua_transport_register(pjsip_transport *tp,
					      pjsua_transport_id *p_id) "public synchronized";
PJ_DECL(pj_status_t) pjsua_transport_register(pjsip_transport *tp,
					      pjsua_transport_id *p_id);
%rename(transport_get_count) pjsua_transport_get_count;
%javamethodmodifiers pjsua_transport_get_count( void ) "public synchronized";
%javamethodmodifiers pjsua_transport_get_count( void ) "public synchronized";
PJ_DECL(unsigned) pjsua_transport_get_count( void );
%rename(enum_transports) pjsua_enum_transports;
%javamethodmodifiers pjsua_enum_transports( pjsua_transport_id id[],
					    unsigned *count ) "public synchronized";
%javamethodmodifiers pjsua_enum_transports( pjsua_transport_id id[],
					    unsigned *count ) "public synchronized";
PJ_DECL(pj_status_t) pjsua_enum_transports( pjsua_transport_id id[],
					    unsigned *count );
%rename(transport_get_info) pjsua_transport_get_info;
%javamethodmodifiers pjsua_transport_get_info(pjsua_transport_id id,
					      pjsua_transport_info *info) "public synchronized";
%javamethodmodifiers pjsua_transport_get_info(pjsua_transport_id id,
					      pjsua_transport_info *info) "public synchronized";
PJ_DECL(pj_status_t) pjsua_transport_get_info(pjsua_transport_id id,
					      pjsua_transport_info *info);
%rename(transport_set_enable) pjsua_transport_set_enable;
%javamethodmodifiers pjsua_transport_set_enable(pjsua_transport_id id,
						pj_bool_t enabled) "public synchronized";
%javamethodmodifiers pjsua_transport_set_enable(pjsua_transport_id id,
						pj_bool_t enabled) "public synchronized";
PJ_DECL(pj_status_t) pjsua_transport_set_enable(pjsua_transport_id id,
						pj_bool_t enabled);
%rename(transport_close) pjsua_transport_close;
%javamethodmodifiers pjsua_transport_close( pjsua_transport_id id,
					    pj_bool_t force ) "public synchronized";
%javamethodmodifiers pjsua_transport_close( pjsua_transport_id id,
					    pj_bool_t force ) "public synchronized";
PJ_DECL(pj_status_t) pjsua_transport_close( pjsua_transport_id id,
					    pj_bool_t force );
#ifndef PJSUA_MAX_ACC
#   define PJSUA_MAX_ACC	    8
#endif
#ifndef PJSUA_REG_INTERVAL
#   define PJSUA_REG_INTERVAL	    300
#endif
#ifndef PJSUA_PUBLISH_EXPIRATION
#   define PJSUA_PUBLISH_EXPIRATION 600
#endif
#ifndef PJSUA_DEFAULT_ACC_PRIORITY
#   define PJSUA_DEFAULT_ACC_PRIORITY	0
#endif
#ifndef PJSUA_SECURE_SCHEME
#   define PJSUA_SECURE_SCHEME		"sips"
#endif
struct pjsua_acc_config
{
    
    void	   *user_data;
    
    int		    priority;
    
    pj_str_t	    id;
    
    pj_str_t	    reg_uri;
    
    pj_bool_t	    publish_enabled;
    
    pjsip_auth_clt_pref auth_pref;
    
    pj_str_t	    pidf_tuple_id;
    
    pj_str_t	    force_contact;
    
    pj_bool_t	    require_100rel;
    
    unsigned	    proxy_cnt;
    
    pj_str_t	    proxy[PJSUA_ACC_MAX_PROXIES];
    
    unsigned	    reg_timeout;
    
    unsigned	    cred_count;
    
    pjsip_cred_info cred_info[PJSUA_ACC_MAX_PROXIES];
    
    pjsua_transport_id  transport_id;
    
    pj_bool_t allow_contact_rewrite;
    
    unsigned	     ka_interval;
    
    pj_str_t	     ka_data;
#if defined(PJMEDIA_HAS_SRTP) && (PJMEDIA_HAS_SRTP != 0)
    
    pjmedia_srtp_use	use_srtp;
    
    int		     srtp_secure_signaling;
#endif
};
%rename(acc_config_default) pjsua_acc_config_default;
%javamethodmodifiers pjsua_acc_config_default(pjsua_acc_config *cfg) "public synchronized";
%javamethodmodifiers pjsua_acc_config_default(pjsua_acc_config *cfg) "public synchronized";
PJ_DECL(void) pjsua_acc_config_default(pjsua_acc_config *cfg);
%rename(acc_config_dup) pjsua_acc_config_dup;
%javamethodmodifiers pjsua_acc_config_dup(pj_pool_t *pool,
				   pjsua_acc_config *dst,
				   const pjsua_acc_config *src) "public synchronized";
%javamethodmodifiers pjsua_acc_config_dup(pj_pool_t *pool,
				   pjsua_acc_config *dst,
				   const pjsua_acc_config *src) "public synchronized";
PJ_DECL(void) pjsua_acc_config_dup(pj_pool_t *pool,
				   pjsua_acc_config *dst,
				   const pjsua_acc_config *src);
struct pjsua_acc_info
{
    
    pjsua_acc_id	id;
    
    pj_bool_t		is_default;
    
    pj_str_t		acc_uri;
    
    pj_bool_t		has_registration;
    
    int			expires;
    
    pjsip_status_code	status;
    
    pj_str_t		status_text;
    
    pj_bool_t		online_status;
    
    pj_str_t		online_status_text;
    
    pjrpid_element	rpid;
    
    char		buf_[PJ_ERR_MSG_SIZE];
};
%rename(acc_get_count) pjsua_acc_get_count;
%javamethodmodifiers pjsua_acc_get_count(void) "public synchronized";
%javamethodmodifiers pjsua_acc_get_count(void) "public synchronized";
PJ_DECL(unsigned) pjsua_acc_get_count(void);
%rename(acc_is_valid) pjsua_acc_is_valid;
%javamethodmodifiers pjsua_acc_is_valid(pjsua_acc_id acc_id) "public synchronized";
%javamethodmodifiers pjsua_acc_is_valid(pjsua_acc_id acc_id) "public synchronized";
PJ_DECL(pj_bool_t) pjsua_acc_is_valid(pjsua_acc_id acc_id);
%rename(acc_set_default) pjsua_acc_set_default;
%javamethodmodifiers pjsua_acc_set_default(pjsua_acc_id acc_id) "public synchronized";
%javamethodmodifiers pjsua_acc_set_default(pjsua_acc_id acc_id) "public synchronized";
PJ_DECL(pj_status_t) pjsua_acc_set_default(pjsua_acc_id acc_id);
%rename(acc_get_default) pjsua_acc_get_default;
%javamethodmodifiers pjsua_acc_get_default(void) "public synchronized";
%javamethodmodifiers pjsua_acc_get_default(void) "public synchronized";
PJ_DECL(pjsua_acc_id) pjsua_acc_get_default(void);
%rename(acc_add) pjsua_acc_add;
%javamethodmodifiers pjsua_acc_add(const pjsua_acc_config *acc_cfg,
				   pj_bool_t is_default,
				   pjsua_acc_id *p_acc_id) "public synchronized";
%javamethodmodifiers pjsua_acc_add(const pjsua_acc_config *acc_cfg,
				   pj_bool_t is_default,
				   pjsua_acc_id *p_acc_id) "public synchronized";
PJ_DECL(pj_status_t) pjsua_acc_add(const pjsua_acc_config *acc_cfg,
				   pj_bool_t is_default,
				   pjsua_acc_id *p_acc_id);
%rename(acc_add_local) pjsua_acc_add_local;
%javamethodmodifiers pjsua_acc_add_local(pjsua_transport_id tid,
					 pj_bool_t is_default,
					 pjsua_acc_id *p_acc_id) "public synchronized";
%javamethodmodifiers pjsua_acc_add_local(pjsua_transport_id tid,
					 pj_bool_t is_default,
					 pjsua_acc_id *p_acc_id) "public synchronized";
PJ_DECL(pj_status_t) pjsua_acc_add_local(pjsua_transport_id tid,
					 pj_bool_t is_default,
					 pjsua_acc_id *p_acc_id);
%rename(acc_set_user_data) pjsua_acc_set_user_data;
%javamethodmodifiers pjsua_acc_set_user_data(pjsua_acc_id acc_id,
					     void *user_data) "public synchronized";
%javamethodmodifiers pjsua_acc_set_user_data(pjsua_acc_id acc_id,
					     void *user_data) "public synchronized";
PJ_DECL(pj_status_t) pjsua_acc_set_user_data(pjsua_acc_id acc_id,
					     void *user_data);
%rename(acc_get_user_data) pjsua_acc_get_user_data;
%javamethodmodifiers pjsua_acc_get_user_data(pjsua_acc_id acc_id) "public synchronized";
%javamethodmodifiers pjsua_acc_get_user_data(pjsua_acc_id acc_id) "public synchronized";
PJ_DECL(void*) pjsua_acc_get_user_data(pjsua_acc_id acc_id);
%rename(acc_del) pjsua_acc_del;
%javamethodmodifiers pjsua_acc_del(pjsua_acc_id acc_id) "public synchronized";
%javamethodmodifiers pjsua_acc_del(pjsua_acc_id acc_id) "public synchronized";
PJ_DECL(pj_status_t) pjsua_acc_del(pjsua_acc_id acc_id);
%rename(acc_modify) pjsua_acc_modify;
%javamethodmodifiers pjsua_acc_modify(pjsua_acc_id acc_id,
				      const pjsua_acc_config *acc_cfg) "public synchronized";
%javamethodmodifiers pjsua_acc_modify(pjsua_acc_id acc_id,
				      const pjsua_acc_config *acc_cfg) "public synchronized";
PJ_DECL(pj_status_t) pjsua_acc_modify(pjsua_acc_id acc_id,
				      const pjsua_acc_config *acc_cfg);
%rename(acc_set_online_status) pjsua_acc_set_online_status;
%javamethodmodifiers pjsua_acc_set_online_status(pjsua_acc_id acc_id,
						 pj_bool_t is_online) "public synchronized";
%javamethodmodifiers pjsua_acc_set_online_status(pjsua_acc_id acc_id,
						 pj_bool_t is_online) "public synchronized";
PJ_DECL(pj_status_t) pjsua_acc_set_online_status(pjsua_acc_id acc_id,
						 pj_bool_t is_online);
%rename(acc_set_online_status2) pjsua_acc_set_online_status2;
%javamethodmodifiers pjsua_acc_set_online_status2(pjsua_acc_id acc_id,
						  pj_bool_t is_online,
						  const pjrpid_element *pr) "public synchronized";
%javamethodmodifiers pjsua_acc_set_online_status2(pjsua_acc_id acc_id,
						  pj_bool_t is_online,
						  const pjrpid_element *pr) "public synchronized";
PJ_DECL(pj_status_t) pjsua_acc_set_online_status2(pjsua_acc_id acc_id,
						  pj_bool_t is_online,
						  const pjrpid_element *pr);
%rename(acc_set_registration) pjsua_acc_set_registration;
%javamethodmodifiers pjsua_acc_set_registration(pjsua_acc_id acc_id, 
						pj_bool_t renew) "public synchronized";
%javamethodmodifiers pjsua_acc_set_registration(pjsua_acc_id acc_id, 
						pj_bool_t renew) "public synchronized";
PJ_DECL(pj_status_t) pjsua_acc_set_registration(pjsua_acc_id acc_id, 
						pj_bool_t renew);
%rename(acc_get_info) pjsua_acc_get_info;
%javamethodmodifiers pjsua_acc_get_info(pjsua_acc_id acc_id,
					pjsua_acc_info *info) "public synchronized";
%javamethodmodifiers pjsua_acc_get_info(pjsua_acc_id acc_id,
					pjsua_acc_info *info) "public synchronized";
PJ_DECL(pj_status_t) pjsua_acc_get_info(pjsua_acc_id acc_id,
					pjsua_acc_info *info);
%rename(enum_accs) pjsua_enum_accs;
%javamethodmodifiers pjsua_enum_accs(pjsua_acc_id ids[],
				     unsigned *count ) "public synchronized";
%javamethodmodifiers pjsua_enum_accs(pjsua_acc_id ids[],
				     unsigned *count ) "public synchronized";
PJ_DECL(pj_status_t) pjsua_enum_accs(pjsua_acc_id ids[],
				     unsigned *count );
%rename(acc_enum_info) pjsua_acc_enum_info;
%javamethodmodifiers pjsua_acc_enum_info( pjsua_acc_info info[],
					  unsigned *count ) "public synchronized";
%javamethodmodifiers pjsua_acc_enum_info( pjsua_acc_info info[],
					  unsigned *count ) "public synchronized";
PJ_DECL(pj_status_t) pjsua_acc_enum_info( pjsua_acc_info info[],
					  unsigned *count );
%rename(acc_find_for_outgoing) pjsua_acc_find_for_outgoing;
%javamethodmodifiers pjsua_acc_find_for_outgoing(const pj_str_t *url) "public synchronized";
%javamethodmodifiers pjsua_acc_find_for_outgoing(const pj_str_t *url) "public synchronized";
PJ_DECL(pjsua_acc_id) pjsua_acc_find_for_outgoing(const pj_str_t *url);
%rename(acc_find_for_incoming) pjsua_acc_find_for_incoming;
%javamethodmodifiers pjsua_acc_find_for_incoming(pjsip_rx_data *rdata) "public synchronized";
%javamethodmodifiers pjsua_acc_find_for_incoming(pjsip_rx_data *rdata) "public synchronized";
PJ_DECL(pjsua_acc_id) pjsua_acc_find_for_incoming(pjsip_rx_data *rdata);
%rename(acc_create_request) pjsua_acc_create_request;
%javamethodmodifiers pjsua_acc_create_request(pjsua_acc_id acc_id,
					      const pjsip_method *method,
					      const pj_str_t *target,
					      pjsip_tx_data **p_tdata) "public synchronized";
%javamethodmodifiers pjsua_acc_create_request(pjsua_acc_id acc_id,
					      const pjsip_method *method,
					      const pj_str_t *target,
					      pjsip_tx_data **p_tdata) "public synchronized";
PJ_DECL(pj_status_t) pjsua_acc_create_request(pjsua_acc_id acc_id,
					      const pjsip_method *method,
					      const pj_str_t *target,
					      pjsip_tx_data **p_tdata);
%rename(acc_create_uac_contact) pjsua_acc_create_uac_contact;
%javamethodmodifiers pjsua_acc_create_uac_contact( pj_pool_t *pool,
						   pj_str_t *contact,
						   pjsua_acc_id acc_id,
						   const pj_str_t *uri) "public synchronized";
%javamethodmodifiers pjsua_acc_create_uac_contact( pj_pool_t *pool,
						   pj_str_t *contact,
						   pjsua_acc_id acc_id,
						   const pj_str_t *uri) "public synchronized";
PJ_DECL(pj_status_t) pjsua_acc_create_uac_contact( pj_pool_t *pool,
						   pj_str_t *contact,
						   pjsua_acc_id acc_id,
						   const pj_str_t *uri);
							   
%rename(acc_create_uas_contact) pjsua_acc_create_uas_contact;
%javamethodmodifiers pjsua_acc_create_uas_contact( pj_pool_t *pool,
						   pj_str_t *contact,
						   pjsua_acc_id acc_id,
						   pjsip_rx_data *rdata ) "public synchronized";
%javamethodmodifiers pjsua_acc_create_uas_contact( pj_pool_t *pool,
						   pj_str_t *contact,
						   pjsua_acc_id acc_id,
						   pjsip_rx_data *rdata ) "public synchronized";
PJ_DECL(pj_status_t) pjsua_acc_create_uas_contact( pj_pool_t *pool,
						   pj_str_t *contact,
						   pjsua_acc_id acc_id,
						   pjsip_rx_data *rdata );
							   
%rename(acc_set_transport) pjsua_acc_set_transport;
%javamethodmodifiers pjsua_acc_set_transport(pjsua_acc_id acc_id,
					     pjsua_transport_id tp_id) "public synchronized";
%javamethodmodifiers pjsua_acc_set_transport(pjsua_acc_id acc_id,
					     pjsua_transport_id tp_id) "public synchronized";
PJ_DECL(pj_status_t) pjsua_acc_set_transport(pjsua_acc_id acc_id,
					     pjsua_transport_id tp_id);
#ifndef PJSUA_MAX_CALLS
#   define PJSUA_MAX_CALLS	    32
#endif
enum pjsua_call_media_status
{
    
    PJSUA_CALL_MEDIA_NONE,
    
    PJSUA_CALL_MEDIA_ACTIVE,
    
    PJSUA_CALL_MEDIA_LOCAL_HOLD,
    
    PJSUA_CALL_MEDIA_REMOTE_HOLD,
    
    PJSUA_CALL_MEDIA_ERROR
};
struct pjsua_call_info
{
    
    pjsua_call_id	id;
    
    pjsip_role_e	role;
    
    pjsua_acc_id	acc_id;
    
    pj_str_t		local_info;
    
    pj_str_t		local_contact;
    
    pj_str_t		remote_info;
    
    pj_str_t		remote_contact;
    
    pj_str_t		call_id;
    
    pjsip_inv_state	state;
    
    pj_str_t		state_text;
    
    pjsip_status_code	last_status;
    
    pj_str_t		last_status_text;
    
    pjsua_call_media_status media_status;
    
    pjmedia_dir		media_dir;
    
    pjsua_conf_port_id	conf_slot;
    
    pj_time_val		connect_duration;
    
    pj_time_val		total_duration;
    
    struct {
	char	local_info[128];
	char	local_contact[128];
	char	remote_info[128];
	char	remote_contact[128];
	char	call_id[128];
	char	last_status_text[128];
    } buf_;
};
%rename(call_get_max_count) pjsua_call_get_max_count;
%javamethodmodifiers pjsua_call_get_max_count(void) "public synchronized";
%javamethodmodifiers pjsua_call_get_max_count(void) "public synchronized";
PJ_DECL(unsigned) pjsua_call_get_max_count(void);
%rename(call_get_count) pjsua_call_get_count;
%javamethodmodifiers pjsua_call_get_count(void) "public synchronized";
%javamethodmodifiers pjsua_call_get_count(void) "public synchronized";
PJ_DECL(unsigned) pjsua_call_get_count(void);
%rename(enum_calls) pjsua_enum_calls;
%javamethodmodifiers pjsua_enum_calls(pjsua_call_id ids[],
				      unsigned *count) "public synchronized";
%javamethodmodifiers pjsua_enum_calls(pjsua_call_id ids[],
				      unsigned *count) "public synchronized";
PJ_DECL(pj_status_t) pjsua_enum_calls(pjsua_call_id ids[],
				      unsigned *count);
%rename(call_make_call) pjsua_call_make_call;
%javamethodmodifiers pjsua_call_make_call(pjsua_acc_id acc_id,
					  const pj_str_t *dst_uri,
					  unsigned options,
					  void *user_data,
					  const pjsua_msg_data *msg_data,
					  pjsua_call_id *p_call_id) "public synchronized";
%javamethodmodifiers pjsua_call_make_call(pjsua_acc_id acc_id,
					  const pj_str_t *dst_uri,
					  unsigned options,
					  void *user_data,
					  const pjsua_msg_data *msg_data,
					  pjsua_call_id *p_call_id) "public synchronized";
PJ_DECL(pj_status_t) pjsua_call_make_call(pjsua_acc_id acc_id,
					  const pj_str_t *dst_uri,
					  unsigned options,
					  void *user_data,
					  const pjsua_msg_data *msg_data,
					  pjsua_call_id *p_call_id);
%rename(call_is_active) pjsua_call_is_active;
%javamethodmodifiers pjsua_call_is_active(pjsua_call_id call_id) "public synchronized";
%javamethodmodifiers pjsua_call_is_active(pjsua_call_id call_id) "public synchronized";
PJ_DECL(pj_bool_t) pjsua_call_is_active(pjsua_call_id call_id);
%rename(call_has_media) pjsua_call_has_media;
%javamethodmodifiers pjsua_call_has_media(pjsua_call_id call_id) "public synchronized";
%javamethodmodifiers pjsua_call_has_media(pjsua_call_id call_id) "public synchronized";
PJ_DECL(pj_bool_t) pjsua_call_has_media(pjsua_call_id call_id);
%rename(call_get_media_session) pjsua_call_get_media_session;
%javamethodmodifiers pjsua_call_get_media_session(pjsua_call_id call_id) "public synchronized";
%javamethodmodifiers pjsua_call_get_media_session(pjsua_call_id call_id) "public synchronized";
PJ_DECL(pjmedia_session*) pjsua_call_get_media_session(pjsua_call_id call_id);
%rename(call_get_media_transport) pjsua_call_get_media_transport;
%javamethodmodifiers pjsua_call_get_media_transport(pjsua_call_id cid) "public synchronized";
%javamethodmodifiers pjsua_call_get_media_transport(pjsua_call_id cid) "public synchronized";
PJ_DECL(pjmedia_transport*) pjsua_call_get_media_transport(pjsua_call_id cid);
%rename(call_get_conf_port) pjsua_call_get_conf_port;
%javamethodmodifiers pjsua_call_get_conf_port(pjsua_call_id call_id) "public synchronized";
%javamethodmodifiers pjsua_call_get_conf_port(pjsua_call_id call_id) "public synchronized";
PJ_DECL(pjsua_conf_port_id) pjsua_call_get_conf_port(pjsua_call_id call_id);
%rename(call_get_info) pjsua_call_get_info;
%javamethodmodifiers pjsua_call_get_info(pjsua_call_id call_id,
					 pjsua_call_info *info) "public synchronized";
%javamethodmodifiers pjsua_call_get_info(pjsua_call_id call_id,
					 pjsua_call_info *info) "public synchronized";
PJ_DECL(pj_status_t) pjsua_call_get_info(pjsua_call_id call_id,
					 pjsua_call_info *info);
%rename(call_set_user_data) pjsua_call_set_user_data;
%javamethodmodifiers pjsua_call_set_user_data(pjsua_call_id call_id,
					      void *user_data) "public synchronized";
%javamethodmodifiers pjsua_call_set_user_data(pjsua_call_id call_id,
					      void *user_data) "public synchronized";
PJ_DECL(pj_status_t) pjsua_call_set_user_data(pjsua_call_id call_id,
					      void *user_data);
%rename(call_get_user_data) pjsua_call_get_user_data;
%javamethodmodifiers pjsua_call_get_user_data(pjsua_call_id call_id) "public synchronized";
%javamethodmodifiers pjsua_call_get_user_data(pjsua_call_id call_id) "public synchronized";
PJ_DECL(void*) pjsua_call_get_user_data(pjsua_call_id call_id);
%rename(call_get_rem_nat_type) pjsua_call_get_rem_nat_type;
%javamethodmodifiers pjsua_call_get_rem_nat_type(pjsua_call_id call_id,
						 pj_stun_nat_type *p_type) "public synchronized";
%javamethodmodifiers pjsua_call_get_rem_nat_type(pjsua_call_id call_id,
						 pj_stun_nat_type *p_type) "public synchronized";
PJ_DECL(pj_status_t) pjsua_call_get_rem_nat_type(pjsua_call_id call_id,
						 pj_stun_nat_type *p_type);
%rename(call_answer) pjsua_call_answer;
%javamethodmodifiers pjsua_call_answer(pjsua_call_id call_id, 
				       unsigned code,
				       const pj_str_t *reason,
				       const pjsua_msg_data *msg_data) "public synchronized";
%javamethodmodifiers pjsua_call_answer(pjsua_call_id call_id, 
				       unsigned code,
				       const pj_str_t *reason,
				       const pjsua_msg_data *msg_data) "public synchronized";
PJ_DECL(pj_status_t) pjsua_call_answer(pjsua_call_id call_id, 
				       unsigned code,
				       const pj_str_t *reason,
				       const pjsua_msg_data *msg_data);
%rename(call_hangup) pjsua_call_hangup;
%javamethodmodifiers pjsua_call_hangup(pjsua_call_id call_id,
				       unsigned code,
				       const pj_str_t *reason,
				       const pjsua_msg_data *msg_data) "public synchronized";
%javamethodmodifiers pjsua_call_hangup(pjsua_call_id call_id,
				       unsigned code,
				       const pj_str_t *reason,
				       const pjsua_msg_data *msg_data) "public synchronized";
PJ_DECL(pj_status_t) pjsua_call_hangup(pjsua_call_id call_id,
				       unsigned code,
				       const pj_str_t *reason,
				       const pjsua_msg_data *msg_data);
%rename(call_set_hold) pjsua_call_set_hold;
%javamethodmodifiers pjsua_call_set_hold(pjsua_call_id call_id,
					 const pjsua_msg_data *msg_data) "public synchronized";
%javamethodmodifiers pjsua_call_set_hold(pjsua_call_id call_id,
					 const pjsua_msg_data *msg_data) "public synchronized";
PJ_DECL(pj_status_t) pjsua_call_set_hold(pjsua_call_id call_id,
					 const pjsua_msg_data *msg_data);
%rename(call_reinvite) pjsua_call_reinvite;
%javamethodmodifiers pjsua_call_reinvite(pjsua_call_id call_id,
					 pj_bool_t unhold,
					 const pjsua_msg_data *msg_data) "public synchronized";
%javamethodmodifiers pjsua_call_reinvite(pjsua_call_id call_id,
					 pj_bool_t unhold,
					 const pjsua_msg_data *msg_data) "public synchronized";
PJ_DECL(pj_status_t) pjsua_call_reinvite(pjsua_call_id call_id,
					 pj_bool_t unhold,
					 const pjsua_msg_data *msg_data);
%rename(call_update) pjsua_call_update;
%javamethodmodifiers pjsua_call_update(pjsua_call_id call_id,
				       unsigned options,
				       const pjsua_msg_data *msg_data) "public synchronized";
%javamethodmodifiers pjsua_call_update(pjsua_call_id call_id,
				       unsigned options,
				       const pjsua_msg_data *msg_data) "public synchronized";
PJ_DECL(pj_status_t) pjsua_call_update(pjsua_call_id call_id,
				       unsigned options,
				       const pjsua_msg_data *msg_data);
%rename(call_xfer) pjsua_call_xfer;
%javamethodmodifiers pjsua_call_xfer(pjsua_call_id call_id, 
				     const pj_str_t *dest,
				     const pjsua_msg_data *msg_data) "public synchronized";
%javamethodmodifiers pjsua_call_xfer(pjsua_call_id call_id, 
				     const pj_str_t *dest,
				     const pjsua_msg_data *msg_data) "public synchronized";
PJ_DECL(pj_status_t) pjsua_call_xfer(pjsua_call_id call_id, 
				     const pj_str_t *dest,
				     const pjsua_msg_data *msg_data);
#define PJSUA_XFER_NO_REQUIRE_REPLACES	1
%rename(call_xfer_replaces) pjsua_call_xfer_replaces;
%javamethodmodifiers pjsua_call_xfer_replaces(pjsua_call_id call_id, 
					      pjsua_call_id dest_call_id,
					      unsigned options,
					      const pjsua_msg_data *msg_data) "public synchronized";
%javamethodmodifiers pjsua_call_xfer_replaces(pjsua_call_id call_id, 
					      pjsua_call_id dest_call_id,
					      unsigned options,
					      const pjsua_msg_data *msg_data) "public synchronized";
PJ_DECL(pj_status_t) pjsua_call_xfer_replaces(pjsua_call_id call_id, 
					      pjsua_call_id dest_call_id,
					      unsigned options,
					      const pjsua_msg_data *msg_data);
%rename(call_dial_dtmf) pjsua_call_dial_dtmf;
%javamethodmodifiers pjsua_call_dial_dtmf(pjsua_call_id call_id, 
					  const pj_str_t *digits) "public synchronized";
%javamethodmodifiers pjsua_call_dial_dtmf(pjsua_call_id call_id, 
					  const pj_str_t *digits) "public synchronized";
PJ_DECL(pj_status_t) pjsua_call_dial_dtmf(pjsua_call_id call_id, 
					  const pj_str_t *digits);
%rename(call_send_im) pjsua_call_send_im;
%javamethodmodifiers pjsua_call_send_im( pjsua_call_id call_id, 
					 const pj_str_t *mime_type,
					 const pj_str_t *content,
					 const pjsua_msg_data *msg_data,
					 void *user_data) "public synchronized";
%javamethodmodifiers pjsua_call_send_im( pjsua_call_id call_id, 
					 const pj_str_t *mime_type,
					 const pj_str_t *content,
					 const pjsua_msg_data *msg_data,
					 void *user_data) "public synchronized";
PJ_DECL(pj_status_t) pjsua_call_send_im( pjsua_call_id call_id, 
					 const pj_str_t *mime_type,
					 const pj_str_t *content,
					 const pjsua_msg_data *msg_data,
					 void *user_data);
%rename(call_send_typing_ind) pjsua_call_send_typing_ind;
%javamethodmodifiers pjsua_call_send_typing_ind(pjsua_call_id call_id, 
						pj_bool_t is_typing,
						const pjsua_msg_data*msg_data) "public synchronized";
%javamethodmodifiers pjsua_call_send_typing_ind(pjsua_call_id call_id, 
						pj_bool_t is_typing,
						const pjsua_msg_data*msg_data) "public synchronized";
PJ_DECL(pj_status_t) pjsua_call_send_typing_ind(pjsua_call_id call_id, 
						pj_bool_t is_typing,
						const pjsua_msg_data*msg_data);
%rename(call_send_request) pjsua_call_send_request;
%javamethodmodifiers pjsua_call_send_request(pjsua_call_id call_id,
					     const pj_str_t *method,
					     const pjsua_msg_data *msg_data) "public synchronized";
%javamethodmodifiers pjsua_call_send_request(pjsua_call_id call_id,
					     const pj_str_t *method,
					     const pjsua_msg_data *msg_data) "public synchronized";
PJ_DECL(pj_status_t) pjsua_call_send_request(pjsua_call_id call_id,
					     const pj_str_t *method,
					     const pjsua_msg_data *msg_data);
%rename(call_hangup_all) pjsua_call_hangup_all;
%javamethodmodifiers pjsua_call_hangup_all(void) "public synchronized";
%javamethodmodifiers pjsua_call_hangup_all(void) "public synchronized";
PJ_DECL(void) pjsua_call_hangup_all(void);
%rename(call_dump) pjsua_call_dump;
%javamethodmodifiers pjsua_call_dump(pjsua_call_id call_id, 
				     pj_bool_t with_media, 
				     char *buffer, 
				     unsigned maxlen,
				     const char *indent) "public synchronized";
%javamethodmodifiers pjsua_call_dump(pjsua_call_id call_id, 
				     pj_bool_t with_media, 
				     char *buffer, 
				     unsigned maxlen,
				     const char *indent) "public synchronized";
PJ_DECL(pj_status_t) pjsua_call_dump(pjsua_call_id call_id, 
				     pj_bool_t with_media, 
				     char *buffer, 
				     unsigned maxlen,
				     const char *indent);
#ifndef PJSUA_MAX_BUDDIES
#   define PJSUA_MAX_BUDDIES	    256
#endif
#ifndef PJSUA_PRES_TIMER
#   define PJSUA_PRES_TIMER	    300
#endif
struct pjsua_buddy_config
{
    
    pj_str_t	uri;
    
    pj_bool_t	subscribe;
    
    void       *user_data;
};
enum pjsua_buddy_status
{
    
    PJSUA_BUDDY_STATUS_UNKNOWN,
    
    PJSUA_BUDDY_STATUS_ONLINE,
    
    PJSUA_BUDDY_STATUS_OFFLINE,
};
struct pjsua_buddy_info
{
    
    pjsua_buddy_id	id;
    
    pj_str_t		uri;
    
    pj_str_t		contact;
    
    pjsua_buddy_status	status;
    
    pj_str_t		status_text;
    
    pj_bool_t		monitor_pres;
    
    pjsip_evsub_state	sub_state;
    
    pj_str_t		sub_term_reason;
    
    pjrpid_element	rpid;
    
    char		buf_[512];
};
%rename(buddy_config_default) pjsua_buddy_config_default;
%javamethodmodifiers pjsua_buddy_config_default(pjsua_buddy_config *cfg) "public synchronized";
%javamethodmodifiers pjsua_buddy_config_default(pjsua_buddy_config *cfg) "public synchronized";
PJ_DECL(void) pjsua_buddy_config_default(pjsua_buddy_config *cfg);
%rename(get_buddy_count) pjsua_get_buddy_count;
%javamethodmodifiers pjsua_get_buddy_count(void) "public synchronized";
%javamethodmodifiers pjsua_get_buddy_count(void) "public synchronized";
PJ_DECL(unsigned) pjsua_get_buddy_count(void);
%rename(buddy_is_valid) pjsua_buddy_is_valid;
%javamethodmodifiers pjsua_buddy_is_valid(pjsua_buddy_id buddy_id) "public synchronized";
%javamethodmodifiers pjsua_buddy_is_valid(pjsua_buddy_id buddy_id) "public synchronized";
PJ_DECL(pj_bool_t) pjsua_buddy_is_valid(pjsua_buddy_id buddy_id);
%rename(enum_buddies) pjsua_enum_buddies;
%javamethodmodifiers pjsua_enum_buddies(pjsua_buddy_id ids[],
					unsigned *count) "public synchronized";
%javamethodmodifiers pjsua_enum_buddies(pjsua_buddy_id ids[],
					unsigned *count) "public synchronized";
PJ_DECL(pj_status_t) pjsua_enum_buddies(pjsua_buddy_id ids[],
					unsigned *count);
%rename(buddy_find) pjsua_buddy_find;
%javamethodmodifiers pjsua_buddy_find(const pj_str_t *uri) "public synchronized";
%javamethodmodifiers pjsua_buddy_find(const pj_str_t *uri) "public synchronized";
PJ_DECL(pjsua_buddy_id) pjsua_buddy_find(const pj_str_t *uri);
%rename(buddy_get_info) pjsua_buddy_get_info;
%javamethodmodifiers pjsua_buddy_get_info(pjsua_buddy_id buddy_id,
					  pjsua_buddy_info *info) "public synchronized";
%javamethodmodifiers pjsua_buddy_get_info(pjsua_buddy_id buddy_id,
					  pjsua_buddy_info *info) "public synchronized";
PJ_DECL(pj_status_t) pjsua_buddy_get_info(pjsua_buddy_id buddy_id,
					  pjsua_buddy_info *info);
%rename(buddy_set_user_data) pjsua_buddy_set_user_data;
%javamethodmodifiers pjsua_buddy_set_user_data(pjsua_buddy_id buddy_id,
					       void *user_data) "public synchronized";
%javamethodmodifiers pjsua_buddy_set_user_data(pjsua_buddy_id buddy_id,
					       void *user_data) "public synchronized";
PJ_DECL(pj_status_t) pjsua_buddy_set_user_data(pjsua_buddy_id buddy_id,
					       void *user_data);
%rename(buddy_get_user_data) pjsua_buddy_get_user_data;
%javamethodmodifiers pjsua_buddy_get_user_data(pjsua_buddy_id buddy_id) "public synchronized";
%javamethodmodifiers pjsua_buddy_get_user_data(pjsua_buddy_id buddy_id) "public synchronized";
PJ_DECL(void*) pjsua_buddy_get_user_data(pjsua_buddy_id buddy_id);
%rename(buddy_add) pjsua_buddy_add;
%javamethodmodifiers pjsua_buddy_add(const pjsua_buddy_config *buddy_cfg,
				     pjsua_buddy_id *p_buddy_id) "public synchronized";
%javamethodmodifiers pjsua_buddy_add(const pjsua_buddy_config *buddy_cfg,
				     pjsua_buddy_id *p_buddy_id) "public synchronized";
PJ_DECL(pj_status_t) pjsua_buddy_add(const pjsua_buddy_config *buddy_cfg,
				     pjsua_buddy_id *p_buddy_id);
%rename(buddy_del) pjsua_buddy_del;
%javamethodmodifiers pjsua_buddy_del(pjsua_buddy_id buddy_id) "public synchronized";
%javamethodmodifiers pjsua_buddy_del(pjsua_buddy_id buddy_id) "public synchronized";
PJ_DECL(pj_status_t) pjsua_buddy_del(pjsua_buddy_id buddy_id);
%rename(buddy_subscribe_pres) pjsua_buddy_subscribe_pres;
%javamethodmodifiers pjsua_buddy_subscribe_pres(pjsua_buddy_id buddy_id,
						pj_bool_t subscribe) "public synchronized";
%javamethodmodifiers pjsua_buddy_subscribe_pres(pjsua_buddy_id buddy_id,
						pj_bool_t subscribe) "public synchronized";
PJ_DECL(pj_status_t) pjsua_buddy_subscribe_pres(pjsua_buddy_id buddy_id,
						pj_bool_t subscribe);
%rename(buddy_update_pres) pjsua_buddy_update_pres;
%javamethodmodifiers pjsua_buddy_update_pres(pjsua_buddy_id buddy_id) "public synchronized";
%javamethodmodifiers pjsua_buddy_update_pres(pjsua_buddy_id buddy_id) "public synchronized";
PJ_DECL(pj_status_t) pjsua_buddy_update_pres(pjsua_buddy_id buddy_id);
%rename(pres_notify) pjsua_pres_notify;
%javamethodmodifiers pjsua_pres_notify(pjsua_acc_id acc_id,
				       pjsua_srv_pres *srv_pres,
				       pjsip_evsub_state state,
				       const pj_str_t *state_str,
				       const pj_str_t *reason,
				       pj_bool_t with_body,
				       const pjsua_msg_data *msg_data) "public synchronized";
%javamethodmodifiers pjsua_pres_notify(pjsua_acc_id acc_id,
				       pjsua_srv_pres *srv_pres,
				       pjsip_evsub_state state,
				       const pj_str_t *state_str,
				       const pj_str_t *reason,
				       pj_bool_t with_body,
				       const pjsua_msg_data *msg_data) "public synchronized";
PJ_DECL(pj_status_t) pjsua_pres_notify(pjsua_acc_id acc_id,
				       pjsua_srv_pres *srv_pres,
				       pjsip_evsub_state state,
				       const pj_str_t *state_str,
				       const pj_str_t *reason,
				       pj_bool_t with_body,
				       const pjsua_msg_data *msg_data);
%rename(pres_dump) pjsua_pres_dump;
%javamethodmodifiers pjsua_pres_dump(pj_bool_t verbose) "public synchronized";
%javamethodmodifiers pjsua_pres_dump(pj_bool_t verbose) "public synchronized";
PJ_DECL(void) pjsua_pres_dump(pj_bool_t verbose);
extern const pjsip_method pjsip_message_method;
%rename(im_send) pjsua_im_send;
%javamethodmodifiers pjsua_im_send(pjsua_acc_id acc_id, 
				   const pj_str_t *to,
				   const pj_str_t *mime_type,
				   const pj_str_t *content,
				   const pjsua_msg_data *msg_data,
				   void *user_data) "public synchronized";
%javamethodmodifiers pjsua_im_send(pjsua_acc_id acc_id, 
				   const pj_str_t *to,
				   const pj_str_t *mime_type,
				   const pj_str_t *content,
				   const pjsua_msg_data *msg_data,
				   void *user_data) "public synchronized";
PJ_DECL(pj_status_t) pjsua_im_send(pjsua_acc_id acc_id, 
				   const pj_str_t *to,
				   const pj_str_t *mime_type,
				   const pj_str_t *content,
				   const pjsua_msg_data *msg_data,
				   void *user_data);
%rename(im_typing) pjsua_im_typing;
%javamethodmodifiers pjsua_im_typing(pjsua_acc_id acc_id, 
				     const pj_str_t *to, 
				     pj_bool_t is_typing,
				     const pjsua_msg_data *msg_data) "public synchronized";
%javamethodmodifiers pjsua_im_typing(pjsua_acc_id acc_id, 
				     const pj_str_t *to, 
				     pj_bool_t is_typing,
				     const pjsua_msg_data *msg_data) "public synchronized";
PJ_DECL(pj_status_t) pjsua_im_typing(pjsua_acc_id acc_id, 
				     const pj_str_t *to, 
				     pj_bool_t is_typing,
				     const pjsua_msg_data *msg_data);
#ifndef PJSUA_MAX_CONF_PORTS
#   define PJSUA_MAX_CONF_PORTS		254
#endif
#ifndef PJSUA_DEFAULT_CLOCK_RATE
#   define PJSUA_DEFAULT_CLOCK_RATE	16000
#endif
#ifndef PJSUA_DEFAULT_AUDIO_FRAME_PTIME
#   define PJSUA_DEFAULT_AUDIO_FRAME_PTIME  20
#endif
#ifndef PJSUA_DEFAULT_CODEC_QUALITY
#   define PJSUA_DEFAULT_CODEC_QUALITY	8
#endif
#ifndef PJSUA_DEFAULT_ILBC_MODE
#   define PJSUA_DEFAULT_ILBC_MODE	30
#endif
#ifndef PJSUA_DEFAULT_EC_TAIL_LEN
#   define PJSUA_DEFAULT_EC_TAIL_LEN	200
#endif
#ifndef PJSUA_MAX_PLAYERS
#   define PJSUA_MAX_PLAYERS		32
#endif
#ifndef PJSUA_MAX_RECORDERS
#   define PJSUA_MAX_RECORDERS		32
#endif
struct pjsua_media_config
{
    
    unsigned		clock_rate;
    
    unsigned		snd_clock_rate;
    
    unsigned		channel_count;
    
    unsigned		audio_frame_ptime;
    
    unsigned		max_media_ports;
    
    pj_bool_t		has_ioqueue;
    
    unsigned		thread_cnt;
    
    unsigned		quality;
    
    unsigned		ptime;
    
    pj_bool_t		no_vad;
    
    unsigned		ilbc_mode;
    
    unsigned		tx_drop_pct;
    
    unsigned		rx_drop_pct;
    
    unsigned		ec_options;
    
    unsigned		ec_tail_len;
    
    int			jb_init;
    
    int			jb_min_pre;
    
    
    int			jb_max_pre;
    
    int			jb_max;
    
    pj_bool_t		enable_ice;
    
    pj_bool_t		ice_no_host_cands;
    
    pj_bool_t		ice_no_rtcp;
    
    pj_bool_t		enable_turn;
    
    pj_str_t		turn_server;
    
    pj_turn_tp_type	turn_conn_type;
    
    pj_stun_auth_cred	turn_auth_cred;
    
    int			snd_auto_close_time;
};
%rename(media_config_default) pjsua_media_config_default;
%javamethodmodifiers pjsua_media_config_default(pjsua_media_config *cfg) "public synchronized";
%javamethodmodifiers pjsua_media_config_default(pjsua_media_config *cfg) "public synchronized";
PJ_DECL(void) pjsua_media_config_default(pjsua_media_config *cfg);
struct pjsua_codec_info
{
    
    pj_str_t		codec_id;
    
    pj_uint8_t		priority;
    
    char		buf_[32];
};
struct pjsua_conf_port_info
{
    
    pjsua_conf_port_id	slot_id;
    
    pj_str_t		name;
    
    unsigned		clock_rate;
    
    unsigned		channel_count;
    
    unsigned		samples_per_frame;
    
    unsigned		bits_per_sample;
    
    unsigned		listener_cnt;
    
    pjsua_conf_port_id	listeners[PJSUA_MAX_CONF_PORTS];
};
struct pjsua_media_transport
{
    
    pjmedia_sock_info	 skinfo;
    
    pjmedia_transport	*transport;
};
%rename(conf_get_max_ports) pjsua_conf_get_max_ports;
%javamethodmodifiers pjsua_conf_get_max_ports(void) "public synchronized";
%javamethodmodifiers pjsua_conf_get_max_ports(void) "public synchronized";
PJ_DECL(unsigned) pjsua_conf_get_max_ports(void);
%rename(conf_get_active_ports) pjsua_conf_get_active_ports;
%javamethodmodifiers pjsua_conf_get_active_ports(void) "public synchronized";
%javamethodmodifiers pjsua_conf_get_active_ports(void) "public synchronized";
PJ_DECL(unsigned) pjsua_conf_get_active_ports(void);
%rename(enum_conf_ports) pjsua_enum_conf_ports;
%javamethodmodifiers pjsua_enum_conf_ports(pjsua_conf_port_id id[],
					   unsigned *count) "public synchronized";
%javamethodmodifiers pjsua_enum_conf_ports(pjsua_conf_port_id id[],
					   unsigned *count) "public synchronized";
PJ_DECL(pj_status_t) pjsua_enum_conf_ports(pjsua_conf_port_id id[],
					   unsigned *count);
%rename(conf_get_port_info) pjsua_conf_get_port_info;
%javamethodmodifiers pjsua_conf_get_port_info( pjsua_conf_port_id port_id,
					       pjsua_conf_port_info *info) "public synchronized";
%javamethodmodifiers pjsua_conf_get_port_info( pjsua_conf_port_id port_id,
					       pjsua_conf_port_info *info) "public synchronized";
PJ_DECL(pj_status_t) pjsua_conf_get_port_info( pjsua_conf_port_id port_id,
					       pjsua_conf_port_info *info);
%rename(conf_add_port) pjsua_conf_add_port;
%javamethodmodifiers pjsua_conf_add_port(pj_pool_t *pool,
					 pjmedia_port *port,
					 pjsua_conf_port_id *p_id) "public synchronized";
%javamethodmodifiers pjsua_conf_add_port(pj_pool_t *pool,
					 pjmedia_port *port,
					 pjsua_conf_port_id *p_id) "public synchronized";
PJ_DECL(pj_status_t) pjsua_conf_add_port(pj_pool_t *pool,
					 pjmedia_port *port,
					 pjsua_conf_port_id *p_id);
%rename(conf_remove_port) pjsua_conf_remove_port;
%javamethodmodifiers pjsua_conf_remove_port(pjsua_conf_port_id port_id) "public synchronized";
%javamethodmodifiers pjsua_conf_remove_port(pjsua_conf_port_id port_id) "public synchronized";
PJ_DECL(pj_status_t) pjsua_conf_remove_port(pjsua_conf_port_id port_id);
%rename(conf_connect) pjsua_conf_connect;
%javamethodmodifiers pjsua_conf_connect(pjsua_conf_port_id source,
					pjsua_conf_port_id sink) "public synchronized";
%javamethodmodifiers pjsua_conf_connect(pjsua_conf_port_id source,
					pjsua_conf_port_id sink) "public synchronized";
PJ_DECL(pj_status_t) pjsua_conf_connect(pjsua_conf_port_id source,
					pjsua_conf_port_id sink);
%rename(conf_disconnect) pjsua_conf_disconnect;
%javamethodmodifiers pjsua_conf_disconnect(pjsua_conf_port_id source,
					   pjsua_conf_port_id sink) "public synchronized";
%javamethodmodifiers pjsua_conf_disconnect(pjsua_conf_port_id source,
					   pjsua_conf_port_id sink) "public synchronized";
PJ_DECL(pj_status_t) pjsua_conf_disconnect(pjsua_conf_port_id source,
					   pjsua_conf_port_id sink);
%rename(conf_adjust_tx_level) pjsua_conf_adjust_tx_level;
%javamethodmodifiers pjsua_conf_adjust_tx_level(pjsua_conf_port_id slot,
						float level) "public synchronized";
%javamethodmodifiers pjsua_conf_adjust_tx_level(pjsua_conf_port_id slot,
						float level) "public synchronized";
PJ_DECL(pj_status_t) pjsua_conf_adjust_tx_level(pjsua_conf_port_id slot,
						float level);
%rename(conf_adjust_rx_level) pjsua_conf_adjust_rx_level;
%javamethodmodifiers pjsua_conf_adjust_rx_level(pjsua_conf_port_id slot,
						float level) "public synchronized";
%javamethodmodifiers pjsua_conf_adjust_rx_level(pjsua_conf_port_id slot,
						float level) "public synchronized";
PJ_DECL(pj_status_t) pjsua_conf_adjust_rx_level(pjsua_conf_port_id slot,
						float level);
%rename(conf_get_signal_level) pjsua_conf_get_signal_level;
%javamethodmodifiers pjsua_conf_get_signal_level(pjsua_conf_port_id slot,
						 unsigned *tx_level,
						 unsigned *rx_level) "public synchronized";
%javamethodmodifiers pjsua_conf_get_signal_level(pjsua_conf_port_id slot,
						 unsigned *tx_level,
						 unsigned *rx_level) "public synchronized";
PJ_DECL(pj_status_t) pjsua_conf_get_signal_level(pjsua_conf_port_id slot,
						 unsigned *tx_level,
						 unsigned *rx_level);
%rename(player_create) pjsua_player_create;
%javamethodmodifiers pjsua_player_create(const pj_str_t *filename,
					 unsigned options,
					 pjsua_player_id *p_id) "public synchronized";
%javamethodmodifiers pjsua_player_create(const pj_str_t *filename,
					 unsigned options,
					 pjsua_player_id *p_id) "public synchronized";
PJ_DECL(pj_status_t) pjsua_player_create(const pj_str_t *filename,
					 unsigned options,
					 pjsua_player_id *p_id);
%rename(playlist_create) pjsua_playlist_create;
%javamethodmodifiers pjsua_playlist_create(const pj_str_t file_names[],
					   unsigned file_count,
					   const pj_str_t *label,
					   unsigned options,
					   pjsua_player_id *p_id) "public synchronized";
%javamethodmodifiers pjsua_playlist_create(const pj_str_t file_names[],
					   unsigned file_count,
					   const pj_str_t *label,
					   unsigned options,
					   pjsua_player_id *p_id) "public synchronized";
PJ_DECL(pj_status_t) pjsua_playlist_create(const pj_str_t file_names[],
					   unsigned file_count,
					   const pj_str_t *label,
					   unsigned options,
					   pjsua_player_id *p_id);
%rename(player_get_conf_port) pjsua_player_get_conf_port;
%javamethodmodifiers pjsua_player_get_conf_port(pjsua_player_id id) "public synchronized";
%javamethodmodifiers pjsua_player_get_conf_port(pjsua_player_id id) "public synchronized";
PJ_DECL(pjsua_conf_port_id) pjsua_player_get_conf_port(pjsua_player_id id);
%rename(player_get_port) pjsua_player_get_port;
%javamethodmodifiers pjsua_player_get_port(pjsua_player_id id,
					   pjmedia_port **p_port) "public synchronized";
%javamethodmodifiers pjsua_player_get_port(pjsua_player_id id,
					   pjmedia_port **p_port) "public synchronized";
PJ_DECL(pj_status_t) pjsua_player_get_port(pjsua_player_id id,
					   pjmedia_port **p_port);
%rename(player_set_pos) pjsua_player_set_pos;
%javamethodmodifiers pjsua_player_set_pos(pjsua_player_id id,
					  pj_uint32_t samples) "public synchronized";
%javamethodmodifiers pjsua_player_set_pos(pjsua_player_id id,
					  pj_uint32_t samples) "public synchronized";
PJ_DECL(pj_status_t) pjsua_player_set_pos(pjsua_player_id id,
					  pj_uint32_t samples);
%rename(player_destroy) pjsua_player_destroy;
%javamethodmodifiers pjsua_player_destroy(pjsua_player_id id) "public synchronized";
%javamethodmodifiers pjsua_player_destroy(pjsua_player_id id) "public synchronized";
PJ_DECL(pj_status_t) pjsua_player_destroy(pjsua_player_id id);
%rename(recorder_create) pjsua_recorder_create;
%javamethodmodifiers pjsua_recorder_create(const pj_str_t *filename,
					   unsigned enc_type,
					   void *enc_param,
					   pj_ssize_t max_size,
					   unsigned options,
					   pjsua_recorder_id *p_id) "public synchronized";
%javamethodmodifiers pjsua_recorder_create(const pj_str_t *filename,
					   unsigned enc_type,
					   void *enc_param,
					   pj_ssize_t max_size,
					   unsigned options,
					   pjsua_recorder_id *p_id) "public synchronized";
PJ_DECL(pj_status_t) pjsua_recorder_create(const pj_str_t *filename,
					   unsigned enc_type,
					   void *enc_param,
					   pj_ssize_t max_size,
					   unsigned options,
					   pjsua_recorder_id *p_id);
%rename(recorder_get_conf_port) pjsua_recorder_get_conf_port;
%javamethodmodifiers pjsua_recorder_get_conf_port(pjsua_recorder_id id) "public synchronized";
%javamethodmodifiers pjsua_recorder_get_conf_port(pjsua_recorder_id id) "public synchronized";
PJ_DECL(pjsua_conf_port_id) pjsua_recorder_get_conf_port(pjsua_recorder_id id);
%rename(recorder_get_port) pjsua_recorder_get_port;
%javamethodmodifiers pjsua_recorder_get_port(pjsua_recorder_id id,
					     pjmedia_port **p_port) "public synchronized";
%javamethodmodifiers pjsua_recorder_get_port(pjsua_recorder_id id,
					     pjmedia_port **p_port) "public synchronized";
PJ_DECL(pj_status_t) pjsua_recorder_get_port(pjsua_recorder_id id,
					     pjmedia_port **p_port);
%rename(recorder_destroy) pjsua_recorder_destroy;
%javamethodmodifiers pjsua_recorder_destroy(pjsua_recorder_id id) "public synchronized";
%javamethodmodifiers pjsua_recorder_destroy(pjsua_recorder_id id) "public synchronized";
PJ_DECL(pj_status_t) pjsua_recorder_destroy(pjsua_recorder_id id);
%rename(enum_snd_devs) pjsua_enum_snd_devs;
%javamethodmodifiers pjsua_enum_snd_devs(pjmedia_snd_dev_info info[],
					 unsigned *count) "public synchronized";
%javamethodmodifiers pjsua_enum_snd_devs(pjmedia_snd_dev_info info[],
					 unsigned *count) "public synchronized";
PJ_DECL(pj_status_t) pjsua_enum_snd_devs(pjmedia_snd_dev_info info[],
					 unsigned *count);
%rename(get_snd_dev) pjsua_get_snd_dev;
%javamethodmodifiers pjsua_get_snd_dev(int *capture_dev,
				       int *playback_dev) "public synchronized";
%javamethodmodifiers pjsua_get_snd_dev(int *capture_dev,
				       int *playback_dev) "public synchronized";
PJ_DECL(pj_status_t) pjsua_get_snd_dev(int *capture_dev,
				       int *playback_dev);
%rename(set_snd_dev) pjsua_set_snd_dev;
%javamethodmodifiers pjsua_set_snd_dev(int capture_dev,
				       int playback_dev) "public synchronized";
%javamethodmodifiers pjsua_set_snd_dev(int capture_dev,
				       int playback_dev) "public synchronized";
PJ_DECL(pj_status_t) pjsua_set_snd_dev(int capture_dev,
				       int playback_dev);
%rename(set_null_snd_dev) pjsua_set_null_snd_dev;
%javamethodmodifiers pjsua_set_null_snd_dev(void) "public synchronized";
%javamethodmodifiers pjsua_set_null_snd_dev(void) "public synchronized";
PJ_DECL(pj_status_t) pjsua_set_null_snd_dev(void);
%rename(set_no_snd_dev) pjsua_set_no_snd_dev;
%javamethodmodifiers pjsua_set_no_snd_dev(void) "public synchronized";
%javamethodmodifiers pjsua_set_no_snd_dev(void) "public synchronized";
PJ_DECL(pjmedia_port*) pjsua_set_no_snd_dev(void);
%rename(set_ec) pjsua_set_ec;
%javamethodmodifiers pjsua_set_ec(unsigned tail_ms, unsigned options) "public synchronized";
%javamethodmodifiers pjsua_set_ec(unsigned tail_ms, unsigned options) "public synchronized";
PJ_DECL(pj_status_t) pjsua_set_ec(unsigned tail_ms, unsigned options);
%rename(get_ec_tail) pjsua_get_ec_tail;
%javamethodmodifiers pjsua_get_ec_tail(unsigned *p_tail_ms) "public synchronized";
%javamethodmodifiers pjsua_get_ec_tail(unsigned *p_tail_ms) "public synchronized";
PJ_DECL(pj_status_t) pjsua_get_ec_tail(unsigned *p_tail_ms);
%rename(enum_codecs) pjsua_enum_codecs;
%javamethodmodifiers pjsua_enum_codecs( pjsua_codec_info id[],
				        unsigned *count ) "public synchronized";
%javamethodmodifiers pjsua_enum_codecs( pjsua_codec_info id[],
				        unsigned *count ) "public synchronized";
PJ_DECL(pj_status_t) pjsua_enum_codecs( pjsua_codec_info id[],
				        unsigned *count );
%rename(codec_set_priority) pjsua_codec_set_priority;
%javamethodmodifiers pjsua_codec_set_priority( const pj_str_t *codec_id,
					       pj_uint8_t priority ) "public synchronized";
%javamethodmodifiers pjsua_codec_set_priority( const pj_str_t *codec_id,
					       pj_uint8_t priority ) "public synchronized";
PJ_DECL(pj_status_t) pjsua_codec_set_priority( const pj_str_t *codec_id,
					       pj_uint8_t priority );
%rename(codec_get_param) pjsua_codec_get_param;
%javamethodmodifiers pjsua_codec_get_param( const pj_str_t *codec_id,
					    pjmedia_codec_param *param ) "public synchronized";
%javamethodmodifiers pjsua_codec_get_param( const pj_str_t *codec_id,
					    pjmedia_codec_param *param ) "public synchronized";
PJ_DECL(pj_status_t) pjsua_codec_get_param( const pj_str_t *codec_id,
					    pjmedia_codec_param *param );
%rename(codec_set_param) pjsua_codec_set_param;
%javamethodmodifiers pjsua_codec_set_param( const pj_str_t *codec_id,
					    const pjmedia_codec_param *param) "public synchronized";
%javamethodmodifiers pjsua_codec_set_param( const pj_str_t *codec_id,
					    const pjmedia_codec_param *param) "public synchronized";
PJ_DECL(pj_status_t) pjsua_codec_set_param( const pj_str_t *codec_id,
					    const pjmedia_codec_param *param);
%rename(media_transports_create) pjsua_media_transports_create;
%javamethodmodifiers pjsua_media_transports_create(const pjsua_transport_config *cfg) "public synchronized";
%javamethodmodifiers pjsua_media_transports_create(const pjsua_transport_config *cfg) "public synchronized";
PJ_DECL(pj_status_t) 
pjsua_media_transports_create(const pjsua_transport_config *cfg);
#define PJ_ERR_MSG_SIZE  80
PJ_DECL(pj_status_t) pj_get_os_error(void);
PJ_DECL(void) pj_set_os_error(pj_status_t code);
PJ_DECL(pj_status_t) pj_get_netos_error(void);
PJ_DECL(void) pj_set_netos_error(pj_status_t code);
PJ_DECL(pj_str_t) pj_strerror( pj_status_t statcode, 
			       char *buf, pj_size_t bufsize);
typedef pj_str_t (*pj_error_callback)(pj_status_t e, char *msg, pj_size_t max);
PJ_DECL(pj_status_t) pj_register_strerror(pj_status_t start_code,
					  pj_status_t err_space,
					  pj_error_callback f);
#ifndef PJ_RETURN_OS_ERROR
#   define PJ_RETURN_OS_ERROR(os_code)   (os_code ? \
					    PJ_STATUS_FROM_OS(os_code) : -1)
#endif
#if PJ_NATIVE_ERR_POSITIVE
#   define PJ_STATUS_FROM_OS(e) (e == 0 ? PJ_SUCCESS : e + PJ_ERRNO_START_SYS)
#else
#   define PJ_STATUS_FROM_OS(e) (e == 0 ? PJ_SUCCESS : PJ_ERRNO_START_SYS - e)
#endif
#if PJ_NATIVE_ERR_POSITIVE
#   define PJ_STATUS_TO_OS(e) (e == 0 ? PJ_SUCCESS : e - PJ_ERRNO_START_SYS)
#else
#   define PJ_STATUS_TO_OS(e) (e == 0 ? PJ_SUCCESS : PJ_ERRNO_START_SYS - e)
#endif
#ifndef PJ_BUILD_ERR
#   define PJ_BUILD_ERR(code,msg) { code, msg " (" #code ")" }
#endif
#define PJ_EUNKNOWN	    (PJ_ERRNO_START_STATUS + 1)	
#define PJ_EPENDING	    (PJ_ERRNO_START_STATUS + 2)	
#define PJ_ETOOMANYCONN	    (PJ_ERRNO_START_STATUS + 3)	
#define PJ_EINVAL	    (PJ_ERRNO_START_STATUS + 4)	
#define PJ_ENAMETOOLONG	    (PJ_ERRNO_START_STATUS + 5)	
#define PJ_ENOTFOUND	    (PJ_ERRNO_START_STATUS + 6)	
#define PJ_ENOMEM	    (PJ_ERRNO_START_STATUS + 7)	
#define PJ_EBUG             (PJ_ERRNO_START_STATUS + 8)	
#define PJ_ETIMEDOUT        (PJ_ERRNO_START_STATUS + 9)	
#define PJ_ETOOMANY         (PJ_ERRNO_START_STATUS + 10)
#define PJ_EBUSY            (PJ_ERRNO_START_STATUS + 11)
#define PJ_ENOTSUP	    (PJ_ERRNO_START_STATUS + 12)
#define PJ_EINVALIDOP	    (PJ_ERRNO_START_STATUS + 13)
#define PJ_ECANCELLED	    (PJ_ERRNO_START_STATUS + 14)
#define PJ_EEXISTS          (PJ_ERRNO_START_STATUS + 15)
#define PJ_EEOF		    (PJ_ERRNO_START_STATUS + 16)
#define PJ_ETOOBIG	    (PJ_ERRNO_START_STATUS + 17)
#define PJ_ERESOLVE	    (PJ_ERRNO_START_STATUS + 18)
#define PJ_ETOOSMALL	    (PJ_ERRNO_START_STATUS + 19)
#define PJ_EIGNORED	    (PJ_ERRNO_START_STATUS + 20)
#define PJ_EIPV6NOTSUP	    (PJ_ERRNO_START_STATUS + 21)
#define PJ_EAFNOTSUP	    (PJ_ERRNO_START_STATUS + 22)
   
   
#define PJ_ERRNO_START		20000
#define PJ_ERRNO_SPACE_SIZE	50000
#define PJ_ERRNO_START_STATUS	(PJ_ERRNO_START + PJ_ERRNO_SPACE_SIZE)
#define PJ_ERRNO_START_SYS	(PJ_ERRNO_START_STATUS + PJ_ERRNO_SPACE_SIZE)
#define PJ_ERRNO_START_USER	(PJ_ERRNO_START_SYS + PJ_ERRNO_SPACE_SIZE)
void pj_errno_clear_handlers(void);
typedef struct pjmedia_snd_port pjmedia_snd_port;
%javamethodmodifiers pjmedia_snd_port_create( pj_pool_t *pool,
					      int rec_id,
					      int play_id,
					      unsigned clock_rate,
					      unsigned channel_count,
					      unsigned samples_per_frame,
					      unsigned bits_per_sample,
					      unsigned options,
					      pjmedia_snd_port **p_port) "public synchronized";
PJ_DECL(pj_status_t) pjmedia_snd_port_create( pj_pool_t *pool,
					      int rec_id,
					      int play_id,
					      unsigned clock_rate,
					      unsigned channel_count,
					      unsigned samples_per_frame,
					      unsigned bits_per_sample,
					      unsigned options,
					      pjmedia_snd_port **p_port);
%javamethodmodifiers pjmedia_snd_port_create_rec(pj_pool_t *pool,
						 int index,
						 unsigned clock_rate,
						 unsigned channel_count,
						 unsigned samples_per_frame,
						 unsigned bits_per_sample,
						 unsigned options,
						 pjmedia_snd_port **p_port) "public synchronized";
PJ_DECL(pj_status_t) pjmedia_snd_port_create_rec(pj_pool_t *pool,
						 int index,
						 unsigned clock_rate,
						 unsigned channel_count,
						 unsigned samples_per_frame,
						 unsigned bits_per_sample,
						 unsigned options,
						 pjmedia_snd_port **p_port);
					      
%javamethodmodifiers pjmedia_snd_port_create_player(pj_pool_t *pool,
						    int index,
						    unsigned clock_rate,
						    unsigned channel_count,
						    unsigned samples_per_frame,
						    unsigned bits_per_sample,
						    unsigned options,
						    pjmedia_snd_port **p_port) "public synchronized";
PJ_DECL(pj_status_t) pjmedia_snd_port_create_player(pj_pool_t *pool,
						    int index,
						    unsigned clock_rate,
						    unsigned channel_count,
						    unsigned samples_per_frame,
						    unsigned bits_per_sample,
						    unsigned options,
						    pjmedia_snd_port **p_port);
					      
%javamethodmodifiers pjmedia_snd_port_destroy(pjmedia_snd_port *snd_port) "public synchronized";
PJ_DECL(pj_status_t) pjmedia_snd_port_destroy(pjmedia_snd_port *snd_port);
%javamethodmodifiers pjmedia_snd_port_get_snd_stream(
						pjmedia_snd_port *snd_port) "public synchronized";
PJ_DECL(pjmedia_snd_stream*) pjmedia_snd_port_get_snd_stream(
						pjmedia_snd_port *snd_port);
%javamethodmodifiers pjmedia_snd_port_set_ec( pjmedia_snd_port *snd_port,
					      pj_pool_t *pool,
					      unsigned tail_ms,
					      unsigned options) "public synchronized";
PJ_DECL(pj_status_t) pjmedia_snd_port_set_ec( pjmedia_snd_port *snd_port,
					      pj_pool_t *pool,
					      unsigned tail_ms,
					      unsigned options);
%javamethodmodifiers pjmedia_snd_port_get_ec_tail(pjmedia_snd_port *snd_port,
						  unsigned *p_length) "public synchronized";
PJ_DECL(pj_status_t) pjmedia_snd_port_get_ec_tail(pjmedia_snd_port *snd_port,
						  unsigned *p_length);
%javamethodmodifiers pjmedia_snd_port_connect(pjmedia_snd_port *snd_port,
					      pjmedia_port *port) "public synchronized";
PJ_DECL(pj_status_t) pjmedia_snd_port_connect(pjmedia_snd_port *snd_port,
					      pjmedia_port *port);
%javamethodmodifiers pjmedia_snd_port_get_port(pjmedia_snd_port *snd_port) "public synchronized";
PJ_DECL(pjmedia_port*) pjmedia_snd_port_get_port(pjmedia_snd_port *snd_port);
%javamethodmodifiers pjmedia_snd_port_disconnect(pjmedia_snd_port *snd_port) "public synchronized";
PJ_DECL(pj_status_t) pjmedia_snd_port_disconnect(pjmedia_snd_port *snd_port);
struct pjmedia_tone_desc
{
    short   freq1;	    
    short   freq2;	    
    short   on_msec;	    
    short   off_msec;	    
    short   volume;	    
    short   flags;	    
};
struct pjmedia_tone_digit
{
    char    digit;	    
    short   on_msec;	    
    short   off_msec;	    
    short   volume;	    
};
struct pjmedia_tone_digit_map
{
    unsigned count;	    
    struct {
	char    digit;	    
	short   freq1;	    
	short   freq2;	    
    } digits[16];	    
};
enum
{
    
    PJMEDIA_TONEGEN_LOOP    = 1,
    
    PJMEDIA_TONEGEN_NO_LOCK = 2
};
%javamethodmodifiers pjmedia_tonegen_create(pj_pool_t *pool,
					    unsigned clock_rate,
					    unsigned channel_count,
					    unsigned samples_per_frame,
					    unsigned bits_per_sample,
					    unsigned options,
					    pjmedia_port **p_port) "public synchronized";
PJ_DECL(pj_status_t) pjmedia_tonegen_create(pj_pool_t *pool,
					    unsigned clock_rate,
					    unsigned channel_count,
					    unsigned samples_per_frame,
					    unsigned bits_per_sample,
					    unsigned options,
					    pjmedia_port **p_port);
%javamethodmodifiers pjmedia_tonegen_create2(pj_pool_t *pool,
					     const pj_str_t *name,
					     unsigned clock_rate,
					     unsigned channel_count,
					     unsigned samples_per_frame,
					     unsigned bits_per_sample,
					     unsigned options,
					     pjmedia_port **p_port) "public synchronized";
PJ_DECL(pj_status_t) pjmedia_tonegen_create2(pj_pool_t *pool,
					     const pj_str_t *name,
					     unsigned clock_rate,
					     unsigned channel_count,
					     unsigned samples_per_frame,
					     unsigned bits_per_sample,
					     unsigned options,
					     pjmedia_port **p_port);
%javamethodmodifiers pjmedia_tonegen_is_busy(pjmedia_port *tonegen) "public synchronized";
PJ_DECL(pj_bool_t) pjmedia_tonegen_is_busy(pjmedia_port *tonegen);
%javamethodmodifiers pjmedia_tonegen_stop(pjmedia_port *tonegen) "public synchronized";
PJ_DECL(pj_status_t) pjmedia_tonegen_stop(pjmedia_port *tonegen);
%javamethodmodifiers pjmedia_tonegen_rewind(pjmedia_port *tonegen) "public synchronized";
PJ_DECL(pj_status_t) pjmedia_tonegen_rewind(pjmedia_port *tonegen);
%javamethodmodifiers pjmedia_tonegen_play(pjmedia_port *tonegen,
					  unsigned count,
					  const pjmedia_tone_desc tones[],
					  unsigned options) "public synchronized";
PJ_DECL(pj_status_t) pjmedia_tonegen_play(pjmedia_port *tonegen,
					  unsigned count,
					  const pjmedia_tone_desc tones[],
					  unsigned options);
%javamethodmodifiers pjmedia_tonegen_play_digits(pjmedia_port *tonegen,
						 unsigned count,
						 const pjmedia_tone_digit digits[],
						 unsigned options) "public synchronized";
PJ_DECL(pj_status_t) pjmedia_tonegen_play_digits(pjmedia_port *tonegen,
						 unsigned count,
						 const pjmedia_tone_digit digits[],
						 unsigned options);
%javamethodmodifiers pjmedia_tonegen_get_digit_map(pjmedia_port *tonegen,
						   const pjmedia_tone_digit_map **m) "public synchronized";
PJ_DECL(pj_status_t) pjmedia_tonegen_get_digit_map(pjmedia_port *tonegen,
						   const pjmedia_tone_digit_map **m);
%javamethodmodifiers pjmedia_tonegen_set_digit_map(pjmedia_port *tonegen,
						   pjmedia_tone_digit_map *m) "public synchronized";
PJ_DECL(pj_status_t) pjmedia_tonegen_set_digit_map(pjmedia_port *tonegen,
						   pjmedia_tone_digit_map *m);
