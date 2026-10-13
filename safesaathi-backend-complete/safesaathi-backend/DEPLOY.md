> Oracle Cloud ke liye alag guide: ORACLE.md dekho (docker-compose.yml, Caddyfile, .env.example bhi diye hain).

# SafeSaathi backend: laptop ke bina, hamesha chalne ke liye (stable address)

**Sabse pehle ye samjho:** laptop band hone par bhi app ka Login/Register chalane ka ek hi asli tarika hai,
backend ko **internet par kisi server (cloud) par** chalana. Tab address ek baar milta hai aur kabhi nahi badalta,
aur laptop, MySQL, VS Code, tunnel, kuch ON rakhne ki zaroorat nahi.
Is zip mein backend cloud ke liye taiyar hai. Bas tumhe cloud par account banake deploy karna hai
(ye tumhare account se hota hai, isliye main nahi kar sakti).

## Sabse chhota raasta (4 cheezein)

1. **Database:** koi bhi managed database lo. MySQL ya PostgreSQL, dono chalte hain.
2. **Code ko GitHub par daalo** (private repo theek hai).
3. **Web service banao** aur repo jodo. "Docker" / "Dockerfile" option chuno.
4. **4 environment variables daalo**, deploy dabao, aur `https://<address>/api/health` kholke test karo.

Free plan ki shartein aur kaun sa provider free hai, ye badalta rehta hai. Signup se pehle unka current plan dekh lena.

## DB_URL ke format

| Database | DB_URL |
|---|---|
| MySQL | `jdbc:mysql://HOST:PORT/DBNAME?sslMode=REQUIRED&serverTimezone=Asia/Kolkata` |
| PostgreSQL | `jdbc:postgresql://HOST:5432/DBNAME?sslmode=require` |

Provider kabhi `mysql://user:pass@host:port/db` ya `postgres://user:pass@host/db` jaisa link deta hai.
Use aise todo: `DB_URL` = `jdbc:` + `<mysql ya postgresql>://host:port/db` (user/pass hata ke),
`DB_USER` = user, `DB_PASSWORD` = pass.

---

# SafeSaathi backend: internet par chalane ka guide

Pehle backend sirf laptop par chalta tha, isliye app sirf same Wi-Fi par kaam karti thi.
Ab ye backend **cloud (internet) par bhi chal sakta hai**, taaki app kisi bhi Wi-Fi, mobile data
aur kisi bhi phone par Login/Register kar sake. Laptop par pehle jaise `Run` button se bhi
chalta rahega (kuch badalna nahi padta).

## 1. Is zip mein kya badla

| File | Badlav |
|---|---|
| `application.properties` | DB URL, DB user, DB password, JWT secret aur port ab environment variable se aate hain. Variable na ho to purane local default use hote hain, to laptop par sab pehle jaisa chalta hai. |
| `application-prod.properties` (naya) | Cloud ke liye. Isme default nahi hai: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` na ho to server start hi nahi hoga. Isse kamzor secret galti se cloud par nahi chalta. |
| `HealthController.java` (naya) | `GET /` aur `GET /api/health` bina login ke `{"status":"ok"}` deta hai. Server check karne ke liye. |
| `SecurityConfig.java` | `/`, `/error`, `/api/health` ko bina token ke allow kiya. (`/error` allow hone se 400 jaise errors 401 nahi ban'te.) |
| `Dockerfile`, `.dockerignore` (naye) | Kisi bhi cloud par deploy karne ke liye (Render, Railway, Fly.io, Google Cloud Run, Azure, VPS...). |
| `target/`, `bin/` | Hata diye (ye build ke kachre files the, dobara ban jaate hain). |

## 2. Environment variables (cloud par set karne hain)

| Naam | Kya daalna hai |
|---|---|
| `DB_URL` | `jdbc:mysql://HOST:PORT/DBNAME?sslMode=REQUIRED&serverTimezone=Asia/Kolkata` |
| `DB_USER` | Database ka username |
| `DB_PASSWORD` | Database ka password |
| `JWT_SECRET` | Lamba random secret, **kam se kam 32 characters**. Kisi ko mat batao. |
| `PORT` | Zyadatar platform khud set karta hai. Na ho to 8080 chalta hai. |

`JWT_SECRET` banane ke liye (PowerShell):

```
$b = New-Object byte[] 48; [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($b); [Convert]::ToBase64String($b)
```

Dhyan rakho: agar tumhare provider ne database ka address `mysql://user:pass@host:port/db` form mein diya hai,
to use `DB_URL` mein `jdbc:mysql://host:port/db?...` bana kar daalo, aur user/password alag variables mein.

## 3. Deploy ke steps (Option A: cloud, laptop ki zaroorat nahi)

1. **Cloud MySQL banao.** Koi bhi managed MySQL service lo (Railway, Aiven, Clever Cloud, TiDB Cloud, AWS RDS, Google Cloud SQL...).
   Free plan ki shartein badalti rehti hain, to signup se pehle unka current plan dekh lo.
   Ek database banao (jaise `safesaathi`) aur host, port, user, password note karo.
2. **Is folder ko GitHub par daalo** (private repo theek hai). `.gitignore` mein `target/` pehle se hai.
3. **Web service banao** apne cloud par aur repo jodo. "Docker" / "Dockerfile" option chuno. Build command ki zaroorat nahi, `Dockerfile` sab kar leti hai.
4. **Upar wale 4 environment variables set karo** (`DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`).
5. **Deploy dabao.** Logs mein `Started BackendApplication` aana chahiye. Pehli baar `users` table apne aap ban jaati hai.
6. **Test karo.** Platform ek address dega, jaise `https://safesaathi-xyz.onrender.com`. Phone ke browser mein kholo:

   ```
   https://<aapka-address>/api/health
   ```

   `{"status":"ok","service":"safesaathi-backend"}` aana chahiye. Ye mobile data par bhi chalna chahiye.
7. **Register test (optional, laptop se):**

   ```
   curl -X POST https://<aapka-address>/api/auth/register -H "Content-Type: application/json" -d "{\"fullName\":\"Test User\",\"email\":\"test@example.com\",\"phone\":\"9876543210\",\"password\":\"secret123\"}"
   ```

   "Registered successfully" aana chahiye.

### Dhyan rakhne wali baatein
- **Free plan par server so jata hai.** Kuch der koi request na aaye to pehli request 30-60 second le sakti hai. App mein timeout isi liye bada rakha hai. Demo se pehle `/api/health` ek baar khol lo.
- Database password aur `JWT_SECRET` kabhi code ya GitHub mein mat likho, sirf environment variables mein.
- `spring.jpa.hibernate.ddl-auto=update` project ke liye theek hai. Asli production mein migration tool (Flyway) behtar hota hai.

## 4. Option B: laptop par hi chalao, par internet par khol do (tunnel)

Demo ke liye sabse tez. Laptop aur backend ON rehna chahiye.

1. VS Code mein backend `Run` karo (`Started BackendApplication`).
2. Ek tunnel tool chalao (koi ek):
   - `ngrok http 8080`
   - `cloudflared tunnel --url http://localhost:8080`
3. Wo ek `https://...` address dega. Wahi app mein daalna hai.
4. Free tunnel ka address aksar har baar badalta hai, to har baar app mein naya address daalna padega.

## 5. App mein kya badalna hai

`ServerConfig.java` mein `DEFAULT_URL` ko apne HTTPS address se badlo (aakhri `/` zaroori):

```java
public static final String DEFAULT_URL = "https://<aapka-address>/";
```

Phir app dobara build karke phone par daalo. Ab Wi-Fi, mobile data, doosra Wi-Fi, sab par chalega.

## 6. Laptop par pehle jaisa chalana (local)

1. MySQL (MYSQL80) Start karo.
2. VS Code mein `BackendApplication.java` > `Run`.
3. App mein `DEFAULT_URL` = `http://<laptop-IP>:8080/` (phone aur laptop same Wi-Fi par).
