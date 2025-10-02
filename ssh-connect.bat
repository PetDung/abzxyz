@echo off
:: =============================
:: SSH Auto Connect Script
:: =============================

:: Cấu hình user, host, port
set USER=root
set HOST=14.225.192.60
set PORT=22

:: Nếu bạn dùng key (.pem hoặc .ppk)
:: set KEY_PATH=C:\path\to\private-key.pem

echo.
echo =============================
echo Đang kết nối SSH tới %USER%@%HOST%:%PORT%
echo =============================
echo.

:: Nếu bạn dùng password-based SSH:
ssh -p %PORT% %USER%@%HOST%

cd code

:: Nếu bạn dùng private key thì đổi thành:
:: ssh -i "%KEY_PATH%" -p %PORT% %USER%@%HOST%

pause
