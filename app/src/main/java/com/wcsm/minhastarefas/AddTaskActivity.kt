package com.wcsm.minhastarefas

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import com.wcsm.minhastarefas.database.TaskDAO
import com.wcsm.minhastarefas.databinding.ActivityAddTaskBinding
import com.wcsm.minhastarefas.model.DailyTask
import com.wcsm.minhastarefas.model.Task
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddTaskActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private val binding by lazy {
        ActivityAddTaskBinding.inflate(layoutInflater)
    }

    private lateinit var taskToEdit: Task
    private lateinit var actualDate: String

    private var day = 0
    private var month = 0
    private var year = 0
    private var hour = 0
    private var minute = 0
    private var savedDay = 0
    private var savedMonth = 0
    private var savedYear = 0
    private var savedTime = ""

    private var generateWeeklyTasks = false
    private var isDailyTasks = false
    private var isLoading = false

    private var coroutineScope: CoroutineScope? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val locale = Locale("pt", "BR")
        Locale.setDefault(locale)

        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, baseContext.resources.displayMetrics)

        getDateTimeCalendar()

        actualDate = "${day}/${month + 1}/${year} - $hour:$minute"

        generateWeeklyTasksHandler()

        val bundle = intent.extras
        if(bundle != null) {
            val screenTitle = bundle.getString("screen_title")
            val btnText = bundle.getString("button_text")
            binding.tvNewTaskScreenTitle.text = screenTitle
            binding.btnAddOrUpdate.text = btnText

            if(bundle.containsKey("task")) {
                taskToEdit = bundle.getParcelable("task")!!
            }
        }

        with(binding) {
            btnBack.setOnClickListener {
                finish()
            }


            binding.tvDatetimePicked.text = actualDate

            pickDate()

            val titleField = layoutTitle
            val mondayField = layoutMonday

            cbGenerateWeeklyTasks.setOnClickListener {
                generateWeeklyTasks = binding.cbGenerateWeeklyTasks.isChecked
                generateWeeklyTasksHandler()
            }

            // Fill fields with task info when updating task
            if(btnAddOrUpdate.text == "ATUALIZAR") {
                editTextTitle.setText(taskToEdit.title)
                editTextDescription.setText(taskToEdit.description)
                cbAllowNotification.isChecked = taskToEdit.allowNotification > 0
                tvDatetimePicked.text = taskToEdit.dueDate
            }

            btnAddOrUpdate.setOnClickListener {
                if(btnAddOrUpdate.text == "ADICIONAR") {
                    val title = binding.editTextTitle.text.toString()
                    val validation = validateTitle(titleField, title)
                    val dueDate = tvDatetimePicked.text.toString()

                    val description = binding.editTextDescription.text.toString()
                    val allowNotification = if(binding.cbAllowNotification.isChecked) 1 else 0

                    if(validation) {
                        isDailyTasks = false
                        addTask(title, description, dueDate, allowNotification)
                    }
                } else if(btnAddOrUpdate.text == "ATUALIZAR") {
                    val title = binding.editTextTitle.text.toString()
                    val validation = validateTitle(titleField, title)
                    val dueDate = tvDatetimePicked.text.toString()
                    if(validation) {
                        updateTask(taskToEdit, title, dueDate)
                    }
                }
            }

            btnGenerate.setOnClickListener {
                isLoading = true
                toggleLoading()
                val monday = binding.editTextMonday.text.toString() // 25 -> Actual Day:
                val validation = validateMonday(mondayField, monday)
                if(validation) {
                    isDailyTasks = true
                    val actualDay = "$monday/${month + 1}/$year"
                    val dailyTasks = listOf(
                        DailyTask("Levantar", "Hora de levantar e começar o dia!!!", "$actualDay - 06:00"),
                        DailyTask("Hora da Caminhada", "Faça uma caminhada para energizar o dia!","$actualDay - 06:05"),
                        DailyTask("Estudar Inglês", "Bora melhorar o Inglês, VAMO!! VAMO!!","$actualDay - 07:00"),
                        DailyTask("Hora de Trabalhar", "Começe a trabalhar, nos vemos em breve!", "$actualDay - 08:00"),
                        DailyTask("Tarefas de Casa", "Realizar as tarefas de casa.", "$actualDay - 17:05"),
                        DailyTask("Começar os Estudos", "Android? Inglês? Outros? Apenas ESTUDE!", "$actualDay - 18:20"),
                        DailyTask("Tempo Livre", "O Fim do dia chegou, faça o que quiser!!!", "$actualDay - 21:20"),
                        DailyTask("Big Trotos Teste", "Saidera que eu quero dormir", "$actualDay - 04:43")
                    )

                    addAllWeeklyTasks(dailyTasks)
                } else {
                    isLoading = false
                    toggleLoading()
                }
            }
        }
    }

    private fun toggleLoading() {
        if(isLoading) {
            with(binding) {
                btnGenerate.isEnabled = false
                cbGenerateWeeklyTasks.isEnabled = false
                pbLoading.visibility = View.VISIBLE
                tvMonday.visibility = View.INVISIBLE
                layoutMonday.visibility = View.INVISIBLE
                editTextMonday.visibility = View.INVISIBLE
            }
        } else {
            with(binding) {
                pbLoading.visibility = View.INVISIBLE
                btnGenerate.isEnabled = true
                cbGenerateWeeklyTasks.isEnabled = true
                tvMonday.visibility = View.VISIBLE
                layoutMonday.visibility = View.VISIBLE
                editTextMonday.visibility = View.VISIBLE
            }
        }
    }

    private fun addAllWeeklyTasks(tasks: List<DailyTask>) {
        coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope?.launch {
            tasks.forEach {
                addTask(it.title, it.description, it.dueDate, 1)
                delay(300)
            }

            Toast.makeText(applicationContext, "Tarefas Diárias criadas com sucesso!", Toast.LENGTH_SHORT).show()

            isLoading = false
            toggleLoading()

            finish()
        }
    }

    private fun addTask(title: String, description: String, dueDate: String, allowNotification: Int ) {
            val task = Task(-1, title, description, convertToSQLiteFormat(actualDate), convertToSQLiteFormat(actualDate), convertToSQLiteFormat(dueDate), allowNotification, 0, 0)
            val taskDAO = TaskDAO(applicationContext)

            if(taskDAO.save(task)) {
                if(!isDailyTasks) {
                    Toast.makeText(applicationContext, "Tarefa registrada com sucesso!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
    }

    private fun updateTask(task: Task, title: String, dueDate: String) {
        val description = binding.editTextDescription.text.toString()
        val allowNotification = if(binding.cbAllowNotification.isChecked) 1 else 0

        val task = Task(task.id, title, description, convertToSQLiteFormat(task.createdAt), convertToSQLiteFormat(actualDate), convertToSQLiteFormat(dueDate), allowNotification, 0, task.completed)
        val taskDAO = TaskDAO(applicationContext)

        if(taskDAO.update(task)) {
            Toast.makeText(applicationContext, "Tarefa atualizada com sucesso!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun validateTitle(titleField: TextInputLayout, title: String): Boolean {
        titleField.error = null
        if(title.isEmpty()) {
            titleField.error = "Digite o título da tarefa."
            return false
        }
        return true
    }

    private fun validateMonday(mondayField: TextInputLayout, mondayInputed: String): Boolean {
        mondayField.error = null

        try {
            val monday = mondayInputed.toInt()
            if(monday <= 0 || monday > 31) {
                mondayField.error = "Informe um dia válido 1 à 31."
                return false
            }
            return true
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            mondayField.error = "Dia inválido."
            return false
        }
    }

    private fun generateWeeklyTasksHandler() {
        if(generateWeeklyTasks) {
            enablingAddTaskOptions("DISABLE")
            enablingGenerateWeeklyTasksOptions("ENABLE")
        } else {
            enablingAddTaskOptions("ENABLE")
            enablingGenerateWeeklyTasksOptions("DISABLE")
        }
    }

    private fun enablingAddTaskOptions(option: String) {
        if(option == "ENABLE") {
            with(binding) {
                layoutTitle.isEnabled = true
                layoutDescription.isEnabled = true
                cbAllowNotification.isEnabled = true
                btnDatetimePicker.isEnabled = true
                tvDatetimePicked.isEnabled = true
                btnAddOrUpdate.isEnabled = true
                btnGenerate.isEnabled = true
            }
        } else {
            with(binding) {
                layoutTitle.isEnabled = false
                layoutDescription.isEnabled = false
                cbAllowNotification.isEnabled = false
                btnDatetimePicker.isEnabled = false
                tvDatetimePicked.isEnabled = false
                btnAddOrUpdate.isEnabled = false
                btnGenerate.isEnabled = false
            }
        }
    }

    private fun enablingGenerateWeeklyTasksOptions(option: String) {
        if(option == "ENABLE") {
            with(binding) {
                tvMonday.isEnabled = true
                layoutMonday.isEnabled = true
                btnGenerate.isEnabled = true
            }
        } else {
            println("DEVE CHEGAR AQUI")
            with(binding) {
                tvMonday.isEnabled = false
                layoutMonday.isEnabled = false
                btnGenerate.isEnabled = false
            }
        }
    }

    private fun convertToSQLiteFormat(inputDate: String): String {
        val inputFormat = SimpleDateFormat("dd/MM/yyyy - HH:mm", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        val date = inputFormat.parse(inputDate) ?: Date()
        return outputFormat.format(date)
    }

    private fun getDateTimeCalendar() {
        val cal = Calendar.getInstance()
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)
        hour = cal.get(Calendar.HOUR_OF_DAY)
        minute = cal.get(Calendar.MINUTE)
    }

    private fun pickDate() {
        binding.btnDatetimePicker.setOnClickListener {
            getDateTimeCalendar()

            val datePickerDialog = DatePickerDialog(this, this, year, month, day)
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
            datePickerDialog.show()
        }
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = dayOfMonth
        savedMonth = month
        savedYear = year

        getDateTimeCalendar()

        val timePickerDialog = TimePickerDialog(this, R.style.MyTimePickerStyle, this, hour, minute, true)
        timePickerDialog.show()
    }

    @SuppressLint("SetTextI18n")
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        savedTime = String.format("%02d:%02d", hourOfDay, minute) //formatTime(hourOfDay, minute)
        binding.tvDatetimePicked.text = "$savedDay/${savedMonth + 1}/$savedYear - $savedTime"
    }

    override fun onDestroy() {
        coroutineScope?.cancel()
        super.onDestroy()
    }
}