package org.zoobastiks.ztnt.config

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.zoobastiks.ztnt.Ztnt
import java.util.regex.Pattern

class ConfigManager(private val plugin: Ztnt) {

    private var config: FileConfiguration = plugin.config
    
    init {
        // Сохраняем конфигурацию по умолчанию, если её нет
        plugin.saveDefaultConfig()
        reloadConfig()
    }
    
    /**
     * Перезагружает конфигурацию из файла
     */
    fun reloadConfig() {
        plugin.reloadConfig()
        config = plugin.config
    }
    
    /**
     * Получает сообщение из конфигурации и заменяет плейсхолдеры
     * @param path Путь к сообщению
     * @param placeholders Плейсхолдеры для замены (ключ -> значение)
     * @return Отформатированное сообщение
     */
    fun getMessage(path: String, placeholders: Map<String, String> = emptyMap()): String {
        var message = config.getString(path, "<red>Сообщение не найдено: $path") ?: "<red>Сообщение не найдено: $path"
        
        // Заменяем плейсхолдеры
        for ((key, value) in placeholders) {
            message = message.replace("{$key}", value)
        }
        
        // Конвертируем MiniMessage в Legacy для консоли
        return miniMessageToLegacy(message)
    }
    
    /**
     * Отправляет сообщение в консоль с префиксом из конфига
     * @param message Сообщение для отправки
     */
    fun sendConsoleMessage(message: String) {
        val prefix = config.getString("settings.console-prefix") ?: "<gray>[<red>Ztnt<gray>] "
        val fullMessage = miniMessageToLegacy(prefix + message)
        Bukkit.getConsoleSender().sendMessage(fullMessage)
    }
    
    /**
     * Отправляет сообщение игроку с префиксом из конфига
     * @param sender Получатель сообщения
     * @param message Сообщение для отправки
     */
    fun sendMessage(sender: CommandSender, message: String) {
        if (message.isEmpty()) return
        
        if (sender is Player) {
            try {
                // Берем префикс из конфига и используем для игрока
                val prefix = config.getString("settings.chat-prefix") ?: "<gray>[<red><bold>Ztnt</bold><gray>] "
                
                // Используем MiniMessage для преобразования префикса и сообщения
                val miniMessage = MiniMessage.miniMessage()
                
                // Создаем компоненты по отдельности и объединяем их
                val prefixComponent = miniMessage.deserialize(prefix)
                val messageComponent = miniMessage.deserialize(message)
                
                // Отправляем объединенный компонент
                sender.sendMessage(prefixComponent.append(messageComponent))
            } catch (e: Exception) {
                // В случае ошибки при парсинге, отправляем как обычный текст с § кодами
                val legacyText = miniMessageToLegacy(config.getString("settings.chat-prefix") ?: "§7[§c§lZtnt§7] ") + miniMessageToLegacy(message)
                sender.sendMessage(legacyText)
            }
        } else {
            // Для консоли используем § коды
            val prefix = config.getString("settings.console-prefix") ?: "<gray>[<red>Ztnt<gray>] "
            sender.sendMessage(miniMessageToLegacy(prefix + message))
        }
    }
    
    /**
     * Конвертирует MiniMessage формат в Legacy формат с § кодами
     */
    private fun miniMessageToLegacy(text: String): String {
        try {
            val miniMessage = MiniMessage.miniMessage()
            val component = miniMessage.deserialize(text)
            return LegacyComponentSerializer.legacySection().serialize(component)
        } catch (e: Exception) {
            // Если возникла ошибка, возвращаем оригинальный текст
            return text
        }
    }
    
    /**
     * Заменяет & на § в тексте
     */
    private fun ampersandToSection(text: String): String {
        return ChatColor.translateAlternateColorCodes('&', text)
    }
    
    /**
     * Конвертирует & коды в эквиваленты MiniMessage
     */
    private fun ampersandToMiniMessage(text: String): String {
        val result = StringBuilder()
        var i = 0
        while (i < text.length) {
            if (i < text.length - 1 && text[i] == '&') {
                val code = text[i + 1]
                when (code) {
                    '0' -> result.append("<black>")
                    '1' -> result.append("<dark_blue>")
                    '2' -> result.append("<dark_green>")
                    '3' -> result.append("<dark_aqua>")
                    '4' -> result.append("<dark_red>")
                    '5' -> result.append("<dark_purple>")
                    '6' -> result.append("<gold>")
                    '7' -> result.append("<gray>")
                    '8' -> result.append("<dark_gray>")
                    '9' -> result.append("<blue>")
                    'a' -> result.append("<green>")
                    'b' -> result.append("<aqua>")
                    'c' -> result.append("<red>")
                    'd' -> result.append("<light_purple>")
                    'e' -> result.append("<yellow>")
                    'f' -> result.append("<white>")
                    'k' -> result.append("<obfuscated>")
                    'l' -> result.append("<bold>")
                    'm' -> result.append("<strikethrough>")
                    'n' -> result.append("<underlined>")
                    'o' -> result.append("<italic>")
                    'r' -> result.append("<reset>")
                    else -> result.append("&$code")
                }
                i += 2
            } else {
                result.append(text[i])
                i++
            }
        }
        return result.toString()
    }
    
    /**
     * Получение списка типов TNT
     * @return Список типов TNT
     */
    fun getTntTypes(): Set<String> {
        return config.getConfigurationSection("tnt-types")?.getKeys(false) ?: emptySet()
    }
    
    /**
     * Получает радиус взрыва для указанного типа TNT
     * @param type Тип TNT
     * @return Радиус взрыва
     */
    fun getExplosionRadius(type: String): Int {
        return config.getInt("tnt-types.$type.explosion.radius", 20)
    }
    
    /**
     * Получает урон от взрыва для указанного типа TNT
     * @param type Тип TNT
     * @return Урон от взрыва
     */
    fun getExplosionDamage(type: String): Int {
        return config.getInt("tnt-types.$type.explosion.damage", 100)
    }
    
    /**
     * Проверяет, создает ли взрыв огонь
     * @param type Тип TNT
     * @return true, если взрыв создает огонь
     */
    fun isExplosionFire(type: String): Boolean {
        return config.getBoolean("tnt-types.$type.explosion.fire", false)
    }
    
    /**
     * Проверяет, разрушает ли взрыв блоки
     * @param type Тип TNT
     * @return true, если взрыв разрушает блоки
     */
    fun isExplosionBreakBlocks(type: String): Boolean {
        return config.getBoolean("tnt-types.$type.explosion.block-break", true)
    }
    
    /**
     * Получает тип анимации для указанного типа TNT
     * @param type Тип TNT
     * @return Название анимации
     */
    fun getExplosionAnimation(type: String): String {
        return config.getString("tnt-types.$type.explosion.animation", "DEFAULT") ?: "DEFAULT"
    }
    
    /**
     * Получает название для указанного типа TNT
     * @param type Тип TNT
     * @return Название TNT
     */
    fun getTntName(type: String): String {
        val name = config.getString("tnt-types.$type.name", "Unknown TNT") ?: "Unknown TNT"
        // Теперь это просто возвращаем как есть, ItemBuilder сам обработает MiniMessage
        return name
    }
    
    /**
     * Получает описание для указанного типа TNT
     * @param type Тип TNT
     * @return Список строк описания
     */
    fun getTntLore(type: String): List<String> {
        // Теперь это просто возвращаем как есть, ItemBuilder сам обработает MiniMessage
        return config.getStringList("tnt-types.$type.lore")
    }
    
    /**
     * Получает custom model data для указанного типа TNT
     * @param type Тип TNT
     * @return Custom model data
     */
    fun getTntCustomModelData(type: String): Int {
        return config.getInt("tnt-types.$type.custom-model-data", 0)
    }
    
    /**
     * Проверяет, включен ли отладочный режим
     * @return true, если отладочный режим включен
     */
    fun isDebugEnabled(): Boolean {
        return config.getBoolean("settings.debug", false)
    }
    
    /**
     * Выводит отладочное сообщение, если отладочный режим включен
     * @param message Сообщение для вывода
     */
    fun debug(message: String) {
        if (isDebugEnabled()) {
            sendConsoleMessage("<gray>[DEBUG] <white>" + message)
        }
    }
    
    /**
     * Проверяет, разрешено ли использование TNT в указанном мире
     * @param worldName Название мира для проверки
     * @return true, если использование TNT разрешено в этом мире
     */
    fun isWorldAllowed(worldName: String): Boolean {
        // Проверяем запрещенные миры (имеют приоритет)
        val deniedWorlds = config.getStringList("settings.worlds.denied")
        if (deniedWorlds.contains(worldName)) {
            return false
        }
        
        // Проверяем разрешенные миры
        val allowedWorlds = config.getStringList("settings.worlds.allowed")
        
        // Если список разрешенных миров пуст, разрешаем все миры, которые не запрещены
        if (allowedWorlds.isEmpty()) {
            return true
        }
        
        // Иначе проверяем, есть ли мир в списке разрешенных
        return allowedWorlds.contains(worldName)
    }
} 