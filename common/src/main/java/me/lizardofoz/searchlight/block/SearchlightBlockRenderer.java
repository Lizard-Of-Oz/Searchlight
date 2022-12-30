package me.lizardofoz.searchlight.block;

import me.lizardofoz.searchlight.util.SearchlightUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.client.model.*;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class SearchlightBlockRenderer implements BlockEntityRenderer<SearchlightBlockEntity>
{
    protected static final Identifier SEARCHLIGHT_BODY_TEXTURE = new Identifier("searchlight", "textures/block/searchlight.png");
    protected static final Identifier SEARCHLIGHT_BEAM = new Identifier("searchlight", "textures/block/searchlight_beam.png");

    protected static final int MAX_LIGHT = LightmapTextureManager.pack(15, 15);
    protected static final int NO_LIGHT = LightmapTextureManager.pack(0, 0);
    protected static final int MAX_OVERLAY = OverlayTexture.packUv(15, 15);

    protected static final Vec3d CEILING_PIVOT = new Vec3d(8, 10, 8);
    protected static final Vec3d FLOOR_PIVOT = new Vec3d(8, 6, 8);
    protected static final Vec3d NORTH_PIVOT = new Vec3d(8, 8, 12);
    protected static final Vec3d SOUTH_PIVOT = new Vec3d(8, 8, 4);
    protected static final Vec3d WEST_PIVOT = new Vec3d(12, 8, 8);
    protected static final Vec3d EAST_PIVOT = new Vec3d(4, 8, 8);

    protected final ModelPart onWallBody;
    protected final ModelPart onWallLightFace;
    protected final ModelPart onFloorBody;
    protected final ModelPart onFloorLightFace;

    public SearchlightBlockRenderer(BlockEntityRendererFactory.Context context)
    {
        //The Searchlight's model has a luminescent part in the front that's rendered separately with a very high brightness
        //Also, the models (positions of cuboids) are slightly different when placed on walls in comp. to floor/ceiling

        ModelPartData root = new ModelData().getRoot();
        root.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-3, -6, -3, 6, 7, 6), ModelTransform.NONE);
        root.addChild("front", ModelPartBuilder.create().uv(0, 13).cuboid(4, 4, 4, 8, 2, 8), ModelTransform.pivot(-8, -12, -8));
        onWallBody = root.createPart(32, 32);
        onWallLightFace = new ModelData().getRoot()
                .addChild("face", ModelPartBuilder.create().uv(0, 23).cuboid(-4, -8, -4, 8, 1, 8), ModelTransform.NONE)
                .createPart(32, 32);

        root = new ModelData().getRoot();
        root.addChild("body", ModelPartBuilder.create().uv(0, 0).cuboid(-3, -4, -3, 6, 7, 6), ModelTransform.NONE);
        root.addChild("front", ModelPartBuilder.create().uv(0, 13).cuboid(4, 4, 4, 8, 2, 8), ModelTransform.pivot(-8, -10, -8));
        onFloorBody = root.createPart(32, 32);
        onFloorLightFace = new ModelData().getRoot()
                .addChild("face", ModelPartBuilder.create().uv(0, 23).cuboid(-4, -6, -4, 8, 1, 8), ModelTransform.NONE)
                .createPart(32, 32);
    }

    @Environment(EnvType.CLIENT)
    public int getRenderDistance()
    {
        return SearchlightUtil.displayBeams() ? 256 : BlockEntityRenderer.super.getRenderDistance();
    }

    @Override
    public boolean rendersOutsideBoundingBox(SearchlightBlockEntity beaconBlockEntity)
    {
        return true;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public void render(SearchlightBlockEntity blockEntity, float tickDelta, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int light, int overlay)
    {
        Vec3d pivot = getModelPivot(blockEntity);
        Vec3d direction = blockEntity.getBeamDirection();
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(RenderLayer.getEntityCutoutNoCull(SEARCHLIGHT_BODY_TEXTURE, true));

        boolean isOnWall = blockEntity.getCachedState().get(WallMountedBlock.FACE) == WallMountLocation.WALL;
        ModelPart body = isOnWall ? onWallBody : onFloorBody;
        ModelPart lightFace = isOnWall ? onWallLightFace : onFloorLightFace;

        body.setPivot((float) pivot.getX(), (float) pivot.getY(), (float) pivot.getZ());
        body.yaw = (float) MathHelper.atan2(direction.x, direction.z);
        body.pitch = (float) (MathHelper.atan2(Math.sqrt(direction.z * direction.z + direction.x * direction.x), direction.y) + Math.PI);
        body.render(matrixStack, vertexConsumer, light, overlay);

        boolean shouldRenderLight = blockEntity.getLightSourcePos() != null && !blockEntity.getCachedState().get(SearchlightBlock.POWERED);
        //This portion renders the luminous front surface of the searchlight
        lightFace.setPivot((float) pivot.getX(), (float) pivot.getY(), (float) pivot.getZ());
        lightFace.yaw = body.yaw;
        lightFace.pitch = body.pitch;
        lightFace.render(matrixStack, vertexConsumer, shouldRenderLight ? MAX_LIGHT : NO_LIGHT, MAX_OVERLAY);

        if (SearchlightUtil.displayBeams() && blockEntity.getLightSourcePos() != null)
        {
            int distance = (int) Math.sqrt(blockEntity.getLightSourcePos().getSquaredDistance(blockEntity.getPos())) + 1;
            drawBeam(pivot, body.yaw, body.pitch, distance, matrixStack, vertexConsumerProvider);
        }
    }

    protected void drawBeam(Vec3d pivot, float yaw, float pitch, int distance, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider)
    {
        matrixStack.push();

        matrixStack.translate(pivot.getX() / 16, pivot.getY() / 16, pivot.getZ() / 16);
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotation(yaw));
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotation((float) (Math.PI + pitch)));
        matrixStack.translate(-0.5, 0.35, -0.5);

        BeaconBlockEntityRenderer.renderBeam(matrixStack, vertexConsumerProvider, SEARCHLIGHT_BEAM,
                0, 1, 0, 0, distance, new float[]{1, 1, 1}, 0, 0.25F);

        matrixStack.pop();
    }

    protected Vec3d getModelPivot(SearchlightBlockEntity blockEntity)
    {
        Direction direction = SearchlightUtil.getDirection(blockEntity.getCachedState());
        if (direction == Direction.UP)
            return FLOOR_PIVOT;
        else if (direction == Direction.DOWN)
            return CEILING_PIVOT;
        else if (direction == Direction.EAST)
            return EAST_PIVOT;
        else if (direction == Direction.WEST)
            return WEST_PIVOT;
        else if (direction == Direction.SOUTH)
            return SOUTH_PIVOT;
        return NORTH_PIVOT;
    }
}