package battleship.gui;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import java.util.List;
import java.util.Arrays;
import java.util.Random;

public class BattleshipGUI {
    private final JFrame frame = new JFrame("Battleship");
    private final JButton[][] oceanButtons  = new JButton[10][10];
    private final JButton[][] targetButtons = new JButton[10][10];
    private final JToggleButton orientationToggle;
    private final JLabel instructionLabel;
    private final GameCoordinator coordinator;

    private static class ShipSpec {
        final String name;
        final int size;
        ShipSpec(String name, int size) {
            this.name = name;
            this.size = size;
        }
    }

    private final List<ShipSpec> shipSpecs = Arrays.asList(
        new ShipSpec("Carrier",    5),
        new ShipSpec("Battleship", 4),
        new ShipSpec("Cruiser",    3),
        new ShipSpec("Submarine",  3),
        new ShipSpec("Destroyer",  2)
    );

    private int shipIndex     = 0;
    private int playersPlaced = 0;
    private boolean placingPhase;
    private final boolean vsComputer;

    public BattleshipGUI() {
        int mode = JOptionPane.showOptionDialog(
            null,
            "Choose game mode:",
            "Mode Select",
            JOptionPane.DEFAULT_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            new String[]{ "Player vs Computer", 
                          "Player vs Player" },
            "Player vs Computer"
        );
        vsComputer   = (mode == 0);
        placingPhase = true;

        Player p1 = new Player("Player 1");
        Player p2 = vsComputer
                  ? new Player("Computer")
                  : new Player("Player 2");
        coordinator = new GameCoordinator(p1, p2);

        instructionLabel  = new JLabel("", SwingConstants.CENTER);
        orientationToggle = new JToggleButton("Horizontal");
        orientationToggle.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                orientationToggle.setText(
                  orientationToggle.isSelected()
                  ? "Vertical" : "Horizontal"
                );
            }
        });

        setupUI();
        updatePlacementInstruction();
        setGridEnabled(oceanButtons,  true);
        setGridEnabled(targetButtons, false);
        updateOceanGrid(
          coordinator.getCurrentPlayer().getBoard(), true
        );
    }

    private void setupUI() {
        frame.setLayout(new BorderLayout());

        JPanel top = new JPanel(new GridLayout(2,1));
        top.add(instructionLabel);
        top.add(orientationToggle);

        JPanel center = new JPanel(new GridLayout(1,2,10,0));
        center.add(makeLabeledGrid(oceanButtons, 
                                  "Ocean Grid", true));
        center.add(makeLabeledGrid(targetButtons,
                                  "Target Grid", false));

        frame.add(top,    BorderLayout.NORTH);
        frame.add(center, BorderLayout.CENTER);
        frame.setSize(800,700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private JPanel makeLabeledGrid(
        final JButton[][] buttons,
        String title,
        boolean isOcean
    ) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(
          new JLabel(title, SwingConstants.CENTER),
          BorderLayout.NORTH
        );

        JPanel grid = new JPanel(new GridLayout(11,11));
        grid.add(new JLabel(""));  // corner

        for (int c = 1; c <= 10; c++) {
            grid.add(new JLabel(
              String.valueOf(c),
              SwingConstants.CENTER
            ));
        }

        for (int r = 0; r < 10; r++) {
            grid.add(new JLabel(
              String.valueOf((char)('A' + r)),
              SwingConstants.CENTER
            ));
            for (int c = 0; c < 10; c++) {
                JButton btn = new JButton("~");
                btn.setOpaque(true);
                btn.setBackground(Color.WHITE);
                final int row = r, col = c;

                if (isOcean) {
                    btn.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            handlePlacement(row, col);
                        }
                    });
                } else {
                    btn.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent e) {
                            handleFire(row, col);
                        }
                    });
                }

                buttons[r][c] = btn;
                grid.add(btn);
            }
        }

        panel.add(grid, BorderLayout.CENTER);
        return panel;
    }

    private void handlePlacement(int row, int col) {
        if (!placingPhase) return;
        GameBoard board = coordinator.getCurrentPlayer().getBoard();
        ShipSpec s = shipSpecs.get(shipIndex);
        boolean v = orientationToggle.isSelected();
        if (!board.placeShip(s.name, s.size, row, col, v)) {
            JOptionPane.showMessageDialog(
              frame,
              "Invalid placement of " + s.name
            );
            return;
        }
        updateOceanGrid(board, true);
        shipIndex++;
        if (shipIndex < shipSpecs.size()) {
            updatePlacementInstruction();
        } else {
            playersPlaced++;
            shipIndex = 0;
            if (vsComputer && playersPlaced == 1) {
                autoPlaceComputer();
                playersPlaced++;
            }
            if (playersPlaced < 2) {
                coordinator.switchTurn();
                updatePlacementInstruction();
                updateOceanGrid(
                  coordinator.getCurrentPlayer().getBoard(),
                  true
                );
                JOptionPane.showMessageDialog(
                  frame,
                  coordinator.getCurrentPlayer().getName()
                  + ", place your ships."
                );
            } else {
                placingPhase = false;
                coordinator.switchTurn();
                instructionLabel.setText(
                  coordinator.getCurrentPlayer().getName()
                  + ": Your turn to fire"
                );
                orientationToggle.setEnabled(false);
                setGridEnabled(oceanButtons,  false);
                setGridEnabled(targetButtons, true);
                updateOceanGrid(
                  coordinator.getCurrentPlayer().getBoard(),
                  true
                );
                updateTargetGrid(
                  coordinator.getOpponent().getBoard()
                );
                JOptionPane.showMessageDialog(
                  frame,
                  "All ships placed. "
                  + coordinator.getCurrentPlayer().getName()
                  + " begins firing!"
                );
            }
        }
    }

    private void handleFire(int row, int col) {
        if (placingPhase) return;
        Player cur = coordinator.getCurrentPlayer();
        Player opp = coordinator.getOpponent();
        GameBoard.FireResult res =
          opp.getBoard().fireAt(row, col);

        // disable only this button
        targetButtons[row][col].setEnabled(false);
        updateTargetGrid(opp.getBoard());
        updateOceanGrid(cur.getBoard(), true);

        if (res.hit) {
            if (res.sunk) {
                if (opp.getBoard().allShipsSunk()) {
                    JOptionPane.showMessageDialog(
                      frame,
                      "You sank the last ship! You win!"
                    );
                    System.exit(0);
                } else {
                    JOptionPane.showMessageDialog(
                      frame,
                      "You sank opponent's " + res.shipName + "!"
                    );
                }
            } else {
                JOptionPane.showMessageDialog(frame, "Hit!");
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Miss!");
        }

        // swap turns
        coordinator.switchTurn();
        instructionLabel.setText(
          coordinator.getCurrentPlayer().getName()
          + ": Your turn to fire"
        );
        updateOceanGrid(
          coordinator.getCurrentPlayer().getBoard(), true
        );
        updateTargetGrid(
          coordinator.getOpponent().getBoard()
        );
    }

    private void updatePlacementInstruction() {
        ShipSpec s = shipSpecs.get(shipIndex);
        instructionLabel.setText(
          coordinator.getCurrentPlayer().getName()
          + ": Place " + s.name
          + " (" + s.size + ")"
        );
    }

    private void updateOceanGrid(GameBoard b, boolean reveal) {
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                JButton btn = oceanButtons[r][c];
                char cell = b.getCell(r, c);
                if (cell == 'X')        btn.setBackground(Color.RED);
                else if (cell == 'M')   btn.setBackground(Color.WHITE);
                else if (reveal
                         && Character.isLetter(cell))
                                        btn.setBackground(Color.LIGHT_GRAY);
                else                    btn.setBackground(Color.WHITE);
                btn.setText("~");
            }
        }
    }

    private void updateTargetGrid(GameBoard b) {
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                JButton btn = targetButtons[r][c];
                char cell = b.getCell(r, c);
                if (cell == 'X') {
                    btn.setBackground(Color.RED);
                    btn.setEnabled(false);
                } else if (cell == 'M') {
                    btn.setBackground(Color.WHITE);
                    btn.setEnabled(false);
                } else {
                    btn.setBackground(Color.WHITE);
                    btn.setEnabled(true);
                }
                btn.setText("~");
            }
        }
    }

    private void setGridEnabled(
      JButton[][] buttons, boolean on
    ) {
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                buttons[r][c].setEnabled(on);
            }
        }
    }

    private void autoPlaceComputer() {
        GameBoard b = coordinator.getCurrentPlayer().getBoard();
        Random rnd = new Random();
        for (ShipSpec s : shipSpecs) {
            boolean ok = false;
            while (!ok) {
                int rr = rnd.nextInt(10);
                int cc = rnd.nextInt(10);
                boolean v = rnd.nextBoolean();
                ok = b.placeShip(s.name, s.size, rr, cc, v);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new BattleshipGUI();
            }
        });
    }
}