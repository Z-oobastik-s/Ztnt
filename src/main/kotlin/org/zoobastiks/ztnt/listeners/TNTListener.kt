package org.zoobastiks.ztnt.listeners

import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.entity.TNTPrimed
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.entity.ExplosionPrimeEvent
import org.bukkit.inventory.ItemStack
import org.zoobastiks.ztnt.Ztnt
import org.zoobastiks.ztnt.utils.ExplosionAnimationUtil

class TNTListener(private val plugin: Ztnt) : Listener {

    /**
     * Обрабатывает размещение блока TNT
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBlockPlace(event: BlockPlaceEvent) {
        val player = event.player
        val block = event.block
        val item = event.itemInHand
        val world = block.world
        
        // Проверяем, что блок - TNT и предмет в руке - кастомный TNT
        if (block.type == Material.TNT && plugin.tntManager.isCustomTNT(item)) {
            // Получаем тип TNT
            val tntType = plugin.tntManager.getTNTType(item) ?: return
            
            // Проверяем, разрешено ли использование TNT в этом мире
            if (!plugin.configManager.isWorldAllowed(world.name)) {
                // Отменяем размещение блока
                event.isCancelled = true
                
                // Отправляем сообщение об ошибке
                plugin.configManager.sendMessage(player, plugin.configManager.getMessage("messages.error.world-denied"))
                
                // Отладочное сообщение
                plugin.configManager.debug("Игрок ${player.name} попытался разместить TNT в запрещенном мире ${world.name}")
                
                return
            }
            
            // Отменяем стандартное размещение блока
            event.isCancelled = true
            
            // Спавним TNTPrimed вместо блока
            val tnt = block.world.spawnEntity(block.location.add(0.5, 0.0, 0.5), EntityType.TNT) as TNTPrimed
            
            // Устанавливаем свойства TNT
            tnt.fuseTicks = 80 // 4 секунды
            tnt.source = player // Устанавливаем игрока как источник
            
            // Устанавливаем тип TNT в метаданных
            plugin.tntManager.setTNTEntityType(tnt, tntType)
            
            // Отладочное сообщение
            plugin.configManager.debug("Игрок ${player.name} разместил кастомное TNT типа: $tntType в мире ${world.name}")
        }
    }
    
    /**
     * Обрабатывает событие подготовки к взрыву TNT
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onExplosionPrime(event: ExplosionPrimeEvent) {
        val entity = event.entity
        
        // Проверяем, что это TNT
        if (entity is TNTPrimed) {
            // Получаем тип TNT из метаданных
            val tntType = plugin.tntManager.getTNTEntityType(entity) ?: return
            
            // Получаем радиус взрыва из конфигурации
            val radius = plugin.configManager.getExplosionRadius(tntType).toFloat()
            
            // Устанавливаем радиус взрыва
            event.radius = radius
            
            // Отладочное сообщение
            plugin.configManager.debug("TNT типа $tntType начинает взрыв с радиусом $radius")
            
            // Воспроизводим анимацию взрыва, если осталось меньше 10 тиков
            if (entity.fuseTicks <= 10) {
                val animationType = plugin.configManager.getExplosionAnimation(tntType)
                ExplosionAnimationUtil.playCountdownAnimation(entity.location, animationType)
            }
        }
    }
    
    /**
     * Обрабатывает событие взрыва TNT
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEntityExplode(event: EntityExplodeEvent) {
        val entity = event.entity
        
        // Проверяем, что это TNT
        if (entity is TNTPrimed) {
            // Получаем тип TNT из метаданных
            val tntType = plugin.tntManager.getTNTEntityType(entity) ?: return
            
            // Получаем параметры взрыва из конфигурации
            val damage = plugin.configManager.getExplosionDamage(tntType)
            val shouldSetFire = plugin.configManager.isExplosionFire(tntType)
            val shouldBreakBlocks = plugin.configManager.isExplosionBreakBlocks(tntType)
            val radius = plugin.configManager.getExplosionRadius(tntType)
            
            // Управление разрушением блоков
            if (!shouldBreakBlocks) {
                event.blockList().clear()
            } else {
                // Оставляем только бедрок в списке неразрушаемых блоков
                // Удаляем из списка разрушаемых блоков только бедрок
                event.blockList().removeIf { it.type == Material.BEDROCK }
                
                // Увеличиваем радиус взрыва для большей разрушительной силы
                val additionalBlocks = mutableListOf<org.bukkit.block.Block>()
                
                // Добавляем блоки в радиусе действия взрыва
                for (x in -radius..radius) {
                    for (y in -radius..radius) {
                        for (z in -radius..radius) {
                            if (x*x + y*y + z*z <= radius * radius) {
                                val blockLocation = entity.location.clone().add(x.toDouble(), y.toDouble(), z.toDouble())
                                val block = blockLocation.block
                                
                                // Добавляем блок, если он не бедрок и не воздух
                                if (block.type != Material.BEDROCK && block.type != Material.AIR 
                                    && !event.blockList().contains(block) && Math.random() < 0.2) {
                                    additionalBlocks.add(block)
                                }
                            }
                        }
                    }
                }
                
                // Добавляем дополнительные блоки к взрыву (максимум 100, чтобы избежать лагов)
                additionalBlocks.shuffled().take(100).forEach { event.blockList().add(it) }
            }
            
            // Воспроизводим анимацию взрыва
            val animationType = plugin.configManager.getExplosionAnimation(tntType)
            ExplosionAnimationUtil.playExplosionAnimation(entity.location, animationType)
            
            // Наносим урон ближайшим игрокам
            val location = entity.location
            val nearbyPlayers = location.world.getNearbyEntities(location, radius.toDouble(), radius.toDouble(), radius.toDouble()) { it is Player }
            
            nearbyPlayers.filterIsInstance<Player>().forEach { player ->
                // Рассчитываем урон в зависимости от расстояния до эпицентра взрыва
                val distance = player.location.distance(location)
                if (distance <= radius) {
                    val distanceFactor = 1.0 - (distance / radius)
                    val finalDamage = (damage * distanceFactor).toDouble()
                    
                    // Наносим урон игроку
                    player.damage(finalDamage, entity)
                    
                    // Эффект молнии для TNT типа "lightning"
                    if (tntType == "lightning" && Math.random() < 0.3) {
                        player.world.strikeLightning(player.location)
                    }
                    
                    // Отладочное сообщение
                    plugin.configManager.debug("Игрок ${player.name} получил урон $finalDamage от TNT типа $tntType")
                }
            }
            
            // Создаем огонь, если нужно
            if (shouldSetFire) {
                // Создаем огонь в случайных местах в радиусе взрыва
                val world = location.world
                for (i in 0 until radius * 2) {
                    val x = location.x + (Math.random() * radius * 2 - radius)
                    val y = location.y + (Math.random() * radius - radius / 2)
                    val z = location.z + (Math.random() * radius * 2 - radius)
                    
                    val fireLocation = location.clone().set(x, y, z)
                    val block = fireLocation.block
                    
                    if (block.type == Material.AIR && block.getRelative(0, -1, 0).type.isSolid) {
                        block.type = Material.FIRE
                    }
                }
            }
            
            // Специальные эффекты для разных типов TNT
            when (tntType) {
                "nuke" -> {
                    // Дополнительные эффекты для ядерного взрыва
                    for (i in 0 until 5) {
                        location.world.strikeLightning(location.clone().add(
                            (Math.random() * radius * 0.5) - (radius * 0.25),
                            0.0,
                            (Math.random() * radius * 0.5) - (radius * 0.25)
                        ))
                    }
                    // Добавляем взрывную волну
                    location.world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 5.0f, 0.5f)
                    location.world.spawnParticle(Particle.EXPLOSION, location, 20, radius * 0.5, radius * 0.5, radius * 0.5, 0.1)
                    
                    // Добавляем эффект радиации
                    val greenFireColor = Color.fromRGB(0, 255, 0)
                    nearbyPlayers.filterIsInstance<Player>().forEach { player ->
                        player.addPotionEffect(org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.GLOWING, 200, 1))
                        player.addPotionEffect(org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.POISON, 200, 1))
                    }
                }
                "lightning" -> {
                    // Множество молний в радиусе взрыва
                    for (i in 0 until 10) {
                        val lightningLoc = location.clone().add(
                            (Math.random() * radius) - (radius * 0.5),
                            0.0,
                            (Math.random() * radius) - (radius * 0.5)
                        )
                        location.world.strikeLightning(lightningLoc)
                    }
                    // Добавляем эффект электричества
                    location.world.spawnParticle(Particle.ENCHANTED_HIT, location, 200, radius * 0.5, radius * 0.5, radius * 0.5, 0.5)
                    
                    // Электрошок для игроков
                    nearbyPlayers.filterIsInstance<Player>().forEach { player ->
                        player.addPotionEffect(org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SLOWNESS, 100, 2))
                    }
                }
                "black_hole" -> {
                    // Эффект "затягивания" для черной дыры - притягиваем всех игроков к эпицентру
                    val force = 1.5
                    nearbyPlayers.forEach { entity ->
                        val entityLoc = entity.location
                        val direction = location.toVector().subtract(entityLoc.toVector()).normalize().multiply(force)
                        entity.velocity = entity.velocity.add(direction)
                    }
                    
                    // Визуальные эффекты черной дыры
                    location.world.spawnParticle(Particle.DRAGON_BREATH, location, 500, radius * 0.3, radius * 0.3, radius * 0.3, 0.01)
                    location.world.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 3.0f, 0.5f)
                    
                    // Затемнение экрана для игроков
                    nearbyPlayers.filterIsInstance<Player>().forEach { player ->
                        player.addPotionEffect(org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS, 60, 1))
                    }
                }
                "frost" -> {
                    // Ледяной взрыв с замораживающим эффектом
                    location.world.spawnParticle(Particle.SNOWFLAKE, location, 300, radius * 0.5, radius * 0.5, radius * 0.5, 0.1)
                    location.world.spawnParticle(Particle.CLOUD, location, 200, radius * 0.4, radius * 0.4, radius * 0.4, 0.2)
                    location.world.playSound(location, Sound.BLOCK_GLASS_BREAK, 3.0f, 1.2f)
                    
                    // Эффект замедления для игроков и замораживание воды в радиусе
                    nearbyPlayers.filterIsInstance<Player>().forEach { player ->
                        player.addPotionEffect(org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.SLOWNESS, 200, 3))
                        player.addPotionEffect(org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.LEVITATION, 20, 0))
                    }
                    
                    // Замораживаем воду в радиусе
                    for (x in -radius/2..radius/2) {
                        for (y in -radius/2..radius/2) {
                            for (z in -radius/2..radius/2) {
                                if (x*x + y*y + z*z <= (radius/2) * (radius/2)) {
                                    val blockLocation = location.clone().add(x.toDouble(), y.toDouble(), z.toDouble())
                                    val block = blockLocation.block
                                    
                                    if (block.type == Material.WATER) {
                                        block.type = Material.ICE
                                    } else if (block.type == Material.LAVA) {
                                        block.type = Material.OBSIDIAN
                                    }
                                }
                            }
                        }
                    }
                }
                "quantum" -> {
                    // Квантовый эффект: случайная телепортация игроков и сущностей
                    location.world.spawnParticle(Particle.PORTAL, location, 500, radius * 0.5, radius * 0.5, radius * 0.5, 0.1)
                    location.world.spawnParticle(Particle.REVERSE_PORTAL, location, 200, radius * 0.3, radius * 0.3, radius * 0.3, 0.05)
                    location.world.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 3.0f, 1.5f)
                    
                    // Случайная телепортация игроков
                    nearbyPlayers.filterIsInstance<Player>().forEach { player ->
                        if (Math.random() < 0.7) {
                            val teleportX = location.x + (Math.random() * radius * 2 - radius)
                            val teleportZ = location.z + (Math.random() * radius * 2 - radius)
                            val teleportY = location.world.getHighestBlockYAt(teleportX.toInt(), teleportZ.toInt()).toDouble() + 1
                            val teleportLocation = org.bukkit.Location(location.world, teleportX, teleportY, teleportZ)
                            player.teleport(teleportLocation)
                            player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f)
                        }
                        
                        // Добавляем эффект дезориентации
                        player.addPotionEffect(org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.NAUSEA, 200, 1))
                    }
                }
                "inferno" -> {
                    // Адский огонь повсюду
                    location.world.spawnParticle(Particle.FLAME, location, 500, radius * 0.7, radius * 0.7, radius * 0.7, 0.05)
                    location.world.spawnParticle(Particle.LAVA, location, 200, radius * 0.5, radius * 0.5, radius * 0.5, 0.1)
                    location.world.playSound(location, Sound.BLOCK_FIRE_AMBIENT, 5.0f, 0.7f)
                    
                    // Добавляем огненные эффекты игрокам
                    nearbyPlayers.filterIsInstance<Player>().forEach { player ->
                        player.fireTicks = 200 // Поджигаем игрока на 10 секунд
                        player.addPotionEffect(org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.WITHER, 100, 1))
                    }
                    
                    // Создаем лаву в случайных местах
                    for (i in 0 until radius) {
                        val fireX = location.x + (Math.random() * radius - radius/2)
                        val fireZ = location.z + (Math.random() * radius - radius/2)
                        val fireY = location.y + (Math.random() * 5 - 2)
                        val fireLocation = org.bukkit.Location(location.world, fireX, fireY, fireZ)
                        val block = fireLocation.block
                        
                        if (block.type == Material.AIR && Math.random() < 0.3) {
                            block.type = Material.FIRE
                        } else if (block.type == Material.AIR && block.getRelative(0, -1, 0).type.isSolid && Math.random() < 0.1) {
                            block.type = Material.LAVA
                        }
                    }
                }
                "celestial" -> {
                    // Звездный эффект с сияющими частицами
                    location.world.spawnParticle(Particle.END_ROD, location, 500, radius * 0.6, radius * 0.6, radius * 0.6, 0.05)
                    location.world.spawnParticle(Particle.FIREWORK, location, 300, radius * 0.5, radius * 0.5, radius * 0.5, 0.1)
                    location.world.playSound(location, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 3.0f, 1.5f)
                    
                    // Эффект левитации для игроков
                    nearbyPlayers.filterIsInstance<Player>().forEach { player ->
                        player.addPotionEffect(org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.LEVITATION, 100, 1))
                        player.addPotionEffect(org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.GLOWING, 200, 0))
                        player.addPotionEffect(org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.NIGHT_VISION, 400, 0))
                    }
                    
                    // Создаем эффект звездопада
                    for (i in 0 until 20) {
                        val fireworkLocation = location.clone().add(
                            (Math.random() * radius) - (radius * 0.5),
                            (Math.random() * radius * 0.5),
                            (Math.random() * radius) - (radius * 0.5)
                        )
                        
                        val firework = location.world.spawn(fireworkLocation, org.bukkit.entity.Firework::class.java)
                        val fireworkMeta = firework.fireworkMeta
                        val effect = org.bukkit.FireworkEffect.builder()
                            .withColor(org.bukkit.Color.fromRGB((Math.random() * 255).toInt(), (Math.random() * 255).toInt(), (Math.random() * 255).toInt()))
                            .withFade(org.bukkit.Color.WHITE)
                            .with(org.bukkit.FireworkEffect.Type.STAR)
                            .trail(true)
                            .build()
                        
                        fireworkMeta.addEffect(effect)
                        fireworkMeta.power = 0
                        firework.fireworkMeta = fireworkMeta
                        
                        // Взрываем фейерверк сразу
                        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                            firework.detonate()
                        }, (Math.random() * 10).toLong())
                    }
                }
                "earthquake" -> {
                    // Эффект землетрясения
                    location.world.spawnParticle(Particle.FALLING_DUST, location, 500, radius * 0.7, radius * 0.5, radius * 0.7, 0.1, Material.DIRT.createBlockData())
                    location.world.spawnParticle(Particle.FALLING_DUST, location, 300, radius * 0.6, radius * 0.3, radius * 0.6, 0.1, Material.GRAVEL.createBlockData())
                    location.world.playSound(location, Sound.ENTITY_WITHER_BREAK_BLOCK, 5.0f, 0.5f)
                    
                    // Делаем обвал земли вниз на несколько блоков
                    for (x in -radius/2..radius/2) {
                        for (z in -radius/2..radius/2) {
                            if (x*x + z*z <= (radius/2) * (radius/2) && Math.random() < 0.3) {
                                // Найдем самый высокий блок
                                val highestY = location.world.getHighestBlockYAt(location.blockX + x, location.blockZ + z)
                                val blockLocation = org.bukkit.Location(location.world, location.x + x, highestY.toDouble(), location.z + z)
                                val block = blockLocation.block
                                
                                // Если блок не является жидкостью и не воздухом, двигаем его вниз
                                if (!block.type.isAir && block.type != Material.BEDROCK &&
                                    block.type != Material.WATER && block.type != Material.LAVA) {
                                    // Создаем FallingBlock и делаем блок воздухом
                                    val fallingBlock = location.world.spawnFallingBlock(blockLocation, block.blockData)
                                    fallingBlock.dropItem = false
                                    block.type = Material.AIR
                                    
                                    // Добавляем случайное падение в разломы
                                    if (Math.random() < 0.1) {
                                        val below = block.getRelative(0, -1, 0)
                                        if (!below.type.isAir && below.type != Material.BEDROCK) {
                                            below.type = Material.AIR
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    // Эффект дрожания экрана для игроков
                    nearbyPlayers.filterIsInstance<Player>().forEach { player ->
                        player.playSound(player.location, Sound.ENTITY_WITHER_BREAK_BLOCK, 1.0f, 0.5f)
                        player.addPotionEffect(org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.NAUSEA, 200, 0))
                        
                        // Делаем эффект подбрасывания вверх
                        val direction = player.location.toVector().subtract(location.toVector()).normalize()
                        direction.setY(0.5)
                        player.velocity = direction.multiply(2.0)
                    }
                }
            }
            
            // Отладочное сообщение
            plugin.configManager.debug("TNT типа $tntType взорвался в локации ${location.x}, ${location.y}, ${location.z}")
        }
    }
} 