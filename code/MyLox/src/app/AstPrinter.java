// // 打印表达式树又叫做完美打印，用来检查语法树是否正确

// package app;

// import java.util.List;

// /**
//  * AstPrinter
//  */
// public class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {
//     String print(Expr expr) {
//         return expr.accept(this);
//     }

//     public String printStatements(List<Stmt> statements) {
//         String content = "";
//         int index = 0;
//         for (Stmt statement : statements) {
//             index++;
//             String t = statement.accept(this);
//             content += (t + '\n');
//         }

//         return content;
//     }

//     @Override
//     public String visitPrintStmt(Stmt.Print stmt) {
//         StringBuilder builder = new StringBuilder();

//         builder.append("(").append("PRINT ");
//         builder.append(print(stmt.expression));
//         builder.append(")");

//         return builder.toString();
//     }

//     @Override
//     public String visitExpressionStmt(Stmt.Expression stmt) {
//         StringBuilder builder = new StringBuilder();

//         builder.append("(").append("Expression ");
//         builder.append(print(stmt.expression));
//         builder.append(")");

//         return builder.toString();
//     }

//     @Override
//     public String visitBinaryExpr(Expr.Binary expr) {
//         return parenthesize(expr.operator.lexeme, expr.left, expr.right);
//     }

//     @Override
//     public String visitGroupingExpr(Expr.Grouping expr) {
//         return parenthesize("group", expr.expression);
//     }

//     @Override
//     public String visitLiteralExpr(Expr.Literal expr) {
//         if (expr.value == null)
//             return "nil";
//         return expr.value.toString();
//     }

//     @Override
//     public String visitUnaryExpr(Expr.Unary expr) {
//         return parenthesize(expr.operator.lexeme, expr.right);
//     }

//     private String parenthesize(String name, Expr... exprs) {
//         StringBuilder builder = new StringBuilder();

//         // 遍历所有的表达式
//         builder.append("(").append(name);
//         for (Expr expr : exprs) {
//             builder.append(" ").append(expr.accept(this));
//         }
//         builder.append(")");

//         return builder.toString();
//     }

//     public static void main(String[] args) {

//         Expr expression = new Expr.Binary(
//                 new Expr.Unary(new Token(TokenType.MINUS, "-", null, 1), new Expr.Literal(123)),
//                 new Token(TokenType.STAR, "*", null, 1), new Expr.Grouping(new Expr.Literal(45.67)));

//         System.out.println(new AstPrinter().print(expression));

//     }
// }