/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.karaf.minho.jmx;

import lombok.Getter;
import lombok.Setter;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.management.remote.rmi.RMIJRMPServerImpl;
import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.*;
import java.nio.channels.ServerSocketChannel;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.rmi.server.UnicastRemoteObject;
import java.util.Enumeration;
import java.util.Map;

@Getter
@Setter
public class ConnectorServerFactory {

    private MBeanServer server;
    private String serviceUrl;
    private Registry registry;
    private String rmiServerHost;
    private boolean locate;
    private boolean create = true;
    private boolean locallyCreated;
    private int port = Registry.REGISTRY_PORT;
    private String host;
    private Remote remoteServerStub;
    private JMXConnectorServer rmiConnectorServer;
    private Map<String, Object> environment;
    private RMIJRMPServerImpl rmiServer;
    private ObjectName objectName;
    private boolean threaded = false;
    private boolean daemon = false;

    public void init() throws Exception {
        JMXServiceURL url = new JMXServiceURL(this.serviceUrl);

        if (registry == null && locate) {
            try {
                Registry reg = LocateRegistry.getRegistry(host, port);
                reg.list();
                registry = reg;
            } catch (Exception e) {
                // ignore
            }
        }
        if (registry == null && create) {
            registry = new JmxRegistry(port, getBindingName(url));
            locallyCreated = true;
        }

        // TODO integrate the registry in the ServiceRegistry ?

        if (this.server == null) {
            throw new IllegalArgumentException("server must be set");
        }

        // TODO add SSL and secure

        setupRMIServerSocketFactory();

        rmiServer = new RMIJRMPServerImpl(url.getPort(),
                (RMIClientSocketFactory)environment.get(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE),
                (RMIServerSocketFactory)environment.get(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE), environment);

        // create the connector server now
        this.rmiConnectorServer = new RMIConnectorServer(url, environment, rmiServer, server);

        if (this.objectName != null) {
            this.server.registerMBean(this.rmiConnectorServer, this.objectName);
        }

        try {
            if (this.threaded) {
                Thread connectorThread = new Thread(() -> {
                    try {
                        Thread.currentThread().setContextClassLoader(ConnectorServerFactory.class.getClassLoader());
                        rmiConnectorServer.start();
                        remoteServerStub = rmiServer.toStub();
                    } catch (IOException ioe) {
                        if (ioe.getCause() instanceof BindException) {
                            // we want just the port message
                            int endIndex = ioe.getMessage().indexOf("nested exception is");
                            // check to make sure we do not get an index out of range
                            if (endIndex > ioe.getMessage().length() || endIndex < 0) {
                                endIndex = ioe.getMessage().length();
                            }
                            throw new RuntimeException("\n" + ioe.getMessage().substring(0, endIndex) +
                                    "\nYou may have started two containers. If you need to start a second container or the default ports are already in use " +
                                    "update the properties in the config service");
                        }
                        throw new RuntimeException("Could not start JMX connector server", ioe);
                    }
                });
                connectorThread.setName("JMX Connector Thread [" + this.serviceUrl + "]");
                connectorThread.setDaemon(this.daemon);
                connectorThread.start();
            } else {
                this.rmiConnectorServer.start();
                remoteServerStub = rmiServer.toStub();
            }
        } catch (Exception e) {
            if (this.objectName != null) {
                doUnregister(this.objectName);
            }
            throw e;
        }
    }

    public void destroy() throws Exception {
        try {
            if (this.rmiConnectorServer != null) {
                this.rmiConnectorServer.stop();
            }
            if (registry != null && locallyCreated) {
                Registry reg = registry;
                registry = null;
                UnicastRemoteObject.unexportObject(reg, true);
            }
        } finally {
            if (this.objectName != null) {
                doUnregister(this.objectName);
            }
        }
    }

    protected void doUnregister(ObjectName objectName) {
        try {
            if (this.objectName != null && this.server.isRegistered(objectName)) {
                this.server.unregisterMBean(objectName);
            }
        } catch (JMException e) {
            // ignore
        }
    }

    protected static String getBindingName(final JMXServiceURL jmxServiceURL) {
        final String urlPath = jmxServiceURL.getURLPath();
        try {
            if (urlPath.startsWith("/jndi/")) {
                return new URI(urlPath.substring(6)).getPath().replaceAll("^/+", "").replaceAll("/+$", "");
            }
        } catch (URISyntaxException e) {
            // ignore
        }
        return "jmxrmi"; // use the default
    }

    private void setupRMIServerSocketFactory() {
        RMIServerSocketFactory rssf = new MinhoRMIServerSocketFactory(rmiServerHost);
        environment.put(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE, rssf);
    }

    @SuppressWarnings("restriction")
    private class JmxRegistry extends sun.rmi.registry.RegistryImpl {
        private final String lookupName;

        JmxRegistry(final int port, final String lookupName) throws RemoteException {
            super(port, null, new MinhoRMIServerSocketFactory(getHost()));
            this.lookupName = lookupName;
        }

        @Override
        public Remote lookup(String s) throws RemoteException, NotBoundException {
            return lookupName.equals(s) ? remoteServerStub : null;
        }

        @Override
        public void bind(String s, Remote remote) throws RemoteException, AlreadyBoundException, AccessException {
        }

        @Override
        public void unbind(String s) throws RemoteException, NotBoundException, AccessException {
        }

        @Override
        public void rebind(String s, Remote remote) throws RemoteException, AccessException {
        }

        @Override
        public String[] list() throws RemoteException {
            return new String[] {lookupName};
        }
    }

    private static class MinhoRMIServerSocketFactory implements RMIServerSocketFactory {
        private String rmiServerHost;

        public MinhoRMIServerSocketFactory(String rmiServerHost) {
            this.rmiServerHost = rmiServerHost;
        }

        public ServerSocket createServerSocket(int port) throws IOException {
            InetAddress host = InetAddress.getByName(rmiServerHost);
            if (host.isLoopbackAddress()) {
                final ServerSocket ss = ServerSocketFactory.getDefault().createServerSocket(port, 50, host);
                return new LocalOnlyServerSocket(ss);
            } else {
                final ServerSocket ss = ServerSocketFactory.getDefault().createServerSocket(port, 50, InetAddress.getByName(rmiServerHost));
                return ss;
            }
        }
    }

    private static class LocalOnlyServerSocket extends ServerSocket {

        private final ServerSocket ss;

        public LocalOnlyServerSocket(ServerSocket ss) throws IOException {
            this.ss = ss;
        }

        @Override
        public void bind(SocketAddress endpoint) throws IOException {
            ss.bind(endpoint);
        }

        @Override
        public void bind(SocketAddress endpoint, int backlog) throws IOException {
            ss.bind(endpoint, backlog);
        }

        @Override
        public InetAddress getInetAddress() {
            return ss.getInetAddress();
        }

        @Override
        public int getLocalPort() {
            return ss.getLocalPort();
        }

        @Override
        public SocketAddress getLocalSocketAddress() {
            return ss.getLocalSocketAddress();
        }

        @Override
        public Socket accept() throws IOException {
            return checkLocal(ss.accept());
        }

        @Override
        public void close() throws IOException {
            ss.close();
        }

        @Override
        public ServerSocketChannel getChannel() {
            return ss.getChannel();
        }

        @Override
        public boolean isBound() {
            return ss.isBound();
        }

        @Override
        public boolean isClosed() {
            return ss.isClosed();
        }

        @Override
        public void setSoTimeout(int timeout) throws SocketException {
            ss.setSoTimeout(timeout);
        }

        @Override
        public int getSoTimeout() throws IOException {
            return ss.getSoTimeout();
        }

        @Override
        public void setReuseAddress(boolean on) throws SocketException {
            ss.setReuseAddress(on);
        }

        @Override
        public boolean getReuseAddress() throws SocketException {
            return ss.getReuseAddress();
        }

        @Override
        public String toString() {
            return ss.toString();
        }

        @Override
        public void setReceiveBufferSize(int size) throws SocketException {
            ss.setReceiveBufferSize(size);
        }

        @Override
        public int getReceiveBufferSize() throws SocketException {
            return ss.getReceiveBufferSize();
        }

        @Override
        public void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
            ss.setPerformancePreferences(connectionTime, latency, bandwidth);
        }
    }

    private static Socket checkLocal(Socket socket) throws IOException {
        InetAddress addr = socket.getInetAddress();
        if (addr != null) {
            if (addr.isLoopbackAddress()) {
                return socket;
            } else {
                try {
                    Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
                    while (nis.hasMoreElements()) {
                        NetworkInterface ni = nis.nextElement();
                        Enumeration<InetAddress> ads = ni.getInetAddresses();
                        while (ads.hasMoreElements()) {
                            InetAddress ad = ads.nextElement();
                            if (ad.equals(addr)) {
                                return socket;
                            }
                        }
                    }
                } catch (SocketException e) {
                    // Ignore
                }
            }
        }
        try {
            socket.close();
        } catch (Exception e) {
            // Ignore
        }
        throw new IOException("Only connections from clients running on the host where the RMI remote objects have been exported are accepted.");
    }

}
