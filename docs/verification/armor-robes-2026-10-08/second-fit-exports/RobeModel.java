package com.quzzar.vestige.equipment.client;

import com.quzzar.vestige.VestigeMainMod;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.world.entity.LivingEntity;
import java.util.List;

/** Layered mage armor with a fitted torso, folded cuffs and a split lower coat. */
final class RobeModel extends HumanoidModel<LivingEntity> {
    static final ModelLayerLocation WIDE = new ModelLayerLocation(VestigeMainMod.location("robe"), "wide");
    static final ModelLayerLocation SLIM = new ModelLayerLocation(VestigeMainMod.location("robe"), "slim");
    private final ModelPart rightCoat;
    private final ModelPart leftCoat;

    RobeModel(ModelPart root) {
        super(root);
        rightCoat = root.getChild("right_coat");
        leftCoat = root.getChild("left_coat");
    }

    static LayerDefinition layer(boolean slim) {
        var cloth = new CubeDeformation(.35f, 0, .35f);
        var mesh = HumanoidModel.createMesh(cloth, 0);
        var root = mesh.getRoot();
        int armWidth = slim ? 3 : 4;
        int armV = slim ? 48 : 16;
        var rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, armV)
                .addBox(slim ? -2 : -3, -2, -2, armWidth, 12, 4, cloth), PartPose.offset(-5, 2, 0));
        var leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, armV).mirror()
                .addBox(-1, -2, -2, armWidth, 12, 4, cloth), PartPose.offset(5, 2, 0));
        int shoulderU = slim ? 16 : 0;
        int cuffU = slim ? 16 : 0;
        rightArm.addOrReplaceChild("shoulder", CubeListBuilder.create().texOffs(shoulderU, 0)
                .addBox(slim ? -2 : -3, -2.15f, -2, armWidth, 3, 4, new CubeDeformation(.5f, 0, .5f)), PartPose.ZERO);
        leftArm.addOrReplaceChild("shoulder", CubeListBuilder.create().texOffs(shoulderU, 0).mirror()
                .addBox(-1, -2.15f, -2, armWidth, 3, 4, new CubeDeformation(.5f, 0, .5f)), PartPose.ZERO);
        rightArm.addOrReplaceChild("cuff", CubeListBuilder.create().texOffs(cuffU, 48)
                .addBox(slim ? -2 : -3, 7, -2, armWidth, 3, 4, new CubeDeformation(.55f, .05f, .55f)), PartPose.ZERO);
        leftArm.addOrReplaceChild("cuff", CubeListBuilder.create().texOffs(cuffU, 48).mirror()
                .addBox(-1, 7, -2, armWidth, 3, 4, new CubeDeformation(.55f, .05f, .55f)), PartPose.ZERO);
        // These are chest-slot garment parts. Native leggings and boots remain
        // independent, while the two coat halves follow their own leg poses.
        root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9f, 12, 0));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9f, 12, 0));
        root.addOrReplaceChild("right_coat", CubeListBuilder.create().texOffs(0, 32)
                .addBox(-3, 0, -2.65f, 5, 8, 5, new CubeDeformation(.1f, 0, .1f)), PartPose.offset(-1.9f, 12, 0));
        root.addOrReplaceChild("left_coat", CubeListBuilder.create().texOffs(20, 32)
                .addBox(-2, 0, -2.63f, 5, 8, 5, new CubeDeformation(.1f, 0, .1f)), PartPose.offset(1.9f, 12, 0));
        root.getChild("body").addOrReplaceChild("collar", CubeListBuilder.create().texOffs(32, 0)
                .addBox(-4, 0, -2, 8, 2, 4, new CubeDeformation(.45f, .05f, .45f)), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return List.of(body, rightArm, leftArm, rightCoat, leftCoat, hat);
    }

    void prepare() {
        rightCoat.copyFrom(rightLeg);
        leftCoat.copyFrom(leftLeg);
        // Overlap the waist and let broad cloth panels move less than trousers.
        // Their slight depth offset avoids coplanar faces along the center join.
        rightCoat.y -= .25f;
        leftCoat.y -= .23f;
        rightCoat.xRot *= .35f;
        leftCoat.xRot *= .35f;
        rightCoat.visible = leftCoat.visible = body.visible;
    }
}
