# ZTNT - Advanced TNT Plugin for Minecraft 1.21.4

## Overview

ZTNT is a premium Minecraft plugin that enhances the TNT experience with customizable explosion types, powerful effects, and an intuitive command system. Create spectacular explosions with various TNT types ranging from nuclear blasts to quantum distortions, all configurable to your server's needs.

Tested on Minecraft 1.21.4, this plugin offers server administrators complete control over explosion mechanics while providing players with exciting and unique TNT variations.

## Features

- **Multiple TNT Types**: 8 different pre-configured TNT types with unique effects
- **Custom Explosions**: Configure blast radius, damage, block breaking behavior and more
- **Special Effects**: Each TNT type includes particle effects, sounds, and player status effects
- **Permission System**: Full permission control integrated with PermissionEx
- **Command System**: Intuitive commands with tab completion
- **Fully Configurable**: Every aspect can be customized through config.yml
- **Message Customization**: All messages support MiniMessage format for rich text customization

## TNT Types

### Standard TNT
- **Description**: Enhanced version of vanilla TNT with configurable properties
- **Commands**: `/ztnt get standard [amount]`, `/ztnt give <player> standard [amount]`
- **Permission**: `ztnt.use.standard`
- **Properties**: Medium blast radius, medium damage, breaks blocks

### Nuclear TNT (☢ Ядерное TNT ☢)
- **Description**: Massive explosion with radiation effects
- **Commands**: `/ztnt get nuke [amount]`, `/ztnt give <player> nuke [amount]`
- **Permission**: `ztnt.use.nuke`
- **Properties**: Very large blast radius (40 blocks), high damage (200), radiation effects, lightning strikes

### Lightning TNT (⚡ Молниеносное TNT ⚡)
- **Description**: Summons multiple lightning strikes around explosion area
- **Commands**: `/ztnt get lightning [amount]`, `/ztnt give <player> lightning [amount]`
- **Permission**: `ztnt.use.lightning`
- **Properties**: Large blast radius (25 blocks), high damage (150), multiple lightning strikes, electric effects

### Black Hole TNT (⚫ Черная Дыра TNT ⚫)
- **Description**: Creates gravitational effects pulling entities toward center
- **Commands**: `/ztnt get black_hole [amount]`, `/ztnt give <player> black_hole [amount]`
- **Permission**: `ztnt.use.black_hole`
- **Properties**: Medium blast radius (30 blocks), medium damage (120), gravitational pull effect

### Frost TNT
- **Description**: Freezes the surrounding area and players
- **Commands**: `/ztnt get frost [amount]`, `/ztnt give <player> frost [amount]`
- **Permission**: `ztnt.use.frost`
- **Properties**: Medium blast radius, freezes water into ice, slowness effect on players

### Quantum TNT
- **Description**: Creates space-time distortions, randomly teleporting entities
- **Commands**: `/ztnt get quantum [amount]`, `/ztnt give <player> quantum [amount]`
- **Permission**: `ztnt.use.quantum`
- **Properties**: Medium blast radius, teleportation effects, disorientation

### Inferno TNT
- **Description**: Creates pools of fire and lava in the explosion area
- **Commands**: `/ztnt get inferno [amount]`, `/ztnt give <player> inferno [amount]`
- **Permission**: `ztnt.use.inferno`
- **Properties**: Large blast radius, fire creation, burning effects on players

### Celestial TNT
- **Description**: Creates spectacular fireworks and star effects
- **Commands**: `/ztnt get celestial [amount]`, `/ztnt give <player> celestial [amount]`
- **Permission**: `ztnt.use.celestial`
- **Properties**: Medium blast radius, levitation effects, visual fireworks, night vision

### Earthquake TNT
- **Description**: Causes the ground to collapse and shake, creating terrain deformation
- **Commands**: `/ztnt get earthquake [amount]`, `/ztnt give <player> earthquake [amount]`
- **Permission**: `ztnt.use.earthquake`
- **Properties**: Large blast radius, creates falling blocks, nausea effect on players

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/ztnt get <type> [amount]` | Get TNT of specified type | `ztnt.command.get`, `ztnt.use.<type>` |
| `/ztnt give <player> <type> [amount]` | Give TNT to a player | `ztnt.command.give`, `ztnt.use.<type>` |
| `/ztnt reload` | Reload the plugin configuration | `ztnt.command.reload` |
| `/ztnt help` | Display help information | `ztnt.command.help` |
| `/ztnt list` | List all available TNT types | `ztnt.command.list` |

## Permissions

| Permission | Description |
|------------|-------------|
| `ztnt.command.get` | Allows use of `/ztnt get` command |
| `ztnt.command.give` | Allows use of `/ztnt give` command |
| `ztnt.command.reload` | Allows use of `/ztnt reload` command |
| `ztnt.command.help` | Allows use of `/ztnt help` command |
| `ztnt.command.list` | Allows use of `/ztnt list` command |
| `ztnt.use.*` | Allows use of all TNT types |
| `ztnt.use.<type>` | Allows use of specific TNT type (replace `<type>` with TNT type name) |
| `ztnt.admin` | Grants all permissions |

## Configuration

The configuration file (`config.yml`) allows you to customize all aspects of the plugin:

### General Settings

```yaml
# General plugin settings
settings:
  debug: false                 # Enable debug mode for troubleshooting
  prefix: "<gray>[<gradient:#FF0000:#FFFF00>ZTNT</gradient>]</gray>"  # Chat prefix for plugin messages
```

### Message Configuration

```yaml
# All plugin messages
messages:
  prefix: "<gray>[<gradient:#FF0000:#FFFF00>ZTNT</gradient>]</gray>"  # Chat prefix
  
  commands:
    no-permission: "<red>You don't have permission to use this command!</red>"
    player-not-found: "<red>Player not found!</red>"
    reload-success: "<green>Plugin configuration reloaded successfully!</green>"
    invalid-amount: "<red>Invalid amount. Please specify a positive number.</red>"
    invalid-type: "<red>Invalid TNT type. Use /ztnt list to see available types.</red>"
    give-success: "<green>Given {amount} {type} TNT to {player}!</green>"
    get-success: "<green>You received {amount} {type} TNT!</green>"
    
  other:
    help-message: |
      <yellow>==== ZTNT Help ====</yellow>
      <green>/ztnt get <type> [amount]</green> - Get TNT of specified type
      <green>/ztnt give <player> <type> [amount]</green> - Give TNT to a player
      <green>/ztnt reload</green> - Reload the plugin configuration
      <green>/ztnt help</green> - Display this help message
      <green>/ztnt list</green> - List all available TNT types
    list-message: "<yellow>Available TNT types:</yellow> {types}"
```

### TNT Types Configuration

Each TNT type can be fully customized:

```yaml
# TNT types configuration
tnt-types:
  standard:
    name: "<white>Standard TNT</white>"
    lore:
      - "<gray>A standard but enhanced TNT</gray>"
      - "<red>Blast Radius:</red> <yellow>10 blocks</yellow>"
      - "<red>Damage:</red> <yellow>50</yellow>"
    material: TNT
    custom-model-data: 1001
    explosion:
      radius: 10
      damage: 50
      fire: false
      block-break: true
    animation: STANDARD
    
  nuke:
    name: "<red>☢ Nuclear TNT ☢</red>"
    lore:
      - "<gray>\"Now I am become Death, the destroyer of worlds.\"</gray>"
      - "<gray>- J. Robert Oppenheimer</gray>"
      - ""
      - "<red>Blast Radius:</red> <yellow>40 blocks</yellow>"
      - "<red>Damage:</red> <yellow>200</yellow>"
    material: TNT
    custom-model-data: 1002
    explosion:
      radius: 40
      damage: 200
      fire: true
      block-break: true
    animation: NUKE
    
  # ... other TNT types configurations ...
```

Each TNT type configuration includes:
- `name`: Display name with MiniMessage format support
- `lore`: Item description with MiniMessage format support
- `material`: Minecraft material type
- `custom-model-data`: For custom resource packs
- `explosion`: Configuration for explosion parameters
  - `radius`: Blast radius in blocks
  - `damage`: Damage amount
  - `fire`: Whether explosion creates fire
  - `block-break`: Whether explosion breaks blocks
- `animation`: Animation type for TNT

## Installation

1. Download the `Ztnt-1.0.jar` file
2. Place the JAR file in your server's `plugins` folder
3. Restart your server or load the plugin with a plugin manager
4. Configure the plugin by editing the `config.yml` file in the `plugins/Ztnt` directory
5. Use `/ztnt reload` to apply changes after editing the configuration

## Support

For support, questions, or suggestions, please contact us through our Telegram channel:
- [Telegram: @Zoobastiks](https://t.me/Zoobastiks)

## License

This plugin is protected by a custom license. See [LICENSE.md](LICENSE.md) for details.

## Credits

- **Author**: Zoobastiks
- **Version**: 1.0
- **Tested on**: Minecraft 1.21.4
