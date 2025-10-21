#!/bin/bash

# === CONFIGURATION ===
BASE_URL="http://localhost:8080"  # 👈 Change this to your deployed base URL later
TEST_STRING="madam level noon racecar civic"
DELETE_STRING="madam"

echo "=== Stage 1: Backend Wizard API Test ==="
echo "Base URL: $BASE_URL"
echo "----------------------------------------"

# 1️⃣ CREATE / ANALYZE STRING
echo "1. Testing POST /strings ..."
CREATE_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/strings" \
  -H "Content-Type: application/json" \
  -d "{\"value\": \"$TEST_STRING\"}")

CREATE_BODY=$(echo "$CREATE_RESPONSE" | head -n 1)
CREATE_STATUS=$(echo "$CREATE_RESPONSE" | tail -n 1)
echo "Status: $CREATE_STATUS"
echo "Response: $CREATE_BODY"
echo "----------------------------------------"

if [ "$CREATE_STATUS" != "201" ] && [ "$CREATE_STATUS" != "409" ]; then
  echo "❌ POST /strings failed."
  exit 1
fi

# Extract the value (hash id or value)
HASH_ID=$(echo "$CREATE_BODY" | grep -o '"id":"[^"]*' | cut -d'"' -f4)

# 2️⃣ GET SPECIFIC STRING
echo "2. Testing GET /strings/{value} ..."
GET_ONE=$(curl -s -w "\n%{http_code}" "$BASE_URL/strings/$DELETE_STRING")
GET_ONE_BODY=$(echo "$GET_ONE" | head -n 1)
GET_ONE_STATUS=$(echo "$GET_ONE" | tail -n 1)
echo "Status: $GET_ONE_STATUS"
echo "Response: $GET_ONE_BODY"
echo "----------------------------------------"

# 3️⃣ GET ALL STRINGS (with filters)
echo "3. Testing GET /strings with filters ..."
GET_FILTERED=$(curl -s -w "\n%{http_code}" \
  "$BASE_URL/strings?is_palindrome=true&min_length=3&max_length=20")
GET_FILTERED_BODY=$(echo "$GET_FILTERED" | head -n 1)
GET_FILTERED_STATUS=$(echo "$GET_FILTERED" | tail -n 1)
echo "Status: $GET_FILTERED_STATUS"
echo "Response (truncated): $(echo "$GET_FILTERED_BODY" | cut -c1-200)..."
echo "----------------------------------------"

# 4️⃣ NATURAL LANGUAGE FILTER
echo "4. Testing GET /strings/filter-by-natural-language ..."
GET_NL=$(curl -s -w "\n%{http_code}" \
  "$BASE_URL/strings/filter-by-natural-language?query=all%20single%20word%20palindromic%20strings")
GET_NL_BODY=$(echo "$GET_NL" | head -n 1)
GET_NL_STATUS=$(echo "$GET_NL" | tail -n 1)
echo "Status: $GET_NL_STATUS"
echo "Response (truncated): $(echo "$GET_NL_BODY" | cut -c1-200)..."
echo "----------------------------------------"

# 5️⃣ DELETE STRING
echo "5. Testing DELETE /strings/{value} ..."
DELETE_RESP=$(curl -s -w "\n%{http_code}" -X DELETE "$BASE_URL/strings/$DELETE_STRING")
DELETE_BODY=$(echo "$DELETE_RESP" | head -n 1)
DELETE_STATUS=$(echo "$DELETE_RESP" | tail -n 1)
echo "Status: $DELETE_STATUS"
if [ "$DELETE_STATUS" == "204" ]; then
  echo "✅ Deleted successfully."
else
  echo "❌ Delete failed. Response: $DELETE_BODY"
fi
echo "----------------------------------------"

echo "🎯 Tests completed."
