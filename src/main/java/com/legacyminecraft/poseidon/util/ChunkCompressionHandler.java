package com.legacyminecraft.poseidon.util;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.Packet51MapChunk;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.Deflater;

public class ChunkCompressionHandler implements Runnable {

    private static final int CHUNK_SIZE = 16 * 128 * 16 * 5 / 2;
    private static final int REDUCED_DEFLATE_THRESHOLD = CHUNK_SIZE / 4;
    private static final int DEFLATE_LEVEL_CHUNKS = 6;
    private static final int DEFLATE_LEVEL_PARTS = 1;

    private final EntityPlayer player;
    private final LinkedBlockingQueue<Packet51MapChunk> packetQueue = new LinkedBlockingQueue<>();

    private final Deflater deflater = new Deflater();
    private byte[] deflateBuffer = new byte[CHUNK_SIZE + 100];

    public ChunkCompressionHandler(EntityPlayer player) {
        this.player = player;
        new Thread(this).start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                handlePacket(packetQueue.take());
            } catch (InterruptedException e) {
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void queuePacket(Packet51MapChunk packet) {
        packetQueue.offer(packet);
    }

    private void handlePacket(Packet51MapChunk packet) {
        if (packet.g == null) {
            int dataSize = packet.rawData.length;
            if (deflateBuffer.length < dataSize + 100) {
                deflateBuffer = new byte[dataSize + 100];
            }

            deflater.reset();
            deflater.setLevel(dataSize < REDUCED_DEFLATE_THRESHOLD ? DEFLATE_LEVEL_PARTS : DEFLATE_LEVEL_CHUNKS);
            deflater.setInput(packet.rawData);
            deflater.finish();
            int size = deflater.deflate(deflateBuffer);
            if (size == 0) {
                size = deflater.deflate(deflateBuffer);
            }

            packet.g = new byte[size];
            packet.h = size;
            System.arraycopy(deflateBuffer, 0, packet.g, 0, size);
        }

        player.netServerHandler.networkManager.queue(packet);
    }

    public int getQueueSize() {
        return packetQueue.size();
    }

}
