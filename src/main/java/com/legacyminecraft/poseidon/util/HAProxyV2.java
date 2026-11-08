package com.legacyminecraft.poseidon.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import com.legacyminecraft.poseidon.PoseidonConfig;

public final class HAProxyV2 {
  private static final byte[] SIG = new byte[] { 0x0D, 0x0A, 0x0D, 0x0A, 0x00, 0x0D, 0x0A, 0x51, 0x55, 0x49, 0x54, 0x0A };

  public static final InetSocketAddress getRemoteAddress(final InputStream in) throws IOException {
    final byte[] header = new byte[16];
    readFully(in, header);
    for (int i = 0; i < 12; i++) {
      if (header[i] != SIG[i]) {
        throw new IOException("Missing Signature");
      }
    }
    final int version = ((header[12] & 0xFF) >> 4) & 0x0F;
    if (version == 2) {
      final int f = header[13];
      final int length = u16(header, 14);
      final byte[] payload = new byte[length];
      readFully(in, payload);
      final int family = (f >> 4) & 0x0F;
      final int transport = f & 0x0F;
      if (transport == 1) {
        if (family == 1) {
          if (length >= 12) {
            final byte[] addr = new byte[4];
            System.arraycopy(payload, 0, addr, 0, 4);
            return new InetSocketAddress(InetAddress.getByAddress(addr), u16(payload, 8));
          } else {
            throw new IOException("INET4 length " + length + " is too short");
          }
        } else if (family == 2) {
          if (length >= 36) {
            final byte[] addr = new byte[4];
            System.arraycopy(payload, 0, addr, 0, 4);
            return new InetSocketAddress(InetAddress.getByAddress(addr), u16(payload, 32));
          } else {
            throw new IOException("INET6 length " + length + " is too short");
          }
        } else {
          throw new IOException("Address type " + family + " is not supported");
        }
      } else {
        throw new IOException("Transport " + transport + " is not supported");
      }
    } else {
      throw new IOException("PROXY version " + version + " is not supported");
    }
  }

  private static final void readFully(final InputStream in, final byte[] b) throws IOException {
    int o = 0;
    while (o < b.length) {
      final int r = in.read(b, o, b.length - o);
      if (r >= 0) {
        o += r;
      } else {
        throw new EOFException();
      }
    }
  }

  private static final int u16(final byte[] b, final int o) {
    return ((b[o] & 0xFF) << 8) | (b[o + 1] & 0xFF);
  }

  public static final boolean isProxyV2Enabled() {
    return PoseidonConfig.getInstance().getBoolean("settings.haproxy-protocol", false);
  }
}
