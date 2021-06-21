package me.lizardofoz.searchlight.block;

import me.lizardofoz.searchlight.util.SearchlightUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BeaconBlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

@Environment(EnvType.CLIENT)
public class SearchlightBlockRenderer extends BlockEntityRenderer<SearchlightBlockEntity>
{
    @SuppressWarnings("deprecation")
    protected static final SpriteIdentifier SEARCHLIGHT_BODY_TEXTURE
            = new SpriteIdentifier(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("searchlight", "block/searchlight"));

    protected static final Identifier SEARCHLIGHT_BEAM = new Identifier("searchlight", "textures/block/searchlight_beam.png");

    protected static final int MAX_LIGHT = LightmapTextureManager.pack(15, 15);
    protected static final int MAX_OVERLAY = OverlayTexture.packUv(15, 15);

    protected static final Vector3f CEILING_PIVOT = new Vector3f(8, 10, 8);
    protected static final Vector3f FLOOR_PIVOT = new Vector3f(8, 6, 8);
    protected static final Vector3f NORTH_PIVOT = new Vector3f(8, 8, 12);
    protected static final Vector3f SOUTH_PIVOT = new Vector3f(8, 8, 4);
    protected static final Vector3f WEST_PIVOT = new Vector3f(12, 8, 8);
    protected static final Vector3f EAST_PIVOT = new Vector3f(4, 8, 8);

    protected final ModelPart onWallBody = new ModelPart(32, 32, 0, 0);
    protected final ModelPart onWallLightFace = new ModelPart(32, 32, 0, 23);
    protected final ModelPart onFloorBody = new ModelPart(32, 32, 0, 0);
    protected final ModelPart ofFloorLightFace = new ModelPart(32, 32, 0, 23);

    public SearchlightBlockRenderer(BlockEntityRenderDispatcher dispatcher)
    {
        super(dispatcher);

        //The Searchlight's model has a luminescent part in the front that's rendered separately with a very high brightness
        //Also, the models (positions of cuboids) are slightly different when placed on walls in comp. to floor/ceiling

        onWallBody.addCuboid(-3, -6, -3, 6, 7, 6);
        ModelPart wallFrontPart = new ModelPart(32, 32, 0, 13);
        wallFrontPart.addCuboid(4, 4, 4, 8, 2, 8);
        wallFrontPart.setPivot(-8, -12, -8);
        onWallBody.addChild(wallFrontPart);
        onWallLightFace.addCuboid(-4, -8, -4, 8, 1, 8);

        onFloorBody.addCuboid(-3, -4, -3, 6, 7, 6);
        ModelPart floorFrontPart = new ModelPart(32, 32, 0, 13);
        floorFrontPart.addCuboid(4, 4, 4, 8, 2, 8);
        floorFrontPart.setPivot(-8, -10, -8);
        onFloorBody.addChild(floorFrontPart);
        ofFloorLightFace.addCuboid(-4, -6, -4, 8, 1, 8);
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
        Vector3f pivot = getModelPivot(blockEntity);
        Vec3d direction = blockEntity.getBeamDirection();
        VertexConsumer vertexConsumer = SEARCHLIGHT_BODY_TEXTURE.getVertexConsumer(vertexConsumerProvider, RenderLayer::getEntityCutoutNoCull);

        boolean isOnWall = blockEntity.getCachedState().get(WallMountedBlock.FACE) == WallMountLocation.WALL;
        ModelPart body = isOnWall ? onWallBody : onFloorBody;
        ModelPart lightFace = isOnWall ? onWallLightFace : ofFloorLightFace;

        body.setPivot(pivot.getX(), pivot.getY(), pivot.getZ());
        body.yaw = (float) MathHelper.atan2(direction.x, direction.z);
        body.pitch = (float) (MathHelper.atan2(Math.sqrt(direction.z * direction.z + direction.x * direction.x), direction.y) + Math.PI);
        body.render(matrixStack, vertexConsumer, light, overlay);

        //This portion renders the luminous front surface of the searchlight
        lightFace.setPivot(pivot.getX(), pivot.getY(), pivot.getZ());
        lightFace.yaw = body.yaw;
        lightFace.pitch = body.pitch;
        lightFace.render(matrixStack, vertexConsumer, MAX_LIGHT, MAX_OVERLAY);

        if (SearchlightUtil.displayBeams() && blockEntity.getLightSourcePos() != null)
        {
            int distance = (int) Math.sqrt(blockEntity.getLightSourcePos().getSquaredDistance(blockEntity.getPos())) + 1;
            drawBeam(pivot, body.yaw, body.pitch, distance, matrixStack, vertexConsumerProvider);
        }
    }

    protected void drawBeam(Vector3f pivot, float yaw, float pitch, int distance, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider)
    {
        matrixStack.push();

        matrixStack.translate(pivot.getX() / 16, pivot.getY() / 16, pivot.getZ() / 16);
        matrixStack.multiply(Vector3f.POSITIVE_Y.getRadialQuaternion(yaw));
        matrixStack.multiply(Vector3f.POSITIVE_X.getRadialQuaternion((float) (Math.PI + pitch)));
        matrixStack.translate(-0.5, 0.35, -0.5);

        BeaconBlockEntityRenderer.renderLightBeam(matrixStack, vertexConsumerProvider, SEARCHLIGHT_BEAM,
                0, 1, 0, 0, distance, new float[]{1, 1, 1}, 0, 0.25F);

        matrixStack.pop();
    }

    protected Vector3f getModelPivot(SearchlightBlockEntity blockEntity)
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