/*
#    This program is free software: you can redistribute it and/or modify
#    it under the terms of the GNU General Public License as published by
#    the Free Software Foundation, either version 3 of the License, or
#    (at your option) any later version.
#
#    This program is distributed in the hope that it will be useful,
#    but WITHOUT ANY WARRANTY; without even the implied warranty of
#    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#    GNU General Public License for more details.
#
#    You should have received a copy of the GNU General Public License
#    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Authors:
 * Mateusz Sławomir Lach ( matlak, msl )
 * Damian Marciniak
 */
package jchess;

import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.io.FileOutputStream;
import static javax.swing.WindowConstants.DISPOSE_ON_CLOSE;

public class ThemeChooseWindow extends JDialog implements ActionListener, ListSelectionListener
{

    JList themesList;
    ImageIcon themePreview;
    GridBagLayout gbl;
    public String result;
    GridBagConstraints gbc;
    JButton themePreviewButton;
    JButton okButton;

    ThemeChooseWindow(Frame parent) throws Exception
    {
        super(parent);

        String[] dirNames = getThemeNames();
        if (dirNames.length > 0)
        {
            this.setTitle(Settings.lang("choose_theme_window_title"));
            Dimension winDim = new Dimension(550, 230);
            this.setMinimumSize(winDim);
            this.setMaximumSize(winDim);
            this.setSize(winDim);
            this.setResizable(false);
            this.setLayout(null);
            this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            this.themesList = new JList(dirNames);
            this.themesList.setLocation(new Point(10, 10));
            this.themesList.setSize(new Dimension(100, 120));
            this.add(this.themesList);
            this.themesList.setSelectionMode(0);
            this.themesList.addListSelectionListener(this);
            this.gbl = new GridBagLayout();
            this.gbc = new GridBagConstraints();
            this.themePreview = getPreviewIcon(GUI.configFile.getProperty("THEME", "default"));
            this.result = "";
            this.themePreviewButton = new JButton(this.themePreview);
            this.themePreviewButton.setLocation(new Point(110, 10));
            this.themePreviewButton.setSize(new Dimension(420, 120));
            this.add(this.themePreviewButton);
            this.okButton = new JButton("OK");
            this.okButton.setLocation(new Point(175, 140));
            this.okButton.setSize(new Dimension(200, 50));
            this.add(this.okButton);
            this.okButton.addActionListener(this);
            this.setModal(true);
        }
        else
        {
            throw new IOException(Settings.lang("error_when_creating_theme_config_window"));
        }

    }

    private static String[] getThemeNames() throws IOException, URISyntaxException
    {
        URL themeUrl = JChessApp.class.getResource("theme/");
        if (themeUrl == null)
        {
            throw new IOException("Could not find theme resources on the classpath");
        }

        Set<String> themeNames = new TreeSet<>();
        if ("file".equals(themeUrl.getProtocol()))
        {
            File themeDirectory = new File(themeUrl.toURI());
            File[] themeDirectories = themeDirectory.listFiles(File::isDirectory);
            if (themeDirectories == null)
            {
                throw new IOException("Could not read theme directory: " + themeDirectory);
            }
            for (File themeDirectoryEntry : themeDirectories)
            {
                themeNames.add(themeDirectoryEntry.getName());
            }
        }
        else if ("jar".equals(themeUrl.getProtocol()))
        {
            JarURLConnection connection = (JarURLConnection) themeUrl.openConnection();
            connection.setUseCaches(false);
            String themeRoot = connection.getEntryName();
            if (!themeRoot.endsWith("/"))
            {
                themeRoot += "/";
            }
            try (JarFile jarFile = connection.getJarFile())
            {
                java.util.Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements())
                {
                    String entryName = entries.nextElement().getName();
                    if (entryName.startsWith(themeRoot))
                    {
                        String nestedPath = entryName.substring(themeRoot.length());
                        int separator = nestedPath.indexOf('/');
                        if (separator > 0)
                        {
                            themeNames.add(nestedPath.substring(0, separator));
                        }
                    }
                }
            }
        }
        else
        {
            throw new IOException("Unsupported theme resource protocol: " + themeUrl.getProtocol());
        }
        return themeNames.toArray(new String[0]);
    }

    private static ImageIcon getPreviewIcon(String themeName)
    {
        URL previewUrl = JChessApp.class.getResource("theme/" + themeName + "/images/Preview.png");
        if (previewUrl == null)
        {
            previewUrl = JChessApp.class.getResource("theme/default/images/Preview.png");
        }
        return (previewUrl != null) ? new ImageIcon(previewUrl) : new ImageIcon();
    }

    @Override
    public void valueChanged(ListSelectionEvent event)
    {
        if (event.getValueIsAdjusting() || this.themesList.getSelectedIndex() < 0)
        {
            return;
        }
        String element = this.themesList.getModel().getElementAt(this.themesList.getSelectedIndex()).toString();
        this.themePreview = getPreviewIcon(element);
        this.themePreviewButton.setIcon(this.themePreview);
    }

    /** Method wich is changing a pawn into queen, rook, bishop or knight
     * @param arg0 Capt information about performed action
     */
    public void actionPerformed(ActionEvent evt)
    {
        if (evt.getSource() == this.okButton)
        {
            Properties prp = GUI.getConfigFile();
            int element = this.themesList.getSelectedIndex();
            String name = this.themesList.getModel().getElementAt(element).toString();
            if (GUI.themeIsValid(name))
            {
                prp.setProperty("THEME", name);
                try (FileOutputStream fOutStr = new FileOutputStream(GUI.getConfigFilePath()))
                {
                    prp.store(fOutStr, null);
                }
                catch (java.io.IOException exc)
                {
                    JOptionPane.showMessageDialog(this, "Could not save theme configuration: " + exc.getMessage(),
                            Settings.lang("error_when_creating_theme_config_window"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                JOptionPane.showMessageDialog(this, Settings.lang("changes_visible_after_restart"));
                this.setVisible(false);

            }
            System.out.print(prp.getProperty("THEME"));
        }
    }
}
