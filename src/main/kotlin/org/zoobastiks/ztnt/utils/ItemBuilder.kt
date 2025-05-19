package org.zoobastiks.ztnt.utils

import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

/**
 * Утилитарный класс для создания предметов с помощью паттерна Builder
 */
class ItemBuilder(private val material: Material) {
    private val item: ItemStack = ItemStack(material)
    private val meta = item.itemMeta!!
    
    /**
     * Устанавливает имя предмета
     * @param name Имя предмета
     * @return this для цепочки вызовов
     */
    fun setName(name: String): ItemBuilder {
        // Конвертируем MiniMessage в Legacy для правильного отображения цветов в предметах
        val legacyName = miniMessageToLegacy(name)
        meta.setDisplayName(legacyName)
        return this
    }
    
    /**
     * Устанавливает описание предмета
     * @param lore Список строк описания
     * @return this для цепочки вызовов
     */
    fun setLore(lore: List<String>): ItemBuilder {
        // Конвертируем каждую строку из MiniMessage в Legacy
        val legacyLore = lore.map { miniMessageToLegacy(it) }
        meta.lore = legacyLore
        return this
    }
    
    /**
     * Устанавливает количество предметов в стаке
     * @param amount Количество (от 1 до 64)
     * @return this для цепочки вызовов
     */
    fun setAmount(amount: Int): ItemBuilder {
        item.amount = amount.coerceIn(1, 64)
        return this
    }
    
    /**
     * Добавляет зачарование к предмету
     * @param enchantment Тип зачарования
     * @param level Уровень зачарования
     * @param ignoreLevelRestriction Игнорировать ли ограничения уровня
     * @return this для цепочки вызовов
     */
    fun addEnchant(enchantment: Enchantment, level: Int, ignoreLevelRestriction: Boolean = false): ItemBuilder {
        meta.addEnchant(enchantment, level, ignoreLevelRestriction)
        return this
    }
    
    /**
     * Добавляет флаг к предмету
     * @param flag Флаг предмета
     * @return this для цепочки вызовов
     */
    fun addItemFlag(flag: ItemFlag): ItemBuilder {
        meta.addItemFlags(flag)
        return this
    }
    
    /**
     * Устанавливает значение custom model data
     * @param modelData Значение custom model data
     * @return this для цепочки вызовов
     */
    fun setCustomModelData(modelData: Int): ItemBuilder {
        if (modelData > 0) {
            meta.setCustomModelData(modelData)
        }
        return this
    }
    
    /**
     * Устанавливает предмет как неразрушаемый
     * @param unbreakable Значение флага неразрушаемости
     * @return this для цепочки вызовов
     */
    fun setUnbreakable(unbreakable: Boolean): ItemBuilder {
        meta.isUnbreakable = unbreakable
        return this
    }
    
    /**
     * Устанавливает данные в PersistentDataContainer
     * @param key Ключ
     * @param type Тип данных
     * @param value Значение
     * @return this для цепочки вызовов
     */
    fun <T, Z : Any> setPersistentData(key: NamespacedKey, type: PersistentDataType<T, Z>, value: Z): ItemBuilder {
        meta.persistentDataContainer.set(key, type, value)
        return this
    }
    
    /**
     * Создает ItemStack с установленными свойствами
     * @return ItemStack с установленными свойствами
     */
    fun build(): ItemStack {
        item.itemMeta = meta
        return item.clone()
    }
    
    /**
     * Конвертирует MiniMessage формат в Legacy формат с § кодами
     * @param text Текст в формате MiniMessage
     * @return Текст в Legacy формате с § кодами
     */
    private fun miniMessageToLegacy(text: String): String {
        try {
            val miniMessage = MiniMessage.miniMessage()
            val component = miniMessage.deserialize(text)
            return LegacyComponentSerializer.legacySection().serialize(component)
        } catch (e: Exception) {
            // В случае ошибки, возвращаем исходный текст
            return text
        }
    }
} 