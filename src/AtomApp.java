import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord2d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import static java.lang.Math.*;

public class AtomApp extends JFrame {
    private JPanel pnlMain;
    private JPanel pnlControl;
    private JPanel pnlOutput;
    private JTextField txtResult;
    private JLabel lblName;
    private JPanel pnlChart;
    private JButton btnChart;
    private JLabel lbll;
    private JLabel lblm;
    private JComboBox cbm;
    private JComboBox cbl;
    private JCheckBox chbmod;
    private JCheckBox chbvesch;

    private int m = 0, l = 0, mod = 1, vesch = 0;

    public AtomApp() {
        super("Атом Водорода");      // заголовок окна
        setContentPane(this.pnlMain); // область с содержимым формы (панель pnlMain)
        setBounds(100, 100, 800, 800);    // размер и положение окна
        // при закрытии окна EXIT_ON_CLOSE завершит приложение (процесс)
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // добавление квантовых чисел l
        for (int i = 0; i <= 5; i++) {
            cbl.addItem(i);
            cbl.setSelectedItem(0);
        }

        // добавление m, реагирование при изменении l
        cbl.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cbm.removeAllItems(); // отчистка
                l = (int) cbl.getSelectedItem(); // получение значения l

                for (int i = -l; i <= l; i++) {
                    cbm.addItem(i);
                }
                cbm.setSelectedItem(0);
            }
        });
        cbm.addItem(0);
        cbm.setSelectedItem(0);

        // галочка модуля
        chbmod.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (chbmod.isSelected()) {
                    mod = 1;
                } else mod = 0;
            }
        });

        // галочка вещь.
        chbvesch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (chbvesch.isSelected()) {
                    vesch = 1;
                } else vesch = 0;
            }
        });

        // панель графика
        pnlChart.setPreferredSize(new Dimension(600, 600)); // размер по умолчанию для графика
        pnlChart.setLayout(new java.awt.BorderLayout());

        //кнопка расчёта
        btnChart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pnlChart.removeAll();
                l = (int) cbl.getSelectedItem();
                m = (int) cbm.getSelectedItem();
                SphericalHarmonic Ylm = new SphericalHarmonic(l, m);
                float Y;
                float theta = 0;        // зенит (0 <= theta < PI)
                float phi = 0;          // азимут (0 <= phi <= 2*PI)
                int splits_theta = 180;//180; // число разбиений интервала углов theta (от 0 до PI)
                int splits_phi = 360; // число разбиений интервала углов phi (от 0 до 2*PI)
                float step_theta = (float) (PI / splits_theta);
                float step_phi = (float) (2 * PI / splits_phi);
                int size = splits_phi * (splits_theta + 1); // число точек для построения
                float x, y, z;      // координаты
                float a;            // прозрачность

                Coord3d[] points = new Coord3d[size];
                Color[] colors = new Color[size];

                int index = 0;
                for (int i = 0; i <= splits_theta; i++) { // цикл по theta
                    theta = i * step_theta; // -(float)PI/2
                    for (int j = 0; j < splits_phi; j++) { // цикл по phi
                        phi = j * step_phi;

                        // если галочка на "Модуль", то берёся это уравнение
                        if (mod == 1){
                            Y = (float)Ylm.eval(theta, phi).abs(); // модуль
                        }
                        // если галочка на "Вещественная часть", то берёся это уравнение
                        else if (vesch == 1) {
                            Y = (float) Ylm.eval(theta, phi).getReal(); // вещественная часть
                        }
                        // иначе нули
                        else Y = 0;

                        x = (float) (Y * sin(theta) * cos(phi)); // перевод из
                        y = (float) (Y * sin(theta) * sin(phi));         // полярных координат
                        z = (float) (Y * cos(theta));                          // в декартовы
                        points[index] = new Coord3d(x, y, z);
                        //points[index] = new Coord3d(phi, theta, Y).cartesian();
                        a = 0.5f; // прозрачность
                        if (Y < 0) colors[index] = new Color(1, 0, 0, a);
                        else colors[index] = new Color(0, 0, 1, a);
                        index++;
                    }
                }

                Scatter scatter = new Scatter(points, colors);

                Chart chart = AWTChartComponentFactory.chart(Quality.Advanced, "swing");
                chart.add(scatter);
                chart.getView().setBoundManual(new BoundingBox3d(new Coord3d(0, 0, 0), 1.0F));
                chart.getView().setViewPoint(new Coord3d(
                        Math.toRadians(45),     // поворот отностительно оси z
                        Math.toRadians(30),     // наклон оси z
                        0));

                pnlChart.add((JPanel) chart.getCanvas(), BorderLayout.CENTER);

                // поворот графика при перемещении мыши после клика в области графика
                pnlChart.addMouseMotionListener(new MouseMotionAdapter() {
                    int x = pnlChart.getWidth() / 2;
                    int y = pnlChart.getHeight() / 2;
                    @Override
                    public void mouseDragged(MouseEvent e) {
                        super.mouseDragged(e);
                        float dx = (float) toRadians(2 * signum(e.getX() - x));
                        float dy = (float) toRadians(2 * signum(e.getY() - y));
                        x = e.getX();
                        y = e.getY();
                        chart.getView().rotate(new Coord2d(dx, dy), true);
                    }
                });
                pnlChart.updateUI();
            }
        });
    }

    public static void main(String[] args) { // основной метод - main, обязателен
        AtomApp app = new AtomApp();    // создаем экземпляр приложения
        app.setVisible(true);   // запуск приложения
        app.pack();             // автоматическая компоновка элементов формы
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}