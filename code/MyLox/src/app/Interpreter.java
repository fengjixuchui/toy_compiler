package app;

import java.util.List;
import java.util.ArrayList;

/**
 * Interpreter
 */

class RuntimeError extends RuntimeException {
    final Token token;

    RuntimeError(Token token, String message) {
        super(message);
        this.token = token;
    }
}

class Return extends RuntimeException {
    final Object value;

    Return(Object value) {
        super(null, null, false, false);
        this.value = value;
    }
}

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    Environment environment = new Environment();

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        Object left = evaluate(expr.left);
        if (isTruthy(left)) {
            // 如果是 or 直接返回 left
            // 如果是 and 返回 right
            // 由返回式的真假判断真假
            if (expr.operator.type == TokenType.OR) {
                return left;
            } else {
                return evaluate(expr.right);
            }
        }
        // 左值为假
        // 如果是 or 返回右值
        if (expr.operator.type == TokenType.OR) {
            return evaluate(expr.right);
        }

        // 如果是 and
        // 返回假的左值
        return left;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        // 执行表达式
        return evaluate(expr.expression);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Token operator = expr.operator;
        Object right = evaluate(expr.right);

        switch (operator.type) {
        case MINUS:
            checkNumberOperands(operator, right);
            return -(double) right;
        case BANG:
            // OR
            return !isTruthy(right);
        default:
            // 不会执行到这里
            break;
        }
        // 不会执行到这里
        return null;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        // 计算左右值
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
        case GREATER:
            checkNumberOperands(expr.operator, left, right);
            return (double) left > (double) right;
        case GREATER_EQUAL:
            checkNumberOperands(expr.operator, left, right);
            return (double) left >= (double) right;
        case LESS:
            checkNumberOperands(expr.operator, left, right);
            return (double) left < (double) right;
        case LESS_EQUAL:
            checkNumberOperands(expr.operator, left, right);
            return (double) left <= (double) right;
        case MINUS:
            checkNumberOperands(expr.operator, left, right);
            return (double) left - (double) right;
        case SLASH:
            checkNumberOperands(expr.operator, left, right);
            if ((double) right == 0) {
                throw new RuntimeError(expr.operator, "divide zero");
            }
            return (double) left / (double) right;
        case STAR:
            checkNumberOperands(expr.operator, left, right);
            return (double) left * (double) right;
        case BANG_EQUAL:
            return !isEqual(left, right);
        case EQUAL_EQUAL:
            return isEqual(left, right);
        case PLUS:
            if (left instanceof Double && right instanceof Double) {
                return (double) left + (double) right;
            }

            if (left instanceof String && right instanceof String) {
                return (String) left + (String) right;
            }
            throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
        default:
            break;
        }

        // Unreachable.

        return null;

    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        // Assign 的表达式是
        // Assign(Token name, Expr value)
        // 顺着作用域链给 Environment 中的「变量」赋值
        Token token = expr.name;
        Object value = evaluate(expr.value);

        environment.assign(token, value);

        return value;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        // 从环境中得到 token 的 value
        Object value = environment.get(expr.name);
        return value;
    }

    @Override
    public Object visitCallExpr(Expr.Call call) {
        // 执行函数
        Object callee = evaluate(call.callee);
        // 计算所有参数
        List<Object> args = new ArrayList<>();
        for (Expr arg : call.arguments) {
            Object value = evaluate(arg);
            args.add(value);
        }
        // call.callee 是一个 variable
        // 所以 evaluate 之后能得到环境中的函数
        LoxFunction function = (LoxFunction) callee;

        // 运行时错误，判断给定的参数是不是等于定义时的参数个数
        if (args.size() != function.arity()) {
            throw new RuntimeError(call.mark,
                    "Expected " + function.arity() + " arguments but got " + args.size() + ".");
        }
        return function.call(this, args);

    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        Expr condition = stmt.condition;
        Stmt thenBranch = stmt.thenBranch;
        Stmt elseBranch = stmt.elseBranch;

        if (isTruthy(evaluate(condition))) {
            execute(thenBranch);
        } else {
            execute(elseBranch);
        }
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        Expr condition = stmt.condition;
        Stmt body = stmt.body;

        while (isTruthy(evaluate(condition))) {
            execute(body);
        }
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        Object value = evaluate(stmt.expression);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        Object value = evaluate(stmt.expr);
        throw new Return(value);
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        // 给 environment 中的变量赋值
        String tokenName = stmt.name.lexeme;
        Expr expr = stmt.initializer;
        if (expr == null) {
            environment.define(tokenName, null);
            return null;
        }
        Object value = evaluate(expr);
        environment.define(tokenName, value);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function function) {
        // 定义函数
        // 在当前环境中定义函数并且将
        String name = function.name.lexeme;
        LoxFunction func = new LoxFunction(function, environment);
        environment.define(name, func);
        return null;
    }

    public void executeBlock(List<Stmt> statements, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;

            for (Stmt statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double)
            return;

        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private void checkNumberOperands(Token operator, Object operand) {
        if (operand instanceof Double)
            return;

        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private boolean isEqual(Object a, Object b) {
        /**
         * 当两者都为 null 的时候相等 调用 object equal 会调用子类重写的方法 equal
         */
        if (a == null && b == null) {
            return true;
        }

        if (a == null) {
            return false;
        }

        return a.equals(b);
    }

    // 执行表达式
    private Object evaluate(Expr expr) {
        return expr.accept(this);
    }

    // 执行语句
    private Object execute(Stmt stmt) {
        return stmt.accept(this);
    }

    // isTruthy
    private boolean isTruthy(Object obj) {
        /**
         * 什么时候是假：null、数字 0、语言本身记录真假 除此之外全为真
         */
        if (obj == null) {
            return false;
        }
        // 由于 obj 是数字是全部由 Double 代替
        if (obj instanceof Double) {
            double value = (double) obj;
            return (value != 0);
        }

        if (obj instanceof Boolean)
            return (boolean) obj;

        return true;
    }

    private String stringify(Object object) {
        if (object == null)
            return "nil";
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }
        return object.toString();
    }

    public void interpret(List<Stmt> statements) {
        try {
            for (Stmt statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError error) {
            MyLox.runtimeError(error);
        }
    }
}