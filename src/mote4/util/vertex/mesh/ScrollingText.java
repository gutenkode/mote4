package mote4.util.vertex.mesh;

import mote4.scenegraph.Window;
import mote4.util.vertex.FontUtils;

/**
 * Encapsulates a Mesh for rendering text.  The text will start out empty and
 * add characters every time render() is called.
 * Does not support colored text.
 * @author Peter
 */
public class ScrollingText implements Mesh {
    
    private VAO vao;
    private boolean destroyed;
    
    private double index;
    private String fullStr, writeStr, metric;
    private float xPos, yPos, xScale, yScale;
    private double charactersPerSecond;
    
    public ScrollingText(String text, String metric, float xPos, float yPos, float xScale, float yScale, double cps) {
        destroyed = false;
        this.xPos = xPos;
        this.yPos = yPos;
        this.xScale = xScale;
        this.yScale = yScale;
        charactersPerSecond = cps;
        this.metric = metric;
        
        index = 0;
        fullStr = text;
        writeStr = "";
    }

    @Override
    public void render() {
        if (destroyed)
            throw new IllegalStateException("Attempted to render destroyed ScrollingText mesh.");
        
        if (index < fullStr.length()) 
        {
            index += Window.delta()*charactersPerSecond;
            index = Math.min(fullStr.length(), index);
            writeStr = fullStr.substring(0, (int)index);
            
            if (vao != null)
                vao.destroy();
            FontUtils.useMetric(metric);
            vao = (VAO)FontUtils.createString(writeStr, xPos, yPos, xScale, yScale);
        }
        
        vao.render();
    }
    
    /**
     * Forces the text to complete without writing out.
     */
    public void complete() {
        index = fullStr.length()-1;
    }
    /**
     * Whether the full length of text has been written out yet.
     * @return 
     */
    public boolean isDone() { return index >= fullStr.length(); }

    @Override
    public void destroy() {
        if (!destroyed && vao != null)
            vao.destroy();
        destroyed = true;
    }

    /**
     * The full string this object will print out.
     * @return
     */
    public String getFullStr() { return fullStr; }
}
