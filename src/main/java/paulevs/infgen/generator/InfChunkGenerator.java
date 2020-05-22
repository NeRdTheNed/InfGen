package paulevs.infgen.generator;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos.Mutable;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeLayerSampler;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorType;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.StructureFeature;
import paulevs.infgen.InfdevWorldType;
import paulevs.infgen.generator.port.InfGenPort;

public class InfChunkGenerator extends ChunkGenerator<InfWorldChunkGeneratorConfig>
{
	public static final ChunkGeneratorType<InfWorldChunkGeneratorConfig, InfChunkGenerator> INFDEV_CHUNK_GEN;
	private static final BlockState BEDROCK = Blocks.BEDROCK.getDefaultState();
	private static final Mutable POS = new Mutable();
	private static final ChunkRandom RND = new ChunkRandom();
	
	private BiomeLayerSampler oceanBiomes;
	private InfGenPort generator;
	
	public InfChunkGenerator(IWorld world, BiomeSource biomeSource, InfWorldChunkGeneratorConfig config)
	{
		super(world, biomeSource, config);
		generator = new InfGenPort(world.getSeed());
		oceanBiomes = config.onlyInfdev() ? null : InfOceanLayer.build(world.getSeed(), InfdevWorldType.INFDEV, config);
	}
	
	public static void register() {}
	
	static
	{
		INFDEV_CHUNK_GEN = new ChunkGeneratorType<InfWorldChunkGeneratorConfig, InfChunkGenerator>(null, false, InfWorldChunkGeneratorConfig::new)
		{
			@Override
			public InfChunkGenerator create(World world, BiomeSource biomeSource, InfWorldChunkGeneratorConfig config)
			{
			      return new InfChunkGenerator(world, biomeSource, config);
			}
		};
		Registry.register(Registry.CHUNK_GENERATOR_TYPE, "inf_gen", INFDEV_CHUNK_GEN);
	}

	@Override
	public void buildSurface(ChunkRegion chunkRegion, Chunk chunk)
	{
		generator.makeChunk(chunk.getPos().x, chunk.getPos().z, chunk, oceanBiomes, chunkRegion);
		if (config.hasBedrock())
			buildBedrock(chunk);
	}

	@Override
	public int getSpawnHeight()
	{
		return this.world.getSeaLevel() + 1;
	}

	@Override
	public void populateNoise(IWorld world, Chunk chunk) {}
	
	@Override
	public int getSeaLevel()
	{
		return 63;
	}
	
	public int getHeightOnGround(int x, int z, Heightmap.Type heightmapType)
	{
		return generator.getHeight(x, z);
	}
	
	public void populate(IWorld world, int x, int z)
	{
		generator.populate(world, world.getChunk(x, z));
	}
	
	@Override
	public boolean hasStructure(Biome biome, StructureFeature<? extends FeatureConfig> structureFeature)
	{
		return biome.hasStructureFeature(structureFeature) && config.hasStructure(structureFeature);
	}
	
	@Override
	public void carve(BiomeAccess biomeAccess, Chunk chunk, GenerationStep.Carver carver)
	{
		if (config.carveCaves())
		{
			super.carve(biomeAccess, chunk, carver);
		}
	}
	
	private void buildBedrock(Chunk chunk)
	{
		RND.setSeed(chunk.getPos().x, chunk.getPos().z);
		for (int x = 0; x < 16; x++)
		{
			POS.setX(x);
			for (int z = 0; z < 16; z++)
			{
				POS.setZ(z);
				int h = 1 + RND.nextInt(2);
				for (int y = 0; y < h; y++)
				{
					POS.setY(y);
					chunk.setBlockState(POS, BEDROCK, false);
				}
			}
		}
	}
}
