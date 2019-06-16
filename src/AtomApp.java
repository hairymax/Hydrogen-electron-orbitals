import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;


public class AtomApp extends JFrame {
    private JPanel pnlMain;
    private JLabel lblName;
    private JButton btnStartRadial;
    private JButton btnStartSpherical;
    private JButton btnOpenBook;
    private SphericalApp sapp = new SphericalApp();
    private RadialApp rapp = new RadialApp();

    public AtomApp() {
        super("Атом Водорода");      // заголовок окна
        setContentPane(this.pnlMain); // область с содержимым формы (панель pnlMain)
        setBounds(50, 50, 100, 100);    // размер и положение окна
        // при закрытии окна EXIT_ON_CLOSE завершит приложение (процесс)
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        btnStartRadial.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (!rapp.isShowing()) { rapp.setVisible(true); rapp.pack(); }
            }
        });

        btnStartSpherical.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (!sapp.isShowing()) { sapp.setVisible(true); sapp.pack(); }
            }
        });

        btnOpenBook.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                try {
                    File file = new File("book.pdf");
                    if (file.exists()) Desktop.getDesktop().open(file);
                    else System.out.println("Файл не найден по указанному пути -> "+ file.getAbsolutePath());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) { // основной метод - main, обязателен
        AtomApp app = new AtomApp();    // создаем экземпляр приложения
        app.setVisible(true);   // запуск приложения
        app.pack();             // автоматическая компоновка элементов формы
    }
}