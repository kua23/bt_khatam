@echo off
echo ========================================
echo Starting All Microservices
echo ========================================
echo.

REM Start Customer Service (Port 8083)
echo Starting Customer Service on port 8083...
start "Customer Service (8083)" cmd /k "cd /d %~dp0customer-service && mvn spring-boot:run"
timeout /t 3 /nobreak >nul

REM Start Product Pricing Service (Port 8084)
echo Starting Product Pricing Service on port 8084...
start "Product Pricing Service (8084)" cmd /k "cd /d %~dp0product-pricing-service && mvn spring-boot:run"
timeout /t 3 /nobreak >nul

REM Start FD Calculator Service (Port 8085)
echo Starting FD Calculator Service on port 8085...
start "FD Calculator Service (8085)" cmd /k "cd /d %~dp0fd-calculator-service && mvn spring-boot:run"
timeout /t 3 /nobreak >nul

REM Start FD Account Service (Port 8086)
echo Starting FD Account Service on port 8086...
start "FD Account Service (8086)" cmd /k "cd /d %~dp0fd-account-service && mvn spring-boot:run"
timeout /t 3 /nobreak >nul

REM Start Login Service (Port 8087)
echo Starting Login Service on port 8087...
start "Login Service (8087)" cmd /k "cd /d %~dp0login-service && mvn spring-boot:run"
timeout /t 3 /nobreak >nul

REM Start API Gateway (Port 8080)
echo Starting API Gateway on port 8080...
start "API Gateway (8080)" cmd /k "cd /d %~dp0gateway && mvn spring-boot:run"

echo.
echo ========================================
echo All services are starting...
echo ========================================
echo.
echo Services:
echo   - Customer Service:       http://localhost:8083
echo   - Product Pricing:        http://localhost:8084
echo   - FD Calculator:          http://localhost:8085
echo   - FD Account:             http://localhost:8086
echo   - Login Service:          http://localhost:8087
echo   - API Gateway:            http://localhost:8080
echo.
echo Wait 30-60 seconds for all services to fully start.
echo Close any window to stop that service.
echo.
pause
