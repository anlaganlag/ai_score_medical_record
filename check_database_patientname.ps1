# 检查数据库中的患者姓名
Write-Host "=== 检查数据库中的患者姓名 ===" -ForegroundColor Green

# 连接到MySQL数据库并查询最新的记录
$connectionString = "Server=localhost;Database=ai_medical_score;Uid=root;Pwd=123456;"

try {
    # 使用MySQL命令行工具查询
    $query = "SELECT id, patient_id, patient_name, score_total, score_level, created_time FROM ai_score_medical_record ORDER BY created_time DESC LIMIT 5;"
    
    Write-Host "执行SQL查询: $query" -ForegroundColor Yellow
    
    # 使用mysql命令行工具
    $result = mysql -u root -p123456 -e "USE ai_medical_score; $query"
    
    Write-Host "查询结果:" -ForegroundColor Cyan
    $result
    
} catch {
    Write-Host "数据库查询失败: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== 检查完成 ===" -ForegroundColor Green 