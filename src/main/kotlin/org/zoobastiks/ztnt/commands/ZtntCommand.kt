package org.zoobastiks.ztnt.commands

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.zoobastiks.ztnt.Ztnt
import java.util.*

class ZtntCommand(private val plugin: Ztnt) : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val configManager = plugin.configManager
        
        // Добавим отладочное сообщение для проверки структуры сообщений
        if (configManager.isDebugEnabled()) {
            configManager.debug("Выполнение команды: /ztnt ${args.joinToString(" ")}")
            configManager.debug("Доступные типы TNT: ${plugin.tntManager.getTNTTypesString()}")
        }
        
        // Проверяем аргументы команды
        if (args.isEmpty()) {
            // Выводим помощь
            val availableTypes = plugin.tntManager.getTNTTypesString()
            val helpMessage = configManager.getMessage("messages.other.help-message", mapOf("types" to availableTypes))
            
            if (sender is Player) {
                configManager.sendMessage(sender, helpMessage)
            } else {
                Bukkit.getConsoleSender().sendMessage(helpMessage)
            }
            return true
        }
        
        when (args[0].lowercase()) {
            "get" -> {
                // Проверяем права
                if (!sender.hasPermission("ztnt.get") && !sender.hasPermission("ztnt.admin")) {
                    val message = configManager.getMessage("messages.error.no-permission")
                    if (sender is Player) {
                        configManager.sendMessage(sender, message)
                    } else {
                        Bukkit.getConsoleSender().sendMessage(message)
                    }
                    return true
                }
                
                // Проверяем, что команда выполняется игроком
                if (sender !is Player) {
                    val message = configManager.getMessage("messages.error.console-cannot-get")
                    Bukkit.getConsoleSender().sendMessage(message)
                    return true
                }
                
                // Проверяем, что указан тип TNT
                if (args.size < 2) {
                    val availableTypes = plugin.tntManager.getTNTTypesString()
                    val message = configManager.getMessage("messages.error.invalid-tnt-type", mapOf("types" to availableTypes))
                    configManager.sendMessage(sender, message)
                    return true
                }
                
                val type = args[1].lowercase()
                
                // Проверяем, что тип TNT существует
                if (!plugin.tntManager.isTNTTypeValid(type)) {
                    val availableTypes = plugin.tntManager.getTNTTypesString()
                    val message = configManager.getMessage("messages.error.invalid-tnt-type", mapOf("types" to availableTypes))
                    configManager.sendMessage(sender, message)
                    return true
                }
                
                // Проверяем, разрешен ли текущий мир для использования TNT
                if (!configManager.isWorldAllowed((sender as Player).world.name)) {
                    val message = configManager.getMessage("messages.error.world-denied")
                    configManager.sendMessage(sender, message)
                    return true
                }
                
                // Получаем количество TNT
                var amount = 1
                if (args.size >= 3) {
                    try {
                        amount = args[2].toInt()
                        if (amount <= 0) {
                            val message = configManager.getMessage("messages.error.invalid-number")
                            configManager.sendMessage(sender, message)
                            return true
                        }
                    } catch (e: NumberFormatException) {
                        val message = configManager.getMessage("messages.error.invalid-number")
                        configManager.sendMessage(sender, message)
                        return true
                    }
                }
                
                // Выдаем TNT игроку
                plugin.tntManager.giveTNTToPlayer(sender, type, amount)
                
                // Отправляем сообщение об успешной выдаче
                val message = configManager.getMessage("messages.success.got-tnt", mapOf(
                    "type" to type,
                    "amount" to amount.toString()
                ))
                configManager.sendMessage(sender, message)
                
                return true
            }
            
            "give" -> {
                // Проверяем права
                if (!sender.hasPermission("ztnt.give") && !sender.hasPermission("ztnt.admin")) {
                    val message = configManager.getMessage("messages.error.no-permission")
                    if (sender is Player) {
                        configManager.sendMessage(sender, message)
                    } else {
                        Bukkit.getConsoleSender().sendMessage(message)
                    }
                    return true
                }
                
                // Проверяем аргументы
                if (args.size < 3) {
                    val message = configManager.getMessage("messages.error.invalid-command")
                    if (sender is Player) {
                        configManager.sendMessage(sender, message)
                    } else {
                        Bukkit.getConsoleSender().sendMessage(message)
                    }
                    return true
                }
                
                // Получаем игрока
                val targetName = args[1]
                val targetPlayer = Bukkit.getPlayer(targetName)
                
                if (targetPlayer == null || !targetPlayer.isOnline) {
                    val message = configManager.getMessage("messages.error.player-not-found")
                    if (sender is Player) {
                        configManager.sendMessage(sender, message)
                    } else {
                        Bukkit.getConsoleSender().sendMessage(message)
                    }
                    return true
                }
                
                val type = args[2].lowercase()
                
                // Проверяем, что тип TNT существует
                if (!plugin.tntManager.isTNTTypeValid(type)) {
                    val availableTypes = plugin.tntManager.getTNTTypesString()
                    val message = configManager.getMessage("messages.error.invalid-tnt-type", mapOf("types" to availableTypes))
                    if (sender is Player) {
                        configManager.sendMessage(sender, message)
                    } else {
                        Bukkit.getConsoleSender().sendMessage(message)
                    }
                    return true
                }
                
                // Проверяем, разрешен ли мир целевого игрока для использования TNT
                if (!configManager.isWorldAllowed(targetPlayer.world.name)) {
                    val message = configManager.getMessage("messages.error.world-denied")
                    if (sender is Player) {
                        configManager.sendMessage(sender, message)
                    } else {
                        Bukkit.getConsoleSender().sendMessage(message)
                    }
                    return true
                }
                
                // Получаем количество TNT
                var amount = 1
                if (args.size >= 4) {
                    try {
                        amount = args[3].toInt()
                        if (amount <= 0) {
                            val message = configManager.getMessage("messages.error.invalid-number")
                            if (sender is Player) {
                                configManager.sendMessage(sender, message)
                            } else {
                                Bukkit.getConsoleSender().sendMessage(message)
                            }
                            return true
                        }
                    } catch (e: NumberFormatException) {
                        val message = configManager.getMessage("messages.error.invalid-number")
                        if (sender is Player) {
                            configManager.sendMessage(sender, message)
                        } else {
                            Bukkit.getConsoleSender().sendMessage(message)
                        }
                        return true
                    }
                }
                
                // Выдаем TNT игроку
                plugin.tntManager.giveTNTToPlayer(targetPlayer, type, amount)
                
                // Отправляем сообщение об успешной выдаче
                val message = configManager.getMessage("messages.success.gave-tnt", mapOf(
                    "player" to targetPlayer.name,
                    "type" to type,
                    "amount" to amount.toString()
                ))
                if (sender is Player) {
                    configManager.sendMessage(sender, message)
                } else {
                    Bukkit.getConsoleSender().sendMessage(message)
                }
                
                return true
            }
            
            "reload" -> {
                // Проверяем права
                if (!sender.hasPermission("ztnt.reload") && !sender.hasPermission("ztnt.admin")) {
                    val message = configManager.getMessage("messages.error.no-permission")
                    if (sender is Player) {
                        configManager.sendMessage(sender, message)
                    } else {
                        Bukkit.getConsoleSender().sendMessage(message)
                    }
                    return true
                }
                
                // Перезагружаем конфигурацию
                plugin.configManager.reloadConfig()
                
                // Отправляем сообщение об успешной перезагрузке
                val message = configManager.getMessage("messages.success.reload")
                if (sender is Player) {
                    configManager.sendMessage(sender, message)
                } else {
                    Bukkit.getConsoleSender().sendMessage(message)
                }
                
                return true
            }
            
            else -> {
                // Неизвестная подкоманда
                val message = configManager.getMessage("messages.error.invalid-command")
                if (sender is Player) {
                    configManager.sendMessage(sender, message)
                } else {
                    Bukkit.getConsoleSender().sendMessage(message)
                }
                return true
            }
        }
    }
    
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): List<String>? {
        val completions = ArrayList<String>()
        
        // Список подкоманд
        if (args.size == 1) {
            // Добавляем доступные подкоманды в зависимости от прав
            if (sender.hasPermission("ztnt.get") || sender.hasPermission("ztnt.admin")) {
                completions.add("get")
            }
            if (sender.hasPermission("ztnt.give") || sender.hasPermission("ztnt.admin")) {
                completions.add("give")
            }
            if (sender.hasPermission("ztnt.reload") || sender.hasPermission("ztnt.admin")) {
                completions.add("reload")
            }
            
            return filterCompletions(completions, args[0])
        }
        
        // Аргументы для подкоманд
        if (args.size == 2) {
            when (args[0].lowercase()) {
                "get" -> {
                    if (sender.hasPermission("ztnt.get") || sender.hasPermission("ztnt.admin")) {
                        // Добавляем типы TNT
                        completions.addAll(plugin.tntManager.getAllTNTTypes())
                    }
                }
                "give" -> {
                    if (sender.hasPermission("ztnt.give") || sender.hasPermission("ztnt.admin")) {
                        // Добавляем имена онлайн игроков
                        Bukkit.getOnlinePlayers().forEach { completions.add(it.name) }
                    }
                }
            }
            
            return filterCompletions(completions, args[1])
        }
        
        // Аргументы для give <player> <type>
        if (args.size == 3 && args[0].equals("give", ignoreCase = true)) {
            if (sender.hasPermission("ztnt.give") || sender.hasPermission("ztnt.admin")) {
                // Добавляем типы TNT
                completions.addAll(plugin.tntManager.getAllTNTTypes())
            }
            
            return filterCompletions(completions, args[2])
        }
        
        // Аргументы для количества
        if (args.size == 3 && args[0].equals("get", ignoreCase = true)) {
            // Предлагаем стандартные количества
            completions.addAll(listOf("1", "5", "10", "64"))
            return filterCompletions(completions, args[2])
        }
        
        // Аргументы для количества при give
        if (args.size == 4 && args[0].equals("give", ignoreCase = true)) {
            // Предлагаем стандартные количества
            completions.addAll(listOf("1", "5", "10", "64"))
            return filterCompletions(completions, args[3])
        }
        
        return emptyList()
    }
    
    /**
     * Фильтрует список автодополнений по начальным символам
     * @param completions Список возможных дополнений
     * @param current Текущий ввод пользователя
     * @return Отфильтрованный список дополнений
     */
    private fun filterCompletions(completions: List<String>, current: String): List<String> {
        return completions.filter { it.lowercase().startsWith(current.lowercase()) }
    }
} 