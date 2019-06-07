import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.*;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord2d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.*;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.lang.Math.*;

import static java.lang.Math.*;

public class AtomApp extends JFrame {
    private JPanel pnlMain;
    private JPanel pnlControl;
    private JPanel pnlOutput;
    private JTextField txtResult;
    private JLabel lblName;
    private JPanel pnlChart;

    public AtomApp() {
        super("Атом Водорода");      // заголовок окна
        setContentPane(this.pnlMain); // область с содержимым формы (панель pnlMain)
        setBounds(-1000, 100, 800, 800);    // размер и положение окна
        // при закрытии окна EXIT_ON_CLOSE завершит приложение (процесс)
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        int l = 2; // квантовые числа
        int m = 1; // TODO надо реализовать возможность выбирать пользователю

        SphericalHarmonic Ylm = new SphericalHarmonic(l,m);
        float Y;
        float theta = 0;        // зенит (0 <= theta < PI)
        float phi = 0;          // азимут (0 <= phi <= 2*PI)
        int splits_count = 180; // число разбиений интервала углов от 0 до PI
        float angle_step = (float) PI/splits_count;
        int size = 2*splits_count*(splits_count+1); // число точек для построения
        float x, y, z;      // координаты
        float a;            // прозрачность

        Coord3d[] points = new Coord3d[size];
        Color[]   colors = new Color[size];
        int index = 0;
        float start_theta = (l%2==1) ? -(float)PI/2 : 0; // для нечётных l смещаем начало интервала для угла theta
        for(int i=0; i<=splits_count; i++){ // цикл по theta
            theta = start_theta + i*angle_step;
            for(int j=0; j<2*splits_count; j++) { // цикл по phi
                phi = j*angle_step;
                Y = (float)Ylm.eval(theta,phi).getReal();   // вещественная часть
                //Y = (float)Ylm.eval(theta, phi).abs();    // модуль
                x = (float)(Y*sin(theta)*cos(phi));         // перевод из
                y = (float)(Y*sin(theta)*sin(phi));             // полярных координат
                z = (float)(Y*cos(theta));                          // в декартовы
                points[index] = new Coord3d(x, y, z);
                a = 0.5f;
                if (Y < 0) colors[index] = new Color(1, 0, 0, a);
                else colors[index] = new Color(0, 0, 1, a);
                index++;
            }
        }

        Scatter scatter = new Scatter(points, colors);

        Chart chart = AWTChartComponentFactory.chart(Quality.Advanced, "swing");

        //chart.addMouseCameraController();
        chart.add(scatter);
        chart.getView().setBoundManual(new BoundingBox3d(new Coord3d(0,0,0),1.0F));
        chart.getView().setViewPoint(new Coord3d(
                Math.toRadians(45),     // поворот отностительно оси z
                Math.toRadians(30),     // наклон оси z
                0));

        pnlChart.setPreferredSize(new Dimension(600, 600)); // размер по умолчанию для графика
        pnlChart.setLayout(new java.awt.BorderLayout());
        pnlChart.add((JPanel)chart.getCanvas(), BorderLayout.CENTER);

        // поворот графика при перемещении мыши после клика в области графика
        pnlChart.addMouseMotionListener(new MouseMotionAdapter() {
            int x = pnlChart.getWidth()/2;
            int y = pnlChart.getHeight()/2;
            @Override
            public void mouseDragged(MouseEvent e) {
                super.mouseDragged(e);
                float dx = (float) toRadians(signum(e.getX() - x));
                float dy = (float) toRadians(signum(e.getY() - y));
                x = e.getX();
                y = e.getY();
                txtResult.setText(x + ", " + y + ";      " + dx + ", " + dy);
                chart.getView().rotate(new Coord2d(dx, dy), true);
                //setViewPoint(new Coord3d(rotX, rotY, 0), true);
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