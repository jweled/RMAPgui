
/**
 * Write a description of main here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Properties;
public class main {
    private static JFrame window;
    private static File rmapjarpath;
    private static File rmapdirpath;
    private static FileReader propsreader;
    private static JLabel labelFor(JComponent c, String text) {
        Rectangle bounds = c.getBounds();
        JLabel label = new JLabel(text);
        label.setBounds(bounds.x, (bounds.y - bounds.height), 320, 15);
        window.add(label);
        return label;
    }
    private static Properties initConfig() {
        Properties props = new Properties();
        try {
            propsreader = new FileReader(new File(".config"));
            props.load(propsreader);
            propsreader.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Config load failed", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return props;
    }
    private static void storeConfig(Properties p, String title, String body) {
        p.setProperty(title, body);
        try {
            FileWriter writer = new FileWriter(new File(".config"));
            p.store(writer, "RMAPGUI configuration");
            writer.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Config store failed", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private static void appendLine(JTextArea c, String t) {
        c.append(System.lineSeparator() + t);
        c.setCaretPosition(c.getDocument().getLength());
    }
    public static void main(String[] args) {
        window = new JFrame("RMAP GUI");
        
        //initialize important stuff
        Properties props = initConfig();
        rmapjarpath = new File(props.getProperty("jarpath"));
        rmapdirpath = new File(props.getProperty("dirpath"));
        
        //create components
        JButton installationbutton = new JButton("Choose...");
        installationbutton.setBounds(15,40,100,25);
        window.add(installationbutton); 
        JLabel installationpath = labelFor(installationbutton, "RMAP .jar file: " + rmapjarpath.getName());
        
        JFileChooser installation = new JFileChooser(rmapjarpath);
        installation.setFileFilter(new FileNameExtensionFilter("JAR executable files", "jar"));
        installationbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                installation.showOpenDialog(null);
                File f = installation.getSelectedFile();
                if (f != null) {
                    if (f.exists()) {
                        rmapjarpath = f;
                        installationpath.setText("RMAP .jar file: " + rmapjarpath.getName());
                    } else {
                        JOptionPane.showMessageDialog(null, "Invalid file selection", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        
        JTextField width = new JTextField(props.getProperty("width"));
        width.setBounds(15,100,50,25);
        window.add(width);
        labelFor(width, "Output image width");
        
        JTextField height = new JTextField(props.getProperty("height"));
        height.setBounds(15,160,50,25);
        window.add(height);
        labelFor(height, "Output image height");
        
        String[] modes = {"random", "color", "static", "lines", "checkered"};
        JComboBox mode = new JComboBox(modes);
        mode.setBounds(15,220,150,25);
        window.add(mode);
        labelFor(mode, "Image generation mode");       
        
        JButton dirbutton = new JButton("Choose...");
        dirbutton.setBounds(15,280,100,25);
        window.add(dirbutton);
        JLabel dirpath = labelFor(dirbutton, "Output directory: " + rmapdirpath.getName());
        
        JFileChooser dir = new JFileChooser();
        dir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        dirbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dir.showOpenDialog(null);
                File f = dir.getSelectedFile();
                if (f != null) {
                    if (f.exists()) {
                        rmapdirpath = f;
                        dirpath.setText("Output directory: " + rmapdirpath.getName());
                    } else {
                        JOptionPane.showMessageDialog(null, "Invalid directory selection", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });
        
        JTextField name = new JTextField(props.getProperty("name"));
        name.setBounds(15,340,100,25);
        window.add(name);
        labelFor(name, "Output image filename");
        
        String[] formats = {"jpg", "png", "gif", "bmp", "wbmp"};
        JComboBox format = new JComboBox(formats);
        format.setBounds(15,400,150,25);
        window.add(format);
        labelFor(format, "Output image format"); 
        
        JTextArea out = new JTextArea("...");
        out.setEditable(false);
        out.setBounds(15,480,800,80);
        
        JScrollPane trout = new JScrollPane(out);
        trout.setBounds(15,480,305,80);
        window.add(trout);
        
        JButton genbutton = new JButton("Generate");
        genbutton.setBounds(15,440,100,25);
        window.add(genbutton);
        genbutton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                appendLine(out, "Saving properties...");
                storeConfig(props, "height", height.getText());
                storeConfig(props, "width", width.getText());
                storeConfig(props, "jarpath", rmapjarpath.getPath());
                storeConfig(props, "dirpath", rmapdirpath.getPath());
                storeConfig(props, "name", name.getText());
                appendLine(out, "Starting RMAP...");
                new Thread() {
                    public void run() {
                        try {
                            Process exec = Runtime.getRuntime().exec("java -jar " + rmapjarpath.getPath()
                            + " " + width.getText()
                            + " " + height.getText()
                            + " " + mode.getSelectedItem()
                            + " " + rmapdirpath.getPath() + "\\"
                            + " " + name.getText()
                            + " " + format.getSelectedItem());
                            
                            BufferedReader stdin = new BufferedReader(new InputStreamReader(exec.getInputStream()));
                            BufferedReader stderr = new BufferedReader(new InputStreamReader(exec.getErrorStream()));
                            
                            String line = null;
                            while ((line = stdin.readLine()) != null) {
                                appendLine(out, line);
                            }
                        } catch (Exception i) {
                            appendLine(out, "Failed");
                            i.printStackTrace();
                            JOptionPane.showMessageDialog(null, "RMAP execution failed, check your .JAR path", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }.start();
            }
        });
        
        window.setIconImage(new ImageIcon("x32.png").getImage());
        window.setLayout(null);
        window.setSize(350,600);
        window.setResizable(false);
        window.setVisible(true);
    }
}
