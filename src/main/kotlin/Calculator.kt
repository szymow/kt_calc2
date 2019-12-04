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
    var pierwsza_liczba : Boolean = true
    var wyswietlono_wynik : Boolean = false

    init {
        title = "Calculator"

        root.lookupAll(".button").forEach { button ->
            button.setOnMouseClicked {
                op((button as Button).text)
            }
        }
    }

    val displayValue: Double
        get() = when (display.text) {
            "" -> 0.0
            else -> display.text.toDouble()
        }

    private fun op(x: String) {
        if(wyswietlono_wynik){
            display.text = ""
            wyswietlono_wynik = false
        }
        if(display1.text.contains("=")){
            czyszczenie()
        }
        if (Regex("[0-9.]").matches(x)) {
            if(x == "." && display.text.isEmpty()) display.text += "0$x"
            if(!(x == "." && display.text.contains("."))) display.text += x
        }
        else {
            if (display.text.isEmpty() && display1.text.isEmpty() && (x[0] in znaki || x[0] == ')')) return
            if(x == "C"){
                czyszczenie()
            }
            else{
                if (display.text.isEmpty() && display1.text.isNotEmpty() && display1.text.last() in znaki) return
                if (x == "+/-"){
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
                "+","-" -> {
                    stosWartosci.add(displayValue)
                    display.text = ""
                    if (!(pierwsza_liczba))
                        dzialanie()
                    else pierwsza_liczba = false
                    stosZnakow.add(x[0])
                }
                "/","*" -> {
                    stosWartosci.add(displayValue)
                    display.text = ""
                    if (!(pierwsza_liczba))
                        dzialanie()
                    else pierwsza_liczba = false
                    stosZnakow.add(x[0])
                }
                "=" -> {
                    stosWartosci.add(displayValue)
                    display.text = ""
                    dzialanie()
                }
                ")" -> {
                    stosWartosci.add(displayValue)
                    display.text = ""
                    dzialanie()
                }
            }
        }
    }

    fun dzialanie(){
        if(stosZnakow.isNotEmpty() && stosWartosci.isNotEmpty()) {

            println("\t stosWartosci: $stosWartosci")
            println("\t stosZnakow: $stosZnakow")

            when (stosZnakow.pop()) {
                '+' -> display.text = stosWartosci.push(dodawanie(stosWartosci.pop(), stosWartosci.pop())).toString()
                '-' -> display.text = stosWartosci.push(odejmowanie(stosWartosci.pop(), stosWartosci.pop())).toString()
                '*' -> display.text = stosWartosci.push(mnozenie(stosWartosci.pop(), stosWartosci.pop())).toString()
                '/' -> display.text = stosWartosci.push(dzielenie(stosWartosci.pop(), stosWartosci.pop())).toString()
            }
            wyswietlono_wynik = true
        }
    }

    //Zamiana kolejności itemów związana jest z kolejnością zdejmowania wartości ze stosu
    fun dodawanie(zmienna1: Double, zmienna2: Double): Double = zmienna2 + zmienna1
    fun odejmowanie(zmienna1: Double, zmienna2: Double): Double = zmienna2 - zmienna1
    fun mnozenie(zmienna1: Double, zmienna2: Double): Double = zmienna1 * zmienna2
    fun dzielenie(zmienna1: Double, zmienna2: Double) : Double = zmienna2 / zmienna1

    fun czyszczenie(){
        display.text = ""
        display1.text = ""
        stosWartosci.clear()
        stosZnakow.clear()
    }

}
