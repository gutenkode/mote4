package mote4.util.shader;

import java.util.List;

/**
 * Encapsulates a shader program and uniforms associated
 * with it that are needed for rendering.
 */
public class ShaderContext implements Bindable {

    public final String programName;
    private List<Bindable> bindables;

    public ShaderContext(String p, List<Bindable> b) {
        programName = p;
        bindables = b;
    }

    public void addBindable(Bindable b) {
        bindables.add(b);
    }

    @Override
    public void bind() {
        ShaderMap.use(programName);
        for (Bindable b : bindables)
            b.bind();
    }
}
