package com.aizej.easyrlc

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.aizej.easyrlc.MainActivity.Position
import com.jetpack.multipledraggable.MultipleDraggableTheme
import com.jetpack.multipledraggable.Purple500
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberAxisLabelComponent
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.tecacet.komplex.Complex
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sign
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    var component_num_R = 1
    var component_num_L = 1
    var component_num_C = 1
    var component_num_w = 1
    var component_num_S = 1

    data class Point(val x: Float, val y: Float)
    data class Position(var x: Float, var y: Float)
    data class Component(
        var equation: String,
        var value: String,
        var input: MutableList<String>,
        var output: MutableList<String>,
        val frontPosition: MutableState<Position>,
        val backPosition: MutableState<Position>,
        val image: Int
    ){
        fun deepCopy(): Component {
            return Component(
                equation = this.equation,
                value = this.value,
                input = this.input.toMutableList(),      // Create a new mutable list with the same elements
                output = this.output.toMutableList(),    // Same for output
                frontPosition = mutableStateOf(this.frontPosition.value.copy()),
                backPosition = mutableStateOf(this.backPosition.value.copy()),
                image = this.image
            )
        }
    }

    val point_size = 40
    val round_to_decilal_places = 4
    val graph_lenght = 50
    val graph_absolute_start_at = 0.01
    val graph_absolute_end_at  = 1000000000.0
    val RLC_graph_from_to = 80.0
    val RL_RC_graph_from = 10.0
    val RL_RC_graph_to = 80.0
    val precision_steps = 30



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MultipleDraggableTheme (){
                Surface(color = MaterialTheme.colors.background) {
                    val textFieldColors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor              = MaterialTheme.colors.onSurface,
                        backgroundColor        = MaterialTheme.colors.surface,
                        cursorColor            = MaterialTheme.colors.primary,
                        focusedBorderColor     = MaterialTheme.colors.primary,
                        unfocusedBorderColor   = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled),
                        placeholderColor       = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.medium)
                    )

                    val Buttoncolors = ButtonDefaults.buttonColors(
                        backgroundColor = MaterialTheme.colors.primary,
                        contentColor    = MaterialTheme.colors.onPrimary,
                        disabledBackgroundColor = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled),
                        disabledContentColor    = MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled)
                    )

                    val Textcolor = MaterialTheme.colors.onBackground

                    // Use remember to keep components state across recompositions
                    val components = remember { mutableStateMapOf<String, Component>() }
                    var current_window_shown = remember { mutableStateOf("circuit") }
                    val show_stats = remember { mutableStateOf(false) }
                    var total_equation = remember {mutableStateOf("")}
                    var abs_graph_data = remember { mutableStateOf(listOf<Point>()) }
                    var phase_graph_data = remember { mutableStateOf(listOf<Point>()) }
                    var graph_from = remember { mutableStateOf(graph_absolute_start_at) }
                    var graph_to = remember { mutableStateOf(graph_absolute_end_at) }
                    val Calculator_L = remember { mutableStateOf("") }
                    val Calculator_C = remember { mutableStateOf("") }
                    val Calculator_F = remember { mutableStateOf("") }
                    
                    val context = LocalContext.current

                    if(!components.contains("P"))
                    {
                        add_plus_and_minus(components)
                    }



                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {

                        if(current_window_shown.value == "circuit")
                        {
                        // Row with Buttons to add components
                            Spacer(Modifier.height(30.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .background(Purple500),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // Resistor section
                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(1f)
                                        .background(Color.White)
                                        .padding(5.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    var R_spawn_text by remember { mutableStateOf("") }

                                    Button(colors = Buttoncolors,
                                        onClick = {
                                        if(R_spawn_text == "")
                                        {
                                            Toast.makeText(context, "Chose value!", Toast.LENGTH_SHORT).show()
                                        }
                                        else{
                                            add_resistor_to_components(components, R_spawn_text)
                                        }
                                    }) {
                                        Text("R", color = Color.Black)
                                    }

                                    OutlinedTextField(
                                        colors = textFieldColors,
                                        value = R_spawn_text,
                                        onValueChange = { if (it.replace(".","").isDigitsOnly()) R_spawn_text = it },
                                        label = { Text("Ω", color = MaterialTheme.colors.onBackground) }
                                    )
                                }

                                // Inductor section
                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(1f)
                                        .background(Color.White)
                                        .padding(5.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    var L_spawn_text by remember { mutableStateOf("") }

                                    Button(colors = Buttoncolors,
                                        onClick = {
                                        if(L_spawn_text == "")
                                        {
                                            Toast.makeText(context, "Chose value!", Toast.LENGTH_SHORT).show()
                                        }
                                        else{
                                            add_inductor_to_components(components, L_spawn_text)
                                        }
                                    }) {
                                        Text("L", color = Color.Black)
                                    }

                                    OutlinedTextField(
                                        colors = textFieldColors,
                                        value = L_spawn_text,
                                        onValueChange = { if (it.replace(".","").isDigitsOnly()) L_spawn_text = it },
                                        label = { Text("H", color = MaterialTheme.colors.onBackground) }
                                    )
                                }

                                // Capacitor section
                                Column(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(1f)
                                        .background(Color.White)
                                        .padding(5.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    var C_spawn_text by remember { mutableStateOf("") }

                                    Button(colors = Buttoncolors,
                                        onClick = {
                                        if(C_spawn_text == "")
                                        {
                                            Toast.makeText(context, "Chose value!", Toast.LENGTH_SHORT).show()
                                        }
                                        else{
                                            add_capacitor_to_components(components, C_spawn_text)
                                        }
                                    }) {
                                        Text("C", color = Color.Black)
                                    }

                                    OutlinedTextField(
                                        colors = textFieldColors,
                                        value = C_spawn_text,
                                        onValueChange = { if (it.replace(".","").isDigitsOnly()) C_spawn_text = it },
                                        label = { Text("F", color = MaterialTheme.colors.onBackground) }
                                    )
                                }

                                Column(
                                    modifier = Modifier
                                        .weight(0.8f)
                                        .background(Color.White)
                                        .fillMaxSize(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Button(colors = Buttoncolors,
                                        onClick = {add_wire_to_components(components)}) {
                                        Text("WIRE", color = Color.Black)
                                    }
                                }
                            }
                        }


                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White)
                                .padding(5.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.Bottom,
                            horizontalAlignment = Alignment.Start
                        ) {
                            val abs_at_fr = remember { mutableStateOf(0.0) }
                            val phase_at_fr = remember { mutableStateOf(0.0) }
                            val fr = remember { mutableStateOf(-1.0) }
                            val fr_precision_error = remember { mutableStateOf(-1.0) }
                            val fmd = remember { mutableStateOf(-1.0) }
                            val fmh = remember { mutableStateOf(-1.0) }
                            val Q = remember { mutableStateOf(-1.0) }
                            val B = remember { mutableStateOf(-1.0) }
                            val show_equation = remember { mutableStateOf(false) }



                            Row (modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.End)
                            ){
                                if(current_window_shown.value == "graph")
                                {
                                    Button(colors = Buttoncolors,
                                        onClick = {show_equation.value = !show_equation.value })
                                    {
                                        Text("EQUATION", color = Color.Black)
                                    }

                                    Button(colors = Buttoncolors,
                                        onClick =
                                        {
                                            //check if 0 is in phase graph range
                                            val is_zero_in_range = sign(phase_graph_data.value[0].y) == -1*sign(phase_graph_data.value[graph_lenght-1].y)
                                            if (is_zero_in_range)
                                            {
                                                var result = find_phase(total_equation.value,graph_from.value,graph_to.value, precision_steps, 0.0)
                                                fr.value = result.first
                                                fr_precision_error.value = result.second

                                                //cant evaluate exactly at fr because there are issues with division with 0
                                                abs_at_fr.value = (amp_phase_from_complex(calculate_value(total_equation.value,fr.value+fr_precision_error.value)).first+
                                                        amp_phase_from_complex(calculate_value(total_equation.value,fr.value-fr_precision_error.value)).first)/2


                                                result = find_phase(total_equation.value,graph_from.value,graph_to.value, precision_steps, 45.0)
                                                val plus_45_phase = result.first
                                                val plus_45_phase_error = result.second


                                                result = find_phase(total_equation.value,graph_from.value,graph_to.value, precision_steps, -45.0)
                                                val minus_45_phase = result.first
                                                val minus_45_phase_error = result.second

                                                if (plus_45_phase> minus_45_phase)
                                                {
                                                    fmd.value = minus_45_phase
                                                    fmh.value = plus_45_phase
                                                }
                                                else
                                                {
                                                    fmd.value = plus_45_phase
                                                    fmh.value = minus_45_phase
                                                }

                                                B.value = fmh.value - fmd.value
                                                Q.value = fr.value/B.value
                                            }
                                            else
                                            {
                                                Toast.makeText(context, "Cannot find fr", Toast.LENGTH_LONG).show()
                                            }

                                            show_stats.value = !show_stats.value
                                        })
                                    {
                                        Text("STATS", color = Color.Black)
                                    }
                                }

                                if(current_window_shown.value == "circuit" || current_window_shown.value == "help")
                                {
                                    Button(colors = Buttoncolors,
                                        onClick = {
                                            if(current_window_shown.value == "circuit")
                                            {
                                                current_window_shown.value = "help"
                                            }
                                            else
                                            {
                                                current_window_shown.value = "circuit"
                                            }
                                        }) {
                                        Text("HELP", color = Color.Black)
                                    }
                                }


                                if(current_window_shown.value == "circuit" || current_window_shown.value == "calculator")
                                {
                                    Button(colors = Buttoncolors,
                                        onClick = {
                                            if(current_window_shown.value == "circuit")
                                            {
                                                current_window_shown.value = "calculator"
                                            }
                                            else
                                            {
                                                current_window_shown.value = "circuit"
                                            }
                                        }) {
                                        Text("CALCULATOR", color = Color.Black)
                                    }
                                }


                                if(current_window_shown.value == "circuit" || current_window_shown.value == "graph")
                                {
                                    Button(colors = Buttoncolors,
                                        onClick =
                                            {
                                                if(current_window_shown.value != "graph")
                                                {
                                                    show_stats.value = false
                                                    graph_from.value = graph_absolute_start_at
                                                    graph_to.value = graph_absolute_end_at


                                                    var solved_components = components.mapValues { it.value.deepCopy() }.toMutableMap()

                                                    if (solved_components.size != 2){
                                                        simplyfy(solved_components)
                                                        var the_component: String = ""
                                                        solved_components.entries.forEach { (key, _) -> the_component = key }

                                                        if (total_equation.value != "0" && solved_components.size == 1
                                                            && (solved_components[the_component]!!.input.contains("M") || solved_components[the_component]!!.output.contains("M"))
                                                            && (solved_components[the_component]!!.input.contains("P") || solved_components[the_component]!!.output.contains("P")))
                                                        {
                                                            solved_components.entries.forEach { (key, _) -> total_equation.value = solved_components[key]!!.equation } // there should be only one component

                                                            var data = getvalues_for_initial_graph(total_equation.value, graph_from.value,graph_to.value)
                                                            abs_graph_data.value = data.first
                                                            phase_graph_data.value = data.second
                                                            //Log.d("autorange_posible","${phase_graph_data.value[0].y != phase_graph_data.value[graph_lenght-1].y}")
                                                            if (phase_graph_data.value[0].y != phase_graph_data.value[graph_lenght-1].y)  // cant estimate graph for just L C
                                                            {
                                                                val newrange = get_range_automaticaly(phase_graph_data.value,total_equation.value)
                                                                Log.d("autorange:","${newrange}")
                                                                if (newrange.first != (-1).toFloat())
                                                                {
                                                                    if (newrange.first == newrange.second)
                                                                    {
                                                                        graph_from.value = (newrange.first*0.9).toDouble()
                                                                        graph_to.value = (newrange.second*1.1).toDouble()
                                                                    }
                                                                    else
                                                                    {
                                                                        graph_from.value = newrange.first.toDouble()
                                                                        graph_to.value = newrange.second.toDouble()
                                                                    }
                                                                }

                                                                data = getvalues_for_initial_graph(total_equation.value, graph_from.value,graph_to.value)
                                                                abs_graph_data.value = data.first
                                                                phase_graph_data.value = data.second
                                                            }


                                                            current_window_shown.value = "graph"
                                                        }
                                                        else
                                                        {
                                                            //Log.d("test","${total_equation.value}")
                                                            //Log.d("test","${solved_components.size}")
                                                            if (solved_components.isEmpty())
                                                            {
                                                                Toast.makeText(context, "The components need to be connected to RED and BLUE!", Toast.LENGTH_LONG).show()
                                                            }
                                                            else if (solved_components.size == 1)
                                                            {
                                                                Toast.makeText(context, "An Error occurred :(", Toast.LENGTH_LONG).show()
                                                            }
                                                            else
                                                            {
                                                                Toast.makeText(context, "Cant yet solve this circuit :(", Toast.LENGTH_LONG).show()
                                                            }
                                                        }
                                                    }
                                                    else{
                                                        if (current_window_shown.value == "circuit")
                                                        {
                                                            Toast.makeText(context, "Add or connect components to BLUE and RED", Toast.LENGTH_LONG).show()
                                                        }
                                                    }
                                                }
                                                else
                                                {
                                                    current_window_shown.value = "circuit"
                                                }
                                            })
                                    {
                                        Text("GRAPH", color = Color.Black)
                                    }
                                }
                            }



                            if (current_window_shown.value == "graph")
                            {
                                if(show_stats.value)
                                {
                                    Column(
                                        modifier = Modifier
                                            .background(Color.White)
                                            .padding(5.dp),
                                        verticalArrangement = Arrangement.Top,
                                        horizontalAlignment = Alignment.Start
                                    ) {

                                        if (show_equation.value)
                                        {
                                            SelectableText("Equation: ${total_equation.value.replace("x","f")}")
                                        }
                                        SelectableText("Resonation frequency (fr): ${fr.value} (+-${fr_precision_error.value})")
                                        SelectableText("Impedance on fr: ${abs_at_fr.value}")
                                        SelectableText("fmd: ${fmd.value}")
                                        SelectableText("fmh: ${fmh.value}")
                                        SelectableText("B: ${B.value}")
                                        SelectableText("Q: ${Q.value}")

                                    }
                                }


                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.White),
                                    verticalArrangement = Arrangement.Top,
                                    horizontalAlignment = Alignment.Start
                                ){
                                    Row () {
                                        var frequency_textfield_text by remember { mutableStateOf("") }
                                        var frequency_from = remember { mutableStateOf("") }
                                        var frequency_to = remember { mutableStateOf("") }
                                        val result_from_eqact_calc = remember { mutableStateOf(Complex(0.0, 0.0)) }
                                        val Z_amount = remember {mutableStateOf(0.0)}
                                        val Z_phase = remember {mutableStateOf(0.0)}
                                        val Z_real = remember {mutableStateOf(0.0)}
                                        val Z_imaginary = remember {mutableStateOf(0.0)}


                                        OutlinedTextField(
                                            colors = textFieldColors,
                                            modifier = Modifier
                                            .weight(2f),
                                            value = frequency_from.value,
                                            onValueChange = { if (it.replace(".","").isDigitsOnly()) frequency_from.value = it },
                                            label = { Text("from", color = MaterialTheme.colors.onBackground) }
                                        )
                                        OutlinedTextField(
                                            colors = textFieldColors,
                                            modifier = Modifier
                                            .weight(1.5f),
                                            value = frequency_to.value,
                                            onValueChange = { if (it.replace(".","").isDigitsOnly()) frequency_to.value = it },
                                            label = { Text("to", color = MaterialTheme.colors.onBackground) }
                                        )

                                        OutlinedTextField(
                                            colors = textFieldColors,
                                            modifier = Modifier
                                            .weight(2.3f),
                                            value = frequency_textfield_text,
                                            onValueChange = { if (it.replace(".","").isDigitsOnly()) frequency_textfield_text = it },
                                            label = { Text("exact", color = MaterialTheme.colors.onBackground) }
                                        )

                                        Button(colors = Buttoncolors,
                                            onClick = {
                                            if (frequency_from.value != "" && frequency_to.value != "")
                                            {
                                                graph_from.value = frequency_from.value.toDouble()
                                                graph_to.value = frequency_to.value.toDouble()
                                            }

                                            val data = getvalues_for_initial_graph(total_equation.value, graph_from.value,graph_to.value)
                                            abs_graph_data.value = data.first
                                            phase_graph_data.value = data.second

                                            if (frequency_textfield_text != "")
                                            {
                                                result_from_eqact_calc.value = calculate_value(total_equation.value,frequency_textfield_text.toDouble())

                                                Z_amount.value = sqrt(result_from_eqact_calc.value.real*result_from_eqact_calc.value.real+result_from_eqact_calc.value.img*result_from_eqact_calc.value.img)
                                                // round if too big
                                                if (Z_amount.value > 0.01)
                                                {
                                                    Z_amount.value = round(Z_amount.value, round_to_decilal_places)
                                                    Z_real.value = round(result_from_eqact_calc.value.real, round_to_decilal_places)
                                                    Z_imaginary.value = round(result_from_eqact_calc.value.img, round_to_decilal_places)
                                                }
                                                else
                                                {
                                                    Z_real.value = result_from_eqact_calc.value.real
                                                    Z_imaginary.value = result_from_eqact_calc.value.img
                                                }



                                                if (result_from_eqact_calc.value.real != 0.toDouble())
                                                {
                                                    Z_phase.value = (atan(result_from_eqact_calc.value.img/result_from_eqact_calc.value.real)/PI*180)
                                                }
                                                else
                                                {
                                                    if (result_from_eqact_calc.value.img == 0.toDouble())
                                                    {
                                                        Z_phase.value = 0.0
                                                    }
                                                    else if (result_from_eqact_calc.value.img < 0.toDouble())
                                                    {
                                                        Z_phase.value = -90.0
                                                    }
                                                    else{
                                                        Z_phase.value = 90.0
                                                    }
                                                }

                                                Z_phase.value = round(Z_phase.value,round_to_decilal_places)
                                            }
                                                         },
                                            modifier = Modifier
                                            .weight(1.2f)) {

                                            Text("=", color = Color.Black)
                                        }


                                        Column(modifier = Modifier
                                            .weight(4f))
                                        {
                                            Text("${Z_amount.value}∠${Z_phase.value}°", color = Color.Black)
                                            Text("(${Z_real.value}, i${Z_imaginary.value})", color = Color.Black)
                                        }
                                    }
                                    MyChart(abs_graph_data.value)
                                    MyChart(phase_graph_data.value)
                                }

                            }
                            Spacer(Modifier.height(50.dp))
                        }
                    }
                    if(current_window_shown.value == "circuit") {
                        components.entries.forEach { (key, _) ->
                            key(key) {
                                if (key == "P") {
                                    DraggablePointComposable(
                                        components = components,
                                        componenta_name = key,
                                        is_front = true,
                                        R.drawable.plus
                                    )
                                } else if (key == "M") {
                                    DraggablePointComposable(
                                        components = components,
                                        componenta_name = key,
                                        is_front = true,
                                        R.drawable.minus
                                    )
                                } else {
                                    DraggablePointComposable(
                                        components = components,
                                        componenta_name = key,
                                        is_front = true,
                                        R.drawable.point
                                    )
                                    DraggablePointComposable(
                                        components = components,
                                        componenta_name = key,
                                        is_front = false,
                                        R.drawable.point
                                    )
                                    ComponentComposable(
                                        components = components,
                                        componenta_name = key
                                    )
                                }
                            }
                        }
                        Connect_near_components(components = components)
                    }


                    if (current_window_shown.value == "calculator")
                    {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ){
                            Spacer(Modifier.height(50.dp))
                            Row (){

                                Column (modifier = Modifier
                                    .padding(5.dp),
                                    verticalArrangement = Arrangement.spacedBy(40.dp)){
                                    Text("Inductance:", color = MaterialTheme.colors.onBackground)
                                    Text("Capacitance:", color = MaterialTheme.colors.onBackground)
                                    Text("Resonation\n Frequency:", color = MaterialTheme.colors.onBackground)
                                }
                                Column {
                                    OutlinedTextField(
                                        colors = textFieldColors,
                                        value = Calculator_L.value,
                                        onValueChange = { if (it.replace(".","").replace("E","").replace("-","").replace("+","").isDigitsOnly()) Calculator_L.value = it },
                                        label = { Text("H", color = MaterialTheme.colors.onBackground) }
                                    )
                                    OutlinedTextField(
                                        colors = textFieldColors,
                                        value = Calculator_C.value,
                                        onValueChange = { if (it.replace(".","").replace("E","").replace("-","").replace("+","").isDigitsOnly()) Calculator_C.value = it },
                                        label = { Text("F", color = MaterialTheme.colors.onBackground) }
                                    )
                                    OutlinedTextField(
                                        colors = textFieldColors,
                                        value = Calculator_F.value,
                                        onValueChange = { if (it.replace(".","").replace("E","").replace("-","").replace("+","").isDigitsOnly()) Calculator_F.value = it },
                                        label = { Text("Hz", color = MaterialTheme.colors.onBackground) }
                                    )
                                }


                            }

                            Button(colors = Buttoncolors,
                                onClick = {

                                    if (bool_to_int(check_string_isnt_any_of_empty_or_not_number_or_zero(Calculator_L.value))
                                        +bool_to_int(check_string_isnt_any_of_empty_or_not_number_or_zero(Calculator_C.value))
                                        +bool_to_int(check_string_isnt_any_of_empty_or_not_number_or_zero(Calculator_F.value)) >= 2)
                                    {
                                        if (check_string_isnt_any_of_empty_or_not_number_or_zero(Calculator_L.value))
                                        {
                                            if (check_string_isnt_any_of_empty_or_not_number_or_zero(Calculator_C.value))
                                            {
                                                Calculator_F.value = (1/(2*PI*sqrt(Calculator_L.value.toDouble()*Calculator_C.value.toDouble()))).toString()
                                            }
                                            else
                                            {
                                                Calculator_C.value = (1/(4*PI.pow(2)*Calculator_L.value.toDouble()*Calculator_F.value.toDouble().pow(2))).toString()
                                            }
                                        }
                                        else
                                        {
                                            if (check_string_isnt_any_of_empty_or_not_number_or_zero(Calculator_C.value))
                                            {
                                                if (check_string_isnt_any_of_empty_or_not_number_or_zero(Calculator_F.value))
                                                {
                                                    Calculator_L.value = (1/(4*PI.pow(2)*Calculator_C.value.toDouble()*Calculator_F.value.toDouble().pow(2))).toString()
                                                }
                                            }
                                        }
                                    }
                                    else
                                    {
                                        Toast.makeText(context, "Chose 2 out of 3 values. The last one will be calculated!", Toast.LENGTH_LONG).show()
                                    }
                                })
                            {
                                Text("=", color = Color.Black)
                            }
                        }
                    }

                    if (current_window_shown.value == "help")
                    {
                        Column {
                            Spacer(Modifier.height(50.dp))
                            Text("Here are some useful tips:")
                            Text("-To delete a component drag it to the left side of the screen.\n-The first graph is the absolute value of impedance while the second is its phase.\n-The calculator screen needs 2 values. The last one will be calculated")
                        }
                    }
                }
            }
        }
    }

    fun get_range_automaticaly(points: List<Point>,equation: String): Pair<Float, Float> {

        val is_zero_in_range = sign(points[0].y) == -1*sign(points[graph_lenght-1].y)
        Log.d("autorange","$is_zero_in_range")
        if (is_zero_in_range)
        {
            val minus_60_freq = find_phase(equation,points[0].x.toDouble(), points[graph_lenght-1].x.toDouble(), 40 , -RLC_graph_from_to).first

            val plus_60_freq = find_phase(equation,points[0].x.toDouble(), points[graph_lenght-1].x.toDouble(), 40 , RLC_graph_from_to).first

            var from  = -1.0
            var to  = -1.0

            if (plus_60_freq > minus_60_freq)
            {
                from  = minus_60_freq
                to  = plus_60_freq
            }
            else
            {
                from  = plus_60_freq
                to  = minus_60_freq
            }
            return Pair(from.toFloat(),to.toFloat())
        }
        else {
            if(points[0].y > 0.0)
            {
                val plus_80_freq = find_phase(equation,points[0].x.toDouble(), points[graph_lenght-1].x.toDouble(), 40 , RL_RC_graph_to).first
                val plus_10_freq = find_phase(equation,points[0].x.toDouble(), points[graph_lenght-1].x.toDouble(), 40 , RL_RC_graph_from).first

                return Pair(min(plus_80_freq,plus_10_freq).toFloat(), max(plus_80_freq,plus_10_freq).toFloat())
            }
            else
            {
                val minus_80_freq = find_phase(equation,points[0].x.toDouble(), points[graph_lenght-1].x.toDouble(), 40 , -RL_RC_graph_to).first
                val minus_10_freq = find_phase(equation,points[0].x.toDouble(), points[graph_lenght-1].x.toDouble(), 40 , -RL_RC_graph_from).first

                return Pair(min(minus_80_freq,minus_10_freq).toFloat(), max(minus_80_freq,minus_10_freq).toFloat())
            }
        }
    }

    // Function to add resistor
    fun add_resistor_to_components(components: MutableMap<String, Component>, value: String)
    {
        val name = "R$component_num_R"
        components[name] = Component(
            equation = value,
            value = value,
            input = mutableListOf<String>(),
            output = mutableListOf<String>(),
            frontPosition = mutableStateOf(Position(getScreenWidth().toFloat()*9/10, getScreenHeight().toFloat()*3/5)),
            backPosition = mutableStateOf(Position(getScreenWidth().toFloat()*9/10, getScreenHeight().toFloat()*2/5)),
            image = R.drawable.resistor
        )
        component_num_R += 1
    }

    // Function to add inductor
    fun add_inductor_to_components(components: MutableMap<String, Component>, value: String)
    {
        val name = "L$component_num_L"
        components[name] = Component(
            equation = "i*2*π*x*$value",
            value = value,
            input = mutableListOf<String>(),
            output = mutableListOf<String>(),
            frontPosition = mutableStateOf(Position(getScreenWidth().toFloat()*9/10, getScreenHeight().toFloat()*3/5)),
            backPosition = mutableStateOf(Position(getScreenWidth().toFloat()*9/10, getScreenHeight().toFloat()*2/5)),
            image = R.drawable.inductor
        )

        component_num_L += 1
    }

    // Function to add capacitor
    fun add_capacitor_to_components(components: MutableMap<String, Component>, value: String)
    {
        val name = "C$component_num_C"
        components[name] = Component(
            equation = "1/(i*2*π*x*$value)",
            value = value,
            input = mutableListOf<String>(),
            output = mutableListOf<String>(),
            frontPosition = mutableStateOf(Position(getScreenWidth().toFloat()*9/10, getScreenHeight().toFloat()*3/5)),
            backPosition = mutableStateOf(Position(getScreenWidth().toFloat()*9/10, getScreenHeight().toFloat()*2/5)),
            image = R.drawable.capacitor
        )

        component_num_C += 1
    }

    fun add_wire_to_components(components: MutableMap<String, Component>)
    {
        val name = "W$component_num_w"
        components[name] = Component(
            equation = "0",
            value = "",
            input = mutableListOf<String>(),
            output = mutableListOf<String>(),
            frontPosition = mutableStateOf(Position(getScreenWidth().toFloat()*9/10, getScreenHeight().toFloat()*3/5)),
            backPosition = mutableStateOf(Position(getScreenWidth().toFloat()*9/10, getScreenHeight().toFloat()*2/5)),
            image = R.drawable.wire
        )
        component_num_w += 1
    }

    fun add_plus_and_minus(components: MutableMap<String, Component>)
    {
        components["P"] = Component(
            equation = "",
            value = "",
            input = mutableListOf<String>(),
            output = mutableListOf<String>(),
            frontPosition = mutableStateOf(Position(getScreenWidth().toFloat()*4/10, getScreenHeight().toFloat()*1/5)),
            backPosition = mutableStateOf(Position(getScreenWidth().toFloat()/2, 99999.0.toFloat())),
            image = R.drawable.plus
        )

        components["M"] = Component(
            equation = "",
            value = "",
            input = mutableListOf<String>(),
            output = mutableListOf<String>(),
            frontPosition = mutableStateOf(Position(getScreenWidth().toFloat()*4/10, getScreenHeight().toFloat()*8/10)),
            backPosition = mutableStateOf(Position(getScreenWidth().toFloat()/2, 99999.0.toFloat())),
            image = R.drawable.minus
        )
    }


    @Composable
    fun DraggablePointComposable(components: MutableMap<String, Component>, componenta_name: String, is_front: Boolean, image: Int)
    {
        val posState = if (is_front) {
            components[componenta_name]?.frontPosition
        } else {
            components[componenta_name]?.backPosition
        }

        val position = posState?.value ?: Position(0f, 0f)



        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = position.x.roundToInt(),
                        y = position.y.roundToInt()
                    )
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        posState?.value = Position(
                            posState.value.x + dragAmount.x,
                            posState.value.y + dragAmount.y
                        )
                    }
                }
                .size(point_size.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = image),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    @Composable
    fun ComponentComposable(components: MutableMap<String, Component>, componenta_name: String)
    {
        var x1 = components[componenta_name]?.frontPosition?.value?.x!!+point_size
        var y1 = components[componenta_name]?.frontPosition?.value?.y!!+point_size
        var x2 = components[componenta_name]?.backPosition?.value?.x!!+point_size
        var y2 = components[componenta_name]?.backPosition?.value?.y!!+point_size


        val length = sqrt( (x2 - x1)*(x2 - x1)+(y2 - y1)*(y2 - y1))

        val angle = atan2(y2 - y1, x2 - x1) * (180f / PI).toFloat()
        var centerX = (x1 + x2) / 2
        var centerY = (y1 + y2) / 2
        var size_width = length/2
        var size_height = length/2
        centerX -= size_width/0.8.toFloat()
        centerY -= size_height/0.8.toFloat()
        size_width = size_width/1.2.toFloat()
        size_height = size_height/1.2.toFloat()

        var image_name  = components[componenta_name]!!.image


        Box(
            modifier = Modifier
                .offset {
                    IntOffset(centerX.roundToInt(), centerY.roundToInt())
                }
                .size(width = size_width.dp, height = size_height.dp)
                .rotate(angle)
        ){
            Image(
                painter = painterResource(id = image_name),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
            Column{
                if(componenta_name.first() != 'W'){
                    Text(componenta_name, color = Color.Black)
                    Text(components[componenta_name]!!.value, color = Color.Black)
                }
            }
        }


    }

    @Composable
    fun Connect_near_components(components: MutableMap<String, Component>) // potentially too potent maybe do just every 0.5s
    {
        components.entries.forEach { (key, _) ->
            components.entries.forEach { (key2, _) ->
                if (key != key2)
                {
                    var key1_front = components[key]?.frontPosition?.value!!
                    var key1_back = components[key]?.backPosition?.value!!

                    var key2_front = components[key2]?.frontPosition?.value!!
                    var key2_back = components[key2]?.backPosition?.value!!


                    if (distance(key1_front,key2_front) < point_size*2)
                    {
                        components[key]?.frontPosition?.value = key2_front

                        if(!components[key]!!.input.contains(key2)) // key2 not in input
                        {
                            components[key]?.input?.add(key2)
                            components[key2]?.input?.add(key)
                        }
                    }

                    else if (distance(key1_front,key2_back) < point_size*2)
                    {
                        components[key]?.frontPosition?.value = key2_back


                        if(!components[key]!!.input.contains(key2)) // key2 not in input of key1
                        {
                            components[key]?.input?.add(key2)
                            components[key2]?.output?.add(key)
                        }
                    }

                    else if (distance(key1_back,key2_front) < point_size*2)
                    {
                        components[key]?.backPosition?.value = key2_front

                        if(!components[key]!!.output.contains(key2)) // key2 not in output of key1
                        {
                            components[key]?.output?.add(key2)
                            components[key2]?.input?.add(key)
                        }
                    }

                    else if (distance(key1_back,key2_back) < point_size*2)
                    {
                        components[key]?.backPosition?.value = key2_back

                        if(!components[key]!!.output.contains(key2)) // key2 not in output of key1
                        {
                            components[key]?.output?.add(key2)
                            components[key2]?.output?.add(key)
                        }
                    }
                    else
                    {
                        if(components[key]!!.input.contains(key2)) // key2 in input of key1
                        {
                            components[key]?.input?.remove(key2)
                            if(components[key2]!!.input.contains(key)) // key2 in input of key1
                            {
                                components[key2]?.input?.remove(key)
                            }
                            else
                            {
                                components[key2]?.output?.remove(key)
                            }
                        }

                        if(components[key]!!.output.contains(key2)) // key2 in output of key1
                        {
                            components[key]?.output?.remove(key2)

                            if(components[key2]!!.input.contains(key)) // key2 in input of key1
                            {
                                components[key2]?.input?.remove(key)
                            }
                            else
                            {
                                components[key2]?.output?.remove(key)
                            }
                        }
                    }
                }
            }
            if (key != "M" && key != "P")
            {
                if (components[key]?.frontPosition?.value!!.x < 0 || components[key]?.backPosition?.value!!.x < 0) // komponent je castecne mimo obrazovku
                {
                    deletecomponent(components,key)
                }
            }
        }
        //Log.d("components","$components")
    }

    fun simplyfy(solved_components: MutableMap<String, Component>){
        solved_components.remove("P")  // simplification or complication?
        solved_components.remove("M")  // simplification or complication?

        val keysToRemove = solved_components.entries
            .filter { it.value.input.isEmpty() || it.value.output.isEmpty() }
            .map { it.key }

        keysToRemove.forEach { key ->
            deletecomponent(solved_components, key)
        }

        var steps = 0
        while (simplyfy_step(solved_components))
        {
            steps += 1
        }
        Log.d("steps","$steps")
    }


    fun simplyfy_step(solved_components: MutableMap<String, Component>): Boolean
    {
        //Log.d("components","${solved_components}")
        if(simplyfy_paraler(solved_components))
        {
            return true
        }

        if(simplyfy_series(solved_components))
        {
            return true
        }

        if (simplyfy_delta_star(solved_components))
        {
            return true
        }

        return false
    }

    fun deletecomponent(solved_components: MutableMap<String, Component>,component_to_delete: String)
    {
        //Log.d("components","$solved_components")          //bugtesting
        //Log.d("deleting_component",component_to_delete)
        solved_components.remove(component_to_delete)
        solved_components.entries.forEach { (component, _) ->
            solved_components[component]!!.input.remove(component_to_delete)
            solved_components[component]!!.output.remove(component_to_delete)
        }
    }

    fun simplyfy_paraler(solved_components: MutableMap<String, Component>): Boolean
    {
        solved_components.entries.forEach { (component, _) ->
            solved_components[component]!!.input.forEach { suspect ->
                if (solved_components[component]!!.output.contains(suspect))
                {
                    solved_components[component]!!.equation = "(" + solved_components[component]!!.equation + "*" + solved_components[suspect]!!.equation + ")" + "/"+"(" + solved_components[component]!!.equation+"+"+ solved_components[suspect]!!.equation+")"
                    deletecomponent(solved_components,suspect)
                    return true
                }
            }
        }
        return false
    }


    fun simplyfy_delta_star(solved_components: MutableMap<String, Component>): Boolean
    {
        for ((first, component) in solved_components) {
            if (first != "P" && first != "M") {
                for (second in component.input) {
                    if (second != "P" && second != "M") {    // neupravujeme p ani M
                        if(!solved_components[second]!!.input.contains(first)) // toto není spoj s first             spoj je (first-in conect second-out)
                        {
                            for (third in solved_components[second]!!.input)
                            {
                                if (third != "P" && third != "M" && third != first)    // teoretickej check third != first mozna tu nepotrebujeme
                                {
                                    if (solved_components[third]!!.input.contains(first))
                                    {
                                        var first_val =  "0"
                                        var second_val =  "0"
                                        var third_val =  "0"
                                        val b_sum = solved_components[first]!!.equation + "+" + solved_components[second]!!.equation + "+" + solved_components[third]!!.equation
                                        if (amp_phase_from_complex(calculate_value(b_sum, 1.0)).first > 0) // realné hodnoty a ne jen dráty
                                        {
                                            first_val =  "(" + solved_components[first]!!.equation + "+" + solved_components[second]!!.equation + ")/(" + b_sum + ")"
                                            second_val =  "(" + solved_components[second]!!.equation + "+" + solved_components[third]!!.equation + ")/(" + b_sum + ")"
                                            third_val =  "(" + solved_components[third]!!.equation + "+" + solved_components[first]!!.equation + ")/(" + b_sum + ")"
                                        }


                                        add_S_to_components_conenctions(solved_components, first, solved_components[first]!!.input)
                                        add_S_to_components(solved_components, first_val, solved_components[first]!!.input, 1)

                                        add_S_to_components_conenctions(solved_components, second, solved_components[second]!!.input)
                                        add_S_to_components(solved_components, second_val, solved_components[second]!!.input, 2)

                                        add_S_to_components_conenctions(solved_components, third, solved_components[third]!!.input)
                                        add_S_to_components(solved_components, third_val, solved_components[third]!!.input, 3)

                                        deletecomponent(solved_components, first)
                                        deletecomponent(solved_components, second)
                                        deletecomponent(solved_components, third)
                                        // mame trojici first-in second-in third-in
                                        return true
                                    }
                                    if (solved_components[third]!!.output.contains(first))
                                    {
                                        var first_val =  "0"
                                        var second_val =  "0"
                                        var third_val =  "0"
                                        val b_sum = solved_components[first]!!.equation + "+" + solved_components[second]!!.equation + "+" + solved_components[third]!!.equation
                                        if (amp_phase_from_complex(calculate_value(b_sum, 1.0)).first > 0) // realné hodnoty a ne jen dráty
                                        {
                                            first_val =  "(" + solved_components[first]!!.equation + "+" + solved_components[second]!!.equation + ")/(" + b_sum + ")"
                                            second_val =  "(" + solved_components[second]!!.equation + "+" + solved_components[third]!!.equation + ")/(" + b_sum + ")"
                                            third_val =  "(" + solved_components[third]!!.equation + "+" + solved_components[first]!!.equation + ")/(" + b_sum + ")"
                                        }

                                        add_S_to_components_conenctions(solved_components, first, solved_components[first]!!.input)
                                        add_S_to_components(solved_components, first_val, solved_components[first]!!.input, 1)

                                        add_S_to_components_conenctions(solved_components, second, solved_components[second]!!.input)
                                        add_S_to_components(solved_components, second_val, solved_components[second]!!.input, 2)

                                        add_S_to_components_conenctions(solved_components, third, solved_components[third]!!.output)
                                        add_S_to_components(solved_components, third_val, solved_components[third]!!.output, 3)

                                        deletecomponent(solved_components, first)
                                        deletecomponent(solved_components, second)
                                        deletecomponent(solved_components, third)
                                        // mame trojici first-in second-in third-out
                                        return true
                                    }
                                }
                            }
                        }
                        else                                                                // toto není spoj s first             spoj je (first-in conect second-in)
                        {
                            for (third in solved_components[second]!!.output)
                            {
                                if (third != "P" && third != "M" && third != first)    // teoretickej check third != first mozna tu nepotrebujeme
                                {
                                    if (solved_components[third]!!.input.contains(first))
                                    {
                                        var first_val =  "0"
                                        var second_val =  "0"
                                        var third_val =  "0"
                                        val b_sum = solved_components[first]!!.equation + "+" + solved_components[second]!!.equation + "+" + solved_components[third]!!.equation
                                        if (amp_phase_from_complex(calculate_value(b_sum, 1.0)).first > 0) // realné hodnoty a ne jen dráty
                                        {
                                            first_val =  "(" + solved_components[first]!!.equation + "+" + solved_components[second]!!.equation + ")/(" + b_sum + ")"
                                            second_val =  "(" + solved_components[second]!!.equation + "+" + solved_components[third]!!.equation + ")/(" + b_sum + ")"
                                            third_val =  "(" + solved_components[third]!!.equation + "+" + solved_components[first]!!.equation + ")/(" + b_sum + ")"
                                        }

                                        add_S_to_components_conenctions(solved_components, first, solved_components[first]!!.input)
                                        add_S_to_components(solved_components, first_val, solved_components[first]!!.input, 1)

                                        add_S_to_components_conenctions(solved_components, second, solved_components[second]!!.output)
                                        add_S_to_components(solved_components, second_val, solved_components[second]!!.output, 2)

                                        add_S_to_components_conenctions(solved_components, third, solved_components[third]!!.input)
                                        add_S_to_components(solved_components, third_val, solved_components[third]!!.input, 3)

                                        deletecomponent(solved_components, first)
                                        deletecomponent(solved_components, second)
                                        deletecomponent(solved_components, third)
                                        // mame trojici first-in second-out third-in
                                        return true
                                    }
                                    if (solved_components[third]!!.output.contains(first))
                                    {
                                        var first_val =  "0"
                                        var second_val =  "0"
                                        var third_val =  "0"
                                        val b_sum = solved_components[first]!!.equation + "+" + solved_components[second]!!.equation + "+" + solved_components[third]!!.equation
                                        if (amp_phase_from_complex(calculate_value(b_sum, 1.0)).first > 0) // realné hodnoty a ne jen dráty
                                        {
                                            first_val =  "(" + solved_components[first]!!.equation + "+" + solved_components[second]!!.equation + ")/(" + b_sum + ")"
                                            second_val =  "(" + solved_components[second]!!.equation + "+" + solved_components[third]!!.equation + ")/(" + b_sum + ")"
                                            third_val =  "(" + solved_components[third]!!.equation + "+" + solved_components[first]!!.equation + ")/(" + b_sum + ")"
                                        }

                                        add_S_to_components_conenctions(solved_components, first, solved_components[first]!!.input)
                                        add_S_to_components(solved_components, first_val, solved_components[first]!!.input, 1)

                                        add_S_to_components_conenctions(solved_components, second, solved_components[second]!!.output)
                                        add_S_to_components(solved_components, second_val, solved_components[second]!!.output, 2)

                                        add_S_to_components_conenctions(solved_components, third, solved_components[third]!!.output)
                                        add_S_to_components(solved_components, third_val, solved_components[third]!!.output, 3)

                                        deletecomponent(solved_components, first)
                                        deletecomponent(solved_components, second)
                                        deletecomponent(solved_components, third)
                                        // mame trojici first-in second-out third-out
                                        return true
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return false
    }


    fun simplyfy_series(solved_components: MutableMap<String, Component>): Boolean
    {
        solved_components.entries.forEach { (component, _) ->
            if (solved_components[component]!!.input.size == 1 && (solved_components[component]!!.input[0] != "P" && solved_components[component]!!.input[0] != "M"))
            {
                solved_components[component]!!.equation = "(" + solved_components[component]!!.equation + "+" + solved_components[solved_components[component]!!.input[0]]!!.equation + ")"


                val next_component = solved_components[component]!!.input[0]

                val conencted_to : MutableList<String>
                if (solved_components[next_component]!!.input[0] == component)
                {
                    conencted_to = solved_components[next_component]!!.output
                }
                else{
                    conencted_to = solved_components[next_component]!!.input
                }


                conencted_to.forEach { next_next_component ->
                    if (next_next_component != "P" && next_next_component != "M")
                    {
                        if (solved_components[next_next_component]!!.input.contains(next_component))
                        {
                            solved_components[next_next_component]!!.input.add(component)
                        }
                        if (solved_components[next_next_component]!!.output.contains(next_component))
                        {
                            solved_components[next_next_component]!!.output.add(component)
                        }
                    }
                }



                deletecomponent(solved_components,solved_components[component]!!.input[0])

                solved_components[component]!!.input = conencted_to
                return true
            }


            if (solved_components[component]!!.output.size == 1 && (solved_components[component]!!.output[0] != "P" && solved_components[component]!!.output[0] != "M"))
            {
                solved_components[component]!!.equation = "(" + solved_components[component]!!.equation + "+" + solved_components[solved_components[component]!!.output[0]]!!.equation + ")"




                val next_component = solved_components[component]!!.output[0]

                val conencted_to : MutableList<String>
                if (solved_components[next_component]!!.input[0] == component)
                {
                    conencted_to = solved_components[next_component]!!.output
                }
                else{
                    conencted_to = solved_components[next_component]!!.input
                }


                conencted_to.forEach { next_next_component ->
                    if (next_next_component != "P" && next_next_component != "M")
                    {
                        if (solved_components[next_next_component]!!.input.contains(next_component))
                        {
                            solved_components[next_next_component]!!.input.add(component)
                        }
                        if (solved_components[next_next_component]!!.output.contains(next_component))
                        {
                            solved_components[next_next_component]!!.output.add(component)
                        }
                    }
                }

                deletecomponent(solved_components,solved_components[component]!!.output[0])

                solved_components[component]!!.output = conencted_to
                return true
            }

        }
        return false
    }


    fun calculate_value(equation: String, xValue: Double): Complex
    {
        //.d("d",equation)
        val result = evaluateComplexExpression(equation, xValue)
        return result
    }

    fun getvalues_for_initial_graph(equation: String, from: Double, to: Double): Pair<MutableList<Point>, MutableList<Point>>
    {
        var abs_data = mutableListOf<Point>()
        var phase_data = mutableListOf<Point>()


        for (i in 0..graph_lenght-1)
        {
            val frequency = (from + ((to-from).toFloat()/(graph_lenght-1)*i))
            val complex = calculate_value(equation, frequency.toDouble())

            val real = complex.real
            val imag = complex.img
            abs_data.add(Point(x = frequency.toFloat(),y = sqrt(real*real+imag*imag).toFloat()))
            if (real != 0.toDouble())
            {
                phase_data.add(Point(x = frequency.toFloat(),y = (atan((imag/real))*180/PI).toFloat()))
            }
            else
            {
                if (imag == 0.toDouble())
                {
                    phase_data.add(Point(x = frequency.toFloat(),y = 0.toFloat()))
                }
                else if (imag < 0.toDouble())
                {
                    phase_data.add(Point(x = frequency.toFloat(),y = (-90).toFloat()))
                }
                else{
                    phase_data.add(Point(x = frequency.toFloat(),y = 90.toFloat()))
                }

            }
        }
        return Pair(abs_data, phase_data)
    }


    @Composable
    fun MyChart(points: List<Point>, modifier: Modifier = Modifier) {
        val modelProducer = remember { CartesianChartModelProducer() }

        val sortedPoints = points.sortedBy { it.x }
        val xList = sortedPoints.map { it.x }
        val yList = sortedPoints.map { it.y }
        LaunchedEffect(points) {
            modelProducer.runTransaction {
                lineSeries {
                    series(xList, yList)
                }
            }
        }

        val fixedStepSize = ((points[graph_lenght-1].x-points[0].x)/(graph_lenght-1)).toDouble()

        val fixedGetXStep: (CartesianChartModel) -> Double = { _ ->
            fixedStepSize
        }

        val screenWidthDpValue = LocalConfiguration.current.screenWidthDp // this is Int

        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(pointSpacing = (screenWidthDpValue/graph_lenght).toInt().dp),
                startAxis = VerticalAxis.rememberStart(label =  rememberAxisLabelComponent(color = Color.Black)),  // Y axis
                bottomAxis = HorizontalAxis.rememberBottom(label =  rememberAxisLabelComponent(color = Color.Black)), // X axis
                getXStep = fixedGetXStep
            ),
            modelProducer = modelProducer,
            modifier = modifier
        )
    }


    fun find_phase(equation: String, start: Double, end: Double, max_iterations: Int, num: Double = 0.0): Pair<Double, Double> {
        var to = end
        var from = start
        var center = (to-from)/2 + start
        var centervalue = phasefromcomplex(calculate_value(equation, center))

        if (phasefromcomplex(calculate_value(equation, to)) > phasefromcomplex(calculate_value(equation, from)))
        {
            for (i in 0..max_iterations - 1) {
                if (centervalue > num) {
                    to = center
                } else {
                    from = center
                }

                center = (to - from) / 2 + from
                centervalue = phasefromcomplex(calculate_value(equation, center))
                //Log.d("center","${center}")
                //Log.d("center","${centervalue}")
            }
        }
        else
        {
            for (i in 0..max_iterations - 1) {
                if (centervalue > num) {
                    from = center
                } else {
                    to = center
                }

                center = (to - from) / 2 + from
                centervalue = phasefromcomplex(calculate_value(equation, center))
                //Log.d("center","${center}")
                //Log.d("center","${centervalue}")
            }
        }

        return Pair(center, to-from)
    }



    @Composable
    fun SelectableText(text: String) {
        SelectionContainer {
            Text(text, color = Color.Black)
        }
    }


    fun add_S_to_components(solved_components: MutableMap<String, Component>,value: String, all_to_add: MutableList<String>,s_number: Int)
    {
        val con_components = mutableListOf<String>()
        if (s_number == 1)
        {
            con_components.add("S${component_num_S+1}")
            con_components.add("S${component_num_S+2}")
        }
        if (s_number == 2)
        {
            con_components.add("S${component_num_S-1}")
            con_components.add("S${component_num_S+1}")
        }
        if (s_number == 3)
        {
            con_components.add("S${component_num_S-2}")
            con_components.add("S${component_num_S-1}")
        }


        val name = "S$component_num_S"
        //Log.d("components add:",name)    //debug
        solved_components[name] = Component(
            equation = value,
            value = "",
            input = con_components,
            output = all_to_add,
            frontPosition = mutableStateOf(Position(getScreenWidth().toFloat()*9/10, getScreenHeight().toFloat()*3/5)),
            backPosition = mutableStateOf(Position(getScreenWidth().toFloat()*9/10, getScreenHeight().toFloat()*2/5)),
            image = R.drawable.resistor
        )
        component_num_S += 1
    }

    fun add_S_to_components_conenctions(solved_components: MutableMap<String, Component>,recognition_component: String, all_to_add: MutableList<String>)
    {
        for (component_key in all_to_add)
        {
            if (component_key != "P" && component_key != "M")
            {
                //Log.d("adding S$component_num_S to :", component_key)   //debug
                if(solved_components[component_key]!!.input.contains(recognition_component))
                {
                    solved_components[component_key]!!.input.add("S$component_num_S")
                }
                else
                {
                    solved_components[component_key]!!.output.add("S${component_num_S}")
                }
            }
        }
    }
}

fun phasefromcomplex(complex: Complex): Double {
    if (complex.img == 0.0)
    {
        return 0.0
    }
    else if (complex.real == 0.0)
    {
        if (complex.img > 0.0)
        {
            return 90.0
        }
        else{
            return -90.0
        }
    }
    else
    {
        return atan((complex.img/complex.real))*180/PI
    }
}

// Define token types
enum class TokenType { NUMBER, VARIABLE, IMAGINARY, OPERATOR, LPAREN, RPAREN }

data class Token(val type: TokenType, val value: String)

// Tokenize the input expression
fun tokenizeExpression(expr: String): List<Token> {
    val tokens = mutableListOf<Token>()
    var i = 0
    while (i < expr.length) {
        val c = expr[i]
        when {
            c.isWhitespace() -> {
                i++
            }
            c.isDigit() || c == '.' -> {
                // Number (including decimals)
                val start = i
                while (i < expr.length && (expr[i].isDigit() || expr[i] == '.')) i++
                tokens.add(Token(TokenType.NUMBER, expr.substring(start, i)))
            }
            c == 'π' -> {
                // Pi constant: replace with its numeric value
                tokens.add(Token(TokenType.NUMBER, PI.toString()))
                i++
            }
            c == 'x' -> {
                tokens.add(Token(TokenType.VARIABLE, "x"))
                i++
            }
            c == 'i' -> {
                tokens.add(Token(TokenType.IMAGINARY, "i"))
                i++
            }
            c == '+' || c == '-' || c == '*' || c == '/' -> {
                tokens.add(Token(TokenType.OPERATOR, c.toString()))
                i++
            }
            c == '(' -> {
                tokens.add(Token(TokenType.LPAREN, "("))
                i++
            }
            c == ')' -> {
                tokens.add(Token(TokenType.RPAREN, ")"))
                i++
            }
            else -> {
                throw IllegalArgumentException("Unexpected character: $c")
            }
        }
    }
    return tokens
}

// Operator properties – precedence and associativity
data class Operator(val precedence: Int, val isLeftAssociative: Boolean)

val operators = mapOf(
    "+" to Operator(1, true),
    "-" to Operator(1, true),
    "*" to Operator(2, true),
    "/" to Operator(2, true)
)


fun check_string_isnt_any_of_empty_or_not_number_or_zero(string: String): Boolean {
    if (string.isEmpty())
    {

        return false
    }

    if (!string.replace(".","").isDigitsOnly())
    {

        return false
    }

    if (string.toDouble() == 0.0)
    {

        return false
    }

    return true
}


fun bool_to_int(bool: Boolean): Int {
    if (bool)
    {
        return 1
    }
    else{
        return 0
    }
}


// Convert list of tokens to RPN using the shunting-yard algorithm
fun shuntingYard(tokens: List<Token>): List<Token> {
    val output = mutableListOf<Token>()
    val stack = mutableListOf<Token>()
    for (token in tokens) {
        when (token.type) {
            TokenType.NUMBER, TokenType.VARIABLE, TokenType.IMAGINARY -> output.add(token)
            TokenType.OPERATOR -> {
                while (stack.isNotEmpty() && stack.last().type == TokenType.OPERATOR) {
                    val op1 = operators[token.value]!!
                    val op2 = operators[stack.last().value]!!
                    if ((op1.isLeftAssociative && op1.precedence <= op2.precedence) ||
                        (!op1.isLeftAssociative && op1.precedence < op2.precedence)
                    ) {
                        output.add(stack.removeAt(stack.lastIndex))
                    } else break
                }
                stack.add(token)
            }
            TokenType.LPAREN -> stack.add(token)
            TokenType.RPAREN -> {
                while (stack.isNotEmpty() && stack.last().type != TokenType.LPAREN) {
                    output.add(stack.removeAt(stack.lastIndex))
                }
                if (stack.isEmpty() || stack.last().type != TokenType.LPAREN) {
                    throw IllegalArgumentException("Mismatched parentheses detected. Please check the expression")
                }
                stack.removeAt(stack.lastIndex) // Remove the left parenthesis
            }
        }
    }
    while (stack.isNotEmpty()) {
        if (stack.last().type == TokenType.LPAREN || stack.last().type == TokenType.RPAREN) {
            throw IllegalArgumentException("Mismatched parentheses detected. Please check the expression")
        }
        output.add(stack.removeAt(stack.lastIndex))
    }
    return output
}

// Evaluate the RPN expression using a stack and return a Complex result
fun evaluateRPN(rpn: List<Token>, xValue: Double): Complex {
    val stack = mutableListOf<Complex>()
    for (token in rpn) {
        when (token.type) {
            TokenType.NUMBER -> {
                stack.add(Complex(token.value.toDouble(), 0.0))
            }
            TokenType.VARIABLE -> {
                stack.add(Complex(xValue, 0.0))
            }
            TokenType.IMAGINARY -> {
                stack.add(Complex(0.0, 1.0))
            }
            TokenType.OPERATOR -> {
                if (stack.size < 2) {
                    throw IllegalArgumentException("Invalid number of operands available for '${token.value}' operator")
                }
                val b = stack.removeAt(stack.lastIndex)
                val a = stack.removeAt(stack.lastIndex)

                val res = when (token.value) {
                    "+" -> a + b
                    "-" -> a - b
                    "*" -> a * b
                    "/" -> a / b
                    else -> throw IllegalArgumentException("Unsupported operator: ${token.value}")
                }
                stack.add(res)
            }
            else -> {
                throw IllegalArgumentException("Unexpected token during evaluation: $token")
            }
        }
    }
    if (stack.size != 1) {
        throw IllegalArgumentException("Invalid expression evaluation. Stack has extra operands.")
    }
    return stack.first()
}

// Main function that ties everything together
fun evaluateComplexExpression(equation: String, x: Double): Complex {
    // First, tokenize the expression string.
    val tokens = tokenizeExpression(equation)
    // Then convert tokens to Reverse Polish Notation.
    val rpn = shuntingYard(tokens)
    // Finally, evaluate the RPN expression using x and Complex arithmetic.
    return evaluateRPN(rpn, x)
}




fun round(X: Double, places: Int): Double {
    return (X*(10.0.pow(places))).toInt().toDouble()/(10.0.pow(places))
}

fun amp_phase_from_complex(complex: Complex): Pair<Double, Double> {
    val Z_amount = sqrt(complex.real*complex.real+complex.img*complex.img)
    var Z_phase = 0.0

    if (complex.real != 0.toDouble())
    {
        Z_phase = (atan(complex.img/complex.real)/PI*180)
    }
    else
    {
        if (complex.img == 0.toDouble())
        {
            Z_phase = 0.0
        }
        else if (complex.img < 0.toDouble())
        {
            Z_phase = -90.0
        }
        else{
            Z_phase = 90.0
        }
    }

    return Pair(Z_amount,Z_phase)
}

fun getScreenWidth(): Int {
    return Resources.getSystem().getDisplayMetrics().widthPixels
}

fun getScreenHeight(): Int {
    return Resources.getSystem().getDisplayMetrics().heightPixels
}

fun distance(point1: Position, point2: Position): Float
{
    return sqrt((point1.x-point2.x)*(point1.x-point2.x)+(point1.y-point2.y)*(point1.y-point2.y))
}
