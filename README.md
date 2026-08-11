# gold-rate-notifier

This project fetches the gold rate from a target website (configurable) and sends the rate by email. It is implemented in Java (Maven) and designed to run automatically using GitHub Actions.

Checklist
- [x] Java program to fetch page and extract gold rate (configurable URL)
- [x] Send email via SMTP (configure via environment variables / GitHub Secrets)
- [x] GitHub Actions workflow to build and run on schedule (daily at 10:00 IST / 04:30 UTC)

Quick start
1. Configure repository secrets (Settings -> Secrets -> Actions):
   - TARGET_URL: URL of the Bhimma Jewelry page (or any page that shows the gold rate)
   - RECIPIENT_EMAIL: recipient email address
   - SMTP_HOST: SMTP server host (e.g. smtp.gmail.com)
   - SMTP_PORT: SMTP port (usually 587)
   - SMTP_USERNAME: SMTP username
   - SMTP_PASSWORD: SMTP password or app-specific password
   - SMTP_FROM: optional from address (defaults to SMTP_USERNAME)

2. Commit and push this repository to GitHub.

3. The workflow defined in `.github/workflows/send-gold-rate.yml` will run daily at 04:30 UTC (which is 10:00 IST).
   You can also trigger it manually from the Actions tab (`workflow_dispatch`).

How it works
- The application uses `TARGET_URL` to fetch the page HTML and attempts to extract a gold rate using common patterns (₹, Rs, or the first large number found).
- If extraction succeeds or fails, the application sends an email to `RECIPIENT_EMAIL` with the result.

Build & test locally
- You need Java 17 and Maven installed.
- Build: mvn -B package
- Run locally (example):

```powershell
$env:TARGET_URL = "https://www.bhimma.com/";
$env:RECIPIENT_EMAIL = "you@example.com";
$env:SMTP_HOST = "smtp.example.com";
$env:SMTP_PORT = "587";
$env:SMTP_USERNAME = "smtp-user@example.com";
$env:SMTP_PASSWORD = "secret";
java -jar target/gold-rate-notifier-1.0.0-shaded.jar
```

Gradle wrapper

This repository now includes Gradle wrapper scripts and a wrapper properties file that point to Gradle 8.14.

Files added:
- `gradlew` (UNIX shell wrapper)
- `gradlew.bat` (Windows wrapper)
- `gradle/wrapper/gradle-wrapper.properties` (points to Gradle 8.14 distribution)

Note: the wrapper JAR `gradle/wrapper/gradle-wrapper.jar` is not included by this automated edit. To generate it locally (or to create the full wrapper), run from the project root:

```powershell
# if you have Gradle installed locally
gradle wrapper --gradle-version 8.14
```

This will create the missing `gradle-wrapper.jar` plus other wrapper files. Commit the generated files so CI and other users can run `./gradlew` without installing Gradle.

IntelliJ configuration (use the wrapper)

- Open File → Settings → Build Tools → Gradle
- In 'Use Gradle from' choose 'Gradle Wrapper' (recommended)
- Set Gradle JVM to a Java 17 SDK (Project SDK)
- Click OK and then in the Gradle tool window click Refresh (or use Reload All Gradle Projects)

GitHub Actions
- The workflow now installs JDK 17 and uses the Gradle GitHub Action to run Gradle 8.14 and build the shadowJar.

Notes
- The scraper uses simple heuristics. If Bhimma's site uses dynamic JS or a specific structure, add a CSS selector extraction logic in `com.main.GoldRateNotifier.java` or supply a more specific `TARGET_URL` that contains the rate in the HTML.
- GitHub Actions cron uses UTC. The provided cron runs at 04:30 UTC (10:00 IST). Change the cron in the workflow if you want another schedule.
