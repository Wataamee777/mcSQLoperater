自作プラグイン SimpleSQL のための、プロフェッショナルかつ分かりやすい README.md を作成しました。
## SimpleSQL


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

storage-type: "SQLITE" # "MYSQL" または "SQLITE"
mysql:
  host: "127.0.0.1"
  port: 3306
  database: "minecraft"
  username: "root"
  password: "password"
sqlite:
  file-name: "database.db"

## 📄 ライセンス
このプロジェクトは MIT License の下で公開されています。詳細は LICENSE ファイルを参照してください。
