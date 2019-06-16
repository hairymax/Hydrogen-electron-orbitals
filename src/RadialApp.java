import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.chart2d.Chart2d;
import org.jzy3d.chart2d.Chart2dComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapHotCold;
import org.jzy3d.maths.Coord2d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Range;
import org.jzy3d.plot2d.primitives.LineSerie2d;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import org.opensourcephysics.numerics.Function;
import org.opensourcephysics.numerics.Polynomial;
import org.opensourcephysics.numerics.specialfunctions.Laguerre;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;

import static java.lang.Math.*;
import static org.opensourcephysics.numerics.Integral.simpson;

public class RadialApp extends JFrame {
    private JPanel pnlMain;
    private JPanel pnlControl;
    private JButton btnCalculate;
    private JTextField txtResult;
    private JComboBox cbl;
    private JComboBox cbm;
    private JComboBox cbn;
    private JCheckBox chbRadial;
    private JCheckBox chbProb;
    private JCheckBox chbSpherical;
    private JPanel pnlChart;
    private JSlider sldXmax;
    private JTextField txtXmax;
    private JSpinner spnRhoMax;
    private JSpinner spnRhoMin;

    private Chart2d chartR;// = AWTChartComponentFactory.chart(Quality.Advanced, "swing");
    private Chart chartPsi;
    private int n = 1, l = 0 , m = 0;
    private double xmax, startRho = 0, finalRho = 100;
    private SphericalHarmonic Ylm;

    private double RPsi(int n, int l, double rho){
        Polynomial L = Laguerre.getPolynomial(n-l-1,2*l+1);
        return  Math.sqrt((double) MathLib.factorial(n-l-1)/MathLib.factorial((n+l)))*
                2/n/n*Math.pow(2*rho/n,l)*Math.exp(-rho/n)*L.evaluate(2*rho/n);
    }

    private double evalRProbability(double startRho, double finalRho, double step){
        Function rR2 = new Function() {
            @Override public double evaluate(double rho) {
                double rR = rho*RPsi(n,l,rho);
                return rR*rR;
            }
        };
        int splits_rho = (int) Math.round( (finalRho-startRho) / step );
        splits_rho += (splits_rho%2==1) ? 1 : 0;
        return simpson(rR2,startRho,finalRho,splits_rho);
    }
    private void UpdateChartPanel(){
        pnlChart.removeAll();
        if ((chbRadial.isSelected() || chbProb.isSelected()) && !chbSpherical.isSelected()) {
            UpdateChartR(false);
            pnlChart.add((JPanel) chartR.getCanvas(), BorderLayout.CENTER);
        } else if (!(chbRadial.isSelected() || chbProb.isSelected()) && chbSpherical.isSelected()) {
            UpdateChartPsi(true);
            pnlChart.add((JPanel) chartPsi.getCanvas(), BorderLayout.CENTER);
        } else {
            UpdateChartR(true);
            UpdateChartPsi(false);
            pnlChart.setLayout(new BoxLayout(pnlChart, BoxLayout.Y_AXIS));
            pnlChart.setSize(new Dimension(600, 800));
            JPanel pnlChart1 = new JPanel();
            pnlChart1.setPreferredSize(new Dimension(600, 200));
            pnlChart1.setLayout(new BorderLayout());
            pnlChart1.add((JPanel) chartR.getCanvas(), BorderLayout.CENTER);
            JPanel pnlChart2  = new JPanel();
            pnlChart2.setPreferredSize(new Dimension(600, 600));
            pnlChart2.setLayout(new BorderLayout());
            pnlChart2.add((JPanel) chartPsi.getCanvas(), BorderLayout.CENTER);

            pnlChart.add(pnlChart1); pnlChart.add(pnlChart2);
        }
        pnlChart.updateUI();
    }

    private void UpdateChartR(boolean showLeft){
        int steps = 200;
        int li = 0;
        LineSerie2d serie2d = new LineSerie2d("тест");
        serie2d.setWidth(2);
        double h = (double)xmax/steps;
        if (showLeft) {
            li = steps/2; h *= 2;
        }

        if (chbProb.isSelected()) {
            serie2d.setColor(Color.BLUE);
            for (int i=0; i<=steps; i++) {
                double R = abs(i-li)*RPsi(n,l,abs((i-li)*h));
                serie2d.add((i-li)*h, R*R);
            }
        } else {
            serie2d.setColor(Color.MAGENTA);
            for (int i=0; i<=steps; i++)
                serie2d.add((i-li)*h, RPsi(n,l,abs((i-li)*h)));
        }

        // получаем обновлённый массив данных
        chartR = Chart2dComponentFactory.chart(Quality.Advanced, "swing");
        chartR.getScene().getGraph().add(serie2d.getDrawable());
    }

    private void UpdateChartPsi(boolean is3D){
        Ylm = new SphericalHarmonic(l,m);

        Mapper radial = new Mapper() {
            @Override
            public double f(double x, double y) {
                double rho = sqrt(x*x+y*y);
                double theta = atan(x/y);
                double rR = rho*RPsi(n,l,rho);
                double Y = Ylm.eval(theta,0).abs();
                return rR*rR*Y*Y;//
            }
        };
        Range range = new Range(-(float)xmax, (float)xmax);
        int steps = 100;
        Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(range, steps, range, steps), radial);
        surface.setColorMapper(new ColorMapper(new ColorMapHotCold(), 0, surface.getBounds().getZmax())); //surface.getBounds().getZmin()
        //surface.setFaceDisplayed(true);
        surface.setWireframeDisplayed(false);

        if (is3D) getPsiChart3d();
        else getPsiChart2d();

        chartPsi.getScene().getGraph().add(surface);
    }

    private void getPsiChart3d(){
        chartPsi = AWTChartComponentFactory.chart(Quality.Advanced, "swing");
        chartPsi.getView().setViewPoint(new Coord3d(
                Math.toRadians(45),     // поворот отностительно оси z
                Math.toRadians(30),     // наклон оси z
                0));
    }

    private void getPsiChart2d(){
        chartPsi = Chart2dComponentFactory.chart(Quality.Advanced, "swing");
    }

    public RadialApp() {
        super("Радиальная вероятность");      // заголовок окна
        setContentPane(this.pnlMain); // область с содержимым формы (панель pnlMain)
        setBounds(100, 100, 850, 850);    // размер и положение окна

        // панель графика
        pnlChart.setPreferredSize(new Dimension(600, 600)); // размер по умолчанию для графика
        pnlChart.setLayout(new BorderLayout());

        // добавление квантовых чисел n
        for (int i = 1; i <= 10; i++) cbn.addItem(i);
        cbn.setSelectedItem(1);
        cbl.addItem(0);
        cbl.setSelectedItem(0);
        cbm.addItem(0);
        cbm.setSelectedItem(0);

        xmax = (double)sldXmax.getValue();
        txtXmax.setText(xmax+"");

        spnRhoMax.setValue(100);

        UpdateChartPanel();
        //Psi = new HydrogenWavefunction(n, l, m);

        // ДАЛЕЕ ОБРАБОТЧИКИ СОБЫТИЙ

        // добавление m, реагирование при изменении l
        cbn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cbl.removeAllItems(); // очистка
                n = (int) cbn.getSelectedItem(); // получение значения n
                for (int i = 0; i < n; i++) cbl.addItem(i);
                cbl.setSelectedItem(0);
            }
        });
        cbl.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cbl.getItemCount() == ((int) cbn.getSelectedItem())) {
                    cbm.removeAllItems(); // очистка
                    l = (int) cbl.getSelectedItem(); // получение значения l
                    for (int i = -l; i <= l; i++) cbm.addItem(i);
                    cbm.setSelectedItem(0);
                    UpdateChartPanel();
                }
            }
        });
        cbm.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (cbm.getItemCount() == (2 * ((int) cbl.getSelectedItem()) + 1)) {
                    m = (int) cbm.getSelectedItem(); // получение значения m
                    UpdateChartPanel();
                }
            }
        });
        //галочка радиальная в.ф.
        chbRadial.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (chbRadial.isSelected() && chbProb.isSelected()) chbProb.setSelected(false);
                UpdateChartPanel();
            }
        });
        // галочка плотность веротности
        chbProb.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (chbRadial.isSelected() && chbProb.isSelected()) chbRadial.setSelected(false);
                UpdateChartPanel();
            }
        });
        // галочка плотность веротности в проскости XZ
        chbSpherical.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                UpdateChartPanel();
                cbm.setEnabled(!cbm.isEnabled());
            }
        });
        // слайдер Xmax для изменения диамазона построения
        sldXmax.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                xmax = (double)sldXmax.getValue();
                txtXmax.setText(xmax+"");
            }
        });
        sldXmax.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                super.mouseReleased(e);
                UpdateChartPanel();
            }
        });
        // диапазон интегрирования
        spnRhoMin.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                int r = (int)spnRhoMin.getValue();
                if (r < 0) spnRhoMin.setValue(0);
                else if (r >= (int)spnRhoMax.getValue()) spnRhoMin.setValue((int)spnRhoMax.getValue()-1);
                startRho = r;
            }
        });
        spnRhoMax.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                int r = (int)spnRhoMax.getValue();
                if ((int)spnRhoMin.getValue() >= r) spnRhoMax.setValue((int)spnRhoMin.getValue()+1);
                finalRho = r;
            }
        });
        // поворот графика при перемещении мыши после клика в области графика
        pnlChart.addMouseMotionListener(new MouseMotionAdapter() {
            int x = pnlChart.getWidth() / 2;
            int y = pnlChart.getHeight() / 2;
            @Override
            public void mouseDragged(MouseEvent e) {
                if (!(chbRadial.isSelected() || chbProb.isSelected())){
                    super.mouseDragged(e);
                    float dx = (float) toRadians(2 * signum(e.getX() - x));
                    float dy = (float) toRadians(2 * signum(e.getY() - y));
                    x = e.getX(); y = e.getY();
                    chartPsi.getView().rotate(new Coord2d(dx, dy), true);
                }
            }
        });
        //кнопка расчёта вероятности
        btnCalculate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double P = evalRProbability(startRho, finalRho, 0.01);
                String ans = (double)round(100000*P)/1000 + "";
                txtResult.setText(ans+ " %");
            }
        });
    }
}
