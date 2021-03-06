package com.tfc.spookyautumnforests;

import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.monster.SkeletonEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.*;
import net.minecraft.state.Property;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.*;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.carver.WorldCarver;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import net.minecraft.world.gen.surfacebuilders.DefaultSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.world.BiomeGenerationSettingsBuilder;
import net.minecraftforge.common.world.MobSpawnInfoBuilder;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import static net.minecraft.world.gen.feature.Features.*;

@Mod("spooky_autumn_forests")
public class SpookyAutumnForests {
	
	public SpookyAutumnForests() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
		
		bus.register(RegistryEvents.class);
		DistExecutor.unsafeRunForDist(() -> () -> {
			bus.addListener(this::clientSetup);
			MinecraftForge.EVENT_BUS.addListener(Client::onTick);
			return null;
		}, () -> () -> null);
		MinecraftForge.EVENT_BUS.addListener(this::onEntitySpawn);
	}
	
	private void onEntitySpawn(LivingEvent.LivingUpdateEvent t) {
		if (t.getEntityLiving().getEntityWorld().isRemote)
			return;
		IWorld world = t.getEntity().world;
		Biome b = world.getBiome(t.getEntity().getPosition());
		ResourceLocation regName = b.getRegistryName();
		if (t.getEntityLiving() instanceof PlayerEntity) {
			if (b.getAmbience().getSkyColor() == 0) {
				if (world.getWorldInfo().getDayTime() % 1000 == 0) {
					BlockPos pos = new BlockPos(t.getEntity().getPosition());
					pos = pos.add((world.getRandom().nextInt(32) + 32) * (world.getRandom().nextBoolean() ? -1 : 1), 0, (world.getRandom().nextInt(32) + 32) * (world.getRandom().nextBoolean() ? -1 : 1));
					pos = world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, pos);
					EntityType<?>[] entities = new EntityType[]{
							EntityType.ZOMBIE,
							EntityType.SKELETON,
							EntityType.SPIDER,
							EntityType.CREEPER,
					};
					for (int i = 0; i < world.getRandom().nextInt(3) + 1; i++) {
						EntityType<?> type = entities[world.getRandom().nextInt(entities.length - 1)];
						Entity e = type.create(t.getEntity().getEntityWorld());
						if (e != null) {
							e.setPosition(pos.getX(), pos.getY(), pos.getZ());
							world.addEntity(e);
						}
					}
				}
			}
		} else {
			if (regName != null)
				if (regName.toString().equals("spooky_autumn_forests:nightmare_forest")) {
					if (t.getEntityLiving() instanceof SkeletonEntity)
						t.getEntityLiving().setItemStackToSlot(EquipmentSlotType.HEAD, new ItemStack(Items.SKELETON_SKULL));
					else if (t.getEntityLiving() instanceof ZombieEntity)
						t.getEntityLiving().setItemStackToSlot(EquipmentSlotType.HEAD, new ItemStack(Items.ZOMBIE_HEAD));
					else
						t.getEntityLiving().setItemStackToSlot(EquipmentSlotType.HEAD, new ItemStack(RegistryEvents.getCopperLeaves()));
					t.getEntityLiving().extinguish();
				}
			if (b.getAmbience().getSkyColor() == 0) {
				if (t.getEntityLiving() instanceof SkeletonEntity)
					t.getEntityLiving().setItemStackToSlot(EquipmentSlotType.HEAD, new ItemStack(Items.SKELETON_SKULL));
				else if (t.getEntityLiving() instanceof ZombieEntity)
					t.getEntityLiving().setItemStackToSlot(EquipmentSlotType.HEAD, new ItemStack(Items.ZOMBIE_HEAD));
				else
					t.getEntityLiving().setItemStackToSlot(EquipmentSlotType.HEAD, new ItemStack(RegistryEvents.getCopperLeaves()));
				t.getEntityLiving().extinguish();
			}
		}
	}
	
	public void clientSetup(FMLClientSetupEvent event) {
		Client.clientSetup(event);
	}
	
	@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class RegistryEvents {
		public static final HashMap<String, Block> blocks = new HashMap<>();
		
		public static Biome spooky_forest_2;
		
		private static Item copperLeaves;
		
		public static Item getCopperLeaves() {
			return copperLeaves;
		}
		
		@SubscribeEvent
		public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
			for (String[] sa : Registries.regularBlocks) {
				try {
//					Field get = net.minecraft.block.Blocks.class.getDeclaredField(sa[1]);
//					get.setAccessible(true);
//					Block block = new Block(AbstractBlock.Properties.from((AbstractBlock) get.get(null)));
					Block block = new Block(AbstractBlock.Properties.from(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(sa[1].toLowerCase()))));
					block.setRegistryName("spooky_autumn_forests", sa[0]);
					blockRegistryEvent.getRegistry().register(block);
					blocks.put(sa[0], block);
				} catch (Throwable err) {
					err.printStackTrace();
				}
			}
			for (String[] sa : Registries.logs) {
				try {
//					Field get = net.minecraft.block.Blocks.class.getDeclaredField(sa[1]);
//					get.setAccessible(true);
//					Block block = new RotatedPillarBlock(AbstractBlock.Properties.from((AbstractBlock) get.get(null))) {
					Block block = new RotatedPillarBlock(AbstractBlock.Properties.from(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(sa[1].toLowerCase())))) {
						@Override
						public @NotNull ActionResultType onBlockActivated(@NotNull BlockState state, @NotNull World worldIn, @NotNull BlockPos pos, @NotNull PlayerEntity player, @NotNull Hand handIn, @NotNull BlockRayTraceResult hit) {
							if (blocks.containsKey("stripped_" + sa[0])) {
								if (player.getHeldItem(handIn).getToolTypes().contains(ToolType.AXE)) {
									AtomicReference<BlockState> state1 = new AtomicReference<>(blocks.get("stripped_" + sa[0]).getDefaultState());
									state.getProperties().forEach(property -> {
										state1.set(applyProperty(state, state1.get(), property));
									});
									worldIn.setBlockState(pos, state1.get());
									worldIn.playSound(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1, 1, false);
									if (!player.isCreative())
										player.getHeldItem(handIn).damageItem(1, player, (p_220040_1_) -> p_220040_1_.sendBreakAnimation(handIn));
									player.swingArm(handIn);
									return ActionResultType.PASS;
								}
							}
							return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
						}
						
						public <A extends Comparable<A>> @NotNull BlockState applyProperty(@NotNull BlockState sourceState, @NotNull BlockState newState, Property<A> property) {
							return newState.with(property, sourceState.get(property));
						}
					};
					block.setRegistryName("spooky_autumn_forests", sa[0]);
					blockRegistryEvent.getRegistry().register(block);
					blocks.put(sa[0], block);
				} catch (Throwable err) {
					err.printStackTrace();
				}
			}
			for (String[] sa : Registries.doors) {
				try {
//					Field get = net.minecraft.block.Blocks.class.getDeclaredField(sa[1]);
//					get.setAccessible(true);
//					Block block = new DoorBlock(AbstractBlock.Properties.from((AbstractBlock) get.get(null)));
					Block block = new DoorBlock(AbstractBlock.Properties.from(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(sa[1].toLowerCase()))));
					block.setRegistryName("spooky_autumn_forests", sa[0]);
					blockRegistryEvent.getRegistry().register(block);
					blocks.put(sa[0], block);
				} catch (Throwable err) {
					err.printStackTrace();
				}
			}
			for (String[] sa : Registries.trapdoors) {
				try {
//					Field get = net.minecraft.block.Blocks.class.getDeclaredField(sa[1]);
//					get.setAccessible(true);
//					Block block = new TrapDoorBlock(AbstractBlock.Properties.from((AbstractBlock) get.get(null)));
					Block block = new TrapDoorBlock(AbstractBlock.Properties.from(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(sa[1].toLowerCase()))));
					block.setRegistryName("spooky_autumn_forests", sa[0]);
					blockRegistryEvent.getRegistry().register(block);
					blocks.put(sa[0], block);
				} catch (Throwable err) {
					err.printStackTrace();
				}
			}
			for (String[] sa : Registries.leaves) {
				try {
//					Field get = net.minecraft.block.Blocks.class.getDeclaredField(sa[1]);
//					get.setAccessible(true);
//					Block block = new LeavesBlock(AbstractBlock.Properties.from((AbstractBlock) get.get(null)));
					Block block = new LeavesBlock(AbstractBlock.Properties.from(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(sa[1].toLowerCase()))));
					block.setRegistryName("spooky_autumn_forests", sa[0]);
					blockRegistryEvent.getRegistry().register(block);
					blocks.put(sa[0], block);
				} catch (Throwable err) {
					err.printStackTrace();
				}
			}
			{
				Block block = new SaplingBlock(AbstractBlock.Properties.from(Blocks.OAK_SAPLING), true).setRegistryName("spooky_autumn_forests", "spooky_wood_copper_sapling");
				blockRegistryEvent.getRegistry().register(block);
				blocks.put("spooky_wood_copper_sapling", block);
			}
			{
				Block block = new SaplingBlock(AbstractBlock.Properties.from(Blocks.OAK_SAPLING), false).setRegistryName("spooky_autumn_forests", "spooky_wood_sapling");
				blockRegistryEvent.getRegistry().register(block);
				blocks.put("spooky_wood_sapling", block);
			}
			{
				Block block = new SaplingBlock(AbstractBlock.Properties.from(Blocks.OAK_SAPLING), false, true).setRegistryName("spooky_autumn_forests", "spooky_forests_tree");
				blockRegistryEvent.getRegistry().register(block);
				blocks.put("spooky_forests_tree", block);
			}
		}
		
		@SubscribeEvent
		public static void onBiomeRegistry(final RegistryEvent.Register<Biome> biomeRegistryEvent) {
			int r = 128;
			int color = ((0xFF) << 24) |
					((r & 0xFF) << 16) |
					(((r / 2) & 0xFF) << 8) |
					(((r / 7) & 0xFF));
			int color2 = ((0xFF) << 24) |
					((r & 0xFF) << 16) |
					(((r / 2) & 0xFF) << 8) |
					((r & 0xFF));
			spooky_forest_2 = new Biome.Builder()
					.scale(0.1f)
					.temperature(8)
					.category(Biome.Category.FOREST)
					.depth(0.2f)
					.precipitation(Biome.RainType.RAIN)
					.withMobSpawnSettings(
							new MobSpawnInfoBuilder(MobSpawnInfo.EMPTY)
									.withCreatureSpawnProbability(0.9f)
									.withSpawner(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.ZOMBIE, 50, 10, 20))
									.withSpawner(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.SKELETON, 50, 10, 20))
									.withSpawner(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.SPIDER, 50, 10, 20))
									.withSpawner(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.CREEPER, 50, 10, 20))
									.withSpawner(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.ENDERMAN, 50, 10, 20))
									
									.withSpawner(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.ZOMBIE, 50, 10, 60))
									.withSpawner(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.SKELETON, 50, 10, 60))
									.withSpawner(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.SPIDER, 50, 10, 60))
									.withSpawner(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.CREEPER, 50, 10, 60))
									.withSpawner(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.ENDERMAN, 50, 10, 60))
									
									.withSpawner(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.ZOMBIE, 50, 10, 20))
									.withSpawner(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.SKELETON, 50, 10, 20))
									.withSpawner(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.SPIDER, 50, 10, 20))
									.withSpawner(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.CREEPER, 50, 10, 20))
									.withSpawner(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.ENDERMAN, 50, 10, 20))
									.copy()
					).setEffects(
							new BiomeAmbience.Builder()
									.setFogColor(12638463)
									.withFoliageColor(color)
									.setWaterColor(color2)
									.setWaterFogColor(color2)
									.withSkyColor(0)
									.withGrassColor(color)
									.setMoodSound(MoodSoundAmbience.DEFAULT_CAVE)
									.build()
					)
					.downfall(0.25f)
					.withGenerationSettings(
							new BiomeGenerationSettingsBuilder(BiomeGenerationSettings.DEFAULT_SETTINGS)
									.withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, ORE_GOLD)
									.withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, ORE_IRON)
									.withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, ORE_COAL)
									.withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, ORE_DIAMOND)
									.withFeature(GenerationStage.Decoration.LAKES, LAKE_WATER)
									.withFeature(GenerationStage.Decoration.VEGETAL_DECORATION, SpookyAutumnForestsFeatures.TREE)
									.withCarver(GenerationStage.Carving.AIR, WorldCarver.CAVE.func_242761_a(new ProbabilityConfig(0.5f)))
									.withCarver(GenerationStage.Carving.AIR, WorldCarver.CANYON.func_242761_a(new ProbabilityConfig(0.5f)))
									.withSurfaceBuilder(DefaultSurfaceBuilder.DEFAULT.func_242929_a(SurfaceBuilder.GRASS_DIRT_GRAVEL_CONFIG))
									.build()
					)
					.build()
					.setRegistryName(new ResourceLocation("spooky_autumn_forests", "nightmare_forest"));
			biomeRegistryEvent.getRegistry().register(spooky_forest_2);
			BiomeManager.addBiome(
					BiomeManager.BiomeType.WARM,
					new BiomeManager.BiomeEntry(
							RegistryKey.getOrCreateKey(
									Registry.BIOME_KEY,
									new ResourceLocation(
											"spooky_autumn_forests", "nightmare_forest"
									)), 1));
			r = 200;
			color = ((0xFF) << 24) |
					((r & 0xFF) << 16) |
					(((r / 2) & 0xFF) << 8) |
					((0 & 0xFF));
			color2 = ((0xFF) << 24) |
					((57 & 0xFF) << 16) |
					((57 & 0xFF) << 8) |
					((167 & 0xFF));
			spooky_forest_2 = new Biome.Builder()
					.scale(0.1f)
					.temperature(8)
					.category(Biome.Category.FOREST)
					.depth(0.2f)
					.precipitation(Biome.RainType.RAIN)
					.withMobSpawnSettings(
							new MobSpawnInfoBuilder(MobSpawnInfo.EMPTY)
									.withCreatureSpawnProbability(0.9f)
									.withSpawner(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.ZOMBIE, 50, 10, 20))
									.withSpawner(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.SKELETON, 50, 10, 20))
									.withSpawner(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.SPIDER, 50, 10, 20))
									.withSpawner(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.CREEPER, 50, 10, 20))
									.withSpawner(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.ENDERMAN, 50, 10, 20))
									.copy()
					).setEffects(
							new BiomeAmbience.Builder()
									.setFogColor(12638463)
									.withFoliageColor(color)
									.setWaterColor(color2)
									.setWaterFogColor(color2)
									.withSkyColor(8431103)
									.withGrassColor(color)
									.setMoodSound(MoodSoundAmbience.DEFAULT_CAVE)
									.build()
					)
					.downfall(0.25f)
					.withGenerationSettings(
							new BiomeGenerationSettingsBuilder(BiomeGenerationSettings.DEFAULT_SETTINGS)
									.withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, ORE_GOLD)
									.withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, ORE_IRON)
									.withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, ORE_COAL)
									.withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, ORE_DIAMOND)
									.withFeature(GenerationStage.Decoration.LAKES, LAKE_WATER)
									.withFeature(GenerationStage.Decoration.VEGETAL_DECORATION, SpookyAutumnForestsFeatures.TREE)
									.withCarver(GenerationStage.Carving.AIR, WorldCarver.CAVE.func_242761_a(new ProbabilityConfig(0.5f)))
									.withCarver(GenerationStage.Carving.AIR, WorldCarver.CANYON.func_242761_a(new ProbabilityConfig(0.5f)))
									.withSurfaceBuilder(DefaultSurfaceBuilder.DEFAULT.func_242929_a(SurfaceBuilder.GRASS_DIRT_GRAVEL_CONFIG))
									.build()
					)
					.build()
					.setRegistryName(new ResourceLocation("spooky_autumn_forests", "spooky_forest"));
			biomeRegistryEvent.getRegistry().register(spooky_forest_2);
			BiomeManager.addBiome(
					BiomeManager.BiomeType.WARM,
					new BiomeManager.BiomeEntry(
							RegistryKey.getOrCreateKey(
									Registry.BIOME_KEY,
									new ResourceLocation(
											"spooky_autumn_forests", "spooky_forest"
									)), 4));
			r = 140;
			color = ((0xFF) << 24) |
					((r & 0xFF) << 16) |
					(((r / 2) & 0xFF) << 8) |
					((0 & 0xFF));
			int color3 = ((0xFF) << 24) |
					((255 & 0xFF) << 16) |
					(((255 / 2) & 0xFF) << 8) |
					((0 & 0xFF));
			spooky_forest_2 = new Biome.Builder()
					.scale(0.1f)
					.temperature(8)
					.category(Biome.Category.FOREST)
					.depth(0.2f)
					.precipitation(Biome.RainType.RAIN)
					.withMobSpawnSettings(
							new MobSpawnInfoBuilder(MobSpawnInfo.EMPTY)
									.withCreatureSpawnProbability(0.9f)
									.withSpawner(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.ZOMBIE, 50, 10, 20))
									.withSpawner(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.SKELETON, 50, 10, 20))
									.withSpawner(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.SPIDER, 50, 10, 20))
									.withSpawner(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.CREEPER, 50, 10, 20))
									.withSpawner(EntityClassification.MONSTER, new MobSpawnInfo.Spawners(EntityType.ENDERMAN, 50, 10, 20))
									.withSpawner(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.SHEEP, 3, 0, 3))
									.withSpawner(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.PIG, 3, 0, 3))
									.withSpawner(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.CHICKEN, 3, 0, 3))
									.withSpawner(EntityClassification.CREATURE, new MobSpawnInfo.Spawners(EntityType.COW, 3, 0, 3))
									.copy()
					).setEffects(
							new BiomeAmbience.Builder()
									.setFogColor(12638463)
									.withFoliageColor(color)
									.setWaterColor(color2)
									.setWaterFogColor(color2)
									.withSkyColor(8431103)
									.withGrassColor(color3)
									.setMoodSound(MoodSoundAmbience.DEFAULT_CAVE)
									.build()
					)
					.downfall(0.25f)
					.withGenerationSettings(
							new BiomeGenerationSettingsBuilder(BiomeGenerationSettings.DEFAULT_SETTINGS)
									.withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, ORE_GOLD)
									.withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, ORE_IRON)
									.withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, ORE_COAL)
									.withFeature(GenerationStage.Decoration.UNDERGROUND_ORES, ORE_DIAMOND)
									.withFeature(GenerationStage.Decoration.LAKES, LAKE_WATER)
									.withFeature(GenerationStage.Decoration.VEGETAL_DECORATION, SpookyAutumnForestsFeatures.TREE)
									.withCarver(GenerationStage.Carving.AIR, WorldCarver.CAVE.func_242761_a(new ProbabilityConfig(0.5f)))
									.withCarver(GenerationStage.Carving.AIR, WorldCarver.CANYON.func_242761_a(new ProbabilityConfig(0.5f)))
									.withSurfaceBuilder(DefaultSurfaceBuilder.DEFAULT.func_242929_a(SurfaceBuilder.GRASS_DIRT_GRAVEL_CONFIG))
									.build()
					)
					.build()
					.setRegistryName(new ResourceLocation("spooky_autumn_forests", "autumn_forest"));
			biomeRegistryEvent.getRegistry().register(spooky_forest_2);
			BiomeManager.addBiome(
					BiomeManager.BiomeType.WARM,
					new BiomeManager.BiomeEntry(
							RegistryKey.getOrCreateKey(
									Registry.BIOME_KEY,
									new ResourceLocation(
											"spooky_autumn_forests", "autumn_forest"
									)), 8));
		}
		
		@SubscribeEvent
		public static void onItemsRegistry(final RegistryEvent.Register<Item> itemRegistryEvent) {
			for (String[] sa : Registries.regularBlocks) {
				Item item = new BlockItem(blocks.get(sa[0]), new Item.Properties().group(ItemGroup.BUILDING_BLOCKS));
				item.setRegistryName("spooky_autumn_forests", sa[0]);
				itemRegistryEvent.getRegistry().register(item);
			}
			for (String[] sa : Registries.logs) {
				Item item = new BlockItem(blocks.get(sa[0]), new Item.Properties().group(ItemGroup.BUILDING_BLOCKS));
				item.setRegistryName("spooky_autumn_forests", sa[0]);
				itemRegistryEvent.getRegistry().register(item);
			}
			for (String[] sa : Registries.trapdoors) {
				Item item = new BlockItem(blocks.get(sa[0]), new Item.Properties().group(ItemGroup.REDSTONE));
				item.setRegistryName("spooky_autumn_forests", sa[0]);
				itemRegistryEvent.getRegistry().register(item);
			}
			for (String[] sa : Registries.doors) {
				Item item = new TallBlockItem(blocks.get(sa[0]), new Item.Properties().group(ItemGroup.REDSTONE));
				item.setRegistryName("spooky_autumn_forests", sa[0]);
				itemRegistryEvent.getRegistry().register(item);
			}
			for (String[] sa : Registries.leaves) {
				Item item = new BlockItem(blocks.get(sa[0]), new Item.Properties().group(ItemGroup.REDSTONE));
				item.setRegistryName("spooky_autumn_forests", sa[0]);
				itemRegistryEvent.getRegistry().register(item);
				if (sa[0].equals("spooky_leaves_copper")) {
					copperLeaves = item;
				}
			}
			itemRegistryEvent.getRegistry().register(new BlockItem(blocks.get("spooky_wood_copper_sapling"), new Item.Properties().group(ItemGroup.DECORATIONS)).setRegistryName("spooky_autumn_forests", "spooky_wood_copper_sapling"));
			itemRegistryEvent.getRegistry().register(new BlockItem(blocks.get("spooky_wood_sapling"), new Item.Properties().group(ItemGroup.DECORATIONS)).setRegistryName("spooky_autumn_forests", "spooky_wood_sapling"));
		}
	}
}
