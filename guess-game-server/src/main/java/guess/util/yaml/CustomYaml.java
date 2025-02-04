package guess.util.yaml;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.BaseConstructor;
import org.yaml.snakeyaml.emitter.CustomEmitter;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;
import org.yaml.snakeyaml.serializer.Serializer;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Custom SnakeYAML Yaml class.
 */
public class CustomYaml extends Yaml {
    /**
     * Create Yaml instance.
     *
     * @param constructor BaseConstructor to construct incoming documents
     */
    public CustomYaml(BaseConstructor constructor) {
        super(constructor);

        loadingConfig.setCodePointLimit(Integer.MAX_VALUE);
    }

    /**
     * Create Yaml instance. It is safe to create a few instances and use them
     * in different Threads.
     *
     * @param constructor   BaseConstructor to construct incoming documents
     * @param representer   Representer to emit outgoing objects
     * @param dumperOptions DumperOptions to configure outgoing objects
     */
    public CustomYaml(BaseConstructor constructor, Representer representer, DumperOptions dumperOptions) {
        super(constructor, representer, dumperOptions);
    }

    /**
     * Serialize a Java object into a YAML stream.
     *
     * @param data   Java object to be serialized to YAML
     * @param output stream to write to
     */
    @Override
    public void dump(Object data, Writer output) {
        List<Object> list = new ArrayList<>(1);
        list.add(data);
        customDumpAll(list.iterator(), output, null);
    }

    private void customDumpAll(Iterator<?> data, Writer output, Tag rootTag) {
        var serializer = new Serializer(new CustomEmitter(output, dumperOptions), resolver,
                dumperOptions, rootTag);
        try {
            serializer.open();
            while (data.hasNext()) {
                var node = representer.represent(data.next());
                serializer.serialize(node);
            }
            serializer.close();
        } catch (IOException e) {
            throw new YAMLException(e);
        }
    }
}
