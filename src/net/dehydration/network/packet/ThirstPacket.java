package net.dehydration.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ThirstPacket(int playerId, int thirstLevel) implements CustomPayload {

    public static final CustomPayload.Id<ThirstPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("dehydration", "thirst_packet"));

    public static final PacketCodec<RegistryByteBuf, ThirstPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeInt(value.playerId);
        buf.writeInt(value.thirstLevel);
    }, buf -> new ThirstPacket(buf.readInt(), buf.readInt()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
