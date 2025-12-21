# Email Configuration Setup Script for Windows
# Run this script in PowerShell before starting your Spring Boot application

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Email Configuration Setup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Set your email configuration here
$env:EMAIL_ENABLED = "true"
$env:EMAIL_HOST = "smtp.gmail.com"
$env:EMAIL_PORT = "587"
$env:EMAIL_USERNAME = "adityarajchaudhary12@gmail.com"
$env:EMAIL_PASSWORD = "hqkcywwierudbcuk"
$env:EMAIL_FROM = "adityarajchaudhary12@gmail.com"

Write-Host "✅ Environment variables set:" -ForegroundColor Green
Write-Host "   EMAIL_ENABLED = $env:EMAIL_ENABLED" -ForegroundColor White
Write-Host "   EMAIL_HOST = $env:EMAIL_HOST" -ForegroundColor White
Write-Host "   EMAIL_PORT = $env:EMAIL_PORT" -ForegroundColor White
Write-Host "   EMAIL_USERNAME = $env:EMAIL_USERNAME" -ForegroundColor White
Write-Host "   EMAIL_PASSWORD = ***" -ForegroundColor White
Write-Host "   EMAIL_FROM = $env:EMAIL_FROM" -ForegroundColor White
Write-Host ""
Write-Host "⚠️  IMPORTANT: These variables are only set for this PowerShell session." -ForegroundColor Yellow
Write-Host "   Start your Spring Boot application in THIS SAME window!" -ForegroundColor Yellow
Write-Host ""
Write-Host "To verify, visit: http://localhost:8080/api/email/diagnostic/config" -ForegroundColor Cyan
Write-Host ""


