# Oracle Cloud par SafeSaathi backend (laptop ke bina, hamesha chalne wala)

Is guide ke baad backend Oracle ke ek virtual server (VM) par chalega. Laptop, MySQL, VS Code, tunnel,
kuch ON rakhne ki zaroorat nahi, aur address kabhi nahi badlega.

## 0. Pehle ye jaan lo
- Free plan ki shartein aur limits badalti rehti hain. Signup se pehle Oracle ka "Always Free" page aur apne
  console mein "Always Free-eligible" label dekh lo.
- **Idle reclamation:** Oracle kam use hone wale free VM ko reclaim kar sakta hai (7 din tak CPU/network 20% se kam).
  Tumhara backend halka hai, to ye risk hai. VM stop ho jaye to console se dobara Start kar sakti ho.
  Oracle ke notices mein ek tarika Pay-As-You-Go account banana bataya gaya hai (free limits ke andar charge nahi lagta,
  par card chahiye aur limit paar karne par bill aa sakta hai). Ye faisla tumhara hai.
- **Home region** signup ke waqt chunna padta hai aur badal nahi sakta. India ka region (Mumbai ya Hyderabad) lo.
- **ARM (A1) VM** aksar "out of capacity" deta hai. Asaan raasta: **AMD "VM.Standard.E2.1.Micro"** (1 GB RAM) lo.
  1 GB mein Spring Boot + PostgreSQL chalta hai, par tight hai, isliye neeche swap banate hain.
- Signup par aksar card verification maangta hai.

## 1. Account aur VM
1. oracle.com/cloud/free par account banao, home region chuno.
2. Console mein: **Compute -> Instances -> Create instance**.
3. Image: **Ubuntu** (Always Free-eligible wala). Shape: **VM.Standard.E2.1.Micro** (Always Free-eligible label ho).
4. Networking mein **Assign a public IPv4 address** ON rakho.
5. SSH key: "Generate a key pair" chuno aur **private key download karo** (kho gayi to server mein ghus nahi paogi).
6. Create dabao. Kuch minute baad **Public IP** dikhega, use note karo.

## 2. Ports kholo (80 aur 443)
1. Instance page -> **Subnet** par click -> **Default Security List** -> **Add Ingress Rules**.
2. Source CIDR `0.0.0.0/0`, IP Protocol **TCP**, Destination Port **80**. Phir ek aur rule port **443** ke liye.

## 3. Server mein ghuso (PowerShell se)
```
ssh -i C:\path\to\private.key ubuntu@<PUBLIC-IP>
```

## 4. Server ke andar: firewall, swap, Docker
```
# Ubuntu ke firewall mein 80/443 kholo
sudo iptables -I INPUT -p tcp --dport 80 -j ACCEPT
sudo iptables -I INPUT -p tcp --dport 443 -j ACCEPT
sudo netfilter-persistent save

# 2 GB swap (1 GB RAM ke liye zaroori)
sudo fallocate -l 2G /swapfile && sudo chmod 600 /swapfile && sudo mkswap /swapfile && sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# Docker
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker $USER
exit
```
Phir dobara `ssh` karke ghuso (taaki docker permission mile).
`netfilter-persistent` na mile to `sudo apt-get install -y iptables-persistent` chalao.

## 5. Code server par bhejo
Apne laptop ke PowerShell se (zip jis folder mein hai wahan):
```
scp -i C:\path\to\private.key safesaathi-backend.zip ubuntu@<PUBLIC-IP>:~/
```
Server mein:
```
sudo apt-get update && sudo apt-get install -y unzip
unzip safesaathi-backend.zip && cd safesaathi-backend
```

## 6. Secrets (.env)
```
cp .env.example .env
nano .env
```
`DB_PASSWORD` aur `JWT_SECRET` badlo (secret ke liye `openssl rand -base64 48`). Nano mein save: Ctrl+O, Enter, Ctrl+X.

## 7. Chalao
**Option 1: HTTPS (behtar).** Ek free domain chahiye, jaise duckdns.org par `safesaathi.duckdns.org`, jo server ke Public IP par point kare.
`.env` mein `DOMAIN=` wahi naam rakho.
```
docker compose up -d --build
```
**Option 2: Simple HTTP (domain ke bina, sirf testing).**
```
docker compose -f docker-compose.simple.yml up -d --build
```
Pehli baar build mein 5-15 minute lag sakte hain (1 GB RAM). Progress dekhne ke liye:
```
docker compose logs -f backend
```
`Started BackendApplication` aane par ruk jao (Ctrl+C se logs band, server chalta rahega).

## 7b. Test
Phone ke browser mein (mobile data par):
- HTTPS: `https://<tumhara-domain>/api/health`
- Simple: `http://<PUBLIC-IP>/api/health`

`{"service":"safesaathi-backend","status":"ok"}` aana chahiye.

## 8. App mein address (sirf ek baar)
`ServerConfig.java` mein:
```java
public static final String DEFAULT_URL = "https://<tumhara-domain>/";
```
Simple HTTP wale option mein: `"http://<PUBLIC-IP>/"`. Phir APK dobara banao.

## 9. Roz ka kaam
| Kaam | Command (server mein, `safesaathi-backend` folder mein) |
|---|---|
| Logs dekhna | `docker compose logs -f backend` |
| Restart | `docker compose restart` |
| Naya code daalna | naya zip bhejo, unzip karo, phir `docker compose up -d --build` |
| Database backup | `docker compose exec db pg_dump -U safeuser safesaathi > backup.sql` |

Simple wale option mein har command ke saath `-f docker-compose.simple.yml` lagao.

## 10. Agar kuch na chale
- **Browser mein site nahi khulti:** Security List mein 80/443 ka rule, aur server ke iptables wale commands dobara check karo.
- **Build beech mein ruk jaye ya "Killed":** RAM khatam hui. Swap (step 4) check karo: `free -h`.
- **Backend baar-baar restart ho:** `docker compose logs backend` mein error dekho.
- **VM band ho gaya:** console mein Instances -> Start. Idle reclamation wala point (section 0) dekho.
