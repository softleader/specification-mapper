# SLKE Website

The [SLKE](https://slke.cloud.softleader.com.tw/) site, built using [Hugo](https://gohugo.io/).

## Build prerequisites

To build and serve the site, you'll need the latest [LTS release](https://nodejs.org/en/about/previous-releases) of **Node**.
Install it using **[nvm](https://github.com/nvm-sh/nvm/blob/master/README.md#installing-and-updating)**, for example:

```console
$ nvm install --lts
```

## Setup

1. Clone this repo.
2. From a terminal window, change to the cloned repo directory.
3. Get NPM packages and git submodules, including the the [Docsy](https://www.docsy.dev/) theme:
  ```console
  $ npm install 
  ```

## Build the site

執行以下指令來建立靜態網站:

```console
$ hugo
```

產生的靜態網站檔案會放 `public` 資料夾中

## Serve the site locally

在本地端啟動並綁定 [localhost:1313](localhost:1313):

```console
$ hugo serve
```

## Site Build

任何針對 main 的 push 或 merge 事件, 都會觸發 [action](https://github.com/softleader/slke/actions/workflows/site-build.yml), 自動包版並部署到 [SLKE](https://github.com/softleader/slke) 中
