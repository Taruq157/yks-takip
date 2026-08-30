package com.omerfaruk.ykstakip.ui

import android.app.TimePickerDialog
import android.widget.Toast
import org.json.JSONObject
import com.omerfaruk.ykstakip.data.SupabaseYigilma
import com.omerfaruk.ykstakip.data.local.CalculationHistoryEntity
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.em
import androidx.compose.ui.window.Dialog
import com.omerfaruk.ykstakip.data.local.NotificationSettings
import com.omerfaruk.ykstakip.data.local.Reward
import com.omerfaruk.ykstakip.data.local.TopicEntity
import com.omerfaruk.ykstakip.data.local.UserInfo
import com.omerfaruk.ykstakip.data.local.NetResultEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun OnboardingScreen(onSave: (String, String, String, String, String, String, String) -> Unit) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var selectedMajor by remember { mutableStateOf("Sayısal") }
    var selectedDisplayName by remember { mutableStateOf("") }
    var selectedExamYear by remember { mutableStateOf("2027") }
    var obpInput by remember { mutableStateOf("100.0") }
    
    val obp = obpInput.toFloatOrNull()
    val isObpValid = obp != null && obp >= 50f && obp <= 100f
    
    val majors = listOf("Sayısal", "Eşit Ağırlık", "Sözel")
    val examYears = listOf("2027", "2028")
    
    val displayOptions = listOf(
        title,
        "$title $firstName",
        "$title $firstName $lastName"
    ).filter { it.isNotBlank() }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Profilini Oluştur", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("Ad") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Soyad") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Unvan / Hitap (Örn: Mühendis, Şampiyon)") },
            modifier = Modifier.fillMaxWidth()
        )
        
        if (firstName.isNotBlank() && title.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Nasıl hitap edilmesini istersiniz?", fontWeight = FontWeight.Bold)
            
            displayOptions.forEach { option ->
                Row(
                    Modifier.fillMaxWidth().selectable(
                        selected = (option == selectedDisplayName),
                        onClick = { selectedDisplayName = option }
                    ).padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (option == selectedDisplayName),
                        onClick = { selectedDisplayName = option }
                    )
                    Text(text = option, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Sınav Yılı", fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            examYears.forEach { year ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = (year == selectedExamYear),
                        onClick = { selectedExamYear = year }
                    )
                    Text(text = year)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Alan Seçimi", fontWeight = FontWeight.Bold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            majors.forEach { major ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = (major == selectedMajor),
                        onClick = { selectedMajor = major }
                    )
                    Text(text = major)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = obpInput,
            onValueChange = { obpInput = it },
            label = { Text("OBP (50.0 - 100.0)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = !isObpValid,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { 
                if (firstName.isNotBlank() && lastName.isNotBlank() && title.isNotBlank() && selectedDisplayName.isNotBlank() && isObpValid) {
                    onSave(firstName, lastName, title, selectedMajor, selectedDisplayName, selectedExamYear, obpInput)
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = firstName.isNotBlank() && lastName.isNotBlank() && title.isNotBlank() && selectedDisplayName.isNotBlank() && isObpValid
        ) {
            Text("Başlayalım!")
        }
    }
}

@Composable
fun MainDashboard(
    userInfo: UserInfo?,
    overallProgress: Float,
    subjects: List<SubjectUiModel>,
    notifSettings: NotificationSettings?,
    newRewardFlow: Flow<Reward>,
    allStudyTimes: List<com.omerfaruk.ykstakip.data.local.StudyTimeEntity>,
    allQuestionLogs: List<com.omerfaruk.ykstakip.data.local.QuestionLogEntity>,
    allTopics: List<com.omerfaruk.ykstakip.data.local.TopicEntity>,
    onSubjectClick: (String, String) -> Unit,
    onUpdateNotif: (NotificationSettings) -> Unit,
    onAddQuestionLog: (String, String, Int, Int) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("TYT", "AYT")
    var showNotifIsland by remember { mutableStateOf(false) }
    var showAddQuestionDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val displayName = userInfo?.displayName ?: ""
    val major = userInfo?.major ?: ""
    val examYear = userInfo?.examYear ?: "2027"

    val examDate = when (examYear) {
        "2027" -> java.time.LocalDate.of(2027, 6, 19)
        "2028" -> java.time.LocalDate.of(2028, 6, 17)
        else -> java.time.LocalDate.of(2027, 6, 19)
    }
    val now = java.time.LocalDate.now()
    val daysLeftTotal = java.time.temporal.ChronoUnit.DAYS.between(now, examDate).coerceAtLeast(0)
    val period = java.time.Period.between(now, examDate)
    val monthsPart = period.years * 12 + period.months
    val daysPart = period.days
    
    val progressToExam = (daysLeftTotal.toFloat() / 365f).coerceIn(0f, 1f)

    LaunchedEffect(Unit) {
        newRewardFlow.collectLatest { reward ->
            Toast.makeText(context, "🏆 Yeni Ödül: ${reward.title.replace("\n", " ")} alındı!", Toast.LENGTH_LONG).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Logo, Günaydın, DisplayName, Notifications
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF65E72)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "YK",
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Günaydın,",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = displayName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = { showNotifIsland = true }) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Bildirimler",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Combined Countdown Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Sınava",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Medium
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = daysLeftTotal.toString(),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = " gün kaldı",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Alan: $major",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    Box(
                        contentAlignment = Alignment.Center, 
                        modifier = Modifier.size(90.dp).clickable { showNotifIsland = true }
                    ) {
                        CircularProgressIndicator(
                            progress = { progressToExam },
                            modifier = Modifier.fillMaxSize(),
                            strokeWidth = 8.dp,
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                            strokeCap = StrokeCap.Round
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (notifSettings?.displayFormat == "MonthsDays") {
                                Text(
                                    text = "$monthsPart A",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "$daysPart G",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = "%${(progressToExam * 100).toInt()}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            // Genel İlerleme Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Genel İlerleme", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            text = "%${(overallProgress * 100).toInt()}", 
                            fontWeight = FontWeight.Black, 
                            color = MaterialTheme.colorScheme.primary, 
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { overallProgress },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        strokeCap = StrokeCap.Round
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Derslerin konu takibinin %${(overallProgress * 100).toInt()}'si tamamlandı.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Günlük Soru Girdisi Card
            val todayStart = java.time.LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            val todayLogs = allQuestionLogs.filter { it.date >= todayStart }
            val todayCorrect = todayLogs.sumOf { it.correctCount }
            val todayWrong = todayLogs.sumOf { it.wrongCount }
            val todayTotal = todayCorrect + todayWrong

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "Günlük Soru Girdisi", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Bilgi",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        OutlinedButton(
                            onClick = { showAddQuestionDialog = true },
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Soru Ekle", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Doğru
                        val greenColor = Color(0xFF34D399)
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = greenColor.copy(alpha = 0.08f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, greenColor.copy(alpha = 0.15f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = greenColor, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "$todayCorrect", fontSize = 18.sp, fontWeight = FontWeight.Black, color = greenColor)
                                Text(text = "Doğru", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        
                        // Yanlış
                        val redColor = Color(0xFFF87171)
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = redColor.copy(alpha = 0.08f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, redColor.copy(alpha = 0.15f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(imageVector = Icons.Default.Cancel, contentDescription = null, tint = redColor, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "$todayWrong", fontSize = 18.sp, fontWeight = FontWeight.Black, color = redColor)
                                Text(text = "Yanlış", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        
                        // Net / Soru
                        val blueColor = Color(0xFF60A5FA)
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = blueColor.copy(alpha = 0.08f)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, blueColor.copy(alpha = 0.15f))
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(imageVector = Icons.Default.RemoveCircle, contentDescription = null, tint = blueColor, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "$todayTotal", fontSize = 18.sp, fontWeight = FontWeight.Black, color = blueColor)
                                Text(text = "Soru", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Tab Selector Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                tabs.forEachIndexed { index, title ->
                    val isSelected = selectedTab == index
                    Card(
                        onClick = { selectedTab = index },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Book,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = title,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Dersler Title and Total Time Header
            val totalStudyDurationSeconds = allStudyTimes.sumOf { it.durationSeconds }
            val totalHours = totalStudyDurationSeconds / 3600
            val totalMinutes = (totalStudyDurationSeconds % 3600) / 60
            val totalSeconds = totalStudyDurationSeconds % 60
            val formattedTotalTime = String.format("%02d:%02d:%02d", totalHours, totalMinutes, totalSeconds)

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Dersler", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Toplam Çalışma Süresi ",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formattedTotalTime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Subjects list in Column
            val filteredSubjects = if (selectedTab == 0) {
                subjects.filter { it.category == "TYT" }
            } else {
                subjects.filter { it.category.startsWith("AYT") }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                filteredSubjects.forEach { subject ->
                    val studyDurationSeconds = allStudyTimes.filter { it.subjectName == subject.name }.sumOf { it.durationSeconds }
                    SubjectCard(subject, studyDurationSeconds) { onSubjectClick(subject.name, subject.category) }
                }
            }
        }

        if (showNotifIsland) {
            Dialog(onDismissRequest = { showNotifIsland = false }) {
                NotificationConfigCard(
                    settings = notifSettings ?: NotificationSettings(),
                    onUpdate = { onUpdateNotif(it) },
                    onClose = { showNotifIsland = false }
                )
            }
        }

        if (showAddQuestionDialog) {
            AddQuestionDialog(
                allTopics = allTopics,
                userMajor = userInfo?.major ?: "Sayısal",
                onDismiss = { showAddQuestionDialog = false },
                onSave = { subject, topic, correct, wrong ->
                    onAddQuestionLog(subject, topic, correct, wrong)
                    showAddQuestionDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddQuestionDialog(
    allTopics: List<com.omerfaruk.ykstakip.data.local.TopicEntity>,
    userMajor: String,
    onDismiss: () -> Unit,
    onSave: (String, String, Int, Int) -> Unit
) {
    var dialogTab by remember { mutableIntStateOf(0) }
    val dialogTabs = listOf("TYT", "AYT")

    val filteredTopicsByTab = remember(allTopics, dialogTab, userMajor) {
        if (dialogTab == 0) {
            allTopics.filter { it.category == "TYT" }
        } else {
            val aytCats = when (userMajor) {
                "Sayısal" -> listOf("AYT_MAT", "AYT_GEO", "AYT_FIZ", "AYT_KIM", "AYT_BIY")
                "Eşit Ağırlık" -> listOf("AYT_MAT", "AYT_GEO", "AYT_EDB", "AYT_TAR1", "AYT_COG1")
                "Sözel" -> listOf("AYT_EDB", "AYT_TAR1", "AYT_COG1", "AYT_TAR2", "AYT_COG2", "AYT_FEL", "AYT_DIN")
                else -> listOf("AYT_MAT", "AYT_GEO", "AYT_FIZ", "AYT_KIM", "AYT_BIY")
            }
            allTopics.filter { it.category in aytCats }
        }
    }

    val uniqueSubjects = remember(filteredTopicsByTab) {
        val extracted = filteredTopicsByTab.map { it.subjectName }.distinct().sorted()
        if (extracted.isEmpty()) listOf("Matematik", "Türkçe", "Fizik", "Kimya", "Biyoloji") else extracted
    }
    
    var selectedSubject by remember { mutableStateOf(uniqueSubjects.firstOrNull() ?: "Matematik") }
    
    LaunchedEffect(uniqueSubjects) {
        uniqueSubjects.firstOrNull()?.let {
            selectedSubject = it
        }
    }
    
    val filteredTopics = remember(filteredTopicsByTab, selectedSubject) {
        val list = filteredTopicsByTab.filter { it.subjectName == selectedSubject }.map { it.title }.distinct().sorted()
        if (list.isEmpty()) listOf("Genel") else list
    }
    
    var selectedTopic by remember(selectedSubject) { mutableStateOf(filteredTopics.firstOrNull() ?: "Genel") }
    
    LaunchedEffect(filteredTopics) {
        filteredTopics.firstOrNull()?.let {
            selectedTopic = it
        }
    }
    
    var correctText by remember { mutableStateOf("0") }
    var wrongText by remember { mutableStateOf("0") }
    
    var subjectExpanded by remember { mutableStateOf(false) }
    var topicExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Çözülen Soru Sayısı Ekle",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // TYT / AYT TabRow
                TabRow(
                    selectedTabIndex = dialogTab,
                    containerColor = Color.Transparent,
                    divider = {},
                    indicator = { tabPositions ->
                        Box(
                            Modifier
                                .tabIndicatorOffset(tabPositions[dialogTab])
                                .height(4.dp)
                                .padding(horizontal = 24.dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    dialogTabs.forEachIndexed { index, title ->
                        Tab(
                            selected = dialogTab == index,
                            onClick = { dialogTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (dialogTab == index) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        )
                    }
                }

                // Ders Seçimi Dropdown
                Column {
                    Text(
                        text = "Ders Seçin",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    ExposedDropdownMenuBox(
                        expanded = subjectExpanded,
                        onExpandedChange = { subjectExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedSubject,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable, true),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = subjectExpanded,
                            onDismissRequest = { subjectExpanded = false }
                        ) {
                            uniqueSubjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = { Text(subject) },
                                    onClick = {
                                        selectedSubject = subject
                                        subjectExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Konu Seçimi Dropdown
                Column {
                    Text(
                        text = "Konu Seçin",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    ExposedDropdownMenuBox(
                        expanded = topicExpanded,
                        onExpandedChange = { topicExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedTopic,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = topicExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(androidx.compose.material3.MenuAnchorType.PrimaryNotEditable, true),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            )
                        )
                        ExposedDropdownMenu(
                            expanded = topicExpanded,
                            onDismissRequest = { topicExpanded = false }
                        ) {
                            filteredTopics.forEach { topic ->
                                DropdownMenuItem(
                                    text = { Text(topic) },
                                    onClick = {
                                        selectedTopic = topic
                                        topicExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Doğru Sayısı Giriş
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Doğru Sayısı", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val current = correctText.toIntOrNull() ?: 0
                                if (current > 0) correctText = (current - 1).toString()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        ) {
                            Text(
                                text = "−",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        OutlinedTextField(
                            value = correctText,
                            onValueChange = {
                                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                    correctText = it
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .width(70.dp)
                                .padding(horizontal = 8.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 16.sp),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        IconButton(
                            onClick = {
                                val current = correctText.toIntOrNull() ?: 0
                                correctText = (current + 1).toString()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        ) {
                            Text(
                                text = "+",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Yanlış Sayısı Giriş
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Yanlış Sayısı", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                val current = wrongText.toIntOrNull() ?: 0
                                if (current > 0) wrongText = (current - 1).toString()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        ) {
                            Text(
                                text = "−",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        OutlinedTextField(
                            value = wrongText,
                            onValueChange = {
                                if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                                    wrongText = it
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .width(70.dp)
                                .padding(horizontal = 8.dp),
                            textStyle = androidx.compose.ui.text.TextStyle(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, fontSize = 16.sp),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        IconButton(
                            onClick = {
                                val current = wrongText.toIntOrNull() ?: 0
                                wrongText = (current + 1).toString()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        ) {
                            Text(
                                text = "+",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val correct = correctText.toIntOrNull() ?: 0
                    val wrong = wrongText.toIntOrNull() ?: 0
                    onSave(selectedSubject, selectedTopic, correct, wrong)
                },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(text = "Kaydet", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "İptal", fontWeight = FontWeight.Bold)
            }
        }
    )
}


@Composable
fun SubjectCard(subject: SubjectUiModel, studyDurationSeconds: Long, onClick: () -> Unit) {
    // Choose dynamic initials and icon colors
    val (initials, circleColor) = when (subject.name) {
        "Türkçe", "TYT Türkçe", "AYT Türkçe" -> "Tt" to Color(0xFFF65E72)
        "Matematik", "Matematik-1", "Matematik-2", "TYT Matematik", "AYT Matematik", "Geometri", "AYT Geometri" -> "√x" to Color(0xFF60A5FA)
        "Fizik", "AYT Fizik" -> "Fz" to Color(0xFF9C27B0)
        "Kimya", "AYT Kimya" -> "Km" to Color(0xFF34D399)
        "Biyoloji", "AYT Biyoloji" -> "By" to Color(0xFFFF9800)
        "Edebiyat", "AYT Edebiyat" -> "Ed" to Color(0xFFE91E63)
        "Tarih", "AYT Tarih" -> "Tr" to Color(0xFF8B5CF6)
        "Coğrafya", "AYT Coğrafya" -> "Cğ" to Color(0xFF10B981)
        "Felsefe", "Felsefe Grubu", "AYT Felsefe Grubu" -> "Fl" to Color(0xFF3F51B5)
        "Din Kültürü", "Din", "AYT Din" -> "Dn" to Color(0xFF009688)
        else -> {
            val letters = if (subject.name.length >= 2) subject.name.take(2) else subject.name
            letters to MaterialTheme.colorScheme.primary
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Circle Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(circleColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = circleColor
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Center Column (Name, Progress bar)
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = subject.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "%${(subject.progress * 100).toInt()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = circleColor
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { subject.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = circleColor,
                    trackColor = circleColor.copy(alpha = 0.15f),
                    strokeCap = StrokeCap.Round
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Right Column (Time & Chevron)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val h = studyDurationSeconds / 3600
                val m = (studyDurationSeconds % 3600) / 60
                val s = studyDurationSeconds % 60
                val timeStr = String.format(java.util.Locale.getDefault(), "%02d:%02d:%02d", h, m, s)
                
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = timeStr,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    subjectName: String,
    topics: List<TopicEntity>,
    allStudyTimes: List<com.omerfaruk.ykstakip.data.local.StudyTimeEntity>,
    onToggleTopic: (TopicEntity) -> Unit,
    onBack: () -> Unit
) {
    val progress = if (topics.isEmpty()) 0f else topics.count { it.isCompleted }.toFloat() / topics.size

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                CenterAlignedTopAppBar(
                    title = { Text(subjectName, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.Default.Home, contentDescription = "Geri")
                        }
                    }
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(4.dp)
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(topics) { topic ->
                val topicDurationSeconds = allStudyTimes.filter { it.subjectName == subjectName && it.topicTitle == topic.title }.sumOf { it.durationSeconds }
                TopicItem(topic = topic, topicDurationSeconds = topicDurationSeconds, onToggle = { onToggleTopic(topic) })
            }
        }
    }
}

@Composable
fun TopicItem(topic: TopicEntity, topicDurationSeconds: Long, onToggle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (topic.isCompleted) 
                MaterialTheme.colorScheme.primary.copy(alpha = 0.05f) 
                else MaterialTheme.colorScheme.surface
        ),
        border = if (topic.isCompleted) 
            null 
            else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = topic.isCompleted,
                onCheckedChange = { onToggle() }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = topic.title,
                    fontSize = 16.sp,
                    fontWeight = if (topic.isCompleted) FontWeight.Medium else FontWeight.Normal,
                    color = if (topic.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                if (topicDurationSeconds > 0) {
                    val h = topicDurationSeconds / 3600
                    val m = (topicDurationSeconds % 3600) / 60
                    val s = topicDurationSeconds % 60
                    Text(
                        text = String.format(java.util.Locale.getDefault(), "%02d:%02d:%02d", h, m, s), 
                        fontSize = 12.sp, 
                        color = MaterialTheme.colorScheme.secondary, 
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            if (topic.isCompleted) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ProfileScreen(
    userInfo: UserInfo?,
    overallProgress: Float,
    rewards: List<Reward>,
    onEditClick: () -> Unit,
    onRewardsClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.align(Alignment.TopEnd)
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Profili Düzenle")
                    }
                }
                
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userInfo?.firstName?.take(1) ?: "U",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = userInfo?.displayName ?: "Kullanıcı", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(text = userInfo?.major ?: "", color = MaterialTheme.colorScheme.primary)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(
                title = "Genel İlerleme",
                value = "%${(overallProgress * 100).toInt()}",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Ödüller",
                value = rewards.filter { it.isAchieved }.size.toString(),
                modifier = Modifier.weight(1f),
                onClick = onRewardsClick
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Başarımlar ve Ödüller", fontWeight = FontWeight.Bold, fontSize = 20.sp, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(12.dp))

        if (rewards.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                Text(text = "Henüz ödül kazanılmadı. Çalışmaya devam et!", color = Color.Gray)
            }
        } else {
            rewards.filter { it.isAchieved }.take(3).forEach { reward ->
                RewardItem(reward)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (rewards.any { it.isAchieved }) {
                TextButton(onClick = onRewardsClick) {
                    Text("Tüm Ödülleri Gör")
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLogoutClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Çıkış Yap",
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Hesaptan Çıkış Yap", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun StatCard(title: String, value: String, modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    Card(
        modifier = modifier.then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, fontSize = 14.sp, color = Color.Gray)
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RewardItem(reward: Reward) {
    Card(
        modifier = Modifier.fillMaxWidth().alpha(if (reward.isAchieved) 1f else 0.5f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (reward.isAchieved) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = if (reward.isAchieved) "🏆" else "🔒", fontSize = 32.sp)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = reward.title, fontWeight = FontWeight.Bold)
                Text(
                    text = when(reward.level) {
                        1 -> "Acemi Seviye"
                        2 -> "Tecrübeli Seviye"
                        3 -> "Uzman Seviye"
                        else -> ""
                    }, 
                    fontSize = 12.sp, 
                    color = Color.Gray
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    userInfo: UserInfo?,
    onSave: (String, String, String, String, String, String, String) -> Unit,
    onBack: () -> Unit
) {
    var firstName by remember { mutableStateOf(userInfo?.firstName ?: "") }
    var lastName by remember { mutableStateOf(userInfo?.lastName ?: "") }
    var title by remember { mutableStateOf(userInfo?.title ?: "") }
    var selectedMajor by remember { mutableStateOf(userInfo?.major ?: "Sayısal") }
    var selectedDisplayName by remember { mutableStateOf(userInfo?.displayName ?: "") }
    var selectedExamYear by remember { mutableStateOf(if (userInfo?.examYear == "2026") "2027" else (userInfo?.examYear ?: "2027")) }
    var obpInput by remember { mutableStateOf(userInfo?.obp ?: "100.0") }

    val obp = obpInput.toFloatOrNull()
    val isObpValid = obp != null && obp >= 50f && obp <= 100f

    val majors = listOf("Sayısal", "Eşit Ağırlık", "Sözel")
    val examYears = listOf("2027", "2028")
    val context = LocalContext.current

    val displayOptions = listOf(
        title,
        "$title $firstName",
        "$title $firstName $lastName"
    ).filter { it.isNotBlank() }.distinct()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profili Düzenle") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("Ad") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Soyad") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Unvan / Hitap") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = obpInput,
                onValueChange = { obpInput = it },
                label = { Text("Diploma Notu (OBP) (50 - 100)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                ),
                isError = !isObpValid,
                supportingText = {
                    if (!isObpValid) {
                        Text("Geçerli bir diploma notu girin (50 - 100 arası).", color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("Yerleştirme puanınıza bu değer * 0.6 eklenecektir.")
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Nasıl hitap edilsin?", fontWeight = FontWeight.Bold)
            displayOptions.forEach { option ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = (option == selectedDisplayName),
                            onClick = { selectedDisplayName = option }
                        )
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (option == selectedDisplayName),
                        onClick = { selectedDisplayName = option }
                    )
                    Text(text = option, modifier = Modifier.padding(start = 8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Sınav Yılı", fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                examYears.forEach { year ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = (year == selectedExamYear),
                            onClick = { selectedExamYear = year }
                        )
                        Text(text = year)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Alan Seçimi", fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                majors.forEach { major ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = (major == selectedMajor),
                            onClick = { selectedMajor = major }
                        )
                        Text(text = major)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = {
                    onSave(firstName, lastName, title, selectedMajor, selectedDisplayName, selectedExamYear, obpInput)
                    Toast.makeText(context, "Profil güncellendi!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = firstName.isNotBlank() && lastName.isNotBlank() && title.isNotBlank() && selectedDisplayName.isNotBlank() && isObpValid
            ) {
                Text("Güncelle")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RewardsScreen(
    rewards: List<Reward>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Başarımlar ve Ödüller") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(rewards) { reward ->
                RewardItem(reward)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: String,
    onThemeChange: (String) -> Unit,
    currentFontSizeMultiplier: Float,
    onFontSizeChange: (Float) -> Unit,
    onResetData: () -> Unit
) {
    val themeOptions = listOf("Light" to "Açık", "Dark" to "Koyu", "System" to "Sistem")
    val fontSizeOptions = listOf(0.8f, 1.0f, 1.2f, 1.4f)
    var showResetDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Verileri Sıfırla") },
            text = { Text("Tüm başarılarınız ve konu ilerlemeniz kalıcı olarak silinecektir. Emin misiniz?") },
            confirmButton = {
                TextButton(onClick = {
                    onResetData()
                    showResetDialog = false
                    Toast.makeText(context, "Veriler sıfırlandı!", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Evet, Sıfırla", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Vazgeç")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ayarlar") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Tema Seçimi", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            themeOptions.forEach { (value, label) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onThemeChange(value) }
                        .padding(vertical = 8.dp)
                ) {
                    RadioButton(selected = currentTheme == value, onClick = { onThemeChange(value) })
                    Text(label, modifier = Modifier.padding(start = 8.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Yazı Boyutu", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                fontSizeOptions.forEach { multiplier ->
                    FilterChip(
                        selected = currentFontSizeMultiplier == multiplier,
                        onClick = { onFontSizeChange(multiplier) },
                        label = { Text("${(multiplier * 100).toInt()}%") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Tüm Verileri Sıfırla")
            }
        }
    }
}

@Composable
fun NotificationConfigCard(
    settings: NotificationSettings,
    onUpdate: (NotificationSettings) -> Unit,
    onClose: () -> Unit
) {
    var isEnabled by remember { mutableStateOf(settings.isEnabled) }
    var selectedFormat by remember { mutableStateOf(settings.displayFormat) }
    var selectedHour by remember { mutableIntStateOf(settings.hour) }
    var selectedMinute by remember { mutableIntStateOf(settings.minute) }
    
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = "Bildirim Ayarları", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Hatırlatıcıları Etkinleştir", modifier = Modifier.weight(1f))
                Switch(checked = isEnabled, onCheckedChange = { isEnabled = it })
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Geri Sayım Formatı", fontWeight = FontWeight.SemiBold)
            Row {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedFormat = "Days" }) {
                    RadioButton(selected = selectedFormat == "Days", onClick = { selectedFormat = "Days" })
                    Text("Sadece Gün")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { selectedFormat = "MonthsDays" }) {
                    RadioButton(selected = selectedFormat == "MonthsDays", onClick = { selectedFormat = "MonthsDays" })
                    Text("Ay / Gün")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    TimePickerDialog(context, { _, h, m ->
                        selectedHour = h
                        selectedMinute = m
                    }, selectedHour, selectedMinute, true).show()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
            ) {
                Text(text = "Bildirim Saati: ${String.format(Locale.getDefault(), "%02d:%02d", selectedHour, selectedMinute)}")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onClose) { Text("Vazgeç") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    onUpdate(NotificationSettings(
                        isEnabled = isEnabled,
                        selectedDays = settings.selectedDays,
                        hour = selectedHour,
                        minute = selectedMinute,
                        displayFormat = selectedFormat
                    ))
                    onClose()
                }) {
                    Text("Kaydet")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtraAppsScreen(onAppClick: (String) -> Unit) {
    val apps = listOf(
        AppItem("Pomodoro", "pomodoro"),
        AppItem("Kronometre", "stopwatch"),
        AppItem("Net Takibi", "net_tracking"),
        AppItem("Soru Takibi", "question_tracking"),
        AppItem("Yanlış Soru Deposu", "wrong_questions"),
        AppItem("Deneme Zamanlayıcı", "exam_timer"),
        AppItem("Sıralama Hesapla", "score_calculation")
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Ek Uygulamalar", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            items(apps) { app ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clickable { onAppClick(app.route) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(text = app.name, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

data class AppItem(val name: String, val route: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceholderAppScreen(appName: String, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(appName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
            Text("$appName yakında eklenecek!", fontWeight = FontWeight.Medium, fontSize = 18.sp)
        }
    }
}

enum class PomodoroMode { WORK, SHORT_BREAK, LONG_BREAK }

@Composable
fun PomodoroScreen(onBack: () -> Unit, onSaveTime: (Long) -> Unit) {
    val context = LocalContext.current
    var timeLeft by remember { mutableIntStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }
    var currentMode by remember { mutableStateOf(PomodoroMode.WORK) }
    
    var pomodoroCount by remember { mutableIntStateOf(1) }
    var shortBreakCount by remember { mutableIntStateOf(0) }
    var longBreakCount by remember { mutableIntStateOf(0) }
    
    var showResetDialog by remember { mutableStateOf(false) }

    fun skipMode(shouldAutoStart: Boolean = true) {
        if (currentMode == PomodoroMode.WORK) {
            if (pomodoroCount % 4 == 0) {
                currentMode = PomodoroMode.LONG_BREAK
                longBreakCount++
                timeLeft = 35 * 60
            } else {
                currentMode = PomodoroMode.SHORT_BREAK
                shortBreakCount++
                timeLeft = 5 * 60
            }
        } else {
            currentMode = PomodoroMode.WORK
            pomodoroCount++
            timeLeft = 25 * 60
        }
        isRunning = shouldAutoStart
    }

    LaunchedEffect(isRunning, timeLeft) {
        if (isRunning && timeLeft > 0) {
            delay(1000L)
            timeLeft -= 1
        } else if (timeLeft == 0 && isRunning) {
            skipMode(shouldAutoStart = true)
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Pomodoro'yu Sıfırla") },
            text = { Text("Tüm ilerlemeniz silinecek ve 1. Pomodoro'ya döneceksiniz. Emin misiniz?") },
            confirmButton = {
                TextButton(onClick = {
                    isRunning = false
                    currentMode = PomodoroMode.WORK
                    pomodoroCount = 1
                    shortBreakCount = 0
                    longBreakCount = 0
                    timeLeft = 25 * 60
                    showResetDialog = false
                }) {
                    Text("Evet", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Vazgeç")
                }
            }
        )
    }

    val targetColor = when (currentMode) {
        PomodoroMode.WORK -> Color(0xFFC63C2C) // Kırmızı
        PomodoroMode.SHORT_BREAK -> Color(0xFF2C78C6) // Mavi
        PomodoroMode.LONG_BREAK -> Color(0xFF388E3C) // Yeşil
    }
    val backgroundColor by animateColorAsState(targetColor, label = "backgroundColor")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Pomodoro Zamanlayıcı",
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(60.dp))

            val minutes = timeLeft / 60
            val seconds = timeLeft % 60
            Text(
                text = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds),
                fontSize = 100.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(80.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .clickable { isRunning = !isRunning },
                        color = Color.White
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (isRunning) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Box(modifier = Modifier.size(width = 6.dp, height = 24.dp).background(targetColor))
                                    Box(modifier = Modifier.size(width = 6.dp, height = 24.dp).background(targetColor))
                                }
                            } else {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Başlat",
                                    tint = targetColor,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("BAŞLAT/DURDUR", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .clickable {
                                val totalWorkSeconds = (pomodoroCount - 1) * 25 * 60 + if (currentMode == PomodoroMode.WORK) (25 * 60 - timeLeft) else 0
                                onSaveTime(totalWorkSeconds.toLong())
                            },
                        color = Color.White
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Kaydet",
                                tint = targetColor,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("KAYDET", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .clickable { showResetDialog = true },
                        color = Color(0xFF9E9E9E)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sıfırla",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("SIFIRLA", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(80.dp))

            val currentLabel = when (currentMode) {
                PomodoroMode.WORK -> "$pomodoroCount. Pomodoro"
                PomodoroMode.SHORT_BREAK -> "$shortBreakCount. Kısa Mola"
                PomodoroMode.LONG_BREAK -> "$longBreakCount. Uzun Mola"
            }
            
            val nextLabel = when (currentMode) {
                PomodoroMode.WORK -> if (pomodoroCount % 4 == 0) "Uzun Mola" else "Kısa Mola"
                else -> "Pomodoro"
            }

            Text(
                text = currentLabel,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Sıradaki Mod: $nextLabel",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .size(56.dp)
                .clip(CircleShape)
                .clickable { skipMode(shouldAutoStart = true) },
            color = Color.White.copy(alpha = 0.2f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Hızlı Geç",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun StopwatchScreen(onBack: () -> Unit, onSaveTime: (Long) -> Unit) {
    val context = LocalContext.current
    var timeMillis by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showDevSheet by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            var lastTime = System.currentTimeMillis()
            while (isRunning) {
                delay(10L)
                val now = System.currentTimeMillis()
                timeMillis += (now - lastTime)
                lastTime = now
            }
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Kronometreyi Sıfırla") },
            text = { Text("Süre sıfırlanacaktır. Emin misiniz?") },
            confirmButton = {
                TextButton(onClick = {
                    isRunning = false
                    timeMillis = 0L
                    showResetDialog = false
                }) {
                    Text("Evet", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Vazgeç")
                }
            }
        )
    }

    val hours = (timeMillis / (1000 * 60 * 60))
    val minutes = (timeMillis / (1000 * 60)) % 60
    val seconds = (timeMillis / 1000) % 60
    val centis = (timeMillis / 10) % 100

    val backgroundColor = Color(0xFF121212)
    val digitalGreen = Color(0xFF76FF03)

    val days = listOf("PZT", "SAL", "ÇAR", "PER", "CUM", "CMT", "PAZ")
    val currentDayIndex = Calendar.getInstance().get(Calendar.DAY_OF_WEEK).let {
        if (it == Calendar.SUNDAY) 6 else it - 2
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(80.dp),
                color = Color.Black.copy(alpha = 0.3f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "kronometre",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Light,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                days.forEachIndexed { index, day ->
                    Text(
                        text = day,
                        color = if (index == currentDayIndex) digitalGreen else Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = if (index == currentDayIndex) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "8:88:88",
                    color = digitalGreen.copy(alpha = 0.05f),
                    fontSize = 80.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds),
                    color = digitalGreen,
                    fontSize = 80.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = String.format(Locale.getDefault(), ".%02d", centis),
                color = digitalGreen,
                fontSize = 30.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 60.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StopwatchButton(
                    label = "BAŞLAT / DURDUR",
                    onClick = { isRunning = !isRunning },
                    color = digitalGreen.copy(alpha = 0.2f),
                    contentColor = digitalGreen,
                    icon = {
                        if (isRunning) {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(modifier = Modifier.size(width = 4.dp, height = 24.dp).background(digitalGreen))
                                Box(modifier = Modifier.size(width = 4.dp, height = 24.dp).background(digitalGreen))
                            }
                        } else {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = digitalGreen, modifier = Modifier.size(32.dp))
                        }
                    }
                )

                StopwatchButton(
                    label = "KAYDET",
                    onClick = { 
                        val totalSeconds = timeMillis / 1000L
                        onSaveTime(totalSeconds)
                    },
                    color = digitalGreen.copy(alpha = 0.4f),
                    contentColor = digitalGreen,
                    icon = {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = digitalGreen, modifier = Modifier.size(32.dp))
                    }
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "GELİŞTİRİCİ",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                            .clickable { showDevSheet = true }
                    )
                    StopwatchButton(
                        label = "SIFIRLA",
                        onClick = {
                            showResetDialog = true
                        },
                        color = Color.Gray.copy(alpha = 0.2f),
                        contentColor = Color.LightGray,
                        icon = {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(32.dp))
                        }
                    )
                }
            }
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier.padding(16.dp).align(Alignment.TopStart)
        ) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
        }
    }

    if (showDevSheet) {
        val sheetState = androidx.compose.material3.rememberModalBottomSheetState()
        var devHours by remember { mutableStateOf(hours.toString()) }
        var devMinutes by remember { mutableStateOf(minutes.toString()) }
        var devSeconds by remember { mutableStateOf(seconds.toString()) }

        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showDevSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Geliştirici - Süre Ayarla", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedTextField(
                        value = devHours,
                        onValueChange = { devHours = it },
                        label = { Text("Saat") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).padding(4.dp)
                    )
                    OutlinedTextField(
                        value = devMinutes,
                        onValueChange = { devMinutes = it },
                        label = { Text("Dakika") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).padding(4.dp)
                    )
                    OutlinedTextField(
                        value = devSeconds,
                        onValueChange = { devSeconds = it },
                        label = { Text("Saniye") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).padding(4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val h = devHours.toLongOrNull() ?: 0L
                        val m = devMinutes.toLongOrNull() ?: 0L
                        val s = devSeconds.toLongOrNull() ?: 0L
                        timeMillis = (h * 3600 + m * 60 + s) * 1000L
                        showDevSheet = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Süreyi Uygula")
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun StopwatchButton(
    label: String,
    onClick: () -> Unit,
    color: Color,
    contentColor: Color,
    icon: @Composable () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier
                .size(70.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onClick() },
            color = color
        ) {
            Box(contentAlignment = Alignment.Center) {
                icon()
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(80.dp)
        )
    }
}

fun getBranchesForMajor(major: String): List<Pair<String, Int>> {
    val tyt = listOf(
        "TYT Türkçe" to 40,
        "TYT Sosyal" to 20,
        "TYT Matematik" to 40,
        "TYT Fen" to 20
    )
    val ayt = when(major) {
        "Sayısal" -> listOf(
            "AYT Matematik" to 40,
            "AYT Fizik" to 14,
            "AYT Kimya" to 13,
            "AYT Biyoloji" to 13
        )
        "Eşit Ağırlık" -> listOf(
            "AYT Matematik" to 40,
            "AYT Edebiyat" to 24,
            "AYT Tarih-1" to 10,
            "AYT Coğrafya-1" to 6
        )
        "Sözel" -> listOf(
            "AYT Edebiyat" to 24,
            "AYT Tarih-1" to 10,
            "AYT Coğrafya-1" to 6,
            "AYT Tarih-2" to 11,
            "AYT Coğrafya-2" to 11,
            "AYT Felsefe Grubu" to 12,
            "AYT Din Kültürü" to 6
        )
        else -> emptyList()
    }
    return tyt + ayt
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetTrackingScreen(viewModel: YksViewModel, userInfo: UserInfo?, onBack: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("TYT", "AYT", "Branş")
    var showAddNetDialog by remember { mutableStateOf(false) }
    var selectedDetailResult by remember { mutableStateOf<NetResultEntity?>(null) }

    
    val major = userInfo?.major ?: "Sayısal"
    val branches = remember(major) { getBranchesForMajor(major) }
    var selectedBranch by remember { mutableStateOf(branches.firstOrNull()?.first ?: "TYT Türkçe") }
    var expandedBranchList by remember { mutableStateOf(false) }
    
    val tytResults by viewModel.tytNetResults.collectAsState()
    val aytResults by viewModel.aytNetResults.collectAsState()
    val bransResults by viewModel.bransNetResults.collectAsState()

    val currentResults = when (selectedTab) {
        0 -> tytResults
        1 -> aytResults
        else -> bransResults.filter { it.details.startsWith("$selectedBranch:") }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(64.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                    Text("Net Takibi", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    if (selectedTab == 2) {
                        Box {
                            TextButton(onClick = { expandedBranchList = true }) {
                                Text(selectedBranch, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = expandedBranchList,
                                onDismissRequest = { expandedBranchList = false },
                            ) {
                                branches.forEach { branch ->
                                    DropdownMenuItem(
                                        text = { Text(branch.first) },
                                        onClick = {
                                            selectedBranch = branch.first
                                            expandedBranchList = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (currentResults.isEmpty()) {
                    Text("Henüz veri yok. İlk denemeni ekle!", color = Color.Gray)
                } else {
                    val maxNet = when (selectedTab) {
                        0 -> 120f
                        1 -> 80f
                        else -> (branches.find { it.first == selectedBranch }?.second ?: 40).toFloat()
                    }
                    NetStatisticsGraph(
                        results = currentResults, 
                        maxNet = maxNet,
                        onPointClick = { result -> selectedDetailResult = result }
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp)) 
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .clickable { showAddNetDialog = true }
                        .shadow(8.dp, CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Ekle", tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(32.dp))
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    modifier = Modifier
                        .height(64.dp)
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(32.dp)),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tabs.forEachIndexed { index, title ->
                            val selected = selectedTab == index
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { selectedTab = index },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = title,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showAddNetDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .blur(8.dp) 
                    .clickable { showAddNetDialog = false }
            )
            
            Dialog(onDismissRequest = { showAddNetDialog = false }) {
                AddNetMiniScreen(
                    major = major,
                    onSave = { type, total, details ->
                        viewModel.saveNetResult(type, total, details)
                        showAddNetDialog = false
                    },
                    onDismiss = { showAddNetDialog = false }
                )
            }
        }

        if (selectedDetailResult != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .blur(8.dp) 
                    .clickable { selectedDetailResult = null }
            )
            
            Dialog(onDismissRequest = { selectedDetailResult = null }) {
                NetDetailDialog(
                    result = selectedDetailResult!!,
                    onUpdate = { updated ->
                        viewModel.updateNetResult(updated)
                        selectedDetailResult = null
                    },
                    onDelete = {
                        viewModel.deleteNetResult(selectedDetailResult!!.id)
                        selectedDetailResult = null
                    },
                    onDismiss = { selectedDetailResult = null }
                )
            }
        }
    }
}

@Composable
fun AddNetMiniScreen(major: String, onSave: (String, Float, String) -> Unit, onDismiss: () -> Unit) {
    var selectedType by remember { mutableStateOf("TYT") }
    val types = listOf("TYT", "AYT", "Branş")
    
    val branches = remember(major) { getBranchesForMajor(major) }
    var selectedBranch by remember { mutableStateOf(branches.firstOrNull()?.first ?: "TYT Türkçe") }
    var expandedBranchList by remember { mutableStateOf(false) }
    
    var bransCorrect by remember { mutableStateOf("") }
    var bransWrong by remember { mutableStateOf("") }
    
    // TYT States
    var tytTurkceD by remember { mutableStateOf("") }
    var tytTurkceY by remember { mutableStateOf("") }
    var tytSosyalD by remember { mutableStateOf("") }
    var tytSosyalY by remember { mutableStateOf("") }
    var tytMatD by remember { mutableStateOf("") }
    var tytMatY by remember { mutableStateOf("") }
    var tytFenD by remember { mutableStateOf("") }
    var tytFenY by remember { mutableStateOf("") }

    // AYT States
    var aytMatD by remember { mutableStateOf("") }
    var aytMatY by remember { mutableStateOf("") }
    var aytFizD by remember { mutableStateOf("") }
    var aytFizY by remember { mutableStateOf("") }
    var aytKimD by remember { mutableStateOf("") }
    var aytKimY by remember { mutableStateOf("") }
    var aytBiyD by remember { mutableStateOf("") }
    var aytBiyY by remember { mutableStateOf("") }
    
    var aytEdbD by remember { mutableStateOf("") }
    var aytEdbY by remember { mutableStateOf("") }
    var aytTar1D by remember { mutableStateOf("") }
    var aytTar1Y by remember { mutableStateOf("") }
    var aytCog1D by remember { mutableStateOf("") }
    var aytCog1Y by remember { mutableStateOf("") }
    
    var aytTar2D by remember { mutableStateOf("") }
    var aytTar2Y by remember { mutableStateOf("") }
    var aytCog2D by remember { mutableStateOf("") }
    var aytCog2Y by remember { mutableStateOf("") }
    var aytFelD by remember { mutableStateOf("") }
    var aytFelY by remember { mutableStateOf("") }
    var aytDinD by remember { mutableStateOf("") }
    var aytDinY by remember { mutableStateOf("") }

    val calculateNet = { d: String, y: String ->
        val correct = d.toIntOrNull() ?: 0
        val wrong = y.toIntOrNull() ?: 0
        correct - (wrong * 0.25f)
    }

    val isError = { d: String, y: String, max: Int -> (d.toIntOrNull() ?: 0) + (y.toIntOrNull() ?: 0) > max }

    val tytError = isError(tytTurkceD, tytTurkceY, 40) || isError(tytSosyalD, tytSosyalY, 20) || isError(tytMatD, tytMatY, 40) || isError(tytFenD, tytFenY, 20)
    
    val aytError = when(major) {
        "Sayısal" -> isError(aytMatD, aytMatY, 40) || isError(aytFizD, aytFizY, 14) || isError(aytKimD, aytKimY, 13) || isError(aytBiyD, aytBiyY, 13)
        "Eşit Ağırlık" -> isError(aytMatD, aytMatY, 40) || isError(aytEdbD, aytEdbY, 24) || isError(aytTar1D, aytTar1Y, 10) || isError(aytCog1D, aytCog1Y, 6)
        "Sözel" -> isError(aytEdbD, aytEdbY, 24) || isError(aytTar1D, aytTar1Y, 10) || isError(aytCog1D, aytCog1Y, 6) || isError(aytTar2D, aytTar2Y, 11) || isError(aytCog2D, aytCog2Y, 11) || isError(aytFelD, aytFelY, 12) || isError(aytDinD, aytDinY, 6)
        else -> false
    }

    val bransMax = branches.find { it.first == selectedBranch }?.second ?: 40
    val bransError = isError(bransCorrect, bransWrong, bransMax)

    val hasError = when(selectedType) {
        "TYT" -> tytError
        "AYT" -> aytError
        else -> bransError
    }

    val totalNet = when (selectedType) {
        "TYT" -> calculateNet(tytTurkceD, tytTurkceY) + calculateNet(tytSosyalD, tytSosyalY) + calculateNet(tytMatD, tytMatY) + calculateNet(tytFenD, tytFenY)
        "AYT" -> when(major) {
            "Sayısal" -> calculateNet(aytMatD, aytMatY) + calculateNet(aytFizD, aytFizY) + calculateNet(aytKimD, aytKimY) + calculateNet(aytBiyD, aytBiyY)
            "Eşit Ağırlık" -> calculateNet(aytMatD, aytMatY) + calculateNet(aytEdbD, aytEdbY) + calculateNet(aytTar1D, aytTar1Y) + calculateNet(aytCog1D, aytCog1Y)
            "Sözel" -> calculateNet(aytEdbD, aytEdbY) + calculateNet(aytTar1D, aytTar1Y) + calculateNet(aytCog1D, aytCog1Y) + calculateNet(aytTar2D, aytTar2Y) + calculateNet(aytCog2D, aytCog2Y) + calculateNet(aytFelD, aytFelY) + calculateNet(aytDinD, aytDinY)
            else -> 0f
        }
        else -> calculateNet(bransCorrect, bransWrong)
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                types.forEach { type ->
                    val selected = selectedType == type
                    Text(
                        text = type,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                            .clickable { selectedType = type }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (selectedType == "TYT") {
                NetInputRow("Türkçe", 40, tytTurkceD, tytTurkceY, { tytTurkceD = it }, { tytTurkceY = it }, isError(tytTurkceD, tytTurkceY, 40))
                NetInputRow("Sosyal", 20, tytSosyalD, tytSosyalY, { tytSosyalD = it }, { tytSosyalY = it }, isError(tytSosyalD, tytSosyalY, 20))
                NetInputRow("Matematik", 40, tytMatD, tytMatY, { tytMatD = it }, { tytMatY = it }, isError(aytMatD, aytMatY, 40))
                NetInputRow("Fen", 20, tytFenD, tytFenY, { tytFenD = it }, { tytFenY = it }, isError(tytFenD, tytFenY, 20))
            } else if (selectedType == "AYT") {
                Text(text = "Alan: $major", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                when(major) {
                    "Sayısal" -> {
                        NetInputRow("Matematik", 40, aytMatD, aytMatY, { aytMatD = it }, { aytMatY = it }, isError(aytMatD, aytMatY, 40))
                        NetInputRow("Fizik", 14, aytFizD, aytFizY, { aytFizD = it }, { aytFizY = it }, isError(aytFizD, aytFizY, 14))
                        NetInputRow("Kimya", 13, aytKimD, aytKimY, { aytKimD = it }, { aytKimY = it }, isError(aytKimD, aytKimY, 13))
                        NetInputRow("Biyoloji", 13, aytBiyD, aytBiyY, { aytBiyD = it }, { aytBiyY = it }, isError(aytBiyD, aytBiyY, 13))
                    }
                    "Eşit Ağırlık" -> {
                        NetInputRow("Matematik", 40, aytMatD, aytMatY, { aytMatD = it }, { aytMatY = it }, isError(aytMatD, aytMatY, 40))
                        NetInputRow("Edebiyat", 24, aytEdbD, aytEdbY, { aytEdbD = it }, { aytEdbY = it }, isError(aytEdbD, aytEdbY, 24))
                        NetInputRow("Tarih-1", 10, aytTar1D, aytTar1Y, { aytTar1D = it }, { aytTar1Y = it }, isError(aytTar1D, aytTar1Y, 10))
                        NetInputRow("Coğrafya-1", 6, aytCog1D, aytCog1Y, { aytCog1D = it }, { aytCog1Y = it }, isError(aytCog1D, aytCog1Y, 6))
                    }
                    "Sözel" -> {
                        NetInputRow("Edebiyat", 24, aytEdbD, aytEdbY, { aytEdbD = it }, { aytEdbY = it }, isError(aytEdbD, aytEdbY, 24))
                        NetInputRow("Tarih-1", 10, aytTar1D, aytTar1Y, { aytTar1D = it }, { aytTar1Y = it }, isError(aytTar1D, aytTar1Y, 10))
                        NetInputRow("Coğrafya-1", 6, aytCog1D, aytCog1Y, { aytCog1D = it }, { aytCog1Y = it }, isError(aytCog1D, aytCog1Y, 6))
                        NetInputRow("Tarih-2", 11, aytTar2D, aytTar2Y, { aytTar2D = it }, { aytTar2Y = it }, isError(aytTar2D, aytTar2Y, 11))
                        NetInputRow("Coğrafya-2", 11, aytCog2D, aytCog2Y, { aytCog2D = it }, { aytCog2Y = it }, isError(aytCog2D, aytCog2Y, 11))
                        NetInputRow("Felsefe Grubu", 12, aytFelD, aytFelY, { aytFelD = it }, { aytFelY = it }, isError(aytFelD, aytFelY, 12))
                        NetInputRow("Din Kültürü", 6, aytDinD, aytDinY, { aytDinD = it }, { aytDinY = it }, isError(aytDinD, aytDinY, 6))
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)) {
                    Column {
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                            OutlinedButton(onClick = { expandedBranchList = true }) {
                                Text(selectedBranch, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = expandedBranchList,
                                onDismissRequest = { expandedBranchList = false },
                            ) {
                                branches.forEach { branch ->
                                    DropdownMenuItem(
                                        text = { Text(branch.first) },
                                        onClick = {
                                            selectedBranch = branch.first
                                            expandedBranchList = false
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        NetInputRow(
                            label = selectedBranch,
                            max = bransMax,
                            correct = bransCorrect,
                            wrong = bransWrong,
                            onCorrectChange = { bransCorrect = it },
                            onWrongChange = { bransWrong = it },
                            isError = bransError
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Toplam Net: ${String.format(Locale.getDefault(), "%.2f", totalNet)}",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (hasError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("İPTAL")
                }
                
                Button(
                    onClick = { 
                        val details = when(selectedType) {
                            "TYT" -> "T:$tytTurkceD/$tytTurkceY S:$tytSosyalD/$tytSosyalY M:$tytMatD/$tytMatY F:$tytFenD/$tytFenY"
                            "AYT" -> when(major) {
                                "Sayısal" -> "M:$aytMatD/$aytMatY F:$aytFizD/$aytFizY K:$aytKimD/$aytKimY B:$aytBiyD/$aytBiyY"
                                "Eşit Ağırlık" -> "M:$aytMatD/$aytMatY E:$aytEdbD/$aytEdbY T1:$aytTar1D/$aytTar1Y C1:$aytCog1D/$aytCog1Y"
                                "Sözel" -> "E:$aytEdbD/$aytEdbY T1:$aytTar1D/$aytTar1Y C1:$aytCog1D/$aytCog1Y T2:$aytTar2D/$aytTar2Y C2:$aytCog2D/$aytCog2Y Fel:$aytFelD/$aytFelY D:$aytDinD/$aytDinY"
                                else -> ""
                            }
                            else -> "$selectedBranch:${bransCorrect.ifEmpty { "0" }}/${bransWrong.ifEmpty { "0" }}"
                        }
                        val saveType = if (selectedType == "Branş") "BRANS" else selectedType
                        onSave(saveType, totalNet, details) 
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !hasError
                ) {
                    Text("KAYDET")
                }
            }
        }
    }
}

@Composable
fun NetInputRow(
    label: String,
    max: Int,
    correct: String,
    wrong: String,
    onCorrectChange: (String) -> Unit,
    onWrongChange: (String) -> Unit,
    isError: Boolean = false
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label, 
            fontWeight = FontWeight.Bold, 
            fontSize = 16.sp,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("D:", fontSize = 14.sp)
            OutlinedTextField(
                value = correct,
                onValueChange = { if (it.length <= 2) onCorrectChange(it) },
                modifier = Modifier.width(60.dp).padding(start = 4.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = isError
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text("Y:", fontSize = 14.sp)
            OutlinedTextField(
                value = wrong,
                onValueChange = { if (it.length <= 2) onWrongChange(it) },
                modifier = Modifier.width(60.dp).padding(start = 4.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = isError
            )
            Text(
                text = "/ $max", 
                modifier = Modifier.padding(start = 8.dp), 
                color = if (isError) MaterialTheme.colorScheme.error else Color.Gray, 
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun NetStatisticsGraph(results: List<NetResultEntity>, maxNet: Float = 120f, onPointClick: ((NetResultEntity) -> Unit)? = null) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 10.sp, color = Color.Gray)
    val pointCoords = remember(results) { mutableListOf<Pair<Offset, NetResultEntity>>() }
    val hitTolerance = with(LocalDensity.current) { 30.dp.toPx() }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp, vertical = 40.dp)
            .pointerInput(results) {
                detectTapGestures { tapOffset: Offset ->
                    val hit = pointCoords.find { (pointOffset, _) ->
                        (tapOffset - pointOffset).getDistance() < hitTolerance
                    }
                    hit?.let { onPointClick?.invoke(it.second) }
                }
            }
    ) {
        val width = size.width
        val labelSpace = 30.dp.toPx()
        val graphHeight = size.height - labelSpace 
        
        // Y-Ekseni Etiketleri
        val yLabels = if (maxNet <= 20f) listOf(0, (maxNet/4).toInt(), (maxNet/2).toInt(), (maxNet*3/4).toInt(), maxNet.toInt())
        else if (maxNet <= 40f) listOf(0, 10, 20, 30, maxNet.toInt())
        else if (maxNet == 80f) listOf(0, 20, 40, 60, 80)
        else listOf(0, 30, 60, 90, 120)
        
        yLabels.forEach { label ->
            val y = graphHeight - (label / maxNet * graphHeight)
            drawText(
                textMeasurer = textMeasurer,
                text = label.toString(),
                style = labelStyle,
                topLeft = Offset(-35.dp.toPx(), y - 7.dp.toPx())
            )
            drawLine(
                color = Color.LightGray.copy(alpha = 0.5f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        if (results.isEmpty()) return@Canvas

        val xStep = if (results.size > 1) width / (results.size - 1) else width
        val path = Path()
        pointCoords.clear()

        results.forEachIndexed { index, result ->
            val x = index * xStep
            val y = graphHeight - (result.totalNet / maxNet * graphHeight)
            val center = Offset(x, y)
            pointCoords.add(center to result)
            
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
            
            drawCircle(
                color = Color(0xFF4CAF50),
                radius = 6.dp.toPx(),
                center = center
            )
        }

        if (results.size > 1) {
            drawPath(
                path = path,
                color = Color(0xFF4CAF50),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

fun getBranchMaxQuestions(subject: String): Int {
    return when(subject) {
        "Türkçe", "Matematik", "Matematik-1", "Matematik-2", "TYT Türkçe", "TYT Matematik", "AYT Matematik" -> 40
        "Sosyal Bilgiler", "Fen Bilimleri", "TYT Sosyal", "TYT Fen", "Fizik/Fen" -> 20
        "Edebiyat", "AYT Edebiyat" -> 24
        "Fizik", "AYT Fizik" -> 14
        "Kimya", "Biyoloji", "AYT Kimya", "AYT Biyoloji" -> 13
        "Felsefe Grubu", "AYT Felsefe Grubu", "Felsefe" -> 12
        "Tarih-2", "Coğrafya-2", "AYT Tarih-2", "AYT Coğrafya-2" -> 11
        "Tarih-1", "AYT Tarih-1" -> 10
        "Coğrafya-1", "Din Kültürü", "AYT Coğrafya-1", "AYT Din Kültürü", "Din" -> 6
        else -> 40
    }
}

@Composable
fun NetDetailDialog(
    result: NetResultEntity,
    onUpdate: (NetResultEntity) -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    var editMode by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val dateStr = java.text.SimpleDateFormat("dd MMMM yyyy, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(result.date))

    val detailMap = mutableMapOf<String, Pair<String, String>>()
    if (result.type == "BRANS") {
        if (result.details.contains(":")) {
            val parts = result.details.split(":")
            val counts = parts[1].split("/")
            detailMap[parts[0]] = (counts.getOrNull(0) ?: "0") to (counts.getOrNull(1) ?: "0")
        } else {
            detailMap[result.details] = "0" to "0"
        }
    } else {
        result.details.split(" ").forEach {
            val parts = it.split(":")
            if(parts.size == 2) {
                val counts = parts[1].split("/")
                detailMap[parts[0]] = (counts.getOrNull(0) ?: "0") to (counts.getOrNull(1) ?: "0")
            }
        }
    }

    val newValues = remember { mutableStateMapOf<String, Pair<String, String>>() }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Silmeyi Onayla") },
            text = { Text("Bu net kaydını silmek istediğinize emin misiniz?") },
            confirmButton = {
                TextButton(onClick = onDelete) { Text("Sil", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Vazgeç") }
            }
        )
    }

    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState())) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Tarih: $dateStr", fontSize = 14.sp, color = Color.Gray)
                    Text(text = "${result.type} - Toplam Net: ${String.format(java.util.Locale.getDefault(), "%.2f", result.totalNet)}", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 16.dp))
                }
                Surface(
                    onClick = { showDeleteConfirm = true },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete, 
                        contentDescription = "Sil", 
                        tint = MaterialTheme.colorScheme.onError, 
                        modifier = Modifier.padding(10.dp).size(26.dp)
                    )
                }
            }

            if (!editMode) {
                detailMap.forEach { (subKey, counts) ->
                    val d = counts.first.toIntOrNull() ?: 0
                    val y = counts.second.toIntOrNull() ?: 0
                    
                    val subjectName = when(subKey) {
                        "T" -> "Türkçe" "S" -> "Sosyal Bilgiler" "M" -> "Matematik" 
                        "F" -> if (result.type == "AYT") "Fizik" else "Fen Bilimleri"
                        "K" -> "Kimya" "B" -> "Biyoloji" "E" -> "Edebiyat" "T1" -> "Tarih-1"
                        "C1" -> "Coğrafya-1" "T2" -> "Tarih-2" "C2" -> "Coğrafya-2"
                        "Fel" -> "Felsefe Grubu" "D" -> "Din Kültürü"
                        else -> subKey
                    }
                    val max = getBranchMaxQuestions(subjectName)
                    val b = max - d - y

                    Text(text = subjectName, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
                    Text(text = "Doğru: $d  Yanlış: $y  Boş: ${if(b < 0) 0 else b}", fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) { Text("Kapat") }
                    Button(onClick = { editMode = true }, shape = RoundedCornerShape(12.dp)) { Text("Düzenle") }
                }
            } else {
                Text("Değiştirmek istemediğiniz alanları boş bırakabilirsiniz.", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 16.dp))
                
                var hasErrorEdit = false
                var totalNetTemp = 0f

                detailMap.forEach { (subKey, oldCounts) ->
                    val subjectName = when(subKey) {
                        "T" -> "Türkçe" "S" -> "Sosyal Bilgiler" "M" -> "Matematik" 
                        "F" -> if (result.type == "AYT") "Fizik" else "Fen Bilimleri"
                        "K" -> "Kimya" "B" -> "Biyoloji" "E" -> "Edebiyat" "T1" -> "Tarih-1"
                        "C1" -> "Coğrafya-1" "T2" -> "Tarih-2" "C2" -> "Coğrafya-2"
                        "Fel" -> "Felsefe Grubu" "D" -> "Din Kültürü"
                        else -> subKey
                    }
                    
                    val max = getBranchMaxQuestions(subjectName)
                    
                    val currentD = newValues[subKey]?.first ?: ""
                    val currentY = newValues[subKey]?.second ?: ""
                    
                    val activeD = currentD.ifEmpty { oldCounts.first }
                    val activeY = currentY.ifEmpty { oldCounts.second }
                    
                    val activeDInt = activeD.toIntOrNull() ?: 0
                    val activeYInt = activeY.toIntOrNull() ?: 0
                    val isSubjectError = (activeDInt + activeYInt > max)
                    if (isSubjectError) hasErrorEdit = true
                    
                    totalNetTemp += (activeDInt - (activeYInt * 0.25f))

                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text(subjectName, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = currentD,
                                onValueChange = { newValues[subKey] = it to currentY },
                                modifier = Modifier.weight(1f),
                                label = { Text("Doğru", fontSize = 12.sp) },
                                placeholder = { Text(oldCounts.first, color = Color.Gray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = isSubjectError
                            )
                            OutlinedTextField(
                                value = currentY,
                                onValueChange = { newValues[subKey] = currentD to it },
                                modifier = Modifier.weight(1f),
                                label = { Text("Yanlış", fontSize = 12.sp) },
                                placeholder = { Text(oldCounts.second, color = Color.Gray) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = isSubjectError
                            )
                        }
                    }
                }
                
                Text(
                    text = "Yeni Toplam Net: ${String.format(java.util.Locale.getDefault(), "%.2f", totalNetTemp)}",
                    fontWeight = FontWeight.Bold,
                    color = if(hasErrorEdit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = { editMode = false }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) { Text("İptal") }
                    Button(
                        onClick = {
                            var newDetailsStr = ""
                            if (result.type == "BRANS") {
                                val branchName = detailMap.keys.first()
                                val oldCounts = detailMap[branchName]!!
                                val d = (newValues[branchName]?.first ?: "").ifEmpty { oldCounts.first }
                                val y = (newValues[branchName]?.second ?: "").ifEmpty { oldCounts.second }
                                newDetailsStr = "$branchName:$d/$y"
                            } else {
                                val portions = detailMap.map { (k, oldCounts) ->
                                    val d = (newValues[k]?.first ?: "").ifEmpty { oldCounts.first }
                                    val y = (newValues[k]?.second ?: "").ifEmpty { oldCounts.second }
                                    "$k:$d/$y"
                                }
                                newDetailsStr = portions.joinToString(" ")
                            }
                            onUpdate(result.copy(totalNet = totalNetTemp, details = newDetailsStr))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !hasErrorEdit
                    ) {
                        Text("Kaydet")
                    }
                }
            }
        }
    }
}

@Composable
fun ExamTimerScreen(onBack: () -> Unit) {
    var isTyt by remember { mutableStateOf(true) }
    var isAnalog by remember { mutableStateOf(false) }
    
    val totalTime = if (isTyt) 165 * 60L else 180 * 60L
    var timeRemaining by remember { mutableLongStateOf(totalTime) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isTyt) {
        timeRemaining = totalTime
        isRunning = false
    }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (timeRemaining > 0) {
                delay(1000L)
                timeRemaining--
            }
            isRunning = false
        }
    }

    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_PAUSE || event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                isRunning = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    DisposableEffect(isRunning) {
        val window = (context as? android.app.Activity)?.window
        if (isRunning) {
            window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
        onDispose {
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(64.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                }
                Text("Deneme Zamanlayıcı", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
            }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (isAnalog) {
                AnalogTimer(timeRemaining, totalTime)
            } else {
                DigitalTimer(timeRemaining)
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalArrangement = Arrangement.Center) {
            Button(
                onClick = { if (!isRunning) isRunning = true }, 
                shape = RoundedCornerShape(24.dp), 
                modifier = Modifier.size(80.dp),
                enabled = !isRunning,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                )
            ) {
                Icon(
                    Icons.Default.PlayArrow, 
                    contentDescription = "Başla", 
                    modifier = Modifier.size(44.dp), 
                    tint = if (!isRunning) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            OutlinedButton(onClick = { timeRemaining = totalTime; isRunning = false }, shape = RoundedCornerShape(24.dp), modifier = Modifier.size(80.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = "Sıfırla", modifier = Modifier.size(40.dp))
            }
        }

        Surface(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (isTyt) "TYT (165 dk)" else "AYT (180 dk)", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                    Switch(checked = !isTyt, onCheckedChange = { isTyt = !it })
                }
                
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(if (isAnalog) "Analog Saat" else "Dijital Saat", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 8.dp))
                    Switch(checked = isAnalog, onCheckedChange = { isAnalog = it })
                }
            }
        }
    }
}

@Composable
fun DigitalTimer(timeRemainingSeconds: Long) {
    val hrs = timeRemainingSeconds / 3600
    val mins = (timeRemainingSeconds % 3600) / 60
    val secs = timeRemainingSeconds % 60
    val timeString = if (hrs > 0) {
        String.format(java.util.Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format(java.util.Locale.getDefault(), "%02d:%02d", mins, secs)
    }
    
    Text(
        text = timeString,
        fontSize = 64.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = FontFamily.Monospace,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(16.dp),
        maxLines = 1
    )
}

@Composable
fun AnalogTimer(timeRemainingSeconds: Long, totalTimeSeconds: Long) {
    val hrs = timeRemainingSeconds / 3600
    val mins = (timeRemainingSeconds % 3600) / 60
    val secs = timeRemainingSeconds % 60
    val timeString = if (hrs > 0) {
        String.format(java.util.Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs)
    } else {
        String.format(java.util.Locale.getDefault(), "%02d:%02d", mins, secs)
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val errorColor = MaterialTheme.colorScheme.error

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(320.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val radius = size.minDimension / 2
            val center = Offset(size.width / 2, size.height / 2)
            
            drawCircle(
                color = primaryColor.copy(alpha = 0.1f),
                radius = radius,
                center = center
            )
            drawCircle(
                color = primaryColor,
                radius = radius,
                center = center,
                style = Stroke(width = 4.dp.toPx())
            )
            
            for (i in 0 until 60) {
                val angle = i * 6 * (Math.PI / 180)
                val startRadius = if (i % 5 == 0) radius * 0.85f else radius * 0.92f
                val strokeW = if (i % 5 == 0) 4.dp.toPx() else 2.dp.toPx()
                
                val startX = center.x + startRadius * kotlin.math.sin(angle).toFloat()
                val startY = center.y - startRadius * kotlin.math.cos(angle).toFloat()
                
                val endX = center.x + radius * 0.98f * kotlin.math.sin(angle).toFloat()
                val endY = center.y - radius * 0.98f * kotlin.math.cos(angle).toFloat()
                
                drawLine(
                    color = if (i % 5 == 0) onSurfaceColor else onSurfaceColor.copy(alpha = 0.5f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = strokeW,
                    cap = StrokeCap.Round
                )
            }
            
            val elapsedSeconds = totalTimeSeconds - timeRemainingSeconds
            val currentSimSeconds = (10 * 3600L + 15 * 60L) + elapsedSeconds
            
            val simHrs = currentSimSeconds / 3600f
            val simMins = (currentSimSeconds % 3600L) / 60f
            val simSecs = (currentSimSeconds % 60L).toFloat()
            
            val hrFloat = simHrs + (simMins / 60f)
            val secAngle = simSecs * 6f 
            val minAngle = simMins * 6f + (simSecs / 60f) * 6f
            val hrAngle = (hrFloat % 12f) * 30f
            
            fun drawHand(angleDeg: Float, handLength: Float, handThickness: Float, handColor: Color) {
                val angleRad = angleDeg * (Math.PI / 180)
                val endX = center.x + handLength * kotlin.math.sin(angleRad).toFloat()
                val endY = center.y - handLength * kotlin.math.cos(angleRad).toFloat()
                
                drawLine(
                    color = handColor,
                    start = center,
                    end = Offset(endX, endY),
                    strokeWidth = handThickness,
                    cap = StrokeCap.Round
                )
            }
            
            drawHand(hrAngle, radius * 0.5f, 8.dp.toPx(), onSurfaceColor)
            drawHand(minAngle, radius * 0.75f, 6.dp.toPx(), primaryColor)
            drawHand(secAngle, radius * 0.9f, 2.dp.toPx(), errorColor)
            
            drawCircle(
                color = errorColor,
                radius = 6.dp.toPx(),
                center = center
            )
        }
        
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 50.dp)
        ) {
            Text(
                text = timeString,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun WrongQuestionsScreen(viewModel: YksViewModel, userInfo: UserInfo?, onBack: () -> Unit) {
    var capturedImagePath by remember { mutableStateOf<String?>(null) }
    var selectedSubjectForGallery by remember { mutableStateOf<String?>(null) }
    var selectedSubjectForSave by remember { mutableStateOf<String?>(null) }
    var selectedExamType by remember { mutableStateOf("TYT") }
    val allTopics by viewModel.allTopics.collectAsState()
    val context = LocalContext.current

    // AYT ders adı → topic category eşlemesi
    val aytSubjectToCat = mapOf(
        "Matematik" to "AYT_MAT", "Geometri" to "AYT_GEO",
        "Fizik" to "AYT_FIZ", "Kimya" to "AYT_KIM", "Biyoloji" to "AYT_BIY",
        "Edebiyat" to "AYT_EDB", "Tarih-1" to "AYT_TAR1", "Coğrafya-1" to "AYT_COG1",
        "Tarih-2" to "AYT_TAR2", "Coğrafya-2" to "AYT_COG2",
        "Felsefe Grubu" to "AYT_FEL", "Din Kültürü" to "AYT_DIN"
    )

    fun topicsFor(subject: String, examType: String): List<String> {
        return if (examType == "TYT") {
            allTopics.filter { it.category == "TYT" && it.subjectName == subject }
                .distinctBy { it.title }.map { it.title }
        } else {
            val cat = aytSubjectToCat[subject]
            if (cat != null)
                allTopics.filter { it.category == cat }.distinctBy { it.title }.map { it.title }
            else
                allTopics.filter { it.category.startsWith("AYT_") && it.subjectName == subject }
                    .distinctBy { it.title }.map { it.title }
        }
    }

    var pendingImageFile by remember { mutableStateOf<java.io.File?>(null) }

    val coroutineScope = rememberCoroutineScope()
    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && pendingImageFile?.exists() == true) {
            coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val originalFile = pendingImageFile!!
                    val bitmap = android.graphics.BitmapFactory.decodeFile(originalFile.absolutePath)
                    if (bitmap != null) {
                        // EXIF rotasyonunu düzelt
                        val correctedBitmap = try {
                            val exif = android.media.ExifInterface(originalFile.absolutePath)
                            val orientation = exif.getAttributeInt(
                                android.media.ExifInterface.TAG_ORIENTATION,
                                android.media.ExifInterface.ORIENTATION_NORMAL
                            )
                            val degrees = when (orientation) {
                                android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                                android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                                android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                                else -> 0f
                            }
                            if (degrees != 0f) {
                                val matrix = android.graphics.Matrix()
                                matrix.postRotate(degrees)
                                android.graphics.Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                                    .also { if (it != bitmap) bitmap.recycle() }
                            } else bitmap
                        } catch (_: Exception) { bitmap }

                        val webpFile = java.io.File(context.cacheDir, "camera_photos/webp_${System.currentTimeMillis()}.webp")
                        java.io.FileOutputStream(webpFile).use { out ->
                            val format = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                                android.graphics.Bitmap.CompressFormat.WEBP_LOSSY
                            } else {
                                @Suppress("DEPRECATION")
                                android.graphics.Bitmap.CompressFormat.WEBP
                            }
                            correctedBitmap.compress(format, 80, out)
                        }
                        correctedBitmap.recycle()
                        originalFile.delete()
                        
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            capturedImagePath = webpFile.absolutePath
                        }
                    } else {
                        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                            capturedImagePath = originalFile.absolutePath
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("WrongQuestions", "Error compressing image to WEBP", e)
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        capturedImagePath = pendingImageFile!!.absolutePath
                    }
                }
            }
        }
    }

    fun launchCamera() {
        try {
            val cameraDir = java.io.File(context.cacheDir, "camera_photos")
            cameraDir.mkdirs()
            val file = java.io.File(cameraDir, "temp_${System.currentTimeMillis()}.jpg")
            pendingImageFile = file
            val uri = androidx.core.content.FileProvider.getUriForFile(
                context, "${context.packageName}.fileprovider", file
            )
            cameraLauncher.launch(uri)
        } catch (e: Exception) {
            android.util.Log.e("WrongQuestions", "Camera launch error: ${e.javaClass.simpleName}: ${e.message}", e)
            Toast.makeText(context, "Hata: ${e.javaClass.simpleName}: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    when {
        capturedImagePath != null && selectedSubjectForSave != null -> {
            SaveWrongQuestionScreen(
                imagePath = capturedImagePath!!,
                subjectName = selectedSubjectForSave!!,
                examType = selectedExamType,
                subjectTopics = topicsFor(selectedSubjectForSave!!, selectedExamType),
                onSave = { subject, examType, topic, path ->
                    viewModel.saveWrongQuestion(subject, examType, topic, path)
                },
                onRetake = { capturedImagePath = null; launchCamera() },
                onCancel = { capturedImagePath = null; selectedSubjectForSave = null }
            )
            return
        }
        selectedSubjectForGallery != null -> {
            SubjectGalleryScreen(
                viewModel = viewModel,
                subjectName = selectedSubjectForGallery!!,
                examType = selectedExamType,
                topicTitles = topicsFor(selectedSubjectForGallery!!, selectedExamType),
                onAddPhoto = { selectedSubjectForSave = selectedSubjectForGallery; launchCamera() },
                onBack = { selectedSubjectForGallery = null }
            )
            return
        }
    }

    var selectedTab by remember { mutableStateOf(0) }
    val tytSubjects = listOf("Türkçe", "Matematik", "Geometri", "Tarih", "Coğrafya", "Felsefe Grubu", "Din Kültürü", "Fizik", "Kimya", "Biyoloji")
    val major = userInfo?.major ?: "Eşit Ağırlık"
    val aytSubjects = when (major) {
        "Sayısal" -> listOf("Matematik", "Geometri", "Fizik", "Kimya", "Biyoloji")
        "Eşit Ağırlık" -> listOf("Matematik", "Geometri", "Edebiyat", "Tarih-1", "Coğrafya-1")
        "Sözel" -> listOf("Edebiyat", "Tarih-1", "Coğrafya-1", "Tarih-2", "Coğrafya-2", "Felsefe Grubu", "Din Kültürü")
        else -> listOf("Matematik", "Geometri", "Fizik", "Kimya", "Biyoloji", "Edebiyat", "Tarih-1", "Coğrafya-1", "Tarih-2", "Coğrafya-2", "Felsefe Grubu", "Din Kültürü")
    }
    val currentSubjects = if (selectedTab == 0) tytSubjects else aytSubjects
    val currentExamType = if (selectedTab == 0) "TYT" else "AYT"

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().height(64.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                    Text("Yanlış Soru Deposu", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                }
            }
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0; selectedExamType = "TYT" },
                    icon = { Icon(Icons.Default.List, contentDescription = "TYT") },
                    label = { Text("TYT") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1; selectedExamType = "AYT" },
                    icon = { Icon(Icons.Default.Menu, contentDescription = "AYT") },
                    label = { Text("AYT") }
                )
            }
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(innerPadding).padding(16.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(currentSubjects) { subject ->
                val questionCount by viewModel.getWrongQuestionsBySubjectAndType(subject, currentExamType)
                    .collectAsState(initial = emptyList())
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { selectedExamType = currentExamType; selectedSubjectForGallery = subject },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(8.dp)) {
                        Text(subject, fontWeight = FontWeight.Bold, fontSize = 16.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${questionCount.size} Soru", color = MaterialTheme.colorScheme.primary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveWrongQuestionScreen(
    imagePath: String,
    subjectName: String,
    examType: String,
    subjectTopics: List<String>,
    onSave: (subject: String, examType: String, topic: String, imagePath: String) -> Unit,
    onRetake: () -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var selectedTopic by remember { mutableStateOf("") }
    var topicExpanded by remember { mutableStateOf(false) }

    // Fotoğrafı EXIF rotasyonuyla birlikte yükle (önizleme için)
    val bmp = remember(imagePath) {
        loadExifCorrectedBitmap(imagePath)
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().height(64.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                    Text("Soru Kaydet – $subjectName", fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp).fillMaxSize()) {
            if (bmp != null) {
                androidx.compose.foundation.Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Çekilen Soru",
                    modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(12.dp)),
                    contentScale = androidx.compose.ui.layout.ContentScale.Fit
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f).clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) { Text("Fotoğraf yüklenemedi") }
            }

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = topicExpanded,
                onExpandedChange = { topicExpanded = !topicExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedTopic,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Konu Seç") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = topicExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = topicExpanded,
                    onDismissRequest = { topicExpanded = false }
                ) {
                    subjectTopics.forEach { topic ->
                        DropdownMenuItem(
                            text = { Text(topic) },
                            onClick = { selectedTopic = topic; topicExpanded = false }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = onRetake, modifier = Modifier.weight(1f)) {
                    Text("Yeniden Dene")
                }
                Button(
                    enabled = selectedTopic.isNotEmpty(),
                    onClick = {
                        // Geçici dosyayı kalıcı isimle taşı — sıkıştırma yok, tam kalite korunuyor
                        try {
                            val srcFile = java.io.File(imagePath)
                            val saveDir = java.io.File(context.filesDir, "wrong_questions")
                            saveDir.mkdirs()
                            val destFile = java.io.File(saveDir, "wrong_${System.currentTimeMillis()}.jpg")

                            // EXIF rotasyonunu oku
                            val exif = android.media.ExifInterface(srcFile.absolutePath)
                            val orientation = exif.getAttributeInt(
                                android.media.ExifInterface.TAG_ORIENTATION,
                                android.media.ExifInterface.ORIENTATION_NORMAL
                            )
                            val degrees = when (orientation) {
                                android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                                android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                                android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                                else -> 0f
                            }

                            if (degrees == 0f) {
                                // Rotasyon yok — doğrudan kopyala (kayıpsız)
                                srcFile.copyTo(destFile, overwrite = true)
                            } else {
                                // Rotasyon var — döndür ve kaydet
                                val bitmap = android.graphics.BitmapFactory.decodeFile(srcFile.absolutePath)
                                val matrix = android.graphics.Matrix()
                                matrix.postRotate(degrees)
                                val rotated = android.graphics.Bitmap.createBitmap(
                                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                                )
                                destFile.outputStream().use { out ->
                                    rotated.compress(android.graphics.Bitmap.CompressFormat.JPEG, 100, out)
                                }
                                bitmap.recycle()
                                rotated.recycle()
                            }

                            srcFile.delete()
                            onSave(subjectName, examType, selectedTopic, destFile.absolutePath)
                            Toast.makeText(context, "Soru kaydedildi!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Kayıt hatası: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                        onCancel()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Kaydet")
                }
            }
        }
    }
}

@Composable
fun SubjectGalleryScreen(
    viewModel: YksViewModel,
    subjectName: String,
    examType: String,
    topicTitles: List<String>,
    onAddPhoto: () -> Unit,
    onBack: () -> Unit
) {
    val allQuestions by viewModel.getWrongQuestionsBySubjectAndType(subjectName, examType).collectAsState(initial = emptyList())
    val questionsByTopic = allQuestions.groupBy { it.topicTitle }
    var selectedQuestion by remember { mutableStateOf<com.omerfaruk.ykstakip.data.local.WrongQuestionEntity?>(null) }

    // Tam ekran fotoğraf görüntüleyici
    if (selectedQuestion != null) {
        val q = selectedQuestion!!
        val bmp = remember(q.imagePath) {
            loadExifCorrectedBitmap(q.imagePath)
        }
        
        var scale by remember { mutableFloatStateOf(1f) }
        var offsetX by remember { mutableFloatStateOf(0f) }
        var offsetY by remember { mutableFloatStateOf(0f) }
        
        // Geri tuşunu yakala
        androidx.activity.compose.BackHandler { selectedQuestion = null }
        
        Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
            // Fotoğraf alanı (ortada, zoom + pan destekli)
            val transformState = rememberTransformableState { zoomChange, panChange, _ ->
                scale = (scale * zoomChange).coerceIn(1f, 5f)
                if (scale > 1f) {
                    offsetX += panChange.x
                    offsetY += panChange.y
                } else {
                    offsetX = 0f
                    offsetY = 0f
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 56.dp, bottom = 72.dp)
                    .transformable(state = transformState),
                contentAlignment = Alignment.Center
            ) {
                if (bmp != null) {
                    androidx.compose.foundation.Image(
                        bitmap = bmp.asImageBitmap(),
                        contentDescription = "Soru",
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                translationX = offsetX
                                translationY = offsetY
                            },
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit
                    )
                }
            }

            // Üst bar (geri butonu + konu adı)
            Surface(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                color = Color.Black.copy(alpha = 0.85f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.statusBarsPadding().padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    IconButton(onClick = { selectedQuestion = null }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color.White)
                    }
                    Text(q.topicTitle, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                }
            }

            // Alt bar (Çözüldü + Sil)
            Surface(
                modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter),
                color = Color.Black.copy(alpha = 0.85f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Sol: Çözüldü tikle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.1f))
                            .clickable { viewModel.toggleWrongQuestionSolved(q) }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(
                                    if (q.isSolved) MaterialTheme.colorScheme.primary
                                    else Color.White.copy(alpha = 0.2f)
                                )
                                .border(
                                    width = 2.dp,
                                    color = if (q.isSolved) MaterialTheme.colorScheme.primary else Color.White,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (q.isSolved) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            if (q.isSolved) "Çözüldü" else "Çözüldü mü?",
                            color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium
                        )
                    }
                    // Sağ: Sil butonu
                    IconButton(
                        onClick = {
                            viewModel.deleteWrongQuestion(q)
                            selectedQuestion = null
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().height(64.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                    Text(subjectName, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPhoto,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Soru Ekle", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    ) { innerPadding ->
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            val displayTopics = if (topicTitles.isEmpty()) questionsByTopic.keys.toList() else topicTitles
            displayTopics.forEach { topicTitle ->
                val photos = questionsByTopic[topicTitle] ?: emptyList()
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                ) {
                    Text(
                        text = "$topicTitle  (${photos.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    HorizontalDivider(modifier = Modifier.weight(2f), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                }
                if (photos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Henüz soru eklenmedi", color = Color.Gray, fontSize = 13.sp)
                    }
                } else {
                    val rows = (photos.size + 1) / 2
                    for (rowIdx in 0 until rows) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (colIdx in 0..1) {
                                val photoIdx = rowIdx * 2 + colIdx
                                if (photoIdx < photos.size) {
                                    val q = photos[photoIdx]
                                    val bmp = remember(q.imagePath) {
                                        loadExifCorrectedBitmap(q.imagePath)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable { selectedQuestion = q },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (bmp != null) {
                                            androidx.compose.foundation.Image(
                                                bitmap = bmp.asImageBitmap(),
                                                contentDescription = "Soru",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                            )
                                        } else {
                                            Icon(Icons.Default.Info, contentDescription = null, tint = Color.Gray)
                                        }
                                        // Çözüldü rozeti
                                        if (q.isSolved) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(4.dp)
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * Verilen dosya yolundaki JPEG'i EXIF rotasyon bilgisini okuyarak
 * doğru yönde döndürülmüş Bitmap olarak döndürür.
 */
fun loadExifCorrectedBitmap(path: String): android.graphics.Bitmap? {
    val file = java.io.File(path)
    if (!file.exists()) return null
    val bitmap = android.graphics.BitmapFactory.decodeFile(path) ?: return null
    return try {
        val exif = android.media.ExifInterface(path)
        val orientation = exif.getAttributeInt(
            android.media.ExifInterface.TAG_ORIENTATION,
            android.media.ExifInterface.ORIENTATION_NORMAL
        )
        val degrees = when (orientation) {
            android.media.ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            android.media.ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            android.media.ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (degrees == 0f) {
            bitmap
        } else {
            val matrix = android.graphics.Matrix()
            matrix.postRotate(degrees)
            android.graphics.Bitmap.createBitmap(
                bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
            ).also { bitmap.recycle() }
        }
    } catch (e: Exception) {
        bitmap // EXIF okunamazsa orijinal bitmap'i döndür
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaveTimeScreen(
    viewModel: YksViewModel,
    totalSeconds: Long,
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val allTopics by viewModel.allTopics.collectAsState()
    val context = LocalContext.current
    
    data class Entry(val id: String, var subject: String, var topic: String, var seconds: Long)
    val entries = remember { mutableStateListOf(Entry(java.util.UUID.randomUUID().toString(), "", "", totalSeconds)) }
    
    val unallocatedTime = totalSeconds - entries.sumOf { it.seconds }
    val colors = listOf(Color(0xFF26A69A), Color(0xFF5C6BC0), Color(0xFFFFCC80), Color(0xFFAB47BC), Color(0xFFEC407A))
    
    Scaffold(
        topBar = {
            Surface(modifier = Modifier.fillMaxWidth().height(64.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 4.dp) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp)) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri") }
                    Text("Çalışma Süresi Kaydet", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp))
                }
            }
        },
        bottomBar = {
            Button(
                onClick = {
                    val validEntries = entries.filter { it.subject.isNotEmpty() && it.topic.isNotEmpty() && it.seconds > 0 }
                    if (validEntries.isEmpty()) {
                        Toast.makeText(context, "Geçerli bir konu ve süre giriniz", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    validEntries.forEach { entry ->
                        viewModel.saveStudyTime(entry.subject, entry.topic, entry.seconds)
                    }
                    Toast.makeText(context, "Süreler kaydedildi!", Toast.LENGTH_SHORT).show()
                    onSave()
                },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("KAYDET")
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp).verticalScroll(rememberScrollState())) {
            
            Text("Toplam Süre", fontSize = 24.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            val h = totalSeconds / 3600
            val m = (totalSeconds % 3600) / 60
            val s = totalSeconds % 60
            Text(String.format(Locale.getDefault(), "%d:%02d:%02d", h, m, s), fontSize = 48.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth().height(24.dp).clip(RoundedCornerShape(12.dp)).background(Color.LightGray)) {
                entries.forEachIndexed { index, entry ->
                    val weight = if (totalSeconds > 0) entry.seconds.toFloat() / totalSeconds.toFloat() else 0f
                    if (weight > 0f) {
                        Box(modifier = Modifier.weight(weight).fillMaxHeight().background(colors[index % colors.size]))
                    }
                }
                if (unallocatedTime > 0 && totalSeconds > 0) {
                    val weight = unallocatedTime.toFloat() / totalSeconds.toFloat()
                    Box(modifier = Modifier.weight(weight).fillMaxHeight().background(Color.LightGray))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Konular", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    entries.forEachIndexed { index, entry ->
                        val entryColor = colors[index % colors.size].copy(alpha = 0.2f)
                        var showSubjectDialog by remember { mutableStateOf(false) }
                        var showTopicDialog by remember { mutableStateOf(false) }
                        var showTimeDialog by remember { mutableStateOf(false) }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(entryColor).padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${index + 1}.", fontWeight = FontWeight.Bold, modifier = Modifier.padding(end = 8.dp))
                            
                            Column(modifier = Modifier.weight(1f).clickable { showSubjectDialog = true }) {
                                Text(if (entry.subject.isEmpty()) "Ders seçiniz" else entry.subject, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(if (entry.topic.isEmpty()) "Konu seçiniz" else entry.topic, fontSize = 12.sp, color = Color.DarkGray)
                            }
                            
                            Spacer(modifier = Modifier.width(8.dp))
                            
                            OutlinedButton(
                                onClick = { showTimeDialog = true },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                val eh = entry.seconds / 3600
                                val em = (entry.seconds % 3600) / 60
                                val es = entry.seconds % 60
                                Text(String.format(Locale.getDefault(), "%d:%02d:%02d", eh, em, es), fontSize = 14.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (showSubjectDialog) {
                            val subjects = allTopics.map { it.subjectName }.distinct()
                            AlertDialog(
                                onDismissRequest = { showSubjectDialog = false },
                                title = { Text("Ders Seç") },
                                text = {
                                    LazyColumn {
                                        items(subjects) { sub ->
                                            Text(
                                                text = sub,
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    entry.subject = sub
                                                    entry.topic = ""
                                                    val temp = entries.toList()
                                                    entries.clear(); entries.addAll(temp)
                                                    showSubjectDialog = false
                                                    showTopicDialog = true
                                                }.padding(12.dp)
                                            )
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showSubjectDialog = false }) { Text("Kapat") } }
                            )
                        }
                        
                        if (showTopicDialog) {
                            val topics = allTopics.filter { it.subjectName == entry.subject }.map { it.title }.distinct()
                            AlertDialog(
                                onDismissRequest = { showTopicDialog = false },
                                title = { Text("Konu Seç") },
                                text = {
                                    LazyColumn {
                                        items(topics) { tp ->
                                            Text(
                                                text = tp,
                                                modifier = Modifier.fillMaxWidth().clickable {
                                                    entry.topic = tp
                                                    val temp = entries.toList()
                                                    entries.clear(); entries.addAll(temp)
                                                    showTopicDialog = false
                                                }.padding(12.dp)
                                            )
                                        }
                                    }
                                },
                                confirmButton = { TextButton(onClick = { showTopicDialog = false }) { Text("Kapat") } }
                            )
                        }
                        
                        if (showTimeDialog) {
                            val initialH = (entry.seconds / 3600).toString()
                            val initialM = ((entry.seconds % 3600) / 60).toString()
                            val initialS = (entry.seconds % 60).toString()
                            var hInput by remember { mutableStateOf(initialH) }
                            var mInput by remember { mutableStateOf(initialM) }
                            var sInput by remember { mutableStateOf(initialS) }
                            
                            AlertDialog(
                                onDismissRequest = { showTimeDialog = false },
                                title = { Text("Süre Ayarla") },
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = hInput,
                                            onValueChange = { hInput = it },
                                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                            label = { Text("Saat") },
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedTextField(
                                            value = mInput,
                                            onValueChange = { mInput = it },
                                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                            label = { Text("Dk") },
                                            modifier = Modifier.weight(1f)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        OutlinedTextField(
                                            value = sInput,
                                            onValueChange = { sInput = it },
                                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                                            label = { Text("Sn") },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = {
                                        val h = hInput.toLongOrNull() ?: 0L
                                        val m = mInput.toLongOrNull() ?: 0L
                                        val s = sInput.toLongOrNull() ?: 0L
                                        entry.seconds = h * 3600 + m * 60 + s
                                        val temp = entries.toList()
                                        entries.clear(); entries.addAll(temp)
                                        showTimeDialog = false
                                    }) { Text("Tamam") }
                                },
                                dismissButton = { TextButton(onClick = { showTimeDialog = false }) { Text("İptal") } }
                            )
                        }
                    }
                    TextButton(
                        onClick = {
                            val remaining = if (unallocatedTime > 0) unallocatedTime else 0L
                            entries.add(Entry(java.util.UUID.randomUUID().toString(), "", "", remaining)) 
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Konu ekle")
                    }
                }
            }
        }
    }
}

@Composable
fun SnippetTypeBadge(type: String, modifier: Modifier = Modifier) {
    val (label, containerColor, textColor) = when (type.lowercase(java.util.Locale.ROOT)) {
        "sinav_taktigi", "taktik" -> Triple(
            "SINAV TAKTİĞİ",
            Color(0xFFF59E0B).copy(alpha = 0.15f),
            Color(0xFFD97706)
        )
        "tavsiye", "rehberlik", "sinav_tavsiyesi", "sınav_tavsiyesi" -> Triple(
            "TAVSİYE",
            Color(0xFF0EA5E9).copy(alpha = 0.15f),
            Color(0xFF0369A1)
        )
        else -> Triple(
            "HAP BİLGİ",
            Color(0xFF10B981).copy(alpha = 0.15f),
            Color(0xFF059669)
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
 fun SnippetCard(
    snippet: com.omerfaruk.ykstakip.data.SupabaseSnippet,
    topic: com.omerfaruk.ykstakip.data.SupabaseTopic?,
    isStarred: Boolean,
    onStarClick: () -> Unit,
    onSubjectClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    contentVerticalPadding: androidx.compose.ui.unit.Dp = 55.dp,
    titleTopPadding: androidx.compose.ui.unit.Dp = 100.dp
) {
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // 1. Top Section: Topic Title (Horizontally centered to the screen)
        val topicTitle = topic?.title ?: "Genel Bilgi"
        Text(
            text = topicTitle,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = titleTopPadding, start = 16.dp, end = 16.dp)
        )

        // 2. Center Section: Snippet Content Text (fills available area with padding constraints)
        val topLimit = titleTopPadding + 50.dp
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, end = 56.dp, top = topLimit, bottom = contentVerticalPadding),
            contentAlignment = Alignment.Center
        ) {
            val contentLength = snippet.content.length
            val calculatedFontSize = when {
                contentLength < 80 -> 30.sp
                contentLength < 150 -> 24.sp
                contentLength < 220 -> 19.sp
                contentLength < 300 -> 16.sp
                else -> 13.sp
            }
            val calculatedLineHeight = when {
                contentLength < 80 -> 42.sp
                contentLength < 150 -> 34.sp
                contentLength < 220 -> 28.sp
                contentLength < 300 -> 22.sp
                else -> 18.sp
            }

            Text(
                text = parseMathText(snippet.content),
                fontSize = calculatedFontSize,
                fontWeight = FontWeight.Bold,
                lineHeight = calculatedLineHeight,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // 3. Bottom Section: Subject Label, SnippetTypeBadge & Star Icon
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(start = 24.dp, end = 56.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (topic != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.clickable { onSubjectClick(topic.subjectName) }
                ) {
                    Text(
                        text = "${topic.subjectName} (${topic.category})".uppercase(java.util.Locale.ROOT),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            SnippetTypeBadge(type = snippet.type)

            IconButton(
                onClick = onStarClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = if (isStarred) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = "Yıldızla",
                    tint = if (isStarred) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HapScreen(
    viewModel: YksViewModel,
    onSubjectClick: (String) -> Unit,
    onNavigateToSaved: () -> Unit,
    onNavigateToLiked: () -> Unit,
    onNavigateToKnown: () -> Unit
) {
    val topics by viewModel.hapTopics.collectAsState()
    val snippets by viewModel.hapSnippets.collectAsState()
    val isLoading by viewModel.isLoadingHap.collectAsState()
    val interactions by viewModel.allSnippetInteractions.collectAsState()
    val followedSubjects by viewModel.allFollowedSubjects.collectAsState()

    var showReportDialogForSnippetId by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf("Genel") }

    LaunchedEffect(selectedTab) {
        viewModel.fetchAllRandomSnippets(onlyStarred = (selectedTab == "Yıldızlılar"))
    }

    val defaultBgColor = MaterialTheme.colorScheme.background
    Box(modifier = Modifier.fillMaxSize().background(defaultBgColor)) {
        if (isLoading && snippets.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                androidx.compose.material3.CircularProgressIndicator()
            }
        } else if (snippets.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = if (selectedTab == "Yıldızlılar") "Henüz yıldızlı dersiniz yok veya bu derslere ait hap bilgi bulunamadı." else "Şu an için hap bilgi bulunmuyor.",
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            val pagerState = androidx.compose.foundation.pager.rememberPagerState(pageCount = { snippets.size })
            
            LaunchedEffect(selectedTab) {
                try {
                    pagerState.scrollToPage(0)
                } catch (e: Exception) {}
            }

            androidx.compose.foundation.pager.VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val snippet = snippets[page]
                val topic = topics.find { it.id == snippet.topicId }

                val isDark = androidx.compose.foundation.isSystemInDarkTheme()
                val slideBgColor = when (snippet.type.lowercase(java.util.Locale.ROOT)) {
                    "sinav_taktigi", "taktik" -> {
                        if (isDark) Color(0xFF2E2614) else Color(0xFFFEF9E7)
                    }
                    "tavsiye", "rehberlik", "sinav_tavsiyesi", "sınav_tavsiyesi" -> {
                        if (isDark) Color(0xFF142436) else Color(0xFFF0F7FF)
                    }
                    else -> {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(slideBgColor)
                        .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 112.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val isStarred = followedSubjects.any { it.subjectName == topic?.subjectName && it.isFollowed }
                    SnippetCard(
                        snippet = snippet,
                        topic = topic,
                        isStarred = isStarred,
                        onStarClick = { topic?.let { viewModel.toggleSubjectFollow(it.subjectName) } },
                        onSubjectClick = onSubjectClick,
                        contentVerticalPadding = 55.dp,
                        titleTopPadding = 150.dp,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Dikey Buton Kolonu (Beğen, Biliyorum, Kaydet, Bildir)
                    val interaction = interactions.find { it.snippetId == snippet.id }
                    val isLiked = interaction?.isLiked == true
                    val isKnown = interaction?.isKnown == true
                    val isSaved = interaction?.isSaved == true

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .width(56.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        color = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                        shape = CircleShape
                                    )
                                    .combinedClickable(
                                        onClick = { viewModel.toggleSnippetLike(snippet.id, snippet.topicId, topic?.subjectName ?: "Genel") },
                                        onLongClick = { onNavigateToLiked() }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Beğen",
                                    tint = if (isLiked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${snippet.likesCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        color = if (isKnown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                        shape = CircleShape
                                    )
                                    .combinedClickable(
                                        onClick = { viewModel.toggleSnippetKnown(snippet.id, snippet.topicId, topic?.subjectName ?: "Genel") },
                                        onLongClick = { onNavigateToKnown() }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Biliyorum",
                                    tint = if (isKnown) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${snippet.knowsCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        color = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                        shape = CircleShape
                                    )
                                    .combinedClickable(
                                        onClick = { viewModel.toggleSnippetSaved(snippet.id, snippet.topicId, topic?.subjectName ?: "Genel") },
                                        onLongClick = { onNavigateToSaved() }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Kaydet",
                                    tint = if (isSaved) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${snippet.savesCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { showReportDialogForSnippetId = snippet.id },
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Report,
                                contentDescription = "Bildir",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        // Floating Pill Selector at the Top!
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 12.dp, bottom = 8.dp)
                .align(Alignment.TopCenter),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f), RoundedCornerShape(24.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val options = listOf("Genel", "Yıldızlılar")
                options.forEach { option ->
                    val isSelected = selectedTab == option
                    val containerColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(containerColor)
                            .clickable {
                                if (selectedTab != option) {
                                    selectedTab = option
                                }
                            }
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option,
                            color = contentColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showReportDialogForSnippetId != null) {
        ReportSnippetDialog(
            snippetId = showReportDialogForSnippetId!!,
            viewModel = viewModel,
            onDismiss = { showReportDialogForSnippetId = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SubjectSnippetsScreen(
    viewModel: YksViewModel,
    subjectName: String,
    onBack: () -> Unit,
    onNavigateToSaved: () -> Unit,
    onNavigateToLiked: () -> Unit,
    onNavigateToKnown: () -> Unit
) {
    val topics by viewModel.hapTopics.collectAsState()
    val snippets by viewModel.hapSnippets.collectAsState()
    val interactions by viewModel.allSnippetInteractions.collectAsState()
    val followedSubjects by viewModel.allFollowedSubjects.collectAsState()

    val isStarred = followedSubjects.any { it.subjectName.equals(subjectName, ignoreCase = true) && it.isFollowed }

    var dbFollowerCount by remember { mutableStateOf<Int?>(null) }
    var initialStarState by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(subjectName) {
        dbFollowerCount = viewModel.getSubjectFollowerCount(subjectName)
    }

    if (initialStarState == null && dbFollowerCount != null) {
        initialStarState = isStarred
    }

    val followerCount = dbFollowerCount?.let { baseCount ->
        val initial = initialStarState ?: isStarred
        when {
            isStarred && !initial -> baseCount + 1
            !isStarred && initial -> (baseCount - 1).coerceAtLeast(0)
            else -> baseCount
        }
    }

    var showReportDialogForSnippetId by remember { mutableStateOf<String?>(null) }
    
    val subjectTopics = topics.filter { it.subjectName.equals(subjectName, ignoreCase = true) }
    val topicIds = subjectTopics.map { it.id }
    
    val filteredSnippets = snippets.filter { it.topicId in topicIds }
        .sortedByDescending { it.createdAt }

    var selectedSnippetIndex by remember { mutableStateOf<Int?>(null) }

    if (selectedSnippetIndex != null) {
        val pagerState = androidx.compose.foundation.pager.rememberPagerState(
            initialPage = selectedSnippetIndex!!,
            pageCount = { filteredSnippets.size }
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "$subjectName Akışı", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { selectedSnippetIndex = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kapat"
                            )
                        }
                    },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            if (followerCount != null) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Text(
                                        text = "$followerCount ⭐",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.toggleSubjectFollow(subjectName) }) {
                                Icon(
                                    imageVector = if (isStarred) Icons.Filled.Star else Icons.Filled.StarBorder,
                                    contentDescription = "Yıldızla",
                                    tint = if (isStarred) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            androidx.compose.foundation.pager.VerticalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) { page ->
                val snippet = filteredSnippets[page]
                val topic = subjectTopics.find { it.id == snippet.topicId }

                val isDark = androidx.compose.foundation.isSystemInDarkTheme()
                val slideBgColor = when (snippet.type.lowercase(java.util.Locale.ROOT)) {
                    "sinav_taktigi", "taktik" -> {
                        if (isDark) Color(0xFF2E2614) else Color(0xFFFEF9E7)
                    }
                    "tavsiye", "rehberlik", "sinav_tavsiyesi", "sınav_tavsiyesi" -> {
                        if (isDark) Color(0xFF142436) else Color(0xFFF0F7FF)
                    }
                    else -> {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(slideBgColor)
                        .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val isStarred = followedSubjects.any { it.subjectName == topic?.subjectName && it.isFollowed }
                    SnippetCard(
                        snippet = snippet,
                        topic = topic,
                        isStarred = isStarred,
                        onStarClick = { topic?.let { viewModel.toggleSubjectFollow(it.subjectName) } },
                        onSubjectClick = {},
                        contentVerticalPadding = 55.dp,
                        titleTopPadding = 130.dp,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Dikey Buton Kolonu (Beğen, Biliyorum, Kaydet, Bildir)
                    val interaction = interactions.find { it.snippetId == snippet.id }
                    val isLiked = interaction?.isLiked == true
                    val isKnown = interaction?.isKnown == true
                    val isSaved = interaction?.isSaved == true

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .width(56.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        color = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                        shape = CircleShape
                                    )
                                    .combinedClickable(
                                        onClick = { viewModel.toggleSnippetLike(snippet.id, snippet.topicId, topic?.subjectName ?: "Genel") },
                                        onLongClick = { onNavigateToLiked() }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Beğen",
                                    tint = if (isLiked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${snippet.likesCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        color = if (isKnown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                        shape = CircleShape
                                    )
                                    .combinedClickable(
                                        onClick = { viewModel.toggleSnippetKnown(snippet.id, snippet.topicId, topic?.subjectName ?: "Genel") },
                                        onLongClick = { onNavigateToKnown() }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Biliyorum",
                                    tint = if (isKnown) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${snippet.knowsCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        color = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                        shape = CircleShape
                                    )
                                    .combinedClickable(
                                        onClick = { viewModel.toggleSnippetSaved(snippet.id, snippet.topicId, topic?.subjectName ?: "Genel") },
                                        onLongClick = { onNavigateToSaved() }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Kaydet",
                                    tint = if (isSaved) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${snippet.savesCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { showReportDialogForSnippetId = snippet.id },
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Report,
                                contentDescription = "Bildir",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = subjectName, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri"
                            )
                        }
                    },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            if (followerCount != null) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                                    modifier = Modifier.padding(end = 4.dp)
                                ) {
                                    Text(
                                        text = "$followerCount ⭐",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            IconButton(onClick = { viewModel.toggleSubjectFollow(subjectName) }) {
                                Icon(
                                    imageVector = if (isStarred) Icons.Filled.Star else Icons.Filled.StarBorder,
                                    contentDescription = "Yıldızla",
                                    tint = if (isStarred) Color(0xFFFFD700) else MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            if (filteredSnippets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Bu derse ait hap bilgi bulunmuyor.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "📚",
                                            fontSize = 20.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = subjectName,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            text = "Toplam ${filteredSnippets.size} Hap Bilgi",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Ders konularını hızlıca tekrar etmek için hazırlanan hap bilgileri incele. Kartlardan birine tıklayarak dikey kaydırma moduna geçebilirsin!",
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }

                    items(filteredSnippets.size) { index ->
                        val snippet = filteredSnippets[index]
                        val topicTitle = subjectTopics.find { it.id == snippet.topicId }?.title ?: "Genel"

                        val cardBgColor = when (snippet.type.lowercase(java.util.Locale.ROOT)) {
                            "sinav_taktigi", "taktik" -> Color(0xFFFEF3C7).copy(alpha = 0.45f)
                            "tavsiye", "rehberlik", "sinav_tavsiyesi", "sınav_tavsiyesi" -> Color(0xFFE0F2FE).copy(alpha = 0.45f)
                            else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clickable { selectedSnippetIndex = index },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = cardBgColor
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = topicTitle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = parseMathText(snippet.content),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal,
                                    lineHeight = 14.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showReportDialogForSnippetId != null) {
        ReportSnippetDialog(
            snippetId = showReportDialogForSnippetId!!,
            viewModel = viewModel,
            onDismiss = { showReportDialogForSnippetId = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoginSuccess: (String, String) -> Unit
) {
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { androidx.credentials.CredentialManager.create(context) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isLoginMode) "Giriş Yap" else "Kayıt Ol",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-posta") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Email
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Şifre") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Password
            )
        )
        
        if (!isLoginMode) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Şifre (Tekrar)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Password
                )
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        if (errorMessage != null) {
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Lütfen tüm alanları doldurun."
                    return@Button
                }
                if (!isLoginMode && password != confirmPassword) {
                    errorMessage = "Şifreler uyuşmuyor."
                    return@Button
                }
                
                isLoading = true
                errorMessage = null
                
                coroutineScope.launch {
                    val response = if (isLoginMode) {
                        com.omerfaruk.ykstakip.data.SupabaseRepository.signIn(email, password)
                    } else {
                        com.omerfaruk.ykstakip.data.SupabaseRepository.signUp(email, password)
                    }
                    
                    isLoading = false
                    if (response.error != null) {
                        errorMessage = response.error
                    } else if (response.accessToken != null && response.refreshToken != null) {
                        Toast.makeText(context, "Başarılı!", Toast.LENGTH_SHORT).show()
                        onLoginSuccess(response.accessToken, response.refreshToken)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                androidx.compose.material3.CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text(if (isLoginMode) "Giriş Yap" else "Kayıt Ol", fontSize = 16.sp)
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        // Google Sign-In Button
        OutlinedButton(
            onClick = {
                isLoading = true
                errorMessage = null
                coroutineScope.launch {
                    try {
                        val googleIdOption = com.google.android.libraries.identity.googleid.GetGoogleIdOption.Builder()
                            .setFilterByAuthorizedAccounts(false)
                            .setServerClientId("578484087501-e1a9vc1qro5olp8n6eau846uoa0n8cm7.apps.googleusercontent.com") 
                            .setAutoSelectEnabled(true)
                            .build()

                        val request = androidx.credentials.GetCredentialRequest.Builder()
                            .addCredentialOption(googleIdOption)
                            .build()

                        val result = credentialManager.getCredential(context, request)
                        val credential = result.credential

                        if (credential is androidx.credentials.CustomCredential &&
                            credential.type == com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                            val googleIdTokenCredential = com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.createFrom(credential.data)
                            val idToken = googleIdTokenCredential.idToken
                            
                            val response = com.omerfaruk.ykstakip.data.SupabaseRepository.signInWithGoogle(idToken)
                            if (response.error != null) {
                                errorMessage = response.error
                                Toast.makeText(context, "Hata: ${response.error}", Toast.LENGTH_LONG).show()
                            } else if (response.accessToken != null && response.refreshToken != null) {
                                Toast.makeText(context, "Google ile Giriş Başarılı!", Toast.LENGTH_SHORT).show()
                                onLoginSuccess(response.accessToken, response.refreshToken)
                            }
                        } else {
                            errorMessage = "Beklenmeyen kimlik türü."
                            Toast.makeText(context, "Beklenmeyen kimlik türü", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: androidx.credentials.exceptions.GetCredentialException) {
                        errorMessage = "Google Hata (${e.javaClass.simpleName}): ${e.message}"
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        android.util.Log.e("Auth", "Google Sign In Error", e)
                    } catch (e: Exception) {
                        errorMessage = "Bilinmeyen Hata (${e.javaClass.simpleName}): ${e.message ?: "Açıklama yok"}"
                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                        android.util.Log.e("Auth", "General Error", e)
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !isLoading
        ) {
            Text("Google ile Giriş Yap", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = {
            isLoginMode = !isLoginMode
            errorMessage = null
        }) {
            Text(if (isLoginMode) "Hesabınız yok mu? Kayıt Olun" else "Zaten hesabınız var mı? Giriş Yapın")
        }
    }
}

fun parseMathText(text: String): AnnotatedString {
    val preprocessed = text
        .replace("\\cdot", "·")
        .replace("\\times", "×")
        .replace("\\div", "÷")
        .replace("\\pm", "±")
        .replace("\\leq", "≤")
        .replace("\\le", "≤")
        .replace("\\geq", "≥")
        .replace("\\ge", "≥")
        .replace("\\neq", "≠")
        .replace("\\infty", "∞")
        .replace("\\pi", "π")
        .replace("\\alpha", "α")
        .replace("\\beta", "β")
        .replace("\\theta", "θ")
        .replace("\\delta", "δ")
        .replace("\\Delta", "Δ")
        .replace("\\lambda", "λ")
        .replace("\\circ", "∘")
        .replace("\\cap", "∩")
        .replace("\\cup", "∪")
        .replace("\\rightarrow", "→")
        .replace("\\subseteq", "⊆")
        .replace("\\emptyset", "∅")

    return buildAnnotatedString {
        var i = 0
        val length = preprocessed.length
        
        while (i < length) {
            when {
                // Parse \sqrt[n]{x} or \sqrt{x}
                preprocessed.startsWith("\\sqrt", i) -> {
                    i += 5
                    var degree = ""
                    var content = ""
                    
                    // Check if there is an optional degree in [n]
                    if (i < length && preprocessed[i] == '[') {
                        i++ // skip '['
                        val start = i
                        while (i < length && preprocessed[i] != ']') {
                            i++
                        }
                        degree = preprocessed.substring(start, i)
                        if (i < length && preprocessed[i] == ']') i++ // skip ']'
                    }
                    
                    // Check if there is content in {x}
                    if (i < length && preprocessed[i] == '{') {
                        i++ // skip '{'
                        val start = i
                        var braceCount = 1
                        while (i < length && braceCount > 0) {
                            if (preprocessed[i] == '{') braceCount++
                            else if (preprocessed[i] == '}') braceCount--
                            i++
                        }
                        content = preprocessed.substring(start, i - 1)
                    }
                    
                    // Render \sqrt[degree]{content}
                    if (degree.isNotEmpty()) {
                        pushStyle(SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = 0.6.em))
                        append(parseMathText(degree))
                        pop()
                    }
                    append("√")
                    if (content.isNotEmpty()) {
                        append(parseMathText(content))
                    }
                }
                
                // Parse fractions: \frac{a}{b}
                preprocessed.startsWith("\\frac", i) -> {
                    i += 5
                    var num = ""
                    var den = ""
                    
                    // Numerator in {a}
                    if (i < length && preprocessed[i] == '{') {
                        i++ // skip '{'
                        val start = i
                        var braceCount = 1
                        while (i < length && braceCount > 0) {
                            if (preprocessed[i] == '{') braceCount++
                            else if (preprocessed[i] == '}') braceCount--
                            i++
                        }
                        num = preprocessed.substring(start, i - 1)
                    }
                    
                    // Denominator in {b}
                    if (i < length && preprocessed[i] == '{') {
                        i++ // skip '{'
                        val start = i
                        var braceCount = 1
                        while (i < length && braceCount > 0) {
                            if (preprocessed[i] == '{') braceCount++
                            else if (preprocessed[i] == '}') braceCount--
                            i++
                        }
                        den = preprocessed.substring(start, i - 1)
                    }
                    
                    // Render fraction as (num/den) with superscript / subscript formatting
                    pushStyle(SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = 0.6.em))
                    append(parseMathText(num))
                    pop()
                    append("/")
                    pushStyle(SpanStyle(baselineShift = BaselineShift.Subscript, fontSize = 0.6.em))
                    append(parseMathText(den))
                    pop()
                }
                
                // Parse superscript: ^
                preprocessed[i] == '^' -> {
                    i++ // skip '^'
                    if (i < length) {
                        if (preprocessed[i] == '{') {
                            i++ // skip '{'
                            val start = i
                            var braceCount = 1
                            while (i < length && braceCount > 0) {
                                if (preprocessed[i] == '{') braceCount++
                                else if (preprocessed[i] == '}') braceCount--
                                i++
                            }
                            val superText = preprocessed.substring(start, i - 1)
                            pushStyle(SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = 0.6.em))
                            append(parseMathText(superText))
                            pop()
                        } else {
                            // Single character superscript
                            val superChar = preprocessed[i].toString()
                            pushStyle(SpanStyle(baselineShift = BaselineShift.Superscript, fontSize = 0.6.em))
                            append(superChar)
                            pop()
                            i++
                        }
                    }
                }
                
                // Parse subscript: _
                preprocessed[i] == '_' -> {
                    i++ // skip '_'
                    if (i < length) {
                        if (preprocessed[i] == '{') {
                            i++ // skip '{'
                            val start = i
                            var braceCount = 1
                            while (i < length && braceCount > 0) {
                                if (preprocessed[i] == '{') braceCount++
                                else if (preprocessed[i] == '}') braceCount--
                                i++
                            }
                            val subText = preprocessed.substring(start, i - 1)
                            pushStyle(SpanStyle(baselineShift = BaselineShift.Subscript, fontSize = 0.6.em))
                            append(parseMathText(subText))
                            pop()
                        } else {
                            // Single character subscript
                            val subChar = preprocessed[i].toString()
                            pushStyle(SpanStyle(baselineShift = BaselineShift.Subscript, fontSize = 0.6.em))
                            append(subChar)
                            pop()
                            i++
                        }
                    }
                }
                
                // Remove $ signs (which wrap math mode in LaTeX)
                preprocessed[i] == '$' -> {
                    i++
                }
                
                else -> {
                    append(preprocessed[i])
                    i++
                }
            }
        }
    }
}

data class SubjectConfig(val name: String, val maxQuestions: Int, val category: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoreCalculationScreen(
    viewModel: YksViewModel,
    onBack: () -> Unit
) {
    var isAddingNew by remember { mutableStateOf(false) }
    val history by viewModel.calculationHistory.collectAsState()
    
    if (isAddingNew) {
        NewCalculationScreen(
            viewModel = viewModel,
            onBack = { isAddingNew = false },
            onSaveComplete = {
                isAddingNew = false
            }
        )
    } else {
        CalculationHistoryListScreen(
            history = history,
            viewModel = viewModel,
            onAddClick = { isAddingNew = true },
            onBack = onBack
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculationHistoryListScreen(
    history: List<CalculationHistoryEntity>,
    viewModel: YksViewModel,
    onAddClick: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sıralama Hesapla", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = onAddClick) {
                        Icon(Icons.Default.Add, contentDescription = "Yeni Hesaplama")
                    }
                }
            )
        }
    ) { paddingValues ->
        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Bilgi",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Henüz kayıtlı hesaplama yok.",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Yeni bir hesaplama yapmak için sağ üst köşedeki + butonuna tıklayabilirsiniz.",
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onAddClick,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Yeni Hesaplama Yap")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
            ) {
                items(history, key = { it.id }) { item ->
                    CalculationHistoryCard(item = item, onDelete = {
                        viewModel.deleteCalculationHistory(item.id)
                    })
                }
            }
        }
    }
}

@Composable
fun CalculationHistoryCard(
    item: CalculationHistoryEntity,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val sdf = remember { java.text.SimpleDateFormat("dd MMMM yyyy HH:mm", java.util.Locale("tr")) }
    val formattedDate = remember(item.date) { sdf.format(java.util.Date(item.date)) }

    val resultsJson = remember(item.resultsJson) {
        try { JSONObject(item.resultsJson) } catch (e: Exception) { null }
    }
    val inputsJson = remember(item.inputsJson) {
        try { JSONObject(item.inputsJson) } catch (e: Exception) { null }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = formattedDate,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "OBP: ${item.obp}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Sil",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Daralt" else "Genişlet"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (!expanded && resultsJson != null) {
                val results2025 = resultsJson.optJSONObject("2025")
                if (results2025 != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        val getRankStr = { key: String, yKey: String ->
                            val r = results2025.optInt(yKey, -1)
                            if (r != -1) formatRank(r) else formatRank(results2025.optInt(key))
                        }
                        SummaryRankChip("TYT: ${getRankStr("TYT_Rank", "Y_TYT_Rank")}")
                        SummaryRankChip("SAY: ${getRankStr("SAY_Rank", "Y_SAY_Rank")}")
                        SummaryRankChip("EA: ${getRankStr("EA_Rank", "Y_EA_Rank")}")
                        SummaryRankChip("SÖZ: ${getRankStr("SOZ_Rank", "Y_SOZ_Rank")}")
                    }
                }
            }

            if (expanded && resultsJson != null) {
                Spacer(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)))
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Tahmini Sıralamalar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))

                val years = listOf("2025", "2024", "2023")
                years.forEach { year ->
                    val yearData = resultsJson.optJSONObject(year)
                    if (yearData != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "$year Yılı Sıralama & Puan Tahmini",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            val rawTYTRank = yearData.optInt("TYT_Rank")
                            val yTYTRank = if (yearData.has("Y_TYT_Rank")) yearData.optInt("Y_TYT_Rank") else rawTYTRank
                            
                            val rawSAYRank = yearData.optInt("SAY_Rank")
                            val ySAYRank = if (yearData.has("Y_SAY_Rank")) yearData.optInt("Y_SAY_Rank") else rawSAYRank
                            
                            val rawEARank = yearData.optInt("EA_Rank")
                            val yEARank = if (yearData.has("Y_EA_Rank")) yearData.optInt("Y_EA_Rank") else rawEARank
                            
                            val rawSOZRank = yearData.optInt("SOZ_Rank")
                            val ySOZRank = if (yearData.has("Y_SOZ_Rank")) yearData.optInt("Y_SOZ_Rank") else rawSOZRank

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("TYT", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    Text("Ham: ${formatRank(rawTYTRank)} (${Math.round(yearData.optDouble("TYT_Score") * 100) / 100.0} P)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Yerl: ${formatRank(yTYTRank)} (${Math.round(yearData.optDouble("Y_TYT_Score") * 100) / 100.0} P)", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("SAY", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    Text("Ham: ${formatRank(rawSAYRank)} (${Math.round(yearData.optDouble("SAY_Score") * 100) / 100.0} P)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Yerl: ${formatRank(ySAYRank)} (${Math.round(yearData.optDouble("Y_SAY_Score") * 100) / 100.0} P)", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("EA", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    Text("Ham: ${formatRank(rawEARank)} (${Math.round(yearData.optDouble("EA_Score") * 100) / 100.0} P)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Yerl: ${formatRank(yEARank)} (${Math.round(yearData.optDouble("Y_EA_Score") * 100) / 100.0} P)", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("SÖZ", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    Text("Ham: ${formatRank(rawSOZRank)} (${Math.round(yearData.optDouble("SOZ_Score") * 100) / 100.0} P)", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("Yerl: ${formatRank(ySOZRank)} (${Math.round(yearData.optDouble("Y_SOZ_Score") * 100) / 100.0} P)", fontSize = 10.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                if (inputsJson != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Giriş Netleri",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        inputsJson.keys().forEach { key ->
                            val input = inputsJson.optJSONObject(key)
                            if (input != null) {
                                val d = input.optDouble("d", 0.0)
                                val y = input.optDouble("y", 0.0)
                                val net = d - 0.25 * y
                                if (net > 0 || d > 0) {
                                    InputNetChip(name = key, net = net)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryRankChip(text: String) {
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
fun InputNetChip(name: String, net: Double) {
    val cleanName = name.replace("TYT ", "").replace("AYT ", "")
    val netStr = if (net % 1.0 == 0.0) net.toInt().toString() else (Math.round(net * 100) / 100.0).toString()
    Box(
        modifier = Modifier
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$cleanName: $netStr Net",
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

@Composable
fun RankDetailRow(type: String, score: Double, rank: Int) {
    val roundedScore = Math.round(score * 100) / 100.0
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.width(150.dp)
    ) {
        Text(
            text = "$type:",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$roundedScore Puan",
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = formatRank(rank),
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

fun formatRank(rank: Int): String {
    if (rank <= 0) return "N/A"
    return try {
        java.text.NumberFormat.getInstance(java.util.Locale("tr")).format(rank) + "."
    } catch (e: Exception) {
        "$rank."
    }
}

data class CalculationResultPreview(
    val obp: Float,
    val scores2023: Map<String, Float>,
    val yScores2023: Map<String, Float>,
    val scores2024: Map<String, Float>,
    val yScores2024: Map<String, Float>,
    val scores2025: Map<String, Float>,
    val yScores2025: Map<String, Float>,
    val rawRankings2023: Map<String, Int>,
    val yRankings2023: Map<String, Int>,
    val rawRankings2024: Map<String, Int>,
    val yRankings2024: Map<String, Int>,
    val rawRankings2025: Map<String, Int>,
    val yRankings2025: Map<String, Int>
)

fun previewCalculation(
    obp: Float,
    correctInputs: Map<String, String>,
    wrongInputs: Map<String, String>,
    yigilmaList: List<SupabaseYigilma>
): CalculationResultPreview {
    val getVal = { subject: String, isCorrect: Boolean ->
        val map = if (isCorrect) correctInputs else wrongInputs
        map[subject]?.toIntOrNull()?.toFloat() ?: 0f
    }
    
    val getNetVal = { subject: String ->
        val d = getVal(subject, true)
        val y = getVal(subject, false)
        d - 0.25f * y
    }

    val tytTurkceNet = getNetVal("TYT Türkçe")
    val tytSosyalNet = getNetVal("TYT Sosyal")
    val tytMatNet = getNetVal("TYT Matematik")
    val tytFenNet = getNetVal("TYT Fen")

    val aytMatNet = getNetVal("AYT Matematik")
    val aytFizikNet = getNetVal("AYT Fizik")
    val aytKimyaNet = getNetVal("AYT Kimya")
    val aytBiyolojiNet = getNetVal("AYT Biyoloji")
    val aytEdebiyatNet = getNetVal("AYT Edebiyat")
    val aytTarih1Net = getNetVal("AYT Tarih-1")
    val aytCografya1Net = getNetVal("AYT Coğrafya-1")
    val aytTarih2Net = getNetVal("AYT Tarih-2")
    val aytCografya2Net = getNetVal("AYT Coğrafya-2")
    val aytFelsefeNet = getNetVal("AYT Felsefe Grb.")
    val aytDinNet = getNetVal("AYT Din")

    val clamp = { v: Float -> v.coerceIn(100f, 500f) }

    val calcTyt = { year: Int ->
        when (year) {
            2023 -> clamp(141.898f + tytTurkceNet * 2.890f + tytSosyalNet * 3.024f + tytMatNet * 3.021f + tytFenNet * 3.057f)
            2024 -> clamp(142.15f + tytTurkceNet * 2.91f + tytSosyalNet * 2.98f + tytMatNet * 2.99f + tytFenNet * 3.02f)
            else -> clamp(142.0f + tytTurkceNet * 2.90f + tytSosyalNet * 3.00f + tytMatNet * 3.00f + tytFenNet * 3.00f)
        }
    }

    val calcSay = { tytScore: Float, year: Int ->
        when (year) {
            2023 -> {
                val aytScore = clamp(118.868f + aytMatNet * 4.70f + aytFizikNet * 4.13f + aytKimyaNet * 4.90f + aytBiyolojiNet * 5.17f)
                clamp(tytScore * 0.4f + aytScore * 0.6f)
            }
            2024 -> {
                val aytScore = clamp(119.15f + aytMatNet * 4.80f + aytFizikNet * 4.50f + aytKimyaNet * 4.80f + aytBiyolojiNet * 4.80f)
                clamp(tytScore * 0.4f + aytScore * 0.6f)
            }
            else -> {
                val aytScore = clamp(119.00f + aytMatNet * 5.00f + aytFizikNet * 4.76f + aytKimyaNet * 5.128f + aytBiyolojiNet * 5.128f)
                clamp(tytScore * 0.4f + aytScore * 0.6f)
            }
        }
    }
    val calcEa = { tytScore: Float, year: Int ->
        when (year) {
            2023 -> {
                val aytScore = clamp(118.868f + aytMatNet * 4.70f + aytEdebiyatNet * 4.70f + aytTarih1Net * 4.38f + aytCografya1Net * 5.22f)
                clamp(tytScore * 0.4f + aytScore * 0.6f)
            }
            2024 -> {
                val aytScore = clamp(119.15f + aytMatNet * 4.80f + aytEdebiyatNet * 4.80f + aytTarih1Net * 4.50f + aytCografya1Net * 5.20f)
                clamp(tytScore * 0.4f + aytScore * 0.6f)
            }
            else -> {
                val aytScore = clamp(119.00f + aytMatNet * 5.00f + aytEdebiyatNet * 5.00f + aytTarih1Net * 4.667f + aytCografya1Net * 5.556f)
                clamp(tytScore * 0.4f + aytScore * 0.6f)
            }
        }
    }
    val calcSoz = { tytScore: Float, year: Int ->
        when (year) {
            2023 -> {
                val aytScore = clamp(118.868f + aytEdebiyatNet * 4.70f + aytTarih1Net * 4.38f + aytCografya1Net * 5.22f + aytTarih2Net * 4.57f + aytCografya2Net * 4.57f + aytFelsefeNet * 4.70f + aytDinNet * 5.22f)
                clamp(tytScore * 0.4f + aytScore * 0.6f)
            }
            2024 -> {
                val aytScore = clamp(119.15f + aytEdebiyatNet * 4.80f + aytTarih1Net * 4.50f + aytCografya1Net * 5.20f + aytTarih2Net * 4.70f + aytCografya2Net * 4.70f + aytFelsefeNet * 4.80f + aytDinNet * 5.20f)
                clamp(tytScore * 0.4f + aytScore * 0.6f)
            }
            else -> {
                val aytScore = clamp(119.00f + aytEdebiyatNet * 5.00f + aytTarih1Net * 4.667f + aytCografya1Net * 5.556f + aytTarih2Net * 4.85f + aytCografya2Net * 4.85f + aytFelsefeNet * 5.00f + aytDinNet * 5.55f)
                clamp(tytScore * 0.4f + aytScore * 0.6f)
            }
        }
    }

    val obpContribution = obp * 0.6f

    // 2023 Scores
    val tyt2023 = calcTyt(2023)
    val say2023 = calcSay(tyt2023, 2023)
    val ea2023 = calcEa(tyt2023, 2023)
    val soz2023 = calcSoz(tyt2023, 2023)

    // 2024 Scores
    val tyt2024 = calcTyt(2024)
    val say2024 = calcSay(tyt2024, 2024)
    val ea2024 = calcEa(tyt2024, 2024)
    val soz2024 = calcSoz(tyt2024, 2024)

    // 2025 Scores
    val tyt2025 = calcTyt(2025)
    val say2025 = calcSay(tyt2025, 2025)
    val ea2025 = calcEa(tyt2025, 2025)
    val soz2025 = calcSoz(tyt2025, 2025)

    val interp = { type: String, year: Int, score: Float, isPlacement: Boolean ->
        val actualPuanTuru = if (isPlacement) {
            val prefixOptions = listOf("Y-$type", "Y_$type", "Y$type")
            prefixOptions.find { option ->
                yigilmaList.any { it.puanTuru.equals(option, ignoreCase = true) && it.yil == year }
            } ?: "Y-$type"
        } else {
            type
        }
        val matchedPoints = yigilmaList.filter { it.puanTuru.equals(actualPuanTuru, ignoreCase = true) && it.yil == year }
            .sortedBy { it.puan }

        if (matchedPoints.isEmpty()) {
            val maxPoints = if (isPlacement) 560f else 500f
            val minPoints = 100f
            val pct = ((maxPoints - score) / (maxPoints - minPoints)).coerceIn(0f, 1f)
            val maxRank = when (type) {
                "TYT" -> 3000000.0
                "SAY" -> 1500000.0
                "EA" -> 1800000.0
                "SOZ" -> 1600000.0
                else -> 2000000.0
            }
            val exponent = when (year) {
                2023 -> 3.6
                2024 -> 3.3
                2025 -> 3.5
                else -> 3.5
            }
            val rank = Math.pow(pct.toDouble(), exponent) * maxRank
            rank.toInt().coerceAtLeast(1)
        } else {
            val target = score

            if (target <= matchedPoints.first().puan) {
                matchedPoints.first().siralama
            } else if (target >= matchedPoints.last().puan) {
                matchedPoints.last().siralama
            } else {
                var res = matchedPoints.last().siralama
                for (i in 0 until matchedPoints.size - 1) {
                    val pA = matchedPoints[i]
                    val pB = matchedPoints[i + 1]
                    if (target >= pA.puan && target <= pB.puan) {
                        val diffPuan = pB.puan - pA.puan
                        val diffSiralama = pB.siralama - pA.siralama
                        res = if (diffPuan == 0f) pA.siralama else (pA.siralama + (target - pA.puan) * diffSiralama / diffPuan).toInt()
                        break
                    }
                }
                res
            }
        }
    }

    return CalculationResultPreview(
        obp = obp,
        scores2023 = mapOf("TYT" to tyt2023, "SAY" to say2023, "EA" to ea2023, "SOZ" to soz2023),
        yScores2023 = mapOf("TYT" to tyt2023 + obpContribution, "SAY" to say2023 + obpContribution, "EA" to ea2023 + obpContribution, "SOZ" to soz2023 + obpContribution),
        scores2024 = mapOf("TYT" to tyt2024, "SAY" to say2024, "EA" to ea2024, "SOZ" to soz2024),
        yScores2024 = mapOf("TYT" to tyt2024 + obpContribution, "SAY" to say2024 + obpContribution, "EA" to ea2024 + obpContribution, "SOZ" to soz2024 + obpContribution),
        scores2025 = mapOf("TYT" to tyt2025, "SAY" to say2025, "EA" to ea2025, "SOZ" to soz2025),
        yScores2025 = mapOf("TYT" to tyt2025 + obpContribution, "SAY" to say2025 + obpContribution, "EA" to ea2025 + obpContribution, "SOZ" to soz2025 + obpContribution),
        rawRankings2023 = mapOf("TYT" to interp("TYT", 2023, tyt2023, false), "SAY" to interp("SAY", 2023, say2023, false), "EA" to interp("EA", 2023, ea2023, false), "SOZ" to interp("SOZ", 2023, soz2023, false)),
        yRankings2023 = mapOf("TYT" to interp("TYT", 2023, tyt2023 + obpContribution, true), "SAY" to interp("SAY", 2023, say2023 + obpContribution, true), "EA" to interp("EA", 2023, ea2023 + obpContribution, true), "SOZ" to interp("SOZ", 2023, soz2023 + obpContribution, true)),
        rawRankings2024 = mapOf("TYT" to interp("TYT", 2024, tyt2024, false), "SAY" to interp("SAY", 2024, say2024, false), "EA" to interp("EA", 2024, ea2024, false), "SOZ" to interp("SOZ", 2024, soz2024, false)),
        yRankings2024 = mapOf("TYT" to interp("TYT", 2024, tyt2024 + obpContribution, true), "SAY" to interp("SAY", 2024, say2024 + obpContribution, true), "EA" to interp("EA", 2024, ea2024 + obpContribution, true), "SOZ" to interp("SOZ", 2024, soz2024 + obpContribution, true)),
        rawRankings2025 = mapOf("TYT" to interp("TYT", 2025, tyt2025, false), "SAY" to interp("SAY", 2025, say2025, false), "EA" to interp("EA", 2025, ea2025, false), "SOZ" to interp("SOZ", 2025, soz2025, false)),
        yRankings2025 = mapOf("TYT" to interp("TYT", 2025, tyt2025 + obpContribution, true), "SAY" to interp("SAY", 2025, say2025 + obpContribution, true), "EA" to interp("EA", 2025, ea2025 + obpContribution, true), "SOZ" to interp("SOZ", 2025, soz2025 + obpContribution, true))
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCalculationScreen(
    viewModel: YksViewModel,
    onBack: () -> Unit,
    onSaveComplete: () -> Unit
) {
    val context = LocalContext.current
    val yigilmaList by viewModel.yigilmaData.collectAsState()
    
    val userInfo by viewModel.userInfo.collectAsState()
    var obpInput by remember { mutableStateOf("100") }
    LaunchedEffect(userInfo) {
        userInfo?.let {
            obpInput = it.obp
        }
    }
    val correctInputs = remember { mutableStateMapOf<String, String>() }
    val wrongInputs = remember { mutableStateMapOf<String, String>() }
    
    val subjectConfigs = remember {
        listOf(
            SubjectConfig("TYT Türkçe", 40, "TYT"),
            SubjectConfig("TYT Sosyal", 20, "TYT"),
            SubjectConfig("TYT Matematik", 40, "TYT"),
            SubjectConfig("TYT Fen", 20, "TYT"),
            SubjectConfig("AYT Matematik", 40, "AYT_MAT"),
            SubjectConfig("AYT Fizik", 14, "AYT_FEN"),
            SubjectConfig("AYT Kimya", 13, "AYT_FEN"),
            SubjectConfig("AYT Biyoloji", 13, "AYT_FEN"),
            SubjectConfig("AYT Edebiyat", 24, "AYT_SOZ"),
            SubjectConfig("AYT Tarih-1", 10, "AYT_SOZ"),
            SubjectConfig("AYT Coğrafya-1", 6, "AYT_SOZ"),
            SubjectConfig("AYT Tarih-2", 11, "AYT_SOZ"),
            SubjectConfig("AYT Coğrafya-2", 11, "AYT_SOZ"),
            SubjectConfig("AYT Felsefe Grb.", 12, "AYT_SOZ"),
            SubjectConfig("AYT Din", 6, "AYT_SOZ")
        )
    }

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("TYT Girişleri", "AYT Girişleri")

    val obp = obpInput.toFloatOrNull()
    val isObpValid = obp != null && obp >= 50f && obp <= 100f

    val subjectErrors = remember(correctInputs.toMap(), wrongInputs.toMap()) {
        val errors = mutableMapOf<String, String>()
        subjectConfigs.forEach { config ->
            val dStr = correctInputs[config.name] ?: ""
            val yStr = wrongInputs[config.name] ?: ""
            val d = dStr.toIntOrNull() ?: 0
            val y = yStr.toIntOrNull() ?: 0
            
            if (dStr.isNotEmpty() && d < 0) {
                errors[config.name] = "Negatif değer girilemez!"
            } else if (yStr.isNotEmpty() && y < 0) {
                errors[config.name] = "Negatif değer girilemez!"
            } else if (d + y > config.maxQuestions) {
                errors[config.name] = "Toplam soru sayısı ${config.maxQuestions}'u aşamaz! ($d + $y = ${d+y})"
            }
        }
        errors
    }

    val hasValidationError = subjectErrors.isNotEmpty() || !isObpValid

    var previewResults by remember { mutableStateOf<CalculationResultPreview?>(null) }
    var showResultDialog by remember { mutableStateOf(false) }

    val activeConfigs = remember(selectedTab) {
        if (selectedTab == 0) {
            subjectConfigs.filter { it.category == "TYT" }
        } else {
            subjectConfigs.filter { it.category != "TYT" }
        }
    }

    val saveAction = {
        val finalObp = obp ?: 100f
        val finalInputs = subjectConfigs.associate { config ->
            val d = correctInputs[config.name]?.toIntOrNull()?.toFloat() ?: 0f
            val y = wrongInputs[config.name]?.toIntOrNull()?.toFloat() ?: 0f
            config.name to Pair(d, y)
        }
        
        viewModel.calculateAndSaveRankings(finalObp, finalInputs) { success, error ->
            if (success) {
                Toast.makeText(context, "Sıralama başarıyla hesaplandı ve kaydedildi!", Toast.LENGTH_SHORT).show()
                onSaveComplete()
            } else {
                Toast.makeText(context, "Hata: $error", Toast.LENGTH_LONG).show()
            }
        }
    }

    if (showResultDialog && previewResults != null) {
        ResultDialog(
            preview = previewResults!!,
            onDismiss = { showResultDialog = false },
            onSave = {
                showResultDialog = false
                saveAction()
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Yeni Hesaplama", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = FontWeight.SemiBold) }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Ortaöğretim Başarı Puanı (OBP)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = obpInput,
                                onValueChange = { obpInput = it },
                                label = { Text("OBP (50 - 100)") },
                                placeholder = { Text("Örn: 92.5") },
                                isError = !isObpValid,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                supportingText = {
                                    if (!isObpValid) {
                                        Text("Geçerli bir OBP girin (50 - 100 arası).", color = MaterialTheme.colorScheme.error)
                                    } else {
                                        Text("Yerleştirme puanınıza OBP * 0.6 eklenecektir.")
                                    }
                                }
                            )
                        }
                    }
                }

                items(activeConfigs, key = { it.name }) { config ->
                    val correctVal = correctInputs[config.name] ?: ""
                    val wrongVal = wrongInputs[config.name] ?: ""
                    val errorMsg = subjectErrors[config.name]

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = config.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Soru: ${config.maxQuestions}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                OutlinedTextField(
                                    value = correctVal,
                                    onValueChange = { correctInputs[config.name] = it },
                                    label = { Text("Doğru") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    isError = errorMsg != null
                                )
                                OutlinedTextField(
                                    value = wrongVal,
                                    onValueChange = { wrongInputs[config.name] = it },
                                    label = { Text("Yanlış") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    isError = errorMsg != null
                                )
                            }
                            if (errorMsg != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = errorMsg,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (!hasValidationError) {
                                val finalObp = obp ?: 100f
                                previewResults = previewCalculation(finalObp, correctInputs, wrongInputs, yigilmaList)
                                showResultDialog = true
                            } else {
                                Toast.makeText(context, "Lütfen formu hatalar olmadan doldurun.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        enabled = !hasValidationError
                    ) {
                        Text("Hesapla")
                    }
                    
                    Button(
                        onClick = {
                            if (!hasValidationError) {
                                saveAction()
                            } else {
                                Toast.makeText(context, "Lütfen formu hatalar olmadan doldurun.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        enabled = !hasValidationError
                    ) {
                        Text("Kaydet")
                    }
                }
            }
        }
    }
}

@Composable
fun ResultDialog(
    preview: CalculationResultPreview,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Hesaplama Sonuçları",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            var selectedYearTab by remember { mutableStateOf(0) }
            val yearTabs = listOf("2025", "2024", "2023")
            
            Column(modifier = Modifier.fillMaxWidth()) {
                TabRow(selectedTabIndex = selectedYearTab) {
                    yearTabs.forEachIndexed { index, year ->
                        Tab(
                            selected = selectedYearTab == index,
                            onClick = { selectedYearTab = index },
                            text = { Text(year, fontWeight = FontWeight.Bold) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val currentYear = yearTabs[selectedYearTab]
                val rawRanks = when (currentYear) {
                    "2023" -> preview.rawRankings2023
                    "2024" -> preview.rawRankings2024
                    else -> preview.rawRankings2025
                }
                val yRanks = when (currentYear) {
                    "2023" -> preview.yRankings2023
                    "2024" -> preview.yRankings2024
                    else -> preview.yRankings2025
                }
                val yearScores = when (currentYear) {
                    "2023" -> preview.scores2023
                    "2024" -> preview.scores2024
                    else -> preview.scores2025
                }
                val yearYScores = when (currentYear) {
                    "2023" -> preview.yScores2023
                    "2024" -> preview.yScores2024
                    else -> preview.yScores2025
                }
                
                Text(
                    text = "$currentYear Yılı Tahmini Sıralamaları",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                listOf("TYT", "SAY", "EA", "SOZ").forEach { type ->
                    val rawScore = yearScores[type] ?: 100f
                    val yScore = yearYScores[type] ?: 100f
                    val rawRank = rawRanks[type] ?: 0
                    val yRank = yRanks[type] ?: 0
                    
                    ResultDetailRow(type = type, rawScore = rawScore, yScore = yScore, rawRank = rawRank, yRank = yRank)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onSave,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Kapat")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun ResultDetailRow(type: String, rawScore: Float, yScore: Float, rawRank: Int, yRank: Int) {
    val roundedRaw = Math.round(rawScore * 100) / 100.0
    val roundedY = Math.round(yScore * 100) / 100.0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = type,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Ham: $roundedRaw Puan",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Yerleştirme: $roundedY Puan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Ham: ",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatRank(rawRank),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Yerleştirme: ",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatRank(yRank),
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun SavedSnippetsScreen(
    viewModel: YksViewModel,
    onBack: () -> Unit
) {
    val topics by viewModel.hapTopics.collectAsState()
    val interactions by viewModel.allSnippetInteractions.collectAsState()
    val followedSubjects by viewModel.allFollowedSubjects.collectAsState()

    var allSnippets by remember { mutableStateOf<List<com.omerfaruk.ykstakip.data.SupabaseSnippet>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (topics.isEmpty()) {
            viewModel.fetchHapTopics()
        }
        isLoading = true
        try {
            allSnippets = com.omerfaruk.ykstakip.data.SupabaseRepository.getAllSnippets()
        } catch (e: Exception) {
            android.util.Log.e("SavedSnippets", "Failed to fetch snippets", e)
        }
        isLoading = false
    }

    val savedSnippetIds = interactions.filter { it.isSaved }.map { it.snippetId }.toSet()
    val savedSnippets = allSnippets.filter { it.id in savedSnippetIds }

    var showReportDialogForSnippetId by remember { mutableStateOf<String?>(null) }
    var selectedSnippetIndex by remember { mutableStateOf<Int?>(null) }

    if (selectedSnippetIndex != null) {
        val pagerState = androidx.compose.foundation.pager.rememberPagerState(
            initialPage = selectedSnippetIndex!!,
            pageCount = { savedSnippets.size }
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Kaydedilen Haplar Akışı", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { selectedSnippetIndex = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kapat"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            androidx.compose.foundation.pager.VerticalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) { page ->
                val snippet = savedSnippets[page]
                val topic = topics.find { it.id == snippet.topicId }

                val isDark = androidx.compose.foundation.isSystemInDarkTheme()
                val slideBgColor = when (snippet.type.lowercase(java.util.Locale.ROOT)) {
                    "sinav_taktigi", "taktik" -> {
                        if (isDark) Color(0xFF2E2614) else Color(0xFFFEF9E7)
                    }
                    "tavsiye", "rehberlik", "sinav_tavsiyesi", "sınav_tavsiyesi" -> {
                        if (isDark) Color(0xFF142436) else Color(0xFFF0F7FF)
                    }
                    else -> {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(slideBgColor)
                        .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val isStarred = followedSubjects.any { it.subjectName == topic?.subjectName && it.isFollowed }
                    SnippetCard(
                        snippet = snippet,
                        topic = topic,
                        isStarred = isStarred,
                        onStarClick = { topic?.let { viewModel.toggleSubjectFollow(it.subjectName) } },
                        onSubjectClick = {},
                        contentVerticalPadding = 55.dp,
                        titleTopPadding = 130.dp,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Dikey Buton Kolonu (Beğen, Biliyorum, Kaydet, Bildir)
                    val interaction = interactions.find { it.snippetId == snippet.id }
                    val isLiked = interaction?.isLiked == true
                    val isKnown = interaction?.isKnown == true
                    val isSaved = interaction?.isSaved == true

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .width(56.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        color = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        viewModel.toggleSnippetLike(snippet.id, snippet.topicId, topic?.subjectName ?: "Genel")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Beğen",
                                    tint = if (isLiked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${snippet.likesCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        color = if (isKnown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        viewModel.toggleSnippetKnown(snippet.id, snippet.topicId, topic?.subjectName ?: "Genel")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Biliyorum",
                                    tint = if (isKnown) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${snippet.knowsCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        color = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        viewModel.toggleSnippetSaved(snippet.id, snippet.topicId, topic?.subjectName ?: "Genel")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Kaydet",
                                    tint = if (isSaved) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${snippet.savesCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { showReportDialogForSnippetId = snippet.id },
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Report,
                                contentDescription = "Bildir",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Kaydedilen Haplar", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            if (isLoading && allSnippets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (savedSnippets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🔖", fontSize = 48.sp)
                        Text(
                            text = "Henüz kaydedilmiş hap bilgi bulunmuyor.",
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    items(savedSnippets.size) { index ->
                        val snippet = savedSnippets[index]
                        val topic = topics.find { it.id == snippet.topicId }
                        val topicTitle = topic?.title ?: "Genel"

                        val cardBgColor = when (snippet.type.lowercase(java.util.Locale.ROOT)) {
                            "sinav_taktigi", "taktik" -> Color(0xFFFEF3C7).copy(alpha = 0.45f)
                            "tavsiye", "rehberlik", "sinav_tavsiyesi", "sınav_tavsiyesi" -> Color(0xFFE0F2FE).copy(alpha = 0.45f)
                            else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clickable { selectedSnippetIndex = index },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = cardBgColor
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = topicTitle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = parseMathText(snippet.content),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal,
                                    lineHeight = 14.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showReportDialogForSnippetId != null) {
        ReportSnippetDialog(
            snippetId = showReportDialogForSnippetId!!,
            viewModel = viewModel,
            onDismiss = { showReportDialogForSnippetId = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun LikedSnippetsScreen(
    viewModel: YksViewModel,
    onBack: () -> Unit
) {
    val topics by viewModel.hapTopics.collectAsState()
    val interactions by viewModel.allSnippetInteractions.collectAsState()
    val followedSubjects by viewModel.allFollowedSubjects.collectAsState()

    var allSnippets by remember { mutableStateOf<List<com.omerfaruk.ykstakip.data.SupabaseSnippet>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (topics.isEmpty()) {
            viewModel.fetchHapTopics()
        }
        isLoading = true
        try {
            allSnippets = com.omerfaruk.ykstakip.data.SupabaseRepository.getAllSnippets()
        } catch (e: Exception) {
            android.util.Log.e("LikedSnippets", "Failed to fetch snippets", e)
        }
        isLoading = false
    }

    val likedSnippetIds = interactions.filter { it.isLiked }.map { it.snippetId }.toSet()
    val likedSnippets = allSnippets.filter { it.id in likedSnippetIds }

    var showReportDialogForSnippetId by remember { mutableStateOf<String?>(null) }
    var selectedSnippetIndex by remember { mutableStateOf<Int?>(null) }

    if (selectedSnippetIndex != null) {
        val pagerState = androidx.compose.foundation.pager.rememberPagerState(
            initialPage = selectedSnippetIndex!!,
            pageCount = { likedSnippets.size }
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Beğenilen Haplar Akışı", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { selectedSnippetIndex = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kapat"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            androidx.compose.foundation.pager.VerticalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) { page ->
                val snippet = likedSnippets[page]
                val topic = topics.find { it.id == snippet.topicId }

                val isDark = androidx.compose.foundation.isSystemInDarkTheme()
                val slideBgColor = when (snippet.type.lowercase(java.util.Locale.ROOT)) {
                    "sinav_taktigi", "taktik" -> {
                        if (isDark) Color(0xFF2E2614) else Color(0xFFFEF9E7)
                    }
                    "tavsiye", "rehberlik", "sinav_tavsiyesi", "sınav_tavsiyesi" -> {
                        if (isDark) Color(0xFF142436) else Color(0xFFF0F7FF)
                    }
                    else -> {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(slideBgColor)
                        .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val isStarred = followedSubjects.any { it.subjectName == topic?.subjectName && it.isFollowed }
                    SnippetCard(
                        snippet = snippet,
                        topic = topic,
                        isStarred = isStarred,
                        onStarClick = { topic?.let { viewModel.toggleSubjectFollow(it.subjectName) } },
                        onSubjectClick = {},
                        contentVerticalPadding = 55.dp,
                        titleTopPadding = 130.dp,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Dikey Buton Kolonu (Beğen, Biliyorum, Kaydet, Bildir)
                    val interaction = interactions.find { it.snippetId == snippet.id }
                    val isLiked = interaction?.isLiked == true
                    val isKnown = interaction?.isKnown == true
                    val isSaved = interaction?.isSaved == true

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .width(56.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        color = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        viewModel.toggleSnippetLike(snippet.id, snippet.topicId, topic?.subjectName ?: "Genel")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Beğen",
                                    tint = if (isLiked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${snippet.likesCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        color = if (isKnown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        viewModel.toggleSnippetKnown(snippet.id, snippet.topicId, topic?.subjectName ?: "Genel")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Biliyorum",
                                    tint = if (isKnown) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${snippet.knowsCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        color = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        viewModel.toggleSnippetSaved(snippet.id, snippet.topicId, topic?.subjectName ?: "Genel")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Kaydet",
                                    tint = if (isSaved) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${snippet.savesCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { showReportDialogForSnippetId = snippet.id },
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Report,
                                contentDescription = "Bildir",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Beğenilen Haplar", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            if (isLoading && allSnippets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (likedSnippets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("❤️", fontSize = 48.sp)
                        Text(
                            text = "Henüz beğendiğiniz hap bilgi bulunmuyor.",
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    items(likedSnippets.size) { index ->
                        val snippet = likedSnippets[index]
                        val topic = topics.find { it.id == snippet.topicId }
                        val topicTitle = topic?.title ?: "Genel"

                        val cardBgColor = when (snippet.type.lowercase(java.util.Locale.ROOT)) {
                            "sinav_taktigi", "taktik" -> Color(0xFFFEF3C7).copy(alpha = 0.45f)
                            "tavsiye", "rehberlik", "sinav_tavsiyesi", "sınav_tavsiyesi" -> Color(0xFFE0F2FE).copy(alpha = 0.45f)
                            else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clickable { selectedSnippetIndex = index },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = cardBgColor
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = topicTitle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = parseMathText(snippet.content),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal,
                                    lineHeight = 14.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showReportDialogForSnippetId != null) {
        ReportSnippetDialog(
            snippetId = showReportDialogForSnippetId!!,
            viewModel = viewModel,
            onDismiss = { showReportDialogForSnippetId = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun KnownSnippetsScreen(
    viewModel: YksViewModel,
    onBack: () -> Unit
) {
    val topics by viewModel.hapTopics.collectAsState()
    val interactions by viewModel.allSnippetInteractions.collectAsState()
    val followedSubjects by viewModel.allFollowedSubjects.collectAsState()

    var allSnippets by remember { mutableStateOf<List<com.omerfaruk.ykstakip.data.SupabaseSnippet>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (topics.isEmpty()) {
            viewModel.fetchHapTopics()
        }
        isLoading = true
        try {
            allSnippets = com.omerfaruk.ykstakip.data.SupabaseRepository.getAllSnippets()
        } catch (e: Exception) {
            android.util.Log.e("KnownSnippets", "Failed to fetch snippets", e)
        }
        isLoading = false
    }

    val knownSnippetIds = interactions.filter { it.isKnown }.map { it.snippetId }.toSet()
    val knownSnippets = allSnippets.filter { it.id in knownSnippetIds }

    var showReportDialogForSnippetId by remember { mutableStateOf<String?>(null) }
    var selectedSnippetIndex by remember { mutableStateOf<Int?>(null) }

    if (selectedSnippetIndex != null) {
        val pagerState = androidx.compose.foundation.pager.rememberPagerState(
            initialPage = selectedSnippetIndex!!,
            pageCount = { knownSnippets.size }
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Bilinen Haplar Akışı", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = { selectedSnippetIndex = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kapat"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            androidx.compose.foundation.pager.VerticalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) { page ->
                val snippet = knownSnippets[page]
                val topic = topics.find { it.id == snippet.topicId }

                val isDark = androidx.compose.foundation.isSystemInDarkTheme()
                val slideBgColor = when (snippet.type.lowercase(java.util.Locale.ROOT)) {
                    "sinav_taktigi", "taktik" -> {
                        if (isDark) Color(0xFF2E2614) else Color(0xFFFEF9E7)
                    }
                    "tavsiye", "rehberlik", "sinav_tavsiyesi", "sınav_tavsiyesi" -> {
                        if (isDark) Color(0xFF142436) else Color(0xFFF0F7FF)
                    }
                    else -> {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(slideBgColor)
                        .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val isStarred = followedSubjects.any { it.subjectName == topic?.subjectName && it.isFollowed }
                    SnippetCard(
                        snippet = snippet,
                        topic = topic,
                        isStarred = isStarred,
                        onStarClick = { topic?.let { viewModel.toggleSubjectFollow(it.subjectName) } },
                        onSubjectClick = {},
                        contentVerticalPadding = 55.dp,
                        titleTopPadding = 130.dp,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Dikey Buton Kolonu (Beğen, Biliyorum, Kaydet, Bildir)
                    val interaction = interactions.find { it.snippetId == snippet.id }
                    val isLiked = interaction?.isLiked == true
                    val isKnown = interaction?.isKnown == true
                    val isSaved = interaction?.isSaved == true

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .width(56.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        color = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        viewModel.toggleSnippetLike(snippet.id, snippet.topicId, topic?.subjectName ?: "Genel")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Beğen",
                                    tint = if (isLiked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${snippet.likesCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        color = if (isKnown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        viewModel.toggleSnippetKnown(snippet.id, snippet.topicId, topic?.subjectName ?: "Genel")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Biliyorum",
                                    tint = if (isKnown) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${snippet.knowsCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        color = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        viewModel.toggleSnippetSaved(snippet.id, snippet.topicId, topic?.subjectName ?: "Genel")
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "Kaydet",
                                    tint = if (isSaved) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${snippet.savesCount}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { showReportDialogForSnippetId = snippet.id },
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                                    shape = CircleShape
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Report,
                                contentDescription = "Bildir",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Bilinen Haplar", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            if (isLoading && allSnippets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (knownSnippets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("✅", fontSize = 48.sp)
                        Text(
                            text = "Henüz 'biliyorum' olarak işaretlenmiş hap bilgi bulunmuyor.",
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    items(knownSnippets.size) { index ->
                        val snippet = knownSnippets[index]
                        val topic = topics.find { it.id == snippet.topicId }
                        val topicTitle = topic?.title ?: "Genel"

                        val cardBgColor = when (snippet.type.lowercase(java.util.Locale.ROOT)) {
                            "sinav_taktigi", "taktik" -> Color(0xFFFEF3C7).copy(alpha = 0.45f)
                            "tavsiye", "rehberlik", "sinav_tavsiyesi", "sınav_tavsiyesi" -> Color(0xFFE0F2FE).copy(alpha = 0.45f)
                            else -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clickable { selectedSnippetIndex = index },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = cardBgColor
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = topicTitle,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = parseMathText(snippet.content),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Normal,
                                    lineHeight = 14.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showReportDialogForSnippetId != null) {
        ReportSnippetDialog(
            snippetId = showReportDialogForSnippetId!!,
            viewModel = viewModel,
            onDismiss = { showReportDialogForSnippetId = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportSnippetDialog(
    snippetId: String,
    viewModel: YksViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var reason by remember { mutableStateOf("Hatalı Bilgi") }
    var explanation by remember { mutableStateOf("") }
    var isReporting by remember { mutableStateOf(false) }
    
    val reasons = listOf("Hatalı Bilgi", "Yazım/Dil Bilgisi Hatası", "Matematiksel Sembol Hatası", "Diğer")
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hata Rapor Et", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Bu hap bilgide bir hata olduğunu düşünüyorsanız lütfen bildirin.", fontSize = 14.sp)
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Neden: $reason")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        reasons.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r) },
                                onClick = {
                                    reason = r
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = explanation,
                    onValueChange = { explanation = it },
                    label = { Text("Açıklama (İsteğe bağlı)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                enabled = !isReporting,
                onClick = {
                    isReporting = true
                    viewModel.reportSnippet(snippetId, reason, explanation) { success ->
                        isReporting = false
                        onDismiss()
                        if (success) {
                            Toast.makeText(context, "Hata bildiriminiz gönderildi. Teşekkürler!", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Gönderilemedi, internet bağlantınızı kontrol edin.", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            ) {
                if (isReporting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Gönder")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

enum class QuestionFilter {
    TODAY, WEEKLY, MONTHLY, ALL_TIME
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically, 
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Text(text = label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
    }
}

@Composable
fun QuestionStatisticsGraph(
    results: List<com.omerfaruk.ykstakip.data.local.QuestionLogEntity>
) {
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 10.sp, color = Color.Gray)
    val formatter = remember {
        java.time.format.DateTimeFormatter.ofPattern("d MMM", java.util.Locale("tr"))
    }
    
    val sorted = remember(results) {
        results.groupBy { log ->
            val instant = java.time.Instant.ofEpochMilli(log.date)
            java.time.LocalDate.ofInstant(instant, java.time.ZoneId.systemDefault())
        }.map { (date, logs) ->
            val totalCorrect = logs.sumOf { it.correctCount }
            val totalWrong = logs.sumOf { it.wrongCount }
            val dayMillis = date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            com.omerfaruk.ykstakip.data.local.QuestionLogEntity(
                id = 0,
                date = dayMillis,
                subjectName = "",
                topicTitle = "",
                correctCount = totalCorrect,
                wrongCount = totalWrong
            )
        }.sortedBy { it.date }
    }
    
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp, vertical = 20.dp)
    ) {
        val width = size.width
        val labelSpace = 20.dp.toPx()
        val graphHeight = size.height - labelSpace 
        
        if (sorted.isEmpty()) {
            drawText(
                textMeasurer = textMeasurer,
                text = "Grafik için veri bulunamadı",
                style = TextStyle(fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold),
                topLeft = Offset(width / 2 - 60.dp.toPx(), graphHeight / 2)
            )
            return@Canvas
        }
        
        val maxVal = sorted.maxOfOrNull { it.correctCount + it.wrongCount }?.coerceAtLeast(10) ?: 10
        val maxValFloat = maxVal.toFloat()
        
        // Y-axis labels
        val yLabels = listOf(0, maxVal / 4, maxVal / 2, maxVal * 3 / 4, maxVal)
        yLabels.distinct().forEach { label ->
            val y = graphHeight - (label / maxValFloat * graphHeight)
            drawText(
                textMeasurer = textMeasurer,
                text = label.toString(),
                style = labelStyle,
                topLeft = Offset(-35.dp.toPx(), y - 7.dp.toPx())
            )
            drawLine(
                color = Color.LightGray.copy(alpha = 0.3f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        val xStep = if (sorted.size > 1) width / (sorted.size - 1) else width
        val pathTotal = Path()
        val pathCorrect = Path()
        val pathWrong = Path()

        val blueColor = Color(0xFF2196F3)
        val greenColor = Color(0xFF4CAF50)
        val redColor = Color(0xFFF44336)

        sorted.forEachIndexed { index, result ->
            val x = index * xStep
            
            val totalY = graphHeight - ((result.correctCount + result.wrongCount) / maxValFloat * graphHeight)
            val correctY = graphHeight - (result.correctCount / maxValFloat * graphHeight)
            val wrongY = graphHeight - (result.wrongCount / maxValFloat * graphHeight)
            
            if (index == 0) {
                pathTotal.moveTo(x, totalY)
                pathCorrect.moveTo(x, correctY)
                pathWrong.moveTo(x, wrongY)
            } else {
                pathTotal.lineTo(x, totalY)
                pathCorrect.lineTo(x, correctY)
                pathWrong.lineTo(x, wrongY)
            }
            
            // Draw points
            drawCircle(color = blueColor, radius = 4.dp.toPx(), center = Offset(x, totalY))
            drawCircle(color = greenColor, radius = 4.dp.toPx(), center = Offset(x, correctY))
            drawCircle(color = redColor, radius = 4.dp.toPx(), center = Offset(x, wrongY))

            // X-axis date label
            val dateStr = java.time.LocalDate.ofInstant(
                java.time.Instant.ofEpochMilli(result.date),
                java.time.ZoneId.systemDefault()
            ).format(formatter)

            val textLayoutResult = textMeasurer.measure(
                text = dateStr,
                style = labelStyle
            )
            val textWidth = textLayoutResult.size.width
            val shouldDrawLabel = sorted.size <= 7 || index == 0 || index == sorted.size - 1 || index == sorted.size / 2
            if (shouldDrawLabel) {
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = Offset(x - textWidth / 2, graphHeight + 4.dp.toPx())
                )
            }
        }

        if (sorted.size > 1) {
            drawPath(
                path = pathTotal,
                color = blueColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )
            drawPath(
                path = pathCorrect,
                color = greenColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )
            drawPath(
                path = pathWrong,
                color = redColor,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun EditQuestionDialog(
    log: com.omerfaruk.ykstakip.data.local.QuestionLogEntity,
    onDismiss: () -> Unit,
    onSave: (Int, Int) -> Unit
) {
    var correctStr by remember { mutableStateOf(log.correctCount.toString()) }
    var wrongStr by remember { mutableStateOf(log.wrongCount.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kaydı Düzenle", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("${log.subjectName} - ${log.topicTitle}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                
                OutlinedTextField(
                    value = correctStr,
                    onValueChange = { correctStr = it },
                    label = { Text("Doğru Sayısı") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = wrongStr,
                    onValueChange = { wrongStr = it },
                    label = { Text("Yanlış Sayısı") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val correct = correctStr.toIntOrNull() ?: 0
                    val wrong = wrongStr.toIntOrNull() ?: 0
                    onSave(correct, wrong)
                }
            ) {
                Text("Kaydet", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Composable
fun QuestionLogItem(
    log: com.omerfaruk.ykstakip.data.local.QuestionLogEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val formatter = remember {
        java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", java.util.Locale("tr"))
    }
    val dateStr = java.time.LocalDateTime.ofInstant(
        java.time.Instant.ofEpochMilli(log.date),
        java.time.ZoneId.systemDefault()
    ).format(formatter)

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(log.subjectName, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                Text(log.topicTitle, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("${log.correctCount} Doğru", fontSize = 12.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.SemiBold)
                    Text("${log.wrongCount} Yanlış", fontSize = 12.sp, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(dateStr, fontSize = 10.sp, color = Color.Gray)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Düzenle", tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionTrackingScreen(
    viewModel: YksViewModel,
    onBack: () -> Unit
) {
    val allQuestionLogs by viewModel.allQuestionLogs.collectAsState()
    val allTopics by viewModel.allTopics.collectAsState()
    val userInfo by viewModel.userInfo.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(QuestionFilter.ALL_TIME) }
    var editingLog by remember { mutableStateOf<com.omerfaruk.ykstakip.data.local.QuestionLogEntity?>(null) }
    var deletingLog by remember { mutableStateOf<com.omerfaruk.ykstakip.data.local.QuestionLogEntity?>(null) }

    var filterSubject by remember { mutableStateOf("Tümü") }
    var filterTopic by remember { mutableStateOf("Tümü") }

    val allSubjects = remember(allTopics) {
        listOf("Tümü") + allTopics.map { it.subjectName }.distinct().sorted()
    }

    val allTopicsForSubject = remember(allTopics, filterSubject) {
        if (filterSubject == "Tümü") listOf("Tümü")
        else listOf("Tümü") + allTopics.filter { it.subjectName == filterSubject }.map { it.title }.distinct().sorted()
    }

    LaunchedEffect(filterSubject) {
        filterTopic = "Tümü"
    }

    // Calculate dates
    val now = System.currentTimeMillis()
    val calendar = java.util.Calendar.getInstance()
    
    // Daily start
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
    calendar.set(java.util.Calendar.MINUTE, 0)
    calendar.set(java.util.Calendar.SECOND, 0)
    calendar.set(java.util.Calendar.MILLISECOND, 0)
    val dayStart = calendar.timeInMillis

    // Weekly start (7 days ago)
    val weekStart = now - (7L * 24 * 60 * 60 * 1000)

    // Monthly start (30 days ago)
    val monthStart = now - (30L * 24 * 60 * 60 * 1000)

    // Stats calculations for TabRow badges (pre-filtering by time ONLY)
    val totalAllTime = allQuestionLogs.sumOf { it.correctCount + it.wrongCount }
    val dailyLogsAll = allQuestionLogs.filter { it.date >= dayStart }
    val totalDaily = dailyLogsAll.sumOf { it.correctCount + it.wrongCount }
    val weeklyLogsAll = allQuestionLogs.filter { it.date >= weekStart }
    val totalWeekly = weeklyLogsAll.sumOf { it.correctCount + it.wrongCount }
    val monthlyLogsAll = allQuestionLogs.filter { it.date >= monthStart }
    val totalMonthly = monthlyLogsAll.sumOf { it.correctCount + it.wrongCount }

    // Fully Filtered Logs for stats card, graph, and list
    val filteredLogs = remember(allQuestionLogs, selectedFilter, filterSubject, filterTopic, dayStart, weekStart, monthStart) {
        val timeFiltered = when (selectedFilter) {
            QuestionFilter.TODAY -> allQuestionLogs.filter { it.date >= dayStart }
            QuestionFilter.WEEKLY -> allQuestionLogs.filter { it.date >= weekStart }
            QuestionFilter.MONTHLY -> allQuestionLogs.filter { it.date >= monthStart }
            QuestionFilter.ALL_TIME -> allQuestionLogs
        }
        timeFiltered.filter { log ->
            val matchSubject = filterSubject == "Tümü" || log.subjectName == filterSubject
            val matchTopic = filterTopic == "Tümü" || log.topicTitle == filterTopic
            matchSubject && matchTopic
        }
    }

    val currentTotal = filteredLogs.sumOf { it.correctCount + it.wrongCount }
    val currentCorrect = filteredLogs.sumOf { it.correctCount }
    val currentWrong = filteredLogs.sumOf { it.wrongCount }

    val filterTitle = when (selectedFilter) {
        QuestionFilter.TODAY -> "Bugün"
        QuestionFilter.WEEKLY -> "Son 7 Gün"
        QuestionFilter.MONTHLY -> "Son 30 Gün"
        QuestionFilter.ALL_TIME -> "Tüm Zamanlar"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Soru Sayısı Takibi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Soru Ekle")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Stats TabRow
            TabRow(
                selectedTabIndex = selectedFilter.ordinal,
                containerColor = Color.Transparent,
                divider = {},
                indicator = { tabPositions ->
                    Box(
                        Modifier
                            .tabIndicatorOffset(tabPositions[selectedFilter.ordinal])
                            .height(4.dp)
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                },
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                QuestionFilter.values().forEach { filter ->
                    val (label, count) = when (filter) {
                        QuestionFilter.TODAY -> "Bugün" to totalDaily
                        QuestionFilter.WEEKLY -> "7 Gün" to totalWeekly
                        QuestionFilter.MONTHLY -> "30 Gün" to totalMonthly
                        QuestionFilter.ALL_TIME -> "Tümü" to totalAllTime
                    }
                    Tab(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        text = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = label,
                                    fontWeight = if (selectedFilter == filter) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "$count",
                                    fontSize = 11.sp,
                                    color = if (selectedFilter == filter) MaterialTheme.colorScheme.primary else Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    )
                }
            }

            // Compact Stats Summary Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                ),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Toplam", fontSize = 11.sp, color = Color.Gray)
                        Text(text = "$currentTotal", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.Gray.copy(alpha = 0.3f)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Doğru", fontSize = 11.sp, color = Color.Gray)
                        Text(text = "$currentCorrect", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32))
                    }
                    Box(modifier = Modifier.width(1.dp).height(24.dp).background(Color.Gray.copy(alpha = 0.3f)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Yanlış", fontSize = 11.sp, color = Color.Gray)
                        Text(text = "$currentWrong", fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            // Dropdown Selectors
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Ders Filtresi
                var subjectExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedCard(
                        onClick = { subjectExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Ders", fontSize = 10.sp, color = Color.Gray)
                                Text(filterSubject, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(
                        expanded = subjectExpanded,
                        onDismissRequest = { subjectExpanded = false }
                    ) {
                        allSubjects.forEach { sub ->
                            DropdownMenuItem(
                                text = { Text(sub) },
                                onClick = {
                                    filterSubject = sub
                                    subjectExpanded = false
                                }
                            )
                        }
                    }
                }

                // Konu Filtresi
                var topicExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedCard(
                        onClick = { topicExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = filterSubject != "Tümü"
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Konu", fontSize = 10.sp, color = Color.Gray)
                                Text(filterTopic, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(
                        expanded = topicExpanded,
                        onDismissRequest = { topicExpanded = false }
                    ) {
                        allTopicsForSubject.forEach { top ->
                            DropdownMenuItem(
                                text = { Text(top) },
                                onClick = {
                                    filterTopic = top
                                    topicExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Statistics Graph Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Graph Legend
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        LegendItem("Toplam", Color(0xFF2196F3))
                        LegendItem("Doğru", Color(0xFF4CAF50))
                        LegendItem("Yanlış", Color(0xFFF44336))
                    }
                    Box(modifier = Modifier.fillMaxSize()) {
                        QuestionStatisticsGraph(results = filteredLogs)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Soru Günlüğü",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            if (filteredLogs.isEmpty()) {
                Text(
                    text = "Filtrelere uygun kayıt bulunamadı.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                filteredLogs.sortedByDescending { it.date }.forEach { log ->
                    QuestionLogItem(
                        log = log,
                        onEdit = { editingLog = log },
                        onDelete = { deletingLog = log }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        if (showAddDialog) {
            AddQuestionDialog(
                allTopics = allTopics,
                userMajor = userInfo?.major ?: "Sayısal",
                onDismiss = { showAddDialog = false },
                onSave = { subject, topic, correct, wrong ->
                    viewModel.addQuestionLog(subject, topic, correct, wrong)
                    showAddDialog = false
                }
            )
        }

        if (deletingLog != null) {
            AlertDialog(
                onDismissRequest = { deletingLog = null },
                title = { Text("Kaydı Sil", fontWeight = FontWeight.Bold) },
                text = { Text("Bu soru kaydını silmek istediğinize emin misiniz? Bu işlem geri alınamaz.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            deletingLog?.let { viewModel.deleteQuestionLog(it) }
                            deletingLog = null
                        }
                    ) {
                        Text("Sil", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { deletingLog = null }) {
                        Text("İptal")
                    }
                }
            )
        }

        if (editingLog != null) {
            EditQuestionDialog(
                log = editingLog!!,
                onDismiss = { editingLog = null },
                onSave = { correct, wrong ->
                    editingLog?.let { viewModel.updateQuestionLog(it, correct, wrong) }
                    editingLog = null
                }
            )
        }
    }
}
