package net.mrbt0907.weather2.block.tile;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.mrbt0907.weather2.api.interfaces.IRadioTransmitter;
import net.mrbt0907.weather2.api.interfaces.ITileInteractable;
import net.mrbt0907.weather2.registry.TileEntityRegistry;

public class TileRadioTransmitter extends TileMachine implements IRadioTransmitter<TileEntity>, ITileInteractable {
    protected String frequency;
    protected String message;
    protected ResourceLocation sound;

    public TileRadioTransmitter() {
        this(TileEntityRegistry.RADIO_TRANSMITTER_TILE.get());
    }

    public TileRadioTransmitter(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
        frequency = "";
        message = "";
        sound = null;
    }

    @Override
    public CompoundNBT save(CompoundNBT nbt) {
        nbt.putString("frequency", frequency);
        nbt.putString("message", message);
        if (sound != null)
            nbt.putString("sound", sound.toString());
        return super.save(nbt);
    }

    @Override
    public void load(BlockState state, CompoundNBT nbt) {
        super.load(state, nbt);
        frequency = nbt.getString("frequency");
        message = nbt.getString("message");
        if (nbt.contains("sound"))
            sound = new ResourceLocation(nbt.getString("sound"));
    }

    @Override
    public void setRadioFrequency(TileEntity obj, String frequency) {
        this.frequency = frequency;
    }

    @Override
    public String getRadioFrequency(TileEntity obj) {
        return frequency;
    }

    @Override
    public void setRadioMessage(TileEntity obj, String message) {
        this.message = message;
    }

    @Override
    public String getRadioMessage(TileEntity obj) {
        return message;
    }

    @Override
    public void setRadioSound(TileEntity obj, ResourceLocation sound) {
        this.sound = sound;
    }

    @Override
    public ResourceLocation getRadioSound(TileEntity obj) {
        return sound;
    }

    @Override
    public void onTileActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction facing, float hitX, float hitY, float hitZ) {
        if (world.isClientSide)
            openScreen();
    }

    @OnlyIn(Dist.CLIENT)
    private void openScreen() {
        net.minecraft.client.Minecraft.getInstance().setScreen(new net.mrbt0907.weather2.client.gui.GuiEZConfig());
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }
}