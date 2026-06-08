#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 1 || $# -gt 2 ]]; then
  echo "Usage: $0 <apk-path> [zipalign-path]" >&2
  exit 1
fi

apk_path="$1"
zipalign_bin="${2:-zipalign}"

if [[ ! -f "$apk_path" ]]; then
  echo "APK not found: $apk_path" >&2
  exit 1
fi

if ! command -v objdump >/dev/null 2>&1; then
  echo "objdump is required but was not found on PATH." >&2
  exit 1
fi

if ! command -v unzip >/dev/null 2>&1; then
  echo "unzip is required but was not found on PATH." >&2
  exit 1
fi

tmp_dir="$(mktemp -d)"
cleanup() {
  rm -rf "$tmp_dir"
}
trap cleanup EXIT

unzip -qq "$apk_path" 'lib/*' -d "$tmp_dir"

libs=()
while IFS= read -r lib; do
  libs+=("$lib")
done < <(find "$tmp_dir/lib" \( -path '*/arm64-v8a/*.so' -o -path '*/x86_64/*.so' \) | sort)

if [[ ${#libs[@]} -eq 0 ]]; then
  echo "No arm64-v8a or x86_64 shared libraries found in $apk_path" >&2
  exit 1
fi

failures=0
for lib in "${libs[@]}"; do
  bad=0
  while IFS= read -r load_line; do
    exp="$(printf '%s\n' "$load_line" | sed -E 's/.*align 2\*\*([0-9]+).*/\1/')"
    if [[ -z "$exp" ]] || [[ "$exp" == "$load_line" ]] || [[ "$exp" -lt 14 ]]; then
      bad=1
      break
    fi
  done < <(objdump -p "$lib" | grep 'LOAD off')

  if [[ $bad -ne 0 ]]; then
    echo "UNALIGNED ELF: ${lib#"$tmp_dir/"}" >&2
    failures=1
  fi
done

if ! "$zipalign_bin" -c -P 16 -v 4 "$apk_path"; then
  echo "ZIP alignment verification failed for $apk_path" >&2
  failures=1
fi

if [[ $failures -ne 0 ]]; then
  exit 1
fi

echo "16 KB verification passed for $apk_path"
