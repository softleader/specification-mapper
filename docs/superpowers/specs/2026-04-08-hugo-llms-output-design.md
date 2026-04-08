# Hugo LLMS Output 設計

## 背景

網站目前使用 `site/hugo.yaml` 作為 Hugo 設定檔，且已啟用 `enableEmoji: true` 與 `enableRobotsTXT: true`。本次需求是補齊 AI-friendly 的純文字輸出格式，讓站點首頁可產生 `llms.txt` 與 `llms-full.txt`。

## 目標

1. 保留既有 `enableEmoji` 與 `enableRobotsTXT` 設定，不重複新增。
2. 新增 `text/plain` 對應的 `mediaTypes` 設定，副檔名為 `txt`。
3. 新增 `LLMS` 與 `LLMSFull` 兩種 `outputFormats`。
4. 讓 Hugo 首頁 (`home`) 實際輸出 `llms.txt` 與 `llms-full.txt`。

## 非目標

1. 不調整內容模板或新增頁面內容來源。
2. 不修改既有 `section` 輸出行為。
3. 不處理部署流程、搜尋、SEO 或其他站台設定。

## 設計方案

### 方案 A：完整啟用（採用）

在 `site/hugo.yaml`：

- 新增 `mediaTypes.text/plain.suffixes: [txt]`
- 新增 `outputFormats.LLMS`
- 新增 `outputFormats.LLMSFull`
- 新增 `outputs.home`，包含 `HTML`、`RSS`、`LLMS`、`LLMSFull`

優點是設定完整，Hugo 會真的把兩個純文字輸出掛到首頁。缺點是除了格式定義外，還要一併調整 `outputs` 區塊，但這是必要改動，不是 YAML cosplay。

### 方案 B：只定義格式

只新增 `mediaTypes` 與 `outputFormats`，不調整 `outputs`。

優點是改動最少。缺點是 Hugo 未必會實際輸出對應檔案，屬於有設定、沒結果的裝忙配置。

### 方案 C：重複貼入現有布林設定

把需求片段原封不動塞進檔案，包含已存在的 `enableEmoji` 與 `enableRobotsTXT`。

優點是沒有。缺點是重複設定增加噪音，也讓後續維護者懷疑是不是 YAML 被人拿來練膽量。

## 組態變更細節

### `mediaTypes`

新增：

```yaml
mediaTypes:
  text/plain:
    suffixes:
      - txt
```

### `outputFormats`

新增：

```yaml
outputFormats:
  LLMS:
    mediaType: text/plain
    baseName: llms
    isPlainText: true
    notAlternative: true
  LLMSFull:
    mediaType: text/plain
    baseName: llms-full
    isPlainText: true
    notAlternative: true
```

### `outputs`

保留既有：

```yaml
outputs:
  section:
    - HTML
    - print
    - RSS
```

並補上：

```yaml
  home:
    - HTML
    - RSS
    - LLMS
    - LLMSFull
```

## 資料流與行為

1. Hugo 讀取 `site/hugo.yaml`。
2. Hugo 註冊 `text/plain` 與兩個自訂 output formats。
3. Hugo 在首頁輸出階段依 `outputs.home` 產生對應輸出。
4. 最終產物應包含 `llms.txt` 與 `llms-full.txt`。

## 錯誤處理與風險

1. 若只定義 `outputFormats` 而未掛進 `outputs.home`，檔案可能不會產生。
2. 若格式名稱與 `outputs.home` 內名稱不一致，Hugo 會無法正確套用輸出格式。
3. 若專案缺少對應 layout/template，最終輸出內容可能為空或不符合預期；本次僅處理設定層，不擴大範圍到模板製作。

## 測試與驗證

1. 檢查 `site/hugo.yaml` YAML 結構正確。
2. 確認 `mediaTypes`、`outputFormats`、`outputs.home` 名稱一致。
3. 實作後可用 Hugo build/serve 驗證 `llms.txt` 與 `llms-full.txt` 是否出現在首頁輸出產物中。

## 實作範圍

本次只修改 `site/hugo.yaml` 的站台輸出設定，屬於單檔、小範圍變更。
