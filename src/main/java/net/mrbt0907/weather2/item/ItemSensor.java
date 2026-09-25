package net.mrbt0907.weather2.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ItemSensor extends ItemBase {
    private final int type;

    public ItemSensor(int type, Item.Properties properties) {
        super(properties);
        this.type = type;
    }

    @Override
    public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);

        if (!worldIn.isClientSide && stack.getItem().equals(this)) {
            CompoundNBT nbt = stack.getOrCreateTag();
            nbt.putBoolean("enabled", !nbt.getBoolean("enabled"));
        }

        return ActionResult.pass(stack);
    }

    public int getType() {
        return type;
    }
}