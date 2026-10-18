# backend

Spring Boot 4.1.1 / Java 17 / Gradleを使用した、API開発の準備用プロジェクトです。ローカルDBにはDockerのPostgreSQL 16を使用します。

- Spring MVC、Validation、Spring Data JPA、PostgreSQLドライバーを導入済みです。
- 基本URLは `http://<BACKEND_HOST>:<BACKEND_PORT>/api` です。各値はルートの `.env` で設定します。
- コントローラー、サービス、エンティティ、リポジトリ、業務用テーブルはまだ作成していません。
- JPAはスキーマの検証のみを行います。テーブルの作成・変更やSQLによる自動初期化は行いません。

## 起動

WebPortfolioのルートに `.env` がない場合のみ、`.env.example` をコピーし、`POSTGRES_PASSWORD` の `replace_with_local_password` をローカル用のパスワードに変更してください。すでに `.env` がある場合は、上書きせずそのまま使用します。`backend/.env` は作成しません。

ルートで、先にDBを起動します。

```sh
docker compose up -d --wait postgres
```

`backend` ディレクトリで実行します。Gradle Wrapperが含まれているため、Gradleを別途インストールする必要はありません。

```sh
./gradlew bootRun
```

アプリケーションは、このディレクトリを作業ディレクトリとして起動すると、親ディレクトリの `.env` を読み込みます。IDEの実行設定でも、作業ディレクトリに `backend` を指定してください。環境変数に値を設定した場合は、ファイルの設定より優先されます。DB接続先・認証情報・待受アドレス・ポート・CORSの設定にはコード内の既定値がないため、ルートの `.env.example` を基に必要な値を設定してください。

## ローカルDBへの接続

| 項目 | ルートの `.env` の設定 |
| --- | --- |
| Host | `DB_HOST` |
| Port | `POSTGRES_PORT` |
| Database | `POSTGRES_DB` |
| Username | `POSTGRES_USER` |
| Password | `POSTGRES_PASSWORD` |
| JDBC URL | `jdbc:postgresql://<DB_HOST>:<POSTGRES_PORT>/<POSTGRES_DB>` |

ホストから接続するポートは `.env` の `POSTGRES_PORT` で指定します。別のPostgreSQLが起動している場合は、使用中のポートと重ならない値にしてください。

SQLコンソールはルートで起動します。

```sh
docker compose exec postgres sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB"'
```

DBのデータは `postgres_data` ボリュームに保存します。`.env` のDB名・ユーザー名・パスワードは、ボリュームの初回作成時に初期化に使用されます。後からこれらの値を変更しても、既存のDBアカウントは自動では変更されません。今回の設定整理では、既存DBの認証情報を変更していません。

## フロントエンドとの接続準備

`frontend` の開発サーバーは `http://localhost:<FRONTEND_PORT>` で起動します。`/api` へのリクエストは、Viteのプロキシを通じて `http://<BACKEND_HOST>:<BACKEND_PORT>` に転送されます。リクエストのパスはそのまま維持されます。これらの値はルートの `.env` で共通管理します。

バックエンドのコンテキストパスは `/api` です。今後、`com.webportfolio.backend` 配下にコントローラーを作成して `/projects` にマッピングすると、最終的なURLは `/api/projects` になります。このAPIは現在実装されていません。

CORSで許可するオリジンは、ルートの `.env` の `CORS_ALLOWED_ORIGINS` にカンマ区切りで設定します。フロントエンドのポートを変更した場合は、この値も合わせて変更してください。Cookieを使用する認証やログインの設定は追加していません。

## 検証とビルド

DBを起動した状態で、`backend` ディレクトリから実行します。

```sh
./gradlew test
./gradlew build
```

テストでは実際のPostgreSQLに接続し、`SELECT 1` の結果を確認します。テスト用のテーブルやデータは作成しません。実行可能なファイルは `build/libs/backend.jar` です。

## Dockerでバックエンドを起動

先にローカルで起動しているバックエンドを終了し、WebPortfolioのルートで実行します。

```sh
docker compose --profile backend up -d --build
docker compose --profile backend logs -f backend
```

コンテナー内では、PostgreSQLのサービス名 `postgres` と内部ポート `5432` を使用して接続します。バックエンドはコンテナー内の `0.0.0.0:8080` で待ち受けます。これらはDocker内部通信用の固定値で、ホスト側の公開ポートはルートの `.env` の `POSTGRES_PORT`・`BACKEND_PORT` で指定します。Dockerイメージには `.env` を含めず、Composeが環境変数として設定を渡します。

現在はエンドポイントがないため、`/api/` の `404` 応答は正常です。`/api` は `/api/` にリダイレクトされます。起動ログの `Started BackendApplication` とDB接続テストで、開発準備が整っていることを確認できます。
