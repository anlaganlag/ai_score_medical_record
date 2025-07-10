$hmsApiUrl = "http://172.16.1.13:10086"
$token = "6a5a8919-a9c7-4605-b917-3abb1df91e83"
$deptId = "127"
$patientId = 3835

$headers = @{
    "Authorization" = "Bearer $token"
    "systemdeptid" = $deptId
    "Content-Type" = "application/json"
}

Write-Host "Testing HMS API endpoints..."

# Test 1: Get patient info
Write-Host "`n1. Testing patient info endpoint:"
$patientUri = "$hmsApiUrl/admin-api/business/outpatient-blood/get?patientId=$patientId"
Write-Host "URI: $patientUri"

try {
    $patientResponse = Invoke-RestMethod -Uri $patientUri -Method Get -Headers $headers -TimeoutSec 10
    Write-Host "Patient info API - SUCCESS"
    Write-Host "Response: $($patientResponse | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "Patient info API - FAILED"
    Write-Host "Error: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        Write-Host "Status: $($_.Exception.Response.StatusCode.value__)"
    }
}

# Test 2: Get treatment info
Write-Host "`n2. Testing treatment info endpoint:"
$treatmentUri = "$hmsApiUrl/admin-api/business/first-course-record/get"
$treatmentBody = @{ patientId = $patientId } | ConvertTo-Json
Write-Host "URI: $treatmentUri"
Write-Host "Body: $treatmentBody"

try {
    $treatmentResponse = Invoke-RestMethod -Uri $treatmentUri -Method Post -Headers $headers -Body $treatmentBody -TimeoutSec 10
    Write-Host "Treatment info API - SUCCESS"
    Write-Host "Response: $($treatmentResponse | ConvertTo-Json -Depth 3)"
} catch {
    Write-Host "Treatment info API - FAILED"
    Write-Host "Error: $($_.Exception.Message)"
    if ($_.Exception.Response) {
        Write-Host "Status: $($_.Exception.Response.StatusCode.value__)"
    }
} 