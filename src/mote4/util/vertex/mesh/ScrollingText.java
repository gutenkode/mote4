package mote4.util.vertex.mesh;

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
    
    private int index, speed;
    private String fullStr, writeStr, metric;
    private float xPos, yPos, xScale, yScale;
    
    public ScrollingText(String text, String metric, float xPos, float yPos, float xScale, float yScale, int speed) {
        destroyed = false;
        this.xPos = xPos;
        this.yPos = yPos;
        this.xScale = xScale;
        this.yScale = yScale;
        this.speed = speed;
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
            index += speed;
            index = Math.min(fullStr.length(), index);
            writeStr = fullStr.substring(0, index);
            
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
        if (vao != null)
            vao.destroy();
        destroyed = true;
    }

    /**
     * The full string this object will print out.
     * @return
     */
    public String getFullStr() { return fullStr; }
}
