#!/usr/bin/env bash
set -euo pipefail

SITE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LLMS_FILE="$SITE_DIR/public/llms.txt"
LLMS_FULL_FILE="$SITE_DIR/public/llms-full.txt"

LLMS_ZH_FILE="$SITE_DIR/public/zh/llms.txt"
LLMS_FULL_ZH_FILE="$SITE_DIR/public/zh/llms-full.txt"

DOCS_URL_REGEX='^https://softleader\.github\.io/specification-mapper/zh/docs/'
LLMS_FULL_ENGLISH_TAGLINE='Specification binding API for Spring Data JPA'
EXPECTED_LLMS_SUMMARY='Specification Mapper 是一套協助建構 Spring Data JPA Specifications 的工具，提供注解式、可擴充且可讀性高的查詢條件組合能力。'

require_file() {
  local file=$1
  if [[ ! -f "$file" ]]; then
    printf 'Missing generated file: %s\n' "$file"
    exit 1
  fi
}

count_expected_doc_urls() {
  local docs_dir="$SITE_DIR/content/zh/docs"
  if [[ ! -d "$docs_dir" ]]; then
    printf '0'
    return
  fi

  find "$docs_dir" -type f -name '*.md' | wc -l
}

count_full_urls() {
  local file=$1
  grep -oE 'https://softleader\.github\.io/specification-mapper/zh/docs/[^)[:space:]]*' "$file" | sort -u | wc -l
}

extract_summary() {
  local file=$1
  awk '
    /^Language: zh-TW/ {found=1; next}
    found && $0 ~ /./ {
      sub(/^Summary:[[:space:]]*/, "", $0)
      print
      exit
    }
  ' "$file"
}

check_llms_summary_zh() {
  local file=$1
  local label=$2

  local summary
  summary=$(extract_summary "$file")

  if [[ -z "$summary" ]]; then
    printf '[%s] Missing llms summary\n' "$label"
    exit 1
  fi

  if [[ "$summary" != "$EXPECTED_LLMS_SUMMARY" ]]; then
    printf '[%s] llms summary does not match expected zh summary\n' "$label"
    exit 1
  fi
}

check_llms_index() {
  local file=$1
  local label=$2

  grep -q '^# Specification Mapper$' "$file" || {
    printf '[%s] Missing site title\n' "$label"
    exit 1
  }

  grep -q '/zh/docs/' "$file" || {
    printf '[%s] Missing zh docs links\n' "$label"
    exit 1
  }

  if grep -q '/en/docs/' "$file"; then
    printf '[%s] Found unexpected English docs links\n' "$label"
    exit 1
  fi

  if grep -q '/zh/search' "$file"; then
    printf '[%s] Found unexpected search page\n' "$label"
    exit 1
  fi

  check_llms_summary_zh "$file" "$label"
}

check_llms_full_structure() {
  local file=$1
  local label=$2

  grep -q '^## ' "$file" || {
    printf '[%s] Missing section headings in llms-full.txt\n' "$label"
    exit 1
  }

  grep -q '^```' "$file" || {
    printf '[%s] Missing fenced code blocks in llms-full.txt\n' "$label"
    exit 1
  }

  grep -Eq '\[[^]]+\]\(https://softleader\.github\.io/specification-mapper/zh/docs/' "$file" || {
    printf '[%s] Missing markdown links in llms-full.txt\n' "$label"
    exit 1
  }

  if ! grep -q '^-' "$file" && ! grep -q '^\* ' "$file" && ! grep -q '^[0-9]\. ' "$file"; then
    printf '[%s] Missing list structure in llms-full.txt\n' "$label"
    exit 1
  fi
}

check_url_coverage() {
  local file=$1
  local label=$2

  local expected_urls
  local actual_urls

  expected_urls=$(count_expected_doc_urls)
  actual_urls=$(count_full_urls "$file")

  if [[ "$actual_urls" -lt "$expected_urls" ]]; then
    printf '[%s] llms-full URL coverage too low: expected >= %s, got %s\n' \
      "$label" "$expected_urls" "$actual_urls"
    exit 1
  fi
}

check_llms_full() {
  local file=$1
  local label=$2

  grep -q '^# Specification Mapper Documentation (zh-TW)$' "$file" || {
    printf '[%s] Missing llms-full heading\n' "$label"
    exit 1
  }

  grep -q '/zh/docs/mapper/' "$file" || {
    printf '[%s] Missing zh mapper documentation link in llms-full.txt\n' "$label"
    exit 1
  }

  grep -q 'tw.com.softleader.data.jakarta' "$file" || {
    printf '[%s] Missing preserved code block content in llms-full.txt\n' "$label"
    exit 1
  }

  check_llms_full_structure "$file" "$label"
  check_url_coverage "$file" "$label"

  if grep -q '&lt;' "$file" || grep -q '&#34;' "$file"; then
    printf '[%s] Found HTML entity escapes in llms-full.txt\n' "$label"
    exit 1
  fi

  if grep -q '{{<' "$file" || grep -q '{{%' "$file"; then
    printf '[%s] Found raw Hugo shortcode markers in llms-full.txt\n' "$label"
    exit 1
  fi
}

require_file "$LLMS_FILE"
require_file "$LLMS_FULL_FILE"
require_file "$LLMS_ZH_FILE"
require_file "$LLMS_FULL_ZH_FILE"

check_llms_index "$LLMS_FILE" 'llms.txt'
check_llms_index "$LLMS_ZH_FILE" 'zh/llms.txt'

check_llms_full "$LLMS_FULL_FILE" 'llms-full.txt'
check_llms_full "$LLMS_FULL_ZH_FILE" 'zh/llms-full.txt'

printf 'LLMS output verification passed\n'
