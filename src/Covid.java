import javax.swing.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.awt.Insets;
import java.awt.event.*;
import java.awt.BorderLayout;
import java.awt.Font;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Locale;
import java.text.NumberFormat;

public class Covid extends JFrame{
    private JButton mergeButton;
    private JButton clearLogs;
    private JButton updateButton;
    private JTextField countryNamTextField;
    //private String link = "https://raw.githubusercontent.com/owid/covid-19-data/master/public/data/owid-covid-data.csv";
    private String link = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
    int noCases;
    int difference_from_yesterday;
    String countryName;
    
    JFrame frame= new JFrame("Covid");
    JPanel panel = new JPanel();
    JMenuBar mBar = new JMenuBar();
    JTextArea log;
    JTable table;

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Covid();
            }
        });
    }

    public Covid() {
        
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        
        log = new JTextArea();
        log.setAlignmentX(100);
        log.setEditable(false);
        frame.add(new JScrollPane(log), null);
        log.setFont(log.getFont().deriveFont(15f));

        
        
        mergeButton = new JButton("Merge");
        mergeButton.setMargin(new Insets(4, 4, 4, 4));
        clearLogs = new JButton("Clear Logs");
        clearLogs.setMargin(new Insets(4, 4, 4, 4));
        updateButton = new JButton("Check");
        updateButton.setMargin(new Insets(4, 4, 4, 4));
        countryNamTextField = new JTextField("New Zealand");
        countryNamTextField.setColumns(10);
        Font font = new Font(Font.SANS_SERIF, Font.ITALIC,12);
        countryNamTextField.setFont(font);
        countryNamTextField.setHorizontalAlignment(JTextField.CENTER);

        countryNamTextField.setToolTipText("Enter Country");

        
        
        //panel.add(mergeButton);
        panel.add(updateButton);
        panel.add(countryNamTextField);
        panel.add(clearLogs);
        

        clearLogs.addActionListener(new ClearLogsActionListener());
        updateButton.addActionListener(new UpdateActionListener());
        countryNamTextField.addActionListener(new countryNamTextFieldActionListener());

        frame.getContentPane().add(BorderLayout.SOUTH, panel);
        frame.getContentPane().add(BorderLayout.NORTH, mBar);
        frame.setVisible(true);

	}

    class ClearLogsActionListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            log.setText(null);  
        }
    }

    class UpdateActionListener implements ActionListener{
        public void actionPerformed(ActionEvent e){
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(link)).build();
            HttpResponse<String> clienResponse;
            //log.append("Does not include all cases in the last 24 hours.\n");
            try {
                clienResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
                StringReader xmlReader = new StringReader(clienResponse.body());
                Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(xmlReader);
                int count = 0;
                int total_cases = 0;
                String interestedCountry = countryNamTextField.getText().strip();
                for(CSVRecord record : records){
                    count++; 
                    System.out.println("Record #"+count);  
                    String country = record.get("Country/Region");
                    int cases = Integer.parseInt(record.get(record.size()-1));
                    if(country.equalsIgnoreCase("Korea, South")){
                        country = "South Korea";
                    }
                    if(country.equalsIgnoreCase(interestedCountry)){
                        difference_from_yesterday = cases - Integer.parseInt(record.get(record.size()-2));
                        total_cases += cases;
                        interestedCountry = country;
                    }
                }
                noCases = total_cases;
                log.append("Confirmed cases in " + interestedCountry + ": " + NumberFormat.getNumberInstance(Locale.US).format(total_cases) +  "\nNew cases: " + NumberFormat.getNumberInstance(Locale.US).format(difference_from_yesterday) + "\n");
            }
            catch (Exception e1) {}   
        }
    }

    class countryNamTextFieldActionListener implements ActionListener{
        public void actionPerformed(ActionEvent e) {
            countryName = countryNamTextField.getText();

        }

    }

}