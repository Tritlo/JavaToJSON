package se.wasp.parser;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Function;

import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import gumtree.spoon.builder.Json4SpoonGenerator;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import gumtree.spoon.builder.jsonsupport.NodePainter;
import spoon.Launcher;

/**
 * Parses
 *
 */
public class Parser
{
    public static void main( String[] args )
    {
        var results = Arrays.stream(args).map((Function<String, JsonObject>) file ->
        {
                var launcher = new Launcher();
                launcher.getEnvironment().setCommentEnabled(true);
                launcher.addInputResource(file);
                launcher.buildModel();
                // Use J4S to make it into JSON
                // Taken from the gumtree-spoon-ast-diff, we need to get to a tree context
                // so we can add a list of operations (like source location).
                SpoonGumTreeBuilder builder = new SpoonGumTreeBuilder();
		        ITree generatedTree = builder.getTree(launcher.getModel().getRootPackage());
		        TreeContext tcontext = builder.getTreeContext();
                // We add the "Loc" painter, which just adds the location to the JSON object.
                Collection<NodePainter> opList = Arrays.asList(new Loc());
                return (new Json4SpoonGenerator()).getJSONwithCustorLabels(tcontext, generatedTree, opList);

        }).filter( obj -> obj != null).toList();

       System.out.println((new Gson()).toJson(results));
    }
}
