package net.mrbt0907.weather2.event;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.corosus.extendedrenderer.render.FoliageRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.merchant.villager.VillagerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.api.WeatherAPI;
import net.mrbt0907.weather2.api.WeatherUtilData;
import net.mrbt0907.weather2.api.event.EventRegisterGrabLists;
import net.mrbt0907.weather2.client.NewSceneEnhancer;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.client.foliage.FoliageEnhancerShader;
import net.mrbt0907.weather2.config.ConfigClient;
import net.mrbt0907.weather2.config.ConfigFoliage;
import net.mrbt0907.weather2.config.ConfigMisc;
import net.mrbt0907.weather2.config.ConfigStorm;
import net.mrbt0907.weather2.entity.AI.EntityAITakeCover;
import net.mrbt0907.weather2.util.WeatherUtilEntity;
import net.mrbt0907.weather2.weather.WindManager;

public class EventHandlerForge {

    @SubscribeEvent
    public void onGrabListRefresh(EventRegisterGrabLists event) {
        event.windResistanceList.add("minecraft:acacia_door", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:birch_door", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:dark_oak_door", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:iron_door", WeatherAPI.getEFWindSpeed(4));
        event.windResistanceList.add("minecraft:jungle_door", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:oak_door", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:spruce_door", WeatherAPI.getEFWindSpeed(1));

        event.windResistanceList.add("minecraft:acacia_fence", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:birch_fence", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:dark_oak_fence", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:jungle_fence", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:nether_brick_fence", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:oak_fence", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:spruce_fence", WeatherAPI.getEFWindSpeed(1));

        event.windResistanceList.add("minecraft:acacia_fence_gate", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:birch_fence_gate", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:dark_oak_fence_gate", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:jungle_fence_gate", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:oak_fence_gate", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:spruce_fence_gate", WeatherAPI.getEFWindSpeed(1));

        event.windResistanceList.add("minecraft:acacia_stairs", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:birch_stairs", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:brick_stairs", WeatherAPI.getEFWindSpeed(4));
        event.windResistanceList.add("minecraft:dark_oak_stairs", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:jungle_stairs", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:nether_brick_stairs", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:oak_stairs", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:purpur_stairs", WeatherAPI.getEFWindSpeed(4));
        event.windResistanceList.add("minecraft:quartz_stairs", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:red_sandstone_stairs", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:sandstone_stairs", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:spruce_stairs", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:stone_brick_stairs", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:stone_stairs", WeatherAPI.getEFWindSpeed(3));

        event.windResistanceList.add("minecraft:activator_rail", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:detector_rail", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:powered_rail", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:rail", WeatherAPI.getEFWindSpeed(3));

        event.windResistanceList.add("minecraft:anvil", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:beacon", WeatherAPI.getEFWindSpeed(4));
        event.windResistanceList.add("minecraft:bricks", WeatherAPI.getEFWindSpeed(4));
        event.windResistanceList.add("minecraft:cobblestone", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:cobblestone_wall", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:end_stone", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:end_stone_bricks", WeatherAPI.getEFWindSpeed(4));
        event.windResistanceList.add("minecraft:mossy_cobblestone", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:nether_bricks", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:prismarine", WeatherAPI.getEFWindSpeed(4));
        event.windResistanceList.add("minecraft:purpur_block", WeatherAPI.getEFWindSpeed(4));
        event.windResistanceList.add("minecraft:purpur_pillar", WeatherAPI.getEFWindSpeed(4));
        event.windResistanceList.add("minecraft:purpur_slab", WeatherAPI.getEFWindSpeed(4));
        event.windResistanceList.add("minecraft:quartz_block", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:red_nether_bricks", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:red_sandstone", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:sandstone", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:sea_lantern", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:stone", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:stone_bricks", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:stone_button", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:stone_pressure_plate", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:stone_slab", WeatherAPI.getEFWindSpeed(3));

        event.windResistanceList.add("minecraft:coal_ore", WeatherAPI.getEFWindSpeed(5));
        event.windResistanceList.add("minecraft:diamond_ore", WeatherAPI.getEFWindSpeed(5));
        event.windResistanceList.add("minecraft:emerald_ore", WeatherAPI.getEFWindSpeed(5));
        event.windResistanceList.add("minecraft:gold_ore", WeatherAPI.getEFWindSpeed(5));
        event.windResistanceList.add("minecraft:iron_ore", WeatherAPI.getEFWindSpeed(5));
        event.windResistanceList.add("minecraft:lapis_ore", WeatherAPI.getEFWindSpeed(5));
        event.windResistanceList.add("minecraft:nether_quartz_ore", WeatherAPI.getEFWindSpeed(4));
        event.windResistanceList.add("minecraft:redstone_ore", WeatherAPI.getEFWindSpeed(5));

        event.windResistanceList.add("minecraft:coal_block", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:diamond_block", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:emerald_block", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:gold_block", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:iron_block", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:iron_bars", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:iron_trapdoor", WeatherAPI.getEFWindSpeed(4));
        event.windResistanceList.add("minecraft:lapis_block", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:redstone_block", WeatherAPI.getEFWindSpeed(1));

        event.windResistanceList.add("minecraft:oak_planks", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:spruce_planks", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:birch_planks", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:jungle_planks", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:acacia_planks", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:dark_oak_planks", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:oak_slab", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:spruce_slab", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:birch_slab", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:jungle_slab", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:acacia_slab", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:dark_oak_slab", WeatherAPI.getEFWindSpeed(2));

        event.windResistanceList.add("minecraft:oak_log", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:spruce_log", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:birch_log", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:jungle_log", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:acacia_log", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:dark_oak_log", WeatherAPI.getEFWindSpeed(3));

        event.windResistanceList.add("minecraft:oak_leaves", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:spruce_leaves", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:birch_leaves", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:jungle_leaves", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:acacia_leaves", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:dark_oak_leaves", WeatherAPI.getEFWindSpeed(1));

        event.windResistanceList.add("minecraft:oak_sapling", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:spruce_sapling", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:birch_sapling", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:jungle_sapling", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:acacia_sapling", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:dark_oak_sapling", WeatherAPI.getEFWindSpeed(0));

        event.windResistanceList.add("minecraft:oak_trapdoor", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:spruce_trapdoor", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:birch_trapdoor", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:jungle_trapdoor", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:acacia_trapdoor", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:dark_oak_trapdoor", WeatherAPI.getEFWindSpeed(2));

        event.windResistanceList.add("minecraft:oak_button", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:spruce_button", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:birch_button", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:jungle_button", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:acacia_button", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:dark_oak_button", WeatherAPI.getEFWindSpeed(1));

        event.windResistanceList.add("minecraft:oak_pressure_plate", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:spruce_pressure_plate", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:birch_pressure_plate", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:jungle_pressure_plate", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:acacia_pressure_plate", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:dark_oak_pressure_plate", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:heavy_weighted_pressure_plate", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:light_weighted_pressure_plate", WeatherAPI.getEFWindSpeed(1));

        event.windResistanceList.add("minecraft:oak_sign", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:oak_wall_sign", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:spruce_sign", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:spruce_wall_sign", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:birch_sign", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:birch_wall_sign", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:jungle_sign", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:jungle_wall_sign", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:acacia_sign", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:acacia_wall_sign", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:dark_oak_sign", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:dark_oak_wall_sign", WeatherAPI.getEFWindSpeed(1));

        event.windResistanceList.add("minecraft:white_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:orange_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:magenta_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:light_blue_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:yellow_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:lime_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:pink_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:gray_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:light_gray_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:cyan_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:purple_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:blue_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:brown_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:green_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:red_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:black_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:white_wall_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:orange_wall_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:magenta_wall_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:light_blue_wall_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:yellow_wall_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:lime_wall_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:pink_wall_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:gray_wall_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:light_gray_wall_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:cyan_wall_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:purple_wall_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:blue_wall_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:brown_wall_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:green_wall_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:red_wall_banner", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:black_wall_banner", WeatherAPI.getEFWindSpeed(0));

        event.windResistanceList.add("minecraft:white_bed", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:orange_bed", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:magenta_bed", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:light_blue_bed", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:yellow_bed", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:lime_bed", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:pink_bed", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:gray_bed", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:light_gray_bed", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:cyan_bed", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:purple_bed", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:blue_bed", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:brown_bed", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:green_bed", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:red_bed", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:black_bed", WeatherAPI.getEFWindSpeed(0));

        event.windResistanceList.add("minecraft:cake", WeatherAPI.getEFWindSpeed(0));

        event.windResistanceList.add("minecraft:skeleton_skull", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:skeleton_wall_skull", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:wither_skeleton_skull", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:wither_skeleton_wall_skull", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:zombie_head", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:zombie_wall_head", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:player_head", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:player_wall_head", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:creeper_head", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:creeper_wall_head", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:dragon_head", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:dragon_wall_head", WeatherAPI.getEFWindSpeed(0));

        event.windResistanceList.add("minecraft:grass", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:fern", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:sunflower", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:lilac", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:tall_grass", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:large_fern", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:rose_bush", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:peony", WeatherAPI.getEFWindSpeed(0));

        event.windResistanceList.add("minecraft:poppy", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:blue_orchid", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:allium", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:azure_bluet", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:red_tulip", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:orange_tulip", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:white_tulip", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:pink_tulip", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:oxeye_daisy", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:cornflower", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:lily_of_the_valley", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:wither_rose", WeatherAPI.getEFWindSpeed(0));

        event.windResistanceList.add("minecraft:dirt", WeatherAPI.getEFWindSpeed(4));
        event.windResistanceList.add("minecraft:farmland", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:grass_block", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:dirt_path", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:gravel", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:mycelium", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:sand", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:soul_sand", WeatherAPI.getEFWindSpeed(2));

        event.windResistanceList.add("minecraft:clay", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:terracotta", WeatherAPI.getEFWindSpeed(3));

        event.windResistanceList.add("minecraft:black_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:blue_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:brown_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:cyan_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:gray_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:green_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:light_blue_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:light_gray_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:lime_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:magenta_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:orange_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:pink_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:purple_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:red_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:white_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:yellow_glazed_terracotta", WeatherAPI.getEFWindSpeed(3));

        event.windResistanceList.add("minecraft:white_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:orange_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:magenta_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:light_blue_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:yellow_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:lime_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:pink_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:gray_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:light_gray_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:cyan_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:purple_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:blue_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:brown_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:green_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:red_terracotta", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:black_terracotta", WeatherAPI.getEFWindSpeed(3));

        event.windResistanceList.add("minecraft:shulker_box", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:black_shulker_box", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:blue_shulker_box", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:brown_shulker_box", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:cyan_shulker_box", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:gray_shulker_box", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:green_shulker_box", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:light_blue_shulker_box", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:light_gray_shulker_box", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:lime_shulker_box", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:magenta_shulker_box", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:orange_shulker_box", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:pink_shulker_box", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:purple_shulker_box", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:red_shulker_box", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:white_shulker_box", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:yellow_shulker_box", WeatherAPI.getEFWindSpeed(1));

        event.windResistanceList.add("minecraft:glass", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:glass_pane", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:white_stained_glass", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:orange_stained_glass", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:magenta_stained_glass", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:light_blue_stained_glass", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:yellow_stained_glass", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:lime_stained_glass", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:pink_stained_glass", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:gray_stained_glass", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:light_gray_stained_glass", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:cyan_stained_glass", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:purple_stained_glass", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:blue_stained_glass", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:brown_stained_glass", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:green_stained_glass", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:red_stained_glass", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:black_stained_glass", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:white_stained_glass_pane", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:orange_stained_glass_pane", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:magenta_stained_glass_pane", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:light_blue_stained_glass_pane", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:yellow_stained_glass_pane", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:lime_stained_glass_pane", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:pink_stained_glass_pane", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:gray_stained_glass_pane", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:light_gray_stained_glass_pane", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:cyan_stained_glass_pane", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:purple_stained_glass_pane", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:blue_stained_glass_pane", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:brown_stained_glass_pane", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:green_stained_glass_pane", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:red_stained_glass_pane", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:black_stained_glass_pane", WeatherAPI.getEFWindSpeed(1));

        event.windResistanceList.add("minecraft:white_wool", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:orange_wool", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:magenta_wool", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:light_blue_wool", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:yellow_wool", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:lime_wool", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:pink_wool", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:gray_wool", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:light_gray_wool", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:cyan_wool", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:purple_wool", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:blue_wool", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:brown_wool", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:green_wool", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:red_wool", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:black_wool", WeatherAPI.getEFWindSpeed(1));

        event.windResistanceList.add("minecraft:white_carpet", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:orange_carpet", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:magenta_carpet", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:light_blue_carpet", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:yellow_carpet", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:lime_carpet", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:pink_carpet", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:gray_carpet", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:light_gray_carpet", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:cyan_carpet", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:purple_carpet", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:blue_carpet", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:brown_carpet", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:green_carpet", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:red_carpet", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:black_carpet", WeatherAPI.getEFWindSpeed(0));

        event.windResistanceList.add("minecraft:white_concrete", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:orange_concrete", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:magenta_concrete", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:light_blue_concrete", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:yellow_concrete", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:lime_concrete", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:pink_concrete", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:gray_concrete", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:light_gray_concrete", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:cyan_concrete", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:purple_concrete", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:blue_concrete", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:brown_concrete", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:green_concrete", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:red_concrete", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("minecraft:black_concrete", WeatherAPI.getEFWindSpeed(3));

        event.windResistanceList.add("minecraft:white_concrete_powder", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:orange_concrete_powder", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:magenta_concrete_powder", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:light_blue_concrete_powder", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:yellow_concrete_powder", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:lime_concrete_powder", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:pink_concrete_powder", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:gray_concrete_powder", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:light_gray_concrete_powder", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:cyan_concrete_powder", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:purple_concrete_powder", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:blue_concrete_powder", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:brown_concrete_powder", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:green_concrete_powder", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:red_concrete_powder", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:black_concrete_powder", WeatherAPI.getEFWindSpeed(0));

        event.windResistanceList.add("minecraft:bone_block", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:bookshelf", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:brewing_stand", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:cactus", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:cauldron", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:chest", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:chorus_flower", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:chorus_plant", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:comparator", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:crafting_table", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:dandelion", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:daylight_detector", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:dead_bush", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:dispenser", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:dragon_egg", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:dropper", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:enchanting_table", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:ender_chest", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:flower_pot", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:furnace", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:glowstone", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:hay_block", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:hopper", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:ice", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:jack_o_lantern", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:jukebox", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:ladder", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:lever", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:lily_pad", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:magma_block", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:melon", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:brown_mushroom_block", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:red_mushroom_block", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:netherrack", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:nether_wart_block", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:note_block", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:observer", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:obsidian", WeatherAPI.getEFWindSpeed(6));
        event.windResistanceList.add("minecraft:packed_ice", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:piston", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:poppy", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:pumpkin", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:redstone_lamp", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:redstone_torch", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:repeater", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:slime_block", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:snow", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:snow_block", WeatherAPI.getEFWindSpeed(1));
        event.windResistanceList.add("minecraft:sponge", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:sticky_piston", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("minecraft:string", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:sugar_cane", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:tnt", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:torch", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:trapped_chest", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:tripwire_hook", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:vine", WeatherAPI.getEFWindSpeed(0));
        event.windResistanceList.add("minecraft:cobweb", WeatherAPI.getEFWindSpeed(0));

        event.windResistanceList.add("weather2:anemometer", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("weather2:barometer_sensor", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("weather2:humidity_sensor", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("weather2:machine_case", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("weather2:radio_transmitter", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("weather2:rain_sensor", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("weather2:storm_sensor", WeatherAPI.getEFWindSpeed(4));
        event.windResistanceList.add("weather2:temperature_sensor", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("weather2:tornado_sensor", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("weather2:tornado_siren", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("weather2:tornado_siren_manual", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("weather2:weather_deflector", WeatherAPI.getEFWindSpeed(5));
        event.windResistanceList.add("weather2:weather_forecast", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("weather2:weather_forecast_2", WeatherAPI.getEFWindSpeed(2));
        event.windResistanceList.add("weather2:weather_forecast_3", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("weather2:weather_machine", WeatherAPI.getEFWindSpeed(6));
        event.windResistanceList.add("weather2:wind_sensor", WeatherAPI.getEFWindSpeed(3));
        event.windResistanceList.add("weather2:wind_vane", WeatherAPI.getEFWindSpeed(3));

        event.grabList.add("minecraft:acacia_door");
        event.grabList.add("minecraft:acacia_fence");
        event.grabList.add("minecraft:acacia_fence_gate");
        event.grabList.add("minecraft:acacia_stairs");
        event.grabList.add("minecraft:activator_rail");
        event.grabList.add("minecraft:anvil");
        event.grabList.add("minecraft:beacon");
        event.grabList.add("minecraft:birch_door");
        event.grabList.add("minecraft:birch_fence");
        event.grabList.add("minecraft:birch_fence_gate");
        event.grabList.add("minecraft:birch_stairs");
        event.grabList.add("minecraft:bone_block");
        event.grabList.add("minecraft:bookshelf");
        event.grabList.add("minecraft:brewing_stand");
        event.grabList.add("minecraft:bricks");
        event.grabList.add("minecraft:brick_stairs");
        event.grabList.add("minecraft:brown_mushroom_block");
        event.grabList.add("minecraft:cactus");
        event.grabList.add("minecraft:cake");
        event.grabList.add("minecraft:cauldron");
        event.grabList.add("minecraft:chest");
        event.grabList.add("minecraft:chorus_flower");
        event.grabList.add("minecraft:chorus_plant");
        event.grabList.add("minecraft:clay");
        event.grabList.add("minecraft:coal_block");
        event.grabList.add("minecraft:coal_ore");
        event.grabList.add("minecraft:cobblestone_wall");
        event.grabList.add("minecraft:comparator");
        event.grabList.add("minecraft:crafting_table");
        event.grabList.add("minecraft:dark_oak_door");
        event.grabList.add("minecraft:dark_oak_fence");
        event.grabList.add("minecraft:dark_oak_fence_gate");
        event.grabList.add("minecraft:dark_oak_stairs");
        event.grabList.add("minecraft:daylight_detector");
        event.grabList.add("minecraft:detector_rail");
        event.grabList.add("minecraft:diamond_block");
        event.grabList.add("minecraft:diamond_ore");
        event.grabList.add("minecraft:dispenser");
        event.grabList.add("minecraft:dragon_egg");
        event.grabList.add("minecraft:dropper");
        event.grabList.add("minecraft:emerald_block");
        event.grabList.add("minecraft:emerald_ore");
        event.grabList.add("minecraft:enchanting_table");
        event.grabList.add("minecraft:end_stone");
        event.grabList.add("minecraft:end_stone_bricks");
        event.grabList.add("minecraft:ender_chest");
        event.grabList.add("minecraft:flower_pot");
        event.grabList.add("minecraft:furnace");
        event.grabList.add("minecraft:gold_block");
        event.grabList.add("minecraft:gold_ore");
        event.grabList.add("minecraft:powered_rail");
        event.grabList.add("minecraft:gravel");
        event.grabList.add("minecraft:hay_block");
        event.grabList.add("minecraft:heavy_weighted_pressure_plate");
        event.grabList.add("minecraft:hopper");
        event.grabList.add("minecraft:iron_bars");
        event.grabList.add("minecraft:iron_block");
        event.grabList.add("minecraft:iron_door");
        event.grabList.add("minecraft:iron_ore");
        event.grabList.add("minecraft:iron_trapdoor");
        event.grabList.add("minecraft:jack_o_lantern");
        event.grabList.add("minecraft:jukebox");
        event.grabList.add("minecraft:jungle_door");
        event.grabList.add("minecraft:jungle_fence");
        event.grabList.add("minecraft:jungle_fence_gate");
        event.grabList.add("minecraft:jungle_stairs");
        event.grabList.add("minecraft:ladder");
        event.grabList.add("minecraft:lapis_block");
        event.grabList.add("minecraft:lapis_ore");
        event.grabList.add("minecraft:lever");
        event.grabList.add("minecraft:light_weighted_pressure_plate");
        event.grabList.add("minecraft:magma_block");
        event.grabList.add("minecraft:melon");
        event.grabList.add("minecraft:mossy_cobblestone");
        event.grabList.add("minecraft:mycelium");
        event.grabList.add("minecraft:nether_bricks");
        event.grabList.add("minecraft:nether_brick_fence");
        event.grabList.add("minecraft:nether_brick_stairs");
        event.grabList.add("minecraft:nether_wart_block");
        event.grabList.add("minecraft:nether_quartz_ore");
        event.grabList.add("minecraft:netherrack");
        event.grabList.add("minecraft:note_block");
        event.grabList.add("minecraft:oak_stairs");
        event.grabList.add("minecraft:observer");
        event.grabList.add("minecraft:obsidian");
        event.grabList.add("minecraft:packed_ice");
        event.grabList.add("minecraft:piston");
        event.grabList.add("minecraft:prismarine");
        event.grabList.add("minecraft:pumpkin");
        event.grabList.add("minecraft:purpur_block");
        event.grabList.add("minecraft:purpur_pillar");
        event.grabList.add("minecraft:purpur_slab");
        event.grabList.add("minecraft:purpur_stairs");
        event.grabList.add("minecraft:quartz_block");
        event.grabList.add("minecraft:quartz_stairs");
        event.grabList.add("minecraft:rail");
        event.grabList.add("minecraft:red_mushroom_block");
        event.grabList.add("minecraft:red_nether_bricks");
        event.grabList.add("minecraft:red_sandstone");
        event.grabList.add("minecraft:red_sandstone_stairs");
        event.grabList.add("minecraft:redstone_block");
        event.grabList.add("minecraft:redstone_ore");
        event.grabList.add("minecraft:redstone_torch");
        event.grabList.add("minecraft:repeater");
        event.grabList.add("minecraft:sand");
        event.grabList.add("minecraft:sandstone");
        event.grabList.add("minecraft:sandstone_stairs");
        event.grabList.add("minecraft:sea_lantern");
        event.grabList.add("minecraft:slime_block");
        event.grabList.add("minecraft:soul_sand");
        event.grabList.add("minecraft:sponge");
        event.grabList.add("minecraft:spruce_door");
        event.grabList.add("minecraft:spruce_fence");
        event.grabList.add("minecraft:spruce_fence_gate");
        event.grabList.add("minecraft:spruce_stairs");
        event.grabList.add("minecraft:sticky_piston");
        event.grabList.add("minecraft:stone_brick_stairs");
        event.grabList.add("minecraft:stone_button");
        event.grabList.add("minecraft:stone_bricks");
        event.grabList.add("minecraft:stone_pressure_plate");
        event.grabList.add("minecraft:stone_slab");
        event.grabList.add("minecraft:stone_stairs");
        event.grabList.add("minecraft:terracotta");
        event.grabList.add("minecraft:tnt");
        event.grabList.add("minecraft:trapped_chest");
        event.grabList.add("minecraft:tripwire_hook");

        event.grabList.add("minecraft:oak_sapling");
        event.grabList.add("minecraft:spruce_sapling");
        event.grabList.add("minecraft:birch_sapling");
        event.grabList.add("minecraft:jungle_sapling");
        event.grabList.add("minecraft:acacia_sapling");
        event.grabList.add("minecraft:dark_oak_sapling");

        event.grabList.add("minecraft:oak_log");
        event.grabList.add("minecraft:spruce_log");
        event.grabList.add("minecraft:birch_log");
        event.grabList.add("minecraft:jungle_log");
        event.grabList.add("minecraft:acacia_log");
        event.grabList.add("minecraft:dark_oak_log");

        event.grabList.add("minecraft:oak_leaves");
        event.grabList.add("minecraft:spruce_leaves");
        event.grabList.add("minecraft:birch_leaves");
        event.grabList.add("minecraft:jungle_leaves");
        event.grabList.add("minecraft:acacia_leaves");
        event.grabList.add("minecraft:dark_oak_leaves");

        event.grabList.add("minecraft:oak_trapdoor");
        event.grabList.add("minecraft:spruce_trapdoor");
        event.grabList.add("minecraft:birch_trapdoor");
        event.grabList.add("minecraft:jungle_trapdoor");
        event.grabList.add("minecraft:acacia_trapdoor");
        event.grabList.add("minecraft:dark_oak_trapdoor");

        event.grabList.add("minecraft:oak_button");
        event.grabList.add("minecraft:spruce_button");
        event.grabList.add("minecraft:birch_button");
        event.grabList.add("minecraft:jungle_button");
        event.grabList.add("minecraft:acacia_button");
        event.grabList.add("minecraft:dark_oak_button");

        event.grabList.add("minecraft:oak_pressure_plate");
        event.grabList.add("minecraft:spruce_pressure_plate");
        event.grabList.add("minecraft:birch_pressure_plate");
        event.grabList.add("minecraft:jungle_pressure_plate");
        event.grabList.add("minecraft:acacia_pressure_plate");
        event.grabList.add("minecraft:dark_oak_pressure_plate");

        event.grabList.add("minecraft:oak_sign");
        event.grabList.add("minecraft:oak_wall_sign");
        event.grabList.add("minecraft:spruce_sign");
        event.grabList.add("minecraft:spruce_wall_sign");
        event.grabList.add("minecraft:birch_sign");
        event.grabList.add("minecraft:birch_wall_sign");
        event.grabList.add("minecraft:jungle_sign");
        event.grabList.add("minecraft:jungle_wall_sign");
        event.grabList.add("minecraft:acacia_sign");
        event.grabList.add("minecraft:acacia_wall_sign");
        event.grabList.add("minecraft:dark_oak_sign");
        event.grabList.add("minecraft:dark_oak_wall_sign");

        event.grabList.add("minecraft:white_banner");
        event.grabList.add("minecraft:orange_banner");
        event.grabList.add("minecraft:magenta_banner");
        event.grabList.add("minecraft:light_blue_banner");
        event.grabList.add("minecraft:yellow_banner");
        event.grabList.add("minecraft:lime_banner");
        event.grabList.add("minecraft:pink_banner");
        event.grabList.add("minecraft:gray_banner");
        event.grabList.add("minecraft:light_gray_banner");
        event.grabList.add("minecraft:cyan_banner");
        event.grabList.add("minecraft:purple_banner");
        event.grabList.add("minecraft:blue_banner");
        event.grabList.add("minecraft:brown_banner");
        event.grabList.add("minecraft:green_banner");
        event.grabList.add("minecraft:red_banner");
        event.grabList.add("minecraft:black_banner");
        event.grabList.add("minecraft:white_wall_banner");
        event.grabList.add("minecraft:orange_wall_banner");
        event.grabList.add("minecraft:magenta_wall_banner");
        event.grabList.add("minecraft:light_blue_wall_banner");
        event.grabList.add("minecraft:yellow_wall_banner");
        event.grabList.add("minecraft:lime_wall_banner");
        event.grabList.add("minecraft:pink_wall_banner");
        event.grabList.add("minecraft:gray_wall_banner");
        event.grabList.add("minecraft:light_gray_wall_banner");
        event.grabList.add("minecraft:cyan_wall_banner");
        event.grabList.add("minecraft:purple_wall_banner");
        event.grabList.add("minecraft:blue_wall_banner");
        event.grabList.add("minecraft:brown_wall_banner");
        event.grabList.add("minecraft:green_wall_banner");
        event.grabList.add("minecraft:red_wall_banner");
        event.grabList.add("minecraft:black_wall_banner");

        event.grabList.add("minecraft:white_bed");
        event.grabList.add("minecraft:orange_bed");
        event.grabList.add("minecraft:magenta_bed");
        event.grabList.add("minecraft:light_blue_bed");
        event.grabList.add("minecraft:yellow_bed");
        event.grabList.add("minecraft:lime_bed");
        event.grabList.add("minecraft:pink_bed");
        event.grabList.add("minecraft:gray_bed");
        event.grabList.add("minecraft:light_gray_bed");
        event.grabList.add("minecraft:cyan_bed");
        event.grabList.add("minecraft:purple_bed");
        event.grabList.add("minecraft:blue_bed");
        event.grabList.add("minecraft:brown_bed");
        event.grabList.add("minecraft:green_bed");
        event.grabList.add("minecraft:red_bed");
        event.grabList.add("minecraft:black_bed");

        event.grabList.add("minecraft:skeleton_skull");
        event.grabList.add("minecraft:skeleton_wall_skull");
        event.grabList.add("minecraft:wither_skeleton_skull");
        event.grabList.add("minecraft:wither_skeleton_wall_skull");
        event.grabList.add("minecraft:zombie_head");
        event.grabList.add("minecraft:zombie_wall_head");
        event.grabList.add("minecraft:player_head");
        event.grabList.add("minecraft:player_wall_head");
        event.grabList.add("minecraft:creeper_head");
        event.grabList.add("minecraft:creeper_wall_head");
        event.grabList.add("minecraft:dragon_head");
        event.grabList.add("minecraft:dragon_wall_head");

        event.grabList.add("minecraft:black_glazed_terracotta");
        event.grabList.add("minecraft:blue_glazed_terracotta");
        event.grabList.add("minecraft:brown_glazed_terracotta");
        event.grabList.add("minecraft:cyan_glazed_terracotta");
        event.grabList.add("minecraft:gray_glazed_terracotta");
        event.grabList.add("minecraft:green_glazed_terracotta");
        event.grabList.add("minecraft:light_blue_glazed_terracotta");
        event.grabList.add("minecraft:light_gray_glazed_terracotta");
        event.grabList.add("minecraft:lime_glazed_terracotta");
        event.grabList.add("minecraft:magenta_glazed_terracotta");
        event.grabList.add("minecraft:orange_glazed_terracotta");
        event.grabList.add("minecraft:pink_glazed_terracotta");
        event.grabList.add("minecraft:purple_glazed_terracotta");
        event.grabList.add("minecraft:red_glazed_terracotta");
        event.grabList.add("minecraft:white_glazed_terracotta");
        event.grabList.add("minecraft:yellow_glazed_terracotta");

        event.grabList.add("minecraft:white_terracotta");
        event.grabList.add("minecraft:orange_terracotta");
        event.grabList.add("minecraft:magenta_terracotta");
        event.grabList.add("minecraft:light_blue_terracotta");
        event.grabList.add("minecraft:yellow_terracotta");
        event.grabList.add("minecraft:lime_terracotta");
        event.grabList.add("minecraft:pink_terracotta");
        event.grabList.add("minecraft:gray_terracotta");
        event.grabList.add("minecraft:light_gray_terracotta");
        event.grabList.add("minecraft:cyan_terracotta");
        event.grabList.add("minecraft:purple_terracotta");
        event.grabList.add("minecraft:blue_terracotta");
        event.grabList.add("minecraft:brown_terracotta");
        event.grabList.add("minecraft:green_terracotta");
        event.grabList.add("minecraft:red_terracotta");
        event.grabList.add("minecraft:black_terracotta");

        event.grabList.add("minecraft:shulker_box");
        event.grabList.add("minecraft:black_shulker_box");
        event.grabList.add("minecraft:blue_shulker_box");
        event.grabList.add("minecraft:brown_shulker_box");
        event.grabList.add("minecraft:cyan_shulker_box");
        event.grabList.add("minecraft:gray_shulker_box");
        event.grabList.add("minecraft:green_shulker_box");
        event.grabList.add("minecraft:light_blue_shulker_box");
        event.grabList.add("minecraft:light_gray_shulker_box");
        event.grabList.add("minecraft:lime_shulker_box");
        event.grabList.add("minecraft:magenta_shulker_box");
        event.grabList.add("minecraft:orange_shulker_box");
        event.grabList.add("minecraft:pink_shulker_box");
        event.grabList.add("minecraft:purple_shulker_box");
        event.grabList.add("minecraft:red_shulker_box");
        event.grabList.add("minecraft:white_shulker_box");
        event.grabList.add("minecraft:yellow_shulker_box");

        event.grabList.add("minecraft:white_wool");
        event.grabList.add("minecraft:orange_wool");
        event.grabList.add("minecraft:magenta_wool");
        event.grabList.add("minecraft:light_blue_wool");
        event.grabList.add("minecraft:yellow_wool");
        event.grabList.add("minecraft:lime_wool");
        event.grabList.add("minecraft:pink_wool");
        event.grabList.add("minecraft:gray_wool");
        event.grabList.add("minecraft:light_gray_wool");
        event.grabList.add("minecraft:cyan_wool");
        event.grabList.add("minecraft:purple_wool");
        event.grabList.add("minecraft:blue_wool");
        event.grabList.add("minecraft:brown_wool");
        event.grabList.add("minecraft:green_wool");
        event.grabList.add("minecraft:red_wool");
        event.grabList.add("minecraft:black_wool");

        event.grabList.add("minecraft:white_concrete");
        event.grabList.add("minecraft:orange_concrete");
        event.grabList.add("minecraft:magenta_concrete");
        event.grabList.add("minecraft:light_blue_concrete");
        event.grabList.add("minecraft:yellow_concrete");
        event.grabList.add("minecraft:lime_concrete");
        event.grabList.add("minecraft:pink_concrete");
        event.grabList.add("minecraft:gray_concrete");
        event.grabList.add("minecraft:light_gray_concrete");
        event.grabList.add("minecraft:cyan_concrete");
        event.grabList.add("minecraft:purple_concrete");
        event.grabList.add("minecraft:blue_concrete");
        event.grabList.add("minecraft:brown_concrete");
        event.grabList.add("minecraft:green_concrete");
        event.grabList.add("minecraft:red_concrete");
        event.grabList.add("minecraft:black_concrete");

        event.grabList.add("minecraft:oak_slab");
        event.grabList.add("minecraft:spruce_slab");
        event.grabList.add("minecraft:birch_slab");
        event.grabList.add("minecraft:jungle_slab");
        event.grabList.add("minecraft:acacia_slab");
        event.grabList.add("minecraft:dark_oak_slab");

        event.grabList.add("weather2:anemometer");
        event.grabList.add("weather2:barometer_sensor");
        event.grabList.add("weather2:humidity_sensor");
        event.grabList.add("weather2:machine_case");
        event.grabList.add("weather2:radio_transmitter");
        event.grabList.add("weather2:rain_sensor");
        event.grabList.add("weather2:storm_sensor");
        event.grabList.add("weather2:temperature_sensor");
        event.grabList.add("weather2:tornado_sensor");
        event.grabList.add("weather2:tornado_siren");
        event.grabList.add("weather2:tornado_siren_manual");
        event.grabList.add("weather2:weather_deflector");
        event.grabList.add("weather2:weather_forecast");
        event.grabList.add("weather2:weather_forecast_2");
        event.grabList.add("weather2:weather_forecast_3");
        event.grabList.add("weather2:weather_machine");
        event.grabList.add("weather2:wind_sensor");
        event.grabList.add("weather2:wind_vane");

        event.replaceList.add("minecraft:glass", "minecraft:air");
        event.replaceList.add("minecraft:glass_pane", "minecraft:air");
        event.replaceList.add("minecraft:glowstone", "minecraft:air");
        event.replaceList.add("minecraft:redstone_lamp", "minecraft:air");
        event.replaceList.add("minecraft:white_stained_glass", "minecraft:air");
        event.replaceList.add("minecraft:orange_stained_glass", "minecraft:air");
        event.replaceList.add("minecraft:magenta_stained_glass", "minecraft:air");
        event.replaceList.add("minecraft:light_blue_stained_glass", "minecraft:air");
        event.replaceList.add("minecraft:yellow_stained_glass", "minecraft:air");
        event.replaceList.add("minecraft:lime_stained_glass", "minecraft:air");
        event.replaceList.add("minecraft:pink_stained_glass", "minecraft:air");
        event.replaceList.add("minecraft:gray_stained_glass", "minecraft:air");
        event.replaceList.add("minecraft:light_gray_stained_glass", "minecraft:air");
        event.replaceList.add("minecraft:cyan_stained_glass", "minecraft:air");
        event.replaceList.add("minecraft:purple_stained_glass", "minecraft:air");
        event.replaceList.add("minecraft:blue_stained_glass", "minecraft:air");
        event.replaceList.add("minecraft:brown_stained_glass", "minecraft:air");
        event.replaceList.add("minecraft:green_stained_glass", "minecraft:air");
        event.replaceList.add("minecraft:red_stained_glass", "minecraft:air");
        event.replaceList.add("minecraft:black_stained_glass", "minecraft:air");
        event.replaceList.add("minecraft:white_stained_glass_pane", "minecraft:air");
        event.replaceList.add("minecraft:orange_stained_glass_pane", "minecraft:air");
        event.replaceList.add("minecraft:magenta_stained_glass_pane", "minecraft:air");
        event.replaceList.add("minecraft:light_blue_stained_glass_pane", "minecraft:air");
        event.replaceList.add("minecraft:yellow_stained_glass_pane", "minecraft:air");
        event.replaceList.add("minecraft:lime_stained_glass_pane", "minecraft:air");
        event.replaceList.add("minecraft:pink_stained_glass_pane", "minecraft:air");
        event.replaceList.add("minecraft:gray_stained_glass_pane", "minecraft:air");
        event.replaceList.add("minecraft:light_gray_stained_glass_pane", "minecraft:air");
        event.replaceList.add("minecraft:cyan_stained_glass_pane", "minecraft:air");
        event.replaceList.add("minecraft:purple_stained_glass_pane", "minecraft:air");
        event.replaceList.add("minecraft:blue_stained_glass_pane", "minecraft:air");
        event.replaceList.add("minecraft:brown_stained_glass_pane", "minecraft:air");
        event.replaceList.add("minecraft:green_stained_glass_pane", "minecraft:air");
        event.replaceList.add("minecraft:red_stained_glass_pane", "minecraft:air");
        event.replaceList.add("minecraft:black_stained_glass_pane", "minecraft:air");

        event.replaceList.add("minecraft:brown_mushroom", "minecraft:air");
        event.replaceList.add("minecraft:dead_bush", "minecraft:air");
        event.replaceList.add("minecraft:dirt", "minecraft:air");
        event.replaceList.add("minecraft:farmland", "minecraft:dirt");
        event.replaceList.add("minecraft:grass_block", "minecraft:dirt");
        event.replaceList.add("minecraft:dirt_path", "minecraft:dirt");
        event.replaceList.add("minecraft:ice", "minecraft:water");
        event.replaceList.add("minecraft:packed_ice", "minecraft:water");
        event.replaceList.add("minecraft:red_mushroom", "minecraft:air");
        event.replaceList.add("minecraft:redstone", "minecraft:air");
        event.replaceList.add("minecraft:sugar_cane", "minecraft:air");
        event.replaceList.add("minecraft:snow", "minecraft:air");
        event.replaceList.add("minecraft:snow_block", "minecraft:air");
        event.replaceList.add("minecraft:stone", "minecraft:cobblestone");
        event.replaceList.add("minecraft:string", "minecraft:air");
        event.replaceList.add("minecraft:torch", "minecraft:air");
        event.replaceList.add("minecraft:vine", "minecraft:air");
        event.replaceList.add("minecraft:lily_pad", "minecraft:air");
        event.replaceList.add("minecraft:cobweb", "minecraft:air");
        event.replaceList.add("minecraft:oak_button", "minecraft:air");
        event.replaceList.add("minecraft:spruce_button", "minecraft:air");
        event.replaceList.add("minecraft:birch_button", "minecraft:air");
        event.replaceList.add("minecraft:jungle_button", "minecraft:air");
        event.replaceList.add("minecraft:acacia_button", "minecraft:air");
        event.replaceList.add("minecraft:dark_oak_button", "minecraft:air");
        event.replaceList.add("minecraft:oak_pressure_plate", "minecraft:air");
        event.replaceList.add("minecraft:spruce_pressure_plate", "minecraft:air");
        event.replaceList.add("minecraft:birch_pressure_plate", "minecraft:air");
        event.replaceList.add("minecraft:jungle_pressure_plate", "minecraft:air");
        event.replaceList.add("minecraft:acacia_pressure_plate", "minecraft:air");
        event.replaceList.add("minecraft:dark_oak_pressure_plate", "minecraft:air");
    }

    @SubscribeEvent
    public void worldSave(Save event) {
        Weather2.writeOutData(false);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void worldRender(RenderWorldLastEvent event) {
        if (ConfigMisc.toaster_pc_mode) return;

        ClientTickHandler.checkClientWeather();
        ClientTickHandler.weatherManager.tickRender(event.getPartialTicks());

        FoliageRenderer.radialRange = ConfigFoliage.shader_range;
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onFogColors(EntityViewRenderEvent.FogColors event) {
        if (ConfigMisc.toaster_pc_mode) return;
        NewSceneEnhancer scene = NewSceneEnhancer.instance();

        if (scene.shouldChangeFogColor()) {
            float red = event.getRed(), green = event.getGreen(), blue = event.getBlue();

            if (scene.seesWeatherObject() && scene.fogMult > 0.0025F) {
                if (scene.fogRed < 0.0F) scene.fogRed = red;
                if (scene.fogGreen < 0.0F) scene.fogGreen = green;
                if (scene.fogBlue < 0.0F) scene.fogBlue = blue;
            } else {
                if (scene.fogRedTarget >= 0.0F && scene.fogRedTarget != red) scene.fogRedTarget = red;
                if (scene.fogGreenTarget >= 0.0F && scene.fogGreenTarget != green) scene.fogGreenTarget = green;
                if (scene.fogBlueTarget >= 0.0F && scene.fogBlueTarget != blue) scene.fogBlueTarget = blue;

                if (scene.fogRed != -1.0F && scene.fogRed == red) scene.fogRed = -1.0F;
                if (scene.fogGreen != -1.0F && scene.fogGreen == green) scene.fogGreen = -1.0F;
                if (scene.fogBlue != -1.0F && scene.fogBlue == blue) scene.fogBlue = -1.0F;
            }

            if (scene.fogRed >= 0.0F && scene.fogGreen >= 0.0F && scene.fogBlue >= 0.0F) {
                event.setRed(scene.fogRed);
                event.setGreen(scene.fogGreen);
                event.setBlue(scene.fogBlue);
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onFogRender(EntityViewRenderEvent.RenderFogEvent event) {
        if (ConfigMisc.toaster_pc_mode || !ConfigClient.enable_custom_fog) return;
        NewSceneEnhancer scene = NewSceneEnhancer.instance();
        RenderSystem.fogMode(GlStateManager.FogMode.EXP);
        RenderSystem.fogStart(0.0F);
        RenderSystem.fogEnd(scene.renderDistance);
        RenderSystem.fogDensity(scene.fogMult);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onRenderTick(TickEvent.RenderTickEvent event) {
        NewSceneEnhancer.instance().tickRender(event);
    }

    @SubscribeEvent
    public void onEntityJoined(EntityJoinWorldEvent event) {
        Entity entity = event.getEntity();
        if (entity.level.isClientSide) return;

        if (ConfigStorm.enable_villagers_take_cover && entity instanceof VillagerEntity) {
            VillagerEntity villager = (VillagerEntity) entity;
            if (!WeatherUtilEntity.hasAITask(villager, EntityAITakeCover.class)) {
                villager.goalSelector.addGoal(2, new EntityAITakeCover(villager));
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void registerIconsForFoliage(TextureStitchEvent.Post event) {
        FoliageEnhancerShader.setupReplacers();
    }


    @SubscribeEvent
    public void onEntityLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        Entity ent = event.getEntity();
        if (!ent.level.isClientSide) {
            if (WeatherUtilData.isWindAffected(ent)) {
                WindManager windMan = ServerTickHandler.getWeatherSystemForDim(
                        ent.level.dimension()).windManager;
                windMan.getEntityWindVectors(ent, 1F / 20F, 0.5F);
            }
        }
    }
}