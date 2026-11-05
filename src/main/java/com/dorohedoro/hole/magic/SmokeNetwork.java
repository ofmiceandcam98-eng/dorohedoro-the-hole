package com.dorohedoro.hole.magic;

import com.dorohedoro.hole.Dorohedoro;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** Dedicated small network layer. Client-supplied skills and values are never trusted. */
public final class SmokeNetwork {
    private SmokeNetwork(){}
    public static void register(IEventBus bus){bus.addListener(SmokeNetwork::payloads);}
    public record Request(String action,String argument) implements CustomPacketPayload {
        public static final Type<Request> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID,"smoke_request"));
        public static final StreamCodec<ByteBuf,Request> CODEC=StreamCodec.composite(ByteBufCodecs.STRING_UTF8,Request::action,ByteBufCodecs.STRING_UTF8,Request::argument,Request::new);
        @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public record Snapshot(String data) implements CustomPacketPayload {
        public static final Type<Snapshot> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID,"smoke_snapshot"));
        public static final StreamCodec<ByteBuf,Snapshot> CODEC=StreamCodec.composite(ByteBufCodecs.STRING_UTF8,Snapshot::data,Snapshot::new);
        @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    private static void payloads(RegisterPayloadHandlersEvent event){
        PayloadRegistrar reg=event.registrar("smoke_1");
        reg.playToClient(Snapshot.TYPE,Snapshot.CODEC,(pkt,ctx)->{
            if(FMLEnvironment.dist==Dist.CLIENT)ctx.enqueueWork(()->com.dorohedoro.hole.client.magic.SmokeClient.receive(pkt.data()));
        });
        reg.playToServer(Request.TYPE,Request.CODEC,(pkt,ctx)->ctx.enqueueWork(()->{
            if(ctx.player() instanceof ServerPlayer player)accept(player,pkt);
        }));
    }
    private static void accept(ServerPlayer player,Request pkt){
        if(pkt.argument().length()>96||pkt.action().length()>16)return;
        try{
            switch(pkt.action()){
                case "status" -> sync(player);
                case "preset" -> SmokeEngine.preset(player,Integer.parseInt(pkt.argument()));
                case "train" -> SmokeEngine.practice(player,pkt.argument());
                case "equip" -> {
                    String[] parts=pkt.argument().split(":",2);
                    if(parts.length==2)SmokeEngine.equip(player,Integer.parseInt(parts[0]),parts[1]);
                }
                case "cast" -> {
                    String[] parts=pkt.argument().split(":",2);
                    if(parts.length==2)SmokeEngine.cast(player,Integer.parseInt(parts[0]),"focused".equals(parts[1]));
                }
                default -> {}
            }
        }catch(NumberFormatException ignored){/* No client input may crash the server. */}
    }
    public static void sync(ServerPlayer player){
        SmokeState s=SmokeEngine.state(player);
        String magic=SmokeEngine.innate(player)==null?"none":SmokeEngine.innate(player).id();
        String slotList=String.join(",",s.slots());
        String studied=s.study().entrySet().stream().map(e->e.getKey()+":"+e.getValue()).collect(Collectors.joining(","));
        String mastered=s.mastery().entrySet().stream().map(e->e.getKey()+":"+e.getValue()).collect(Collectors.joining(","));
        String cooldowns=IntStream.range(0,SmokeState.SLOTS).mapToObj(i->String.valueOf(SmokeEngine.remaining(player,s.equipped(i)))).collect(Collectors.joining(","));
        String line=String.join("|",magic,""+s.smoke(),""+s.capacity(),""+s.rank(),""+s.experience(),""+s.preset(),slotList,studied,mastered,cooldowns,OtherMagicMechanics.injuryStatus(player));
        PacketDistributor.sendToPlayer(player,new Snapshot(line));
    }
}
