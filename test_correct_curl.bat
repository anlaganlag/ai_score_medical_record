@echo off
echo Testing correct API path: /admin-api/business/ai-score/generate
echo.

echo Testing generate API...
curl -X POST http://localhost:8080/admin-api/business/ai-score/generate ^
  -H "Content-Type: application/json" ^
  -d "{\"patientId\": 4360}"

echo.
echo.
echo Testing report API...
curl -X GET http://localhost:8080/admin-api/business/ai-score/report/4360

echo.
echo Test completed.
pause 