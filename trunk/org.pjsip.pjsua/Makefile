#
# For this makefile to work, you need pjsip 1.0 compiled for both linux and windows!
# And don't forget to apply the patches in pjsip_patches.
# Compile and install in linux using
# $ ./configure --prefix=/usr/local
# $ make dep
# $ make
# $ sudo make install
#
# In order to generate a debug build, prepend 'CFLAGS="-g"' to ./configure
#
# There is no uninstall target, but you can use the following script (does not
# delete all directories though):
# sudo rm /usr/local/lib/pkgconfig/libpj.pc
# for file in `find . -name '*.a'`; do filename=`basename $file`; linkedfile=`echo $filename | sed -e "s/-i686-pc-linux-gnu//"`; sudo rm /usr/local/lib/$filename /usr/local/lib/$linkedfile; done
# basedir=`pwd`; for dir in `find . -name include`; do echo "---$dir---"; cd $dir; for file in `find .`; do deletefile=`echo $file | sed 's/^\.\/\?//'`; sudo rm /usr/local/include/$deletefile; done; cd $basedir; done
# 
# For the windows build you should extract the pjsip library separately.
# Compiling for win32 is a matter of installing mingw, invoking:
# $ ./configure --host=i586-mingw32msvc
# (adapt the host string to the prefix of the mingw-gcc executable)
# and adapting the line
# export PJDIR := 
# in build.mak to point to the appropriate directory (where you untarred pjsip)
# You can then compile using
# $ make dep
# $ make
#
PJSUA_INCLUDE_PREFIX=/usr/local/include
PJSUA_HEADER=${PJSUA_INCLUDE_PREFIX}/pjsua-lib/pjsua.h
PJ_ERRNO_HEADER=${PJSUA_INCLUDE_PREFIX}/pj/errno.h
PJMEDIA_SOUND_PORT_HEADER=${PJSUA_INCLUDE_PREFIX}/pjmedia/sound_port.h
PJMEDIA_TONEGEN_HEADER=${PJSUA_INCLUDE_PREFIX}/pjmedia/tonegen.h
PJSIP_SIP_MSG_HEADER=${PJSUA_INCLUDE_PREFIX}/pjsip/sip_msg.h
LINUX_INCLUDE=-I /usr/lib/jvm/java-6-sun/include -I /usr/lib/jvm/java-6-sun/include/linux
LINUX_LIBDIR=
LINUX_LIBS=-lpjsua-i686-pc-linux-gnu -lpjsip-ua-i686-pc-linux-gnu -lpjsip-simple-i686-pc-linux-gnu -lpjsip-i686-pc-linux-gnu -lpjmedia-codec-i686-pc-linux-gnu -lpjmedia-i686-pc-linux-gnu -lpjnath-i686-pc-linux-gnu -lpjlib-util-i686-pc-linux-gnu -lpj-i686-pc-linux-gnu -lportaudio-i686-pc-linux-gnu -lgsmcodec-i686-pc-linux-gnu -lilbccodec-i686-pc-linux-gnu -lspeex-i686-pc-linux-gnu -lresample-i686-pc-linux-gnu -lmilenage-i686-pc-linux-gnu -lsrtp-i686-pc-linux-gnu -lm -lssl -luuid -lpthread -lasound
WIN_PJDIR=/manual_installs/pjproject-1.0_win/pjproject-1.0
WIN_CC=i586-mingw32msvc-gcc
WIN_CXX=i586-mingw32msvc-g++
WIN_CFLAGS=-D__MSVCRT__ -D_JNI_IMPLEMENTATION_
# The --kill-at flags disables name decoration (function names) to enable java to
# resolve JNI function names.
WIN_LDFLAGS=--kill-at
WIN_INCLUDE=-I /usr/i586-mingw32msvc/include -I /opt/java-6-sun-windows/include -I /opt/java-6-sun-windows/include/win32 -I ${WIN_PJDIR}/pjlib/include -I ${WIN_PJDIR}/pjlib-util/include -I ${WIN_PJDIR}/pjnath/include -I ${WIN_PJDIR}/pjmedia/include -I ${WIN_PJDIR}/pjsip/include
WIN_LIBDIR=-L /usr/i586-mingw32msvc/lib -L /opt/java-6-sun-windows/lib -L ${WIN_PJDIR}/pjlib/lib -L ${WIN_PJDIR}/pjlib-util/lib -L ${WIN_PJDIR}/pjnath/lib -L ${WIN_PJDIR}/pjmedia/lib -L ${WIN_PJDIR}/pjsip/lib -L ${WIN_PJDIR}/third_party/lib
WIN_LIBS=-lpjsua-i586-pc-mingw32msvc  -lpjsip-ua-i586-pc-mingw32msvc -lpjsip-simple-i586-pc-mingw32msvc -lpjsip-i586-pc-mingw32msvc -lpjmedia-codec-i586-pc-mingw32msvc -lpjmedia-i586-pc-mingw32msvc -lpjnath-i586-pc-mingw32msvc -lpjlib-util-i586-pc-mingw32msvc -lpj-i586-pc-mingw32msvc -lportaudio-i586-pc-mingw32msvc -lgsmcodec-i586-pc-mingw32msvc -lilbccodec-i586-pc-mingw32msvc -lspeex-i586-pc-mingw32msvc -lresample-i586-pc-mingw32msvc -lmilenage-i586-pc-mingw32msvc -lsrtp-i586-pc-mingw32msvc -lm -lwinmm -lwsock32 -lws2_32 -lole32
all : libpjsua_jni.so pjsua_jni.dll

libpjsua_jni.so : nativesrc/pjsua_wrap.cpp
	g++ -g -I . ${LINUX_INCLUDE} -c -o pjsua_wrap.o nativesrc/pjsua_wrap.cpp
	libtool --mode=link g++ ${LINUX_INCLUDE} -g -export-dynamic -shared -o libpjsua_jni.so ${LINUX_LIBS} pjsua_wrap.o

pjsua_jni.dll : nativesrc/pjsua_wrap.cpp
	${WIN_CXX} ${WIN_CFLAGS} -g -I . ${WIN_INCLUDE} -c -o pjsua_wrap_win.o nativesrc/pjsua_wrap.cpp
	${WIN_CXX} ${WIN_LIBDIR} -shared -o pjsua_jni.dll pjsua_wrap_win.o ${WIN_LIBS} -Wl,${WIN_LDFLAGS} 

nativesrc/pjsua_wrap.cpp : pjsua.i
	swig -Wall -o nativesrc/pjsua_wrap.cpp -outdir src/org/pjsip/pjsua -java -package org.pjsip.pjsua -c++ pjsua.i
	# Fix the JNICALL for win32 dll
	# see Re: Re: MinGW+MSYS+Swig+JNI-->DLL Question: msg#00417
	# http://osdir.com/ml/gnu.mingw.user/2002-10/msg00417.html
	sed -i 's/#include <jni.h>/#include <jni.h>\n\/\/ In jni_md.h: #define JNICALL __stdcall, which seems to mess us up\n\/\/ (adds some bit of name mangling). We want cdecl not stdcall... eh\?\n\/\/ This must come after jni.h\n#undef JNICALL\n#define JNICALL/' nativesrc/pjsua_wrap.cpp

pjsua.i : ${PJSUA_HEADER} pjsua.i_header
	cat ${PJSUA_HEADER} | ./strip_comments.pl | ./strip_unnecessary_stuff.pl | ./strip_unwanted_stuff.pl | ./strip_double_typedef_name.pl | ./delete_blank_lines.sh | ./add_swig_rename.pl | ./add_synchronised.pl > pjsua.i_body
	cat ${PJ_ERRNO_HEADER} | ./strip_comments.pl | ./strip_unnecessary_stuff.pl | ./strip_unwanted_stuff.pl | ./strip_double_typedef_name.pl | ./delete_blank_lines.sh | ./add_synchronised.pl >> pjsua.i_body
	cat ${PJMEDIA_SOUND_PORT_HEADER} | ./strip_comments.pl | ./strip_unnecessary_stuff.pl | ./strip_unwanted_stuff.pl | ./strip_double_typedef_name.pl | ./delete_blank_lines.sh | ./add_synchronised.pl >> pjsua.i_body
	cat ${PJMEDIA_TONEGEN_HEADER} | ./strip_comments.pl | ./strip_unnecessary_stuff.pl | ./strip_unwanted_stuff.pl | ./strip_double_typedef_name.pl | ./delete_blank_lines.sh | ./add_synchronised.pl >> pjsua.i_body
#	cat ${PJSIP_SIP_MSG_HEADER} | ./strip_comments.pl | ./strip_unnecessary_stuff.pl | ./strip_unwanted_stuff.pl | ./strip_double_typedef_name.pl | ./delete_blank_lines.sh | ./add_synchronised.pl >> pjsua.i_body
	cat pjsua.i_header pjsua.i_body > pjsua.i
	rm pjsua.i_body || true
	
clean :
	#-del nativesrc/net_sourceforge_iaxclient_jni_LibJiaxc.h libjiaxc.so jiaxc.o
	rm pjsua.i nativesrc/pjsua_wrap.cpp src/org/pjsip/pjsua/*.java pjsua_wrap.o pjsua_wrap_win.o libpjsua_jni.so pjsua_jni.dll || true
