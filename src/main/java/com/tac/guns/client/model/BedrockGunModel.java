package com.tac.guns.client.model;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.tac.guns.api.attachment.AttachmentType;
import com.tac.guns.api.item.IGun;
import com.tac.guns.client.model.bedrock.BedrockPart;
import com.tac.guns.client.model.bedrock.ModelRendererWrapper;
import com.tac.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.tac.guns.client.resource.pojo.model.BedrockVersion;
import com.tac.guns.util.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.List;

import static com.tac.guns.client.model.CommonComponents.*;

public class BedrockGunModel extends BedrockAnimatedModel {
    private static final String IRON_VIEW_NODE = "iron_view";
    private static final String IDLE_VIEW_NODE = "idle_view";
    private static final String REFIT_VIEW_NODE = "refit_view";
    private static final String THIRD_PERSON_HAND_ORIGIN_NODE = "thirdperson_hand";
    private static final String FIXED_ORIGIN_NODE = "fixed";
    private static final String GROUND_ORIGIN_NODE = "ground";
    private static final String SHELL_ORIGIN_NODE = "shell";
    private static final String CONSTRAINT_NODE = "constraint";
    private static final String ATTACHMENT_POS_SUFFIX = "_pos";
    private static final String REFIT_VIEW_PREFIX = "refit_";
    private static final String REFIT_VIEW_SUFFIX = "_view";
    private static final String ROOT_NODE = "root";
    protected final @Nonnull EnumMap<AttachmentType, List<BedrockPart>> refitAttachmentViewPath = Maps.newEnumMap(AttachmentType.class);
    private final EnumMap<AttachmentType, ItemStack> currentAttachmentItem = Maps.newEnumMap(AttachmentType.class);
    // 第一人称机瞄摄像机定位组的路径
    protected @Nullable List<BedrockPart> ironSightPath;
    // 第一人称idle状态摄像机定位组的路径
    protected @Nullable List<BedrockPart> idleSightPath;
    // 第三人称手部物品渲染原点定位组的路径
    protected @Nullable List<BedrockPart> thirdPersonHandOriginPath;
    // 展示框渲染原点定位组的路径
    protected @Nullable List<BedrockPart> fixedOriginPath;
    // 地面实体渲染原点定位组的路径
    protected @Nullable List<BedrockPart> groundOriginPath;
    // 瞄具配件定位组的路径。其他配件不需要存路径，只需要替换渲染。但是瞄具定位组需要用来辅助第一人称瞄准的摄像机定位。
    protected @Nullable List<BedrockPart> scopePosPath;
    // 动画约束组的路径
    protected @Nullable List<BedrockPart> constraintPath;
    // 抛壳子弹的起始位置
    protected @Nullable List<BedrockPart> shellOriginPath;
    protected @Nullable BedrockPart root;
    private boolean renderHand = true;
    private ItemStack currentGunItem;

    public BedrockGunModel(BedrockModelPOJO pojo, BedrockVersion version) {
        super(pojo, version);
        this.setFunctionalRenderer("lefthand_pos", bedrockPart -> (poseStack, transformType, light, overlay) -> {
            if (transformType.firstPerson()) {
                if (!renderHand) {
                    return;
                }
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(180f));
                Matrix3f normal = poseStack.last().normal().copy();
                Matrix4f pose = poseStack.last().pose().copy();
                //和枪械模型共用顶点缓冲的都需要代理到渲染结束后渲染
                this.delegateRender((poseStack1, transformType1, light1, overlay1) -> {
                    PoseStack poseStack2 = new PoseStack();
                    poseStack2.last().normal().mul(normal);
                    poseStack2.last().pose().multiply(pose);
                    RenderHelper.renderFirstPersonArm(Minecraft.getInstance().player, HumanoidArm.LEFT, poseStack2, light1);
                    Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
                });
            }
        });
        this.setFunctionalRenderer("righthand_pos", bedrockPart -> (poseStack, transformType, light, overlay) -> {
            if (transformType.firstPerson()) {
                if (!renderHand) {
                    return;
                }
                poseStack.mulPose(Vector3f.ZP.rotationDegrees(180f));
                Matrix3f normal = poseStack.last().normal().copy();
                Matrix4f pose = poseStack.last().pose().copy();
                //和枪械模型共用顶点缓冲的都需要代理到渲染结束后渲染
                this.delegateRender((poseStack1, transformType1, light1, overlay1) -> {
                    PoseStack poseStack2 = new PoseStack();
                    poseStack2.last().normal().mul(normal);
                    poseStack2.last().pose().multiply(pose);
                    RenderHelper.renderFirstPersonArm(Minecraft.getInstance().player, HumanoidArm.RIGHT, poseStack2, light1);
                    Minecraft.getInstance().renderBuffers().bufferSource().endBatch();
                });
            }
        });
        this.setFunctionalRenderer(BULLET_IN_BARREL, bedrockPart -> {
            // 弹药数大于 1 时渲染
            IGun iGun = IGun.getIGunOrNull(currentGunItem);
            if (iGun != null) {
                int ammoCount = iGun.getCurrentAmmoCount(currentGunItem);
                bedrockPart.visible = ammoCount > 0;
            }
            return null;
        });
        this.setFunctionalRenderer(BULLET_IN_MAG, bedrockPart -> {
            // 弹药数大于 1 时渲染
            IGun iGun = IGun.getIGunOrNull(currentGunItem);
            if (iGun != null) {
                int ammoCount = iGun.getCurrentAmmoCount(currentGunItem);
                bedrockPart.visible = ammoCount > 1;
            }
            return null;
        });
        this.setFunctionalRenderer(BULLET_CHAIN, bedrockPart -> {
            // 弹药数大于 1 时渲染
            IGun iGun = IGun.getIGunOrNull(currentGunItem);
            if (iGun != null) {
                int ammoCount = iGun.getCurrentAmmoCount(currentGunItem);
                bedrockPart.visible = ammoCount > 0;
            }
            return null;
        });
        this.setFunctionalRenderer(MOUNT, bedrockPart -> {
            // 安装瞄具时可见
            ItemStack scopeItem = currentAttachmentItem.get(AttachmentType.SCOPE);
            bedrockPart.visible = (scopeItem != null && !scopeItem.isEmpty());
            return null;
        });
        this.setFunctionalRenderer(CARRY, bedrockPart -> {
            // 未安装瞄具时可见
            ItemStack scopeItem = currentAttachmentItem.get(AttachmentType.SCOPE);
            bedrockPart.visible = (scopeItem == null || scopeItem.isEmpty());
            return null;
        });
        this.setFunctionalRenderer(SIGHT_FOLDED, bedrockPart -> {
            // 安装瞄具时可见
            ItemStack scopeItem = currentAttachmentItem.get(AttachmentType.SCOPE);
            bedrockPart.visible = (scopeItem != null && !scopeItem.isEmpty());
            return null;
        });
        this.setFunctionalRenderer(SIGHT, bedrockPart -> {
            // 未安装瞄具时可见
            ItemStack scopeItem = currentAttachmentItem.get(AttachmentType.SCOPE);
            bedrockPart.visible = (scopeItem == null || scopeItem.isEmpty());
            return null;
        });
        this.setFunctionalRenderer(MAG_EXTENDED_1, bedrockPart -> {
            //TODO 安装一级扩容弹匣时可见
            return null;
        });
        this.setFunctionalRenderer(MAG_EXTENDED_2, bedrockPart -> {
            //TODO 安装二级扩容弹匣时可见
            return null;
        });
        this.setFunctionalRenderer(MAG_EXTENDED_3, bedrockPart -> {
            //TODO 安装三级扩容弹匣时可见
            return null;
        });
        this.setFunctionalRenderer(MAG_STANDARD, bedrockPart -> {
            //TODO 未安装扩容弹匣时可见
            return null;
        });

        ironSightPath = getPath(modelMap.get(IRON_VIEW_NODE));
        idleSightPath = getPath(modelMap.get(IDLE_VIEW_NODE));
        thirdPersonHandOriginPath = getPath(modelMap.get(THIRD_PERSON_HAND_ORIGIN_NODE));
        fixedOriginPath = getPath(modelMap.get(FIXED_ORIGIN_NODE));
        groundOriginPath = getPath(modelMap.get(GROUND_ORIGIN_NODE));
        shellOriginPath = getPath(modelMap.get(SHELL_ORIGIN_NODE));
        scopePosPath = getPath(modelMap.get(AttachmentType.SCOPE.name().toLowerCase() + ATTACHMENT_POS_SUFFIX));
        constraintPath = getPath(modelMap.get(CONSTRAINT_NODE));
        ModelRendererWrapper rootWrapper = modelMap.get(ROOT_NODE);
        if (rootWrapper != null) {
            root = rootWrapper.getModelRenderer();
        }

        // 缓存改装UI下各个配件的特写视角定位组
        for (AttachmentType type : AttachmentType.values()) {
            if (type == AttachmentType.NONE) {
                refitAttachmentViewPath.put(type, getPath(modelMap.get(REFIT_VIEW_NODE)));
                continue;
            }
            String nodeName = REFIT_VIEW_PREFIX + type.name().toLowerCase() + REFIT_VIEW_SUFFIX;
            refitAttachmentViewPath.put(type, getPath(modelMap.get(nodeName)));
        }

        // 准备各个配件的渲染
        for (AttachmentType type : AttachmentType.values()) {
            if (type == AttachmentType.NONE || type == AttachmentType.SCOPE) { // 瞄具的渲染需要提前。
                continue;
            }
            String nodeName = type.name().toLowerCase() + ATTACHMENT_POS_SUFFIX;
            this.setFunctionalRenderer(nodeName, bedrockPart -> {
                bedrockPart.visible = false;
                return (poseStack, transformType, light, overlay) -> {
                    ItemStack attachmentItem = currentAttachmentItem.get(type);
                    if (attachmentItem != null && !attachmentItem.isEmpty()) {
                        Matrix3f normal = poseStack.last().normal().copy();
                        Matrix4f pose = poseStack.last().pose().copy();
                        //和枪械模型共用顶点缓冲的都需要代理到渲染结束后渲染
                        this.delegateRender((poseStack1, transformType1, light1, overlay1) -> {
                            PoseStack poseStack2 = new PoseStack();
                            poseStack2.last().normal().mul(normal);
                            poseStack2.last().pose().multiply(pose);
                            MultiBufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                            // 直接调用配件的 ISTER 进行渲染
                            Minecraft.getInstance().getItemRenderer().renderStatic(attachmentItem, ItemTransforms.TransformType.NONE,
                                    light, overlay, poseStack2, bufferSource, 0);
                        });
                    }
                };
            });
        }
    }

    @Nullable
    public List<BedrockPart> getIronSightPath() {
        return ironSightPath;
    }

    @Nullable
    public List<BedrockPart> getIdleSightPath() {
        return idleSightPath;
    }

    @Nullable
    public List<BedrockPart> getThirdPersonHandOriginPath() {
        return thirdPersonHandOriginPath;
    }

    @Nullable
    public List<BedrockPart> getFixedOriginPath() {
        return fixedOriginPath;
    }

    @Nullable
    public List<BedrockPart> getGroundOriginPath() {
        return groundOriginPath;
    }

    @Nullable
    public List<BedrockPart> getShellOriginPath() {
        return shellOriginPath;
    }

    @Nullable
    public List<BedrockPart> getScopePosPath() {
        return scopePosPath;
    }

    @Nullable
    public List<BedrockPart> getConstraintPath() {
        return constraintPath;
    }

    @Nullable
    public List<BedrockPart> getRefitAttachmentViewPath(AttachmentType type) {
        return refitAttachmentViewPath.get(type);
    }

    @Nullable
    public BedrockPart getRootNode() {
        return root;
    }

    public boolean getRenderHand() {
        return renderHand;
    }

    public void setRenderHand(boolean renderHand) {
        this.renderHand = renderHand;
    }

    public void render(PoseStack matrixStack, ItemStack gunItem, ItemTransforms.TransformType transformType, RenderType renderType, int light, int overlay) {
        IGun iGun = IGun.getIGunOrNull(gunItem);
        if (iGun == null) {
            return;
        }
        currentGunItem = gunItem;
        // 更新配件物品的缓存，以供渲染使用
        for (AttachmentType type : AttachmentType.values()) {
            if (type == AttachmentType.NONE) {
                continue;
            }
            ItemStack attachmentItem = iGun.getAttachment(gunItem, type);
            currentAttachmentItem.put(type, attachmentItem);
        }
        // 镜子需要先渲染，写入模板值
        ItemStack attachmentItem = currentAttachmentItem.get(AttachmentType.SCOPE);
        if (scopePosPath != null && attachmentItem != null && !attachmentItem.isEmpty()) {
            matrixStack.pushPose();
            for (BedrockPart bedrockPart : scopePosPath) {
                bedrockPart.translateAndRotateAndScale(matrixStack);
            }
            MultiBufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
            Minecraft.getInstance().getItemRenderer().renderStatic(attachmentItem, ItemTransforms.TransformType.NONE,
                    light, overlay, matrixStack, bufferSource, 0);
            matrixStack.popPose();
            RenderHelper.enableItemEntityStencilTest();
        }
        RenderSystem.stencilFunc(GL11.GL_EQUAL, 0, 0xFF);
        RenderSystem.stencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
        super.render(matrixStack, transformType, renderType, light, overlay);
        RenderHelper.disableItemEntityStencilTest();
    }
}
