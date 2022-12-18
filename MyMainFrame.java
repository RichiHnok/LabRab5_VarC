import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.event.MenuEvent;
import javax.swing.JMenuItem;
import javax.swing.event.MenuListener;

public class MyMainFrame extends JFrame{
     
    public static void main(String[] args){
        MyMainFrame frame = new MyMainFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static final int WIDTH = 720;
    private static final int HEIGHT = 400;

    private JMenuItem showAxisMenuItem;
    private JMenuItem showDotsMenuItem;
    private JMenuItem showIntegralsMenuItem;
    private JMenuItem resetGraphicsMenuItem;
    private JMenuItem saveToGraphicsMenuItem;

    private JFileChooser fileChooser = null;
    private boolean fileLoaded;

    private MyGraphicsDisplay graphicsDisplay = new MyGraphicsDisplay();

    public MyMainFrame(){
        super("Построение графиков функции на основе заранее подготовленного файла");

        Toolkit kit = Toolkit.getDefaultToolkit();
        setSize(WIDTH, HEIGHT);
        setLocation((kit.getScreenSize().width - WIDTH)/2, (kit.getScreenSize().height - HEIGHT)/2);
    //@ Реализация основного функционала программы
        //^ Меню
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        
        JMenu fileMenu = new JMenu("Файл");
        JMenu graphicsMenu = new JMenu("График");

        menuBar.add(fileMenu);
        menuBar.add(graphicsMenu);

        Action openGraphicsAction = new AbstractAction("Открыть файл"){
            public void actionPerformed(ActionEvent event){
                if(fileChooser == null){
                    fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("."));
                }
                if(fileChooser.showOpenDialog(MyMainFrame.this) == JFileChooser.APPROVE_OPTION){
                    openGraphics(fileChooser.getSelectedFile());
                }
            }
        };

        fileMenu.add(openGraphicsAction);

        menuBar.add(graphicsMenu);

        Action showAxisAction = new AbstractAction("Показать оси координат"){
            public void actionPerformed(ActionEvent event){
                graphicsDisplay.setShowAxis(showAxisMenuItem.isSelected());
            }
        };

        showAxisMenuItem = new JCheckBoxMenuItem(showAxisAction);
        graphicsMenu.add(showAxisMenuItem);

        showAxisMenuItem.setSelected(true);

        Action showDotsAction = new AbstractAction("Показать маркеры точек"){
            public void actionPerformed(ActionEvent event){
                graphicsDisplay.setShowDots(showDotsMenuItem.isSelected());
            }
        };

        showDotsMenuItem = new JCheckBoxMenuItem(showDotsAction);
        graphicsMenu.add(showDotsMenuItem);

        showDotsMenuItem.setSelected(true);
        graphicsMenu.addMenuListener(new GraphicsMenuListener());

        Action showIntegralsAction = new AbstractAction("Интегралы"){
            public void actionPerformed(ActionEvent event){
                graphicsDisplay.setShowIntegrals(showIntegralsMenuItem.isSelected());
            }
        };

        showIntegralsMenuItem = new JCheckBoxMenuItem(showIntegralsAction);
        graphicsMenu.add(showIntegralsMenuItem);

        showIntegralsMenuItem.setSelected(false);
        graphicsMenu.addMenuListener(new GraphicsMenuListener());
        Action resetGraphicsAction = new AbstractAction("Отменить все изменения") {
            public void actionPerformed(ActionEvent event){
                MyMainFrame.this.graphicsDisplay.reset();
            }
        };

        resetGraphicsMenuItem = fileMenu.add(resetGraphicsAction);
        resetGraphicsMenuItem.setEnabled(false);

        Action saveToGraphicsAction = new AbstractAction("Сохранить изменения"){
            public void actionPerformed(ActionEvent event){
                if(fileChooser == null){
                    fileChooser = new JFileChooser();
                    fileChooser = new JFileChooser();
                }
                if(fileChooser.showSaveDialog(MyMainFrame.this) == JFileChooser.APPROVE_OPTION){
                    saveToGraphicsFile(fileChooser.getSelectedFile());
                }
            }
        };

        saveToGraphicsMenuItem = fileMenu.add(saveToGraphicsAction);
        saveToGraphicsMenuItem.setEnabled(false);

        getContentPane().add(graphicsDisplay, BorderLayout.CENTER);
    //@
    }

    private class GraphicsMenuListener implements MenuListener{
        public void menuSelected(MenuEvent event){
            showAxisMenuItem.setEnabled(fileLoaded);
            showDotsMenuItem.setEnabled(fileLoaded);
            showIntegralsMenuItem.setEnabled(fileLoaded);
            resetGraphicsMenuItem.setEnabled(fileLoaded);
            saveToGraphicsMenuItem.setEnabled(fileLoaded);
        }

        public void menuDeselected(MenuEvent event){}

        public void menuCanceled(MenuEvent event){}
    }

    protected void saveToGraphicsFile(File selectedFile){
        try(DataOutputStream out = new DataOutputStream(new FileOutputStream(selectedFile))){
            for(int i = 0; i < graphicsDisplay.getData().size(); i++){
                out.writeDouble((Double)graphicsDisplay.getData().get(i)[0]);
                out.writeDouble((Double)graphicsDisplay.getData().get(i)[1]);
            }
        }catch(Exception ex){}
    }

    protected void openGraphics(File selectedFile){
        try{
            DataInputStream in = new DataInputStream(new FileInputStream(selectedFile));

            ArrayList<Double[]> graphicsData = new ArrayList<>();

            while(in.available() > 0){
                Double x = in.readDouble();
                Double y = in.readDouble();
                graphicsData.add( new Double[]{x, y});
                
            }
            if(graphicsData != null && graphicsData.size() > 0){
                fileLoaded = true;
                graphicsDisplay.showGraphics(graphicsData);
                this.resetGraphicsMenuItem.setEnabled(true);
                saveToGraphicsMenuItem.setEnabled(true);
            }

            in.close();
        }catch(FileNotFoundException ex){
            JOptionPane.showMessageDialog(
                MyMainFrame.this,
                "Указанный файл не найден",
                "Ошибка загрузки данных", 
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }catch(IOException ex){
            JOptionPane.showMessageDialog(
                MyMainFrame.this,
                "Ошибка чтения координат точек из файла",
                "Ошибка загрузки данных",
                JOptionPane.WARNING_MESSAGE
            );
            return;
        }
    }
}