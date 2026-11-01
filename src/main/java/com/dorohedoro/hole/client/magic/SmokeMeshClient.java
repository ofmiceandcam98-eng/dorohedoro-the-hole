package com.dorohedoro.hole.client.magic;

import com.dorohedoro.hole.Dorohedoro;
import com.dorohedoro.hole.character.MagicType;
import com.dorohedoro.hole.magic.SmokeSkill;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** Client presentation only: short-lived 3D mesh impacts and a rig-following
 * reptile anatomy layer. Server owns damage, injury, block destruction and
 * persistence. Events are dispatched to nearby clients after valid casts. */
public final class SmokeMeshClient {
    private SmokeMeshClient() {}
    private static final int MAX_EVENTS=96;
    private static final Map<Integer, Morph> MORPHS=new HashMap<>();
    private static final ArrayDeque<Cue> EVENTS=new ArrayDeque<>();
    private static net.minecraft.client.multiplayer.ClientLevel activeLevel;
    private static SpellMeshModel meshes;
    private static final ResourceLocation SLASH=ResourceLocation.fromNamespaceAndPath(
        Dorohedoro.MOD_ID,"textures/magic/sever_mesh.png");
    private static final ResourceLocation HEAL=ResourceLocation.fromNamespaceAndPath(
        Dorohedoro.MOD_ID,"textures/magic/healing_mesh.png");
    private static final ResourceLocation BLAST=ResourceLocation.fromNamespaceAndPath(
        Dorohedoro.MOD_ID,"textures/magic/blast_mesh.png");
    private static final ResourceLocation FUNGAL=ResourceLocation.fromNamespaceAndPath(
        Dorohedoro.MOD_ID,"textures/magic/fungal_mesh.png");
    private static final ResourceLocation CURSE=ResourceLocation.fromNamespaceAndPath(
        Dorohedoro.MOD_ID,"textures/magic/curse_mesh.png");
    private static final ResourceLocation PROTECTION=ResourceLocation.fromNamespaceAndPath(
        Dorohedoro.MOD_ID,"textures/magic/protection_mesh.png");
    private static final ResourceLocation TIME=ResourceLocation.fromNamespaceAndPath(
        Dorohedoro.MOD_ID,"textures/magic/time_mesh.png");

    private record Morph(long until,float power) {}
    private record Cue(int caster,int target,Vec3 point,SmokeSkill skill,
                       int mastery,boolean focused,long start) {}

    public static void register(IEventBus modBus) {
        modBus.addListener(SmokeMeshClient::layers);
        modBus.addListener(SmokeMeshClient::addPlayerLayers);
        NeoForge.EVENT_BUS.addListener(SmokeMeshClient::renderLevel);
        NeoForge.EVENT_BUS.addListener(LimbAnimationClient::clientTick);
    }
    private static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(SpellMeshModel.LAYER,SpellMeshModel::createBodyLayer);
        event.registerLayerDefinition(ReptileMorphLayer.LAYER,ReptileMorphLayer::createBodyLayer);
        event.registerLayerDefinition(LimbAnimationClient.LAYER, LimbAnimationClient::createBodyLayer);
    }
    private static void addPlayerLayers(EntityRenderersEvent.AddLayers event) {
        meshes=new SpellMeshModel(event.getEntityModels().bakeLayer(SpellMeshModel.LAYER));
        LimbAnimationClient.bake(event.getEntityModels().bakeLayer(LimbAnimationClient.LAYER));
        for(var skin : event.getSkins()) {
            if(event.getSkin(skin) instanceof PlayerRenderer renderer)
                renderer.addLayer(new ReptileMorphLayer(renderer,
                    event.getEntityModels().bakeLayer(ReptileMorphLayer.LAYER)));
        }
    }

    /** Invoked via the server-to-client payload handler on the client game thread. */
    public static void receive(String data) {
        Minecraft mc=Minecraft.getInstance();
        if(mc.level==null || data.length()>256) return;
        String[] parts=data.split(",",8);
        if(parts.length!=8)return;
        try {
            int caster=Integer.parseInt(parts[0]);
            int target=Integer.parseInt(parts[1]);
            double x=Double.parseDouble(parts[2]),y=Double.parseDouble(parts[3]),z=Double.parseDouble(parts[4]);
            int mastery=Math.clamp(Integer.parseInt(parts[5]),0,1000);
            SmokeSkill skill=SmokeSkill.fromId(parts[6]);
            if(skill==null || !Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(z))return;
            if(activeLevel!=mc.level) {EVENTS.clear();MORPHS.clear();activeLevel=mc.level;}
            boolean focused="1".equals(parts[7]);
            if(EVENTS.size()>=MAX_EVENTS)EVENTS.removeFirst();
            EVENTS.addLast(new Cue(caster,target,new Vec3(x,y,z),skill,mastery,focused,mc.level.getGameTime()));
            if(skill.magic()==MagicType.REPTILE) {
                int generic=(focused?220:130)+Math.min(100,20+mastery/25);
                int duration=switch(skill) {
                    case APEX_FORM -> 120 + Math.min(380,mastery/2) + (focused?120:0);
                    case WALL_CRAWL -> 100 + Math.min(450,mastery) + (focused?100:0);
                    case SCALE_SKIN,REPTILE_RAGE,CHAMELEON_HUNT,PREDATOR_SPRINT -> generic;
                    case SHED_SKIN -> 80;
                    default -> 24;
                };
                float strength=skill==SmokeSkill.APEX_FORM?1.0f
                    :skill==SmokeSkill.REPTILE_RAGE?.85f:.60f;
                Morph current=MORPHS.get(caster);
                long expires=mc.level.getGameTime()+duration;
                // Tail Lash while transformed must not prematurely cancel Apex Form.
                if(current==null || current.until()<expires)
                    MORPHS.put(caster,new Morph(expires,Math.max(strength,current==null?0:current.power())));
            }
        } catch(RuntimeException ignored) { /* Corrupt or stale cosmetic packet. */ }
    }

    static float morphPower(int id,long tick) {
        Morph m=MORPHS.get(id);
        if(m==null)return 0;
        if(tick>=m.until()) {MORPHS.remove(id);return 0;}
        return m.power();
    }

    private static void renderLevel(RenderLevelStageEvent e) {
        if(e.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES || e.getPoseStack()==null || meshes==null)return;
        Minecraft mc=Minecraft.getInstance();
        if(mc.level==null)return;
        // Limb events must render even if this world has not yet received a
        // normal SmokeMesh cast. Do not skip their first render frame.
        if(activeLevel!=mc.level) {EVENTS.clear();MORPHS.clear();activeLevel=mc.level;}
        LimbAnimationClient.renderLevel(e);
        if(EVENTS.isEmpty())return;
        long tick=mc.level.getGameTime();
        Vec3 camera=e.getCamera().getPosition();
        PoseStack pose=e.getPoseStack();
        MultiBufferSource.BufferSource buffers=mc.renderBuffers().bufferSource();
        var severTexture=buffers.getBuffer(RenderType.entityTranslucent(SLASH));
        var healTexture=buffers.getBuffer(RenderType.entityTranslucent(HEAL));
        var blastTexture=buffers.getBuffer(RenderType.entityTranslucent(BLAST));
        var fungusTexture=buffers.getBuffer(RenderType.entityTranslucent(FUNGAL));
        var curseTexture=buffers.getBuffer(RenderType.entityTranslucent(CURSE));
        var protectionTexture=buffers.getBuffer(RenderType.entityTranslucent(PROTECTION));
        var timeTexture=buffers.getBuffer(RenderType.entityTranslucent(TIME));
        Iterator<Cue> it=EVENTS.iterator();
        while(it.hasNext()) {
            Cue c=it.next();
            int duration=c.skill.magic()==MagicType.DESTRUCTION?32:28;
            double age=tick-c.start;
            if(age<0||age>duration){it.remove();continue;}
            MagicType type=c.skill.magic();
            // Reptile uses a body-attached model layer. All seven other magic
            // families use these synchronized, ephemeral world meshes.
            if(type==MagicType.REPTILE)continue;
            Vec3 at=c.point;
            Entity live=c.target<0?null:mc.level.getEntity(c.target);
            if(live!=null && live.isAlive())at=live.position().add(0,live.getBbHeight()*.60,0);
            if(at.distanceToSqr(camera)>96*96)continue;
            pose.pushPose();
            pose.translate(at.x-camera.x,at.y-camera.y,at.z-camera.z);
            Entity caster=mc.level.getEntity(c.caster);
            if(caster!=null) {
                Vec3 diff=at.subtract(caster.position());
                pose.mulPose(Axis.YP.rotationDegrees((float)(-Math.atan2(diff.x,diff.z)*180/Math.PI)));
            }
            float t=(float)Math.clamp(age/duration,0,1);
            float scale;
            // JOML rotationDegrees expects float; short-lived cues fit exactly.
            float rotationAge = (float) age;
            switch(type) {
                case MUSHROOM -> {
                    // Mycelial veins spread at floor level ahead of the REAL
                    // small/giant mushroom blocks, which supply collision.
                    float strength=(c.skill()==SmokeSkill.SPORE_DOMINION?1.45f:
                        c.skill()==SmokeSkill.FUNGAL_BLOOM?1.15f:.68f);
                    scale=(float)(.15+1.2*Math.sin(Math.min(1.0,t/.65)*Math.PI/2))
                        *(1-t*.75f)*strength*(c.focused?1.18f:1f);
                    pose.translate(0,-.72,0);
                    pose.mulPose(Axis.YP.rotationDegrees(rotationAge*3.4f));
                    pose.scale(scale,scale,scale);
                    meshes.fungal().render(pose,fungusTexture,0xF000F0,OverlayTexture.NO_OVERLAY);
                }
                case CURSE -> {
                    // A strangling wreath: eight irregular dark tendrils close
                    // onto the mark, then retreat. The curse itself persists on
                    // the server and is not represented by this render timer.
                    float threat=(c.skill()==SmokeSkill.RUINOUS_CURSE?1.35f:1f);
                    scale=(float)(.40+1.15*Math.sin(Math.PI*Math.min(1,t/.72f)))
                        *(1-t*.70f)*threat;
                    pose.mulPose(Axis.YP.rotationDegrees(rotationAge*11f));
                    pose.mulPose(Axis.XP.rotationDegrees((float)Math.sin(age*.22)*10));
                    pose.scale(scale,scale,scale);
                    meshes.curse().render(pose,curseTexture,0xF000F0,OverlayTexture.NO_OVERLAY);
                }
                case PROTECTION -> {
                    // Shell geometry forms outward from the protected target;
                    // the actual temporary barrier has its own saved expiry.
                    scale=(float)(.10+1.2*Math.min(1,age/9.0))*(1-t*.76f)
                        *(c.skill()==SmokeSkill.SMOKE_BULWARK?1.32f:1f);
                    pose.mulPose(Axis.YP.rotationDegrees(rotationAge*2.4f));
                    pose.scale(scale,scale,scale);
                    meshes.protection().render(pose,protectionTexture,0xF000F0,OverlayTexture.NO_OVERLAY);
                }
                case TIME -> {
                    // Uneven double chronometer: visually distinct from a
                    // generic slowing sphere, without promising real time travel.
                    scale=(float)(.15+1.15*Math.min(1,age/8.0))*(1-t*.77f)
                        *(c.skill()==SmokeSkill.CHRONO_BURST?1.35f:1f);
                    pose.mulPose(Axis.YP.rotationDegrees(rotationAge*2f));
                    pose.mulPose(Axis.ZP.rotationDegrees(rotationAge*(c.focused?13f:8f)));
                    pose.scale(scale,scale,scale);
                    meshes.time().render(pose,timeTexture,0xF000F0,OverlayTexture.NO_OVERLAY);
                }
                case DISMEMBERMENT -> {
                    // A violent, short lived cross-cut. Grows then collapses.
                    scale=(float)(.24+Math.min(1,age/5.0)*1.35)*(1-t*.85f);
                    pose.mulPose(Axis.ZP.rotationDegrees(t*38f));
                    pose.scale(scale,scale,scale);
                    meshes.sever().render(pose,severTexture,0xF000F0,OverlayTexture.NO_OVERLAY);
                }
                case HEALING -> {
                    // Anatomical repair: crisscrossing wound fibers converge.
                    scale=(float)(.10+.9*Math.min(1,age/7.0))*(1-t*.87f);
                    pose.mulPose(Axis.YP.rotationDegrees(rotationAge*8f));
                    pose.scale(scale,scale,scale);
                    meshes.healing().render(pose,healTexture,0xF000F0,OverlayTexture.NO_OVERLAY);
                }
                case DESTRUCTION -> {
                    // A compressed core erupts into 3D outward ribs.
                    float techniqueScale=switch(c.skill) {
                        case SMOKE_SHOT,RUPTURE_ARMOR -> .48f;
                        case BREACH_BOLT,DETONATION -> .80f;
                        case SHOCKWAVE,SHRAPNEL_SMOKE -> 1.20f;
                        case ANNIHILATION_STREAM -> 1.75f;
                        default -> 1.0f;
                    };
                    scale=(float)(.18+Math.pow(t,.55)*2.1)*(1-t*.73f)
                        *(c.focused?1.3f:1f)*(1+Math.min(1,c.mastery/1000f)*.22f)*techniqueScale;
                    pose.mulPose(Axis.YP.rotationDegrees(rotationAge*16f));
                    pose.mulPose(Axis.ZP.rotationDegrees(rotationAge*9f));
                    pose.scale(scale,scale,scale);
                    meshes.blast().render(pose,blastTexture,0xF000F0,OverlayTexture.NO_OVERLAY);
                }
                default -> { }
            }
            pose.popPose();
        }
        buffers.endBatch(RenderType.entityTranslucent(SLASH));
        buffers.endBatch(RenderType.entityTranslucent(HEAL));
        buffers.endBatch(RenderType.entityTranslucent(BLAST));
        buffers.endBatch(RenderType.entityTranslucent(FUNGAL));
        buffers.endBatch(RenderType.entityTranslucent(CURSE));
        buffers.endBatch(RenderType.entityTranslucent(PROTECTION));
        buffers.endBatch(RenderType.entityTranslucent(TIME));
    }
}
