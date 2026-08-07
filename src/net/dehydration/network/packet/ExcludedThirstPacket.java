package net.dehydration.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ExcludedThirstPacket(int playerId, boolean excludingThirst) implements CustomPayload {

    public static final CustomPayload.Id<ExcludedThirstPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("dehydration", "excluded_thirst_packet"));

    public static final PacketCodec<RegistryByteBuf, ExcludedThirstPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeInt(value.playerId);
        buf.writeBoolean(value.excludingThirst);
    }, buf -> new ExcludedThirstPacket(buf.readInt(), buf.readBoolean()));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
