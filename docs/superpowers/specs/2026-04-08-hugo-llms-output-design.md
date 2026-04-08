# Hugo LLMS Output 設計

## 背景

網站目前使用 `site/hugo.yaml` 作為 Hugo 設定檔，且已啟用 `enableEmoji: true` 與 `enableRobotsTXT: true`。站台採 Hugo module 匯入 Docsy，`site/layouts/` 目前只有 `404.html` 與 `_markup/render-heading.html`，沒有任何 `llms`、`home` 或 `text/plain` 的自訂模板。

本次需求最初只是補上 AI-friendly 的純文字輸出格式設定，但經過檢視與追問後，範圍已明確擴大為：除了在 Hugo 設定中宣告 `LLMS` / `LLMSFull` 兩種輸出格式外，還要補齊對應模板，讓站點實際產生只包含繁體中文內容的 `llms.txt` 與 `llms-full.txt`。

## 目標

1. 保留既有 `enableEmoji` 與 `enableRobotsTXT` 設定，不重複新增。
2. 新增 `text/plain` 對應的 `mediaTypes` 設定，副檔名為 `txt`。
3. 新增 `LLMS` 與 `LLMSFull` 兩種 `outputFormats`，並掛到首頁輸出。
4. 提供首頁的 `llms.txt` 與 `llms-full.txt` 模板，讓 Hugo build 後產生實體檔案。
5. `llms.txt` 只輸出繁體中文站點索引摘要。
6. `llms-full.txt` 只聚合 `site/content/zh/docs/**` 的全文內容，排除 `search.md`。
7. `llms-full.txt` 依 Hugo 文件樹與 `weight` 排序，保留標題、段落、清單、程式碼與連結資訊。

## 非目標

1. 不新增英文 (`en`) 版本的 LLMS 輸出。
2. 不修改既有 `section` 輸出行為。
3. 不調整部署流程、搜尋、SEO 或其他站台設定。
4. 不將 `site/content/zh/search.md` 或 zh 首頁 `_index.md` 併入 `llms-full.txt` 正文聚合。

## 設計方案

### 方案 A：完整啟用並補模板（採用）

在 `site/hugo.yaml`：

- 新增 `mediaTypes.text/plain.suffixes: [txt]`
- 新增 `outputFormats.LLMS`
- 新增 `outputFormats.LLMSFull`
- 新增 `outputs.home`，包含 `HTML`、`RSS`、`LLMS`、`LLMSFull`

在 `site/layouts/`：

- 新增首頁 `llms.txt` 模板，輸出 zh 站點摘要與文件索引
- 新增首頁 `llms-full.txt` 模板，輸出 zh docs 全文聚合

優點是設定與模板同時到位，Hugo 不只知道有這兩種格式，還真的生得出檔案。缺點是範圍從純設定擴大成設定加模板，但這是需求自己長出來的，不是我們手癢。

### 方案 B：只定義格式，不補模板

只新增 `mediaTypes` 與 `outputFormats`，或頂多補上 `outputs.home`，但不新增對應模板。

優點是改動最少。缺點是 Hugo 未必會實際輸出對應檔案，就算輸出了，內容也不會自動符合 zh-only 聚合需求，屬於有設定、沒結果的裝忙配置。

### 方案 C：直接聚合原始 Markdown 來源

直接讀取 Markdown 原文並聚合，包含 shortcode、front matter 與原始語法。

優點是實作直觀。缺點是輸出會混入 Hugo shortcode 與 front matter 噪音，違反本次要提供「接近渲染後純文字」內容的目標。

## 定案內容

### 輸出檔案

1. `llms.txt`：只包含繁體中文站點摘要與文件索引。
2. `llms-full.txt`：只包含繁體中文 docs 全文聚合。

### 語系與內容來源

1. 兩個輸出都只處理 `zh` 語系。
2. `llms-full.txt` 只收 `site/content/zh/docs/**`。
3. `search.md` 不納入任何全文聚合。
4. `site/content/zh/_index.md` 可作為 `llms.txt` 摘要來源，但不併入 `llms-full.txt` 正文。

### 呈現與排序

1. 輸出目標是接近已渲染內容的純文字，而不是原始 Markdown 傾倒。
2. 保留標題、段落、清單、程式碼區塊與連結資訊。
3. 程式碼區塊完整保留，不做截斷。
4. `llms-full.txt` 依 Hugo 文件樹與 `weight` 排序。
5. 每篇聚合內容前應包含文件標題與 canonical URL。

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

### 模板位置

依 Hugo output format lookup rules，首頁 (`home`) 應使用 page kind 命名，而不是 `index`。因此預計新增：

1. `site/layouts/home.llms.txt`
2. `site/layouts/home.llmsfull.txt`

對應原則如下：

1. `LLMS` output format 對應 `home.llms.txt`
2. `LLMSFull` output format 對應 `home.llmsfull.txt`
3. `baseName` 只控制輸出檔名（`llms.txt`、`llms-full.txt`），不控制模板 lookup 名稱

## 資料流與行為

1. Hugo 讀取 `site/hugo.yaml`。
2. Hugo 註冊 `text/plain` 與兩個自訂 output formats。
3. Hugo 在首頁輸出階段依 `outputs.home` 啟用 `LLMS` 與 `LLMSFull`。
4. `llms.txt` 模板讀取 zh 首頁與 zh docs 導覽資料，輸出索引摘要。
5. `llms-full.txt` 模板走訪 zh docs 頁面集合，依文件樹與 `weight` 聚合為純文字全文。
6. 最終產物包含 `llms.txt` 與 `llms-full.txt`，且兩者都只含 zh 內容。

## 錯誤處理與風險

1. 若只定義 `outputFormats` 而未掛進 `outputs.home`，檔案不會產生。
2. 若格式名稱、baseName 與模板 lookup 名稱不一致，Hugo 會找不到正確模板。
3. 若直接聚合原始 Markdown，輸出會混入 shortcode 與 front matter 噪音，降低 LLM 可讀性。
4. 若頁面集合選取不精準，可能誤收 `search.md` 或非 zh 頁面，破壞 zh-only 驗收標準。
5. 程式碼區塊完整保留會讓 `llms-full.txt` 變大，但這是已接受的取捨。

## 測試與驗證

1. 檢查 `site/hugo.yaml` YAML 結構正確。
2. 確認 `mediaTypes`、`outputFormats`、`outputs.home` 名稱一致。
3. 檢查模板 lookup 名稱與 Hugo output format 對應正確。
4. 以 `hugo build` 驗證 build 成功。
5. 確認輸出產物中存在 `llms.txt` 與 `llms-full.txt`。
6. 檢查 `llms.txt` 只包含 zh 索引摘要。
7. 檢查 `llms-full.txt` 只聚合 zh docs 內容，依文件樹排序，並保留完整程式碼區塊。

## 實作範圍

本次實作將同時修改 `site/hugo.yaml` 與新增首頁純文字輸出模板，屬於小型但跨設定與模板的功能變更。
