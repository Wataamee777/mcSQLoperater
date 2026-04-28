[![SimpleSQL](https://api.mcbanners.com/banner/resource/hangar/SimpleSQL/banner.png?background__template=BLUE_RADIAL)](https://hangar.papermc.io/Wataamee777/SimpleSQL)
[![SimpleSQL](https://img.shields.io/hangar/dt/SimpleSQL?link=https%3A%2F%2Fhangar.papermc.io%2FWataamee777%2FSimpleSQL&style=flat)](https://hangar.papermc.io/Wataamee777/SimpleSQL)
[![SimpleSQL](https://img.shields.io/hangar/views/SimpleSQL?link=https%3A%2F%2Fhangar.papermc.io%2FWataamee777%2FSimpleSQL&style=flat)](https://hangar.papermc.io/Wataamee777/SimpleSQL)

※The English version of the README is below.

管理者（OP）がゲーム内から直接SQLを操作するための、軽量で安全なMinecraftサーバー用bukkitプラグインです。
## ✨ 特徴

* ゲーム内SQL実行: /sql <query> でデータベースを即座に操作。
* 非同期処理 (Async): すべてのSQL実行は別スレッドで行われるため、サーバーがラグることはありません。
* MySQL / SQLite 両対応: config.yml から簡単に切り替え可能。
* タブ補完: SQLキーワードの補完機能により、ミスを防ぎ効率的な操作をサポート。
* Vault 連携: 経済系プラグインとの親和性を確保（Soft-depend）。

## 🚀 コマンド

| コマンド | 権限 | 説明 |
|---|---|---|
| /sql <SQL文> | OP | SQLクエリを実行します。 |
| /sql help | OP | プラグインの現在のバージョンを表示します。 |

## 🛠️ インストール

   1. Releases から最新の .jar をダウンロード。
   2. サーバーの plugins フォルダに入れ、サーバーを起動。
   3. plugins/SimpleSQL/config.yml を編集し、データベース情報を設定。
   4. /restart またはサーバーを再起動して反映。

## ⚙️ 設定 (config.yml)
```yml
storage-type: "SQLITE" # "MYSQL" または "SQLITE"
mysql:
  host: "127.0.0.1"
  port: 3306
  database: "minecraft"
  username: "root"
  password: "password"
sqlite:
  file-name: "database.db"
```
## 📄 ライセンス
このプロジェクトは MIT License の下で公開されています。詳細は LICENSE ファイルを参照してください。

---

# SimpleSQL

A lightweight and secure Bukkit plugin for Minecraft servers, allowing administrators (OPs) to directly manipulate SQL from within the game.

## ✨ Features

* In-game SQL execution: Instantly manipulate the database with /sql <query>.

* Asynchronous processing (Async): All SQL executions are performed on a separate thread, preventing server lag.

* MySQL / SQLite compatible: Easily switch between them from config.yml.

* Tab completion: SQL keyword completion prevents errors and supports efficient operation.

* Vault integration: Ensures compatibility with economic plugins (Soft-depend).

## 🚀 Commands

| Command | Permissions | Description |
|---|---|---|
| /sql <SQL statement> | OP | Executes an SQL query. |
| /sql help | OP | Displays the current version of the plugin. |

## 🛠️ Installation

1. Download the latest .jar from Releases.

2. Place the file in the server's plugins folder and start the server.
3. Edit plugins/SimpleSQL/config.yml and configure the database information.
4. Restart the server using /restart to apply the changes.

## ⚙️ Configuration (config.yml)
```yml
storage-type: "SQLITE" # "MYSQL" or "SQLITE"
mysql:
host: "127.0.0.1"
port: 3306
database: "minecraft"
username: "root"
password: "password"
sqlite:
file-name: "database.db"
```

## 📄 License
This project is released under the MIT License. See the LICENSE file for details.
