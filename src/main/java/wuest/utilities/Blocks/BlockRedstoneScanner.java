package wuest.utilities.Blocks;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import wuest.utilities.WuestUtilities;
import wuest.utilities.Gui.GuiRedstoneScanner;
import wuest.utilities.Proxy.CommonProxy;
import wuest.utilities.Tiles.TileEntityRedstoneScanner;

/**
 * This class is to provide a way to scan for entities and if a particular entity was found, generate a redstone signal.
 * @author WuestMan
 *
 */
public class BlockRedstoneScanner extends Block implements ITileEntityProvider
{
	public static final PropertyBool POWERED = PropertyBool.create("powered");
	public static BlockRedstoneScanner RegisteredBlock;
	
	/**
	 * Registers this block in the game registry and adds the block recipe.
	 */
	public static void RegisterBlock()
	{
		BlockRedstoneScanner.RegisteredBlock = new BlockRedstoneScanner();
		
		CommonProxy.registerBlock(BlockRedstoneScanner.RegisteredBlock);
		
		GameRegistry.registerTileEntity(TileEntityRedstoneScanner.class, "RedstoneScanner");
		
		GameRegistry.addShapedRecipe(new ItemStack(BlockRedstoneScanner.RegisteredBlock), 
				"x x",
				"zyz",
				"x x",
				'x', Items.REPEATER,
				'y', Item.getItemFromBlock(Blocks.REDSTONE_BLOCK),
				'z', Item.getItemFromBlock(Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE));
	}

	protected static final AxisAlignedBB BOUNDING_AABB = new AxisAlignedBB(0.0625, 0.00001D, 0.0625D, 0.9375, 0.625D, 0.9375D);
	protected static final AxisAlignedBB BASE_AABB = new AxisAlignedBB(0.125D, 0.0625D, 0.125D, 0.875, 0.1875, 0.875D);
	protected static final AxisAlignedBB STICK1_AABB = new AxisAlignedBB(0.1875D, 0.0D, 0.1875D, 0.3125D, 0.625D, 0.3125D);
	protected static final AxisAlignedBB STICK2_AABB = new AxisAlignedBB(0.6875D, 0.0D, 0.6875D, 0.8125D, 0.625D, 0.8125D);
	protected static final AxisAlignedBB STICK3_AABB = new AxisAlignedBB(0.1875D, 0.0D, 0.6875D, 0.3125D, 0.625D, 0.8125D);
	protected static final AxisAlignedBB STICK4_AABB = new AxisAlignedBB(0.6875D, 0.0D, 0.1875D, 0.8125D, 0.625D, 0.3125D);
	
	protected int tickRate = 20;
	
	/**
	 * Initializes a new instance of the BlockMiniRedstone.
	 */
	public BlockRedstoneScanner()
	{
		super(Material.IRON, MapColor.TNT);
		this.setCreativeTab(CreativeTabs.REDSTONE);
		CommonProxy.setBlockName(this, "blockRedstoneScanner");
		this.setHarvestLevel(null, 0);
		this.setHardness(.5f);
		this.setResistance(10.0f);
		this.setSoundType(SoundType.METAL);
		this.setDefaultState(this.blockState.getBaseState().withProperty(POWERED, Boolean.valueOf(true)));
	}
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
	{
		TileEntityRedstoneScanner tileEntity = this.getLocalTileEntity(worldIn, pos);

		if (tileEntity.getRedstoneStrength() > 0)
		{
			this.updateNeighbors(worldIn, pos);
		}

		super.breakBlock(worldIn, pos, state);
		worldIn.removeTileEntity(pos);
	}
	
	/**
	 * Determine if this block can make a redstone connection on the side provided,
	 * Useful to control which sides are inputs and outputs for redstone wires.
	 *
	 * @param world The current world
	 * @param pos Block position in world
	 * @param side The side that is trying to make the connection, CAN BE NULL
	 * @return True to make the connection
	 */
	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side)
	{
		/**
		 * Can this block provide power. Only wire currently seems to have this change based on its state.
		 */
		return this.canProvidePower(state) && side != null;
	}
	
	/**
     * Determines if the player can harvest this block, obtaining it's drops when the block is destroyed.
     *
     * @param player The player damaging the block, may be null
     * @param meta The block's current metadata
     * @return True to spawn the drops
     */
    @Override
	public boolean canHarvestBlock(IBlockAccess world, BlockPos pos, EntityPlayer player)
    {
        return true;
    }
    
	/**
	 * Determines if a torch can be placed on the top surface of this block.
	 * Useful for creating your own block that torches can be on, such as fences.
	 *
	 * @param state The current state
	 * @param world The current world
	 * @param pos Block position in world
	 * @return True to allow the torch to be placed
	 */
	@Override
	public boolean canPlaceTorchOnTop(IBlockState state, IBlockAccess world, BlockPos pos)
	{
		return false;
	}
	
	@Override
	public boolean canProvidePower(IBlockState state)
	{
		return true;
	}
	
	@Override
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }
	
    /**
     * The type of render function called. 3 for standard block models, 2 for TESR's, 1 for liquids, -1 is no render
     */
    @Override
	public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }
    
    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn)
    {
        this.addCollisionBoxToList(pos, entityBox, collidingBoxes, STICK1_AABB);
        this.addCollisionBoxToList(pos, entityBox, collidingBoxes, STICK2_AABB);
        this.addCollisionBoxToList(pos, entityBox, collidingBoxes, STICK3_AABB);
        this.addCollisionBoxToList(pos, entityBox, collidingBoxes, STICK4_AABB);
        this.addCollisionBoxToList(pos, entityBox, collidingBoxes, BASE_AABB);
    }
    
    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return BOUNDING_AABB;
    }
	
    /**
     * Used to determine ambient occlusion and culling when rebuilding chunks for render
     */
    @Override
	public boolean isOpaqueCube(IBlockState state)
    {
        return false;
    }

	@Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }
	
	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) 
	{
		//System.out.println("Creating new tile entity.");
		return new TileEntityRedstoneScanner();
	}
	
	/**
	 * Gets the redstone scanner entity at the current position.
	 * @param worldIn The world to the entity for.
	 * @param pos The position in the world to get the entity for.
	 * @return Null if the tile was not found or if one was found and is not a redstone scanner entity. Otherwise the TileEntityRedstoneScanner instance.
	 */
	public TileEntityRedstoneScanner getLocalTileEntity(World worldIn, BlockPos pos)
	{
		TileEntity entity = worldIn.getTileEntity(pos);

		if (entity != null && entity.getClass() == TileEntityRedstoneScanner.class)
		{
			return (TileEntityRedstoneScanner) entity;
		}
		else
		{
			TileEntityRedstoneScanner tileEntity = new TileEntityRedstoneScanner();
			worldIn.setTileEntity(pos, tileEntity);
			tileEntity.setPos(pos);

			return tileEntity;
		}
	}
	
	@Override
	public int getStrongPower(IBlockState blockState, IBlockAccess worldIn, BlockPos pos,EnumFacing side)
	{
		TileEntityRedstoneScanner tileEntity = this.getLocalTileEntity((World)worldIn, pos);

		return tileEntity.getRedstoneStrength();
	}
	
	@Override
	public int getWeakPower(IBlockState blockState, IBlockAccess worldIn, BlockPos pos, EnumFacing side)
	{
		TileEntityRedstoneScanner tileEntity = this.getLocalTileEntity((World)worldIn, pos);

		return tileEntity.getRedstoneStrength();
	}
	
	@Override
	public boolean hasTileEntity(IBlockState state)
	{
		return true;
	}
	
	public void notifyNeighborsOfStateChange(World worldIn, BlockPos pos, Block blockType)
	{
		if(net.minecraftforge.event.ForgeEventFactory.onNeighborNotify(worldIn, pos, worldIn.getBlockState(pos), java.util.EnumSet.allOf(EnumFacing.class)).isCanceled())
		{
			return;
		}

		worldIn.notifyBlockOfStateChange(pos.west(), blockType);
		worldIn.notifyBlockOfStateChange(pos.east(), blockType);
		worldIn.notifyBlockOfStateChange(pos.down(), blockType);
		worldIn.notifyBlockOfStateChange(pos.up(), blockType);
		worldIn.notifyBlockOfStateChange(pos.north(), blockType);
		worldIn.notifyBlockOfStateChange(pos.south(), blockType);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
	{
		if (world.isRemote) 
		{
			player.openGui(WuestUtilities.instance, GuiRedstoneScanner.GUI_ID, world, pos.getX(), pos.getY(), pos.getZ());
		}

		return true;
	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state)
	{
		for (EnumFacing enumfacing : EnumFacing.values())
		{
			worldIn.notifyNeighborsOfStateChange(pos.offset(enumfacing), this);
		}

		worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));

		if (worldIn.getTileEntity(pos) == null)
		{
			TileEntityRedstoneScanner scanner = new TileEntityRedstoneScanner();
			worldIn.setTileEntity(pos, scanner);
		}
	}
	
	@Override
	public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int eventID, int eventParam) 
	{
		super.eventReceived(state, worldIn, pos, eventID, eventParam);
		TileEntity tileentity = worldIn.getTileEntity(pos);
		return tileentity == null ? false : tileentity.receiveClientEvent(eventID, eventParam);
	}
	
	/**
	 * Called by ItemBlocks just before a block is actually set in the world, to allow for adjustments to the
	 * IBlockstate
	 */
	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		return this.getDefaultState();
	}
	
	/**
	 * How many world ticks before ticking
	 */
	@Override
	public int tickRate(World worldIn)
	{
		return this.tickRate;
	}
	
	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		return this.getDefaultState().withProperty(POWERED, Boolean.valueOf(meta == 1));
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state)
	{
		return ((Boolean)state.getValue(POWERED)).booleanValue() ? 1 : 0;
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return new BlockStateContainer(this, new IProperty[] {POWERED});
	}
	
	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
	{
		this.updateState(worldIn, pos, state);
	}
	
	/**
	 * Notify block and block below of changes
	 */
	public void updateNeighbors(World worldIn, BlockPos pos)
	{
		this.notifyNeighborsOfStateChange(worldIn, pos, this);
		this.notifyNeighborsOfStateChange(worldIn, pos.down(), this);
	}

	public void updateState(World worldIn, BlockPos pos, IBlockState state)
	{
		TileEntityRedstoneScanner tileEntity = this.getLocalTileEntity(worldIn, pos);
		state = tileEntity.setRedstoneStrength(state);
		worldIn.setBlockState(pos, state, 3);
		tileEntity.markDirty();
		this.updateNeighbors(worldIn, pos);
		worldIn.markBlockRangeForRenderUpdate(pos, pos);
		worldIn.scheduleUpdate(pos, this, tileEntity.getTickDelay());
	}

}