package com.github.rogerhowell.javaparser_ast_inspector.plugin.util;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

public final class FontUtil {

    public static final Font TITLE;

    static {
        Map<TextAttribute, Object> attributes = new HashMap<>();

        attributes.put(TextAttribute.FAMILY, Font.DIALOG);
//        attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_SEMIBOLD);
        attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_EXTRABOLD);
        attributes.put(TextAttribute.SIZE, 12);

        TITLE = Font.getFont(attributes);
    }

    private FontUtil() {
        // Empty private constructor, to prevent instantiation.
    }


}
