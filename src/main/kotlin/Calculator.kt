import Op.*
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.input.KeyEvent
import javafx.scene.layout.VBox
import javafx.stage.Stage
import tornadofx.App
import tornadofx.View
import tornadofx.importStylesheet

import java.util.Stack

var stosWartosci = Stack<Double>()
var stosZnakow = Stack<Char>()

class CalculatorApp : App() {
    override val primaryView = Calculator::class

    override fun start(stage: Stage) {
        importStylesheet("/style.css")
        stage.isResizable = false
        super.start(stage)
    }
}

class Calculator : View() {
    override val root: VBox by fxml()
    @FXML lateinit var display: Label
    @FXML lateinit var display1: Label

    val znaki = charArrayOf('+','-','*','/')

    init {
        title = "Calculator"

        root.lookupAll(".button").forEach { button ->
            button.setOnMouseClicked {
                op((button as Button).text)
            }
        }
    }

    var curried: Op = add(0.0)

    fun opAction(fn: Op) {
        curried = fn
        display.text = ""
    }

    val displayValue: Double
        get() = when (display.text) {
            "" -> 0.0
            else -> display.text.toDouble()
        }

    private fun op(x: String): Unit {
        if(display1.text.contains("=")){
            display.text = ""
            display1.text = ""
        }
        if (Regex("[0-9.]").matches(x)) {
            if(x == "." && display.text.isEmpty()) display.text += "0" + x
            if(x == "." && display.text.contains("."))
            else
                display.text += x
        } else {
            if (display.text.isEmpty() && display1.text.isEmpty() && (x[0] in znaki || x[0] == ')')) return
            if(x == "C"){
                display1.text = ""
                stosWartosci.clear()
                stosZnakow.clear()
                opAction(add(0.0))
            }
            else{
                if (display.text.isEmpty() && display1.text.isNotEmpty() && display1.text.last() in znaki) return
                if ( x == "+/-"){
                    var temp : Double = display.text.toDouble()
                    temp = -temp
                    display.text = temp.toString()
                }
                else
                {
                    display1.text += display.text
                    display1.text += x
                }
            }
            when (x) {
                "+" -> {
                    stosWartosci.add(displayValue)
                    stosZnakow.add(x[0])
                    opAction(add(displayValue))
                }
                "-" -> {
                    stosWartosci.add(displayValue)
                    stosZnakow.add(x[0])
                    opAction(sub(displayValue))
                }
                "/" -> {
                    stosWartosci.add(displayValue)
                    stosZnakow.add(x[0])
                    opAction(div(displayValue))
                }
                "*" -> {
                    stosWartosci.add(displayValue)
                    stosZnakow.add(x[0])
                    opAction(mult(displayValue))
                }
                "=" -> {
                    display.text = curried.calc(displayValue).toString()
                }
            }
            println("\t stosWartosci: $stosWartosci")
            println("\t stosZnakow: $stosZnakow")
        }
    }
}

sealed class Op(val x: Double) {
    abstract fun calc(y: Double): Double
    class add(x: Double) : Op(x) { override fun calc(y: Double) = x + y }
    class sub(x: Double) : Op(x) { override fun calc(y: Double) = x - y }
    class mult(x: Double) : Op(x) { override fun calc(y: Double) = x * y }
    class div(x: Double) : Op(x) { override fun calc(y: Double) = x / y }
}


//Zamiana kolejności itemów związana jest z kolejnością zdejmowania wartości ze stosu
fun dodawanie(zmienna1: Double, zmienna2: Double): Double = zmienna2 + zmienna1
fun odejmowanie(zmienna1: Double, zmienna2: Double): Double = zmienna2 - zmienna1
fun mnozenie(zmienna1: Double, zmienna2: Double): Double = zmienna1 * zmienna2
fun dzielenie(zmienna1: Double, zmienna2: Double) : Double = zmienna2 / zmienna1