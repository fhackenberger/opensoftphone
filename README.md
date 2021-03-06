# opensoftphone
This **SIP softphone** is written in **Java** as an eclipse RCP application. It uses the pjsip SIP stack for connecting to SIP servers. The phone runs on **Windows and Linux**. It would run on Mac OS too, but manually compiling it is necessary because of the JNI bindings to pjsip. The Java-JNI binding which are used by the phone are hosted on [sourceforge.net](http://sourceforge.net/projects/pjsip-jni), but are currently included in the SVN tree.

If you would like to obtain a commercial license, or need customisations of the phone for your environment, please contact us through our website http://www.acoveo.com.

In order to use the phone, just download the tarball/zip file for your platform, extract it wherever you would like to keep it and run the softphone/softphone.exe executable. It will then ask you to enter you SIP user name and password. As soon as the phone runs, click on the wrench button in the lower right corner, switch to the ''Account Settings'' preference page and put your asterisk server hostname into the ''Hostname'' field. Click on ''Ok'' and the softphone should show a yellow smiley next to your Agent ID.

The phone is tailored to be used in a **Callcenter environment** based on **asterisk**. It currently lacks a few features which a home user would expect. Most importantly these are SRTP and NAT traversal. Both features can be implemented quite easily, though.
