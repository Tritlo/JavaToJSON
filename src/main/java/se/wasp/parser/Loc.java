package se.wasp.parser;

import com.github.gumtreediff.tree.ITree;
import com.google.gson.JsonObject;

import gumtree.spoon.builder.jsonsupport.NodePainter;
import spoon.reflect.declaration.CtElement;

public class Loc implements NodePainter {

    public void paint(ITree tree, JsonObject jsontree){
        Object spobj = tree.getMetadata("spoon_object");
        if (spobj != null){
            CtElement el = (CtElement) spobj;
            var pos = el.getPosition();
            if (pos.isValidPosition()){
                var loc = new JsonObject();
                loc.addProperty("start-line", pos.getLine());
                loc.addProperty("end-line", pos.getEndLine());
                loc.addProperty("start-col", pos.getColumn());
                loc.addProperty("end-col", pos.getEndColumn());
                loc.addProperty("file", pos.getFile().getAbsolutePath());
                jsontree.add("location", loc);
            }
            jsontree.addProperty("pretty-printed", el.toString());
            // TODO: add imports if we are in the root of a file.
        }
    }

}
