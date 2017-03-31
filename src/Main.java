import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by admin on 31.03.2017.
 */
public class Main {
    public static void main(String[] args) throws IOException, MimeTypeParseException, URISyntaxException {
        download(new URI("https://www.mirea.ru/"), new HashSet<>());
    }

    private static void download(URI link, HashSet<URI> visited) {
        if (visited.contains(link) || visited.size() >= 16) return;
        visited.add(link);
        System.out.println(link);
        try {
            URL url = link.toURL();

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            String ct = conn.getHeaderField("Content-Type");
            MimeType mt = new MimeType(ct);
            String cs = mt.getParameter("charset");
            String html;
            try (InputStream is = conn.getInputStream()) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                while (true) {
                    int c = is.read();
                    if (c < 0) break;
                    bos.write(c);
                }
                html = cs == null ? bos.toString() : bos.toString(cs);
            }
            Pattern p = Pattern.compile("href\\s*=\\s*([^\\s>]+|'[^']*'|\"[^\"]*\")");
            Matcher m = p.matcher(html);
            while (m.find()) {
                String href = m.group(1);
                if (href.startsWith("'") || href.startsWith("\"")) {
                    href = href.substring(1, href.length() - 1);
                }
                URI child = link.resolve(href);
                //download(child, visited);
                Runnable action =() -> download(child,visited);
                new Thread(action).start();
            }
        } catch (Exception error) {
            System.out.println(error);
        }
    }
}
