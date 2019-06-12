import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.colormaps.ColorMapRainbowNoBorder;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord2d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import static java.lang.Math.*;

public class AtomApp extends JFrame {
    private JPanel pnlMain;
    private JPanel pnlControl;
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
    private JLabel lbllm;
    private JLabel lblwhat;
    private JLabel lblpossib;
    private JSpinner spnThetaMin;
    private JSpinner spnThetaMax;
    private JSpinner spnPhiMin;
    private JSpinner spnPhiMax;
    private JCheckBox chbshowrange;

    private Chart chart = AWTChartComponentFactory.chart(Quality.Advanced, "swing");
    private int m = 0, l = 0;
    private double startTheta = 0, finalTheta = PI, startPhi = 0, finalPhi = 2*PI;
    private SphericalHarmonic Ylm;

    private Scatter GetYlmScatter(SphericalHarmonic Ylm, boolean modSelected, boolean showIntRange){
        float Y;
        float theta = 0;        // зенит (0 <= theta < PI)
        float phi = 0;          // азимут (0 <= phi <= 2*PI)
        int splits_theta = 180; // 180; // число разбиений интервала углов theta (от 0 до PI)
        int splits_phi = 360;   // число разбиений интервала углов phi (от 0 до 2*PI)
        float step_theta = (float) (PI / splits_theta);
        float step_phi = (float) (2 * PI / splits_phi);
        int size = splits_phi * (splits_theta + 1); // число точек для построения
        float x, y, z;      // координаты

        Coord3d[] points = new Coord3d[size];
        Color[] colors = new Color[size];

        int index = 0;
        for (int i = 0; i <= splits_theta; i++) { // цикл по theta
            theta = i * step_theta; // -(float)PI/2
            for (int j = 0; j < splits_phi; j++) { // цикл по phi
                phi = j * step_phi;

                // В зависимости от того, какая галочка выбрана, строим
                if (modSelected) Y = (float) Ylm.eval(theta, phi).abs(); // модуль
                else Y = (float) Ylm.eval(theta, phi).getReal(); // вещественная часть

                x = (float) (Y * sin(theta) * cos(phi)); // перевод из
                y = (float) (Y * sin(theta) * sin(phi));         // полярных координат
                z = (float) (Y * cos(theta));                          // в декартовы
                points[index] = new Coord3d(x, y, z);

                // В зависимости от того, какая галочка выбрана, задаём цвет
                if (showIntRange && theta >= startTheta && theta <= finalTheta && phi >= startPhi && phi <= finalPhi){
                    colors[index] = Color.BLACK;
                } else {
                    if (modSelected) {
                        Color col = new ColorMapRainbowNoBorder()
                                    .getColor(0,0, Ylm.eval(theta, phi).getArgument(), -PI, PI);
                        colors[index] = new Color(col.r,col.g,col.b,0.5f);
                    }
                    else // для вещественной части: синий значит "+", красный значит "-"
                        colors[index] = (Y < 0) ? new Color(1,0,0,0.5f)
                                                : new Color(0,0,1,0.5f);
                }
                index++;
            }
        }
        return new Scatter(points, colors, 1.2f);
    }

    private void UpdateChart(boolean showIntRange){
        // получаем обновлённый массив данных
        chart = AWTChartComponentFactory.chart(Quality.Advanced, "swing");
        chart.add(GetYlmScatter(Ylm, chbmod.isSelected(), showIntRange));
        // настройка видовой области
        chart.getView().setBoundManual(new BoundingBox3d(new Coord3d(0, 0, 0), 1.0F));
        chart.getView().setViewPoint(new Coord3d(
                Math.toRadians(45),     // поворот отностительно оси z
                Math.toRadians(30),     // наклон оси z
                0));

        pnlChart.removeAll();
        pnlChart.add((JPanel) chart.getCanvas(), BorderLayout.CENTER);
        pnlChart.updateUI();
    }

    public AtomApp() {
        super("Атом Водорода");      // заголовок окна
        setContentPane(this.pnlMain); // область с содержимым формы (панель pnlMain)
        setBounds(100, 100, 800, 800);    // размер и положение окна
        // при закрытии окна EXIT_ON_CLOSE завершит приложение (процесс)
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // панель графика
        pnlChart.setPreferredSize(new Dimension(600, 600)); // размер по умолчанию для графика
        pnlChart.setLayout(new java.awt.BorderLayout());

        // добавление квантовых чисел l
        for (int i = 0; i <= 5; i++) cbl.addItem(i);
        cbl.setSelectedItem(0);
        cbm.addItem(0);
        cbm.setSelectedItem(0);
        chbmod.setSelected(true);
        Ylm = new SphericalHarmonic(l, m);
        UpdateChart(false);

        spnThetaMin.setValue(0); spnThetaMax.setValue(180);
        spnPhiMin.setValue(0); spnPhiMax.setValue(360);

        // ДАЛЕЕ ОБРАБОТЧИКИ СОБЫТИЙ

        // добавление m, реагирование при изменении l
        cbl.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                cbm.removeAllItems(); // очистка
                l = (int) cbl.getSelectedItem(); // получение значения l
                for (int i = -l; i <= l; i++) cbm.addItem(i);
                cbm.setSelectedItem(0);
            }
        });
        cbm.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (cbm.getItemCount()==(2*((int)cbl.getSelectedItem())+1)) {
                    m = (int) cbm.getSelectedItem(); // получение значения m
                    Ylm = new SphericalHarmonic(l, m);
                    UpdateChart(false);
                }
            }
        });
        // галочка модуля
        chbmod.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (chbmod.isSelected() && chbvesch.isSelected()) chbvesch.setSelected(false);
                UpdateChart(false);
            }
        });
        // галочка вещь.
        chbvesch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (chbmod.isSelected() && chbvesch.isSelected()) chbmod.setSelected(false);
                UpdateChart(false);
            }
        });

        //Установка диапазонов интегрирования
        spnThetaMin.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                int T = (int)spnThetaMin.getValue();
                if (T < 0) spnThetaMin.setValue(0);
                else if (T >= (int)spnThetaMax.getValue()) spnThetaMin.setValue((int)spnThetaMax.getValue()-1);
                startTheta = toRadians(T);
            }
        });
        spnThetaMax.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                int T = (int)spnThetaMax.getValue();
                if (T > 180) spnThetaMax.setValue(180);
                else if ((int)spnThetaMin.getValue() >= T) spnThetaMax.setValue((int)spnThetaMin.getValue()+1);
                finalTheta = toRadians(T);
            }
        });
        spnPhiMin.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                int P = (int)spnPhiMin.getValue();
                if (P < 0) spnPhiMin.setValue(0);
                else if (P >= (int)spnPhiMax.getValue()) spnPhiMin.setValue((int)spnPhiMax.getValue()-1);
                startPhi = toRadians(P);
            }
        });
        spnPhiMax.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                int P = (int)spnPhiMax.getValue();
                if (P > 360) spnPhiMax.setValue(360);
                else if ((int)spnPhiMin.getValue() >= P) spnPhiMax.setValue((int)spnPhiMin.getValue()+1);
                finalPhi = toRadians(P);
            }
        });

        //кнопка расчёта вероятности
        btnChart.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double P = Ylm.evalProbability(startTheta, finalTheta, startPhi, finalPhi,PI/100);
                String ans = (double)round(100000*P)/1000 + "";
                txtResult.setText(ans+ " %");
                if (chbshowrange.isSelected()) UpdateChart(chbshowrange.isSelected());
            }
        });

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