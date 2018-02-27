module moteEngine {
    requires java.desktop;
    requires org.lwjgl;
    requires guava;

    exports mote4.scenegraph;
    exports mote4.scenegraph.target;
    exports mote4.util;
    exports mote4.util.audio;
    exports mote4.util.matrix;
    exports mote4.util.shader;
    exports mote4.util.texture;
    exports mote4.util.vertex;
    exports mote4.util.vertex.builder;
    exports mote4.util.vertex.mesh;
}