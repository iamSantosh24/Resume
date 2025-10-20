#!/usr/bin/env bash
# start_with_sa.sh
# Usage:
#   ./start_with_sa.sh /absolute/path/to/serviceAccount.json
# or set SERVICE_ACCOUNT_PATH in the environment and run without args

set -euo pipefail

SA_PATH="${1:-${SERVICE_ACCOUNT_PATH:-}}"
if [[ -z "$SA_PATH" ]]; then
  echo "Error: no service account path provided. Usage: $0 /path/to/serviceAccount.json or set SERVICE_ACCOUNT_PATH env var." >&2
  exit 2
fi

if [[ ! -f "$SA_PATH" ]]; then
  echo "Error: service account file not found at: $SA_PATH" >&2
  exit 3
fi

export SERVICE_ACCOUNT_PATH="$SA_PATH"
export FIREBASE_DATABASE_URL="${FIREBASE_DATABASE_URL:-https://resume-b707f-default-rtdb.firebaseio.com}"

echo "Starting admin server with SERVICE_ACCOUNT_PATH=$SERVICE_ACCOUNT_PATH"
cd "$(dirname "$0")"
npm start

