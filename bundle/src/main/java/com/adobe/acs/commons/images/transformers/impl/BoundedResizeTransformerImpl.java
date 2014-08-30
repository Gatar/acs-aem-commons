package com.adobe.acs.commons.images.transformers.impl;

import com.adobe.acs.commons.images.ImageTransformer;
import com.day.image.Layer;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
        label = "ACS AEM Commons - Image Transformer - Bounded Resize",
        description =
                "ImageTransformer that resizes the layer but will not resize past maximum dimension constraints. "
                        + "Accepts two Integer params: height and width. "
                        + "Either width or height will scale to the parameterized limit. "
                        + "The other dimension scale automatically to maintain the original aspect ratio"
                        + "If the original image is smaller than the configured dimensions the image won't be resized")
@Properties({
        @Property(
                name = ImageTransformer.PROP_TYPE,
                value = BoundedResizeTransformerImpl.TYPE,
                propertyPrivate = true
        )
})
@Service(value = ImageTransformer.class)
public class BoundedResizeTransformerImpl implements ImageTransformer {
    private static final Logger log = LoggerFactory.getLogger(BoundedResizeTransformerImpl.class);

    static final String TYPE = "bounded-resize";

    private static final String KEY_WIDTH = "width";

    private static final String KEY_WIDTH_ALIAS = "w";

    private static final String KEY_HEIGHT = "height";

    private static final String KEY_HEIGHT_ALIAS = "h";

    private static final String KEY_UPSCALE = "upscale";


    @Override
    public final Layer transform(final Layer layer, final ValueMap properties) {
        if (properties == null || properties.isEmpty()) {
            log.warn("Transform [ {} ] requires parameters.", TYPE);
            return layer;
        }

        log.debug("Transforming with [ {} ]", TYPE);

        int originalWidth = layer.getWidth();
        int originalHeight = layer.getHeight();
        int width = properties.get(KEY_WIDTH, properties.get(KEY_WIDTH_ALIAS, originalWidth));
        int height = properties.get(KEY_HEIGHT, properties.get(KEY_HEIGHT_ALIAS, originalHeight));
        boolean upscale = properties.get(KEY_UPSCALE, false);


        if ((float) width / originalWidth < (float) height / originalHeight) {
            final float aspect = (float) width / layer.getWidth();
            height = Math.round(layer.getHeight() * aspect);
        } else {
            final float aspect = (float) height / layer.getHeight();
            width = Math.round(layer.getWidth() * aspect);
        }

        //only resize if image is not upscaled or upscaling is allowed explicitly
        if (upscale || (!(width > originalWidth) && !(height > originalHeight))) {
            layer.resize(width, height);
        }

        return layer;
    }
}