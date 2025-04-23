import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;


public class WebScraperGUI extends JFrame {
    private JTextField urlField;
    private JTextArea resultArea;
    private JButton scrapeButton;
    private JButton saveButton;
    
    public WebScraperGUI() {
        // Set up the frame
        super("Web Scraper");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Create the top panel for input
        JPanel topPanel = new JPanel(new BorderLayout());
        JLabel urlLabel = new JLabel("Website URL: ");
        urlField = new JTextField("https://");
        scrapeButton = new JButton("Scrape");
        
        topPanel.add(urlLabel, BorderLayout.WEST);
        topPanel.add(urlField, BorderLayout.CENTER);
        topPanel.add(scrapeButton, BorderLayout.EAST);
        
        // Create the center panel for results
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        resultArea.setWrapStyleWord(true);
        resultArea.setLineWrap(true);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        
        // Create the bottom panel for save functionality
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Save Results");
        saveButton.setEnabled(false);
        bottomPanel.add(saveButton);
        
        // Add panels to the frame
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Add action listeners
        scrapeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scrapeWebsite();
            }
        });
        
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveResults();
            }
        });
    }
    
    private void scrapeWebsite() {
        String url = urlField.getText();
        resultArea.setText("Scraping " + url + "...\n\n");
        
        // Create a new thread for web scraping to keep UI responsive
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                try {
                    // Connect to the website and get the document
                    Document doc = Jsoup.connect(url).get();
                    
                    // Create a structured representation of the scraped data
                    StringBuilder result = new StringBuilder();
                    
                    // Title
                    result.append("Title: ").append(doc.title()).append("\n\n");
                    
                    // Extract meta description if available
                    Elements metaDescription = doc.select("meta[name=description]");
                    if (!metaDescription.isEmpty()) {
                        result.append("Description: ").append(metaDescription.attr("content")).append("\n\n");
                    }
                    
                    // Extract headings
                    result.append("=== HEADINGS ===\n");
                    Elements h1s = doc.select("h1");
                    for (int i = 0; i < h1s.size(); i++) {
                        result.append("H1 #").append(i+1).append(": ").append(h1s.get(i).text()).append("\n");
                    }
                    result.append("\n");
                    
                    // Extract paragraphs (limiting to first 10 to avoid too much content)
                    result.append("=== MAIN CONTENT ===\n");
                    Elements paragraphs = doc.select("p");
                    int pLimit = Math.min(paragraphs.size(), 10);
                    for (int i = 0; i < pLimit; i++) {
                        result.append(paragraphs.get(i).text()).append("\n\n");
                    }
                    
                    // Extract links
                    result.append("=== LINKS ===\n");
                    Elements links = doc.select("a[href]");
                    int linkLimit = Math.min(links.size(), 20);
                    for (int i = 0; i < linkLimit; i++) {
                        result.append(links.get(i).text()).append(" - ").append(links.get(i).attr("abs:href")).append("\n");
                    }
                    
                    return result.toString();
                } catch (Exception e) {
                    return "Error scraping website: " + e.getMessage();
                }
            }
            
            @Override
            protected void done() {
                try {
                    String result = get();
                    resultArea.setText(result);
                    saveButton.setEnabled(true);
                } catch (Exception e) {
                    resultArea.setText("Error retrieving results: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void saveResults() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Scraped Data");
        
        int userSelection = fileChooser.showSaveDialog(this);
        
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            
            // Add .txt extension if not present
            if (!fileToSave.getAbsolutePath().endsWith(".txt")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".txt");
            }
            
            try (FileWriter writer = new FileWriter(fileToSave)) {
                writer.write(resultArea.getText());
                JOptionPane.showMessageDialog(this, 
                                            "File saved successfully to: " + fileToSave.getAbsolutePath(),
                                            "Save Success",
                                            JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                                            "Error saving file: " + e.getMessage(),
                                            "Save Error",
                                            JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public static void main(String[] args) {
        // Set look and feel to system default
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Launch the application
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                WebScraperGUI app = new WebScraperGUI();
                app.setVisible(true);
            }
        });
    }
}