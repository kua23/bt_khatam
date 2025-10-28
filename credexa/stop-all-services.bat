@echo off
echo Stopping all Spring Boot services...

REM Kill all Java processes running Maven Spring Boot
taskkill /F /FI "WINDOWTITLE eq Customer Service (8083)*" 2>nul
taskkill /F /FI "WINDOWTITLE eq Product Pricing Service (8084)*" 2>nul
taskkill /F /FI "WINDOWTITLE eq FD Calculator Service (8085)*" 2>nul
taskkill /F /FI "WINDOWTITLE eq FD Account Service (8086)*" 2>nul
taskkill /F /FI "WINDOWTITLE eq Login Service (8087)*" 2>nul
taskkill /F /FI "WINDOWTITLE eq API Gateway (8080)*" 2>nul

echo.
echo All services stopped.
pause
