import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.font.FontRenderContext;
import java.util.ArrayList;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.Iterator;

public class MyGraphicsDisplay extends JPanel{

    private ArrayList<Double[]> graphicsData;
    private ArrayList<Double[]> originalData;

    private boolean showAxis = true;
    private boolean showDots = true;
    private boolean showIntegrals = false;

    private double minX;
    private double minY;
    private double maxX;
    private double maxY;

    private double scaleX;
    private double scaleY;

    private double[][] viewport = new double[2][2];
    private int selectedMarker = -1;

    private BasicStroke axisStroke;
    private BasicStroke graphicsStroke;
    private BasicStroke markerStroke;
    private BasicStroke selectionStroke;
    
    private Font axisFont;
    private Font squareFont;
    private Font labelsFont;

    private static DecimalFormat formatter = (DecimalFormat)NumberFormat.getInstance();

    private boolean scaleMode = false;
    private boolean changeMode = false;
    private double[] originalPoint = new double[2];
    private Rectangle2D.Double selectionRect = new Rectangle2D.Double();
    private ArrayList<double[][]> undoHistory = new ArrayList(5);

    public ArrayList<Double[]> getData(){
        return graphicsData;
    }

    public MyGraphicsDisplay(){
        setBackground(Color.WHITE);
        graphicsStroke = new BasicStroke(
            2.0f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_ROUND,
            10.0f,
            new float[] {4*6,1*6,1*6,1*6,1*6,1*6,2*6,1*6,2*6},
            0.0f
        );
        axisStroke = new BasicStroke(
            2.0f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            10.0f,
            null,
            0.0f
        );
        markerStroke = new BasicStroke(
            1f,
            BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER,
            10.0f,
            null,
            0.0f
        );
        selectionStroke = new BasicStroke(
            1.0F,
            0,
            0,
            10.0F,
            new float[]{10.0F, 10.0F},
            0.0F
        );

        axisFont = new Font("Serif", Font.BOLD, 16);
        squareFont = new Font("Calibri", Font.PLAIN, 10);
        labelsFont = new Font("Arial", Font.BOLD, 15);
        formatter.setMaximumFractionDigits(5);

        this.addMouseListener(new MouseHandler());
        this.addMouseMotionListener(new MouseMotionHandler());
    }

    public void showGraphics(ArrayList<Double[]> graphicsData){
        this.graphicsData = graphicsData;
        this.originalData = new ArrayList(graphicsData.size());

        Iterator var3 = graphicsData.iterator();

        while(var3.hasNext()) {
            Double[] point = (Double[])var3.next();
            Double[] newPoint = new Double[]{new Double(point[0]), new Double(point[1])};
            this.originalData.add(newPoint);
        }

        this.minX = ((Double[])graphicsData.get(0))[0];
        this.maxX = ((Double[])graphicsData.get(graphicsData.size() - 1))[0];
        this.minY = ((Double[])graphicsData.get(0))[1];
        this.maxY = this.minY;

        for(int i = 1; i < graphicsData.size(); ++i) {
            if (((Double[])graphicsData.get(i))[1] < this.minY) {
                this.minY = ((Double[])graphicsData.get(i))[1];
            }

            if (((Double[])graphicsData.get(i))[1] > this.maxY) {
                this.maxY = ((Double[])graphicsData.get(i))[1];
            }
        }

        this.zoomToRegion(this.minX, this.maxY, this.maxX, this.minY);
    }

    public void zoomToRegion(double x1, double y1, double x2, double y2) {
        this.viewport[0][0] = x1;
        this.viewport[0][1] = y1;
        this.viewport[1][0] = x2;
        this.viewport[1][1] = y2;
        this.repaint();
    }

    public void setShowAxis(boolean showAxis){
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowDots(boolean showDots){
        this.showDots = showDots;
        repaint();
    }

    public void setShowIntegrals(boolean showIntegrals){
        this.showIntegrals = showIntegrals;
        repaint();
    }

    public void reset(){
        this.showGraphics(this.originalData);    
    }
    
    protected void paintGraphics(Graphics2D canvas){
        canvas.setStroke(graphicsStroke);
        canvas.setColor(Color.RED);
        Double currentX = null;
        Double currentY = null;
        Iterator var5 = this.graphicsData.iterator();

        while(var5.hasNext()) {
            Double[] point = (Double[])var5.next();
            if (!(point[0] < this.viewport[0][0]) && !(point[1] > this.viewport[0][1]) && !(point[0] > this.viewport[1][0]) && !(point[1] < this.viewport[1][1])) {
                if (currentX != null && currentY != null) {
                    canvas.draw(new Line2D.Double(this.xyToPoint(currentX, currentY), this.xyToPoint(point[0], point[1])));
                }

                currentX = point[0];
                currentY = point[1];
            }
        }

    }

    protected void paintAxis(Graphics2D canvas){
        canvas.setStroke(axisStroke);
        canvas.setColor(Color.BLACK);
        canvas.setPaint(Color.BLACK);
        canvas.setFont(axisFont);

        FontRenderContext context = canvas.getFontRenderContext();

        Double beginOfY = maxY;
        Double endOfY = minY;
        int dirY = 1;
        
        Double beginOfX = maxX;
        Double endOfX = minX;
        int dirX = 2;

        if(minX <= 0.0 && maxX >= 0.0){
            canvas.draw(new Line2D.Double(xyToPoint(0, beginOfY), xyToPoint(0, endOfY)));

            Point2D.Double lineEnd = xyToPoint(0, beginOfY);
            drawArrow(canvas, lineEnd, dirY);
            
            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = xyToPoint(0, beginOfY);

            canvas.drawString("y", (float)labelPos.getX() + 10, (float)(labelPos.getY() - bounds.getY()));
        }
        
        if(minY <= 0.0 && maxY >= 0){
            canvas.draw(new Line2D.Double(xyToPoint(beginOfX, 0), xyToPoint(endOfX, 0)));

            Point2D.Double lineEnd = xyToPoint(beginOfX, 0);
            drawArrow(canvas, lineEnd, dirX);
            Point2D.Double labelPos = xyToPoint(beginOfX, 0);

            canvas.drawString("x", (float)labelPos.getX() - 25, (float)labelPos.getY() + 25);
        }
    }

    private void drawArrow(Graphics2D canvas, Point2D.Double lineEnd, int direction){
        GeneralPath arrow = new GeneralPath();
        arrow.moveTo(lineEnd.getX(), lineEnd.getY());
        
        if(direction == 4){
            arrow.lineTo(arrow.getCurrentPoint().getX() + 20, arrow.getCurrentPoint().getY() + 5);
            arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY() - 10);    
        }
        if(direction == 1){
            arrow.lineTo(arrow.getCurrentPoint().getX() - 5, arrow.getCurrentPoint().getY() +20);
            arrow.lineTo(arrow.getCurrentPoint().getX() + 10, arrow.getCurrentPoint().getY());
        }
        if(direction == 2){
            arrow.lineTo(arrow.getCurrentPoint().getX() - 20, arrow.getCurrentPoint().getY() + 5);
            arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY() - 10);    
        }

        arrow.closePath();
        canvas.draw(arrow);
        canvas.fill(arrow);
    }

    protected void paintDots(Graphics2D canvas){
        canvas.setStroke(markerStroke);
                
        for(int i = 0; i < graphicsData.size(); i++){
            canvas.setColor(Color.YELLOW);
            
            double y = graphicsData.get(i)[1];
            Double a = Double.valueOf(y);
            char[] chars = a.toString().toCharArray();

            for(int u = 0; u < 4 && u + 1 < chars.length; u++){
                if(chars[u] == '.' || chars[u] == '-'){
                    continue;
                }
                if(chars[u+1] == '.' && chars[u] > chars[u+2]){
                    canvas.setColor(Color.BLACK);
                    break;
                }
                if(chars[u] > chars[u+1]){
                    canvas.setColor(Color.BLACK);
                    break;
                }
            }

            GeneralPath dot = new GeneralPath();
            Point2D.Double point = xyToPoint(graphicsData.get(i)[0], graphicsData.get(i)[1]);
            
            dot.moveTo(point.getX() + 5.5, point.getY());
            dot.lineTo(point.getX() - 5.5, point.getY());
            
            dot.moveTo(point.getX(), point.getY() + 5.5);
            dot.lineTo(point.getX(), point.getY() - 5.5);
            
            dot.moveTo(point.getX() - 5.5, point.getY() - 5.5);
            dot.lineTo(point.getX() + 5.5, point.getY() + 5.5);

            dot.moveTo(point.getX() - 5.5, point.getY() + 5.5);
            dot.lineTo(point.getX() + 5.5, point.getY() - 5.5);
            canvas.draw(dot);
        }
    }
    
    protected Point2D.Double xyToPoint(double x, double y) {
        double deltaX;
        double deltaY;
        deltaX = x - this.viewport[0][0];
        deltaY = this.viewport[0][1] - y;
        return new Point2D.Double(deltaX*scaleX, deltaY*scaleY);
    }

    protected double[] pointToXY(int x, int y){
        return new double[]{this.viewport[0][0] + (double)x / this.scaleX, this.viewport[0][1] - (double)y / this.scaleY};
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);
        if (this.graphicsData == null || this.graphicsData.size() == 0){
            return;
        }
        
        this.scaleX = this.getSize().getWidth() / (this.viewport[1][0] - this.viewport[0][0]);
        this.scaleY = this.getSize().getHeight() / (this.viewport[0][1] - this.viewport[1][1]);
        minX = graphicsData.get(0)[0];
        maxX = graphicsData.get(graphicsData.size() - 1)[0];
        minY = graphicsData.get(0)[1];
        maxY = minY;
        for (int i = 1; i < graphicsData.size(); i++) {
            if (graphicsData.get(i)[1] < minY) {
                minY = graphicsData.get(i)[1];
            }
            if (graphicsData.get(i)[1] > maxY) {
                maxY = graphicsData.get(i)[1];
            }
        }

        Graphics2D canvas = (Graphics2D) g;
        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();

        if (showAxis)
            paintAxis(canvas);
        
        if(showIntegrals)
            paintSquare(canvas);

        paintGraphics(canvas);    

        if (showDots)
            paintDots(canvas);

        this.paintLabels(canvas);
        this.paintSelection(canvas);
            
        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);    
    }

    private void paintSelection(Graphics2D canvas) {
        if (this.scaleMode) {
            canvas.setStroke(this.selectionStroke);
            canvas.setColor(Color.BLACK);
            canvas.draw(this.selectionRect);
        }
    }

    private void paintLabels(Graphics2D canvas){
        canvas.setFont(this.labelsFont);
        FontRenderContext context = canvas.getFontRenderContext();

        Point2D.Double point;
        String label;
        Rectangle2D bounds;

        if (this.selectedMarker >= 0) {
            point = this.xyToPoint(((Double[])this.graphicsData.get(this.selectedMarker))[0], ((Double[])this.graphicsData.get(this.selectedMarker))[1]);
            label = "X=" + formatter.format(((Double[])this.graphicsData.get(this.selectedMarker))[0]) + ", Y=" + formatter.format(((Double[])this.graphicsData.get(this.selectedMarker))[1]);
            bounds = this.labelsFont.getStringBounds(label, context);
            canvas.setColor(Color.BLUE);
            canvas.drawString(label, (float)(point.getX() + 5.0), (float)(point.getY() - bounds.getHeight()));
        }
    }

    protected int findSelectedPoint(int x, int y) {
        if (this.graphicsData == null) {
            return -1;
        } else {
            int pos = 0;

            for(Iterator var5 = this.graphicsData.iterator(); var5.hasNext(); ++pos) {
                Double[] point = (Double[])var5.next();
                Point2D.Double screenPoint = this.xyToPoint(point[0], point[1]);
                double distance = (screenPoint.getX() - (double)x) * (screenPoint.getX() - (double)x) + (screenPoint.getY() - (double)y) * (screenPoint.getY() - (double)y);
                if (distance < 100.0) {
                    return pos;
                }
            }

            return -1;
        }
    }

    public void paintSquare(Graphics2D canvas){
        canvas.setStroke(markerStroke);
        canvas.setFont(squareFont);
        
        ArrayList<Double> zeros = new ArrayList<>();
        ArrayList<ArrayList<Double>> pointsMinMax = new ArrayList<>();
        
        for(int i = 0; i < graphicsData.size() - 1; i++){
            if(graphicsData.get(i)[1] == 0){
                zeros.add(graphicsData.get(i)[0]);
                i++;
                continue;
            }

            if(graphicsData.get(i)[1] / graphicsData.get(i+1)[1] < 0){
                
                Double x1;
                x1 = graphicsData.get(i)[0];
                Double y1;
                y1 = graphicsData.get(i)[1];
                Double x2;
                x2 = graphicsData.get(i+1)[0];
                Double y2;
                y2 = graphicsData.get(i+1)[1];
                
                Double zero = (x1*(y1 - y2) - y1*(x1 - x2))/(y1 - y2);
    
                zeros.add(zero);
                i++;
            }
        }
        if(zeros.size() < 2) return;
        
        for(int i = 1; i < graphicsData.size() - 1; i++){
            if(
                Math.abs(graphicsData.get(i)[1]) > Math.abs(graphicsData.get(i-1)[1]) && 
                Math.abs(graphicsData.get(i)[1]) > Math.abs(graphicsData.get(i+1)[1])
                ){
                ArrayList<Double> extr = new ArrayList<>();
                extr.add(graphicsData.get(i)[0]);
                extr.add(graphicsData.get(i)[1]);
                pointsMinMax.add(extr);
            }
        }
        
        
        int u = 0;
        while(graphicsData.get(u)[0] < zeros.get(0)) { u++; }
        
        Double step = Math.abs(graphicsData.get(0)[0]) - Math.abs(graphicsData.get(1)[0]);

        for(int i = 0; i < zeros.size() - 1; i++){
            canvas.setColor(Color.BLUE);
            canvas.setPaint(Color.BLUE);
            Double area = .0;
            
            Double part1OfStep;
            Double part2OfStep;

            if(graphicsData.get(u)[1] == 0){u++;}
            
            part1OfStep = Math.abs(zeros.get(i) - graphicsData.get(u)[0]);
            Double area1 = .0;
            if(part1OfStep == 0){
                area1 += Math.abs(step*graphicsData.get(u)[1]/2);
            }else{
                area1 += Math.abs(part1OfStep*graphicsData.get(u)[1]/2);
            }            
            
            GeneralPath ar = new GeneralPath();
            Point2D point1 = xyToPoint(zeros.get(i), 0);
            Point2D point2 = xyToPoint(zeros.get(i + 1), 0);
            Point2D pointMM = xyToPoint(pointsMinMax.get(i).get(0), pointsMinMax.get(i).get(1)/(3));
            
            ar.moveTo(point1.getX(), point1.getY());
            
            while(graphicsData.get(u)[0] < zeros.get(i+1)){
                Point2D pnt = xyToPoint(graphicsData.get(u)[0], graphicsData.get(u)[1]);
                ar.lineTo(pnt.getX(), pnt.getY());
                if(graphicsData.get(u+1)[0] > zeros.get(i+1)){
                    Double area2 = .0;
                    
                    part2OfStep = Math.abs(graphicsData.get(u)[0] - zeros.get(i+1));
                    
                    if(part2OfStep == 0){
                        area2 += Math.abs(step*graphicsData.get(u)[1]/2);
                    }else{
                        area2 += Math.abs(part2OfStep*graphicsData.get(u)[1]/2);
                    }
                    area += area2;
                    break;
                }
                area += Math.abs((graphicsData.get(u)[1] + graphicsData.get(u+1)[1]))*step/2;
                u++;
            }
            ar.lineTo(point2.getX(), point2.getY());
            
            area += area1;

            canvas.draw(ar);
            canvas.fill(ar);
            canvas.setColor(Color.WHITE);
            canvas.setPaint(Color.WHITE);
            DecimalFormat df = new DecimalFormat("#.###");
            canvas.drawString(df.format(area), (float)pointMM.getX() - 10, (float)pointMM.getY());
        }
    }

    public class MouseHandler extends MouseAdapter{
        public MouseHandler(){}

        public void mouseClicked(MouseEvent ev){
            if(ev.getButton() == 3){
                if(MyGraphicsDisplay.this.undoHistory.size() > 0){
                    MyGraphicsDisplay.this.viewport = (double[][])MyGraphicsDisplay.this.undoHistory.get(MyGraphicsDisplay.this.undoHistory.size() - 1);
                    MyGraphicsDisplay.this.undoHistory.remove(MyGraphicsDisplay.this.undoHistory.size() -1);
                }else{
                    MyGraphicsDisplay.this.zoomToRegion(MyGraphicsDisplay.this.minX, MyGraphicsDisplay.this.maxY, MyGraphicsDisplay.this.maxX, MyGraphicsDisplay.this.minY);
                }

                MyGraphicsDisplay.this.repaint();
            }
        }

        public void mousePressed(MouseEvent ev){
            if(ev.getButton() == 1){
                MyGraphicsDisplay.this.selectedMarker = MyGraphicsDisplay.this.findSelectedPoint(ev.getX(), ev.getY());
                MyGraphicsDisplay.this.originalPoint = MyGraphicsDisplay.this.pointToXY(ev.getX(), ev.getY());
                if (MyGraphicsDisplay.this.selectedMarker >= 0) {
                    MyGraphicsDisplay.this.changeMode = true;
                    MyGraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(8));
                } else {
                    MyGraphicsDisplay.this.scaleMode = true;
                    MyGraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(5));
                    MyGraphicsDisplay.this.selectionRect.setFrame((double)ev.getX(), (double)ev.getY(), 1.0, 1.0);
                }
            }
        }

        public void mouseReleased(MouseEvent ev) {
            if (ev.getButton() == 1) {
                MyGraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(0));
                if (MyGraphicsDisplay.this.changeMode) {
                    MyGraphicsDisplay.this.changeMode = false;
                } else {
                    MyGraphicsDisplay.this.scaleMode = false;
                    double[] finalPoint = MyGraphicsDisplay.this.pointToXY(ev.getX(), ev.getY());
                    MyGraphicsDisplay.this.undoHistory.add(MyGraphicsDisplay.this.viewport);
                    MyGraphicsDisplay.this.viewport = new double[2][2];
                    MyGraphicsDisplay.this.zoomToRegion(MyGraphicsDisplay.this.originalPoint[0], MyGraphicsDisplay.this.originalPoint[1], finalPoint[0], finalPoint[1]);
                    MyGraphicsDisplay.this.repaint();
                }

            }
        }
    }

    public class MouseMotionHandler implements MouseMotionListener {
        public MouseMotionHandler() {
        }

        public void mouseMoved(MouseEvent ev) {
            MyGraphicsDisplay.this.selectedMarker = MyGraphicsDisplay.this.findSelectedPoint(ev.getX(), ev.getY());
            if (MyGraphicsDisplay.this.selectedMarker >= 0) {
                MyGraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(8));
            } else {
                MyGraphicsDisplay.this.setCursor(Cursor.getPredefinedCursor(0));
            }

            MyGraphicsDisplay.this.repaint();
        }

        public void mouseDragged(MouseEvent ev) {
            if (MyGraphicsDisplay.this.changeMode) {
                double[] currentPoint = MyGraphicsDisplay.this.pointToXY(ev.getX(), ev.getY());
                double newY = ((Double[])MyGraphicsDisplay.this.graphicsData.get(MyGraphicsDisplay.this.selectedMarker))[1] + (currentPoint[1] - ((Double[])MyGraphicsDisplay.this.graphicsData.get(MyGraphicsDisplay.this.selectedMarker))[1]);
                if (newY > MyGraphicsDisplay.this.viewport[0][1]) {
                    newY = MyGraphicsDisplay.this.viewport[0][1];
                }

                if (newY < MyGraphicsDisplay.this.viewport[1][1]) {
                    newY = MyGraphicsDisplay.this.viewport[1][1];
                }

                ((Double[])MyGraphicsDisplay.this.graphicsData.get(MyGraphicsDisplay.this.selectedMarker))[1] = newY;
                MyGraphicsDisplay.this.repaint();
            } else {
                double width = (double)ev.getX() - MyGraphicsDisplay.this.selectionRect.getX();
                if (width < 5.0) {
                    width = 5.0;
                }

                double height = (double)ev.getY() - MyGraphicsDisplay.this.selectionRect.getY();
                if (height < 5.0) {
                    height = 5.0;
                }

                MyGraphicsDisplay.this.selectionRect.setFrame(MyGraphicsDisplay.this.selectionRect.getX(), MyGraphicsDisplay.this.selectionRect.getY(), width, height);
                MyGraphicsDisplay.this.repaint();
            }

        }
    }
}
