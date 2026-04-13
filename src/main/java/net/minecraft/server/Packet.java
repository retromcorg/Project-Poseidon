package net.minecraft.server;

import com.legacyminecraft.poseidon.PoseidonConfig;
import com.legacyminecraft.poseidon.packets.ArtificialPacket53BlockChange;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class Packet {

    private static Map packetIdToClassMap = new HashMap();
    private static Map packetClassToIdMap = new HashMap();
    private static Set clientPacketIdList = new HashSet();
    private static Set serverPacketIdList = new HashSet();
    public final long timestamp = System.currentTimeMillis();
    public boolean k = false;
    private static HashMap e;
    private static int f;

    public Packet() {}
    
    /**
     * Register a packet
     * @author moderator_Man
     * @param id
     * @param clientSide
     * @param serverSide
     * @param oclass
     */
    public static void registerPacket(int id, boolean clientSide, boolean serverSide, Class oclass)
    {
        if (packetIdToClassMap.containsKey(Integer.valueOf(id)))
            throw new IllegalArgumentException("Duplicate packet id:" + id);
        else if (packetClassToIdMap.containsKey(oclass))
            throw new IllegalArgumentException("Duplicate packet class:" + oclass);
        else {
            packetIdToClassMap.put(Integer.valueOf(id), oclass);
            packetClassToIdMap.put(oclass, Integer.valueOf(id));
            if (clientSide)
                clientPacketIdList.add(Integer.valueOf(id));
            if (serverSide)
                serverPacketIdList.add(Integer.valueOf(id));
        }
    }
    
    static void a(int i, boolean flag, boolean flag1, Class oclass) {
        if (packetIdToClassMap.containsKey(Integer.valueOf(i))) {
            throw new IllegalArgumentException("Duplicate packet id:" + i);
        } else if (packetClassToIdMap.containsKey(oclass)) {
            throw new IllegalArgumentException("Duplicate packet class:" + oclass);
        } else {
            packetIdToClassMap.put(Integer.valueOf(i), oclass);
            packetClassToIdMap.put(oclass, Integer.valueOf(i));
            if (flag) {
                clientPacketIdList.add(Integer.valueOf(i));
            }

            if (flag1) {
                serverPacketIdList.add(Integer.valueOf(i));
            }
        }
    }

    public static Packet a(int i) {
        try {
            Class oclass = (Class) packetIdToClassMap.get(Integer.valueOf(i));

            return oclass == null ? null : (Packet) oclass.newInstance();
        } catch (Exception exception) {
            exception.printStackTrace();
            System.out.println("Skipping packet with id " + i);
            return null;
        }
    }

    public final int b() {
        return ((Integer) packetClassToIdMap.get(this.getClass())).intValue();
    }

    // CraftBukkit - throws IOException
    public static Packet a(DataInputStream datainputstream, boolean flag) throws IOException {
        boolean flag1 = false;
        Packet packet = null;

        int i;

        try {
            i = datainputstream.read();
            if (i == -1) {
                return null;
            }

            if (flag && !serverPacketIdList.contains(Integer.valueOf(i)) || !flag && !clientPacketIdList.contains(Integer.valueOf(i))) {
                System.out.println("Bad packet id: " + i); //Project Poseidon
                return null; //Project Poseidon
                //throw new IOException("Bad packet id " + i); //Project Poseidon - Comment Out
            }

            packet = a(i);
            if (packet == null) {
                throw new IOException("Bad packet id " + i);
            }

            packet.a(datainputstream);
        } catch (EOFException eofexception) {
            System.out.println("Reached end of stream");
            return null;
        }

        // CraftBukkit start
        catch (java.net.SocketTimeoutException exception) {
            System.out.println("Read timed out");
            return null;
        } catch (java.net.SocketException exception) {
            if (!(boolean) PoseidonConfig.getInstance().getConfigOption("settings.remove-join-leave-debug", true)) {
                System.out.println("Connection reset");
            }
            return null;
        }
        // CraftBukkit end

        PacketCounter packetcounter = (PacketCounter) e.get(Integer.valueOf(i));

        if (packetcounter == null) {
            packetcounter = new PacketCounter((EmptyClass1) null);
            e.put(Integer.valueOf(i), packetcounter);
        }

        packetcounter.a(packet.a());
        ++f;
        if (f % 1000 == 0) {
            ;
        }

        return packet;
    }

    // CraftBukkit - throws IOException
    public static void a(Packet packet, DataOutputStream dataoutputstream) throws IOException {
        dataoutputstream.write(packet.b());
        packet.a(dataoutputstream);
    }

    // CraftBukkit - throws IOException
    public static void a(String s, DataOutputStream dataoutputstream)  throws IOException {
        if (s.length() > 32767) {
            throw new IOException("String too big");
        } else {
            dataoutputstream.writeShort(s.length());
            dataoutputstream.writeChars(s);
        }
    }

    // CraftBukkit - throws IOException
    public static String a(DataInputStream datainputstream, int i)  throws IOException {
        short short1 = datainputstream.readShort();

        if (short1 > i) {
            throw new IOException("Received string length longer than maximum allowed (" + short1 + " > " + i + ")");
        } else if (short1 < 0) {
            throw new IOException("Received string length is less than zero! Weird string!");
        } else {
            StringBuilder stringbuilder = new StringBuilder();

            for (int j = 0; j < short1; ++j) {
                stringbuilder.append(datainputstream.readChar());
            }

            return stringbuilder.toString();
        }
    }

    public abstract void a(DataInputStream datainputstream) throws IOException; // CraftBukkit

    public abstract void a(DataOutputStream dataoutputstream) throws IOException; // CraftBukkit

    public abstract void a(NetHandler nethandler);

    public abstract int a();

    static {
        a(0, true, true, Packet0KeepAlive.class);
        a(1, true, true, Packet1Login.class);
        a(2, true, true, Packet2Handshake.class);
        a(3, true, true, Packet3Chat.class);
        a(4, true, false, Packet4UpdateTime.class);
        a(5, true, false, Packet5EntityEquipment.class);
        a(6, true, false, Packet6SpawnPosition.class);
        a(7, false, true, Packet7UseEntity.class);
        a(8, true, false, Packet8UpdateHealth.class);
        a(9, true, true, Packet9Respawn.class);
        a(10, true, true, Packet10Flying.class);
        a(11, true, true, Packet11PlayerPosition.class);
        a(12, true, true, Packet12PlayerLook.class);
        a(13, true, true, Packet13PlayerLookMove.class);
        a(14, false, true, Packet14BlockDig.class);
        a(15, false, true, Packet15Place.class);
        a(16, false, true, Packet16BlockItemSwitch.class);
        a(17, true, false, Packet17.class);
        a(18, true, true, Packet18ArmAnimation.class);
        a(19, false, true, Packet19EntityAction.class);
        a(20, true, false, Packet20NamedEntitySpawn.class);
        a(21, true, false, Packet21PickupSpawn.class);
        a(22, true, false, Packet22Collect.class);
        a(23, true, false, Packet23VehicleSpawn.class);
        a(24, true, false, Packet24MobSpawn.class);
        a(25, true, false, Packet25EntityPainting.class);
        a(27, false, false, Packet27.class); // CraftBukkit - true -> false; disabled unused packet. TODO -- check if needed
        a(28, true, false, Packet28EntityVelocity.class);
        a(29, true, false, Packet29DestroyEntity.class);
        a(30, true, false, Packet30Entity.class);
        a(31, true, false, Packet31RelEntityMove.class);
        a(32, true, false, Packet32EntityLook.class);
        a(33, true, false, Packet33RelEntityMoveLook.class);
        a(34, true, false, Packet34EntityTeleport.class);
        a(38, true, false, Packet38EntityStatus.class);
        a(39, true, false, Packet39AttachEntity.class);
        a(40, true, false, Packet40EntityMetadata.class);
        a(50, true, false, Packet50PreChunk.class);
        a(51, true, false, Packet51MapChunk.class);
        a(52, true, false, Packet52MultiBlockChange.class);
        a(53, true, false, Packet53BlockChange.class);
        a(54, true, false, Packet54PlayNoteBlock.class);
        a(60, true, false, Packet60Explosion.class);
        a(61, true, false, Packet61.class);
        a(70, true, false, Packet70Bed.class);
        a(71, true, false, Packet71Weather.class);
        a(100, true, false, Packet100OpenWindow.class);
        a(101, true, true, Packet101CloseWindow.class);
        a(102, false, true, Packet102WindowClick.class);
        a(103, true, false, Packet103SetSlot.class);
        a(104, true, false, Packet104WindowItems.class);
        a(105, true, false, Packet105CraftProgressBar.class);
        a(106, true, true, Packet106Transaction.class);
        a(130, true, true, Packet130UpdateSign.class);
        a(131, true, false, Packet131.class);
        a(200, true, false, Packet200Statistic.class);
        a(254, false, true, Packet254GetInfo.class);
        a(255, true, true, Packet255KickDisconnect.class);
        packetClassToIdMap.put(ArtificialPacket53BlockChange.class, 53); //Poseidon - Artificial Block Change Packet
        e = new HashMap();
        f = 0;
    }
}
