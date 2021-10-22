package se.wasp.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.apache.commons.io.FileUtils;

import gumtree.spoon.builder.Json4SpoonGenerator;
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
                return (new Json4SpoonGenerator()).getJSONasJsonObject(launcher.getModel().getRootPackage());

        }).filter( obj -> obj != null).toList();

       System.out.println((new Gson()).toJson(results));
    }
}
