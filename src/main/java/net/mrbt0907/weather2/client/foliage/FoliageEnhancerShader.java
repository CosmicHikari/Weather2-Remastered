package net.mrbt0907.weather2.client.foliage;

import net.corosus.coroutillegacy.client.model.InvisibleModelWrapper;
import net.corosus.coroutillegacy.config.ConfigCoroUtilLegacy;
import net.corosus.coroutillegacy.util.CoroUtilBlockLightCache;
import net.corosus.coroutillegacy.util.Vec3;
import net.corosus.extendedrenderer.EventHandler;
import net.corosus.extendedrenderer.ExtendedRenderer;
import net.corosus.extendedrenderer.foliage.Foliage;
import net.corosus.extendedrenderer.foliage.FoliageData;
import net.corosus.extendedrenderer.particle.ParticleRegistry;
import net.corosus.extendedrenderer.render.FoliageRenderer;
import net.corosus.extendedrenderer.render.RotatingParticleManager;
import net.corosus.extendedrenderer.shader.InstancedMeshFoliage;
import net.corosus.extendedrenderer.shader.MeshBufferManagerFoliage;
import net.minecraft.block.BeetrootBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.IUnbakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.config.ConfigFoliage;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.EZConfigParser;
import org.lwjgl.BufferUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("deprecation")
public class FoliageEnhancerShader implements Runnable {

    private static final java.util.Set<com.mojang.datafixers.util.Pair<String, String>> MATERIAL_ERRORS = new HashSet<>();
    public static boolean useThread = true;
    public static List<FoliageReplacerBase> listFoliageReplacers = new ArrayList<>();
    public static ConcurrentHashMap<BlockPos, FoliageLocationData> lookupPosToFoliage = new ConcurrentHashMap<>();
    public static ModelLoader modelLoader;
    public static Map<ResourceLocation, IBakedModel> modelRegistry;
    public static HashMap<ModelResourceLocation, IBakedModel> lookupBackupReplacedModels = new HashMap<>();

    @SuppressWarnings("unchecked")
    private static Map<ResourceLocation, IUnbakedModel> getTopLevelModels(ModelLoader modelLoader) {
        try {
            return ObfuscationReflectionHelper.getPrivateValue(
                    net.minecraft.client.renderer.model.ModelBakery.class,
                    modelLoader,
                    "field_217851_H"
            );
        } catch (Exception e) {
            Weather2.error("Failed to reflect topLevelModels: " + e.getMessage());
            return null;
        }
    }

    public static void modelBakeEvent(ModelBakeEvent event) {
        modelLoader = event.getModelLoader();
        modelRegistry = event.getModelRegistry();

        for (FoliageReplacerBase replacer : listFoliageReplacers) {
            for (TextureAtlasSprite sprite : replacer.sprites) {
                MeshBufferManagerFoliage.setupMeshIfMissing(sprite);
            }
        }

        processModels();
    }

    public static void liveReloadModels() {
        setupReplacers();
        processModels();
        Minecraft.getInstance().levelRenderer.allChanged();
    }

    public static void processModels() {

        if (modelLoader == null || modelRegistry == null) {
            Weather2.error("modelLoader or modelRegistry null, aborting");
            return;
        }

        boolean hackyLiveReplace = false;

        boolean replaceVanillaModels = ConfigCoroUtilLegacy.foliageShaders && EventHandler.queryUseOfShaders() && !ConfigMisc.toaster_pc_mode;

        FoliageData.backupBakedModelStore.clear();

        if (replaceVanillaModels) {

            lookupBackupReplacedModels.clear();

            String str = "Weather2: Replacing shaderized models";
            Weather2.debug(str);

            Map<ResourceLocation, IUnbakedModel> topLevelModels = getTopLevelModels(modelLoader);
            if (topLevelModels == null) {
                return;
            }

            for (Map.Entry<ResourceLocation, IUnbakedModel> entry : topLevelModels.entrySet()) {
                ResourceLocation res = entry.getKey();
                IUnbakedModel model = entry.getValue();

                if (!(res instanceof ModelResourceLocation)) continue;
                ModelResourceLocation mrl = (ModelResourceLocation) res;

                if (model != null) {
                    try {
                        Set<net.minecraft.client.renderer.model.RenderMaterial> materials;
                        try {
                            materials = new HashSet<>(model.getMaterials(modelLoader::getModel, new HashSet<>()));
                        } catch (Exception ex) {
                            materials = new HashSet<>();
                        }

                        Set<ResourceLocation> textures = new HashSet<>();
                        for (net.minecraft.client.renderer.model.RenderMaterial mat : materials) {
                            textures.add(mat.texture());
                        }

                        if (!mrl.getVariant().equals("inventory")) {
                            escape:
                            for (FoliageReplacerBase replacer : listFoliageReplacers) {
                                for (TextureAtlasSprite sprite : replacer.sprites) {
                                    ResourceLocation spriteName = sprite.getName();
                                    for (ResourceLocation texRes : textures) {
                                        if (texRes.equals(spriteName)) {
                                            if (!res.toString().contains("flower_pot")) {

                                                IBakedModel originalModel = modelRegistry.get(res);
                                                if (originalModel != null) {
                                                    lookupBackupReplacedModels.put(mrl, originalModel);
                                                    modelRegistry.put(res, new InvisibleModelWrapper(originalModel));
                                                }

                                                break escape;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }

        } else {

            if (!hackyLiveReplace) {
                return;
            }

            if (lookupBackupReplacedModels.size() == 0) {
                return;
            }

            Map<ResourceLocation, IUnbakedModel> topLevelModels = getTopLevelModels(modelLoader);
            if (topLevelModels == null) {
                return;
            }

            for (Map.Entry<ResourceLocation, IUnbakedModel> entry : topLevelModels.entrySet()) {
                ResourceLocation res = entry.getKey();
                IUnbakedModel model = entry.getValue();

                if (!(res instanceof ModelResourceLocation)) continue;
                ModelResourceLocation mrl = (ModelResourceLocation) res;

                if (model != null) {
                    try {
                        Set<net.minecraft.client.renderer.model.RenderMaterial> materials;
                        try {
                            materials = new HashSet<>(model.getMaterials(modelLoader::getModel, new HashSet<>()));
                        } catch (Exception ex) {
                            materials = new HashSet<>();
                        }

                        Set<ResourceLocation> textures = new HashSet<>();
                        for (net.minecraft.client.renderer.model.RenderMaterial mat : materials) {
                            textures.add(mat.texture());
                        }

                        if (!mrl.getVariant().equals("inventory")) {
                            escape:
                            for (FoliageReplacerBase replacer : listFoliageReplacers) {
                                for (TextureAtlasSprite sprite : replacer.sprites) {
                                    ResourceLocation spriteName = sprite.getName();
                                    for (ResourceLocation texRes : textures) {
                                        if (texRes.equals(spriteName)) {
                                            if (!res.toString().contains("flower_pot")) {
                                                IBakedModel backup = lookupBackupReplacedModels.get(mrl);
                                                if (backup != null) {
                                                    modelRegistry.put(res, backup);
                                                }
                                                break escape;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    public static void shadersInit() {
        setupReplacers();

        Weather2.debug("Weather2: Setting up meshes for foliage shader");

        for (FoliageReplacerBase replacer : listFoliageReplacers) {
            for (TextureAtlasSprite sprite : replacer.sprites) {
                MeshBufferManagerFoliage.setupMeshIfMissing(sprite);
            }
        }
    }

    public static void shadersReset() {
        lookupPosToFoliage.clear();
    }

    public static void setupReplacers(AtlasTexture atlas) {
        Weather2.debug("Weather2: Setting up foliage replacers");
        listFoliageReplacers.clear();

        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.OAK_SAPLING.defaultBlockState())
                .setSprite(getMeshAndSetupSprite(atlas, "minecraft:block/oak_sapling"))
                .setStateSensitive(false)
                .setBiomeColorize(false));

        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.SPRUCE_SAPLING.defaultBlockState())
                .setSprite(getMeshAndSetupSprite(atlas, "minecraft:block/spruce_sapling"))
                .setStateSensitive(false)
                .setBiomeColorize(false));

        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.BIRCH_SAPLING.defaultBlockState())
                .setSprite(getMeshAndSetupSprite(atlas, "minecraft:block/birch_sapling"))
                .setStateSensitive(false)
                .setBiomeColorize(false));

        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.JUNGLE_SAPLING.defaultBlockState())
                .setSprite(getMeshAndSetupSprite(atlas, "minecraft:block/jungle_sapling"))
                .setStateSensitive(false)
                .setBiomeColorize(false));

        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.ACACIA_SAPLING.defaultBlockState())
                .setSprite(getMeshAndSetupSprite(atlas, "minecraft:block/acacia_sapling"))
                .setStateSensitive(false)
                .setBiomeColorize(false));

        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.DARK_OAK_SAPLING.defaultBlockState())
                .setSprite(getMeshAndSetupSprite(atlas, "minecraft:block/dark_oak_sapling"))
                .setStateSensitive(false)
                .setBiomeColorize(false));

        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.DEAD_BUSH.defaultBlockState())
                .setSprite(getMeshAndSetupSprite(atlas, "minecraft:block/dead_bush"))
                .setStateSensitive(false)
                .setRandomizeCoord(false)
                .setBiomeColorize(false));

        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.GRASS.defaultBlockState())
                .setSprite(getMeshAndSetupSprite(atlas, "minecraft:block/grass"))
                .setStateSensitive(false)
                .setRandomizeCoord(false)
                .setBiomeColorize(true));

        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.FERN.defaultBlockState())
                .setSprite(getMeshAndSetupSprite(atlas, "minecraft:block/fern"))
                .setStateSensitive(false)
                .setRandomizeCoord(false)
                .setBiomeColorize(true));

        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.DANDELION.defaultBlockState())
                .setSprite(getMeshAndSetupSprite(atlas, "minecraft:block/dandelion"))
                .setRandomizeCoord(false)
                .setBiomeColorize(false));

        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.ALLIUM.defaultBlockState())
                .setSprite(getMeshAndSetupSprite(atlas, "minecraft:block/allium"))
                .setRandomizeCoord(false)
                .setStateSensitive(false)
                .setBiomeColorize(false));

        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.BLUE_ORCHID.defaultBlockState())
                .setSprite(getMeshAndSetupSprite(atlas, "minecraft:block/blue_orchid"))
                .setRandomizeCoord(false)
                .setStateSensitive(false)
                .setBiomeColorize(false));

        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.AZURE_BLUET.defaultBlockState())
                .setSprite(getMeshAndSetupSprite(atlas, "minecraft:block/azure_bluet"))
                .setRandomizeCoord(false)
                .setStateSensitive(false)
                .setBiomeColorize(false));

        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.ORANGE_TULIP.defaultBlockState())
                .setSprite(getMeshAndSetupSprite(atlas, "minecraft:block/orange_tulip"))
                .setRandomizeCoord(false)
                .setStateSensitive(false)
                .setBiomeColorize(false));

        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.OXEYE_DAISY.defaultBlockState())
                .setSprite(getMeshAndSetupSprite(atlas, "minecraft:block/oxeye_daisy"))
                .setRandomizeCoord(false)
                .setStateSensitive(false)
                .setBiomeColorize(false));

        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.PINK_TULIP.defaultBlockState())
                .setSprite(getMeshAndSetupSprite(atlas, "minecraft:block/pink_tulip"))
                .setRandomizeCoord(false)
                .setStateSensitive(false)
                .setBiomeColorize(false));

        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.POPPY.defaultBlockState())
                .setSprite(getMeshAndSetupSprite(atlas, "minecraft:block/poppy"))
                .setRandomizeCoord(false)
                .setStateSensitive(false)
                .setBiomeColorize(false));

        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.RED_TULIP.defaultBlockState())
                .setSprite(getMeshAndSetupSprite(atlas, "minecraft:block/red_tulip"))
                .setRandomizeCoord(false)
                .setStateSensitive(false)
                .setBiomeColorize(false));

        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.WHITE_TULIP.defaultBlockState())
                .setSprite(getMeshAndSetupSprite(atlas, "minecraft:block/white_tulip"))
                .setRandomizeCoord(false)
                .setStateSensitive(false)
                .setBiomeColorize(false));

        for (int i = 0; i < 8; i++) {
            int temp = i;
            listFoliageReplacers.add(new FoliageReplacerCross(Blocks.WHEAT.defaultBlockState())
                    .setBaseMaterial(Material.DIRT)
                    .setSprite(getMeshAndSetupSprite(atlas, "minecraft:block/wheat_stage" + temp))
                    .setRandomizeCoord(false)
                    .setStateSensitive(true)
                    .addComparable(CropsBlock.AGE, i));
        }

        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.SUGAR_CANE.defaultBlockState(), -1)
                .setSprite(getMeshAndSetupSprite(atlas, "minecraft:block/sugar_cane"))
                .setBaseMaterial(Material.SAND)
                .setBiomeColorize(true)
                .setRandomizeCoord(false)
                .setLooseness(0.3F));

        HashMap<Integer, Integer> lookupStateToModel = new HashMap<>();
        lookupStateToModel.put(0, 0);
        lookupStateToModel.put(1, 0);
        lookupStateToModel.put(2, 1);
        lookupStateToModel.put(3, 1);
        lookupStateToModel.put(4, 2);
        lookupStateToModel.put(5, 2);
        lookupStateToModel.put(6, 2);
        lookupStateToModel.put(7, 3);

        for (Map.Entry<Integer, Integer> entrySet : lookupStateToModel.entrySet()) {
            listFoliageReplacers.add(new FoliageReplacerCross(Blocks.CARROTS.defaultBlockState())
                    .setBaseMaterial(Material.DIRT)
                    .setSprite(getMeshAndSetupSprite(atlas, "minecraft:block/carrots_stage" + entrySet.getValue()))
                    .setRandomizeCoord(false)
                    .setStateSensitive(true)
                    .addComparable(CropsBlock.AGE, entrySet.getKey()));
        }

        for (Map.Entry<Integer, Integer> entrySet : lookupStateToModel.entrySet()) {
            listFoliageReplacers.add(new FoliageReplacerCross(Blocks.POTATOES.defaultBlockState())
                    .setBaseMaterial(Material.DIRT)
                    .setSprite(getMeshAndSetupSprite(atlas, "minecraft:block/potatoes_stage" + entrySet.getValue()))
                    .setRandomizeCoord(false)
                    .setStateSensitive(true)
                    .addComparable(CropsBlock.AGE, entrySet.getKey()));
        }

        for (int i = 0; i < 4; i++) {
            listFoliageReplacers.add(new FoliageReplacerCross(Blocks.BEETROOTS.defaultBlockState())
                    .setBaseMaterial(Material.DIRT)
                    .setSprite(getMeshAndSetupSprite(atlas, "minecraft:block/beetroots_stage" + i))
                    .setRandomizeCoord(false)
                    .setStateSensitive(true)
                    .addComparable(BeetrootBlock.AGE, i));
        }

        List<TextureAtlasSprite> sprites;

        sprites = new ArrayList<>();
        sprites.add(getMeshAndSetupSprite(atlas, "minecraft:block/tall_grass_bottom"));
        sprites.add(getMeshAndSetupSprite(atlas, "minecraft:block/tall_grass_top"));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.TALL_GRASS.defaultBlockState(), 2)
                .setSprites(sprites)
                .setStateSensitive(false)
                .setBiomeColorize(true));

        sprites = new ArrayList<>();
        sprites.add(getMeshAndSetupSprite(atlas, "minecraft:block/rose_bush_bottom"));
        sprites.add(getMeshAndSetupSprite(atlas, "minecraft:block/rose_bush_top"));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.ROSE_BUSH.defaultBlockState(), 2)
                .setSprites(sprites)
                .setBiomeColorize(false)
                .setStateSensitive(false));

        sprites = new ArrayList<>();
        sprites.add(getMeshAndSetupSprite(atlas, "minecraft:block/large_fern_bottom"));
        sprites.add(getMeshAndSetupSprite(atlas, "minecraft:block/large_fern_top"));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.LARGE_FERN.defaultBlockState(), 2)
                .setSprites(sprites)
                .setBiomeColorize(true)
                .setStateSensitive(false));

        sprites = new ArrayList<>();
        sprites.add(getMeshAndSetupSprite(atlas, "minecraft:block/peony_bottom"));
        sprites.add(getMeshAndSetupSprite(atlas, "minecraft:block/peony_top"));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.PEONY.defaultBlockState(), 2)
                .setSprites(sprites)
                .setBiomeColorize(false)
                .setStateSensitive(false));

        sprites = new ArrayList<>();
        sprites.add(getMeshAndSetupSprite(atlas, "minecraft:block/lilac_bottom"));
        sprites.add(getMeshAndSetupSprite(atlas, "minecraft:block/lilac_top"));
        listFoliageReplacers.add(new FoliageReplacerCross(Blocks.LILAC.defaultBlockState(), 2)
                .setSprites(sprites)
                .setBiomeColorize(false)
                .setStateSensitive(false));

        if (ConfigFoliage.enable_extra_grass) {
            listFoliageReplacers.add(new FoliageReplacerCrossGrass(Blocks.AIR.defaultBlockState()) {
                @Override
                public boolean isActive() {
                    return ConfigFoliage.enable_extra_grass;
                }
            }
                    .setSprite(getMeshAndSetupSprite(atlas, ExtendedRenderer.modid + ":particles/grass"))
                    .setRandomizeCoord(true)
                    .setBiomeColorize(true));
        }

        boolean extraLeaves = false;

        if (extraLeaves) {
            listFoliageReplacers.add(new FoliageReplacerCrossLeaves(Blocks.OAK_LEAVES.defaultBlockState())
                    .setSprite(getMeshAndSetupSprite(atlas, "minecraft:block/grass"))
                    .setBiomeColorize(true));
        }
    }

    public static void setupReplacers() {
        AtlasTexture blockAtlas = (AtlasTexture) Minecraft.getInstance()
                .getTextureManager()
                .getTexture(AtlasTexture.LOCATION_BLOCKS);
        setupReplacers(blockAtlas);
    }

    public static TextureAtlasSprite getMeshAndSetupSprite(AtlasTexture atlas, String spriteLoc) {
        ResourceLocation loc = new ResourceLocation(spriteLoc);
        return atlas.getSprite(loc);
    }

    public static TextureAtlasSprite getMeshAndSetupSprite(String spriteLoc) {
        ResourceLocation loc = new ResourceLocation(spriteLoc);
        AtlasTexture blockAtlas = (AtlasTexture) Minecraft.getInstance()
                .getTextureManager()
                .getTexture(AtlasTexture.LOCATION_BLOCKS);
        return blockAtlas.getSprite(loc);
    }

    public static boolean tickClientCloseToPlayer() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level != null && mc.player != null && EZConfigParser.isEffectsEnabled(mc.level.dimension().location().toString())) {
            return tickFoliage(5, false);
        } else {
            return true;
        }
    }

    public static boolean tickClientThreaded() {
        Minecraft mc = Minecraft.getInstance();

        if (mc.level != null && mc.player != null && EZConfigParser.isEffectsEnabled(mc.level.dimension().location().toString())) {
            return tickFoliage(ConfigFoliage.shader_range, true);
        } else {
            return true;
        }
    }

    @SuppressWarnings("finally")
    public static boolean tickFoliage(int radialRange, boolean trimRange) {
        if (ExtendedRenderer.foliageRenderer.lockVBO2.tryLock()) {
            try {
                return profileForFoliageShader(radialRange, trimRange);
            } finally {
                ExtendedRenderer.foliageRenderer.lockVBO2.unlock();
                return true;
            }
        } else {
            return false;
        }
    }

    @SuppressWarnings("finally")
    public static boolean profileForFoliageShader(int radialRange, boolean trimRange) {

        World world = Minecraft.getInstance().level;
        Entity entityIn = Minecraft.getInstance().player;
        BlockPos pos = entityIn.blockPosition();

        boolean add = true;
        boolean trim = true;

        int xzRange = radialRange;
        int yRange = radialRange;
        Random rand = new Random();

        double centerX = entityIn.getX();
        double centerY = entityIn.getY();
        double centerZ = entityIn.getZ();

        for (TextureAtlasSprite sprite : ExtendedRenderer.foliageRenderer.foliage.keySet()) {
            InstancedMeshFoliage mesh = MeshBufferManagerFoliage.getMesh(sprite);
            mesh.lastAdditionCount = 0;
            mesh.lastRemovalCount = 0;
        }

        if (trim) {
            Iterator<Map.Entry<BlockPos, FoliageLocationData>> it = lookupPosToFoliage.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<BlockPos, FoliageLocationData> entry = it.next();
                if (!entry.getValue().foliageReplacer.isActive() || !entry.getValue().foliageReplacer.validFoliageSpot(world, entry.getKey().below())) {
                    it.remove();
                    for (Foliage entry2 : entry.getValue().listFoliage) {
                        entry.getValue().foliageReplacer.markMeshesDirty();
                        ExtendedRenderer.foliageRenderer.getFoliageForSprite(entry2.particleTexture).remove(entry2);
                        MeshBufferManagerFoliage.getMesh(entry2.particleTexture).lastRemovalCount++;
                    }
                } else if (trimRange && entry.getKey().distSqr(centerX, centerY, centerZ, false) > radialRange * radialRange) {
                    it.remove();
                    for (Foliage entry2 : entry.getValue().listFoliage) {
                        entry.getValue().foliageReplacer.markMeshesDirty();
                        ExtendedRenderer.foliageRenderer.getFoliageForSprite(entry2.particleTexture).remove(entry2);
                        MeshBufferManagerFoliage.getMesh(entry2.particleTexture).lastRemovalCount++;
                    }
                }
            }
        }

        if (add) {
            for (int x = -xzRange; x <= xzRange; x++) {
                for (int z = -xzRange; z <= xzRange; z++) {
                    for (int y = -yRange; y <= yRange; y++) {
                        BlockPos posScan = pos.offset(x, y, z);
                        if (!lookupPosToFoliage.containsKey(posScan)) {
                            if (posScan.distSqr(centerX, centerY, centerZ, false) <= radialRange * radialRange) {

                                boolean tryAll = true;

                                if (tryAll) {
                                    for (FoliageReplacerBase replacer : listFoliageReplacers) {
                                        if (replacer.isActive() && replacer.validFoliageSpot(entityIn.level, posScan.below())) {
                                            replacer.addForPos(entityIn.level, posScan);
                                            replacer.markMeshesDirty();

                                            for (TextureAtlasSprite sprite : replacer.sprites) {
                                                MeshBufferManagerFoliage.getMesh(sprite).lastAdditionCount++;
                                            }
                                        }
                                    }
                                } else {
                                    int randTry = rand.nextInt(listFoliageReplacers.size());
                                    FoliageReplacerBase replacer = listFoliageReplacers.get(randTry);
                                    if (replacer.isActive() && replacer.validFoliageSpot(entityIn.level, posScan.below())) {
                                        replacer.addForPos(entityIn.level, posScan);
                                        replacer.markMeshesDirty();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        try {
            for (Map.Entry<TextureAtlasSprite, List<Foliage>> entry : ExtendedRenderer.foliageRenderer.foliage.entrySet()) {
                InstancedMeshFoliage mesh = MeshBufferManagerFoliage.getMesh(entry.getKey());

                if (mesh.dirtyVBO2Flag) {
                    mesh.interpPosXThread = entityIn.getX();
                    mesh.interpPosYThread = entityIn.getY();
                    mesh.interpPosZThread = entityIn.getZ();

                    updateVBO2Threaded(entry.getKey());
                }
            }
        } finally {
            return true;
        }
    }

    public static void markMeshDirty(TextureAtlasSprite sprite, boolean flag) {
        InstancedMeshFoliage mesh = MeshBufferManagerFoliage.getMesh(sprite);

        if (mesh != null) {
            mesh.dirtyVBO2Flag = flag;
        } else {
            Weather2.debug("MESH NULL HERE, FIX INIT ORDER");
        }
    }

    @SuppressWarnings("static-access")
    public static void updateVBO2Threaded(TextureAtlasSprite sprite) {

        Minecraft mc = Minecraft.getInstance();
        Entity entityIn = mc.getCameraEntity();

        float partialTicks = 1F;

        InstancedMeshFoliage mesh = MeshBufferManagerFoliage.getMesh(sprite);
        if (mesh == null) {
            return;
        }

        int lastPos = mesh.curBufferPosVBO2;

        mesh.curBufferPosVBO2 = 0;
        mesh.instanceDataBufferVBO2.clear();

        int guessAtExtraMeshesPerFoliage = 4;
        int extraMeshes = (mesh.lastAdditionCount * guessAtExtraMeshesPerFoliage) - (mesh.lastRemovalCount * guessAtExtraMeshesPerFoliage);

        if (lastPos + extraMeshes > mesh.numInstances) {
            if (mesh.numInstances * 4 < lastPos + extraMeshes) {
                mesh.numInstances = (int) (Math.ceil((float) (lastPos + extraMeshes) / 10000F) * 10000F);
            } else {
                mesh.numInstances *= 4;
            }

            mesh.instanceDataBufferVBO2 = BufferUtils.createFloatBuffer(mesh.numInstances * InstancedMeshFoliage.INSTANCE_SIZE_FLOATS_SELDOM);
            mesh.instanceDataBufferVBO2.clear();

            mesh.instanceDataBufferVBO1 = BufferUtils.createFloatBuffer(mesh.numInstances * InstancedMeshFoliage.INSTANCE_SIZE_FLOATS);
        }

        for (Foliage foliage : ExtendedRenderer.foliageRenderer.getFoliageForSprite(sprite)) {
            foliage.updateQuaternion(entityIn);
            foliage.renderForShaderVBO2(mesh, ExtendedRenderer.foliageRenderer.transformation, null, entityIn, partialTicks);
        }

        if (FoliageRenderer.testStaticLimit) {
            mesh.instanceDataBufferVBO2.limit(30000 * mesh.INSTANCE_SIZE_FLOATS_SELDOM);
        } else {
            mesh.instanceDataBufferVBO2.limit(mesh.curBufferPosVBO2 * mesh.INSTANCE_SIZE_FLOATS_SELDOM);
        }
    }

    public static void addForPos(FoliageReplacerBase replacer, int height, BlockPos pos) {
        addForPos(replacer, height, pos, new Vec3(0.4, 0, 0.4), true, 0);
    }

    public static void addForPos(FoliageReplacerBase replacer, int height, BlockPos pos, Vec3 randPosVar, boolean biomeColorize) {
        addForPos(replacer, height, pos, randPosVar, biomeColorize, 0);
    }

    public static void addForPos(FoliageReplacerBase replacer, int height, BlockPos pos, Vec3 randPosVar, boolean biomeColorize, int colorizeOffset) {
        addForPos(replacer, height, pos, randPosVar, biomeColorize, colorizeOffset, null);
    }

    public static void addForPos(FoliageReplacerBase replacer, int height, BlockPos pos, Vec3 randPosVar, boolean biomeColorize, int colorizeOffset, Vec3 extraPos) {

        World world = Minecraft.getInstance().level;

        Random rand = new Random();
        FoliageLocationData data = new FoliageLocationData(replacer);

        int heightIndex;

        float randX = 0;
        float randZ = 0;
        if (randPosVar != null) {
            randX = (rand.nextFloat() - rand.nextFloat()) * (float) randPosVar.xCoord;
            randZ = (rand.nextFloat() - rand.nextFloat()) * (float) randPosVar.zCoord;
        }

        int clutterSize = 2;
        int meshesPerLayer = 2;

        if (replacer instanceof FoliageReplacerCross) {
            clutterSize = 2 * height;
        }

        if (replacer instanceof FoliageReplacerCrossGrass) {
            clutterSize = 4;
        }

        for (int i = 0; i < clutterSize; i++) {
            heightIndex = i / meshesPerLayer;

            if (replacer instanceof FoliageReplacerCrossGrass) {
                heightIndex = 0;
            }

            TextureAtlasSprite sprite = replacer.sprites.get(0);
            if (replacer instanceof FoliageReplacerCross) {
                if (heightIndex < replacer.sprites.size()) {
                    sprite = replacer.sprites.get(heightIndex);
                }
            }

            Foliage foliage = new Foliage(sprite);
            foliage.setPosition(pos.offset(0, 0, 0));
            foliage.prevPosY = foliage.posY;
            foliage.heightIndex = heightIndex;

            Vector3d vec = world.getBlockState(pos).getOffset(world, pos);
            foliage.posX += 0.5F + randX + vec.x;
            foliage.prevPosX = foliage.posX;
            foliage.posZ += 0.5F + randZ + vec.z;
            if (extraPos != null) {
                foliage.posX += extraPos.xCoord;
                foliage.posZ += extraPos.zCoord;
            }
            foliage.prevPosZ = foliage.posZ;
            foliage.rotationYaw = 0;
            foliage.rotationYaw = world.random.nextInt(360);

            foliage.rotationYaw = 45;
            if ((i + 1) % 2 == 0) {
                foliage.rotationYaw += 90;
            }

            if (replacer instanceof FoliageReplacerCrossGrass) {
                foliage.rotationYaw = 45;
                double dist = 0.17;
                if (i == 0) {
                    foliage.rotationYaw += 90;
                    foliage.posX += dist;
                    foliage.posZ += dist;
                } else if (i == 1) {
                    foliage.rotationYaw += 90;
                    foliage.posX -= dist;
                    foliage.posZ -= dist;
                } else if (i == 2) {
                    foliage.posX += dist;
                    foliage.posZ -= dist;
                } else if (i == 3) {
                    foliage.posX -= dist;
                    foliage.posZ += dist;
                }
            }

            foliage.looseness = replacer.looseness;

            foliage.particleScale /= 0.2;

            if (biomeColorize) {
                int color = Minecraft.getInstance().getBlockColors().getColor(world.getBlockState(pos.above(colorizeOffset)), world, pos.above(colorizeOffset), 0);
                foliage.particleRed = (float) (color >> 16 & 255) / 255.0F;
                foliage.particleGreen = (float) (color >> 8 & 255) / 255.0F;
                foliage.particleBlue = (float) (color & 255) / 255.0F;

                if (replacer instanceof FoliageReplacerCrossGrass) {
                    color = Minecraft.getInstance().getBlockColors().getColor(Blocks.GRASS.defaultBlockState(), world, pos.above(colorizeOffset), 0);
                    foliage.particleRed = (float) (color >> 16 & 255) / 255.0F;
                    foliage.particleGreen = (float) (color >> 8 & 255) / 255.0F;
                    foliage.particleBlue = (float) (color & 255) / 255.0F;
                }
            }

            foliage.brightnessCache = CoroUtilBlockLightCache.brightnessPlayer;

            data.listFoliage.add(foliage);
            ExtendedRenderer.foliageRenderer.getFoliageForSprite(sprite).add(foliage);
        }

        lookupPosToFoliage.put(pos, data);
    }

    @Override
    public void run() {
        if (useThread) {
            while (true) {
                try {
                    if (ConfigCoroUtilLegacy.foliageShaders && RotatingParticleManager.useShaders && !ConfigMisc.toaster_pc_mode) {
                        boolean gotLock = tickClientThreaded();
                        if (gotLock) {
                            Thread.sleep(ConfigFoliage.shader_process_delay);
                        } else {
                            Thread.sleep(20);
                        }
                    } else {
                        Thread.sleep(5000);
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }
    }

    public void addForPosSeaweed(BlockPos pos) {

        World world = Minecraft.getInstance().level;

        Random rand = new Random();
        BlockState state = world.getBlockState(pos.below());
        List<Foliage> listClutter = new ArrayList<>();

        int heightIndex = 0;

        float variance = 0.4F;
        float randX = (rand.nextFloat() - rand.nextFloat()) * variance;
        float randZ = (rand.nextFloat() - rand.nextFloat()) * variance;

        int clutterSize = 14;
        clutterSize = rand.nextInt(7) * 2;

        for (int i = 0; i < clutterSize; i++) {
            heightIndex = i / 2;
            TextureAtlasSprite sprite = ParticleRegistry.listSeaweed.get(heightIndex);
            Foliage foliage = new Foliage(sprite);
            foliage.setPosition(pos.offset(0, 0, 0));
            foliage.posY += 0.0F;
            foliage.prevPosY = foliage.posY;
            foliage.heightIndex = heightIndex;

            foliage.posX += 0.5F + randX;
            foliage.prevPosX = foliage.posX;
            foliage.posZ += 0.5F + randZ;
            foliage.prevPosZ = foliage.posZ;
            foliage.rotationYaw = 0;
            foliage.rotationYaw = world.random.nextInt(360);

            foliage.rotationYaw = 45;
            if ((i + 1) % 2 == 0) {
                foliage.rotationYaw += 90;
            }

            foliage.rotationYaw = 0;
            if ((i + 1) % 2 == 0) {
                foliage.rotationYaw = 1;
            }

            foliage.particleScale /= 0.2;

            int color = Minecraft.getInstance().getBlockColors().getColor(state, world, pos.below(), 0);
            foliage.particleRed = (float) (color >> 16 & 255) / 255.0F;
            foliage.particleGreen = (float) (color >> 8 & 255) / 255.0F;
            foliage.particleBlue = (float) (color & 255) / 255.0F;

            foliage.particleRed = 1F;
            foliage.particleGreen = 1F;
            foliage.particleBlue = 1F;

            foliage.brightnessCache = CoroUtilBlockLightCache.brightnessPlayer;

            listClutter.add(foliage);
            ExtendedRenderer.foliageRenderer.getFoliageForSprite(sprite).add(foliage);
        }
    }
}