
import org.apache.commons.math3.geometry.*;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.*;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.*;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import javax.swing.*;
import java.awt.*;

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
        setBounds(100, 100, 800, 800);    // размер и положение окна
        // при закрытии окна EXIT_ON_CLOSE завершит приложение (процесс)
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        int n = 10;
        int a = MathLib.factorial(n);
        txtResult.setText("n! = " + a + ";\n n!! = " + MathLib.semifactorial(n));


        // Define a function to plot
        Mapper mapper = new Mapper() {
            public double f(double x, double y) {
                return 10 * Math.sin(x / 10) * Math.cos(y / 20);
            }
        };

// Define range and precision for the function to plot
        Range range = new Range(-150, 150);
        int steps = 50;

    // Create a surface drawing that function
        Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(range, steps), mapper);
        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), new Range(-10, 10)));
        surface.setFaceDisplayed(true);
        surface.setWireframeDisplayed(false);
        surface.setWireframeColor(Color.BLACK);

    // Create a chart and add the surface
        Chart chart = AWTChartComponentFactory.chart(Quality.Advanced, "swing");
        chart.add(surface);

        //JPanel panel3d = new JPanel();
        pnlChart.setPreferredSize(new Dimension(600, 600)); // Set default size for panel3d
        pnlChart.setLayout(new java.awt.BorderLayout());
        pnlChart.add((JPanel)chart.getCanvas(), BorderLayout.CENTER);
        //setContentPane(this.pnlMain);
        //panel3d.setVisible(true);
        //pnlOutput.add(panel3d);
        //pnlOutput.validate();
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
