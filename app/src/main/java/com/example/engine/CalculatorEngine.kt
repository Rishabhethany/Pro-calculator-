package com.example.engine

import com.example.model.CalculationResult
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import java.util.Stack
import kotlin.math.*

enum class AngleMode {
    DEG, RAD, GRAD
}

object CalculatorEngine {

    private val MATH_CTX = MathContext(16, RoundingMode.HALF_UP)

    fun evaluate(expression: String, angleMode: AngleMode = AngleMode.DEG): CalculationResult {
        val trimmed = expression.trim()
        if (trimmed.isEmpty()) {
            return CalculationResult(resultString = "0", numericValue = 0.0)
        }

        return try {
            val tokens = tokenize(trimmed)
            if (tokens.isEmpty()) {
                return CalculationResult(resultString = "0", numericValue = 0.0)
            }
            val postfix = infixToPostfix(tokens)
            val result = evaluatePostfix(postfix, angleMode)

            // Format nicely
            val formatted = formatResult(result)
            CalculationResult(
                resultString = formatted,
                numericValue = result
            )
        } catch (e: ArithmeticException) {
            CalculationResult(
                resultString = e.message ?: "Math Error",
                isError = true,
                errorMessage = e.message ?: "Math Error"
            )
        } catch (e: IllegalArgumentException) {
            CalculationResult(
                resultString = e.message ?: "Invalid Expression",
                isError = true,
                errorMessage = e.message ?: "Invalid Expression"
            )
        } catch (e: Exception) {
            CalculationResult(
                resultString = "Invalid expression",
                isError = true,
                errorMessage = "Invalid expression"
            )
        }
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var i = 0
        val len = expr.length

        // Normalized characters
        val clean = expr.replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("√", "sqrt")
            .replace("π", "pi")

        while (i < len) {
            val c = clean[i]
            when {
                c.isWhitespace() -> {
                    i++
                }
                c.isDigit() || c == '.' -> {
                    val sb = StringBuilder()
                    while (i < len && (clean[i].isDigit() || clean[i] == '.')) {
                        sb.append(clean[i])
                        i++
                    }
                    // Handle scientific E notation: e.g. 1.23E4
                    if (i < len && (clean[i] == 'E' || clean[i] == 'e')) {
                        sb.append('E')
                        i++
                        if (i < len && (clean[i] == '+' || clean[i] == '-')) {
                            sb.append(clean[i])
                            i++
                        }
                        while (i < len && clean[i].isDigit()) {
                            sb.append(clean[i])
                            i++
                        }
                    }
                    tokens.add(sb.toString())
                }
                c.isLetter() -> {
                    val sb = StringBuilder()
                    while (i < len && (clean[i].isLetter() || clean[i].isDigit())) {
                        sb.append(clean[i])
                        i++
                    }
                    val word = sb.toString()
                    tokens.add(word)
                }
                c == '+' || c == '-' -> {
                    // Check if unary minus / plus
                    val isUnary = tokens.isEmpty() ||
                            tokens.last() in listOf("+", "-", "*", "/", "%", "^", "(", "mod")
                    if (isUnary && c == '-') {
                        // Check if immediately followed by a number
                        if (i + 1 < len && (clean[i + 1].isDigit() || clean[i + 1] == '.')) {
                            i++
                            val sb = StringBuilder("-")
                            while (i < len && (clean[i].isDigit() || clean[i] == '.')) {
                                sb.append(clean[i])
                                i++
                            }
                            tokens.add(sb.toString())
                        } else {
                            tokens.add("neg")
                            i++
                        }
                    } else if (isUnary && c == '+') {
                        // Unary plus can be ignored
                        i++
                    } else {
                        tokens.add(c.toString())
                        i++
                    }
                }
                c == '*' || c == '/' || c == '(' || c == ')' || c == '^' || c == '%' || c == '!' -> {
                    tokens.add(c.toString())
                    i++
                }
                else -> {
                    i++
                }
            }
        }
        return tokens
    }

    private val OPERATORS = mapOf(
        "+" to 1,
        "-" to 1,
        "*" to 2,
        "/" to 2,
        "%" to 2,
        "mod" to 2,
        "^" to 3,
        "neg" to 4,
        "!" to 4
    )

    private val FUNCTIONS = setOf(
        "sin", "cos", "tan",
        "asin", "acos", "atan",
        "sinh", "cosh", "tanh",
        "ln", "log", "log2",
        "sqrt", "cbrt",
        "abs", "floor", "ceil",
        "inv" // 1/x
    )

    private fun infixToPostfix(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val stack = Stack<String>()

        for (token in tokens) {
            when {
                token.toDoubleOrNull() != null || token == "pi" || token == "e" -> {
                    output.add(token)
                }
                token in FUNCTIONS -> {
                    stack.push(token)
                }
                token == "(" -> {
                    stack.push(token)
                }
                token == ")" -> {
                    while (stack.isNotEmpty() && stack.peek() != "(") {
                        output.add(stack.pop())
                    }
                    if (stack.isEmpty()) {
                        throw IllegalArgumentException("Mismatched parentheses")
                    }
                    stack.pop() // remove '('
                    if (stack.isNotEmpty() && stack.peek() in FUNCTIONS) {
                        output.add(stack.pop())
                    }
                }
                token in OPERATORS -> {
                    val prec = OPERATORS[token] ?: 0
                    while (stack.isNotEmpty() && stack.peek() != "(" &&
                        ((OPERATORS[stack.peek()] ?: 0) >= prec && token != "^")
                    ) {
                        output.add(stack.pop())
                    }
                    stack.push(token)
                }
                else -> {
                    throw IllegalArgumentException("Unknown operator: $token")
                }
            }
        }

        while (stack.isNotEmpty()) {
            val op = stack.pop()
            if (op == "(" || op == ")") {
                throw IllegalArgumentException("Mismatched parentheses")
            }
            output.add(op)
        }

        return output
    }

    private fun evaluatePostfix(postfix: List<String>, angleMode: AngleMode): Double {
        val stack = Stack<Double>()

        for (token in postfix) {
            when {
                token == "pi" -> stack.push(Math.PI)
                token == "e" -> stack.push(Math.E)
                token.toDoubleOrNull() != null -> {
                    stack.push(token.toDouble())
                }
                token == "neg" -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("Invalid syntax")
                    stack.push(-stack.pop())
                }
                token == "!" -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("Invalid syntax")
                    val v = stack.pop()
                    if (v < 0 || v != floor(v)) throw ArithmeticException("Factorial of non-positive integer")
                    if (v > 170) throw ArithmeticException("Number too large")
                    stack.push(factorial(v.toLong()).toDouble())
                }
                token in FUNCTIONS -> {
                    if (stack.isEmpty()) throw IllegalArgumentException("Invalid syntax")
                    val v = stack.pop()
                    val res = applyFunction(token, v, angleMode)
                    stack.push(res)
                }
                token in OPERATORS -> {
                    if (stack.size < 2) throw IllegalArgumentException("Invalid syntax")
                    val b = stack.pop()
                    val a = stack.pop()
                    val res = applyBinaryOp(token, a, b)
                    stack.push(res)
                }
                else -> throw IllegalArgumentException("Unknown token: $token")
            }
        }

        if (stack.size != 1) {
            throw IllegalArgumentException("Invalid expression")
        }

        val res = stack.pop()
        if (res.isNaN()) throw ArithmeticException("Invalid expression")
        if (res.isInfinite()) throw ArithmeticException("Number too large")
        return res
    }

    private fun applyFunction(fn: String, x: Double, angleMode: AngleMode): Double {
        val rad = toRadians(x, angleMode)
        return when (fn) {
            "sin" -> sin(rad)
            "cos" -> cos(rad)
            "tan" -> {
                val cosVal = cos(rad)
                if (abs(cosVal) < 1e-15) throw ArithmeticException("Tangent undefined")
                sin(rad) / cosVal
            }
            "asin" -> {
                if (x < -1.0 || x > 1.0) throw ArithmeticException("Domain error for asin")
                fromRadians(asin(x), angleMode)
            }
            "acos" -> {
                if (x < -1.0 || x > 1.0) throw ArithmeticException("Domain error for acos")
                fromRadians(acos(x), angleMode)
            }
            "atan" -> fromRadians(atan(x), angleMode)
            "sinh" -> sinh(x)
            "cosh" -> cosh(x)
            "tanh" -> tanh(x)
            "ln" -> {
                if (x <= 0) throw ArithmeticException("Log of non-positive number")
                ln(x)
            }
            "log" -> {
                if (x <= 0) throw ArithmeticException("Log of non-positive number")
                log10(x)
            }
            "log2" -> {
                if (x <= 0) throw ArithmeticException("Log of non-positive number")
                ln(x) / ln(2.0)
            }
            "sqrt" -> {
                if (x < 0) throw ArithmeticException("Square root of negative number")
                sqrt(x)
            }
            "cbrt" -> cbrt(x)
            "abs" -> abs(x)
            "floor" -> floor(x)
            "ceil" -> ceil(x)
            "inv" -> {
                if (x == 0.0) throw ArithmeticException("Cannot divide by zero")
                1.0 / x
            }
            else -> throw IllegalArgumentException("Unknown function: $fn")
        }
    }

    private fun toRadians(angle: Double, mode: AngleMode): Double {
        return when (mode) {
            AngleMode.RAD -> angle
            AngleMode.DEG -> Math.toRadians(angle)
            AngleMode.GRAD -> angle * (Math.PI / 200.0)
        }
    }

    private fun fromRadians(rad: Double, mode: AngleMode): Double {
        return when (mode) {
            AngleMode.RAD -> rad
            AngleMode.DEG -> Math.toDegrees(rad)
            AngleMode.GRAD -> rad * (200.0 / Math.PI)
        }
    }

    private fun applyBinaryOp(op: String, a: Double, b: Double): Double {
        return when (op) {
            "+" -> a + b
            "-" -> a - b
            "*" -> a * b
            "/" -> {
                if (b == 0.0) throw ArithmeticException("Cannot divide by zero")
                a / b
            }
            "%" -> (a * b) / 100.0
            "mod" -> {
                if (b == 0.0) throw ArithmeticException("Cannot divide by zero")
                a % b
            }
            "^" -> {
                val res = a.pow(b)
                if (res.isNaN()) throw ArithmeticException("Invalid power operation")
                res
            }
            else -> throw IllegalArgumentException("Unknown binary op $op")
        }
    }

    fun factorial(n: Long): Double {
        if (n < 0) throw ArithmeticException("Factorial of negative number")
        if (n > 170) throw ArithmeticException("Number too large")
        var res = 1.0
        for (i in 2..n) {
            res *= i
        }
        return res
    }

    fun gcd(a: Long, b: Long): Long {
        var x = abs(a)
        var y = abs(b)
        while (y != 0L) {
            val t = y
            y = x % y
            x = t
        }
        return x
    }

    fun lcm(a: Long, b: Long): Long {
        if (a == 0L || b == 0L) return 0L
        return abs(a * b) / gcd(a, b)
    }

    fun isPrime(n: Long): Boolean {
        if (n <= 1) return false
        if (n <= 3) return true
        if (n % 2 == 0L || n % 3 == 0L) return false
        var i = 5L
        while (i * i <= n) {
            if (n % i == 0L || n % (i + 2) == 0L) return false
            i += 6
        }
        return true
    }

    fun permutations(n: Long, r: Long): Double {
        if (n < 0 || r < 0 || r > n) throw IllegalArgumentException("r must be between 0 and n")
        var res = 1.0
        for (i in (n - r + 1)..n) {
            res *= i
        }
        return res
    }

    fun combinations(n: Long, r: Long): Double {
        if (n < 0 || r < 0 || r > n) throw IllegalArgumentException("r must be between 0 and n")
        val p = permutations(n, r)
        val f = factorial(r)
        return p / f
    }

    fun formatResult(value: Double): String {
        if (value.isNaN()) return "NaN"
        if (value.isInfinite()) return if (value > 0) "Infinity" else "-Infinity"

        // Round tiny floating precision artifacts (e.g. sin(180) -> 0.0)
        val rounded = if (abs(value) < 1e-12) 0.0 else value

        // Check if integer
        if (rounded == floor(rounded) && abs(rounded) < 1e14) {
            return rounded.toLong().toString()
        }

        // Check for scientific notation needed
        if (abs(rounded) >= 1e12 || (abs(rounded) > 0 && abs(rounded) < 1e-6)) {
            return String.format("%.6E", rounded)
        }

        // Format to max 10 decimal places, stripping trailing zeros
        val bd = BigDecimal(rounded.toString(), MATH_CTX)
            .stripTrailingZeros()
        return bd.toPlainString()
    }
}
