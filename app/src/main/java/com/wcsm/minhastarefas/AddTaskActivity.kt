package com.wcsm.minhastarefas

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.core.view.isEmpty
import com.google.android.material.textfield.TextInputLayout
import com.wcsm.minhastarefas.database.TaskDAO
import com.wcsm.minhastarefas.databinding.ActivityAddTaskBinding
import com.wcsm.minhastarefas.model.Task
import java.util.Calendar

class AddTaskActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private var day = 0
    private var month = 0
    private var year = 0
    private var hour = 0
    private var minute = 0
    private var savedDay = 0
    private var savedMonth = 0
    private var savedYear = 0
    private var savedTime = ""

    private val binding by lazy {
        ActivityAddTaskBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val bundle = intent.extras
        if(bundle != null) {
            val screenTitle = bundle.getString("screen_title")
            val btnText = bundle.getString("button_text")
            binding.tvNewTaskScreenTitle.text = screenTitle
            binding.btnAddOrUpdate.text = btnText
        }

        with(binding) {

            val allowNotification = cbAllowNotification.isChecked

            btnBack.setOnClickListener {
                finish()
            }

            btnAddOrUpdate.setOnClickListener {
                if(btnAddOrUpdate.text == "ADICIONAR") {
                    val titleField = layoutTitle
                    val dueDate = tvDatetimePicked.text.toString()

                    Log.i("teste", "$dueDate") //31/12/2023 - 00:50

                    val validation = validateTitle(titleField)

                    if(validation) {
                        // VALIDATION OK -> ADD TASK
                        val title = editTextTitle.text.toString()
                        val description = editTextTitle.text.toString()

                        val task = Task(-1, title, description, "default", dueDate, allowNotification, false)
                        val taskDAO = TaskDAO(applicationContext)
                        if(taskDAO.save(task)) {
                            Toast.makeText(applicationContext, "Tarefa registrada com sucesso!", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                } else if(btnAddOrUpdate.text == "ATUALIZAR") {
                    // UPDATE TASK
                }
            }
        }


        getDateTimeCalendar()
        binding.tvDatetimePicked.text = "$day/${month + 1}/$year - ${formatTime(hour, minute)}"

        pickDate()
    }

    private fun validateTitle(title: TextInputLayout): Boolean {
        title.error = null

        if(title.isEmpty()) {
            title.error = "Digite o título da tarefa"
            return false
        }
        return true
    }

    private fun getDateTimeCalendar() {
        val cal = Calendar.getInstance()
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)
        hour = cal.get(Calendar.HOUR)
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

    private fun formatTime(hour: Int, minute: Int): String {
        return String.format("%02d:%02d", hour, minute)
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
        savedTime = formatTime(hourOfDay, minute)

        binding.tvDatetimePicked.text = "$savedDay/${savedMonth + 1}/$savedYear - $savedTime"
    }
}