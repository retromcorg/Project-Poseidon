// Poseidon - For use with Strultz's Beta 1.7.3 BungeeCord
// https://github.com/Strultz/BungeeCord-b1.7.3

package net.minecraft.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Packet249BungeePayload extends Packet {
    public int length;
    public byte[] data;
    
    public Packet249BungeePayload() {
    }

    public void a(DataInputStream dataInputStream1) throws IOException {
        this.length = dataInputStream1.readShort();
        if(this.length > 0 && this.length < 32767) {
            this.data = new byte[this.length];
            dataInputStream1.readFully(this.data);
        }

    }

    public void a(DataOutputStream dataOutputStream1) throws IOException {
        dataOutputStream1.writeShort((short)this.length);
        if(this.data != null) {
            dataOutputStream1.write(this.data);
        }

    }

    public void a(NetHandler netHandler1) {
        netHandler1.handleBungeePayload(this);
    }

    public int a() {
        return 2 + this.length;
    }
}
