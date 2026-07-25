package net.minecraft.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.HashMap;

import com.legacyminecraft.poseidon.util.HAProxyV2;

class NetworkAcceptThread extends Thread {

    final MinecraftServer a;

    final NetworkListenThread b;

    NetworkAcceptThread(NetworkListenThread networklistenthread, String s, MinecraftServer minecraftserver) {
        super(s);
        this.b = networklistenthread;
        this.a = minecraftserver;
    }

    public void run() {
        HashMap<InetAddress, Long> hashmap = new HashMap<>();

        while (this.b.b) {
            try {
                final Socket socket = NetworkListenThread.a(this.b).accept();

                if (socket != null) {
                    final SocketAddress socketaddress;
                    final InetAddress inetaddress;
                    if (HAProxyV2.isProxyV2Enabled()) {
                        final InetSocketAddress addr = HAProxyV2.getRemoteAddress(socket.getInputStream());
                        socketaddress = addr;
                        inetaddress = addr.getAddress();
                    } else {
                        socketaddress = socket.getRemoteSocketAddress();
                        inetaddress = socket.getInetAddress();
                    }

                    if (hashmap.containsKey(inetaddress) && !"127.0.0.1".equals(inetaddress.getHostAddress()) && System.currentTimeMillis() - ((Long) hashmap.get(inetaddress)).longValue() < 5000L) {
                        hashmap.put(inetaddress, Long.valueOf(System.currentTimeMillis()));
                        socket.close();
                    } else {
                        hashmap.put(inetaddress, Long.valueOf(System.currentTimeMillis()));
                        NetLoginHandler netloginhandler = new NetLoginHandler(this.a, socket, "Connection #" + NetworkListenThread.b(this.b));
                        netloginhandler.networkManager.setSocketAddress(socketaddress);

                        NetworkListenThread.a(this.b, netloginhandler);
                    }
                }
            } catch (IOException ioexception) {
                ioexception.printStackTrace();
            }
        }
    }
}
