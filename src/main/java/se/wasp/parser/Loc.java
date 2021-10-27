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

    public static JsonObject getPosObj (CtElement el ){
            var pos = el.getPosition();
            if (pos.isValidPosition()){
                var loc = new JsonObject();
                loc.addProperty("start-line", pos.getLine());
                loc.addProperty("end-line", pos.getEndLine());
                loc.addProperty("start-col", pos.getColumn());
                loc.addProperty("end-col", pos.getEndColumn());
                loc.addProperty("file", pos.getFile().getAbsolutePath());
                return loc;
            }
            return null;
    }

    public void paint(ITree tree, JsonObject jsontree){
        Object spobj = tree.getMetadata("spoon_object");
        if (spobj != null){
            CtElement el = (CtElement) spobj;
            var pos = el.getPosition();
            jsontree.add("location", getPosObj(el));
            jsontree.addProperty("pretty-printed", el.toString());
            jsontree.addProperty("spoon-class", spobj.getClass().getName());

            if (spobj.getClass() == CtClassImpl.class){
                var imports = Arrays.stream(pos.getCompilationUnit().getImports().toArray()).map(i ->
                    {  CtImport iel = (CtImport) i;
                       var imp = new JsonObject();
                       imp.addProperty("type", "Import");
                       if (iel.getReference() != null){
                         imp.addProperty("label", iel.toString());
                       }
                       imp.addProperty("reference", iel.getReference() != null ? iel.getReference().toString() : null);
                       imp.add("location", getPosObj(iel));
                       imp.addProperty("pretty-printed", iel.toString());
                       imp.addProperty("spoon-class", i.getClass().getName());
                       return imp;
                    }).toList();
                JsonArray imps = new JsonArray();
                for (var i : imports){
                    imps.add(i);
                }
                jsontree.add("imports", imps);
            }
        }
    }

}
