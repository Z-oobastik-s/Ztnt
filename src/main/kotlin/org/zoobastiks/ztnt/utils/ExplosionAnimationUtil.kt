package org.zoobastiks.ztnt.utils

import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.scheduler.BukkitRunnable
import org.zoobastiks.ztnt.Ztnt

/**
 * Утилитарный класс для работы с анимациями взрывов
 */
object ExplosionAnimationUtil {

    /**
     * Воспроизводит анимацию отсчета перед взрывом
     * @param location Местоположение
     * @param animationType Тип анимации
     */
    fun playCountdownAnimation(location: Location, animationType: String) {
        when (animationType.uppercase()) {
            "FIRE" -> {
                // Огненная анимация
                location.world.spawnParticle(Particle.FLAME, location, 20, 0.5, 0.5, 0.5, 0.1)
                location.world.spawnParticle(Particle.LAVA, location, 5, 0.5, 0.5, 0.5, 0.1)
                location.world.playSound(location, Sound.BLOCK_FIRE_AMBIENT, 1.0f, 1.0f)
            }
            "SAFE" -> {
                // Безопасная анимация
                location.world.spawnParticle(Particle.END_ROD, location, 20, 0.5, 0.5, 0.5, 0.1)
                location.world.spawnParticle(Particle.CRIT, location, 30, 0.5, 0.5, 0.5, 1.0)
                location.world.playSound(location, Sound.BLOCK_BEACON_AMBIENT, 1.0f, 1.0f)
            }
            else -> {
                // Стандартная анимация
                location.world.spawnParticle(Particle.CLOUD, location, 20, 0.5, 0.5, 0.5, 0.1)
                location.world.spawnParticle(Particle.FLAME, location, 10, 0.5, 0.5, 0.5, 0.1)
                location.world.playSound(location, Sound.ENTITY_TNT_PRIMED, 1.0f, 1.0f)
            }
        }
    }
    
    /**
     * Воспроизводит анимацию взрыва
     * @param location Местоположение
     * @param animationType Тип анимации
     */
    fun playExplosionAnimation(location: Location, animationType: String) {
        when (animationType.uppercase()) {
            "FIRE" -> {
                // Огненная анимация взрыва
                playFireExplosionAnimation(location)
            }
            "SAFE" -> {
                // Безопасная анимация взрыва
                playSafeExplosionAnimation(location)
            }
            else -> {
                // Стандартная анимация взрыва
                playDefaultExplosionAnimation(location)
            }
        }
    }
    
    /**
     * Воспроизводит стандартную анимацию взрыва
     * @param location Местоположение
     */
    private fun playDefaultExplosionAnimation(location: Location) {
        // Звуки взрыва
        location.world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 4.0f, 1.0f)
        
        // Частицы взрыва
        location.world.spawnParticle(Particle.EXPLOSION, location, 1, 0.0, 0.0, 0.0, 0.0)
        location.world.spawnParticle(Particle.EXPLOSION, location, 10, 3.0, 3.0, 3.0, 0.0)
        location.world.spawnParticle(Particle.CLOUD, location, 40, 4.0, 4.0, 4.0, 0.1)
    }
    
    /**
     * Воспроизводит огненную анимацию взрыва
     * @param location Местоположение
     */
    private fun playFireExplosionAnimation(location: Location) {
        // Звуки взрыва
        location.world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 4.0f, 0.8f)
        location.world.playSound(location, Sound.ENTITY_BLAZE_SHOOT, 2.0f, 0.5f)
        
        // Частицы взрыва
        location.world.spawnParticle(Particle.EXPLOSION, location, 1, 0.0, 0.0, 0.0, 0.0)
        location.world.spawnParticle(Particle.EXPLOSION, location, 15, 3.0, 3.0, 3.0, 0.0)
        location.world.spawnParticle(Particle.FLAME, location, 80, 5.0, 5.0, 5.0, 0.2)
        location.world.spawnParticle(Particle.LAVA, location, 40, 5.0, 5.0, 5.0, 0.1)
        
        // Создаем долгоиграющую анимацию огня
        object : BukkitRunnable() {
            var ticks = 0
            
            override fun run() {
                if (ticks >= 20) {
                    cancel()
                    return
                }
                
                // Создаем эффект огненных столбов
                for (i in 0 until 5) {
                    val offsetX = (Math.random() * 10.0) - 5.0
                    val offsetZ = (Math.random() * 10.0) - 5.0
                    val flameLoc = location.clone().add(offsetX, 0.0, offsetZ)
                    
                    // Огненный столб
                    for (y in 0 until 8) {
                        flameLoc.add(0.0, 1.0, 0.0)
                        location.world.spawnParticle(Particle.FLAME, flameLoc, 3, 0.2, 0.2, 0.2, 0.05)
                    }
                }
                
                ticks++
            }
        }.runTaskTimer(Ztnt.instance, 2L, 2L)
    }
    
    /**
     * Воспроизводит безопасную анимацию взрыва
     * @param location Местоположение
     */
    private fun playSafeExplosionAnimation(location: Location) {
        // Звуки взрыва
        location.world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 3.0f, 1.5f)
        location.world.playSound(location, Sound.ENTITY_ILLUSIONER_PREPARE_MIRROR, 2.0f, 1.0f)
        
        // Частицы взрыва
        location.world.spawnParticle(Particle.EXPLOSION, location, 5, 1.0, 1.0, 1.0, 0.0)
        location.world.spawnParticle(Particle.END_ROD, location, 100, 5.0, 5.0, 5.0, 0.5)
        location.world.spawnParticle(Particle.CRIT, location, 80, 5.0, 5.0, 5.0, 0.1)
        
        // Создаем защитную сферу
        object : BukkitRunnable() {
            var radius = 1.0
            val maxRadius = 20.0
            
            override fun run() {
                if (radius >= maxRadius) {
                    cancel()
                    return
                }
                
                // Рисуем сферу
                for (i in 0 until 20) {
                    val phi = Math.PI * 2 * Math.random()
                    val theta = Math.PI * Math.random()
                    
                    val x = radius * Math.sin(theta) * Math.cos(phi)
                    val y = radius * Math.sin(theta) * Math.sin(phi)
                    val z = radius * Math.cos(theta)
                    
                    location.world.spawnParticle(Particle.END_ROD, location.clone().add(x, y, z), 1, 0.0, 0.0, 0.0, 0.0)
                }
                
                // Звук расширения сферы
                if (radius % 4 == 0.0) {
                    location.world.playSound(location, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.0f + (radius / maxRadius).toFloat())
                }
                
                radius += 1.0
            }
        }.runTaskTimer(Ztnt.instance, 0L, 1L)
    }
} 