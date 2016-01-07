package de.adrodoc55.minecraft.mpl.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.undo.UndoManager;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.Token;

import de.adrodoc55.minecraft.mpl.antlr.MplLexer;

public class MplEditor extends JComponent {

    private static final Pattern INSERT_PATTERN = Pattern
            .compile("\\$\\{[^{}]*+\\}");

    private static final long serialVersionUID = 1L;

    private static JFileChooser chooser;

    private static JFileChooser getFileChooser() {
        if (chooser == null) {
            chooser = new JFileChooser();
        }
        return chooser;
    }

    public static JFileChooser getDirChooser() {
        JFileChooser chooser = getFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setFileFilter(null);
        FileFilter filter = getFileFilter();
        chooser.removeChoosableFileFilter(filter);
        return chooser;
    }

    public static JFileChooser getMplChooser() {
        JFileChooser chooser = getFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileFilter filter = getFileFilter();
        chooser.setFileFilter(filter);
        chooser.addChoosableFileFilter(filter);
        return chooser;
    }

    private static FileFilter filter;

    private static FileFilter getFileFilter() {
        if (filter == null) {
            filter = new FileNameExtensionFilter(
                    "Minecraft Programming Language", new String[] { "mpl" });
        }
        return filter;
    }

    private TabCloseComponent tabComponent;
    private File file;
    private boolean unsavedChanges;

    private JScrollPane scrollPane;
    private JTextPane textPane;
    private UndoManager undoManager;

    private Style lowFocusKeywordStyle;
    private Style highFocusKeywordStyle;
    private Style impulseStyle;
    private Style chainStyle;
    private Style repeatStyle;
    // private Style conditionalStyle;
    private Style needsRedstoneStyle;
    private Style commentStyle;
    private Style insertStyle;
    private Style identifierStyle;

    public MplEditor() {
        setLayout(new BorderLayout());
        add(getScrollPane(), BorderLayout.CENTER);
    }

    public MplEditor(File file) throws IOException {
        this();
        this.file = file;
        if (file != null) {
            byte[] bytes = Files.readAllBytes(file.toPath());
            String content = new String(bytes);
            getTextPane().setText(content);
            recolor();
        }
    }

    public TabCloseComponent getTabComponent() {
        return tabComponent;
    }

    public void setTabComponent(TabCloseComponent tabComponent) {
        this.tabComponent = tabComponent;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
        if (tabComponent != null) {
            tabComponent.setTitle(getTitle());
        }
    }

    public String getTitle() {
        return file != null ? file.getName() : "new.mpl";
    }

    public boolean getUnsavedChanges() {
        return unsavedChanges;
    }

    public void setUnsavedChanges(boolean unsavedChanges) {
        this.unsavedChanges = unsavedChanges;
        getTabComponent().setUnsavedChanges(unsavedChanges);
    }

    /**
     * Saves the changes to this Editor's File, overwriting the content. The
     * file and all it's parent directories will be created if necassary. If the
     * file is null a JFileChooser dialog will be opened.<br>
     * If an IOException is thrown the user will be informed via a JOptionPane.
     *
     */
    public void save() {
        if (file == null) {
            saveUnder();
        } else {
            try {
                file.getParentFile().mkdirs();
                byte[] bytes = getTextPane().getText().getBytes();
                Files.write(file.toPath(), bytes);
                setUnsavedChanges(false);
            } catch (IOException ex) {
                String path = file != null ? file.getPath() : null;
                JOptionPane.showMessageDialog(chooser,
                        "An Exception occured while trying to save to '" + path
                                + "'. Exception: " + ex.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void saveUnder() {
        JFileChooser chooser = getMplChooser();
        int userAction = chooser.showSaveDialog(this);
        if (userAction != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        if (file.exists()) {
            int overwrite = JOptionPane.showOptionDialog(chooser, "The File '"
                    + file.getName()
                    + "' already exists and will be overwritten.", "Save...",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, null, null);
            if (overwrite != JOptionPane.OK_OPTION) {
                saveUnder();
                return;
            }
        }
        setFile(file);
        save();
    }

    private void recolor() {
        String text = getTextPane().getText().replace("\r\n", "\n")
                .replace("\r", "\n");
        MplLexer lexer = new MplLexer(new ANTLRInputStream(text));
        loop: while (true) {
            Token token = lexer.nextToken();
            switch (token.getType()) {
            case MplLexer.EOF:
                break loop;
            case MplLexer.IMPULSE:
                Style style = getImpulseStyle();
                styleToken(token, style);
                break;
            case MplLexer.CHAIN:
                styleToken(token, getChainStyle());
                break;
            case MplLexer.REPEAT:
                styleToken(token, getRepeatStyle());
                break;
            case MplLexer.UNCONDITIONAL:
            case MplLexer.ALWAYS_ACTIVE:
                styleToken(token, getLowFocusKeywordStyle());
                break;
            case MplLexer.NEEDS_REDSTONE:
                styleToken(token, getNeedsRedstoneStyle());
                break;
            case MplLexer.COMMENT:
                styleToken(token, getCommentStyle());
                break;
            case MplLexer.CONDITIONAL:
            case MplLexer.INVERT:
            case MplLexer.INCLUDE:
            case MplLexer.IMPORT:
            case MplLexer.INSTALL:
            case MplLexer.PROCESS:
            case MplLexer.PROJECT:
            case MplLexer.START:
            case MplLexer.STOP:
            case MplLexer.NOTIFY:
            case MplLexer.WAITFOR:
            case MplLexer.SKIP:
            case MplLexer.UNINSTALL:
                styleToken(token, getHighFocusKeywordStyle());
                break;
            case MplLexer.IDENTIFIER:
                styleToken(token, getIdentifierStyle());
                break;
            case MplLexer.COMMAND:
                styleToken(token, getDefaultStyle());
                Matcher insert = INSERT_PATTERN.matcher(token.getText());
                while (insert.find()) {
                    int tokenStart = token.getStartIndex();
                    int start = tokenStart + insert.start();
                    int stop = token.getStartIndex() + insert.end();
                    styleToken(start, stop, getInsertStyle());
                }
                break;

            default:
                styleToken(token, getDefaultStyle());
            }

        }
    }

    private void styleToken(Token token, Style style) {
        styleToken(token.getStartIndex(), token.getStopIndex() + 1, style);
    }

    private void styleToken(int start, int stop, Style style) {
        int length = stop - start;
        getStyledDocument().setCharacterAttributes(start, length, style, true);
    }

    private JScrollPane getScrollPane() {
        if (scrollPane == null) {
            scrollPane = new JScrollPane(getTextPane());
        }
        return scrollPane;
    }

    private CancelableRunable runnable;

    private JTextPane getTextPane() {
        if (textPane == null) {
            textPane = new JTextPane();
            textPane.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    if (
                    // e.getKeyChar() == KeyEvent.CHAR_UNDEFINED||
                    // e.getKeyCode() == KeyEvent.VK_UNDEFINED
                    e.getKeyChar() == (char) 19) {
                        return;
                    }
                    // System.out.println("Keycode: " + e.getKeyCode()
                    // + " Keycharcode: " + (int) e.getKeyChar()
                    // + " Keychar: " + e.getKeyChar());
                    setUnsavedChanges(true);
                    if (runnable != null) {
                        runnable.cancel();
                    }
                    runnable = new CancelableRunable(new Runnable() {
                        @Override
                        public void run() {
                            recolor();
                        }
                    });
                    EventQueue.invokeLater(runnable);
                }
            });
            UndoManager undoManager = getUndoManager();
            textPane.getDocument().addUndoableEditListener(undoManager);
            int ctrl = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
            textPane.getInputMap().put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_Y, ctrl), "redo");
            textPane.getInputMap().put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_Z, ctrl), "undo");
            textPane.getActionMap().put("redo", new RedoAction(undoManager));
            textPane.getActionMap().put("undo", new UndoAction(undoManager));

            textPane.getInputMap().put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_S, ctrl), "save");
            textPane.getActionMap().put("save", new AbstractAction() {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(ActionEvent e) {
                    save();
                }
            });
        }
        return textPane;
    }

    private UndoManager getUndoManager() {
        if (undoManager == null) {
            undoManager = new UndoManagerFix();
        }
        return undoManager;
    }

    private StyledDocument getStyledDocument() {
        return getTextPane().getStyledDocument();
    }

    private Style getDefaultStyle() {
        Style style = getStyledDocument().getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setFontSize(style, 12);
        return style;
    }

    private Style getLowFocusKeywordStyle() {
        if (lowFocusKeywordStyle == null) {
            lowFocusKeywordStyle = getStyledDocument().addStyle(
                    "lowFocusKeyword", getDefaultStyle());
            StyleConstants.setBold(lowFocusKeywordStyle, true);
            StyleConstants.setForeground(lowFocusKeywordStyle, new Color(128,
                    128, 128));
        }
        return lowFocusKeywordStyle;
    }

    private Style getHighFocusKeywordStyle() {
        if (highFocusKeywordStyle == null) {
            highFocusKeywordStyle = getStyledDocument().addStyle(
                    "highFocusKeyword", getDefaultStyle());
            StyleConstants.setBold(highFocusKeywordStyle, true);
            StyleConstants.setForeground(highFocusKeywordStyle, new Color(128,
                    0, 0));
        }
        return highFocusKeywordStyle;
    }

    private Style getImpulseStyle() {
        if (impulseStyle == null) {
            impulseStyle = getStyledDocument().addStyle("impulse",
                    getDefaultStyle());
            StyleConstants.setBold(impulseStyle, true);
            StyleConstants.setForeground(impulseStyle, new Color(255, 127, 80));
        }
        return impulseStyle;
    }

    private Style getChainStyle() {
        if (chainStyle == null) {
            chainStyle = getStyledDocument().addStyle("chain",
                    getDefaultStyle());
            StyleConstants.setBold(chainStyle, true);
            StyleConstants.setForeground(chainStyle, new Color(60, 179, 113));
        }
        return chainStyle;
    }

    private Style getRepeatStyle() {
        if (repeatStyle == null) {
            repeatStyle = getStyledDocument().addStyle("repeat",
                    getDefaultStyle());
            StyleConstants.setBold(repeatStyle, true);
            StyleConstants.setForeground(repeatStyle, new Color(106, 90, 205));
        }
        return repeatStyle;
    }

    // private Style getConditionalStyle() {
    // if (conditionalStyle == null) {
    // conditionalStyle = getStyledDocument().addStyle("conditional",
    // getDefaultStyle());
    // StyleConstants.setBold(conditionalStyle, true);
    // StyleConstants.setForeground(conditionalStyle, new Color(169, 169, 169));
    // }
    // return conditionalStyle;
    // }

    private Style getNeedsRedstoneStyle() {
        if (needsRedstoneStyle == null) {
            needsRedstoneStyle = getStyledDocument().addStyle("needsRedstone",
                    getDefaultStyle());
            StyleConstants.setBold(needsRedstoneStyle, true);
            StyleConstants.setForeground(needsRedstoneStyle, Color.RED);
        }
        return needsRedstoneStyle;
    }

    private Style getCommentStyle() {
        if (commentStyle == null) {
            commentStyle = getStyledDocument().addStyle("comment",
                    getDefaultStyle());
            StyleConstants.setForeground(commentStyle, new Color(0, 128, 0));
        }
        return commentStyle;
    }

    private Style getInsertStyle() {
        if (insertStyle == null) {
            insertStyle = getStyledDocument().addStyle("insert",
                    getDefaultStyle());
            StyleConstants.setForeground(insertStyle, new Color(128, 0, 0));
            StyleConstants.setBackground(insertStyle, new Color(240, 230, 140));
        }
        return insertStyle;
    }

    private Style getIdentifierStyle() {
        if (identifierStyle == null) {
            identifierStyle = getStyledDocument().addStyle("identifier",
                    getDefaultStyle());
            StyleConstants.setBold(identifierStyle, true);
            StyleConstants.setForeground(identifierStyle,
                    new Color(128, 128, 0));
        }
        return identifierStyle;
    }

}
