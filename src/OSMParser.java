
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Dominik on 11.04.2017.
 */
public class OSMParser {

    public static void main(String[] args) {

        try {
            URL website = new URL("http://www.openstreetmap.org/api/0.6/map?bbox=18.602856,54.320462,18.607562,54.323349");
            URLConnection connection = website.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = bufferedReader.readLine()) != null)
                response.append(inputLine);

            bufferedReader.close();

            System.out.println(response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
