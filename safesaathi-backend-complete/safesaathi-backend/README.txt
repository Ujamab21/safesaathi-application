SafeSaathi backend (Spring Boot 4.0.8, Java 17, MySQL)

LAPTOP PAR (local):
1. MySQL chalu rakho (Services > MYSQL80 > Start). Database: safesaathi, user: safeuser
2. src/main/resources/application.properties me spring.datasource.password apna password daalo
   (ya DB_PASSWORD environment variable set karo)
3. VS Code > File > Open Folder > yeh 'safesaathi-backend' folder
4. BackendApplication.java kholo > Run
5. Test: http://localhost:8080/api/health  -> {"status":"ok"}

INTERNET PAR (kisi bhi phone/network par chalane ke liye):
DEPLOY.md padho. Dockerfile diya hua hai. MySQL aur PostgreSQL dono chalte hain.
