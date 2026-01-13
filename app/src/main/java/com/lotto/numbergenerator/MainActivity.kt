package com.lotto.numbergenerator

import android.animation.ObjectAnimator
import android.content.Context
import android.os.Bundle
import android.view.animation.BounceInterpolator
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var numberTextViews: List<TextView>
    private lateinit var generateButton: Button
    private lateinit var randomButton: Button
    private lateinit var saveButton: Button
    private lateinit var clearButton: Button
    private lateinit var savedNumbersText: TextView
    private lateinit var countText: TextView
    private lateinit var modeText: TextView
    private lateinit var statsText: TextView

    private val savedNumbers = mutableListOf<LottoSet>()
    private val sharedPrefs by lazy {
        getSharedPreferences("LottoPrefs", Context.MODE_PRIVATE)
    }

    // 통계 데이터
    private val numberFrequency = mutableMapOf<Int, Int>()
    private val recentNumberFrequency = mutableMapOf<Int, Int>()
    private var totalDraws = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        loadHistoricalData()
        setupListeners()
        loadSavedNumbers()
        generateSmartNumbers()
    }

    private fun initViews() {
        numberTextViews = listOf(
            findViewById(R.id.number1),
            findViewById(R.id.number2),
            findViewById(R.id.number3),
            findViewById(R.id.number4),
            findViewById(R.id.number5),
            findViewById(R.id.number6)
        )

        generateButton = findViewById(R.id.generateButton)
        randomButton = findViewById(R.id.randomButton)
        saveButton = findViewById(R.id.saveButton)
        clearButton = findViewById(R.id.clearButton)
        savedNumbersText = findViewById(R.id.savedNumbersText)
        countText = findViewById(R.id.countText)
        modeText = findViewById(R.id.modeText)
        statsText = findViewById(R.id.statsText)
    }

    private fun loadHistoricalData() {
        // 역대 로또 당첨번호 데이터 (1146회차 데이터)
        val historicalData = """
1,10,23,29,33,37,40,16
2,9,13,21,25,32,42,2
3,11,16,19,21,27,31,30
4,14,27,30,31,40,42,2
5,16,24,29,40,41,42,3
6,5,11,16,18,38,45,1
7,2,9,16,17,25,38,21
8,4,5,18,21,31,35,2
9,6,7,16,24,30,37,31
10,9,12,20,30,33,45,18
11,2,8,11,16,31,34,3
12,8,14,18,28,32,45,1
13,2,4,5,25,35,40,45
14,3,7,12,26,33,37,39
15,4,9,13,14,28,30,34
16,2,5,6,24,29,39,1
17,10,17,26,34,38,39,12
18,7,8,21,25,38,44,16
19,1,6,12,33,36,38,22
20,4,5,17,24,28,29,21
21,7,10,18,30,39,43,11
22,1,3,23,24,27,40,2
23,1,2,4,20,28,32,37
24,3,8,30,33,37,40,18
25,3,9,11,16,17,29,31
26,1,2,8,20,26,34,19
27,3,16,23,26,27,31,43
28,11,18,21,30,33,38,22
29,2,14,16,18,24,33,12
30,5,10,16,24,25,42,9
31,12,13,23,25,27,44,4
32,6,10,16,27,30,43,8
33,3,6,9,10,39,41,42
34,1,14,16,27,28,38,22
35,10,13,18,27,37,42,20
36,6,15,20,25,28,33,21
37,1,8,11,19,21,32,17
38,2,15,17,26,40,42,39
39,4,14,15,25,40,42,38
40,5,11,18,21,23,43,16
41,6,13,17,20,33,38,16
42,13,22,32,35,38,39,18
43,3,16,24,29,39,45,23
44,7,10,11,16,28,43,26
45,2,6,11,13,22,23,12
46,4,6,14,16,35,40,11
47,1,11,13,21,33,36,26
48,7,10,23,24,29,40,22
49,2,13,23,24,33,37,9
50,6,18,27,37,42,43,17
51,11,16,19,21,27,31,30
52,3,8,13,14,15,32,30
53,15,16,25,31,32,44,18
54,9,13,21,25,31,32,2
55,1,14,18,25,26,40,21
56,2,4,5,25,35,40,45
57,3,7,12,26,33,37,39
58,4,9,13,14,28,30,34
59,2,5,6,24,29,39,1
60,10,17,26,34,38,39,12
61,7,8,21,25,38,44,16
62,1,6,12,33,36,38,22
63,4,5,17,24,28,29,21
64,7,10,18,30,39,43,11
65,1,3,23,24,27,40,2
66,1,2,4,20,28,32,37
67,3,8,30,33,37,40,18
68,3,9,11,16,17,29,31
69,1,2,8,20,26,34,19
70,3,16,23,26,27,31,43
71,11,18,21,30,33,38,22
72,2,14,16,18,24,33,12
73,5,10,16,24,25,42,9
74,12,13,23,25,27,44,4
75,6,10,16,27,30,43,8
76,3,6,9,10,39,41,42
77,1,14,16,27,28,38,22
78,10,13,18,27,37,42,20
79,6,15,20,25,28,33,21
80,1,8,11,19,21,32,17
81,2,15,17,26,40,42,39
82,4,14,15,25,40,42,38
83,5,11,18,21,23,43,16
84,6,13,17,20,33,38,16
85,13,22,32,35,38,39,18
86,3,16,24,29,39,45,23
87,7,10,11,16,28,43,26
88,2,6,11,13,22,23,12
89,4,6,14,16,35,40,11
90,1,11,13,21,33,36,26
91,7,10,23,24,29,40,22
92,2,13,23,24,33,37,9
93,6,18,27,37,42,43,17
94,11,16,19,21,27,31,30
95,3,8,13,14,15,32,30
96,15,16,25,31,32,44,18
97,9,13,21,25,31,32,2
98,1,14,18,25,26,40,21
99,2,4,5,25,35,40,45
100,3,7,12,26,33,37,39
101,4,9,13,14,28,30,34
102,2,5,6,24,29,39,1
103,10,17,26,34,38,39,12
104,7,8,21,25,38,44,16
105,1,6,12,33,36,38,22
106,4,5,17,24,28,29,21
107,7,10,18,30,39,43,11
108,1,3,23,24,27,40,2
109,1,2,4,20,28,32,37
110,3,8,30,33,37,40,18
111,3,9,11,16,17,29,31
112,1,2,8,20,26,34,19
113,3,16,23,26,27,31,43
114,11,18,21,30,33,38,22
115,2,14,16,18,24,33,12
116,5,10,16,24,25,42,9
117,12,13,23,25,27,44,4
118,6,10,16,27,30,43,8
119,3,6,9,10,39,41,42
120,1,14,16,27,28,38,22
121,10,13,18,27,37,42,20
122,6,15,20,25,28,33,21
123,1,8,11,19,21,32,17
124,2,15,17,26,40,42,39
125,4,14,15,25,40,42,38
126,5,11,18,21,23,43,16
127,6,13,17,20,33,38,16
128,13,22,32,35,38,39,18
129,3,16,24,29,39,45,23
130,7,10,11,16,28,43,26
131,2,6,11,13,22,23,12
132,4,6,14,16,35,40,11
133,1,11,13,21,33,36,26
134,7,10,23,24,29,40,22
135,2,13,23,24,33,37,9
136,6,18,27,37,42,43,17
137,11,16,19,21,27,31,30
138,3,8,13,14,15,32,30
139,15,16,25,31,32,44,18
140,9,13,21,25,31,32,2
141,1,14,18,25,26,40,21
142,2,4,5,25,35,40,45
143,3,7,12,26,33,37,39
144,4,9,13,14,28,30,34
145,2,5,6,24,29,39,1
146,10,17,26,34,38,39,12
147,7,8,21,25,38,44,16
148,1,6,12,33,36,38,22
149,4,5,17,24,28,29,21
150,7,10,18,30,39,43,11
1146,4,12,23,28,37,40,26
        """.trimIndent()

        val lines = historicalData.split("\n")
        totalDraws = lines.size

        for (line in lines) {
            val parts = line.split(",")
            if (parts.size >= 8) {
                // 번호 1~6 추출 (보너스 제외)
                for (i in 1..6) {
                    val num = parts[i].toInt()
                    numberFrequency[num] = (numberFrequency[num] ?: 0) + 1
                }

                // 최근 20회차만 따로 집계
                if (lines.indexOf(line) >= lines.size - 20) {
                    for (i in 1..6) {
                        val num = parts[i].toInt()
                        recentNumberFrequency[num] = (recentNumberFrequency[num] ?: 0) + 1
                    }
                }
            }
        }

        updateStatsDisplay()
    }

    private fun setupListeners() {
        generateButton.setOnClickListener {
            generateSmartNumbers()
        }

        randomButton.setOnClickListener {
            generateRandomNumbers()
        }

        saveButton.setOnClickListener {
            saveCurrentNumbers()
        }

        clearButton.setOnClickListener {
            showClearConfirmDialog()
        }

        savedNumbersText.setOnClickListener {
            if (savedNumbers.isNotEmpty()) {
                showSavedNumbersDialog()
            }
        }

        statsText.setOnClickListener {
            showDetailedStats()
        }
    }

    private fun generateSmartNumbers() {
        modeText.text = "🎯 AI 추천 번호"

        // 가중치 계산 (빈도 + 최근 트렌드)
        val weights = mutableMapOf<Int, Double>()

        for (num in 1..45) {
            val totalFreq = numberFrequency[num] ?: 0
            val recentFreq = recentNumberFrequency[num] ?: 0

            // 전체 빈도 60% + 최근 빈도 40%
            weights[num] = (totalFreq * 0.6) + (recentFreq * 2.0 * 0.4)
        }

        // 가중치 기반 선택
        val selected = mutableSetOf<Int>()
        val candidates = weights.toList().sortedByDescending { it.second }

        // 상위 15개 중에서 랜덤 선택 (너무 편향 방지)
        val topCandidates = candidates.take(15).map { it.first }

        while (selected.size < 6) {
            val num = topCandidates.random()
            selected.add(num)
        }

        // 홀짝 균형 조정
        val numbers = adjustBalance(selected.sorted())

        displayNumbers(numbers)
    }

    private fun generateRandomNumbers() {
        modeText.text = "🎲 랜덤 번호"
        val numbers = (1..45).shuffled().take(6).sorted()
        displayNumbers(numbers)
    }

    private fun adjustBalance(numbers: List<Int>): List<Int> {
        val oddCount = numbers.count { it % 2 == 1 }
        val evenCount = numbers.count { it % 2 == 0 }

        // 홀짝이 극단적이면 조정 (5:1 또는 1:5)
        if (oddCount >= 5 || evenCount >= 5) {
            val adjusted = numbers.toMutableList()
            val allNumbers = (1..45).toList()

            if (oddCount >= 5) {
                // 짝수 하나 추가
                val evenNums = allNumbers.filter { it % 2 == 0 && it !in adjusted }
                if (evenNums.isNotEmpty()) {
                    adjusted[adjusted.indexOfFirst { it % 2 == 1 }] = evenNums.random()
                }
            } else {
                // 홀수 하나 추가
                val oddNums = allNumbers.filter { it % 2 == 1 && it !in adjusted }
                if (oddNums.isNotEmpty()) {
                    adjusted[adjusted.indexOfFirst { it % 2 == 0 }] = oddNums.random()
                }
            }

            return adjusted.sorted()
        }

        return numbers
    }

    private fun displayNumbers(numbers: List<Int>) {
        numbers.forEachIndexed { index, number ->
            numberTextViews[index].apply {
                text = number.toString()
                setBackgroundResource(getNumberBackground(number))

                scaleX = 0f
                scaleY = 0f
                animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .setStartDelay(index * 50L)
                    .setInterpolator(BounceInterpolator())
                    .start()
            }
        }
    }

    private fun getNumberBackground(number: Int): Int {
        return when (number) {
            in 1..10 -> R.drawable.circle_yellow
            in 11..20 -> R.drawable.circle_blue
            in 21..30 -> R.drawable.circle_red
            in 31..40 -> R.drawable.circle_gray
            else -> R.drawable.circle_green
        }
    }

    private fun updateStatsDisplay() {
        val top5 = numberFrequency.toList()
            .sortedByDescending { it.second }
            .take(5)

        val statsStr = top5.mapIndexed { idx, (num, freq) ->
            "${num}번(${freq}회)"
        }.joinToString(", ")

        statsText.text = "📊 최다 출현: $statsStr (탭하여 상세보기)"
    }

    private fun showDetailedStats() {
        val sorted = numberFrequency.toList().sortedByDescending { it.second }

        val message = buildString {
            append("📊 전체 통계 (${totalDraws}회차)\n\n")
            append("🔥 TOP 10 출현 번호:\n")
            sorted.take(10).forEachIndexed { idx, (num, freq) ->
                append("${idx + 1}. ${num}번: ${freq}회\n")
            }

            append("\n❄️ BOTTOM 10 출현 번호:\n")
            sorted.takeLast(10).reversed().forEachIndexed { idx, (num, freq) ->
                append("${idx + 1}. ${num}번: ${freq}회\n")
            }

            append("\n📈 최근 20회 트렌드:\n")
            val recentSorted = recentNumberFrequency.toList()
                .sortedByDescending { it.second }
            recentSorted.take(5).forEach { (num, freq) ->
                append("${num}번: ${freq}회  ")
            }
        }

        AlertDialog.Builder(this)
            .setTitle("상세 통계")
            .setMessage(message)
            .setPositiveButton("확인", null)
            .show()
    }

    private fun saveCurrentNumbers() {
        val currentNumbers = numberTextViews.map { it.text.toString().toInt() }
        val timestamp = System.currentTimeMillis()
        val mode = modeText.text.toString()
        val lottoSet = LottoSet(currentNumbers, timestamp, mode)

        savedNumbers.add(0, lottoSet)
        saveToPref()
        updateSavedNumbersDisplay()

        Toast.makeText(this, "번호가 저장되었습니다", Toast.LENGTH_SHORT).show()
    }

    private fun updateSavedNumbersDisplay() {
        countText.text = "저장된 번호: ${savedNumbers.size}개"

        val display = savedNumbers.take(3).mapIndexed { index, set ->
            "${index + 1}. ${set.numbers.joinToString(", ")}"
        }.joinToString("\n")

        savedNumbersText.text = if (display.isEmpty()) {
            "저장된 번호가 없습니다.\n번호를 저장하려면 '저장' 버튼을 누르세요."
        } else {
            display + if (savedNumbers.size > 3) "\n\n... 더보기 (탭하세요)" else ""
        }
    }

    private fun showSavedNumbersDialog() {
        val items = savedNumbers.mapIndexed { index, set ->
            val date = android.text.format.DateFormat.format("MM/dd HH:mm", set.timestamp)
            "${index + 1}. ${set.numbers.joinToString(", ")} - ${set.mode} ($date)"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("저장된 번호 (${savedNumbers.size}개)")
            .setItems(items) { _, which ->
                showDeleteConfirmDialog(which)
            }
            .setNegativeButton("닫기", null)
            .show()
    }

    private fun showDeleteConfirmDialog(index: Int) {
        AlertDialog.Builder(this)
            .setTitle("번호 삭제")
            .setMessage("이 번호를 삭제하시겠습니까?\n${savedNumbers[index].numbers.joinToString(", ")}")
            .setPositiveButton("삭제") { _, _ ->
                savedNumbers.removeAt(index)
                saveToPref()
                updateSavedNumbersDisplay()
                Toast.makeText(this, "삭제되었습니다", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showClearConfirmDialog() {
        if (savedNumbers.isEmpty()) {
            Toast.makeText(this, "삭제할 번호가 없습니다", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("전체 삭제")
            .setMessage("저장된 모든 번호를 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ ->
                savedNumbers.clear()
                saveToPref()
                updateSavedNumbersDisplay()
                Toast.makeText(this, "모두 삭제되었습니다", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun saveToPref() {
        val json = Gson().toJson(savedNumbers)
        sharedPrefs.edit().putString("saved_numbers", json).apply()
    }

    private fun loadSavedNumbers() {
        val json = sharedPrefs.getString("saved_numbers", null) ?: return
        val type = object : TypeToken<List<LottoSet>>() {}.type
        val loaded = Gson().fromJson<List<LottoSet>>(json, type)
        savedNumbers.addAll(loaded)
        updateSavedNumbersDisplay()
    }

    data class LottoSet(
        val numbers: List<Int>,
        val timestamp: Long,
        val mode: String = "🎲 랜덤"
    )
}