package com.main;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GoldRateNotifier {

    public static void main(String[] args) {
        // Reads secrets from environment variables (used locally or via GitHub Actions)
        String senderEmail = System.getenv("manja21ms@gmail.com");
        String senderPassword = System.getenv("SENDER_PASSWORD");
        String recipientEmail = "manja21ms@gmail.com"; // Set your target email here

        String rateText = fetchGoldRate();
        // Print fetched rate HTML/text for testing
        System.out.println("--- Fetched rate output (for testing) ---");
        System.out.println(rateText);
        System.out.println("--- End fetched output ---");

        // Email sending is commented out during testing to avoid accidental sends
        // sendEmail(senderEmail, senderPassword, recipientEmail, "Daily Bhima Gold Rate Update", rateText);
    }

    private static String fetchGoldRate() {
        try {
            // First try the site's API (the page updates rates client-side via an API)
            String apiUrl = "https://bhima-price-calculator-9njho.ondigitalocean.app/api/metal-prices/formatted";
            try {
                String json = Jsoup.connect(apiUrl)
                        .ignoreContentType(true)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                        .timeout(10000)
                        .execute()
                        .body();

                // Parse JSON using Jackson instead of regex
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(json);
                JsonNode raw = root.path("data").path("raw");
                if (!raw.isMissingNode()) {
                    int gold22Int = raw.path("gold22kt").asInt(-1);
                    int silverInt = raw.path("silver").asInt(-1);
                    System.out.println("API fetch succeeded: gold22=" + gold22Int + " silver=" + silverInt);

                    String gold22 = gold22Int > 0 ? formatRupees(Integer.toString(gold22Int)) : "N/A";
                    String silver = silverInt > 0 ? formatRupees(Integer.toString(silverInt)) : "N/A";

                    StringBuilder sb = new StringBuilder();
                    sb.append("<html><body>");
                    sb.append("<h3>Today's Rates</h3>");
                    sb.append("<table border='1' cellpadding='6' cellspacing='0'>");
                    sb.append("<tr><th>Metal</th><th>Purity</th><th>Price (per g)</th></tr>");
                    sb.append("<tr><td>Gold</td><td>22K</td><td>").append(gold22).append("</td></tr>");
                    sb.append("<tr><td>Silver</td><td>-</td><td>").append(silver).append("</td></tr>");
                    sb.append("</table>");
                    sb.append("</body></html>");

                    return sb.toString();
                }
            } catch (Exception apiEx) {
                // If the API request fails, fall back to scraping the static page text
                System.out.println("API fetch failed or JSON parse error, falling back to page scraping: " + apiEx.getMessage());
            }

            // Fallback: scrape the page (static fallback values embedded in HTML)
            String url = "https://www.bhimajewellery.com/pages/todays-rate";
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                    .timeout(10000)
                    .get();

            String text = doc.body().text();

            String gold22 = findRate(text, "22K");
            String silver = findRate(text, "Silver");

            StringBuilder sb = new StringBuilder();
            sb.append("<html><body>");
            sb.append("<h3>Today's Rates</h3>");
            sb.append("<table border='1' cellpadding='6' cellspacing='0'>");
            sb.append("<tr><th>Metal</th><th>Purity</th><th>Price (per g)</th></tr>");
            sb.append("<tr><td>Gold</td><td>22K</td><td>").append(gold22).append("</td></tr>");
            sb.append("<tr><td>Silver</td><td>-</td><td>").append(silver).append("</td></tr>");
            sb.append("</table>");
            sb.append("</body></html>");

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to fetch today's gold rate from Bhima Jewellers.";
        }
    }

    // Looks for a rate value near a key phrase (e.g. "24K", "22K", "Silver"). Returns "N/A" if not found.
    private static String findRate(String text, String key) {
        try {
            // Try to find patterns like "24K ... ₹12,835"
            Pattern p = Pattern.compile(Pattern.quote(key) + ".*?₹\\s?([\\d,]+(?:\\.\\d+)?)", Pattern.CASE_INSENSITIVE);
            Matcher m = p.matcher(text);
            if (m.find()) {
                return "₹" + m.group(1);
            }

            // Fallback: find any currency value and ensure the context contains the key
            p = Pattern.compile("₹\\s?([\\d,]+(?:\\.\\d+)?)(?:/g)?", Pattern.CASE_INSENSITIVE);
            m = p.matcher(text);
            while (m.find()) {
                int start = Math.max(0, m.start() - 60);
                int end = Math.min(text.length(), m.end() + 60);
                String context = text.substring(start, end);
                if (context.toLowerCase().contains(key.toLowerCase())) {
                    return "₹" + m.group(1);
                }
            }
        } catch (Exception ignored) {
        }
        return "N/A";
    }

    // Formats a numeric rupee value (e.g. "13965") into a rupee string with commas and /g
    private static String formatRupees(String numeric) {
        try {
            long v = Long.parseLong(numeric);
            StringBuilder sb = new StringBuilder();
            String s = Long.toString(v);
            int len = s.length();
            int firstGroup = len % 3;
            if (firstGroup == 0) firstGroup = 3;
            sb.append(s.substring(0, firstGroup));
            for (int i = firstGroup; i < len; i += 2) {
                if (i + 2 <= len) {
                    sb.append(',');
                    sb.append(s.substring(i, Math.min(i + 2, len)));
                }
            }
            // The above grouping is not perfect for Indian grouping; use a simple solution instead
            // Fallback to using standard grouping every three digits for simplicity
            java.text.NumberFormat nf = java.text.NumberFormat.getInstance(java.util.Locale.US);
            return "₹" + nf.format(v) + "/g";
        } catch (Exception e) {
            return "₹" + numeric + "/g";
        }
    }

    private static void sendEmail(String sender, String password, String recipient, String subject, String body) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sender, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject);
            // The body returned by fetchGoldRate() is HTML; send as HTML email
            message.setContent(body, "text/html; charset=UTF-8");

            Transport.send(message);
            System.out.println("Email sent successfully!");
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}