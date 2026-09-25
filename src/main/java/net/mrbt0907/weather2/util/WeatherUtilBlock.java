package net.mrbt0907.weather2.util;

import net.corosus.coroutillegacy.block.BlockRepairingBlock;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.WeatherAPI;
import net.mrbt0907.weather2.block.BlockSandLayer;
import net.mrbt0907.weather2.config.ConfigGrab;
import net.mrbt0907.weather2.registry.BlockRegistry;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.weather.storm.StormObject;

import java.util.ArrayList;
import java.util.List;


public class WeatherUtilBlock {
    public static int layerableHeightPropMax = 8;

    public static boolean safeReplaceCheck(BlockState state, World world, BlockPos pos) {
        try {
            return state.getMaterial().isReplaceable();
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isReplacable(BlockState state, boolean includeReplaceableBlocks) {
        Material material = state.getMaterial();
        if (includeReplaceableBlocks)
            return material.isReplaceable();
        return material.isLiquid()
                || material == Material.AIR
                || material == Material.PORTAL
                || material == Material.BARRIER
                || material == Material.STRUCTURAL_AIR
                || material == Material.PLANT
                || material == Material.REPLACEABLE_PLANT
                || material == Material.WATER_PLANT
                || material == Material.REPLACEABLE_WATER_PLANT;
    }

    public static void fillAgainstWallSmoothly(World world, Vec3 posSource, float directionYaw, float scanDistance, float fillRadius, Block blockLayerable) {
        WeatherUtilBlock.fillAgainstWallSmoothly(world, posSource, directionYaw, scanDistance, fillRadius, blockLayerable, 4);
    }

    public static void fillAgainstWallSmoothly(World world, Vec3 posSource, float directionYaw, float scanDistance, float fillRadius, Block blockLayerable, int heightDiff) {
        BlockState stateTest = ChunkUtils.getBlockState(world, posSource.toBlockPos());
        if (stateTest.getBlock() == blockLayerable) {
            int heightTest = WeatherUtilBlock.getHeightForAnyBlock(stateTest);
            if (heightTest < 8) {
            }
        }

        BlockPos posSourcei = posSource.toBlockPos();
        int y = posSourcei.getY();
        float tickStep = 0.75F;

        Vec3 posLastNonWall = posSource.copy();
        Vec3 posWall = null;
        BlockPos lastScannedPosXZ = null;
        int previousBlockHeight = 0;

        for (float i = 0; i < scanDistance; i += tickStep) {
            double vecX = (-Maths.fastSin(Math.toRadians(directionYaw)) * (i));
            double vecZ = (Maths.fastCos(Math.toRadians(directionYaw)) * (i));
            int x = MathHelper.floor(posSource.posX + vecX);
            int z = MathHelper.floor(posSource.posZ + vecZ);
            BlockPos pos = new BlockPos(x, y, z);
            BlockPos posXZ = new BlockPos(x, 0, z);
            BlockState state = ChunkUtils.getBlockState(world, pos);

            if (!posXZ.equals(lastScannedPosXZ)) {
                lastScannedPosXZ = new BlockPos(posXZ);
                VoxelShape shape = state.getShape(world, pos);
                List<AxisAlignedBB> listAABBCollision = new ArrayList<>();
                if (!shape.isEmpty()) listAABBCollision.add(shape.bounds().move(pos));

                if (state.getMaterial() != Material.AIR && state.getMaterial() != Material.PLANT && !WeatherUtilBlock.safeReplaceCheck(state, world, pos) && !listAABBCollision.isEmpty()) {
                    BlockPos posUp = new BlockPos(x, y + 1, z);
                    BlockState stateUp = ChunkUtils.getBlockState(world, posUp);
                    if (stateUp.getMaterial() == Material.AIR) {
                        int height = WeatherUtilBlock.getHeightForAnyBlock(state);
                        if (height - previousBlockHeight <= heightDiff) {
                            if (height == 8) {
                                previousBlockHeight = 0;
                                y++;
                            } else {
                                previousBlockHeight = height;
                            }
                            posLastNonWall = new Vec3(posSource.posX + vecX, y, posSource.posZ + vecZ);
                            continue;
                        } else {
                            posWall = new Vec3(posSource.posX + vecX, y, posSource.posZ + vecZ);
                            break;
                        }
                    } else {
                        posWall = new Vec3(posSource.posX + vecX, y, posSource.posZ + vecZ);
                        break;
                    }
                } else {
                    posLastNonWall = new Vec3(posSource.posX + vecX, y, posSource.posZ + vecZ);
                }
            } else {
                continue;
            }
        }

        if (posWall != null) {
            BlockState state = ChunkUtils.getBlockState(world, posWall.toBlockPos());
            BlockState state1 = ChunkUtils.getBlockState(world, posLastNonWall.toBlockPos().offset(1, 0, 0));
            BlockState state22 = ChunkUtils.getBlockState(world, posLastNonWall.toBlockPos().offset(-1, 0, 0));
            BlockState state3 = ChunkUtils.getBlockState(world, posLastNonWall.toBlockPos().offset(0, 0, 1));
            BlockState state4 = ChunkUtils.getBlockState(world, posLastNonWall.toBlockPos().offset(0, 0, -1));
            if (state.getBlock() == Blocks.CACTUS || state1.getBlock() == Blocks.CACTUS ||
                    state22.getBlock() == Blocks.CACTUS || state3.getBlock() == Blocks.CACTUS || state4.getBlock() == Blocks.CACTUS)
                return;
            BlockPos pos2 = new BlockPos(posLastNonWall.posX, posLastNonWall.posY, posLastNonWall.posZ);
            BlockState state2 = ChunkUtils.getBlockState(world, pos2);
            if (state2.getMaterial() == Material.WATER || state2.getMaterial() == Material.LAVA) return;
            WeatherUtilBlock.trySpreadOnPos2(world, new BlockPos(posLastNonWall.posX, posLastNonWall.posY, posLastNonWall.posZ), 1, 1, 10, blockLayerable);
        }
    }

    public static void fillAgainstWall(World world, Vec3 posSource, float directionYaw, float scanDistance, float fillRadius, Block blockLayerable) {
        float tickStep = 0.75F;
        int y = (int) posSource.posY;
        @SuppressWarnings("unused") float startScan = scanDistance;
        Vec3 posLastNonWall = posSource.copy();
        Vec3 posWall = null;

        for (float i = 0; i < scanDistance; i += tickStep) {
            double vecX = (-Maths.fastSin(Math.toRadians(directionYaw)) * (i));
            double vecZ = (Maths.fastCos(Math.toRadians(directionYaw)) * (i));
            int x = MathHelper.floor(posSource.posX + vecX);
            int z = MathHelper.floor(posSource.posZ + vecZ);
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = ChunkUtils.getBlockState(world, pos);
            if (state.getMaterial() != Material.AIR) {
                startScan = i;
                posWall = new Vec3(posSource.posX + vecX, y, posSource.posZ + vecZ);
                break;
            } else {
                posLastNonWall = new Vec3(posSource.posX + vecX, y, posSource.posZ + vecZ);
            }
        }

        if (posWall != null) {
            BlockPos posCheck = new BlockPos(posWall.toBlockPos());
            int heightOfWall = 0;
            int heightNeeded = 2;
            while (heightOfWall++ < heightNeeded) {
                posCheck = posCheck.offset(0, 1, 0);
                BlockState stateCheck = ChunkUtils.getBlockState(world, posCheck);
                if (!stateCheck.isFaceSturdy(world, posCheck, Direction.UP)) break;
            }
            if (heightOfWall >= heightNeeded)
                WeatherUtilBlock.trySpreadOnPos2(world, new BlockPos(posLastNonWall.posX, posLastNonWall.posY, posLastNonWall.posZ), 4, 2, 10, blockLayerable);
        }
    }

    public static void floodAreaWithLayerableBlock(World world, Vec3 posSource, float directionYaw, float scanDistance, float fillRadius, Block blockLayerable, int amountToTakeOrFill) {
        WeatherUtilBlock.floodAreaWithLayerableBlock(world, posSource, directionYaw, scanDistance, fillRadius, -1, blockLayerable, amountToTakeOrFill);
    }

    public static void floodAreaWithLayerableBlock(World world, Vec3 posSource, float directionYaw, float scanDistance, float fillRadius, float takeRadius, Block blockLayerable, int amountToTakeOrRelocate) {
        float tickStep = 0.75F;
        BlockPos posSourcei = posSource.toBlockPos();
        int y = (int) posSource.posY;
        @SuppressWarnings("unused") float startScan = scanDistance;
        Vec3 posLastNonWall = posSource.copy();
        @SuppressWarnings("unused") Vec3 posWall = null;

        for (float i = 0; i < scanDistance; i += tickStep) {
            double vecX = (-Maths.fastSin(Math.toRadians(directionYaw)) * (i));
            double vecZ = (Maths.fastCos(Math.toRadians(directionYaw)) * (i));
            int x = MathHelper.floor(posSource.posX + vecX);
            int z = MathHelper.floor(posSource.posZ + vecZ);
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = ChunkUtils.getBlockState(world, pos);
            if (state.getMaterial() != Material.AIR) {
                startScan = i;
                posWall = new Vec3(posSource.posX + vecX, y, posSource.posZ + vecZ);
                break;
            } else {
                posLastNonWall = new Vec3(posSource.posX + vecX, y, posSource.posZ + vecZ);
            }
        }

        double distFromSourceToWall = posSource.distanceSq(posLastNonWall);
        boolean doRadius = !(takeRadius != -1 && distFromSourceToWall <= 2);

        float angleScanResolution = 1;
        float spreadDist = fillRadius;
        int maxFallDist = 20;
        int amountToTakePerXZ = 2;
        int amountToAddPerXZ = 2;

        int amountWeHave = takeRadius != -1
                ? WeatherUtilBlock.tryTakeFromPos(world, posSourcei, 0, amountToTakePerXZ, maxFallDist, blockLayerable)
                : amountToTakeOrRelocate;

        List<BlockPos> listProcessedFilter = new ArrayList<>();

        if (doRadius) {
            for (float i = 1; i < takeRadius; i += 0.75F) {
                for (float angle = 0; angle <= 180; angle += angleScanResolution) {
                    for (int mode = 0; mode <= 1; mode++) {
                        float om = mode == 1 ? -1F : 1F;
                        double vecX = (-Maths.fastSin(Math.toRadians(directionYaw - (angle * om))) * (i));
                        double vecZ = (Maths.fastCos(Math.toRadians(directionYaw - (angle * om))) * (i));
                        BlockPos pos = new BlockPos(MathHelper.floor(posSource.posX + vecX), (int) posSource.posY, MathHelper.floor(posSource.posZ + vecZ));
                        if (!listProcessedFilter.contains(pos)) {
                            listProcessedFilter.add(pos);
                            amountWeHave = WeatherUtilBlock.tryTakeFromPos(world, pos, amountWeHave, amountToTakePerXZ, maxFallDist, blockLayerable);
                        }
                    }
                }
            }
        }

        listProcessedFilter.clear();
        amountWeHave = WeatherUtilBlock.trySpreadOnPos2(world, new BlockPos(posLastNonWall.posX, posLastNonWall.posY, posLastNonWall.posZ), amountWeHave, amountToAddPerXZ, maxFallDist, blockLayerable);

        if (doRadius) {
            for (float i = 1; i < spreadDist && amountWeHave > 0; i += 0.75F) {
                int amountToAddBasedOnDist = Math.max((int) (((float) WeatherUtilBlock.layerableHeightPropMax + 1F) - (i * 1.5F)), 1);
                amountToAddBasedOnDist = 2;
                for (float angle = 0; angle <= 180 && amountWeHave > 0; angle += angleScanResolution) {
                    for (int mode = 0; mode <= 1 && amountWeHave > 0; mode++) {
                        float om = mode == 1 ? -1F : 1F;
                        double vecX = (-Maths.fastSin(Math.toRadians(directionYaw - (angle * om))) * (i));
                        double vecZ = (Maths.fastCos(Math.toRadians(directionYaw - (angle * om))) * (i));
                        int x = MathHelper.floor(posLastNonWall.posX + vecX);
                        int z = MathHelper.floor(posLastNonWall.posZ + vecZ);
                        BlockPos pos = new BlockPos(x, (int) posLastNonWall.posY, z);
                        Vector3d sourceTest = posSource.addVector(0, 1D, 0).toVec3MC();
                        Vector3d destTest = new Vector3d(x + 0.5F, (int) posLastNonWall.posY + 1.5F, z + 0.5F);
                        BlockRayTraceResult destFound = world.clip(new RayTraceContext(sourceTest, destTest, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, null));
                        if (destFound.getType() == BlockRayTraceResult.Type.MISS && !listProcessedFilter.contains(pos)) {
                            listProcessedFilter.add(pos);
                            amountWeHave = WeatherUtilBlock.trySpreadOnPos2(world, pos, amountWeHave, amountToAddBasedOnDist, maxFallDist, blockLayerable);
                        }
                    }
                }
            }
        }

        Weather2.debug("leftover: " + amountWeHave);
    }

    public static int tryTakeFromPos(World world, BlockPos posTakeFrom, int amount, int amountAllowedToTakeForXZ, int maxDropAllowed, Block blockLayerable) {
        int amountTaken = 0;
        BlockState statePos = ChunkUtils.getBlockState(world, posTakeFrom);
        if (!WeatherUtilBlock.isLayeredOrVanillaVersionOfBlock(statePos, blockLayerable) && !statePos.isAir(world, posTakeFrom))
            return amount;

        int dropDist = 0;
        BlockPos posScan = new BlockPos(posTakeFrom);
        while (statePos.isAir(world, posScan) && dropDist++ < maxDropAllowed) {
            posScan = posScan.offset(0, -1, 0);
            statePos = ChunkUtils.getBlockState(world, posScan);
            if (!WeatherUtilBlock.isLayeredOrVanillaVersionOfBlock(statePos, blockLayerable) && !statePos.isAir(world, posScan))
                return amount;
        }

        while (amountTaken < amountAllowedToTakeForXZ) {
            int amountReturn = WeatherUtilBlock.takeHeightFromLayerableBlock(world, posScan, blockLayerable, amountAllowedToTakeForXZ);
            amountTaken += amountReturn;
            posScan = posScan.offset(0, -1, 0);
            statePos = ChunkUtils.getBlockState(world, posScan);
            if (!WeatherUtilBlock.isLayeredOrVanillaVersionOfBlock(statePos, blockLayerable)) break;
        }
        return amount + amountTaken;
    }

    public static int trySpreadOnPos2(World world, BlockPos posSpreadTo, int amount, int amountAllowedToAdd, int maxDropAllowed, Block blockLayerable) {
        if (amount <= 0) return amount;
        if (ChunkUtils.getBlockState(world, posSpreadTo.offset(0, 1, 0)).getMaterial() != Material.AIR) return amount;

        BlockPos posCheckNonAir = new BlockPos(posSpreadTo);
        BlockState stateCheckNonAir = ChunkUtils.getBlockState(world, posCheckNonAir);
        int depth = 0;

        while (stateCheckNonAir.getMaterial() == Material.AIR) {
            posCheckNonAir = posCheckNonAir.offset(0, -1, 0);
            stateCheckNonAir = ChunkUtils.getBlockState(world, posCheckNonAir);
            if (++depth > maxDropAllowed) return amount;
        }

        BlockPos posCheckPlaceable = new BlockPos(posCheckNonAir);
        BlockState stateCheckPlaceable = ChunkUtils.getBlockState(world, posCheckPlaceable);
        int distForPlaceableBlocks = 0;

        while (distForPlaceableBlocks < 10) {
            VoxelShape shape = stateCheckPlaceable.getShape(world, posCheckPlaceable);
            List<AxisAlignedBB> listAABBCollision = new ArrayList<>();
            if (!shape.isEmpty()) listAABBCollision.add(shape.bounds().move(posCheckPlaceable));

            if (stateCheckPlaceable.getBlock() != blockLayerable && WeatherUtilBlock.safeReplaceCheck(stateCheckPlaceable, world, posCheckPlaceable) && listAABBCollision.isEmpty()) {
                posCheckPlaceable = posCheckPlaceable.offset(0, -1, 0);
                stateCheckPlaceable = ChunkUtils.getBlockState(world, posCheckPlaceable);
                distForPlaceableBlocks++;
            } else if (stateCheckPlaceable.isFaceSturdy(world, posCheckPlaceable, Direction.UP) || stateCheckPlaceable.getBlock() == blockLayerable) {
                break;
            } else {
                return amount;
            }
        }

        if (distForPlaceableBlocks >= 10) return amount;
        if (!stateCheckPlaceable.isFaceSturdy(world, posCheckPlaceable, Direction.UP) && stateCheckPlaceable.getBlock() != blockLayerable) {
            Weather2.error("sandstorm: shouldnt be, failed a check somewhere!");
            return amount;
        }

        for (int i = 0; i < distForPlaceableBlocks; i++)
            ChunkUtils.setBlockState(world, posCheckNonAir.offset(0, -i, 0), Blocks.AIR.defaultBlockState());

        BlockPos posPlaceLayerable = new BlockPos(posCheckPlaceable);
        BlockState statePlaceLayerable = ChunkUtils.getBlockState(world, posPlaceLayerable);
        int amountToAdd = amountAllowedToAdd;

        while (amountAllowedToAdd > 0 && ChunkUtils.getBlockState(world, posPlaceLayerable.offset(0, 1, 0)).getMaterial() == Material.AIR) {
            if (statePlaceLayerable.getBlock() == blockLayerable && WeatherUtilBlock.getHeightForLayeredBlock(statePlaceLayerable) < WeatherUtilBlock.layerableHeightPropMax) {
                int height = WeatherUtilBlock.getHeightForLayeredBlock(statePlaceLayerable) + amountAllowedToAdd;
                if (height > WeatherUtilBlock.layerableHeightPropMax) {
                    amountAllowedToAdd = height - WeatherUtilBlock.layerableHeightPropMax;
                    height = WeatherUtilBlock.layerableHeightPropMax;
                } else {
                    amountAllowedToAdd = 0;
                }
                try {
                    ChunkUtils.setBlockState(world, posPlaceLayerable, WeatherUtilBlock.setBlockWithLayerState(blockLayerable, height));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (height == WeatherUtilBlock.layerableHeightPropMax) {
                    posPlaceLayerable = posPlaceLayerable.offset(0, 1, 0);
                    statePlaceLayerable = ChunkUtils.getBlockState(world, posPlaceLayerable);
                }
            } else if (statePlaceLayerable.isFaceSturdy(world, posPlaceLayerable, Direction.UP)) {
                posPlaceLayerable = posPlaceLayerable.offset(0, 1, 0);
                statePlaceLayerable = ChunkUtils.getBlockState(world, posPlaceLayerable);
            } else if (statePlaceLayerable.getMaterial() == Material.AIR) {
                int height = amountAllowedToAdd;
                if (height > WeatherUtilBlock.layerableHeightPropMax) {
                    amountAllowedToAdd = height - WeatherUtilBlock.layerableHeightPropMax;
                    height = WeatherUtilBlock.layerableHeightPropMax;
                } else {
                    amountAllowedToAdd = 0;
                }
                try {
                    ChunkUtils.setBlockState(world, posPlaceLayerable, WeatherUtilBlock.setBlockWithLayerState(blockLayerable, height));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (height == WeatherUtilBlock.layerableHeightPropMax) {
                    posPlaceLayerable = posPlaceLayerable.offset(0, 1, 0);
                    statePlaceLayerable = ChunkUtils.getBlockState(world, posPlaceLayerable);
                }
            } else {
                Weather2.debug("wat! - " + statePlaceLayerable);
                break;
            }
        }

        if (amountAllowedToAdd < 0) Weather2.debug("wat");
        amount -= amountToAdd - amountAllowedToAdd;
        return amount;
    }

    public static boolean isLayeredOrVanillaVersionOfBlock(BlockState state, Block blockLayerable) {
        Block block = state.getBlock();
        if (block == blockLayerable) return true;
        if (blockLayerable == BlockRegistry.sand_layer.get() && block == Blocks.SAND) return true;
        return blockLayerable == Blocks.SNOW && block == Blocks.SNOW_BLOCK;
    }

    public static int getHeightForAnyBlock(BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.SNOW) return state.getValue(SnowBlock.LAYERS).intValue();
        else if (block == BlockRegistry.sand_layer.get()) return state.getValue(BlockSandLayer.LAYERS).intValue();
        else if (block == Blocks.SAND || block == Blocks.SNOW_BLOCK) return 8;
        else if (block instanceof SlabBlock) return 4;
        else if (block == Blocks.AIR) return 0;
        else return 8;
    }

    public static int getHeightForLayeredBlock(BlockState state) {
        if (state.getBlock() == Blocks.SNOW) return state.getValue(SnowBlock.LAYERS).intValue();
        else if (state.getBlock() == BlockRegistry.sand_layer.get())
            return state.getValue(BlockSandLayer.LAYERS).intValue();
        else if (state.getBlock() == Blocks.SAND || state.getBlock() == Blocks.SNOW_BLOCK) return 8;
        else return 0;
    }

    public static BlockState setBlockWithLayerState(Block block, int height) {
        if (block == Blocks.SNOW) {
            if (height == WeatherUtilBlock.layerableHeightPropMax) return Blocks.SNOW_BLOCK.defaultBlockState();
            else return block.defaultBlockState().setValue(SnowBlock.LAYERS, height);
        } else if (block == BlockRegistry.sand_layer.get()) {
            if (height == WeatherUtilBlock.layerableHeightPropMax) return Blocks.SAND.defaultBlockState();
            else return block.defaultBlockState().setValue(BlockSandLayer.LAYERS, height);
        } else return null;
    }

    public static boolean divideToNeighborCheck(BlockState state, World worldIn, BlockPos pos, Block blockIn) {
        boolean foundSpotToSpread = false;
        int heightToUse = WeatherUtilBlock.getHeightForLayeredBlock(state);
        if (heightToUse > 2) {
            for (Direction enumfacing : Direction.Plane.HORIZONTAL) {
                if (heightToUse > 2) {
                    BlockPos posCheck = pos.relative(enumfacing);
                    BlockState stateCheck = worldIn.getBlockState(posCheck);
                    if (stateCheck.getBlock() == state.getBlock()) {
                        int heightCheck = WeatherUtilBlock.getHeightForLayeredBlock(stateCheck);
                        if (heightCheck + 2 <= heightToUse) {
                            heightToUse -= 1;
                            WeatherUtilBlock.addHeightToLayerableBLock(worldIn, posCheck, stateCheck.getBlock(), heightCheck, 1);
                            foundSpotToSpread = true;
                        }
                    }
                }
            }
        }
        if (foundSpotToSpread)
            worldIn.setBlockAndUpdate(pos, WeatherUtilBlock.setBlockWithLayerState(state.getBlock(), heightToUse));
        return foundSpotToSpread;
    }

    public static int addHeightToLayerableBLock(World world, BlockPos pos, Block block, int sourceAmount, int amount) {
        int curAmount = sourceAmount + amount;
        int leftOver = 0;
        if (curAmount > WeatherUtilBlock.layerableHeightPropMax) {
            leftOver = curAmount - WeatherUtilBlock.layerableHeightPropMax;
            curAmount = WeatherUtilBlock.layerableHeightPropMax;
        }
        try {
            ChunkUtils.setBlockState(world, pos, WeatherUtilBlock.setBlockWithLayerState(block, curAmount));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return leftOver;
    }

    public static int takeHeightFromLayerableBlock(World world, BlockPos pos, Block block, int amount) {
        BlockState state = ChunkUtils.getBlockState(world, pos);
        int height = WeatherUtilBlock.getHeightForLayeredBlock(state);
        int newHeight, amountReceived;
        if (height <= amount) {
            newHeight = 0;
            amountReceived = height;
        } else {
            newHeight = height - amount;
            amountReceived = amount;
        }
        if (newHeight > 0) {
            try {
                ChunkUtils.setBlockState(world, pos, WeatherUtilBlock.setBlockWithLayerState(block, newHeight));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            world.removeBlock(pos, false);
        }
        return amountReceived;
    }

    public static BlockPos grabBlockPos(World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
        return world.isLoaded(pos) ? pos : null;
    }

    public static boolean canReachBlock(World world, BlockPos pos) {
        return WeatherUtilBlock.canReachBlock(world, pos, 1);
    }

    public static boolean canReachBlock(World world, BlockPos pos, int range) {
        if (!world.isLoaded(pos)) return false;
        if (WeatherUtilBlock.getHeightSafe(world, pos).getY() - 1 == pos.getY()) return true;

        for (int i = 1; i <= range; i++)
            if (WeatherUtilBlock.getHeightSafe(world, pos.north(i)).getY() <= pos.getY()
                    || WeatherUtilBlock.getHeightSafe(world, pos.east(i)).getY() <= pos.getY()
                    || WeatherUtilBlock.getHeightSafe(world, pos.south(i)).getY() <= pos.getY()
                    || WeatherUtilBlock.getHeightSafe(world, pos.west(i)).getY() <= pos.getY())
                return true;

        return false;
    }

    public static boolean canGrabBlock(StormObject storm, BlockPos pos, BlockState state) {
        if (!ConfigGrab.grab_blocks || pos == null) return false;
        World world = storm.manager.getWorld();
        if (world == null || !WeatherUtilBlock.canReachBlock(world, pos)) return false;
        return WeatherUtilBlock.checkIllegalList(state);
    }

    public static boolean checkIllegalList(BlockState state) {
        Block block = state.getBlock();
        Material material = state.getMaterial();
        return !(state.isAir() || material.isLiquid() || block instanceof BlockRepairingBlock);
    }

    public static boolean checkResistance(StormObject storm, String blockID) {
        ConfigList list = WeatherAPI.getWRList();
        float resistance = list.exists(blockID) ? (float) list.get(blockID) / 9.657718F : -1.0F;
        return resistance > -1.0F && storm.windSpeed >= resistance;
    }

    public static BlockPos getHeightSafe(World world, BlockPos pos) {
        Chunk chunk = WeatherUtilBlock.getChunk(world, pos.getX(), pos.getZ());
        if (chunk == null)
            return pos.below(pos.getY());

        int surfaceY = chunk.getHeight(Heightmap.Type.WORLD_SURFACE, pos.getX() & 15, pos.getZ() & 15);
        BlockPos new_pos = new BlockPos(pos.getX(), Math.min(pos.getY(), surfaceY), pos.getZ());

        while (new_pos.getY() < 255) {
            BlockState state = chunk.getBlockState(new_pos);
            if (WeatherUtilBlock.isReplacable(state, false))
                break;
            new_pos = new_pos.above();
        }
        return new_pos;
    }

    public static BlockPos getPrecipitationHeightSafe(World world, BlockPos pos) {
        if (world.isLoaded(pos))
            return world.getHeightmapPos(Heightmap.Type.MOTION_BLOCKING, pos);
        else
            return new BlockPos(pos.getX(), 0, pos.getZ());
    }

    public static Chunk getChunk(World world, int x, int z) {
        if (world.isClientSide)
            return world.getChunk(x >> 4, z >> 4);
        else
            return world.getChunkSource().getChunk(x >> 4, z >> 4, false);
    }
}