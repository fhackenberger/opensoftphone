package org.acoveo.tools;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;

public class NetworkTools {
	/** Return the first non loopback, non wildcard Ipv4 address we can find
	 * 
	 * @return A non loopback, non wildcard Ipv4 address or localhost if none could be found 
	 * @throws SocketException
	 * @throws UnknownHostException
	 */
	public static InetAddress getDefaultBindAddress() throws SocketException, UnknownHostException {
		for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
			NetworkInterface iface = ifaces.nextElement();
			for(InterfaceAddress address : iface.getInterfaceAddresses()) {
				InetAddress inetAddress = address.getAddress();
				// We want a Ipv4, non loopback, non wildcard address
				if(Inet4Address.class.isAssignableFrom(inetAddress.getClass()) && 
						!inetAddress.isLoopbackAddress() && !inetAddress.isAnyLocalAddress()) {
					return inetAddress;
				}
			}
		}
		return InetAddress.getLocalHost();
	}
	
	/** Returns a list of all non loopback, non wildcard Ipv4 addresses on the host
	 * 
	 * @return A list of non loopback, non wildcard Ipv4 addresses
	 * @throws SocketException
	 */
	public static Collection<InetAddress> getAllNonLocalNetworkAddresses() throws SocketException {
		LinkedList<InetAddress> addresses = new LinkedList<InetAddress>();
		for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
			NetworkInterface iface = ifaces.nextElement();
			for(InterfaceAddress address : iface.getInterfaceAddresses()) {
				InetAddress inetAddress = address.getAddress();
				// We want a Ipv4, non loopback, non wildcard address
				if(Inet4Address.class.isAssignableFrom(inetAddress.getClass()) && 
						!inetAddress.isLoopbackAddress() && !inetAddress.isAnyLocalAddress()) {
					addresses.add(inetAddress);
				}
			}
		}
		return addresses;
	}
	
	/**
	 * Rely on www.google.com:80 to find an unused port
	 * 
	 * @return A free port number on localhost
	 * @throws IOException
	 */
	public static int findFreePort() throws IOException {
		Socket newSocket = new Socket("www.google.com", 80);
		return newSocket.getLocalPort();
	}
}
