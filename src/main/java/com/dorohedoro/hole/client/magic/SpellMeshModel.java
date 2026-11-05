package com.dorohedoro.hole.client.magic;

import com.dorohedoro.hole.Dorohedoro;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/** Original low-poly 3D spell meshes. Cubes are real geometry (not billboard particles).
 * Units are vanilla model pixels: 16 pixels = one world block. */
public final class SpellMeshModel {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
        ResourceLocation.fromNamespaceAndPath(Dorohedoro.MOD_ID, "spell_impact_mesh"), "main");
    private final ModelPart sever;
    private final ModelPart healing;
    private final ModelPart blast;
    private final ModelPart fungal;
    private final ModelPart curse;
    private final ModelPart protection;
    private final ModelPart time;

    public SpellMeshModel(ModelPart root) {
        sever = root.getChild("sever");
        healing = root.getChild("healing");
        blast = root.getChild("blast");
        fungal = root.getChild("fungal");
        curse = root.getChild("curse");
        protection = root.getChild("protection");
        time = root.getChild("time");
    }
    public ModelPart sever() { return sever; }
    public ModelPart healing() { return healing; }
    public ModelPart blast() { return blast; }
    public ModelPart fungal() { return fungal; }
    public ModelPart curse() { return curse; }
    public ModelPart protection() { return protection; }
    public ModelPart time() { return time; }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // Two crossed thick scalpel-like edges, a trailing blade ribbon,
        // and irregular fracture splinters. Oriented toward the hit target.
        PartDefinition s = root.addOrReplaceChild("sever", CubeListBuilder.create(), PartPose.ZERO);
        s.addOrReplaceChild("blade", CubeListBuilder.create()
            .texOffs(0,0).addBox(-.8f,-15.5f,-.65f,1.6f,31f,1.3f)
            .texOffs(20,0).addBox(-.3f,-17.5f,-.45f,.6f,35f,.9f),
            PartPose.rotation(0,0,.70f));
        s.addOrReplaceChild("countercut", CubeListBuilder.create()
            .texOffs(37,0).addBox(-.5f,-11.5f,-.5f,1f,23f,1f),
            PartPose.rotation(.34f,0,-.66f));
        s.addOrReplaceChild("shard_left", CubeListBuilder.create()
            .texOffs(50,0).addBox(-1f,-4f,-.45f,2f,8f,.9f),
            PartPose.offsetAndRotation(-6.6f,0,0,.14f,0,.65f));
        s.addOrReplaceChild("shard_right", CubeListBuilder.create()
            .texOffs(58,0).addBox(-.8f,-4.5f,-.45f,1.6f,9f,.9f),
            PartPose.offsetAndRotation(6.9f,1,0,.2f,0,-.48f));

        // Flesh-knitting strips form a short lived real 3D lattice over the
        // patient's chest. The effect vanishes; only healing is permanent.
        PartDefinition h = root.addOrReplaceChild("healing", CubeListBuilder.create(), PartPose.ZERO);
        h.addOrReplaceChild("strand1", CubeListBuilder.create().texOffs(0,40)
            .addBox(-.40f,-11f,-.40f,.8f,22f,.8f), PartPose.rotation(.08f,0,.70f));
        h.addOrReplaceChild("strand2", CubeListBuilder.create().texOffs(8,40)
            .addBox(-.40f,-11f,-.40f,.8f,22f,.8f), PartPose.rotation(-.08f,0,-.70f));
        h.addOrReplaceChild("strand3", CubeListBuilder.create().texOffs(16,40)
            .addBox(-.5f,-10f,-.4f,1f,20f,.8f), PartPose.rotation(.10f,.14f,1.22f));
        h.addOrReplaceChild("knots", CubeListBuilder.create().texOffs(26,40)
            .addBox(-1.3f,-1.3f,-.8f,2.6f,2.6f,1.6f)
            .texOffs(38,40).addBox(-1,-6,-.8f,2f,2f,1.6f)
            .texOffs(50,40).addBox(-1,4,-.8f,2f,2f,1.6f), PartPose.ZERO);

        // A multi-axis expanding black smoke compression shell with angular
        // outward ribs. This is model geometry, not a flat particle ring.
        PartDefinition b = root.addOrReplaceChild("blast", CubeListBuilder.create(), PartPose.ZERO);
        for (int n=0;n<12;n++) {
            float angle = (float) (n * Math.PI / 6);
            b.addOrReplaceChild("rib_"+n,
                CubeListBuilder.create().texOffs((n%4)*16, 80)
                    .addBox(-1.10f,-13f,-1.15f,2.2f,12f,2.3f),
                PartPose.offsetAndRotation(0,0,0,
                    (float)(.32 * Math.sin(angle)), angle, (float)(.25 * Math.cos(angle))));
        }
        b.addOrReplaceChild("core", CubeListBuilder.create()
            .texOffs(68,80).addBox(-4f,-4f,-4f,8f,8f,8f), PartPose.ZERO);
        // Mushroom: animated mycelial roots appear beneath physically placed
        // vanilla mushroom blocks. The mesh NEVER pretends to be the collider;
        // only MushroomSpellVisuals' real world blocks provide collision.
        PartDefinition fungus = root.addOrReplaceChild("fungal", CubeListBuilder.create(), PartPose.ZERO);
        for (int n=0; n<9; n++) {
            float angle = (float)(n*Math.PI*2/9);
            float radius = 6f + (n%3)*1.8f;
            fungus.addOrReplaceChild("mycelium_" + n,
                CubeListBuilder.create().texOffs((n%4)*12, 96)
                    .addBox(-.52f, -1f, -1.0f, 1.04f, 2f, 13f),
                PartPose.offsetAndRotation(0, -1.0f, 0, 0, -angle, .11f*(n%3)));
            fungus.addOrReplaceChild("spore_node_" + n,
                CubeListBuilder.create().texOffs((n%4)*12, 106)
                    .addBox(-1.2f,-1.2f,-1.2f,2.4f,2.4f,2.4f),
                PartPose.offset((float)Math.cos(angle)*radius, -.4f, (float)Math.sin(angle)*radius));
        }
        // Curse: soot-black tendrils latch onto the marked target's torso.
        // This geometry is entirely temporary; curse duration stays server-owned.
        PartDefinition hex = root.addOrReplaceChild("curse", CubeListBuilder.create(), PartPose.ZERO);
        for (int n=0; n<8; n++) {
            float angle = (float)(n*Math.PI/4);
            hex.addOrReplaceChild("tendril_" + n,
                CubeListBuilder.create().texOffs((n%4)*14, 0)
                    .addBox(-.55f,-10f,-.70f,1.1f,20f,1.4f),
                PartPose.offsetAndRotation((float)Math.cos(angle)*6f,0,
                    (float)Math.sin(angle)*6f,.31f*(n%2==0?1:-1),-angle,
                    .32f*(n%3==0?1:-1)));
        }
        hex.addOrReplaceChild("mark", CubeListBuilder.create().texOffs(56,0)
            .addBox(-3f,-3f,-.7f,6f,6f,1.4f), PartPose.ZERO);

        // Protection: several concentric smoke ribs build into a curved shell.
        // The actual collision and finite expiry are handled on the server by
        // OtherMagicMechanics' condensed-smoke blocks, NOT these model parts.
        PartDefinition shell = root.addOrReplaceChild("protection", CubeListBuilder.create(), PartPose.ZERO);
        for (int n=0; n<10; n++) {
            float angle = (float)(n*Math.PI*2/10);
            shell.addOrReplaceChild("rib_" + n,
                CubeListBuilder.create().texOffs((n%4)*14, 32)
                    .addBox(-.9f,-12f,-.8f,1.8f,24f,1.6f),
                PartPose.offsetAndRotation((float)Math.cos(angle)*9f,0,
                    (float)Math.sin(angle)*9f,.09f*(n%2==0?1:-1),-angle,0));
        }
        for (int ring=0; ring<3; ring++) {
            for (int n=0; n<10; n++) {
                float angle = (float)(n*Math.PI*2/10);
                shell.addOrReplaceChild("band_" + ring + "_" + n,
                    CubeListBuilder.create().texOffs((n%4)*14,56)
                        .addBox(-3f,-.6f,-.65f,6f,1.2f,1.3f),
                    PartPose.offsetAndRotation((float)Math.cos(angle)*9f, (ring-1)*9f,
                        (float)Math.sin(angle)*9f,0,-angle+(float)Math.PI/2,0));
            }
        }

        // Time: staggered concentric soot clock rings, twelve raised markers
        // and counter-rotating hands. The visual does not claim real time travel.
        PartDefinition clock = root.addOrReplaceChild("time", CubeListBuilder.create(), PartPose.ZERO);
        for (int ring=0; ring<2; ring++) {
            for (int n=0; n<12; n++) {
                float angle = (float)(n*Math.PI/6);
                float rad=ring==0?12.0f:7.8f;
                clock.addOrReplaceChild("clock_" + ring + "_" + n,
                    CubeListBuilder.create().texOffs((n%4)*14, 72)
                        .addBox(-3.1f,-.65f,-.9f,6.2f,1.3f,1.8f),
                    PartPose.offsetAndRotation((float)Math.cos(angle)*rad,
                        (float)Math.sin(angle)*rad, ring==0?0:2.2f,
                        0,0,angle+(float)Math.PI/2));
            }
        }
        clock.addOrReplaceChild("hour", CubeListBuilder.create().texOffs(56, 72)
            .addBox(-.70f,-8.0f,-1.3f,1.4f,9f,2.6f),PartPose.rotation(0,0,-.65f));
        clock.addOrReplaceChild("minute", CubeListBuilder.create().texOffs(70,72)
            .addBox(-.35f,-12.5f,-1.7f,.7f,13.5f,3.4f),PartPose.rotation(0,0,.45f));
        clock.addOrReplaceChild("pivot", CubeListBuilder.create().texOffs(84,72)
            .addBox(-2f,-2f,-2f,4f,4f,4f), PartPose.ZERO);
        return LayerDefinition.create(mesh, 128,128);
    }
}
