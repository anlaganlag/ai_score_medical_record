@echo off
echo Testing AI Medical Score System APIs...
echo.

set BASE_URL=http://localhost:8080/admin-api/business/ai-score

echo 1. Testing health check...
curl -s %BASE_URL%/../../../actuator/health
echo.
echo.

echo 2. Testing generate AI score...
curl -X POST "%BASE_URL%/generate" ^
  -H "Content-Type: application/json" ^
  -d "{\"patientId\": 123}"
echo.
echo.

echo 3. Testing get score report...
curl -X GET "%BASE_URL%/report/123"
echo.
echo.

echo 4. Testing save expert comment...
curl -X POST "%BASE_URL%/expert-comment" ^
  -H "Content-Type: application/json" ^
  -d "{\"patientId\": 123, \"expertComment\": \"测试专家点评\"}"
echo.
echo.

echo API testing completed!
pause 