package com.wuest.utilities.Blocks;

import java.util.Random;

import com.wuest.utilities.ModRegistry;

import net.minecraft.block.BlockGlowstone;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public abstract class BlockGlowstoneSlab extends BlockSlab
{
	/**
	 * The property used for the variant.
	 * Needed for interactions with ItemSlab.
	 */
	private static final PropertyBool VARIANT_PROPERTY = PropertyBool.create("variant");

	public BlockGlowstoneSlab()
	{
		super(Material.GROUND);

		this.setSoundType(SoundType.GLASS);
		IBlockState iblockstate = this.blockState.getBaseState();
		this.setHardness(0.5F);
		this.setHarvestLevel("pickaxe", 0);
		this.setTickRandomly(true);
		this.setLightLevel(1.0f);
		
		if (!this.isDouble())
		{
			iblockstate = iblockstate.withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM);
			ModRegistry.setBlockName(this, "blockhalfglowstoneslab");
			this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
		}
		else
		{
			ModRegistry.setBlockName(this, "blockglowstoneslab");
		}
		
		iblockstate = iblockstate.withProperty(VARIANT_PROPERTY, false);

		this.setDefaultState(iblockstate);
		this.useNeighborBrightness = !this.isDouble();
	}

	@Override
	public String getUnlocalizedName(int meta)
	{
		return super.getUnlocalizedName();
	}
	
	@Override
	public boolean isDouble()
	{
		return false;
	}

	@Override
	public IProperty<?> getVariantProperty()
	{
		return VARIANT_PROPERTY;
	}

	@Override
	public Comparable<?> getTypeForItem(ItemStack stack)
	{
		return false;
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		IBlockState blockState = this.getDefaultState();
		blockState = blockState.withProperty(VARIANT_PROPERTY, false);
		
		if (!this.isDouble()) 
		{
			EnumBlockHalf value = EnumBlockHalf.BOTTOM;

			if ((meta & 8) != 0) 
			{
				value = EnumBlockHalf.TOP;
			}

			blockState = blockState.withProperty(HALF, value);
		}

		return blockState;
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state)
	{
		int i = 0;

		if (!this.isDouble() && state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP)
		{
			i |= 8;
		}

		return i;
	}

	@Override
	protected BlockStateContainer createBlockState()
	{
		return this.isDouble() ? new BlockStateContainer(this, new IProperty[] {VARIANT_PROPERTY}): new BlockStateContainer(this, new IProperty[] {HALF, VARIANT_PROPERTY});
	}

	/**
	 * Get the MapColor for this Block and the given BlockState
	 */
	@Override
	public MapColor getMapColor(IBlockState state)
	{
		return ((BlockGlowstone)Blocks.GLOWSTONE).getMapColor(Blocks.GLOWSTONE.getDefaultState());
	}

	/**
	 * Get the Item that this Block should drop when harvested.
	 */
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune)
	{
		return Item.getItemFromBlock(ModRegistry.GlowstoneSlab());
	}

	/**
	 * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
	 * returns the metadata of the dropped item based on the old metadata of the block.
	 */
	@Override
	public int damageDropped(IBlockState state)
	{
		return 0;
	}

}