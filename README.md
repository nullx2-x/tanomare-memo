# 頼まれごとメモ（Tanomare Memo）

頼まれごとを「誰に・いつまで」とセットで記録し、期限の時刻に通知で思い出させてくれる Android アプリです。

## 特徴

- **クイック登録**：件名 / 頼んだ人 / 期限（日付＋時刻）/ メモ。件名と頼んだ人は必須入力
- **一覧**：期限の早い順、期限切れは赤色＋「期限切れ」、今日が期限は強調＋「今日まで」
- **フィルタ**：未完了 / 完了 / すべて
- **完了チェック・編集・削除**（削除はスナックバーから取り消し可能）
- **期限通知**：期限時刻に通知（WorkManager で再起動後も復元。Android 13+ の通知許可に対応）
- **端末内のみ保存**：Room（SQLite）に保存し、**インターネット権限を持ちません**。外部にデータを送信することは一切ありません
- Material 3（ライト / ダーク、ダイナミックカラー対応）

## 動作環境

- Android 8.0（API 26）以上

## ビルド

JDK 17 と Android SDK（platform 34 / build-tools 34.0.0）が必要です。

```sh
./gradlew assembleRelease
```

リリース署名は、環境変数 `TANOMARE_SIGNING_PROPS` が指す
リポジトリ外のプロパティファイル（storeFile / storePassword / keyAlias / keyPassword）
から読み込みます。未設定の場合はデバッグ署名でビルドされます。

## 技術構成

- Kotlin + Jetpack Compose (Material 3)
- Room（ローカル保存）
- WorkManager（期限通知のスケジューリング）
- 単一モジュール、applicationId: `app.nullx2.tanomare`

## ライセンス

ソースコードは学習・個人利用目的で公開しています。アイコンには Google の Material Icons（Apache License 2.0）を含みます。
