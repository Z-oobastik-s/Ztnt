package org.zoobastiks.ztnt.managers

import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.entity.TNTPrimed
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.zoobastiks.ztnt.Ztnt
import org.zoobastiks.ztnt.utils.ItemBuilder

class TNTManager(private val plugin: Ztnt) {

    // Ключи для хранения метаданных в TNT
    private val tntTypeKey = NamespacedKey(plugin, "tnt_type")
    
    /**
     * Создает предмет TNT указанного типа
     * @param type Тип TNT
     * @param amount Количество
     * @return ItemStack с TNT
     */
    fun createTNTItem(type: String, amount: Int = 1): ItemStack {
        val configManager = plugin.configManager
        
        // Получаем данные из конфигурации
        val name = configManager.getTntName(type)
        val lore = configManager.getTntLore(type)
        val customModelData = configManager.getTntCustomModelData(type)
        
        // Создаем ItemStack с помощью ItemBuilder
        return ItemBuilder(Material.TNT)
            .setName(name)
            .setLore(lore)
            .setCustomModelData(customModelData)
            .setPersistentData(tntTypeKey, PersistentDataType.STRING, type)
            .setAmount(amount)
            .build()
    }
    
    /**
     * Выдает TNT игроку
     * @param player Игрок
     * @param type Тип TNT
     * @param amount Количество
     * @return true, если TNT успешно выдан
     */
    fun giveTNTToPlayer(player: Player, type: String, amount: Int = 1): Boolean {
        // Проверяем, существует ли тип TNT
        if (!isTNTTypeValid(type)) {
            return false
        }
        
        // Создаем TNT и добавляем его в инвентарь игрока
        val tntItem = createTNTItem(type, amount)
        player.inventory.addItem(tntItem)
        
        return true
    }
    
    /**
     * Проверяет, является ли предмет кастомным TNT
     * @param item Предмет для проверки
     * @return true, если предмет - кастомный TNT
     */
    fun isCustomTNT(item: ItemStack?): Boolean {
        if (item == null || item.type != Material.TNT) {
            return false
        }
        
        val meta = item.itemMeta ?: return false
        return meta.persistentDataContainer.has(tntTypeKey, PersistentDataType.STRING)
    }
    
    /**
     * Получает тип TNT из предмета
     * @param item Предмет TNT
     * @return Тип TNT или null, если предмет не является кастомным TNT
     */
    fun getTNTType(item: ItemStack): String? {
        if (!isCustomTNT(item)) {
            return null
        }
        
        val meta = item.itemMeta ?: return null
        return meta.persistentDataContainer.get(tntTypeKey, PersistentDataType.STRING)
    }
    
    /**
     * Устанавливает тип TNT для активированного TNT
     * @param entity TNT сущность
     * @param type Тип TNT
     */
    fun setTNTEntityType(entity: TNTPrimed, type: String) {
        entity.persistentDataContainer.set(tntTypeKey, PersistentDataType.STRING, type)
    }
    
    /**
     * Получает тип TNT из сущности TNT
     * @param entity TNT сущность
     * @return Тип TNT или null, если сущность не является кастомным TNT
     */
    fun getTNTEntityType(entity: TNTPrimed): String? {
        return if (entity.persistentDataContainer.has(tntTypeKey, PersistentDataType.STRING)) {
            entity.persistentDataContainer.get(tntTypeKey, PersistentDataType.STRING)
        } else {
            null
        }
    }
    
    /**
     * Проверяет, существует ли тип TNT в конфигурации
     * @param type Тип TNT для проверки
     * @return true, если тип TNT существует
     */
    fun isTNTTypeValid(type: String): Boolean {
        return plugin.configManager.getTntTypes().contains(type)
    }
    
    /**
     * Получает список всех доступных типов TNT
     * @return Список типов TNT
     */
    fun getAllTNTTypes(): Set<String> {
        return plugin.configManager.getTntTypes()
    }
    
    /**
     * Возвращает строку списка типов TNT для использования в сообщениях
     * @return Форматированная строка со списком типов TNT
     */
    fun getTNTTypesString(): String {
        return getAllTNTTypes().joinToString(", ")
    }
} 