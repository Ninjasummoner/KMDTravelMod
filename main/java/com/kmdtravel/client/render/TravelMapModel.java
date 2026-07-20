package com.kmdtravel.client.render;

import com.kmdtravel.KMDTravel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public final class TravelMapModel {
    public static final float ANIMATION_LENGTH = 1.16667F;
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(KMDTravel.MOD_ID, "travel_map"), "main");

    private final ModelPart root;
    private final ModelPart closed_scroll;
    private final ModelPart opening_scroll;
    private final ModelPart open_scroll;
    private final ModelPart parchment_strip_rig_01;
    private final ModelPart parchment_strip_rig_02;
    private final ModelPart parchment_strip_rig_03;
    private final ModelPart parchment_strip_rig_04;
    private final ModelPart parchment_strip_rig_05;
    private final ModelPart parchment_strip_rig_06;
    private final ModelPart parchment_strip_rig_07;
    private final ModelPart parchment_strip_rig_08;
    private final ModelPart parchment_strip_rig_09;
    private final ModelPart parchment_strip_rig_10;
    private final ModelPart parchment_strip_rig_11;
    private final ModelPart parchment_strip_rig_12;
    private final ModelPart left_roller_rig;
    private final ModelPart right_roller_rig;

    public TravelMapModel(ModelPart root) {
        this.root = root;
        this.closed_scroll = root.getChild("closed_scroll");
        this.opening_scroll = root.getChild("opening_scroll");
        this.open_scroll = root.getChild("open_scroll");
        this.parchment_strip_rig_01 = this.open_scroll.getChild("parchment_strip_rig_01");
        this.parchment_strip_rig_02 = this.open_scroll.getChild("parchment_strip_rig_02");
        this.parchment_strip_rig_03 = this.open_scroll.getChild("parchment_strip_rig_03");
        this.parchment_strip_rig_04 = this.open_scroll.getChild("parchment_strip_rig_04");
        this.parchment_strip_rig_05 = this.open_scroll.getChild("parchment_strip_rig_05");
        this.parchment_strip_rig_06 = this.open_scroll.getChild("parchment_strip_rig_06");
        this.parchment_strip_rig_07 = this.open_scroll.getChild("parchment_strip_rig_07");
        this.parchment_strip_rig_08 = this.open_scroll.getChild("parchment_strip_rig_08");
        this.parchment_strip_rig_09 = this.open_scroll.getChild("parchment_strip_rig_09");
        this.parchment_strip_rig_10 = this.open_scroll.getChild("parchment_strip_rig_10");
        this.parchment_strip_rig_11 = this.open_scroll.getChild("parchment_strip_rig_11");
        this.parchment_strip_rig_12 = this.open_scroll.getChild("parchment_strip_rig_12");
        this.left_roller_rig = this.open_scroll.getChild("left_roller_rig");
        this.right_roller_rig = this.open_scroll.getChild("right_roller_rig");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition closed_scroll = root.addOrReplaceChild("closed_scroll", CubeListBuilder.create(), PartPose.offsetAndRotation(8F, -8F, 8F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        PartDefinition opening_scroll = root.addOrReplaceChild("opening_scroll", CubeListBuilder.create(), PartPose.offsetAndRotation(8F, -8F, 8F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        PartDefinition open_scroll = root.addOrReplaceChild("open_scroll", CubeListBuilder.create(), PartPose.offsetAndRotation(8F, -8F, 8F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        PartDefinition parchment_strip_rig_01 = open_scroll.addOrReplaceChild("parchment_strip_rig_01", CubeListBuilder.create(), PartPose.offsetAndRotation(-5.71417F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        PartDefinition parchment_strip_rig_02 = open_scroll.addOrReplaceChild("parchment_strip_rig_02", CubeListBuilder.create(), PartPose.offsetAndRotation(-4.6725F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        PartDefinition parchment_strip_rig_03 = open_scroll.addOrReplaceChild("parchment_strip_rig_03", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.63083F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        PartDefinition parchment_strip_rig_04 = open_scroll.addOrReplaceChild("parchment_strip_rig_04", CubeListBuilder.create(), PartPose.offsetAndRotation(-2.58917F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        PartDefinition parchment_strip_rig_05 = open_scroll.addOrReplaceChild("parchment_strip_rig_05", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.5475F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        PartDefinition parchment_strip_rig_06 = open_scroll.addOrReplaceChild("parchment_strip_rig_06", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.50583F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        PartDefinition parchment_strip_rig_07 = open_scroll.addOrReplaceChild("parchment_strip_rig_07", CubeListBuilder.create(), PartPose.offsetAndRotation(0.53583F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        PartDefinition parchment_strip_rig_08 = open_scroll.addOrReplaceChild("parchment_strip_rig_08", CubeListBuilder.create(), PartPose.offsetAndRotation(1.5775F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        PartDefinition parchment_strip_rig_09 = open_scroll.addOrReplaceChild("parchment_strip_rig_09", CubeListBuilder.create(), PartPose.offsetAndRotation(2.61917F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        PartDefinition parchment_strip_rig_10 = open_scroll.addOrReplaceChild("parchment_strip_rig_10", CubeListBuilder.create(), PartPose.offsetAndRotation(3.66083F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        PartDefinition parchment_strip_rig_11 = open_scroll.addOrReplaceChild("parchment_strip_rig_11", CubeListBuilder.create(), PartPose.offsetAndRotation(4.7025F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        PartDefinition parchment_strip_rig_12 = open_scroll.addOrReplaceChild("parchment_strip_rig_12", CubeListBuilder.create(), PartPose.offsetAndRotation(5.74417F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        PartDefinition left_roller_rig = open_scroll.addOrReplaceChild("left_roller_rig", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        PartDefinition right_roller_rig = open_scroll.addOrReplaceChild("right_roller_rig", CubeListBuilder.create(), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        closed_scroll.addOrReplaceChild("cube_0", CubeListBuilder.create().texOffs(0, 0).addBox(-1.25F, -5F, -1.5F, 2.5F, 10F, 3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        closed_scroll.addOrReplaceChild("cube_1", CubeListBuilder.create().texOffs(32, 0).addBox(-1.75F, 4F, -2F, 3.5F, 1.75F, 4F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        closed_scroll.addOrReplaceChild("cube_2", CubeListBuilder.create().texOffs(32, 0).addBox(-1.75F, -5.75F, -2F, 3.5F, 1.75F, 4F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        closed_scroll.addOrReplaceChild("cube_3", CubeListBuilder.create().texOffs(48, 0).addBox(-1F, 5.5F, -1.5F, 2F, 1F, 3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        closed_scroll.addOrReplaceChild("cube_4", CubeListBuilder.create().texOffs(48, 0).addBox(-1F, -6.5F, -1.5F, 2F, 1F, 3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        opening_scroll.addOrReplaceChild("cube_5", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -4F, -1F, 3.5F, 8F, 1.25F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 8F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        opening_scroll.addOrReplaceChild("cube_6", CubeListBuilder.create().texOffs(0, 0).addBox(0F, -4F, -1F, 3.5F, 8F, 1.25F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, -8F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        opening_scroll.addOrReplaceChild("cube_7", CubeListBuilder.create().texOffs(32, 0).addBox(-4.25F, -5F, -1.6F, 1.5F, 10F, 2.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        opening_scroll.addOrReplaceChild("cube_8", CubeListBuilder.create().texOffs(48, 0).addBox(-4.8F, 4F, -2.1F, 2.6F, 1.75F, 3.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        opening_scroll.addOrReplaceChild("cube_9", CubeListBuilder.create().texOffs(48, 0).addBox(-4.8F, -5.75F, -2.1F, 2.6F, 1.75F, 3.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        opening_scroll.addOrReplaceChild("cube_10", CubeListBuilder.create().texOffs(32, 0).addBox(2.75F, -5F, -1.6F, 1.5F, 10F, 2.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        opening_scroll.addOrReplaceChild("cube_11", CubeListBuilder.create().texOffs(48, 0).addBox(2.2F, 4F, -2.1F, 2.6F, 1.75F, 3.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        opening_scroll.addOrReplaceChild("cube_12", CubeListBuilder.create().texOffs(48, 0).addBox(2.2F, -5.75F, -2.1F, 2.6F, 1.75F, 3.6F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        opening_scroll.addOrReplaceChild("cube_13", CubeListBuilder.create().texOffs(48, 0).addBox(-0.5F, -6.5F, -1.8F, 2F, 1F, 3F), PartPose.offsetAndRotation(3F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        left_roller_rig.addOrReplaceChild("cube_14", CubeListBuilder.create().texOffs(32, 0).addBox(-6.25F, -5F, -1.7F, 1.5F, 10F, 2.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        left_roller_rig.addOrReplaceChild("cube_15", CubeListBuilder.create().texOffs(48, 0).addBox(-6.85F, 4F, -2.2F, 2.7F, 1.85F, 3.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        left_roller_rig.addOrReplaceChild("cube_16", CubeListBuilder.create().texOffs(48, 0).addBox(-6.85F, -5.85F, -2.2F, 2.7F, 1.85F, 3.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        right_roller_rig.addOrReplaceChild("cube_17", CubeListBuilder.create().texOffs(32, 0).addBox(4.75F, -5F, -1.7F, 1.5F, 10F, 2.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        right_roller_rig.addOrReplaceChild("cube_18", CubeListBuilder.create().texOffs(48, 0).addBox(4.15F, 4F, -2.2F, 2.7F, 1.85F, 3.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        right_roller_rig.addOrReplaceChild("cube_19", CubeListBuilder.create().texOffs(48, 0).addBox(4.15F, -5.85F, -2.2F, 2.7F, 1.85F, 3.8F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        right_roller_rig.addOrReplaceChild("cube_20", CubeListBuilder.create().texOffs(48, 0).addBox(-0.5F, -6.6F, -1.8F, 2F, 1F, 3F), PartPose.offsetAndRotation(5F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        opening_scroll.addOrReplaceChild("cube_21", CubeListBuilder.create().texOffs(48, 0).addBox(-0.5F, -6.5F, -1.8F, 2F, 1F, 3F), PartPose.offsetAndRotation(-4F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        opening_scroll.addOrReplaceChild("cube_22", CubeListBuilder.create().texOffs(48, 0).addBox(-0.5F, -6.5F, -1.8F, 2F, 1F, 3F), PartPose.offsetAndRotation(3F, 12F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        opening_scroll.addOrReplaceChild("cube_23", CubeListBuilder.create().texOffs(48, 0).addBox(-0.5F, -6.5F, -1.8F, 2F, 1F, 3F), PartPose.offsetAndRotation(-4F, 12F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        left_roller_rig.addOrReplaceChild("cube_24", CubeListBuilder.create().texOffs(48, 0).addBox(-0.5F, -6.6F, -1.8F, 2F, 1F, 3F), PartPose.offsetAndRotation(-6F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        right_roller_rig.addOrReplaceChild("cube_25", CubeListBuilder.create().texOffs(48, 0).addBox(-0.5F, -6.6F, -1.8F, 2F, 1F, 3F), PartPose.offsetAndRotation(5F, 12F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        left_roller_rig.addOrReplaceChild("cube_26", CubeListBuilder.create().texOffs(48, 0).addBox(-0.5F, -6.6F, -1.8F, 2F, 1F, 3F), PartPose.offsetAndRotation(-6F, 12F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        parchment_strip_rig_01.addOrReplaceChild("cube_27", CubeListBuilder.create().texOffs(0, 0).addBox(-0.53583F, -4.5F, -1.15F, 1.07167F, 9F, 1.3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        parchment_strip_rig_02.addOrReplaceChild("cube_28", CubeListBuilder.create().texOffs(0, 0).addBox(-0.53583F, -4.5F, -1.15F, 1.07166F, 9F, 1.3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        parchment_strip_rig_03.addOrReplaceChild("cube_29", CubeListBuilder.create().texOffs(0, 0).addBox(-0.53584F, -4.5F, -1.15F, 1.07167F, 9F, 1.3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        parchment_strip_rig_04.addOrReplaceChild("cube_30", CubeListBuilder.create().texOffs(0, 0).addBox(-0.53583F, -4.5F, -1.15F, 1.07167F, 9F, 1.3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        parchment_strip_rig_05.addOrReplaceChild("cube_31", CubeListBuilder.create().texOffs(0, 0).addBox(-0.53583F, -4.5F, -1.15F, 1.07166F, 9F, 1.3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        parchment_strip_rig_06.addOrReplaceChild("cube_32", CubeListBuilder.create().texOffs(0, 0).addBox(-0.53584F, -4.5F, -1.15F, 1.07167F, 9F, 1.3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        parchment_strip_rig_07.addOrReplaceChild("cube_33", CubeListBuilder.create().texOffs(0, 0).addBox(-0.53583F, -4.5F, -1.15F, 1.07167F, 9F, 1.3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        parchment_strip_rig_08.addOrReplaceChild("cube_34", CubeListBuilder.create().texOffs(0, 0).addBox(-0.53583F, -4.5F, -1.15F, 1.07166F, 9F, 1.3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        parchment_strip_rig_09.addOrReplaceChild("cube_35", CubeListBuilder.create().texOffs(0, 0).addBox(-0.53584F, -4.5F, -1.15F, 1.07167F, 9F, 1.3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        parchment_strip_rig_10.addOrReplaceChild("cube_36", CubeListBuilder.create().texOffs(0, 0).addBox(-0.53583F, -4.5F, -1.15F, 1.07167F, 9F, 1.3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        parchment_strip_rig_11.addOrReplaceChild("cube_37", CubeListBuilder.create().texOffs(0, 0).addBox(-0.53583F, -4.5F, -1.15F, 1.07166F, 9F, 1.3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        parchment_strip_rig_12.addOrReplaceChild("cube_38", CubeListBuilder.create().texOffs(0, 0).addBox(-0.53584F, -4.5F, -1.15F, 1.07167F, 9F, 1.3F), PartPose.offsetAndRotation(0F, 0F, 0F, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD, 0F * Mth.DEG_TO_RAD));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public void setupAnimation(float animationSeconds) {
        this.closed_scroll.resetPose();
        this.opening_scroll.resetPose();
        this.open_scroll.resetPose();
        this.parchment_strip_rig_01.resetPose();
        this.parchment_strip_rig_02.resetPose();
        this.parchment_strip_rig_03.resetPose();
        this.parchment_strip_rig_04.resetPose();
        this.parchment_strip_rig_05.resetPose();
        this.parchment_strip_rig_06.resetPose();
        this.parchment_strip_rig_07.resetPose();
        this.parchment_strip_rig_08.resetPose();
        this.parchment_strip_rig_09.resetPose();
        this.parchment_strip_rig_10.resetPose();
        this.parchment_strip_rig_11.resetPose();
        this.parchment_strip_rig_12.resetPose();
        this.left_roller_rig.resetPose();
        this.right_roller_rig.resetPose();
        float time = Mth.clamp(animationSeconds, 0.0F, ANIMATION_LENGTH);
        apply(this.closed_scroll, Channel.SCALE, time, new float[] {0F, 1.15F}, new float[] {0.001F, 0.001F, 0.001F, 0.001F, 0.001F, 0.001F});
        apply(this.opening_scroll, Channel.SCALE, time, new float[] {0F, 1.15F}, new float[] {0.001F, 0.001F, 0.001F, 0.001F, 0.001F, 0.001F});
        apply(this.left_roller_rig, Channel.POSITION, time, new float[] {0F, 0.18F, 0.6F, 1.05F}, new float[] {5.5F, 0F, 0F, 5.5F, 0F, 0F, 2.6F, 0F, 0F, 0F, 0F, 0F});
        apply(this.left_roller_rig, Channel.SCALE, time, new float[] {0F, 1.15F}, new float[] {1F, 1F, 1F, 1F, 1F, 1F});
        apply(this.right_roller_rig, Channel.POSITION, time, new float[] {0F, 0.18F, 0.6F, 1.05F}, new float[] {-5.5F, 0F, 0F, -5.5F, 0F, 0F, -2.6F, 0F, 0F, 0F, 0F, 0F});
        apply(this.right_roller_rig, Channel.SCALE, time, new float[] {0F, 1.15F}, new float[] {1F, 1F, 1F, 1F, 1F, 1F});
        apply(this.parchment_strip_rig_01, Channel.ROTATION, time, new float[] {0F, 0.18F, 0.6F, 1.05F}, new float[] {0F, -32F, 0F, 0F, -32F, 0F, 0F, -12F, 0F, 0F, 0F, 0F});
        apply(this.parchment_strip_rig_01, Channel.POSITION, time, new float[] {0F, 0.18F, 0.6F, 0.83333F, 1.05F, 1.16667F}, new float[] {5.714167F, 0F, 0.18F, 5.714167F, 0F, 0.18F, 2.7428F, 0F, 0.06F, 2.03F, 0F, 0.02F, 1F, 0F, 0F, 1F, 0F, 0F});
        apply(this.parchment_strip_rig_01, Channel.SCALE, time, new float[] {0F, 1.15F}, new float[] {1F, 1F, 1F, 1F, 1F, 1F});
        apply(this.parchment_strip_rig_02, Channel.ROTATION, time, new float[] {0F, 0.18F, 0.6F, 1.05F}, new float[] {0F, -26.18182F, 0F, 0F, -26.18182F, 0F, 0F, -9.818182F, 0F, 0F, 0F, 0F});
        apply(this.parchment_strip_rig_02, Channel.POSITION, time, new float[] {0F, 0.18F, 0.6F, 1.05F}, new float[] {4.6725F, 0F, 0.1472727F, 4.6725F, 0F, 0.1472727F, 2.2428F, 0F, 0.04909091F, 0F, 0F, 0F});
        apply(this.parchment_strip_rig_02, Channel.SCALE, time, new float[] {0F, 1.15F}, new float[] {1F, 1F, 1F, 1F, 1F, 1F});
        apply(this.parchment_strip_rig_03, Channel.ROTATION, time, new float[] {0F, 0.18F, 0.6F, 1.05F}, new float[] {0F, -20.36364F, 0F, 0F, -20.36364F, 0F, 0F, -7.636364F, 0F, 0F, 0F, 0F});
        apply(this.parchment_strip_rig_03, Channel.POSITION, time, new float[] {0F, 0.18F, 0.6F, 1.05F}, new float[] {3.630833F, 0F, 0.1145455F, 3.630833F, 0F, 0.1145455F, 1.7428F, 0F, 0.03818182F, 0F, 0F, 0F});
        apply(this.parchment_strip_rig_03, Channel.SCALE, time, new float[] {0F, 1.15F}, new float[] {1F, 1F, 1F, 1F, 1F, 1F});
        apply(this.parchment_strip_rig_04, Channel.ROTATION, time, new float[] {0F, 0.18F, 0.6F, 1.05F}, new float[] {0F, -14.54545F, 0F, 0F, -14.54545F, 0F, 0F, -5.454545F, 0F, 0F, 0F, 0F});
        apply(this.parchment_strip_rig_04, Channel.POSITION, time, new float[] {0F, 0.18F, 0.6F, 1.05F}, new float[] {2.589167F, 0F, 0.08181818F, 2.589167F, 0F, 0.08181818F, 1.2428F, 0F, 0.02727273F, 0F, 0F, 0F});
        apply(this.parchment_strip_rig_04, Channel.SCALE, time, new float[] {0F, 1.15F}, new float[] {1F, 1F, 1F, 1F, 1F, 1F});
        apply(this.parchment_strip_rig_05, Channel.ROTATION, time, new float[] {0F, 0.18F, 0.6F, 1.05F}, new float[] {0F, -8.727273F, 0F, 0F, -8.727273F, 0F, 0F, -3.272727F, 0F, 0F, 0F, 0F});
        apply(this.parchment_strip_rig_05, Channel.POSITION, time, new float[] {0F, 0.18F, 0.6F, 1.05F}, new float[] {1.5475F, 0F, 0.04909091F, 1.5475F, 0F, 0.04909091F, 0.7428F, 0F, 0.01636364F, 0F, 0F, 0F});
        apply(this.parchment_strip_rig_05, Channel.SCALE, time, new float[] {0F, 1.15F}, new float[] {1F, 1F, 1F, 1F, 1F, 1F});
        apply(this.parchment_strip_rig_06, Channel.ROTATION, time, new float[] {0F, 0.18F, 0.6F, 1.05F}, new float[] {0F, -2.909091F, 0F, 0F, -2.909091F, 0F, 0F, -1.090909F, 0F, 0F, 0F, 0F});
        apply(this.parchment_strip_rig_06, Channel.POSITION, time, new float[] {0F, 0.18F, 0.6F, 1.05F}, new float[] {0.5058333F, 0F, 0.01636364F, 0.5058333F, 0F, 0.01636364F, 0.2428F, 0F, 0.005454545F, 0F, 0F, 0F});
        apply(this.parchment_strip_rig_06, Channel.SCALE, time, new float[] {0F, 1.15F}, new float[] {1F, 1F, 1F, 1F, 1F, 1F});
        apply(this.parchment_strip_rig_07, Channel.ROTATION, time, new float[] {0F, 0.18F, 0.6F, 1.05F}, new float[] {0F, 2.909091F, 0F, 0F, 2.909091F, 0F, 0F, 1.090909F, 0F, 0F, 0F, 0F});
        apply(this.parchment_strip_rig_07, Channel.POSITION, time, new float[] {0F, 0.18F, 0.6F, 1.05F}, new float[] {-0.5358333F, 0F, 0.01636364F, -0.5358333F, 0F, 0.01636364F, -0.2572F, 0F, 0.005454545F, 0F, 0F, 0F});
        apply(this.parchment_strip_rig_07, Channel.SCALE, time, new float[] {0F, 1.15F}, new float[] {1F, 1F, 1F, 1F, 1F, 1F});
        apply(this.parchment_strip_rig_08, Channel.ROTATION, time, new float[] {0F, 0.18F, 0.6F, 1.05F}, new float[] {0F, 8.727273F, 0F, 0F, 8.727273F, 0F, 0F, 3.272727F, 0F, 0F, 0F, 0F});
        apply(this.parchment_strip_rig_08, Channel.POSITION, time, new float[] {0F, 0.18F, 0.6F, 1.05F}, new float[] {-1.5775F, 0F, 0.04909091F, -1.5775F, 0F, 0.04909091F, -0.7572F, 0F, 0.01636364F, 0F, 0F, 0F});
        apply(this.parchment_strip_rig_08, Channel.SCALE, time, new float[] {0F, 1.15F}, new float[] {1F, 1F, 1F, 1F, 1F, 1F});
        apply(this.parchment_strip_rig_09, Channel.ROTATION, time, new float[] {0F, 0.18F, 0.6F, 1.05F}, new float[] {0F, 14.54545F, 0F, 0F, 14.54545F, 0F, 0F, 5.454545F, 0F, 0F, 0F, 0F});
        apply(this.parchment_strip_rig_09, Channel.POSITION, time, new float[] {0F, 0.18F, 0.6F, 1.05F}, new float[] {-2.619167F, 0F, 0.08181818F, -2.619167F, 0F, 0.08181818F, -1.2572F, 0F, 0.02727273F, 0F, 0F, 0F});
        apply(this.parchment_strip_rig_09, Channel.SCALE, time, new float[] {0F, 1.15F}, new float[] {1F, 1F, 1F, 1F, 1F, 1F});
        apply(this.parchment_strip_rig_10, Channel.ROTATION, time, new float[] {0F, 0.18F, 0.6F, 1.05F}, new float[] {0F, 20.36364F, 0F, 0F, 20.36364F, 0F, 0F, 7.636364F, 0F, 0F, 0F, 0F});
        apply(this.parchment_strip_rig_10, Channel.POSITION, time, new float[] {0F, 0.18F, 0.6F, 1.05F}, new float[] {-3.660833F, 0F, 0.1145455F, -3.660833F, 0F, 0.1145455F, -1.7572F, 0F, 0.03818182F, 0F, 0F, 0F});
        apply(this.parchment_strip_rig_10, Channel.SCALE, time, new float[] {0F, 1.15F}, new float[] {1F, 1F, 1F, 1F, 1F, 1F});
        apply(this.parchment_strip_rig_11, Channel.ROTATION, time, new float[] {0F, 0.18F, 0.6F, 1.05F}, new float[] {0F, 26.18182F, 0F, 0F, 26.18182F, 0F, 0F, 9.818182F, 0F, 0F, 0F, 0F});
        apply(this.parchment_strip_rig_11, Channel.POSITION, time, new float[] {0F, 0.18F, 0.6F, 1.05F}, new float[] {-4.7025F, 0F, 0.1472727F, -4.7025F, 0F, 0.1472727F, -2.2572F, 0F, 0.04909091F, 0F, 0F, 0F});
        apply(this.parchment_strip_rig_11, Channel.SCALE, time, new float[] {0F, 1.15F}, new float[] {1F, 1F, 1F, 1F, 1F, 1F});
        apply(this.parchment_strip_rig_12, Channel.ROTATION, time, new float[] {0F, 0.18F, 0.6F, 1.05F}, new float[] {0F, 32F, 0F, 0F, 32F, 0F, 0F, 12F, 0F, 0F, 0F, 0F});
        apply(this.parchment_strip_rig_12, Channel.POSITION, time, new float[] {0F, 0.18F, 0.6F, 0.83333F, 1.05F, 1.16667F}, new float[] {-5.744167F, 0F, 0.18F, -5.744167F, 0F, 0.18F, -2.7572F, 0F, 0.06F, -2.04F, 0F, 0.02F, -1F, 0F, 0F, -1F, 0F, 0F});
        apply(this.parchment_strip_rig_12, Channel.SCALE, time, new float[] {0F, 1.15F}, new float[] {1F, 1F, 1F, 1F, 1F, 1F});
    }

    public void render(PoseStack poseStack, VertexConsumer consumer, int packedLight) {
        this.root.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
    }

    private static void apply(ModelPart part, Channel channel, float time, float[] times, float[] values) {
        float[] value = sample(time, times, values);
        switch (channel) {
            case POSITION -> {
                part.x += value[0];
                part.y -= value[1];
                part.z += value[2];
            }
            case ROTATION -> {
                part.xRot += value[0] * Mth.DEG_TO_RAD;
                part.yRot += value[1] * Mth.DEG_TO_RAD;
                part.zRot += value[2] * Mth.DEG_TO_RAD;
            }
            case SCALE -> {
                part.xScale = value[0];
                part.yScale = value[1];
                part.zScale = value[2];
            }
        }
    }

    private static float[] sample(float time, float[] times, float[] values) {
        if (times.length == 1 || time <= times[0]) {
            return valueAt(values, 0);
        }
        int last = times.length - 1;
        if (time >= times[last]) {
            return valueAt(values, last);
        }
        for (int index = 0; index < last; index++) {
            if (time <= times[index + 1]) {
                float span = times[index + 1] - times[index];
                float alpha = span <= 0.0F ? 1.0F : (time - times[index]) / span;
                alpha = alpha * alpha * (3.0F - 2.0F * alpha);
                int offset = index * 3;
                int nextOffset = offset + 3;
                return new float[] {
                        Mth.lerp(alpha, values[offset], values[nextOffset]),
                        Mth.lerp(alpha, values[offset + 1], values[nextOffset + 1]),
                        Mth.lerp(alpha, values[offset + 2], values[nextOffset + 2])
                };
            }
        }
        return valueAt(values, last);
    }

    private static float[] valueAt(float[] values, int index) {
        int offset = index * 3;
        return new float[] {values[offset], values[offset + 1], values[offset + 2]};
    }

    private enum Channel {
        POSITION,
        ROTATION,
        SCALE
    }
}
