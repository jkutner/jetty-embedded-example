import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;
import java.security.cert.*;
import java.io.*;

public class Main extends HttpServlet {
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    resp.getWriter().print("down");
  }

  public static void main(String[] args) throws Exception{
    final Server server = new Server(Integer.valueOf(System.getenv("PORT")));
    final ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
    context.setContextPath("/");
    server.setHandler(context);
    context.addServlet(new ServletHolder(new Main()),"/*");
    server.start();

    server.setStopTimeout(10000L);;
    new Thread() {
      @Override
      public void run() {
        try {
          while (true) {
            Thread.sleep(2000);

            URL url = new URL("http://localhost:" + System.getenv("PORT"));
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            con.setRequestMethod("GET");
            con.setReadTimeout(3000);
            String health = readInput(con.getInputStream());

            if (!"up".equals(health)) {
              System.out.println("Stopping Jetty");
              context.stop();
              server.stop();
              break;
            }
          }
        } catch (Exception ex) {
          System.out.println("Failed to stop Jetty");
          ex.printStackTrace();
        }
      }
    }.start();

    server.join();
  }

  public static String readInput(InputStream is) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    StringBuilder data = new StringBuilder();
    String s = "";
    while((s = br.readLine()) != null) data.append(s);
    return data.toString();
  }
}
