#!/usr/bin/env bash
# scripts/design-lint.sh — design debt ratchet: hardcoded values can only decrease
set -euo pipefail

SRC="app/src/main/java/com/example"
DIRS="$SRC/ui $SRC/deckforge $SRC/duel $SRC/forge $SRC/p2p"

cnt() {
  local matches
  matches=$(grep -rE "$1" $DIRS --include='*.kt' --exclude-dir=theme --exclude-dir=design 2>/dev/null || true)
  if [ -z "$matches" ]; then
    echo 0
  else
    printf '%s\n' "$matches" | wc -l | tr -d ' '
  fi
}

colors=$(cnt 'Color\(0x[0-9A-Fa-f]{8}\)')
sizes=$(cnt 'fontSize *= *[0-9.]+\.sp')
mono=$(cnt 'FontFamily\.Monospace')

read -r b_colors b_sizes b_mono < design-baseline.txt
echo "design-lint: colors=$colors/$b_colors sizes=$sizes/$b_sizes mono=$mono/$b_mono"

if [ "$colors" -gt "$b_colors" ] || [ "$sizes" -gt "$b_sizes" ] || [ "$mono" -gt "$b_mono" ]; then
  echo "ERROR: Design debt increased above baseline!" >&2
  exit 1
fi

if [ "${UPDATE_BASELINE:-0}" = "1" ]; then
  echo "$colors $sizes $mono" > design-baseline.txt
  echo "Updated design-baseline.txt -> $colors $sizes $mono"
fi
