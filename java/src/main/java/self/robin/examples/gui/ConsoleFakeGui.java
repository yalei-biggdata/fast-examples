package self.robin.examples.gui;

import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

/**
 * ...
 * lanterna http://code.google.com/p/lanterna/
 * Java Curses Library   http://sourceforge.net/projects/javacurses/
 *
 * @author Robin-Li
 * @since: 2023-05-02 15:14
 */
public class ConsoleFakeGui {

    public static void main(String[] args) throws Exception {
        Terminal terminal = new DefaultTerminalFactory().createTerminal();
        Screen screen = new TerminalScreen(terminal);

        String s = "Hello World!";
        TextGraphics tGraphics = screen.newTextGraphics();

        screen.startScreen();
        screen.clear();

        tGraphics.putString(10, 10, s);
        screen.refresh();

        screen.readInput();
        screen.stopScreen();
    }
}
