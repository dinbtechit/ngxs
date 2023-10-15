# <img src="src/main/resources/META-INF/pluginIcon.svg" alt="drawing" width="75"/> NGXS

![Build](https://github.com/dinbtechit/ngxs/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/22604-ngxs.svg)](https://plugins.jetbrains.com/plugin/22604-ngxs)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/22604-ngxs.svg)](https://plugins.jetbrains.com/plugin/22604-ngxs)
[![Static Badge](https://img.shields.io/badge/--FFDD04?style=flat&logo=buy-me-a-coffee&logoColor=222222&label=Buy%20Me%20a%20Coffee&labelColor=FFDD04&color=FFDD04&link=https%3A%2F%2Fwww.buymeacoffee.com%2Fdinbtechit)
](https://www.buymeacoffee.com/dinbtechit)

<!-- Plugin description -->
NGXS is a state management library for Angular. This plugin provides NGXS CLI/Schematics, Intellisense and
auto-completions for Jetbrains IDE.

> Please ensure you have [ngxs cli](https://www.ngxs.io/plugins/cli) installed either globally or at the project level.

# Features

- Simply right click -> New -> NGXS CLI/Schematics to generate a boiler plate store.
- Navigate to Action Implementation using Gutter Icons
- LiveTemplate to autocompletion for creating `@Actions`, `@Selectors` and `export class NewActions` quickly
  - `cmd` + `Insert`/`ctrl` + `Insert` within Generator
  - `Alt` + `Enter` / `options` + `Enter` - QuickFixes and generate `@Action`s & `@Selector`s quickly
- Advance Templates (Editor Autocompletion - Mac - `cmd` + `space` or Windows/Linux - `ctrl` + `space`)
  - `methodName-action` 
    ```ts
    @Action(Add)
    add(ctx: State<StateModel>) {
     // TODO - Implement action
    }
    ```
  - `methodName-parameter1:Type-action-payload`
    ```ts
    // add-id:string-action-payload (ctrl + space)
    
    // Result 
    // *.state.ts
    @Action(Add)
    add(ctx: State<StateModel>, payload: Add) {
     // TODO - Implement action
    }
    
    // *.actions.ts
    export class Add {
     static readonly type = '[MyStore] Add';
     constructor(public id: string) {
     }
    }
    ``` 
  - `methodName-ClassName-action`  
  - `methodName-ClassName-parameter1:type,parameter2:type-action-payload` 
  - `methodName-selector-meta`
  -  `methodName-selector`
  
- Many more coming soon. Checkout [Github - List all Enhancements](https://github.com/dinbtechit/ngxs/issues?q=is%3Aissue+is%3Aopen+label%3Aenhancement)

<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "ngxs"</kbd> >
  <kbd>Install</kbd>

- Manually:

  Download the [latest release](https://github.com/dinbtechit/ngxs/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

# How can I contribute

- By starring this Github project and rating the [NGXS plugin](https://plugins.jetbrains.com/plugin/22604-ngxs).
- By submitting bugs and features -> https://github.com/dinbtechit/ngxs/issues
- By submitting pull requests for the above roadmap items.
- By sponsoring its development to ensure that the project is actively maintained and improved.

> If you find this plugin useful consider sponsoring its development to ensure that the project is actively maintained
> and improved. [Buy me a Coffee](https://www.buymeacoffee.com/dinbtechit)

[![image](https://www.buymeacoffee.com/assets/img/guidelines/download-assets-sm-1.svg)](https://www.buymeacoffee.com/dinbtechit)
