package net.mrbt0907.weather2.item;

import net.corosus.extendedrenderer.particle.ParticleRegistry;
import net.corosus.extendedrenderer.particle.behavior.ParticleBehaviorSandstorm;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.PacketDistributor;
import net.mrbt0907.weather2.Weather2;
import net.mrbt0907.weather2.client.entity.particle.ParticleSandstorm;
import net.mrbt0907.weather2.client.event.ClientTickHandler;
import net.mrbt0907.weather2.network.packets.PacketPocketSand;
import net.mrbt0907.weather2.registry.BlockRegistry;
import net.mrbt0907.weather2.util.Maths;
import net.mrbt0907.weather2.util.Maths.Vec3;
import net.mrbt0907.weather2.util.WeatherUtilBlock;

import java.util.Random;

public class ItemPocketSand extends Item {
    @OnlyIn(Dist.CLIENT)
    public static ParticleBehaviorSandstorm particleBehavior;

    public ItemPocketSand(Item.Properties properties) {
        super(properties);
    }

    @OnlyIn(Dist.CLIENT)
    public static void particulate(World world, LivingEntity player) {

        if (particleBehavior == null) {
            particleBehavior = new ParticleBehaviorSandstorm(new Vec3(player.blockPosition()).toVec3Coro());
        }

        Random rand = world.random;

        TextureAtlasSprite sprite = ParticleRegistry.cloud256;

        double xzAdj = Maths.fastCos(Math.toRadians(player.xRot));
        for (int i = 0; i < 15; i++) {
            ParticleSandstorm part = new ParticleSandstorm((ClientWorld) world, player.getX(), player.getY() + 1.5D, player.getZ()
                    , 0, 0, 0, sprite);
            particleBehavior.initParticle(part);

            double speed = 0.6F;
            double randSize = 20;
            double randAngle = player.level.random.nextDouble() * randSize - player.level.random.nextDouble() * randSize;
            double vecX = (-Maths.fastSin(Math.toRadians(player.yHeadRot + randAngle)) * (speed));
            randAngle = player.level.random.nextDouble() * randSize - player.level.random.nextDouble() * randSize;
            double vecZ = (Maths.fastCos(Math.toRadians(player.yHeadRot + randAngle)) * (speed));
            randAngle = player.level.random.nextDouble() * randSize - player.level.random.nextDouble() * randSize;


            double vecY = (-Maths.fastSin(Math.toRadians(player.xRot + randAngle)) * (speed));


            part.setMotionX(vecX * xzAdj);
            part.setMotionZ(vecZ * xzAdj);
            part.setMotionY(vecY);

            part.setFacePlayer(false);
            part.isTransparent = true;
            part.rotationYaw = (float) rand.nextInt(360);
            part.rotationPitch = (float) rand.nextInt(360);
            part.setMaxAge(80);
            part.setGravity(0.09F);
            part.setAlphaF(1F);
            float brightnessMulti = 1F - (rand.nextFloat() * 0.5F);
            part.setColor(0.65F * brightnessMulti, 0.6F * brightnessMulti, 0.3F * brightnessMulti);
            part.setScale(20);

            part.aboveGroundHeight = 0.5D;
            part.collisionSpeedDampen = false;
            part.bounceSpeed = 0.03D;
            part.bounceSpeedAhead = 0.0D;

            part.setKillOnCollide(false);

            part.windWeight = 1F;

            particleBehavior.particles.add(part);
            ClientTickHandler.weatherManager.addEffectedParticle(part);
            part.spawnAsWeatherEffect();
        }


    }

    public static void particulateToClients(World world, LivingEntity player) {
        Weather2.PACKET_HANDLER.send(
                PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        50,
                        world.dimension()
                )),
                new PacketPocketSand(player.getName().getString())
        );
    }

    @OnlyIn(Dist.CLIENT)
    public static void particulateFromServer(String username) {
        World world = Minecraft.getInstance().level;


        PlayerEntity player = world.players().stream()
                .filter(p -> p.getName().getString().equals(username))
                .findFirst()
                .orElse(null);

        if (player != null) {
            particulate(world, player);
        }
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity player, Hand hand) {

        ItemStack itemStackIn = player.getItemInHand(hand);

        if (!player.level.isClientSide) {

            if (!player.abilities.instabuild) {
                if (itemStackIn.getCount() > 0) {
                    itemStackIn.shrink(1);
                }
            }
            int y = (int) player.getBoundingBox().minY;
            double randSize = 20;
            double randAngle = player.level.random.nextDouble() * randSize - player.level.random.nextDouble() * randSize;
            WeatherUtilBlock.fillAgainstWallSmoothly(player.level, new Vec3(player.getX(), y + 0.5D, player.getZ()), player.yHeadRot + (float) randAngle, 15, 2, BlockRegistry.sand_layer.get(), 2);
            particulateToClients(worldIn, player);
        } else {
            particulate(player.level, player);
        }

        return super.use(worldIn, player, hand);
    }

    @Override
    public void inventoryTick(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {

        if (worldIn.isClientSide) {
            tickClient(stack, worldIn, entityIn, itemSlot, isSelected);
        }

        super.inventoryTick(stack, worldIn, entityIn, itemSlot, isSelected);
    }

    @OnlyIn(Dist.CLIENT)
    public void tickClient(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (particleBehavior == null) {
            particleBehavior = new ParticleBehaviorSandstorm(new Vec3(entityIn.blockPosition()).toVec3Coro());
        }
        particleBehavior.tickUpdateList();
    }
}