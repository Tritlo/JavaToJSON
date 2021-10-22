JavaToJson
===

Parses a list of Java source files to a list of JSON, using Spoon and gumtree-spoon-ast-diff

Usage
---

Compile with

`mvn clean install`

Run with

`java -jar target/parser-1.0-jar-with-dependencies <file1.java> <file2.java> <etc...>`


Example:
---

The source for this project, in `Parser.java`:

```
package se.wasp.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
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
            try {
                return (new Json4SpoonGenerator()).getJSONasJsonObject(Launcher.parseClass(FileUtils.readFileToString(new File(file), "utf8")));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

        }).filter( obj -> obj != null).toList();

       System.out.println((new Gson()).toJson(results));
    }
}


```

The source for the "tests" for this project, in `AppTest.java`:

```
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

`java -jar target/parser-1.0-jar-with-dependencies.jar src/main/java/se/wasp/parser/Parser.java src/test/java/se/wasp/AppTest.java`

```
[
  {
    "label": "",
    "type": "root",
    "children": [
      {
        "label": "Parser",
        "type": "Class",
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
            "children": [
              {
                "label": "void",
                "type": "RETURN_TYPE",
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
                "children": [
                  {
                    "label": "java.lang.String[]",
                    "type": "VARIABLE_TYPE",
                    "children": []
                  }
                ]
              },
              {
                "label": "results",
                "type": "LocalVariable",
                "children": [
                  {
                    "label": "se.wasp.parser.var",
                    "type": "VARIABLE_TYPE",
                    "children": []
                  },
                  {
                    "label": "toList",
                    "type": "Invocation",
                    "children": [
                      {
                        "label": "filter",
                        "type": "Invocation",
                        "children": [
                          {
                            "label": "map",
                            "type": "Invocation",
                            "children": [
                              {
                                "label": "stream",
                                "type": "Invocation",
                                "children": [
                                  {
                                    "label": "java.util.Arrays",
                                    "type": "TypeAccess",
                                    "children": []
                                  },
                                  {
                                    "label": "args",
                                    "type": "VariableRead",
                                    "children": []
                                  }
                                ]
                              },
                              {
                                "label": "lambda$0",
                                "type": "Lambda",
                                "children": [
                                  {
                                    "label": "file",
                                    "type": "Parameter",
                                    "children": [
                                      {
                                        "label": "java.lang.String",
                                        "type": "VARIABLE_TYPE",
                                        "children": []
                                      }
                                    ]
                                  },
                                  {
                                    "label": "",
                                    "type": "Try",
                                    "children": [
                                      {
                                        "label": "return",
                                        "type": "Return",
                                        "children": [
                                          {
                                            "label": "getJSONasJsonObject",
                                            "type": "Invocation",
                                            "children": [
                                              {
                                                "label": "gumtree.spoon.builder.Json4SpoonGenerator()",
                                                "type": "ConstructorCall",
                                                "children": []
                                              },
                                              {
                                                "label": "parseClass",
                                                "type": "Invocation",
                                                "children": [
                                                  {
                                                    "label": "spoon.Launcher",
                                                    "type": "TypeAccess",
                                                    "children": []
                                                  },
                                                  {
                                                    "label": "readFileToString",
                                                    "type": "Invocation",
                                                    "children": [
                                                      {
                                                        "label": "org.apache.commons.io.FileUtils",
                                                        "type": "TypeAccess",
                                                        "children": []
                                                      },
                                                      {
                                                        "label": "java.io.File(java.lang.String)",
                                                        "type": "ConstructorCall",
                                                        "children": [
                                                          {
                                                            "label": "file",
                                                            "type": "VariableRead",
                                                            "children": []
                                                          }
                                                        ]
                                                      },
                                                      {
                                                        "label": "\"utf8\"",
                                                        "type": "Literal",
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
                                        "label": "",
                                        "type": "Catch",
                                        "children": [
                                          {
                                            "label": "e",
                                            "type": "CatchVariable",
                                            "children": [
                                              {
                                                "label": "java.io.FileNotFoundException",
                                                "type": "VARIABLE_TYPE",
                                                "children": []
                                              }
                                            ]
                                          },
                                          {
                                            "label": "printStackTrace",
                                            "type": "Invocation",
                                            "children": [
                                              {
                                                "label": "e",
                                                "type": "VariableRead",
                                                "children": []
                                              }
                                            ]
                                          },
                                          {
                                            "label": "return",
                                            "type": "Return",
                                            "children": [
                                              {
                                                "label": "null",
                                                "type": "Literal",
                                                "children": []
                                              }
                                            ]
                                          }
                                        ]
                                      },
                                      {
                                        "label": "",
                                        "type": "Catch",
                                        "children": [
                                          {
                                            "label": "e",
                                            "type": "CatchVariable",
                                            "children": [
                                              {
                                                "label": "java.io.IOException",
                                                "type": "VARIABLE_TYPE",
                                                "children": []
                                              }
                                            ]
                                          },
                                          {
                                            "label": "printStackTrace",
                                            "type": "Invocation",
                                            "children": [
                                              {
                                                "label": "e",
                                                "type": "VariableRead",
                                                "children": []
                                              }
                                            ]
                                          },
                                          {
                                            "label": "return",
                                            "type": "Return",
                                            "children": [
                                              {
                                                "label": "null",
                                                "type": "Literal",
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
                            "label": "lambda$1",
                            "type": "Lambda",
                            "children": [
                              {
                                "label": "obj",
                                "type": "Parameter",
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
                                "children": [
                                  {
                                    "label": "obj",
                                    "type": "VariableRead",
                                    "children": []
                                  },
                                  {
                                    "label": "null",
                                    "type": "Literal",
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
                "children": [
                  {
                    "label": "out",
                    "type": "FieldRead",
                    "children": [
                      {
                        "label": "java.lang.System",
                        "type": "TypeAccess",
                        "children": []
                      }
                    ]
                  },
                  {
                    "label": "toJson",
                    "type": "Invocation",
                    "children": [
                      {
                        "label": "com.google.gson.Gson()",
                        "type": "ConstructorCall",
                        "children": []
                      },
                      {
                        "label": "results",
                        "type": "VariableRead",
                        "children": []
                      }
                    ]
                  }
                ]
              }
            ]
          },
          {
            "label": "Hello world!",
            "type": "JavaDoc",
            "children": []
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
        "label": "AppTest",
        "type": "Class",
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
            "children": [
              {
                "label": "void",
                "type": "RETURN_TYPE",
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
                "label": "@Test",
                "type": "Annotation",
                "children": []
              },
              {
                "label": "assertTrue",
                "type": "Invocation",
                "children": [
                  {
                    "label": "true",
                    "type": "Literal",
                    "children": []
                  }
                ]
              },
              {
                "label": "Rigorous Test :-)",
                "type": "JavaDoc",
                "children": []
              }
            ]
          },
          {
            "label": "Unit test for simple App.",
            "type": "JavaDoc",
            "children": []
          }
        ]
      }
    ]
  }
]
```