package thedarkcolour.futuremc.block;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thedarkcolour.core.block.InteractionBlock;
import thedarkcolour.futuremc.FutureMC;
import thedarkcolour.futuremc.entity.bee.EntityBee;
import thedarkcolour.futuremc.init.FutureConfig;
import thedarkcolour.futuremc.tile.TileBeeHive;

import java.util.List;

public class BlockBeeHive extends InteractionBlock {
    public static final PropertyBool IS_FULL = PropertyBool.create("full");
    public static final PropertyDirection FACING = BlockHorizontal.FACING;

    public BlockBeeHive(String regName) {
        super(regName, Material.WOOD, SoundType.WOOD);
        setDefaultState(getDefaultState().withProperty(FACING, EnumFacing.NORTH).withProperty(IS_FULL, false));
        setCreativeTab(FutureConfig.general.useVanillaTabs ? CreativeTabs.DECORATIONS : FutureMC.TAB);
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileBeeHive();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, IS_FULL);
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
        if (worldIn.getTileEntity(pos) instanceof TileBeeHive) {
            return ((TileBeeHive) worldIn.getTileEntity(pos)).getHoneyLevel();
        }

        return 0;
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World worldIn, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        if (!worldIn.isRemote) {
            if (worldIn.getTileEntity(pos) instanceof TileBeeHive && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, player.getHeldItemMainhand()) == 0) {
                ((TileBeeHive)worldIn.getTileEntity(pos)).angerBees(player, TileBeeHive.BeeState.DELIVERED);
                worldIn.updateComparatorOutputLevel(pos, this);
            }

            List<EntityBee> nearbyBees = worldIn.getEntitiesWithinAABB(EntityBee.class, (new AxisAlignedBB(pos)).expand(8.0D, 6.0D, 8.0D));
            if (!nearbyBees.isEmpty()) {
                List<EntityPlayer> nearbyPlayers = worldIn.getEntitiesWithinAABB(EntityPlayer.class, (new AxisAlignedBB(pos)).expand(8.0D, 6.0D, 8.0D));
                int int_1 = nearbyPlayers.size();

                for (EntityBee bee : nearbyBees) {
                    if (bee.getAttackTarget() == null) {
                        bee.setBeeAttacker(nearbyPlayers.get(worldIn.rand.nextInt(int_1)));
                    }
                }
            }
        }

        return super.removedByPlayer(state, worldIn, pos, player, willHarvest);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(IS_FULL, (meta & 4) != 0).withProperty(FACING, EnumFacing.byHorizontalIndex(meta & -5));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return (state.getValue(IS_FULL) ? 4 : 0) | state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public BlockBeeHive setHardness(float hardness) {
        super.setHardness(hardness);
        return this;
    }
}