# backend

Spring Boot 4.1.1 / Java 17 / Gradleを使用した、API開発の準備用プロジェクトです。ローカルDBにはDockerのPostgreSQL 16を使用します。このフォルダー単独でGit管理・チェックアウト・起動でき、親フォルダーの設定には依存しません。

- Spring MVC、Validation、Spring Data JPA、PostgreSQLドライバーを導入済みです。
- 基本URLは `http://<ホスト名>:<BACKEND_PORT>/api` です。
- コントローラー、サービス、エンティティ、リポジトリ、業務用テーブルはまだ作成していません。
- JPAはスキーマの検証のみを行います。テーブルの作成・変更やSQLによる自動初期化は行いません。

## 起動

Docker DesktopとJava 17が必要です。このREADMEがあるディレクトリを作業ディレクトリとして、以下のコマンドを実行します。

初回のみ `.env.example` を `.env` にコピーし、`POSTGRES_PASSWORD` の `replace_with_local_password` をローカル用のパスワードに変更してください。すでに `.env` がある場合は、上書きせずそのまま使用します。

```sh
cp .env.example .env
```

先にDBを起動してから、バックエンドを起動します。Gradle Wrapperが含まれているため、Gradleを別途インストールする必要はありません。

```sh
docker compose up -d --wait postgres
./gradlew bootRun
```

アプリケーションは作業ディレクトリ内の `.env` を読み込みます。IDEの実行設定でも、このバックエンドのフォルダーを作業ディレクトリに指定してください。プロセスに環境変数を設定した場合は、ファイルの設定より優先されます。

## 環境変数

DB接続用の設定は、このフォルダーの `.env` に置きます。バックエンドの待受設定とCORSも同じファイルで管理します。コード内の既定値は用意していないため、`.env.example` の項目をすべて設定してください。

| 変数 | 用途 |
| --- | --- |
| `DB_HOST` | ローカル起動したバックエンドからDBへ接続するホスト |
| `POSTGRES_PORT` | ホスト側に公開するDBのポート |
| `POSTGRES_DB` | DB名 |
| `POSTGRES_USER` | DBユーザー名 |
| `POSTGRES_PASSWORD` | DBパスワード |
| `SERVER_ADDRESS` | ローカル起動時の待受アドレス |
| `BACKEND_PORT` | バックエンドの公開ポート |
| `CORS_ALLOWED_ORIGINS` | 接続を許可するフロントエンドのオリジン（カンマ区切り） |

`.env`・`.env.*` は、このフォルダーの `.gitignore` でGit管理から除外しています。共有用の `.env.example` のみをコミットします。別のGitリポジトリにアップする場合も、`.gitignore` と `.env.example` を含めてください。`.env` はDockerイメージにも含めません。

## ローカルDBへの接続

| 項目 | このフォルダーの `.env` の設定 |
| --- | --- |
| Host | `DB_HOST` |
| Port | `POSTGRES_PORT` |
| Database | `POSTGRES_DB` |
| Username | `POSTGRES_USER` |
| Password | `POSTGRES_PASSWORD` |
| JDBC URL | `jdbc:postgresql://<DB_HOST>:<POSTGRES_PORT>/<POSTGRES_DB>` |

別のPostgreSQLが起動している場合は、`POSTGRES_PORT` を使用中のポートと重ならない値にしてください。

SQLコンソールを開く場合も、このバックエンドのフォルダーで実行します。

```sh
docker compose exec postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
```

DBのデータは名前付きボリュームに保存します。`.env` のDB名・ユーザー名・パスワードは、DBの初回初期化に使用されます。後からこれらの値を変更しても、既存のDBアカウントは自動では変更されません。今回の設定整理では、既存DBの認証情報を変更していません。

## フロントエンドとの接続準備

フロントエンドは別のリポジトリとして管理できます。フロントエンド側の `.env` にある `BACKEND_HOST`・`BACKEND_PORT` を、このバックエンドの接続先に合わせて設定してください。両方のリポジトリで設定ファイルを共有する必要はありません。

バックエンドのコンテキストパスは `/api` です。今後、`com.webportfolio.backend` 配下にコントローラーを作成して `/projects` にマッピングすると、最終的なURLは `/api/projects` になります。このAPIは現在実装されていません。

CORSで許可するオリジンは、このフォルダーの `.env` の `CORS_ALLOWED_ORIGINS` にカンマ区切りで設定します。フロントエンドのポートを変更した場合は、この値も合わせて変更してください。Cookieを使用する認証やログインの設定は追加していません。

## 検証とビルド

DBを起動した状態で、このバックエンドのフォルダーから実行します。

```sh
./gradlew test
./gradlew build
```

テストでは実際のPostgreSQLに接続し、`SELECT 1` の結果を確認します。テスト用のテーブルやデータは作成しません。実行可能なファイルは `build/libs/backend.jar` です。

## Dockerでバックエンドを起動

先にローカルで起動しているバックエンドを終了し、このバックエンドのフォルダーで実行します。`compose.yaml` と `.env` は同じ場所にあるため、追加の設定ファイル指定は不要です。

```sh
docker compose --profile backend up -d --build
docker compose --profile backend logs -f backend
```

通常の `docker compose up -d` ではPostgreSQLのみを起動します。`backend` プロファイルを指定すると、バックエンドのコンテナも起動します。

コンテナー内では、PostgreSQLのサービス名 `postgres` と内部ポート `5432` を使用して接続します。バックエンドはコンテナー内の `0.0.0.0:8080` で待ち受けます。これらはDocker内部通信用の固定値で、ホスト側の公開ポートは `.env` の `POSTGRES_PORT`・`BACKEND_PORT` で指定します。Composeが環境変数として設定を渡します。

停止するときは次を実行します。DBデータのボリュームは保持されます。

```sh
docker compose --profile backend down
```

現在はエンドポイントがないため、`/api/` の `404` 応答は正常です。`/api` は `/api/` にリダイレクトされます。起動ログの `Started BackendApplication` とDB接続テストで、開発準備が整っていることを確認できます。
