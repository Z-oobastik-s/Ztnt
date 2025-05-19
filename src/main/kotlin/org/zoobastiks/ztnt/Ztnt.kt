package org.zoobastiks.ztnt

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.zoobastiks.ztnt.commands.ZtntCommand
import org.zoobastiks.ztnt.config.ConfigManager
import org.zoobastiks.ztnt.listeners.TNTListener
import org.zoobastiks.ztnt.managers.TNTManager

class Ztnt : JavaPlugin() {

    companion object {
        lateinit var instance: Ztnt
            private set
    }

    lateinit var configManager: ConfigManager
        private set
    
    lateinit var tntManager: TNTManager
        private set

    override fun onEnable() {
        instance = this
        
        // Инициализация менеджеров
        configManager = ConfigManager(this)
        tntManager = TNTManager(this)
        
        // Регистрация команд
        getCommand("ztnt")?.setExecutor(ZtntCommand(this))
        
        // Регистрация листенеров
        server.pluginManager.registerEvents(TNTListener(this), this)
        
        // Вывод информации о плагине
        printPluginInfo()
        
        // Отправляем сообщение о включении плагина
        Bukkit.getConsoleSender().sendMessage("§a" + configManager.getMessage("messages.other.plugin-enabled"))
    }

    override fun onDisable() {
        // Отправляем сообщение о выключении плагина
        Bukkit.getConsoleSender().sendMessage("§c" + configManager.getMessage("messages.other.plugin-disabled"))
    }

    /**
     * Выводит красивую информацию о плагине в консоль
     */
    private fun printPluginInfo() {
        val lines = arrayOf(
            "§6╔══════════════════════════════════════════════╗",
            "§6║                                              ║",
            "§6║  §bZtnt §fv${description.version}§c - Custom TNT Plugin               §6║",
            "§6║  §fAuthor: §aZoobastiks                          §6║",
            "§6║  §fSupport: §9https://t.me/Zoobastiks            §6║",
            "§6║                                              ║",
            "§6╚══════════════════════════════════════════════╝"
        )
        
        // Выводим каждую строку в консоль через ConsoleCommandSender
        lines.forEach { Bukkit.getConsoleSender().sendMessage(it) }
    }
} 