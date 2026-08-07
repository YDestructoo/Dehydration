package net.dehydration.network.packet;

import java.util.List;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record HydrationTemplatePacket(List<Integer> templateList, List<Identifier> templateIdentifiers) implements CustomPayload {

    public static final CustomPayload.Id<HydrationTemplatePacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("dehydration", "hydration_template_packet"));

    public static final PacketCodec<RegistryByteBuf, HydrationTemplatePacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        buf.writeCollection(value.templateList, PacketByteBuf::writeInt);
        buf.writeCollection(value.templateIdentifiers, PacketByteBuf::writeIdentifier);
    }, buf -> new HydrationTemplatePacket(buf.readList(PacketByteBuf::readInt), buf.readList(PacketByteBuf::readIdentifier)));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
