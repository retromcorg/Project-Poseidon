package net.minecraft.server;

import com.legacyminecraft.poseidon.PoseidonConfig;
import com.legacyminecraft.poseidon.level.ChunkCompressionType;
import net.jpountz.lz4.LZ4BlockInputStream;
import net.jpountz.lz4.LZ4BlockOutputStream;

import java.io.*;
import java.util.ArrayList;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

public class RegionFile {

    private static final byte[] a = new byte[4096];
    private final File b;
    private RandomAccessFile c;
    private final int[] d = new int[1024];
    private final int[] e = new int[1024];
    private ArrayList f;
    private int g;
    private long h = 0L;
    // Poseidon - Server chunk compression type.
    private static ChunkCompressionType chunkCompressionType;

    public RegionFile(File file1) {
        if(chunkCompressionType == null) {
            String typeString = PoseidonConfig.getInstance().getConfigString("world.settings.compression.level");
            chunkCompressionType = ChunkCompressionType.fromName(typeString);
        }

        this.b = file1;
        this.b("REGION LOAD " + this.b);
        this.g = 0;

        try {
            if (file1.exists()) {
                this.h = file1.lastModified();
            }

            this.c = new RandomAccessFile(file1, "rw");
            int i;

            if (this.c.length() < 4096L) {
                for (i = 0; i < 1024; ++i) {
                    this.c.writeInt(0);
                }

                for (i = 0; i < 1024; ++i) {
                    this.c.writeInt(0);
                }

                this.g += 8192;
            }

            if ((this.c.length() & 4095L) != 0L) {
                for (i = 0; (long) i < (this.c.length() & 4095L); ++i) {
                    this.c.write(0);
                }
            }

            i = (int) this.c.length() / 4096;
            this.f = new ArrayList(i);

            int j;

            for (j = 0; j < i; ++j) {
                this.f.add(Boolean.valueOf(true));
            }

            this.f.set(0, Boolean.valueOf(false));
            this.f.set(1, Boolean.valueOf(false));
            this.c.seek(0L);

            int k;

            for (j = 0; j < 1024; ++j) {
                k = this.c.readInt();
                this.d[j] = k;
                if (k != 0 && (k >> 8) + (k & 255) <= this.f.size()) {
                    for (int l = 0; l < (k & 255); ++l) {
                        this.f.set((k >> 8) + l, Boolean.valueOf(false));
                    }
                }
            }

            for (j = 0; j < 1024; ++j) {
                k = this.c.readInt();
                this.e[j] = k;
            }
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
        }
    }

    public synchronized int a() {
        int i = this.g;

        this.g = 0;
        return i;
    }

    private void a(String s) {
        // Poseidon start - Allow printing region debug information. Useful for developers (and nerds).
        if(PoseidonConfig.getInstance().getConfigBoolean("developer.region-transaction-logs.enable", false)) {
            MinecraftServer.log.info(s);
        }
        // Poseidon end
    }

    private void b(String s) {
        this.a(s + "\n");
    }

    private void a(String s, int i, int j, String s1) {
        this.a("REGION " + s + " " + this.b.getName() + "[" + i + "," + j + "] = " + s1);
    }

    private void a(String s, int i, int j, int k, String s1) {
        this.a("REGION " + s + " " + this.b.getName() + "[" + i + "," + j + "] " + k + "B = " + s1);
    }

    private void b(String s, int i, int j, String s1) {
        this.a(s, i, j, s1 + "\n");
    }

    public synchronized DataInputStream a(int i, int j) {
        if (this.d(i, j)) {
            this.b("READ", i, j, "out of bounds");
            return null;
        } else {
            try {
                int k = this.e(i, j);

                if (k == 0) {
                    return null;
                } else {
                    int l = k >> 8;
                    int i1 = k & 255;

                    if (l + i1 > this.f.size()) {
                        this.b("READ", i, j, "invalid sector");
                        return null;
                    } else {
                        this.c.seek((long) (l * 4096));
                        int j1 = this.c.readInt();

                        if (j1 > 4096 * i1) {
                            this.b("READ", i, j, "invalid length: " + j1 + " > 4096 * " + i1);
                            return null;
                        } else {
                            // Poseidon start - More compression formats
                            byte b0 = this.c.readByte();
                            byte[] abyte = new byte[j1 - 1];
                            this.c.read(abyte);

                            DataInputStream datainputstream;
                            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(abyte);

                            if (b0 == ChunkCompressionType.NONE.getId()) {
                                datainputstream = new DataInputStream(byteArrayInputStream);
                            } else if (b0 == ChunkCompressionType.GZIP.getId()) {
                                datainputstream = new DataInputStream(new GZIPInputStream(byteArrayInputStream));
                            } else if (b0 == ChunkCompressionType.DEFLATE.getId()) {
                                datainputstream = new DataInputStream(new InflaterInputStream(byteArrayInputStream));
                            } else if (b0 == ChunkCompressionType.LZ4.getId()) {
                                datainputstream = new DataInputStream(new LZ4BlockInputStream(byteArrayInputStream));
                            } else {
                                this.b("READ", i, j, "unknown version " + b0);
                                return null;
                            }

                            return datainputstream;
                            // Poseidon end
                        }
                    }
                }
            } catch (IOException ioexception) {
                this.b("READ", i, j, "exception");
                return null;
            }
        }
    }

    public DataOutputStream b(int i, int j) {
        // Poseidon start - Allow saving in more formats
        if(this.d(i, j)) { // Check if chunk is in bounds.
            return null;
        } else {
            ChunkBuffer chunkBuffer = new ChunkBuffer(this, i, j);

            switch(chunkCompressionType) {
                case NONE:
                    return new DataOutputStream(chunkBuffer);
                case GZIP:
                    try {
                        return new DataOutputStream(new GZIPOutputStream(chunkBuffer));
                    } catch (IOException ex) {
                        return null;
                    }
                case DEFLATE:
                    return new DataOutputStream(new DeflaterOutputStream(chunkBuffer));
                case LZ4:
                    return new DataOutputStream(new LZ4BlockOutputStream(chunkBuffer));
                default:
                    return null;
            }

        }
        // Poseidon end
    }

    protected synchronized void a(int i, int j, byte[] abyte, int k) {
        try {
            int l = this.e(i, j);
            int i1 = l >> 8;
            int j1 = l & 255;
            int k1 = (k + 5) / 4096 + 1;

            if (k1 >= 256) {
                return;
            }

            if (i1 != 0 && j1 == k1) {
                this.a("SAVE", i, j, k, "rewrite");
                this.a(i1, abyte, k);
            } else {
                int l1;

                for (l1 = 0; l1 < j1; ++l1) {
                    this.f.set(i1 + l1, Boolean.valueOf(true));
                }

                l1 = this.f.indexOf(Boolean.valueOf(true));
                int i2 = 0;
                int j2;

                if (l1 != -1) {
                    for (j2 = l1; j2 < this.f.size(); ++j2) {
                        if (i2 != 0) {
                            if (((Boolean) this.f.get(j2)).booleanValue()) {
                                ++i2;
                            } else {
                                i2 = 0;
                            }
                        } else if (((Boolean) this.f.get(j2)).booleanValue()) {
                            l1 = j2;
                            i2 = 1;
                        }

                        if (i2 >= k1) {
                            break;
                        }
                    }
                }

                if (i2 >= k1) {
                    this.a("SAVE", i, j, k, "reuse");
                    i1 = l1;
                    this.a(i, j, l1 << 8 | k1);

                    for (j2 = 0; j2 < k1; ++j2) {
                        this.f.set(i1 + j2, Boolean.valueOf(false));
                    }

                    this.a(i1, abyte, k);
                } else {
                    this.a("SAVE", i, j, k, "grow");
                    this.c.seek(this.c.length());
                    i1 = this.f.size();

                    for (j2 = 0; j2 < k1; ++j2) {
                        this.c.write(a);
                        this.f.add(Boolean.valueOf(false));
                    }

                    this.g += 4096 * k1;
                    this.a(i1, abyte, k);
                    this.a(i, j, i1 << 8 | k1);
                }
            }

            this.b(i, j, (int) (System.currentTimeMillis() / 1000L));
        } catch (IOException ioexception) {
            ioexception.printStackTrace();
        }
    }

    private void a(int i, byte[] abyte, int j) throws IOException {
        this.b(" " + i);
        this.c.seek((long) (i * 4096));
        this.c.writeInt(j + 1);
        // Poseidon start - Write correct compression format to file.
        this.c.writeByte(chunkCompressionType.getId());
        // Poseidon end
        this.c.write(abyte, 0, j);
    }

    private boolean d(int i, int j) {
        return i < 0 || i >= 32 || j < 0 || j >= 32;
    }

    private int e(int i, int j) {
        return this.d[i + j * 32];
    }

    public boolean c(int i, int j) {
        return this.e(i, j) != 0;
    }

    private void a(int i, int j, int k) throws IOException {
        this.d[i + j * 32] = k;
        this.c.seek((long) ((i + j * 32) * 4));
        this.c.writeInt(k);
    }

    private void b(int i, int j, int k) throws IOException {
        this.e[i + j * 32] = k;
        this.c.seek((long) (4096 + (i + j * 32) * 4));
        this.c.writeInt(k);
    }

    public void b() throws IOException {
        this.c.close();
    }
}
