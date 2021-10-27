JavaToJson
===

Parses a list of Java source files to a list of JSON, using Spoon and gumtree-spoon-ast-diff

Usage
---

Compile with

`mvn clean install`

Run with

`java -jar target/parser-1.0-jar-with-dependencies <file1.java> <file2.java> <etc...>`

Filtering
---

We can filter the output using `jq` (note, requires jq to be installed).

To use a predefined filter, you can pipe the input into `ASTFilter.sh <visibilty> <type>` where
`<visibility>` is either `public` or `private`  and `<type>` is either `class` or `method`.

You can then manipulate the resulting AST nodes, or just get their labels by using `jq .label`.

Example:
```
mvn clean install && java -jar target/parser-1.0-jar-with-dependencies.jar src/main/java/se/wasp/parser/ | ./ASTFilter.sh public class | jq .label
``` 

returns
```
"Loc"
"Parser"
```

Note, ASTFilter is just a wrapper around invoking `jq` directly using a filter:

`jq ".. | select(.children?) | select(.children | .[] | .children? | .[] | .label == \"$VISIBLITY\") | select(.spoon_class == \"$TYPE\")" <&0`

Replace Literal
---
We can replace literals using "LitReplace.sh":
`java -jar target/parser-1.0-jar-with-dependencies.jar Hello.java | ./ASTFilter.sh public method | ./LitReplace.sh '\"hello, world\"' '"goodbye, world"'`

Which produces a diff:

```
diff --git a/home/tritlo/WASPProj/parser/Hello.java b/home/tritlo/WASPProj/parser/Hello.java
--- a/home/tritlo/WASPProj/parser/Hello.java
+++ b/home/tritlo/WASPProj/parser/Hello.java
@@ -3,1 +3,1 @@
-    System.out.println("hello, world");
+    System.out.println("goodbye, world");
```

By adding `patch`, we can modify the source file:

`java -jar target/parser-1.0-jar-with-dependencies.jar Hello.java | ./ASTFilter.sh public method | ./LitReplace.sh '\"hello, world\"' '"goodbye, world"' | patch`


Example:
---

The source for this project, in `Parser.java`:

```java
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


```

And `Loc.java`:
```java
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
            var loc = new JsonObject();
            var pos = el.getPosition();
            if (pos.isValidPosition()){
                loc.addProperty("start-line", pos.getLine());
                loc.addProperty("end-line", pos.getEndLine());
                loc.addProperty("start-col", pos.getColumn());
                loc.addProperty("end-col", pos.getEndColumn());
                loc.addProperty("file", pos.getFile().getAbsolutePath());
                jsontree.add("location", loc);
            }
        }
    }

}
```

The source for the "tests" for this project, in `AppTest.java`:

```java
package se.wasp;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest
{
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue()
    {
        assertTrue( true );
    }
}
```

Output of running

`mvn clean install && java -jar target/parser-1.0-jar-with-dependencies.jar src/main/java/se/wasp/ src/test/java/se/ | jq `

```json
[
  {
    "label": "",
    "type": "root",
    "children": [
      {
        "label": "unnamed package",
        "type": "RootPac",
        "children": [
          {
            "label": "se",
            "type": "Package",
            "children": [
              {
                "label": "wasp",
                "type": "Package",
                "children": [
                  {
                    "label": "parser",
                    "type": "Package",
                    "children": [
                      {
                        "label": "Loc",
                        "type": "Class",
                        "location": {
                          "start-line": 9,
                          "end-line": 28,
                          "start-col": 14,
                          "end-col": 1,
                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                        },
                        "children": [
                          {
                            "label": "SuperInterfaces",
                            "type": "SUPER_INTERFACES",
                            "children": [
                              {
                                "label": "gumtree.spoon.builder.jsonsupport.NodePainter",
                                "type": "INTERFACE",
                                "location": {
                                  "start-line": 9,
                                  "end-line": 9,
                                  "start-col": 29,
                                  "end-col": 39,
                                  "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                },
                                "children": []
                              }
                            ]
                          },
                          {
                            "label": "",
                            "type": "Modifiers_Class",
                            "children": [
                              {
                                "label": "public",
                                "type": "Modifier",
                                "children": []
                              }
                            ]
                          },
                          {
                            "label": "paint",
                            "type": "Method",
                            "location": {
                              "start-line": 11,
                              "end-line": 26,
                              "start-col": 17,
                              "end-col": 5,
                              "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                            },
                            "children": [
                              {
                                "label": "void",
                                "type": "RETURN_TYPE",
                                "location": {
                                  "start-line": 11,
                                  "end-line": 11,
                                  "start-col": 12,
                                  "end-col": 15,
                                  "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                },
                                "children": []
                              },
                              {
                                "label": "",
                                "type": "Modifiers_Method",
                                "children": [
                                  {
                                    "label": "public",
                                    "type": "Modifier",
                                    "children": []
                                  }
                                ]
                              },
                              {
                                "label": "tree",
                                "type": "Parameter",
                                "location": {
                                  "start-line": 11,
                                  "end-line": 11,
                                  "start-col": 29,
                                  "end-col": 32,
                                  "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                },
                                "children": [
                                  {
                                    "label": "com.github.gumtreediff.tree.ITree",
                                    "type": "VARIABLE_TYPE",
                                    "location": {
                                      "start-line": 11,
                                      "end-line": 11,
                                      "start-col": 23,
                                      "end-col": 27,
                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                    },
                                    "children": []
                                  }
                                ]
                              },
                              {
                                "label": "jsontree",
                                "type": "Parameter",
                                "location": {
                                  "start-line": 11,
                                  "end-line": 11,
                                  "start-col": 46,
                                  "end-col": 53,
                                  "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                },
                                "children": [
                                  {
                                    "label": "com.google.gson.JsonObject",
                                    "type": "VARIABLE_TYPE",
                                    "location": {
                                      "start-line": 11,
                                      "end-line": 11,
                                      "start-col": 35,
                                      "end-col": 44,
                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                    },
                                    "children": []
                                  }
                                ]
                              },
                              {
                                "label": "spobj",
                                "type": "LocalVariable",
                                "location": {
                                  "start-line": 12,
                                  "end-line": 12,
                                  "start-col": 16,
                                  "end-col": 56,
                                  "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                },
                                "children": [
                                  {
                                    "label": "java.lang.Object",
                                    "type": "VARIABLE_TYPE",
                                    "location": {
                                      "start-line": 12,
                                      "end-line": 12,
                                      "start-col": 9,
                                      "end-col": 14,
                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                    },
                                    "children": []
                                  },
                                  {
                                    "label": "getMetadata",
                                    "type": "Invocation",
                                    "location": {
                                      "start-line": 12,
                                      "end-line": 12,
                                      "start-col": 24,
                                      "end-col": 55,
                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                    },
                                    "children": [
                                      {
                                        "label": "tree",
                                        "type": "VariableRead",
                                        "location": {
                                          "start-line": 12,
                                          "end-line": 12,
                                          "start-col": 24,
                                          "end-col": 27,
                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                        },
                                        "children": []
                                      },
                                      {
                                        "label": "\"spoon_object\"",
                                        "type": "Literal",
                                        "location": {
                                          "start-line": 12,
                                          "end-line": 12,
                                          "start-col": 41,
                                          "end-col": 54,
                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                        },
                                        "children": []
                                      }
                                    ]
                                  }
                                ]
                              },
                              {
                                "label": "if",
                                "type": "If",
                                "location": {
                                  "start-line": 13,
                                  "end-line": 25,
                                  "start-col": 9,
                                  "end-col": 9,
                                  "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                },
                                "children": [
                                  {
                                    "label": "NE",
                                    "type": "BinaryOperator",
                                    "location": {
                                      "start-line": 13,
                                      "end-line": 13,
                                      "start-col": 13,
                                      "end-col": 25,
                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                    },
                                    "children": [
                                      {
                                        "label": "spobj",
                                        "type": "VariableRead",
                                        "location": {
                                          "start-line": 13,
                                          "end-line": 13,
                                          "start-col": 13,
                                          "end-col": 17,
                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                        },
                                        "children": []
                                      },
                                      {
                                        "label": "null",
                                        "type": "Literal",
                                        "location": {
                                          "start-line": 13,
                                          "end-line": 13,
                                          "start-col": 22,
                                          "end-col": 25,
                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                        },
                                        "children": []
                                      }
                                    ]
                                  },
                                  {
                                    "label": "THEN",
                                    "type": "then",
                                    "location": {
                                      "start-line": 13,
                                      "end-line": 25,
                                      "start-col": 27,
                                      "end-col": 9,
                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                    },
                                    "children": [
                                      {
                                        "label": "el",
                                        "type": "LocalVariable",
                                        "location": {
                                          "start-line": 14,
                                          "end-line": 14,
                                          "start-col": 23,
                                          "end-col": 45,
                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                        },
                                        "children": [
                                          {
                                            "label": "spoon.reflect.declaration.CtElement",
                                            "type": "VARIABLE_TYPE",
                                            "location": {
                                              "start-line": 14,
                                              "end-line": 14,
                                              "start-col": 13,
                                              "end-col": 21,
                                              "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                            },
                                            "children": []
                                          },
                                          {
                                            "label": "spobj",
                                            "type": "VariableRead",
                                            "location": {
                                              "start-line": 14,
                                              "end-line": 14,
                                              "start-col": 40,
                                              "end-col": 44,
                                              "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                            },
                                            "children": []
                                          }
                                        ]
                                      },
                                      {
                                        "label": "loc",
                                        "type": "LocalVariable",
                                        "location": {
                                          "start-line": 15,
                                          "end-line": 15,
                                          "start-col": 17,
                                          "end-col": 39,
                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                        },
                                        "children": [
                                          {
                                            "label": "se.wasp.parser.var",
                                            "type": "VARIABLE_TYPE",
                                            "location": {
                                              "start-line": 15,
                                              "end-line": 15,
                                              "start-col": 13,
                                              "end-col": 15,
                                              "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                            },
                                            "children": []
                                          },
                                          {
                                            "label": "com.google.gson.JsonObject()",
                                            "type": "ConstructorCall",
                                            "location": {
                                              "start-line": 15,
                                              "end-line": 15,
                                              "start-col": 23,
                                              "end-col": 38,
                                              "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                            },
                                            "children": []
                                          }
                                        ]
                                      },
                                      {
                                        "label": "pos",
                                        "type": "LocalVariable",
                                        "location": {
                                          "start-line": 16,
                                          "end-line": 16,
                                          "start-col": 17,
                                          "end-col": 39,
                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                        },
                                        "children": [
                                          {
                                            "label": "se.wasp.parser.var",
                                            "type": "VARIABLE_TYPE",
                                            "location": {
                                              "start-line": 16,
                                              "end-line": 16,
                                              "start-col": 13,
                                              "end-col": 15,
                                              "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                            },
                                            "children": []
                                          },
                                          {
                                            "label": "getPosition",
                                            "type": "Invocation",
                                            "location": {
                                              "start-line": 16,
                                              "end-line": 16,
                                              "start-col": 23,
                                              "end-col": 38,
                                              "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                            },
                                            "children": [
                                              {
                                                "label": "el",
                                                "type": "VariableRead",
                                                "location": {
                                                  "start-line": 16,
                                                  "end-line": 16,
                                                  "start-col": 23,
                                                  "end-col": 24,
                                                  "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                },
                                                "children": []
                                              }
                                            ]
                                          }
                                        ]
                                      },
                                      {
                                        "label": "if",
                                        "type": "If",
                                        "location": {
                                          "start-line": 17,
                                          "end-line": 24,
                                          "start-col": 13,
                                          "end-col": 13,
                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                        },
                                        "children": [
                                          {
                                            "label": "isValidPosition",
                                            "type": "Invocation",
                                            "location": {
                                              "start-line": 17,
                                              "end-line": 17,
                                              "start-col": 17,
                                              "end-col": 37,
                                              "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                            },
                                            "children": [
                                              {
                                                "label": "pos",
                                                "type": "VariableRead",
                                                "location": {
                                                  "start-line": 17,
                                                  "end-line": 17,
                                                  "start-col": 17,
                                                  "end-col": 19,
                                                  "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                },
                                                "children": []
                                              }
                                            ]
                                          },
                                          {
                                            "label": "THEN",
                                            "type": "then",
                                            "location": {
                                              "start-line": 17,
                                              "end-line": 24,
                                              "start-col": 39,
                                              "end-col": 13,
                                              "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                            },
                                            "children": [
                                              {
                                                "label": "addProperty",
                                                "type": "Invocation",
                                                "location": {
                                                  "start-line": 18,
                                                  "end-line": 18,
                                                  "start-col": 17,
                                                  "end-col": 61,
                                                  "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                },
                                                "children": [
                                                  {
                                                    "label": "loc",
                                                    "type": "VariableRead",
                                                    "location": {
                                                      "start-line": 18,
                                                      "end-line": 18,
                                                      "start-col": 17,
                                                      "end-col": 19,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                    },
                                                    "children": []
                                                  },
                                                  {
                                                    "label": "\"start-line\"",
                                                    "type": "Literal",
                                                    "location": {
                                                      "start-line": 18,
                                                      "end-line": 18,
                                                      "start-col": 33,
                                                      "end-col": 44,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                    },
                                                    "children": []
                                                  },
                                                  {
                                                    "label": "getLine",
                                                    "type": "Invocation",
                                                    "location": {
                                                      "start-line": 18,
                                                      "end-line": 18,
                                                      "start-col": 47,
                                                      "end-col": 59,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                    },
                                                    "children": [
                                                      {
                                                        "label": "pos",
                                                        "type": "VariableRead",
                                                        "location": {
                                                          "start-line": 18,
                                                          "end-line": 18,
                                                          "start-col": 47,
                                                          "end-col": 49,
                                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                        },
                                                        "children": []
                                                      }
                                                    ]
                                                  }
                                                ]
                                              },
                                              {
                                                "label": "addProperty",
                                                "type": "Invocation",
                                                "location": {
                                                  "start-line": 19,
                                                  "end-line": 19,
                                                  "start-col": 17,
                                                  "end-col": 62,
                                                  "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                },
                                                "children": [
                                                  {
                                                    "label": "loc",
                                                    "type": "VariableRead",
                                                    "location": {
                                                      "start-line": 19,
                                                      "end-line": 19,
                                                      "start-col": 17,
                                                      "end-col": 19,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                    },
                                                    "children": []
                                                  },
                                                  {
                                                    "label": "\"end-line\"",
                                                    "type": "Literal",
                                                    "location": {
                                                      "start-line": 19,
                                                      "end-line": 19,
                                                      "start-col": 33,
                                                      "end-col": 42,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                    },
                                                    "children": []
                                                  },
                                                  {
                                                    "label": "getEndLine",
                                                    "type": "Invocation",
                                                    "location": {
                                                      "start-line": 19,
                                                      "end-line": 19,
                                                      "start-col": 45,
                                                      "end-col": 60,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                    },
                                                    "children": [
                                                      {
                                                        "label": "pos",
                                                        "type": "VariableRead",
                                                        "location": {
                                                          "start-line": 19,
                                                          "end-line": 19,
                                                          "start-col": 45,
                                                          "end-col": 47,
                                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                        },
                                                        "children": []
                                                      }
                                                    ]
                                                  }
                                                ]
                                              },
                                              {
                                                "label": "addProperty",
                                                "type": "Invocation",
                                                "location": {
                                                  "start-line": 20,
                                                  "end-line": 20,
                                                  "start-col": 17,
                                                  "end-col": 62,
                                                  "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                },
                                                "children": [
                                                  {
                                                    "label": "loc",
                                                    "type": "VariableRead",
                                                    "location": {
                                                      "start-line": 20,
                                                      "end-line": 20,
                                                      "start-col": 17,
                                                      "end-col": 19,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                    },
                                                    "children": []
                                                  },
                                                  {
                                                    "label": "\"start-col\"",
                                                    "type": "Literal",
                                                    "location": {
                                                      "start-line": 20,
                                                      "end-line": 20,
                                                      "start-col": 33,
                                                      "end-col": 43,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                    },
                                                    "children": []
                                                  },
                                                  {
                                                    "label": "getColumn",
                                                    "type": "Invocation",
                                                    "location": {
                                                      "start-line": 20,
                                                      "end-line": 20,
                                                      "start-col": 46,
                                                      "end-col": 60,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                    },
                                                    "children": [
                                                      {
                                                        "label": "pos",
                                                        "type": "VariableRead",
                                                        "location": {
                                                          "start-line": 20,
                                                          "end-line": 20,
                                                          "start-col": 46,
                                                          "end-col": 48,
                                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                        },
                                                        "children": []
                                                      }
                                                    ]
                                                  }
                                                ]
                                              },
                                              {
                                                "label": "addProperty",
                                                "type": "Invocation",
                                                "location": {
                                                  "start-line": 21,
                                                  "end-line": 21,
                                                  "start-col": 17,
                                                  "end-col": 63,
                                                  "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                },
                                                "children": [
                                                  {
                                                    "label": "loc",
                                                    "type": "VariableRead",
                                                    "location": {
                                                      "start-line": 21,
                                                      "end-line": 21,
                                                      "start-col": 17,
                                                      "end-col": 19,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                    },
                                                    "children": []
                                                  },
                                                  {
                                                    "label": "\"end-col\"",
                                                    "type": "Literal",
                                                    "location": {
                                                      "start-line": 21,
                                                      "end-line": 21,
                                                      "start-col": 33,
                                                      "end-col": 41,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                    },
                                                    "children": []
                                                  },
                                                  {
                                                    "label": "getEndColumn",
                                                    "type": "Invocation",
                                                    "location": {
                                                      "start-line": 21,
                                                      "end-line": 21,
                                                      "start-col": 44,
                                                      "end-col": 61,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                    },
                                                    "children": [
                                                      {
                                                        "label": "pos",
                                                        "type": "VariableRead",
                                                        "location": {
                                                          "start-line": 21,
                                                          "end-line": 21,
                                                          "start-col": 44,
                                                          "end-col": 46,
                                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                        },
                                                        "children": []
                                                      }
                                                    ]
                                                  }
                                                ]
                                              },
                                              {
                                                "label": "addProperty",
                                                "type": "Invocation",
                                                "location": {
                                                  "start-line": 22,
                                                  "end-line": 22,
                                                  "start-col": 17,
                                                  "end-col": 73,
                                                  "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                },
                                                "children": [
                                                  {
                                                    "label": "loc",
                                                    "type": "VariableRead",
                                                    "location": {
                                                      "start-line": 22,
                                                      "end-line": 22,
                                                      "start-col": 17,
                                                      "end-col": 19,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                    },
                                                    "children": []
                                                  },
                                                  {
                                                    "label": "\"file\"",
                                                    "type": "Literal",
                                                    "location": {
                                                      "start-line": 22,
                                                      "end-line": 22,
                                                      "start-col": 33,
                                                      "end-col": 38,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                    },
                                                    "children": []
                                                  },
                                                  {
                                                    "label": "getAbsolutePath",
                                                    "type": "Invocation",
                                                    "location": {
                                                      "start-line": 22,
                                                      "end-line": 22,
                                                      "start-col": 41,
                                                      "end-col": 71,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                    },
                                                    "children": [
                                                      {
                                                        "label": "getFile",
                                                        "type": "Invocation",
                                                        "location": {
                                                          "start-line": 22,
                                                          "end-line": 22,
                                                          "start-col": 41,
                                                          "end-col": 53,
                                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                        },
                                                        "children": [
                                                          {
                                                            "label": "pos",
                                                            "type": "VariableRead",
                                                            "location": {
                                                              "start-line": 22,
                                                              "end-line": 22,
                                                              "start-col": 41,
                                                              "end-col": 43,
                                                              "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                            },
                                                            "children": []
                                                          }
                                                        ]
                                                      }
                                                    ]
                                                  }
                                                ]
                                              },
                                              {
                                                "label": "add",
                                                "type": "Invocation",
                                                "location": {
                                                  "start-line": 23,
                                                  "end-line": 23,
                                                  "start-col": 17,
                                                  "end-col": 46,
                                                  "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                },
                                                "children": [
                                                  {
                                                    "label": "jsontree",
                                                    "type": "VariableRead",
                                                    "location": {
                                                      "start-line": 23,
                                                      "end-line": 23,
                                                      "start-col": 17,
                                                      "end-col": 24,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                    },
                                                    "children": []
                                                  },
                                                  {
                                                    "label": "\"location\"",
                                                    "type": "Literal",
                                                    "location": {
                                                      "start-line": 23,
                                                      "end-line": 23,
                                                      "start-col": 30,
                                                      "end-col": 39,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                    },
                                                    "children": []
                                                  },
                                                  {
                                                    "label": "loc",
                                                    "type": "VariableRead",
                                                    "location": {
                                                      "start-line": 23,
                                                      "end-line": 23,
                                                      "start-col": 42,
                                                      "end-col": 44,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Loc.java"
                                                    },
                                                    "children": []
                                                  }
                                                ]
                                              }
                                            ]
                                          }
                                        ]
                                      }
                                    ]
                                  }
                                ]
                              }
                            ]
                          }
                        ]
                      },
                      {
                        "label": "Parser",
                        "type": "Class",
                        "location": {
                          "start-line": 21,
                          "end-line": 45,
                          "start-col": 14,
                          "end-col": 1,
                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                        },
                        "children": [
                          {
                            "label": "",
                            "type": "Modifiers_Class",
                            "children": [
                              {
                                "label": "public",
                                "type": "Modifier",
                                "children": []
                              }
                            ]
                          },
                          {
                            "label": "main",
                            "type": "Method",
                            "location": {
                              "start-line": 23,
                              "end-line": 44,
                              "start-col": 24,
                              "end-col": 5,
                              "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                            },
                            "children": [
                              {
                                "label": "void",
                                "type": "RETURN_TYPE",
                                "location": {
                                  "start-line": 23,
                                  "end-line": 23,
                                  "start-col": 19,
                                  "end-col": 22,
                                  "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                },
                                "children": []
                              },
                              {
                                "label": "",
                                "type": "Modifiers_Method",
                                "children": [
                                  {
                                    "label": "public",
                                    "type": "Modifier",
                                    "children": []
                                  },
                                  {
                                    "label": "static",
                                    "type": "Modifier",
                                    "children": []
                                  }
                                ]
                              },
                              {
                                "label": "args",
                                "type": "Parameter",
                                "location": {
                                  "start-line": 23,
                                  "end-line": 23,
                                  "start-col": 39,
                                  "end-col": 42,
                                  "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                },
                                "children": [
                                  {
                                    "label": "java.lang.String[]",
                                    "type": "VARIABLE_TYPE",
                                    "location": {
                                      "start-line": 23,
                                      "end-line": 23,
                                      "start-col": 30,
                                      "end-col": 37,
                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                    },
                                    "children": []
                                  }
                                ]
                              },
                              {
                                "label": "results",
                                "type": "LocalVariable",
                                "location": {
                                  "start-line": 25,
                                  "end-line": 41,
                                  "start-col": 13,
                                  "end-col": 48,
                                  "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                },
                                "children": [
                                  {
                                    "label": "se.wasp.parser.var",
                                    "type": "VARIABLE_TYPE",
                                    "location": {
                                      "start-line": 25,
                                      "end-line": 25,
                                      "start-col": 9,
                                      "end-col": 11,
                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                    },
                                    "children": []
                                  },
                                  {
                                    "label": "toList",
                                    "type": "Invocation",
                                    "location": {
                                      "start-line": 25,
                                      "end-line": 41,
                                      "start-col": 23,
                                      "end-col": 47,
                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                    },
                                    "children": [
                                      {
                                        "label": "filter",
                                        "type": "Invocation",
                                        "location": {
                                          "start-line": 25,
                                          "end-line": 41,
                                          "start-col": 23,
                                          "end-col": 38,
                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                        },
                                        "children": [
                                          {
                                            "label": "map",
                                            "type": "Invocation",
                                            "location": {
                                              "start-line": 25,
                                              "end-line": 41,
                                              "start-col": 23,
                                              "end-col": 10,
                                              "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                            },
                                            "children": [
                                              {
                                                "label": "stream",
                                                "type": "Invocation",
                                                "location": {
                                                  "start-line": 25,
                                                  "end-line": 25,
                                                  "start-col": 23,
                                                  "end-col": 41,
                                                  "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                },
                                                "children": [
                                                  {
                                                    "label": "java.util.Arrays",
                                                    "type": "TypeAccess",
                                                    "location": {
                                                      "start-line": 25,
                                                      "end-line": 25,
                                                      "start-col": 23,
                                                      "end-col": 28,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                    },
                                                    "children": []
                                                  },
                                                  {
                                                    "label": "args",
                                                    "type": "VariableRead",
                                                    "location": {
                                                      "start-line": 25,
                                                      "end-line": 25,
                                                      "start-col": 37,
                                                      "end-col": 40,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                    },
                                                    "children": []
                                                  }
                                                ]
                                              },
                                              {
                                                "label": "lambda$0",
                                                "type": "Lambda",
                                                "location": {
                                                  "start-line": 25,
                                                  "end-line": 41,
                                                  "start-col": 78,
                                                  "end-col": 9,
                                                  "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                },
                                                "children": [
                                                  {
                                                    "label": "file",
                                                    "type": "Parameter",
                                                    "location": {
                                                      "start-line": 25,
                                                      "end-line": 25,
                                                      "start-col": 78,
                                                      "end-col": 81,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                    },
                                                    "children": [
                                                      {
                                                        "label": "java.lang.String",
                                                        "type": "VARIABLE_TYPE",
                                                        "children": []
                                                      }
                                                    ]
                                                  },
                                                  {
                                                    "label": "launcher",
                                                    "type": "LocalVariable",
                                                    "location": {
                                                      "start-line": 27,
                                                      "end-line": 27,
                                                      "start-col": 21,
                                                      "end-col": 46,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                    },
                                                    "children": [
                                                      {
                                                        "label": "se.wasp.parser.var",
                                                        "type": "VARIABLE_TYPE",
                                                        "location": {
                                                          "start-line": 27,
                                                          "end-line": 27,
                                                          "start-col": 17,
                                                          "end-col": 19,
                                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                        },
                                                        "children": []
                                                      },
                                                      {
                                                        "label": "spoon.Launcher()",
                                                        "type": "ConstructorCall",
                                                        "location": {
                                                          "start-line": 27,
                                                          "end-line": 27,
                                                          "start-col": 32,
                                                          "end-col": 45,
                                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                        },
                                                        "children": []
                                                      }
                                                    ]
                                                  },
                                                  {
                                                    "label": "setCommentEnabled",
                                                    "type": "Invocation",
                                                    "location": {
                                                      "start-line": 28,
                                                      "end-line": 28,
                                                      "start-col": 17,
                                                      "end-col": 66,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                    },
                                                    "children": [
                                                      {
                                                        "label": "getEnvironment",
                                                        "type": "Invocation",
                                                        "location": {
                                                          "start-line": 28,
                                                          "end-line": 28,
                                                          "start-col": 17,
                                                          "end-col": 41,
                                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                        },
                                                        "children": [
                                                          {
                                                            "label": "launcher",
                                                            "type": "VariableRead",
                                                            "location": {
                                                              "start-line": 28,
                                                              "end-line": 28,
                                                              "start-col": 17,
                                                              "end-col": 24,
                                                              "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                            },
                                                            "children": []
                                                          }
                                                        ]
                                                      },
                                                      {
                                                        "label": "true",
                                                        "type": "Literal",
                                                        "location": {
                                                          "start-line": 28,
                                                          "end-line": 28,
                                                          "start-col": 61,
                                                          "end-col": 64,
                                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                        },
                                                        "children": []
                                                      }
                                                    ]
                                                  },
                                                  {
                                                    "label": "addInputResource",
                                                    "type": "Invocation",
                                                    "location": {
                                                      "start-line": 29,
                                                      "end-line": 29,
                                                      "start-col": 17,
                                                      "end-col": 48,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                    },
                                                    "children": [
                                                      {
                                                        "label": "launcher",
                                                        "type": "VariableRead",
                                                        "location": {
                                                          "start-line": 29,
                                                          "end-line": 29,
                                                          "start-col": 17,
                                                          "end-col": 24,
                                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                        },
                                                        "children": []
                                                      },
                                                      {
                                                        "label": "file",
                                                        "type": "VariableRead",
                                                        "location": {
                                                          "start-line": 29,
                                                          "end-line": 29,
                                                          "start-col": 43,
                                                          "end-col": 46,
                                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                        },
                                                        "children": []
                                                      }
                                                    ]
                                                  },
                                                  {
                                                    "label": "buildModel",
                                                    "type": "Invocation",
                                                    "location": {
                                                      "start-line": 30,
                                                      "end-line": 30,
                                                      "start-col": 17,
                                                      "end-col": 38,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                    },
                                                    "children": [
                                                      {
                                                        "label": "launcher",
                                                        "type": "VariableRead",
                                                        "location": {
                                                          "start-line": 30,
                                                          "end-line": 30,
                                                          "start-col": 17,
                                                          "end-col": 24,
                                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                        },
                                                        "children": []
                                                      }
                                                    ]
                                                  },
                                                  {
                                                    "label": "builder",
                                                    "type": "LocalVariable",
                                                    "location": {
                                                      "start-line": 34,
                                                      "end-line": 34,
                                                      "start-col": 37,
                                                      "end-col": 72,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                    },
                                                    "children": [
                                                      {
                                                        "label": "gumtree.spoon.builder.SpoonGumTreeBuilder",
                                                        "type": "VARIABLE_TYPE",
                                                        "location": {
                                                          "start-line": 34,
                                                          "end-line": 34,
                                                          "start-col": 17,
                                                          "end-col": 35,
                                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                        },
                                                        "children": []
                                                      },
                                                      {
                                                        "label": "gumtree.spoon.builder.SpoonGumTreeBuilder()",
                                                        "type": "ConstructorCall",
                                                        "location": {
                                                          "start-line": 34,
                                                          "end-line": 34,
                                                          "start-col": 47,
                                                          "end-col": 71,
                                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                        },
                                                        "children": []
                                                      },
                                                      {
                                                        "label": "Use J4S to make it into JSON",
                                                        "type": "Comment",
                                                        "location": {
                                                          "start-line": 31,
                                                          "end-line": 31,
                                                          "start-col": 17,
                                                          "end-col": 47,
                                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                        },
                                                        "children": []
                                                      },
                                                      {
                                                        "label": "Taken from the gumtree-spoon-ast-diff, we need to get to a tree context",
                                                        "type": "Comment",
                                                        "location": {
                                                          "start-line": 32,
                                                          "end-line": 32,
                                                          "start-col": 17,
                                                          "end-col": 90,
                                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                        },
                                                        "children": []
                                                      },
                                                      {
                                                        "label": "so we can add a list of operations (like source location).",
                                                        "type": "Comment",
                                                        "location": {
                                                          "start-line": 33,
                                                          "end-line": 33,
                                                          "start-col": 17,
                                                          "end-col": 77,
                                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                        },
                                                        "children": []
                                                      }
                                                    ]
                                                  },
                                                  {
                                                    "label": "generatedTree",
                                                    "type": "LocalVariable",
                                                    "location": {
                                                      "start-line": 35,
                                                      "end-line": 35,
                                                      "start-col": 17,
                                                      "end-col": 86,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                    },
                                                    "children": [
                                                      {
                                                        "label": "com.github.gumtreediff.tree.ITree",
                                                        "type": "VARIABLE_TYPE",
                                                        "location": {
                                                          "start-line": 35,
                                                          "end-line": 35,
                                                          "start-col": 11,
                                                          "end-col": 15,
                                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                        },
                                                        "children": []
                                                      },
                                                      {
                                                        "label": "getTree",
                                                        "type": "Invocation",
                                                        "location": {
                                                          "start-line": 35,
                                                          "end-line": 35,
                                                          "start-col": 33,
                                                          "end-col": 85,
                                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                        },
                                                        "children": [
                                                          {
                                                            "label": "builder",
                                                            "type": "VariableRead",
                                                            "location": {
                                                              "start-line": 35,
                                                              "end-line": 35,
                                                              "start-col": 33,
                                                              "end-col": 39,
                                                              "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                            },
                                                            "children": []
                                                          },
                                                          {
                                                            "label": "getRootPackage",
                                                            "type": "Invocation",
                                                            "location": {
                                                              "start-line": 35,
                                                              "end-line": 35,
                                                              "start-col": 49,
                                                              "end-col": 84,
                                                              "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                            },
                                                            "children": [
                                                              {
                                                                "label": "getModel",
                                                                "type": "Invocation",
                                                                "location": {
                                                                  "start-line": 35,
                                                                  "end-line": 35,
                                                                  "start-col": 49,
                                                                  "end-col": 67,
                                                                  "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                                },
                                                                "children": [
                                                                  {
                                                                    "label": "launcher",
                                                                    "type": "VariableRead",
                                                                    "location": {
                                                                      "start-line": 35,
                                                                      "end-line": 35,
                                                                      "start-col": 49,
                                                                      "end-col": 56,
                                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                                    },
                                                                    "children": []
                                                                  }
                                                                ]
                                                              }
                                                            ]
                                                          }
                                                        ]
                                                      }
                                                    ]
                                                  },
                                                  {
                                                    "label": "tcontext",
                                                    "type": "LocalVariable",
                                                    "location": {
                                                      "start-line": 36,
                                                      "end-line": 36,
                                                      "start-col": 23,
                                                      "end-col": 58,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                    },
                                                    "children": [
                                                      {
                                                        "label": "com.github.gumtreediff.tree.TreeContext",
                                                        "type": "VARIABLE_TYPE",
                                                        "location": {
                                                          "start-line": 36,
                                                          "end-line": 36,
                                                          "start-col": 11,
                                                          "end-col": 21,
                                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                        },
                                                        "children": []
                                                      },
                                                      {
                                                        "label": "getTreeContext",
                                                        "type": "Invocation",
                                                        "location": {
                                                          "start-line": 36,
                                                          "end-line": 36,
                                                          "start-col": 34,
                                                          "end-col": 57,
                                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                        },
                                                        "children": [
                                                          {
                                                            "label": "builder",
                                                            "type": "VariableRead",
                                                            "location": {
                                                              "start-line": 36,
                                                              "end-line": 36,
                                                              "start-col": 34,
                                                              "end-col": 40,
                                                              "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                            },
                                                            "children": []
                                                          }
                                                        ]
                                                      }
                                                    ]
                                                  },
                                                  {
                                                    "label": "opList",
                                                    "type": "LocalVariable",
                                                    "location": {
                                                      "start-line": 38,
                                                      "end-line": 38,
                                                      "start-col": 41,
                                                      "end-col": 74,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                    },
                                                    "children": [
                                                      {
                                                        "label": "java.util.Collection",
                                                        "type": "VARIABLE_TYPE",
                                                        "location": {
                                                          "start-line": 38,
                                                          "end-line": 38,
                                                          "start-col": 17,
                                                          "end-col": 39,
                                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                        },
                                                        "children": [
                                                          {
                                                            "label": "gumtree.spoon.builder.jsonsupport.NodePainter",
                                                            "type": "TYPE_ARGUMENT",
                                                            "location": {
                                                              "start-line": 38,
                                                              "end-line": 38,
                                                              "start-col": 28,
                                                              "end-col": 38,
                                                              "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                            },
                                                            "children": []
                                                          }
                                                        ]
                                                      },
                                                      {
                                                        "label": "asList",
                                                        "type": "Invocation",
                                                        "location": {
                                                          "start-line": 38,
                                                          "end-line": 38,
                                                          "start-col": 50,
                                                          "end-col": 73,
                                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                        },
                                                        "children": [
                                                          {
                                                            "label": "java.util.Arrays",
                                                            "type": "TypeAccess",
                                                            "location": {
                                                              "start-line": 38,
                                                              "end-line": 38,
                                                              "start-col": 50,
                                                              "end-col": 55,
                                                              "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                            },
                                                            "children": []
                                                          },
                                                          {
                                                            "label": "se.wasp.parser.Loc()",
                                                            "type": "ConstructorCall",
                                                            "location": {
                                                              "start-line": 38,
                                                              "end-line": 38,
                                                              "start-col": 64,
                                                              "end-col": 72,
                                                              "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                            },
                                                            "children": []
                                                          }
                                                        ]
                                                      },
                                                      {
                                                        "label": "We add the \"Loc\" painter, which just adds the location to the JSON object.",
                                                        "type": "Comment",
                                                        "location": {
                                                          "start-line": 37,
                                                          "end-line": 37,
                                                          "start-col": 17,
                                                          "end-col": 93,
                                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                        },
                                                        "children": []
                                                      }
                                                    ]
                                                  },
                                                  {
                                                    "label": "return",
                                                    "type": "Return",
                                                    "location": {
                                                      "start-line": 39,
                                                      "end-line": 39,
                                                      "start-col": 17,
                                                      "end-col": 108,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                    },
                                                    "children": [
                                                      {
                                                        "label": "getJSONwithCustorLabels",
                                                        "type": "Invocation",
                                                        "location": {
                                                          "start-line": 39,
                                                          "end-line": 39,
                                                          "start-col": 24,
                                                          "end-col": 107,
                                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                        },
                                                        "children": [
                                                          {
                                                            "label": "gumtree.spoon.builder.Json4SpoonGenerator()",
                                                            "type": "ConstructorCall",
                                                            "location": {
                                                              "start-line": 39,
                                                              "end-line": 39,
                                                              "start-col": 24,
                                                              "end-col": 50,
                                                              "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                            },
                                                            "children": []
                                                          },
                                                          {
                                                            "label": "tcontext",
                                                            "type": "VariableRead",
                                                            "location": {
                                                              "start-line": 39,
                                                              "end-line": 39,
                                                              "start-col": 76,
                                                              "end-col": 83,
                                                              "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                            },
                                                            "children": []
                                                          },
                                                          {
                                                            "label": "generatedTree",
                                                            "type": "VariableRead",
                                                            "location": {
                                                              "start-line": 39,
                                                              "end-line": 39,
                                                              "start-col": 86,
                                                              "end-col": 98,
                                                              "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                            },
                                                            "children": []
                                                          },
                                                          {
                                                            "label": "opList",
                                                            "type": "VariableRead",
                                                            "location": {
                                                              "start-line": 39,
                                                              "end-line": 39,
                                                              "start-col": 101,
                                                              "end-col": 106,
                                                              "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                            },
                                                            "children": []
                                                          }
                                                        ]
                                                      }
                                                    ]
                                                  }
                                                ]
                                              }
                                            ]
                                          },
                                          {
                                            "label": "lambda$1",
                                            "type": "Lambda",
                                            "location": {
                                              "start-line": 41,
                                              "end-line": 41,
                                              "start-col": 20,
                                              "end-col": 37,
                                              "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                            },
                                            "children": [
                                              {
                                                "label": "obj",
                                                "type": "Parameter",
                                                "location": {
                                                  "start-line": 41,
                                                  "end-line": 41,
                                                  "start-col": 20,
                                                  "end-col": 22,
                                                  "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                },
                                                "children": [
                                                  {
                                                    "label": "com.google.gson.JsonObject",
                                                    "type": "VARIABLE_TYPE",
                                                    "children": []
                                                  }
                                                ]
                                              },
                                              {
                                                "label": "NE",
                                                "type": "BinaryOperator",
                                                "location": {
                                                  "start-line": 41,
                                                  "end-line": 41,
                                                  "start-col": 27,
                                                  "end-col": 37,
                                                  "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                },
                                                "children": [
                                                  {
                                                    "label": "obj",
                                                    "type": "VariableRead",
                                                    "location": {
                                                      "start-line": 41,
                                                      "end-line": 41,
                                                      "start-col": 27,
                                                      "end-col": 29,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                    },
                                                    "children": []
                                                  },
                                                  {
                                                    "label": "null",
                                                    "type": "Literal",
                                                    "location": {
                                                      "start-line": 41,
                                                      "end-line": 41,
                                                      "start-col": 34,
                                                      "end-col": 37,
                                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                                    },
                                                    "children": []
                                                  }
                                                ]
                                              }
                                            ]
                                          }
                                        ]
                                      }
                                    ]
                                  }
                                ]
                              },
                              {
                                "label": "println",
                                "type": "Invocation",
                                "location": {
                                  "start-line": 43,
                                  "end-line": 43,
                                  "start-col": 8,
                                  "end-col": 56,
                                  "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                },
                                "children": [
                                  {
                                    "label": "out",
                                    "type": "FieldRead",
                                    "location": {
                                      "start-line": 43,
                                      "end-line": 43,
                                      "start-col": 8,
                                      "end-col": 17,
                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                    },
                                    "children": [
                                      {
                                        "label": "java.lang.System",
                                        "type": "TypeAccess",
                                        "location": {
                                          "start-line": 43,
                                          "end-line": 43,
                                          "start-col": 8,
                                          "end-col": 13,
                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                        },
                                        "children": []
                                      }
                                    ]
                                  },
                                  {
                                    "label": "toJson",
                                    "type": "Invocation",
                                    "location": {
                                      "start-line": 43,
                                      "end-line": 43,
                                      "start-col": 27,
                                      "end-col": 54,
                                      "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                    },
                                    "children": [
                                      {
                                        "label": "com.google.gson.Gson()",
                                        "type": "ConstructorCall",
                                        "location": {
                                          "start-line": 43,
                                          "end-line": 43,
                                          "start-col": 27,
                                          "end-col": 38,
                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                        },
                                        "children": []
                                      },
                                      {
                                        "label": "results",
                                        "type": "VariableRead",
                                        "location": {
                                          "start-line": 43,
                                          "end-line": 43,
                                          "start-col": 47,
                                          "end-col": 53,
                                          "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                                        },
                                        "children": []
                                      }
                                    ]
                                  }
                                ]
                              }
                            ]
                          },
                          {
                            "label": "Parses",
                            "type": "JavaDoc",
                            "location": {
                              "start-line": 17,
                              "end-line": 20,
                              "start-col": 1,
                              "end-col": 3,
                              "file": "/home/tritlo/WASPProj/parser/src/main/java/se/wasp/parser/Parser.java"
                            },
                            "children": []
                          }
                        ]
                      }
                    ]
                  }
                ]
              }
            ]
          }
        ]
      }
    ]
  },
  {
    "label": "",
    "type": "root",
    "children": [
      {
        "label": "unnamed package",
        "type": "RootPac",
        "children": [
          {
            "label": "se",
            "type": "Package",
            "children": [
              {
                "label": "wasp",
                "type": "Package",
                "children": [
                  {
                    "label": "AppTest",
                    "type": "Class",
                    "location": {
                      "start-line": 10,
                      "end-line": 20,
                      "start-col": 14,
                      "end-col": 1,
                      "file": "/home/tritlo/WASPProj/parser/src/test/java/se/wasp/AppTest.java"
                    },
                    "children": [
                      {
                        "label": "",
                        "type": "Modifiers_Class",
                        "children": [
                          {
                            "label": "public",
                            "type": "Modifier",
                            "children": []
                          }
                        ]
                      },
                      {
                        "label": "shouldAnswerWithTrue",
                        "type": "Method",
                        "location": {
                          "start-line": 16,
                          "end-line": 19,
                          "start-col": 17,
                          "end-col": 5,
                          "file": "/home/tritlo/WASPProj/parser/src/test/java/se/wasp/AppTest.java"
                        },
                        "children": [
                          {
                            "label": "void",
                            "type": "RETURN_TYPE",
                            "location": {
                              "start-line": 16,
                              "end-line": 16,
                              "start-col": 12,
                              "end-col": 15,
                              "file": "/home/tritlo/WASPProj/parser/src/test/java/se/wasp/AppTest.java"
                            },
                            "children": []
                          },
                          {
                            "label": "",
                            "type": "Modifiers_Method",
                            "children": [
                              {
                                "label": "public",
                                "type": "Modifier",
                                "children": []
                              }
                            ]
                          },
                          {
                            "label": "@org.junit.Test",
                            "type": "Annotation",
                            "location": {
                              "start-line": 15,
                              "end-line": 15,
                              "start-col": 5,
                              "end-col": 9,
                              "file": "/home/tritlo/WASPProj/parser/src/test/java/se/wasp/AppTest.java"
                            },
                            "children": []
                          },
                          {
                            "label": "assertTrue",
                            "type": "Invocation",
                            "location": {
                              "start-line": 18,
                              "end-line": 18,
                              "start-col": 9,
                              "end-col": 27,
                              "file": "/home/tritlo/WASPProj/parser/src/test/java/se/wasp/AppTest.java"
                            },
                            "children": [
                              {
                                "label": "true",
                                "type": "Literal",
                                "location": {
                                  "start-line": 18,
                                  "end-line": 18,
                                  "start-col": 21,
                                  "end-col": 24,
                                  "file": "/home/tritlo/WASPProj/parser/src/test/java/se/wasp/AppTest.java"
                                },
                                "children": []
                              }
                            ]
                          },
                          {
                            "label": "Rigorous Test :-)",
                            "type": "JavaDoc",
                            "location": {
                              "start-line": 12,
                              "end-line": 14,
                              "start-col": 5,
                              "end-col": 7,
                              "file": "/home/tritlo/WASPProj/parser/src/test/java/se/wasp/AppTest.java"
                            },
                            "children": []
                          }
                        ]
                      },
                      {
                        "label": "Unit test for simple App.",
                        "type": "JavaDoc",
                        "location": {
                          "start-line": 7,
                          "end-line": 9,
                          "start-col": 1,
                          "end-col": 3,
                          "file": "/home/tritlo/WASPProj/parser/src/test/java/se/wasp/AppTest.java"
                        },
                        "children": []
                      }
                    ]
                  }
                ]
              }
            ]
          }
        ]
      }
    ]
  }
]
```
