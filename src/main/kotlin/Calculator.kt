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
    var bladDziel : Boolean = false
    var znak : Char = '\u0000'

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
        if(bladDziel){
            display1.text = ""
            bladDziel = false
        }
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
            //Zamiana String na Char
            if(x != "+/-")
                znak = x[0]
            if (display.text.isEmpty() && x == "+/-") return
            if (display.text.isEmpty() && display1.text.isEmpty() && (x[0] in znaki || znak == ')')) return
            if (display.text.isEmpty() && display1.text.isNotEmpty() && znak == ')')
                if ((display1.text.count { c: Char -> c == '('} < (display1.text.count { c: Char -> c == ')'} + 1))) return
            if(x == "C"){
                czyszczenie()
            }
            else{
                //Zapobiega wpisanu paru znakow zaraz po sobie
                if (display.text.isEmpty() && display1.text.isNotEmpty())
                        if (znak in znaki && display1.text.last() in znaki) return
                if (x == "+/-"){
                    if(display.text.contains('.')){
                        val temp1 : Double = display.text.toDouble()
                        val temp2 : Double = -temp1
                        display.text = temp2.toString()
                    } else {
                        val temp3 : Int = display.text.toInt()
                        val temp4 : Int = -temp3
                        display.text = temp4.toString()
                    }
                    return
                }
                else
                {
                    display1.text += display.text
                    display1.text += x
                }
            }
            when (znak) {
                '+','-' -> {
                    sprawdz_nawias()
                    display.text = ""
                    if(znak == '-' && display1.text[display1.text.lastIndex - 1] == '(') stosWartosci.add(0.0)
                    if (!(pierwsza_liczba)){
                        if(stosZnakow.peek() in arrayOf('+','-','*','/'))
                            do dzialanie()
                            while(stosZnakow.isNotEmpty() && stosZnakow.peek() in arrayOf('+','-'))
                    }
                    else pierwsza_liczba = false
                    stosZnakow.add(znak)
                }
                '/','*' -> {
                    sprawdz_nawias()
                    display.text = ""
                    if (!(pierwsza_liczba)){
                        if(stosZnakow.peek() in arrayOf('*','/'))
                            dzialanie()
                    }
                    else pierwsza_liczba = false
                    stosZnakow.add(znak)
                }
                '=' -> {
                    sprawdz_nawias()
                    display.text = ""
                    if(stosZnakow.isEmpty() && stosWartosci.size == 1){
                        display1.text += stosWartosci.peek()
                        return
                    }
                    do dzialanie()
                    while(stosZnakow.isNotEmpty())
                    pozostale_obliczenia()
                }
                ')' -> {
                    sprawdz_nawias()
                    display.text = ""

                    if(stosZnakow.peek() != '(') {
                        do dzialanie()
                        while (stosZnakow.isNotEmpty() && stosZnakow.peek() != '(')
                    }
                    else{
                        if(stosZnakow.isEmpty()){
                            display.text = stosWartosci.peek().toString()
                        }
                        wyswietlono_wynik = true
                    }

                    if(stosZnakow.isNotEmpty()) stosZnakow.pop()

                    wyswietl_log()
                    if(stosWartosci.size == 1) pierwsza_liczba = true
                }
                '(' -> {
                    stosZnakow.add(znak)
                }
            }
        }
    }

    fun dzialanie(){
        if(stosZnakow.isNotEmpty() && stosWartosci.isNotEmpty()) {
            wyswietl_log()

            when (stosZnakow.pop()) {
                '+' -> display.text = stosWartosci.push(dodawanie(stosWartosci.pop(), stosWartosci.pop())).toString()
                '-' -> display.text = stosWartosci.push(odejmowanie(stosWartosci.pop(), stosWartosci.pop())).toString()
                '*' -> display.text = stosWartosci.push(mnozenie(stosWartosci.pop(), stosWartosci.pop())).toString()
                '/' -> {if(stosWartosci.peek() != 0.0)
                    display.text = stosWartosci.push(dzielenie(stosWartosci.pop(), stosWartosci.pop())).toString()
                    else bladDzielZero()
                }
            }
            wyswietlono_wynik = true
            wyswietl_log()

            pozostale_obliczenia()
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
        pierwsza_liczba = true
    }

    fun sprawdz_nawias(){
        //Przed ostatni znak
        if(display1.text.isNotEmpty() && display.text.isNotEmpty())
            if(display1.text[display1.text.lastIndex - 1] != ')')
                stosWartosci.add(displayValue)
    }

    fun wyswietl_log(){
        println("\t stosWartosci: $stosWartosci")
        println("\t stosZnakow: $stosZnakow")
    }

    fun pozostale_obliczenia(){
        if(stosZnakow.isEmpty() && stosWartosci.isNotEmpty() && stosWartosci.size > 1){
            do{
                wyswietl_log()
                display.text = stosWartosci.push(dodawanie(stosWartosci.pop(), stosWartosci.pop())).toString()
                wyswietl_log()
            }
            while(stosWartosci.isEmpty())
        }
    }

    fun bladDzielZero(){
        display1.text = "Błąd dzielenia przez 0"
        display.text = ""
        stosWartosci.clear()
        stosZnakow.clear()
        pierwsza_liczba = true
        bladDziel = true
    }
}
