package se.wasp.parser;

import java.util.Arrays;
import java.util.function.Function;

import com.github.gumtreediff.tree.ITree;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import gumtree.spoon.builder.Json4SpoonGenerator;
import gumtree.spoon.builder.jsonsupport.NodePainter;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtImport;
import spoon.support.reflect.declaration.CtClassImpl;

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
            jsontree.addProperty("spoon-class", spobj.getClass().getName());

            if (spobj.getClass() == CtClassImpl.class){
                var imports = Arrays.stream(pos.getCompilationUnit().getImports().toArray()).map(i -> ((CtImport) i).toString() ).toList();
                JsonArray imps = new JsonArray();
                for (var i : imports){
                    imps.add(i);
                }
                jsontree.add("imports", imps);
            }
        }
    }

}
