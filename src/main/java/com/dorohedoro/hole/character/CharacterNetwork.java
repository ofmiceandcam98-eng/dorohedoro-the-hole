package com.dorohedoro.hole.character;

import com.dorohedoro.hole.Dorohedoro;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;
import java.util.Arrays;

/** Server-authoritative origin, hidden affinity quiz, optional fate roll and tie-limited choice. */
public final class CharacterNetwork {
    private CharacterNetwork() {}
    private static final Map<UUID,Session> SESSIONS=new ConcurrentHashMap<>();
    private static final class Session {
        final int[] order;
        MagicType[] tied=null;
        String submittedAnswers=null;
        Session(int[] order){this.order=order;}
    }
    public static void register(IEventBus bus){bus.addListener(CharacterNetwork::registerPayloads);}

    public record OpenCreation(String reason,String questions) implements CustomPacketPayload {
        public static final Type<OpenCreation> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID,"open_character_creation"));
        public static final StreamCodec<ByteBuf,OpenCreation> CODEC=StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,OpenCreation::reason,ByteBufCodecs.STRING_UTF8,OpenCreation::questions,OpenCreation::new);
        @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    /** Action: human, quiz, fate, or tie; choice is accepted only when tied on the server. */
    public record Submit(String action,String value) implements CustomPacketPayload {
        public static final Type<Submit> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID,"submit_character_origin"));
        public static final StreamCodec<ByteBuf,Submit> CODEC=StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,Submit::action,ByteBufCodecs.STRING_UTF8,Submit::value,Submit::new);
        @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    /** Result: human, sorcerer, tie, or error. */
    public record Decision(String result,String value) implements CustomPacketPayload {
        public static final Type<Decision> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID,"character_decision"));
        public static final StreamCodec<ByteBuf,Decision> CODEC=StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,Decision::result,ByteBufCodecs.STRING_UTF8,Decision::value,Decision::new);
        @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }

    public static void begin(ServerPlayer player) {
        if(player.getData(CharacterAttachments.PROFILE).selected()) return;
        Session session=new Session(PersonalityQuiz.randomQuestions(new Random(player.getRandom().nextLong())));
        SESSIONS.put(player.getUUID(),session);
        PacketDistributor.sendToPlayer(player,new OpenCreation("new",PersonalityQuiz.encodeOrder(session.order)));
    }
    public static void abandon(ServerPlayer player){SESSIONS.remove(player.getUUID());}

    private static void registerPayloads(RegisterPayloadHandlersEvent event){
        PayloadRegistrar reg=event.registrar("character_3");
        reg.playToClient(OpenCreation.TYPE,OpenCreation.CODEC,(payload,ctx)->{
            if(FMLEnvironment.dist==Dist.CLIENT) ctx.enqueueWork(()->
                com.dorohedoro.hole.client.character.CharacterCreationScreen.onServerOpen(payload.reason(),payload.questions()));
        });
        reg.playToClient(Decision.TYPE,Decision.CODEC,(payload,ctx)->{
            if(FMLEnvironment.dist==Dist.CLIENT) ctx.enqueueWork(()->
                com.dorohedoro.hole.client.character.CharacterCreationScreen.onServerDecision(payload.result(),payload.value()));
        });
        reg.playToServer(Submit.TYPE,Submit.CODEC,(payload,ctx)->ctx.enqueueWork(()->{
            if(ctx.player() instanceof ServerPlayer player) handleSubmit(player,payload);
        }));
    }
    private static void reject(ServerPlayer player,String why){
        player.sendSystemMessage(Component.literal("Character creation: "+why));
        PacketDistributor.sendToPlayer(player,new Decision("error",why));
    }
    private static void finish(ServerPlayer player, String origin, MagicType type, String evidence, String mode) {
        // One save write, server only. Subsequent submissions cannot change this identity.
        if(player.getData(CharacterAttachments.PROFILE).selected()) return;
        String magic=type==null?"none":type.id();
        player.setData(CharacterAttachments.PROFILE,new CharacterProfile(origin,magic,evidence,mode,false,1));
        SESSIONS.remove(player.getUUID());
        PacketDistributor.sendToPlayer(player,new Decision(origin,magic));
        com.dorohedoro.hole.magic.SmokeNetwork.sync(player);
    }
    private static void handleSubmit(ServerPlayer player,Submit packet){
        if(player.getData(CharacterAttachments.PROFILE).selected()) {reject(player,"Character already created.");return;}
        Session session=SESSIONS.get(player.getUUID());
        if(session==null){reject(player,"Session expired. Rejoin to begin again.");return;}
        switch(packet.action()) {
            case "human" -> {
                if(!packet.value().isEmpty()||session.tied!=null){reject(player,"Invalid Human submission.");return;}
                finish(player,"human",null,"","human");
            }
            case "fate" -> {
                if(!packet.value().isEmpty()||session.tied!=null){reject(player,"Fate is only available before quiz submission.");return;}
                finish(player,"sorcerer",PersonalityQuiz.rollFate(new Random(player.getRandom().nextLong())),"","fate");
            }
            case "quiz" -> {
                if(session.tied!=null){reject(player,"Resolve the pending tied result first.");return;}
                if(!PersonalityQuiz.valid(session.order,packet.value())){reject(player,"Complete all eight quiz questions.");return;}
                MagicType[] leaders=PersonalityQuiz.leaders(session.order,packet.value());
                String evidence=PersonalityQuiz.encodeOrder(session.order)+"|"+packet.value();
                if(leaders.length==1){finish(player,"sorcerer",leaders[0],evidence,"quiz");return;}
                session.tied=leaders;
                session.submittedAnswers=packet.value();
                String names=String.join(",",Arrays.stream(leaders).map(MagicType::id).toArray(String[]::new));
                PacketDistributor.sendToPlayer(player,new Decision("tie",names));
            }
            case "tie" -> {
                MagicType requested=MagicType.fromId(packet.value());
                if(session.tied==null||requested==null||Arrays.stream(session.tied).noneMatch(t->t==requested)) {
                    reject(player,"You may choose only a highest-score tied magic type.");return;
                }
                finish(player,"sorcerer",requested,PersonalityQuiz.encodeOrder(session.order)+"|"+session.submittedAnswers,"quiz_tie");
            }
            default -> reject(player,"Unknown character selection action.");
        }
    }
}
