// expression → literal
// | unary
// | binary
// | grouping ;

// literal → NUMBER | STRING | "true" | "false" | "nil" ;
// grouping → "(" expression ")" ;
// unary → ( "-" | "!" ) expression ;
// binary → expression operator expression ;
// operator → "==" | "!=" | "<" | "<=" | ">" | ">="
// | "+" | "-" | "*" | "/" ;

/*
 * 生成树首先实现
 *
 * 下面的所有类都继承基类 Expr
 *  
 * Binary(Expr left, Token Opreator, Expr right)
 * 
 * Grouping(Expr expression)
 * 
 * Literal(Object Value)
 * 
 * Unary(Token operator, Expr right)
 * 
 * 由于是访问者模式
 * 
 * 所以基类有一个 accept
 * 
 */

package tools;

import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * GenerateAst
 */
public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage is: GenerateAst <output_dir>");
            System.exit(-1);
        }

        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList("Binary : Expr left, Token Opreator, Expr right",
                "Grouping : Expr expression", "Literal : Object value", "Unary : Token operator, Expr right"));
    }

    private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
        writer.println("    static class " + className + " extends " + baseName + " {");

        // Constructor.
        writer.println("        " + className + "(" + fieldList + ") {");

        // Store parameters in fields.
        String[] fields = fieldList.split(", ");
        for (String field : fields) {
            String name = field.split(" ")[1];
            writer.println("            this." + name + " = " + name + ";");
        }

        writer.println("        }");

        // Visitor pattern.
        writer.println();
        writer.println("        <R> R accept(Visitor<R> visitor) {");
        writer.println("            return visitor.visit" + className + baseName + "(this);");
        writer.println("        }");

        // Fields.
        writer.println();
        for (String field : fields) {
            writer.println("        final " + field + ";");
        }

        writer.println("    }");
    }

    private static void defineVisitor(PrintWriter writer, String baseName, List<String> types) {
        writer.println("    interface Visitor<R> {");

        for (String type : types) {
            String typeName = type.split(":")[0].trim();
            writer.println("        R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
        }

        writer.println("    }");
    }

    private static void defineAst(String outputDir, String basename, List<String> types) throws IOException {
        String path = outputDir + "/" + basename + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        // 开始生成表达式的基类
        writer.println("package app;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + basename + " {");

        // 定义基类抽象方法 accept
        writer.println("    abstract <R> R accept(Visitor<R> visitor);");

        // 定义抽象类内部 Vistor 接口
        defineVisitor(writer, basename, types);

        for (String type : types) {
            String classname = type.split(":")[0].trim();
            String fields = type.split(":")[1].trim();
            defineType(writer, basename, classname, fields);
        }

        writer.println("}");
        writer.close();
    }
}