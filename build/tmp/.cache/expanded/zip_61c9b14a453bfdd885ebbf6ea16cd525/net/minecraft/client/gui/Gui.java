package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.AttackIndicatorStatus;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.gui.components.SubtitleOverlay;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PlayerRideableJumping;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class Gui {
    protected static final ResourceLocation CROSSHAIR_SPRITE = new ResourceLocation("hud/crosshair");
    protected static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE = new ResourceLocation("hud/crosshair_attack_indicator_full");
    protected static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE = new ResourceLocation("hud/crosshair_attack_indicator_background");
    protected static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE = new ResourceLocation("hud/crosshair_attack_indicator_progress");
    protected static final ResourceLocation EFFECT_BACKGROUND_AMBIENT_SPRITE = new ResourceLocation("hud/effect_background_ambient");
    protected static final ResourceLocation EFFECT_BACKGROUND_SPRITE = new ResourceLocation("hud/effect_background");
    protected static final ResourceLocation HOTBAR_SPRITE = new ResourceLocation("hud/hotbar");
    protected static final ResourceLocation HOTBAR_SELECTION_SPRITE = new ResourceLocation("hud/hotbar_selection");
    protected static final ResourceLocation HOTBAR_OFFHAND_LEFT_SPRITE = new ResourceLocation("hud/hotbar_offhand_left");
    protected static final ResourceLocation HOTBAR_OFFHAND_RIGHT_SPRITE = new ResourceLocation("hud/hotbar_offhand_right");
    protected static final ResourceLocation HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE = new ResourceLocation("hud/hotbar_attack_indicator_background");
    protected static final ResourceLocation HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE = new ResourceLocation("hud/hotbar_attack_indicator_progress");
    protected static final ResourceLocation JUMP_BAR_BACKGROUND_SPRITE = new ResourceLocation("hud/jump_bar_background");
    protected static final ResourceLocation JUMP_BAR_COOLDOWN_SPRITE = new ResourceLocation("hud/jump_bar_cooldown");
    protected static final ResourceLocation JUMP_BAR_PROGRESS_SPRITE = new ResourceLocation("hud/jump_bar_progress");
    protected static final ResourceLocation EXPERIENCE_BAR_BACKGROUND_SPRITE = new ResourceLocation("hud/experience_bar_background");
    protected static final ResourceLocation EXPERIENCE_BAR_PROGRESS_SPRITE = new ResourceLocation("hud/experience_bar_progress");
    protected static final ResourceLocation ARMOR_EMPTY_SPRITE = new ResourceLocation("hud/armor_empty");
    protected static final ResourceLocation ARMOR_HALF_SPRITE = new ResourceLocation("hud/armor_half");
    protected static final ResourceLocation ARMOR_FULL_SPRITE = new ResourceLocation("hud/armor_full");
    protected static final ResourceLocation FOOD_EMPTY_HUNGER_SPRITE = new ResourceLocation("hud/food_empty_hunger");
    protected static final ResourceLocation FOOD_HALF_HUNGER_SPRITE = new ResourceLocation("hud/food_half_hunger");
    protected static final ResourceLocation FOOD_FULL_HUNGER_SPRITE = new ResourceLocation("hud/food_full_hunger");
    protected static final ResourceLocation FOOD_EMPTY_SPRITE = new ResourceLocation("hud/food_empty");
    protected static final ResourceLocation FOOD_HALF_SPRITE = new ResourceLocation("hud/food_half");
    protected static final ResourceLocation FOOD_FULL_SPRITE = new ResourceLocation("hud/food_full");
    protected static final ResourceLocation AIR_SPRITE = new ResourceLocation("hud/air");
    protected static final ResourceLocation AIR_BURSTING_SPRITE = new ResourceLocation("hud/air_bursting");
    protected static final ResourceLocation HEART_VEHICLE_CONTAINER_SPRITE = new ResourceLocation("hud/heart/vehicle_container");
    protected static final ResourceLocation HEART_VEHICLE_FULL_SPRITE = new ResourceLocation("hud/heart/vehicle_full");
    protected static final ResourceLocation HEART_VEHICLE_HALF_SPRITE = new ResourceLocation("hud/heart/vehicle_half");
    protected static final ResourceLocation VIGNETTE_LOCATION = new ResourceLocation("textures/misc/vignette.png");
    protected static final ResourceLocation PUMPKIN_BLUR_LOCATION = new ResourceLocation("textures/misc/pumpkinblur.png");
    protected static final ResourceLocation SPYGLASS_SCOPE_LOCATION = new ResourceLocation("textures/misc/spyglass_scope.png");
    protected static final ResourceLocation POWDER_SNOW_OUTLINE_LOCATION = new ResourceLocation("textures/misc/powder_snow_outline.png");
    private static final Comparator<PlayerScoreEntry> SCORE_DISPLAY_ORDER = Comparator.comparing(PlayerScoreEntry::value)
        .reversed()
        .thenComparing(PlayerScoreEntry::owner, String.CASE_INSENSITIVE_ORDER);
    protected static final Component DEMO_EXPIRED_TEXT = Component.translatable("demo.demoExpired");
    protected static final Component SAVING_TEXT = Component.translatable("menu.savingLevel");
    protected static final int COLOR_WHITE = 16777215;
    protected static final float MIN_CROSSHAIR_ATTACK_SPEED = 5.0F;
    protected static final int NUM_HEARTS_PER_ROW = 10;
    protected static final int LINE_HEIGHT = 10;
    protected static final String SPACER = ": ";
    protected static final float PORTAL_OVERLAY_ALPHA_MIN = 0.2F;
    protected static final int HEART_SIZE = 9;
    protected static final int HEART_SEPARATION = 8;
    protected static final float AUTOSAVE_FADE_SPEED_FACTOR = 0.2F;
    protected final RandomSource random = RandomSource.create();
    protected final Minecraft minecraft;
    protected final ItemRenderer itemRenderer;
    protected final ChatComponent chat;
    protected int tickCount;
    @Nullable
    protected Component overlayMessageString;
    protected int overlayMessageTime;
    protected boolean animateOverlayMessageColor;
    protected boolean chatDisabledByPlayerShown;
    public float vignetteBrightness = 1.0F;
    protected int toolHighlightTimer;
    protected ItemStack lastToolHighlight = ItemStack.EMPTY;
    protected final DebugScreenOverlay debugOverlay;
    protected final SubtitleOverlay subtitleOverlay;
    protected final SpectatorGui spectatorGui;
    protected final PlayerTabOverlay tabList;
    protected final BossHealthOverlay bossOverlay;
    protected int titleTime;
    @Nullable
    protected Component title;
    @Nullable
    protected Component subtitle;
    protected int titleFadeInTime;
    protected int titleStayTime;
    protected int titleFadeOutTime;
    protected int lastHealth;
    protected int displayHealth;
    protected long lastHealthTime;
    protected long healthBlinkTime;
    protected int screenWidth;
    protected int screenHeight;
    protected float autosaveIndicatorValue;
    protected float lastAutosaveIndicatorValue;
    protected float scopeScale;

    public Gui(Minecraft p_232355_, ItemRenderer p_232356_) {
        this.minecraft = p_232355_;
        this.itemRenderer = p_232356_;
        this.debugOverlay = new DebugScreenOverlay(p_232355_);
        this.spectatorGui = new SpectatorGui(p_232355_);
        this.chat = new ChatComponent(p_232355_);
        this.tabList = new PlayerTabOverlay(p_232355_, this);
        this.bossOverlay = new BossHealthOverlay(p_232355_);
        this.subtitleOverlay = new SubtitleOverlay(p_232355_);
        this.resetTitleTimes();
    }

    public void resetTitleTimes() {
        this.titleFadeInTime = 10;
        this.titleStayTime = 70;
        this.titleFadeOutTime = 20;
    }

    public void render(GuiGraphics p_282884_, float p_282611_) {
        Window window = this.minecraft.getWindow();
        this.screenWidth = p_282884_.guiWidth();
        this.screenHeight = p_282884_.guiHeight();
        Font font = this.getFont();
        RenderSystem.enableBlend();
        if (Minecraft.useFancyGraphics()) {
            this.renderVignette(p_282884_, this.minecraft.getCameraEntity());
        } else {
            RenderSystem.enableDepthTest();
        }

        float f = this.minecraft.getDeltaFrameTime();
        this.scopeScale = Mth.lerp(0.5F * f, this.scopeScale, 1.125F);
        if (this.minecraft.options.getCameraType().isFirstPerson()) {
            if (this.minecraft.player.isScoping()) {
                this.renderSpyglassOverlay(p_282884_, this.scopeScale);
            } else {
                this.scopeScale = 0.5F;
                ItemStack itemstack = this.minecraft.player.getInventory().getArmor(3);
                if (itemstack.is(Blocks.CARVED_PUMPKIN.asItem())) {
                    this.renderTextureOverlay(p_282884_, PUMPKIN_BLUR_LOCATION, 1.0F);
                }
            }
        }

        if (this.minecraft.player.getTicksFrozen() > 0) {
            this.renderTextureOverlay(p_282884_, POWDER_SNOW_OUTLINE_LOCATION, this.minecraft.player.getPercentFrozen());
        }

        float f1 = Mth.lerp(p_282611_, this.minecraft.player.oSpinningEffectIntensity, this.minecraft.player.spinningEffectIntensity);
        if (f1 > 0.0F && !this.minecraft.player.hasEffect(MobEffects.CONFUSION)) {
            this.renderPortalOverlay(p_282884_, f1);
        }

        if (this.minecraft.gameMode.getPlayerMode() == GameType.SPECTATOR) {
            this.spectatorGui.renderHotbar(p_282884_);
        } else if (!this.minecraft.options.hideGui) {
            this.renderHotbar(p_282611_, p_282884_);
        }

        if (!this.minecraft.options.hideGui) {
            RenderSystem.enableBlend();
            this.renderCrosshair(p_282884_);
            this.minecraft.getProfiler().push("bossHealth");
            this.bossOverlay.render(p_282884_);
            this.minecraft.getProfiler().pop();
            if (this.minecraft.gameMode.canHurtPlayer()) {
                this.renderPlayerHealth(p_282884_);
            }

            this.renderVehicleHealth(p_282884_);
            RenderSystem.disableBlend();
            int i = this.screenWidth / 2 - 91;
            PlayerRideableJumping playerrideablejumping = this.minecraft.player.jumpableVehicle();
            if (playerrideablejumping != null) {
                this.renderJumpMeter(playerrideablejumping, p_282884_, i);
            } else if (this.minecraft.gameMode.hasExperience()) {
                this.renderExperienceBar(p_282884_, i);
            }

            if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR) {
                this.renderSelectedItemName(p_282884_);
            } else if (this.minecraft.player.isSpectator()) {
                this.spectatorGui.renderTooltip(p_282884_);
            }
        }

        if (this.minecraft.player.getSleepTimer() > 0) {
            this.minecraft.getProfiler().push("sleep");
            float f2 = (float)this.minecraft.player.getSleepTimer();
            float f5 = f2 / 100.0F;
            if (f5 > 1.0F) {
                f5 = 1.0F - (f2 - 100.0F) / 10.0F;
            }

            int j = (int)(220.0F * f5) << 24 | 1052704;
            p_282884_.fill(RenderType.guiOverlay(), 0, 0, this.screenWidth, this.screenHeight, j);
            this.minecraft.getProfiler().pop();
        }

        if (this.minecraft.isDemo()) {
            this.renderDemoOverlay(p_282884_);
        }

        this.renderEffects(p_282884_);
        if (this.debugOverlay.showDebugScreen()) {
            this.debugOverlay.render(p_282884_);
        }

        if (!this.minecraft.options.hideGui) {
            if (this.overlayMessageString != null && this.overlayMessageTime > 0) {
                this.minecraft.getProfiler().push("overlayMessage");
                float f3 = (float)this.overlayMessageTime - p_282611_;
                int j1 = (int)(f3 * 255.0F / 20.0F);
                if (j1 > 255) {
                    j1 = 255;
                }

                if (j1 > 8) {
                    p_282884_.pose().pushPose();
                    p_282884_.pose().translate((float)(this.screenWidth / 2), (float)(this.screenHeight - 68), 0.0F);
                    int l1 = 16777215;
                    if (this.animateOverlayMessageColor) {
                        l1 = Mth.hsvToRgb(f3 / 50.0F, 0.7F, 0.6F) & 16777215;
                    }

                    int k = j1 << 24 & 0xFF000000;
                    int l = font.width(this.overlayMessageString);
                    this.drawBackdrop(p_282884_, font, -4, l, 16777215 | k);
                    p_282884_.drawString(font, this.overlayMessageString, -l / 2, -4, l1 | k);
                    p_282884_.pose().popPose();
                }

                this.minecraft.getProfiler().pop();
            }

            if (this.title != null && this.titleTime > 0) {
                this.minecraft.getProfiler().push("titleAndSubtitle");
                float f4 = (float)this.titleTime - p_282611_;
                int k1 = 255;
                if (this.titleTime > this.titleFadeOutTime + this.titleStayTime) {
                    float f6 = (float)(this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime) - f4;
                    k1 = (int)(f6 * 255.0F / (float)this.titleFadeInTime);
                }

                if (this.titleTime <= this.titleFadeOutTime) {
                    k1 = (int)(f4 * 255.0F / (float)this.titleFadeOutTime);
                }

                k1 = Mth.clamp(k1, 0, 255);
                if (k1 > 8) {
                    p_282884_.pose().pushPose();
                    p_282884_.pose().translate((float)(this.screenWidth / 2), (float)(this.screenHeight / 2), 0.0F);
                    RenderSystem.enableBlend();
                    p_282884_.pose().pushPose();
                    p_282884_.pose().scale(4.0F, 4.0F, 4.0F);
                    int i2 = k1 << 24 & 0xFF000000;
                    int j2 = font.width(this.title);
                    this.drawBackdrop(p_282884_, font, -10, j2, 16777215 | i2);
                    p_282884_.drawString(font, this.title, -j2 / 2, -10, 16777215 | i2);
                    p_282884_.pose().popPose();
                    if (this.subtitle != null) {
                        p_282884_.pose().pushPose();
                        p_282884_.pose().scale(2.0F, 2.0F, 2.0F);
                        int k2 = font.width(this.subtitle);
                        this.drawBackdrop(p_282884_, font, 5, k2, 16777215 | i2);
                        p_282884_.drawString(font, this.subtitle, -k2 / 2, 5, 16777215 | i2);
                        p_282884_.pose().popPose();
                    }

                    RenderSystem.disableBlend();
                    p_282884_.pose().popPose();
                }

                this.minecraft.getProfiler().pop();
            }

            this.subtitleOverlay.render(p_282884_);
            Scoreboard scoreboard = this.minecraft.level.getScoreboard();
            Objective objective = null;
            PlayerTeam playerteam = scoreboard.getPlayersTeam(this.minecraft.player.getScoreboardName());
            if (playerteam != null) {
                DisplaySlot displayslot = DisplaySlot.teamColorToSlot(playerteam.getColor());
                if (displayslot != null) {
                    objective = scoreboard.getDisplayObjective(displayslot);
                }
            }

            Objective objective1 = objective != null ? objective : scoreboard.getDisplayObjective(DisplaySlot.SIDEBAR);
            if (objective1 != null) {
                this.displayScoreboardSidebar(p_282884_, objective1);
            }

            RenderSystem.enableBlend();
            int l2 = Mth.floor(this.minecraft.mouseHandler.xpos() * (double)window.getGuiScaledWidth() / (double)window.getScreenWidth());
            int i1 = Mth.floor(this.minecraft.mouseHandler.ypos() * (double)window.getGuiScaledHeight() / (double)window.getScreenHeight());
            this.minecraft.getProfiler().push("chat");
            this.chat.render(p_282884_, this.tickCount, l2, i1);
            this.minecraft.getProfiler().pop();
            objective1 = scoreboard.getDisplayObjective(DisplaySlot.LIST);
            if (!this.minecraft.options.keyPlayerList.isDown()
                || this.minecraft.isLocalServer() && this.minecraft.player.connection.getListedOnlinePlayers().size() <= 1 && objective1 == null) {
                this.tabList.setVisible(false);
            } else {
                this.tabList.setVisible(true);
                this.tabList.render(p_282884_, this.screenWidth, scoreboard, objective1);
            }

            this.renderSavingIndicator(p_282884_);
        }
    }

    protected void drawBackdrop(GuiGraphics p_282548_, Font p_93041_, int p_93042_, int p_93043_, int p_93044_) {
        int i = this.minecraft.options.getBackgroundColor(0.0F);
        if (i != 0) {
            int j = -p_93043_ / 2;
            p_282548_.fill(j - 2, p_93042_ - 2, j + p_93043_ + 2, p_93042_ + 9 + 2, FastColor.ARGB32.multiply(i, p_93044_));
        }
    }

    public void renderCrosshair(GuiGraphics p_282828_) {
        Options options = this.minecraft.options;
        if (options.getCameraType().isFirstPerson()) {
            if (this.minecraft.gameMode.getPlayerMode() != GameType.SPECTATOR || this.canRenderCrosshairForSpectator(this.minecraft.hitResult)) {
                if (this.debugOverlay.showDebugScreen() && !this.minecraft.player.isReducedDebugInfo() && !options.reducedDebugInfo().get()) {
                    Camera camera = this.minecraft.gameRenderer.getMainCamera();
                    PoseStack posestack = RenderSystem.getModelViewStack();
                    posestack.pushPose();
                    posestack.mulPoseMatrix(p_282828_.pose().last().pose());
                    posestack.translate((float)(this.screenWidth / 2), (float)(this.screenHeight / 2), 0.0F);
                    posestack.mulPose(Axis.XN.rotationDegrees(camera.getXRot()));
                    posestack.mulPose(Axis.YP.rotationDegrees(camera.getYRot()));
                    posestack.scale(-1.0F, -1.0F, -1.0F);
                    RenderSystem.applyModelViewMatrix();
                    RenderSystem.renderCrosshair(10);
                    posestack.popPose();
                    RenderSystem.applyModelViewMatrix();
                } else {
                    RenderSystem.blendFuncSeparate(
                        GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR,
                        GlStateManager.SourceFactor.ONE,
                        GlStateManager.DestFactor.ZERO
                    );
                    int i = 15;
                    p_282828_.blitSprite(CROSSHAIR_SPRITE, (this.screenWidth - 15) / 2, (this.screenHeight - 15) / 2, 15, 15);
                    if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.CROSSHAIR) {
                        float f = this.minecraft.player.getAttackStrengthScale(0.0F);
                        boolean flag = false;
                        if (this.minecraft.crosshairPickEntity != null && this.minecraft.crosshairPickEntity instanceof LivingEntity && f >= 1.0F) {
                            flag = this.minecraft.player.getCurrentItemAttackStrengthDelay() > 5.0F;
                            flag &= this.minecraft.crosshairPickEntity.isAlive();
                        }

                        int j = this.screenHeight / 2 - 7 + 16;
                        int k = this.screenWidth / 2 - 8;
                        if (flag) {
                            p_282828_.blitSprite(CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE, k, j, 16, 16);
                        } else if (f < 1.0F) {
                            int l = (int)(f * 17.0F);
                            p_282828_.blitSprite(CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE, k, j, 16, 4);
                            p_282828_.blitSprite(CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE, 16, 4, 0, 0, k, j, l, 4);
                        }
                    }

                    RenderSystem.defaultBlendFunc();
                }
            }
        }
    }

    private boolean canRenderCrosshairForSpectator(@Nullable HitResult p_93025_) {
        if (p_93025_ == null) {
            return false;
        } else if (p_93025_.getType() == HitResult.Type.ENTITY) {
            return ((EntityHitResult)p_93025_).getEntity() instanceof MenuProvider;
        } else if (p_93025_.getType() == HitResult.Type.BLOCK) {
            BlockPos blockpos = ((BlockHitResult)p_93025_).getBlockPos();
            Level level = this.minecraft.level;
            return level.getBlockState(blockpos).getMenuProvider(level, blockpos) != null;
        } else {
            return false;
        }
    }

    public void renderEffects(GuiGraphics p_282812_) {
        Collection<MobEffectInstance> collection = this.minecraft.player.getActiveEffects();
        if (!collection.isEmpty()) {
            Screen $$4 = this.minecraft.screen;
            if ($$4 instanceof EffectRenderingInventoryScreen effectrenderinginventoryscreen && effectrenderinginventoryscreen.canSeeEffects()) {
                return;
            }

            RenderSystem.enableBlend();
            int j1 = 0;
            int k1 = 0;
            MobEffectTextureManager mobeffecttexturemanager = this.minecraft.getMobEffectTextures();
            List<Runnable> list = Lists.newArrayListWithExpectedSize(collection.size());

            for(MobEffectInstance mobeffectinstance : Ordering.natural().reverse().sortedCopy(collection)) {
                MobEffect mobeffect = mobeffectinstance.getEffect();
                var renderer = net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions.of(mobeffectinstance);
                if (!renderer.isVisibleInGui(mobeffectinstance)) continue;
                if (mobeffectinstance.showIcon()) {
                    int i = this.screenWidth;
                    int j = 1;
                    if (this.minecraft.isDemo()) {
                        j += 15;
                    }

                    if (mobeffect.isBeneficial()) {
                        ++j1;
                        i -= 25 * j1;
                    } else {
                        ++k1;
                        i -= 25 * k1;
                        j += 26;
                    }

                    float f = 1.0F;
                    if (mobeffectinstance.isAmbient()) {
                        p_282812_.blitSprite(EFFECT_BACKGROUND_AMBIENT_SPRITE, i, j, 24, 24);
                    } else {
                        p_282812_.blitSprite(EFFECT_BACKGROUND_SPRITE, i, j, 24, 24);
                        if (mobeffectinstance.endsWithin(200)) {
                            int k = mobeffectinstance.getDuration();
                            int l = 10 - k / 20;
                            f = Mth.clamp((float)k / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F)
                                + Mth.cos((float)k * (float) Math.PI / 5.0F) * Mth.clamp((float)l / 10.0F * 0.25F, 0.0F, 0.25F);
                        }
                    }

                    if (renderer.renderGuiIcon(mobeffectinstance, this, p_282812_, i, j, 0, f)) continue;
                    TextureAtlasSprite textureatlassprite = mobeffecttexturemanager.get(mobeffect);
                    int i1 = j;
                    float f1 = f;
                    int i_f = i;
                    list.add(() -> {
                        p_282812_.setColor(1.0F, 1.0F, 1.0F, f1);
                        p_282812_.blit(i_f + 3, i1 + 3, 0, 18, 18, textureatlassprite);
                        p_282812_.setColor(1.0F, 1.0F, 1.0F, 1.0F);
                    });
                }
            }

            list.forEach(Runnable::run);
        }
    }

    public void renderHotbar(float p_283031_, GuiGraphics p_282108_) {
        Player player = this.getCameraPlayer();
        if (player != null) {
            ItemStack itemstack = player.getOffhandItem();
            HumanoidArm humanoidarm = player.getMainArm().getOpposite();
            int i = this.screenWidth / 2;
            int j = 182;
            int k = 91;
            p_282108_.pose().pushPose();
            p_282108_.pose().translate(0.0F, 0.0F, -90.0F);
            p_282108_.blitSprite(HOTBAR_SPRITE, i - 91, this.screenHeight - 22, 182, 22);
            p_282108_.blitSprite(HOTBAR_SELECTION_SPRITE, i - 91 - 1 + player.getInventory().selected * 20, this.screenHeight - 22 - 1, 24, 23);
            if (!itemstack.isEmpty()) {
                if (humanoidarm == HumanoidArm.LEFT) {
                    p_282108_.blitSprite(HOTBAR_OFFHAND_LEFT_SPRITE, i - 91 - 29, this.screenHeight - 23, 29, 24);
                } else {
                    p_282108_.blitSprite(HOTBAR_OFFHAND_RIGHT_SPRITE, i + 91, this.screenHeight - 23, 29, 24);
                }
            }

            p_282108_.pose().popPose();
            int l = 1;

            for(int i1 = 0; i1 < 9; ++i1) {
                int j1 = i - 90 + i1 * 20 + 2;
                int k1 = this.screenHeight - 16 - 3;
                this.renderSlot(p_282108_, j1, k1, p_283031_, player, player.getInventory().items.get(i1), l++);
            }

            if (!itemstack.isEmpty()) {
                int i2 = this.screenHeight - 16 - 3;
                if (humanoidarm == HumanoidArm.LEFT) {
                    this.renderSlot(p_282108_, i - 91 - 26, i2, p_283031_, player, itemstack, l++);
                } else {
                    this.renderSlot(p_282108_, i + 91 + 10, i2, p_283031_, player, itemstack, l++);
                }
            }

            RenderSystem.enableBlend();
            if (this.minecraft.options.attackIndicator().get() == AttackIndicatorStatus.HOTBAR) {
                float f = this.minecraft.player.getAttackStrengthScale(0.0F);
                if (f < 1.0F) {
                    int j2 = this.screenHeight - 20;
                    int k2 = i + 91 + 6;
                    if (humanoidarm == HumanoidArm.RIGHT) {
                        k2 = i - 91 - 22;
                    }

                    int l1 = (int)(f * 19.0F);
                    p_282108_.blitSprite(HOTBAR_ATTACK_INDICATOR_BACKGROUND_SPRITE, k2, j2, 18, 18);
                    p_282108_.blitSprite(HOTBAR_ATTACK_INDICATOR_PROGRESS_SPRITE, 18, 18, 0, 18 - l1, k2, j2 + 18 - l1, 18, l1);
                }
            }

            RenderSystem.disableBlend();
        }
    }

    public void renderJumpMeter(PlayerRideableJumping p_282774_, GuiGraphics p_282939_, int p_283351_) {
        this.minecraft.getProfiler().push("jumpBar");
        float f = this.minecraft.player.getJumpRidingScale();
        int i = 182;
        int j = (int)(f * 183.0F);
        int k = this.screenHeight - 32 + 3;
        p_282939_.blitSprite(JUMP_BAR_BACKGROUND_SPRITE, p_283351_, k, 182, 5);
        if (p_282774_.getJumpCooldown() > 0) {
            p_282939_.blitSprite(JUMP_BAR_COOLDOWN_SPRITE, p_283351_, k, 182, 5);
        } else if (j > 0) {
            p_282939_.blitSprite(JUMP_BAR_PROGRESS_SPRITE, 182, 5, 0, 0, p_283351_, k, j, 5);
        }

        this.minecraft.getProfiler().pop();
    }

    public void renderExperienceBar(GuiGraphics p_281906_, int p_282731_) {
        this.minecraft.getProfiler().push("expBar");
        int i = this.minecraft.player.getXpNeededForNextLevel();
        if (i > 0) {
            int j = 182;
            int k = (int)(this.minecraft.player.experienceProgress * 183.0F);
            int l = this.screenHeight - 32 + 3;
            p_281906_.blitSprite(EXPERIENCE_BAR_BACKGROUND_SPRITE, p_282731_, l, 182, 5);
            if (k > 0) {
                p_281906_.blitSprite(EXPERIENCE_BAR_PROGRESS_SPRITE, 182, 5, 0, 0, p_282731_, l, k, 5);
            }
        }

        this.minecraft.getProfiler().pop();
        if (this.minecraft.player.experienceLevel > 0) {
            this.minecraft.getProfiler().push("expLevel");
            String s = this.minecraft.player.experienceLevel + "";
            int i1 = (this.screenWidth - this.getFont().width(s)) / 2;
            int j1 = this.screenHeight - 31 - 4;
            p_281906_.drawString(this.getFont(), s, i1 + 1, j1, 0, false);
            p_281906_.drawString(this.getFont(), s, i1 - 1, j1, 0, false);
            p_281906_.drawString(this.getFont(), s, i1, j1 + 1, 0, false);
            p_281906_.drawString(this.getFont(), s, i1, j1 - 1, 0, false);
            p_281906_.drawString(this.getFont(), s, i1, j1, 8453920, false);
            this.minecraft.getProfiler().pop();
        }
    }

    public void renderSelectedItemName(GuiGraphics p_283501_) {
        renderSelectedItemName(p_283501_, 0);
    }

    public void renderSelectedItemName(GuiGraphics p_283501_, int yShift) {
        this.minecraft.getProfiler().push("selectedItemName");
        if (this.toolHighlightTimer > 0 && !this.lastToolHighlight.isEmpty()) {
            MutableComponent mutablecomponent = Component.empty()
                .append(this.lastToolHighlight.getHoverName())
                .withStyle(this.lastToolHighlight.getRarity().getStyleModifier());
            if (this.lastToolHighlight.hasCustomHoverName()) {
                mutablecomponent.withStyle(ChatFormatting.ITALIC);
            }

            Component highlightTip = this.lastToolHighlight.getHighlightTip(mutablecomponent);
            int i = this.getFont().width(highlightTip);
            int j = (this.screenWidth - i) / 2;
            int k = this.screenHeight - Math.max(yShift, 59);
            if (!this.minecraft.gameMode.canHurtPlayer()) {
                k += 14;
            }

            int l = (int)((float)this.toolHighlightTimer * 256.0F / 10.0F);
            if (l > 255) {
                l = 255;
            }

            if (l > 0) {
                p_283501_.fill(j - 2, k - 2, j + i + 2, k + 9 + 2, this.minecraft.options.getBackgroundColor(0));
                Font font = net.neoforged.neoforge.client.extensions.common.IClientItemExtensions.of(lastToolHighlight).getFont(lastToolHighlight, net.neoforged.neoforge.client.extensions.common.IClientItemExtensions.FontContext.SELECTED_ITEM_NAME);
                if (font == null) {
                    p_283501_.drawString(this.getFont(), highlightTip, j, k, 16777215 + (l << 24));
                } else {
                    j = (this.screenWidth - font.width(highlightTip)) / 2;
                    p_283501_.drawString(font, highlightTip, j, k, 16777215 + (l << 24));
                }
            }
        }

        this.minecraft.getProfiler().pop();
    }

    public void renderDemoOverlay(GuiGraphics p_281825_) {
        this.minecraft.getProfiler().push("demo");
        Component component;
        if (this.minecraft.level.getGameTime() >= 120500L) {
            component = DEMO_EXPIRED_TEXT;
        } else {
            component = Component.translatable(
                "demo.remainingTime",
                StringUtil.formatTickDuration((int)(120500L - this.minecraft.level.getGameTime()), this.minecraft.level.tickRateManager().tickrate())
            );
        }

        int i = this.getFont().width(component);
        p_281825_.drawString(this.getFont(), component, this.screenWidth - i - 10, 5, 16777215);
        this.minecraft.getProfiler().pop();
    }

    public void displayScoreboardSidebar(GuiGraphics p_282008_, Objective p_283455_) {
        Scoreboard scoreboard = p_283455_.getScoreboard();
        NumberFormat numberformat = p_283455_.numberFormatOrDefault(StyledFormat.SIDEBAR_DEFAULT);

        @OnlyIn(Dist.CLIENT)
        record DisplayEntry(Component name, Component score, int scoreWidth) {
        }

        DisplayEntry[] agui$1displayentry = scoreboard.listPlayerScores(p_283455_)
            .stream()
            .filter(p_313419_ -> !p_313419_.isHidden())
            .sorted(SCORE_DISPLAY_ORDER)
            .limit(15L)
            .map(p_313418_ -> {
                PlayerTeam playerteam = scoreboard.getPlayersTeam(p_313418_.owner());
                Component component1 = p_313418_.ownerName();
                Component component2 = PlayerTeam.formatNameForTeam(playerteam, component1);
                Component component3 = p_313418_.formatValue(numberformat);
                int i1 = this.getFont().width(component3);
                return new DisplayEntry(component2, component3, i1);
            })
            .toArray(p_313425_ -> new DisplayEntry[p_313425_]);
        Component component = p_283455_.getDisplayName();
        int i = this.getFont().width(component);
        int j = i;
        int k = this.getFont().width(": ");

        for(DisplayEntry gui$1displayentry : agui$1displayentry) {
            j = Math.max(j, this.getFont().width(gui$1displayentry.name) + (gui$1displayentry.scoreWidth > 0 ? k + gui$1displayentry.scoreWidth : 0));
        }

        int l = j;
        p_282008_.drawManaged(() -> {
            int i1 = agui$1displayentry.length;
            int j1 = i1 * 9;
            int k1 = this.screenHeight / 2 + j1 / 3;
            int l1 = 3;
            int i2 = this.screenWidth - l - 3;
            int j2 = this.screenWidth - 3 + 2;
            int k2 = this.minecraft.options.getBackgroundColor(0.3F);
            int l2 = this.minecraft.options.getBackgroundColor(0.4F);
            int i3 = k1 - i1 * 9;
            p_282008_.fill(i2 - 2, i3 - 9 - 1, j2, i3 - 1, l2);
            p_282008_.fill(i2 - 2, i3 - 1, j2, k1, k2);
            p_282008_.drawString(this.getFont(), component, i2 + l / 2 - i / 2, i3 - 9, -1, false);

            for(int j3 = 0; j3 < i1; ++j3) {
                DisplayEntry gui$1displayentry1 = agui$1displayentry[j3];
                int k3 = k1 - (i1 - j3) * 9;
                p_282008_.drawString(this.getFont(), gui$1displayentry1.name, i2, k3, -1, false);
                p_282008_.drawString(this.getFont(), gui$1displayentry1.score, j2 - gui$1displayentry1.scoreWidth, k3, -1, false);
            }
        });
    }

    @Nullable
    private Player getCameraPlayer() {
        Entity entity = this.minecraft.getCameraEntity();
        return entity instanceof Player player ? player : null;
    }

    @Nullable
    private LivingEntity getPlayerVehicleWithHealth() {
        Player player = this.getCameraPlayer();
        if (player != null) {
            Entity entity = player.getVehicle();
            if (entity == null) {
                return null;
            }

            if (entity instanceof LivingEntity) {
                return (LivingEntity)entity;
            }
        }

        return null;
    }

    private int getVehicleMaxHearts(@Nullable LivingEntity p_93023_) {
        if (p_93023_ != null && p_93023_.showVehicleHealth()) {
            float f = p_93023_.getMaxHealth();
            int i = (int)(f + 0.5F) / 2;
            if (i > 30) {
                i = 30;
            }

            return i;
        } else {
            return 0;
        }
    }

    private int getVisibleVehicleHeartRows(int p_93013_) {
        return (int)Math.ceil((double)p_93013_ / 10.0);
    }

    private void renderPlayerHealth(GuiGraphics p_283143_) {
        Player player = this.getCameraPlayer();
        if (player != null) {
            int i = Mth.ceil(player.getHealth());
            boolean flag = this.healthBlinkTime > (long)this.tickCount && (this.healthBlinkTime - (long)this.tickCount) / 3L % 2L == 1L;
            long j = Util.getMillis();
            if (i < this.lastHealth && player.invulnerableTime > 0) {
                this.lastHealthTime = j;
                this.healthBlinkTime = (long)(this.tickCount + 20);
            } else if (i > this.lastHealth && player.invulnerableTime > 0) {
                this.lastHealthTime = j;
                this.healthBlinkTime = (long)(this.tickCount + 10);
            }

            if (j - this.lastHealthTime > 1000L) {
                this.lastHealth = i;
                this.displayHealth = i;
                this.lastHealthTime = j;
            }

            this.lastHealth = i;
            int k = this.displayHealth;
            this.random.setSeed((long)(this.tickCount * 312871));
            FoodData fooddata = player.getFoodData();
            int l = fooddata.getFoodLevel();
            int i1 = this.screenWidth / 2 - 91;
            int j1 = this.screenWidth / 2 + 91;
            int k1 = this.screenHeight - 39;
            float f = Math.max((float)player.getAttributeValue(Attributes.MAX_HEALTH), (float)Math.max(k, i));
            int l1 = Mth.ceil(player.getAbsorptionAmount());
            int i2 = Mth.ceil((f + (float)l1) / 2.0F / 10.0F);
            int j2 = Math.max(10 - (i2 - 2), 3);
            int k2 = k1 - (i2 - 1) * j2 - 10;
            int l2 = k1 - 10;
            int i3 = player.getArmorValue();
            int j3 = -1;
            if (player.hasEffect(MobEffects.REGENERATION)) {
                j3 = this.tickCount % Mth.ceil(f + 5.0F);
            }

            this.minecraft.getProfiler().push("armor");

            for(int k3 = 0; k3 < 10; ++k3) {
                if (i3 > 0) {
                    int l3 = i1 + k3 * 8;
                    if (k3 * 2 + 1 < i3) {
                        p_283143_.blitSprite(ARMOR_FULL_SPRITE, l3, k2, 9, 9);
                    }

                    if (k3 * 2 + 1 == i3) {
                        p_283143_.blitSprite(ARMOR_HALF_SPRITE, l3, k2, 9, 9);
                    }

                    if (k3 * 2 + 1 > i3) {
                        p_283143_.blitSprite(ARMOR_EMPTY_SPRITE, l3, k2, 9, 9);
                    }
                }
            }

            this.minecraft.getProfiler().popPush("health");
            this.renderHearts(p_283143_, player, i1, k1, j2, j3, f, i, k, l1, flag);
            LivingEntity livingentity = this.getPlayerVehicleWithHealth();
            int l4 = this.getVehicleMaxHearts(livingentity);
            if (l4 == 0) {
                this.minecraft.getProfiler().popPush("food");

                for(int i4 = 0; i4 < 10; ++i4) {
                    int j4 = k1;
                    ResourceLocation resourcelocation;
                    ResourceLocation resourcelocation1;
                    ResourceLocation resourcelocation2;
                    if (player.hasEffect(MobEffects.HUNGER)) {
                        resourcelocation = FOOD_EMPTY_HUNGER_SPRITE;
                        resourcelocation1 = FOOD_HALF_HUNGER_SPRITE;
                        resourcelocation2 = FOOD_FULL_HUNGER_SPRITE;
                    } else {
                        resourcelocation = FOOD_EMPTY_SPRITE;
                        resourcelocation1 = FOOD_HALF_SPRITE;
                        resourcelocation2 = FOOD_FULL_SPRITE;
                    }

                    if (player.getFoodData().getSaturationLevel() <= 0.0F && this.tickCount % (l * 3 + 1) == 0) {
                        j4 = k1 + (this.random.nextInt(3) - 1);
                    }

                    int k4 = j1 - i4 * 8 - 9;
                    p_283143_.blitSprite(resourcelocation, k4, j4, 9, 9);
                    if (i4 * 2 + 1 < l) {
                        p_283143_.blitSprite(resourcelocation2, k4, j4, 9, 9);
                    }

                    if (i4 * 2 + 1 == l) {
                        p_283143_.blitSprite(resourcelocation1, k4, j4, 9, 9);
                    }
                }

                l2 -= 10;
            }

            this.minecraft.getProfiler().popPush("air");
            int i5 = player.getMaxAirSupply();
            int j5 = Math.min(player.getAirSupply(), i5);
            if (player.isEyeInFluid(FluidTags.WATER) || j5 < i5) {
                int k5 = this.getVisibleVehicleHeartRows(l4) - 1;
                l2 -= k5 * 10;
                int l5 = Mth.ceil((double)(j5 - 2) * 10.0 / (double)i5);
                int i6 = Mth.ceil((double)j5 * 10.0 / (double)i5) - l5;

                for(int j6 = 0; j6 < l5 + i6; ++j6) {
                    if (j6 < l5) {
                        p_283143_.blitSprite(AIR_SPRITE, j1 - j6 * 8 - 9, l2, 9, 9);
                    } else {
                        p_283143_.blitSprite(AIR_BURSTING_SPRITE, j1 - j6 * 8 - 9, l2, 9, 9);
                    }
                }
            }

            this.minecraft.getProfiler().pop();
        }
    }

    protected void renderHearts(
        GuiGraphics p_282497_,
        Player p_168690_,
        int p_168691_,
        int p_168692_,
        int p_168693_,
        int p_168694_,
        float p_168695_,
        int p_168696_,
        int p_168697_,
        int p_168698_,
        boolean p_168699_
    ) {
        Gui.HeartType gui$hearttype = Gui.HeartType.forPlayer(p_168690_);
        boolean flag = p_168690_.level().getLevelData().isHardcore();
        int i = Mth.ceil((double)p_168695_ / 2.0);
        int j = Mth.ceil((double)p_168698_ / 2.0);
        int k = i * 2;

        for(int l = i + j - 1; l >= 0; --l) {
            int i1 = l / 10;
            int j1 = l % 10;
            int k1 = p_168691_ + j1 * 8;
            int l1 = p_168692_ - i1 * p_168693_;
            if (p_168696_ + p_168698_ <= 4) {
                l1 += this.random.nextInt(2);
            }

            if (l < i && l == p_168694_) {
                l1 -= 2;
            }

            this.renderHeart(p_282497_, Gui.HeartType.CONTAINER, k1, l1, flag, p_168699_, false);
            int i2 = l * 2;
            boolean flag1 = l >= i;
            if (flag1) {
                int j2 = i2 - k;
                if (j2 < p_168698_) {
                    boolean flag2 = j2 + 1 == p_168698_;
                    this.renderHeart(p_282497_, gui$hearttype == Gui.HeartType.WITHERED ? gui$hearttype : Gui.HeartType.ABSORBING, k1, l1, flag, false, flag2);
                }
            }

            if (p_168699_ && i2 < p_168697_) {
                boolean flag3 = i2 + 1 == p_168697_;
                this.renderHeart(p_282497_, gui$hearttype, k1, l1, flag, true, flag3);
            }

            if (i2 < p_168696_) {
                boolean flag4 = i2 + 1 == p_168696_;
                this.renderHeart(p_282497_, gui$hearttype, k1, l1, flag, false, flag4);
            }
        }
    }

    private void renderHeart(
        GuiGraphics p_283024_, Gui.HeartType p_281393_, int p_283636_, int p_283279_, boolean p_283440_, boolean p_282496_, boolean p_294129_
    ) {
        p_283024_.blitSprite(p_281393_.getSprite(p_283440_, p_294129_, p_282496_), p_283636_, p_283279_, 9, 9);
    }

    private void renderVehicleHealth(GuiGraphics p_283368_) {
        LivingEntity livingentity = this.getPlayerVehicleWithHealth();
        if (livingentity != null) {
            int i = this.getVehicleMaxHearts(livingentity);
            if (i != 0) {
                int j = (int)Math.ceil((double)livingentity.getHealth());
                this.minecraft.getProfiler().popPush("mountHealth");
                int k = this.screenHeight - 39;
                int l = this.screenWidth / 2 + 91;
                int i1 = k;

                for(int j1 = 0; i > 0; j1 += 20) {
                    int k1 = Math.min(i, 10);
                    i -= k1;

                    for(int l1 = 0; l1 < k1; ++l1) {
                        int i2 = l - l1 * 8 - 9;
                        p_283368_.blitSprite(HEART_VEHICLE_CONTAINER_SPRITE, i2, i1, 9, 9);
                        if (l1 * 2 + 1 + j1 < j) {
                            p_283368_.blitSprite(HEART_VEHICLE_FULL_SPRITE, i2, i1, 9, 9);
                        }

                        if (l1 * 2 + 1 + j1 == j) {
                            p_283368_.blitSprite(HEART_VEHICLE_HALF_SPRITE, i2, i1, 9, 9);
                        }
                    }

                    i1 -= 10;
                }
            }
        }
    }

    protected void renderTextureOverlay(GuiGraphics p_282304_, ResourceLocation p_281622_, float p_281504_) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        p_282304_.setColor(1.0F, 1.0F, 1.0F, p_281504_);
        p_282304_.blit(p_281622_, 0, 0, -90, 0.0F, 0.0F, this.screenWidth, this.screenHeight, this.screenWidth, this.screenHeight);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        p_282304_.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public void renderSpyglassOverlay(GuiGraphics p_282069_, float p_283442_) {
        float f = (float)Math.min(this.screenWidth, this.screenHeight);
        float f1 = Math.min((float)this.screenWidth / f, (float)this.screenHeight / f) * p_283442_;
        int i = Mth.floor(f * f1);
        int j = Mth.floor(f * f1);
        int k = (this.screenWidth - i) / 2;
        int l = (this.screenHeight - j) / 2;
        int i1 = k + i;
        int j1 = l + j;
        p_282069_.blit(SPYGLASS_SCOPE_LOCATION, k, l, -90, 0.0F, 0.0F, i, j, i, j);
        p_282069_.fill(RenderType.guiOverlay(), 0, j1, this.screenWidth, this.screenHeight, -90, -16777216);
        p_282069_.fill(RenderType.guiOverlay(), 0, 0, this.screenWidth, l, -90, -16777216);
        p_282069_.fill(RenderType.guiOverlay(), 0, l, k, j1, -90, -16777216);
        p_282069_.fill(RenderType.guiOverlay(), i1, l, this.screenWidth, j1, -90, -16777216);
    }

    private void updateVignetteBrightness(Entity p_93021_) {
        BlockPos blockpos = BlockPos.containing(p_93021_.getX(), p_93021_.getEyeY(), p_93021_.getZ());
        float f = LightTexture.getBrightness(p_93021_.level().dimensionType(), p_93021_.level().getMaxLocalRawBrightness(blockpos));
        float f1 = Mth.clamp(1.0F - f, 0.0F, 1.0F);
        this.vignetteBrightness += (f1 - this.vignetteBrightness) * 0.01F;
    }

    public void renderVignette(GuiGraphics p_283063_, @Nullable Entity p_283439_) {
        WorldBorder worldborder = this.minecraft.level.getWorldBorder();
        float f = 0.0F;
        if (p_283439_ != null) {
            float f1 = (float)worldborder.getDistanceToBorder(p_283439_);
            double d0 = Math.min(
                worldborder.getLerpSpeed() * (double)worldborder.getWarningTime() * 1000.0, Math.abs(worldborder.getLerpTarget() - worldborder.getSize())
            );
            double d1 = Math.max((double)worldborder.getWarningBlocks(), d0);
            if ((double)f1 < d1) {
                f = 1.0F - (float)((double)f1 / d1);
            }
        }

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.blendFuncSeparate(
            GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
        );
        if (f > 0.0F) {
            f = Mth.clamp(f, 0.0F, 1.0F);
            p_283063_.setColor(0.0F, f, f, 1.0F);
        } else {
            float f2 = this.vignetteBrightness;
            f2 = Mth.clamp(f2, 0.0F, 1.0F);
            p_283063_.setColor(f2, f2, f2, 1.0F);
        }

        p_283063_.blit(VIGNETTE_LOCATION, 0, 0, -90, 0.0F, 0.0F, this.screenWidth, this.screenHeight, this.screenWidth, this.screenHeight);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        p_283063_.setColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
    }

    protected void renderPortalOverlay(GuiGraphics p_283375_, float p_283296_) {
        if (p_283296_ < 1.0F) {
            p_283296_ *= p_283296_;
            p_283296_ *= p_283296_;
            p_283296_ = p_283296_ * 0.8F + 0.2F;
        }

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        p_283375_.setColor(1.0F, 1.0F, 1.0F, p_283296_);
        TextureAtlasSprite textureatlassprite = this.minecraft
            .getBlockRenderer()
            .getBlockModelShaper()
            .getParticleIcon(Blocks.NETHER_PORTAL.defaultBlockState());
        p_283375_.blit(0, 0, -90, this.screenWidth, this.screenHeight, textureatlassprite);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        p_283375_.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderSlot(GuiGraphics p_283283_, int p_283213_, int p_281301_, float p_281885_, Player p_283644_, ItemStack p_283317_, int p_283261_) {
        if (!p_283317_.isEmpty()) {
            float f = (float)p_283317_.getPopTime() - p_281885_;
            if (f > 0.0F) {
                float f1 = 1.0F + f / 5.0F;
                p_283283_.pose().pushPose();
                p_283283_.pose().translate((float)(p_283213_ + 8), (float)(p_281301_ + 12), 0.0F);
                p_283283_.pose().scale(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
                p_283283_.pose().translate((float)(-(p_283213_ + 8)), (float)(-(p_281301_ + 12)), 0.0F);
            }

            p_283283_.renderItem(p_283644_, p_283317_, p_283213_, p_281301_, p_283261_);
            if (f > 0.0F) {
                p_283283_.pose().popPose();
            }

            p_283283_.renderItemDecorations(this.minecraft.font, p_283317_, p_283213_, p_281301_);
        }
    }

    public void tick(boolean p_193833_) {
        this.tickAutosaveIndicator();
        if (!p_193833_) {
            this.tick();
        }
    }

    private void tick() {
        if (this.overlayMessageTime > 0) {
            --this.overlayMessageTime;
        }

        if (this.titleTime > 0) {
            --this.titleTime;
            if (this.titleTime <= 0) {
                this.title = null;
                this.subtitle = null;
            }
        }

        ++this.tickCount;
        Entity entity = this.minecraft.getCameraEntity();
        if (entity != null) {
            this.updateVignetteBrightness(entity);
        }

        if (this.minecraft.player != null) {
            ItemStack itemstack = this.minecraft.player.getInventory().getSelected();
            if (itemstack.isEmpty()) {
                this.toolHighlightTimer = 0;
            } else if (this.lastToolHighlight.isEmpty()
                || !itemstack.is(this.lastToolHighlight.getItem())
                || (!itemstack.getHoverName().equals(this.lastToolHighlight.getHoverName()) || !itemstack.getHighlightTip(itemstack.getHoverName()).equals(lastToolHighlight.getHighlightTip(lastToolHighlight.getHoverName())))) {
                this.toolHighlightTimer = (int)(40.0 * this.minecraft.options.notificationDisplayTime().get());
            } else if (this.toolHighlightTimer > 0) {
                --this.toolHighlightTimer;
            }

            this.lastToolHighlight = itemstack;
        }

        this.chat.tick();
    }

    private void tickAutosaveIndicator() {
        MinecraftServer minecraftserver = this.minecraft.getSingleplayerServer();
        boolean flag = minecraftserver != null && minecraftserver.isCurrentlySaving();
        this.lastAutosaveIndicatorValue = this.autosaveIndicatorValue;
        this.autosaveIndicatorValue = Mth.lerp(0.2F, this.autosaveIndicatorValue, flag ? 1.0F : 0.0F);
    }

    public void setNowPlaying(Component p_93056_) {
        Component component = Component.translatable("record.nowPlaying", p_93056_);
        this.setOverlayMessage(component, true);
        this.minecraft.getNarrator().sayNow(component);
    }

    public void setOverlayMessage(Component p_93064_, boolean p_93065_) {
        this.setChatDisabledByPlayerShown(false);
        this.overlayMessageString = p_93064_;
        this.overlayMessageTime = 60;
        this.animateOverlayMessageColor = p_93065_;
    }

    public void setChatDisabledByPlayerShown(boolean p_238398_) {
        this.chatDisabledByPlayerShown = p_238398_;
    }

    public boolean isShowingChatDisabledByPlayer() {
        return this.chatDisabledByPlayerShown && this.overlayMessageTime > 0;
    }

    public void setTimes(int p_168685_, int p_168686_, int p_168687_) {
        if (p_168685_ >= 0) {
            this.titleFadeInTime = p_168685_;
        }

        if (p_168686_ >= 0) {
            this.titleStayTime = p_168686_;
        }

        if (p_168687_ >= 0) {
            this.titleFadeOutTime = p_168687_;
        }

        if (this.titleTime > 0) {
            this.titleTime = this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime;
        }
    }

    public void setSubtitle(Component p_168712_) {
        this.subtitle = p_168712_;
    }

    public void setTitle(Component p_168715_) {
        this.title = p_168715_;
        this.titleTime = this.titleFadeInTime + this.titleStayTime + this.titleFadeOutTime;
    }

    public void clear() {
        this.title = null;
        this.subtitle = null;
        this.titleTime = 0;
    }

    public ChatComponent getChat() {
        return this.chat;
    }

    public int getGuiTicks() {
        return this.tickCount;
    }

    public Font getFont() {
        return this.minecraft.font;
    }

    public SpectatorGui getSpectatorGui() {
        return this.spectatorGui;
    }

    public PlayerTabOverlay getTabList() {
        return this.tabList;
    }

    public void onDisconnected() {
        this.tabList.reset();
        this.bossOverlay.reset();
        this.minecraft.getToasts().clear();
        this.debugOverlay.reset();
        this.chat.clearMessages(true);
    }

    public BossHealthOverlay getBossOverlay() {
        return this.bossOverlay;
    }

    public DebugScreenOverlay getDebugOverlay() {
        return this.debugOverlay;
    }

    public void clearCache() {
        this.debugOverlay.clearChunkCache();
    }

    private void renderSavingIndicator(GuiGraphics p_282761_) {
        if (this.minecraft.options.showAutosaveIndicator().get() && (this.autosaveIndicatorValue > 0.0F || this.lastAutosaveIndicatorValue > 0.0F)) {
            int i = Mth.floor(
                255.0F * Mth.clamp(Mth.lerp(this.minecraft.getFrameTime(), this.lastAutosaveIndicatorValue, this.autosaveIndicatorValue), 0.0F, 1.0F)
            );
            if (i > 8) {
                Font font = this.getFont();
                int j = font.width(SAVING_TEXT);
                int k = 16777215 | i << 24 & 0xFF000000;
                p_282761_.drawString(font, SAVING_TEXT, this.screenWidth - j - 10, this.screenHeight - 15, k);
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    static enum HeartType {
        CONTAINER(
            new ResourceLocation("hud/heart/container"),
            new ResourceLocation("hud/heart/container_blinking"),
            new ResourceLocation("hud/heart/container"),
            new ResourceLocation("hud/heart/container_blinking"),
            new ResourceLocation("hud/heart/container_hardcore"),
            new ResourceLocation("hud/heart/container_hardcore_blinking"),
            new ResourceLocation("hud/heart/container_hardcore"),
            new ResourceLocation("hud/heart/container_hardcore_blinking")
        ),
        NORMAL(
            new ResourceLocation("hud/heart/full"),
            new ResourceLocation("hud/heart/full_blinking"),
            new ResourceLocation("hud/heart/half"),
            new ResourceLocation("hud/heart/half_blinking"),
            new ResourceLocation("hud/heart/hardcore_full"),
            new ResourceLocation("hud/heart/hardcore_full_blinking"),
            new ResourceLocation("hud/heart/hardcore_half"),
            new ResourceLocation("hud/heart/hardcore_half_blinking")
        ),
        POISIONED(
            new ResourceLocation("hud/heart/poisoned_full"),
            new ResourceLocation("hud/heart/poisoned_full_blinking"),
            new ResourceLocation("hud/heart/poisoned_half"),
            new ResourceLocation("hud/heart/poisoned_half_blinking"),
            new ResourceLocation("hud/heart/poisoned_hardcore_full"),
            new ResourceLocation("hud/heart/poisoned_hardcore_full_blinking"),
            new ResourceLocation("hud/heart/poisoned_hardcore_half"),
            new ResourceLocation("hud/heart/poisoned_hardcore_half_blinking")
        ),
        WITHERED(
            new ResourceLocation("hud/heart/withered_full"),
            new ResourceLocation("hud/heart/withered_full_blinking"),
            new ResourceLocation("hud/heart/withered_half"),
            new ResourceLocation("hud/heart/withered_half_blinking"),
            new ResourceLocation("hud/heart/withered_hardcore_full"),
            new ResourceLocation("hud/heart/withered_hardcore_full_blinking"),
            new ResourceLocation("hud/heart/withered_hardcore_half"),
            new ResourceLocation("hud/heart/withered_hardcore_half_blinking")
        ),
        ABSORBING(
            new ResourceLocation("hud/heart/absorbing_full"),
            new ResourceLocation("hud/heart/absorbing_full_blinking"),
            new ResourceLocation("hud/heart/absorbing_half"),
            new ResourceLocation("hud/heart/absorbing_half_blinking"),
            new ResourceLocation("hud/heart/absorbing_hardcore_full"),
            new ResourceLocation("hud/heart/absorbing_hardcore_full_blinking"),
            new ResourceLocation("hud/heart/absorbing_hardcore_half"),
            new ResourceLocation("hud/heart/absorbing_hardcore_half_blinking")
        ),
        FROZEN(
            new ResourceLocation("hud/heart/frozen_full"),
            new ResourceLocation("hud/heart/frozen_full_blinking"),
            new ResourceLocation("hud/heart/frozen_half"),
            new ResourceLocation("hud/heart/frozen_half_blinking"),
            new ResourceLocation("hud/heart/frozen_hardcore_full"),
            new ResourceLocation("hud/heart/frozen_hardcore_full_blinking"),
            new ResourceLocation("hud/heart/frozen_hardcore_half"),
            new ResourceLocation("hud/heart/frozen_hardcore_half_blinking")
        );

        private final ResourceLocation full;
        private final ResourceLocation fullBlinking;
        private final ResourceLocation half;
        private final ResourceLocation halfBlinking;
        private final ResourceLocation hardcoreFull;
        private final ResourceLocation hardcoreFullBlinking;
        private final ResourceLocation hardcoreHalf;
        private final ResourceLocation hardcoreHalfBlinking;

        private HeartType(
            ResourceLocation p_294435_,
            ResourceLocation p_294438_,
            ResourceLocation p_295036_,
            ResourceLocation p_295439_,
            ResourceLocation p_296249_,
            ResourceLocation p_295479_,
            ResourceLocation p_296219_,
            ResourceLocation p_296437_
        ) {
            this.full = p_294435_;
            this.fullBlinking = p_294438_;
            this.half = p_295036_;
            this.halfBlinking = p_295439_;
            this.hardcoreFull = p_296249_;
            this.hardcoreFullBlinking = p_295479_;
            this.hardcoreHalf = p_296219_;
            this.hardcoreHalfBlinking = p_296437_;
        }

        public ResourceLocation getSprite(boolean p_295909_, boolean p_295387_, boolean p_294486_) {
            if (!p_295909_) {
                if (p_295387_) {
                    return p_294486_ ? this.halfBlinking : this.half;
                } else {
                    return p_294486_ ? this.fullBlinking : this.full;
                }
            } else if (p_295387_) {
                return p_294486_ ? this.hardcoreHalfBlinking : this.hardcoreHalf;
            } else {
                return p_294486_ ? this.hardcoreFullBlinking : this.hardcoreFull;
            }
        }

        static Gui.HeartType forPlayer(Player p_168733_) {
            Gui.HeartType gui$hearttype;
            if (p_168733_.hasEffect(MobEffects.POISON)) {
                gui$hearttype = POISIONED;
            } else if (p_168733_.hasEffect(MobEffects.WITHER)) {
                gui$hearttype = WITHERED;
            } else if (p_168733_.isFullyFrozen()) {
                gui$hearttype = FROZEN;
            } else {
                gui$hearttype = NORMAL;
            }

            return gui$hearttype;
        }
    }
}
