package shadows.apotheosis.adventure.affix.reforging;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import shadows.apotheosis.Apoth;
import shadows.placebo.block_entity.TickingBlockEntity;

public class ReforgingTableTile extends BlockEntity implements TickingBlockEntity {

	public int time = 0;
	public boolean step1 = true;

	protected ItemStackHandler inv = new ItemStackHandler(2) {
		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			if (slot == 0) return ReforgingMenu.isRarityMat(stack);
			return stack.is(Apoth.Items.GEM_DUST);
		};

		@Override
		protected void onContentsChanged(int slot) {
			ReforgingTableTile.this.setChanged();
		};
	};

	public ReforgingTableTile(BlockPos pWorldPosition, BlockState pBlockState) {
		super(Apoth.Tiles.REFORGING_TABLE, pWorldPosition, pBlockState);
	}

	@Override
	public void clientTick(Level pLevel, BlockPos pPos, BlockState pState) {
		Player player = pLevel.getNearestPlayer((double) pPos.getX() + 0.5D, (double) pPos.getY() + 0.5D, (double) pPos.getZ() + 0.5D, 4, false);

		if (player != null) {
			time++;
		} else {
			if (time == 0 && step1) return;
			else time++;
		}

		if (step1 && time == 59) {
			step1 = false;
			time = 0;
		} else if (time == 4 && !step1) {
			Random rand = pLevel.random;
			for (int i = 0; i < 6; i++) {
				pLevel.addParticle(ParticleTypes.CRIT, pPos.getX() + 0.5 - 0.1 * rand.nextDouble(), pPos.getY() + 13 / 16D, pPos.getZ() + 0.5 + 0.1 * rand.nextDouble(), 0, 0, 0);
			}
			pLevel.playLocalSound(pPos.getX(), pPos.getY(), pPos.getZ(), SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.03F, 1.7F + rand.nextFloat() * 0.2F, true);
			step1 = true;
			time = 0;
		}
	}

	@Override
	public void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		tag.put("inventory", this.inv.serializeNBT());
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.inv.deserializeNBT(tag.getCompound("inventory"));
	}

	LazyOptional<IItemHandler> invCap = LazyOptional.of(() -> this.inv);

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return this.invCap.cast();
		return super.getCapability(cap, side);
	}

	@Override
	public void invalidateCaps() {
		super.invalidateCaps();
		this.invCap.invalidate();
	}

	@Override
	public void reviveCaps() {
		super.reviveCaps();
		this.invCap = LazyOptional.of(() -> this.inv);
	}

}
