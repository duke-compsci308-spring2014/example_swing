import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;


/**
 * Demo program to show many of the awt events that can be generated.
 * 
 * Illustrates XXXListeners for consuming several types of events including
 * semantic events (e.g., ActionEvent) and low-level events (e.g., FocusEvent)
 * 
 * See this tutorial for help on how to use the variety of components:
 *   http://docs.oracle.com/javase/tutorial/uiswing/examples/components/
 * 
 * @author Owen Astrachan
 * @author Robert C. Duvall
 */
@SuppressWarnings("serial")
public class EventDemo extends JFrame {
    private static final String DEFAULT_RESOURCE_PACKAGE = "resources.";
    // this constant should be defined by Java, not me :( 
    private static final String USER_DIR = "user.dir";
    private static final int FIELD_SIZE = 30;
    // most GUI components will be temporary variables,
    // only store components you need to refer to later
    private JTextArea myTextArea;
    private JFileChooser myChooser;
    // get strings from resource file
    private ResourceBundle myResources;
    // in this example, the listeners are reused by many
    // components, so keep track of them
    private ActionListener myActionListener;
    private KeyListener myKeyListener;
    private MouseListener myMouseListener;
    private MouseMotionListener myMouseMotionListener;
    private FocusListener myFocusListener;

    /**
     * Construct demo with a title that will appear in the title bar. Language
     * controls which properties file is read to generate the button text.
     * 
     * Note, this constructor builds the entire GUI and displays it --- leaving
     * no flexibility for users. Not necessarily the best practice.
     * 
     * @param title title that appears in the Frame window's title bar
     * @param language language in which to display messages
     */
    public EventDemo (String title, String language) {
        // set properties of frame
        setTitle(title);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // create a single file chooser for the entire example
        myChooser = new JFileChooser(System.getProperties().getProperty(USER_DIR));
        // create and arrange sub-parts of the GUI
        myResources = ResourceBundle.getBundle(DEFAULT_RESOURCE_PACKAGE + language);
        // create listeners that will respond to events
        makeListeners();
        // position interface components
        getContentPane().add(makeInput(), BorderLayout.NORTH);
        getContentPane().add(makeDisplay(), BorderLayout.CENTER);
        // create app menus
        setJMenuBar(makeMenus());
        // size and display the GUI
        pack();
        setVisible(true);
    }

    /**
     * Display any string message in the main text area.
     * 
     * @param message message to display
     */
    public void showMessage (String message) {
        myTextArea.append(message + "\n");
        myTextArea.setCaretPosition(myTextArea.getText().length());
    }

    /**
     * Display any string message in a popup error dialog.
     * 
     * @param message message to display
     */
    public void showError (String message) {
        JOptionPane.showMessageDialog(this, message, 
                                      myResources.getString("ErrorTitle"),
                                      JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Create all the listeners so they can be later assigned to specific
     * components.
     * 
     * Note, since these classes will not ever be used by any other class, make
     * them inline (i.e., as anonymous inner classes) --- saves making a
     * separate file for one line of actual code.
     */
    protected void makeListeners () {
        // listener for "high-level" events, i.e., those made
        // up of a sequence of low-level events, like a button
        // press (mouse down and up within a button object)
        myActionListener = new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                echo("action", e);
            }
        };
        // listener for low-level keyboard events
        myKeyListener = new KeyListener() {
            @Override
            public void keyPressed (KeyEvent e) {
                echo("pressed", e);
            }
            @Override
            public void keyReleased (KeyEvent e) {
                echo("released", e);
            }
            @Override
            public void keyTyped (KeyEvent e) {
                echo("typed", e);
            }
        };
        // listener for low-level mouse events
        myMouseListener = new MouseListener() {
            @Override
            public void mouseClicked (MouseEvent e) {
                echo("clicked", e);
            }
            @Override
            public void mouseEntered (MouseEvent e) {
                echo("enter", e);
            }
            @Override
            public void mouseExited (MouseEvent e) {
                echo("exit", e);
            }
            @Override
            public void mousePressed (MouseEvent e) {
                echo("pressed", e);
            }
            @Override
            public void mouseReleased (MouseEvent e) {
                echo("released", e);
            }
        };
        // listener for low-level mouse movement events
        myMouseMotionListener = new MouseMotionListener() {
            @Override
            public void mouseDragged (MouseEvent e) {
                echo("drag", e);
            }
            @Override
            public void mouseMoved (MouseEvent e) {
                echo("move", e);
            }
        };
        // listener for low-level focus events, i.e., the mouse
        // entering/leaving a component so you can type in it
        myFocusListener = new FocusListener() {
            @Override
            public void focusGained (FocusEvent e) {
                echo("gained", e);
            }
            @Override
            public void focusLost (FocusEvent e) {
                echo("lost", e);
            }
        };
    }

    /**
     * Create a menu to appear at the top of the frame, 
     *   usually File, Edit, App Specific Actions, Help
     */
    protected JMenuBar makeMenus () {
        JMenuBar result = new JMenuBar();
        result.add(makeFileMenu());
        return result;
    }

    /**
     * Create an input area for the user --- 
     *   text field for text, 
     *   buttons for starting actions
     */
    protected JComponent makeInput () {
        JPanel result = new JPanel();
        result.add(makeTextField());
        result.add(makeButton());
        result.add(makeClear());
        return result;
    }

    /**
     * Create a display area for showing out to the user, since it may display
     * lots of text, make it automatically scroll when needed
     */
    protected JComponent makeDisplay () {
        // create with size in rows and columns
        myTextArea = new JTextArea(FIELD_SIZE, FIELD_SIZE);
        myTextArea.addMouseListener(myMouseListener);
        myTextArea.addMouseMotionListener(myMouseMotionListener);
        return new JScrollPane(myTextArea);
    }

    /**
     * Create a menu that will pop up when the menu button is pressed in the
     * frame. File menu usually contains Open, Save, and Exit
     * 
     * Note, since these classes will not ever be used by any other class, make
     * them inline (i.e., as anonymous inner classes) --- saves making a
     * separate file for one line of actual code.
     */
    protected JMenu makeFileMenu () {
        JMenu result = new JMenu(myResources.getString("FileMenu"));
        result.add(new AbstractAction(myResources.getString("OpenCommand")) {
            @Override
            public void actionPerformed (ActionEvent e) {
                try {
                    int response = myChooser.showOpenDialog(null);
                    if (response == JFileChooser.APPROVE_OPTION) {
                        echo(new FileReader(myChooser.getSelectedFile()));
                    }
                }
                catch (IOException io) {
                    // let user know an error occurred, but keep going
                    showError(io.toString());
                }
            }
        });
        result.add(new AbstractAction(myResources.getString("SaveCommand")) {
            @Override
            public void actionPerformed (ActionEvent e) {
                try {
                    echo(new FileWriter("demo.out"));
                }
                catch (IOException io) {
                    // let user know an error occurred, but keep going
                    showError(io.toString());
                }
            }
        });
        result.add(new JSeparator());
        result.add(new AbstractAction(myResources.getString("QuitCommand")) {
            @Override
            public void actionPerformed (ActionEvent e) {
                // clean up any open resources, then
                // end program
                System.exit(0);
            }
        });
        return result;
    }

    /**
     * Create a standard text field (a single line that responds to enter being
     * pressed as an ActionEvent) that listens for a variety of kinds of events
     */
    protected JTextField makeTextField () {
        JTextField result = new JTextField(FIELD_SIZE);
        result.addKeyListener(myKeyListener);
        result.addFocusListener(myFocusListener);
        result.addActionListener(myActionListener);
        return result;
    }

    /**
     * Create a standard button (a rectangular area that responds to mouse
     * press and release within its bounds) that listens for a variety of kinds
     * of events
     */
    protected JButton makeButton () {
        JButton result = new JButton(myResources.getString("ActionCommand"));
        result.addActionListener(myActionListener);
        result.addKeyListener(myKeyListener);
        result.addMouseListener(myMouseListener);
        return result;
    }

    /**
     * Create a button whose action is to clear the display area when pressed.
     * 
     * Note, since this class will not ever be used by any other class, make it
     * inline (i.e., as anonymous inner classes) --- saves making a separate
     * file for one line of actual code.
     */
    protected JButton makeClear () {
        JButton result = new JButton(myResources.getString("ClearCommand"));
        result.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent e) {
                myTextArea.setText("");
            }
        });
        return result;
    }

    /**
     * Echo key presses by showing important attributes
     */
    private void echo (String s, KeyEvent e) {
        showMessage(s + " char:" + e.getKeyChar() + " mod: " +
                    KeyEvent.getKeyModifiersText(e.getModifiers()) + " mod: " +
                    KeyEvent.getKeyText(e.getKeyCode()));
    }

    /**
     * Echo action events including time event occurs
     */
    private void echo (String s, ActionEvent e) {
        showMessage(s + " = " + e.getActionCommand() + " " + e.getWhen());
    }

    /**
     * Echo mouse events (enter, leave, etc., including position and buttons)
     */
    private void echo (String s, MouseEvent e) {
        showMessage(s + " x = " + e.getX() + " y = " + e.getY() + " mod: " +
                    MouseEvent.getMouseModifiersText(e.getModifiers()) + " button: " +
                    e.getButton() + " clicks " + e.getClickCount());
    }

    /**
     * Echo other events (e.g., Focus)
     */
    private void echo (String s, AWTEvent e) {
        showMessage(s + " " + e);
    }

    /**
     * Echo data read from reader to display
     */
    private void echo (Reader r) {
        try {
            String s = "";
            BufferedReader input = new BufferedReader(r);
            String line = input.readLine();
            while (line != null) {
                s += line + "\n";
                line = input.readLine();
            }
            showMessage(s);
        }
        catch (IOException e) {
            showError(e.toString());
        }
    }

    /**
     * Echo display to writer
     */
    private void echo (Writer w) {
        PrintWriter output = new PrintWriter(w);
        output.println(myTextArea.getText());
        output.flush();
        output.close();
    }

    /**
     * Java starts the program here and does not end until GUI goes away
     * 
     * @param args command-line arguments
     */
    public static void main (String[] args) {
        new EventDemo("Event Demo", "English");
    }
}
