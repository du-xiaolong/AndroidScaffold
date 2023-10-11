package com.dxl.scaffold.ui

import android.content.Context
import android.graphics.Color
import com.dxl.scaffold.R
import com.dxl.scaffold.databinding.DialogYearMonthDaySelectBinding
import com.dxl.scaffold.ui.wheel.adapter.WheelAdapter
import com.dxl.scaffold.ui.wheel.view.WheelView
import com.dxl.scaffold.utils.parseDateAuto
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.BottomPopupView
import java.lang.Integer.max
import java.util.*
import kotlin.math.min

/**
 * @author duxiaolong
 */
class DateSelectDialog(context: Context) : BottomPopupView(context) {


    //默认年份
    private var defaultYear: Int = 0

    //默认月份
    private var defaultMonth: Int = 0

    //默认日期
    private var defaultDay: Int = 0


    private var max: Long = 0 //可选的最大日期时间戳(毫秒)
    private var min: Long = 0  //可选的最小日期时间戳（毫秒）

    private var onDateSelected: ((year: Int, month: Int, day: Int) -> Unit)? = null

    private constructor(builder: Builder) : this(builder.context) {
        this.defaultYear = builder.defaultYear
        this.defaultMonth = builder.defaultMonth
        this.defaultDay = builder.defaultDay
        this.min = builder.min
        this.max = builder.max
        this.onDateSelected = builder.onDateSelected
    }


    private val wheelDay by lazy { findViewById<WheelView>(R.id.options1) }
    private val wheelMonth by lazy { findViewById<WheelView>(R.id.options2) }
    private val wheelYear by lazy { findViewById<WheelView>(R.id.options3) }

    private lateinit var vb: DialogYearMonthDaySelectBinding

    override fun getImplLayoutId(): Int {
        return R.layout.dialog_year_month_day_select
    }

    override fun onCreate() {
        super.onCreate()
        vb = DialogYearMonthDaySelectBinding.bind(popupImplView)

        vb.btnConfirm.setOnClickListener {
            val year = wheelYear.adapter.getItem(wheelYear.currentItem).toString().toInt()
            val month = wheelMonth.adapter.getItem(wheelMonth.currentItem).toString().toInt()
            val day = wheelDay.adapter.getItem(wheelDay.currentItem).toString().toInt()
            onDateSelected?.invoke(year, month, day)
            dismiss()
        }
        vb.btnCancel.setOnClickListener { dismiss() }

        setWheels(wheelDay, wheelMonth, wheelYear)


        setWheelYear(defaultYear)
        setWheelMonth(defaultMonth)
        setWheelDay(defaultDay)
    }

    private val minCalender by lazy {
        Calendar.getInstance().apply {
            time = Date(min)
        }
    }

    private val maxCalendar by lazy {
        Calendar.getInstance().apply {
            time = Date(max)
        }
    }

    private val minYear by lazy {
        if (min <= 0) 1900 else
            max(minCalender.get(Calendar.YEAR), 1900)
    }

    private val maxYear by lazy {
        if (max <= 0) 2100 else
            min(maxCalendar.get(Calendar.YEAR), 2100)
    }

    private val minMonth by lazy {
        minCalender.get(Calendar.MONTH) + 1
    }

    private val maxMonth by lazy {
        maxCalendar.get(Calendar.MONTH) + 1
    }

    private fun setWheelYear(defaultYear: Int) {
        wheelYear.adapter = object : WheelAdapter<Int> {
            override fun getItemsCount(): Int {
                return maxYear - minYear + 1
            }

            override fun getItem(index: Int): Int {
                return minYear + index
            }

            override fun indexOf(o: Int): Int {
                return o - minYear
            }
        }
        wheelYear.currentItem = defaultYear - minYear
        wheelYear.setOnItemSelectedListener {
            setWheelMonth()
            setWheelDay()
        }

    }

    private fun setWheelMonth(defaultMonth: Int = -1) {
        //当前选的年
        val year = wheelYear.adapter.getItem(wheelYear.currentItem).toString().toInt()
        var minMonth = 1
        var maxMonth = 12
        if (year <= minYear) {
            val calendar = Calendar.getInstance()
            calendar.time = Date(min)
            minMonth = calendar.get(Calendar.MONTH) + 1
        }
        if (year >= maxYear) {
            val calendar = Calendar.getInstance()
            calendar.time = Date(max)
            maxMonth = calendar.get(Calendar.MONTH) + 1
        }

        wheelMonth.adapter = object : WheelAdapter<Int> {
            override fun getItemsCount(): Int {
                return maxMonth - minMonth + 1
            }

            override fun getItem(index: Int): Int {
                return minMonth + index
            }

            override fun indexOf(o: Int): Int {
                return o - minMonth
            }
        }


        var default = defaultMonth - minMonth
        if (default < 0) {
            default = wheelMonth.currentItem
        }
        if (default > wheelMonth.itemsCount - 1) {
            default = wheelMonth.itemsCount - 1
        }
        wheelMonth.currentItem = default

        wheelMonth.setOnItemSelectedListener {
            setWheelDay()
        }
    }

    private fun setWheelDay(defaultDay: Int = -1) {
        val year = wheelYear.adapter.getItem(wheelYear.currentItem).toString().toInt()
        val month = wheelMonth.adapter.getItem(wheelMonth.currentItem).toString().toInt()

        var dayCount = 30
        if (month in listOf(1, 3, 5, 7, 8, 10, 12)) dayCount = 31
        if (month == 2) {
            dayCount = if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) 29 else 28

        }
        var minDay = 1
        var maxDay = dayCount

        if (year <= minYear && month <= minMonth) {
            minDay = minCalender.get(Calendar.DAY_OF_MONTH)
        }

        if (year >= maxYear && month >= maxMonth) {
            maxDay = maxCalendar.get(Calendar.DAY_OF_MONTH)
        }

        wheelDay.adapter = object : WheelAdapter<Int> {
            override fun getItemsCount(): Int = maxDay - minDay + 1
            override fun getItem(index: Int) = minDay + index
            override fun indexOf(o: Int): Int = o - minDay
        }

        var default = defaultDay - minDay
        if (default < 0) {
            default = wheelDay.currentItem
        }
        if (default > wheelDay.itemsCount - 1) {
            default = wheelDay.itemsCount - 1
        }
        wheelDay.currentItem = default

    }

    private fun setWheels(vararg wheels: WheelView) {
        wheels.forEach { wheel ->
            wheel.setCyclic(false)
            wheel.setItemsVisibleCount(3)
            wheel.setAlphaGradient(false)
            wheel.setDividerColor(Color.TRANSPARENT)
            wheel.setLineSpacingMultiplier(2.5f)
            wheel.setTextColorCenter(Color.BLACK)
            wheel.setTextSize(22f)
            wheel.setTextColorOut(Color.parseColor("#969696"))
            wheel.setIsOptions(true)
        }
    }

    companion object {
        fun with(context: Context): Builder {
            return Builder(context)
        }


    }


    class Builder(val context: Context) {

        //默认年份
        var defaultYear: Int = Calendar.getInstance().get(Calendar.YEAR)

        //默认月份 1-12
        var defaultMonth: Int = Calendar.getInstance().get(Calendar.MONTH) + 1

        //默认日 1-31
        var defaultDay: Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

        var max: Long = 0 //可选的最大日期时间戳(毫秒)

        var min: Long = 0  //可选的最小日期时间戳（毫秒）

        //回调
        var onDateSelected: ((year: Int, month: Int, day: Int) -> Unit)? = null

        fun setDefaultYear(@androidx.annotation.IntRange(from = 1900) defaultYear: Int): Builder {
            this.defaultYear = defaultYear
            return this
        }

        fun setMaxTime(max: Long): Builder {
            this.max = max
            return this
        }

        fun setMinTime(min: Long): Builder {
            this.min = min
            return this
        }

        //设置月份
        fun setDefaultMonth(
            @androidx.annotation.IntRange(
                from = 1,
                to = 12
            ) defaultMonth: Int
        ): Builder {
            this.defaultMonth = defaultMonth
            return this
        }

        //设置日
        fun setDefaultDay(
            @androidx.annotation.IntRange(
                from = 1,
                to = 31
            ) defaultDay: Int
        ): Builder {
            this.defaultDay = defaultDay
            return this
        }

        fun setDefault(time: Long?): Builder {
            time ?: return this
            val calendar = Calendar.getInstance()
            calendar.time = Date(time)
            this.defaultYear = calendar.get(Calendar.YEAR)
            this.defaultMonth = calendar.get(Calendar.MONTH) + 1
            this.defaultDay = calendar.get(Calendar.DAY_OF_MONTH)
            return this
        }

        fun setDefault(year: Int, month: Int, day: Int): Builder {
            this.defaultYear = year
            this.defaultMonth = month
            this.defaultDay = day
            return this
        }

        fun setMaxTime(timeFormat: String): Builder {
            timeFormat.parseDateAuto()?.time?.let { max = it }
            return this
        }

        fun setMinTime(timeFormat: String): Builder {
            timeFormat.parseDateAuto()?.time?.let { min = it }
            return this
        }

        fun setOnDateSelected(onDateSelected: (year: Int, month: Int, day: Int) -> Unit): Builder {
            this.onDateSelected = onDateSelected
            return this
        }


        fun setOnDateSelected(onDateSelected: (Date) -> Unit): Builder {
            this.onDateSelected = { year, month, day ->
                val calendar = Calendar.getInstance()
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month - 1)
                calendar.set(Calendar.DAY_OF_MONTH, day)
                onDateSelected.invoke(calendar.time)
            }
            return this
        }


        fun show() {
            XPopup.Builder(context)
                .isDestroyOnDismiss(true)
                .asCustom(DateSelectDialog(this))
                .show()
        }

    }

}